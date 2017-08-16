package io.makerplayground.ui.canvas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.makerplayground.ui.Tutorial;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.naming.Binding;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

public class TutorialView extends Dialog {
    @FXML
    private Label topicLabel;
    @FXML
    private Label descriptLabel;
    @FXML
    private ImageView tipImage;
    @FXML
    private Button prevBtn;
    @FXML
    private Button nextBtn;
    @FXML
    private Button cancelBtn;

    public TutorialView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/TutorialView.fxml"));
        fxmlLoader.setRoot(this.getDialogPane());
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            initView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObservableList<Tutorial> tutorials = FXCollections.observableArrayList();
        List<Tutorial> tutorial = mapper.readValue(getClass().getResourceAsStream("/json/tutorials.json"),
                new TypeReference<List<Tutorial>>() {});
        ListIterator<Tutorial> i = tutorial.listIterator();
        System.out.println(tutorial);

        setTitle("Tips & Tricks");
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UNDECORATED);

        getDialogPane().getStylesheets().add(getClass().getResource("/css/TutorialView.css").toExternalForm());
        Tutorial t = i.next();

        descriptLabel.setText(t.getDescription());
        topicLabel.setText(t.getTopic());
        tipImage.setImage(new Image(getClass().getResourceAsStream(t.getImg())));
        prevBtn.setDisable(true);
        prevBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(i.hasPrevious()){
                    Tutorial p = i.previous();
                    topicLabel.setText(p.getTopic());
                    descriptLabel.setText(p.getDescription());
                    tipImage.setImage(new Image(getClass().getResourceAsStream(p.getImg())));
                    prevBtn.setDisable(false);
                }
                else if(!i.hasPrevious()){
                    prevBtn.setDisable(true);
                    nextBtn.setDisable(false);
                }
                if(i.hasNext()) nextBtn.setDisable(false);
            }
        });
        nextBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                if(i.hasNext()){
                    Tutorial n = i.next();
                    topicLabel.setText(n.getTopic());
                    descriptLabel.setText(n.getDescription());
                    tipImage.setImage(new Image(getClass().getResourceAsStream(n.getImg())));
                    nextBtn.setDisable(false);
                }
                else if(!i.hasNext()){
                    nextBtn.setDisable(true);
                }
                if(i.hasPrevious()) prevBtn.setDisable(false);
            }
        });
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Window window = getDialogPane().getScene().getWindow();
                window.setOnCloseRequest(event -> window.hide());
            }
        });
    }
}
