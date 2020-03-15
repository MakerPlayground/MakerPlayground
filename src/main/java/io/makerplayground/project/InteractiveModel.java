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
import io.makerplayground.project.expression.*;
import io.makerplayground.project.term.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InteractiveModel {

    private final Map<ProjectDevice, UserSetting> userSettings = new HashMap<>();
    private final Map<ProjectDevice, ActualDevice> deviceMap = new HashMap<>();
    private final LinkedHashMap<ProjectDevice, List<Action>> actionMap = new LinkedHashMap<>();
    private final LinkedHashMap<ProjectDevice, LinkedHashMap<Condition, ReadOnlyBooleanWrapper>> conditionMap = new LinkedHashMap<>();
    private final LinkedHashMap<ProjectDevice, LinkedHashMap<Value, ReadOnlyDoubleWrapper>> valueMap = new LinkedHashMap<>();
    private final Project project;

    private UploadTarget uploadTarget;
    private final ReadOnlyBooleanWrapper interactiveModeStarted = new ReadOnlyBooleanWrapper();

    InteractiveModel(Project project) {
        this.project = project;
    }

    public UserSetting getOrCreateUserSetting(ProjectDevice projectDevice) {
        if (userSettings.containsKey(projectDevice)) {
            return userSettings.get(projectDevice);
        } else {
            if (projectDevice.getGenericDevice().hasAction()) {
                UserSetting userSetting = new UserSetting(projectDevice, projectDevice.getGenericDevice().getAction().get(0));
                userSettings.put(projectDevice, userSetting);
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
        for (ProjectDevice projectDevice : new ArrayList<>(userSettings.keySet())) {
            if (!configuration.getUnmodifiableDeviceMap().containsKey(projectDevice)
                    || configuration.getUnmodifiableDeviceMap().get(projectDevice) != deviceMap.get(projectDevice)) {
                userSettings.remove(projectDevice);
            }
        }

        deviceMap.clear();
        deviceMap.putAll(configuration.getDeviceMap());

        actionMap.clear();
        conditionMap.clear();
        valueMap.clear();
        for (ProjectDevice projectDevice : project.getUnmodifiableProjectDevice()) {
            configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).ifPresent(actualDevice -> {
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
            });
        }
    }

    /*
     * We must freeze the project instance before calling method since the board as project may changed by user
     * while uploading interactive firmware but we want to initialize internal state based on the project status
     * at the time that the interactive firmware code was generated.
     */
    public void start(UploadTarget uploadTarget) {
        this.uploadTarget = uploadTarget;
        initialize();
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
                processInMessage(message);
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
        interactiveModeStarted.set(false);
    }

    public void sendCommand(UserSetting userSetting) {
        List<String> args = new ArrayList<>();
        args.add("\"" + userSetting.getDevice().getName() + "\"");
        args.add("\"" + userSetting.getAction().getName() + "\"");
        for (Parameter parameter : userSetting.getAction().getParameter()) {
            args.add("\"" + evaluateExpression(userSetting.getParameterMap().get(parameter)) + "\"");
        }
        String commandString = (String.join(" ", args) + "\r");

        if (UploadMode.SERIAL_PORT.equals(this.uploadTarget.getUploadMode())) {
            SerialPort serialPort = this.uploadTarget.getSerialPort();
            if (isStarted() && serialPort != null && serialPort.isOpen()) {
                byte[] command = commandString.getBytes();
                serialPort.writeBytes(command, command.length);
            }
        } else if (UploadMode.RPI_ON_NETWORK.equals(this.uploadTarget.getUploadMode())) {
            webSocketClient.send(commandString);
        } else {
            throw new IllegalStateException("Not supported yet");
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
        }
        // SimpleRTCExpression, ImageExpression
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
//        System.out.println(message);
        List<String> args = Arrays.stream(message.split("[ \"]")).filter(s->!s.isBlank()).collect(Collectors.toList());

        deviceMap.keySet().stream()
            .filter(projectDevice -> projectDevice.getName().equals(args.get(0)))
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