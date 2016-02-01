package CompilerDesigner;

import java.util.HashMap;
import java.util.Vector;

import lexer.Lexer;
import lexer.Token;

/**
 * @author Pavan Sokke Nagaraj <pavansn8@gmail.com>
 * Assignment 1: Driver class to extract all the tokens 
 *
 */
public class Driver {

	/**
	 * main function to generate tokens, errors, and comment section
	 * @param args
	 */
	public static void main(String[] args) {

		String inputFile = "COMMENTS_TEST.txt";
		String tokenFile = "TOKEN.txt";
		String errorFile = "ERROR.txt";
		String commentFile = "COMMENT.txt";

		HashMap<String, Vector> tokenMap = new HashMap<String, Vector>();

		Lexer lexer = new Lexer();
		tokenMap = lexer.scanInputFile(inputFile);

		Vector<Token> tokens = tokenMap.get("TOKENS");
		Vector<Token> errors = tokenMap.get("ERRORS");
		Vector<Token> comments = tokenMap.get("COMMENTS");

		lexer.writeToFile(tokens, tokenFile);
		lexer.writeToFile(errors, errorFile);
		lexer.writeToFile(comments, commentFile);
	}

}
