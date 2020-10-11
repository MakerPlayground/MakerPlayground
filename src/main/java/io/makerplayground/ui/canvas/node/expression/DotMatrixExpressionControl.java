/*
 * Copyright (c) 2020. The Maker Playground Authors.
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

import io.makerplayground.device.shared.DotMatrix;
import io.makerplayground.device.shared.RGBDotMatrix;
import io.makerplayground.device.shared.SingleColorDotMatrix;
import io.makerplayground.project.expression.DotMatrixExpression;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Cursor;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.PopupWindow;

public class DotMatrixExpressionControl extends VBox {

    private ReadOnlyObjectWrapper<DotMatrixExpression> expression;
    private boolean erasing = false;

    public DotMatrixExpressionControl(DotMatrixExpression expression) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        initControl();
    }

    private void initControl() {
        this.setSpacing(3);
        this.getChildren().clear();

        ColorPicker colorPicker = new ColorPicker();
        // A hack to prevent the setting popup from disappearing and prevent users from using the custom color dialog inside of the color picker
        colorPicker.setOnShowing(event -> {
            if (getScene().getWindow() instanceof PopupWindow) {
                ((PopupWindow) getScene().getWindow()).setAutoHide(false);
            }
        });
        colorPicker.setOnHiding(event -> {
            if (getScene().getWindow() instanceof PopupWindow) {
                ((PopupWindow) getScene().getWindow()).setAutoHide(true);
            }
        });

        GridPane gridPaneDot = new GridPane();
        DotMatrix dotMatrix = expression.get().getDotMatrix();
        for (int i=0; i<dotMatrix.getRow(); i++) {
            for (int j=0; j<dotMatrix.getColumn(); j++) {
                Rectangle rectangle = new Rectangle(12, 12);
                rectangle.setStyle("-fx-stroke: #cccccc; -fx-stroke-width: 1;");
                if (dotMatrix instanceof SingleColorDotMatrix) {
                    rectangle.fillProperty().bind(Bindings.when(((SingleColorDotMatrix) dotMatrix).getData()[i][j]).then(Color.rgb(255, 0, 0)).otherwise(Color.BLACK));
                } else {
                    rectangle.fillProperty().bind(((RGBDotMatrix) dotMatrix).getData()[i][j]);
                }
                int finalI = i;
                int finalJ = j;
                rectangle.setOnMousePressed(event -> {
                    requestFocus();
                    if (dotMatrix instanceof SingleColorDotMatrix) {
                        ((SingleColorDotMatrix) dotMatrix).set(finalI, finalJ, !event.isSecondaryButtonDown());
                    } else {
                        ((RGBDotMatrix) dotMatrix).set(finalI, finalJ, event.isSecondaryButtonDown() ? Color.BLACK : colorPicker.getValue());
                    }
                });
                rectangle.setOnMouseReleased(event -> getScene().setCursor(Cursor.DEFAULT));
                rectangle.setOnDragDetected(event -> {
                    requestFocus();
                    startFullDrag();
                    erasing = event.isSecondaryButtonDown();
                });
                rectangle.setOnMouseDragEntered(event -> {
                    if (dotMatrix instanceof SingleColorDotMatrix) {
                        ((SingleColorDotMatrix) dotMatrix).set(finalI, finalJ, !erasing);
                    } else {
                        ((RGBDotMatrix) dotMatrix).set(finalI, finalJ, erasing ? Color.BLACK : colorPicker.getValue());
                    }
                });
                rectangle.setOnMouseDragReleased(event -> {
                    getScene().setCursor(Cursor.DEFAULT);
                });
                gridPaneDot.add(rectangle, j, i);
            }
        }

        this.getChildren().add(gridPaneDot);
        this.getChildren().add(colorPicker);

        GridPane gridPaneControl = new GridPane();
        gridPaneControl.setHgap(5);
        gridPaneControl.setVgap(0);

        gridPaneControl.add(new Label("Column"), 0, 0);

        ImageView addColumnButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/add-expression.png")));
        addColumnButton.setFitHeight(25);
        addColumnButton.setFitWidth(25);
        addColumnButton.setPreserveRatio(true);
        addColumnButton.setOnMouseClicked(event -> {
            dotMatrix.resize(dotMatrix.getRow(), dotMatrix.getColumn() + 1);
            initControl();
        });
        gridPaneControl.add(addColumnButton, 2, 0);

        if (dotMatrix.getColumn() > 1) {
            ImageView removeColumnButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/remove-expression.png")));
            removeColumnButton.setFitHeight(25);
            removeColumnButton.setFitWidth(25);
            removeColumnButton.setPreserveRatio(true);
            removeColumnButton.setOnMouseClicked(event -> {
                dotMatrix.resize(dotMatrix.getRow(), dotMatrix.getColumn() - 1);
                initControl();
            });
            gridPaneControl.add(removeColumnButton, 3, 0);
        }

        gridPaneControl.add(new Label("Row"), 0, 1);

        ImageView addRowButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/add-expression.png")));
        addRowButton.setFitHeight(25);
        addRowButton.setFitWidth(25);
        addRowButton.setPreserveRatio(true);
        addRowButton.setOnMouseClicked(event -> {
            dotMatrix.resize(dotMatrix.getRow() + 1, dotMatrix.getColumn());
            initControl();
        });
        gridPaneControl.add(addRowButton, 2, 1);

        if (dotMatrix.getRow() > 1) {
            ImageView removeRowButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/remove-expression.png")));
            removeRowButton.setFitHeight(25);
            removeRowButton.setFitWidth(25);
            removeRowButton.setPreserveRatio(true);
            removeRowButton.setOnMouseClicked(event -> {
                dotMatrix.resize(dotMatrix.getRow() - 1, dotMatrix.getColumn());
                initControl();
            });
            gridPaneControl.add(removeRowButton, 3, 1);
        }

        this.getChildren().add(gridPaneControl);
    }

    public ReadOnlyObjectWrapper<DotMatrixExpression> expressionProperty() {
        return expression;
    }
}
