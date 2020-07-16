package com.company.KDRobot.KDRobotCfg;

public class Config {
    public DataBaseCfg dataBaseCfg;
    public Long GroupID;
    public Long AdminID;
    public String TurlingKey;

    public Config() {
        GroupID = AdminID = null;
        TurlingKey = null;
        dataBaseCfg = new DataBaseCfg();
    }

    @Override
    public String toString() {
        return "Config{" +
                "dataBaseCfg=" + dataBaseCfg +
                ", GroupID=" + GroupID +
                ", AdminID=" + AdminID +
                ", TurlingKey='" + TurlingKey + '\'' +
                '}';
    }
}