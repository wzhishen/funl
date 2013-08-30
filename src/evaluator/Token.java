/**
 * Tokenizer for CIT594, Spring 2010.
 */
package evaluator;

/**
 * @author David Matuszek, modified by Zhishen Wen
 * @version February 14, 2010
 */
public class Token {
    private static boolean INCLUDE_TYPE_IN_PRINT_STRING = false;
    private TokenType type;
    private String value;
    
    /**
     * Returns the type of this Token.
     * @return The type of this Token.
     */
    public TokenType getType() {
        return type;
    }
    
    /**
     * Returns the value of this Token.
     * @return The value of this Token.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Specifies whether the type should be included in the result of <code>toString()</code>.
     * @param include <code>true</code> if the type should be included in <code>toString()</code>.
     */
    public static void includeType(boolean include) {
        INCLUDE_TYPE_IN_PRINT_STRING = include;
    }
    
    /**
     * Creates a token.
     * @param type The type of this new Token.
     * @param value The value of this new Token.
     */
    public Token(TokenType type, String value) {
        super();
        assert type != null && value != null;
        this.type = type;
        this.value = value;
    }

    /**
     * Returns a string of the form type[value].
     * @return A representation of this Token.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (INCLUDE_TYPE_IN_PRINT_STRING) {
            return type + ":" + value;
        }
        else return value;
    }
    
    /**
     * Tests whether this Token equals the input.
     * @param o The thing to be tested against this Token.
     * @return <code>true</code> iff the argument equals this Token.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Token) {
            Token that = (Token) o;
            return this.type == that.type && this.value.equals(that.value);
        }
        return false;
    }
    
    /**
     * Returns a hash code for this token.
     * @return A hash code for this Token.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
