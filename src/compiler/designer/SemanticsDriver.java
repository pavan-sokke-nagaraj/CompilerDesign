package compiler.designer;

import java.io.IOException;

import compiler.lexer.Lexer;
import compiler.semantics.SymbolTable;
import compiler.syntacticAnalysis.SynatcticParser;
import compiler.utils.PrintUtil;

/**
 * @author Pavan Sokke Nagaraj <pavansn8@gmail.com> Assignment 1: Driver class
 *         to extract all the tokens
 *
 */
public class SemanticsDriver {

	/**
	 * main function to generate tokens, errors, and comment section
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		String inputDir = System.getProperty("user.dir") + "\\Input\\";

		String inputFile = inputDir + "TEST.txt";

		Lexer lexer = new Lexer(inputFile);
		// 1st order parsing
		// PrintUtil.isLog = true;
		SynatcticParser sParser = new SynatcticParser(lexer);
		sParser.parse();
		SymbolTable firstTable = sParser.semantics.mainTable.clone();
		PrintUtil.isLog = true;
		// 2nd order parsing
		lexer = new Lexer(inputFile);
		sParser = new SynatcticParser(lexer);
		sParser.parse(firstTable);
		sParser.semantics.printSymbolTable();
		sParser.semantics.printData();
		sParser.semantics.printCode();
	}

}
