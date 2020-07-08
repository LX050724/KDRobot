package com.company.KDRobot.function.TimeOutTimer;

import cc.moecraft.icq.sender.IcqHttpApi;

import java.util.*;

public class TimeOutTimer extends TimerTask {
    private Timer timer;
    private TreeMap<String, Node> map;

    public TimeOutTimer() {
        map = new TreeMap<>();
        timer = new Timer();
        timer.schedule(this, 1000, 1000);
    }

    public boolean Add(String Key, int time, IcqHttpApi api, TimeOutCallBack Callback) {
        if (!map.containsKey(Key)) {
            map.put(Key, new Node(time, api, Callback));
            return true;
        }
        return false;
    }

    public boolean delete(String Key) {
        if (map.containsKey(Key)) {
            map.remove(Key);
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        Vector<String> del = new Vector<>();
        map.forEach((Key, n) -> {
            n.time--;
            if (n.time == 0) {
                n.call(Key);
                del.add(Key);
            }
        });
        del.forEach((Key) -> map.remove(Key));
    }
}
