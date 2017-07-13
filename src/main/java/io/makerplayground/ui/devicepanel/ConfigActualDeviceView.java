package io.makerplayground.ui.devicepanel;


import io.makerplayground.device.Device;
import io.makerplayground.project.ProjectDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;


/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceView extends Dialog {
    private final ConfigActualDeviceViewModel viewModel;

    public ConfigActualDeviceView(ConfigActualDeviceViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

    private void initView() {
        Dialog dialog = new Dialog();
        ScrollPane scrollPane = new ScrollPane();
        VBox allDevice = new VBox();
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        for (ProjectDevice projectDevice : viewModel.getAllDevice()) {
            VBox row = new VBox();
            HBox entireDevice = new HBox();
            VBox devicePic = new VBox();
            Image image = new Image(getClass().getResourceAsStream("/icons/colorIcons/"
                                    + projectDevice.getGenericDevice().getName() + ".png"));
            ImageView imageView = new ImageView(image);
            Label name = new Label();
            name.setText(projectDevice.getName());
            devicePic.getChildren().addAll(imageView, name);

            ObservableList<Device> oDeviceList = FXCollections.observableArrayList(viewModel.getCompatibleDevice(projectDevice));
            ComboBox<Device> comboBox = new ComboBox<>(oDeviceList);
            comboBox.setCellFactory(new Callback<ListView<Device>, ListCell<Device>>() {
                @Override
                public ListCell<Device> call(ListView<Device> param) {
                    return new ListCell<Device>() {
                        @Override
                        protected void updateItem(Device item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setText("");
                            } else {
                                setText(item.getBrand() + " " + item.getModel());
                            }
                        }
                    };
                }
            });
            comboBox.setButtonCell(new ListCell<Device>(){
                @Override
                protected void updateItem(Device item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.getBrand() + " " + item.getModel());
                    }
                }
            });
            if (projectDevice.getActualDevice() == null) {
                comboBox.getSelectionModel().selectFirst();
            } else {
                comboBox.getSelectionModel().select(projectDevice.getActualDevice());
            }
            comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setDevice(projectDevice, newValue));

            entireDevice.getChildren().addAll(devicePic, comboBox);
            row.getChildren().add(entireDevice);
            allDevice.getChildren().add(row);
        }

        scrollPane.setContent(allDevice);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }
}
