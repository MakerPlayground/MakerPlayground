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

package io.makerplayground.device.actual;

import javafx.scene.paint.Color;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public enum ConnectionType {
    INTEGRATED(List.of(), 0),
    WIRE(List.of(Color.BLUE), 4),
    GROVE(List.of(Color.BLACK, Color.RED, Color.WHITE, Color.YELLOW), 4),
    MAKER_PLAYGROUND(List.of(Color.BLACK, Color.RED, Color.WHITE, Color.YELLOW), 4),
    M5STACK(List.of(Color.BLACK, Color.RED, Color.YELLOW, Color.WHITE), 4),
    KIDBRIGHT(List.of(Color.BLACK, Color.YELLOW, Color.WHITE, Color.RED, Color.RED), 4),
    INEX3(List.of(Color.BLACK, Color.WHITE, Color.RED), 3),
    INEX_SERVO(List.of(Color.BLACK, Color.RED, Color.WHITE), 3),
    UNO_SHIELD(Collections.emptyList(), 0),
    CSI(List.of(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE), 3.2);

    private final List<Color> pinColors;
    private final double lineWidth;

    ConnectionType(List<Color> colors, double lineWeight) {
        this.pinColors = colors;
        this.lineWidth = lineWeight;
    }

    public boolean canConsume(ConnectionType type) {
        return this == type;
    }
}
