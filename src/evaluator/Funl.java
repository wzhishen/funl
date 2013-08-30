package evaluator;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.swing.JFileChooser;

/**
 * Class for Funl evaluator
 * @author Zhishen Wen
 * @version Mar 19, 2013
 */
public class Funl {
    /* members */
    private boolean inREPL = false;
    private boolean readCalled = false;
    private File file;
    private JFileChooser chooser = new JFileChooser();
    /* main data structures */
    HashMap<String, Tree<Token>> functions = new HashMap<String, Tree<Token>>();
    Stack<HashMap<String, Tree<Token>>> valueStack = new Stack<HashMap<String, Tree<Token>>>();
    /* static members */
    private static Scanner sc = new Scanner(System.in);
    private static int scopeDepth = 0;
    private static final int MAX_SCOPE_DEPTH = 1000; // limit for nested scope depth
    
    /**
     * Takes a Funl program (one or more function definitions), 
     * parse it, and save the functions.
     * @param functionDefinitions String to be parsed.
     */
    public void define(String functionDefinitions) {
        Parser parser = new Parser(functionDefinitions);
        parser.program();
        functions = parser.functions;
    }
    
    /**
     * Takes a Funl expression, evaluates it, and returns 
     * another Funl expression in a tree form.
     * @param expr Funl expression to be parsed.
     * @return Another Funl expression.
     */
    public Tree<Token> eval(Tree<Token> expr) {
        /* evaluate single function definition */
        if (expr.getValue().getValue().equals("def")) {
            return evalFunctionDef(expr);
        }
        /* evaluate expressions */
        if (expr.getValue().getValue().equals("$seq")) {
            return evalExprs(expr);
        }
        /* evaluate expression */
        // evaluate function call
        if (expr.getValue().getValue().equals("$call")) {
            return evalFunctionCall(expr);
        }
        // evaluate value definition
        if (expr.getValue().getValue().equals("val")) {
            return evalValueDef(expr);
        }
        // evaluate arithmetic expression
        if ("+-*/".contains(expr.getValue().getValue())) {
            return evalArithmeticExpr(expr);
        }
        // evaluate a single name or number
        if (expr.getValue().getType() == TokenType.NAME ||
                expr.getValue().getType() == TokenType.NUMBER) {
            return evalValue(expr);
        }
        // evaluate if expression
        if (expr.getValue().getValue().equals("if" )) {
            return evalIfExpr(expr);
        }
        // evaluate read expression
        if (expr.getValue().getValue().equals("read")) {
            return evalReadExpr(expr);
        }
        assert false;
        return null;
    }
    
    /**
     * Evaluates a function definition.
     * @param expr Funl expression to be parsed.
     * @return Another Funl expression.
     */
    private Tree<Token> evalFunctionDef(Tree<Token> expr) {
        functions.put(expr.getChild(0).getValue().getValue(), expr);
        return expr;
    }
    
    /**
     * Evaluates a function call.
     * @param expr Funl expression to be parsed.
     * @return Another Funl expression.
     */
    private Tree<Token> evalFunctionCall(Tree<Token> expr) {
        // check function name
        String funcName = expr.getChild(0).getValue().getValue();
        if ((!valueStack.isEmpty() && !valueStack.peek().containsKey(funcName)) && 
                !functions.containsKey(funcName))
            throw new RuntimeException(
                    "Runtime Exception: cannot resolve '" + funcName + "' to a function name.");
        // check number of params
        Tree<Token> func;
        if (!valueStack.isEmpty() && valueStack.peek().containsKey(funcName))
            func = fetch(funcName);
        else
            func = functions.get(funcName);
        int argNum = expr.getChild(1).getNumberOfChildren();
        int paramNum = func.getChild(1).getNumberOfChildren();
        if (argNum != paramNum)
            throw new RuntimeException(
                    "Runtime Exception: number of argument(s) not match for function '" + funcName + "'.\n" +
                    "Expected: " + paramNum + ", actual: " + argNum);
        // evaluate each argument
        HashMap<String, Tree<Token>> map = new HashMap<String, Tree<Token>>();
        for (int i = 0; i < argNum; ++i) {
            map.put(
                    func.getChild(1).getChild(i).getValue().getValue(),
                    eval(expr.getChild(1).getChild(i))
                    );
        }
        // create new scope
        createNewScope();
        valueStack.peek().putAll(map);
        // execute function body and get return value
        Tree<Token> retVal = eval(func.getChild(2));
        // discard new scope
        removeNewScope();
        return retVal;
    }
    
