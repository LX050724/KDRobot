package com.company.KDRobot;

import cc.moecraft.icq.PicqBotX;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.Vector;

public class KDRobotCfg {
    public static class DataBaseCfg {
        public String URL;
        public String NAME;
        public String PASSWORD;
        public Long Group;
    }

    public static class Config {
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

    private Vector<Config> ConfigList;

    private String URL;
    private String NAME;
    private String PASSWORD;


    public KDRobotCfg(String CfgPATH) throws Exception {
        ConfigList = new Vector<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document d = builder.parse(CfgPATH);

        /* 获取根元素 */
        Element root = d.getDocumentElement();

        if (!root.getTagName().equals("KDRobotConfig"))
            throw new Exception(CfgPATH + " 可能不是bot配置文件");

        NodeList DataBaseList = root.getElementsByTagName("DataBase");

        if (DataBaseList.getLength() != 1)
            throw new Exception("有多个或者没有DataBase");

        Element DataBaseElement = (Element) DataBaseList.item(0);

        URL = DataBaseElement.getAttribute("URL");
        NAME = DataBaseElement.getAttribute("NAME");
        PASSWORD = DataBaseElement.getAttribute("PASSWORD");

        NodeList GroupList = root.getElementsByTagName("Group");

        if (GroupList.getLength() == 0)
            throw new Exception(CfgPATH + "中没有配置");

        for (int i = 0; i < GroupList.getLength(); i++) {
            Config cfg = new Config();
            Element GroupElement = (Element) GroupList.item(i);
            cfg.GroupID = Long.parseLong(GroupElement.getAttribute("ID"));
            String admin_s = GroupElement.getAttribute("Admin");
            String Key = GroupElement.getAttribute("TurlingKey");

            cfg.TurlingKey = Key.isEmpty() ? null : Key;
            cfg.AdminID = admin_s.isEmpty() ? null : Long.parseLong(admin_s);
            cfg.dataBaseCfg.NAME = NAME;
            cfg.dataBaseCfg.PASSWORD = PASSWORD;
            cfg.dataBaseCfg.URL = URL;
            cfg.dataBaseCfg.Group = cfg.GroupID;
            this.ConfigList.add(cfg);
        }
    }

    public Vector<Config> getConfigList() {
        return ConfigList;
    }

    public void Register(PicqBotX bot) {
        for (Config i : getConfigList()) {
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
