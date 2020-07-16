package com.company;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.PicqConfig;
import cc.moecraft.logger.environments.ColorSupportLevel;
import com.company.KDRobot.KDRobotCfg.KDRobotCfg;
import org.apache.commons.cli.*;

public class Main {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("help", "help", false, "-help 显示此帮助");
        options.addOption("Config", "Config", true, "-Config <PATH> 必填 配置文件路径 ");
        options.addOption("Color", "Color", false, "-Color 选填 开启颜色支持");
        options.addOption("Host", "Host", true, "-Host <IP> 选填 Http插件posthost 默认值'127.0.0.1'");
        options.addOption("PostPort", "PostPort", true, "-PostPort <Prot> 选填 Http插件postport 默认值'5701'");
        options.addOption("SocketPort", "SocketPort", true, "-SocketPort <Port> 选填 Http插件socketPort 默认值'5702'");
        options.addOption("Log", "Log", true, "-Log <FolderPATH> 选填 Log输出路径 默认不输出");
        options.addOption("Name", "Name", true, "-Name <Name> 选填 Bot账号名 默认值'Bot01'");
        options.addOption("AccessToken", "AccessToken", true, "-AccessToken <Token> 选填 Access Token 访问令牌，默认为空");
        options.addOption("Secret", "Secret", true, "-Secret <Key> 选填 SHA1 验证秘钥，默认为空");

        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        if (commandLine.hasOption("help")) {
            System.out.println("选项包括:");
            for (Option option : options.getOptions()) {
                System.out.println(String.format("\t-%-10s\t%s", option.getLongOpt(), option.getDescription()));
            }
            System.exit(0);
        }

        if (!System.getProperty("file.encoding").equals("UTF-8")) {
            exit("默认编码集必须是UTF-8,请在虚拟机参数中添加'-Dfile.encoding=UTF-8'");
        }

        int socketPort = 5702;
        if (commandLine.hasOption("SocketPort")) {
            try {
                socketPort = Integer.parseInt(commandLine.getOptionValue("SocketPort"));
            } catch (NumberFormatException e) {
                exit(e.getMessage() + " socketport 格式错误");
            }
        }

        int postport = 5701;
        if (commandLine.hasOption("PostPort")) {
            try {
                postport = Integer.parseInt(commandLine.getOptionValue("PostPort"));
            } catch (NumberFormatException e) {
                exit(e.getMessage() + " postport 格式错误");
            }
        }

        KDRobotCfg botcfg = null;
        if (commandLine.hasOption("Config")) {
            try {
                botcfg = new KDRobotCfg(commandLine.getOptionValue("Config"));
            } catch (Exception e) {
                exit(e.getMessage());
            }
        } else exit("未指定配置文件路径");


        String Botname = commandLine.getOptionValue("Name", "Bot01");
        String host = commandLine.getOptionValue("Host", "127.0.0.1");

        PicqConfig config = new PicqConfig(socketPort);

        if (!commandLine.hasOption("Color")) {
            config.setColorSupportLevel(ColorSupportLevel.DISABLED);
        }

        config.setLogPath(commandLine.getOptionValue("Log", ""));
        config.setAccessToken(commandLine.getOptionValue("AccessToken", ""));
        config.setSecret(commandLine.getOptionValue("Secret", ""));

        PicqBotX bot = null;
        try {
            bot = new PicqBotX(config);
            bot.addAccount(Botname, host, postport);
        } catch (Exception e) {
            exit(e.getMessage());
        }
        botcfg.Register(bot);
        bot.startBot();
    }

    public static void exit(String info) {
        System.err.println(info);
        System.exit(0);
    }
}
