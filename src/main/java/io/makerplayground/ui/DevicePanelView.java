package io.makerplayground.ui;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import io.makerplayground.uihelper.ViewFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class DevicePanelView extends VBox {

    private final DevicePanelViewModel viewModel;

    @FXML private VBox devicePanel;
    @FXML private FlowPane inputPane;
    @FXML private FlowPane outputPane;
    @FXML private FlowPane microcontrollerPane;

    private final ViewFactory<DevicePanelIconViewModel, DevicePanelIcon> viewFactory = new ViewFactory<DevicePanelIconViewModel, DevicePanelIcon>() {
        @Override
        public DevicePanelIcon newInstance(DevicePanelIconViewModel devicePanelIconViewModel) {
            DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
            icon.setOnAction(event -> viewModel.removeDevice(devicePanelIconViewModel));
            return icon;
        }
    };
    private final NodeConsumer<FlowPane, DevicePanelIcon> nodeConsumer = new NodeConsumer<FlowPane, DevicePanelIcon>() {
        @Override
        public void addNode(FlowPane parent, DevicePanelIcon node) {
            parent.getChildren().add(node);
        }

        @Override
        public void removeNode(FlowPane parent, DevicePanelIcon node) {
            parent.getChildren().remove(node);
        }
    };

    public DevicePanelView(DevicePanelViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DevicePanelView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> inputViewCreator =
              new DynamicViewCreator<>(viewModel.getInputChildViewModel(), inputPane, viewFactory, nodeConsumer);
        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> outputViewCreator =
              new DynamicViewCreator<>(viewModel.getOutputChildViewModel(), outputPane, viewFactory, nodeConsumer);
    }
//
//    private void initView() {
//        ResourceBundle labels = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault());
//
//        FlowPane mcuPane = new FlowPane();
//        // TODO: add code for mcu
//
//        FlowPane inputPane = new FlowPane();
//        inputPane.setHgap(5);
//        inputPane.setVgap(5);
//        Button newInputDeviceButton = new Button("+");
//        newInputDeviceButton.setPrefSize(50.0,50.0);
//        inputPane.setRowValignment(TOP);
//        newInputDeviceButton.setOnAction(event -> {
//            // TODO: copy from output pane
//        });
//        inputPane.getChildren().add(newInputDeviceButton);
//        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> inputViewCreator =
//                new DynamicViewCreator<>(viewModel.getInputChildViewModel(), inputPane, viewFactory, nodeConsumer);
//
//        FlowPane outputPane = new FlowPane();
//        outputPane.setHgap(5);
//        outputPane.setVgap(5);
//        Button newOutputDeviceButton = new Button("+");
//        newOutputDeviceButton.setPrefSize(50.0,50.0);
//        outputPane.setRowValignment(TOP);
//        newOutputDeviceButton.setOnAction(event -> {
//            // TODO: invoke device selector
//            //DeviceSelectorView  selectorView = new DeviceSelectorView();
//            //selectorView.show(getScene().getWindow());
//        });
//        outputPane.getChildren().add(newOutputDeviceButton);
//        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> outputViewCreator =
//                new DynamicViewCreator<>(viewModel.getOutputChildViewModel(), outputPane, viewFactory, nodeConsumer);
//
//        TitledPane t1 = new TitledPane(labels.getString("Microcontroller"), mcuPane);
//        TitledPane t2 = new TitledPane(labels.getString("Output Device"), outputPane);
//        TitledPane t3 = new TitledPane(labels.getString("Input Device"), inputPane);
//
//        t1.setExpanded(false);
//        t2.setExpanded(false);
//        t3.setExpanded(false);
//        getChildren().addAll(t1, t2, t3);
//
//        setMinWidth(200);
//    }

}
