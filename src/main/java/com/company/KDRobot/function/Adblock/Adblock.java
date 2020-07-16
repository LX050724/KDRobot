package com.company.KDRobot.function.Adblock;

import com.company.KDRobot.function.Get;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adblock {
    /**
     * 搜索消息中的image消息，识别其中是否包含二维码
     *
     * @param msg 原始消息
     * @return 是否包含有效的二维码
     */
    public static boolean check(String msg) {
        for (String CQImage : Get.CQCode(msg, "image")) {
            Pattern r = Pattern.compile("^\\[CQ:image,file=.*,url=(.*)]");
            Matcher m = r.matcher(CQImage);
            String FileName;
            String Url;
            if (m.find()) {
                Url = m.group(1);
            } else {
                System.err.println(CQImage + "错误");
                return true;
            }
            byte[] img = DownloadPicFromUrl.downloadPicture(Url);
            if (QRCode.deEncode(img) != null)
                return true;
        }
        return false;
    }
}
