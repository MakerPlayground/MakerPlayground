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

package io.makerplayground.ui.canvas.node;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public class SelectionGroup<T extends Selectable> {
    //private Selectable selectingSelectable;
    private final ObservableList<T> selectable;
    private final FilteredList<T> selected;
    private final BooleanProperty multipleSelection;

    public SelectionGroup() {
        selectable = FXCollections.observableArrayList(param -> new Observable[] {param.selectedProperty()});
        selectable.addListener(new ListChangeListener<Selectable>() {
            @Override
            public void onChanged(Change<? extends Selectable> c) {
                while (c.next()) {
                    if (c.wasPermutated()) {

                    } else if (c.wasUpdated()) {
                        //update item
                    } else {
                        for (Selectable remitem : c.getRemoved()) {

                        }
                        for (Selectable additem : c.getAddedSubList()) {
                            additem.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                // When multiple selection is disabled and this node is being selected, we deselect every other nodes
                                if (!multipleSelection.get() && newValue) {
                                    for (Selectable s : selectable) {
                                        if (s != additem)
                                            s.setSelected(false);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
        multipleSelection = new SimpleBooleanProperty(false);
        selected = new FilteredList<>(selectable, Selectable::isSelected);
    }

    public ObservableList<T> getSelectable() {
        return selectable;
    }

    public ObservableList<T> getSelected() {
        return selected;
    }

    public void setSelected(Selectable s) {
        deselect();
        s.setSelected(true);
    }

    public void setSelected(List<Selectable> l) {
        deselect();
        l.forEach(s -> s.setSelected(true));
    }

    public boolean isMultipleSelection() {
        return multipleSelection.get();
    }

    public BooleanProperty multipleSelectionProperty() {
        return multipleSelection;
    }

    public void setMultipleSelection(boolean multipleSelection) {
        this.multipleSelection.set(multipleSelection);
    }

    public void deselect() {
        for (Selectable s : selectable) {
            s.setSelected(false);
        }
    }
}
