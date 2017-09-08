package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by tanyagorn on 9/7/2017.
 */
public class SingletonWiringDiagram {
    private static SingletonWiringDiagram instance = null;

    private SingletonWiringDiagram() {}

    private Date openTime;
    private long openTimeToDB;
    private Date closeTime;
    private long duration;

    public static SingletonWiringDiagram getInstance() {
        if (instance == null) {
            instance = new SingletonWiringDiagram();
        }
        return instance;
    }


    public void setOpenTime() {
        this.openTime = new Date();
        this.openTimeToDB = new Date().getTime();
    }

    public void setCloseTime() {
        this.closeTime = new Date();
        duration = Singleton.getInstance().getDateDiff(openTime, closeTime, TimeUnit.SECONDS);
        String command = "insert into WiringDiagram (App_ID, OpenTime, Duration) values('Add ID 1'," + openTimeToDB + ","
                + duration + ")";
        SingletonConnectDB.getInstance().execute(command);
    }

}
