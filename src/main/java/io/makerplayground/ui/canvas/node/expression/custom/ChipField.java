package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.term.*;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.SpinnerWithUnit;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChipField extends VBox {
    @FXML private ScrollPane scrollPane;
    @FXML private HBox mainPane;
    @FXML private Rectangle cursor;

    @FXML private GridPane chipSelectorPane;
    @FXML private StackPane numberChip;
    @FXML private StackPane valueChip;
    @FXML private StackPane plusChip;
    @FXML private StackPane multiplyChip;
    @FXML private StackPane minusChip;
    @FXML private StackPane divideChip;
    @FXML private StackPane openParenthesisChip;
    @FXML private StackPane closeParenthesisChip;

    private final List<ProjectValue> projectValues;
    private final ReadOnlyObjectWrapper<CustomNumberExpression> expressionProperty;

    private final List<Chip> chipList = new ArrayList<>();
    private final Map<Chip, Term> chipMap = new HashMap<>();

    private final BooleanProperty chipFieldFocus = new SimpleBooleanProperty();

    private final Insets CHIP_FIT_INSETS = new Insets(0, 0, 0, -10);

    public ChipField(CustomNumberExpression expression, List<ProjectValue> projectValues) {
        this.projectValues = projectValues;
        this.expressionProperty = new ReadOnlyObjectWrapper<>(expression);
        initView();
        initEvent();
    }

    private void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/ChipField.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ComboBox<Expression.RefreshInterval> refreshIntervalComboBox = new ComboBox<>(FXCollections.observableArrayList(Expression.RefreshInterval.values()));
        refreshIntervalComboBox.getSelectionModel().select(getExpression().getRefreshInterval());
        refreshIntervalComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            getExpression().setRefreshInterval(newValue);
        });

        SpinnerWithUnit customIntervalSpinner = new SpinnerWithUnit(0, Double.MAX_VALUE, List.of(Unit.SECOND, Unit.MILLISECOND), getExpression().getUserDefinedInterval());
        customIntervalSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            getExpression().setUserDefinedInterval(newValue);
        });
        customIntervalSpinner.visibleProperty().bind(refreshIntervalComboBox.getSelectionModel().selectedItemProperty().isEqualTo(Expression.RefreshInterval.USER_DEFINED));
        customIntervalSpinner.managedProperty().bind(customIntervalSpinner.visibleProperty());

        HBox refreshIntervalHBox = new HBox(5);
        refreshIntervalHBox.setAlignment(Pos.CENTER_LEFT);
        Label toValueLabel = new Label("to value");
        refreshIntervalHBox.getChildren().addAll(refreshIntervalComboBox, customIntervalSpinner, toValueLabel);
        getChildren().add(0, refreshIntervalHBox);

        // initialize chip based on expression
        List<Term> listTerm = expressionProperty.get().getTerms();
        for (int i=0; i<listTerm.size(); i++) {
            addChipUI(listTerm.get(i), i);
        }

        // hide the cursor until the ChipField or it's children (chip in the ChipField) received focus
        cursor.setVisible(false);
        cursor.setManaged(false);

        // hide the chipSelectorPane until the ChipField or it's children (chip in the ChipField) received focus
        chipSelectorPane.setVisible(false);
        chipSelectorPane.setManaged(false);

        updateViewLayout();
        updateHilight();
    }

    private void initEvent() {
        scrollPane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                int currentCursorPosition = mainPane.getChildren().indexOf(cursor);
                if (currentCursorPosition > 0) {
                    Chip chip = chipList.get(currentCursorPosition - 1);
                    removeChip(chip);
                }
            } else if (event.getCode() == KeyCode.DELETE) {
                int currentCursorPosition = mainPane.getChildren().indexOf(cursor);
                if (currentCursorPosition < chipList.size()) {
                    Chip chip = chipList.get(currentCursorPosition);
                    removeChip(chip);
                }
            } else if (event.getCode() == KeyCode.LEFT) {
                int currentCursorPosition = mainPane.getChildren().indexOf(cursor);
                if (currentCursorPosition > 0) {
                    mainPane.getChildren().remove(cursor);
                    mainPane.getChildren().add(currentCursorPosition - 1, cursor);
                    updateViewLayout();
                    repositionScrollpane(cursor);
                }
            } else if (event.getCode() == KeyCode.RIGHT) {
                int currentCursorPosition = mainPane.getChildren().indexOf(cursor);
                if (currentCursorPosition < chipList.size()) {
                    mainPane.getChildren().remove(cursor);
                    mainPane.getChildren().add(currentCursorPosition + 1, cursor);
                    updateViewLayout();
                    repositionScrollpane(cursor);
                }
            }
        });

        scrollPane.addEventHandler(KeyEvent.ANY, Event::consume);

        // add chip when the icons in the chipSelectorPane is pressed
        numberChip.setOnMousePressed(event -> addChip(new NumberWithUnitTerm(NumberWithUnit.ZERO)));
        valueChip.setOnMousePressed(event -> addChip(new ValueTerm(null)));
        plusChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.PLUS)));
        multiplyChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.MULTIPLY)));
        minusChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.MINUS)));
        divideChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.DIVIDE)));
        openParenthesisChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.OPEN_PARENTHESIS)));
        closeParenthesisChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.CLOSE_PARENTHESIS)));

        // manually chipFieldFocus to scrollPane and it's children focused property
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.focusOwnerProperty().addListener((observable1, oldFocusOwner, newFocusOwner) -> {
                    chipFieldFocus.set(scrollPane.isFocused() || isChildFocused(scrollPane));
                });
            }
        });

        // blink the cursor when the ChipField or it's children (chip in the ChipField) received focus
        BooleanProperty cursorVisible = new SimpleBooleanProperty();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), evt -> cursorVisible.set(false)),
                new KeyFrame(Duration.seconds( 1), evt -> cursorVisible.set(true)));
        timeline.setCycleCount(Animation.INDEFINITE);
        chipFieldFocus.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timeline.play();
            } else {
                timeline.stop();
            }
            updateViewLayout();
        });

        cursor.visibleProperty().bind(chipFieldFocus.and(cursorVisible));
        cursor.managedProperty().bind(chipFieldFocus);

        chipSelectorPane.visibleProperty().bind(chipFieldFocus);
        chipSelectorPane.managedProperty().bind(chipFieldFocus);
    }

    private void repositionScrollpane(Node node) {
        // ensure that the ChipField has been redrawn completely otherwise bounds receive from various method call below
        // will be the old bound and the calculation will be wrong
        getParent().layout();

        double chipMinX = node.getBoundsInParent().getMinX();
        double chipMaxX = node.getBoundsInParent().getMaxX();
        double contentWidth = mainPane.getLayoutBounds().getWidth();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double extraWidth = contentWidth - viewportWidth;
        if (extraWidth > 0) {
            if (chipMinX < scrollPane.getHvalue() * extraWidth) {
                scrollPane.setHvalue(chipMinX / extraWidth);
            } else if (chipMaxX > scrollPane.getHvalue() * extraWidth + viewportWidth) {
                scrollPane.setHvalue((chipMaxX - viewportWidth) / extraWidth);
            }
        } else {
            scrollPane.setHvalue(0);
        }
    }

    private boolean isChildFocused(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node.isFocused()) {
                return true;
            } else if (node instanceof Parent) {
                if (isChildFocused((Parent) node)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addChip(Term t) {
        Chip c = addChipUI(t, mainPane.getChildren().indexOf(cursor));
        updateExpression();
        repositionScrollpane(c);
    }

    private Chip addChipUI(Term t, int index) {
        Chip chip;
        if (t instanceof NumberWithUnitTerm) {
            chip = new NumberWithUnitChip(((NumberWithUnitTerm) t).getValue());
            ((NumberWithUnitChip) chip).valueProperty().addListener((observable, oldValue, newValue) -> updateExpression());
        } else if (t instanceof OperatorTerm) {
            chip = new OperatorChip(((OperatorTerm) t).getValue());
            ((OperatorChip) chip).valueProperty().addListener((observable, oldValue, newValue) -> updateExpression());
        } else if (t instanceof ValueTerm) {
            chip = new ProjectValueChip(((ValueTerm) t).getValue(), projectValues);
            ((ProjectValueChip) chip).valueProperty().addListener((observable, oldValue, newValue) -> updateExpression());
        } else {
            throw new IllegalStateException();
        }

        chip.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            int chipPosition = chipList.indexOf(chip);
            mainPane.getChildren().remove(cursor);
            mainPane.getChildren().add(chipPosition+1, cursor);
            updateViewLayout();
            repositionScrollpane(chip);
        });

        chipList.add(index, chip);
        chipMap.put(chip, t);
        mainPane.getChildren().add(index, chip);

        return chip;
    }

    private void removeChip(Chip chip) {
        chipList.remove(chip);
        chipMap.remove(chip);
        mainPane.getChildren().remove(chip);
        updateExpression();
    }

    private void updateExpression() {
        expressionProperty.set(new CustomNumberExpression(chipList.stream().map(Chip::getTerm).collect(Collectors.toList())));
        updateViewLayout();
        updateHilight();
    }

    private void updateViewLayout() {
        if (chipList.isEmpty()) {
            return;
        }

        int currentCursorPosition = mainPane.getChildren().indexOf(cursor);

        HBox.setMargin(chipList.get(0), Insets.EMPTY);
        for (int i=1; i<chipList.size(); i++) {
            Chip previousChip = chipList.get(i - 1);
            Chip currentChip = chipList.get(i);
            if (i == currentCursorPosition && chipFieldFocus.get()) {
                HBox.setMargin(currentChip, Insets.EMPTY);
            } else if ((previousChip instanceof NumberWithUnitChip || previousChip instanceof ProjectValueChip
                    || isOperatorChip(previousChip, OperatorType.RIGHT_UNARY))
                    && (currentChip instanceof NumberWithUnitChip || currentChip instanceof ProjectValueChip
                    || isOperatorChip(currentChip, OperatorType.LEFT_UNARY))) {
                HBox.setMargin(currentChip, Insets.EMPTY);
            } else if (isOperatorChip(previousChip, OperatorType.BINARY)
                    && isOperatorChip(currentChip, OperatorType.BINARY)) {
                HBox.setMargin(currentChip, Insets.EMPTY);
            } else if (isOperatorChip(previousChip, OperatorType.LEFT_UNARY)
                    && isOperatorChip(currentChip, OperatorType.RIGHT_UNARY)) {
                HBox.setMargin(currentChip, Insets.EMPTY);
            } else {
                HBox.setMargin(currentChip, CHIP_FIT_INSETS);
            }
        }
    }

    private boolean isOperatorChip(Chip<?> chip, OperatorType operator) {
        return (chip instanceof OperatorChip) && (((OperatorChip) chip).getTerm().getValue().getType() == operator);
    }

    private void updateHilight() {
        if (!expressionProperty.get().isValid()) {
            scrollPane.setStyle("-fx-effect: dropshadow(gaussian, #ff0000, 5.0 , 0.5, 0.0 , 0.0);");
        } else {
            scrollPane.setStyle("-fx-effect: null;");
        }
    }

    public CustomNumberExpression getExpression() {
        return expressionProperty.get();
    }

    public ReadOnlyObjectProperty<CustomNumberExpression> expressionProperty() {
        return expressionProperty.getReadOnlyProperty();
    }

}
