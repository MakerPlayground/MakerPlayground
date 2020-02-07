package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.VirtualProjectDevice;
import io.makerplayground.project.expression.VariableExpression;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

public class VariableSelectorControl extends VBox {
    final ObjectProperty<VariableExpression> expression;
    StringProperty errorMessage;
    BooleanProperty isEditMode;
    Project project;

    ListView<ProjectValue> valueListView;
    HBox hBox;
    TextField displayTextField;
    TextField nameTextField;
    Label errorMessageLabel;

    public VariableSelectorControl(Project project, VariableExpression expression) {
        this.project = project;
        this.expression = new SimpleObjectProperty<>(expression);
        this.isEditMode = new SimpleBooleanProperty(false);
        this.errorMessage = new SimpleStringProperty();

        initView();
        initEvent();
    }

    EventHandler<ActionEvent> onAddSubmit = event -> {
        String varName = nameTextField.getText();
        Project.VariableAddResult result = project.addVariable(varName);
        if (result.getError() == Project.VariableError.OK) {
            valueListView.getSelectionModel().select(result.getProjectValue());
            valueListView.scrollTo(result.getProjectValue());
            valueListView.requestFocus();
            errorMessage.set("");
        } else if (result.getError() == Project.VariableError.DUPLICATE_NAME) {
            VirtualProjectDevice.Memory.unmodifiableVariables.stream().filter(projectValue -> projectValue.getValue().getName().equals(varName)).findFirst().ifPresent((value) -> {
                valueListView.getSelectionModel().select(value);
                valueListView.scrollTo(value);
                valueListView.requestFocus();
                errorMessage.set("");
            });
        } else {
            errorMessage.set(result.getError().getErrorMessage());
        }
    };

