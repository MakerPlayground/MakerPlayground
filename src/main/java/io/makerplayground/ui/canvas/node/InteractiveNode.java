package io.makerplayground.ui.canvas.node;

import io.makerplayground.ui.canvas.InteractivePane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public abstract class InteractiveNode extends Group implements Selectable {

    protected final InteractivePane interactivePane;
    private final BooleanProperty select = new SimpleBooleanProperty();

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    private boolean hasDragged;

    public InteractiveNode(InteractivePane interactivePane) {
        this.interactivePane = interactivePane;

        // allow this node to be selected
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.isPrimaryButtonDown()) {
                select.set(!select.get());
            }
        });

        // show/hide hi-light when this scene is selected/deselected
        select.addListener((observable, oldValue, newValue) -> showHilight(newValue));
    }

    protected void makeMovable(Node n) {
        // allow node to be dragged
        n.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            // allow dragging only when the left button is pressed
            if (!event.isPrimaryButtonDown())
                return;

            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            translateAnchorX = getTranslateX();
            translateAnchorY = getTranslateY();
        });
        n.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            // allow dragging only when the left button is pressed
            if (!event.isPrimaryButtonDown())
                return;

            double deltaX = ((event.getSceneX() - mouseAnchorX) / interactivePane.getScale());
            double deltaY = ((event.getSceneY() - mouseAnchorY) / interactivePane.getScale());

            // adjust deltaX/Y if needed, to avoid negative coordinate
            double newMinX = getBoundsInParent().getMinX() - getTranslateX() + (translateAnchorX + deltaX);
            double newMinY = getBoundsInParent().getMinY() - getTranslateY() + (translateAnchorY + deltaY);
            if (newMinX < 0) {
                deltaX = deltaX - newMinX;
            }
            if (newMinY < 0) {
                deltaY = deltaY - newMinY;
            }

            setTranslateX(translateAnchorX + deltaX);
            setTranslateY(translateAnchorY + deltaY);

            hasDragged = true;
            event.consume();

            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.MOVED, null, null
                    , getBoundsInParent().getMinX(), getBoundsInParent().getMinY()));
        });
        n.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (hasDragged) {
                // we consume this event so that the property window will not be opened if we happened to be dragging
                // this node and release our mouse
                event.consume();
            }
            hasDragged = false;
        });
    }

    /**
     * Show green hilight when node is selected otherwise hilight in red if error is found in the node
     * @param b true to show green hilight otherwise the node will be hilighted in red/black according to {@link this.isError}
     */
    protected void showHilight(boolean b) {
        if (b) {
            setStyle("-fx-effect: dropshadow(gaussian, #5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
        } else if (isError()) {
            setStyle("-fx-effect: dropshadow(gaussian, #c25a5a, 15.0 , 0.5, 0.0 , 0.0);");
        } else {
            setStyle("-fx-effect: dropshadow(gaussian, derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
        }
    }

    @Override
    public BooleanProperty selectedProperty() {
        return select;
    }

    @Override
    public boolean isSelected() {
        return select.get();
    }

    @Override
    public void setSelected(boolean b) {
        select.set(b);
    }

    protected abstract boolean isError();
}

