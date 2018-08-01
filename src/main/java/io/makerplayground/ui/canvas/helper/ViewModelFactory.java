package io.makerplayground.ui.canvas.helper;

/**
 *
 * Created by Nuntipat Narkthong on 6/9/2017 AD.
 */
public interface ViewModelFactory<T, U> {
    U newInstance(T t);
}
