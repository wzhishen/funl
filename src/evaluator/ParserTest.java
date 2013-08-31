package evaluator;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit tests for Parser
 * @author Zhishen Wen
 * @version Mar 24, 2013
 */
public class ParserTest {
    private Parser parser;

    @Test
    public final void testParser() {
        use("def minus x y = x - y end");
        assertTrue(parser instanceof Parser);
        Parser exp = new Parser("def minus x y = x - y end");
        assertNotNull(parser);
        assertNotNull(exp);
        assertNotSame(exp, parser);
    }

    @Test
    public final void testProgram() {
        use("def minus x y = x - y end");
        parser.program();
        assertEquals(0, parser.stack.size());
        assertTrue(equalIgnoreTypes(Tree.parse("def(minus $seq(x y) $seq(-(x y)))"), parser.functions.get("minus")));
    }

    @Test
    public final void testFunctionDefinition() {
        use("def foo x y = x - y end");
        shouldGive(parser.functionDefinition(), "def(foo $seq(x y) $seq(-(x y)))  ");
        use("def foo = 1 end");
        shouldGive(parser.functionDefinition(), "def(foo $seq $seq(1.0))");
        use("def foo x y = val w = x * y, val z = 2 + w, y / w end");
        shouldGive(parser.functionDefinition(), "def(foo $seq(x y) $seq(val(w *(x y)) val(z +(2.0 w)) /(y w)))");
    }

    @Test
    public final void testParameter() {
        use("param");
        shouldGive(parser.parameter(), "param");
        use("name");
        shouldGive(parser.parameter(), "name");
    }

    @Test
    public final void testExpressions() {
        use("expr");
        shouldGive(parser.expressions(), "$seq(expr)");
        use("expr, expr2");
        shouldGive(parser.expressions(), "$seq(expr expr2)");
        use("expr, val z = expr2, 2 * bar");
        shouldGive(parser.expressions(), "$seq(expr val(z expr2) *(2.0 bar))");
    }

    @Test
    public final void testExpression() {
        use("expr");
        shouldGive(parser.expression(), "expr");
        use("expr / expr2");
        shouldGive(parser.expression(), "/(expr expr2)");
        use("expr - expr2 - expr3");
        shouldGive(parser.expression(), "-(-(expr expr2) expr3)");
        use("expr * expr2");
        shouldGive(parser.expression(), "*(expr expr2)");
        use("expr * expr2 * expr3");
        shouldGive(parser.expression(), "*(*(expr expr2) expr3)");
        use("expr + expr2 * expr3 - expr4");
        shouldGive(parser.expression(), "-(+(expr *(expr2 expr3))expr4)");
    }

    @Test
    public final void testValueDefinition() {
        use("val foo = a");
        shouldGive(parser.valueDefinition(), "val(foo a)");
        use("val foo = 1.0 + 2 + 5.5");
        shouldGive(parser.valueDefinition(), "val(foo +(+(1.0 2.0)5.5))");
    }

    @Test
    public final void testTerm() {
        use("term");
        shouldGive(parser.term(), "term");
        use("term * term2");
        shouldGive(parser.term(), "*(term term2)");
        use("term / term2 / term3");
        shouldGive(parser.term(), "/(/(term term2) term3)");
    }

    @Test
    public final void testFactor() {
        use("1234567");
        shouldGive(parser.factor(), "1234567.0");
        use("THIS_IS_A_VERY_LONG_NAME_I_GUESS");
        shouldGive(parser.factor(), "THIS_IS_A_VERY_LONG_NAME_I_GUESS");
        use("foo(bar)");
        shouldGive(parser.factor(), "$call(foo $seq(bar))");
        use("foo(bar, barbar)");
        shouldGive(parser.factor(), "$call(foo $seq(bar barbar))");
        use("empty()");
        shouldGive(parser.factor(), "$call(empty $seq)");
        use("foo(bar + barbar + 8)");
        shouldGive(parser.factor(), "$call(foo $seq(+(+(bar barbar)8.0)))");
    }

    @Test
    public final void testAddOperator() {
        use("+");
        shouldGive(parser.addOperator(), "+");
        use("-");
        shouldGive(parser.addOperator(), "-");
        use("&");
        assertFalse(parser.addOperator());
        use("*");
        assertFalse(parser.addOperator());
    }

    @Test
    public final void testMultiplyOperator() {
        use("*");
        shouldGive(parser.multiplyOperator(), "*");
        use("/");
        shouldGive(parser.multiplyOperator(), "/");
        use("+");
        assertFalse(parser.multiplyOperator());
        use("&");
        assertFalse(parser.addOperator());
    }

    @Test
    public final void testNumber() {
        use("1");
        shouldGive(parser.number(), "1.0");
        use("1234567");
        shouldGive(parser.number(), "1234567.0");
    }

    @Test
    public final void testName() {
        use("THIS_IS_A_VERY_LONG_NAME_I_GUESS");
        shouldGive(parser.name(), "THIS_IS_A_VERY_LONG_NAME_I_GUESS");
    }
    
    /* private helpers */
    
    private void use(String s) {
        parser = new Parser(s);
    }
    
    private void shouldGive(boolean parserReturnValue, String expected) {
        assertTrue(parserReturnValue);
        Tree<String> expectedTree = Tree.parse(expected);
        Tree<Token> actualTree = parser.stack.peek();
        assertTrue("Expected " + expectedTree + " but got " + actualTree,
                   equalIgnoreTypes(expectedTree, actualTree));
    }
 
    private boolean equalIgnoreTypes(Tree<String> tree1, Tree<Token> tree2) {
        if (tree1 == null) return tree2 == null;
        if (tree2 == null) return false;
        if (! tree1.getValue().equals(tree2.getValue().getValue())) return false;
        if (tree1.getNumberOfChildren() != tree2.getNumberOfChildren()) return false;
        for (int i = 0; i < tree1.getNumberOfChildren(); i++) {
            if (! equalIgnoreTypes(tree1.getChild(i), tree2.getChild(i))) return false;
        }
        return true;
    }

}
