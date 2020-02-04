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

package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.project.term.Term;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;

import java.util.Collections;
import java.util.List;

public abstract class Chip<T> extends StackPane {
    private final Term.Type type;
    private final ReadOnlyObjectWrapper<T> value = new ReadOnlyObjectWrapper<>();
    private final ObservableList<T> choices;

    public Chip(T initialValue, Term.Type type) {
        this(initialValue, type, FXCollections.emptyObservableList());
    }

    public Chip(T initialValue, Term.Type type, ObservableList<T> choices) {
        this.type = type;
        this.value.set(initialValue);
        this.choices = choices;

        initView();
        // use geometric shape of this node instead of the bounding box for mouse event
        setPickOnBounds(false);
    }

    protected abstract void initView();

    public abstract Term getTerm();

    public Term.Type getChipType() {
        return type;
    }

    public T getValue() {
        return value.get();
    }

    public ReadOnlyObjectProperty<T> valueProperty() {
        return value.getReadOnlyProperty();
    }

    protected void setValue(T value) {
        this.value.set(value);
    }

    public ObservableList<T> getChoices() {
        return choices;
    }
}
