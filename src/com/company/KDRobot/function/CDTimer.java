package com.company.KDRobot.function;

import cc.moecraft.logger.HyLogger;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CDTimer extends TimerTask {
    private Timer timer;
    private HyLogger logger;

    private HashMap<String, Pair<Long, Long>> CDList;

    public CDTimer(HyLogger logger) {
        this.logger = logger;
        CDList = new HashMap<>();
        timer = new Timer();
        timer.schedule(this, 1000, 1000);
    }

    public void AddCD(String name, Long time) {
        CDList.putIfAbsent(name, new Pair<>(time, 0L));
    }

    public Long GetLastTime(String name) {
        return CDList.get(name).getValue();
    }

    public boolean CD(String name) {
        Pair<Long, Long> t = CDList.get(name);
        if (t.getValue() == 0L) {
            CDList.replace(name, new Pair<>(t.getKey(), t.getKey()));
            return true;
        }
        logger.log(name + " CD Time last " + t.getValue());
        return false;
    }

    @Override
    public void run() {
        Map<String, Pair<Long, Long>> map = CDList;
        for (Map.Entry<String, Pair<Long, Long>> entry : map.entrySet()) {
            Long time = entry.getValue().getValue();
            if (time > 0) time--;
            CDList.replace(entry.getKey(), new Pair<>(entry.getValue().getKey(), time));
        }
    }
}
