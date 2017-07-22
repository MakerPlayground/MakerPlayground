package io.makerplayground.generator;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sun.org.apache.bcel.internal.generic.NEW;
import io.makerplayground.device.DevicePort;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.device.Parameter;
import io.makerplayground.device.Value;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.*;

/**
 *
 */
public class Sourcecode {

    public enum Error {
        NONE(""),
        NOT_FOUND_SCENE_OR_CONDITION("Can't find any scene or condition connect to the begin node"),
        MULT_DIRECT_CONN_TO_SCENE("Found multiple direct connection to the same scene"),
        NEST_CONDITION("Multiple condition are connected together");

        private final String description;

        Error(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private String code;
    private Error error;
    private String location;

    private Sourcecode(String code) {
        this.code = code;
    }

    private Sourcecode(Error error, String location) {
        this.error = error;
        this.location = location;
    }

    public String getCode() {
        return code;
    }

    public Error getError() {
        return error;
    }

    public String getLocation() {
        return location;
    }

    private static final String INDENT = "    ";
    private static final String NEW_LINE = "\n";
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static Sourcecode generateCode(Project project) {
        //Begin begin = project.getBegin();

        StringBuilder sb = new StringBuilder();

        Scene currentScene = null;
        Queue<Scene> queue = new ArrayDeque<>();

        // get all adjacent vertices which may be another scene(s) or another condition(s)
        List<NodeElement> adjacentVertices = findAdjacentVertices(project, project.getBegin());
        List<Scene> adjacentScene = getScene(adjacentVertices);
        List<Condition> adjacentCondition = getCondition(adjacentVertices);

        // generate include
        for (GenericDevice genericDevice : project.getAllDeviceTypeUsed()) {
            sb.append("#include \"MP_").append(genericDevice.getName().replace(" ", "_")).append(".h\"").append(NEW_LINE);
        }
        sb.append(NEW_LINE);
        sb.append("void (*currentScene)(void);").append(NEW_LINE);

        // instantiate object(s) for each device
        sb.append(NEW_LINE);
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            sb.append("MP_").append(projectDevice.getGenericDevice().getName().replace(" ", "_")).append(" ")
                    .append(projectDevice.getName().replace(" ", "_")).append("(");
            List<String> portName = new ArrayList<>();
            for (Peripheral peripheral : projectDevice.getDeviceConnection().values()) {
                List<String> tmp = project.getController().getController().getPort(peripheral).stream()
                        .map(DevicePort::getName).collect(Collectors.toList());
                portName.addAll(tmp);
            }
            sb.append(String.join(",", portName));
            sb.append(")").append(";").append(NEW_LINE);
        }

        // generate setup function
        sb.append(NEW_LINE);
        sb.append("void setup() {").append(NEW_LINE);
        sb.append(INDENT).append("Serial.begin(115200);").append(NEW_LINE);
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            sb.append(INDENT).append(projectDevice.getName().replace(" ", "_")).append(".init();").append(NEW_LINE);
        }
        sb.append(INDENT).append("currentScene = beginScene;").append(NEW_LINE);
        sb.append("}").append(NEW_LINE);

        // generate loop function
        sb.append(NEW_LINE);
        sb.append("void loop() {").append(NEW_LINE);
        sb.append(INDENT).append("currentScene();").append(NEW_LINE);
        sb.append("}").append(NEW_LINE);

