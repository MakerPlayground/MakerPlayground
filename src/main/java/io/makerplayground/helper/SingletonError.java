package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonError {
    private static SingletonError instance = null;

    private SingletonError() {}

    private String error;
    private String time;

    public static SingletonError getInstance() {
        if (instance == null) {
            instance = new SingletonError();
        }

        return instance;
    }

    public void setAll(String error) {
        this.error = error;
        this.time = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
        System.out.println("error = " + error + time);
    }
}
