package io.makerplayground.generator;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.makerplayground.project.*;

/**
 *
 */
public class CodeGenerator {

    public static void generateCode(Project project) {
        Begin begin = project.getBegin();
        List<Scene> sceneList = project.getScene();
        List<Condition> conditionList = project.getCondition();

        StringBuilder sb = new StringBuilder();
        Scene currentScene = null;

        Queue<Scene> queue = new ArrayDeque<>();
        while (!queue.isEmpty()) {
            currentScene = queue.remove();

            // create function header
            sb.append("void ");
            sb.append(currentScene.getName());
            sb.append("() {");

            // do action
            for (UserSetting setting : currentScene.getSetting()) {
                
            }

            // get all adjacent vertices which may be another scene(s) or another condition(s)
            List<NodeElement> adjacentVertices = findAdjacentVertices(project, begin);
            List<Scene> adjacentScene = getScene(adjacentVertices);
            List<Condition> adjacentCondition = getCondition(adjacentVertices);

            // if there is any adjacent scene, move to that scene and ignore condition
            if (!adjacentScene.isEmpty()) {

            } else { // there is a condition so we generate code for that condition
                for (NodeElement nodeElement : adjacentVertices) {

                }
            }
        }
    }

    private static List<NodeElement> findAdjacentVertices(Project project, NodeElement source) {
        return project.getLine().stream().filter(line -> line.getSource() == source).map(new Function<Line, NodeElement>() {
            @Override
            public NodeElement apply(Line line) {
                return line.getDestination();
            }
        }).collect(Collectors.toList());
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
