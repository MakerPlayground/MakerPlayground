package io.makerplayground.ui.canvas;

import io.makerplayground.device.*;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.SimpleExpression;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.List;
import java.util.Optional;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class ConditionDevicePropertyWindow extends PopOver {
    private final SceneDeviceIconViewModel viewModel;

    // Create a grid pane as a main layout for this property sheet
    private final GridPane propertyPane = new GridPane();

    private Label conditionLabel;
    private ComboBox<Action> conditionComboBox;

    public ConditionDevicePropertyWindow(SceneDeviceIconViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

    private void initView() {
        // Create title layout
        Image img = new Image(getClass().getResourceAsStream("/icons/colorIcons/" + viewModel.getImageName() + ".png"));
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);

        Label customName = new Label(viewModel.getName());
        customName.setMaxWidth(Region.USE_COMPUTED_SIZE);

        HBox titleHBox = new HBox();
        titleHBox.getChildren().addAll(imageView, customName);
        titleHBox.setAlignment(Pos.CENTER_LEFT);
        titleHBox.setSpacing(10);

        // Create ComboBox for user to select a condition
        conditionLabel = new Label("Condition");
        GridPane.setRowIndex(conditionLabel, 0);
        GridPane.setColumnIndex(conditionLabel, 0);

        conditionComboBox = new ComboBox<>(FXCollections.observableArrayList(viewModel.getGenericDevice().getAction()));
        conditionComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Action> call(ListView<Action> param) {
                return new ListCell<>() {
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
        conditionComboBox.setButtonCell(new ListCell<>(){
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
        conditionComboBox.getSelectionModel().select(viewModel.getAction());
        // bind action selected to the view model
        viewModel.actionProperty().bind(conditionComboBox.getSelectionModel().selectedItemProperty());
        GridPane.setRowIndex(conditionComboBox, 0);
        GridPane.setColumnIndex(conditionComboBox, 1);

        propertyPane.setVgap(5);
        propertyPane.getChildren().addAll(conditionLabel, conditionComboBox);

        // add listener to update property sheet when the condition selected has changed
        viewModel.actionProperty().addListener((observable, oldValue, newValue) -> redrawProperty());
        redrawProperty();

        // arrange title and property sheet
        VBox mainPane = new VBox();
        mainPane.getStylesheets().add(this.getClass().getResource("/css/DevicePropertyWindow.css").toExternalForm());
        mainPane.getChildren().addAll(titleHBox, propertyPane);
        mainPane.setSpacing(5.0);
        mainPane.setPadding(new Insets(20, 20, 20, 20));

        setDetachable(false);
        setContentNode(mainPane);
    }

    private void redrawProperty() {
        propertyPane.getChildren().clear();
        propertyPane.getChildren().addAll(conditionLabel, conditionComboBox);

        List<Parameter> params = viewModel.getAction().getParameter();
        for (int i=0; i<params.size(); i++) {
            Parameter p = params.get(i);

            Label name = new Label(p.getName());
            GridPane.setRowIndex(name, i+1);
            GridPane.setColumnIndex(name, 0);

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

        if (viewModel.getAction().getName().equals("Compare")) {    // TODO: compare with condition name may be dangerous
            List<Value> values = viewModel.getValue();
            for (int i=0; i<values.size(); i++) {
                Value value = values.get(i);
                createExpressionControl(i, value);
            }
        }
    }

    private void createExpressionControl(int i, Value value) {
        Expression expression = viewModel.getExpression(value);

        CheckBox enableCheckbox = new CheckBox(value.getName());
        enableCheckbox.selectedProperty().bindBidirectional(expression.enableProperty());
        GridPane.setRowIndex(enableCheckbox, i+1);
        GridPane.setColumnIndex(enableCheckbox, 0);
        propertyPane.getChildren().add(enableCheckbox);

        if (expression instanceof SimpleExpression) {
            SimpleExpressionControl expressionControl = new SimpleExpressionControl((SimpleExpression) expression, value);
            GridPane.setRowIndex(expressionControl, i+1);
            GridPane.setColumnIndex(expressionControl, 1);
            propertyPane.getChildren().add(expressionControl);
        } else {
            throw new IllegalStateException("");    // TODO: add support soon
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

//    public class ValuePropertyItem implements PropertySheet.Item {
//        private final Value v;
//        private Object expressions;
//
//        public ValuePropertyItem(Value v) {
//            this.v = v;
//        }
//
//        @Override
//        public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
//            return Optional.empty();
//        }
//
//        @Override
//        public boolean isEditable() {
//            return true;
//        }
//
//        @Override
//        public Class<?> getType() {
//            return null;
//        }
//
//        @Override
//        public String getCategory() {
//            return "";
//        }
//
//        @Override
//        public String getName() {
//            return v.getName();
//        }
//
//        @Override
//        public String getDescription() {
//            return "wait for description in JSON";
//        }
//
//        @Override
//        public Object getValue() {
//            return viewModel.getExpression(v);
//        }
//
//        @Override
//        public void setValue(Object o) {
//            viewModel.setExpression(v, (ObservableList<Expression>) o);
//        }
//
//        @Override
//        public Optional<ObservableValue<? extends Object>> getObservableValue() {
//            return Optional.empty();
//        }
//
//        public Value getV() { return v;}
//    }

    public class SliderPropertyEditor extends AbstractPropertyEditor<NumberWithUnit, SliderWithUnit> {
        public SliderPropertyEditor(ParameterPropertyItem property, SliderWithUnit control)
        {
            super(property, control);
        }

        public SliderPropertyEditor(ParameterPropertyItem item)
        {
            this(item, new SliderWithUnit());   // TODO: add constraint
            this.getEditor().setUnit(FXCollections.observableArrayList(item.getParameter().getUnit()));
        }

        @Override
        public void setValue(NumberWithUnit number) {
            this.getEditor().setValue(number);
        }

        @Override
        protected ObservableValue<NumberWithUnit> getObservableValue() {
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

    public class ValuePropertyEditor extends AbstractPropertyEditor<ProjectValue, ComboBox<ProjectValue>> {
        public ValuePropertyEditor(PropertySheet.Item property, ComboBox<ProjectValue> control) {
            super(property, control);
            control.setCellFactory(param -> new ListCell<ProjectValue>() {
                @Override
                protected void updateItem(ProjectValue item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.getDevice().getName() + "'s " + item.getValue().getName());
                    }
                }
            });
            control.setButtonCell(new ListCell<ProjectValue>(){
                @Override
                protected void updateItem(ProjectValue item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.getDevice().getName() + "'s " + item.getValue().getName());
                    }
                }
            });
            ObservableList<ProjectValue> list = FXCollections.observableArrayList(viewModel.getProjectValue());
            control.setItems(list);
        }

        public ValuePropertyEditor(PropertySheet.Item property) {
            this(property, new ComboBox<ProjectValue>());
        }

        @Override
        protected ObservableValue<ProjectValue> getObservableValue() {
            return this.getEditor().valueProperty();
        }

        @Override
        public void setValue(ProjectValue s) {
            this.getEditor().setValue(s);
        }
    }

    public class SpinBoxPropertyEditor extends AbstractPropertyEditor<NumberWithUnit, SpinnerWithUnit> {
        public SpinBoxPropertyEditor(ParameterPropertyItem property, SpinnerWithUnit control) {
            super(property, control);
        }

        public SpinBoxPropertyEditor(ParameterPropertyItem property) {
            this(property, new SpinnerWithUnit(((NumericConstraint) property.getParameter().getConstraint()).getMin(),
                    ((NumericConstraint) property.getParameter().getConstraint()).getMax(),
                    ((NumberWithUnit) property.getParameter().getDefaultValue()).getValue(),        // TODO: use unit of value?
                    ((NumericConstraint) property.getParameter().getConstraint()).getUnit()));      // TODO: add constraint
            this.getEditor().setUnit(FXCollections.observableArrayList(property.getParameter().getUnit()));
        }

        @Override
        protected ObservableValue<NumberWithUnit> getObservableValue() {
            return this.getEditor().valueProperty();
        }

        @Override
        public void setValue(NumberWithUnit integer) {
            this.getEditor().setValue(integer);
        }
    }

//    public class ExpressionPropertyEditor extends AbstractPropertyEditor<ObservableList<Expression>, ExpressionControl> {
//        public ExpressionPropertyEditor(PropertySheet.Item property, ExpressionControl control) {
//            super(property, control);
//        }
//
//        public ExpressionPropertyEditor(PropertySheet.Item property) {
//            this(property, new ExpressionControl(((ValuePropertyItem) property).getV(), viewModel.getProjectValue()));
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
//
//    public class ExpressionPropertyEditor extends PropertyEditor<ObservableList<expression>, ExpressionControl> {
//        public ExpressionPropertyEditor(PropertySheet.Item property, ExpressionControl control) {
//            super(property, control);
//        }
//
//        public ExpressionPropertyEditor(PropertySheet.Item property) {
//            this(property, new ExpressionControl(viewModel.getProjectValue()));
//        }
//
//        @Override
//        protected ObservableValue<ObservableList<expression>> getObservableValue() {
//            return this.getEditor().expressionsListProperty();
//        }
//
//        @Override
//        public void setValue(ObservableList<expression> expression) {
//            this.getEditor().setExpressionsList(expression);
//        }
//    }

}
