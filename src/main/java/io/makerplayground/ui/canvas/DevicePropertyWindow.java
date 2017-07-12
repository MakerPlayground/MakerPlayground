package io.makerplayground.ui.canvas;

import io.makerplayground.device.Action;
import io.makerplayground.device.CategoricalConstraint;
import io.makerplayground.device.Value;
import io.makerplayground.helper.ControlType;
import io.makerplayground.device.Parameter;
import io.makerplayground.project.Expression;
import javafx.beans.InvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import javax.print.DocFlavor;
import java.util.Optional;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class DevicePropertyWindow extends PopOver {
    private final SceneDeviceIconViewModel viewModel;
//    private final ConditionDeviceIconViewModel conditionViewModel;
    //private VBox paramVBox;
    private PropertySheet propertySheet;

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
        vbox.getStylesheets().add(this.getClass().getResource("/css/DevicePropertyWindow.css").toExternalForm());
        HBox row1 = new HBox();


        Image img = new Image(getClass().getResourceAsStream("/icons/colorIcons/" + viewModel.getImageName() + ".png"));
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);

        Label customName = new Label(viewModel.getName());
        customName.setMaxWidth(Region.USE_COMPUTED_SIZE);
        row1.getChildren().addAll(imageView, customName);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.setSpacing(10);

        HBox row2 = new HBox();
        Label state = new Label("Scene");

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

        //paramVBox = new VBox();
        propertySheet = new PropertySheet();

        propertySheet.setModeSwitcherVisible(false);
        propertySheet.setSearchBoxVisible(false);
        propertySheet.setPropertyEditorFactory(new Callback<PropertySheet.Item, PropertyEditor<?>>() {
            @Override

            public PropertyEditor<?> call(PropertySheet.Item param) {
                if (param instanceof ParameterPropertyItem) {
                    Parameter p = ((ParameterPropertyItem) param).getParameter();
                    if (p.getControlType() == ControlType.SLIDER)
                        return new SliderPropertyEditor(param);
                    else if (p.getControlType() == ControlType.TEXTBOX)
                        return new TextBoxPropertyEditor(param);
                    else if (p.getControlType() == ControlType.DROPDOWN)
                        return new DropDownPropertyEditor(param);
                    else if (p.getControlType() == ControlType.SPINBOX)
                        return new SpinBoxPropertyEditor(param);
                    else
                        throw new IllegalStateException("Found unsupported type!!!");
                } else if (param instanceof ValuePropertyItem) {
                    return new ExpressionPropertyEditor(param);
                } else {
                    return null;
                }

            }
        });
        viewModel.actionProperty().addListener((observable, oldValue, newValue) -> {
            redrawProperty();
        });

        redrawProperty();

        row2.getChildren().addAll(state, comboBox);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.setSpacing(5.0);

        vbox.getChildren().addAll(row1, row2, propertySheet);
        vbox.setSpacing(5.0);
        vbox.setPadding(new Insets(20, 20, 20, 20));
        setContentNode(vbox);
    }

    private void redrawProperty() {
        //paramVBox.getChildren().clear();
        propertySheet.getItems().clear();

        for (Parameter p : viewModel.getAction().getParameter()) {
            propertySheet.getItems().add(new ParameterPropertyItem(p));
            //HBox customRow = new HBox();
            //Label name = new Label(p.getName());
            //customRow.getChildren().add(name);

//            if (p.getControlType() == ControlType.SLIDER) {
//                Slider slider = new Slider();
//                TextField textField = new TextField();
//
//                // Set initial value and its constraint
//                textField.setText(String.valueOf(viewModel.getParameterValue(p)));
////                slider.setMin(p.getConstraint().getMin());
////                slider.setMax(p.getConstraint().getMax());
//                slider.setValue((Double) (viewModel.getParameterValue(p)));
//
//                // Handle event
//                slider.valueProperty().addListener(new ChangeListener<Number>() {
//                    @Override
//                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                        String valueString = String.format("%1$.2f", slider.getValue());
//                        viewModel.setParameterValue(p, slider.getValue());
//                        textField.setText(valueString);
//                    }
//                });
//
//                textField.textProperty().addListener((observable, oldValue, newValue) -> {
//                    Double value = Double.parseDouble(textField.getText());
//                    viewModel.setParameterValue(p, value);
//                    slider.setValue(value);
//                });
//
//                PropertySheet.Item
//
//                customRow.getChildren().addAll(slider, textField);
//            }
//
//            if (p.getControlType() == ControlType.TEXTBOX) {
//                TextField textField = new TextField ();
//                if (p.getDataType() == DataType.DOUBLE) {
//                    textField.setText(String.valueOf(viewModel.getParameterValue(p)));
//                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
//                        viewModel.setParameterValue(p, Double.parseDouble(textField.getText()));
//                    });
//                }
//                else if (p.getDataType() == DataType.STRING) {
//                    textField.setText(String.valueOf(viewModel.getParameterValue(p)));
//                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
//                        viewModel.setParameterValue(p, textField.getText());
//                    });
//                }
//                // else
//
//                customRow.getChildren().add(textField);
//            }
//
//            if (p.getControlType() == ControlType.DROPDOWN) {
//                // Casting ArrayList to ObservableList
////                ObservableList<String> list = FXCollections.observableArrayList(p.getConstraint().getValue());
////                ComboBox comboBox = new ComboBox(list);
////                comboBox.getSelectionModel().select(viewModel.getParameterValue(p));
////
////                comboBox.valueProperty().addListener(new ChangeListener() {
////                    @Override
////                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
////                        // TODO: test with real data
////                        System.out.println("eiei");
////                    }
////                });
////
////                customRow.getChildren().add(comboBox);
//            }
//
//            if (p.getControlType() == ControlType.SPINBOX) {
//                Spinner spinner = new Spinner();
//                spinner.setEditable(true);
//
//                // Value factory.
////                SpinnerValueFactory valueFactory = //
////                        new SpinnerValueFactory.DoubleSpinnerValueFactory(p.getConstraint().getMin(),
////                                p.getConstraint().getMax(), (Double) viewModel.getParameterValue(p));
////                spinner.setValueFactory(valueFactory);
//
//                spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
//                    viewModel.setParameterValue(p, Double.parseDouble(spinner.getEditor().getText()));
//                });
//
//                customRow.getChildren().add(spinner);
//            }
//
//            if (p.getControlType() == ControlType.TIME) {
//                Spinner spinnerMin = new Spinner();
//                Spinner spinnerSec = new Spinner();
//                spinnerMin.setEditable(true);
//                spinnerSec.setEditable(true);
//
//                // TODO: Must compatible with format from JSON
//                // Value factory.
////                SpinnerValueFactory valueFactory = //
////                        new SpinnerValueFactory.DoubleSpinnerValueFactory(p.getConstraint().getMin(),
////                                p.getConstraint().getMax(), (Double) viewModel.getParameterValue(p));
////                spinnerMin.setValueFactory(valueFactory);
////                spinnerSec.setValueFactory(valueFactory);
//
//                customRow.getChildren().addAll(spinnerMin, spinnerSec);
//            }
//
////            if (p.getControlType() == ControlType.CUSTOMSEGMENT) {
////                TextField digit1 = new TextField();
////            }
//
//            // TODO: Add more control type
//            paramVBox.getChildren().add(customRow);
        }

        for (Value v : viewModel.getValue()) {
            propertySheet.getItems().add(new ValuePropertyItem(v));
//            HBox customRow = new HBox();
//            Label name = new Label(v.getName());
//            ObservableList<Expression> e = viewModel.getExpression(v);
//            ExpressionControl expression = new ExpressionControl(e, viewModel.getProjectValue());
//            customRow.getChildren().addAll(name, expression);
//            paramVBox.getChildren().add(customRow);
        }
    }

    public class ParameterPropertyItem implements PropertySheet.Item {
        private final Parameter parameter;

        public ParameterPropertyItem(Parameter parameter) {
            this.parameter = parameter;
        }

        @Override
        public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
            return Optional.empty();
        }

        @Override
        public boolean isEditable() {
            return true;
        }

        @Override
        public Class<?> getType() {
            return null;
        }

        @Override
        public String getCategory() {
            return "";
        }

        @Override
        public String getName() {
            return parameter.getName();
        }

        @Override
        public String getDescription() {
            return "Wait for description in JSON";
        }

        @Override
        public Object getValue() {
            return viewModel.getParameterValue(parameter);
        }

        @Override
        public void setValue(Object o) {
            viewModel.setParameterValue(parameter, o);
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return Optional.empty();
        }

        public Parameter getParameter() {
            return parameter;
        }
    }

    public class ValuePropertyItem implements PropertySheet.Item {
        private final Value v;
        private Object expressions;

        public ValuePropertyItem(Value v) {
            this.v = v;
        }

        @Override
        public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
            return Optional.empty();
        }

        @Override
        public boolean isEditable() {
            return true;
        }

        @Override
        public Class<?> getType() {
            return null;
        }

        @Override
        public String getCategory() {
            return "";
        }

        @Override
        public String getName() {
            return v.getName();
        }

        @Override
        public String getDescription() {
            return "wait for description in JSON";
        }

        @Override
        public Object getValue() {
            return viewModel.getExpression(v);
        }

        @Override
        public void setValue(Object o) {
            viewModel.setExpression(v, (ObservableList<Expression>) o);
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return Optional.empty();
        }

        public Value getV() { return v;}
    }

    public class SliderPropertyEditor extends AbstractPropertyEditor<Number, Slider> {
        public SliderPropertyEditor(PropertySheet.Item property, Slider control)
        {
            super(property, control);
        }

        public SliderPropertyEditor(PropertySheet.Item item)
        {
            this(item, new Slider());
        }

        @Override
        public void setValue(Number number) {
            this.getEditor().setValue(number.doubleValue());
        }

        @Override
        protected ObservableValue<Number> getObservableValue() {
            return this.getEditor().valueProperty();
        }
    }

    public class TextBoxPropertyEditor extends AbstractPropertyEditor<String, TextField> {
        public TextBoxPropertyEditor(PropertySheet.Item property, TextField control) {
            super(property, control);
        }

        public TextBoxPropertyEditor(PropertySheet.Item item) {
            this(item, new TextField());
        }

        @Override
        protected StringProperty getObservableValue() {
            return this.getEditor().textProperty();
        }

        @Override
        public void setValue(String s) {
            this.getEditor().setText(s);
        }
    }

    public class DropDownPropertyEditor extends AbstractPropertyEditor<String, ComboBox<String>> {
        public DropDownPropertyEditor(PropertySheet.Item property, ComboBox<String> control) {
            super(property, control);
            Parameter p = ((ParameterPropertyItem) property).getParameter();
            ObservableList<String> list = FXCollections.observableArrayList(((CategoricalConstraint) p.getConstraint()).getCategories());
            control.setItems(list);
        }

        public DropDownPropertyEditor(PropertySheet.Item property) {
            this(property, new ComboBox<String>());
        }

        @Override
        protected ObservableValue<String> getObservableValue() {
            return this.getEditor().valueProperty();
        }

        @Override
        public void setValue(String s) {
            this.getEditor().setValue(s);
        }
    }

    public class SpinBoxPropertyEditor extends AbstractPropertyEditor<Integer, Spinner<Integer>> {
        public SpinBoxPropertyEditor(PropertySheet.Item property, Spinner<Integer> control) {
            super(property, control);
        }

        public SpinBoxPropertyEditor(PropertySheet.Item property) {
            this(property, new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100))); // TODO: fake data
        }

        @Override
        protected ObservableValue<Integer> getObservableValue() {
            return this.getEditor().getValueFactory().valueProperty();
        }

        @Override
        public void setValue(Integer integer) {
            this.getEditor().getValueFactory().setValue(integer);
        }
    }

    public class ExpressionPropertyEditor extends AbstractPropertyEditor<ObservableList<Expression>, ExpressionControl> {
        public ExpressionPropertyEditor(PropertySheet.Item property, ExpressionControl control) {
            super(property, control);
        }

        public ExpressionPropertyEditor(PropertySheet.Item property) {
            this(property, new ExpressionControl(viewModel.getProjectValue()));
        }

        @Override
        protected ObservableValue<ObservableList<Expression>> getObservableValue() {
            return this.getEditor().expressionsListProperty();
        }

        @Override
        public void setValue(ObservableList<Expression> expression) {
            this.getEditor().setExpressionsList(expression);
        }
    }
//
//    public class ExpressionPropertyEditor extends PropertyEditor<ObservableList<Expression>, ExpressionControl> {
//        public ExpressionPropertyEditor(PropertySheet.Item property, ExpressionControl control) {
//            super(property, control);
//        }
//
//        public ExpressionPropertyEditor(PropertySheet.Item property) {
//            this(property, new ExpressionControl(viewModel.getProjectValue()));
//        }
//
//        @Override
//        protected ObservableValue<ObservableList<Expression>> getObservableValue() {
//            return this.getEditor().expressionsListProperty();
//        }
//
//        @Override
//        public void setValue(ObservableList<Expression> expression) {
//            this.getEditor().setExpressionsList(expression);
//        }
//    }

}
