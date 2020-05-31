package com.company.KDRobot.function.sc;

import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.notice.groupmember.increase.EventNoticeGroupMemberIncrease;
import cc.moecraft.icq.sender.IcqHttpApi;
import com.company.KDRobot.function.Get;

public class SuperCommand {
    private BlackListDataBase db;

    public SuperCommand(String DataBasePath) {
        db = new BlackListDataBase(DataBasePath);
    }

    public void peocess_bl(EventGroupMessage event, String[] cmd) {
        switch (cmd[2]) {
            case "ls": {
                event.respond(db.ListBlackList());
                break;
            }
            case "rm": {
                String ID = db.RemoveBlackList(cmd[3]);
                if (ID != null) event.respond(ID + "成功移除黑名单");
                else event.respond("移除失败,有可能是没有添加过或拼写错误");
                break;
            }
            default: {
                String ID = db.AddBlackList(cmd[2]);
                if (ID != null) {
                    event.respond(ID + "成功添加至黑名单");
                    event.getHttpApi().setGroupKick(event.getGroupId(), Long.parseLong(ID));
                } else {
                    event.respond("添加失败,有可能是已经添加过或拼写错误");
                }
            }
        }
    }

    private void process_shutup(EventGroupMessage event, String[] cmd) {
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

    public void process(EventGroupMessage event, String[] cmd) {
        switch (cmd[1]) {
            case "bl":
                peocess_bl(event, cmd);
                break;
            case "shutup":
                process_shutup(event, cmd);
                break;
            default:
                event.respond("命令错误");
        }
    }

    public boolean chick(EventNoticeGroupMemberIncrease event, Long ID) {
        if (db.Check(ID.toString())) {
            IcqHttpApi api = event.getHttpApi();
            api.setGroupKick(event.getGroupId(), ID);
            api.sendGroupMsg(event.getGroupId(), ID.toString() + "是黑名单成员!");
            return false;
        }
        return true;
    }
}
