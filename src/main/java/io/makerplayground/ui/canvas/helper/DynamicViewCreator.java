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

package io.makerplayground.ui.canvas.helper;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @param <T>
 * @param <U>
 * @param <V>
 */
public class DynamicViewCreator<T extends Parent, U,  V extends Node> {
    private final DynamicViewModelCreator<?, U> modelLoader;
    private final T parent;
    private final ViewFactory<U, V> viewFactory;
    private final NodeAdder<T, V> adder;
    private final NodeRemover<T, V> remover;

    private final Map<U, V> nodeMap;

    public DynamicViewCreator(DynamicViewModelCreator<?, U> modelLoader, T parent, ViewFactory<U, V> viewFactory, NodeAdder<T, V> adder, NodeRemover<T, V> remover) {
        this.modelLoader = modelLoader;
        this.parent = parent;
        this.viewFactory = viewFactory;
        this.adder = adder;
        this.remover = remover;

        this.nodeMap = new HashMap<>();

        ObservableMap<?, U> controllerMap = modelLoader.getControllerMap();

        modelLoader.getUnmodifiableModel().forEach(model -> addNode(controllerMap.get(model)));

        controllerMap.addListener((MapChangeListener<Object, U>) change -> {
            if (change.wasRemoved()) {
                removeNode(change.getValueRemoved());
            } else if (change.wasAdded()) {
                addNode(change.getValueAdded());
            }
            // TODO: add case for update
        });
    }

    private void addNode(U controller) {
        V node = viewFactory.newInstance(controller);
        nodeMap.put(controller, node);
        adder.addNode(parent, node);
    }

    private void removeNode(U controller) {
        V node = nodeMap.remove(controller);
        remover.removeNode(parent, node);
    }
}
