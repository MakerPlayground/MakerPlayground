package io.makerplayground.ui.control;

import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class AutoResizeTextField extends TextField {
    public AutoResizeTextField() {
        this("");
    }

    public AutoResizeTextField(String text) {
        super(text);
        fontProperty().addListener((observable, oldValue, newValue) -> adjustPreferredWidth());
        textProperty().addListener((observable, oldValue, newValue) -> adjustPreferredWidth());
    }

    private void adjustPreferredWidth() {
        // update the width of the nameTextField based on it's content by measuring the width of the text using Text
        // instance with the same content. 'W' is added to reserved space for the next character and to prevent the
        // the text from overflow the textfield and force the first character to hide below the left edge.
        // Note that we should have used nameTextField.prefColumnCountProperty() but that property computes the
        // preferred width based on the width of character 'W' multiply by the number of character in the text
        // which is way off from the actual width when the font isn't monospaced
        Text t = new Text(getText() + "W");
        t.setFont(getFont());
        setPrefWidth(t.getBoundsInLocal().getWidth());
    }
}
