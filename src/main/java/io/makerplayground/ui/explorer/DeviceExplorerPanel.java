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
import io.makerplayground.generator.devicemapping.DeviceMappingResult;
import io.makerplayground.project.DeviceConnection;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
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
    private Project currentProject;
    private ActualDevice currentController;
    private String currentSearchKeyword = "";

    public DeviceExplorerPanel(Project project, HostServices hostServices) {
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

        applyFilter();
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

    private void applyFilter() {
        for (DeviceInfoPane deviceInfoPane : deviceInfoPanes) {
            deviceInfoPane.setDisable(true);
            deviceInfoPane.setVisible(false);
            deviceInfoPane.setManaged(false);
        }

        // enable device that is supported by the current controller
        for (DeviceInfoPane deviceInfoPane : deviceInfoPanes) {
            if (currentController != null && isSupportByController(deviceInfoPane.getActualDevice())) {
                deviceInfoPane.setDisable(false);
            }
        }

        // show device based on the search keyword
        for (DeviceInfoPane deviceInfoPane : deviceInfoPanes) {
            if (deviceInfoPaneParentMap.get(deviceInfoPane).getText().toLowerCase().contains(currentSearchKeyword)
                    || deviceInfoPane.getActualDevice().getBrand().toLowerCase().contains(currentSearchKeyword)
                    || deviceInfoPane.getActualDevice().getModel().toLowerCase().contains(currentSearchKeyword)) {
                deviceInfoPane.setVisible(true);
                deviceInfoPane.setManaged(true);
            }
        }

        // hide integrated device of the other controllers
        for (DeviceInfoPane deviceInfoPane : deviceInfoPanes) {
            if (deviceInfoPane.getActualDevice() instanceof IntegratedActualDevice) {
                if (((IntegratedActualDevice) deviceInfoPane.getActualDevice()).getParent() != currentController) {
                    deviceInfoPane.setDisable(true);
                    deviceInfoPane.setVisible(false);
                    deviceInfoPane.setManaged(false);
                }
            }
        }

        // hide category that doesn't have any devices left after filter
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

            ImageView addDeviceButton = new ImageView(new Image(getClass().getResourceAsStream("/css/add-project-device.png")));
            addDeviceButton.opacityProperty().bind(imageOpacity);
            addDeviceButton.setFitWidth(18);
            addDeviceButton.setFitHeight(18);
            addDeviceButton.setPickOnBounds(true);
            addDeviceButton.setOnMousePressed(event -> {
                if (actualDeviceConsumer != null) {
                    actualDeviceConsumer.accept(actualDevice);
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
            setStyle("-fx-border-color: #cccccc;  -fx-border-radius: 10 10 10 10; ");
            getChildren().addAll(imageViewWrapper, nameLabel, buttonLayout);
        }

        public ActualDevice getActualDevice() {
            return actualDevice;
        }
    }

}