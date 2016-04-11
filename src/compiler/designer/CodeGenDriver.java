package compiler.designer;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import compiler.lexer.Lexer;
import compiler.semantics.SymbolTable;
import compiler.syntacticAnalysis.SynatcticParser;
import compiler.utils.PrintUtil;

/**
 * @author Pavan Sokke Nagaraj <pavansn8@gmail.com> Assignment 4: CodeGenDriver
 *         class for Lexical Analysis, Syntactic Analysis, Semantics Analysis
 *         and Code Generation
 *
 */
public class CodeGenDriver {

	/**
	 * main function to generate tokens, errors, and comment section Lexical
	 * Analysis, Syntactic Analysis, Semantics Analysis and Code Generation
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		String inputDir = System.getProperty("user.dir") + "\\Input\\";
		String logDir = System.getProperty("user.dir") + "\\logs\\";
		String logFileDir = "";
		String inputFile = "";

		// Input user - provided file name
		System.out.println("COMP 6421 Compiler Design");
		System.out.println("Enter the File Name:\t");
		Scanner scan = new Scanner(System.in);
		// String fileName = scan.nextLine();
		String fileName = "TEST.txt";

		inputFile = inputDir + fileName;

		String[] fileDir = fileName.split("\\.");
		logFileDir = logDir + fileDir[0];

		File file = new File(logFileDir);

		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println(logFileDir + " LOGS Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}

		PrintUtil.logFileDir = logFileDir + "\\";

		Lexer lexer = new Lexer(inputFile);
		// 1st order parsing
		// PrintUtil.isLog = true;
		SynatcticParser synatcticParser = new SynatcticParser(lexer);
		synatcticParser.parse();
		SymbolTable firstTable = synatcticParser.semantics.mainTable.clone();
		PrintUtil.isLog = true;
		// 2nd order parsing
		lexer = new Lexer(inputFile);
		synatcticParser = new SynatcticParser(lexer);
		synatcticParser.parse(firstTable);
		synatcticParser.semantics.printSymbolTable();
		synatcticParser.semantics.printData();
		synatcticParser.semantics.printCode();
	}

}
