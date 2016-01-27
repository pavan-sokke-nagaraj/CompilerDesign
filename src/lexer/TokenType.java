package lexer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

public class TokenType {

	public static Collection<String> reserveWords = Arrays.asList(new String[] {
			"if", "then", "else", "for", "class", "int", "float", "get", "put",
			"return" });
	public final static Vector RESERVE_WORD = new Vector(reserveWords);

	public final static String T_OP_AND = "and";
	public final static String T_OP_OR = "or";
	public final static String T_OP_NOT = "not";

	public final static char T_OP_ADD = '+';
	public final static char T_OP_SUB = '-';
	public final static char T_OP_MUL = '*';
	public final static char T_OP_DIV = '/';

	public final static char T_OP_EQUAL = '=';
	public final static char T_OP_GT = '>';
	public final static char T_OP_LT = '<';

	public final static char T_DEL_R_LPAREN = '(';
	public final static char T_DEL_R_RPAREN = ')';
	public final static char T_DEL_S_LPAREN = '[';
	public final static char T_DEL_S_RPAREN = ']';
	public final static char T_DEL_C_LPAREN = '{';
	public final static char T_DEL_C_RPAREN = '}';
	public final static char T_DEL_SEMICOLON = ';';
	public final static char T_DEL_COMMA = ',';
	public final static char T_DEL_DOT = '.';

}
