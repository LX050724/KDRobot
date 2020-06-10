package com.company.KDRobot.function.MessageBord;

import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.icq.sender.returndata.ReturnData;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cc.moecraft.logger.HyLogger;
import com.company.KDRobot.KDRobotCfg;
import com.company.KDRobot.function.CDTimer;
import com.company.KDRobot.function.Get;

import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MessageBord {

    private static class TimeOut extends TimerTask {
        private Timer timer;
        private MessageBord p;
        private int time;
        private boolean running;
        private IcqHttpApi api;
        private Long GroupID;

        public TimeOut(MessageBord Parent) {
            p = Parent;
            running = false;
            timer = new Timer();
            timer.schedule(this, 1000, 1000);
        }

        public void start(int time, Long GroupID, IcqHttpApi api) {
            this.api = api;
            this.GroupID = GroupID;
            this.time = time;
            running = true;
        }

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            if (time > 0) time--;
            if (time == 0 && running) {
                p.timeout(api, GroupID);
                running = false;
            }
        }
    }

    private MessageBordDataBase db;
    private CDTimer timer;
    private TimeOut timeOut;
    private Long Admin;

    private MessageBordDataBase.Message tmpmsg;

    public MessageBord(Statement stmt, Long Admin, HyLogger logger) {
        tmpmsg = null;
        this.Admin = Admin;
        db = new MessageBordDataBase(stmt);
        timer = new CDTimer(logger);
        timeOut = new TimeOut(this);
        timer.AddCD("ls", 60L);
        timer.AddCD("look", 60L);
        timer.AddCD("push", 600L);
        timer.AddCD("del", 10L);
        timer.AddCD("help", 60L);
    }

    public void process(EventGroupMessage event, String[] cmd) {
        switch (cmd[2]) {
            case "ls":
                if (timer.CD(cmd[2]))
                    process_ls(event, cmd);
                break;
            case "look":
                if (cmd.length < 4) break;
                if (timer.CD(cmd[2]))
                    process_look(event, cmd);
                break;
            case "del":
                if (cmd.length < 4) break;
                if (timer.CD(cmd[2]))
                    process_del(event, cmd);
                break;
            case "push":
                if (tmpmsg != null || timer.CD(cmd[2]))
                    process_push(event, cmd);
                break;
            case "help":
                if (timer.CD(cmd[2]))
                    event.respond("bot msg 帮助:\n" +
                            "ls:列出帖子列表没有第三参数为查看帖子总数和页数,加第三参数为列出列表那一页\n" +
                            "look:查看帖子,第三参数为帖子ID\n" +
                            "del:删除帖子,第三参数为帖子ID\n" +
                            "push:发帖子,第三参数为标题,截断长度20字\n" +
                            "help:显示此帮助\n" +
                            "以上5条命令push的CD为10分钟,del为10秒,其他均为1分钟,所有人有效,只支持纯文本,发送图片导致截断消息后果自负");
                break;
            default:
                event.respond("命令错误\n输入'bot msg help'查询帮助");
        }
    }

    private void process_ls(EventGroupMessage event, String[] cmd) {
        Vector<MessageBordDataBase.Message> list = db.ListMsg();
        int size = list.size();
        int sumpage = size / 5 + (size % 5 == 0 ? 0 : 1);
        if (cmd.length == 3) {
            event.respond(String.format("共%d条,%d页", size, sumpage));
            return;
        }

        Long p = Get.At2Long(cmd[3]);
        if (p == null) {
            event.respond("页码输入错误");
            return;
        }

        StringBuilder builder = new StringBuilder();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        builder.append("第").append(p).append("页帖子列表:\n");
        int page = p.intValue() - 1;
        for (int i = page * 5; i < Math.min(page * 5 + 5, list.size()); ++i) {
            MessageBordDataBase.Message msg = list.elementAt(i);
            builder.append(String.format("ID:%d\n" +
                            "发帖者:%s\n" +
                            "日期:%s\n" +
                            "标题:%s",
                    msg.msgID,
                    Get.ID2Name(event.getHttpApi(), event.getGroupId(), msg.userID),
                    ft.format(new Date(msg.time)),
                    msg.title)).append('\n');
        }
        builder.append('共').append(sumpage).append('页');
        event.respond(builder.toString());
    }

    private void process_look(EventGroupMessage event, String[] cmd) {
        Long ID = Get.At2Long(cmd[3]);
        if (ID != null) {
            MessageBordDataBase.Message msg = db.find(ID);
            if (msg != null) {
                SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                event.respond(String.format("发帖者%s\n" +
                                "帖子ID:%d\n" +
                                "日期:%s\n" +
                                "标题:%s\n" +
                                "正文:\n%s",
                        Get.ID2Name(event.getHttpApi(), event.getGroupId(), msg.userID),
                        msg.msgID,
                        ft.format(new Date(msg.time)),
                        msg.title,
                        msg.body));
                return;
            }
        }
        event.respond("输入有误或未查询到该ID的帖子");
    }

    private void process_del(EventGroupMessage event, String[] cmd) {
        Long ID = Get.At2Long(cmd[3]);
        if (ID != null) {
            MessageBordDataBase.Message msg = db.find(ID);
            if (msg != null) {
                IcqHttpApi api = event.getHttpApi();
                ReturnData<RGroupMemberInfo> info = api.getGroupMemberInfo(event.getGroupId(), event.getSenderId());
                boolean permissions = info.getData().getRole().equals("owner") ||
                        info.getData().getRole().equals("admin") ||
                        (Admin != null && Admin.equals(event.getSenderId()));
                if (permissions || msg.userID.equals(event.getSenderId())) {
                    if (db.deleteMsg(ID)) event.respond("成功删除");
                    else event.respond("错误");
                } else event.respond("不是发帖者或者管理员,无权删除");
            } else event.respond("未找到此帖子");
        } else event.respond("输入有误");
    }

    private void process_push(EventGroupMessage event, String[] cmd) {
        if (cmd.length == 3) {
            event.respond("输入错误，请输入标题重试");
            return;
        }
        if (tmpmsg == null) {
            tmpmsg = new MessageBordDataBase.Message();
            tmpmsg.userID = event.getSenderId();
            tmpmsg.title = event.getMessage().substring("bot msg push ".length());
            if (tmpmsg.title.length() > 20)
                tmpmsg.title = tmpmsg.title.substring(0, 20);
            event.respond(new MessageBuilder()
                    .add(new ComponentAt(event.getSenderId()))
                    .add(" 请在5分钟以内再次使用该命令输入正文,截断长度100字")
                    .toString());
            timeOut.start(300, event.getGroupId(), event.getHttpApi());
        } else if (tmpmsg.userID.equals(event.getSenderId())) {
            tmpmsg.body = event.getMessage().substring("bot msg push ".length());
            if (tmpmsg.title.length() > 100)
                tmpmsg.body = tmpmsg.body.substring(0, 100);
            Long ID = db.pushMsg(tmpmsg);
            if (ID == null) event.respond("错误");
            else event.respond("发帖成功,帖子ID:" + ID);
            timeOut.stop();
            tmpmsg = null;
        }
    }

    private void timeout(IcqHttpApi api, Long groupID) {
        if (tmpmsg != null) {
            api.sendGroupMsg(groupID, new MessageBuilder()
                    .add(new ComponentAt(tmpmsg.userID))
                    .add("发帖超时已经取消")
                    .toString());
            tmpmsg = null;
        }
    }
}
