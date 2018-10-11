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

package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.Value;

@JsonSerialize(using = ProjectValueSerializer.class)
public class ProjectValue {
    private final ProjectDevice device;
    private final Value value;

    public ProjectValue(ProjectDevice device, Value value) {
        this.device = device;
        this.value = value;
    }

    public ProjectDevice getDevice() {
        return device;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ProjectValue{" +
                "device=" + device.getName() +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectValue that = (ProjectValue) o;

        if (device != null ? !device.equals(that.device) : that.device != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = device != null ? device.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
