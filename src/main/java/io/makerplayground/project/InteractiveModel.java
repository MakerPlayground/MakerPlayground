package io.makerplayground.project;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Compatibility;
import io.makerplayground.device.actual.DeviceType;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.*;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.generator.upload.UploadMode;
import io.makerplayground.generator.upload.UploadTarget;
import io.makerplayground.project.VirtualProjectDevice.Memory;
import io.makerplayground.project.expression.*;
import io.makerplayground.project.term.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InteractiveModel {

    private final Map<ProjectDevice, UserSetting> actionUserSettings = new HashMap<>();
    private final Map<ProjectDevice, UserSetting> conditionUserSettings = new HashMap<>();
    private final LinkedHashMap<ProjectDevice, List<Action>> actionMap = new LinkedHashMap<>();
    private final LinkedHashMap<ProjectDevice, LinkedHashMap<Condition, ReadOnlyBooleanWrapper>> conditionMap = new LinkedHashMap<>();
    private final LinkedHashMap<ProjectDevice, LinkedHashMap<Value, ReadOnlyStringWrapper>> valueMap = new LinkedHashMap<>();
    private final Project project;

    /* Cached device configuration at the time interactive mode is initialized */
    private final Map<ProjectDevice, String> deviceNameMap = new HashMap<>();   // keep the original device name for communicating with the firmware
    private final Map<String, ProjectDevice> nameDeviceMap = new HashMap<>();
    private ProjectConfiguration cachedConfiguration;

    private UploadTarget uploadTarget;
    private final ReadOnlyBooleanWrapper interactiveModeStarted = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper interactiveNeedReinitialize = new ReadOnlyBooleanWrapper();
    private final Runnable reinitializeCheckRunnable;
    private final BooleanProperty sensorReading = new SimpleBooleanProperty(true);
    private final IntegerProperty sensorReadingRate = new SimpleIntegerProperty(100);
    private final Map<ProjectDevice, Boolean> deviceValid = new HashMap<>();

    InteractiveModel(Project project) {
        this.project = project;
        this.reinitializeCheckRunnable = () -> {
            interactiveNeedReinitialize.set(!project.getProjectConfiguration().equals(cachedConfiguration));
            deviceValid.clear();
            for (ProjectDevice pd : project.getUnmodifiableProjectDevice()) {
                deviceValid.put(pd, isProjectDeviceStillTheSame(pd));
            }
        };
    }

    public boolean isDeviceValid(ProjectDevice projectDevice) {
        if (projectDevice instanceof VirtualProjectDevice) {
            return true;
        } else {
            return deviceValid.get(projectDevice);
        }
    }

    private boolean isProjectDeviceStillTheSame(ProjectDevice projectDevice) {
        Optional<ActualDevice> actualDevice = project.getProjectConfiguration().getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice);
        if (actualDevice.isEmpty()) {
            return false;
        }
        if (actualDevice.get().getDeviceType() == DeviceType.CONTROLLER) {
            return project.getProjectConfiguration().getController() == cachedConfiguration.getController();
        }
        // Command can be sent iff the following conditions are satisfy
        // 1. Current actual device selected is the same one as selected when start the interactive mode
        // 2. Current connection is the same as when start the interactive mode
        // 3. Current device property is the same as when start the interactive mode
        // 4. If the device is a cloud device, the cloud property should be the same as when start the interactive mode
        return (actualDevice.get() == cachedConfiguration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).orElse(null))
                && Objects.equals(project.getProjectConfiguration().getDeviceConnection(projectDevice), cachedConfiguration.getDeviceConnection(projectDevice))
                && Objects.equals(project.getProjectConfiguration().getUnmodifiableDevicePropertyValueMap().get(projectDevice)
                    , cachedConfiguration.getUnmodifiableDevicePropertyValueMap().get(projectDevice))
                && ((actualDevice.get().getCloudConsume() == null) || Objects.equals(project.getProjectConfiguration().getUnmodifiableCloudParameterMap().get(actualDevice.get().getCloudConsume())
                    , cachedConfiguration.getUnmodifiableCloudParameterMap().get(actualDevice.get().getCloudConsume())));
    }

    public UserSetting getOrCreateActionUserSetting(ProjectDevice projectDevice) {
        if (actionUserSettings.containsKey(projectDevice)) {
            return actionUserSettings.get(projectDevice);
        } else {
            if (projectDevice.getGenericDevice().hasAction()) {
                UserSetting userSetting = new UserSetting(project, projectDevice, projectDevice.getGenericDevice().getAction().get(0));
                actionUserSettings.put(projectDevice, userSetting);
                return userSetting;
            } else {
                throw new IllegalStateException("Device doesn't have any action");
            }
        }
    }

    public UserSetting getOrCreateConditionUserSetting(ProjectDevice projectDevice) {
        if (conditionUserSettings.containsKey(projectDevice)) {
            return conditionUserSettings.get(projectDevice);
        } else {
            if (projectDevice.getGenericDevice().hasCondition()) {
                UserSetting userSetting = new UserSetting(project, projectDevice, projectDevice.getGenericDevice().getCondition().get(0));
                conditionUserSettings.put(projectDevice, userSetting);
                return userSetting;
            } else {
                throw new IllegalStateException("Device doesn't have any action");
            }
        }
    }

    public boolean canSendCommand(ProjectDevice projectDevice, Action action) {
        // the firmware uploaded to the board support this device and this action and the device configuration hasn't been changed
        return actionMap.containsKey(projectDevice) && actionMap.get(projectDevice).contains(action) && isDeviceValid(projectDevice);
    }

    public Optional<ReadOnlyBooleanProperty> getConditionProperty(ProjectDevice projectDevice, Condition condition) {
        if (!conditionMap.containsKey(projectDevice) || !conditionMap.get(projectDevice).containsKey(condition)) {
            return Optional.empty();
        }
        return Optional.of(conditionMap.get(projectDevice).get(condition).getReadOnlyProperty());
    }

    public Optional<ReadOnlyStringProperty> getValueProperty(ProjectDevice projectDevice, Value value) {
        if (!valueMap.containsKey(projectDevice) || !valueMap.get(projectDevice).containsKey(value)) {
            return Optional.empty();
        }
        return Optional.of(valueMap.get(projectDevice).get(value).getReadOnlyProperty());
    }

    public List<ProjectValue> getProjectValues() {
        List<ProjectValue> projectValues = new ArrayList<>();
        for (ProjectDevice pd: valueMap.keySet()) {
            for(Value value: valueMap.get(pd).keySet()) {
                projectValues.add(new ProjectValue(pd, value));
            }
        }
        return projectValues;
    }

    public boolean isStarted() {
        return interactiveModeStarted.get();
    }

    public ReadOnlyBooleanProperty startedProperty() {
        return interactiveModeStarted.getReadOnlyProperty();
    }

    public boolean needReinitialize() {
        return interactiveNeedReinitialize.get();
    }

    public ReadOnlyBooleanProperty needReinitializeProperty() {
        return interactiveNeedReinitialize.getReadOnlyProperty();
    }

    public boolean getSensorReading() {
        return sensorReading.get();
    }

    public BooleanProperty sensorReadingProperty() {
        return sensorReading;
    }

    public int getSensorReadingRate() {
        return sensorReadingRate.get();
    }

    public IntegerProperty sensorReadingRateProperty() {
        return sensorReadingRate;
    }

    /**
     * This method must be called to initialize internal state.
     */
    private void initialize() {
        // check for precondition
        if (ProjectLogic.validateDeviceAssignment(project) != ProjectMappingResult.OK) {
            throw new IllegalStateException("Actual device and port must have been selected before creating InteractiveModel");
        }

        ProjectConfiguration configuration = project.getProjectConfiguration();

        // initialize storage for actions, conditions, values and current device selection

        // remove usersetting only if the project device has been removed or the actual device selected has changed so
        // that we can retain user setting from previous session
        for (ProjectDevice projectDevice : new ArrayList<>(actionUserSettings.keySet())) {
            if (!configuration.getUnmodifiableDeviceMap().containsKey(projectDevice)
                    || configuration.getUnmodifiableDeviceMap().get(projectDevice) != cachedConfiguration.getUnmodifiableDeviceMap().get(projectDevice)) {
                actionUserSettings.remove(projectDevice);
            }
        }
        for (ProjectDevice projectDevice : new ArrayList<>(conditionUserSettings.keySet())) {
            if (!configuration.getUnmodifiableDeviceMap().containsKey(projectDevice)
                    || configuration.getUnmodifiableDeviceMap().get(projectDevice) != cachedConfiguration.getUnmodifiableDeviceMap().get(projectDevice)) {
                conditionUserSettings.remove(projectDevice);
            }
        }

        // cache current project configuration
        deviceNameMap.clear();
        nameDeviceMap.clear();
        for (ProjectDevice pd : configuration.getUnmodifiableDeviceMap().keySet()) {
            deviceNameMap.put(pd, pd.getName());
            nameDeviceMap.put(pd.getName(), pd);
        }
        for (ProjectDevice pd : configuration.getUnmodifiableIdenticalDeviceMap().keySet()) {
            deviceNameMap.put(pd, pd.getName());
            nameDeviceMap.put(pd.getName(), pd);
        }
        cachedConfiguration = new ProjectConfiguration(configuration);

        configuration.addConfigurationChangedCallback(reinitializeCheckRunnable);
        reinitializeCheckRunnable.run();

        actionMap.clear();
        conditionMap.clear();
        valueMap.clear();

        for (List<ProjectDevice> projectDeviceList: project.getProjectDevicesGroupByActualDevice()) {
            for (ProjectDevice projectDevice : projectDeviceList) {
                if (configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).isEmpty()) {
                    continue;
                }
                ActualDevice actualDevice = configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).get();
                Compatibility compatibility = actualDevice.getCompatibilityMap().get(projectDevice.getGenericDevice());
                Set<Action> actions = compatibility.getDeviceAction().keySet();
                if (!actions.isEmpty()) {
                    actionMap.put(projectDevice, projectDevice.getGenericDevice().getAction());
                }
                Set<Condition> conditions = compatibility.getDeviceCondition().keySet();
                if (!conditions.isEmpty()) {
                    conditionMap.put(projectDevice, new LinkedHashMap<>());
                    for (Condition condition: conditions) {
                        conditionMap.get(projectDevice).put(condition, new ReadOnlyBooleanWrapper(false));
                    }
                }
                Set<Value> values = compatibility.getDeviceValue().keySet();
                if (!values.isEmpty()) {
                    valueMap.put(projectDevice, new LinkedHashMap<>());
                    for (Value value: values) {
                        valueMap.get(projectDevice).put(value, new ReadOnlyStringWrapper("0"));
                    }
                }
            }
        }

        /* Initialize the map for the virtual devices i.e. memory */
        actionMap.put(Memory.projectDevice, List.of(Memory.setValue));
        conditionMap.put(Memory.projectDevice, new LinkedHashMap<>());
        conditionMap.get(Memory.projectDevice).put(Memory.compare, new ReadOnlyBooleanWrapper(false));
        valueMap.put(Memory.projectDevice, new LinkedHashMap<>());
        project.getUnmodifiableVariable().forEach(projectValue -> valueMap.get(Memory.projectDevice).put(projectValue.getValue(), new ReadOnlyStringWrapper("")));
    }

    /*
     * We must freeze the project instance before calling method since the board as project may changed by user
     * while uploading interactive firmware but we want to initialize internal state based on the project status
     * at the time that the interactive firmware code was generated.
     */
    public void start(UploadTarget uploadTarget) {
        this.uploadTarget = uploadTarget;
        initialize();
        sensorReading.addListener(this::onSensorReadingChanged);
        sensorReadingRate.addListener(this::onSensorReadingRateChanged);
        UploadMode uploadMode = uploadTarget.getUploadMode();
        switch (uploadMode) {
            case SERIAL_PORT:
                startOnSerialPort(uploadTarget.getSerialPort());
                break;
            case RPI_ON_NETWORK:
                startOnRpiSocket(uploadTarget.getRpiHostName());
                break;
            default:
                throw new IllegalStateException("Not supported yet");
        }
        if (interactiveModeStarted.get()) {
            sendFreezeSensorCommand(sensorReading.get());
            sendReadingRateCommand(sensorReadingRate.get());
        }
    }

    private void startOnSerialPort(SerialPort serialPort) {
        // initialize and open the serial port
        serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.addDataListener(new SerialPortMessageListener() {
            @Override
            public byte[] getMessageDelimiter() {
                return new byte[]{'\r'};
            }

            @Override
            public boolean delimiterIndicatesEndOfMessage() {
                return true;
            }

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                String message = new String(event.getReceivedData()).strip();
                try {
                    processInMessage(message);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        if (serialPort.openPort()) {
            interactiveModeStarted.set(true);
        }
    }

    private WebSocketClient webSocketClient;
    private void startOnRpiSocket(String rpiHostName) {
        try {
            Thread.sleep(2000);
            URI rpiWsUrl = new URI("ws://" + rpiHostName + ":6213");
            webSocketClient = new WebSocketClient(rpiWsUrl) {

                @Override
                public void onOpen(ServerHandshake handshakedata) {

                }

                @Override
                public void onMessage(String message) {
                    String[] messageArray = message.split("\n");
                    for (String msg: messageArray) {
                        processInMessage(msg.strip());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Platform.runLater(() -> interactiveModeStarted.set(false));
                }

                @Override
                public void onError(Exception ex) {

                }
            };
            interactiveModeStarted.set(webSocketClient.connectBlocking(5, TimeUnit.SECONDS));
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            interactiveModeStarted.set(false);
        }
    }

    public void stop() {
        if (this.uploadTarget != null) {
            if (UploadMode.SERIAL_PORT.equals(this.uploadTarget.getUploadMode())) {
                SerialPort serialPort = uploadTarget.getSerialPort();
                if (serialPort != null && serialPort.isOpen()) {
                    if (!serialPort.closePort()) {
                        System.err.println("Warning: Serial Port can't be closed");
                    }
                }
            } else if (UploadMode.RPI_ON_NETWORK.equals(uploadTarget.getUploadMode())) {
                webSocketClient.close();
            } else {
                throw new IllegalStateException("Not supported yet");
            }
        }
        sensorReading.removeListener(this::onSensorReadingChanged);
        sensorReadingRate.removeListener(this::onSensorReadingRateChanged);
        project.getProjectConfiguration().removeConfigurationChangedCallback(reinitializeCheckRunnable);
        interactiveModeStarted.set(false);
    }

    public void setAndSendConditionParameterCommand(ProjectDevice projectDevice, Condition condition, Parameter parameter, Expression expression) {
        UserSetting setting = getOrCreateConditionUserSetting(projectDevice);
        if (!setting.getCondition().equals(condition)) {
            setting.setCondition(condition);
        }
        setting.getParameterMap().put(parameter, expression);

        List<String> args = new ArrayList<>();
        args.add("\"" + deviceNameMap.get(projectDevice) + "\"");
        args.add("\"" + condition.getName() + "\"");
        for (Parameter param : setting.getParameterMap().keySet()) {
            args.add("\"" + evaluateExpression(setting.getParameterMap().get(param)) + "\"");
        }
        String commandString = (String.join(" ", args) + "\r");
        sendCommand(commandString);
    }

    private void onSensorReadingChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        sendFreezeSensorCommand(newValue);
    }

    private void sendFreezeSensorCommand(boolean sensorReading) {
        if (sensorReading) {
            sendCommand("\"$\" \"Sensor\" \"Unfreeze\"\r");
        } else {
            sendCommand("\"$\" \"Sensor\" \"Freeze\"\r");
        }
    }

    private void onSensorReadingRateChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        sendReadingRateCommand(newValue.intValue());
    }

    private void sendReadingRateCommand(int rate) {
        sendCommand("\"$\" \"SensorRate\" \"" + rate + "\"\r");
    }

    public void sendActionCommand(UserSetting userSetting) {
        // memory values are computed and stored in the program side
        if (userSetting.getDevice().equals(Memory.projectDevice) && userSetting.getAction().equals(Memory.setValue)) {
            VariableExpression expression = (VariableExpression) userSetting.getParameterMap().get(Memory.nameParameter);
            expression.getProjectValue().ifPresent(projectValue ->
                    valueMap.get(Memory.projectDevice).get(projectValue.getValue()).set(
                            Double.toString(evaluateCustomNumberExpression((CustomNumberExpression) (userSetting.getParameterMap().get(Memory.valueParameter))))
                    )
            );
            return;
        }

        List<String> args = new ArrayList<>();
        args.add("\"" + deviceNameMap.get(userSetting.getDevice()) + "\"");
        args.add("\"" + userSetting.getAction().getName() + "\"");
        for (Parameter parameter : userSetting.getAction().getParameter()) {
            args.add("\"" + evaluateExpression(userSetting.getParameterMap().get(parameter)) + "\"");
        }
        String commandString = (String.join(" ", args) + "\r");
        sendCommand(commandString);
    }

    private void sendCommand(String commandString) {
        System.out.println(commandString);
        if (UploadMode.SERIAL_PORT.equals(this.uploadTarget.getUploadMode())) {
            SerialPort serialPort = this.uploadTarget.getSerialPort();
            if (isStarted() && serialPort != null && serialPort.isOpen()) {
                byte[] command = commandString.getBytes();
                serialPort.writeBytes(command, command.length);
            }
        }
    }

    private String evaluateExpression(Expression expression) {
        if (expression instanceof CustomNumberExpression) {
            return String.valueOf(evaluateCustomNumberExpression((CustomNumberExpression) expression));
        } else if (expression instanceof SimpleStringExpression) {
            return ((SimpleStringExpression) expression).getString();
        } else if (expression instanceof ComplexStringExpression) {
            StringBuilder sb = new StringBuilder();
            for (Expression e : ((ComplexStringExpression) expression).getSubExpressions()) {
                if (e instanceof CustomNumberExpression) {
                    sb.append(evaluateCustomNumberExpression((CustomNumberExpression) e));
                } else if (e instanceof SimpleStringExpression) {
                    sb.append(((SimpleStringExpression) e).getString());
                } else if (e instanceof ProjectValueExpression) {
                    ProjectValue pv = ((ProjectValueExpression) e).getProjectValue();
                    this.getValueProperty(pv.getDevice(), pv.getValue()).ifPresent(stringProperty -> sb.append(stringProperty.get()));
                } else {
                    throw new IllegalStateException();
                }
            }
            return sb.toString();
        } else if (expression instanceof RecordExpression) {
            StringBuilder sb = new StringBuilder();
            List<RecordEntry> entryList = ((RecordExpression) expression).getRecord().getEntryList();
            sb.append(entryList.stream().map(recordEntry -> {
                String str = "[" + recordEntry.getField() + ",";
                if (recordEntry.getValue() instanceof CustomNumberExpression) {
                    str += String.valueOf(evaluateCustomNumberExpression((CustomNumberExpression) recordEntry.getValue()));
                } else if (recordEntry.getValue() instanceof NumberWithUnitExpression) {
                    str += String.valueOf(((NumberWithUnitExpression) recordEntry.getValue()).getNumberWithUnit().getValue());
                } else if (recordEntry.getValue() instanceof ProjectValueExpression) {
                    ProjectValue projectValue = ((ProjectValueExpression) recordEntry.getValue()).getProjectValue();
                    ReadOnlyStringProperty currentValue = valueMap.get(projectValue.getDevice()).get(projectValue.getValue());
                    str += currentValue.get();
                } else {
                    throw new IllegalStateException();
                }
                return str + "]";
            }).collect(Collectors.joining()));
            return sb.toString();
        } else if (expression instanceof SimpleIntegerExpression) {
            return String.valueOf(((SimpleIntegerExpression) expression).getInteger());
        } else if (expression instanceof StringIntegerExpression) {
            return String.valueOf(((StringIntegerExpression) expression).getInteger());
        } else if (expression instanceof DotMatrixExpression) {
            return ((DotMatrixExpression) expression).getDotMatrix().getDataAsString();
        } else if (expression instanceof SimpleRTCExpression) {
            LocalDateTime rtc = ((SimpleRTCExpression) expression).getRealTimeClock().getLocalDateTime();
            return rtc.getYear()+","+rtc.getMonth().getValue()+","+rtc.getDayOfMonth()+","+rtc.getHour()+","+rtc.getMinute()+","+rtc.getSecond();
        }
        // ImageExpression
        throw new UnsupportedOperationException();
    }

    private double evaluateCustomNumberExpression(CustomNumberExpression expression) {
        Deque<Double> operandStack = new ArrayDeque<>();
        Deque<Operator> operatorStack = new ArrayDeque<>();

        List<Term> terms = expression.getTerms();
        int i = 0;
        while (i < terms.size()) {
            Term term = terms.get(i);
            if (term instanceof NumberWithUnitTerm) {
                operandStack.push(((NumberWithUnitTerm) term).getValue().getValue());
            } else if (term instanceof ValueTerm) {
                ProjectValue projectValue = ((ValueTerm) term).getValue();
                ReadOnlyStringProperty currentValue = valueMap.get(projectValue.getDevice()).get(projectValue.getValue());
                operandStack.push(Double.parseDouble(currentValue.get()));
            } else if (term instanceof OperatorTerm) {
                Operator operator = ((OperatorTerm) term).getValue();
                if (operatorStack.isEmpty() || operator == Operator.OPEN_PARENTHESIS || operator == Operator.MULTIPLY
                        || operator == Operator.DIVIDE || operator == Operator.MOD) {
                    operatorStack.push(operator);
                } else if (operator == Operator.PLUS || operator == Operator.MINUS) {
                    Operator stackTop = operatorStack.peek();
                    if (stackTop == Operator.MULTIPLY || stackTop == Operator.DIVIDE || stackTop == Operator.MOD) {
                        operandStack.push(evaluateCustomNumberExpressionProcess(operandStack, operatorStack));
                        continue;
                    } else {
                        operatorStack.push(operator);
                    }
                } else if (operator == Operator.CLOSE_PARENTHESIS) {
                    Operator stackTop = operatorStack.peek();
                    if (stackTop == Operator.OPEN_PARENTHESIS) {
                        operatorStack.pop();
                    } else {
                        operandStack.push(evaluateCustomNumberExpressionProcess(operandStack, operatorStack));
                        continue;
                    }
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new IllegalStateException();
            }
            i++;
        }

        while (!operatorStack.isEmpty()) {
            operandStack.push(evaluateCustomNumberExpressionProcess(operandStack, operatorStack));
        }

        if (operandStack.size() != 1) {
            throw new IllegalStateException();
        }

        return operandStack.pop();
    }

    private double evaluateCustomNumberExpressionProcess(Deque<Double> operandStack, Deque<Operator> operatorStack) {
        Operator operator = operatorStack.pop();
        double rightOperand = operandStack.pop();
        double leftOperand = operandStack.pop();
        if (operator == Operator.PLUS) {
            return leftOperand + rightOperand;
        } else if (operator == Operator.MINUS) {
            return leftOperand - rightOperand;
        } else if (operator == Operator.MULTIPLY) {
            return leftOperand * rightOperand;
        } else if (operator == Operator.DIVIDE) {
            return leftOperand / rightOperand;
        } else if (operator == Operator.MOD) {
            return leftOperand % rightOperand;
        } else {
            throw new IllegalStateException();
        }
    }

    void processInMessage(String message) {
        System.out.println(message);
        if (!sensorReading.get()) {
            return;
        }
        List<String> args = Arrays.stream(message.split("[\\u0000\"]")).filter(s->!s.isBlank()).collect(Collectors.toList());
        ProjectDevice projectDevice = nameDeviceMap.get(args.get(0));
        if (projectDevice == null) {
            System.err.println("Unknown message : " + args);
            return;
        }
        Platform.runLater(() -> {
            int argsIndex = 1;
            if (conditionMap.containsKey(projectDevice)) {
                for (Condition condition : conditionMap.get(projectDevice).keySet()) {
                    if (condition.getName().equals("Compare")) {
                        continue;
                    }
                    conditionMap.get(projectDevice).get(condition).set(!args.get(argsIndex).equals("0"));
                    argsIndex++;
                }
            }
            if (valueMap.containsKey(projectDevice)) {
                for (Value value : valueMap.get(projectDevice).keySet()) {
                    valueMap.get(projectDevice).get(value).set(args.get(argsIndex));
                    argsIndex++;
                }
            }
        });
    }
}