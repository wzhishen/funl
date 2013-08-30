package evaluator;

import java.util.*;

/**
 * Class Tree that handles basic tree data structure.
 * @author Zhishen Wen, some adapted from David Matuszek
 * @version Feb 1, 2013
 * @param <V> The type of the elements that can be held in this tree.
 */
public class Tree<V> {

    private V value;
    ArrayList<Tree<V>> children;
    
    /**
     * Constructor for the tree.
     * @param value The value in current node.
     * @param children Any number of children to be added to this node.
     * @throws IllegalArgumentException If children contain null node,
     * or adding null value as immediate node, or adding children will
     * lead to formation of circular tree.
     */
    @SafeVarargs
    public Tree(V value, Tree<V>... children) {
        this.value = value;
        if (children == null) 
            throw new IllegalArgumentException("ERROR: Null Subtree");
        for (Tree<V> child : children) {
            if (child == null) 
                throw new IllegalArgumentException("ERROR: Null Nodes Found");
            if (child.contains(this))
                throw new IllegalArgumentException("ERROR: Circular Tree");
        }
            
        this.children = new ArrayList<Tree<V>>(
                Arrays.asList(children));
    }
    
    /**
     * Sets value to this node.
     * @param value The value to be set.
     */
    public void setValue(V value) {
        this.value = value;
    }
    
    /**
     * Gets the value in this node.
     * @return The value in this node.
     */
    public V getValue() {
        return value;
    }
    
    /**
     * Adds the child as the new index'th node of this Tree.
     * @param index The index for the node to be added.
     * @param child The node to be added.
     * @throws IllegalArgumentException If adding this node
     * will lead to formation of circular tree.
     * @throws IndexOutOfBoundsException If index is negative
     * or is greater than the current number of children of 
     * this node.
     */
    public void addChild(int index, Tree<V> child) {
        if (child.contains(this))
            throw new IllegalArgumentException();
        if (index < 0 || index > getNumberOfChildren())
            throw new IndexOutOfBoundsException();
        children.add(index, child);
    }
    
    /**
     * Overloaded version of addChild.
     * Adds the child to this node after any current children.
     * @param child The node to be added.
     */
    public void addChild(Tree<V> child) {
        addChild(getNumberOfChildren(), child);
    }
    
    /**
     * Adds the new children to this node after any current children.
     * @param children The nodes to be added.
     * @throws  IllegalArgumentException if adding these nodes will 
     * lead to formation of circular tree.
     */
    @SuppressWarnings("unchecked")
    public void addChildren(Tree<V>... children) {
        for (Tree<V> child : children)
            if (child.contains(this))
                throw new IllegalArgumentException();
        this.children.addAll(Arrays.asList(children));
    }
    
    /**
     * Returns the number of children that this node has.
     * @return the number of children.
     */
    public int getNumberOfChildren() {
        return children.size();
    }
    
    /**
     * Returns the index'th child of this node.
     * @param index The index for the node to be returned.
     * @return The index'th child.
     */
    public Tree<V> getChild(int index) {
        if (index < 0 || index >= getNumberOfChildren())
            throw new IndexOutOfBoundsException();
        return children.get(index);
    }
    
    /**
     * Returns an iterator for the children of this node.
     * @return An iterator for the children.
     */
    public Iterator<Tree<V>> iterator() {
        return children.iterator();
    }
    
    /**
     * Search this tree for a node that is equal to target node.
     * @param node The node to be searched.
     * @return True if node is found in this tree; 
     * false otherwise.
     */
    boolean contains(Tree<V> node) {
        if (this == node) return true;
        if (getNumberOfChildren() == 0) return false;
        for (Tree<V> child : children)
            if (child.contains(node))
                return true;
        return false;
    }
    
    /**
     * Translates a String description of a tree into a Tree<String> object.
     * @param treeDescription The tree description to be parsed.
     * @return The tree that corresponds to the tree description.
     * @throws IllegalArgumentException If tree description leads to 
     * formation of a tree with no root, or multiple roots, or does NOT meet
     * the following standard:
     * The treeDescription has the form value(child child ... child), where 
     * a value is any sequence of characters not containing parentheses or 
     * whitespace, and each child is either just a (String) value or is 
     * another treeDescription. Whitespace is optional except where needed 
     * to separate values.
     */
    public static Tree<String> parseOldVersion(String treeDescription) {
        String[] tokens = tokenize(treeDescription.trim());
        if (tokens[0].equals("(") || tokens[0].equals(")"))
            throw new IllegalArgumentException("ERROR: Root Empty");
        if (tokens.length > 1 && !tokens[1].equals("(") && !tokens[1].equals(")"))
            throw new IllegalArgumentException("ERROR: Multiple Roots");
        
        Tree<String> parentNode = new Tree<String>(tokens[0]);
        Tree<String> currentNode = parentNode;
        Stack<Tree<String>> operandStack = new Stack<Tree<String>>();
        
        for (int i = 1; i < tokens.length; ++i)
            if (tokens[i].equals("(")) {
                operandStack.push(currentNode);
                parentNode = currentNode;
            }
            else if (tokens[i].equals(")")) {
                if (operandStack.size() != 0) operandStack.pop();
                if (operandStack.size() == 0) return parentNode;
                parentNode = operandStack.peek();
            }
            else {
                currentNode = new Tree<String>(tokens[i]);
                parentNode.addChild(currentNode);
            }
        throw new IllegalArgumentException("ERROR: Tree Description Not Understood");
    }
    
