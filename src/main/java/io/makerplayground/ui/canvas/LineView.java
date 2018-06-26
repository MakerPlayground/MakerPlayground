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

import io.makerplayground.ui.canvas.event.InteractiveNodeEvent;
import io.makerplayground.ui.canvas.node.InteractiveNode;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Path;

/**
 *
 */
public class LineView extends InteractiveNode {

    private static final int REMOVE_BTN_GAP = 20;

    private final LineViewModel viewModel;

    public LineView(LineViewModel viewModel, InteractivePane interactivePane) {
        super(interactivePane);
        this.viewModel = viewModel;

        Image removeButtonImage = new Image(getClass().getResourceAsStream("/icons/cancelLine.png"));
        ImageView removeButton = new ImageView(removeButtonImage);
        removeButton.layoutXProperty().bind(viewModel.centerXProperty().subtract(removeButtonImage.getWidth()/2)
                .add(viewModel.centerUnitTangentXProperty().multiply(REMOVE_BTN_GAP)));
        removeButton.layoutYProperty().bind(viewModel.centerYProperty().subtract(removeButtonImage.getHeight()/2)
                .add(viewModel.centerUnitTangentYProperty().multiply(REMOVE_BTN_GAP)));
        removeButton.visibleProperty().bind(selectedProperty());
        // remove line when press the remove button
        removeButton.setOnMousePressed(event -> fireEvent(new InteractiveNodeEvent(this, null
                , InteractiveNodeEvent.REMOVED, null, null, 0, 0)));

        Path path = new Path();
        path.setStrokeWidth(3.25);
        path.setStyle("-fx-stroke: #313644;");
        Bindings.bindContentBidirectional(path.getElements(), viewModel.getPoint());

        getChildren().addAll(path, removeButton);

        // TODO: Consume the event to avoid the interactive pane from accepting it and deselect every node
        setOnMousePressed(Event::consume);
    }

    public LineViewModel getLineViewModel() {
        return viewModel;
    }

    @Override
    protected boolean isError() {
        return false;
    }
}
