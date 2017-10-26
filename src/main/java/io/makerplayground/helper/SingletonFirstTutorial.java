package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonFirstTutorial {
    private static SingletonFirstTutorial instance = null;

    private SingletonFirstTutorial() {}

    private int whichPage = 1;
    private String openTime;
    private String closeTime;

    public static SingletonFirstTutorial getInstance() {
        if (instance == null) {
            instance = new SingletonFirstTutorial();
        }

        return instance;
    }

    public void openTime() {
        openTime = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
        //System.out.println("first " + openTime);
    }

    public void closeTime() {
        closeTime = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
        //System.out.println("close " + closeTime);
    }

    public void increaseWhichPage() {
        whichPage++;
        //System.out.println(whichPage);
    }

    public void decreaseWhichPage() {
        whichPage--;
        //System.out.println(whichPage);
    }
}