    /**
     * Prints a multiline version of the tree.
     */
    public void print() {
        String[] tokens = tokenize(this.toString());
        Stack<String> operatorStack = new Stack<String>();
        
        for (String token : tokens)
            if (token.equals("(")) {
                operatorStack.push(token);
            }
            else if (token.equals(")")) {
                operatorStack.pop();
            }
            else {
                for (int i = 0; i < operatorStack.size(); ++i)
                    System.out.print("|  ");
                System.out.println(token);
            }
    }
    
    /**
     * Returns a string representation of this tree.
     * @returns A String representing this tree.
     * The returned String would be in the same format 
     * as the parse method expects as input.
     */
    @Override
    public String toString() {
        String buffer = getValue() + "(";
        if (getNumberOfChildren() == 0)
            return getValue() + " ";
        for (Tree<V> child : children) {
            buffer += child.toString();
        }
        return buffer.trim() + ")";
    }
    
    /**
     * Determines whether that tree is equal to this tree.
     * @param obj The object to be compared with.
     * @return True if and only if a tree has the same shape 
     * and contains the same values as this tree;
     * false otherwise.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (!(obj instanceof Tree)) return false;
        Tree<V> that = (Tree<V>) obj;
        return this.toString().equals(that.toString());
    }
    
    /**
     * Parses a string of the general form
     * <code>value(child child ... child)</code> and returns the
     * corresponding tree. Children are separated by spaces.
     * Node values are all Strings.
     * 
     * @param s The String to be parsed.
     * @return The resultant Tree&lt;String&lt;.
     * @throws IllegalArgumentException
     *             If problems are detected in the input string.
     */
    public static Tree<String> parse(String s) throws IllegalArgumentException {
        checkParentheses(s);
        StringTokenizer tokenizer = new StringTokenizer(s, " ()", true);
        List<String> tokens = new LinkedList<String>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.trim().length() == 0)
                continue;
            tokens.add(token);
        }
        Tree<String> result = parse(tokens);
        if (tokens.size() > 0) {
            throw new IllegalArgumentException("Leftover tokens: " + tokens);
        }
        return result;
    }
    
    /*
     ************************ helpers ************************
     */
    /**
     * Tokenizes a string into an array of tokens containing
     * only string representation of unsigned integers and 
     * parentheses.
     * @param s The string to be tokenized.
     * @return An array of tokens.
     */
    public static String[] tokenize(String s) {
        String buffer = s;
        buffer = buffer.replace("(", " ( ");
        buffer = buffer.replace(")", " ) ");
        return buffer.split("\\s+");
    }
    
    /**
     * Parses and returns one tree, consisting of a value and possible children
     * (enclosed in parentheses), starting at the first element of tokens.
     * Returns null if this token is a close parenthesis, or if there are no
     * more tokens.
     * 
     * @param tokens
     *            The tokens that describe a Tree.
     * @return The Tree described by the tokens.
     * @throws IllegalArgumentException
     *             If problems are detected in the input list.
     */
    private static Tree<String> parse(List<String> tokens)
            throws IllegalArgumentException {
        // No tokens -- return null
        if (tokens.size() == 0) {
            return null;
        }
        // Get the next token and remove it from the list
        String token = tokens.remove(0);
        // If the token is an open parenthesis
        if (token.equals("(")) {
            throw new IllegalArgumentException(
                "Unexpected open parenthesis before " + tokens);
        }
        // If the token is a close parenthesis, we are at the end of a list of
        // children
        if (token.equals(")")) {
            return null;
        }
        // Make a tree with this token as its value
        Tree<String> tree = new Tree<String>(token);
        // Check for children
        if (tokens.size() > 0 && tokens.get(0).equals("(")) {
            tokens.remove(0);
            Tree<String> child;
            while ((child = parse(tokens)) != null) {
                tree.addChildren(child);
            }
        }
        return tree;
    }
    
    /**
     * Checks whether parentheses in s are balanced.
     * @param s The string to check.
     * @throws IllegalArgumentException If parentheses are not balanced.
     */
    private static void checkParentheses(String s) throws IllegalArgumentException {
        int depth = 0;
        char ch = ' ';
        for (int i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            if (ch == '(') {
                depth += 1;
            }
            if (ch == ')') {
                depth -= 1;
                if (depth < 0) {
                    throw new IllegalArgumentException("Too many closing parentheses: " + s);
                }
            }
        }
        if (depth != 0) {
            throw new IllegalArgumentException("Not enough closing parentheses: " + s);
        }
    }
    
}
