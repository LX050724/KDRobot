package com.company.KDRobot.KDRobotCfg;

public class DataBaseCfg implements Cloneable {
    public String URL;
    public String NAME;
    public String PASSWORD;
    public long Group;

    public boolean Assert() {
        return (URL != null && !URL.isEmpty()) &&
                (NAME != null && !NAME.isEmpty()) &&
                (PASSWORD != null && !PASSWORD.isEmpty());
    }

    @Override
    public String toString() {
        return "DataBaseCfg{" +
                "URL='" + URL + '\'' +
                ", NAME='" + NAME + '\'' +
                ", PASSWORD='" + PASSWORD + '\'' +
                ", Group=" + Group +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}