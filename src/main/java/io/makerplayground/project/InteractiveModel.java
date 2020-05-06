package io.makerplayground.project;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Compatibility;
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
import java.util.stream.Stream;

public class InteractiveModel {

    private final Map<ProjectDevice, UserSetting> actionUserSettings = new HashMap<>();
    private final Map<ProjectDevice, UserSetting> conditionUserSettings = new HashMap<>();
    private final Map<ProjectDevice, ActualDevice> deviceMap = new HashMap<>();
    private final Map<ProjectDevice, ProjectDevice> identicalDeviceMap = new HashMap<>();
    private final LinkedHashMap<ProjectDevice, List<Action>> actionMap = new LinkedHashMap<>();
    private final LinkedHashMap<ProjectDevice, LinkedHashMap<Condition, ReadOnlyBooleanWrapper>> conditionMap = new LinkedHashMap<>();
    private final LinkedHashMap<ProjectDevice, LinkedHashMap<Value, ReadOnlyDoubleWrapper>> valueMap = new LinkedHashMap<>();
    private final Project project;

    private UploadTarget uploadTarget;
    private final ReadOnlyBooleanWrapper interactiveModeStarted = new ReadOnlyBooleanWrapper();
    private final BooleanProperty sensorReading = new SimpleBooleanProperty(true);
    private final IntegerProperty sensorReadingRate = new SimpleIntegerProperty(100);

    InteractiveModel(Project project) {
        this.project = project;
    }

    public UserSetting getOrCreateActionUserSetting(ProjectDevice projectDevice) {
        if (actionUserSettings.containsKey(projectDevice)) {
            return actionUserSettings.get(projectDevice);
        } else {
            ActualDevice actualDevice = findActualDevice(projectDevice);
            if (actualDevice != null) {
                Set<Action> deviceActions = actualDevice.getCompatibilityMap().get(projectDevice.getGenericDevice()).getDeviceAction().keySet();
                if (!deviceActions.isEmpty()) {
                    UserSetting userSetting = new UserSetting(project, projectDevice, deviceActions.iterator().next());
                    actionUserSettings.put(projectDevice, userSetting);
                    return userSetting;
                }
            }
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
            ActualDevice actualDevice = findActualDevice(projectDevice);
            if (actualDevice != null) {
                Set<Condition> deviceConditions = actualDevice.getCompatibilityMap().get(projectDevice.getGenericDevice()).getDeviceCondition().keySet();
                if (!deviceConditions.isEmpty()) {
                    UserSetting userSetting = new UserSetting(project, projectDevice, deviceConditions.iterator().next());
                    conditionUserSettings.put(projectDevice, userSetting);
                    return userSetting;
                }
            }
            if (projectDevice.getGenericDevice().hasCondition()) {
                UserSetting userSetting = new UserSetting(project, projectDevice, projectDevice.getGenericDevice().getCondition().get(0));
                conditionUserSettings.put(projectDevice, userSetting);
                return userSetting;
            } else {
                throw new IllegalStateException("Device doesn't have any action");
            }
        }
    }

    public boolean hasCommand(ProjectDevice projectDevice, Action action) {
        return actionMap.containsKey(projectDevice) && actionMap.get(projectDevice).contains(action);
    }

    public Optional<ReadOnlyBooleanProperty> getConditionProperty(ProjectDevice projectDevice, Condition condition) {
        if (!conditionMap.containsKey(projectDevice) || !conditionMap.get(projectDevice).containsKey(condition)) {
            return Optional.empty();
        }
        return Optional.of(conditionMap.get(projectDevice).get(condition).getReadOnlyProperty());
    }

    public Optional<ReadOnlyDoubleProperty> getValueProperty(ProjectDevice projectDevice, Value value) {
        if (!valueMap.containsKey(projectDevice) || !valueMap.get(projectDevice).containsKey(value)) {
            return Optional.empty();
        }
        return Optional.of(valueMap.get(projectDevice).get(value).getReadOnlyProperty());
    }

    public boolean isStarted() {
        return interactiveModeStarted.get();
    }

