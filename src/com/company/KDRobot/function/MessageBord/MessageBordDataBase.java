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
            stmt.execute("CREATE TABLE if not exists MSGBORD(" +
                    "ID INT NOT NULL AUTO_INCREMENT," +
                    "USERID INT NOT NULL," +
                    "TITLE VARCHAR(20) NOT NULL," +
                    "MSG VARCHAR(100) NOT NULL," +
                    "TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX MSGBORD_pk(ID)," +
                    "INDEX MSGBORD_USERID_index(USERID)," +
                    "CONSTRAINT MSGBORD_pk PRIMARY KEY (ID));");
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
        try {
            PreparedStatement ptmt = stmt.getConnection().prepareStatement(
                    "INSERT INTO MSGBORD (USERID, TITLE, MSG, TIME) VALUES (?, ?, ?, DEFAULT);");
            ptmt.setLong(1, msg.userID);
            ptmt.setString(2, msg.title);
            ptmt.setString(3, msg.body);

            ptmt = stmt.getConnection().prepareStatement(
                    "SELECT ID FROM MSGBORD WHERE USERID=? AND TITLE=? AND MSG=?;");
            ptmt.setLong(1, msg.userID);
            ptmt.setString(2, msg.title);
            ptmt.setString(3, msg.body);
            ResultSet rs = ptmt.executeQuery();
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
