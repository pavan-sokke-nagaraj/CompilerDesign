package compiler.semantics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import compiler.lexer.Token;
import compiler.semantics.Symbol.STRUCTURE;
import compiler.semantics.Symbol.SYMBOLTYPE;
import compiler.utils.PrintUtil;
import compiler.utils.PrintUtil.LOGTYPE;

public class SemanticAnalysis {

	public SymbolTable mainTable = null; // main table in the semantic analysis
	public SymbolTable currTable = null; // present table with symbolsparsing
	// work on the 2nd order parsing
	public SymbolTable firstTable = null; // used for the 2nd level parsing
	private Logger semanticLog;
	private Logger stLog;

	public SemanticAnalysis() {
		semanticLog = PrintUtil.setLogger("SEMANTIC.log");
		stLog = PrintUtil.setLogger("SymbolTables.html");
	}

	// Start of Global Table
	public void progDecl() {
		mainTable = createTable(null);
		currTable = mainTable;
	}

	// Insert Class to Symbol Table
	public void classDecl(Symbol symbol) {
		symbol.symbolType = SYMBOLTYPE.CLASS;
		symbol.setSelfTable(currTable);
		String address = symbol.getSelfTable().getAddrLink() + "_CLASS_"
				+ symbol.getToken().getValue() + "_POSITION_"
				+ symbol.getToken().getPosition();
		symbol.setAddress(address);
		symbol.setLink("");
		if (isClassRedfined(symbol)) {
			symbol.setDuplicate(true);
			String msg = "CLASS NAME REDEFINED:\t"
					+ symbol.getToken().getValue() + "  at Line Number:\t"
					+ symbol.getToken().getPosition();
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
		}
		symbol.setChildTable(createTable(symbol));
		currTable.getSymbolList().add(symbol);
		currTable = symbol.getChildTable();
	}

	// Function call to check if the class is redefined or not
	private boolean isClassRedfined(Symbol symbol) {
		for (int i = 0; i < symbol.getSelfTable().getSymbolList().size(); i++) {
			Symbol tableSymbol = symbol.getSelfTable().getSymbolList().get(i);
			if (tableSymbol.symbolType == SYMBOLTYPE.CLASS
					&& tableSymbol.getToken().getValue()
							.equals(symbol.getToken().getValue())) {
				return true;
			}
		}
		return false;
	}

	// Insert Variable to Symbol Table
	public void variableDecl(Symbol symbol) {
		// System.out.println("VARIABLE DECLERATION");
		if (symbol.symbolType != SYMBOLTYPE.PARAMETER) {
			symbol.symbolType = SYMBOLTYPE.VARIABLE;
		}
		if (symbol.symbolType == SYMBOLTYPE.PARAMETER) {
			currTable.getParent().setNoOfParams(
					currTable.getParent().getNoOfParams() + 1);
			String param = symbol.getDataType().getValue();
			if (symbol.isArray()) {
				for (int i = 0; i < symbol.getArrSize().size(); i++) {
					param += "[" + symbol.getArrSize().get(i) + "]";
				}
			}
			currTable.getParent().getParams().add(param);
		}
		symbol.setSelfTable(currTable);
		String address = symbol.getSelfTable().getAddrLink() + "_VARIABLE_"
				+ symbol.getToken().getValue() + "_POSITION_"
				+ symbol.getToken().getPosition();
		symbol.setAddress(address);
		if (isVarRedfined(symbol)) {
			symbol.setDuplicate(true);
			// System.out.println("--------->>>>>  VARIABLE RE--DEFINED");
			String msg = "VARIABLE NAME REDEFINED:\t"
					+ symbol.getToken().getValue() + "  at Line Number:\t"
					+ symbol.getToken().getPosition();
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
		}
		if (!isDataTypeDefined(symbol)) {
			symbol.setDataTypeDefined(false);
			String msg = "DATA TYPE UNDEFINED:\t"
					+ symbol.getDataType().getValue() + "  at Line Number:\t"
					+ symbol.getToken().getPosition();
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
		} else {
			symbol.setLink(link);
		}
		if (isValidVarName(symbol)) {
		}
		// structure
		if (symbol.isArray()
				&& symbol.getDataType().getDesc().equals("T_IDENTIFIER")) {
			symbol.structure = STRUCTURE.CLASSARRAY;
		} else if (symbol.getDataType().getDesc().equals("T_IDENTIFIER")) {
			symbol.structure = STRUCTURE.CLASS;
		} else if (symbol.isArray()) {
			symbol.structure = STRUCTURE.ARRAY;
		} else {
			symbol.structure = STRUCTURE.SIMPLE;
		}
		currTable.getSymbolList().add(symbol);
	}

