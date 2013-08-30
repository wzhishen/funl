package evaluator;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * Tokenizer for the CIT 594 Logo project, Spring 2010.
 * 
 * @author David Matuszek, modified by Zhishen Wen
 * @version February 14, 2010
 */
public class Tokenizer {
    private StreamTokenizer tokenizer;
    private Token lastToken = null;
    private final Token EOL = new Token(TokenType.EOL, "\n");
    private final Token EOI = new Token(TokenType.EOI, "");
    private static String[] keywords =
        ("def val if else then end read").split(" ");

    /**
     * Creates a Tokenizer with an input source.
     * 
     * @param reader The input source to be tokenized.
     */
    public Tokenizer(Reader reader) {
        tokenizer = new StreamTokenizer(reader);
        tokenizer.whitespaceChars((char) 0, (char) 31);
        tokenizer.eolIsSignificant(true);
        tokenizer.ordinaryChars('!', '/');
        tokenizer.ordinaryChars(':', '@');
        tokenizer.ordinaryChars('[', '`');
        tokenizer.ordinaryChars('{', (char) 127);
        tokenizer.wordChars('_', '_');
        tokenizer.slashSlashComments(true);
        tokenizer.slashStarComments(true);
        tokenizer.quoteChar('"');
        
        lastToken = next();
        pushBack();
    }
    
    /**
     * Creates a Tokenizer for a specific string.
     * 
     * @param input The string to be tokenized.
     */
    public Tokenizer(String input) {
        this(new StringReader(input));
    }

    /**
     * Returns the next Token.
     * @return The next Token.
     */
    public Token next() {
        int tokenType;

        try {
            tokenType = tokenizer.nextToken();

            switch (tokenType) {
                case StreamTokenizer.TT_WORD:
                        if (member(tokenizer.sval, keywords)) {
                            lastToken = new Token(TokenType.KEYWORD, tokenizer.sval);
                        } else {
                            lastToken = new Token(TokenType.NAME, tokenizer.sval);
                        }
                        break;

                case '"':
                    lastToken = new Token(TokenType.STRING, tokenizer.sval);
                    break;

                case StreamTokenizer.TT_NUMBER:
                    lastToken = new Token(TokenType.NUMBER, tokenizer.nval + "");
                    break;

                case StreamTokenizer.TT_EOL:
                    lastToken = EOL;
                    break;

                case StreamTokenizer.TT_EOF:
                    lastToken = EOI;
                    break;
                default:
                    lastToken = new Token(TokenType.SYMBOL, "" + (char) tokenType);
                    break;
            }
        }
        catch (IOException e) {
            lastToken = new Token(TokenType.ERROR, e.getMessage());
        }
        return lastToken;
    }

    /**
     * @param sval
     * @param keywords2
     * @return
     */
    private boolean member(String sval, String[] keywords) {
        for (int i = 0; i < keywords.length; i++) {
            if (sval.equals(keywords[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * "Puts back" the Token that was most recently returned, so that it will
     * be returned again on the next call to next().
     */
    public void pushBack() {
        tokenizer.pushBack();
    }
    
    public static void main(String[] args) {///////////////////////
        Tokenizer t = new Tokenizer("read ^.79u \"quoted string\"");
        Token to = t.next();
        System.out.println(to+" "+to.getType());
        to = t.next();
        System.out.println(to+" "+to.getType());
        to = t.next();
        System.out.println(to+" "+to.getType());
        to = t.next();
        System.out.println(to+" "+to.getType());
        to = t.next();
        System.out.println(to+" "+to.getType());
        to = t.next();
        System.out.println(to+" "+to.getType());
        to = t.next();
        System.out.println(to+" "+to.getType());
    }
}
