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

import io.makerplayground.device.shared.*;
import io.makerplayground.project.term.StringAnimationTerm;
import io.makerplayground.project.term.Term;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.stream.Collectors;

public class StringAnimationChip extends Chip<AnimatedValue> {

    @FXML private Rectangle background;
    @FXML private Label input;

    private static final DecimalFormat df = new DecimalFormat("0.######");

    public StringAnimationChip(AnimatedValue animatedValue) {
        super(animatedValue, Term.Type.STRING_ANIMATED);
    }

    @Override
    protected void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/StringAnimationChip.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        valueProperty().addListener(((observable, oldValue, newValue) -> redraw()));
        redraw();

        // update width of the background based on the combobox width
        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> background.setWidth(newValue.getWidth()));
    }

    private void redraw() {
        if (getValue() instanceof ContinuousAnimatedValue) {
            String startValue = ((ContinuousAnimatedValue) getValue()).getStartValue().getTerms().stream().map(Term::toString).collect(Collectors.joining(" "));
            String endValue = ((ContinuousAnimatedValue) getValue()).getEndValue().getTerms().stream().map(Term::toString).collect(Collectors.joining(" "));
            input.setText(startValue + " - " + endValue);
        } else if (getValue() instanceof StringCategoricalAnimatedValue) {
            input.setText(((StringCategoricalAnimatedValue) getValue()).getKeyValues().stream()
                    .map((kv) -> kv.getValue().getTerms().stream().map(Term::toString).collect(Collectors.joining(" ")))
                    .collect(Collectors.joining(", ")));
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public Term getTerm() {
        return new StringAnimationTerm(getValue());
    }
}
