package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.Record;
import io.makerplayground.device.shared.constraint.IntegerCategoricalConstraint;
import io.makerplayground.device.shared.constraint.StringIntegerCategoricalConstraint;
import io.makerplayground.project.*;
import io.makerplayground.project.expression.*;
import io.makerplayground.ui.canvas.node.expression.*;
import io.makerplayground.ui.canvas.node.expression.custom.MultiFunctionNumericControl;
import io.makerplayground.ui.canvas.node.expression.custom.StringChipField;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.*;

public class SceneDevicePropertyPane extends VBox {

    // Create a grid pane as a main layout for this property sheet
    private final GridPane propertyPane = new GridPane();

    private Label actionLabel;
    private ComboBox<Action> actionComboBox;

    private final Project project;
    private final UserSetting userSetting;
    private final ActualDevice actualDevice;

    public SceneDevicePropertyPane(UserSetting userSetting, Project project, ActualDevice actualDevice) {
        this.project = project;
        this.userSetting = userSetting;
        this.actualDevice = actualDevice;
        initView();
    }

    private void initView() {
        GenericDevice genericDevice = userSetting.getDevice().getGenericDevice();

        // Create title layout
        Image img = new Image(getClass().getResourceAsStream("/icons/colorIcons-3/" + genericDevice.getName() + ".png"));
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);

        Label customName = new Label(userSetting.getDevice().getName());
        customName.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(customName, Priority.ALWAYS);

        // Interactive button
        ImageView interactiveStartImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/interactive-start.png")));
        interactiveStartImageView.setFitWidth(20);
        interactiveStartImageView.setFitHeight(20);

        Tooltip sendActionButtonTooltip = new Tooltip();
        sendActionButtonTooltip.setShowDelay(Duration.millis(250));
        sendActionButtonTooltip.setText("Test on real device");

        Button sendActionButton = new Button();
        sendActionButton.setTooltip(sendActionButtonTooltip);
        sendActionButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0px;");
        sendActionButton.setGraphic(interactiveStartImageView);

        ReadOnlyBooleanProperty initializedProperty = project.getInteractiveModel().startedProperty();
        ReadOnlyObjectProperty<Action> actionProperty = userSetting.actionProperty();
        ReadOnlyObjectProperty<Map<ProjectDevice, Set<Value>>> allValueUsedProperty =  userSetting.allValueUsedProperty();
        ObservableMap<Value, Expression> expressionMap = userSetting.getExpression();
        ObservableMap<Parameter, Expression> parameterMap = userSetting.getParameterMap();
        BooleanBinding disableBinding = Bindings.createBooleanBinding(()-> {
            if (!initializedProperty.get()) {
                return true;
            }
            InteractiveModel interactiveModel = project.getInteractiveModel();
            if (!interactiveModel.hasCommand(userSetting.getDevice(), actionProperty.get())) {
                return true;
            }
            if (allValueUsedProperty.get().entrySet().stream().anyMatch(entry -> entry.getValue().stream().anyMatch(value ->
                    interactiveModel.getValueProperty(entry.getKey(), value).isEmpty()))) {
                return true;
            }
            if (!expressionMap.values().stream().allMatch(Expression::isValid)) {
                return true;
            }
            if (!parameterMap.values().stream().allMatch(Expression::isValid)) {
                return true;
            }
            return false;
        }, initializedProperty, actionProperty, allValueUsedProperty, expressionMap, parameterMap);
        sendActionButton.disableProperty().bind(disableBinding);
        sendActionButton.setOnAction(event -> project.getInteractiveModel().sendActionCommand(userSetting));

        HBox titleHBox = new HBox();
        titleHBox.getChildren().addAll(imageView, customName, sendActionButton);
        titleHBox.setAlignment(Pos.CENTER_LEFT);
        titleHBox.setSpacing(10);

        // Create ComboBox for user to select a condition
        actionLabel = new Label("Action");
        GridPane.setRowIndex(actionLabel, 0);
        GridPane.setColumnIndex(actionLabel, 0);

        List<Action> actions = new ArrayList<>(genericDevice.getAction());
        if (actualDevice != null) {
            actions.retainAll(actualDevice.getCompatibilityMap().get(genericDevice).getDeviceAction().keySet());
        }
        actionComboBox = new ComboBox<>(FXCollections.observableArrayList(actions));
        actionComboBox.setCellFactory(new Callback<>() {
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
        actionComboBox.setButtonCell(new ListCell<>(){
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
        actionComboBox.getSelectionModel().select(userSetting.getAction());
        actionComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            userSetting.setAction(newValue);
            redrawProperty();
        });
        GridPane.setRowIndex(actionComboBox, 0);
        GridPane.setColumnIndex(actionComboBox, 1);

        propertyPane.setHgap(10);
        propertyPane.setVgap(5);
        propertyPane.getChildren().addAll(actionLabel, actionComboBox);

        redrawProperty();

        // arrange title and property sheet
        getStylesheets().add(this.getClass().getResource("/css/canvas/node/usersetting/DevicePropertyWindow.css").toExternalForm());
        getChildren().addAll(titleHBox, propertyPane);
        setSpacing(5.0);
    }

    private void redrawProperty() {
        propertyPane.getChildren().retainAll(actionLabel, actionComboBox);

        List<Parameter> params = userSetting.getAction().getParameter();
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
                ObservableList<String> list = FXCollections.observableArrayList(((StringIntegerCategoricalConstraint) p.getConstraint()).getMap().keySet());
                ComboBox<String> comboBox = new ComboBox<>(list);
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> userSetting.getParameterMap().put(p, new StringIntegerExpression((StringIntegerCategoricalConstraint)p.getConstraint(), newValue)));
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
            } else if (p.getControlType() == ControlType.DOT_MATRIX && p.getDataType() == DataType.DOT_MATRIX_DATA) {
                DotMatrixExpressionControl expressionControl = new DotMatrixExpressionControl((DotMatrixExpression) userSetting.getParameterMap().get(p));
            } else if (p.getControlType() == ControlType.VARIABLE) {
                VariableSelectorControl expressionControl = new VariableSelectorControl(
                        project,
                        (VariableExpression) (userSetting.getParameterMap().get(p))
                );
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
    }
}