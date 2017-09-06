package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonLaunch {
    private static SingletonLaunch instance = null;

    private SingletonLaunch() {}

    private String openTime;
    private String closeTime;

    public static SingletonLaunch getInstance() {
        if (instance == null) {
            instance = new SingletonLaunch();
        }

        return instance;
    }

    public void launchProgram() {
        openTime = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
        System.out.println("launch time = " + openTime);
    }

    public void closeProgram() {
        closeTime = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
        System.out.println("close time = " + closeTime);
    }


}
