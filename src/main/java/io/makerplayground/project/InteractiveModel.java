package io.makerplayground.project;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.project.expression.*;
import io.makerplayground.project.term.*;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.*;

public class InteractiveModel implements SerialPortMessageListener {

    private final Map<ProjectDevice, Map<Condition, ReadOnlyBooleanWrapper>> conditionMap = new HashMap<>();
    private final Map<ProjectDevice, Map<Value, ReadOnlyDoubleWrapper>> valueMap = new HashMap<>();
    private final Map<ProjectDevice, List<Action>> actionMap = new HashMap<>();

    private final Project project;
    private SerialPort serialPort;
    private final ReadOnlyBooleanWrapper interactiveModeStarted = new ReadOnlyBooleanWrapper();

    public InteractiveModel(Project project) {
        this.project = project;
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
     * This method must be called to initialize internal state before calling the start method every time. We must called this
     * method before start uploading interactive firmware to the board as project may changed while uploading by user but we want
     * to initialize internal state based on the project status at the time that the interactive firmware code was generated.
     */
    public void initialize() {
        // check for precondition
        if (ProjectLogic.validateDeviceAssignment(project) != ProjectMappingResult.OK) {
            throw new IllegalStateException("Actual device and port must have been selected before creating InteractiveModel");
        }

        // initialize storage for conditions and values
        conditionMap.clear();
        valueMap.clear();
        actionMap.clear();
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            if (projectDevice.getGenericDevice().hasCondition()) {
                conditionMap.put(projectDevice, new HashMap<>());
                for (Condition condition : projectDevice.getGenericDevice().getCondition()) {
                    conditionMap.get(projectDevice).put(condition, new ReadOnlyBooleanWrapper(false));
                }
            }
            if (projectDevice.getGenericDevice().hasValue()) {
                valueMap.put(projectDevice, new HashMap<>());
                for (Value value : projectDevice.getGenericDevice().getValue()) {
                    valueMap.get(projectDevice).put(value, new ReadOnlyDoubleWrapper(0.0));
                }
            }
            if (projectDevice.getGenericDevice().hasAction()) {
                actionMap.put(projectDevice, new ArrayList<>(projectDevice.getGenericDevice().getAction()));
            }
        }
    }

    public boolean start(SerialPort serialPort) {
        // initialize and open the serial port
        this.serialPort = serialPort;
        serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.addDataListener(this);
        if (serialPort.openPort()) {
            interactiveModeStarted.set(true);
            return true;
        }
        return false;
    }

    public void stop() {
        if (serialPort != null && serialPort.isOpen()) {
            if (!serialPort.closePort()) {
                System.err.println("Warning: Serial Port can't be closed");
            }
        }
        interactiveModeStarted.set(false);
    }

    public void sendCommand(UserSetting userSetting) {
        if (isStarted() && serialPort != null && serialPort.isOpen()) {
            List<String> args = new ArrayList<>();
            args.add("\"" + userSetting.getDevice().getName() + "\"");
            args.add("\"" + userSetting.getAction().getName() + "\"");
            for (Parameter parameter : userSetting.getAction().getParameter()) {
                args.add("\"" + evaluateExpression(userSetting.getParameterMap().get(parameter)) + "\"");
            }

            byte[] command = (String.join(" ", args) + "\r").getBytes();
//            System.out.println(new String(command));
            serialPort.writeBytes(command, command.length);
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
        }
        // SimpleRTCExpression, ImageExpression, RecordExpression
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
//        System.out.println(message.strip());
        String[] args = message.split(" ");

        project.getAllDeviceUsed().stream()
                .filter(projectDevice -> projectDevice.getName().equals(args[0]))
                .findAny()
                .ifPresent(projectDevice ->
                        Platform.runLater(() -> {
                            List<Condition> conditions = projectDevice.getGenericDevice().getCondition();
                            List<Value> values = projectDevice.getGenericDevice().getValue();

                            int expectedArgumentCount = conditions.size() + values.size();
                            if (conditions.stream().anyMatch(condition -> condition.getName().equals("Compare"))) {
                                expectedArgumentCount -= 1;
                            }
                            if (expectedArgumentCount != args.length - 1 ) {
                                return;
                            }

                            int argsIndex = 1;
                            for (Condition condition : conditions) {
                                if (condition.getName().equals("Compare")) {
                                    continue;
                                }
                                conditionMap.get(projectDevice).get(condition).set(args[argsIndex].equals("1"));
                                argsIndex++;
                            }
                            for (Value value : values) {
                                valueMap.get(projectDevice).get(value).set(Double.parseDouble(args[argsIndex]));
                                argsIndex++;
                            }
                        })
                );
    }

    public boolean hasCommand(ProjectDevice projectDevice, Action action) {
        return actionMap.containsKey(projectDevice) && actionMap.get(projectDevice).contains(action);
    }
}