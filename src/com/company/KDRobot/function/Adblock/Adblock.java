package com.company.KDRobot.function.Adblock;

import cc.moecraft.logger.HyLogger;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adblock {
    private final String PATH;
    private HyLogger logger;

    public Adblock(String WorkSpace, HyLogger logger) {
        this.PATH = WorkSpace + "/img";
        this.logger = logger;
        File d = new File(PATH);
        if (!d.exists())
            if(!d.mkdir())
                System.err.println("创建文件夹失败");
    }

    public boolean check(String CQImage) {
        Pattern r = Pattern.compile("^\\[CQ:image,file=(.*),url=(.*)]");
        Matcher m = r.matcher(CQImage);
        String FileName = null;
        String Url = null;
        if (m.find()) {
            FileName = m.group(1);
            Url = m.group(2);
        } else {
            System.err.println(CQImage + "错误");
            return true;
        }
        DownloadPicFromUrl.downloadPicture(Url, PATH + '\\' + FileName);
        String code = QRCode.deEncodeByPath(PATH + '\\' + FileName);
        if(code != null) {
            return false;
        }
        File imgfile = new File(PATH + '\\' + FileName);
        if(!imgfile.exists() || !imgfile.delete())
            System.err.println(FileName + "删除失败");
        return true;
    }
}
