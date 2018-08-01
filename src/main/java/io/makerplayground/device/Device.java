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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.helper.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represent an actual device/board ex. Arduino UNO, SparkFun 9DoF IMU Breakout, DHT22 temperature/humidity sensor, etc.
 */
@JsonDeserialize(using = DeviceDeserializer.class)
public class Device {

    enum Dependency {
        SPEAKER, AMPLIFIER
    }

    private final String id;
    private final String brand;
    private final String model;
    private final String url;
    private final double width;
    private final double height;
    private final double v;
    private final double i;
    private final double w;
    private final Dependency dependency;
    private final Dependency category;
    private final String classname;
    private final List<String> sourceToInclude;
    private final List<String> libraryDependency;

    private final DeviceType deviceType;    // CONTROLLER, PERIPHERAL, DEVICE (MOTOR, SPEAKER)
    private final FormFactor formFactor;    // BREAKOUT_BOARD, SHIELD, STANDALONE
    private final Set<Platform> supportedPlatform;          // ARDUINO, ARM, RPI_LINUX, RPI_WIN10, GROOVE_ARDUINO

    private final List<DevicePort> port;     // port names with their function ex. "0" : {"UART1": "RX", "GPIO_1": "INOUT"}
    // and port position and type ex. WIRE, GROOVE_3PIN
    private final List<Peripheral> connectivity;    // possible connection for peripheral (at lease one should be selected) ex. I2C1, SPI1
    // or list of connection available for controller.
    // shield will contain empty list as every pin must be connected

    private final Map<GenericDevice, Integer> supportedDevice;                                  // generic device(s) supported and number of instance available
    //private final Map<GenericDevice, Action> supportedAction;
    private final Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedAction;  // action support for each generic device
    private final Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedCondition;  // action support for each generic device
    private final Map<GenericDevice, Map<Value, Constraint>> supportedValue;                   // value supported for each generic device


    //private final Map<String, List<String>> dependency;     // list of device that depend on this device ex. speakers that can be used with this amp
    // or an amplifier for a thermistor

    /**
     * Construct a new device. The constructor should only be invoked by the DeviceLibrary
     * in order to rebuild the library from file.
     *
     * @param brand           brand of this device ex. Sparkfun
     * @param model           model of this device ex. SparkFun 9DoF IMU Breakout
     * @param url             url to produce description page ex. https://www.sparkfun.com/products/13284
     * @param supportedAction
     * @param supportedValue
     */
    Device(String id, String brand, String model, String url, double width, double height, DeviceType deviceType, FormFactor formFactor
            , String classname
            , List<String> sourceToInclude
            , List<String> libraryDependency
            , Set<Platform> supportedPlatform
            , List<DevicePort> port
            , List<Peripheral> connectivity
            , Map<GenericDevice, Integer> supportedDevice
            , Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedAction
            , Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedCondition
            , Map<GenericDevice, Map<Value, Constraint>> supportedValue
            , Dependency dependency
            , Dependency category
            , double v
            , double i
            , double w ) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.url = url;
        this.width = width;
        this.height = height;
        this.deviceType = deviceType;
        this.formFactor = formFactor;
        this.classname = classname;
        this.sourceToInclude = sourceToInclude;
        this.libraryDependency = libraryDependency;
        this.supportedPlatform = supportedPlatform;
        this.port = port;
        this.connectivity = Collections.unmodifiableList(connectivity);
        this.supportedDevice = supportedDevice;
        this.supportedAction = supportedAction;
        this.supportedCondition = supportedCondition;
        this.supportedValue = supportedValue;
        this.dependency = dependency;
        this.v = v;
        this.i = i;
        this.w = w;
        this.category = category;
    }

    public double getV() {
        return v;
    }

    public double getI() {
        return i;
    }

    public double getW() {
        return w;
    }

    public Dependency getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    /**
     * Get brand of this device
     *
     * @return brand of this device ex. Sparkfun, Adafruit, etc.
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Get model of this device
     *
     * @return model of this device ex. Sparkfun
     */
    public String getModel() {
        return model;
    }

    /**
     * Get the url to the product description page on manufacturer website
     *
     * @return url to manufacturer website
     */
    public String getUrl() {
        return url;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public FormFactor getFormFactor() {
        return formFactor;
    }

    public Set<Platform> getSupportedPlatform() {
        return supportedPlatform;
    }

    public String getClassname() {
        return classname;
    }

    public List<String> getSourceToInclude() {
        return sourceToInclude;
    }

    public List<String> getLibraryDependency() {
        return libraryDependency;
    }

    /*public String getMPLibraryName() {
        for (String s : sourceToInclude) {
            if (s.startsWith("MP_")) {
                return s;
            }
        }
        return null;
    }*/

    /*public String getSuggestedInstanceName() {
        return getMPLibraryName().substring(3);
    }*/

    public List<DevicePort> getPort() {
        return port;
    }

    public DevicePort getPort(String name) {
        for (DevicePort dp : port) {
            if (dp.getName().equals(name))
                return dp;
        }
        return null;
    }

    public List<DevicePort> getPort(Peripheral peripheral) {
        return port.stream().filter(new Predicate<DevicePort>() {
            @Override
            public boolean test(DevicePort devicePort) {
                return devicePort.getFunction().stream().anyMatch(new Predicate<DevicePort.DevicePortFunction>() {
                    @Override
                    public boolean test(DevicePort.DevicePortFunction devicePortFunction) {
                        return devicePortFunction.getPeripheral() == peripheral;
                    }
                });
            }
        }).collect(Collectors.toList());
    }

    public List<Peripheral> getConnectivity() {
        return connectivity;
    }

    public Map<GenericDevice, Integer> getSupportedDevice() {
        return supportedDevice;
    }

    public Map<GenericDevice, Map<Value, Constraint>> getSupportedValue() {
        return supportedValue;
    }

    public Dependency getDependency() {
        return dependency;
    }

    public Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> getSupportedAction() {
        return supportedAction;
    }

    public boolean isSupport(GenericDevice genericDevice, Map<Action, Map<Parameter, Constraint>> theirMap) {
        if (!supportedAction.containsKey(genericDevice)) {
            return false;
        }

        Map<Action, Map<Parameter, Constraint>> actionMapActualDevice = supportedAction.get(genericDevice);
        for (Action action : theirMap.keySet()) {
            if (!actionMapActualDevice.containsKey(action)) {
                return false;
            }

            Map<Parameter, Constraint> parameterMapActualDevice = actionMapActualDevice.get(action);
            for (Parameter parameter : theirMap.get(action).keySet()) {
                if (!parameterMapActualDevice.containsKey(parameter)) {
                    return false;
                }
                if (theirMap.get(action).get(parameter) != null && !parameterMapActualDevice.get(parameter).isCompatible(theirMap.get(action).get(parameter))) {
                    return false;
                }
            }
        }

        return true;
    }



    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", url='" + url + '\'' +
                ", deviceType=" + deviceType +
                ", formFactor=" + formFactor +
                ", supportedPlatform=" + supportedPlatform +
                ", port=" + port +
                ", connectivity=" + connectivity +
                ", supportedDevice=" + supportedDevice +
                ", supportedAction=" + supportedAction +
                ", supportedValue=" + supportedValue +
                ", dependency=" + dependency +
                '}';
    }
}
