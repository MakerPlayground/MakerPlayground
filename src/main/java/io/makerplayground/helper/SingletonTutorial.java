package io.makerplayground.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonTutorial {
    private static SingletonTutorial instance = null;

    private SingletonTutorial() {}

    private int clickCount = 0;
    private int whichPage = 1;
    private String openTime;
    private String closeTime;

    public static SingletonTutorial getInstance() {
        if (instance == null) {
            instance = new SingletonTutorial();
        }

        return instance;
    }

    public void clickCount() {
        clickCount++;
    }

    public void openTime() {
        openTime = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
        System.out.println(openTime);
    }

    public void closeTime() {
        closeTime = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
        System.out.println(closeTime);
    }

    public void increaseWhichPage() {
        whichPage++;
        System.out.println(whichPage);
    }

    public void decreaseWhichPage() {
        whichPage--;
        System.out.println(whichPage);
    }
}
