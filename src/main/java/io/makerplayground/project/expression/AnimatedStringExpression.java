package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.project.term.StringAnimationTerm;

public class AnimatedStringExpression extends Expression {

    public AnimatedStringExpression(StringAnimationTerm t) {
        super(Expression.Type.ANIMATED_STRING);
        terms.add(t);
    }

    AnimatedStringExpression(AnimatedStringExpression e) {
        super(e);
    }

    @JsonIgnore
    public StringAnimationTerm getAnimatedTerm() {
        return (StringAnimationTerm) terms.get(0);
    }

    @Override
    public AnimatedStringExpression deepCopy() {
        return new AnimatedStringExpression(this);
    }

}
