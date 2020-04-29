package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.device.shared.*;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.ComplexStringExpression;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AnimationConfigPane extends GridPane {
    private final ReadOnlyObjectWrapper<AnimatedValue> animatedValue;
    private final ObservableList<ProjectValue> projectValues;
    // create instance of both animation type so that we can retain user setting when switch mode
    private ContinuousAnimatedValue continuousAnimatedValue;
    private CategoricalAnimatedValue categoricalAnimatedValue;

    private Label modeLabel;
    private ComboBox<String> modeCombobox;

    public AnimationConfigPane(AnimatedValue initialValue, boolean allowString, ObservableList<ProjectValue> projectValues) {
        animatedValue = new ReadOnlyObjectWrapper<>(initialValue);
        this.projectValues = projectValues;

        setHgap(10);
        setVgap(5);

        // initialize internal animation instance
        if (animatedValue.get() instanceof ContinuousAnimatedValue) {
            if (allowString) {
                throw new IllegalStateException("continuous animation is not supported for type string");
            }
            continuousAnimatedValue = (ContinuousAnimatedValue) animatedValue.get();
            categoricalAnimatedValue = new NumericCategoricalAnimatedValue();
        } else if (animatedValue.get() instanceof NumericCategoricalAnimatedValue) {
            continuousAnimatedValue = new ContinuousAnimatedValue();
            categoricalAnimatedValue = (NumericCategoricalAnimatedValue) animatedValue.get();
        } else if (animatedValue.get() instanceof StringCategoricalAnimatedValue) {
            if (!allowString) {
                throw new IllegalStateException("string categorical animation is only supported on type string");
            }
            continuousAnimatedValue = null;
            categoricalAnimatedValue = (StringCategoricalAnimatedValue) animatedValue.get();
        }

        // add mode selection UI when the pane is used for configure number animation
        if (!allowString) {
            modeLabel = new Label("Mode");
            GridPane.setConstraints(modeLabel, 0, 0);

            modeCombobox = new ComboBox<>(FXCollections.observableArrayList("Continuous", "Categorical"));
            modeCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.equals("Continuous")) {
                    animatedValue.set(continuousAnimatedValue);
                } else {
                    animatedValue.set(categoricalAnimatedValue);
                }
                initUI();
            });
            GridPane.setConstraints(modeCombobox, 1, 0);

            getChildren().addAll(modeLabel, modeCombobox);

            // preselect the combobox with the initial value to force redraw for the first time
            if (animatedValue.get() instanceof ContinuousAnimatedValue) {
                modeCombobox.getSelectionModel().select("Continuous");
            } else if (animatedValue.get() instanceof CategoricalAnimatedValue) {
                modeCombobox.getSelectionModel().select("Categorical");
            }
        } else {
            initUI();
        }
    }

    private void initUI() {
        if (animatedValue.get() instanceof ContinuousAnimatedValue) {
            initContinuousAnimatedUI();
        } else if (animatedValue.get() instanceof NumericCategoricalAnimatedValue) {
            initNumericCategoricalAnimatedUI();
        } else if (animatedValue.get() instanceof StringCategoricalAnimatedValue) {
            initStringCategoricalAnimatedUI();
        }
    }

    private void initContinuousAnimatedUI() {
        getChildren().retainAll(modeLabel, modeCombobox);

        Label startLabel = new Label("Start");
        startLabel.setMinHeight(25);
        GridPane.setConstraints(startLabel, 0, 1);
        GridPane.setValignment(startLabel, VPos.TOP);

        NumericChipField startChipField = new NumericChipField(continuousAnimatedValue.getStartValue(), projectValues, false);
        startChipField.expressionProperty().addListener(((observable, oldValue, newValue) -> continuousAnimatedValue.setStartValue(newValue)));
        GridPane.setConstraints(startChipField, 1, 1);

        Label endLabel = new Label("End");
        endLabel.setMinHeight(25);
        GridPane.setConstraints(endLabel, 0, 2);
        GridPane.setValignment(endLabel, VPos.TOP);

        NumericChipField endChipField = new NumericChipField(continuousAnimatedValue.getEndValue(), projectValues, false);
        endChipField.expressionProperty().addListener(((observable, oldValue, newValue) -> continuousAnimatedValue.setEndValue(newValue)));
        GridPane.setConstraints(endChipField, 1, 2);

        Label durationLabel = new Label("Duration");
        durationLabel.setMinHeight(25);
        GridPane.setConstraints(durationLabel, 0, 3);
        GridPane.setValignment(durationLabel, VPos.TOP);

        NumericChipField durationChipField = new NumericChipField(continuousAnimatedValue.getDuration(), projectValues, false);
        durationChipField.expressionProperty().addListener(((observable, oldValue, newValue) -> continuousAnimatedValue.setDuration(newValue)));

        ComboBox<DelayUnit> durationUnitCombobox = new ComboBox<>(FXCollections.observableArrayList(DelayUnit.values()));
        durationUnitCombobox.getSelectionModel().select(continuousAnimatedValue.getDelayUnit());
        durationUnitCombobox.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> continuousAnimatedValue.setDelayUnit(newValue)));

        HBox durationHBox = new HBox();
        durationHBox.setSpacing(5);
        durationHBox.getChildren().addAll(durationChipField, durationUnitCombobox);
        GridPane.setConstraints(durationHBox, 1, 3);

        Label easingLabel = new Label("Easing");
        GridPane.setConstraints(easingLabel, 0, 4);

        ComboBox<String> easingCombobox = new ComboBox<>(FXCollections.observableArrayList("Linear", "Bezier"));
        easingCombobox.getSelectionModel().select(continuousAnimatedValue.getEasing().getName());
        easingCombobox.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.equals("Linear")) {
                continuousAnimatedValue.setEasing(ContinuousAnimatedValue.LinearEasing.getInstance());
            } else if (newValue.equals("Bezier")) {
                continuousAnimatedValue.setEasing(ContinuousAnimatedValue.BezierEasing.getInstance(0.8, 0, 1, 0.2));
            }
        }));
        GridPane.setConstraints(easingCombobox, 1, 4);

        getChildren().addAll(startLabel, startChipField, endLabel, endChipField, durationLabel, durationHBox, easingLabel, easingCombobox);
    }

    private void initNumericCategoricalAnimatedUI() {
        getChildren().retainAll(modeLabel, modeCombobox);

        Label valueLabel = new Label("Value");
        valueLabel.setMinHeight(25);
        GridPane.setConstraints(valueLabel, 0, 1);
        GridPane.setValignment(valueLabel, VPos.TOP);

        CategoricalKeyValuePane<CustomNumberExpression> keyValuePane = new CategoricalKeyValuePane<>(categoricalAnimatedValue.getKeyValues(), false, projectValues);
        GridPane.setConstraints(keyValuePane, 1, 1);

        getChildren().addAll(valueLabel, keyValuePane);
    }

    private void initStringCategoricalAnimatedUI() {
        getChildren().clear();

        Label valueLabel = new Label("Value");
        valueLabel.setMinHeight(25);
        GridPane.setConstraints(valueLabel, 0, 0);
        GridPane.setValignment(valueLabel, VPos.TOP);

        CategoricalKeyValuePane<ComplexStringExpression> keyValuePane = new CategoricalKeyValuePane<>(categoricalAnimatedValue.getKeyValues(), true, projectValues);
        GridPane.setConstraints(keyValuePane, 1, 0);

        getChildren().addAll(valueLabel, keyValuePane);
    }

    private static class CategoricalKeyValuePane<T extends Expression> extends VBox {
        private final ObservableList<CategoricalAnimatedValue.AnimatedKeyValue<T>> values;
        private final boolean allowString;
        private final ObservableList<ProjectValue> projectValues;

        public CategoricalKeyValuePane(ObservableList<CategoricalAnimatedValue.AnimatedKeyValue<T>> values, boolean allowString, ObservableList<ProjectValue> projectValues) {
            this.values = values;
            this.allowString = allowString;
            this.projectValues = projectValues;
            setSpacing(5);
            redraw();
        }

        private void redraw() {
            getChildren().clear();

            for (var keyValue : values) {
                ChipField chipField;
                if (allowString) {
                    chipField = new StringChipField((ComplexStringExpression) keyValue.getValue(), projectValues, false);
                } else {
                    chipField = new NumericChipField((CustomNumberExpression) keyValue.getValue(), projectValues, false);
                }
                chipField.expressionProperty().addListener(((observable, oldValue, newValue) -> keyValue.setValue((T) newValue)));

                Label delayLabel = new Label("Delay");
                delayLabel.setMinHeight(25);

                NumericChipField delayChipField = new NumericChipField(keyValue.getDelay(), projectValues, false);
                delayChipField.expressionProperty().addListener(((observable, oldValue, newValue) -> keyValue.setDelay(newValue)));

                ComboBox<DelayUnit> delayUnitComboBox = new ComboBox<>(FXCollections.observableArrayList(DelayUnit.values()));
                delayUnitComboBox.getSelectionModel().select(keyValue.getDelayUnit());
                delayUnitComboBox.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> keyValue.setDelayUnit(newValue)));

                ImageView addImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/add-expression.png")));
                addImageView.setFitHeight(25);
                addImageView.setFitWidth(25);
                addImageView.setPreserveRatio(true);
                addImageView.setOnMousePressed(event -> {
                    if (allowString) {
                        values.add(values.indexOf(keyValue), new CategoricalAnimatedValue.AnimatedKeyValue<T>((T) ComplexStringExpression.INVALID, CustomNumberExpression.INVALID, DelayUnit.SECOND));
                    } else {
                        values.add(values.indexOf(keyValue), new CategoricalAnimatedValue.AnimatedKeyValue<T>((T) CustomNumberExpression.INVALID, CustomNumberExpression.INVALID, DelayUnit.SECOND));
                    }
                    redraw();
                });

                ImageView removeImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/remove-expression.png")));
                removeImageView.setFitHeight(25);
                removeImageView.setFitWidth(25);
                removeImageView.setPreserveRatio(true);
                removeImageView.setOnMousePressed(event -> {
                    values.remove(keyValue);
                    redraw();
                });

                HBox row = new HBox();
                row.setAlignment(Pos.TOP_LEFT);
                row.setSpacing(5);
                row.getChildren().addAll(chipField, delayLabel, delayChipField, delayUnitComboBox, addImageView, removeImageView);
                getChildren().addAll(row);
            }

            if (values.isEmpty()) {
                ImageView addImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/add-expression.png")));
                addImageView.setFitHeight(25);
                addImageView.setFitWidth(25);
                addImageView.setPreserveRatio(true);
                addImageView.setOnMousePressed(event -> {
                    if (allowString) {
                        values.add(new CategoricalAnimatedValue.AnimatedKeyValue<T>((T) ComplexStringExpression.INVALID, CustomNumberExpression.INVALID, DelayUnit.SECOND));
                    } else {
                        values.add(new CategoricalAnimatedValue.AnimatedKeyValue<T>((T) CustomNumberExpression.INVALID, CustomNumberExpression.INVALID, DelayUnit.SECOND));
                    }
                    redraw();
                });
                getChildren().addAll(addImageView);
            }
        }
    }
}
