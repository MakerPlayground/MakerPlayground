/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.dialog.tutorial;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

public class TutorialView extends Stage {

    private AnchorPane rootPane;
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

    private int currentPosition = 0;

    public TutorialView(Window owner) {
        rootPane = new AnchorPane();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/tutorial/TutorialView.fxml"));
        fxmlLoader.setRoot(rootPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            initView(owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView(Window owner) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObservableList<Tutorial> tutorials = FXCollections.observableArrayList();
        List<Tutorial> tutorial = mapper.readValue(getClass().getResourceAsStream("/json/tutorials.json"),
                new TypeReference<List<Tutorial>>() {});
        ListIterator<Tutorial> i = tutorial.listIterator();

        setTitle("Tips & Tricks");
        rootPane.getStylesheets().add(getClass().getResource("/css/dialog/tutorial/TutorialView.css").toExternalForm());



//        descriptLabel.setText(t.getDescription());
//        topicLabel.setText(t.getTopic());
        tipImage.setImage(new Image(getClass().getResourceAsStream(tutorial.get(currentPosition).getImg())));
        prevBtn.setDisable(true);
        prevBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                currentPosition--;
                tipImage.setImage(new Image(getClass().getResourceAsStream(tutorial.get(currentPosition).getImg())));
                if (currentPosition == 0) {
                    prevBtn.setDisable(true);
                } else {
                    prevBtn.setDisable(false);
                }
                if (currentPosition == tutorial.size() - 1) {
                    nextBtn.setDisable(true);
                } else {
                    nextBtn.setDisable(false);
                }
            }
        });
        nextBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                currentPosition++;
                tipImage.setImage(new Image(getClass().getResourceAsStream(tutorial.get(currentPosition).getImg())));
                if (currentPosition == 0) {
                    prevBtn.setDisable(true);
                } else {
                    prevBtn.setDisable(false);
                }
                if (currentPosition == tutorial.size() - 1) {
                    nextBtn.setDisable(true);
                } else {
                    nextBtn.setDisable(false);
                }
            }
        });
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                if (Singleton.getInstance().isFlagFirstTime())
//                    SingletonTutorial.getInstance().closeTime();
//                else
//                    SingletonTutorial.getInstance().closeTime();

                hide();
            }
        });

        initStyle(StageStyle.TRANSPARENT);
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);
        Scene scene = new Scene(rootPane);
        setScene(scene);
    }
}
