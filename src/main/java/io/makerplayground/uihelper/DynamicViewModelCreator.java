package io.makerplayground.uihelper;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.HashMap;

/**
 * A helper class for creating instances of ViewModel based on the number of Model in an ObservableList provided.
 *
 * @param <T> type of Model of the ViewModel to be created by the instance of this class
 * @param <U> type of ViewModel to be created
 */
public class DynamicViewModelCreator<T, U> {
    private final ObservableList<T> model;
    private final ViewModelFactory<T, U> viewModelFactory;
    private final ModelFilter<T> modelFilter;

    private final ObservableMap<T, U> controllerMap;

    /**
     *
     * @param model list of Model objects to create a ViewModel
     * @param viewModelFactory
     */
    public DynamicViewModelCreator(ObservableList<T> model, ViewModelFactory<T, U> viewModelFactory) {
        this(model, viewModelFactory, null);
    }

    /**
     *
     * @param model
     * @param viewModelFactory
     * @param modelFilter
     */
    public DynamicViewModelCreator(ObservableList<T> model, ViewModelFactory<T, U> viewModelFactory, ModelFilter<T> modelFilter) {
        this.model = model;
        this.viewModelFactory = viewModelFactory;
        this.modelFilter = modelFilter;
        this.controllerMap = FXCollections.observableMap(new HashMap<>());

        for (T t : model) {
            if (this.modelFilter == null || this.modelFilter.apply(t))
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
                        for (T removedItem : c.getRemoved()) {
                            if (DynamicViewModelCreator.this.modelFilter == null
                                    || DynamicViewModelCreator.this.modelFilter.apply(removedItem))
                                removeController(removedItem);
                        }
                        for (T addedItem : c.getAddedSubList()) {
                            if (DynamicViewModelCreator.this.modelFilter == null
                                    || DynamicViewModelCreator.this.modelFilter.apply(addedItem))
                                addController(addedItem);
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
        //if (node == null)
        //    throw new IllegalStateException();
    }

    public void invalidate(T model) {
        removeController(model);
        if (this.modelFilter == null || this.modelFilter.apply(model))
            addController(model);
    }

//    public U getViewModel(T model) {
//        return controllerMap.get(model);
//    }
}
