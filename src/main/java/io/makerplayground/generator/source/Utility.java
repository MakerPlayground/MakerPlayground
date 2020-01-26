/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.generator.source;

import io.makerplayground.device.actual.Property;
import io.makerplayground.project.*;

import java.util.*;
import java.util.stream.Collectors;

class Utility {

    static long getMaximumNumberOfExpression(Project project, ProjectDevice device) {
        return project.getUnmodifiableScene().stream()
                .flatMap(scene -> scene.getSetting().stream())
                .filter(userSetting -> userSetting.getDevice() == device)
                .filter(UserSetting::isDataBindingUsed)
                .mapToLong(UserSetting::getNumberOfDatabindParams)
                .max().orElse(0);
    }

    static List<NodeElement> findAdjacentNodes(Project project, NodeElement source) {
        return project.getUnmodifiableLine().stream().filter(line -> line.getSource() == source)
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

    static List<Delay> takeDelay(Collection<NodeElement> nodeList) {
        return nodeList.stream().filter(nodeElement -> nodeElement instanceof Delay)
                .map(nodeElement -> (Delay) nodeElement).collect(Collectors.toList());
    }

    static List<Condition> findAdjacentConditions(Project project, NodeElement source) {
        return project.getUnmodifiableLine().stream()
                .filter(line -> line.getSource() == source)
                .map(Line::getDestination)
                .filter(nodeElement -> nodeElement instanceof Condition)
                .map(nodeElement -> (Condition) nodeElement)
                .collect(Collectors.toList());
    }

    static List<Delay> findAdjacentDelays(Project project, NodeElement source) {
        return project.getUnmodifiableLine().stream()
                .filter(line -> line.getSource() == source)
                .map(Line::getDestination)
                .filter(nodeElement -> nodeElement instanceof Delay)
                .map(nodeElement -> (Delay) nodeElement)
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
            nodeToTraverse.addAll(takeDelay(adjacentNodes).stream().filter(delay -> !visitedNodes.contains(delay)).collect(Collectors.toList()));
        }
        return visitedNodes;
    }

    static boolean validateDiagram(Project project) {
        Set<NodeElement> visitNodes = getAllUsedNodes(project);
        return takeScene(visitNodes).stream().noneMatch(scene -> scene.getError() != DiagramError.NONE)
                && takeCondition(visitNodes).stream().noneMatch(condition -> condition.getError() != DiagramError.NONE)
                && takeDelay(visitNodes).stream().noneMatch(delay -> delay.getError() != DiagramError.NONE)
                && project.getDiagramConnectionStatus().isEmpty();
    }

    static boolean validateDeviceProperty(Project project) {
        ProjectConfiguration configuration = project.getProjectConfiguration();
        for (ProjectDevice device : project.getAllDeviceUsed()) {
            // skip device that share actual device with other project device
            if (configuration.getIdenticalDevice(device).isPresent()) {
                continue;
            }
            // check only device that has a property
            if (configuration.getActualDevice(device).isPresent() && configuration.getActualDevice(device).get().getProperty() != null && !configuration.getActualDevice(device).get().getProperty().isEmpty()) {
                for (Property p : configuration.getActualDevice(device).get().getProperty()) {
                    Object value = configuration.getPropertyValue(device, p);
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
        return project.getUnmodifiableScene().stream()
                .flatMap(scene -> scene.getSetting().stream())
                .filter(UserSetting::isDataBindingUsed)
                .map(UserSetting::getDevice)
                .collect(Collectors.toSet());
    }
}
