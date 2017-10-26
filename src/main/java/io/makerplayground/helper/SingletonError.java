package io.makerplayground.helper;

import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonError {
    private static SingletonError instance = null;

    private SingletonError() {}

    private String error;
    private long time;

    public static SingletonError getInstance() {
        if (instance == null) {
            instance = new SingletonError();
        }

        return instance;
    }

    public void setAll(String error) {
        this.error = error;
        this.time = new Date().getTime();


        //System.out.println("error = " + error);
        String command = "insert into Error (App_ID, Time, Message) values('"
                + SingletonConnectDB.getINSTANCE().getUuid() + "'," + time + ",\""
                + error + "\")";
        SingletonConnectDB.getINSTANCE().execute(command);
    }
}
