package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonAddDevice {
    private static SingletonAddDevice instance = null;

    private SingletonAddDevice() {}

    private String type;
    private String objID;
    private String time;
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
        this.time = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
        this.operation = "ADD";
    }
}