    private void initEvent() {
        sceneProperty().addListener((observable, oldValue, newScene) -> {
            if (newScene != null) {
                newScene.focusOwnerProperty().addListener((observable1, oldFocusOwner, newFocusOwner) -> {
                    Queue<Node> queue = new ArrayDeque<>();
                    queue.add(hBox);
                    queue.add(valueListView);
                    queue.add(errorMessageLabel);
                    while (!queue.isEmpty()) {
                        Node node = queue.remove();
                        if (node == newFocusOwner) {
                            isEditMode.set(true);
                            return;
                        } else if (node instanceof Parent) {
                            queue.addAll(((Parent) node).getChildrenUnmodifiable());
                        }
                    }
                    nameTextField.fireEvent(new ActionEvent());
                    errorMessage.set("");
                    isEditMode.set(false);
                    updateHilight();
                });
            }
        });

        displayTextField.setOnMouseClicked(event -> {
            isEditMode.set(true);
            if (VirtualProjectDevice.Memory.unmodifiableVariables.isEmpty()) {
                nameTextField.requestFocus();
            } else {
                valueListView.requestFocus();
            }
        });
        displayTextField.textProperty().bind(Bindings.createStringBinding(() -> expression.get().getVariableName(), expression));

        nameTextField.setOnAction(onAddSubmit);

        nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                nameTextField.fireEvent(new ActionEvent());
            }
        });

        valueListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.isNull(newValue)) {
                expression.set(VariableExpression.NO_VARIABLE_SELECTED);
            } else {
                nameTextField.setText(newValue.getValue().getName());
                expression.set(new VariableExpression(newValue));
                errorMessage.set("");
            }
        });
    }

    StringConverter<ProjectValue> valueStringConverter = new StringConverter<>() {
        @Override
        public String toString(ProjectValue value) {
            return Objects.nonNull(value) ? value.getValue().getName() : "";
        }

        @Override
        public ProjectValue fromString(String varName) {
            Optional<ProjectValue> valueOptional = VirtualProjectDevice.Memory.unmodifiableVariables.stream().filter(value -> value.getValue().getName().equals(varName)).findFirst();
            return valueOptional.orElseGet(() -> new ProjectValue(VirtualProjectDevice.Memory.projectDevice, new Value(varName, DataType.DOUBLE, Constraint.NONE))); // temporary ProjectValue instance that will be committed for adding to project later.
        }
    };

    private void initView() {
        hBox = new HBox(5.0);
        hBox.setAlignment(Pos.CENTER_LEFT);
        displayTextField = new TextField();
        displayTextField.visibleProperty().bind(isEditMode.not());
        displayTextField.managedProperty().bind(isEditMode.not());
        nameTextField = new TextField(expression.get().getVariableName());
        nameTextField.editableProperty().bind(isEditMode);
        nameTextField.visibleProperty().bind(isEditMode);
        nameTextField.managedProperty().bind(isEditMode);
        Label orSelectLabel = new Label("or select");
        orSelectLabel.visibleProperty().bind(isEditMode.and(Bindings.isNotEmpty(VirtualProjectDevice.Memory.unmodifiableVariables)));
        orSelectLabel.managedProperty().bind(orSelectLabel.visibleProperty());
        hBox.getChildren().addAll(displayTextField, nameTextField, orSelectLabel);

        errorMessageLabel = new Label();
        errorMessageLabel.textProperty().bind(errorMessage);
        errorMessageLabel.managedProperty().bind(errorMessage.isEmpty().not());
        errorMessageLabel.visibleProperty().bind(errorMessage.isEmpty().not());

        valueListView = new ListView<>(VirtualProjectDevice.Memory.unmodifiableVariables);
        valueListView.setMaxHeight(70.0);
        valueListView.visibleProperty().bind(orSelectLabel.visibleProperty());
        valueListView.managedProperty().bind(orSelectLabel.visibleProperty());
        valueListView.setOnKeyPressed(event -> {
            ProjectValue projectValue = valueListView.getSelectionModel().getSelectedItem();
            int index = valueListView.getSelectionModel().getSelectedIndex();
            if (projectValue != null && (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE)) {
                // Delete Variable
                nameTextField.setText("");
                Project.VariableError result = project.removeVariable(projectValue.getValue().getName());
                if (result == Project.VariableError.OK) {
                    if (index < 0 || index > valueListView.getItems().size() - 1) {
                        valueListView.getSelectionModel().selectFirst();
                    } else {
                        valueListView.getSelectionModel().clearSelection();
                        valueListView.getSelectionModel().select(index);
                    }
                }
            }
            updateHilight();
            event.consume();
        });
        valueListView.setOnEditCancel(event -> errorMessage.set(""));
        valueListView.setOnEditCommit(event -> {
            ListView<ProjectValue> listView = event.getSource();
            int index = event.getIndex();
            String oldName = listView.getItems().get(index).getValue().getName();
            String newName = event.getNewValue().getValue().getName();
            if (!oldName.equals(newName)) {
                // Rename Variable
                Project.VariableError result = project.renameVariable(oldName, newName);
                if (result != Project.VariableError.OK) {
                    errorMessage.set(result.getErrorMessage());
                } else {
                    errorMessage.set("");
                }
                valueListView.getSelectionModel().clearSelection();
                valueListView.getSelectionModel().select(index);
            }
            updateHilight();
            event.consume();
        });
        valueListView.setCellFactory(TextFieldListCell.forListView(valueStringConverter));
        valueListView.setEditable(true);

        setSpacing(5.0);
        getChildren().addAll(hBox, errorMessageLabel, valueListView);
    }

    public ObjectProperty<VariableExpression> expressionProperty() {
        return expression;
    }

    public VariableExpression getExpression() {
        return expression.get();
    }

    private void updateHilight() {
        if (!getExpression().isValid()) {
            displayTextField.setStyle("-fx-effect: dropshadow(gaussian, #ff0000, 5.0 , 0.5, 0.0 , 0.0);");
        } else {
            displayTextField.setStyle("-fx-effect: null;");
        }
    }
}
