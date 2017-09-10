package io.makerplayground.helper;

import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonConfigDevice {
    private static SingletonConfigDevice instance = null;

    private SingletonConfigDevice() {}

    private String objID;
    private String actualDevice;
    private int isAuto;
    private String port;
    private long time;

    public static SingletonConfigDevice getInstance() {
        if (instance == null) {
            instance = new SingletonConfigDevice();
        }

        return instance;
    }

    public void setAll(String objID, String actualDevice, boolean isAuto, String port) {
        this.objID = objID;
        this.actualDevice = actualDevice;
        if (isAuto)
            this.isAuto = 1;
        else
            this.isAuto = 0;
        this.port = port;
        this.time = new Date().getTime();

        String command = "insert into ConfigDevice (App_ID, ObjectID, RealHW, isAuto, Port, Time) values('"
                + SingletonConnectDB.getINSTANCE().getUuid() + "','" + objID + "','"
                + actualDevice + "'," + this.isAuto + ",'" + port + "'," + time + ")";
        SingletonConnectDB.getINSTANCE().execute(command);

        //System.out.println(objID + actualDevice + isAuto + port + time);
    }
}
