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

package io.makerplayground.project.term;

import io.makerplayground.device.shared.AnimatedValue;
import io.makerplayground.device.shared.ContinuousAnimatedValue;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.NumericCategoricalAnimatedValue;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

public class NumberAnimationTerm extends Term {
    public NumberAnimationTerm(AnimatedValue value) {
        super(Type.NUMBER_ANIMATED, value);
    }

    @Override
    public AnimatedValue getValue() {
        return (AnimatedValue) value;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    public String toString() {
        if (getValue() instanceof ContinuousAnimatedValue) {
            String startValue = ((ContinuousAnimatedValue) getValue()).getStartValue().getTerms().stream().map(Term::toString).collect(Collectors.joining(" "));
            String endValue = ((ContinuousAnimatedValue) getValue()).getEndValue().getTerms().stream().map(Term::toString).collect(Collectors.joining(" "));
            return startValue + " - " + endValue + "(" + ((ContinuousAnimatedValue) getValue()).getEasing().getName() + ")";
        } else if (getValue() instanceof NumericCategoricalAnimatedValue) {
            return ((NumericCategoricalAnimatedValue) getValue()).getKeyValues().stream()
                    .map((kv) -> kv.getValue().getTerms().stream().map(Term::toString).collect(Collectors.joining(" ")))
                    .collect(Collectors.joining(", "));
        } else {
            throw new IllegalStateException();
        }
    }
}
