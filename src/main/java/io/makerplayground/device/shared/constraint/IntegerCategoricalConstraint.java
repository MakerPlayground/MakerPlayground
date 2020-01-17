package io.makerplayground.device.shared.constraint;

import io.makerplayground.device.shared.Unit;

import java.util.*;

public class IntegerCategoricalConstraint implements Constraint {
    private final Collection<Integer> integerCategoricalValue;

    /**
     * Construct a constraint to match a specify integer. The constructor should only
     * be invoked by the DeviceLibrary in order to rebuild the library from file.
     * @param s the string to be matched by this constraint
     */
    IntegerCategoricalConstraint(Integer s) {
        this.integerCategoricalValue = Collections.singletonList(s);
    }

    /**
     * Construct a constraint to match a list of integers given. The constructor should only
     * be invoked by the DeviceLibrary in order to rebuild the library from file.
     * @param value the list of strings value to be matched
     */
    IntegerCategoricalConstraint(Collection<Integer> value) {
        this.integerCategoricalValue = value;
    }

    public Collection<Integer> getCategories() {
        return integerCategoricalValue;
    }

    @Override
    public boolean test(double d, Unit unit) {
        return false;
    }

    @Override
    public boolean test(String s) {
        return false;
    }

    @Override
    public boolean test(Integer i) {
        return integerCategoricalValue.contains(i);
    }

    @Override
    public boolean isCompatible(Constraint constraint) {
        if (!(constraint instanceof IntegerCategoricalConstraint))
            return false;

        return integerCategoricalValue.containsAll(((IntegerCategoricalConstraint) constraint).integerCategoricalValue);
    }

    @Override
    public Constraint union(Constraint constraint) {
        if (!(constraint instanceof IntegerCategoricalConstraint))
            throw new ClassCastException();

        IntegerCategoricalConstraint categoricalConstraint = (IntegerCategoricalConstraint) constraint;
        Set<Integer> tmp = new HashSet<>(integerCategoricalValue);
        tmp.addAll(categoricalConstraint.integerCategoricalValue);
        return new IntegerCategoricalConstraint(tmp);
    }

    @Override
    public Constraint intersect(Constraint constraint) {
        if (constraint == Constraint.NONE) {
            return Constraint.NONE;
        }

        if (!(constraint instanceof IntegerCategoricalConstraint))
            throw new ClassCastException();

        IntegerCategoricalConstraint categoricalConstraint = (IntegerCategoricalConstraint) constraint;
        Set<Integer> tmp = new HashSet<>(integerCategoricalValue);
        tmp.retainAll(categoricalConstraint.integerCategoricalValue);
        return new IntegerCategoricalConstraint(tmp);
    }

    @Override
    public String toString() {
        return "IntegerCategoricalConstraint{" +
                "categoricalValue=" + integerCategoricalValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerCategoricalConstraint that = (IntegerCategoricalConstraint) o;
        return this.integerCategoricalValue.containsAll(that.integerCategoricalValue) && that.integerCategoricalValue.containsAll(this.integerCategoricalValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(integerCategoricalValue);
    }
}
