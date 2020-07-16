package com.company.KDRobot.KDRobotCfg;

import cc.moecraft.icq.PicqBotX;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.company.KDRobot.KDRobot;

import java.io.FileInputStream;
import java.util.Vector;

public class KDRobotCfg {

    private Vector<Config> ConfigList;

    private String URL;
    private String NAME;
    private String PASSWORD;

    public KDRobotCfg(String CfgPATH) throws Exception {
        ConfigList = new Vector<>();
        FileInputStream inputStream = new FileInputStream(CfgPATH);
        JSONObject jsonObject = JSON.parseObject(inputStream, JSONObject.class);

        DataBaseCfg dataBaseCfg = jsonObject.getObject("DataBase", DataBaseCfg.class);
        if (dataBaseCfg == null || !dataBaseCfg.Assert())
            throw new Exception("没有DataBase或参数不全");

        JSONArray Groups = jsonObject.getJSONArray("Group");
        if (Groups == null || Groups.isEmpty())
            throw new Exception(CfgPATH + "中没有群配置");
        for (Object GroupObj : Groups) {
            JSONObject Group = (JSONObject) GroupObj;
            Config cfg = new Config();
            cfg.TurlingKey = Group.getString("TurlingKey");
            cfg.GroupID = Group.getLong("ID");
            if(cfg.GroupID == null)
                throw new Exception(Group + "\n缺少ID");
            cfg.AdminID = Group.getLong("Admin");
            cfg.dataBaseCfg = (DataBaseCfg) dataBaseCfg.clone();
            cfg.dataBaseCfg.Group = cfg.GroupID;
            ConfigList.add(cfg);
        }
    }

    public Vector<Config> getConfigList() {
        return ConfigList;
    }

    public void Register(PicqBotX bot) {
        for (Config i : ConfigList) {
            bot.getEventManager().registerListeners(new KDRobot(bot.getLogger(), i));
        }
    }

    @Override
    public String toString() {
        return "KDRobotCfg{" +
                "ConfigList=" + ConfigList +
                '}';
    }
}


