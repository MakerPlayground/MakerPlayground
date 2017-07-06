package io.makerplayground.ui.canvas;

import io.makerplayground.device.Action;
import io.makerplayground.helper.ControlType;
import io.makerplayground.helper.DataType;
import io.makerplayground.device.Parameter;
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
    private final SceneDeviceIconViewModel viewModel;
//    private final ConditionDeviceIconViewModel conditionViewModel;
    private VBox paramVBox;

    public DevicePropertyWindow(SceneDeviceIconViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

//    public DevicePropertyWindow(ConditionDeviceIconViewModel conditionViewModel) {
//        this.conditionViewModel = conditionViewModel;
//        initView();
//    }

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
                TextField textField = new TextField();

                // Set initial value and its constraint
                textField.setText(String.valueOf(viewModel.getParameterValue(p)));
//                slider.setMin(p.getConstraint().getMin());
//                slider.setMax(p.getConstraint().getMax());
                slider.setValue((Double) (viewModel.getParameterValue(p)));

                // Handle event
                slider.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        String valueString = String.format("%1$.2f", slider.getValue());
                        viewModel.setParameterValue(p, slider.getValue());
                        textField.setText(valueString);
                    }
                });

                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    Double value = Double.parseDouble(textField.getText());
                    viewModel.setParameterValue(p, value);
                    slider.setValue(value);
                });

                customRow.getChildren().addAll(slider, textField);
            }

            if (p.getControlType() == ControlType.TEXTBOX) {
                TextField textField = new TextField ();
                if (p.getDataType() == DataType.DOUBLE) {
                    textField.setText(String.valueOf(viewModel.getParameterValue(p)));
                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        viewModel.setParameterValue(p, Double.parseDouble(textField.getText()));
                    });
                }
                else if (p.getDataType() == DataType.STRING) {
                    textField.setText(String.valueOf(viewModel.getParameterValue(p)));
                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        viewModel.setParameterValue(p, textField.getText());
                    });
                }
                // else

                customRow.getChildren().add(textField);
            }

            if (p.getControlType() == ControlType.DROPDOWN) {
                // Casting ArrayList to ObservableList
//                ObservableList<String> list = FXCollections.observableArrayList(p.getConstraint().getValue());
//                ComboBox comboBox = new ComboBox(list);
//                comboBox.getSelectionModel().select(viewModel.getParameterValue(p));
//
//                comboBox.valueProperty().addListener(new ChangeListener() {
//                    @Override
//                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//                        // TODO: test with real data
//                        System.out.println("eiei");
//                    }
//                });
//
//                customRow.getChildren().add(comboBox);
            }

            if (p.getControlType() == ControlType.SPINBOX) {
                Spinner spinner = new Spinner();
                spinner.setEditable(true);

                // Value factory.
//                SpinnerValueFactory valueFactory = //
//                        new SpinnerValueFactory.DoubleSpinnerValueFactory(p.getConstraint().getMin(),
//                                p.getConstraint().getMax(), (Double) viewModel.getParameterValue(p));
//                spinner.setValueFactory(valueFactory);

                spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                    viewModel.setParameterValue(p, Double.parseDouble(spinner.getEditor().getText()));
                });

                customRow.getChildren().add(spinner);
            }

            if (p.getControlType() == ControlType.TIME) {
                Spinner spinnerMin = new Spinner();
                Spinner spinnerSec = new Spinner();
                spinnerMin.setEditable(true);
                spinnerSec.setEditable(true);

                // TODO: Must compatible with format from JSON
                // Value factory.
//                SpinnerValueFactory valueFactory = //
//                        new SpinnerValueFactory.DoubleSpinnerValueFactory(p.getConstraint().getMin(),
//                                p.getConstraint().getMax(), (Double) viewModel.getParameterValue(p));
//                spinnerMin.setValueFactory(valueFactory);
//                spinnerSec.setValueFactory(valueFactory);

                customRow.getChildren().addAll(spinnerMin, spinnerSec);
            }

//            if (p.getControlType() == ControlType.CUSTOMSEGMENT) {
//                TextField digit1 = new TextField();
//            }

            // TODO: Add more control type
            paramVBox.getChildren().add(customRow);
        }
    }
}