	// Function call to check if the variable name is valid or not
	private boolean isValidVarName(Symbol symbol) {
		return false;
	}

	private static String link = "";

	// Function call to check if the data type is defined or not
	public boolean isDataTypeDefined(Symbol symbol) {
		// System.out.println(symbol.getToken().getValue());
		if (symbol.getDataType().getValue().equals("int")
				|| symbol.getDataType().getValue().equals("float")) {
			link = "";
			return true;
		}
		for (int i = 0; i < mainTable.getSymbolList().size(); i++) {
			Symbol tableSymbol = mainTable.getSymbolList().get(i);
			if (tableSymbol.symbolType == SYMBOLTYPE.CLASS
					&& tableSymbol.getToken().getValue()
							.equals(symbol.getDataType().getValue())) {
				if (symbol
						.getDataType()
						.getValue()
						.equals(symbol.getSelfTable().getParent().getToken()
								.getValue())) {
				} else {
					if (tableSymbol.getAddress() != null)
						link = tableSymbol.getAddress();
					return true;
				}
			}
		}
		// check for the function types defined after the program body during
		// the 2nd round of parsing
		if (firstTable != null) {
			for (int i = 0; i < firstTable.getSymbolList().size(); i++) {
				Symbol tableSymbol = firstTable.getSymbolList().get(i);
				if (tableSymbol.symbolType == SYMBOLTYPE.CLASS
						&& tableSymbol.getToken().getValue()
								.equals(symbol.getDataType().getValue())) {
					if (symbol
							.getDataType()
							.getValue()
							.equals(symbol.getSelfTable().getParent()
									.getToken().getValue())) {

					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

	// Function call to check if the variable is redefined or not
	private boolean isVarRedfined(Symbol symbol) {
		for (int i = 0; i < symbol.getSelfTable().getSymbolList().size(); i++) {
			Symbol tableSymbol = symbol.getSelfTable().getSymbolList().get(i);
			if (tableSymbol.symbolType == SYMBOLTYPE.VARIABLE
					|| tableSymbol.symbolType == SYMBOLTYPE.PARAMETER) {
				if (tableSymbol.getToken().getValue()
						.equals(symbol.getToken().getValue())) {
					return true;
				}
			}
		}
		return false;
	}

	// Function call to create a new entry of symbol table and link it to the
	// parent table
	private SymbolTable createTable(Symbol symbol) {
		SymbolTable symbolTable = new SymbolTable();
		symbolTable.setParent(symbol);
		if (symbol == null) {
			symbolTable.setAddrLink("GLOBAL");
		} else {
			String prefix = symbol.getSelfTable().getAddrLink() + "_"
					+ symbol.getToken().getValue();
			symbolTable.setAddrLink(prefix);
		}
		return symbolTable;
	}

	// Quit the current Symbol Table
	public void QuitPresentTable() {
		currTable = currTable.getParent().getSelfTable();
		// System.out.println(currTable.getAddrLink());
	}

	// Insert function to Symbol Table
	public void functionDecl(Symbol symbol) {
		// System.out.println(symbol.getToken().getValue());
		symbol.symbolType = SYMBOLTYPE.FUNCTION;
		symbol.setSelfTable(currTable);
		symbol.setChildTable(createTable(symbol));
		String address = symbol.getSelfTable().getAddrLink() + "_FUNCTION_"
				+ symbol.getToken().getValue() + "_POSITION_"
				+ symbol.getToken().getPosition();
		symbol.setAddress(address);
		if (!isDataTypeDefined(symbol)) {
			symbol.setDataTypeDefined(false);
			String msg = "DATA TYPE UNDEFINED:\t"
					+ symbol.getDataType().getValue() + "  at Line Number:\t"
					+ symbol.getToken().getPosition();
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
		} else {
			symbol.setLink(link);
		}
		// if (isFunctionReDefined(symbol)) {
		// symbol.setDataTypeDefined(false);
		// String msg = "FUNCTION NAME REDEFINED:\t"
		// + symbol.getToken().getValue() + "  at Line Number:\t"
		// + symbol.getToken().getPosition();
		// PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
		// }
		currTable.getSymbolList().add(symbol);
		currTable = symbol.getChildTable();
	}

	public boolean isFunctionReDefined(Symbol symbol) {
		boolean isRedefined = false;
		int funcCount = 0;
		if (symbol.symbolType == SYMBOLTYPE.PARAMETER) {
			Symbol function = symbol.getSelfTable().getParent();
			SymbolTable funcTable = function.getSelfTable();
			// System.out.println(function.getToken().getValue() + "  "
			// + function.getToken().getPosition());
			for (int i = 0; i < funcTable.getSymbolList().size(); i++) {
				Symbol tableSymbol = funcTable.getSymbolList().get(i);
				if (tableSymbol.symbolType == SYMBOLTYPE.FUNCTION
						&& tableSymbol.getToken().getValue()
								.equals(function.getToken().getValue())
						&& tableSymbol.getDataType().getValue()
								.equals(function.getDataType().getValue())) {
					if (tableSymbol.getNoOfParams() == function.getNoOfParams()) {
						ArrayList<String> paramsF1 = function.getParams();
						ArrayList<String> paramsF2 = tableSymbol.getParams();
						int pCount = function.getNoOfParams();
						for (int j = 0; j < pCount; j++) {
							if (paramsF1.get(j).equals(paramsF2.get(j))) {
								if (j == pCount - 1) {
									funcCount++;
								}
							}
						}
					}
				}
			}
		} else if (symbol.symbolType == SYMBOLTYPE.FUNCTION) {
			SymbolTable funcTable = symbol.getSelfTable();
			// System.out.println(symbol.getToken().getValue() + "  "
			// + symbol.getToken().getPosition());
			for (int i = 0; i < funcTable.getSymbolList().size(); i++) {
				Symbol tableSymbol = funcTable.getSymbolList().get(i);
				if (tableSymbol.getToken().getValue()
						.equals(symbol.getToken().getValue())
						&& tableSymbol.getDataType().getValue()
								.equals(symbol.getDataType().getValue())) {
					funcCount++;
				}
			}
		} else {
			System.out
					.println("CALL 911... SHOW ME THE CODE.. COMPILER HAS GONE MAD");
		}
		// System.out.println(funcCount);
		if (funcCount != 1) {
			String token = "";
			int position = -1;
			if (symbol.symbolType == SYMBOLTYPE.PARAMETER) {
				symbol.getSelfTable().getParent().setDuplicate(true);
				token = symbol.getSelfTable().getParent().getToken().getValue();
				position = symbol.getSelfTable().getParent().getToken()
						.getPosition();
			} else if (symbol.symbolType == SYMBOLTYPE.FUNCTION) {
				symbol.setDuplicate(true);
				token = symbol.getToken().getValue();
				position = symbol.getToken().getPosition();
			}
			// System.out.println(funcCount);
			String msg = "FUNCTION REDEFINED:\t" + token
					+ "  at Line Number:\t" + position;
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
		}
		return isRedefined;
	}

	// Insert Program to Symbol Table
	public void programDecl(Symbol symbol) {
		symbol.symbolType = SYMBOLTYPE.PROGRAM;
		symbol.getDataType().setValue("program");
		symbol.setSelfTable(currTable);
		symbol.setChildTable(createTable(symbol));
		String address = symbol.getSelfTable().getAddrLink() + "_PROGRAM_"
				+ symbol.getToken().getValue() + "_POSITION_"
				+ symbol.getToken().getPosition();
		symbol.setAddress(address);
		symbol.setLink("");
		currTable.getSymbolList().add(symbol);
		currTable = symbol.getChildTable();
	}

	// Function to print all the symbol tables
	public void printSymbolTable() {
		String msg = "SYMBOL TABLE:\tGLOBAL\n";
		PrintUtil.info(semanticLog, LOGTYPE.SEMATICS, msg);
		PrintUtil.print(stLog, LOGTYPE.HTML, PrintUtil.htmlStart);
		PrintUtil.print(stLog, LOGTYPE.HTML, PrintUtil.h2O + "GLOBAL"
				+ PrintUtil.h2C);
		// System.out.println(output);
		printSymbolTable(mainTable, "GLOBAL");
		PrintUtil.print(stLog, LOGTYPE.HTML, PrintUtil.htmlEnd);
	}

	// Function to print the current symbol table and it's children table and
	// all the symbols
	public void printSymbolTable(SymbolTable symbolTable, String tableName) {
		if (symbolTable != null) {
			String msg = "|___Name___|____ Type ____|____ Kind ____|____Structure____|"
					+ "___Array Dimension ___|___ NO OF PARAMS ___|_____ADDRESS____|";
			PrintUtil.info(semanticLog, LOGTYPE.SEMATICS, msg);
			PrintUtil.print(stLog, LOGTYPE.HTML, PrintUtil.tableO);
			for (int i = 0; i < symbolTable.getSymbolList().size(); i++) {
				Symbol symbol = symbolTable.getSymbolList().get(i);
				PrintUtil.print(stLog, LOGTYPE.HTML, PrintUtil.trO);
				printSymbols(symbol);
				PrintUtil.print(stLog, LOGTYPE.HTML, PrintUtil.trC);
			}
			PrintUtil.print(stLog, LOGTYPE.HTML, PrintUtil.tableC);
			for (int i = 0; i < symbolTable.getSymbolList().size(); i++) {
				Symbol symbol = symbolTable.getSymbolList().get(i);
				if (symbol.getChildTable() != null) {
					String msg1 = "\n\nSYMBOL TABLE:\t" + tableName + " :: "
							+ symbol.getToken().getValue() + "\n";
					PrintUtil.info(semanticLog, LOGTYPE.SEMATICS, msg1);
					PrintUtil.print(stLog, LOGTYPE.HTML, PrintUtil.h2O
							+ tableName + " :: " + symbol.getToken().getValue()
							+ PrintUtil.h2C);
					printSymbolTable(symbol.getChildTable(), tableName + " :: "
							+ symbol.getToken().getValue() + " :: ");
				}
			}
		}
	}

	public static String tdO = "<td>";
	public static String tdC = "</\td>";

	// Function to print the symbol and all it's details
	private void printSymbols(Symbol symbol) {
		String name = symbol.getToken().getValue();
		SYMBOLTYPE kind = symbol.symbolType;
		String type = symbol.getDataType().getValue();
		if (kind == SYMBOLTYPE.VARIABLE || kind == SYMBOLTYPE.PARAMETER
				&& symbol.isArray()) {
			for (int i = 0; i < symbol.getArrLength(); i++) {
				type += "[" + symbol.getArrSize().get(i) + "]";
			}
		} else if (kind == SYMBOLTYPE.FUNCTION) {
			type += "::";
			if (symbol.getNoOfParams() > 0) {
				for (int i = 0; i < symbol.getParams().size(); i++) {
					if (i == symbol.getNoOfParams() - 1) {
						type += symbol.getParams().get(i);
					} else {
						type += symbol.getParams().get(i) + ",";
					}
				}
			} else {
				type += "NILL";
			}
		}
		String print = "   " + name + "   \t" + kind + "\t" + type + "\t\t"
				+ symbol.structure;
		String h2Print = tdO + name + tdC + tdO + kind + tdC + tdO + type + tdC
				+ tdO + symbol.structure + tdC;
		if (symbol.isArray()) {
			print += "\t" + symbol.getArrLength() + "\t";
			h2Print += tdO + symbol.getArrLength() + tdC;
		} else {
			print += "\t\tN/A\t";
			h2Print += tdO + "N/A" + tdC;
		}
		if (symbol.getNoOfParams() > 0) {
			print += "" + symbol.getNoOfParams() + "\t";
			h2Print += tdO + symbol.getNoOfParams() + tdC;
		} else {
			print += "\t\tN/A\t";
			h2Print += tdO + "N/A" + tdC;
		}
		print += "\t" + symbol.getAddress() + "\t";
		h2Print += tdO + symbol.getAddress() + tdC;
		h2Print += tdO + symbol.isDuplicate() + tdC;
		h2Print += tdO + symbol.isArray() + tdC;
		h2Print += tdO + symbol.isDataTypeDefined() + tdC;
		h2Print += tdO + symbol.getLink() + tdC;
		PrintUtil.info(semanticLog, LOGTYPE.SEMATICS, print);
		PrintUtil.print(stLog, LOGTYPE.HTML, h2Print);
	}

	// Function call to check if the variable is declared or not
	public boolean isVarDeclared(Symbol symbol) {
		for (int i = 0; i < currTable.getSymbolList().size(); i++) {
			Symbol tableSymbol = currTable.getSymbolList().get(i);
			if (tableSymbol.symbolType == SYMBOLTYPE.VARIABLE
					&& tableSymbol.getToken().getValue()
							.equals(symbol.getToken().getValue())) {
				copySymbol(symbol, tableSymbol);
				return true;
			}
		}
		for (int i = 0; i < currTable.getSymbolList().size(); i++) {
			Symbol tableSymbol = currTable.getSymbolList().get(i);
			if (tableSymbol.symbolType == SYMBOLTYPE.PARAMETER
					&& tableSymbol.getToken().getValue()
							.equals(symbol.getToken().getValue())) {
				copySymbol(symbol, tableSymbol);
				return true;
			}
		}
		if (currTable.getParent() != null) {
			for (int i = 0; i < currTable.getParent().getSelfTable()
					.getSymbolList().size(); i++) {
				Symbol tableSymbol = currTable.getParent().getSelfTable()
						.getSymbolList().get(i);
				if (tableSymbol.symbolType == SYMBOLTYPE.VARIABLE
						&& tableSymbol.getToken().getValue()
								.equals(symbol.getToken().getValue())) {
					copySymbol(symbol, tableSymbol);
					return true;
				}
				if (tableSymbol.symbolType == SYMBOLTYPE.FUNCTION
						&& tableSymbol.getToken().getValue()
								.equals(symbol.getToken().getValue())) {
					copySymbol(symbol, tableSymbol);
					return true;
				}
			}
		}
		// check for the function types defined after the program body during
		// the 2nd round of parsing
		if (firstTable != null) {
			for (int i = 0; i < firstTable.getSymbolList().size(); i++) {
				Symbol tableSymbol = firstTable.getSymbolList().get(i);
				if (tableSymbol.symbolType == SYMBOLTYPE.VARIABLE
						&& tableSymbol.getToken().getValue()
								.equals(symbol.getToken().getValue())) {
					copySymbol(symbol, tableSymbol);
					return true;
				}
				if (tableSymbol.symbolType == SYMBOLTYPE.FUNCTION
						&& tableSymbol.getToken().getValue()
								.equals(symbol.getToken().getValue())) {
					copySymbol(symbol, tableSymbol);
					return true;
				}
			}
		}
		String msg = "IDENTIFIER UNDECLARED:\t" + symbol.getToken().getValue()
				+ "  at Line Number:\t" + symbol.getToken().getPosition();
		PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
		return false;
	}

	private void copySymbol(Symbol symbol, Symbol tableSymbol) {
		symbol.setDataType(copyToken(tableSymbol.getDataType()));
		symbol.structure = tableSymbol.structure;
		symbol.symbolType = tableSymbol.symbolType;
		symbol.setArray(tableSymbol.isArray());
		symbol.setArrLength(tableSymbol.getArrLength());
		symbol.setArrSize(tableSymbol.getArrSize());
		symbol.setNoOfParams(tableSymbol.getNoOfParams());
		symbol.setParams(tableSymbol.getParams());
	}

	private Token copyToken(Token token) {
		Token cpyToken = new Token(token.getPosition(), token.getValue(),
				token.getDesc(), token.getTokenType());
		return cpyToken;
	}

	// Function call to check if the the type is of class or not
	public boolean isClassType(Symbol symbol, Symbol tempSymb) {
		if (firstTable != null) {
			for (int i = 0; i < firstTable.getSymbolList().size(); i++) {
				Symbol tableSymbol = firstTable.getSymbolList().get(i);
				// System.out.println(tableSymbol.symbolType);
				// System.out.println(tableSymbol.getToken().getValue());
				if (tableSymbol.symbolType == SYMBOLTYPE.CLASS
						&& tableSymbol.getToken().getValue()
								.equals(symbol.getDataType().getValue())) {
					for (int j = 0; j < tableSymbol.getChildTable()
							.getSymbolList().size(); j++) {
						Symbol thisSymb = tableSymbol.getChildTable()
								.getSymbolList().get(j);
						if (thisSymb.getToken().getValue()
								.equals(tempSymb.getToken().getValue())) {
							return true;
						}
					}
					String msg = "\""
							+ tempSymb.getToken().getValue()
							+ "\" IS NOT A DECLARED MEMBER FUNCTION/VARIABLE OF \""
							+ tableSymbol.getToken().getValue() + "\""
							+ "  at Line Number:\t"
							+ tempSymb.getToken().getPosition();
					PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
					return false;
				}
			}
			String msg = "TOKEN FOR THE LEFT SIDE OF  \"."
					+ tempSymb.getToken().getValue()
					+ "\" SHOULD BE OF A TYPE CLASS" + "  at Line Number:\t"
					+ tempSymb.getToken().getPosition();
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
			return false;
		}
		return false;
	}
}
