/*
 * Copyright (c) 2020. The Maker Playground Authors.
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

package io.makerplayground.device.shared.constraint;

import io.makerplayground.device.shared.Unit;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StringIntegerCategoricalConstraint implements Constraint {

    private final LinkedHashMap<String, Integer> map;

    public LinkedHashMap<String, Integer> getMap() {
        return map;
    }

    public StringIntegerCategoricalConstraint(LinkedHashMap<String, Integer> map) {
        this.map = map;
    }

    public StringIntegerCategoricalConstraint(String key, int value) {
        this.map = new LinkedHashMap<>(Collections.singletonMap(key, value));
    }

    @Override
    public boolean test(double d, Unit unit) {
        return false;
    }

    @Override
    public boolean test(String s) {
        return map.containsKey(s);
    }

    @Override
    public boolean test(Integer i) {
        return map.containsValue(i);
    }

    @Override
    public boolean isCompatible(Constraint constraint) {
        if (!(constraint instanceof StringIntegerCategoricalConstraint)) {
            return false;
        }
        Map<String, Integer> thatMap = ((StringIntegerCategoricalConstraint) constraint).getMap();
        for (String s: thatMap.keySet()) {
            if (!this.map.containsKey(s) || !thatMap.get(s).equals(map.get(s))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Constraint union(Constraint constraint) {
        if (!(constraint instanceof StringIntegerCategoricalConstraint)) {
            return this;
        }
        LinkedHashMap<String, Integer> thatMap = ((StringIntegerCategoricalConstraint) constraint).getMap();
        LinkedHashMap<String, Integer> newMap = new LinkedHashMap<>();
        newMap.putAll(thatMap);
        newMap.putAll(map);
        return new StringIntegerCategoricalConstraint(newMap);
    }

    @Override
    public Constraint intersect(Constraint constraint) {
        if (!(constraint instanceof StringIntegerCategoricalConstraint)) {
            return this;
        }
        LinkedHashMap<String, Integer> thatMap = ((StringIntegerCategoricalConstraint) constraint).getMap();
        LinkedHashMap<String, Integer> newMap = new LinkedHashMap<>();
        map.forEach((s, integer) -> {
            if (map.containsKey(s) && thatMap.containsKey(s) && integer.equals(thatMap.get(s))) {
                newMap.put(s, integer);
            }
        });
        return new StringIntegerCategoricalConstraint(newMap);
    }
}
