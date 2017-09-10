package io.makerplayground.helper;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonTutorial {
    private static SingletonTutorial instance = null;

    private SingletonTutorial() {}

    private int clickCount = 0;
    private int isClick = 0;
    private int whichPage = 1;
    private Date open;
    private Date close;
    private long openTime;
    private long duration;

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
        open = new Date();
        openTime = new Date().getTime();
    }

    public void closeTime() {
        close = new Date();
        duration = Singleton.getInstance().getDateDiff(open, close, TimeUnit.SECONDS);
        String command = "insert into Tutorial (App_ID, isClick, Page, OpenTime, Duration) values('"
                + SingletonConnectDB.getINSTANCE().getUuid() + "'," + isClick + "," + whichPage + "," + openTime + ","
                + duration + ")";
        SingletonConnectDB.getINSTANCE().execute(command);
    }

    public void increaseWhichPage() {
        whichPage++;
    }

    public void decreaseWhichPage() {
        whichPage--;
    }

    // 0 means first tutorial, 1 means user click by himself
    public void setIsClick(int isClick) {
        this.isClick = isClick;
    }
}
