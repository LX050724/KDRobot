package com.company.KDRobot;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.notice.groupmember.decrease.EventNoticeGroupMemberKick;
import cc.moecraft.icq.event.events.notice.groupmember.decrease.EventNoticeGroupMemberLeave;
import cc.moecraft.icq.event.events.notice.groupmember.increase.EventNoticeGroupMemberIncrease;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.logger.HyLogger;
import com.company.KDRobot.function.*;
import com.company.KDRobot.function.MessageBord.MessageBord;
import com.company.KDRobot.function.Top.Top;
import com.company.KDRobot.function.sc.SuperCommand;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KDRobot extends IcqListener {
    private Long GroupID;
    private Long Admin;
    private CDTimer cdTimer;
    private SuperCommand sc;
    private Top top;
    private TuringAPI turingAPI;
    private HyLogger logger;
    //    private Adblock adblock;
    private MessageBord Msg;

    public KDRobot(HyLogger logger, KDRobotCfg.Config cfg) {
        this.logger = logger;
        Statement stmt = null;
        /* 检查数据库是否存在 */
        try {
            Connection conn = DriverManager.getConnection(cfg.dataBaseCfg.URL, cfg.dataBaseCfg.NAME, cfg.dataBaseCfg.PASSWORD);
            stmt = conn.createStatement();
            stmt.execute("create database if not exists Group" + cfg.dataBaseCfg.Group + ';');
            stmt.close();
            conn.close();
            /* 设置到特定库的URL */
            int index = cfg.dataBaseCfg.URL.indexOf('?');
            cfg.dataBaseCfg.URL = cfg.dataBaseCfg.URL.substring(0, index) +
                    "/Group" + cfg.dataBaseCfg.Group +
                    cfg.dataBaseCfg.URL.substring(index);
            conn = DriverManager.getConnection(cfg.dataBaseCfg.URL, cfg.dataBaseCfg.NAME, cfg.dataBaseCfg.PASSWORD);
            stmt = conn.createStatement();
        } catch (Exception e) {
            System.err.println(cfg.dataBaseCfg.URL + "连接失败\n\n");
            e.printStackTrace();
            System.exit(-1);
        }

        logger.log("数据库'" + cfg.dataBaseCfg.URL + "'连接成功");

        this.Admin = cfg.AdminID;
        this.GroupID = cfg.GroupID;
        turingAPI = new TuringAPI("f4f88216f44c4fbc84f3ae03cc355300");
        Msg = new MessageBord(stmt, this.Admin, logger);
        sc = new SuperCommand(stmt, GroupID, this.Admin);
        top = new Top(stmt, this.Admin, logger);
        cdTimer = new CDTimer(logger);
//        adblock = new Adblock(PATH, logger);
        cdTimer.AddCD("Turling", 10L);
        cdTimer.AddCD("New", 10L);
        cdTimer.AddCD("about", 60L);
        cdTimer.AddCD("at", 30L);
        cdTimer.AddCD("STFW", 45L);
        cdTimer.AddCD("还有什么补充", 45L);
        cdTimer.AddCD("quiet", 600L);
        cdTimer.AddCD("baike", 600L);
    }

    @EventHandler
    public void onEGEvent(EventGroupMessage event) {
        try {
            if (event.getGroupId().equals(GroupID)) {
                boolean permissions = Get.permissions(event.getHttpApi(), event.getGroupId(), event.getSenderId(), Admin);

                /* top统计以及刷屏禁言 */
                top.getMsg(event, permissions);

                /* 去除首尾空格 */
                String msg = event.getMessage().trim();

                if (msg.equals("STFW") && cdTimer.CD("STFW")) {
                    event.respond("当你提问的时候，别人说STFW是什么意思?\n" +
                            "鉴于你思考，动手以及信息检索能力的不足下面简单介绍STFW。\n" +
                            "STFW全称Search The Fucking Web。意思是回答者认为你应该\"到他妈的网上搜索一下!\"。\n" +
                            "这意味着：\n" +
                            "1.你需要的信息非常容易获得。\n" +
                            "2.你自己去找这些信息更能让你学到更多。\n" +
                            "如果你觉得被冒犯了，请平静回应，以确认他是想教育你，还是单纯想冒犯你。\n" +
                            "关于如何避免你愚蠢的提问方式导致这样的回答请仔细阅读群文件的“提问的智慧”又或者是我提供的github网址查看：\n" +
                            "https://github.com/FredWe/How-To-Ask-Questions-The-Smart-Way/blob/master/README-zh_CN.md\n" +
                            "（感谢来自782307413提供的文本，遵循 CC 4.0 BY-SA 版权协议）");
                    return;
                }

                if (Pattern.matches(".*有什么.?补充.*", msg) && cdTimer.CD("还有什么补充")) {
                    event.respond("当回答者询问你\"还有什么补充\"时，这意味着:\n" +
                            "1.你提供的信息少地不足以让人分析问题所在\n" +
                            "2.你的提问缺少清晰、正确、精准并语法正确的基本要求\n" +
                            "3.你的提问没有准确的描述你想攻克的难题\n" +
                            "4.你似乎并没有尝试过独立解决问题\n" +
                            "一般得到这样的回答时，说明回答者对你的提问非常失望，你将往往得不到答案。\n" +
                            "如果你可以尝试以以下例句的方式进行提问，也许对你以后有所帮助:\n" +
                            "------------------------\n" +
                            "我在 S2464 主机板上试过了 X 、 Y 和 Z ，但没什么作用，我又试了 A 、 B 和 C 。请注意当我尝试 C 时的奇怪现象。" +
                            "显然 florbish 正在 grommicking，但结果出人意料。通常在 Athlon MP 主机板上引起 grommicking 的原因是什么？" +
                            "有谁知道接下来我该做些什么测试才能找出问题？\n" +
                            "------------------------\n" +
                            "关于如何避免你愚蠢的提问方式导致这样的回答请仔细阅读群文件的“提问的智慧”又或者是我提供的github网址查看：\n" +
                            "https://github.com/FredWe/How-To-Ask-Questions-The-Smart-Way/blob/master/README-zh_CN.md\n" +
                            "（感谢来自782307413提供的文本，遵循 CC 4.0 BY-SA 版权协议）");
                    return;
                }

                if (msg.contains(String.format("[CQ:at,qq=%d]", event.getSelfId()))) {
                    if (cdTimer.CD("at"))
                        event.respond("想要聊天？使用'bot t'命令和图灵机器人聊天");
                    return;
                }

                if (!permissions) for (String contact : Get.CQCode(msg, "contact")) {
                    Pattern r = Pattern.compile("^\\[CQ:contact,id=(.*),type=group]");
                    Matcher m = r.matcher(contact);
                    if (m.find()) {
                        String id = m.group(1);
                        if (!id.equals(event.getGroupId().toString())) {
                            if (sc.Addbl(event.getSenderId()))
                                event.respond("分享群，添加黑名单");
                            else
                                event.respond("添加黑名单错误");
                            event.getHttpApi().setGroupKick(event.getGroupId(), event.getSenderId());
                            event.getHttpApi().deleteMsg(event.getMessageId());
                        }
                    }
                }

                String[] cmd = msg.split("\\s+");

                if (cmd.length <= 1)
                    return;

                if (cmd[0].equals("bot")) {
                    switch (cmd[1]) {
                        case "msg":
                            Msg.process(event, cmd);
                            break;
                        case "top":
                            top.process(event, cmd);
                            break;
//                    case "quiet":
//                        if (cdTimer.CD("quiet")) {
//                            Long time = Get.Str2Time(cmd[2]);
//                            if (time != null) {
//                                if (time <= 3600 * 5)
//                                    api.setGroupBan(event.getGroupId(), event.getSenderId(), time);
//                                else
//                                    event.respond("最长5小时，请10分钟后重试");
//                            } else event.respond("输入有误");
//                        }
//                        break;
                        case "t":
                            if (cdTimer.CD("Turling")) {
                                String m = event.getMessage();
                                m = m.substring(m.indexOf("t", m.indexOf("bot") + 4) + 2);
                                String r = turingAPI.machine(m);
                                logger.log(String.format("接到%d的图灵消息:'%s',回复:'%s'", event.getSenderId(), m, r));
                                event.respond(r);
                            }
                            break;
                        case "about":
                            if (cdTimer.CD("about"))
                                event.respond("此机器人是基于酷Q CQHTTP插件，使用https://github.com/HyDevelop/PicqBotX框架开发的群管理类机器人\n" +
                                        "机器人代码Github地址https://github.com/LX050724/KDRobot");
                            break;
                        case "baike":
                            if (cmd.length < 3) {
                                event.respond("请输入要查询的关键字");
                                break;
                            }
                            if (permissions || cdTimer.CD("baike")) {
                                String keyword = msg.substring(msg.indexOf("baike") + 6);
                                event.respond(keyword + "百度百科：\n" + BaiKe.GetBaiKe(keyword));
                            } else {
                                event.respond("baike" + " CD Time last " + cdTimer.GetLastTime("baike"));
                            }
                            break;
                        case "help":
                            event.respond("bot 帮助:\n" +
                                    "top:水群排行\n" +
                                    "quiet:buff(第三参数为时间,结尾为d,h,m单位分别为天,时,分,不加为秒)\n" +
                                    "msg:留言板\n" +
                                    "t:和图灵机器人聊天(所有人10秒CD)\n" +
                                    "about:相关信息\n" +
                                    "baike:查询百度百科(CD10分钟)\n" +
                                    "help:显示此帮助");
                            break;
                        case "verify":
                            sc.verify(event, cmd);
                            break;
                        default:
                            event.respond("命令错误\n输入'bot help'查询帮助");
                    }
                }

                if (cmd[0].equals("sc")) {
                    if (permissions)
                        sc.process(event, cmd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.getHttpApi().sendGroupMsg(GroupID, "发生异常！\n" + e.getMessage());
        }
    }

    @EventHandler
    public void onENGMIEvent(EventNoticeGroupMemberIncrease event) {
        try {
            if (event.getGroupId().equals(GroupID)) {
                Long UserID = event.getUserId();
                top.AddMember(UserID, event.getOperatorId());
                if (sc.chick(event, UserID) && cdTimer.CD("New"))
                    event.getHttpApi().sendGroupMsg(event.getGroupId(), new MessageBuilder()
                            .add(new ComponentAt(UserID))
                            .add("新人看公告\n查看机器人帮助发送\n'bot help'")
                            .toString()
                    );
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.getHttpApi().sendGroupMsg(GroupID, "发生异常！\n" + e.getMessage());
        }
    }

    @EventHandler
    public void onENGMLEvent(EventNoticeGroupMemberLeave event) {
        try {
            if (event.getGroupId().equals(GroupID)) {
                IcqHttpApi api = event.getHttpApi();
                api.sendGroupMsg(event.getGroupId(), String.format("%d 自己退群了！", event.getUserId()));
                logger.log(String.format("群:%d, %d 主动退群", event.getGroupId(), event.getUserId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.getHttpApi().sendGroupMsg(GroupID, "发生异常！\n" + e.getMessage());
        }
    }

    @EventHandler
    public void onENGMKEvent(EventNoticeGroupMemberKick event) {
        try {
            if (event.getGroupId().equals(GroupID)) {
                IcqHttpApi api = event.getHttpApi();
                api.sendGroupMsg(event.getGroupId(), String.format("%d 被狗管理 %s 踢了！",
                        event.getUserId(), Get.ID2Name(api, event.getGroupId(), event.getOperatorId())));
                logger.log(String.format("群:%d, %d 被%d踢了", event.getGroupId(), event.getUserId(), event.getOperatorId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.getHttpApi().sendGroupMsg(GroupID, "发生异常！\n" + e.getMessage());
        }
    }
}
