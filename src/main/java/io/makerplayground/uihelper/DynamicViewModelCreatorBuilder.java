package io.makerplayground.uihelper;

import javafx.collections.ObservableList;

import java.util.function.Predicate;

public class DynamicViewModelCreatorBuilder<T, U> {
    private ObservableList<T> model;
    private ViewModelFactory<T, U> viewModelFactory;
    private Predicate<T> filter = null;

    public DynamicViewModelCreatorBuilder<T, U> setModel(ObservableList<T> model) {
        this.model = model;
        return this;
    }

    public DynamicViewModelCreatorBuilder<T, U> setViewModelFactory(ViewModelFactory<T, U> viewModelFactory) {
        this.viewModelFactory = viewModelFactory;
        return this;
    }

    public DynamicViewModelCreatorBuilder<T, U> setFilter(Predicate<T> filter) {
        this.filter = filter;
        return this;
    }

    public DynamicViewModelCreator<T, U> createDynamicViewModelCreator() {
        return new DynamicViewModelCreator<T, U>(model, viewModelFactory, filter);
    }
}