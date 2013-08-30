package evaluator;

import java.util.HashMap;
import java.util.Stack;

/**
 * Class for Funl parser
 * @author Zhishen Wen
 * @version Mar 19, 2013
 */
public class Parser {
    Stack<Tree<Token>> stack;
    HashMap<String, Tree<Token>> functions;
    Tokenizer tokenizer;
    
    /**
     * Constructor for Parser.
     * @param s String to be parsed.
     */
    public Parser(String s) {
        stack = new Stack<Tree<Token>>();
        functions = new HashMap<String, Tree<Token>>();
        tokenizer = new Tokenizer(s);
    }
    
    /**
     * Parses a Funl program.
     */
    public void program() {
        while (functionDefinition()) {
            Tree<Token> function = stack.pop();
            functions.put(function.getChild(0).getValue().getValue(), function);
        }
    }
    
    /**
     * Parses a function definition.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean functionDefinition() {
        if (!keyword("def")) return false;
        stack.push(new Tree<Token>(new Token(TokenType.KEYWORD, "def")));
        if (!name()) error("No name after keyword 'def'");
        Tree<Token> param = new Tree<Token>(new Token(TokenType.KEYWORD, "$seq"));
        while (parameter()) {
            param.addChild(stack.pop());
        }
        stack.push(param);
        if (!symbol("=")) error("No equal sign ('=') after parameters or in wrong position");
        if (!expressions()) error("No expressions after equal sign ('=')");
        if (!keyword("end")) error("Function definition ends without keyword 'end'");
        makeTree(4, 3, 2, 1);
        return true;
    }
    
    /**
     * Parses a parameter.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean parameter() {
        return name();
    }
    
    /**
     * Parses expressions.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean expressions() {
        if (!expression()) return false;
        Tree<Token> exp = stack.pop();
        Tree<Token> seq = new Tree<Token>(new Token(TokenType.KEYWORD, "$seq"));
        seq.addChild(exp);
        stack.push(seq);
        while (symbol(",")) {
            if (!expression())
                error("No expression after comma sign (',')");
            makeTree(2, 1);
        }
        return true;
    }
    
    /**
     * Parses an expression.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean expression() {
        return valueDefinition() || addTerm();
    }
    
    /**
     * Parses an add term.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    private boolean addTerm() {
        if (!term()) return false;
        while (addOperator()) {
            if (!term()) error("No term after '+' or '-'");
            makeTree(2, 3, 1);
        }
        return true;
    }
    
    /**
     * Parses a value definition.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean valueDefinition() {
        if (!keyword("val")) return false;
        stack.push(new Tree<Token>(new Token(TokenType.KEYWORD, "val")));
        if (!name()) error("No name after keyword 'val'");
        if (!symbol("=")) error("No equal sign ('=') after name");
        if (!expression()) error("No expression after equal sign ('=')");
        makeTree(3, 2, 1);
        return true;
    }
    
    /**
     * Parses a term.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean term() {
        if (!factor()) return false;
        while (multiplyOperator()) {
            if (!factor()) error("No factor after '*' or '/'");
            makeTree(2, 3, 1);
        }
        return true;
    }
    
    /**
     * Parses a factor.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean factor() {
        return nameOrFunctionCall() ||
                ifExpression() ||
                number() ||
                read() ||
                parenthesizedExpression();
    }
    
    /**
     * Parses a name or a function call.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    private boolean nameOrFunctionCall() {
        // normal name
        if (!name()) return false;
        // function call
        if (symbol("(")) {
            stack.push(new Tree<Token>(new Token(TokenType.KEYWORD, "$call")));
            if(!expressions())
                stack.push(new Tree<Token>(new Token(TokenType.KEYWORD, "$seq")));
            if (!symbol(")")) 
                error("No closing parentheses (')') after expressions in a function call");
            makeTree(2, 3, 1);
            return true;
        }
        return true;
    }
    
    /**
     * Parses an if expression.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    private boolean ifExpression() {
        if (!keyword("if")) return false;
        stack.push(new Tree<Token>(new Token(TokenType.KEYWORD, "if")));
        if (!expressions()) error("No expressions after keyword 'if'");
        if (!keyword("then")) error("No keyword 'then' after expressions");
        if (!expressions()) error("No expressions after keyword 'then'");
        if (!keyword("else")) error("No keyword 'else' after expressions");
        if (!expressions()) error("No expressions after keyword 'else'");
        if (!keyword("end")) error("No keyword 'end' after last expressions");
        makeTree(4, 3, 2, 1);
        return true;
    }
    
    /**
     * Parses a read expression.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    private boolean read() {
        if (!keyword("read")) return false;
        stack.push(new Tree<Token>(new Token(TokenType.KEYWORD, "read")));
        if (!quotedString()) error("No quoted string after keyword 'read'");
        makeTree(2, 1);
        return true;
    }
    
    /**
     * Parses a parenthesizedExpression.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    private boolean parenthesizedExpression() {
        if (!symbol("(")) return false;
        if (!expression()) error("No expression after opening parentheses ('(')");
        if (!symbol(")")) error("No closing parentheses (')') after expression");
        return true;
    }
    
    /**
     * Parses an add operator.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean addOperator() {
        return operater("+") || operater("-");
    }
    
    /**
     * Parses a multiply operator.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean multiplyOperator() {
        return operater("*") || operater("/");
    }
    
    /**
     * Parses an operator.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    private boolean operater(String op) {
        if (!symbol(op)) return false;
        stack.push(new Tree<Token>(new Token(TokenType.SYMBOL, op)));
        return true;
    }
    
    /**
     * Parses a number.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean number() {
        return nextTokenMatchesIgnoreVal(TokenType.NUMBER);
    }
    
    /**
     * Parses a quoted string.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    private boolean quotedString() {
        return nextTokenMatchesIgnoreVal(TokenType.STRING);
    }
    
    /**
     * Parses a name.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    public boolean name() {
        return nextTokenMatchesIgnoreVal(TokenType.NAME);
    }
    
    /**
     * Checks whether next token matches target type.
     * @return True if it is matched; false otherwise.
     */
    private boolean nextTokenMatchesIgnoreVal(TokenType type) {
        Token token = tokenizer.next();
        if (token.getType() == type) {
            stack.push(new Tree<Token>(token));
            return true;
        }
        tokenizer.pushBack();
        return false;
    }
    
