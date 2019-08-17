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

package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.CategoricalConstraint;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.project.expression.*;
import io.makerplayground.ui.canvas.node.expression.RTCExpressionControl;
import io.makerplayground.ui.canvas.node.expression.RecordExpressionControl;
import io.makerplayground.ui.canvas.node.expression.custom.MultiFunctionNumericControl;
import io.makerplayground.ui.canvas.node.expression.custom.StringChipField;
import io.makerplayground.ui.canvas.node.expression.valuelinking.SliderNumberWithUnitExpressionControl;
import io.makerplayground.ui.canvas.node.expression.valuelinking.SpinnerNumberWithUnitExpressionControl;
import io.makerplayground.ui.canvas.node.expression.StringExpressionControl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class SceneDevicePropertyWindow extends PopOver {

    public SceneDevicePropertyWindow(SceneDeviceIconViewModel viewModel) {
        DevicePropertyPane devicePropertyPane = new DevicePropertyPane(viewModel.getUserSetting(), viewModel.getProject(), true);
        devicePropertyPane.setPadding(new Insets(20, 20, 20, 20));

        setDetachable(false);
        setContentNode(devicePropertyPane);
    }

}
