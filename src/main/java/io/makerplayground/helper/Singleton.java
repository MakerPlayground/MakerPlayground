package io.makerplayground.helper;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class Singleton {
    private static Singleton instance = null;

    private Singleton() {}

    // For first tutorial when program is opened
    private boolean flagFirstTime = false;

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }

        return instance;
    }

    public boolean isFlagFirstTime() {
        return flagFirstTime;
    }

    public void setFlagFirstTime(boolean flagFirstTime) {
        this.flagFirstTime = flagFirstTime;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }
}
