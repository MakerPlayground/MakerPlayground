package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.*;
import io.makerplayground.helper.ControlType;
import io.makerplayground.helper.DataType;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.*;
import io.makerplayground.ui.canvas.node.expressioncontrol.DoubleNumberExpressionControl;
import io.makerplayground.ui.canvas.node.expressioncontrol.NumberInRangeExpressionControl;
import io.makerplayground.ui.canvas.node.expressioncontrol.SpinnerWithUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;

import java.util.List;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class SceneDevicePropertyWindow extends PopOver {
    private final SceneDeviceIconViewModel viewModel;

    // Create a grid pane as a main layout for this property sheet
    private final GridPane propertyPane = new GridPane();

    private Label actionLabel;
    private ComboBox<Action> actionComboBox;

    public SceneDevicePropertyWindow(SceneDeviceIconViewModel viewModel) {
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
        actionLabel = new Label("Action");
        GridPane.setRowIndex(actionLabel, 0);
        GridPane.setColumnIndex(actionLabel, 0);

        actionComboBox = new ComboBox<>(FXCollections.observableArrayList(viewModel.getGenericDevice().getAction()));
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
        actionComboBox.getSelectionModel().select(viewModel.getAction());
        // bind action selected to the view model
        viewModel.actionProperty().bind(actionComboBox.getSelectionModel().selectedItemProperty());
        GridPane.setRowIndex(actionComboBox, 0);
        GridPane.setColumnIndex(actionComboBox, 1);

        propertyPane.setVgap(5);
        propertyPane.getChildren().addAll(actionLabel, actionComboBox);

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
        propertyPane.getChildren().retainAll(actionLabel, actionComboBox);

        List<Parameter> params = viewModel.getAction().getParameter();
        for (int i=0; i<params.size(); i++) {
            Parameter p = params.get(i);

            Label name = new Label(p.getName());
            GridPane.setRowIndex(name, i+1);
            GridPane.setColumnIndex(name, 0);

            Node control = null;
            if (p.getDataType() == DataType.VALUE) {
                ObservableList<ProjectValue> list = FXCollections.observableArrayList(viewModel.getProjectValue());
                ComboBox<ProjectValue> comboBox = new ComboBox<>(list);
                if (viewModel.getParameterValue(p) != null) {
                    comboBox.setValue(((ProjectValueExpression) viewModel.getParameterValue(p)).getProjectValue());
                }
                comboBox.setCellFactory(param -> new ListCell<>() {
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
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, new ProjectValueExpression(newValue)));
                control = comboBox;
            } else if (p.getControlType() == ControlType.SLIDER) {
                if (viewModel.getParameterValue(p) == null) {
                    viewModel.setParameterValue(p, new NumberWithUnitExpression(new NumberWithUnit(0.0, Unit.NOT_SPECIFIED)));
                }
                DoubleNumberExpressionControl doubleNumberExpressionControl = new DoubleNumberExpressionControl(
                        p.getMinimumValue(),
                        p.getMaximumValue(),
                        FXCollections.observableArrayList(p.getUnit()),
                        FXCollections.observableArrayList(viewModel.getProjectValue()),
                        viewModel.getParameterValue(p)
                );
                doubleNumberExpressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
                control = doubleNumberExpressionControl;
            } else if (p.getControlType() == ControlType.TEXTBOX) {
                TextField textField = new TextField();
                textField.setText(((SimpleStringExpression) viewModel.getParameterValue(p)).getString());
                textField.textProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, new SimpleStringExpression(newValue)));
                control = textField;
            } else if (p.getControlType() == ControlType.DROPDOWN) {
                ObservableList<String> list = FXCollections.observableArrayList(((CategoricalConstraint) p.getConstraint()).getCategories());
                ComboBox<String> comboBox = new ComboBox<>(list);
                comboBox.getSelectionModel().select(((SimpleStringExpression) viewModel.getParameterValue(p)).getString());
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, new SimpleStringExpression(newValue)));
                control = comboBox;
            } else if (p.getControlType() == ControlType.SPINBOX) {
                NumericConstraint constraint = ((NumericConstraint) p.getConstraint());
                NumberWithUnit defaultValue;
                if (viewModel.getParameterValue(p) != null) {
                    defaultValue = ((NumberWithUnitExpression) viewModel.getParameterValue(p)).getNumberWithUnit();
                }
                else {
                    defaultValue = (NumberWithUnit) p.getDefaultValue();
                }
                SpinnerWithUnit spinner = new SpinnerWithUnit(constraint.getMin(), constraint.getMax()
                        , defaultValue.getValue()
                        , defaultValue.getUnit()
                        , FXCollections.observableArrayList(p.getUnit()));
                spinner.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, new NumberWithUnitExpression(newValue)));
                control = spinner;
            } else {
                throw new IllegalStateException("Found unknown control type " + p);
            }
            GridPane.setRowIndex(control, i+1);
            GridPane.setColumnIndex(control, 1);
            propertyPane.getChildren().addAll(name, control);
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

        if (expression instanceof NumberInRangeExpression) {
            NumberInRangeExpressionControl expressionControl = new NumberInRangeExpressionControl((NumberInRangeExpression) expression, value);
            GridPane.setRowIndex(expressionControl, i+1);
            GridPane.setColumnIndex(expressionControl, 1);
            propertyPane.getChildren().add(expressionControl);
        } else {
            throw new IllegalStateException("");    // TODO: add support soon
        }
    }

}
