package com.company.KDRobot.function.GroupConfig;

import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashMap;

public class ConfigDataBase {
    private Statement stmt;
    private HashMap<String, Configurable> CallBack;

    public ConfigDataBase(Statement stmt) {
        CallBack = new HashMap<>();
        this.stmt = stmt;
        try {
            /* 检查CONFIG表是否存在 */
            stmt.execute("create table if not exists  CONFIG(" +
                    "`variable` varchar(128) NOT NULL," +
                    "`value` varchar(255) DEFAULT NULL," +
                    "PRIMARY KEY (`variable`)," +
                    "INDEX CONFIG_variable_index(`variable`));");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * 将可配置的类属性注册,并读取其中的属性默认值
     *
     * @param configurable 可配置类接口
     */
    public void Register(@NotNull Configurable configurable) {
        String[] attributes = configurable.GetAttributes();
        HashMap<String, String> config = new HashMap<>();
        try {
            PreparedStatement ptmt = stmt.getConnection().prepareStatement(
                    "insert ignore into `config` values(?, ?)");
            for (int i = 0; i < attributes.length; i += 2) {
                ptmt.setString(1, attributes[i]);
                ptmt.setString(2, attributes[i + 1]);
                ptmt.addBatch();
            }
            ptmt.executeBatch();

            ResultSet rs = stmt.executeQuery("select * from `config`");
            while (rs.next()) {
                config.put(rs.getString(1), rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < attributes.length; i += 2) {
            CallBack.put(attributes[i], configurable);
            configurable.Config(attributes[i], config.get(attributes[i]));
        }
    }

    /**
     * 应用设置并写入数据库
     *
     * @param Variable 变量名
     * @param Value    值
     * @return 成功返回true, 配置不存在返回false
     */
    public boolean SetConfig(String Variable, String Value) {
        if (Variable.length() > 128)
            Variable = Variable.substring(0, 128);
        if (Value.length() > 255)
            Value = Value.substring(0, 255);
        Configurable callback = CallBack.get(Variable);
        if (callback != null) {
            return (callback.Config(Variable, Value) && WriteConfig(Variable, Value));
        } else return false;
    }

    /**
     * 将配置写入数据库
     *
     * @param Variable 变量名
     * @param Value    值
     */
    private boolean WriteConfig(String Variable, String Value) {
        try {
            PreparedStatement ptmt = stmt.getConnection().prepareStatement(
                    "update `config` set `value` = ? where `variable` = ?;");
            ptmt.setString(1, Value);
            ptmt.setString(2, Variable);
            ptmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读取数据库变量
     *
     * @param Variable 变量名
     * @return 值
     */
    public String ReadConfig(String Variable) {
        try {
            PreparedStatement ptmt = stmt.getConnection().prepareStatement(
                    "select `value` from `config` where `variable`=?;");
            ptmt.setString(1, Variable);
            ResultSet rs = ptmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}