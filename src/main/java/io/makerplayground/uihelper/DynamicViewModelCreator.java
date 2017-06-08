package io.makerplayground.uihelper;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.HashMap;

/**
 *
 * Created by Nuntipat Narkthong on 6/8/2017 AD.
 */
public class DynamicViewModelCreator<T, U> {
    private final ObservableList<T> model;
    private final ViewModelFactory<T, U> viewModelFactory;

    private final ObservableMap<T, U> controllerMap;

    public DynamicViewModelCreator(ObservableList<T> model, ViewModelFactory<T, U> viewModelFactory) {
        this.model = model;
        this.viewModelFactory = viewModelFactory;

        this.controllerMap = FXCollections.observableMap(new HashMap<>());

        for (T t : model) {
            addController(t);
        }

        model.addListener(new ListChangeListener<T>() {
            @Override
            public void onChanged(Change<? extends T> c) {
                while (c.next()) {
                    if (c.wasPermutated()) {
                        for (int i = c.getFrom(); i < c.getTo(); ++i) {
                            throw new UnsupportedOperationException();
                        }
                    } else if (c.wasUpdated()) {
                        throw new UnsupportedOperationException();
                    } else {
                        for (T remitem : c.getRemoved()) {
                            removeController(remitem);
                        }
                        for (T additem : c.getAddedSubList()) {
                            addController(additem);
                        }
                    }
                }
            }
        });
    }

    ObservableMap<T, U> getControllerMap() {
        return controllerMap;
    }

    private void addController(T model) {
        U node = viewModelFactory.newInstance(model);
        controllerMap.put(model, node);
    }

    private void removeController(T model) {
        U node = controllerMap.remove(model);
        if (node == null)
            throw new IllegalStateException();
    }

    public U getViewModel(T model) {
        return controllerMap.get(model);
    }

//    public T getModel(U viewModel) {
//        for (Map.Entry<T, U> entry : controllerMap.entrySet()) {
//            if (entry.getValue() == viewModel)
//                return entry.getKey();
//        }
//        return null;
//    }
}
