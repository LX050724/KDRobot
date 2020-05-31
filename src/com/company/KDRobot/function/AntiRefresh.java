package com.company.KDRobot.function;

import cc.moecraft.icq.event.events.message.EventGroupMessage;

import java.util.HashMap;

public class AntiRefresh {
    private static class Unit {
        private int repeat;
        private String lastMeg;

        public Unit() {
            lastMeg = "";
            repeat = 0;
        }

        public boolean input(String msg) {
            if (msg.equals(lastMeg)) repeat++;
            else repeat = 0;
            lastMeg = msg;
            return (repeat < 10);
        }
    }

    private HashMap<Long, Unit> list;

    public AntiRefresh() {
        list = new HashMap<>();
    }

    public void process(EventGroupMessage event) {
        Unit p = list.get(event.getSenderId());
        if (p != null) {
            if (!p.input(event.getMessage())) {
                event.getHttpApi().setGroupBan(event.getGroupId(), event.getSenderId(), 1800);
                event.getBot().getLogger().log(event.getSenderId() + "刷屏禁言");
                event.respond(Get.ID2Name(event.getHttpApi(), event.getGroupId(), event.getSenderId()) + "刷屏禁言");
            }
        } else {
            list.put(event.getSenderId(), new Unit());
        }
    }
}
