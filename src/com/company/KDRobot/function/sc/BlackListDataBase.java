package com.company.KDRobot.function.sc;

import com.company.KDRobot.KDRobotCfg;
import com.company.KDRobot.function.Get;

import java.sql.*;
import java.util.Vector;


public class BlackListDataBase {
    private Statement stmt;

    public BlackListDataBase(Statement stmt) {
        this.stmt = stmt;
        try {
            /* 检查TOP表是否存在 */
            try {
                stmt.executeQuery("select ID from BLACKLIST;");
            } catch (SQLSyntaxErrorException e) {
                if (e.getErrorCode() == 1146) {
                    System.out.println("BLACKLIST表不存在不存在，创建");
                    stmt.execute("create table BLACKLIST(ID BIGINT UNSIGNED NOT NULL," +
                            "ADDTIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "CONSTRAINT BLACKLIST_pk PRIMARY KEY (ID));");
                    stmt.execute("create index BLACKLIST_ID_index on BLACKLIST (ID);");
                } else {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
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
            try {
                stmt.execute("INSERT INTO BLACKLIST (ID, ADDTIME) VALUES (" + Id + ", DEFAULT);");
            } catch (SQLIntegrityConstraintViolationException e) {
                if (e.getErrorCode() != 1062)
                    e.printStackTrace();
                return null;
            }
            return Id.toString();
        } catch (SQLException e) {
            System.err.println("ErrCode = " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
    }

    public Vector<Long> ListBlackList() {
        Vector<Long> backlist = new Vector<>();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM BLACKLIST;");
            while (rs.next()) {
                Long e = rs.getLong("ID");
                if (e != null)
                    backlist.add(e);
            }
        } catch (SQLException e) {
            System.err.println("ErrCode = " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
        return backlist;
    }

    public String StringBlackList() {
        StringBuilder str = new StringBuilder();
        int count = 0;
        for (Long i : ListBlackList()) {
            str.append(i).append('\n');
            ++count;
        }
        str.insert(0, "总计" + count + '\n');
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    public boolean CheckOne(String ID) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM BLACKLIST WHERE ID=" + ID + ';');
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String SQL_Eval(String sql, Boolean write) {
        try {
            if (write) {
                String s = stmt.execute(sql) ? "execute return true" : "execute return false";
                return stmt.getUpdateCount() + " rows retrieved " + s;
            } else {
                StringBuilder str = new StringBuilder();
                ResultSet rs = stmt.executeQuery(sql);
                ResultSetMetaData resultMetaData = rs.getMetaData();
                Vector<String> Columns = new Vector<>();

                for (int i = 1; i <= resultMetaData.getColumnCount(); ++i) {
                    Columns.add(resultMetaData.getColumnLabel(i));
                    str.append(resultMetaData.getColumnLabel(i)).append(',');
                }
                str.deleteCharAt(str.length() - 1);
                str.append(":\n");
                while (rs.next()) {
                    try {
                        for (String Column : Columns) {
                            Object Obj = rs.getObject(Column);
                            String val;
                            if (Obj == null) val = "<NULL>";
                            else val = Obj.toString();
                            str.append(val).append(',');
                        }
                        str.deleteCharAt(str.length() - 1);
                        str.append('\n');
                    } catch (Exception e) {
                        e.printStackTrace();
                        return e.toString();
                    }
                }
                str.deleteCharAt(str.length() - 1);
                return str.toString();
            }
        } catch (SQLException e) {
            return "Error ID=" + e.getErrorCode() + '\n' + e.getMessage();
        }
    }
}
