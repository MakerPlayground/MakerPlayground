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

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.function.Predicate;

/**
 * A helper class for creating instances of ViewModel based on the number of Model in an ObservableList provided.
 *
 * @param <T> type of Model of the ViewModel to be created by the instance of this class
 * @param <U> type of ViewModel to be created
 */
public class DynamicViewModelCreator<T, U> {
    private final ObservableList<T> model;
    private final ViewModelFactory<T, U> viewModelFactory;
    private final Predicate<T> filter;

    private final ObservableMap<T, U> controllerMap;

    public DynamicViewModelCreator(ObservableList<T> model, ViewModelFactory<T, U> viewModelFactory) {
        this(model, viewModelFactory, null);
    }

    public DynamicViewModelCreator(ObservableList<T> model, ViewModelFactory<T, U> viewModelFactory, Predicate<T> filter) {
        this.model = model;
        this.viewModelFactory = viewModelFactory;
        this.filter = filter;

        this.controllerMap = FXCollections.observableMap(new HashMap<>());

        for (T t : model) {
            if (this.filter == null || this.filter.test(t))
                addController(t);
        }

        model.addListener((ListChangeListener<T>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    throw new UnsupportedOperationException();
                } else if (c.wasUpdated()) {
                    for (T updatedItem : c.getList().subList(c.getFrom(), c.getTo())) {
                        if (filter != null) {
                            if (filter.test(updatedItem) && !controllerMap.containsKey(updatedItem))
                                addController(updatedItem);
                            else if (!filter.test(updatedItem) && controllerMap.containsKey(updatedItem))
                                removeController(updatedItem);
                        }
                    }
                } else {
                    for (T removedItem : c.getRemoved()) {
                        if (filter == null || filter.test(removedItem))
                            removeController(removedItem);
                    }
                    for (T addedItem : c.getAddedSubList()) {
                        if (filter == null || filter.test(addedItem))
                            addController(addedItem);
                    }
                }
            }
        });
    }

    ObservableMap<T, U> getControllerMap() {
        return controllerMap;
    }

    private void addController(T model) {
        U node = viewModelFactory.newInstance(model);
        controllerMap.put(model, node);
    }

    private void removeController(T model) {
        U node = controllerMap.remove(model);
        if (node == null)
            throw new IllegalStateException();
    }

}
