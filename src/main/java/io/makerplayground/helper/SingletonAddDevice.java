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
public class SingletonAddDevice {
    private static SingletonAddDevice instance = null;

    private SingletonAddDevice() {}

    private String type;
    private String objID;
    private long time;
    private String operation;

    public static SingletonAddDevice getInstance() {
        if (instance == null) {
            instance = new SingletonAddDevice();
        }

        return instance;
    }

    public void setAll(String type, String objID) {
        this.type = type;
        this.objID = objID;
        this.time = new Date().getTime();
        this.operation = "ADD";

        String command = "insert into AddorDelDevice (App_ID, Action, Object_ID, OP, Time) values('"
                + SingletonConnectDB.getINSTANCE().getUuid() + "','" + operation + "','"
                + objID + "','" + type + "'," + time + ")";
        SingletonConnectDB.getINSTANCE().execute(command);
    }
}
