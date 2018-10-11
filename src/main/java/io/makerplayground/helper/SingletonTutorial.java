/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        whichPage = 1;
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
