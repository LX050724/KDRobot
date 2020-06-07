package com.company;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.PicqConfig;
import cc.moecraft.logger.environments.ColorSupportLevel;
import com.company.KDRobot.KDRobotCfg;

public class Main {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        if (args.length == 0) {
            System.err.println("未指定配置文件路径");
            System.exit(0);
        }

        boolean color = false;
        if (args.length == 2) {
            color = args[1].equals("color");
        }

        KDRobotCfg botcfg = new KDRobotCfg(args[0]);

        if (botcfg.getErrMsg() != null) {
            System.err.println(botcfg.getErrMsg());
            System.exit(0);
        }

        PicqConfig config = new PicqConfig(5702);
        config.setLogPath("");
        if (!color)
            config.setColorSupportLevel(ColorSupportLevel.DISABLED);
        PicqBotX bot = new PicqBotX(config);
        bot.addAccount("Bot01", "127.0.0.1", 5701);

        botcfg.Register(bot);
        bot.startBot();
    }
}
