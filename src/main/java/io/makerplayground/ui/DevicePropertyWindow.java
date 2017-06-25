package io.makerplayground.ui;

import io.makerplayground.device.Action;
import io.makerplayground.device.ControlType;
import io.makerplayground.device.Parameter;
import io.makerplayground.device.ParameterType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
            if (p.getControlType() == ControlType.SLIDER) {
                Slider slider = new Slider();
                slider.setMin(p.getConstraint().getMin());
                slider.setMax(p.getConstraint().getMax());
                slider.setValue((Double) (viewModel.getParameterValue(p)));
                customRow.getChildren().add(slider);

                slider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends Boolean> observableValue,
                            Boolean wasChanging,
                            Boolean changing) {
                        String valueString = String.format("%1$.3f", slider.getValue());

                        if (!changing) {
                            viewModel.setParameterValue(p, slider.getValue());
                        }
                    }
                });
            }
            if (p.getControlType() == ControlType.TEXTBOX) {
                TextField textField = new TextField ();
                if (p.getParameterType() == ParameterType.DOUBLE)
                    textField.setText(String.valueOf(viewModel.getParameterValue(p)));
                // else
                customRow.getChildren().add(textField);

                textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        viewModel.setParameterValue(p, Double.parseDouble(textField.getText()));
                    }
                });
            }
            if (p.getControlType() == ControlType.DROPDOWN) {
                // Casting ArrayList to ObservableList
                ObservableList<String> list = FXCollections.observableArrayList(p.getConstraint().getValue());
                ComboBox comboBox = new ComboBox(list);
                comboBox.getSelectionModel().select(viewModel.getParameterValue(p));
                customRow.getChildren().add(comboBox);

                comboBox.valueProperty().addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        // TODO: test with real data
                        System.out.println("eiei");
                    }
                });
            }
            if (p.getControlType() == ControlType.SPINBOX) {
                Spinner spinner = new Spinner();
                spinner.setEditable(true);

                // Value factory.
                SpinnerValueFactory valueFactory = //
                        new SpinnerValueFactory.DoubleSpinnerValueFactory(p.getConstraint().getMin(),
                                p.getConstraint().getMax(), (Double) viewModel.getParameterValue(p));

                spinner.setValueFactory(valueFactory);
                customRow.getChildren().add(spinner);

                spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
                   if (!newValue) {
                       viewModel.setParameterValue(p, Double.parseDouble(spinner.getEditor().getText()));
                   }
                });
            }
            if (p.getControlType() == ControlType.TIME) {
                Spinner spinnerMin = new Spinner();
                Spinner spinnerSec = new Spinner();
                spinnerMin.setEditable(true);
                spinnerSec.setEditable(true);

                customRow.getChildren().addAll(spinnerMin, spinnerSec);
            }

            // TODO: Add more control type
            paramVBox.getChildren().add(customRow);
        }
    }
}
