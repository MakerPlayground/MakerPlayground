package io.makerplayground.uihelper;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public interface ModelFilter<T> {
    boolean apply(T t);
}
