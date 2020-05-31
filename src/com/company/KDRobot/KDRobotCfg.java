package com.company.KDRobot;

import cc.moecraft.icq.PicqBotX;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.Vector;

public class KDRobotCfg {
    public static class Config {
        public Long GroupID;
        public Long AdminID;
        public boolean AdminEnable;
        public String WorkSpace;

        public Config() {
            GroupID = AdminID = null;
            AdminEnable = false;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "GroupID=" + GroupID +
                    ", AdminID=" + AdminID +
                    ", AdminEnable=" + AdminEnable +
                    ", WorkSpace='" + WorkSpace + '\'' +
                    '}';
        }
    }

    private Vector<Config> ConfigList;
    private String ErrMsg;

    public KDRobotCfg(String CfgPATH) {
        ConfigList = new Vector<>();
        ErrMsg = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document d = builder.parse(CfgPATH);

            /* 获取根元素 */
            Element root = d.getDocumentElement();

            if (!root.getTagName().equals("KDRobotConfig")) {
                ErrMsg = (CfgPATH + " 可能不是bot配置文件");
                return;
            }

            NodeList GroupList = root.getElementsByTagName("Group");

            if (GroupList.getLength() == 0) {
                ErrMsg = (CfgPATH + "中没有配置");
                return;
            }

            for (int i = 0; i < GroupList.getLength(); i++) {
                Config cfg = new Config();
                Element GroupElement = (Element) GroupList.item(i);
                cfg.GroupID = Long.parseLong(GroupElement.getAttribute("ID"));

                NodeList AdminList = GroupElement.getElementsByTagName("Admin");
                if (AdminList.getLength() == 1) {
                    Element AdminElement = (Element) AdminList.item(0);
                    cfg.AdminID = Long.parseLong(AdminElement.getAttribute("ID"));
                    cfg.AdminEnable = true;
                } else if (AdminList.getLength() > 1) {
                    ErrMsg = (cfg.GroupID + "中有多个Admin");
                    return;
                }

                NodeList WorkSpaceList = GroupElement.getElementsByTagName("WorkSpace");
                if (WorkSpaceList.getLength() == 1) {
                    Element WorkSpaceElement = (Element) WorkSpaceList.item(0);
                    cfg.WorkSpace = WorkSpaceElement.getAttribute("PATH");
                } else {
                    ErrMsg = (cfg.GroupID + "中没有或有多个WorkSpace");
                    return;
                }
                this.ConfigList.add(cfg);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public String getErrMsg() {
        return ErrMsg;
    }
}
