package io.makerplayground.helper;

import java.util.Date;

/**
 * Created by tanyagorn on 9/8/2017.
 */
public class SingletonDeviceURL {
    private static SingletonDeviceURL instance = null;

    private SingletonDeviceURL() {}

    private String url;
    private long time;

    public static SingletonDeviceURL getInstance() {
        if (instance == null) {
            instance = new SingletonDeviceURL();
        }
        return instance;
    }

    public void setAll(String url) {
        this.url = url;
        this.time = new Date().getTime();

        String command = "insert into ClickURL (App_ID, Time, URL) values('"
                + SingletonConnectDB.getINSTANCE().getUuid() + "'," + time + ",'"
                + url + "')";
        SingletonConnectDB.getINSTANCE().execute(command);
    }
}
