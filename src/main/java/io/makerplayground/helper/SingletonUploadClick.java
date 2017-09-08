package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/7/2017.
 */
public class SingletonUploadClick {
    private static SingletonUploadClick instance = null;

    private SingletonUploadClick() {}

    private long time;

    public static SingletonUploadClick getInstance() {
        if (instance == null) {
            instance = new SingletonUploadClick();
        }

        return instance;
    }

    public void click() {
        this.time = new Date().getTime();
        String command = "insert into UploadClick (App_ID, Time) values('Add ID 1'," + time + ")";
        SingletonConnectDB.getInstance().execute(command);
    }
}
