package com.company.KDRobot.function.MessageBord;

import com.company.KDRobot.function.Get;

import java.sql.*;
import java.util.Vector;

public class MessageBordDataBase {
    public static class Message {
        public int msgID;
        public Long userID;
        public Long time;
        public String title;
        public String body;

        @Override
        public String toString() {
            return "Message{" +
                    "msgID=" + msgID +
                    ", userID=" + userID +
                    ", time=" + time +
                    ", title='" + title + '\'' +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    private Statement stmt;

    public MessageBordDataBase(Statement stmt) {
        this.stmt = stmt;
        try {
            /* 检查MSGBORD表是否存在 */
            try {
                stmt.execute("select ID from MSGBORD;");
            } catch (SQLSyntaxErrorException e) {
                if (e.getErrorCode() == 1146) {
                    System.out.println("MAGBORD表不存在不存在，创建");
                    stmt.execute("CREATE TABLE MSGBORD(\n" +
                            "ID INT NOT NULL AUTO_INCREMENT," +
                            "USERID INT NOT NULL," +
                            "TITLE VARCHAR(20) NOT NULL," +
                            "MSG VARCHAR(100) NOT NULL," +
                            "TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "CONSTRAINT MSGBORD_pk PRIMARY KEY (ID));");
                    stmt.execute("CREATE INDEX MSGBORD_ID_index ON MSGBORD (ID);");
                    stmt.execute("CREATE INDEX MSGBORD_USERID_index ON MSGBORD (USERID);");
                } else {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Vector<Message> ListMsg() {
        Vector<Message> vector = new Vector<>();

        try {
            stmt.execute("DELETE FROM MSGBORD WHERE CURRENT_TIMESTAMP() - TIME > 3600 * 24 * 3;");
            ResultSet rs = stmt.executeQuery("SELECT * FROM MSGBORD;");
            while (rs.next()) {
                Message msg = new Message();
                msg.body = rs.getString("MSG");
                msg.title = rs.getString("TITLE");
                msg.time = rs.getTimestamp("TIME").getTime();
                msg.msgID = rs.getInt("ID");
                msg.userID = rs.getLong("USERID");
                vector.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vector;
    }

    public Long pushMsg(Message msg) {
        /* 替换转义字符 */
        String title = Get.SQLstr(msg.title);
        String body = Get.SQLstr(msg.body);

        try {
            stmt.execute(String.format(
                    "INSERT INTO MSGBORD (USERID, TITLE, MSG, TIME) VALUES (%d, '%s', '%s', DEFAULT);",
                    msg.userID, title, body));
            ResultSet rs = stmt.executeQuery(String.format(
                    "SELECT ID FROM MSGBORD WHERE USERID='%d' AND TITLE='%S' AND MSG='%S';",
                    msg.userID, title, body));
            if (rs.next()) {
                return rs.getLong(1);
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteMsg(int MsgID) {
        try {
            stmt.execute("DELETE FROM MSGBORD WHERE ID=" + MsgID + ';');
            return stmt.getUpdateCount() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Message find(int MsgID) {
        try {
            stmt.execute("DELETE FROM MSGBORD WHERE CURRENT_TIMESTAMP() - TIME > 3600 * 24 * 3;");
            ResultSet rs = stmt.executeQuery("SELECT * FROM MSGBORD WHERE ID=" + MsgID + ';');
            if (rs.next()) {
                Message msg = new Message();
                msg.body = rs.getString("MSG");
                msg.title = rs.getString("TITLE");
                msg.time = rs.getTimestamp("TIME").getTime();
                msg.msgID = rs.getInt("ID");
                msg.userID = rs.getLong("USERID");
                return msg;
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
