package compiler.designer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType.TOKENTYPE;
import compiler.syntacticAnalysis.SynatcticParser;

/**
 * @author Pavan Sokke Nagaraj <pavansn8@gmail.com> Assignment 1: Driver class
 *         to extract all the tokens
 *
 */
public class SyntacticDriver {

	/**
	 * main function to generate tokens, errors, and comment section
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		String inputDir = System.getProperty("user.dir") + "\\Input\\";
		String outputDir = System.getProperty("user.dir") + "\\Output\\";

		String inputFile = inputDir + "SYNTAX_TEST2_ERR.txt";
		String tokenFile = outputDir + "TOKEN.txt";
		String errorFile = outputDir + "ERROR.txt";
		String commentFile = outputDir + "COMMENT.txt";

		Lexer lexer = new Lexer(inputFile);

		// Token token = null;
		// do {
		// token = lexer.getNextToken();
		// } while (token.getValue() != "EOF");

		SynatcticParser sParser = new SynatcticParser(lexer);
		sParser.parse();

		lexer.writeToFile(lexer.tokens, tokenFile);
		lexer.writeToFile(lexer.errors, errorFile);
		lexer.writeToFile(lexer.comments, commentFile);
	}

}
