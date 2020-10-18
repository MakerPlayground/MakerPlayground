package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.Record;
import io.makerplayground.device.shared.constraint.IntegerCategoricalConstraint;
import io.makerplayground.device.shared.constraint.StringIntegerCategoricalConstraint;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.UserSetting;
import io.makerplayground.project.VirtualProjectDevice.Memory;
import io.makerplayground.project.expression.*;
import io.makerplayground.ui.canvas.node.expression.ConditionalExpressionControl;
import io.makerplayground.ui.canvas.node.expression.RTCExpressionControl;
import io.makerplayground.ui.canvas.node.expression.RecordExpressionControl;
import io.makerplayground.ui.canvas.node.expression.StringExpressionControl;
import io.makerplayground.ui.canvas.node.expression.custom.MultiFunctionNumericControl;
import io.makerplayground.ui.canvas.node.expression.custom.StringChipField;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ConditionDevicePropertyPane extends VBox {

    // Create a grid pane as a main layout for this property sheet
    private final GridPane propertyPane = new GridPane();

    private Label conditionLabel;
    private ComboBox<Condition> conditionComboBox;

    private final Project project;
    private final UserSetting userSetting;

    public ConditionDevicePropertyPane(UserSetting userSetting, Project project) {
        this.project = project;
        this.userSetting = userSetting;
        initView();
    }

    private void initView() {
        GenericDevice genericDevice = userSetting.getDevice().getGenericDevice();

        // Create title layout
        Image img = new Image(DeviceLibrary.getGenericDeviceIconAsStream(genericDevice));
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);

        Label customName = new Label(userSetting.getDevice().getName());
        customName.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(customName, Priority.ALWAYS);

        HBox titleHBox = new HBox();
        titleHBox.getChildren().addAll(imageView, customName);
        titleHBox.setAlignment(Pos.CENTER_LEFT);
        titleHBox.setSpacing(10);

        // Create ComboBox for user to select a condition
        conditionLabel = new Label("Condition");
        GridPane.setRowIndex(conditionLabel, 0);
        GridPane.setColumnIndex(conditionLabel, 0);

        conditionComboBox = new ComboBox<>(FXCollections.observableArrayList(genericDevice.getCondition()));
        conditionComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Condition> call(ListView<Condition> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Condition item, boolean empty) {
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
            protected void updateItem(Condition item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });
        conditionComboBox.getSelectionModel().select(userSetting.getCondition());
        conditionComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            userSetting.setCondition(newValue);
            redrawProperty();
        });
        GridPane.setRowIndex(conditionComboBox, 0);
        GridPane.setColumnIndex(conditionComboBox, 1);

        propertyPane.setHgap(10);
        propertyPane.setVgap(5);
        propertyPane.getChildren().addAll(conditionLabel, conditionComboBox);

        redrawProperty();

        // arrange title and property sheet
        getStylesheets().add(this.getClass().getResource("/css/canvas/node/usersetting/DevicePropertyWindow.css").toExternalForm());
        getChildren().addAll(titleHBox, propertyPane);
        setSpacing(5.0);
    }

    private void redrawProperty() {
        propertyPane.getChildren().retainAll(conditionLabel, conditionComboBox);

        if (project.getInteractiveModel().isStarted()
                && !userSetting.getCondition().getName().equals("Compare")) { // TODO: compare with condition name is dangerous
            Label valueLabel = new Label();
            valueLabel.setStyle("-fx-text-fill: grey;");
            Optional<ReadOnlyBooleanProperty> conditionProperty = project.getInteractiveModel().getConditionProperty(userSetting.getDevice(), userSetting.getCondition());
            if (conditionProperty.isPresent()) {
                BooleanBinding deviceValidBinding = Bindings.createBooleanBinding(() -> project.getInteractiveModel().isDeviceValid(userSetting.getDevice()), conditionProperty.get());
                valueLabel.textProperty().bind(Bindings.when(deviceValidBinding)
                        .then(Bindings.concat("(").concat(conditionProperty.get().asString()).concat(")"))
                        .otherwise("(status unavailable)"));
                Tooltip tooltip = new Tooltip("Restart the preview mode to see realtime value");
                tooltip.setShowDelay(Duration.millis(250));
                valueLabel.tooltipProperty().bind(Bindings.when(deviceValidBinding).then((Tooltip) null).otherwise(tooltip));
            } else {
                valueLabel.setText("(status unavailable)");
            }
            GridPane.setRowIndex(valueLabel, 0);
            GridPane.setColumnIndex(valueLabel, 2);
            propertyPane.getChildren().add(valueLabel);
        }

        List<Parameter> params = userSetting.getCondition().getParameter();
        for (int i=0; i<params.size(); i++) {
            Parameter p = params.get(i);

            Label name = new Label(p.getName());
            name.setMinHeight(25);  // TODO: find better way to center the label to the height of 1 row control when the control spans to multiple rows
            GridPane.setRowIndex(name, i + 1);
            GridPane.setColumnIndex(name, 0);
            GridPane.setValignment(name, VPos.TOP);

            Node control = null;
            if (p.getControlType() == ControlType.SLIDER) {
                MultiFunctionNumericControl expressionControl = new MultiFunctionNumericControl(
                        p,
                        project.getAvailableValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)),
                        CustomNumberExpression.of(userSetting.getParameterMap().get(p))
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.getParameterMap().put(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.TEXTBOX) {
                StringChipField stringChipField = new StringChipField((ComplexStringExpression) userSetting.getParameterMap().get(p)
                        , project.getAvailableValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)));
                stringChipField.expressionProperty().addListener((observableValue, oldValue, newValue) -> userSetting.getParameterMap().put(p, newValue));
                control = stringChipField;
            } else if (p.getControlType() == ControlType.DROPDOWN && p.getDataType() == DataType.INTEGER_ENUM) {
                ObservableList<Integer> list = FXCollections.observableArrayList(((IntegerCategoricalConstraint) p.getConstraint()).getCategories());
                ComboBox<Integer> comboBox = new ComboBox<>(list);
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> userSetting.getParameterMap().put(p, new SimpleIntegerExpression(newValue)));
                comboBox.getSelectionModel().select((((SimpleIntegerExpression) userSetting.getParameterMap().get(p)).getInteger()));
                control = comboBox;
            } else if (p.getControlType() == ControlType.DROPDOWN && p.getDataType() == DataType.STRING_INT_ENUM) {
                StringIntegerCategoricalConstraint constraint = (StringIntegerCategoricalConstraint) p.getConstraint();
                ObservableList<String> list = FXCollections.observableArrayList(constraint.getMap().keySet());
                ComboBox<String> comboBox = new ComboBox<>(list);
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> userSetting.getParameterMap().put(p, new StringIntegerExpression(constraint, newValue)));
                comboBox.getSelectionModel().select((((StringIntegerExpression) userSetting.getParameterMap().get(p)).getString()));
                control = comboBox;
            } else if (p.getControlType() == ControlType.SPINBOX) {
                MultiFunctionNumericControl expressionControl = new MultiFunctionNumericControl(
                        p,
                        project.getAvailableValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)),
                        CustomNumberExpression.of(userSetting.getParameterMap().get(p))
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.getParameterMap().put(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.DATETIMEPICKER) {
                RTCExpressionControl expressionControl = new RTCExpressionControl((SimpleRTCExpression) userSetting.getParameterMap().get(p));
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.getParameterMap().put(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.IMAGE_SELECTOR) {
                ComboBox<ProjectValue> comboBox = new ComboBox<>(FXCollections.observableArrayList(project.getAvailableValue(EnumSet.of(DataType.IMAGE))));
                comboBox.setCellFactory(param -> new ListCell<>(){
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
                comboBox.setButtonCell(new ListCell<>(){
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
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> userSetting.getParameterMap().put(p, new ImageExpression(newValue)));
                ProjectValue projectValue = ((ImageExpression) userSetting.getParameterMap().get(p)).getProjectValue();
                if (projectValue != null) {
                    comboBox.getSelectionModel().select(projectValue);
                }
                control = comboBox;
            } else if (p.getControlType() == ControlType.TEXTBOX_WITH_TEXT_SELECTOR) {
                StringExpressionControl expressionControl = new StringExpressionControl(
                        p,
                        project.getAvailableValue(Set.of(DataType.STRING)),
                        userSetting.getParameterMap().get(p)
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.getParameterMap().put(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.RECORD) {
                if (userSetting.getParameterMap().get(p) == null) {
                    userSetting.getParameterMap().put(p, new RecordExpression(new Record()));
                }
                // a fake parameter to make value linking works as expect
                RecordExpressionControl expressionControl = new RecordExpressionControl(project.getAvailableValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER))
                        , (RecordExpression) userSetting.getParameterMap().get(p));
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.getParameterMap().put(p, newValue));
                control = expressionControl;
            } else {
                throw new IllegalStateException("Found unknown control type " + p);
            }
            GridPane.setRowIndex(control, i+1);
            GridPane.setColumnIndex(control, 1);
            GridPane.setHalignment(control, HPos.LEFT);
            GridPane.setFillWidth(control, false);
            propertyPane.getChildren().addAll(name, control);
        }

        if (userSetting.getDevice().equals(Memory.projectDevice) && userSetting.getCondition() == Memory.compare) {
            List<ProjectValue> values = project.getUnmodifiableVariable();
            for (int i=0; i<values.size(); i++) {
                ProjectValue value = values.get(i);
                createExpressionControl(i, value.getValue());
            }
        } else if (userSetting.getCondition().getName().equals("Compare")) {    // TODO: compare with condition name may be dangerous
            List<Value> values = userSetting.getDevice().getGenericDevice().getValue();
            for (int i=0; i<values.size(); i++) {
                Value value = values.get(i);
                createExpressionControl(i, value);
            }
        }
    }

    private void createExpressionControl(int i, Value value) {
        Expression expression = userSetting.getExpression().get(value);

        CheckBox enableCheckbox = new CheckBox(value.getName());
        enableCheckbox.setMinHeight(25);    // TODO: find better way to center the label to the height of 1 row control when the control spans to multiple rows
        enableCheckbox.setSelected(userSetting.getExpressionEnable().get(value));
        enableCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> userSetting.getExpressionEnable().put(value, newValue));
        GridPane.setValignment(enableCheckbox, VPos.TOP);
        GridPane.setRowIndex(enableCheckbox, i+1);
        GridPane.setColumnIndex(enableCheckbox, 0);

        ConditionalExpressionControl expressionControl = new ConditionalExpressionControl(userSetting.getDevice()
                , value, project.getAvailableValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)), expression/*, viewModel.isExpressionEnable(value)*/);
        expressionControl.disableProperty().bind(enableCheckbox.selectedProperty().not());
        expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.getExpression().put(value, newValue));
        GridPane.setRowIndex(expressionControl, i+1);
        GridPane.setColumnIndex(expressionControl, 1);

        propertyPane.getChildren().addAll(enableCheckbox, expressionControl);

        if (project.getInteractiveModel().isStarted()) {
            Label valueLabel = new Label();
            valueLabel.setMinHeight(25);  // TODO: find better way to center the label to the height of 1 row control when the control spans to multiple rows
            valueLabel.setStyle("-fx-text-fill: grey;");
            Optional<ReadOnlyDoubleProperty> valueProperty = project.getInteractiveModel().getValueProperty(userSetting.getDevice(), value);
            if (valueProperty.isPresent()) {
                BooleanBinding deviceValidBinding = Bindings.createBooleanBinding(() -> project.getInteractiveModel().isDeviceValid(userSetting.getDevice()), valueProperty.get());
                valueLabel.textProperty().bind(Bindings.when(deviceValidBinding)
                        .then(Bindings.concat("(value = ").concat(valueProperty.get().asString()).concat(")"))
                        .otherwise("(value unavailable)"));
                Tooltip tooltip = new Tooltip("Restart the interactive mode to see realtime value");
                tooltip.setShowDelay(Duration.millis(250));
                valueLabel.tooltipProperty().bind(Bindings.when(deviceValidBinding).then((Tooltip) null).otherwise(tooltip));
            } else {
                valueLabel.setText("(value unavailable)");
            }
            GridPane.setRowIndex(valueLabel, i + 1);
            GridPane.setColumnIndex(valueLabel, 2);
            GridPane.setValignment(valueLabel, VPos.TOP);
            propertyPane.getChildren().add(valueLabel);
        }
    }
}