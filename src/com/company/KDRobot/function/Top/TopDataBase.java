package com.company.KDRobot.function.Top;

import com.company.KDRobot.KDRobotCfg;
import javafx.util.Pair;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


public class TopDataBase {
    private static class RefreshTimer extends TimerTask {
        private Timer timer;
        private Statement stmt;
        private SimpleDateFormat ft;

        public RefreshTimer(Statement s) {
            stmt = s;
            ft = new SimpleDateFormat("dd");
        }

        public void Start() {
            timer = new Timer();
            timer.schedule(this, 1000 * 60 * 5, 1000 * 60 * 5);
        }

        @Override
        public void run() {
            try {
                ResultSet rs = stmt.executeQuery("SELECT LAST_MSG_TIME FROM TOP WHERE ID=0;");
                rs.next();
                String time = ft.format(rs.getTimestamp("LAST_MSG_TIME").getTime());
                String nowtime = ft.format(new Date());
                if (!time.equals(nowtime)) {
                    stmt.execute("UPDATE TOP SET TODAY=0,TICKET=1 WHERE ID!=0;");
                    stmt.execute("UPDATE TOP SET `KILL`=0 WHERE ID!=0 AND `KILL` < 3;");
                    stmt.execute("UPDATE TOP SET `KILL`=`KILL` - 3 WHERE ID!=0 AND `KILL` != 0;");
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private RefreshTimer refreshTimer;
    private Statement stmt;
    private Connection conn;

    public TopDataBase(KDRobotCfg.DataBaseCfg dataBaseCfg) {
        try {
            conn = DriverManager.getConnection(dataBaseCfg.URL, dataBaseCfg.NAME, dataBaseCfg.PASSWORD);
            stmt = conn.createStatement();

            /* 前面已经检查过数据库存在了，直接使用 */
            stmt.execute("USE Group" + dataBaseCfg.Group);

            /* 检查TOP表是否存在 */
            try {
                stmt.executeQuery("select * from TOP;");
            } catch (SQLSyntaxErrorException e) {
                if (e.getErrorCode() == 1146) {
                    System.out.println("TOP表不存在不存在，创建");
                    stmt.execute("create table TOP(" +
                            "ID BIGINT UNSIGNED default 0 not null," +
                            "TODAY BIGINT UNSIGNED default 0 not null," +
                            "`ALL` BIGINT UNSIGNED default 0 not null," +
                            "LAST_MSG VARCHAR(100) null," +
                            "LAST_MSG_TIME TIMESTAMP null," +
                            "`KILL` SMALLINT UNSIGNED default 0 not null," +
                            "TICKET SMALLINT UNSIGNED default 1 not null);");
                    stmt.execute("create index TOP_ALL_index on TOP (`ALL`);");
                    stmt.execute("create index TOP_ID_index on TOP (ID);");
                    stmt.execute("create index TOP_TODAY_index on TOP (TODAY);");
                    /* 添加用于记录保存时间的0号 */
                    stmt.execute("INSERT INTO TOP VALUES (0, 0, 0, null, CURRENT_TIMESTAMP(), DEFAULT, DEFAULT);");
                } else {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println(dataBaseCfg.URL + "连接连接失败\n\n");
            e.printStackTrace();
            System.exit(-1);
        }

        refreshTimer = new RefreshTimer(stmt);
        refreshTimer.Start();
    }

    public void Add(Long ID) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT TODAY,`ALL` FROM TOP WHERE ID=" + ID + ";");
            if (rs.next()) {
                stmt.execute("UPDATE TOP " +
                        "SET TODAY=TODAY + 1,`ALL`=`ALL` + 1" +
                        ",LAST_MSG_TIME=CURRENT_TIMESTAMP() " +
                        "WHERE ID=" + ID + ";");
            } else {
                stmt.execute("INSERT INTO TOP VALUES (" + ID + ", 1, 1, null, CURRENT_TIMESTAMP(), DEFAULT, DEFAULT);");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Pair<Long, Long>> getTop() {
        ArrayList<Pair<Long, Long>> list = new ArrayList<>();
        try {
            ResultSet rs = stmt.executeQuery("SELECT ID,`ALL` FROM TOP WHERE `ALL` > 0 AND ID != 0 ORDER BY `ALL` DESC LIMIT 10;");
            while (rs.next()) {
                list.add(new Pair<>(rs.getLong("ID"), rs.getLong("ALL")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<Pair<Long, Long>> getTopTodat() {
        ArrayList<Pair<Long, Long>> list = new ArrayList<>();
        try {
            ResultSet rs = stmt.executeQuery("SELECT ID,TODAY FROM TOP WHERE TODAY > 0 AND ID != 0 ORDER BY TODAY DESC LIMIT 10;");
            while (rs.next()) {
                list.add(new Pair<>(rs.getLong("ID"), rs.getLong("TODAY")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int vote(Long OperatorID, Long UserID) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT TICKET FROM TOP WHERE ID=" + OperatorID + ';');

            /* Operator为发送者，肯定存在 */
            rs.next();

            /* 检查Operator的投票机会 */
            if (rs.getInt("TICKET") > 0) {
                /* 获取UserID的KILL */
                rs = stmt.executeQuery("SELECT `KILL` FROM TOP WHERE ID=" + UserID + ';');

                /* 如果UserID存在，进行投票 */
                if (rs.next()) {
                    int kill = rs.getInt("KILL");
                    stmt.execute("UPDATE TOP SET TICKET=0 WHERE ID=" + OperatorID + ';');
                    stmt.execute("UPDATE TOP SET `KILL`=`KILL` + 1 WHERE ID=" + UserID + ';');
                    return kill;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
