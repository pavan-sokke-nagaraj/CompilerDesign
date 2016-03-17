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

		String inputFile = inputDir + "SYNTAX_TEST.txt";

		Lexer lexer = new Lexer(inputFile);


		SynatcticParser sParser = new SynatcticParser(lexer);
		sParser.parse();

	}

}
