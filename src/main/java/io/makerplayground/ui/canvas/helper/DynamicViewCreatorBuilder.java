/*
 * Copyright (c) 2018. The Maker Playground Authors.
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

import javafx.scene.Node;
import javafx.scene.Parent;

public class DynamicViewCreatorBuilder<T extends Parent, U, V extends Node> {
    private DynamicViewModelCreator<?, U> modelLoader;
    private T parent;
    private ViewFactory<U, V> viewFactory;
    private NodeAdder<T, V> adder;
    private NodeRemover<T, V> remover;

    public DynamicViewCreatorBuilder<T, U, V> setModelLoader(DynamicViewModelCreator<?, U> modelLoader) {
        this.modelLoader = modelLoader;
        return this;
    }

    public DynamicViewCreatorBuilder<T, U, V> setParent(T parent) {
        this.parent = parent;
        return this;
    }

    public DynamicViewCreatorBuilder<T, U, V> setViewFactory(ViewFactory<U, V> viewFactory) {
        this.viewFactory = viewFactory;
        return this;
    }

//    public DynamicViewCreatorBuilder<T, U, V> setNodeConsumer(NodeConsumer<T, V> nodeConsumer) {
//        this.nodeConsumer = nodeConsumer;
//        return this;
//    }
    public DynamicViewCreatorBuilder<T, U, V> setNodeAdder(NodeAdder<T,V> adder) {
        this.adder = adder;
        return this;
    }

    public DynamicViewCreatorBuilder<T, U, V> setNodeRemover(NodeRemover<T,V> remover) {
        this.remover = remover;
        return this;
    }

    public DynamicViewCreator<T, U, V> createDynamicViewCreator() {
//        if (nodeConsumer == null) {
//            if (!(parent instanceof Pane)) {
//                throw new IllegalStateException("A NodeConsumer must be set if Parent is not a subclass of Pane.");
//            }
//
//            nodeConsumer = new NodeConsumer<T, V>() {
//                @Override
//                public void addNode(T parent, V node) {
//                    ((Pane) parent).getChildren().add(node);
//                }
//
//                @Override
//                public void removeNode(T parent, V node) {
//                    ((Pane) parent).getChildren().remove(node);
//                }
//            };
//        }

        return new DynamicViewCreator<>(modelLoader, parent, viewFactory, adder, remover);
    }
}