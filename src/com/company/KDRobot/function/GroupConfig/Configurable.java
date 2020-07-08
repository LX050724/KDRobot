package com.company.KDRobot.function.GroupConfig;

/**
 * 可配置类接口,实现此接口以接入配置数据库
 */
public interface Configurable {
    /**
     * 获取属性列表及其默认值,属性由类内的String[]存储,
     * 数据成对存在,先属性名,后默认值
     *
     * @return 属性列表及其默认值
     */
    String[] GetAttributes();

    /**
     * 配置回调,由ConfigDataBase调用,传入配置
     *  @param Variable 变量名
     * @param Value    值
     * @return 成功返回true
     */
    boolean Config(String Variable, String Value);
}
