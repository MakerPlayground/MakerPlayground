package io.makerplayground.generator.source;

import io.makerplayground.project.term.Term;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NumberExpressionTreeNode {
    private NumberExpressionTreeNode leftChild;
    private Term root;
    private NumberExpressionTreeNode rightChild;

    public NumberExpressionTreeNode(Term root) {
        this.root = root;
    }

    public boolean hasChildren() {
        return leftChild != null && rightChild != null;
    }
}
