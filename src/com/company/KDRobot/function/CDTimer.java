package com.company.KDRobot.function;

import cc.moecraft.logger.HyLogger;
import javafx.util.Pair;

import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class CDTimer extends TimerTask {
    private Timer timer;
    private HyLogger logger;

    private TreeMap<String, Pair<Long, Long>> CDList;

    public CDTimer(HyLogger logger) {
        this.logger = logger;
        CDList = new TreeMap<>();
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
        CDList.forEach((Key, pair) -> {
            Long time = pair.getValue();
            if (time > 0) {
                time--;
                CDList.replace(Key, new Pair<>(pair.getKey(), time));
            }
        });
    }
}
