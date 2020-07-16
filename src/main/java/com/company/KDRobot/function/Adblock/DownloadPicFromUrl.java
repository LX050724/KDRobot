package com.company.KDRobot.function.Adblock;

import java.io.*;
import java.net.URL;

public class DownloadPicFromUrl {
    //链接url下载图片
    public static byte[] downloadPicture(String urlList) {
        URL url;
        try {
            url = new URL(urlList);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            return output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}