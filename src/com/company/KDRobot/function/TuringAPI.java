package com.company.KDRobot.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class TuringAPI {
    private final String Key;

    public TuringAPI(String Key) {
        this.Key = Key;
    }

    public String machine(String quesiton) {
        //接入机器人，输入问题
        try {
            String INFO = URLEncoder.encode(quesiton, "utf-8");//这里可以输入问题
            String getURL = "http://www.tuling123.com/openapi/api?key=" + Key + "&info=" + INFO;
            URL getUrl = new URL(getURL);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.connect();

            // 取得输入流，并使用Reader读取
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            // 断开连接
            connection.disconnect();
            String[] ss;
            String s = sb.toString();
            String answer;
            ss = s.split(":");
            answer = ss[ss.length - 1];
            answer = answer.substring(1, answer.length() - 2);
            return answer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}