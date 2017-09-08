package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonUtilTools {
    private static SingletonUtilTools instance = null;

    private SingletonUtilTools() {}

    private String type;
    private long time;

    public static SingletonUtilTools getInstance() {
        if (instance == null) {
            instance = new SingletonUtilTools();
        }

        return instance;
    }

    public void setAll(String type) {
        this.type = type;
        this.time = new Date().getTime();

        String command = "insert into UtilTools (App_ID, Type, Time) values('Add ID 1','" + type + "',"
                + time + ")";
        SingletonConnectDB.getInstance().execute(command);
    }

}
