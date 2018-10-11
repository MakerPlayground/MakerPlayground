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
public class SingletonError {
    private static SingletonError instance = null;

    private SingletonError() {}

    private String error;
    private long time;

    public static SingletonError getInstance() {
        if (instance == null) {
            instance = new SingletonError();
        }

        return instance;
    }

    public void setAll(String error) {
        this.error = error;
        this.time = new Date().getTime();


        //System.out.println("error = " + error);
        String command = "insert into Error (App_ID, Time, Message) values('"
                + SingletonConnectDB.getINSTANCE().getUuid() + "'," + time + ",\""
                + error + "\")";
        SingletonConnectDB.getINSTANCE().execute(command);
    }
}
