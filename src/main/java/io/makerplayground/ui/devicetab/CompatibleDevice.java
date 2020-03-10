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

package io.makerplayground.ui.devicetab;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.project.ProjectDevice;

import java.util.Objects;
import java.util.Optional;

public class CompatibleDevice implements Comparable<CompatibleDevice> {
    private ActualDevice actualDevice;
    private ProjectDevice projectDevice;

    public CompatibleDevice(ActualDevice actualDevice) {
        this.actualDevice = actualDevice;
    }

    public CompatibleDevice(ProjectDevice projectDevice) {
        this.projectDevice = projectDevice;
    }

    public Optional<ActualDevice> getActualDevice() {
        return Optional.ofNullable(actualDevice);
    }

    public Optional<ProjectDevice> getProjectDevice() {
        return Optional.ofNullable(projectDevice);
    }

    @Override
    public String toString() {
        if (actualDevice != null) {
            return actualDevice.getBrand() + " " + actualDevice.getModel();
        } else {
            return "Use the same device as " + projectDevice.getName();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompatibleDevice that = (CompatibleDevice) o;
        return ((actualDevice != null) && (actualDevice == that.actualDevice))
                || ((projectDevice != null) && (projectDevice == that.projectDevice));
    }

    @Override
    public int hashCode() {
        if (actualDevice != null) {
            return Objects.hash(actualDevice);
        } else {
            return Objects.hash(projectDevice);
        }
    }

    @Override
    public int compareTo(CompatibleDevice that) {
        if (projectDevice != null && that.projectDevice != null) {
            return ProjectDevice.NAME_COMPARATOR.compare(projectDevice, that.projectDevice);
        } else if (actualDevice != null && that.actualDevice != null) {
            return ActualDevice.NAME_COMPARATOR.compare(actualDevice, that.actualDevice);
        } else if (projectDevice != null) { // that.projectDevice always null in this case
            return -1;
        } else if (that.projectDevice != null) { // this.projectDevice always null in this case
            return 1;
        }
        throw new IllegalStateException("CompatibleDevice must contains neither projectDevice nor actualDevice");
    }
}
