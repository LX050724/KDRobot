package com.company.KDRobot.function.sc;

import com.company.KDRobot.function.Get;

import java.sql.*;
import java.util.Vector;


public class BlackListDataBase {
    private Statement stmt;

    public BlackListDataBase(Statement stmt) {
        this.stmt = stmt;
        try {
            /* 检查BLACKLIST表是否存在 */
            stmt.execute("create table if not exists BLACKLIST(ID BIGINT UNSIGNED NOT NULL," +
                    "ADDTIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX BLACKLIST_ID_index(ID)," +
                    "CONSTRAINT BLACKLIST_pk PRIMARY KEY (ID));");
        } catch (
                Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public String RemoveBlackList(String ID) {
        Long Id = Get.At2Long(ID);
        if (Id == null) return null;
        try {
            if (stmt.executeQuery("SELECT ID FROM BLACKLIST WHERE ID=" + Id + ';').next()) {
                stmt.execute("DELETE FROM BLACKLIST WHERE ID=" + Id + ';');
                return Id.toString();
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long AddBlackList(Long ID) {
        try {
            try {
                stmt.execute("INSERT INTO BLACKLIST (ID, ADDTIME) VALUES (" + ID + ", DEFAULT);");
            } catch (SQLIntegrityConstraintViolationException e) {
                if (e.getErrorCode() != 1062)
                    e.printStackTrace();
                return null;
            }
            return ID;
        } catch (SQLException e) {
            System.err.println("ErrCode = " + e.getErrorCode());
            e.printStackTrace();
        }
        return null;
    }

    public Long AddBlackList(String ID) {
        Long Id = Get.At2Long(ID);
        if (Id == null) return null;
        return AddBlackList(Id);
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

    public Long GetOPT(Long ID) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT OPT FROM TOP WHERE ID=" + ID + ';');
            if (rs.next()) {
                return rs.getLong(1);
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
            return "Error Code=" + e.getErrorCode() + '\n' + e.getMessage();
        }
    }
}