    /**
     * Parses a keyword.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    private boolean keyword(String kw) {
        return nextTokenMatches(TokenType.KEYWORD, kw);
    }
    
    /**
     * Parses a symbol.
     * @return True if parsing is successful, ie., input
     * to be parsed has no Funl syntax errors; false otherwise.
     */
    private boolean symbol(String s) {
        return nextTokenMatches(TokenType.SYMBOL, s);
    }
    
    /**
     * Checks whether next token matches target type as well
     * as target value.
     * @return True if it is matched; false otherwise.
     */
    private boolean nextTokenMatches(TokenType type, String val) {
        Token token = tokenizer.next();
        if (type == token.getType() && val.equals(token.getValue()))
            return true;
        tokenizer.pushBack();
        return false;
    }
    
    /**
     * Builds a tree with given node indices.
     * @param rootInd The index of the root.
     * @param childInd The index or indices of the children.
     */
    private void makeTree(int rootInd, int... childInd) {
        Tree<Token> root = getStackItem(rootInd);
        for (int i = 0; i < childInd.length; ++i)
            root.addChild(getStackItem(childInd[i]));
        for (int i = 0; i < childInd.length + 1; ++i)
            stack.pop();
        stack.push(root);
    }
    
    /**
     * Fetches a specified item from the stack.
     * @param n Index for item to be retrieved.
     * @return The item retrieved.
     */
    private Tree<Token> getStackItem(int n) {
        return stack.get(stack.size() - n);
    }
    
    /**
     * Throws an IllegalArgumentException with
     * specified error message.
     * @param msg Error message to print.
     */
    private void error(String msg) {
        throw new IllegalArgumentException("Syntax Error: " + msg);
    }
}
