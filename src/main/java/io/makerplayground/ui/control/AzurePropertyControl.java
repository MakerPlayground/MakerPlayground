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

package io.makerplayground.ui.control;

import io.makerplayground.ui.dialog.AzureSettingDialog;
import io.makerplayground.util.AzureResource;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class AzurePropertyControl<T extends AzureResource> extends HBox {

    private final ReadOnlyObjectWrapper<T> value;
    private final Label resourceName;
    private final Button button;

    public AzurePropertyControl(AzureSettingDialog.Service service, T currentValue) {
        value = new ReadOnlyObjectWrapper<>(currentValue);

        resourceName = new Label();
        button = new Button();
        button.setOnAction(event -> {
            AzureSettingDialog<T> dialog = new AzureSettingDialog<>(service, getScene().getWindow());
            dialog.showAndWait();
            Optional<T> result = dialog.getResult();
            if (result.isPresent()) {
                value.set(result.get());
            } else {
                value.set(null);
            }
            updateUI();
        });
        updateUI();

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(5);
        getChildren().addAll(resourceName, button);
    }

    private void updateUI() {
        if (value.get() != null) {
            resourceName.setText(value.get().getName());
            button.setText("Change Resource");
        } else {
            resourceName.setText("Not Selected");
            button.setText("Select Resource");
        }
    }

    public T getValue() {
        return value.get();
    }

    public ReadOnlyObjectProperty<T> valueProperty() {
        return value.getReadOnlyProperty();
    }
}
