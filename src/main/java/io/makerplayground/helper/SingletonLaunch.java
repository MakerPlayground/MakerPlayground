package io.makerplayground.helper;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonLaunch {
    private static SingletonLaunch instance = null;

    private SingletonLaunch() {}

    private Date open;
    private Date close;
    private long openTime;
    private long duration;

    public static SingletonLaunch getInstance() {
        if (instance == null) {
            instance = new SingletonLaunch();
        }

        return instance;
    }

    public void launchProgram() {
        open = new Date();
        openTime = new Date().getTime();
    }

    public void closeProgram() {
        close = new Date();
        duration = Singleton.getInstance().getDateDiff(open, close, TimeUnit.SECONDS);
        String command = "insert into Launching (App_ID, OpenTime, Duration) values('"
                + SingletonConnectDB.getINSTANCE().getUuid() + "'," + openTime + ","
                + duration + ")";
        SingletonConnectDB.getINSTANCE().execute(command);
    }


}
