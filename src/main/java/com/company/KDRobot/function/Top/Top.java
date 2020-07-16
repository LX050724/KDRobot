package com.company.KDRobot.function.Top;

import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.returndata.ReturnData;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cc.moecraft.logger.HyLogger;
import com.company.KDRobot.function.CDTimer.CDTimer;
import com.company.KDRobot.function.Get;
import com.company.KDRobot.function.GroupConfig.Configurable;
import com.company.KDRobot.util.Pair;

import java.sql.Statement;
import java.util.*;

public class Top implements Configurable {
    private TopDataBase db;
    private CDTimer cdTimer;
    private Long Admin;
    private Long GroupID;
    public static final String[] attributes = {
            "top", "300",               //bot top CD时间
            "today", "300",             //bot top today CD时间
            "check", "300",             //bot top check CD时间
            "checktoday", "300",        //bot top checktoday CD时间
            "report", "60",             //bot top report CD时间
            "report#", "10",
            "tophelp", "300",           //bot top help CD时间
            "MAXfreq", "30",            //最大发言频率（估计）
            "FreqLimitEnable", "NO"     //启用发言频率检测，YES或NO
    };

    public Top(Statement stmt, Long Group, Long Admin, HyLogger logger) {
        this.Admin = Admin;
        this.GroupID = Group;
        db = new TopDataBase(stmt);
        cdTimer = new CDTimer(logger);
    }

    @Override
    public String[] GetAttributes() {
        return attributes;
    }

    @Override
    public boolean Config(String Variable, String Value) {
        try {
            switch (Variable) {
                case "MAXfreq": {
                    double freq = Double.parseDouble(Value);
                    if (freq < 0) return false;
                    db.setFreq(freq);
                    break;
                }
                case "FreqLimitEnable": {
                    if (Value.equals("YES"))
                        db.setFreqLimitEnable(true);
                    else if (Value.equals("NO"))
                        db.setFreqLimitEnable(false);
                    else return false;
                    break;
                }
                default: {
                    long time = Long.parseLong(Value);
                    if (time < 0) return false;
                    cdTimer.AddCD(Variable, time);
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    public void getMsg(EventGroupMessage event, boolean permissions) {
        if (db.Add(event.getSelfId(), event.getMessage(), permissions) &&
                Get.permissions(event.getHttpApi(), GroupID, event.getSelfId(), Admin)) {
            event.getHttpApi().setGroupBan(event.getGroupId(), event.getSenderId(), 900);
            event.getBot().getLogger().log(String.format("群%d,成员%d刷屏禁言",GroupID, event.getSenderId()));
            event.respond(Get.ID2Name(event.getHttpApi(), event.getGroupId(), event.getSenderId()) + "刷屏禁言");
        }
    }

    public void AddMember(Long ID, Long OperatorID) {
        db.AddMember(ID, OperatorID);
    }

    private String getTopTable(EventGroupMessage event, ArrayList<Pair<Long, Long>> list) {
        IcqHttpApi api = event.getHttpApi();
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (Pair<Long, Long> i : list) {
            if (i.first == 0) continue;
            String name = Get.ID2Name(api, event.getGroupId(), i.second);
            stringBuilder.append(String.format("第%d名 %s---%d条", ++count, name, i.first))
                    .append('\n');
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
        Pair<Long, Long> top = db.getCheck(ID);

        if (top == null) {
            event.respond("查无此人或输入有误");
            return;
        }

        String name = Get.ID2Name(event.getHttpApi(), event.getGroupId(), ID);
        event.respond(String.format("%s 总排行第%d名,共%d条", name, top.second, top.first));
    }

    private void process_chicktoday(EventGroupMessage event, String IDStr) {
        Long ID = Get.At2Long(IDStr);
        if (ID == null) {
            event.respond("输入有误");
            return;
        }
        Pair<Long, Long> top = db.getCheckToday(ID);

        if (top == null) {
            event.respond("查无此人或输入有误");
            return;
        }

        String name = Get.ID2Name(event.getHttpApi(), event.getGroupId(), ID);
        event.respond(String.format("%s 今日排行第%d名,共%d条", name, top.second, top.first));
    }

    private void process_report(EventGroupMessage event, String[] cmd) {
        if (cmd.length == 0) return;
        Long ID = Get.At2Long(cmd[0]);

        IcqHttpApi api = event.getHttpApi();
        if (ID == null) {
            event.respond("输入有误");
            return;
        }

        ReturnData<RGroupMemberInfo> info = api.getGroupMemberInfo(GroupID, ID);

        if (info.getData() == null) {
            event.respond("成员" + ID + "不存在");
            return;
        }

        Boolean permissions = Get.permissions(api, GroupID, ID, Admin);
        permissions = (permissions == null) ? Boolean.FALSE : permissions;

        if (permissions) {
            event.respond("你想造反?");
            return;
        }

        int kill = db.vote(event.getSenderId(), ID);
        if (kill == -1) {
            event.respond("输入有误或投票当天机会用尽,每人每天一次投票机会,不累计");
            return;
        }
        if (kill >= 15) {
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
                kill));
    }

    public void process(EventGroupMessage event, String[] cmd) {
        Boolean permissions = Get.permissions(event.getHttpApi(), event.getGroupId(), event.getSenderId(), Admin);
        permissions = (permissions == null) ? Boolean.FALSE : permissions;

        if (cmd.length == 0) {
            if (permissions || cdTimer.CD("top"))
                peocess_Top(event);
        } else switch (cmd[0]) {
            case "today":
                if (permissions || cdTimer.CD("today"))
                    process_TopToday(event);
                break;
            case "check":
                if (permissions || cdTimer.CD("check"))
                    process_chick(event, cmd.length == 1 ? event.getSenderId().toString() : cmd[1]);
                break;
            case "checktoday":
                if (permissions || cdTimer.CD("checktoday"))
                    process_chicktoday(event, cmd.length == 1 ? event.getSenderId().toString() : cmd[1]);
                break;
            case "report":
                if (permissions || cdTimer.CD("report"))
                    process_report(event, Arrays.copyOfRange(cmd, 1, cmd.length));
                else if (cdTimer.CD("report#"))
                    event.respond("剩余CD时间" + cdTimer.GetLastTime("report") + "秒,注意,请勿跟风投票,珍惜你的投票机会共同打造良好的交流环境");
                break;
            case "tophelp":
                if (permissions || cdTimer.CD("tophelp"))
                    event.respond("bot top 帮助:\n" +
                            "空:查询总水群排行\n" +
                            "today:查询当天水群排行\n" +
                            "check:查询某人的总排行,不加第四个值为查询自己\n" +
                            "checktoday:查询某人的今日排行,不加第四个值为查询自己\n" +
                            "report:投诉某人,累计数量到达一定程度的时候给予处分\n" +
                            "help:显示此帮助\n" +
                            "非管理员report的CD为1分钟,其他为5分钟");
                break;
            default:
                event.respond("命令错误\n输入'bot top help'查询帮助");
        }
    }

}
