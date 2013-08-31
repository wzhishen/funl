package evaluator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ParserTestDave {
    private Parser parser;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testProgram() {
        use("def foo x y = x + y end");
        parser.program();
        assertEquals(0, parser.stack.size());
        assertTrue(equalIgnoreTypes(Tree.parse("def(foo $seq(x y) $seq(+(x y)))"), parser.functions.get("foo")));

        use("def three = 1, 2, 3 end def foo x y = x + y end");
        parser.program();
        assertEquals(0, parser.stack.size());
        assertTrue(equalIgnoreTypes(Tree.parse("def(three $seq $seq(1.0 2.0 3.0))"), parser.functions.get("three")));
        assertTrue(equalIgnoreTypes(Tree.parse("def(foo $seq(x y) $seq(+(x y)))"), parser.functions.get("foo")));
    }

    @Test
    public void testFunctionDefinition() {
        use("def foo x y = x + y end");
        shouldGive(parser.functionDefinition(), "def(foo $seq(x y) $seq(+(x y)))  ");

        use("def three = 1, 2, 3 end");
        shouldGive(parser.functionDefinition(), "def(three $seq $seq(1.0 2.0 3.0))");

        use("def foo x y = val w = x + y, 2 * w end");
        shouldGive(parser.functionDefinition(), "def(foo $seq(x y) $seq(val(w +(x y)) *(2.0 w)))");
    }

    @Test
    public void testExpressions() {
        use("abc");
        shouldGive(parser.expressions(), "$seq(abc)");
        
        use("abc, 2 + 3");
        shouldGive(parser.expressions(), "$seq(abc +(2.0 3.0))");
        
        use("5.25, val z = 0, a * b");
        shouldGive(parser.expressions(), "$seq(5.25 val(z 0.0) *(a b))");
        
        use("abc");
        shouldGive(parser.expressions(), "$seq(abc)");
    }

    @Test
    public void testExpression_AsTerms() {
        use("abc");
        shouldGive(parser.expression(), "abc");
        
        use("abc + xyz");
        shouldGive(parser.expression(), "+(abc xyz)");
        
        use("abc - pqr + xyz");
        shouldGive(parser.expression(), "+(-(abc pqr) xyz)");
        
        use("abc * xyz");
        shouldGive(parser.expression(), "*(abc xyz)");
        
        use("abc / pqr * xyz");
        shouldGive(parser.expression(), "*(/(abc pqr) xyz)");
        
        use("abc + pqr * xyz");
        shouldGive(parser.expression(), "+(abc *(pqr xyz))");
    }

    @Test
    public void testExpression_AsValueDefinition() {
        use("val one = 1.0");
        shouldGive(parser.expression(), "val(one 1.0)");
        
        use("val three = 1.0 + 2");
        shouldGive(parser.expression(), "val(three +(1.0 2.0))");
    }

    @Test
    public void testValueDefinition() {
        use("val one = 1.0");
        shouldGive(parser.valueDefinition(), "val(one 1.0)");

        use("val three = 1.0 + 2");
        shouldGive(parser.valueDefinition(), "val(three +(1.0 2.0))");
        
        use("val xxx = x + x / x");
        shouldGive(parser.valueDefinition(), "val(xxx +(x /(x x)))");
        
        use("val a= val b = c");
        shouldGive(parser.valueDefinition(), "val(a val(b c))");
    }

    @Test
    public void testTerm() {
        use("abc");
        shouldGive(parser.term(), "abc");
        
        use("abc * xyz");
        shouldGive(parser.term(), "*(abc xyz)");
        
        use("abc / pqr * xyz");
        shouldGive(parser.term(), "*(/(abc pqr) xyz)");
        
        use("2 * xyz");
        shouldGive(parser.term(), "*(2.0 xyz)");
    }

    @Test
    public void testFactor_Name() {
        use("abracadabra");
        shouldGive(parser.factor(), "abracadabra");
    }

    @Test
    public void testFactor_Number() {
        use("125");
        shouldGive(parser.factor(), "125.0");
    }

    @Test
    public void testFactor_FunctionCall() {
        use("foo(bar)");
        shouldGive(parser.factor(), "$call(foo $seq(bar))");

        use("foo(bar, baz)");
        shouldGive(parser.factor(), "$call(foo $seq(bar baz))");

        use("foo()");
        shouldGive(parser.factor(), "$call(foo $seq)");

        use("foo(a + 5)");
        shouldGive(parser.factor(), "$call(foo $seq(+(a 5.0)))");
    }
    
    @Test
    public void testFactor_IfExpression() {
        use("if x, y then x + y, x - y else x * z, y * z, z * z end");
        shouldGive(parser.factor(), "if($seq(x y) $seq(+(x y) -(x y)) $seq(*(x z) *(y z) *(z z)))");

        use("if x then y else z end");
        shouldGive(parser.factor(), "if($seq(x) $seq(y) $seq(z))");
    }
    
    @Test
    public void testFactor_Read() {
        use("read \"quoted string\"");
        // Build desired tree (Tree.parse() can't handle quoted strings)
        Tree<Token> readTree = new Tree(new Token(TokenType.KEYWORD, "read"));
        Tree<Token> stringTree = new Tree(new Token(TokenType.STRING, "quoted string"));
        Tree<Token> expectedTree = new Tree(readTree, stringTree);
        // Get parse result and compare using Tree.equals()
        assertTrue(parser.factor());
        Tree<Token> actualTree = parser.stack.peek();
//        assertEquals(expectedTree, actualTree);
        assertEquals(new Token(TokenType.KEYWORD, "read"), actualTree.getValue());
        assertEquals(1, actualTree.getNumberOfChildren());
        assertEquals("quoted string", actualTree.getChild(0).getValue().getValue());
    }
    
    @Test
    public void testFactor_ParenthesizedExpression() {
        use("(x)");
        shouldGive(parser.factor(), "x");
        
        use("(x+y)");
        shouldGive(parser.factor(), "+(x y)");
        
        use("(((x)))");
        shouldGive(parser.factor(), "x");
        
        use("((x+y))");
        shouldGive(parser.factor(), "+(x y)");
    }

    @Test
    public void testAddOperator() {
        use("+");
        shouldGive(parser.addOperator(), "+");
        use("-");
        shouldGive(parser.addOperator(), "-");
        use("%");
        assertFalse(parser.addOperator());
    }

    @Test
    public void testMultiplyOperator() {
        use("*");
        shouldGive(parser.multiplyOperator(), "*");
        use("/");
        shouldGive(parser.multiplyOperator(), "/");
        use("+");
        assertFalse(parser.multiplyOperator());
    }

    /**
     * @param s The string to be parsed.
     */
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
