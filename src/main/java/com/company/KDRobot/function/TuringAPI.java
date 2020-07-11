package com.company.KDRobot.function;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TuringAPI {
    private static final String tulingAPI = "http://openapi.tuling123.com/openapi/api/v2";
    private final String Key;

    public TuringAPI(String Key) {
        this.Key = Key;
    }

    public String Send(Long Group, Long ID, String msg) {
        return Send(Group, ID, "", msg);
    }

    public String Send(Long Group, Long ID, String Name, String msg) {
        try {
            URL url = new URL(tulingAPI);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(GetJson(Group, ID.intValue(), Name, msg).getBytes(UTF_8));
            wr.flush();
            wr.close();
            con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), UTF_8));

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return ReadJson(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String GetJson(long Group, long ID, String Name, String msg) {
        if (msg.length() > 128) msg = msg.substring(0, 128);
        int ID_S = (int) (((ID >> 32) ^ ID) & 0x00000000ffffffffL);

        JSONObject object = new JSONObject();
        object.put("reqType", 0);

        JSONObject perception = new JSONObject();
        JSONObject inputText = new JSONObject();
        inputText.put("text", msg);
        perception.put("inputText", inputText);
        object.put("perception", perception);

        JSONObject userInfo = new JSONObject();
        userInfo.put("apiKey", Key);
        userInfo.put("userId", String.valueOf(ID_S));
        userInfo.put("groupId", String.valueOf(Group));
        userInfo.put("userIdName", Name);
        object.put("userInfo", userInfo);

        return object.toJSONString();
    }

    private String ReadJson(String json) {
        JSONObject object = JSONObject.parseObject(json);
        JSONObject intent = object.getJSONObject("intent");
        int code = intent.getInteger("code");
        JSONArray results = object.getJSONArray("results");
        String enomsg = GetErrorMsg(code);
        if (enomsg != null) {
            System.out.println(json);
            return "Error code=" + code + "\n" + enomsg;
        }
        StringBuilder builder = new StringBuilder();
        for (Object res : results) {
            JSONObject obj = (JSONObject) res;
            if (obj.getString("resultType").equals("text")) {
                JSONObject values = obj.getJSONObject("values");
                builder.append(values.getString("text"));
            }
        }
        return builder.toString();
    }

    public static String GetErrorMsg(int code) {
        switch (code) {
            case 0:
                return "上传成功";
            case 5000:
                return "无解析结果";
            case 6000:
                return "暂不支持该功能";
            case 4000:
                return "请求参数格式错误";
            case 4001:
                return "加密方式错误";
            case 4002:
            case 4005:
                return "无功能权限";
            case 4003:
                return "该apikey没有可用请求次数";
            case 4007:
                return "apikey不合法";
            case 4100:
                return "userid获取失败";
            case 4200:
                return "上传格式错误";
            case 4300:
                return "批量操作超过限制";
            case 4400:
                return "没有上传合法userid";
            case 4500:
                return "userid申请个数超过限制";
            case 4600:
                return "输入内容为空";
            case 4602:
                return "输入文本内容超长(上限150)";
            case 7002:
                return "上传信息失败";
            case 8008:
                return "服务器错误";
            default:
                return null;
        }
    }
}
