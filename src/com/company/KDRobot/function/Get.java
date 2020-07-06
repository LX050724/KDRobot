package com.company.KDRobot.function;

import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cc.moecraft.utils.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;
import java.util.regex.Pattern;

public class Get {
    public static Long At2Long(String str) {
        String ID;
        if (str.startsWith("[CQ:at,qq="))
            ID = str.substring(10, str.lastIndexOf(']'));
        else
            ID = str;

        if (StringUtils.isNumeric(ID))
            return Long.parseLong(ID);
        else
            return null;
    }

    public static String ID2Name(IcqHttpApi api, Long GroupID, Long UserID) {
        RGroupMemberInfo info = api.getGroupMemberInfo(GroupID, UserID).getData();
        String name = info == null ? UserID.toString() + "(获取名称失败)" : info.getCard();
        if (name.isEmpty()) name = info.getNickname();
        return name;
    }

    public static Vector<String> CQCode(String msg, String tag) {
        Vector<String> strings = new Vector<>();
        int start, end = 0;
        while (true) {
            start = msg.indexOf("[CQ:" + tag, end);
            if (start == -1)
                break;
            end = msg.indexOf(']', start);
            strings.add(msg.substring(start, end + 1));
        }
        return strings;
    }

    public static Long Str2Time(String str) {
        if (Pattern.matches("^[0-9]*[dhm]$", str)) {
            String sub = str.substring(0, str.length() - 1);
            if (StringUtils.isNumeric(sub)) {
                if (str.endsWith("d")) {
                    return Long.parseLong(sub) * 3600 * 24;
                } else if (str.endsWith("h")) {
                    return Long.parseLong(sub) * 3600;
                } else if (str.endsWith("m")) {
                    return Long.parseLong(sub) * 60;
                }
            }
        } else if (Pattern.matches("^[0-9]*$", str)) {
            return Long.parseLong(str);
        }
        return null;
    }

    public static boolean permissions(IcqHttpApi api, Long GroupId, Long ID, Long Admin) {
        String Role = api.getGroupMemberInfo(GroupId, ID).getData().getRole();

        if (Admin == null)
            return Role.equals("owner") || Role.equals("admin");
        else
            return Role.equals("owner") || Role.equals("admin") || ID.equals(Admin);
    }

    public static String StackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
