package com.company.KDRobot.function.TimeOutTimer;

import cc.moecraft.icq.sender.IcqHttpApi;

class Node {
    int time;
    IcqHttpApi api;
    TimeOutCallBack Callback;

    public Node(int time, IcqHttpApi api, TimeOutCallBack Callback) {
        this.time = time;
        this.api = api;
        this.Callback = Callback;
    }

    public void call(String Key) {
        Callback.timeout(Key, api);
    }
}
