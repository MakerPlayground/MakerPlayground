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

package io.makerplayground.ui.devicetab;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Connection;
import io.makerplayground.device.actual.DeviceType;
import io.makerplayground.device.actual.IntegratedActualDevice;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.generator.devicemapping.DeviceConnectionLogic;
import io.makerplayground.generator.devicemapping.DeviceConnectionResult;
import io.makerplayground.generator.devicemapping.DeviceConnectionResultStatus;
import io.makerplayground.project.DeviceConnection;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.version.DeviceLibraryVersion;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.SegmentedButton;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeviceExplorerView extends VBox {

    private final HostServices hostServices;

    private ScrollPane scrollPane;
    private final List<TitledPane> titledPanes = new ArrayList<>();
    private final List<DeviceInfoPane> deviceInfoPanes = new ArrayList<>();
    private final Map<DeviceInfoPane, TitledPane> deviceInfoPaneParentMap = new HashMap<>();
    private Consumer<ActualDevice> actualDeviceConsumer;
    private Project currentProject;
    private ActualDevice currentController;
    private String currentSearchKeyword = "";

    List<String> allBrands;
    List<GenericDevice> allGenericDevices;
    Map<String, List<ActualDevice>> actualDevicePerBrand;

    public DeviceExplorerView(Project project, HostServices hostServices) {
        this.currentProject = project;
        this.currentController = project.getSelectedController();
        this.hostServices = hostServices;

        TextField searchTextField = new TextField();
        searchTextField.setId("searchTextField");
        searchTextField.setPromptText("Search...");
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentSearchKeyword = newValue.toLowerCase();
            applyFilter();
            // auto scroll to the first matched category
            deviceInfoPanes.stream().filter(Node::isVisible).findFirst().ifPresent(this::ensureVisible);
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button collapseButton = new Button();
        BooleanProperty buttonState = new SimpleBooleanProperty(true);
        collapseButton.textProperty().bind(Bindings.when(buttonState).then("Collapse All").otherwise("Expand All"));
        collapseButton.setId("collapsedButton");
        collapseButton.setOnAction(event -> {
            if (buttonState.get()) {
                collapseAllPanes();
                buttonState.set(false);
            } else {
                expandAllPanes();
                buttonState.set(true);
            }
        });

        ToggleButton typeToggleButton = new ToggleButton("Type");
        ToggleButton brandToggleButton = new ToggleButton("Brand");
        brandToggleButton.setSelected(true);
        SegmentedButton segmentedButton = new SegmentedButton(typeToggleButton, brandToggleButton);
        segmentedButton.getToggleGroup().selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                oldValue.setSelected(true);
                return;
            }

            if (newValue == typeToggleButton) {
                buttonState.set(true);
                initViewDeviceType();
            } else if (newValue == brandToggleButton) {
                buttonState.set(true);
                initViewBrand();
            } else {
                throw new IllegalStateException();
            }
        });

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(8, 8, 8, 8));
        hbox.setSpacing(5);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getStyleClass().add("hbox");
        hbox.getChildren().addAll(searchTextField, spacer, collapseButton, new HBox(), segmentedButton);

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getStylesheets().add(getClass().getResource("/css/DeviceExplorer.css").toExternalForm());
        getChildren().addAll(hbox, scrollPane);

        allGenericDevices = new ArrayList<>(DeviceLibrary.INSTANCE.getGenericDevice());
        allGenericDevices.sort(Comparator.comparing(GenericDevice::getName));

        actualDevicePerBrand = DeviceLibrary.INSTANCE.getActualAndIntegratedDevice().stream()
                .filter(actualDevice -> !actualDevice.getDeviceType().equals(DeviceType.CONTROLLER))
                .collect(Collectors.groupingBy(ActualDevice::getBrand));

        allBrands = new ArrayList<>(actualDevicePerBrand.keySet());
        allBrands.sort(Comparator.naturalOrder());

        initViewBrand();
    }

    private void collapseAllPanes() {
        for (TitledPane pane: titledPanes) {
            pane.setExpanded(false);
        }
    }

    private void expandAllPanes() {
        for (TitledPane pane: titledPanes) {
            pane.setExpanded(true);
        }
    }

    private void initViewDeviceType() {
        initView(allGenericDevices
                , GenericDevice::getName
                , DeviceLibrary.INSTANCE::getActualAndIntegratedDevice
                , actualDevice -> actualDevice.getDeviceType() != DeviceType.CONTROLLER
        );
    }

    private void initViewBrand() {
        initView(allBrands
                , Function.identity()
                , actualDevicePerBrand::get
                , actualDevice -> actualDevice.getDeviceType() != DeviceType.CONTROLLER
        );
    }

    private <T> void initView(List<T> categories, Function<T, String> nameExtractor, Function<T, List<ActualDevice>> actualDevicesGetter, Predicate<ActualDevice> filter) {
        titledPanes.clear();
        deviceInfoPanes.clear();
        deviceInfoPaneParentMap.clear();

        VBox vBox = new VBox();
        vBox.setFillWidth(true);

        for (T t : categories) {
            List<ActualDevice> actualDevices = actualDevicesGetter.apply(t);
            FlowPane paneLayout = new FlowPane();
            paneLayout.setVgap(10);
            paneLayout.setHgap(10);
            TitledPane titledPane = new TitledPane(nameExtractor.apply(t), paneLayout);
            vBox.getChildren().add(titledPane);
            titledPanes.add(titledPane);
            actualDevices.stream().filter(filter).forEachOrdered((actualDevice) -> {
                DeviceInfoPane deviceInfoPane = new DeviceInfoPane(hostServices, actualDevice);
                paneLayout.getChildren().add(deviceInfoPane);
                deviceInfoPanes.add(deviceInfoPane);
                deviceInfoPaneParentMap.put(deviceInfoPane, titledPane);
                applyFilterEach(deviceInfoPane);
            });
            hideTitleIfNoneVisible(titledPane);
        }

        scrollPane.setContent(vBox);
    }

    public void setController(ActualDevice controller) {
        currentController = controller;
        applyFilter();
    }

    public void setOnAddButtonPressed(Consumer<ActualDevice> consumer) {
        this.actualDeviceConsumer = consumer;
    }

    private boolean isSupportByController(ActualDevice actualDevice) {
        if (!actualDevice.getPlatformSourceCodeLibrary().keySet().containsAll(currentController.getPlatformSourceCodeLibrary().keySet())) {
            return false;
        }

        if (actualDevice.getCloudConsume() != null && !currentProject.getProjectConfiguration().getCloudPlatformProvide().contains(actualDevice.getCloudConsume())) {
            return false;
        }

        Set<Connection> allConnectionProvide = new HashSet<>(currentController.getConnectionProvideByOwnerDevice(ProjectDevice.CONTROLLER));
        DeviceConnectionResult result = DeviceConnectionLogic.generatePossibleDeviceConnection(allConnectionProvide
                , Collections.emptyMap(), null, actualDevice, DeviceConnection.NOT_CONNECTED);
        if (result.getStatus() != DeviceConnectionResultStatus.OK) {
            return false;
        }
        return true;
    }

    private void applyFilterEach(DeviceInfoPane deviceInfoPane) {
        ActualDevice actualDevice = deviceInfoPane.getActualDevice();
        if ((deviceInfoPane.getActualDevice() instanceof IntegratedActualDevice
                && ((IntegratedActualDevice) deviceInfoPane.getActualDevice()).getParent() != currentController)) {
            deviceInfoPane.setVisible(false);
        } else {
            deviceInfoPane.setVisible(actualDevice.getBrand().toLowerCase().contains(currentSearchKeyword)
                || actualDevice.getModel().toLowerCase().contains(currentSearchKeyword)
                || actualDevice.getCompatibilityMap().keySet().stream().anyMatch(genericDevice -> genericDevice.getName().toLowerCase().contains(currentSearchKeyword))
                || deviceInfoPaneParentMap.get(deviceInfoPane).getText().toLowerCase().contains(currentSearchKeyword));
        }
        deviceInfoPane.setDisable((currentController == null) || !isSupportByController(deviceInfoPane.getActualDevice()));
    }

    private void applyFilter() {
        deviceInfoPanes.stream().forEach(this::applyFilterEach);
        titledPanes.forEach(this::hideTitleIfNoneVisible);
    }

    private void hideTitleIfNoneVisible(TitledPane pane) {
        boolean visible = ((FlowPane) pane.getContent()).getChildren().stream().anyMatch(Node::isVisible);
        pane.setVisible(visible);
        pane.setManaged(visible);
    }

    private void ensureVisible(DeviceInfoPane pane) {
        double height = scrollPane.getContent().getBoundsInLocal().getHeight();
        double y = deviceInfoPaneParentMap.get(pane).getBoundsInParent().getMinY();
        double vValue = y / height;
        scrollPane.setVvalue(Double.isNaN(vValue) ? 0 : vValue);
    }

    private class DeviceInfoPane extends AnchorPane {
        private final ActualDevice actualDevice;

        public DeviceInfoPane(HostServices hostServices, ActualDevice actualDevice) {
            this.actualDevice = actualDevice;

            DoubleBinding imageOpacity = Bindings.when(disabledProperty()).then(0.5).otherwise(1.0);

            StackPane imageViewWrapper = new StackPane();
            imageViewWrapper.setMinSize(80, 80);
            imageViewWrapper.setPrefSize(80, 80);
            imageViewWrapper.setMaxSize(80, 80);
            imageViewWrapper.opacityProperty().bind(imageOpacity);
            Path deviceImagePath = DeviceLibrary.getDeviceThumbnailPath(actualDevice);
            if (Files.exists(deviceImagePath)) {
                ImageView imageView = new ImageView(new Image(deviceImagePath.toUri().toString()));
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
                imageViewWrapper.getChildren().add(imageView);
            }
            AnchorPane.setTopAnchor(imageViewWrapper, 8.0);
            AnchorPane.setLeftAnchor(imageViewWrapper, 10.0);
            AnchorPane.setRightAnchor(imageViewWrapper, 10.0);

            Label nameLabel = new Label(actualDevice.getBrand() + " " + actualDevice.getModel());
            nameLabel.setMinSize(120, 48);
            nameLabel.setPrefSize(120, 48);
            nameLabel.setMaxSize(120, 48);
            nameLabel.setAlignment(Pos.CENTER);
            nameLabel.setTextAlignment(TextAlignment.CENTER);
            nameLabel.setWrapText(true);
            nameLabel.setStyle("-fx-font-size: 11px");
            AnchorPane.setBottomAnchor(nameLabel, 0.0);
            AnchorPane.setLeftAnchor(nameLabel, 10.0);
            AnchorPane.setRightAnchor(nameLabel, 10.0);

            Button addDeviceButton = new Button();
            addDeviceButton.setId("addDeviceBtn");
            addDeviceButton.setMinSize(18, 18);
            addDeviceButton.setPrefSize(18, 18);
            addDeviceButton.setMaxSize(18, 18);
            addDeviceButton.setOnAction(event -> {
                if (DeviceExplorerView.this.actualDeviceConsumer != null) {
                    DeviceExplorerView.this.actualDeviceConsumer.accept(actualDevice);
                }
            });

            ImageView urlButton = new ImageView(new Image(getClass().getResourceAsStream("/css/url.png")));
            urlButton.opacityProperty().bind(imageOpacity);
            urlButton.setFitWidth(18);
            urlButton.setFitHeight(18);
            urlButton.setOnMousePressed(event -> hostServices.showDocument(actualDevice.getUrl()));

            VBox buttonLayout = new VBox();
            buttonLayout.setSpacing(0);
            buttonLayout.getChildren().addAll(addDeviceButton, urlButton);
            AnchorPane.setTopAnchor(buttonLayout, 5.0);
            AnchorPane.setRightAnchor(buttonLayout, 5.0);

            setAlignment(Pos.TOP_CENTER);
            setMinSize(140, 140);
            setPrefSize(140, 140);
            setMaxSize(140, 140);
            styleProperty().bind(Bindings.when(disabledProperty())
                    .then("-fx-border-color: #dddddd;  -fx-border-radius: 10 10 10 10; ")
                    .otherwise("-fx-border-color: #cccccc;  -fx-border-radius: 10 10 10 10; ")
            );

            managedProperty().bind(visibleProperty());

            getChildren().addAll(imageViewWrapper, nameLabel, buttonLayout);

            getStylesheets().add(getClass().getResource("/css/DeviceInfoPane.css").toExternalForm());
        }

        public ActualDevice getActualDevice() {
            return actualDevice;
        }
    }

}