    public ReadOnlyBooleanProperty startedProperty() {
        return interactiveModeStarted.getReadOnlyProperty();
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
                    || configuration.getUnmodifiableDeviceMap().get(projectDevice) != deviceMap.get(projectDevice)) {
                actionUserSettings.remove(projectDevice);
            }
        }
        for (ProjectDevice projectDevice : new ArrayList<>(conditionUserSettings.keySet())) {
            if (!configuration.getUnmodifiableDeviceMap().containsKey(projectDevice)
                    || configuration.getUnmodifiableDeviceMap().get(projectDevice) != deviceMap.get(projectDevice)) {
                conditionUserSettings.remove(projectDevice);
            }
        }

        deviceMap.clear();
        deviceMap.putAll(configuration.getDeviceMap());

        identicalDeviceMap.clear();
        identicalDeviceMap.putAll(configuration.getUnmodifiableIdenticalDeviceMap());

        actionMap.clear();
        conditionMap.clear();
        valueMap.clear();

        for (List<ProjectDevice> projectDeviceList: project.getAllDevicesGroupBySameActualDevice()) {
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
                        valueMap.get(projectDevice).put(value, new ReadOnlyDoubleWrapper(0.0));
                    }
                }
            }
        }

        /* Initialize the map for the virtual devices i.e. memory */
        actionMap.put(Memory.projectDevice, List.of(Memory.setValue));
        conditionMap.put(Memory.projectDevice, new LinkedHashMap<>());
        conditionMap.get(Memory.projectDevice).put(Memory.compare, new ReadOnlyBooleanWrapper(false));
        valueMap.put(Memory.projectDevice, new LinkedHashMap<>());
        project.getUnmodifiableVariable().forEach(projectValue -> valueMap.get(Memory.projectDevice).put(projectValue.getValue(), new ReadOnlyDoubleWrapper(0.0)));
    }

    public ActualDevice findActualDevice(ProjectDevice projectDevice) {
        if (deviceMap.containsKey(projectDevice)) {
            return deviceMap.get(projectDevice);
        }
        while (!deviceMap.containsKey(projectDevice)) {
            if (identicalDeviceMap.containsKey(projectDevice)) {
                projectDevice = identicalDeviceMap.get(projectDevice);
            } else {
                return null;
            }
        }
        return deviceMap.get(projectDevice);
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
        interactiveModeStarted.set(false);
    }

    public void setAndSendConditionParameterCommand(ProjectDevice projectDevice, Condition condition, Parameter parameter, Expression expression) {
        UserSetting setting = getOrCreateConditionUserSetting(projectDevice);
        if (!setting.getCondition().equals(condition)) {
            setting.setCondition(condition);
        }
        setting.getParameterMap().put(parameter, expression);

        List<String> args = new ArrayList<>();
        args.add("\"" + projectDevice.getName() + "\"");
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
                            evaluateCustomNumberExpression((CustomNumberExpression) (userSetting.getParameterMap().get(Memory.valueParameter)))
                    )
            );
            return;
        }

        List<String> args = new ArrayList<>();
        args.add("\"" + userSetting.getDevice().getName() + "\"");
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
        } else if (UploadMode.RPI_ON_NETWORK.equals(this.uploadTarget.getUploadMode())) {
            if (isStarted() && webSocketClient.isOpen()) {
                webSocketClient.send(commandString);
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
                    ReadOnlyDoubleProperty currentValue = valueMap.get(projectValue.getDevice()).get(projectValue.getValue());
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
            return ((DotMatrixExpression) expression).getDotMatrix().getBase16String();
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
                ReadOnlyDoubleProperty currentValue = valueMap.get(projectValue.getDevice()).get(projectValue.getValue());
                operandStack.push(currentValue.get());
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
        List<String> args = Arrays.stream(message.split("[ \"]")).filter(s->!s.isBlank()).collect(Collectors.toList());
        Stream.concat(valueMap.keySet().stream(), conditionMap.keySet().stream())
            .filter(projectDevice -> !args.isEmpty() && projectDevice.getName().equals(args.get(0)))
            .findAny()
            .ifPresent(projectDevice ->
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
                            valueMap.get(projectDevice).get(value).set(Double.parseDouble(args.get(argsIndex)));
                            argsIndex++;
                        }
                    }
                })
            );
    }
}