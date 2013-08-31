package evaluator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for Funl
 * @author Zhishen Wen
 * @version Mar 24, 2013
 */
public class FunlTest {
    Funl funl;
    Parser parser;

    @Before
    public void setUp() throws Exception {
        funl = new Funl();
    }

    @Test
    public final void testDefine() {
        funl.define("def minus x y = x - y end");
        assertTrue(equalIgnoreTypes(Tree.parse("def(minus $seq(x y) $seq(-(x y)))"), funl.functions.get("minus")));
        
        funl.define("def minus x y = x - y end def add x y = x + y end");
        assertTrue(equalIgnoreTypes(Tree.parse("def(minus $seq(x y) $seq(-(x y)))"), funl.functions.get("minus")));
        assertTrue(equalIgnoreTypes(Tree.parse("def(add $seq(x y) $seq(+(x y)))"), funl.functions.get("add")));
        
        funl.define("def minus x y = x - y end def add x y = x + y end def seq = 1, 2, 3, 4, 5 end");
        assertTrue(equalIgnoreTypes(Tree.parse("def(minus $seq(x y) $seq(-(x y)))"), funl.functions.get("minus")));
        assertTrue(equalIgnoreTypes(Tree.parse("def(add $seq(x y) $seq(+(x y)))"), funl.functions.get("add")));
        assertTrue(equalIgnoreTypes(Tree.parse("def(seq $seq $seq(1.0 2.0 3.0 4.0 5.0))"), funl.functions.get("seq")));
    }

    @Test
    public final void testEval() {
        funl.define("def minus x y = x - y end def add x y = x + y end def seq = 1, 2, 3, 4, 5 end");
        assertTrue(equalIgnoreTypes(Tree.parse("3.0"), funl.eval(get("1 + 2"))));
        assertTrue(equalIgnoreTypes(Tree.parse("27.0"), funl.eval(get("3*9"))));
        assertTrue(equalIgnoreTypes(Tree.parse("36.0"), funl.eval(get("3*9 + 9"))));
        assertTrue(equalIgnoreTypes(Tree.parse("5.0"), funl.eval(get("seq()"))));
        assertTrue(equalIgnoreTypes(Tree.parse("37.0"), funl.eval(get("add(3*9 + 9, 1)"))));
        assertTrue(equalIgnoreTypes(Tree.parse("41.0"), funl.eval(get("add(3*9 + minus(10, 1), seq())"))));
    }
    
    /* private helpers */
    
    private Tree<Token> get(String s) {
        return funl.parseInput(s);
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