    /**
     * Evaluates a read expression.
     * @param expr Funl expression to be parsed.
     * @return Another Funl expression.
     */
    private Tree<Token> evalReadExpr(Tree<Token> expr) {
        readCalled = true;
        msg(expr.getChild(0).getValue().getValue());
        try {
            String res = sc.nextDouble() + "";
            return new Tree<Token>(new Token(TokenType.NUMBER, res));
        }
        catch (InputMismatchException e) {
            throw new RuntimeException(
                    "Runtime Exception: only accept number for read expressions.");
        }
    }
    
    /**
     * Evaluates an if expression.
     * @param expr Funl expression to be parsed.
     * @return Another Funl expression.
     */
    private Tree<Token> evalIfExpr(Tree<Token> expr) {
        try {
            String cond = eval(expr.getChild(0)).getValue().getValue();
            double condition = Double.parseDouble(cond);
            if (condition > 0)
                return eval(expr.getChild(1));
            else
                return eval(expr.getChild(2));
        }
        catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Runtime Exception: condition part of an if expression evaluates to a function.");
        }
    }
    
    /**
     * Evaluates a value from given name.
     * @param expr Funl expression to be parsed.
     * @return Another Funl expression.
     */
    private Tree<Token> evalValue(Tree<Token> expr) {
        if (expr.getValue().getType() == TokenType.NAME) {
            String varName = expr.getValue().getValue();
            if (!containsKeyOnStack(varName) && !functions.containsKey(varName))
                throw new RuntimeException(
                        "Runtime Exception: cannot resolve '" + varName + "' to a variable or function name.");
            return functions.containsKey(varName) ? functions.get(varName) : fetch(varName);
        }
        return expr;
    }
    
    /**
     * Evaluates an arithmetic expression.
     * @param expr Funl expression to be parsed.
     * @return Another Funl expression.
     */
    private Tree<Token> evalArithmeticExpr(Tree<Token> expr) {
        try {
            if (expr.getValue().getValue().equals("+")) {
                String left = eval(expr.getChild(0)).getValue().getValue();
                String right = eval(expr.getChild(1)).getValue().getValue();
                double res = Double.parseDouble(left) + Double.parseDouble(right);
                return new Tree<Token>(new Token(TokenType.NUMBER, res + ""));
            }
            if (expr.getValue().getValue().equals("-")) {
                String left = eval(expr.getChild(0)).getValue().getValue();
                String right = eval(expr.getChild(1)).getValue().getValue();
                double res = Double.parseDouble(left) - Double.parseDouble(right);
                return new Tree<Token>(new Token(TokenType.NUMBER, res + ""));
            }
            if (expr.getValue().getValue().equals("*")) {
                String left = eval(expr.getChild(0)).getValue().getValue();
                String right = eval(expr.getChild(1)).getValue().getValue();
                double res = Double.parseDouble(left) * Double.parseDouble(right);
                return new Tree<Token>(new Token(TokenType.NUMBER, res + ""));
            }
            if (expr.getValue().getValue().equals("/")) {
                String left = eval(expr.getChild(0)).getValue().getValue();
                String right = eval(expr.getChild(1)).getValue().getValue();
                double res = Double.parseDouble(left) / Double.parseDouble(right);
                return new Tree<Token>(new Token(TokenType.NUMBER, res + ""));
            }
            assert false;
            return null;
        }
        catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Runtime Exception: arithmetic operand evaluates to a function.");
        }
    }
    
    /**
     * Evaluates a value definition.
     * @param expr Funl expression to be parsed.
     * @return Another Funl expression.
     */
    private Tree<Token> evalValueDef(Tree<Token> expr) {
        String varName = expr.getChild(0).getValue().getValue();
        if (containsKeyOnStack(varName) && !inREPL)
            throw new RuntimeException("Runtime Exception: variable with name '" + varName + "' already exists");
        if (functions.containsKey(varName))
            throw new RuntimeException("Runtime Exception: function with name '" + varName + "' already exists");
        Tree<Token> retVal = eval(expr.getChild(1));
        store(varName, retVal);
        return retVal;
    }
    
    /**
     * Evaluates expressions.
     * @param expr Funl expression to be parsed.
     * @return Another Funl expression.
     */
    private Tree<Token> evalExprs(Tree<Token> expr) {
        for (int i = 0; i < expr.getNumberOfChildren() - 1; ++i)
            eval(expr.getChild(i));
        return eval(expr.getChild(expr.getNumberOfChildren() - 1));
    }
    
    /**
     * Main method to run REPL.
     * @param args Command line args.
     */
    public static void main(String[] args) {
        // init REPL
        Funl funl = new Funl();
        funl.setREPL(true);
        funl.createNewScope();
        msg(introMsg()); // print introductory message
        // run REPL
        prompt();
        while (true) {
            try {
                String input = sc.nextLine().trim();
                if (funl.readCalled) {
                    funl.readCalled = false;
                    continue;
                }
                if (input.isEmpty()) {
                    prompt();
                    continue;
                }
                if (input.equals("quit")) 
                    break;
                if (input.equals("load")) {
                    funl.load();
                    prompt();
                    continue;
                }
                Tree<Token> in = funl.parseInput(input);
                if (in == null) msg("Syntax Error: expression '" + input + "' not understood.");
                else msg(funl.eval(in) + "");
            }
            catch (Exception e) {
                msg(e.getMessage());
            }
            prompt();
        }
        // destroy
        sc.close();
        funl.clearScope();
        msg("Quit Funl REPL.");
    }
    
    /**
     * Invokes a JFileChooser to read in a Funl program
     * from a file.
     */
    private void load() {
        String funcDefs;
        setREPL(false);
        try {
            chooser.setDialogTitle("Read in a Funl Program:");
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) 
                file = chooser.getSelectedFile();
            else
                return;
            funcDefs = read(new BufferedReader(new FileReader(file)));
        }
        catch (FileNotFoundException e) {
            System.out.println("Input Error: file not found.");
            return;
        }
        catch (IOException e) {
            System.out.println("Input Error: unable to read this file.");
            return;
        }
        // backup for potential recovery
        HashMap<String, Tree<Token>> oldFunctions = functions;
        define(funcDefs);
        if (!functions.isEmpty()) {
            clearScope();
            createNewScope();
            msg(functions.keySet().size() + " functions loaded successfully:");
            printLoadedFunc();
        }
        else {
            functions = oldFunctions;
            msg("Input Error: file format not supported.");
        }
        setREPL(true);
    }
    
    /**
     * Parses a string into a Funl expression in a tree form
     * @param s String to be parsed.
     * @return A tree representing a Funl expression.
     */
    Tree<Token> parseInput(String s) {
        Parser parser = new Parser(s);
        if (s.startsWith("def")) parser.functionDefinition();
        else if (s.contains(",")) parser.expressions();
        else parser.expression();
        return parser.stack.isEmpty() ? null : parser.stack.peek();
    }
    
    /**
     * Reads in characters from a Reader.
     * @param reader Reader to be read from.
     * @return A string containing characters read.
     * @throws IOException
     */
    private String read(Reader reader) throws IOException {
        String buffer = "";
        int ch;
        while ((ch = reader.read()) != -1) {
            buffer += (char) ch;
        }
        reader.close();
        return buffer.replace(System.lineSeparator(), " ");
    }
    
    /**
     * Checks whether the key exists in every 'scope'.
     * @param key Key to be checked.
     * @return True if this key is found in every 'scope';
     * false otherwise.
     */
    private boolean containsKeyOnStack(String key) {
        for (int i = 0; i < valueStack.size(); ++i)
            if ((valueStack.get(i)).containsKey(key))
                return true;
        return false;
    }
    
    /**
     * Fetches a specified value from the topmost HashMap.
     * @param name Key for the value to be retrieved.
     * @return The value fetched.
     */
    private Tree<Token> fetch(String name) {
        return valueStack.peek().get(name);
    }
    
    /**
     * Stores a value with specified key to the topmost HashMap.
     * @param name Key for the value to be stored.
     * @param value Value for the value to be stored.
     */
    private void store(String name, Tree<Token> value) {
        valueStack.peek().put(name, value);
    }
    
    /**
     * Pushes a new HashMap onto the stack, ie., creates
     * a new scope.
     */
    private void createNewScope() {
        try {
            if (++scopeDepth >= MAX_SCOPE_DEPTH)
                throw new StackOverflowError();
            valueStack.push(new HashMap<String, Tree<Token>>());
        }
        catch (StackOverflowError e) {
            while (valueStack.size() > 1)
                removeNewScope();
            throw new RuntimeException(
                    "Runtime Exception: exceeded maximum nested scope depth of " + MAX_SCOPE_DEPTH);
        }
    }
    
    /**
     * Pops the topmost HashMap from the stack, ie., 
     * removes a new scope.
     */
    private HashMap<String, Tree<Token>> removeNewScope() {
        if (valueStack.isEmpty())
                return null;
        --scopeDepth;
        return valueStack.pop();
    }
    
    /**
     * Pops all HashMaps from the stack, ie., 
     * removes all scopes.
     */
    private void clearScope() {
        scopeDepth = 0;
        valueStack.clear();
    }
    
    /**
     * Sets to indicate if REPL is run.
     * @param b Boolean to be set to 
     * indicate if REPL is run.
     */
    private void setREPL(boolean b) {
        inREPL = b;
    }
    
    /**
     * Prints loaded functions to the console.
     */
    private void printLoadedFunc() {
        String s = ""; int i = 0;
        for (String key : functions.keySet()) {
            s += key + ", ";
            if (++i % 9 == 0) s += "\n";
        }
        msg(s.substring(0, s.length() - 2));
    }
    
    /**
     * Prints a prompt.
     */
    private static void prompt() {
        msg(System.getProperty("user.name") + "@Funl>>> ", false);
    }
    
    /**
     * Prints a message to the console.
     * @param msg Message to be printed.
     * @param b Set to true if the newline character ought 
     * to be appended to the message; false otherwiese.
     */
    private static void msg(String msg, boolean b) {
        if (b) System.out.println(msg);
        else System.out.print(msg);
    }
    
    /**
     * Prints a message with a newline character 
     * appended at the end to the console.
     * @param msg Message to be printed.
     */
    private static void msg(String msg) {
        msg(msg, true);
    }
    
    /**
     * Prints introductory message.
     */
    private static final String introMsg() {
        return
                "===============================================================\n" +
                "    *  Funl - a Simple Functional Language\n" +
                "    *  REPL (ver 1.0.0) on " + 
                System.getProperty("os.name") + 
                System.getProperty("os.version") + "(" +
                System.getProperty("os.arch").toUpperCase() + ")\n" +
                "    *  " + new SimpleDateFormat("MMM dd yyyy, HH:mm:ss").format(new Date()) + "\n" +
                "    *  Type \"load\" to load a Funl program, \"quit\" to exit.\n" +
                "       Current version supports function definition typed\n" +
                "       directly in the REPL.\n" +
                "===============================================================";
    }
}
