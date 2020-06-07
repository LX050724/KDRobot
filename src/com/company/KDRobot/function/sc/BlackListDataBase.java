package com.company.KDRobot.function.sc;

import com.company.KDRobot.KDRobotCfg;
import com.company.KDRobot.function.Get;

import java.sql.*;


public class BlackListDataBase {
    private Statement stmt;
    private Connection conn;

    public BlackListDataBase(KDRobotCfg.DataBaseCfg dataBaseCfg) {
        try {
            conn = DriverManager.getConnection(dataBaseCfg.URL, dataBaseCfg.NAME, dataBaseCfg.PASSWORD);
            stmt = conn.createStatement();

            /* 前面已经检查过数据库存在了，直接使用 */
            stmt.execute("USE Group" + dataBaseCfg.Group);

            /* 检查TOP表是否存在 */
            try {
                stmt.executeQuery("select * from BLACKLIST;");
            } catch (SQLSyntaxErrorException e) {
                if (e.getErrorCode() == 1146) {
                    System.out.println("BLACKLIST表不存在不存在，创建");
                    stmt.execute("create table BLACKLIST(ID BIGINT UNSIGNED default 0 not null);");
                    stmt.execute("create index BLACKLIST_ID_index on BLACKLIST (ID);");
                } else {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println(dataBaseCfg.URL + "连接连接失败\n\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public String RemoveBlackList(String ID) {
        Long Id = Get.At2Long(ID);
        if (Id == null) return null;
        try {
            if (stmt.execute("SELECT ID FROM BLACKLIST WHERE ID=" + Id + ';')) {
                stmt.execute("DELETE FROM BLACKLIST WHERE ID=" + Id + ';');
                return Id.toString();
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String AddBlackList(String ID) {
        Long Id = Get.At2Long(ID);
        if (Id == null) return null;
        try {
            if (!stmt.executeQuery("SELECT ID FROM BLACKLIST WHERE ID=" + Id + ';').next()) {
                stmt.execute("INSERT INTO BLACKLIST VALUE (" + Id + ");");
                return Id.toString();
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String ListBlackList() {
        StringBuilder str = new StringBuilder();
        int count = 0;
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM BLACKLIST;");
            while (rs.next()) {
                str.append(rs.getLong("ID")).append('\n');
                ++count;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "err " + e.getMessage();
        }
        str.insert(0, "总计" + count + '\n');
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    public boolean Check(String ID) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM BLACKLIST WHERE ID=" + ID + ';');
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
