package io.makerplayground.ui.canvas;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

public class CaptureSelection {

    private double mouseX , mouseY;
    private final InteractivePane mainPane;
    Rectangle rect;
    Bounds screenBounds;
    Group group;
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();

    public CaptureSelection(Group group,Bounds screenBounds,InteractivePane mainPane) {
        this.screenBounds = screenBounds;
        this.group = group;
        this.mainPane = mainPane;
        this.mainPane.setCapture();
        rect = new Rectangle( 0,0,0,0);
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(4);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(null);
        group.addEventHandler(MouseEvent.MOUSE_PRESSED, pressedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_DRAGGED, draggedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_RELEASED, releasedEventHandler);
    }

    EventHandler<MouseEvent> pressedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            resetRectangle();
            mouseX = event.getX();
            mouseY = event.getY();
            rect.setX(mouseX );
            rect.setY(mouseY);
            rect.setWidth(0);
            rect.setHeight(0);
            group.getChildren().add(rect);
        }
    };

    EventHandler<MouseEvent> draggedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            double setX = event.getX() - mouseX;
            double setY = event.getY() - mouseY;
            if ( setX > 0) {
                rect.setWidth(setX);
            }
            else {
                rect.setX(event.getX());
                rect.setWidth(mouseX - rect.getX());
            }
            if ( setY > 0) {
                rect.setHeight(setY);
            } else {
                rect.setY(event.getY());
                rect.setHeight(mouseY - rect.getY());
            }
        }
    };


    EventHandler<MouseEvent> releasedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Rectangle screenCapture = new Rectangle(rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight());
            resetRectangle();

            // not crop rectangle line.
            // The thickness is 4.
            int x = (int) screenBounds.getMinX() + (int) screenCapture.getX() + 2;
            int y = (int) screenBounds.getMinY() + (int) screenCapture.getY() + 2;
            int width = (int) screenCapture.getWidth() - 4;
            int height = (int) screenCapture.getHeight() - 4;

            // When width and height are longer than application bound
            // not crop scroll bar
            if ( (x + width) > screenBounds.getMaxX()) {
                width = (int) screenBounds.getMaxX() - x - 12;
            }
            if ( (y + height) > screenBounds.getMaxY()) {
                height = (int) screenBounds.getMaxY() - y - 12;
            }

            if (width > 0 && height > 0) {
                Rectangle2D screenRect = new Rectangle2D(x,y,width,height);
                javafx.scene.robot.Robot robot =new javafx.scene.robot.Robot();
                WritableImage writableImage = robot.getScreenCapture(new WritableImage(width,height),screenRect);
                content.putImage(writableImage);
                clipboard.setContent(content);
            }

            mainPane.resetCapture();
            group.removeEventHandler(MouseEvent.MOUSE_PRESSED, pressedEventHandler);
            group.removeEventHandler(MouseEvent.MOUSE_DRAGGED, draggedEventHandler);
            group.removeEventHandler(MouseEvent.MOUSE_RELEASED, releasedEventHandler);
        }
    };

    public void resetRectangle() {
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(0);
        rect.setHeight(0);
        group.getChildren().remove(rect);
    }
}

