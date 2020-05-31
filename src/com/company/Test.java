package com.company;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.message.EventPrivateMessage;
import com.company.KDRobot.function.TuringAPI;

public class Test extends IcqListener {
    private final TuringAPI turingAPI;
    private final long ID;
    private final boolean Group;

    public Test(long ID) {
        this(ID, false);
    }

    public Test(long ID, boolean Group) {
        turingAPI = new TuringAPI("f4f88216f44c4fbc84f3ae03cc355300");
        this.ID = ID;
        this.Group = Group;
    }

    @EventHandler
    public void onPMEvent(EventPrivateMessage event) {
        if (!Group) {
            Long ID = event.getSenderId();
            if (ID == this.ID && event.getMessage().startsWith("#")) {
                String r = turingAPI.machine(event.getMessage().substring(1));
                System.out.println(String.format("接到%d的图灵消息 %s;回复:%s", ID, event.getMessage(), r));
                event.respond("turing:" + r);
            }
        }
    }

    @EventHandler
    public void onEGEvent(EventGroupMessage event) {
        if (Group) {
            Long GroupID = event.getGroupId();
            Long SenderID = event.getSenderId();
            if (GroupID == this.ID && event.getMessage().startsWith("#")) {
                String r = turingAPI.machine(event.getMessage().substring(1));
                System.out.println(String.format("接到群%d,%d的图灵消息 %s;回复:%s", GroupID, SenderID, event.getMessage(), r));
//                event.respond("turing:" + r);
                event.getHttpApi().sendGroupMsg(GroupID, "turing:" + r);
            }
        }
    }
}
