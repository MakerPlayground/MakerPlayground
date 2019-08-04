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

package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.project.term.StringTerm;
import io.makerplayground.project.term.Term;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

public class StringChip extends Chip<String> {

    @FXML private Rectangle background;
    @FXML private TextField input;

    public StringChip(String initialValue) {
        super(initialValue, Term.Type.STRING);
    }

    @Override
    protected void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/StringChip.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        input.setText(getValue());
        input.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                setValue(input.getText());
            }
        });
        input.prefColumnCountProperty().bind(input.textProperty().length());

        // update width of the background based on the combobox width
        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> background.setWidth(newValue.getWidth()));
    }

    @Override
    public Term getTerm() {
        return new StringTerm(getValue());
    }
}