        // generate code for begin
        sb.append(NEW_LINE);
        sb.append("void beginScene() {").append(NEW_LINE);
        if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
            if (adjacentScene.size() == 1) {
                Scene s = adjacentScene.get(0);
                sb.append(INDENT).append("currentScene = ").append(s.getName().replace(" ", "_")).append(";").append(NEW_LINE);
                queue.add(s);
            } else {
                return new Sourcecode(Error.MULT_DIRECT_CONN_TO_SCENE, "beginScene");
            }
        } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
            Error error = processCondition(sb, queue, project, adjacentCondition);
            if (error != Error.NONE) {
                return new Sourcecode(error, "beginScene");
            }
        } else {
            return new Sourcecode(Error.NOT_FOUND_SCENE_OR_CONDITION, "beginScene");
        }
        sb.append("}").append(NEW_LINE);

        // generate function for each scene
        while (!queue.isEmpty()) {
            currentScene = queue.remove();

            // create function header
            sb.append(NEW_LINE);
            sb.append("void ").append(currentScene.getName().replace(" ", "_")).append("() {").append(NEW_LINE);

            // do action
            for (UserSetting setting : currentScene.getSetting()) {
                sb.append(INDENT).append(setting.getDevice().getName().replace(" ", "_")).append(".")
                        .append(setting.getAction().getFunctionName()).append("(");
                List<String> params = new ArrayList<>();
                for (Parameter parameter : setting.getAction().getParameter()) {
                    Object value = setting.getValueMap().get(parameter);
                    if (value instanceof NumberWithUnit) {
                        params.add(df.format(((NumberWithUnit) value).getValue()));
                    } else if (value instanceof String) {
                        params.add("\"" + value + "\"");
                    }
                }
                sb.append(String.join(", ", params)).append(");").append(NEW_LINE);
            }

            // delay
            if (currentScene.getDelay() != 0) {
                if (currentScene.getDelayUnit() == Scene.DelayUnit.Second) {
                    sb.append(INDENT).append("delay(").append(df.format(currentScene.getDelay() * 1000))
                            .append(");").append(NEW_LINE);
                } else if (currentScene.getDelayUnit() == Scene.DelayUnit.MilliSecond) {
                    sb.append(INDENT).append("delay(").append(df.format(currentScene.getDelay())).append(");")
                            .append(NEW_LINE);
                }
            }

            // update list of adjacent vertices (scenes/conditions)
            adjacentVertices = findAdjacentVertices(project, currentScene);
            adjacentScene = getScene(adjacentVertices);
            adjacentCondition = getCondition(adjacentVertices);

            if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                if (adjacentScene.size() == 1) {
                    Scene s = adjacentScene.get(0);
                    queue.add(s);
                    sb.append(INDENT).append("currentScene = ").append(s.getName().replace(" ", "_")).append(";").append(NEW_LINE);
                } else {
                    return new Sourcecode(Error.MULT_DIRECT_CONN_TO_SCENE, currentScene.getName().replace(" ", "_"));
                }
            } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
                Error error = processCondition(sb, queue, project, adjacentCondition);
                if (error != Error.NONE) {
                    return new Sourcecode(error, currentScene.getName().replace(" ", "_"));
                }
            } else {
                sb.append(INDENT).append("currentScene = beginScene;").append(NEW_LINE);
            }

            // end of scene's function
            sb.append("}").append(NEW_LINE);
        }

        System.out.println(sb);
        return new Sourcecode(sb.toString());
    }

    private static Error processCondition(StringBuilder sb, Queue<Scene> queue, Project project, List<Condition> adjacentCondition) {
        // gather every value used by every condition connect to the current scene
        Map<ProjectDevice, Set<Value>> valueUsed = new HashMap<>();
        for (Condition condition : adjacentCondition) {
            for (UserSetting setting : condition.getSetting()) {
                Map<ProjectDevice, Set<Value>> tmp = setting.getAllValueUsed();
                // merge tmp into valueUsed
                for (ProjectDevice projectDevice : tmp.keySet()) {
                    if (!valueUsed.containsKey(projectDevice)) {
                        valueUsed.put(projectDevice, new HashSet<>());
                    }
                    valueUsed.get(projectDevice).addAll(tmp.get(projectDevice));
                }
            }
        }

        // declare variable and get value from input device(s)
        for (ProjectDevice projectDevice : valueUsed.keySet()) {
            for (Value v : valueUsed.get(projectDevice)) {
                sb.append(INDENT).append("double ").append(projectDevice.getName().replace(" ", "_")).append("_")
                        .append(v.getName().replace(" ", "_")).append(" = ").append(projectDevice.getName().replace(" ", "_")).append(".get")
                        .append(v.getName().replace(" ", "_")).append("();").append(NEW_LINE);
            }
        }

        // generate if for each condition
        sb.append(INDENT).append("while (1) {").append(NEW_LINE);
        for (Condition condition : adjacentCondition) {
            sb.append(INDENT).append(INDENT).append("if").append("(");
            List<String> conditionList = new ArrayList<>();
            for (UserSetting setting : condition.getSetting()) {
                if ((setting.getAction() != null) && !setting.getAction().getName().replace(" ", "_").equals("Compare")) {
                    StringBuilder action = new StringBuilder();
                    action.append("(").append(setting.getDevice().getName().replace(" ", "_")).append(".")
                            .append(setting.getAction().getFunctionName()).append("(");
                    for (Parameter parameter : setting.getAction().getParameter()) {
                        Object value = setting.getValueMap().get(parameter);
                        if (value instanceof NumberWithUnit) {
                            action.append(df.format(((NumberWithUnit) value).getValue()));
                        } else if (value instanceof String) {
                            action.append("\"" + value + "\"");
                        }
                    }
                    action.append("))");
                    conditionList.add(action.toString());
                } else {
                    for (Value value : setting.getExpression().keySet()) {
                        List<String> valueList = new ArrayList<>();
                        for (Expression e : setting.getExpression().get(value)) {
                            if (e.getOperator().isBetween()) {
                                valueList.add("(" + setting.getDevice().getName().replace(" ", "_") + "_"
                                        + value.getName() + ' ' + e.getOperator().getCodeValue() + ' '
                                        + df.format(((NumberWithUnit) e.getFirstOperand()).getValue()) + ")");
                            } else {
                                valueList.add("(" + setting.getDevice().getName().replace(" ", "_") + "_" + value.getName() + " > "
                                        + df.format(((NumberWithUnit) e.getFirstOperand()).getValue()) + ")"
                                        + " && " + "(" + setting.getDevice().getName().replace(" ", "_") + "_" + value.getName() + " < "
                                        + df.format(((NumberWithUnit) e.getSecondOperand()).getValue()) + ")");
                            }
                        }
                        conditionList.add("(" + String.join(" || ", valueList) + ")");
                    }
                }
            }
            sb.append(String.join(" && ", conditionList)).append(") {").append(NEW_LINE);

            List<NodeElement> nextVertices = findAdjacentVertices(project, condition);
            List<Scene> nextScene = getScene(nextVertices);
            List<Condition> nextCondition = getCondition(nextVertices);
            if (!nextScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                if (nextScene.size() == 1) {
                    sb.append(INDENT).append(INDENT).append(INDENT).append("currentScene = ")
                            .append(nextScene.get(0).getName().replace(" ", "_")).append(";").append(NEW_LINE);
                    sb.append(INDENT).append(INDENT).append(INDENT).append("break;").append(NEW_LINE);
                    queue.add(nextScene.get(0));
                } else {
                    return Error.MULT_DIRECT_CONN_TO_SCENE;
                }
            } else if (!nextCondition.isEmpty()) { // nest condition is not allowed
                return Error.NEST_CONDITION;
            } else {
                sb.append(INDENT).append(INDENT).append(INDENT).append("currentScene = beginScene;").append(NEW_LINE);
                sb.append(INDENT).append(INDENT).append(INDENT).append("break;").append(NEW_LINE);
            }

            sb.append(INDENT).append(INDENT).append("}").append(NEW_LINE);
        }
        sb.append(INDENT).append("}").append(NEW_LINE); // end of while loop

        return Error.NONE;
    }

    private static List<NodeElement> findAdjacentVertices(Project project, NodeElement source) {
        return project.getLine().stream().filter(line -> line.getSource() == source)
                .map(Line::getDestination).collect(Collectors.toList());
    }

    private static List<Scene> getScene(List<NodeElement> nodeElements) {
        return nodeElements.stream().filter(nodeElement -> nodeElement instanceof Scene)
                .map(nodeElement -> (Scene) nodeElement).collect(Collectors.toList());
    }

    private static List<Condition> getCondition(List<NodeElement> nodeElements) {
        return nodeElements.stream().filter(nodeElement -> nodeElement instanceof Condition)
                .map(nodeElement -> (Condition) nodeElement).collect(Collectors.toList());
    }
}
