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
