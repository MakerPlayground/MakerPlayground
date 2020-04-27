package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.VirtualProjectDevice;
import io.makerplayground.project.expression.VariableExpression;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public class VariableSelectorControl extends VBox {
    private final Project project;
    private final ReadOnlyObjectWrapper<VariableExpression> expression;
    private final BooleanProperty editing;
    private final Timeline errorMessageTimeline;

    private TextField nameTextField;
    private ListView<ProjectValue> valueListView;
    private Label errorMessageLabel;

    public VariableSelectorControl(Project project, VariableExpression expression) {
        this.project = project;
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        this.editing = new SimpleBooleanProperty(false);
        // auto hide error message after 5 sec
        this.errorMessageTimeline = new Timeline();
        this.errorMessageTimeline.getKeyFrames().addAll(new KeyFrame(Duration.seconds(0), (event) -> errorMessageLabel.setVisible(true)),
                new KeyFrame(Duration.seconds(5), (event) -> errorMessageLabel.setVisible(false)));

        initView();
        initEvent();
    }

    private void initView() {
        nameTextField = new TextField(expression.get().getVariableName());

        valueListView = new ListView<>(project.getUnmodifiableVariable());
        valueListView.visibleProperty().bind(editing.and(Bindings.isNotEmpty(project.getUnmodifiableVariable())));
        valueListView.managedProperty().bind(valueListView.visibleProperty());
        valueListView.setCellFactory(TextFieldListCell.forListView(new StringConverter<>() {
            @Override
            public String toString(ProjectValue value) {
                return Objects.nonNull(value) ? value.getValue().getName() : "";
            }

            @Override
            public ProjectValue fromString(String varName) {
                // TODO: check whether it is safe to create this dummy instance
                return project.getVariableByName(varName).orElseGet(() -> new ProjectValue(VirtualProjectDevice.Memory.projectDevice, new Value(varName, DataType.DOUBLE, Constraint.createNumericConstraint(-Double.MAX_VALUE, Double.MAX_VALUE, Unit.NOT_SPECIFIED))));
            }
        }));
        valueListView.setEditable(true);
        expression.get().getProjectValue().ifPresent((pv) -> valueListView.getSelectionModel().select(pv));

        errorMessageLabel = new Label();
        errorMessageLabel.setVisible(false);
        errorMessageLabel.managedProperty().bind(errorMessageLabel.visibleProperty());

        setSpacing(5.0);
        getChildren().addAll(nameTextField, valueListView, errorMessageLabel);
        getStylesheets().add(getClass().getResource("/css/canvas/node/expressioncontrol/VariableSelectorControl.css").toExternalForm());
    }

    private void initEvent() {
        // toggle edit mode when focus changes
        sceneProperty().addListener((observable, oldValue, newScene) -> {
            if (newScene != null) {
                newScene.focusOwnerProperty().addListener((observable1, oldFocusOwner, newFocusOwner) -> {
                    Queue<Node> queue = new ArrayDeque<>();
                    queue.add(this);
                    while (!queue.isEmpty()) {
                        Node node = queue.remove();
                        if (node == newFocusOwner) {
                            editing.set(true);
                            return;
                        } else if (node instanceof Parent) {
                            queue.addAll(((Parent) node).getChildrenUnmodifiable());
                        }
                    }
                    editing.set(false);
                    updateHilight();
                });
            }
        });

        nameTextField.setOnAction((event) -> createNewVariable());
        nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                createNewVariable();
            }
            updateHilight();
        });

        valueListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.isNull(newValue)) {
                nameTextField.setText("");
                expression.set(VariableExpression.NO_VARIABLE_SELECTED);
            } else {
                nameTextField.setText(newValue.getValue().getName());
                expression.set(new VariableExpression(newValue));
            }
            updateHilight();
        });
        valueListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                ProjectValue projectValue = valueListView.getSelectionModel().getSelectedItem();
                if (projectValue != null) {
                    project.removeVariable(projectValue.getValue().getName());
                    nameTextField.setText("");
                    valueListView.getSelectionModel().clearSelection();
                }
                updateHilight();
                event.consume();
            }
        });
        valueListView.setOnEditCommit(event -> {
            int index = event.getIndex();
            String oldName = valueListView.getItems().get(index).getValue().getName();
            String newName = event.getNewValue().getValue().getName();
            if (!oldName.equals(newName)) {
                Project.VariableError result = project.renameVariable(oldName, newName);
                if (result != Project.VariableError.OK) {
                    showErrorMessage(result.getErrorMessage());
                } else {
                    nameTextField.setText(newName);
                }
            }
            valueListView.requestFocus();
            updateHilight();
        });
    }

    private void createNewVariable() {
        String varName = nameTextField.getText();
        Project.VariableAddResult result = project.addVariable(varName);
        if (result.getError() == Project.VariableError.OK) {
            valueListView.getSelectionModel().select(result.getProjectValue());
            valueListView.scrollTo(result.getProjectValue());
        } else if (result.getError() == Project.VariableError.DUPLICATE_NAME) {
            project.getVariableByName(varName).ifPresent((value) -> {
                valueListView.getSelectionModel().select(value);
                valueListView.scrollTo(value);
                valueListView.requestFocus();
            });
        } else {
            showErrorMessage(result.getError().getErrorMessage());
        }
    }

    private void showErrorMessage(String s) {
        errorMessageLabel.setText(s);
        errorMessageTimeline.playFromStart();
    }

    public ReadOnlyObjectProperty<VariableExpression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }

    public VariableExpression getExpression() {
        return expression.get();
    }

    private void updateHilight() {
        if (!getExpression().isValid()) {
            nameTextField.setStyle("-fx-effect: dropshadow(gaussian, #ff0000, 5.0 , 0.5, 0.0 , 0.0);");
        } else {
            nameTextField.setStyle("-fx-effect: null;");
        }
    }
}
