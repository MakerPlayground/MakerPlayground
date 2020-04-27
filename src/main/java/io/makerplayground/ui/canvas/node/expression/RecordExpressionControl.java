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

package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.Record;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.RecordExpression;
import io.makerplayground.ui.canvas.node.expression.valuelinking.SpinnerNumberWithUnitNoBindingExpressionControl;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class RecordExpressionControl extends VBox {
    private static final Parameter parameter = new Parameter("Field", DataType.DOUBLE, NumberWithUnit.ZERO, NumericConstraint.NONE, ControlType.SPINBOX);
    private final ObservableList<ProjectValue> projectValues;
    private final ReadOnlyObjectWrapper<RecordExpression> expression;

    private List<RecordExpressionControlEntry> entryList;

    public RecordExpressionControl(ObservableList<ProjectValue> projectValues, RecordExpression expression) {
        this.projectValues = projectValues;
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        initControl();
    }

    private void initControl() {
        Button addButton = new Button("+");
        addButton.setOnAction(actionEvent -> {
            RecordExpressionControlEntry newEntry = new RecordExpressionControlEntry(new RecordEntry());
            entryList.add(newEntry);
            initEntryControl(newEntry);
            invalidate();
        });

        setSpacing(5.0);
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(addButton);

        entryList = expression.get().getRecord().getEntryList().stream().map(RecordExpressionControlEntry::new).collect(Collectors.toList());

        for (RecordExpressionControlEntry entry : entryList) {
            initEntryControl(entry);
        }
    }

    private void initEntryControl(RecordExpressionControlEntry entry) {
        HBox hbox = new HBox();

        TextField textField = new TextField();
        textField.setPromptText("Field name");
        textField.setText(entry.field);
        NumberWithUnitNoBindingExpressionControl expressionControl = new SpinnerNumberWithUnitNoBindingExpressionControl(parameter, projectValues, entry.value);
        Button removeButton = new Button("-");
        removeButton.setOnAction(actionEvent -> {
            entryList.remove(entry);
            getChildren().remove(hbox);
            invalidate();
        });

        textField.textProperty().addListener((observable, oldKey, newKey) -> {
            entry.field = newKey;
            invalidate();
        });
        expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> {
            entry.value = newValue;
            invalidate();
        });

        hbox.setSpacing(5.0);
        hbox.setAlignment(Pos.TOP_LEFT);
        hbox.getChildren().addAll(textField, expressionControl, removeButton);
        getChildren().add(getChildren().size() - 1, hbox);
    }

    private void invalidate() {
        List<RecordEntry> entry = entryList.stream().map(e -> new RecordEntry(e.field, e.value)).collect(Collectors.toList());
        expression.set(new RecordExpression(new Record(entry)));
    }

    public ReadOnlyObjectProperty<RecordExpression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }

    private static class RecordExpressionControlEntry {
        String field;
        Expression value;

        RecordExpressionControlEntry(RecordEntry entry) {
            this.field = entry.getField();
            this.value = entry.getValue();
        }
    }
}
