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

package io.makerplayground.ui.canvas.node.expression.valuelinking;

import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.ui.canvas.node.expression.NumberWithUnitExpressionControl;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.NumberWithUnitControl;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.SliderWithUnit;

import java.util.List;

public class SliderNumberWithUnitExpressionControl extends NumberWithUnitExpressionControl {

    public SliderNumberWithUnitExpressionControl(Parameter p, List<ProjectValue> projectValues, Expression expression) {
        super(p, projectValues, expression);
    }

    @Override
    protected NumberWithUnitControl createNumberWithUnitControl(double min, double max, List<Unit> unit, NumberWithUnit initialValue) {
        return new SliderWithUnit(min, max, unit, initialValue);
    }
}
