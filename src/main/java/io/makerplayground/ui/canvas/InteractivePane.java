package io.makerplayground.ui.canvas;

import io.makerplayground.project.NodeElement;
import io.makerplayground.ui.canvas.node.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class InteractivePane extends ScrollPane {
    private final Pane content = new Pane();
    private final Group group = new Group();
    private DoubleProperty scale;

    private final SelectionGroup<InteractiveNode> selectionGroup = new SelectionGroup<>();

    private final Line guideLine = new Line();
    private final Rectangle groupSelectionArea = new Rectangle();
    private double groupSelectionStartX, groupSelectionStartY;

    private NodeElement sourceNode; // TODO: leak model into view
    private NodeElement destNode;   // TODO: leak model into view

    private double mouseX, mouseY;

    public InteractivePane() {
        // a pane to add content into
        content.setStyle("-fx-background-color: #dbdbdb;");

        // wrap content in a group to scroll based on visual bounds according to ScrollPane's javadoc
        group.getChildren().add(content);
        setContent(group);

        // resize the content pane when the window is resized (viewport's bound of the ScrollPane changed)
        viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getWidth() > content.getPrefWidth()) {
                content.setPrefWidth(newValue.getWidth());
            }
            if (newValue.getHeight() > content.getPrefHeight()) {
                content.setPrefHeight(newValue.getHeight());
            }
        });

        // group's layout bound will be larger when a child of content pane is dragged out of it's area so we keep track
        // of this bound and enlarge our content pane respectively
        group.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            content.setPrefSize(newValue.getWidth() / scale.doubleValue()
                    , newValue.getHeight() / scale.doubleValue());
        });

        scale = new SimpleDoubleProperty(1);
        // when scale value changed, we scale content and move scroll position to maintain center
        scale.addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == 0 || !Double.isFinite(newValue.doubleValue())) {
                throw new IllegalStateException("Scale can't be 0, Infinity or NaN!!!");
            }

            Point2D scrollOffset = figureScrollOffset(group, this);
            double oldScaleFactor = content.getScaleX();

            content.setScaleX(scale.get());
            content.setScaleY(scale.get());

            repositionScroller(group, this, scale.get() / oldScaleFactor, scrollOffset);
        });

        guideLine.setVisible(false);
        // guideLine is always bring to front when visible so we must make it transparent otherwise it will block
        // MOUSE_DRAGGED event of the destination port and prevent us from creating connection
        guideLine.setMouseTransparent(true);
        guideLine.setStrokeWidth(3.25);
        guideLine.setStyle("-fx-stroke: #313644;");
        content.getChildren().add(guideLine);

        groupSelectionArea.setVisible(false);
        groupSelectionArea.setStroke(Color.RED);
        groupSelectionArea.setStrokeWidth(1);
        groupSelectionArea.setFill(Color.color(1, 0, 0, 0.5));
        content.getChildren().add(groupSelectionArea);

        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            updateMousePosition(event);
            groupSelectionStartX = mouseX;
            groupSelectionStartY = mouseY;
            groupSelectionArea.setX(mouseX);
            groupSelectionArea.setY(mouseY);
            groupSelectionArea.setWidth(0);
            groupSelectionArea.setHeight(0);
            groupSelectionArea.toFront();
            groupSelectionArea.setVisible(true);

            // deselect when left click at blank space in the canvas while the shift key is not pressed
            if (event.isPrimaryButtonDown() && !event.isShiftDown()) {
                selectionGroup.deselect();
            }
        });

        addEventHandler(MouseEvent.DRAG_DETECTED, event -> startFullDrag());

        addEventHandler(MouseEvent.MOUSE_MOVED, this::updateMousePosition);

        addEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, event -> {
            updateMousePosition(event);
            guideLine.setEndX(mouseX);
            guideLine.setEndY(mouseY);

            if (mouseX > groupSelectionStartX) {
                groupSelectionArea.setX(groupSelectionStartX);
                groupSelectionArea.setWidth(mouseX - groupSelectionStartX);
            } else {
                groupSelectionArea.setX(mouseX);
                groupSelectionArea.setWidth(groupSelectionStartX - mouseX);
            }
            if (mouseY > groupSelectionStartY) {
                groupSelectionArea.setY(groupSelectionStartY);
                groupSelectionArea.setHeight(mouseY - groupSelectionStartY);
            } else {
                groupSelectionArea.setY(mouseY);
                groupSelectionArea.setHeight(groupSelectionStartY - mouseY);
            }
            event.consume();
        });

        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            updateMousePosition(event);
            guideLine.setVisible(false);

            if (groupSelectionArea.isVisible()) {
                groupSelectionArea.setVisible(false);

                if (!event.isShiftDown()) {
                    selectionGroup.deselect();
                }
                selectionGroup.setMultipleSelection(true);
                for (Node node : content.getChildren()) {
                    if (node instanceof InteractiveNode) {
                        if (groupSelectionArea.getBoundsInParent().contains(node.getBoundsInParent())) {
                            ((InteractiveNode) node).setSelected(true);
                        }
                    }
                }
                if (!event.isShiftDown()) {
                    selectionGroup.setMultipleSelection(false);
                }
            }
        });
    }

    // https://stackoverflow.com/questions/16680295/javafx-correct-scaling/16682180#16682180
    private Point2D figureScrollOffset(Node scrollContent, ScrollPane scroller) {
        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        double hScrollProportion = (scroller.getHvalue() - scroller.getHmin()) / (scroller.getHmax() - scroller.getHmin());
        double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);

        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        double vScrollProportion = (scroller.getVvalue() - scroller.getVmin()) / (scroller.getVmax() - scroller.getVmin());
        double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);

        return new Point2D(scrollXOffset, scrollYOffset);
    }

    // https://stackoverflow.com/questions/16680295/javafx-correct-scaling/16682180#16682180
    private void repositionScroller(Node scrollContent, ScrollPane scroller, double scaleFactor, Point2D scrollOffset) {
        double scrollXOffset = scrollOffset.getX();
        double scrollYOffset = scrollOffset.getY();
        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        if (extraWidth > 0) {
            double halfWidth = scroller.getViewportBounds().getWidth() / 2 ;
            double newScrollXOffset = (scaleFactor - 1) *  halfWidth + scaleFactor * scrollXOffset;
            scroller.setHvalue(scroller.getHmin() + newScrollXOffset * (scroller.getHmax() - scroller.getHmin()) / extraWidth);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        if (extraHeight > 0) {
            double halfHeight = scroller.getViewportBounds().getHeight() / 2 ;
            double newScrollYOffset = (scaleFactor - 1) * halfHeight + scaleFactor * scrollYOffset;
            scroller.setVvalue(scroller.getVmin() + newScrollYOffset * (scroller.getVmax() - scroller.getVmin()) / extraHeight);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
    }

    // listener attaches to every node to automatically move the scrollbar when it moves out of viewport
    private final EventHandler<InteractiveNodeEvent> nodeMovedHandler =  (event) -> {
        double viewportWidth = getViewportBounds().getWidth();
        double viewportHeight = getViewportBounds().getHeight();
        double extraWidth = group.getLayoutBounds().getWidth() - viewportWidth;
        double extraHeight = group.getLayoutBounds().getHeight() - viewportHeight;

        double left = getHvalue() * extraWidth; // getViewportBounds().getMinX()
        double right = left + viewportWidth;
        double top = getVvalue() * extraHeight;
        double bottom = top + viewportHeight;

        Bounds newValue = event.getSource().getBoundsInParent();

        if (left > newValue.getMinX() * scale.get()) {
            setHvalue(newValue.getMinX() * scale.get() / extraWidth);
        } else if (right < newValue.getMaxX() * scale.get()) {
            setHvalue((newValue.getMaxX() * scale.get() - viewportWidth) / extraWidth);
        } else if (top > newValue.getMinY() * scale.get()) {
            setVvalue(newValue.getMinY() * scale.get() / extraHeight);
        } else if (bottom < newValue.getMaxY() * scale.get()) {
            setVvalue((newValue.getMaxY() * scale.get() - viewportHeight) / extraHeight);
        }

        double deltaX = event.getX();
        double deltaY = event.getY();

//        // prevent node from moving out of the canvas
//        for (Node interactiveNode : selectionGroup.getSelected()) {
//            Bounds bounds = interactiveNode.getBoundsInParent();
//            if (bounds.getMinX() + deltaX < 0) {
//                deltaX = -bounds.getMinX();
//            }
//            if (bounds.getMinY() + deltaY < 0) {
//                deltaY = -bounds.getMinY();
//            }
//        }

        for (InteractiveNode interactiveNode : selectionGroup.getSelected()) {
            interactiveNode.moveNode(deltaX, deltaY);
        }
    };

    public void addChildren(InteractiveNode n) {
        content.getChildren().add(n);
        // add node to the selection group
        selectionGroup.getSelectable().add(n);
        // auto scroll when child leave current viewport
        n.addEventHandler(InteractiveNodeEvent.MOVED, nodeMovedHandler);
        // show guide line when connecting the nodes
        n.addEventFilter(InteractiveNodeEvent.CONNECTION_BEGIN, event -> {
            guideLine.setStartX(event.getX());
            guideLine.setStartY(event.getY());
            guideLine.setEndX(event.getX());
            guideLine.setEndY(event.getY());
            guideLine.toFront();
            guideLine.setVisible(true);

            // keep model of node that is being dragged to create connection
            // this is used by InteractiveNode to fire InteractiveNodeEvent.CONNECTION_DONE
            sourceNode = event.getSourceNode();
            destNode = event.getDestinationNode();
        });
        n.addEventFilter(InteractiveNodeEvent.CONNECTION_DONE, event -> {
            guideLine.setVisible(false);

            sourceNode = null;
            destNode = null;
        });
    }

    public void removeChildren(InteractiveNode n) {
        content.getChildren().remove(n);
        selectionGroup.getSelectable().remove(n);
        n.removeEventHandler(InteractiveNodeEvent.MOVED, nodeMovedHandler);
    }

    public SelectionGroup<InteractiveNode> getSelectionGroup() {
        return selectionGroup;
    }

    public NodeElement getSourceNode() {
        return sourceNode;
    }

    public NodeElement getDestNode() {
        return destNode;
    }

    public double getScale() {
        return scale.get();
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale.set(scale);
    }

    // Update current mouse position relative to the pane origin (top left corner) based on the current cursor position
    // in the visible portion of the pane, the current scroll bar position and the current scale value.
    // The position calculated is used for example by the copy and paste logic to paste node to the current mouse
    // position on the pane.
    private void updateMousePosition(MouseEvent event) {
        double viewportWidth = getViewportBounds().getWidth();
        double viewportHeight = getViewportBounds().getHeight();
        double extraWidth = group.getLayoutBounds().getWidth() - viewportWidth;
        double extraHeight = group.getLayoutBounds().getHeight() - viewportHeight;

        double left = getHvalue() * extraWidth;
        double top = getVvalue() * extraHeight;

        mouseX = (left + event.getX()) / scale.get();
        mouseY = (top + event.getY()) / scale.get();
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public double getViewportMinX() {
        double viewportWidth = getViewportBounds().getWidth();
        double extraWidth = group.getLayoutBounds().getWidth() - viewportWidth;
        return getHvalue() * extraWidth;
    }

    public double getViewportMinY() {
        double viewportHeight = getViewportBounds().getHeight();
        double extraHeight = group.getLayoutBounds().getHeight() - viewportHeight;
        return getVvalue() * extraHeight;
    }
}
