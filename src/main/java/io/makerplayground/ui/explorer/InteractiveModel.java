package io.makerplayground.ui.explorer;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.DeviceMapperResult;
import io.makerplayground.generator.source.ArduinoCodeGenerator;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.UserSetting;
import io.makerplayground.project.expression.*;
import io.makerplayground.project.term.*;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.*;

public class InteractiveModel implements SerialPortMessageListener {

    private final Map<ProjectDevice, UserSetting> userSettings = new HashMap<>();
    private final Map<ProjectDevice, Map<Action, ReadOnlyBooleanWrapper>> conditionMap = new HashMap<>();
    private final Map<ProjectDevice, Map<Value, ReadOnlyDoubleWrapper>> valueMap = new HashMap<>();

    private final Project project;
    private SerialPort serialPort;
    private final BooleanProperty interactiveModeInitialized = new SimpleBooleanProperty();

    public InteractiveModel(Project project) {
        this.project = project;

        // check for precondition
        if (DeviceMapper.validateDeviceAssignment(project) != DeviceMapperResult.OK) {
            throw new IllegalStateException("Actual device and port must have been selected before creating InteractiveModel");
        }
        // initialize user setting
        for (ProjectDevice projectDevice : project.getDevice())
        {
            if (projectDevice.getGenericDevice().hasAction()) {
                userSettings.put(projectDevice, new UserSetting(projectDevice, projectDevice.getGenericDevice().getAction().get(0)));
            }
        }
        // initialize condition and value
        for (ProjectDevice projectDevice : project.getDevice()) {
            if (projectDevice.getGenericDevice().hasCondition()) {
                conditionMap.put(projectDevice, new HashMap<>());
                for (Action action : projectDevice.getGenericDevice().getCondition()) {
                    conditionMap.get(projectDevice).put(action, new ReadOnlyBooleanWrapper(false));
                }
            }
            if (projectDevice.getGenericDevice().hasValue()) {
                valueMap.put(projectDevice, new HashMap<>());
                for (Value value : projectDevice.getGenericDevice().getValue()) {
                    valueMap.get(projectDevice).put(value, new ReadOnlyDoubleWrapper(0.0));
                }
            }
        }
    }

    public UserSetting getUserSetting(ProjectDevice projectDevice) {
        return userSettings.get(projectDevice);
    }

    public ReadOnlyBooleanProperty getConditionProperty(ProjectDevice projectDevice, Action action) {
        return conditionMap.get(projectDevice).get(action).getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty getValueProperty(ProjectDevice projectDevice, Value value) {
        return valueMap.get(projectDevice).get(value).getReadOnlyProperty();
    }

    public boolean isInitialized() {
        return interactiveModeInitialized.get();
    }

    public BooleanProperty initializeProperty() {
        return interactiveModeInitialized;
    }

    public void setInitialized(boolean b) {
        interactiveModeInitialized.set(b);
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public void sendCommand(ProjectDevice projectDevice) {
        if (isInitialized() && serialPort != null && serialPort.isOpen()) {
            UserSetting userSetting = userSettings.get(projectDevice);

            List<String> args = new ArrayList<>();
            args.add("\"" + ArduinoCodeGenerator.parseDeviceVariableName(projectDevice) + "\"");
            args.add("\"" + userSetting.getAction().getName() + "\"");
            for (Parameter parameter : userSetting.getAction().getParameter()) {
                args.add("\"" + evaluateExpression(userSetting.getValueMap().get(parameter)) + "\"");
            }


            byte[] command = (String.join(" ", args) + "\r").getBytes();
            System.out.println(new String(command));
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

        project.getDevice().stream()
                .filter(projectDevice -> ArduinoCodeGenerator.parseDeviceVariableName(projectDevice).equals(args[0]))
                .findAny()
                .ifPresent(projectDevice ->
                    Platform.runLater(() -> {
                        List<Action> conditions = projectDevice.getGenericDevice().getCondition();
                        List<Value> values = projectDevice.getGenericDevice().getValue();

                        int expectedArgumentCount = conditions.size() + values.size();
                        if (conditions.stream().anyMatch(condition -> condition.getName().equals("Compare"))) {
                            expectedArgumentCount -= 1;
                        }
                        if (expectedArgumentCount != args.length - 1 ) {
                            return;
                        }

                        int argsIndex = 1;
                        for (Action condition : conditions) {
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
}
