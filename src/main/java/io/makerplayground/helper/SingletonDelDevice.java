package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonDelDevice {
    private static SingletonDelDevice instance = null;

    private SingletonDelDevice() {}

    private String type;
    private String objID;
    private long time;
    private String operation;

    public static SingletonDelDevice getInstance() {
        if (instance == null) {
            instance = new SingletonDelDevice();
        }

        return instance;
    }

    public void setAll(String type, String objID) {
        this.type = type;
        this.objID = objID;
        this.time = new Date().getTime();
        this.operation = "REMOVE";

        String command = "insert into AddorDelDevice (App_ID, Action, Object_ID, Type, Time) values('Add ID 1','" + operation + "','"
                + objID + "','" + type + "'," + time + ")";
        SingletonConnectDB.getInstance().execute(command);
    }
}
