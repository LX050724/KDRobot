package com.company.KDRobot.function.sc;

import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.notice.groupmember.increase.EventNoticeGroupMemberIncrease;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import com.company.KDRobot.function.Get;
import com.company.KDRobot.function.TimeOutTimer.TimeOutCallBack;
import com.company.KDRobot.function.TimeOutTimer.TimeOutTimer;

import java.sql.Statement;
import java.util.List;
import java.util.Vector;

public class SuperCommand implements TimeOutCallBack {
    private BlackListDataBase db;
    private Long Admin;
    private Long GroupID;
    private TimeOutTimer timeOutTimer;

    public SuperCommand(Statement stmt, Long Group, Long Admin) {
        timeOutTimer = new TimeOutTimer();
        db = new BlackListDataBase(stmt);
        this.Admin = Admin;
        this.GroupID = Group;
    }

    private void peocess_bl(EventGroupMessage event, String[] cmd) {
        if (cmd.length < 3) return;
        switch (cmd[2]) {
            case "ls": {
                event.respond(db.StringBlackList());
                break;
            }
            case "rm": {
                String ID = db.RemoveBlackList(cmd[3]);
                if (ID != null) event.respond(ID + "成功移除黑名单");
                else event.respond("移除失败,有可能是没有添加过或拼写错误");
                break;
            }
            case "check": {
                MessageBuilder msg = new MessageBuilder();
                Long GroupID = event.getGroupId();
                IcqHttpApi api = event.getHttpApi();
                Vector<Long> Blcaklist = db.ListBlackList();
                List<RGroupMemberInfo> GroupMemberList = api.getGroupMemberList(event.getGroupId()).data;
                if (GroupMemberList != null && Blcaklist != null) {
                    for (RGroupMemberInfo Info : GroupMemberList) {
                        if (Blcaklist.contains(Info.getUserId())) {
                            api.setGroupKick(GroupID, Info.getUserId());
                            msg.add(Info.getUserId()).add(",\n");
                        }
                    }
                    msg.add("踢出以上");
                    event.respond(msg.toString());
                } else event.respond("错误");
                break;
            }
            default: {
                IcqHttpApi api = event.getHttpApi();
                Long ID = db.AddBlackList(cmd[2]);
                Long Group = event.getGroupId();
                MessageBuilder builder = new MessageBuilder();
                if (ID != null) {
                    builder.add(ID).add(" 成功添加至黑名单");
                    if (cmd.length >= 4 && cmd[3].equals("up")) {
                        Long OPT = db.GetOPT(ID);
                        if (OPT != null) {
                            if (!Get.permissions(api, Group, OPT, Admin)) {
                                builder.add("，邀请者 ").add(new ComponentAt(OPT)).add(" 请在60分钟内发送'bot verify <你的QQ号>'验证身份");
                                timeOutTimer.Add(OPT.toString(), 3600, api, this);
                            }
                        } else builder.add("，未查询到邀请者");
                    }
                } else builder.add("添加失败,有可能是已经添加过或拼写错误");
                event.respond(builder.toString());
                api.setGroupKick(event.getGroupId(), ID);
            }
        }
    }

    private void process_shutup(EventGroupMessage event, String[] cmd) {
        if (cmd.length < 4) return;
        Long Id = Get.At2Long(cmd[2]);
        Long time = Get.Str2Time(cmd[3]);

        if (Id == null || time == null) {
            event.respond("禁言失败,输入有误");
        }

        event.getHttpApi().setGroupBan(event.getGroupId(), Id, time);
        String name = Get.ID2Name(event.getHttpApi(), event.getGroupId(), Id);
        if (time == 0) event.respond(name + "解禁");
        else event.respond(String.format("%s禁言%d秒", name, time));
    }

    private void process_sql(EventGroupMessage event, String[] cmd) {
        if (cmd.length < 4) return;
        String SQL = event.getMessage().substring(7);
        if (SQL.length() < 3) return;

        String r;
        if (cmd[2].equals("w"))
            r = db.SQL_Eval(SQL.substring(2), true);
        else if (cmd[2].equals("r"))
            r = db.SQL_Eval(SQL.substring(2), false);
        else {
            event.respond("错误，指定操作方向r、w");
            return;
        }
        event.getBot().getLogger().log("群'" + event.getGroupId() + "'\t管理员'" +
                event.getSenderId() + "'\n操作:\n" + SQL.substring(2) + "\n返回:\n" + r);
        event.respond(r);
    }

    public void process(EventGroupMessage event, String[] cmd) {
        if (cmd.length < 2) return;
        switch (cmd[1]) {
            case "bl":
                peocess_bl(event, cmd);
                break;
            case "shutup":
                process_shutup(event, cmd);
                break;
            case "sql":
                if (Admin != null && event.getSenderId().equals(Admin))
                    process_sql(event, cmd);
                else
                    event.respond("仅机器人管理员可操作数据库");
                break;
            default:
                event.respond("命令错误");
        }
    }

    public boolean chick(EventNoticeGroupMemberIncrease event, Long ID) {
        if (db.CheckOne(ID.toString())) {
            IcqHttpApi api = event.getHttpApi();
            api.setGroupKick(event.getGroupId(), ID);
            api.sendGroupMsg(event.getGroupId(), ID.toString() + "是黑名单成员!");
            return false;
        }
        return true;
    }

    public boolean Addbl(Long ID) {
        return db.AddBlackList(ID) != null;
    }

    public void verify(EventGroupMessage event, String[] cmd) {
        Long Sender = event.getSenderId();
        if (cmd[2].equals(Sender.toString()) && timeOutTimer.delete(cmd[2]))
            event.respond("验证成功");
    }

    @Override
    public void timeout(String Key, IcqHttpApi api) {
        Long ID = Get.At2Long(Key);
        Addbl(ID);
        api.setGroupKick(GroupID, ID);
        api.sendGroupMsg(GroupID, Key + "验证超时，添加BlackList踢出!");
    }
}
