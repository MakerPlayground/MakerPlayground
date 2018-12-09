package io.makerplayground.device.shared;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class DotMatrix {
    private int row;
    private int column;
    private byte[][] dots;

    public DotMatrix(int row, int column) {
        this(row, column, new byte[row][column]);
    }

    public DotMatrix(int row, int column, byte[][] dots) {
        this.row = row;
        this.column = column;
        this.dots = dots;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public byte[][] getDots() {
        return dots;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(System.lineSeparator());
        for (byte[] row: dots) {
            sj.add(Arrays.toString(row));
        }
        return sj.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DotMatrix that = (DotMatrix) o;
        return row == that.getRow() && column == that.getColumn() && Arrays.deepEquals(dots, that.dots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column, dots);
    }
}
