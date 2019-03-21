package io.makerplayground.ui.devtool;

import io.makerplayground.device.actual.DevicePortSubType;
import io.makerplayground.device.actual.DevicePortType;
import io.makerplayground.device.actual.DeviceType;
import io.makerplayground.device.actual.FormFactor;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceJsonEditorView extends BorderPane {

    @FXML Button saveButton;
    @FXML Button discardButton;
    @FXML Label statusLabel;

    @FXML TextField deviceIDTextField;
    @FXML TextField brandTextField;
    @FXML TextField modelTextField;
    @FXML TextField urlTextField;
    @FXML ComboBox<DeviceType> deviceTypeCombo;
    @FXML ComboBox<FormFactor> formFactorCombo;
    @FXML Button pngImageButton;
    @FXML Button pngImageDeleteButton;
    @FXML Label pngImageLabel;
    @FXML Button svgImageButton;
    @FXML Button svgImageDeleteButton;
    @FXML Label svgImageLabel;
    @FXML Button newPortButton;
    @FXML VBox connectivityVBox;
    @FXML VBox compatibilityVBox;
    @FXML VBox platformVBox;
    @FXML VBox cloudPlatformVBox;

    @FXML VBox rightColumn;
    @FXML ImageView pngImageView;
    @FXML Label portLabel;
    @FXML VBox portVBox;

    DeviceJsonEditorViewModel viewModel;

    public DeviceJsonEditorView(DeviceJsonEditorViewModel deviceJsonEditorViewModel) {

        this.viewModel = deviceJsonEditorViewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DeviceJsonEditorView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initComponents();
        initEvents();
    }

    private void initComponents() {
        deviceIDTextField.setText(viewModel.getDeviceId());
        brandTextField.setText(viewModel.getBrand());
        modelTextField.setText(viewModel.getModel());
        urlTextField.setText(viewModel.getUrl());
        deviceTypeCombo.setItems(FXCollections.observableArrayList(DeviceType.values()));
        deviceTypeCombo.getSelectionModel().select(viewModel.getDeviceType());
        formFactorCombo.setItems(FXCollections.observableArrayList(FormFactor.values()));
        formFactorCombo.getSelectionModel().select(viewModel.getFormFactor());
        pngImageView.setImage(viewModel.getPngImage());
        pngImageView.setPickOnBounds(true);
        pngImageView.setOnMouseClicked(event -> {
            Node portView = createPortView(
                    new DeviceJsonEditorViewModel.Port("", null, null
                            , FXCollections.observableArrayList(), 0.0, 0.0
                            , event.getX(), event.getY(), 0.0));
            portVBox.getChildren().addAll(portView);
        });
        if (viewModel.getPngImageFile().exists()) {
            pngImageLabel.setText(viewModel.getPngImageFile().getName() + " (" + FileUtils.byteCountToDisplaySize(viewModel.getPngImageFile().length()) + ")");
        }
        if (viewModel.getSvgImageFile().exists()) {
            svgImageLabel.setText(viewModel.getSvgImageFile().getName() + " (" + FileUtils.byteCountToDisplaySize(viewModel.getSvgImageFile().length()) + ")");
        }
    }

    private void initEvents() {
        deviceIDTextField.addEventFilter(KeyEvent.ANY, keyEvent -> viewModel.setDeviceId(deviceIDTextField.getText()));
        brandTextField.addEventFilter(KeyEvent.ANY, keyEvent -> viewModel.setBrand(brandTextField.getText()));
        modelTextField.addEventFilter(KeyEvent.ANY, keyEvent -> viewModel.setModel(modelTextField.getText()));
        urlTextField.addEventFilter(KeyEvent.ANY, keyEvent -> viewModel.setModel(urlTextField.getText()));
        deviceTypeCombo.getSelectionModel().selectedItemProperty().addListener((observableValue, deviceType, t1) -> viewModel.setDeviceType(t1));
        formFactorCombo.getSelectionModel().selectedItemProperty().addListener((observableValue, deviceType, t1) -> viewModel.setFormFactor(t1));

        saveButton.setOnAction(actionEvent -> {
            viewModel.save();
            this.showDisappearingStatusMessage("Saved!");
        });

        discardButton.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("You are going to remove all changes you have made for this device.");
            alert.setContentText("Continue?");
            if (ButtonType.OK == alert.showAndWait().orElse(ButtonType.CANCEL)){
                viewModel.discardSave();
                initComponents();
                this.showDisappearingStatusMessage("Discarded");
            }
        });

        final FileChooser fileChooser = new FileChooser();

        pngImageButton.setOnAction(actionEvent -> {
            File file = fileChooser.showOpenDialog(this.getScene().getWindow());
            if (file != null) {
                viewModel.saveDevicePng(file);
                pngImageView.setImage(new Image(file.toURI().toString()));
                pngImageLabel.setText(file.getName() + " (" + FileUtils.byteCountToDisplaySize(file.length()) + ")");
                pngImageView.setPickOnBounds(true);
                pngImageView.setOnMouseClicked(event -> {
                    Node portView = createPortView(
                            new DeviceJsonEditorViewModel.Port("", null, null
                                    , FXCollections.observableArrayList(), 0.0, 0.0
                                    , event.getX(), event.getY(), 0.0));
                    portVBox.getChildren().addAll(portView);
                });
            }
        });

        pngImageDeleteButton.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Deleting the device.png file from the directory");
            alert.setContentText("Continue?");
            if (ButtonType.OK == alert.showAndWait().orElse(ButtonType.CANCEL)){
                viewModel.deleteDevicePng();
                pngImageView.setImage(null);
                pngImageLabel.setText("");
            }
        });

        pngImageDeleteButton.visibleProperty().bind(pngImageView.imageProperty().isNotNull());
        rightColumn.visibleProperty().bind(pngImageView.imageProperty().isNotNull());

        svgImageButton.setOnAction(actionEvent -> {
            File file = fileChooser.showOpenDialog(this.getScene().getWindow());
            if (file != null) {
                viewModel.saveDeviceSvg(file);
                svgImageLabel.setText(file.getName() + " (" + FileUtils.byteCountToDisplaySize(file.length()) + ")");
            }
        });

        svgImageDeleteButton.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Deleting the device.svg file from the directory");
            alert.setContentText("Continue?");
            if (ButtonType.OK == alert.showAndWait().orElse(ButtonType.CANCEL)){
                viewModel.deleteDeviceSvg();
                svgImageLabel.setText("");
            }
        });

        svgImageDeleteButton.visibleProperty().bind(svgImageLabel.textProperty().isNotEmpty());

        portLabel.visibleProperty().bind(new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return viewModel.getPort().size() > 0;
            }
        });
    }

    private Node createPortView(DeviceJsonEditorViewModel.Port port) {
        Label nameLabel = new Label("name");
        TextField nameText = new TextField(port.name);
        nameText.setPrefWidth(100);

        HBox hBox1 = new HBox(5, nameLabel, nameText);
        hBox1.setAlignment(Pos.CENTER_LEFT);

        Label xLabel = new Label("x");
        TextField xText = new TextField(Double.toString(port.x));
        xText.setPrefWidth(50);

        Label yLabel = new Label("y");
        TextField yText = new TextField(Double.toString(port.y));
        yText.setPrefWidth(50);

        Label vMinLabel = new Label("v_min");
        TextField vMinText = new TextField(Double.toString(port.v_min));
        vMinText.setPrefWidth(50);

        Label vMaxLabel = new Label("v_max");
        TextField vMaxText = new TextField(Double.toString(port.v_max));
        vMaxText.setPrefWidth(50);

        HBox hBox2 = new HBox(5, xLabel, xText, yLabel, yText, vMinLabel, vMinText, vMaxLabel, vMaxText);
        hBox2.setAlignment(Pos.CENTER_LEFT);

        Label typeLabel = new Label("type");

        DevicePortType[] portType = DevicePortType.values();
        Arrays.sort(portType, Comparator.comparing(Enum::toString));
        ComboBox<DevicePortType> typeComboBox = new ComboBox<>(FXCollections.observableArrayList(portType));
        typeComboBox.getItems().add(0, null);
        typeComboBox.getSelectionModel().select(port.type);

        Label subTypeLabel = new Label("sub_type");
        ComboBox<DevicePortSubType> subTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(DevicePortSubType.values()));
        subTypeComboBox.getItems().add(0, null);
        subTypeComboBox.getSelectionModel().select(port.sub_type);

        HBox hBox3 = new HBox(5, typeLabel, typeComboBox, subTypeLabel, subTypeComboBox);
        hBox3.setAlignment(Pos.CENTER_LEFT);

        return new VBox(5, hBox1, hBox2, hBox3);
    }

    private void showDisappearingStatusMessage(String text) {
        statusLabel.setText(text);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> statusLabel.setText(""));
            }
        }, 2500);
    }
}
