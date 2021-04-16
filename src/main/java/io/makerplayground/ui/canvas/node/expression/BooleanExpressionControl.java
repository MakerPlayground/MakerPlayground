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

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.BooleanExpression;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.term.Operator;
import io.makerplayground.ui.canvas.node.expression.custom.NumericChipField;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BooleanExpressionControl extends VBox {

    private final ReadOnlyObjectWrapper<BooleanExpression> expression = new ReadOnlyObjectWrapper<>();
    private final ObservableList<ProjectValue> projectValues;
    private final List<EntryView> entryViewList;
    private final ImageView addImageView;

    public BooleanExpressionControl(BooleanExpression expression, ObservableList<ProjectValue> projectValues) {
        this.expression.set(expression);
        this.projectValues = projectValues;
        this.entryViewList = new ArrayList<>();

        addImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/add-expression.png")));
        addImageView.setFitHeight(25);
        addImageView.setFitWidth(25);
        addImageView.setPreserveRatio(true);
        addImageView.setOnMousePressed(event -> {
            BooleanExpression.Entry entry = new BooleanExpression.Entry(new CustomNumberExpression(), Operator.LESS_THAN, new CustomNumberExpression());
            expression.getEntries().add(entry);
            createExpressionRowControl(entry);
        });

        for (BooleanExpression.Entry entry : expression.getEntries()) {
            createExpressionRowControl(entry);
        }

        setSpacing(5);
        invalidateView();
    }

    private void createExpressionRowControl(BooleanExpression.Entry entry) {
        EntryView entryView = new EntryView(entry, projectValues);
        entryView.setOnRemoveButtonPressed(event -> {
            expression.get().getEntries().remove(entry);
            getChildren().remove(entryView);
            entryViewList.remove(entryView);
            invalidateView();
        });
        getChildren().add(getChildren().size(), entryView);
        entryViewList.add(entryView);
        invalidateView();
    }

    private void invalidateView() {
        for (EntryView view : entryViewList) {
            view.setOperatorVisible(true);
            view.setRemoveButtonVisible(true);
            view.getChildren().remove(addImageView);
        }
        // hide 'and' label of the last expression entry
        entryViewList.get(entryViewList.size() - 1).setOperatorVisible(false);
        // hide remove button when there is only 1 entry in the expression
        if (entryViewList.size() == 1) {
            entryViewList.get(0).setRemoveButtonVisible(false);
        }
        // add 'add' button to the last row
        entryViewList.get(entryViewList.size() - 1).getChildren().add(addImageView);
    }

    public BooleanExpression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<BooleanExpression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }

    private static class EntryView extends HBox {
        private static final DecimalFormat df = new DecimalFormat("#,###.###");
        private ImageView removeImageView;
        private Label operatorLabel;

        public EntryView(BooleanExpression.Entry entry, ObservableList<ProjectValue> projectValues) {
            NumericChipField leftChipField = new NumericChipField(entry.getFirstOperand(), projectValues);
            leftChipField.expressionProperty().addListener((observable, oldValue, newValue) -> entry.setFirstOperand(newValue));

            ComboBox<Operator> operatorComboBox = new ComboBox<>(FXCollections.observableArrayList(Operator.getComparisonOperator()));
            if (entry.getOperator() != null) {
                operatorComboBox.setValue(entry.getOperator());
            }
            operatorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> entry.setOperator(newValue));

            NumericChipField rightChipField = new NumericChipField(entry.getSecondOperand(), projectValues);
            rightChipField.expressionProperty().addListener((observable, oldValue, newValue) -> entry.setSecondOperand(newValue));

            operatorLabel = new Label("and");
            operatorLabel.setMinHeight(25); // a hack to center the label to the height of 1 row control when the control spans to multiple rows
            operatorLabel.managedProperty().bind(operatorLabel.visibleProperty());

            removeImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/remove-expression.png")));
            removeImageView.setFitHeight(25);
            removeImageView.setFitWidth(25);
            removeImageView.setPreserveRatio(true);
            removeImageView.managedProperty().bind(removeImageView.visibleProperty());

            setAlignment(Pos.TOP_LEFT);
            setSpacing(5);
            getChildren().addAll(leftChipField, operatorComboBox, rightChipField, removeImageView, operatorLabel);
        }

        void setOnRemoveButtonPressed(EventHandler<MouseEvent> e) {
            removeImageView.setOnMousePressed(e);
        }

        void setRemoveButtonVisible(boolean b) {
            removeImageView.setVisible(b);
        }

        void setOperatorVisible(boolean b) {
            operatorLabel.setVisible(b);
        }
    }
}
