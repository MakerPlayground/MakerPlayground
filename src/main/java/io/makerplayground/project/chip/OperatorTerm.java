package io.makerplayground.project.chip;

public class OperatorTerm extends Term {

    public OperatorTerm(ChipOperator value) {
        super(ChipType.OPERATOR, value);
    }

    @Override
    public ChipOperator getValue() {
        return (ChipOperator) value;
    }
}
