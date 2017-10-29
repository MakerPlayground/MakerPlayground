package io.makerplayground.ui.canvas;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.expression.*;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ChipField extends ScrollPane {
    private final Expression expression;
    private final SelectionGroup<Chip> selectionGroup = new SelectionGroup<>();

    private final HBox mainPane = new HBox();
    private final Text cursor = new Text();

    public ChipField(Expression expression) {
        this.expression = expression;
        initView();
        initEvent();
    }

    private void initView() {
        mainPane.setSpacing(5);
        mainPane.setMinSize(300, 30);
        mainPane.setAlignment(Pos.CENTER_LEFT);
        mainPane.setPadding(new Insets(0, 10, 0, 10));
        mainPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(10), BorderStroke.THIN)));

        cursor.setText("I");
        mainPane.getChildren().add(cursor);

        // initialize chip based on expression
        expression.getTerms().forEach(this::addTerm);

        // add/remove chip when expression changed
        expression.getTerms().addListener((ListChangeListener<? super Term>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    throw new UnsupportedOperationException();
                } else if (c.wasUpdated()) {
                    throw new UnsupportedOperationException();
                } else {
                    for (Term removedItem : c.getRemoved()) {
                        removeTerm(removedItem, c.getFrom());
                    }
                    for (Term addedItem : c.getAddedSubList()) {
                        addTerm(addedItem, c.getFrom());
                    }
                }
            }
        });

        setPrefSize(300, 30);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        setFocusTraversable(true);
        setContent(mainPane);
    }

    private void initEvent() {
        // deselect all chips if select at blank space
        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> selectionGroup.deselect());

        // move cursor with left/right arrow key (an event handler is needed as we don't want to interfere TextField in Chip)
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.LEFT) {
                decreaseCursor();
            } else if (event.getCode() == KeyCode.RIGHT) {
                advanceCursor();
            }
        });

        // delete selected chip with del key (an event filter is needed as a TextField in Chip can consume KeyEvent)
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DELETE) {
                selectionGroup.getSelected().forEach(this::removeChip);
            }
        });
    }

    // Add new chip to the current cursor position of ChipField. Change will also be reflected to the underlying expression.
    public void addTerm(Term t) {
        expression.getTerms().add(mainPane.getChildren().indexOf(cursor), t);
    }

    // Add new chip when underlying expression has changed
    private void addTerm(Term t, int index) {
        Chip chip = null;
        if (t instanceof NumberWithUnitTerm) {
            chip = new NumberWithUnitChip((NumberWithUnit) t.getValue());
        } else if (t instanceof StringTerm) {
            chip = new StringChip((String) t.getValue());
        } else if (t instanceof ValueTerm) {
            chip = OperatorChip.getInstance((ChipOperator) t.getValue());
        } else {
            throw new IllegalStateException();
        }

        mainPane.getChildren().add(index, chip);
        selectionGroup.getSelectable().add(chip);
    }

    // Remove chip when underlying expression has changed
    private void removeTerm(Term t, int index) {
        Chip removedChip = null;

        int cursorIndex = mainPane.getChildren().indexOf(cursor);
        if (cursorIndex <= index) {
            removedChip = (Chip) mainPane.getChildren().remove(index + 1);
        } else {
            removedChip = (Chip) mainPane.getChildren().remove(index);
        }

        mainPane.getChildren().remove(removedChip);
        selectionGroup.getSelectable().remove(removedChip);
    }

    // Remove chip when press delete key or the delete button
    private void removeChip(Chip c) {
        int indexToBeRemoved = mainPane.getChildren().indexOf(c);
        int cursorIndex = mainPane.getChildren().indexOf(cursor);

        if (cursorIndex <= indexToBeRemoved) {
            expression.getTerms().remove(indexToBeRemoved - 1);
        } else {
            expression.getTerms().remove(indexToBeRemoved);
        }
    }

    private void advanceCursor() {
        int index = mainPane.getChildren().indexOf(cursor);
        if (index != mainPane.getChildren().size() - 1) {
            mainPane.getChildren().remove(cursor);
            mainPane.getChildren().add(index + 1, cursor);
        }
    }

    private void decreaseCursor() {
        int index = mainPane.getChildren().indexOf(cursor);
        if (index != 0) {
            mainPane.getChildren().remove(cursor);
            mainPane.getChildren().add(index - 1, cursor);
        }
    }
}
