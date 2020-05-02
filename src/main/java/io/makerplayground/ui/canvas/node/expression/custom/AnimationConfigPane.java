package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.device.shared.*;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.ComplexStringExpression;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class AnimationConfigPane extends GridPane {
    private final ReadOnlyObjectWrapper<AnimatedValue> animatedValue;
    private final ObservableList<ProjectValue> projectValues;
    // create instance of both animation type so that we can retain user setting when switch mode
    private ObjectProperty<ContinuousAnimatedValue> continuousAnimatedValue;
    private ObjectProperty<CategoricalAnimatedValue> categoricalAnimatedValue;

    private Label modeLabel;
    private ComboBox<String> modeCombobox;
    private CurveEditor curveEditor;

    public AnimationConfigPane(AnimatedValue initialValue, boolean allowString, ObservableList<ProjectValue> projectValues) {
        animatedValue = new ReadOnlyObjectWrapper<>();
        this.projectValues = projectValues;

        setHgap(10);
        setVgap(5);

        // initialize internal animation instance
        if (initialValue instanceof ContinuousAnimatedValue) {
            if (allowString) {
                throw new IllegalStateException("continuous animation is not supported for type string");
            }
            continuousAnimatedValue = new SimpleObjectProperty<>((ContinuousAnimatedValue) initialValue);
            categoricalAnimatedValue = new SimpleObjectProperty<>(new NumericCategoricalAnimatedValue());
        } else if (initialValue instanceof NumericCategoricalAnimatedValue) {
            continuousAnimatedValue = new SimpleObjectProperty<>(new ContinuousAnimatedValue());
            categoricalAnimatedValue = new SimpleObjectProperty<>((NumericCategoricalAnimatedValue) initialValue);
        } else if (initialValue instanceof StringCategoricalAnimatedValue) {
            if (!allowString) {
                throw new IllegalStateException("string categorical animation is only supported on type string");
            }
            continuousAnimatedValue = null;
            categoricalAnimatedValue = new SimpleObjectProperty<>((StringCategoricalAnimatedValue) initialValue);
        }

        // add mode selection UI when the pane is used for configure number animation
        if (!allowString) {
            modeLabel = new Label("Mode");
            GridPane.setConstraints(modeLabel, 0, 0);

            modeCombobox = new ComboBox<>(FXCollections.observableArrayList("Continuous", "Categorical"));
            modeCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.equals("Continuous")) {
                    animatedValue.bind(continuousAnimatedValue);
                } else {
                    animatedValue.bind(categoricalAnimatedValue);
                }
                initUI();
            });
            GridPane.setConstraints(modeCombobox, 1, 0);

            getChildren().addAll(modeLabel, modeCombobox);

            // preselect the combobox with the initial value to force redraw for the first time
            if (initialValue instanceof ContinuousAnimatedValue) {
                modeCombobox.getSelectionModel().select("Continuous");
            } else if (initialValue instanceof CategoricalAnimatedValue) {
                modeCombobox.getSelectionModel().select("Categorical");
            }
        } else {
            animatedValue.bind(categoricalAnimatedValue);
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

        NumericChipField startChipField = new NumericChipField(continuousAnimatedValue.get().getStartValue(), projectValues, false);
        startChipField.expressionProperty().addListener((observable, oldValue, newValue) -> continuousAnimatedValue.setValue(continuousAnimatedValue.get().withStartValue(newValue)));
        GridPane.setConstraints(startChipField, 1, 1);

        Label endLabel = new Label("End");
        endLabel.setMinHeight(25);
        GridPane.setConstraints(endLabel, 0, 2);
        GridPane.setValignment(endLabel, VPos.TOP);

        NumericChipField endChipField = new NumericChipField(continuousAnimatedValue.get().getEndValue(), projectValues, false);
        endChipField.expressionProperty().addListener((observable, oldValue, newValue) -> continuousAnimatedValue.setValue(continuousAnimatedValue.get().withEndValue(newValue)));
        GridPane.setConstraints(endChipField, 1, 2);

        Label durationLabel = new Label("Duration");
        durationLabel.setMinHeight(25);
        GridPane.setConstraints(durationLabel, 0, 3);
        GridPane.setValignment(durationLabel, VPos.TOP);

        NumericChipField durationChipField = new NumericChipField(continuousAnimatedValue.get().getDuration(), projectValues, false);
        durationChipField.expressionProperty().addListener((observable, oldValue, newValue) -> continuousAnimatedValue.setValue(continuousAnimatedValue.get().withDuration(newValue)));

        ComboBox<DelayUnit> durationUnitCombobox = new ComboBox<>(FXCollections.observableArrayList(DelayUnit.values()));
        durationUnitCombobox.getSelectionModel().select(continuousAnimatedValue.get().getDelayUnit());
        durationUnitCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                continuousAnimatedValue.setValue(continuousAnimatedValue.get().withDelayUnit(newValue)));

        HBox durationHBox = new HBox();
        durationHBox.setSpacing(5);
        durationHBox.getChildren().addAll(durationChipField, durationUnitCombobox);
        GridPane.setConstraints(durationHBox, 1, 3);

        Label easingLabel = new Label("Easing");
        easingLabel.setMinHeight(25);
        GridPane.setConstraints(easingLabel, 0, 4);
        GridPane.setValignment(easingLabel, VPos.TOP);

        ComboBox<String> easingCombobox = new ComboBox<>(FXCollections.observableArrayList("Linear", "EaseInExpo", "Custom"));
        easingCombobox.getSelectionModel().select(continuousAnimatedValue.get().getEasing().getName());

        curveEditor = new CurveEditor((ContinuousAnimatedValue.BezierEasing) continuousAnimatedValue.get().getEasing());
        curveEditor.easingProperty().addListener((observable, oldValue, newValue) -> continuousAnimatedValue.setValue(continuousAnimatedValue.get().withEasing(newValue)));

        VBox easingVBox = new VBox();
        easingVBox.setSpacing(5);
        easingVBox.getChildren().addAll(easingCombobox, curveEditor);
        GridPane.setConstraints(easingVBox, 1, 4);

        easingCombobox.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            easingVBox.getChildren().remove(curveEditor);
            if (newValue.equals("Linear")) {
                curveEditor = new CurveEditor(ContinuousAnimatedValue.LinearEasing.getInstance());
                continuousAnimatedValue.setValue(continuousAnimatedValue.get().withEasing(ContinuousAnimatedValue.LinearEasing.getInstance()));
            } else if (newValue.equals("EaseInExpo")) {
                curveEditor = new CurveEditor(ContinuousAnimatedValue.EaseInExpo.getInstance());
                continuousAnimatedValue.setValue(continuousAnimatedValue.get().withEasing(ContinuousAnimatedValue.EaseInExpo.getInstance()));
            } else if (newValue.equals("Custom")) {
                curveEditor = new CurveEditor(ContinuousAnimatedValue.CustomEasing.getInstance());
                continuousAnimatedValue.setValue(continuousAnimatedValue.get().withEasing(ContinuousAnimatedValue.CustomEasing.getInstance()));
            } else {
                throw new IllegalStateException("Unsupport easing curve");
            }
            // editable easing can be edited be the CurveEditor
            curveEditor.easingProperty().addListener(((observable1, oldValue1, newValue1) -> {
                continuousAnimatedValue.setValue(continuousAnimatedValue.get().withEasing(curveEditor.easingProperty().get()));
            }));
            easingVBox.getChildren().add(curveEditor);
        }));

        getChildren().addAll(startLabel, startChipField, endLabel, endChipField, durationLabel, durationHBox, easingLabel, easingVBox);
    }

    private void initNumericCategoricalAnimatedUI() {
        getChildren().retainAll(modeLabel, modeCombobox);

        Label valueLabel = new Label("Value");
        valueLabel.setMinHeight(25);
        GridPane.setConstraints(valueLabel, 0, 1);
        GridPane.setValignment(valueLabel, VPos.TOP);

        CategoricalKeyValuePane<CustomNumberExpression> keyValuePane = new CategoricalKeyValuePane<>(categoricalAnimatedValue.get().getKeyValues(), false, projectValues);
        categoricalAnimatedValue.get().getKeyValues().addListener((InvalidationListener) c ->
                categoricalAnimatedValue.set(new NumericCategoricalAnimatedValue((NumericCategoricalAnimatedValue) categoricalAnimatedValue.getValue())));
        GridPane.setConstraints(keyValuePane, 1, 1);

        getChildren().addAll(valueLabel, keyValuePane);
    }

    private void initStringCategoricalAnimatedUI() {
        getChildren().clear();

        Label valueLabel = new Label("Value");
        valueLabel.setMinHeight(25);
        GridPane.setConstraints(valueLabel, 0, 0);
        GridPane.setValignment(valueLabel, VPos.TOP);

        CategoricalKeyValuePane<ComplexStringExpression> keyValuePane = new CategoricalKeyValuePane<>(categoricalAnimatedValue.get().getKeyValues(), true, projectValues);
        categoricalAnimatedValue.get().getKeyValues().addListener((InvalidationListener) c ->
                categoricalAnimatedValue.set(new StringCategoricalAnimatedValue((StringCategoricalAnimatedValue) categoricalAnimatedValue.getValue())));
        GridPane.setConstraints(keyValuePane, 1, 0);

        getChildren().addAll(valueLabel, keyValuePane);
    }

    public ReadOnlyObjectProperty<AnimatedValue> animatedValueProperty() {
        return animatedValue.getReadOnlyProperty();
    }

    private static class CurveEditor extends Pane {
        private static final int DIMENSION = 200;
        private static final int CONTROL_RADIUS = 5;

        private final ReadOnlyObjectWrapper<ContinuousAnimatedValue.Easing> easing;

        private double mouseAnchorX = 0;
        private double mouseAnchorY = 0;

        public CurveEditor(ContinuousAnimatedValue.BezierEasing easing) {
            this.easing = new ReadOnlyObjectWrapper<>(easing);

            Rectangle canvas = new Rectangle();
            canvas.setFill(Color.WHITE);
            canvas.setStroke(Color.web("#cccccc"));
            canvas.setWidth(DIMENSION);
            canvas.setHeight(DIMENSION);

            Circle control1 = new Circle(easing.getC1x() * DIMENSION, DIMENSION - easing.getC1y() * DIMENSION, CONTROL_RADIUS);
            control1.setVisible(easing instanceof ContinuousAnimatedValue.CustomEasing);

            Circle control2 = new Circle(easing.getC2x() * DIMENSION, DIMENSION - easing.getC2y() * DIMENSION, CONTROL_RADIUS);
            control2.setVisible(easing instanceof ContinuousAnimatedValue.CustomEasing);

            control1.setOnMousePressed(event -> {
                if (!event.isPrimaryButtonDown())
                    return;
                mouseAnchorX = event.getSceneX();
                mouseAnchorY = event.getSceneY();
            });
            control1.setOnMouseDragged(event -> {
                if (!event.isPrimaryButtonDown())
                    return;
                control1.setTranslateX(event.getSceneX() - mouseAnchorX);
                control1.setTranslateY(event.getSceneY() - mouseAnchorY);
            });
            control1.setOnMouseReleased(event -> {
                control1.setCenterX(control1.getCenterX() + control1.getTranslateX());
                control1.setCenterY(control1.getCenterY() + control1.getTranslateY());
                control1.setTranslateX(0);
                control1.setTranslateY(0);
                this.easing.set(new ContinuousAnimatedValue.CustomEasing(control1.getCenterX() / DIMENSION, (DIMENSION - control1.getCenterY()) / DIMENSION
                        , control2.getCenterX() / DIMENSION, (DIMENSION - control2.getCenterY()) / DIMENSION));
            });
            control2.setOnMousePressed(event -> {
                if (!event.isPrimaryButtonDown())
                    return;
                mouseAnchorX = event.getSceneX();
                mouseAnchorY = event.getSceneY();
            });
            control2.setOnMouseDragged(event -> {
                if (!event.isPrimaryButtonDown())
                    return;
                control2.setTranslateX(event.getSceneX() - mouseAnchorX);
                control2.setTranslateY(event.getSceneY() - mouseAnchorY);
            });
            control2.setOnMouseReleased(event -> {
                control2.setCenterX(control2.getCenterX() + control2.getTranslateX());
                control2.setCenterY(control2.getCenterY() + control2.getTranslateY());
                control2.setTranslateX(0);
                control2.setTranslateY(0);
                this.easing.set(new ContinuousAnimatedValue.CustomEasing(control1.getCenterX() / DIMENSION, (DIMENSION - control1.getCenterY()) / DIMENSION
                        , control2.getCenterX() / DIMENSION, (DIMENSION - control2.getCenterY()) / DIMENSION));
            });

            CubicCurveTo cubicCurveTo = new CubicCurveTo();
            cubicCurveTo.controlX1Property().bind(control1.centerXProperty());
            cubicCurveTo.controlY1Property().bind(control1.centerYProperty());
            cubicCurveTo.controlX2Property().bind(control2.centerXProperty());
            cubicCurveTo.controlY2Property().bind(control2.centerYProperty());
            cubicCurveTo.setX(DIMENSION);
            cubicCurveTo.setY(0);

            Path path = new Path();
            path.setStrokeWidth(3);
            path.getElements().addAll(new MoveTo(0, DIMENSION), cubicCurveTo);

            getChildren().addAll(canvas, path, control1, control2);
        }

        public ReadOnlyObjectProperty<ContinuousAnimatedValue.Easing> easingProperty() {
            return easing.getReadOnlyProperty();
        }
    }

    private class CategoricalKeyValuePane<T extends Expression> extends VBox {
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
                chipField.expressionProperty().addListener(((observable, oldValue, newValue) -> {
                    keyValue.withValue((T) newValue);
                    animatedValue.setValue(categoricalAnimatedValue);
                }));

                Label delayLabel = new Label("Delay");
                delayLabel.setMinHeight(25);

                NumericChipField delayChipField = new NumericChipField(keyValue.getDelay(), projectValues, false);
                delayChipField.expressionProperty().addListener(((observable, oldValue, newValue) -> {
                    keyValue.setDelay(newValue);
                    animatedValue.setValue(categoricalAnimatedValue);
                }));

                ComboBox<DelayUnit> delayUnitComboBox = new ComboBox<>(FXCollections.observableArrayList(DelayUnit.values()));
                delayUnitComboBox.getSelectionModel().select(keyValue.getDelayUnit());
                delayUnitComboBox.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
                    keyValue.setDelayUnit(newValue);
                    animatedValue.setValue(categoricalAnimatedValue);
                }));

                ImageView addImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/add-expression.png")));
                addImageView.setFitHeight(25);
                addImageView.setFitWidth(25);
                addImageView.setPreserveRatio(true);
                addImageView.setOnMousePressed(event -> {
                    if (allowString) {
                        values.add(values.indexOf(keyValue) + 1, new CategoricalAnimatedValue.AnimatedKeyValue<T>((T) ComplexStringExpression.INVALID, CustomNumberExpression.INVALID, DelayUnit.SECOND));
                    } else {
                        values.add(values.indexOf(keyValue) + 1, new CategoricalAnimatedValue.AnimatedKeyValue<T>((T) CustomNumberExpression.INVALID, CustomNumberExpression.INVALID, DelayUnit.SECOND));
                    }
                    animatedValue.setValue(categoricalAnimatedValue);
                    redraw();
                });

                ImageView removeImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/remove-expression.png")));
                removeImageView.setFitHeight(25);
                removeImageView.setFitWidth(25);
                removeImageView.setPreserveRatio(true);
                removeImageView.setOnMousePressed(event -> {
                    values.remove(keyValue);
                    animatedValue.setValue(categoricalAnimatedValue);
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
                    animatedValue.setValue(categoricalAnimatedValue);
                    redraw();
                });
                getChildren().addAll(addImageView);
            }
        }
    }
}
