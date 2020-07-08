package com.company.KDRobot.function.MessageBord;

import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.logger.HyLogger;
import com.company.KDRobot.function.CDTimer.CDTimer;
import com.company.KDRobot.function.Get;
import com.company.KDRobot.function.TimeOutTimer.TimeOutCallBack;
import com.company.KDRobot.function.TimeOutTimer.TimeOutTimer;

import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

public class MessageBord implements TimeOutCallBack {

    private MessageBordDataBase db;
    private CDTimer timer;
    private TimeOutTimer timeOut;
    private Long Admin;
    private Long GroupID;

    private MessageBordDataBase.Message tmpmsg;

    public MessageBord(Statement stmt, Long Admin, HyLogger logger) {
        tmpmsg = null;
        this.Admin = Admin;
        db = new MessageBordDataBase(stmt);
        timer = new CDTimer(logger);
        timeOut = new TimeOutTimer();
        timer.AddCD("ls", 60L);
        timer.AddCD("look", 60L);
        timer.AddCD("push", 600L);
        timer.AddCD("del", 10L);
        timer.AddCD("help", 60L);
    }

    public void process(EventGroupMessage event, String[] cmd) {
        if (cmd.length < 1) return;
        String[] _cmd = Arrays.copyOfRange(cmd, 1, cmd.length);
        switch (cmd[0]) {
            case "ls":
                if (timer.CD(cmd[0]))
                    process_ls(event, _cmd);
                break;
            case "look":
                if (cmd.length >= 2 && timer.CD(cmd[0]))
                    process_look(event, _cmd);
                break;
            case "del":
                if (cmd.length >= 2 && timer.CD(cmd[0]))
                    process_del(event, _cmd);
                break;
            case "push":
                if (tmpmsg != null || timer.CD(cmd[0]))
                    process_push(event, _cmd);
                break;
            case "help":
                if (timer.CD(cmd[0]))
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
        if (cmd.length == 0) {
            event.respond(String.format("共%d条,%d页", size, sumpage));
            return;
        }

        Long p = Get.At2Long(cmd[0]);
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
        Long ID = Get.At2Long(cmd[0]);
        if (ID != null) {
            MessageBordDataBase.Message msg = db.find(ID.intValue());
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
        Long ID = Get.At2Long(cmd[0]);
        if (ID != null) {
            MessageBordDataBase.Message msg = db.find(ID.intValue());
            if (msg != null) {
                Boolean permissions = Get.permissions(event.getHttpApi(), event.getGroupId(), event.getSenderId(), Admin);
                permissions = (permissions == null) ? Boolean.FALSE : permissions;
                if (permissions || msg.userID.equals(event.getSenderId())) {
                    if (db.deleteMsg(ID.intValue())) event.respond("成功删除");
                    else event.respond("错误");
                } else event.respond("不是发帖者或者管理员,无权删除");
            } else event.respond("未找到此帖子");
        } else event.respond("输入有误");
    }

    private void process_push(EventGroupMessage event, String[] cmd) {
        if (cmd.length == 0) {
            event.respond("输入错误，请输入标题重试");
            return;
        }
        if (tmpmsg == null) {
            tmpmsg = new MessageBordDataBase.Message();
            tmpmsg.userID = event.getSenderId();
            tmpmsg.title = event.getMessage().substring(event.getMessage().indexOf("push") + 5);
            if (tmpmsg.title.length() > 20)
                tmpmsg.title = tmpmsg.title.substring(0, 20);
            event.respond(new MessageBuilder()
                    .add(new ComponentAt(event.getSenderId()))
                    .add(" 请在5分钟以内再次使用该命令输入正文,截断长度255字")
                    .toString());
            GroupID = event.getGroupId();
            timeOut.Add(event.getSenderId().toString(), 300, event.getHttpApi(), this);
        } else if (tmpmsg.userID.equals(event.getSenderId())) {
            tmpmsg.body = event.getMessage().substring(event.getMessage().indexOf("push") + 5);
            if (tmpmsg.body.length() > 255)
                tmpmsg.body = tmpmsg.body.substring(0, 255);
            Long ID = db.pushMsg(tmpmsg);
            if (ID == null) event.respond("错误");
            else event.respond("发帖成功,帖子ID:" + ID);
            timeOut.delete(event.getSenderId().toString());
            tmpmsg = null;
        }
    }

    @Override
    public void timeout(String Key, IcqHttpApi api) {
        if (tmpmsg != null) {
            api.sendGroupMsg(GroupID, new MessageBuilder()
                    .add(new ComponentAt(tmpmsg.userID))
                    .add("发帖超时已经取消")
                    .toString());
            tmpmsg = null;
        }
    }
}
