package com.company.KDRobot.function.CDTimer;

import cc.moecraft.logger.HyLogger;

import java.util.Date;
import java.util.TreeMap;

public class CDTimer {
    private HyLogger logger;

    private TreeMap<String, Node> CDList;
    private boolean Enable;

    public CDTimer(HyLogger logger) {
        this.logger = logger;
        Enable = true;
        CDList = new TreeMap<>();
    }

    public void SetEnable(Boolean en) {
        Enable = en;
    }

    public void AddCD(String name, Long time) {
        CDList.put(name, new Node(time * 1000, null));
    }

    public Long GetLastTime(String name) {
        Node node = CDList.get(name);
        return (node.CD - (new Date().getTime() - node.time)) / 1000;
    }

    public boolean CD(String name) {
        if (!Enable) return true;
        Node node = CDList.get(name);
        if (node.time == null) {
            node.time = new Date().getTime();
            return true;
        }
        long lasttime = node.CD - (new Date().getTime() - node.time);
        if (lasttime > 0) {
            logger.log(name + " CD Time last " + (lasttime / 1000));
            return false;
        }
        node.time = new Date().getTime();
        return true;
    }

    public void Ready(String name) {
        CDList.get(name).time = null;
    }
}
