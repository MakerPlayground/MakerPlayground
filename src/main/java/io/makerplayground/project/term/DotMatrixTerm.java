package io.makerplayground.project.term;

import io.makerplayground.device.shared.DotMatrix;

public class DotMatrixTerm extends Term {

    public DotMatrixTerm(DotMatrix dotMatrix) {
        super(Type.DOTMATRIX, dotMatrix);
    }

    @Override
    public String toCCode() {
        return null;
    }

    @Override
    public DotMatrix getValue() {
        return (DotMatrix) value;
    }

    @Override
    public boolean isValid() {
        return false;
    }
}
