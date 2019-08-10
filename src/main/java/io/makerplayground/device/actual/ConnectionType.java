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
    WIRE(List.of(Color.YELLOW), 2, false),
    GROVE(List.of(Color.WHITE, Color.YELLOW, Color.RED, Color.BLACK), 2, true),
    MAKER_PLAYGROUND(List.of(Color.YELLOW, Color.WHITE, Color.RED, Color.BLACK), 2, true),
    M5STACK(List.of(Color.WHITE, Color.YELLOW, Color.RED, Color.BLACK), 2, true),
    INEX3(List.of(Color.RED, Color.WHITE, Color.BLACK), 2, true),
    UNO_SHIELD(Collections.emptyList(), 0, true);

    private final List<Color> pinColors;
    private final double lineWidth;
    private final boolean splittable;

    ConnectionType(List<Color> colors, double lineWeight, boolean splittable) {
        this.pinColors = colors;
        this.lineWidth = lineWeight;
        this.splittable = splittable;
    }

    public boolean canConsume(ConnectionType type) {
        return this == type;
    }
}
