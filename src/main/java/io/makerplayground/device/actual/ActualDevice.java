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

package io.makerplayground.device.actual;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.Constraint;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Represent an actual device/board ex. Arduino UNO, SparkFun 9DoF IMU Breakout, DHT22 temperature/humidity sensor, etc.
 */
@JsonDeserialize(using = ActualDeviceDeserializer.class)
public class ActualDevice {

    private final String id;
    private final String brand;
    private final String model;
    private final String url;
    private final double width;
    private final double height;

    private final DeviceType deviceType;    // CONTROLLER, PERIPHERAL, DEVICE (MOTOR, SPEAKER)
    private final FormFactor formFactor;    // BREAKOUT_BOARD, SHIELD, STANDALONE
    private final Map<Platform, String> classnames;
    private final Map<Platform, List<String>> externalLibraries;
    private final CloudPlatform cloudPlatform;
    private final String pioBoardId;
    private final WiringMethod wiringMethod;

    private final List<DevicePort> port;     // port names with their function ex. "0" : {"UART1": "RX", "GPIO_1": "INOUT"}
    // and port position and type ex. WIRE, GROOVE_3PIN
    private final List<Peripheral> connectivity;    // possible connection for peripheral (at lease one should be selected) ex. I2C1, SPI1
    // or list of connection available for controller.
    // shield will contain empty list as every pin must be connected

    //private final Map<GenericDevice, Action> supportedAction;
    private final Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedAction;  // action support for each generic device
    private final Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedCondition;  // action support for each generic device
    private final Map<GenericDevice, Map<Value, Constraint>> supportedValue;                   // value supported for each generic device
    private final Map<CloudPlatform, CloudPlatformLibrary> supportedCloudPlatform;          // optional value for microcontroller
    private final List<Property> property;

    private final List<IntegratedActualDevice> integratedDevices;

    //private final Map<String, List<String>> dependency;     // list of device that depend on this device ex. speakers that can be used with this amp
    // or an amplifier for a thermistor

    /**
     * Construct a new device. The constructor should only be invoked by the DeviceLibrary
     * in order to rebuild the library from file.
     *
     * @param brand           brand of this device ex. Sparkfun
     * @param model           model of this device ex. SparkFun 9DoF IMU Breakout
     * @param url             url to produce description page ex. https://www.sparkfun.com/products/13284
     */
    ActualDevice(String id, String brand, String model, String url, double width, double height, DeviceType deviceType
            , String pioBoardId
            , WiringMethod wiringMethod
            , FormFactor formFactor
            , Map<Platform, String> classnames
            , Map<Platform, List<String>> externalLibraries
            , CloudPlatform cloudPlatform
            , List<DevicePort> port
            , List<Peripheral> connectivity
            , Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedAction
            , Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedCondition
            , Map<GenericDevice, Map<Value, Constraint>> supportedValue
            , List<Property> property
            , Map<CloudPlatform, CloudPlatformLibrary> supportedCloudPlatform
            , List<IntegratedActualDevice> integratedDevices) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.url = url;
        this.width = width;
        this.height = height;
        this.deviceType = deviceType;
        this.pioBoardId = pioBoardId;
        this.wiringMethod = wiringMethod;
        this.formFactor = formFactor;
        this.classnames = classnames;
        this.externalLibraries = externalLibraries;
        this.cloudPlatform = cloudPlatform;
        this.port = port;
        this.connectivity = Collections.unmodifiableList(connectivity);
        this.supportedAction = supportedAction;
        this.supportedCondition = supportedCondition;
        this.supportedValue = supportedValue;
        this.property = Collections.unmodifiableList(property);
        this.supportedCloudPlatform = supportedCloudPlatform;
        this.integratedDevices = Collections.unmodifiableList(integratedDevices);

        // TODO: check for duplicate peripheral between port

