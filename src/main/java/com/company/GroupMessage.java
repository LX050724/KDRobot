package com.company;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.notice.groupmember.decrease.EventNoticeGroupMemberKick;
import cc.moecraft.icq.event.events.notice.groupmember.decrease.EventNoticeGroupMemberLeave;
import cc.moecraft.icq.event.events.notice.groupmember.increase.EventNoticeGroupMemberIncrease;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;

import java.io.*;
import java.util.Date;

public class GroupMessage extends IcqListener {
    @EventHandler // 这个注解必须加, 用于反射时判断哪些方法是事件方法的, 不用 @Override
    public void onEGEvent(EventGroupMessage event) // 想监听什么事件就写在事件类名这里, 一个方法只能有一个事件参数
    {
        long GroupID = event.getGroupId();
        System.out.println(String.format("In Group %d, User %d:%s",
                GroupID, event.getSender().getId(), event.message));
        Grouptest(event);
    }

    @EventHandler
    public void onENGMKEvent(EventNoticeGroupMemberKick event) {
        IcqHttpApi api = event.getHttpApi();
        api.sendGroupMsg(event.getGroupId(), new MessageBuilder()
                .add(event.getUserId().toString() + "被踢了！")
                .toString()
        );
        System.out.println(String.format("群:%d, %d 被%d踢了", event.getGroupId(), event.getUserId(), event.getOperatorId()));
    }

    @EventHandler
    public void onENGMIEvent(EventNoticeGroupMemberIncrease event) {
        IcqHttpApi api = event.getHttpApi();
        api.sendGroupMsg(event.getGroupId(), new MessageBuilder()
                .add(new ComponentAt(event.getUserId()))
                .add("新人爆照")
                .toString());
        System.out.println(String.format("群:%d, %d 新成员", event.getGroupId(), event.getUserId()));
    }

    @EventHandler
    public void onENGMLEvent(EventNoticeGroupMemberLeave event) {
        IcqHttpApi api = event.getHttpApi();
        api.sendGroupMsg(event.getGroupId(), new MessageBuilder()
                .add(event.getUserId().toString() + "自己退群了！")
                .toString()
        );
        System.out.println(String.format("群:%d, %d 主动退群", event.getGroupId(), event.getUserId()));
    }

    private void Grouptest(EventGroupMessage event) {
        long GroupID = event.getGroupId();
        long ID = event.getSender().getId();
        WriteMsg(GroupID, ID, event.getMessage());
    }

    private void WriteMsg(long GroupID, long ID, String msg) {
        Date time = new Date();
        String log = time.toString() + ":" + msg + '\n';
        File d = new File("msg/" + GroupID);

        if (!d.exists()) d.mkdir();

        try {
            FileWriter f = new FileWriter(d.getAbsolutePath() + '/' + ID + ".txt", true);
            f.write(log);
            f.flush();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
