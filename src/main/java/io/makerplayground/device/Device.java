/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.device;

import java.util.Collections;
import java.util.Map;

/**
 * Represent an actual device/board ex. SparkFun 9DoF IMU Breakout, DHT22 temperature/humidity sensor, etc.
 */
public class Device {
    private final String brand;
    private final String model;
    private final String url;
    private final Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedAction;
    private final Map<GenericDevice, Map<Value, Constraint>>  supportedValue;

    /**
     * Construct a new device. The constructor should only be invoked by the DeviceLibrary
     * in order to rebuild the library from file.
     * @param brand brand of this device ex. Sparkfun
     * @param model model of this device ex. SparkFun 9DoF IMU Breakout
     * @param url url to produce description page ex. https://www.sparkfun.com/products/13284
     * @param supportedAction
     * @param supportedValue
     */
    Device(String brand, String model, String url
            , Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedAction
            , Map<GenericDevice, Map<Value, Constraint>> supportedValue) {
        this.brand = brand;
        this.model = model;
        this.url = url;
        this.supportedAction = Collections.unmodifiableMap(supportedAction);
        this.supportedValue = Collections.unmodifiableMap(supportedValue);
    }

    /**
     * Get brand of this device
     * @return brand of this device ex. Sparkfun, Adafruit, etc.
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Get model of this device
     * @return model of this device ex. Sparkfun
     */
    public String getModel() {
        return model;
    }

    /**
     * Get the url to the product description page on manufacturer website
     * @return url to manufacturer website
     */
    public String getUrl() {
        return url;
    }

}
