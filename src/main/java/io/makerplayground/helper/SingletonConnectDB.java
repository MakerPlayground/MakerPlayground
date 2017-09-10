package io.makerplayground.helper;

import com.microsoft.sqlserver.jdbc.SQLServerBulkCopy;

import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by tanyagorn on 9/8/2017.
 */
public class SingletonConnectDB {
    private static SingletonConnectDB INSTANCE = null;
    private static final String azureConnectionString = "jdbc:sqlserver://makerplayground.database.windows.net:1433;database=makerplayground-log;user=makerplayground;password=Visionear#2017;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";

    private Connection sqliteConnection = null;
    private String uuid = "";
    private Executor executor = Executors.newSingleThreadExecutor();

    private SingletonConnectDB() {
        try {
            sqliteConnection = DriverManager.getConnection("jdbc:sqlite::resource:log.db");

            // read user's uuid from the database
            ResultSet rs = sqliteConnection.createStatement().executeQuery("SELECT uuid FROM UUID");
            if (rs.next()) {
                uuid = rs.getString("uuid");
            } else {
                uuid = UUID.randomUUID().toString().replace("-", "");
                sqliteConnection.createStatement().execute("INSERT INTO UUID VALUES (\"" + uuid + "\")");
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Write to azure every 10 minutes
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                            try (Statement stmt = sqliteConnection.createStatement()) {
                                // Get data from the source table as a ResultSet.
                                try (ResultSet rsSourceData = stmt.executeQuery(
                                        "SELECT App_ID, isClick, Page, OpenTime, Duration FROM Tutorial"))
                                {
                                    // Open the destination connection.
                                    try (Connection azureConnection =
                                                 DriverManager.getConnection(azureConnectionString)) {
                                        try (SQLServerBulkCopy bulkCopy =
                                                     new SQLServerBulkCopy(azureConnection)) {
                                            bulkCopy.setDestinationTableName("Tutorial");
                                            // Write from the source to the destination.
                                            bulkCopy.writeToServer(rsSourceData);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e ) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 60000, 60000);
    }

    public void close() {
        try {
            if (sqliteConnection != null) {
                sqliteConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static SingletonConnectDB getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new SingletonConnectDB();
        }

        return INSTANCE;
    }

    public void execute(String command) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    sqliteConnection.createStatement().execute(command);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
