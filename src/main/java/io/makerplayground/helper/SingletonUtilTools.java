package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonUtilTools {
    private static SingletonUtilTools instance = null;

    private SingletonUtilTools() {}

    private String type;
    private String time;

    public static SingletonUtilTools getInstance() {
        if (instance == null) {
            instance = new SingletonUtilTools();
        }

        return instance;
    }

    public void setAll(String type) {
        this.type = type;
        this.time = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
    }

}
