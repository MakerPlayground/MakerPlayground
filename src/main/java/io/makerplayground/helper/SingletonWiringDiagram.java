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
 * Created by tanyagorn on 9/7/2017.
 */
public class SingletonWiringDiagram {
    private static SingletonWiringDiagram instance = null;

    private SingletonWiringDiagram() {}

    private Date openTime;
    private long openTimeToDB;
    private Date closeTime;
    private long duration;

    public static SingletonWiringDiagram getInstance() {
        if (instance == null) {
            instance = new SingletonWiringDiagram();
        }
        return instance;
    }


    public void setOpenTime() {
        this.openTime = new Date();
        this.openTimeToDB = new Date().getTime();
    }

    public void setCloseTime() {
        this.closeTime = new Date();
        duration = Singleton.getInstance().getDateDiff(openTime, closeTime, TimeUnit.SECONDS);
        String command = "insert into WiringDiagram (App_ID, OpenTime, Duration) values('"
                + SingletonConnectDB.getINSTANCE().getUuid() + "'," + openTimeToDB + ","
                + duration + ")";
        SingletonConnectDB.getINSTANCE().execute(command);
    }

}
