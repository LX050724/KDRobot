package com.company.KDRobot.function.TimeOutTimer;

import cc.moecraft.icq.sender.IcqHttpApi;

public interface TimeOutCallBack {
    void timeout(String Key, IcqHttpApi api);
}
