package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonGraphJson {
    private static SingletonGraphJson instance = null;

    private SingletonGraphJson() {}

    private String save;
    private long time;


    public static SingletonGraphJson getInstance() {
        if (instance == null) {
            instance = new SingletonGraphJson();
        }

        return instance;
    }

    public void setAll(String s) {
        this.save = s;
        this.time = new Date().getTime();

        String command = "insert into Graph (App_ID, FileID, Time) values('Add ID 1','" + save + "',"
                + time + ")";
        SingletonConnectDB.getInstance().execute(command);
    }
}
