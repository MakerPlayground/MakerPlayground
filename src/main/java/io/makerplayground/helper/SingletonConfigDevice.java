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

/**
 * Created by tanyagorn on 9/6/2017.
 */
public class SingletonConfigDevice {
    private static SingletonConfigDevice instance = null;

    private SingletonConfigDevice() {}

    private String objID;
    private String actualDevice;
    private int isAuto;
    private String port;
    private long time;

    public static SingletonConfigDevice getInstance() {
        if (instance == null) {
            instance = new SingletonConfigDevice();
        }

        return instance;
    }

    public void setAll(String objID, String actualDevice, boolean isAuto, String port) {
        this.objID = objID;
        this.actualDevice = actualDevice;
        if (isAuto)
            this.isAuto = 1;
        else
            this.isAuto = 0;
        this.port = port;
        this.time = new Date().getTime();

        String command = "insert into ConfigDevice (App_ID, ObjectID, RealHW, isAuto, Port, Time) values('"
                + SingletonConnectDB.getINSTANCE().getUuid() + "','" + objID + "','"
                + actualDevice + "'," + this.isAuto + ",'" + port + "'," + time + ")";
        SingletonConnectDB.getINSTANCE().execute(command);

        //System.out.println(objID + actualDevice + isAuto + port + time);
    }
}
