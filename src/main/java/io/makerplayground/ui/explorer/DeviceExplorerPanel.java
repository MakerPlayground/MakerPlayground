package io.makerplayground.ui.explorer;

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
import io.makerplayground.project.ProjectDevice;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.controlsfx.control.SegmentedButton;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeviceExplorerPanel extends VBox {

    private final HostServices hostServices;

    private ScrollPane scrollPane;
    private final List<TitledPane> titledPanes = new ArrayList<>();
    private final List<DeviceInfoPane> deviceInfoPanes = new ArrayList<>();
    private final Map<DeviceInfoPane, TitledPane> deviceInfoPaneParentMap = new HashMap<>();
    private Consumer<ActualDevice> actualDeviceConsumer;
    private ActualDevice currentController;
    private String currentSearchKeyword = "";

    public DeviceExplorerPanel(ActualDevice currentController, HostServices hostServices) {
        this.currentController = currentController;
        this.hostServices = hostServices;

        TextField searchTextField = new TextField();
        searchTextField.setId("searchTextField");
        searchTextField.setPromptText("Search...");
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentSearchKeyword = newValue.toLowerCase();
            applyFilterBySearchKeyword();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToggleButton typeToggleButton = new ToggleButton("Type");
        typeToggleButton.setSelected(true);
        ToggleButton brandToggleButton = new ToggleButton("Brand");
        SegmentedButton segmentedButton = new SegmentedButton(typeToggleButton, brandToggleButton);
        segmentedButton.getToggleGroup().selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                oldValue.setSelected(true);
                return;
            }

            getChildren().remove(scrollPane);

            if (newValue == typeToggleButton) {
                initViewDeviceType();
            } else if (newValue == brandToggleButton) {
                initViewBrand();
            } else {
                throw new IllegalStateException();
            }
        });

        HBox hbox = new HBox();
        hbox.setId("titlePane");
        hbox.setPadding(new Insets(8, 8, 8, 8));
        hbox.setSpacing(5);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().addAll(searchTextField, spacer/*, filterLabel*/, segmentedButton);

        setStyle("-fx-background-color: background-color");
        getStylesheets().add(getClass().getResource("/css/DeviceExplorer.css").toExternalForm());
        getChildren().add(hbox);

        initViewDeviceType();
    }

    private void initViewDeviceType() {
        List<GenericDevice> genericDevices = new ArrayList<>(DeviceLibrary.INSTANCE.getGenericDevice());
        genericDevices.sort(Comparator.comparing(GenericDevice::getName));

        initView(genericDevices, GenericDevice::getName, DeviceLibrary.INSTANCE::getActualAndIntegratedDevice
                , actualDevice -> actualDevice.getDeviceType() != DeviceType.CONTROLLER);
    }

    private void initViewBrand() {
        Map<String, List<ActualDevice>> actualDevicePerBrand = DeviceLibrary.INSTANCE.getActualAndIntegratedDevice().stream()
                .collect(Collectors.groupingBy(ActualDevice::getBrand));

        List<String> brands = new ArrayList<>(actualDevicePerBrand.keySet());
        brands.sort(Comparator.naturalOrder());

        initView(brands, Function.identity(), actualDevicePerBrand::get
                , actualDevice -> actualDevice.getDeviceType() != DeviceType.CONTROLLER);
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

            for (ActualDevice actualDevice : actualDevices) {
                if (filter.test(actualDevice)) {
                    DeviceInfoPane deviceInfoPane = new DeviceInfoPane(hostServices, actualDevice);
                    paneLayout.getChildren().add(deviceInfoPane);
                    deviceInfoPanes.add(deviceInfoPane);
                    deviceInfoPaneParentMap.put(deviceInfoPane, titledPane);
                }
            }
        }

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(vBox);

        getChildren().add(scrollPane);

        applyFilterByController();
        applyFilterBySearchKeyword();
    }

    public void setController(ActualDevice controller) {
        currentController = controller;
        applyFilterByController();
    }

    public void setOnAddButtonPressed(Consumer<ActualDevice> consumer) {
        this.actualDeviceConsumer = consumer;
    }

    private boolean isSupportByController(ActualDevice actualDevice) {
        Set<Connection> allConnectionProvide = new HashSet<>(currentController.getConnectionProvideByOwnerDevice(ProjectDevice.CONTROLLER));
        DeviceConnectionResult result = DeviceConnectionLogic.generatePossibleDeviceConnection(allConnectionProvide
                , Collections.emptyMap(), null, actualDevice, DeviceConnection.NOT_CONNECTED);
        return result.getStatus() == DeviceConnectionResultStatus.OK && actualDevice.getPlatformSourceCodeLibrary().keySet().containsAll(currentController.getPlatformSourceCodeLibrary().keySet());
    }

    private void applyFilterByController() {
        for (DeviceInfoPane deviceInfoPane : deviceInfoPanes) {
            if (currentController != null && isSupportByController(deviceInfoPane.getActualDevice())) {
                deviceInfoPane.setDisable(false);
            } else {
                deviceInfoPane.setDisable(true);
            }
        }

        applyIntegratedDeviceFilter();
        applyTitlePaneFilter();
    }

    private void applyFilterBySearchKeyword() {
        for (DeviceInfoPane deviceInfoPane : deviceInfoPanes) {
            if (deviceInfoPaneParentMap.get(deviceInfoPane).getText().toLowerCase().contains(currentSearchKeyword)
                    || deviceInfoPane.getActualDevice().getBrand().toLowerCase().contains(currentSearchKeyword)
                    || deviceInfoPane.getActualDevice().getModel().toLowerCase().contains(currentSearchKeyword)) {
                deviceInfoPane.setVisible(true);
                deviceInfoPane.setManaged(true);
            } else {
                deviceInfoPane.setVisible(false);
                deviceInfoPane.setManaged(false);
            }
        }

        applyIntegratedDeviceFilter();
        applyTitlePaneFilter();

        // auto scroll to the first matched category
        deviceInfoPanes.stream().filter(Node::isVisible).findFirst().ifPresent(this::ensureVisible);
    }

    private void ensureVisible(DeviceInfoPane pane) {
        double height = scrollPane.getContent().getBoundsInLocal().getHeight();
        double y = deviceInfoPaneParentMap.get(pane).getBoundsInParent().getMinY();
        double vValue = y / height;
        scrollPane.setVvalue(Double.isNaN(vValue) ? 0 : vValue);
    }

    private void applyIntegratedDeviceFilter() {
        if (currentController != null) {
            for (DeviceInfoPane deviceInfoPane : deviceInfoPanes) {
                if (deviceInfoPane.getActualDevice() instanceof IntegratedActualDevice) {
                    boolean isIntegratedDeviceOfCurrentController = ((IntegratedActualDevice) deviceInfoPane.getActualDevice()).getParent() == currentController;
                    deviceInfoPane.setDisable(!isIntegratedDeviceOfCurrentController);
                    deviceInfoPane.setVisible(isIntegratedDeviceOfCurrentController);
                    deviceInfoPane.setManaged(isIntegratedDeviceOfCurrentController);
                }
            }
        } else {
            for (DeviceInfoPane deviceInfoPane : deviceInfoPanes) {
                if (deviceInfoPane.getActualDevice() instanceof IntegratedActualDevice) {
                    deviceInfoPane.setDisable(true);
                    deviceInfoPane.setVisible(false);
                    deviceInfoPane.setManaged(false);
                }
            }
        }
    }

    private void applyTitlePaneFilter() {
        for (TitledPane pane : titledPanes) {
            if (((FlowPane) pane.getContent()).getChildren().stream().noneMatch(Node::isVisible)) {
                pane.setVisible(false);
                pane.setManaged(false);
            } else {
                pane.setVisible(true);
                pane.setManaged(true);
            }
        }
    }

    private class DeviceInfoPane extends AnchorPane {
        private final ActualDevice actualDevice;

        public DeviceInfoPane(HostServices hostServices, ActualDevice actualDevice) {
            this.actualDevice = actualDevice;

            Path deviceImagePath = DeviceLibrary.getDeviceThumbnailPath(actualDevice);
            StackPane imageViewWrapper = new StackPane();
            imageViewWrapper.setMinSize(80, 80);
            imageViewWrapper.setPrefSize(80, 80);
            imageViewWrapper.setMaxSize(80, 80);
            if (Files.exists(deviceImagePath)) {
                ImageView imageView = new ImageView(new Image(deviceImagePath.toUri().toString()));
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
                imageViewWrapper.getChildren().add(imageView);
            }

            Label nameLabel = new Label(actualDevice.getBrand() + " " + actualDevice.getModel());
            nameLabel.setMinHeight(40);
            nameLabel.setPrefHeight(40);
            nameLabel.setMaxHeight(40);
            nameLabel.setAlignment(Pos.TOP_CENTER);
            nameLabel.setWrapText(true);

            Hyperlink addToProject = new Hyperlink("add to project");
            addToProject.setStyle("-fx-padding: 0");
            addToProject.setOnAction(event -> {
                if (actualDeviceConsumer != null) {
                    actualDeviceConsumer.accept(actualDevice);
                }
            });

            Hyperlink url = new Hyperlink("More info");
            url.setStyle("-fx-padding: 0");
            url.setOnAction(event -> hostServices.showDocument(actualDevice.getUrl()));

            HBox tmp = new HBox();
            tmp.setAlignment(Pos.TOP_CENTER);
            tmp.getChildren().addAll(addToProject/*, url*/);    // TODO: fix

            VBox vBox = new VBox();
            vBox.setAlignment(Pos.TOP_CENTER);
            vBox.getChildren().addAll(imageViewWrapper, nameLabel, tmp);
            AnchorPane.setTopAnchor(vBox, 0.0);
            AnchorPane.setBottomAnchor(vBox, 0.0);
            AnchorPane.setLeftAnchor(vBox, 0.0);
            AnchorPane.setRightAnchor(vBox, 0.0);

            setAlignment(Pos.TOP_CENTER);
            setPadding(new Insets(10));
            setMinSize(160, 160);
            setPrefSize(160, 160);
            setMaxSize(160, 160);
            setStyle("-fx-border-color: #cccccc;  -fx-border-radius: 10 10 10 10; ");
            getChildren().add(vBox);
        }

        public ActualDevice getActualDevice() {
            return actualDevice;
        }
    }

}