        if (deviceType == DeviceType.CONTROLLER) {
            List<Peripheral> gpioList = new ArrayList<>(Peripheral.values(ConnectionType.GPIO));
            List<Peripheral> pwmList = new ArrayList<>(Peripheral.values(ConnectionType.PWM));
            List<Peripheral> analogList = new ArrayList<>(Peripheral.values(ConnectionType.ANALOG));
            List<Peripheral> uartList = new ArrayList<>(Peripheral.values(ConnectionType.UART));

            // remove peripheral that has been used in case that this device has both normal port and splittable port
            gpioList.removeAll(connectivity);
            pwmList.removeAll(connectivity);
            analogList.removeAll(connectivity);
            uartList.removeAll(connectivity);

            Map<DevicePort, Set<Peripheral>> portToPeripheralMap = new HashMap<>();
            Map<Peripheral, Peripheral> oldToNewPeripheralMap = new HashMap<>();
            for (DevicePort devicePort : port) {
                if (devicePort.getType() == DevicePortType.INTERNAL || devicePort.getType() == DevicePortType.VIRTUAL) {
                    continue;
                }
                Map<Peripheral, Peripheral> splitMap = new HashMap<>();
                try {
                    for (Peripheral peripheral : devicePort.getPeripheral()) {
                        if (peripheral.getConnectionType().isGPIO()) {
                            splitMap.put(peripheral, gpioList.remove(0));
                        } else if (peripheral.getConnectionType().isPWM()) {
                            splitMap.put(peripheral, pwmList.remove(0));
                        } else if (peripheral.getConnectionType().isAnalog()) {
                            splitMap.put(peripheral, analogList.remove(0));
                        } else if (peripheral.getConnectionType().isI2C()) {
                            splitMap.put(peripheral, Peripheral.I2C_1);
                        } else if (peripheral.getConnectionType().isUART()) {
                            splitMap.put(peripheral, uartList.remove(0));
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalStateException("There aren't any free peripheral left for creating a new split port. Please add new peripherals in Peripheral.java");
                }
                if (!splitMap.isEmpty()) {
                    portToPeripheralMap.put(devicePort, splitMap.keySet());
                    oldToNewPeripheralMap.putAll(splitMap);
                }
            }

            for (DevicePort originalPort : portToPeripheralMap.keySet()) {
                List<DevicePort.DevicePortFunction> firstPortFunction = new ArrayList<>();
                List<DevicePort.DevicePortFunction> secondPortFunction = new ArrayList<>();

                for (Peripheral splitPeripheral : portToPeripheralMap.get(originalPort)) {
                    Peripheral newPeripheral = oldToNewPeripheralMap.get(splitPeripheral);
                    connectivity.add(newPeripheral);

                    List<Peripheral> newConflictPeripheral = new ArrayList<>();
                    for (Peripheral conflictPeripheral : originalPort.getConflictPeripheral(splitPeripheral)) {
                        newConflictPeripheral.add(conflictPeripheral);                          // add conflict to peripherals that the original peripheral conflicts to
                        if (oldToNewPeripheralMap.containsKey(conflictPeripheral)) {
                            newConflictPeripheral.add(oldToNewPeripheralMap.get(conflictPeripheral));   // if that peripheral is converted into new peripheral, we are conflicted to it to
                        }
                    }
                    newConflictPeripheral.addAll(originalPort.getPeripheral());  // add conflict to original peripherals as both can't be used simultaneously

                    if (splitPeripheral.getConnectionType() == ConnectionType.MP_I2C || splitPeripheral.getConnectionType() == ConnectionType.GROVE_I2C) {
                        firstPortFunction.add(new DevicePort.DevicePortFunction(newPeripheral, newConflictPeripheral, PinType.I2C_SCL));
                        secondPortFunction.add(new DevicePort.DevicePortFunction(newPeripheral, newConflictPeripheral, PinType.I2C_SDA));
                    } else if (splitPeripheral.getConnectionType() == ConnectionType.INEX_I2C) {
                        if (originalPort.isSDA()) {
                            firstPortFunction.add(new DevicePort.DevicePortFunction(newPeripheral, newConflictPeripheral, PinType.I2C_SDA));
                        } else {
                            firstPortFunction.add(new DevicePort.DevicePortFunction(newPeripheral, newConflictPeripheral, PinType.I2C_SCL));
                        }
                    } else if (splitPeripheral.getConnectionType() == ConnectionType.GROVE_UART) {
                        firstPortFunction.add(new DevicePort.DevicePortFunction(newPeripheral, newConflictPeripheral, PinType.UART_RX));
                        secondPortFunction.add(new DevicePort.DevicePortFunction(newPeripheral, newConflictPeripheral, PinType.UART_TX));
                    } else if (splitPeripheral.isSingle()) {
                        firstPortFunction.add(new DevicePort.DevicePortFunction(newPeripheral, newConflictPeripheral, PinType.INOUT));
                    } else if (splitPeripheral.isDual()) {
                        secondPortFunction.add(new DevicePort.DevicePortFunction(newPeripheral, newConflictPeripheral, PinType.INOUT));
                    }
                }

                List<Peripheral> newPeripheral = portToPeripheralMap.get(originalPort).stream().map(oldToNewPeripheralMap::get).collect(Collectors.toList());
                for (Peripheral originalPeripheral : originalPort.getPeripheral()) {
                    originalPort.addConflictPeripheral(originalPeripheral, newPeripheral);  // add conflict to the new peripheral as both can't be used simultaneously
                }

                String name = originalPort.getName() + "_1";
                List<String> alias;
                if (!originalPort.getAlias().isEmpty()) {
                    alias = List.of(originalPort.getAlias().get(0));
                } else {
                    alias = Collections.emptyList();
                }
                port.add(new DevicePort(name, alias, DevicePortType.WIRE, null, firstPortFunction, 0, 0, 0, 0, 0, originalPort));
                if (!secondPortFunction.isEmpty()) {
                    name = originalPort.getName() + "_2";
                    if (!originalPort.getAlias().isEmpty()) {
                        alias = List.of(originalPort.getAlias().get(1));
                    } else {
                        alias = Collections.emptyList();
                    }
                    port.add(new DevicePort(name, alias, DevicePortType.WIRE, null, secondPortFunction, 0, 0, 0, 0, 0, originalPort));
                }
            }
        }
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

    public String getPlatformIOBoardId() {
        return pioBoardId;
    }

    public WiringMethod getWiringMethod() {
        return wiringMethod;
    }

    public FormFactor getFormFactor() {
        return formFactor;
    }

    public Set<Platform> getSupportedPlatform() {
//        return supportedPlatform;
        return classnames.keySet();
    }

    public CloudPlatform getCloudPlatform() {
        return cloudPlatform;
    }

    public String getMpLibrary(Platform p) {
        return classnames.get(p);
    }

    public List<String> getExternalLibrary(Platform p) {
        return externalLibraries.get(p);
    }

    public List<Property> getProperty() { return property; }

    public Property getProperty(String name) {
        for (Property p : property) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }


    public Set<CloudPlatform> getSupportedCloudPlatform() {
        return supportedCloudPlatform.keySet();
    }

    public String getCloudPlatformLibraryName(CloudPlatform cloudPlatform) {
        return supportedCloudPlatform.get(cloudPlatform).getClassName();
    }

    public List<String> getCloudPlatformLibraryDependency(CloudPlatform cloudPlatform) {
        return supportedCloudPlatform.get(cloudPlatform).getDependency();
    }

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
        return port.stream().filter(devicePort -> devicePort.hasPeripheral(peripheral)).collect(Collectors.toList());
    }

    public List<DevicePort> getPort(ConnectionType connectionType) {
        return port.stream().filter(devicePort -> devicePort.hasConnectionType(connectionType)).collect(Collectors.toList());
    }

    public List<List<DevicePort>> getI2CPort() {
        List<List<DevicePort>> result = new ArrayList<>();

        // consider actual (these are usually connect with a breadboard so we can just pick any port regardless of the distance between port)
        Optional<DevicePort> sdaPort = port.stream().filter(DevicePort::isSDA)
                .filter(devicePort -> devicePort.getParent() == null).findFirst();
        Optional<DevicePort> sclPort = port.stream().filter(DevicePort::isSCL)
                .filter(devicePort -> devicePort.getParent() == null).findFirst();
        if (sdaPort.isPresent() && sclPort.isPresent()) {
            result.add(List.of(sclPort.get(), sdaPort.get()));
        }

        // consider split port
        Map<DevicePort, List<DevicePort>> sdaPortMap = port.stream().filter(DevicePort::isSDA)
                .filter(devicePort -> devicePort.getParent() != null).collect(groupingBy(DevicePort::getParent));
        Map<DevicePort, List<DevicePort>> sclPortMap = port.stream().filter(DevicePort::isSCL)
                .filter(devicePort -> devicePort.getParent() != null).collect(groupingBy(DevicePort::getParent));
        // type of sdaPortMap and sclPortMap are actually Map<DevicePort, DevicePort> as there shouldn't be more than 1
        // sda/scl pin sharing the same parent but groupingBy is designed to return a list so we live with it and use get(0)
        if (sdaPortMap.values().stream().mapToInt(Collection::size).anyMatch(v -> v != 1)
                && sclPortMap.values().stream().mapToInt(Collection::size).anyMatch(v -> v != 1)) {
            throw new IllegalStateException();
        }
        // case 1: SDA and SCL pins come from the same port e.g. makerplayground or grove 4 pin port
        for (DevicePort parent : new HashSet<>(sclPortMap.keySet())) {
            if (sdaPortMap.containsKey(parent)) {
                DevicePort scl = sclPortMap.get(parent).get(0);
                DevicePort sda = sdaPortMap.get(parent).get(0);
                result.add(List.of(scl, sda));
                sclPortMap.remove(parent);
            }
        }
        // case 2: SDA and SCL pins come from different port e.g. INEX 3 pin port
        // In this case, we iterate over the unmapped scl pin left from case 1 and map it to every sda port left
        for (DevicePort parent : sclPortMap.keySet()) {
            DevicePort scl = sclPortMap.get(parent).get(0);
            sdaPortMap.values().stream().flatMap(Collection::stream).forEach(sda -> result.add(List.of(scl, sda)));
        }

        return result;
    }

    public List<Peripheral> getConnectivity() {
        return connectivity;
    }

    public Map<GenericDevice, Map<Value, Constraint>> getSupportedValue() {
        return supportedValue;
    }

    public Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> getSupportedAction() {
        return supportedAction;
    }

    public boolean isSupport(ActualDevice controller, GenericDevice genericDevice, Map<Action, Map<Parameter, Constraint>> theirMap) {
        // skip port type test for virtual and integrated device
        if (deviceType != DeviceType.VIRTUAL && deviceType != DeviceType.INTEGRATED) {
            Set<DevicePortType> controllerPortType = controller.getPort().stream().map(DevicePort::getType)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(DevicePortType.class)));
            Set<DevicePortType> devicePortType = getPort().stream().map(DevicePort::getType)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(DevicePortType.class)));
            // this device is supported if and only if its ports has the same type as some ports of the controller
            devicePortType.retainAll(controllerPortType);
            if (devicePortType.isEmpty()) {
                return false;
            }
        }

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

    public List<IntegratedActualDevice> getIntegratedDevices() {
        return integratedDevices;
    }

    public Optional<IntegratedActualDevice> getIntegratedDevices(String actualDeviceName) {
        return integratedDevices.stream().filter(integratedActualDevice ->
                integratedActualDevice.getModel().equals(actualDeviceName)).findFirst();
    }
    @Override
    public String toString() {
        return "ActualDevice{" +
                "id='" + id + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", url='" + url + '\'' +
                ", deviceType=" + deviceType +
                ", formFactor=" + formFactor +
                ", supportedPlatform=" + getSupportedPlatform() +
                ", cloudPlatform=" + cloudPlatform +
                ", port=" + port +
                ", connectivity=" + connectivity +
                ", supportedAction=" + supportedAction +
                ", supportedValue=" + supportedValue +
                ", property=" + property +
                '}';
    }
}
