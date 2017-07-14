/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.canvas;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Path;

/**
 *
 */
public class LineView extends Group implements Selectable{
    private final Path path;
    private final Button removeLineBtn;
    private final LineViewModel viewModel;

    private BooleanProperty select;

    public LineView(LineViewModel viewModel) {
        this.viewModel = viewModel;
        this.select = new SimpleBooleanProperty();
        this.select.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
            } else {
                setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
            }
        });
        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                select.set(true);
                event.consume();
            }
        });
        BackgroundImage backgroundImage = new BackgroundImage( new Image( getClass().getResource("/icons/cancelLine.png").toExternalForm()),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);
        removeLineBtn = new Button();
        removeLineBtn.layoutXProperty().bind(viewModel.centerXProperty());
        removeLineBtn.layoutYProperty().bind(viewModel.centerYProperty());
        removeLineBtn.setVisible(false);
        removeLineBtn.setMinSize(50,50);
        removeLineBtn.setMaxSize(50,50);
        removeLineBtn.visibleProperty().bind(select);
        removeLineBtn.setBackground(background);

        path = new Path();
        path.setStrokeWidth(3.25);
        path.setStyle("-fx-stroke: #313644;");

        setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
        Bindings.bindContentBidirectional(path.getElements(), viewModel.getPoint());

        getChildren().addAll(path, removeLineBtn);
//        path.setOnMouseEntered(mouseEvent -> {
//            if (!mouseEvent.isPrimaryButtonDown()) {
//                getScene().setCursor(javafx.scene.Cursor.HAND);
//            }
//            else removeLineBtn.setVisible(false);
//        });
//        path.setOnMousePressed(mouseEvent -> {
//            if(mouseEvent.isPrimaryButtonDown()){
//                setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
//                removeLineBtn.setVisible(true);
//            }
//            else removeLineBtn.setVisible(false);
//
//            getScene().setCursor(javafx.scene.Cursor.MOVE);
//        });
//        path.setOnMouseReleased(mouseEvent -> {
//            getScene().setCursor(javafx.scene.Cursor.HAND);
//        });
//        setOnMouseExited(mouseEvent -> {
//            if (!mouseEvent.isPrimaryButtonDown()) {
//                getScene().setCursor(javafx.scene.Cursor.DEFAULT);
//            }
//        });
    }

    @Override
    public BooleanProperty selectedProperty() {
        return select;
    }

    @Override
    public boolean isSelected() {
        return select.get();
    }

    @Override
    public void setSelected(boolean b) {
        select.set(b);
    }

    public void setOnAction(EventHandler<ActionEvent> event) {
        removeLineBtn.setOnAction(event);
    }

    public Button getRemoveLineBtn() {
        return removeLineBtn;
    }
}
