package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.term.*;
import io.makerplayground.ui.canvas.node.SelectionGroup;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ChipField extends ScrollPane {
    @FXML private HBox mainPane;
    private ChipSelectorPopover popOver;

    private final List<ProjectValue> projectValues;
    private final ReadOnlyObjectWrapper<CustomNumberExpression> expressionProperty;

    private final List<Chip> chipList = new ArrayList<>();
    private final Map<Chip, Term> chipMap = new HashMap<>();

    private final SelectionGroup<Chip> selectionGroup = new SelectionGroup<>();

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

        // initialize chip based on expression
        List<Term> listTerm = expressionProperty.get().getTerms();
        for (int i=0; i<listTerm.size(); i++) {
            addChipUI(listTerm.get(i), i);
        }

        // initialize popup window to add new chip
        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (popOver == null || !popOver.isShowing()) {
                popOver = new ChipSelectorPopover();
                popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
                popOver.setOnChipSelected(this::addChip);
                popOver.show(ChipField.this);
            }
        });

        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            selectionGroup.deselect();
        });

        updateViewLayout();
        updateHilight();
    }

    private void initEvent() {
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DELETE) {
                if (!selectionGroup.getSelected().isEmpty()) {
                    Chip selectedChip = selectionGroup.getSelected().get(0);
                    selectionGroup.setSelected(chipList.get(Math.floorMod(chipList.indexOf(selectedChip) + 1, chipList.size())));
                    removeChip(selectedChip);
                }
            }
        });

        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.LEFT) {
                if (!selectionGroup.getSelected().isEmpty()) {
                    Chip selectedChip = selectionGroup.getSelected().get(0);
                    selectionGroup.setSelected(chipList.get(Math.floorMod(chipList.indexOf(selectedChip) - 1, chipList.size())));
                }
            } else if (event.getCode() == KeyCode.RIGHT) {
                if (!selectionGroup.getSelected().isEmpty()) {
                    Chip selectedChip = selectionGroup.getSelected().get(0);
                    selectionGroup.setSelected(chipList.get(Math.floorMod(chipList.indexOf(selectedChip) + 1, chipList.size())));
                }
            }
        });

        selectionGroup.getSelected().addListener((InvalidationListener) observable -> {
            if (!selectionGroup.getSelected().isEmpty()) {
                repositionScrollpane(selectionGroup.getSelected().get(0));
            }
        });
    }

    private void repositionScrollpane(Chip c) {
        double chipMinX = c.getBoundsInParent().getMinX();
        double chipMaxX = c.getBoundsInParent().getMaxX();
        double contentWidth = mainPane.getLayoutBounds().getWidth();
        double viewportWidth = getViewportBounds().getWidth();
        double extraWidth = contentWidth - viewportWidth;
        if (extraWidth > 0) {
            if (chipMinX < getHvalue() * extraWidth) {
                setHvalue(chipMinX / extraWidth);
            } else if (chipMaxX > getHvalue() * extraWidth + viewportWidth) {
                setHvalue((chipMaxX - viewportWidth) / extraWidth);
            }
        } else {
            setHvalue(0);
        }
    }

    private void addChip(Term t) {
        Chip c;
        if (selectionGroup.getSelected().isEmpty()) {
            c = addChipUI(t, chipList.size());
        } else {
            c = addChipUI(t, chipList.indexOf(selectionGroup.getSelected().get(0)) + 1);
        }
        updateExpression();
        selectionGroup.setSelected(c);
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
        chipList.add(index, chip);
        chipMap.put(chip, t);
        selectionGroup.getSelectable().add(chip);
        mainPane.getChildren().add(index, chip);
        return chip;
    }

    private void removeChip(Chip chip) {
        chipList.remove(chip);
        chipMap.remove(chip);
        selectionGroup.getSelectable().remove(chip);
        mainPane.getChildren().remove(chip);
        updateExpression();
    }

    private void updateExpression() {
        expressionProperty.set(new CustomNumberExpression(expressionProperty.get().getMinValue(), expressionProperty.get().getMaxValue()
                , chipList.stream().map(Chip::getTerm).collect(Collectors.toList())));
        updateViewLayout();
        updateHilight();
    }

    private void updateViewLayout() {
        if (chipList.isEmpty()) {
            return;
        }

        HBox.setMargin(chipList.get(0), Insets.EMPTY);
        // Add margin between chips that aren't fit together
        for (int i=1; i<chipList.size(); i++) {
            Chip previousChip = chipList.get(i-1);
            Chip currentChip = chipList.get(i);
            if ((previousChip instanceof NumberWithUnitChip || previousChip instanceof ProjectValueChip
                    || (previousChip instanceof OperatorChip && ((OperatorChip) previousChip).getTerm().getValue().getType() == OperatorType.RIGHT_UNARY))
                    && (currentChip instanceof NumberWithUnitChip || currentChip instanceof ProjectValueChip
                    || (currentChip instanceof OperatorChip && ((OperatorChip) currentChip).getTerm().getValue().getType() == OperatorType.LEFT_UNARY))) {
                HBox.setMargin(currentChip, new Insets(0, 0, 0, 10));
            } else {
                HBox.setMargin(currentChip, Insets.EMPTY);
            }
        }
    }

    private void updateHilight() {
        if (!expressionProperty.get().isValid()) {
            setStyle("-fx-effect: dropshadow(gaussian, #ff0000, 5.0 , 0.5, 0.0 , 0.0);");
        } else {
            setStyle("-fx-effect: null;");
        }
    }

    public CustomNumberExpression getExpression() {
        return expressionProperty.get();
    }

    public ReadOnlyObjectProperty<CustomNumberExpression> expressionProperty() {
        return expressionProperty.getReadOnlyProperty();
    }

}
