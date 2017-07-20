package io.makerplayground.generator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.makerplayground.device.Parameter;
import io.makerplayground.device.Value;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.*;

/**
 *
 */
public class CodeGenerator {

    private static final String INDENT = "    ";
    private static final String NEW_LINE = "\n";

    public static String generateCode(Project project) {
        Begin begin = project.getBegin();
        List<Scene> sceneList = project.getScene();
        List<Condition> conditionList = project.getCondition();

        StringBuilder sb = new StringBuilder();
        Scene currentScene = null;

        Queue<Scene> queue = new ArrayDeque<>();

        // get all adjacent vertices which may be another scene(s) or another condition(s)
        List<NodeElement> adjacentVertices = findAdjacentVertices(project, begin);
        List<Scene> adjacentScene = getScene(adjacentVertices);
        List<Condition> adjacentCondition = getCondition(adjacentVertices);

        // if there is any adjacent scene, move to that scene and ignore condition
        sb.append("void loop() {\n");
        if (!adjacentScene.isEmpty()) {
            if (adjacentScene.size() == 1) {
                Scene s = adjacentScene.get(0);
                queue.add(s);
                sb.append(INDENT).append("currentSceneFunction = ").append(s.getName()).append(";").append(NEW_LINE);
            } else {
                System.out.println("Error: found connection to multiple scene");
            }
        } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
            for (Condition condition : adjacentCondition) {

            }
        } else {
            System.out.println("No scene and condition is found!!!");
        }
        sb.append("}").append(NEW_LINE);

        while (!queue.isEmpty()) {
            currentScene = queue.remove();

            // create function header
            sb.append("void ");
            sb.append(currentScene.getName());
            sb.append("() {\n");

            // do action
            for (UserSetting setting : currentScene.getSetting()) {
                sb.append(INDENT).append(setting.getDevice().getName()).append(".")
                        .append(setting.getAction().getFunctionName()).append("(");
                List<String> params = new ArrayList<>();
                for (Parameter parameter : setting.getAction().getParameter()) {
                    Object value = setting.getValueMap().get(parameter);
                    if (value instanceof NumberWithUnit) {
                        params.add(String.valueOf(((NumberWithUnit) value).getValue()));
                    } else if (value instanceof String) {
                        params.add("\"" + value + "\"");
                    }
                }
                sb.append(String.join(", ", params)).append(");").append(NEW_LINE);
            }

            // get all adjacent vertices which may be another scene(s) or another condition(s)
            adjacentVertices = findAdjacentVertices(project, currentScene);
            adjacentScene = getScene(adjacentVertices);
            adjacentCondition = getCondition(adjacentVertices);

            // if there is any adjacent scene, move to that scene and ignore condition
            if (!adjacentScene.isEmpty()) {
                if (adjacentScene.size() == 1) {
                    Scene s = adjacentScene.get(0);
                    queue.add(s);
                    sb.append(INDENT).append("currentSceneFunction = ").append(s.getName()).append(NEW_LINE);
                } else {
                    System.out.println("Error: found connection to multiple scene");
                }
            } else { // there is a condition so we generate code for that condition
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

                for (ProjectDevice projectDevice : valueUsed.keySet()) {
                    for (Value v : valueUsed.get(projectDevice)) {
                        sb.append(INDENT).append("double ").append(projectDevice.getName()).append("_")
                                .append(v.getName()).append(" = ").append(projectDevice.getName()).append(".get")
                                .append(v.getName()).append("();").append(NEW_LINE);
                    }
                }

                for (Condition condition : adjacentCondition) {
                    sb.append(INDENT).append(INDENT).append("if").append("(");
                    List<String> conditionlist = new ArrayList<>();
                    List<String> params = new ArrayList<>();
                    for (UserSetting setting : condition.getSetting()) {
                        if (!setting.getAction().getName().equals("Compare") && (setting.getAction() != null)) {
                            conditionlist.add("(" + setting.getDevice().getName() + "."
                                    + setting.getAction().getFunctionName() + "(");

                            for (Parameter parameter : setting.getAction().getParameter()) {
                                Object value = setting.getValueMap().get(parameter);
                                if (value instanceof NumberWithUnit) {
                                    params.add(String.valueOf(((NumberWithUnit) value).getValue()));
                                } else if (value instanceof String) {
                                    params.add("\"" + value + "\"");
                                }
                            }

                        } else {
                            for (Value value : setting.getExpression().keySet()) {
                                List<Expression> expressions = setting.getExpression().get(value);
                                for (Expression e : expressions) {

                                }
                            }
                        }
                    }

                    sb.append(String.join(" && ", conditionlist)).append(String.join(", ", params))
                            .append(")").append(") {").append(NEW_LINE);

                    List<NodeElement> nextVertices = findAdjacentVertices(project, condition);
                    List<Scene> nextScene = getScene(nextVertices);
                    List<Condition> nextCondition = getCondition(nextVertices);
                    if (nextScene.size() == 1 && nextCondition.isEmpty()) {
                        sb.append(INDENT).append(INDENT).append(INDENT).append("currentSceneFunction = ")
                                .append(nextScene.get(0).getName()).append(NEW_LINE);
                        queue.add(nextScene.get(0));
                    } else {
                        System.out.println("Error: found connection to multiple scene");
                        for (Scene s : nextScene) {
                            System.out.println(s.getName());
                        }
                        for (Condition c : nextCondition) {
                            System.out.println(c.getName());
                        }
                    }

                    sb.append(INDENT).append(INDENT).append("}").append(NEW_LINE);
                }
            }

            sb.append("}\n");
        }

        System.out.println(sb);
        return sb.toString();
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
