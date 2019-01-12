package io.makerplayground.generator.source;

import io.makerplayground.device.actual.Property;
import io.makerplayground.project.*;

import java.util.*;
import java.util.stream.Collectors;

class Utility {

    static long getMaximumNumberOfExpression(Project project, ProjectDevice device) {
        return project.getScene().stream()
                .flatMap(scene -> scene.getSetting().stream())
                .filter(userSetting -> userSetting.getDevice() == device)
                .filter(UserSetting::isDataBindingUsed)
                .mapToLong(UserSetting::getNumberOfDatabindParams)
                .max().orElse(0);
    }

    static List<NodeElement> findAdjacentNodes(Project project, NodeElement source) {
        return project.getLine().stream().filter(line -> line.getSource() == source)
                .map(Line::getDestination).collect(Collectors.toList());
    }

    static List<Scene> takeScene(Collection<NodeElement> nodeList) {
        return nodeList.stream().filter(nodeElement -> nodeElement instanceof Scene)
                .map(nodeElement -> (Scene) nodeElement).collect(Collectors.toList());
    }

    static List<Condition> takeCondition(Collection<NodeElement> nodeList) {
        return nodeList.stream().filter(nodeElement -> nodeElement instanceof Condition)
                .map(nodeElement -> (Condition) nodeElement).collect(Collectors.toList());
    }

    static List<Condition> findAdjacentConditions(Project project, NodeElement source) {
        return project.getLine().stream()
                .filter(line -> line.getSource() == source)
                .map(Line::getDestination)
                .filter(nodeElement -> nodeElement instanceof Condition)
                .map(nodeElement -> (Condition) nodeElement)
                .collect(Collectors.toList());
    }

    static Set<NodeElement> getAllUsedNodes(Project project) {
        Queue<NodeElement> nodeToTraverse = new ArrayDeque<>();
        Set<NodeElement> visitedNodes = new HashSet<>();
        visitedNodes.add(project.getBegin());
        List<NodeElement> adjacentNodes = findAdjacentNodes(project, project.getBegin());
        nodeToTraverse.addAll(takeScene(adjacentNodes));
        nodeToTraverse.addAll(takeCondition(adjacentNodes));

        NodeElement current;
        while (!nodeToTraverse.isEmpty()) {
            current = nodeToTraverse.remove();
            visitedNodes.add(current);
            adjacentNodes = findAdjacentNodes(project, current);
            nodeToTraverse.addAll(takeScene(adjacentNodes).stream().filter(scene -> !visitedNodes.contains(scene)).collect(Collectors.toList()));
            nodeToTraverse.addAll(takeCondition(adjacentNodes).stream().filter(condition -> !visitedNodes.contains(condition)).collect(Collectors.toList()));
        }
        return visitedNodes;
    }

    static boolean validateDiagram(Project project) {
        Set<NodeElement> visitNodes = getAllUsedNodes(project);
        return takeScene(visitNodes).stream().noneMatch(scene -> scene.getError() != DiagramError.NONE)
                && takeCondition(visitNodes).stream().noneMatch(condition -> condition.getError() != DiagramError.NONE)
                && project.getDiagramStatus().isEmpty();
    }

    static boolean validateDeviceProperty(Project project) {
        for (ProjectDevice device : project.getAllDeviceUsed()) {
            // check only device that has a property
            if (!device.getActualDevice().getProperty().isEmpty()) {
                for (Property p : device.getActualDevice().getProperty()) {
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

    static Set<ProjectDevice> getUsedDevicesWithTask(Project project) {
        return project.getScene().stream()
                .flatMap(scene -> scene.getSetting().stream())
                .filter(UserSetting::isDataBindingUsed)
                .map(UserSetting::getDevice)
                .collect(Collectors.toSet());
    }
}
