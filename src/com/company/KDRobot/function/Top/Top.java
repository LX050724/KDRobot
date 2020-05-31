package com.company.KDRobot.function.Top;

import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.returndata.ReturnData;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cc.moecraft.logger.HyLogger;
import com.company.KDRobot.function.CDTimer;
import com.company.KDRobot.function.Get;
import javafx.util.Pair;

import java.util.*;

public class Top {
    private static class AutoSave extends TimerTask {
        private Timer timer;
        private HyLogger logger;
        private TopDataBase p;

        public AutoSave(TopDataBase Parent, HyLogger logger) {
            p = Parent;
            this.logger = logger;
            timer = new Timer();
            timer.schedule(this, 60000, 60000);
        }

        @Override
        public void run() {
            p.Save();
        }
    }

    private TopDataBase db;
    private AutoSave autoSave;
    private CDTimer cdTimer;
    private Long Admin;

    public Top(String DataBasePath, HyLogger logger, Long Admin) {
        this.Admin = Admin;
        db = new TopDataBase(DataBasePath);
        autoSave = new AutoSave(db, logger);
        cdTimer = new CDTimer(logger);
        cdTimer.AddCD("top", 300L);
        cdTimer.AddCD("today", 300L);
        cdTimer.AddCD("check", 300L);
        cdTimer.AddCD("checktoday", 300L);
        cdTimer.AddCD("report", 120L);
        cdTimer.AddCD("report#", 10L);
        cdTimer.AddCD("help", 300L);
    }

    public void getMsg(Long ID) {
        db.Add(ID);
    }

    private String getTopTable(EventGroupMessage event, ArrayList<Pair<Long, Long>> list) {
        IcqHttpApi api = event.getHttpApi();
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (Pair<Long, Long> i : list) {
            if (i.getValue() == 0) continue;
            String name = Get.ID2Name(api, event.getGroupId(), i.getKey());
            stringBuilder.append(String.format("第%d名 %s---%d条", ++count, name, i.getValue()))
                    .append('\n');
            if (count == 10) break;
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private void peocess_Top(EventGroupMessage event) {
        event.respond("水群总排行：\n" + getTopTable(event, db.getTop()));
    }

    private void process_TopToday(EventGroupMessage event) {
        event.respond("今日水群排行：\n" + getTopTable(event, db.getTopTodat()));
    }

    private void process_chick(EventGroupMessage event, String IDStr) {
        Long ID = Get.At2Long(IDStr);
        if (ID == null) {
            event.respond("输入有误");
            return;
        }
        int conut = 1;
        for (Pair<Long, Long> i : db.getTop()) {
            if (i.getKey().equals(ID)) {
                String name = Get.ID2Name(event.getHttpApi(), event.getGroupId(), i.getKey());
                event.respond(String.format("%s 总排行第%d名,共%d条", name, conut, i.getValue()));
            }
            ++conut;
        }
    }

    private void process_chicktoday(EventGroupMessage event, String IDStr) {
        Long ID = Get.At2Long(IDStr);
        if (ID == null) {
            event.respond("输入有误");
            return;
        }
        int conut = 1;
        for (Pair<Long, Long> i : db.getTopTodat()) {
            if (i.getKey().equals(ID)) {
                String name = Get.ID2Name(event.getHttpApi(), event.getGroupId(), i.getKey());
                event.respond(String.format("%s 今日排行第%d名,共%d条", name, conut, i.getValue()));
            }
            ++conut;
        }
    }

    private void process_report(EventGroupMessage event, String[] cmd) {
        Long ID = Get.At2Long(cmd[3]);

        IcqHttpApi api = event.getHttpApi();
        ReturnData<RGroupMemberInfo> info = api.getGroupMemberInfo(event.getGroupId(), ID);
        boolean permissions = info.getData().getRole().equals("owner") || info.getData().getRole().equals("admin");

        if (permissions) {
            event.respond("你想造反?");
            return;
        }

        if (ID == null) {
            event.respond("输入有误");
            return;
        }
        TopDataBase.Member m = db.vote(event.getSenderId(), ID);
        if (m == null) {
            event.respond("输入有误或投票当天机会用尽,每人每天一次投票机会,不累计");
            return;
        }
        if (m.Kill >= 15) {
            event.getHttpApi().setGroupBan(event.getGroupId(), ID, 3600);
            event.respond(Get.ID2Name(event.getHttpApi(), event.getGroupId(), ID) + "经多次投诉禁言,大于15票禁言,每天0点自然减三票");
            if (Admin != null)
                event.getHttpApi().sendPrivateMsg(Admin, ID + " 被举报生效");
        }
        event.respond("收到反馈");
        event.getBot().getLogger().log(String.format("群'%d':'%d'投诉'%d'现有%d票",
                event.getGroupId(),
                event.getSenderId(),
                ID,
                m.Kill));
    }

    public void process(EventGroupMessage event, String[] cmd) {
        IcqHttpApi api = event.getHttpApi();
        ReturnData<RGroupMemberInfo> info = api.getGroupMemberInfo(event.getGroupId(), event.getSenderId());
        boolean permissions = info.getData().getRole().equals("owner") || info.getData().getRole().equals("admin");

        if (cmd.length < 3) {
            if (permissions || cdTimer.CD("top"))
                peocess_Top(event);
        } else switch (cmd[2]) {
            case "today":
                if (permissions || cdTimer.CD(cmd[2]))
                    process_TopToday(event);
                break;
            case "check":
                if (permissions || cdTimer.CD(cmd[2]))
                    process_chick(event, cmd.length == 3 ? event.getSenderId().toString() : cmd[3]);
                break;
            case "checktoday":
                if (permissions || cdTimer.CD(cmd[2]))
                    process_chicktoday(event, cmd.length == 3 ? event.getSenderId().toString() : cmd[3]);
                break;
            case "report":
                if (permissions || cdTimer.CD(cmd[2]))
                    process_report(event, cmd);
                else if (cdTimer.CD("report#"))
                    event.respond("剩余CD时间" + cdTimer.GetLastTime("report") + "秒,注意,请勿跟风投票,珍惜你的投票机会共同打造良好的交流环境");
                break;
            case "help":
                if (permissions || cdTimer.CD(cmd[2]))
                    event.respond("bot top 帮助:\n" +
                            "空:查询总水群排行\n" +
                            "today:查询当天水群排行\n" +
                            "check:查询某人的总排行,不加第四个值为查询自己\n" +
                            "checktoday:查询某人的今日排行,不加第四个值为查询自己\n" +
                            "report:投诉某人,累计数量到达一定程度的时候给予处分\n" +
                            "help:显示此帮助\n" +
                            "非管理员report的CD为2分钟,其他为5分钟");
                break;
            default:
                event.respond("命令错误\n输入'bot top help'查询帮助");
        }
    }
}
