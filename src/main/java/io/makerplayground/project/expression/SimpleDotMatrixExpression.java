package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.device.shared.DotMatrix;
import io.makerplayground.project.term.DotMatrixTerm;

public class SimpleDotMatrixExpression extends Expression{

    public SimpleDotMatrixExpression(DotMatrix dotMatrix) {
        super(Type.DOT_MATRIX);
        terms.add(new DotMatrixTerm(dotMatrix));
    }

    SimpleDotMatrixExpression(SimpleDotMatrixExpression e) {
        super(e);
    }

    @JsonIgnore
    public DotMatrix getByte2D() {
        return ((DotMatrixTerm) terms.get(0)).getValue();
    }

    public void setValueAt(int row, int column, byte value) {
        getByte2D().getDots()[row][column] = value;
    }

    public byte getValueAt(int row, int column) {
        return getByte2D().getDots()[row][column];
    }

    @Override
    public SimpleDotMatrixExpression deepCopy() {
        return new SimpleDotMatrixExpression(this);
    }
}
