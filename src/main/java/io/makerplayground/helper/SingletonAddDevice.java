package io.makerplayground.helper;

import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonAddDevice {
    private static SingletonAddDevice instance = null;

    private SingletonAddDevice() {}

    private String type;
    private String objID;
    private long time;
    private String operation;

    public static SingletonAddDevice getInstance() {
        if (instance == null) {
            instance = new SingletonAddDevice();
        }

        return instance;
    }

    public void setAll(String type, String objID) {
        this.type = type;
        this.objID = objID;
        this.time = new Date().getTime();
        this.operation = "ADD";

        String command = "insert into AddorDelDevice (App_ID, Action, Object_ID, Type, Time) values('Add ID 1','" + operation + "','"
                + objID + "','" + type + "'," + time + ")";
        SingletonConnectDB.getINSTANCE().execute(command);
    }
}
