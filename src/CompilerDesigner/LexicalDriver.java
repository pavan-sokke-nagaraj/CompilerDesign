package CompilerDesigner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType.TOKENTYPE;

/**
 * @author Pavan Sokke Nagaraj <pavansn8@gmail.com> Assignment 1: Driver class
 *         to extract all the tokens
 *
 */
public class LexicalDriver {

	/**
	 * main function to generate tokens, errors, and comment section
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		String inputFile = "ALL_TEST.txt";
		String tokenFile = "TOKEN.txt";
		String errorFile = "ERROR.txt";
		String commentFile = "COMMENT.txt";

		Vector<Token> tokens = new Vector<Token>();
		Vector<Token> errors = new Vector<Token>();
		Vector<Token> comments = new Vector<Token>();

		Lexer lexer = new Lexer(inputFile);

		Token nextToken;

		do {
			nextToken = lexer.getNextToken();
			if (nextToken.getTokenType() == TOKENTYPE.TOKEN)
				tokens.add(nextToken);
			else if (nextToken.getTokenType() == TOKENTYPE.ERROR)
				errors.add(nextToken);
			else if (nextToken.getTokenType() == TOKENTYPE.COMMENT)
				comments.add(nextToken);
		} while (nextToken.getTokenType() != TOKENTYPE.EOF);

		lexer.writeToFile(tokens, tokenFile);
		lexer.writeToFile(errors, errorFile);
		lexer.writeToFile(comments, commentFile);
	}

}
