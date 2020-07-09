package com.company.KDRobot.function;

import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cc.moecraft.utils.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;
import java.util.regex.Pattern;

public class Get {

    /**
     * 从纯数字字符串或者CQ的At消息中获取QQ号
     *
     * @param str 字符串
     * @return 成功解析返回其值，失败返回null
     */
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

    /**
     * 从QQ号获取名字
     *
     * @param api     酷QAPI
     * @param GroupID 群号
     * @param UserID  QQ号
     * @return 如果获取到返回名字，群昵称优先，其次是QQ昵称，
     * 获取不到返回 QQ号(获取名称失败)
     */
    public static String ID2Name(IcqHttpApi api, Long GroupID, Long UserID) {
        RGroupMemberInfo info = api.getGroupMemberInfo(GroupID, UserID).getData();
        String name = info == null ? UserID.toString() + "(获取名称失败)" : info.getCard();
        if (name.isEmpty()) name = info.getNickname();
        return name;
    }

    /**
     * 获取指定Tag的CQ消息
     *
     * @param msg 原始消息字符串
     * @param tag Tag
     * @return 原始消息内所有Tag符合的CQ消息内容，没有则返回空向量
     */
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

    /**
     * 从时间字符串获取所表示的秒数
     *
     * @param str 时间字符串
     * @return 时间（秒），若无法解析返回null
     */
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

    /**
     * 检查成员是否存在并且是否拥有管理员身份
     *
     * @param api     酷QAPI
     * @param GroupId 群号
     * @param ID      QQ号
     * @param Admin   机器人管理员QQ号
     * @return 成员拥有管理员身份返回TRUE，否则返回FALSE，成员不在群内返回null
     */
    public static Boolean permissions(IcqHttpApi api, Long GroupId, Long ID, Long Admin) {
        RGroupMemberInfo memberInfo = api.getGroupMemberInfo(GroupId, ID).getData();
        if (memberInfo == null) return null;

        String Role = memberInfo.getRole();

        if (Admin == null)
            return Role.equals("owner") || Role.equals("admin");
        else
            return Role.equals("owner") || Role.equals("admin") || ID.equals(Admin);
    }

    /**
     * 获取异常堆栈信息字符串
     *
     * @param throwable 异常
     * @return 异常堆栈字符串
     */
    public static String StackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
