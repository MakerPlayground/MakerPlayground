package io.makerplayground.helper;

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
}
