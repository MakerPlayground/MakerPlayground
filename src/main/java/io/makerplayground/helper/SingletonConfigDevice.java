package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonConfigDevice {
    private static SingletonConfigDevice instance = null;

    private SingletonConfigDevice() {}

    private String objID;
    private String actualDevice;
    private boolean isAuto;
    private String port;
    private String time;

    public static SingletonConfigDevice getInstance() {
        if (instance == null) {
            instance = new SingletonConfigDevice();
        }

        return instance;
    }

    public void setAll(String objID, String actualDevice, boolean isAuto, String port) {
        this.objID = objID;
        this.actualDevice = actualDevice;
        this.isAuto = isAuto;
        this.port = port;
        this.time = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
        System.out.println(objID + actualDevice + isAuto + port + time);
    }
}
