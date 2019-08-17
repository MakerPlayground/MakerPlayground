package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.CategoricalConstraint;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.UserSetting;
import io.makerplayground.project.expression.*;
import io.makerplayground.ui.canvas.node.expression.ConditionalExpressionControl;
import io.makerplayground.ui.canvas.node.expression.RTCExpressionControl;
import io.makerplayground.ui.canvas.node.expression.RecordExpressionControl;
import io.makerplayground.ui.canvas.node.expression.StringExpressionControl;
import io.makerplayground.ui.canvas.node.expression.custom.MultiFunctionNumericControl;
import io.makerplayground.ui.canvas.node.expression.custom.StringChipField;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class DevicePropertyPane extends VBox {

    // Create a grid pane as a main layout for this property sheet
    private final GridPane propertyPane = new GridPane();

    private Label actionLabel;
    private ComboBox<Action> actionComboBox;

    private final Project project;
    private final UserSetting userSetting;
    private final boolean isAction;

    public DevicePropertyPane(UserSetting userSetting, Project project, boolean isAction) {
        this.project = project;
        this.userSetting = userSetting;
        this.isAction = isAction;
        initView();
    }

    private void initView() {
        GenericDevice genericDevice = userSetting.getProjectDevice().getGenericDevice();

        // Create title layout
        Image img = new Image(getClass().getResourceAsStream("/icons/colorIcons-3/" + genericDevice.getName() + ".png"));
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);

        Label customName = new Label(userSetting.getProjectDevice().getName());
        customName.setMaxWidth(Region.USE_COMPUTED_SIZE);

        HBox titleHBox = new HBox();
        titleHBox.getChildren().addAll(imageView, customName);
        titleHBox.setAlignment(Pos.CENTER_LEFT);
        titleHBox.setSpacing(10);

        // Create ComboBox for user to select a condition
        actionLabel = new Label(isAction ? "Action" : "Condition");
        GridPane.setRowIndex(actionLabel, 0);
        GridPane.setColumnIndex(actionLabel, 0);

        actionComboBox = new ComboBox<>(FXCollections.observableArrayList(isAction ? genericDevice.getAction() : genericDevice.getCondition()));
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
                        CustomNumberExpression.of(userSetting.getParameterValue(p))
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.setParameterValue(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.TEXTBOX) {
                StringChipField stringChipField = new StringChipField((ComplexStringExpression) userSetting.getParameterValue(p)
                        , project.getAvailableValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)));
                stringChipField.expressionProperty().addListener((observableValue, oldValue, newValue) -> userSetting.setParameterValue(p, newValue));
                control = stringChipField;
            } else if (p.getControlType() == ControlType.DROPDOWN) {
                ObservableList<String> list = FXCollections.observableArrayList(((CategoricalConstraint) p.getConstraint()).getCategories());
                ComboBox<String> comboBox = new ComboBox<>(list);
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> userSetting.setParameterValue(p, new SimpleStringExpression(newValue)));
                comboBox.getSelectionModel().select(((SimpleStringExpression) userSetting.getParameterValue(p)).getString());
                control = comboBox;
            } else if (p.getControlType() == ControlType.SPINBOX) {
                MultiFunctionNumericControl expressionControl = new MultiFunctionNumericControl(
                        p,
                        project.getAvailableValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)),
                        CustomNumberExpression.of(userSetting.getParameterValue(p))
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.setParameterValue(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.DATETIMEPICKER) {
                RTCExpressionControl expressionControl = new RTCExpressionControl((SimpleRTCExpression) userSetting.getParameterValue(p));
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.setParameterValue(p, newValue));
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
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> userSetting.setParameterValue(p, new ImageExpression(newValue)));
                ProjectValue projectValue = ((ImageExpression) userSetting.getParameterValue(p)).getProjectValue();
                if (projectValue != null) {
                    comboBox.getSelectionModel().select(projectValue);
                }
                control = comboBox;
            } else if (p.getControlType() == ControlType.TEXTBOX_WITH_TEXT_SELECTOR) {
                StringExpressionControl expressionControl = new StringExpressionControl(
                        p,
                        project.getAvailableValue(Set.of(DataType.STRING)),
                        userSetting.getParameterValue(p)
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.setParameterValue(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.RECORD) {
                if (userSetting.getParameterValue(p) == null) {
                    userSetting.setParameterValue(p, new RecordExpression(new Record()));
                }
                // a fake parameter to make value linking works as expect
                RecordExpressionControl expressionControl = new RecordExpressionControl(project.getAvailableValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER))
                        , (RecordExpression) userSetting.getParameterValue(p));
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.setParameterValue(p, newValue));
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

        if (!isAction && userSetting.getAction().getName().equals("Compare")) {    // TODO: compare with condition name may be dangerous
            List<Value> values = userSetting.getProjectDevice().getGenericDevice().getValue();
            for (int i=0; i<values.size(); i++) {
                Value value = values.get(i);
                createExpressionControl(i, value);
            }
        }
    }

    private void createExpressionControl(int i, Value value) {
        Expression expression = userSetting.getExpression(value);

        CheckBox enableCheckbox = new CheckBox(value.getName());
        enableCheckbox.setSelected(userSetting.isExpressionEnable(value));
        enableCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> userSetting.setExpressionEnable(value, newValue));
        GridPane.setValignment(enableCheckbox, VPos.TOP);
        GridPane.setRowIndex(enableCheckbox, i+1);
        GridPane.setColumnIndex(enableCheckbox, 0);

        ConditionalExpressionControl expressionControl = new ConditionalExpressionControl(userSetting.getProjectDevice()
                , value, project.getAvailableValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)), expression/*, viewModel.isExpressionEnable(value)*/);
        expressionControl.disableProperty().bind(enableCheckbox.selectedProperty().not());
        expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> userSetting.setExpression(value, newValue));
        GridPane.setRowIndex(expressionControl, i+1);
        GridPane.setColumnIndex(expressionControl, 1);

        propertyPane.getChildren().addAll(enableCheckbox, expressionControl);
    }
}
