package io.makerplayground.generator;

import io.makerplayground.device.*;
import io.makerplayground.helper.ConnectionType;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.Platform;
import io.makerplayground.project.*;
import io.makerplayground.project.term.*;
import io.makerplayground.project.expression.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class Sourcecode {

    public enum Error {
        NONE(""),
        SCENE_ERROR("Missing required parameter in some scenes"),
        MISSING_PROPERTY("Missing required device's property"),
        NOT_FOUND_SCENE_OR_CONDITION("Can't find any scene or condition connect to the begin node"),
        MULT_DIRECT_CONN_TO_SCENE("Found multiple direct connection to the same scene"),
        NEST_CONDITION("Multiple condition are connected together"),
        SHORT_CIRCUIT("Some conditions are not reachable"),
        CONDITION_ERROR("Missing required parameter in some conditions");

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
    private static final Map<String, List<String>> MP_PORT_MAP = Map.ofEntries(
            Map.entry("D1", List.of("3", "4")),
            Map.entry("D2", List.of("5", "6")),
            Map.entry("D3", List.of("6", "7")),
            Map.entry("D4", List.of("9", "10")),
            Map.entry("D5/A1", List.of("A0", "A1")),
            Map.entry("D6/A2", List.of("A2", "A3")),
            Map.entry("I2C1", Collections.<String>emptyList()),
            Map.entry("I2C2", Collections.<String>emptyList()),
            Map.entry("I2C3", Collections.<String>emptyList()),
            Map.entry("I2C4", Collections.<String>emptyList()),
            Map.entry("Internal", Collections.<String>emptyList())
    );

    public static Sourcecode generateCode(Project project, boolean cppMode) {
        //Begin begin = project.getBegin();
        if (!checkScene(project)) {
            return new Sourcecode(Error.SCENE_ERROR, "-");   // TODO: add location
        }

        if (!checkCondition(project)) {
            return new Sourcecode(Error.CONDITION_ERROR, "-");
        }

        if (!checkDeviceProperty(project)) {
            return new Sourcecode(Error.MISSING_PROPERTY, "-");   // TODO: add location
        }

        StringBuilder headerStringBuilder = new StringBuilder();
        StringBuilder sb = new StringBuilder();

        Scene currentScene = null;
        Queue<Scene> queue = new ArrayDeque<>();

        // get all adjacent vertices which may be another scene(s) or another condition(s)
        List<NodeElement> adjacentVertices = findAdjacentVertices(project, project.getBegin());
        List<Scene> adjacentScene = getScene(adjacentVertices);
        List<Condition> adjacentCondition = getCondition(adjacentVertices);

        // add #include <Arduino.h> if in cpp mode
        if (cppMode) {
            headerStringBuilder.append("#include <Arduino.h>").append(NEW_LINE);
        }

        // generate include
        project.getAllDeviceUsed().stream()
                .map(projectDevice -> projectDevice.getActualDevice().getSourceToInclude())
                .collect(Collectors.toSet())
                .forEach(s -> headerStringBuilder.append("#include \"").append(s).append("\"").append(NEW_LINE));

        headerStringBuilder.append(NEW_LINE);
        sb.append("void (*currentScene)(void);").append(NEW_LINE);

        // instantiate object(s) for each device
        sb.append(NEW_LINE);
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            sb.append(projectDevice.getActualDevice().getClassname())
                    .append(" ")
                    .append("_"+projectDevice.getName().replace(" ","_"));

            List<String> args = new ArrayList<>();

            // port
            for (Peripheral p : projectDevice.getActualDevice().getConnectivity()) {
                if ((p.getConnectionType() != ConnectionType.I2C) && (p.getConnectionType() != ConnectionType.MP_I2C)) {
                    List<DevicePort> port = projectDevice.getDeviceConnection().get(p);
                    if (port == null) {
                        throw new IllegalStateException("Port hasn't been selected!!!");
                    }
                    // SPECIAL CASE
                    if (project.getPlatform() == Platform.MP_ARDUINO) {
                        if (port.size() != 1) {
                            throw new IllegalStateException();
                        }
                        List<String> portName = MP_PORT_MAP.get(port.get(0).getName());
                        if (!portName.isEmpty()) {
                            if (p.isMPDual()) {
                                args.addAll(portName);
                            } else {
                                args.add(portName.get(0));
                            }
                        }
                    } else {
                        args.addAll(port.stream().map(DevicePort::getName).collect(Collectors.toList()));
                    }
                }
            }

            // property for the generic device
            for (Property p : projectDevice.getGenericDevice().getProperty()) {
                String value = projectDevice.getPropertyValue(p);
                if (value == null) {
                    throw new IllegalStateException("Property hasn't been set");
                }
                args.add("\"" + value + "\"");
            }

            if (!args.isEmpty()) {
                sb.append("(").append(String.join(",", args))
                .append(",\""+ projectDevice.getName().replace(" ", "_")).append("\")");
            }
            else {
                sb.append("(\""+ projectDevice.getName().replace(" ", "_")).append("\")");
            }
            sb.append(";").append(NEW_LINE);
        }

        // local variable needed
        sb.append(NEW_LINE);
        sb.append("unsigned long endTime = 0;").append(NEW_LINE);
        Map<ProjectDevice, Set<Value>> variableMap = project.getAllValueUsedMap();
        for (ProjectDevice projectDevice : variableMap.keySet()) {
            for (Value v : variableMap.get(projectDevice)) {
                sb.append("double ").append("_" + projectDevice.getName().replace(" ", "_")).append("_")
                        .append(v.getName().replace(" ", "_")).append(";").append(NEW_LINE);
            }
        }


        // generate setup function
        sb.append(NEW_LINE);
        sb.append("void setup() {").append(NEW_LINE);
        sb.append(INDENT).append("Serial.begin(115200);").append(NEW_LINE);
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            sb.append(INDENT).append("_" + projectDevice.getName().replace(" ", "_")).append(".init();").append(NEW_LINE);
        }
        sb.append(INDENT).append("currentScene = beginScene;").append(NEW_LINE);
        sb.append("}").append(NEW_LINE);

        // generate update function
        sb.append(NEW_LINE);
        sb.append("void update() {").append(NEW_LINE);
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            // TODO: BAD CODE (add update function for actuator and sensor to adjust read frequency)
            if (DeviceLibrary.INSTANCE.getGenericConnectivityDevice().contains(projectDevice.getGenericDevice())) {
                sb.append(INDENT).append("_" + projectDevice.getName().replace(" ", "_")).append(".update(millis());").append(NEW_LINE);
            }
        }
        sb.append("}").append(NEW_LINE);

        // generate loop function
        sb.append(NEW_LINE);
        sb.append("void loop() {").append(NEW_LINE);
        sb.append(INDENT).append("update();").append(NEW_LINE);
        sb.append(INDENT).append("currentScene();").append(NEW_LINE);
        sb.append("}").append(NEW_LINE);

        Set<Scene> visitedScene = new HashSet<>();

        // generate code for begin
        sb.append(NEW_LINE);
        sb.append("void beginScene() {").append(NEW_LINE);
        if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
            if (adjacentScene.size() == 1) {
                Scene s = adjacentScene.get(0);
                sb.append(INDENT).append("currentScene = ").append("scene_").append(s.getName().replace(" ", "_")).append(";").append(NEW_LINE);
                visitedScene.add(s);
                queue.add(s);
            } else {
                return new Sourcecode(Error.MULT_DIRECT_CONN_TO_SCENE, "beginScene");
            }
        } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
            Error error = processCondition(sb, queue, visitedScene, project, adjacentCondition);
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
            sb.append("void ").append("scene_").append(currentScene.getName().replace(" ", "_")).append("() {").append(NEW_LINE);
            sb.append(INDENT).append("update();").append(NEW_LINE);

            HashSet<String> variableUpdateNameSet = new HashSet<>();
            for (UserSetting setting : currentScene.getSetting()) {
                for (Map.Entry<ProjectDevice, Set<Value>> entry : setting.getAllValueUsed().entrySet()) {
                    for(Value v : entry.getValue()) {
                        String str = "_" + entry.getKey().getName().replace(" ", "_") +("_")
                                + (v.getName().replace(" ", "_")) + (" = ") + ("_" + entry.getKey().getName().replace(" ", "_")) + (".get")
                                + (v.getName().replace(" ", "_")) + ("();");
                        variableUpdateNameSet.add(str);
                    }
                }
            }

            for(String str : variableUpdateNameSet){
                sb.append(INDENT).append(str).append(NEW_LINE);
            }


            // do action
            for (UserSetting setting : currentScene.getSetting()) {
                String deviceName = "_" + setting.getDevice().getName().replace(" ", "_");
                List<String> params = new ArrayList<>();
                for (Parameter parameter : setting.getAction().getParameter()) {
                    Expression expression = setting.getValueMap().get(parameter);
                    if (expression instanceof CustomNumberExpression) {
                        double maxValue = ((CustomNumberExpression) expression).getMaxValue();
                        double minValue = ((CustomNumberExpression) expression).getMinValue();
                        params.add("constrain(" + expression.translateToCCode() + "," + minValue + "," + maxValue + ")");
                    } else {
                        params.add(expression.translateToCCode());
                    }
                }
                sb.append(INDENT).append(deviceName).append(".")
                        .append(setting.getAction().getFunctionName()).append("(");
                sb.append(String.join(", ", params)).append(");").append(NEW_LINE);
            }

            // delay
            if (currentScene.getDelay() != 0) {
                int delayDuration = 0;  // in ms
                if (currentScene.getDelayUnit() == Scene.DelayUnit.Second) {
                    delayDuration = (int) (currentScene.getDelay() * 1000);
                } else if (currentScene.getDelayUnit() == Scene.DelayUnit.MilliSecond) {
                    delayDuration = (int) currentScene.getDelay();
                }
                sb.append(INDENT).append("endTime = millis() + ").append(delayDuration).append(";").append(NEW_LINE);
                sb.append(INDENT).append("while (millis() < endTime) {").append(NEW_LINE);
                sb.append(INDENT).append(INDENT).append("update();").append(NEW_LINE);
                sb.append(INDENT).append("}").append(NEW_LINE);
            }

            // update list of adjacent vertices (scenes/conditions)
            adjacentVertices = findAdjacentVertices(project, currentScene);
            adjacentScene = getUnvisitedScene(adjacentVertices, visitedScene);
            adjacentCondition = getCondition(adjacentVertices);

            if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                if (adjacentScene.size() == 1) {
                    Scene s = adjacentScene.get(0);
                    visitedScene.add(s);
                    queue.add(s);
                    sb.append(INDENT).append("currentScene = ").append("scene_").append(s.getName().replace(" ", "_")).append(";").append(NEW_LINE);
                } else {
                    return new Sourcecode(Error.MULT_DIRECT_CONN_TO_SCENE, currentScene.getName().replace(" ", "_"));
                }
            } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
                Error error = processCondition(sb, queue, visitedScene, project, adjacentCondition);
                if (error != Error.NONE) {
                    return new Sourcecode(error, currentScene.getName().replace(" ", "_"));
                }
            } else {
                sb.append(INDENT).append("currentScene = beginScene;").append(NEW_LINE);
            }

            // end of scene's function
            sb.append("}").append(NEW_LINE);
        }

        // generate function declaration for each scene
        if (cppMode) {
            headerStringBuilder.append("void beginScene();").append(NEW_LINE);
            for (Scene scene : visitedScene) {
                headerStringBuilder.append("void ").append("scene_").append(scene.getName().replace(" ", "_")).append("();").append(NEW_LINE);
            }
            headerStringBuilder.append(NEW_LINE);
        }

        return new Sourcecode(headerStringBuilder.append(sb).toString());
    }

    private static Error processCondition(StringBuilder sb, Queue<Scene> queue, Collection<Scene> visitedScene, Project project, List<Condition> adjacentCondition) {
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


        // loop to check sensor
        sb.append(INDENT).append("while (1) {").append(NEW_LINE);
        // call the update function
        sb.append(INDENT).append(INDENT).append("update();").append(NEW_LINE);
        // update value from input device(s) to the variable
        for (ProjectDevice projectDevice : valueUsed.keySet()) {
            for (Value v : valueUsed.get(projectDevice)) {
                sb.append(INDENT).append(INDENT).append("_" + projectDevice.getName().replace(" ", "_")).append("_")
                .append(v.getName().replace(" ", "_")).append(" = ").append("_" + projectDevice.getName().replace(" ", "_")).append(".get")
                        //.append(v.getName().replace(" ", "_")).append(" = ").append("_" + projectDevice.getName().replace(" ", "_")).append(".get")
                .append(v.getName().replace(" ", "_")).append("();").append(NEW_LINE);
            }
        }
        // generate if for each condition
        for (Condition condition : adjacentCondition) {
            List<String> conditionList = new ArrayList<>();
            for (UserSetting setting : condition.getSetting()) {
                if ((setting.getAction() != null) && !setting.getAction().getName().equals("Compare")) {
                    List<String> parameterList = new ArrayList<>();
                    for (Parameter parameter : setting.getAction().getParameter()) {
                        Object value = setting.getValueMap().get(parameter);
                        if (value instanceof NumberWithUnitExpression) {
                            parameterList.add(df.format(((NumberWithUnitExpression) value).getNumberWithUnit().getValue()));
                        } else if (value instanceof SimpleStringExpression) {
                            parameterList.add("\"" + ((SimpleStringExpression) value).getString() + "\"");
                        }
                    }
                    StringBuilder action = new StringBuilder();
                    action.append("_" + setting.getDevice().getName().replace(" ", "_")).append(".")
                            .append(setting.getAction().getFunctionName()).append("(").append(String.join(",", parameterList)).append(")");
                    conditionList.add(action.toString());
                } else {
                    for (Value value : setting.getExpression().keySet()) {
                        Expression expression = setting.getExpression().get(value);
                        // skip if it is disable
                        if (!expression.isEnable()) {
                            continue;
                        }
                        conditionList.add("(" + expression.getTerms().stream().map(term -> {
                            if (term instanceof NumberWithUnitTerm) {
                                return String.valueOf(((NumberWithUnitTerm) term).getValue().getValue());
                            } else if (term instanceof StringTerm) {
                                return ((StringTerm) term).getValue();
                            } else if (term instanceof OperatorTerm) {
                                OperatorTerm.OP chipOperator = ((OperatorTerm) term).getValue();
                                if (chipOperator == OperatorTerm.OP.AND) {
                                    return " && ";
                                } else if (chipOperator == OperatorTerm.OP.OR) {
                                    return " || ";
                                } else {
                                    return " " + chipOperator.toString() + " ";
                                }
                            } else if (term instanceof ValueTerm) {
                                return "_" + setting.getDevice().getName().replace(" ", "_") + "_"
                                        + value.getName().replace(" ", "_");
                            } else {
                                throw new IllegalStateException("Unknown term is found " + term);
                            }
                        }).collect(Collectors.joining()) + ")");
                    }
                }
            }
            if (!conditionList.isEmpty()) {
                sb.append(INDENT).append(INDENT).append("if").append("(");
                sb.append(String.join(" && ", conditionList)).append(") {").append(NEW_LINE);
            } else {
                return Error.CONDITION_ERROR;
            }

            List<NodeElement> nextVertices = findAdjacentVertices(project, condition);
            List<Scene> nextScene = getScene(nextVertices);
            List<Condition> nextCondition = getCondition(nextVertices);
            if (!nextScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                if (nextScene.size() == 1) {
                    Scene s = nextScene.get(0);
                    sb.append(INDENT).append(INDENT).append(INDENT).append("currentScene = ").append("scene_")
                            .append(s.getName().replace(" ", "_")).append(";").append(NEW_LINE);
                    sb.append(INDENT).append(INDENT).append(INDENT).append("break;").append(NEW_LINE);
                    if (!visitedScene.contains(s)) {
                        visitedScene.add(s);
                        queue.add(s);
                    }
                } else {
                    return Error.MULT_DIRECT_CONN_TO_SCENE;
                }
            } else if (!nextCondition.isEmpty()) { // nest condition is not allowed
                return Error.NEST_CONDITION;
            } else {
                sb.append(INDENT).append(INDENT).append(INDENT).append("currentScene = beginScene;").append(NEW_LINE);
                sb.append(INDENT).append(INDENT).append(INDENT).append("break;").append(NEW_LINE);
            }

            sb.append(INDENT).append(INDENT).append("}").append(NEW_LINE); // end of if
        }
        sb.append(INDENT).append("}").append(NEW_LINE); // end of while loop

        return Error.NONE;
    }

    private static boolean checkScene(Project project) {
        return project.getScene().stream().noneMatch(Scene::isError);
    }

    private static boolean checkCondition(Project project) {
        return project.getCondition().stream().noneMatch(Condition::isError);
    }

    private static boolean checkDeviceProperty(Project project) {
        for (ProjectDevice device : project.getAllDeviceUsed()) {
            // check only device that has a property
            if (!device.getGenericDevice().getProperty().isEmpty()) {
                for (Property p : device.getGenericDevice().getProperty()) {
                    String value = device.getPropertyValue(p);
                    // TODO: allow property to be optional
                    if (value == null || value.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static List<NodeElement> findAdjacentVertices(Project project, NodeElement source) {
        return project.getLine().stream().filter(line -> line.getSource() == source)
                .map(Line::getDestination).collect(Collectors.toList());
    }

    private static List<Scene> getScene(List<NodeElement> nodeElements) {
        return nodeElements.stream().filter(nodeElement -> nodeElement instanceof Scene)
                .map(nodeElement -> (Scene) nodeElement).collect(Collectors.toList());
    }

    private static List<Scene> getUnvisitedScene(List<NodeElement> nodeElements, Collection<Scene> visitedScene) {
        return nodeElements.stream().filter(nodeElement -> nodeElement instanceof Scene)
                .filter(nodeElement -> !visitedScene.contains(nodeElement))
                .map(nodeElement -> (Scene) nodeElement).collect(Collectors.toList());
    }

    private static List<Condition> getCondition(List<NodeElement> nodeElements) {
        return nodeElements.stream().filter(nodeElement -> nodeElement instanceof Condition)
                .map(nodeElement -> (Condition) nodeElement).collect(Collectors.toList());
    }
}
