package com.company.KDRobot.function.sc;

import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.notice.groupmember.increase.EventNoticeGroupMemberIncrease;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import com.company.KDRobot.function.Get;
import com.company.KDRobot.function.GroupConfig.ConfigDataBase;
import com.company.KDRobot.function.TimeOutTimer.TimeOutCallBack;
import com.company.KDRobot.function.TimeOutTimer.TimeOutTimer;

import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class SuperCommand implements TimeOutCallBack {
    private BlackListDataBase db;
    private Long Admin;
    private Long GroupID;
    private TimeOutTimer timeOutTimer;
    private ConfigDataBase configDataBase;

    public SuperCommand(Statement stmt, ConfigDataBase configDataBase, Long Group, Long Admin) {
        this.configDataBase = configDataBase;
        timeOutTimer = new TimeOutTimer();
        db = new BlackListDataBase(stmt);
        this.Admin = Admin;
        this.GroupID = Group;
    }

    private void peocess_bl(EventGroupMessage event, String[] cmd) {
        if (cmd.length == 0) {
            event.respond("输入有误");
            return;
        }
        IcqHttpApi api = event.getHttpApi();
        switch (cmd[0]) {
            case "ls": {
                event.respond(db.StringBlackList());
                break;
            }
            case "rm": {
                if (cmd.length < 2) break;
                String ID = db.RemoveBlackList(cmd[1]);
                if (ID != null) event.respond(ID + "成功移除黑名单");
                else event.respond("移除失败,有可能是没有添加过或拼写错误");
                break;
            }
            case "check": {
                MessageBuilder msg = new MessageBuilder();
                Long GroupID = event.getGroupId();
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
            case "verify": {
                if (cmd.length < 2) break;
                Long Id = Get.At2Long(cmd[1]);
                if (Id != null) {
                    Boolean permissions = Get.permissions(api, GroupID, Id, Admin);
                    if (permissions == null) {
                        event.respond("没有该成员或输入有误");
                        break;
                    }
                    if (permissions) {
                        event.respond("对管理员无效");
                        break;
                    }
                    if (timeOutTimer.Add(Id.toString(), 3600, api, this)) {
                        MessageBuilder builder = new MessageBuilder();
                        builder.add(new ComponentAt(Id)).add(" 请在60分钟内发送'bot verify <你的QQ号>'验证身份");
                        event.respond(builder.toString());
                    } else event.respond("验证列表添加异常，可能是已经添加过");
                } else event.respond("输入有误");
                break;
            }
            case "celverify": {
                if (cmd.length < 2) break;
                Long Id = Get.At2Long(cmd[1]);
                if (Id != null) {
                    if (timeOutTimer.delete(Id.toString())) {
                        event.respond("取消成功");
                    } else event.respond("取消失败，" + Get.ID2Name(api, GroupID, Id) + " 不在列表中");
                } else event.respond("输入有误");
                break;
            }
            default: {
                Long Id = db.AddBlackList(cmd[0]);
                MessageBuilder builder = new MessageBuilder();
                if (Id != null) {
                    Boolean permissions = Get.permissions(api, GroupID, Id, Admin);
                    permissions = (permissions == null) ? Boolean.FALSE : permissions;
                    if (permissions) {
                        event.respond("对管理员无效");
                        break;
                    }
                    builder.add(Id).add(" 成功添加至黑名单");
                    if (cmd.length >= 2 && cmd[1].equals("up")) {
                        Long OPT = db.GetOPT(Id);
                        if (OPT != null) {
                            Boolean per = Get.permissions(api, GroupID, OPT, Admin);
                            if (per != null && !per) {
                                builder.add("，邀请者 ").add(new ComponentAt(OPT)).add(" 请在60分钟内发送'bot verify <你的QQ号>'验证身份");
                                timeOutTimer.Add(OPT.toString(), 3600, api, this);
                            }
                        } else builder.add("，未查询到邀请者");
                    }
                    api.setGroupKick(event.getGroupId(), Id);
                } else builder.add("添加失败,有可能是已经添加过或拼写错误");
                event.respond(builder.toString());
            }
        }
    }

    private void process_shutup(EventGroupMessage event, String[] cmd) {
        if (cmd.length < 2) return;
        Long Id = Get.At2Long(cmd[0]);
        Long time = Get.Str2Time(cmd[1]);

        if (Id == null || time == null) {
            event.respond("禁言失败,输入有误");
            return;
        }

        event.getHttpApi().setGroupBan(event.getGroupId(), Id, time);
        String name = Get.ID2Name(event.getHttpApi(), event.getGroupId(), Id);
        if (time == 0) event.respond(name + "解禁");
        else event.respond(String.format("%s禁言%d秒", name, time));
    }

    private void process_sql(EventGroupMessage event, String[] cmd) {
        if (cmd.length < 2) return;
        String msg = event.getMessage();
        String SQL = msg.substring(msg.indexOf(cmd[1]));
        if (SQL.length() < 3) return;

        String r;
        if (cmd[0].equals("w"))
            r = db.SQL_Eval(SQL, true);
        else if (cmd[0].equals("r"))
            r = db.SQL_Eval(SQL, false);
        else {
            event.respond("错误，指定操作方向r、w");
            return;
        }
        event.getBot().getLogger().log("群'" + event.getGroupId() + "'\t管理员'" +
                event.getSenderId() + "'\n操作:\n" + SQL + "\n返回:\n" + r);
        event.respond(r);
    }

    private void peocess_config(EventGroupMessage event, String[] cmd) {
        if (cmd.length < 2) return;

        switch (cmd[0]) {
            case "read":
                String Value = configDataBase.ReadConfig(cmd[1]);
                if (Value == null) {
                    event.respond("配置'" + cmd[1] + "'不存在");
                } else {
                    event.respond(cmd[1] + " = " + Value);
                }
                break;
            case "set":
                if (cmd.length >= 3) {
                    String value = event.getMessage();
                    value = value.substring(value.indexOf("set") + 3);
                    value = value.substring(value.indexOf(cmd[1]) + cmd[1].length() + 1);
                    if (configDataBase.SetConfig(cmd[1], value))
                        event.respond("set " + cmd[1] + " = " + value);
                    else event.respond("输入有误或配置'" + value + "'不存在");
                } else event.respond("输入有误");
                break;
            default:
                event.respond("输入有误");
        }
    }

    public void process(EventGroupMessage event, String[] cmd) {
        if (cmd.length < 1) return;
        String[] _cmd = Arrays.copyOfRange(cmd, 1, cmd.length);
        switch (cmd[0]) {
            case "bl":
                peocess_bl(event, _cmd);
                break;
            case "shutup":
                process_shutup(event, _cmd);
                break;
            case "cfg":
                peocess_config(event, _cmd);
                break;
            case "sql":
                if (Admin != null && event.getSenderId().equals(Admin))
                    process_sql(event, _cmd);
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
        if (cmd[0].equals(Sender.toString()) && timeOutTimer.delete(cmd[0]))
            event.respond("验证成功");
    }

    @Override
    public void timeout(String Key, IcqHttpApi api) {
        Long ID = Get.At2Long(Key);
        if (Addbl(ID)) api.sendGroupMsg(GroupID, Key + "验证超时，添加BlackList踢出!");
        else api.sendGroupMsg(GroupID, Key + "尝试踢出，但添加黑名单异常");
        api.setGroupKick(GroupID, ID);
    }
}
