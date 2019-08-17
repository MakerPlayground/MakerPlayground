package io.makerplayground.generator.source;

import io.makerplayground.device.actual.Property;
import io.makerplayground.project.*;

import java.util.*;
import java.util.stream.Collectors;

public class Utility {

    static long getMaximumNumberOfExpression(Project project, ProjectDevice device) {
        return project.getScene().stream()
                .flatMap(scene -> scene.getSetting().stream())
                .filter(userSetting -> userSetting.getProjectDevice() == device)
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
        nodeToTraverse.addAll(project.getBegin());
        Set<NodeElement> visitedNodes = new HashSet<>();

        NodeElement current;
        while (!nodeToTraverse.isEmpty()) {
            current = nodeToTraverse.remove();
            visitedNodes.add(current);
            List<NodeElement> adjacentNodes = findAdjacentNodes(project, current);
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

    public static boolean validateDeviceProperty(Project project) {
        for (ProjectDevice device : project.getAllDeviceUsed()) {
            // skip device that share actual device with other project device
            if (device.isMergeToOtherDevice()) {
                continue;
            }
            // check only device that has a property
            if (!device.getActualDevice().getProperty().isEmpty()) {
                for (Property p : device.getActualDevice().getProperty()) {
                    Object value = device.getPropertyValue(p);
                    // TODO: allow property to be optional
                    if (value == null) {
                        return false;
                    } else if ((value instanceof String) && ((String) value).isEmpty()) {
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
                .map(UserSetting::getProjectDevice)
                .collect(Collectors.toSet());
    }
}
