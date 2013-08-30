package evaluator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for class Tree
 * @author Zhishen (Jason) Wen
 * @version Feb 1, 2013
 */
public class TreeTest {
    
    Tree<String> root, node2, node3, node4, node5, node6, node7, node8, node9;

    @Before
    public void setUp() throws Exception {
        
        node8 = new Tree<String>("eight");
        node7 = new Tree<String>("seven");
        node5 = new Tree<String>("five");
        node2 = new Tree<String>("two");
        node9 = new Tree<String>("nine");
        node6 = new Tree<String>("six", node8);
        node4 = new Tree<String>("four", node6, node7);
        node3 = new Tree<String>("three", node5);
        root = new Tree<String>("one", node2, node3, node4, node9);
    }

    @Test
    public final void testTree() {
        Tree<String> tree1 = new Tree<String>("new", node2, node3, node4);
        Tree<String> tree2 = new Tree<String>("new", node2, node3, node4);
        Tree<String> tree3 = new Tree<String>("old", node2, node3, node4);
        assertEquals(tree1.getValue(), "new");
        assertEquals(tree1.getNumberOfChildren(), 3);
        assertEquals(tree1, tree2);
        assertFalse(tree1.equals(tree3));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public final void testTreeException() {
        root = new Tree<String>("root", null);
        
        Tree<String> subtree = null;
        node2 = new Tree<String>("root", subtree);
        
        node4 = new Tree<String>("root", node3);
        node3 = new Tree<String>("root", node4);
        node5 = new Tree<String>("root", node5);
    }

    @Test
    public final void testSetValue() {
        root.setValue("new");
        assertEquals(root.getValue(), "new");
        assertFalse(root.getValue().equals("one"));
    }

    @Test
    public final void testGetValue() {
        assertEquals(root.getValue(), "one");
        assertEquals(node8.getValue(), "eight");
        assertEquals(node9.getValue(), "nine");
        assertFalse(root.getValue().equals("new"));
        assertFalse(node8.getValue().equals("nine"));
    }

    @Test
    public final void testAddChildIntTreeOfV() {
        Tree<String> tree1 = new Tree<String>("tree", node8);
        tree1.addChild(0, node2);
        Tree<String> tree2 = new Tree<String>("tree", node2, node8);
        assertEquals(tree1, tree2);
        assertFalse(tree1.equals(root));
    }

    @Test
    public final void testAddChildTreeOfV() {
        Tree<String> tree1 = new Tree<String>("tree");
        tree1.addChild(node2);
        Tree<String> tree2 = new Tree<String>("tree", node2);
        assertEquals(tree1, tree2);
        assertFalse(tree1.equals(root));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public final void testAddChildException1() {
        node8.addChild(root);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public final void testAddChildException2() {
        root.addChild(10, node2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public final void testAddChildren() {
        Tree<String> tree1 = new Tree<String>("tree");
        tree1.addChildren(node2, node9);
        Tree<String> tree2 = new Tree<String>("tree", node2, node9);
        assertEquals(tree1, tree2);
        assertFalse(tree1.equals(root));
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public final void testAddChildrenException() {
        node8.addChildren(node2, node4);
    }

    @Test
    public final void testGetNumberOfChildren() {
        assertEquals(root.getNumberOfChildren(), 4);
        assertEquals(node3.getNumberOfChildren(), 1);
        assertEquals(node7.getNumberOfChildren(), 0);
        assertFalse(node3.getNumberOfChildren() == 2);
    }

    @Test
    public final void testGetChild() {
        assertEquals(root.getChild(0), node2);
        assertFalse(root.getChild(1).equals(node8));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public final void testGetChildException() {
        root.getChild(10);
    }

    @Test
    public final void testContains() {
        assertTrue(root.contains(node8));
        assertTrue(root.contains(node2));
        assertTrue(root.contains(node7));
        assertTrue(root.contains(root));
        assertFalse(node5.contains(root));
    }

    @Test
    public final void testParse() {
        assertEquals(Tree.parse("one ( two three(five)four  ( six(eight ) seven) nine)  "), root);
        assertFalse(Tree.parse("one ( two three(five)four  ( six(eight ) ) nine)  ").equals(root));
        assertFalse(Tree.parse("one ( two three(five)four  ( six(eight ) BUZZ ) nine)  ").equals(root));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public final void testParseException() {
        Tree.parse("one (( ) )) two three(five)four  ( six(eight ) seven) nine)  ");
        Tree.parse(")))))(((((one (( ) )) two three(five)four  ( six(end)  ");
        Tree.parse("  \t\r\n\f");
    }

    @Test
    public final void testToString() {
        assertEquals(root.toString(), "one(two three(five)four(six(eight)seven)nine)");
        assertFalse(root.toString().equals("one(two three(five)four(six(eight)seven)TEN)"));
    }

    @Test
    public final void testEqualsObject() {
        Tree<String> tree1 = new Tree<String>("new", node2, node3, node4);
        Tree<String> tree2 = new Tree<String>("new", node2, node3, node4);
        Tree<String> tree3 = new Tree<String>("old", node2, node3, node4);
        assertEquals(tree1, tree2);
        assertEquals(root, root);
        assertFalse(tree1.equals(tree3));
    }

    @Test
    public final void testTokenize() {
        String[] tokens = {"one", "(", "two", "(", "three", ")", "four", ")"};
        String[] result = Tree.tokenize("one (   two(three)four  )");
        assertArrayEquals(tokens, result);
    }

}
