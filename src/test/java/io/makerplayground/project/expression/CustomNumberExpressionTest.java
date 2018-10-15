package io.makerplayground.project.expression;

import org.junit.jupiter.api.Test;

class CustomNumberExpressionTest {

    @Test
    void TheseExpressionShouldValid() {
        /* 3+5 */
        /* pv+5 */
        /* (3+5) */
        /* (3*5)-10 */
        /* ((3+5)/10) */
        /* (3+5)/pv */
    }

    @Test
    void TheseExpressionShouldNotValid() {
        /* empty */
        /* not complete parenthesis */
        /* not complete parenthesis */
        /* close parenthesis before open parenthesis */
        /* not match parenthesis */
        /* not match parenthesis */
        /* not a math expression */
        /* not a math expression */
        /* not a math expression */
        /* not a math expression */
    }

    @Test
    void TheseExpressionShouldValidTerm() {
        /* empty */
        /* 3+5 */
        /* pv+5 */
        /* (3+5) */
        /* (3*5)-10 */
        /* ((3+5)/10) */
        /* (3+5)/pv */
    }

    @Test
    void TheseExpressionShouldNotValidTerm() {
        /* not complete parenthesis */
        /* not complete parenthesis */
        /* close parenthesis before open parenthesis */
        /* not match parenthesis */
        /* not match parenthesis */
        /* not a math expression */
        /* not a math expression */
        /* not a math expression */
        /* not a math expression */
    }
}