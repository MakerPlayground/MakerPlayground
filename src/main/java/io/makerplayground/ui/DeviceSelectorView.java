package io.makerplayground.ui;

import io.makerplayground.device.GenericDevice;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/7/2017 AD.
 */
public class DeviceSelectorView extends Popup {

    private final DeviceSelectorViewModel viewModel;

    public DeviceSelectorView(DeviceSelectorViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

    private void initView() {
        FlowPane inputPane = new FlowPane();
        for (Map.Entry<GenericDevice, SimpleIntegerProperty> entry : viewModel.getInputDeviceMap().entrySet()) {
            // TODO: copy from output pane
        }

        FlowPane outputPane = new FlowPane();
        for (Map.Entry<GenericDevice, SimpleIntegerProperty> entry : viewModel.getOutputDeviceMap().entrySet()) {
            // TODO: replace by custom control
            Image deviceImg = new Image(getClass().getResourceAsStream("/icons/" + entry.getKey().getName() + ".png"));
            ImageView deviceImgView = new ImageView(deviceImg);
            inputPane.getChildren().add(deviceImgView);

            SpinnerValueFactory<Integer> spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100);
            Spinner<Integer> spinner = new Spinner<>();
            spinner.setValueFactory(spinnerValueFactory);
            //spinnerValueFactory.valueProperty().bindBidirectional(new SimpleIntegerProperty().asObject());

            inputPane.getChildren().add(spinner);
        }

        // TODO: use internationalize string
        TitledPane outputTitledPane = new TitledPane("Output", outputPane);
        TitledPane inputTitledPane = new TitledPane("input", inputPane);

        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(inputTitledPane, outputTitledPane);

        Button importButton = new Button("Import");
        importButton.setOnAction(event -> {
            viewModel.importDeviceToProject();
        });

        VBox mainLayout = new VBox();
        mainLayout.getChildren().addAll(accordion, importButton);

        getContent().addAll(new Label("GenericDevice Selector"), mainLayout);
    }
}

//public class DeviceSelectorView extends Stage {
//    public DeviceSelectorView() {
//        initView();
//        initModality(Modality.APPLICATION_MODAL);
//    }
//
//    private void initView() {
//        FlowPane outputPane = new FlowPane();
//        FlowPane inputPane = new FlowPane();
//
//        // TODO: use internationalize string
//        TitledPane outputTitledPane = new TitledPane("Output", outputPane);
//        TitledPane inputTitledPane = new TitledPane("input", inputPane);
//
//        Accordion accordion = new Accordion();
//        accordion.getPanes().addAll(inputTitledPane, outputTitledPane);
//
//        Scene s = new Scene(accordion);
//        setScene(s);
//    }
//}
