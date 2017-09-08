package io.makerplayground.helper;

import java.sql.*;

/**
 * Created by tanyagorn on 9/8/2017.
 */
public class SingletonConnectDB {
    private static SingletonConnectDB instance = null;
    private Connection connection = null;

    private SingletonConnectDB() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite::resource:log.db");
            //connection = DriverManager.getConnection("jdbc:sqlserver://makerplayground.database.windows.net:1433;database=makerplayground-log;user=makerplayground;password=Visionear#2017;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
            Statement statement = connection.createStatement();
//            ResultSet rs = statement.executeQuery("select * from WiringDiagram");
//            while (rs.next()) {
//                //System.out.println(rs.getString("App_ID") + " " + rs.getString("HowtoDrag") + " " + rs.getString("Time"));
//                System.out.println(rs.getString("App_ID") + " " + rs.getString("OpenTime") + " " + rs.getString("Duration"));
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        String[] tableName = {"AA", "BB"};
        Connection connection2 = null;
        try {
            connection2 = DriverManager.getConnection("jdbc:sqlserver://makerplayground.database.windows.net:1433;database=makerplayground-log;user=makerplayground;password=Visionear#2017;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
            for (String table : tableName) {
                try {
                    connection2.createStatement().execute("INSERT INTO " + table + " VALUE (  ) ");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String time;

    public static SingletonConnectDB getInstance() {
        if (instance == null) {
            instance = new SingletonConnectDB();
        }

        return instance;
    }

    public void execute(String command) {
        try {
            connection.createStatement().execute(command);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
