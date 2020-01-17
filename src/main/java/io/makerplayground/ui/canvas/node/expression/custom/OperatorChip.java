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

import io.makerplayground.project.term.Operator;
import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.OperatorType;
import io.makerplayground.project.term.Term;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.io.IOException;

public class OperatorChip extends Chip<Operator> {

    @FXML private Path background;
    @FXML private Text text;

    public OperatorChip(Operator initialValue) {
        super(initialValue, Term.Type.OPERATOR);
    }

    @Override
    protected void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/Operator.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        text.setText(getValue().toString());

        if (getValue().getType() == OperatorType.LEFT_UNARY) {
            background.getElements().addAll(new MoveTo(10, 0)
                    , new ArcTo(12.5, 12.5, 90, 0, 10, false, false)
                    , new LineTo(0, 15)
                    , new ArcTo(12.5, 12.5, 0, 10, 25, false, false)
                    , new LineTo(25, 25)
                    , new ArcTo(12.5, 12.5, 0, 15, 15, false, true)
                    , new LineTo(15, 10)
                    , new ArcTo(12.5, 12.5, 0, 25, 0, false, true)
                    , new ClosePath());
            StackPane.setMargin(text, new Insets(0, 7.5, 0, 0));
        } else if (getValue().getType() == OperatorType.BINARY) {
            background.getElements().addAll(new MoveTo(10, 0)
                    , new LineTo(0, 0)
                    , new ArcTo(12.5, 12.5, 90, 10, 10, false, true)
                    , new LineTo(10, 15)
                    , new ArcTo(12.5, 12.5, 0, 0, 25, false, true)
                    , new LineTo(35, 25)
                    , new ArcTo(12.5, 12.5, 0, 25, 15, false, true)
                    , new LineTo(25, 10)
                    , new ArcTo(12.5, 12.5, 0, 35, 0, false, true)
                    , new ClosePath());
        } else if (getValue().getType() == OperatorType.RIGHT_UNARY) {
            background.getElements().addAll(new MoveTo(15, 0)
                    , new LineTo(0, 0)
                    , new ArcTo(12.5, 12.5, 90, 10, 10, false, true)
                    , new LineTo(10, 15)
                    , new ArcTo(12.5, 12.5, 0, 0, 25, false, true)
                    , new LineTo(15, 25)
                    , new ArcTo(12.5, 12.5, 0, 25, 15, false, false)
                    , new LineTo(25, 10)
                    , new ArcTo(12.5, 12.5, 0, 15, 0, false, false)
                    , new ClosePath());
            StackPane.setMargin(text, new Insets(0, 0, 0, 7.5));
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public OperatorTerm getTerm() {
        return new OperatorTerm(getValue());
    }
}
