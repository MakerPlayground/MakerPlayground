package io.makerplayground.ui.explorer;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.DeviceType;
import io.makerplayground.device.generic.GenericDevice;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.controlsfx.control.SegmentedButton;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeviceExplorerPanel extends VBox {

    private static final String deviceDirectoryPath = DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";

    private final HostServices hostServices;

    private ScrollPane scrollPane;
    private final List<DeviceInfoPane> deviceInfoPanes = new ArrayList<>();
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

//        Label filterLabel = new Label("Filter");
//        filterLabel.setId("filterLabel");

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

        getStylesheets().add(getClass().getResource("/css/DeviceExplorer.css").toExternalForm());
        getChildren().add(hbox);

        initViewDeviceType();
    }

    private void initViewDeviceType() {
        List<GenericDevice> genericDevices = new ArrayList<>(DeviceLibrary.INSTANCE.getGenericDevice());
        genericDevices.sort(Comparator.comparing(GenericDevice::getName));

        initView(genericDevices, GenericDevice::getName, DeviceLibrary.INSTANCE::getActualDevice
                , actualDevice -> actualDevice.getDeviceType() == DeviceType.PERIPHERAL);
    }

    private void initViewBrand() {
        Map<String, List<ActualDevice>> actualDevicePerBrand = DeviceLibrary.INSTANCE.getActualDevice().stream()
                .collect(Collectors.groupingBy(ActualDevice::getBrand));

        List<String> brands = new ArrayList<>(actualDevicePerBrand.keySet());
        brands.sort(Comparator.naturalOrder());

        initView(brands, Function.identity(), actualDevicePerBrand::get
                , actualDevice -> actualDevice.getDeviceType() == DeviceType.PERIPHERAL);
    }

    private <T> void initView(List<T> categories, Function<T, String> nameExtractor, Function<T, List<ActualDevice>> actualDevicesGetter, Predicate<ActualDevice> filter) {
        VBox vBox = new VBox();
        vBox.setFillWidth(true);

        for (T t : categories) {
            List<ActualDevice> actualDevices = actualDevicesGetter.apply(t);

            FlowPane paneLayout = new FlowPane();
            paneLayout.setVgap(10);
            paneLayout.setHgap(10);
            for (ActualDevice actualDevice : actualDevices) {
                if (filter.test(actualDevice)) {
                    DeviceInfoPane deviceInfoPane = new DeviceInfoPane(hostServices, actualDevice);
                    paneLayout.getChildren().add(deviceInfoPane);
                    deviceInfoPanes.add(deviceInfoPane);
                }
            }
            applyFilterByController();
            applyFilterBySearchKeyword();

            if (!paneLayout.getChildren().isEmpty()) {
                TitledPane titledPane = new TitledPane(nameExtractor.apply(t), paneLayout);
                vBox.getChildren().add(titledPane);
            }
        }

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(vBox);

        getChildren().add(scrollPane);
    }

    public void setController(ActualDevice controller) {
        currentController = controller;
        applyFilterByController();
    }

    public void setOnAddButtonPressed(Consumer<ActualDevice> consumer) {
        this.actualDeviceConsumer = consumer;
    }

    private void applyFilterByController() {
        for (DeviceInfoPane deviceInfoPane : deviceInfoPanes) {
            if (currentController != null && deviceInfoPane.getActualDevice().isSupport(currentController)) {
                deviceInfoPane.setDisable(false);
            } else {
                deviceInfoPane.setDisable(true);
            }
        }
    }

    private void applyFilterBySearchKeyword() {
        for (DeviceInfoPane deviceInfoPane : deviceInfoPanes) {
            if (deviceInfoPane.getActualDevice().getBrand().toLowerCase().contains(currentSearchKeyword)
                    || deviceInfoPane.getActualDevice().getModel().toLowerCase().contains(currentSearchKeyword)) {
                deviceInfoPane.setVisible(true);
                deviceInfoPane.setManaged(true);
            } else {
                deviceInfoPane.setVisible(false);
                deviceInfoPane.setManaged(false);
            }
        }
    }

    private class DeviceInfoPane extends AnchorPane {
        private final ActualDevice actualDevice;

        public DeviceInfoPane(HostServices hostServices, ActualDevice actualDevice) {
            this.actualDevice = actualDevice;

            Path deviceImagePath = Path.of(deviceDirectoryPath,actualDevice.getId(), "asset", "device.png");
            ImageView imageView = new ImageView(new Image(deviceImagePath.toUri().toString()));
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);
            StackPane imageViewWrapper = new StackPane();
//            imageViewWrapper.setStyle("-fx-border-color: black");
            imageViewWrapper.setMinSize(80, 80);
            imageViewWrapper.setPrefSize(80, 80);
            imageViewWrapper.setMaxSize(80, 80);
            imageViewWrapper.getChildren().add(imageView);

            Label nameLabel = new Label(actualDevice.getModel());
            nameLabel.setMinHeight(40);
            nameLabel.setPrefHeight(40);
            nameLabel.setMaxHeight(40);
            nameLabel.setAlignment(Pos.TOP_CENTER);
            nameLabel.setWrapText(true);
//            nameLabel.setStyle("-fx-border-color: black");

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
