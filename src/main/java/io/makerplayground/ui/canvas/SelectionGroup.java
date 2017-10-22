package io.makerplayground.ui.canvas;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public class SelectionGroup {
    private Selectable selectingSelectable;
    private final ObservableList<Selectable> selectable;

    public SelectionGroup() {
        selectable = FXCollections.observableArrayList();
        selectable.addListener(new ListChangeListener<Selectable>() {
            @Override
            public void onChanged(Change<? extends Selectable> c) {
                while (c.next()) {
                    if (c.wasPermutated()) {

                    } else if (c.wasUpdated()) {
                        //update item
                    } else {
                        for (Selectable remitem : c.getRemoved()) {

                        }
                        for (Selectable additem : c.getAddedSubList()) {
                            additem.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                // if this node is selected, we deselect other node. if this node is deselect, do nothing.
                                if (newValue) {
                                    for (Selectable s : selectable) {
                                        if (s != additem)
                                            s.setSelected(false);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }


    public ObservableList<Selectable> getSelectable() {
        return selectable;
    }

    public void deselect() {
        for (Selectable s : selectable) {
            s.setSelected(false);
        }
    }
}
