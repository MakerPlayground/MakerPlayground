package io.makerplayground.ui;

import io.makerplayground.device.Action;
import io.makerplayground.device.ControlType;
import io.makerplayground.device.Parameter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class DevicePropertyWindow extends PopOver {
    private final StateDeviceIconViewModel viewModel;

    private VBox paramVBox;

    public DevicePropertyWindow(StateDeviceIconViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

    private void initView() {
        VBox vbox = new VBox();

        HBox row1 = new HBox();
        row1.setSpacing(10);

        Image img = new Image(getClass().getResourceAsStream("/icons/" + viewModel.getImageName() + ".png"));
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);

        Label customName = new Label(viewModel.getName());
        row1.getChildren().addAll(imageView, customName);

        HBox row2 = new HBox();
        Label state = new Label("State");

        // Casting ArrayList to ObservableList
        ObservableList<Action> actionList = FXCollections.observableArrayList(viewModel.getGenericDevice().getAction());
//        ObservableList<String> options = FXCollections.<String>observableArrayList();
//        for (Action a : actionList) {
//            options.add(a.getName());
//        }

        ComboBox<Action> comboBox = new ComboBox<>(actionList);
        comboBox.setCellFactory(new Callback<ListView<Action>, ListCell<Action>>() {
            @Override
            public ListCell<Action> call(ListView<Action> param) {
                return new ListCell<Action>() {
                    @Override
                    protected void updateItem(Action item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });
        comboBox.setButtonCell(new ListCell<Action>(){
            @Override
            protected void updateItem(Action item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });
        comboBox.getSelectionModel().select(viewModel.getAction());
        viewModel.actionProperty().bind(comboBox.getSelectionModel().selectedItemProperty());

        //ComboBox comboBox = new ComboBox(options);
        //comboBox.getSelectionModel().select(viewModel.getUserSetting().getAction().getName());

        paramVBox = new VBox();
        viewModel.actionProperty().addListener((observable, oldValue, newValue) -> {
            redrawProperty();
        });

        redrawProperty();

        row2.getChildren().addAll(state, comboBox);
        vbox.getChildren().addAll(row1, row2, paramVBox);

        vbox.setPadding(new Insets(20, 20, 20, 20));
        setContentNode(vbox);
    }

    private void redrawProperty() {
        paramVBox.getChildren().clear();

        for (Parameter p : viewModel.getAction().getParameter()) {
            HBox customRow = new HBox();
            Label name = new Label(p.getName());
            customRow.getChildren().add(name);
            if (p.getControlType() == ControlType.NUMERIC_SLIDER) {
                Slider slider = new Slider();
                slider.setMin(p.getConstraint().getMin());
                slider.setMax(p.getConstraint().getMax());
                slider.setValue((Integer) (p.getDefaultValue()));
                customRow.getChildren().add(slider);
            }
            // TODO: Add more control type
            paramVBox.getChildren().add(customRow);
        }
    }
}
