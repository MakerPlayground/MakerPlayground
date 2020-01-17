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

package io.makerplayground.ui.control;

import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class AutoResizeTextField extends TextField {
    public AutoResizeTextField() {
        this("");
    }

    public AutoResizeTextField(String text) {
        super(text);
        fontProperty().addListener((observable, oldValue, newValue) -> adjustPreferredWidth());
        textProperty().addListener((observable, oldValue, newValue) -> adjustPreferredWidth());
    }

    // TODO: this calculation only works when -fx-padding: 0px is set in the CSS
    private void adjustPreferredWidth() {
        // update the width of the nameTextField based on it's content by measuring the width of the text using Text
        // instance with the same content. 'W' is added to reserved space for the next character and to prevent the
        // the text from overflow the textfield and force the first character to hide below the left edge.
        // Note that we should have used nameTextField.prefColumnCountProperty() but that property computes the
        // preferred width based on the width of character 'W' multiply by the number of character in the text
        // which is way off from the actual width when the font isn't monospaced
        Text t = new Text(getText() + "W");
        t.setFont(getFont());
        setPrefWidth(t.getBoundsInLocal().getWidth());
    }
}
