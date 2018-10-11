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

package io.makerplayground.ui.dialog;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.util.function.BooleanSupplier;

public class UndecoratedDialog extends Stage {

    private BooleanSupplier closingPredicate = () -> true;

    public UndecoratedDialog(Window owner) {
        initOwner(owner);
        initStyle(StageStyle.TRANSPARENT);

        // always center to the parent window
        widthProperty().addListener((observable, oldValue, newValue) -> {
            setX(owner.getX() + owner.getWidth() / 2 - newValue.doubleValue() / 2);
        });
        heightProperty().addListener((observable, oldValue, newValue) -> {
            setY(owner.getY() + owner.getHeight() / 2 - newValue.doubleValue() / 2);
        });

        // allow the dialog to be closed using escape key
        addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
            if (KeyCode.ESCAPE == event.getCode() && closingPredicate.getAsBoolean()) {
                hide();
            }
        });

        // JavaFX's modal stage blocks event to other stage so we can't allow user to close this dialog by pressing at
        // the surround space. Thus, we consume every mouse event to the parent window here (except MOUSE_EXITED_TARGET)
        // to simulate behaviour of a modal dialog and close ourselves when detect MOUSE_PRESSED at the parent window
        Parent rootPane = owner.getScene().getRoot();
        EventHandler<MouseEvent> mouseEventFilter = event -> {
            // we shouldn't consume MOUSE_EXITED_TARGET otherwise the skin of a button we've pressed to open this dialog
            // will stuck in the press state as it doesn't receive MOUSE_EXITED_TARGET
            if (event.getEventType() == MouseEvent.MOUSE_EXITED_TARGET) {
                return;
            }
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED && closingPredicate.getAsBoolean()) {
                hide();
            }
            event.consume();
        };
        rootPane.addEventFilter(MouseEvent.ANY, mouseEventFilter);

        // dim the parent window after the dialog is shown on the screen
        Effect previousEffect = rootPane.getEffect();

        ColorAdjust colorAdjust = new ColorAdjust(0, 0, 0, 0);
        rootPane.setEffect(colorAdjust);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(750),
                new KeyValue(colorAdjust.brightnessProperty(), -0.25)
        ));
        addEventHandler(WindowEvent.WINDOW_SHOWING, event -> {
            timeline.play();
        });
        addEventHandler(WindowEvent.WINDOW_HIDDEN, t -> {
            rootPane.removeEventFilter(MouseEvent.ANY, mouseEventFilter);
            rootPane.setEffect(previousEffect);
        });
    }

    public void setContent(Parent root) {
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    public void setClosingPredicate(BooleanSupplier supplier) {
        this.closingPredicate = supplier;
    }
}
