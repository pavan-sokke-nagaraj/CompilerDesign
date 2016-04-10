package compiler.semantics;

import java.util.ArrayList;
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
	public Logger semanticLog;
	private Logger stLog;
	public Logger codeLog;

	public SemanticAnalysis() {
		semanticLog = PrintUtil.setLogger("SEMANTIC.log");
		stLog = PrintUtil.setSimpleLogger("SYMBOLTABLES.html");
		codeLog = PrintUtil.setSimpleLogger("//moon//CODE.txt");
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
		String address = symbol.getSelfTable().getAddrLink() + "_C_"
				+ symbol.getToken().getValue() + "_"
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
		moonData.add(symbol.getAddress() + "\tdw\t" + "0");
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
		String address = symbol.getSelfTable().getAddrLink() + "_V_"
				+ symbol.getToken().getValue() + "_"
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

		// allocate memory
		System.out.println(symbol.toString());
		if (symbol.getDataType().getValue().equals("int")
				|| symbol.getDataType().getValue().equals("float")) {
			symbol.setMemory(4);
		} else {
			boolean isMemSet = false;
			for (int i = 0; i < mainTable.getSymbolList().size(); i++) {
				Symbol tmpSymbol = mainTable.getSymbolList().get(i);
				if (tmpSymbol.symbolType == SYMBOLTYPE.CLASS
						&& tmpSymbol.getToken().getValue()
								.equals(symbol.getDataType().getValue())) {
					symbol.setMemory(tmpSymbol.getMemory());
					isMemSet = true;
					break;
				}
			}
			if (firstTable != null && !isMemSet) {
				for (int i = 0; i < firstTable.getSymbolList().size(); i++) {
					Symbol tmpSymbol = firstTable.getSymbolList().get(i);
					if (tmpSymbol.symbolType == SYMBOLTYPE.CLASS
							&& tmpSymbol.getToken().getValue()
									.equals(symbol.getToken().getValue())) {
						symbol.setMemory(tmpSymbol.getMemory());
						isMemSet = true;
						break;
					}
				}
			}
		}
		if (symbol.isDataTypeDefined() && !symbol.isDuplicate()) {
			genVarCode(symbol);
		}

		currTable.getSymbolList().add(symbol);
		// moonData.add(symbol.getAddress() + "\tDW\t" + "0");
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
			symbolTable.setAddrLink("F");
		} else {
			String prefix = "";
			if (symbol.getToken().getValue().equals("program")) {
				prefix = symbol.getSelfTable().getAddrLink() + "_" + "P";
			} else {
				prefix = symbol.getSelfTable().getAddrLink() + "_"
						+ symbol.getToken().getValue();
			}

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
		String address = symbol.getSelfTable().getAddrLink() + "_F_"
				+ symbol.getToken().getValue() + "_"
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
		symbol.setMemory(4);
		currTable.getSymbolList().add(symbol);
		currTable = symbol.getChildTable();
		if (symbol.isDataTypeDefined() && !symbol.isDuplicate()) {
			genFuncStartCode(symbol);
		}
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
				/*
				 * && tableSymbol.getDataType().getValue()
				 * .equals(function.getDataType().getValue())
				 */) {
					if (tableSymbol.getNoOfParams() == function.getNoOfParams()) {
						ArrayList<String> paramsF1 = function.getParams();
						ArrayList<String> paramsF2 = tableSymbol.getParams();
						int pCount = function.getNoOfParams();
						int matchCount = 0;
						for (int j = 0; j < pCount; j++) {
							if (paramsF1.get(j).equals(paramsF2.get(j))) {
								matchCount++;
								if (matchCount == pCount) {
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
				/*
				 * && tableSymbol.getDataType().getValue()
				 * .equals(symbol.getDataType().getValue())
				 */) {
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
		String address = symbol.getSelfTable().getAddrLink() + "_P_"
				+ symbol.getToken().getValue() + "_"
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
		h2Print += tdO + symbol.getMemory() + tdC;
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

	// copy all the symbol related values
	// except the token and symbol table details
	private void copySymbol(Symbol symbol, Symbol tableSymbol) {
		symbol.symbolType = tableSymbol.symbolType;
		symbol.structure = tableSymbol.structure;
		symbol.setDataType(copyToken(tableSymbol.getDataType()));
		symbol.setDataTypeDefined(tableSymbol.isDataTypeDefined());
		symbol.setDuplicate(tableSymbol.isDuplicate());
		symbol.setValidVarName(tableSymbol.isValidVarName());

		symbol.setArray(tableSymbol.isArray());
		symbol.setArrLength(tableSymbol.getArrLength());
		symbol.setArrSize(tableSymbol.getArrSize());

		symbol.setNoOfParams(tableSymbol.getNoOfParams());
		symbol.getParams().addAll(tableSymbol.getParams());
		symbol.setAddress(tableSymbol.getAddress());
		symbol.setLink(tableSymbol.getLink());
		symbol.setClassName(tableSymbol.getClassName());
		symbol.setMemory(tableSymbol.getMemory());
	}

	private Token copyToken(Token token) {
		Token cpyToken = new Token(token.getPosition(), token.getValue(),
				token.getDesc(), token.getTokenType());
		return cpyToken;
	}

	// Function call to check if the the type is of class or not
	public boolean isClassType(Symbol symbol, Symbol subSymb) {
		if (firstTable != null) {
			for (int i = 0; i < firstTable.getSymbolList().size(); i++) {
				Symbol tableSymbol = firstTable.getSymbolList().get(i);
				// System.out.println(tableSymbol.symbolType);
				// System.out.println(tableSymbol.getToken().getValue());
				if (tableSymbol.symbolType == SYMBOLTYPE.CLASS
						&& tableSymbol.getToken().getValue()
								.equals(symbol.getDataType().getValue())) {
					int memOffSet = 0;
					for (int j = 0; j < tableSymbol.getChildTable()
							.getSymbolList().size(); j++) {
						Symbol classSymb = tableSymbol.getChildTable()
								.getSymbolList().get(j);
						if (classSymb.getToken().getValue()
								.equals(subSymb.getToken().getValue())) {
							if (subSymb.getAddress() == null) {
								subSymb.setAddress(symbol.getAddress());
							}
							subSymb.setDataType(copyToken(classSymb
									.getDataType()));
							// copySymbol(subSymb, classSymb);
							subSymb.setMemory(classSymb.getMemory());
							subSymb.setArray(classSymb.isArray());
							subSymb.setArrLength(classSymb.getArrLength());
							subSymb.setArrSize(classSymb.getArrSize());
							subSymb.setClassName(tableSymbol.getToken()
									.getValue());
							setClassOffset(memOffSet);
							return true;
						}
						if (classSymb.symbolType == SYMBOLTYPE.VARIABLE) {
							if (classSymb.isArray()) {
								memOffSet += getArrayMemory(classSymb);
							} else {
								memOffSet = classSymb.getMemory();
							}
						}
					}
					String msg = "\""
							+ subSymb.getToken().getValue()
							+ "\" IS NOT A DECLARED MEMBER FUNCTION/VARIABLE OF \""
							+ tableSymbol.getToken().getValue() + "\""
							+ "  at Line Number:\t"
							+ subSymb.getToken().getPosition();
					PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
					return false;
				}
			}
			String msg = "TOKEN FOR THE LEFT SIDE OF  \"."
					+ subSymb.getToken().getValue()
					+ "\" SHOULD BE OF A TYPE CLASS" + "  at Line Number:\t"
					+ subSymb.getToken().getPosition();
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
			return false;
		}
		return false;
	}

	private ArrayList<Symbol> arrayIndice = new ArrayList<Symbol>();

	// function to validate array parameters
	// calcArrayAddr
	public void checkArray(Symbol symbol) {
		System.out.println("LOL");
		if (arrayIndice.size() != symbol.getArrLength()) {
			String msg = "ARRAY DIMENSIONS MISSMATCH:" + " REQUIRED: "
					+ symbol.getArrLength() + " ASSIGNED: "
					+ arrayIndice.size() + "\t FOR VARIABLE:\t"
					+ symbol.getToken().getValue() + "\tAT LINE NUMBER:\t"
					+ symbol.getToken().getPosition();
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
		}
		if (arrayIndice.size() != 0) {
			for (int i = 0; i < arrayIndice.size(); i++) {
				int arrSize = 1;
				for (int j = i + 1; j < symbol.getArrSize().size(); j++) {
					arrSize *= symbol.getArrSize().get(j);
				}
				arrSize *= symbol.getMemory();
				moonCode.add("\t\t%SET ARRAY INDEX: "
						+ arrayIndice.get(i).getToken().getValue());
				moonCode.add("\t\t" + "addi" + "\t" + "r1,\t" + "r0,\t"
						+ arrSize);
				loadWord(arrayIndice.get(i), "r2", "");
				moonCode.add("\t\t" + "mul" + "\t" + "r1,\t" + "r1,\t" + "r2");
				moonCode.add("\t\t" + "add" + "\t" + "r11,\t" + "r11,\t" + "r1");
				moonCode.add("\n");
			}
		}
		arrayIndice.clear();
	}

	// function to check index data type
	// ifValidIndexType
	public void checkIndex(Symbol index) {
		System.out.println("LOL");
		System.out.println(index.getDataType().getValue());
		if (!(index.getDataType().getDesc().equals("T_INTEGER") || index
				.getDataType().getDesc().equals("T_RESERVE_WORD_INT"))) {
			String msg = "ARRAY INDEX OUT OF BOUND: ASSIGNED \t"
					+ index.getDataType().getValue() + "["
					+ index.getDataType().getDesc() + "]"
					+ "\t FOR DIMENSION TYPE:\t" + "T_INTEGER"
					+ "\tAT LINE NUMBER:\t" + index.getToken().getPosition();
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
		} else {
			arrayIndice.add(index);
		}
	}

	// function to compare LHS datatype to RHS datatype
	// compDateType
	public void checkDataTypes(Symbol symLHS, Symbol symRHS) {
		System.out.println("LOL");
		String lhsVal = symLHS.getDataType().getValue();
		String rhsVal = symRHS.getDataType().getValue();
		System.out.println("LHS: " + lhsVal + " RHS: " + rhsVal);
		// printCode();
		if (lhsVal.equals(rhsVal)) {
			loadWord(symRHS, "r1", "\t\t%" + symLHS.getToken().getValue()
					+ " = " + symRHS.getToken().getValue());
			storeWord(symLHS);
		} else if (lhsVal.equals("int") && rhsVal.equals("float")) {
			// convert symLHS to float
			String msg = "DATA TYPE MISSMATCH:\t CONVERTING "
					+ symLHS.getToken().getValue()
					+ " FROM INT DATA TYPE TO FLOAT DATA TYPE"
					+ "\tAT LINE NUMBER:\t" + symLHS.getToken().getPosition();
			PrintUtil.warning(semanticLog, LOGTYPE.SEMATICS, msg);
		} else if (lhsVal.equals("float") && rhsVal.equals("int")) {
			// convert symRHS to float
			String msg = "DATA TYPE MISSMATCH:\t CONVERTING "
					+ symRHS.getToken().getValue()
					+ " FROM INT DATA TYPE TO FLOAT DATA TYPE"
					+ "\tAT LINE NUMBER:\t" + symRHS.getToken().getPosition();
			PrintUtil.warning(semanticLog, LOGTYPE.SEMATICS, msg);
		} else {
			String msg = "INVALID DATA TYPES:\t" + symLHS.getToken().getValue()
					+ " AND " + symRHS.getToken().getValue()
					+ " SHOULD BE OF INT OR FLOAT DATA TYPE"
					+ "\tAT LINE NUMBER:\t" + symLHS.getToken().getPosition();
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
		}
	}

	// compare addOp data types
	// compDateTypeNum
	public Symbol addOpComp(Symbol symLHS, Token operToken, Symbol symRHS) {
		System.out.println("LOL");
		String lhsVal = symLHS.getDataType().getValue();
		String rhsVal = symRHS.getDataType().getValue();
		System.out.println("LHS: " + lhsVal + " RHS: " + rhsVal);
		if (lhsVal.equals("int") && rhsVal.equals("int")) {
			genMathCode(symLHS, operToken, symRHS);
			return symLHS;
		} else if (lhsVal.equals("int") && rhsVal.equals("float")) {
			// convert symLHS to float
			String msg = "DATA TYPE MISSMATCH:\t CONVERTING "
					+ symLHS.getToken().getValue()
					+ " FROM INT DATA TYPE TO FLOAT DATA TYPE"
					+ "\tAT LINE NUMBER:\t" + symLHS.getToken().getPosition();
			PrintUtil.warning(semanticLog, LOGTYPE.SEMATICS, msg);
			return symRHS;
		} else if (lhsVal.equals("float") && rhsVal.equals("int")) {
			// convert symRHS to float
			String msg = "DATA TYPE MISSMATCH:\t CONVERTING "
					+ symRHS.getToken().getValue()
					+ " FROM INT DATA TYPE TO FLOAT DATA TYPE"
					+ "\tAT LINE NUMBER:\t" + symRHS.getToken().getPosition();
			PrintUtil.warning(semanticLog, LOGTYPE.SEMATICS, msg);
			return symLHS;
		} else if (lhsVal.equals("float") && rhsVal.equals("float")) {
			return symLHS;
		} else {
			String msg = "INVALID DATA TYPES:\t" + symLHS.getToken().getValue()
					+ " AND " + symRHS.getToken().getValue()
					+ " SHOULD BE OF INT OR FLOAT DATA TYPE"
					+ "\tAT LINE NUMBER:\t" + symLHS.getToken().getPosition();
			PrintUtil.error(semanticLog, LOGTYPE.SEMATICS, msg);
			return symRHS;
		}
	}

	public ArrayList<Symbol> aParams = new ArrayList<Symbol>();
	public ArrayList<String> aParamVars = new ArrayList<String>();

	public void checkParams(Symbol symbol) {
		System.out.println(symbol.toString());
		// if symbol is type of var = className.function(param p1, param p2)
		if (symbol.symbolType == SYMBOLTYPE.ISCLASSORFUNC) {
			if (firstTable != null) {
				System.out.println(symbol.toString());
				// check the class -> methods first from
				for (int i = 0; i < firstTable.getSymbolList().size(); i++) {
					Symbol tableSymbol = firstTable.getSymbolList().get(i);
					System.out.println(tableSymbol.symbolType);
					System.out.println(tableSymbol.getToken().getValue());
					if (tableSymbol.symbolType == SYMBOLTYPE.CLASS
							&& tableSymbol.getToken().getValue()
									.equals(symbol.getClassName())) {
						for (int j = 0; j < tableSymbol.getChildTable()
								.getSymbolList().size(); j++) {
							Symbol funcSymb = tableSymbol.getChildTable()
									.getSymbolList().get(j);
							if (funcSymb.symbolType == SYMBOLTYPE.FUNCTION
									&& funcSymb
											.getToken()
											.getValue()
											.equals(symbol.getToken()
													.getValue())) {
								if (compareParams(funcSymb.getChildTable())) {
									symbol.symbolType = SYMBOLTYPE.FUNCTION;
								}
							}
						}

					}
				}
			}
		} else {
			// if symbol is type of var = functionName(param p1, param p2)
			// check for local function call
			if (symbol.getSelfTable() != null) {
				for (int i = 0; i < symbol.getSelfTable().getSymbolList()
						.size(); i++) {
					Symbol localFunc = symbol.getSelfTable().getSymbolList()
							.get(i);
					if (localFunc.symbolType == SYMBOLTYPE.FUNCTION
							&& localFunc.getToken().getValue()
									.equals(symbol.getToken().getValue())) {
						if (compareParams(localFunc.getChildTable())) {
							symbol.symbolType = SYMBOLTYPE.FUNCTION;
						}
					}
				}
			} else {
				// if symbol is type of var = functionName(param p1, param p2)
				// and call is made to free functions from global table
				if (firstTable != null) {
					System.out.println(symbol.toString());
					// check the class -> methods first from
					for (int i = 0; i < firstTable.getSymbolList().size(); i++) {
						Symbol tableSymbol = firstTable.getSymbolList().get(i);
						System.out.println(tableSymbol.symbolType);
						System.out.println(tableSymbol.getToken().getValue());
						System.out.println();
						if (tableSymbol.symbolType == SYMBOLTYPE.FUNCTION
								&& tableSymbol.getToken().getValue()
										.equals(symbol.getToken().getValue())) {
							if (compareParams(tableSymbol.getChildTable())) {
								symbol.symbolType = SYMBOLTYPE.FUNCTION;
							}
						}
					}
				}
			}
		}
		// System.out.println("UNDECLARED PARAMETERS");
	}

	private boolean compareParams(SymbolTable childTable) {
		int paramCount = 0;
		for (int i = 0; i < childTable.getSymbolList().size(); i++) {
			Symbol aParam = childTable.getSymbolList().get(i);
			if (aParam.symbolType == SYMBOLTYPE.PARAMETER) {
				paramCount++;
				if (paramCount > aParams.size()) {
					return false;
				}
				Symbol cParam = aParams.get(i);
				if (!aParam.getDataType().getValue()
						.equals(cParam.getDataType().getValue())) {
					System.out.println("DATA TYPE MISS MATCH");
					return false;
				}
			}
		}
		if (paramCount != aParams.size()) {
			return false;
		}
		System.out.println("all is well");
		return true;
	}

	public boolean setClassMemory(Symbol symbol) {
		for (int i = 0; i < currTable.getSymbolList().size(); i++) {
			Symbol tmpSymb = currTable.getSymbolList().get(i);
			if (tmpSymb.symbolType == SYMBOLTYPE.VARIABLE) {
				if (tmpSymb.isArray()) {
					int arrMemory = getArrayMemory(tmpSymb);
					System.out.println("arrMemory: " + arrMemory);
					symbol.setMemory(symbol.getMemory() + arrMemory);
				} else {
					symbol.setMemory(symbol.getMemory() + tmpSymb.getMemory());
				}
			}
		}
		return true;
	}

	private int getArrayMemory(Symbol arrSymb) {
		int arrMemory = 1;
		for (int i = 0; i < arrSymb.getArrSize().size(); i++) {
			if (arrSymb.getArrSize().get(i) != 0)
				arrMemory *= arrSymb.getArrSize().get(i);
		}
		arrMemory = arrMemory * arrSymb.getMemory();
		// arrSymb.setMemory(arrMemory);
		return arrMemory;

	}

	public ArrayList<String> moonData = new ArrayList<String>();
	public ArrayList<String> moonCode = new ArrayList<String>();

	public void printData() {
		for (int i = 0; i < moonData.size(); i++) {
			String data = moonData.get(i);
			System.out.println(data);
			PrintUtil.print(codeLog, LOGTYPE.HTML, data + "\n");
		}
	}

	public void printCode() {
		for (int i = 0; i < moonCode.size(); i++) {
			String code = moonCode.get(i);
			System.out.println(code);
			PrintUtil.print(codeLog, LOGTYPE.HTML, code + "\n");
		}
	}

	private boolean genVarCode(Symbol symbol) {
		if (symbol.getSelfTable().getParent() != null) {
			if (symbol.getSelfTable().getParent().symbolType != SYMBOLTYPE.CLASS) {
				if (symbol.isArray()) {
					moonData.add(symbol.getAddress() + "\tres\t"
							+ getArrayMemory(symbol));
				} else {
					if (symbol.getMemory() > 4
							&& symbol.symbolType == SYMBOLTYPE.VARIABLE) {
						moonData.add(symbol.getAddress() + "\tres\t"
								+ getArrayMemory(symbol));
					} else {
						moonData.add(symbol.getAddress() + "\tdw\t" + "0");
					}
				}
			}
		}
		return true;
	}

	private boolean genFuncStartCode(Symbol symbol) {
		moonCode.add("");
		moonData.add(symbol.getAddress() + "\tdw\t" + "0");
		moonCode.add("%FUNCTION:\t" + symbol.getToken().getValue());
		moonCode.add(symbol.getAddress() + "_F_START");
		return true;
	}

	public boolean genFuncEndCode() {
		moonCode.add("\t\t" + "jr\t" + "R15");
		moonCode.add("");
		return true;
	}

	private static String floatMask = "1000";

	private boolean genMathCode(Symbol symLHS, Token operToken, Symbol symRHS) {
		loadWord(symRHS, "r2", "\t\t%" + symLHS.getToken().getValue()
				+ operToken.getValue() + symRHS.getToken().getValue());
		popOffset(symLHS);
		loadWord(symLHS, "r1", "\t\t%");
		String opType = "add";
		if (operToken.getValue().equals("+")) {
			opType = "add";
		} else if (operToken.getValue().equals("-")) {
			opType = "sub";
		} else if (operToken.getValue().equals("*")) {
			opType = "mul";
		} else if (operToken.getValue().equals("/")) {
			opType = "div";
			// if (flag == 1) {
			// //asmCode("", "sl", "r2", "8", "", "% << 8");
			// asmCode("", "muli", "r2", "r2", floatMask, "% mul " + floatMask);
			// } else if (flag != 0) {
			// //asmCode("", "sl", "r1", "8", "", "% << 8");
			// asmCode("", "muli", "r1", "r1", floatMask, "% mul " + floatMask);
			// }
			// asmCode("", "sl", "r2", "8", "", "% << 8");
		} else if (operToken.getValue().equals("and")) {
			opType = "and";
		} else if (operToken.getValue().equals("or")) {
			opType = "or";
		} else if (operToken.getValue().equals("==")) {
			opType = "ceq";
		} else if (operToken.getValue().equals("<>")) {
			opType = "cne";
		} else if (operToken.getValue().equals("<")) {
			opType = "clt";
		} else if (operToken.getValue().equals("<=")) {
			opType = "cle";
		} else if (operToken.getValue().equals(">")) {
			opType = "cgt";
		} else if (operToken.getValue().equals(">=")) {
			opType = "cge";
		}
		moonCode.add("\t\t" + opType + "\t" + "r3,\t" + "r1,\t" + "r2");
		String addr = "E_" + addrCount++;
		symLHS.symbolType = SYMBOLTYPE.UNKNOWN;
		symLHS.setAddress(addr);
		moonData.add(addr + "\t" + "dw" + "\t" + "0");
		moonCode.add("\t\t" + "sw\t" + addr + "(r0),\t" + "r3");
		return true;
	}

	public int addrCount = 0;

	private boolean loadWord(Symbol symbol, String reg, String comment) {
		if (symbol.symbolType == SYMBOLTYPE.NUM) {
			String addr = "V_" + addrCount++;
			if (symbol.getDataType().getValue().equals("T_FLOAT")) {

			} else {
				moonData.add(addr + "\t" + "dw" + "\t"
						+ symbol.getToken().getValue());
				moonCode.add("\t\t" + "lw\t" + reg + ",\t" + addr + "(r0)"
						+ comment);
			}
			return true;
		}
		Symbol tSym = new Symbol();
		Symbol vSym = new Symbol();
		boolean isValid = false;
		int offset = 0;
		if (firstTable != null) {
			if (symbol.symbolType != SYMBOLTYPE.FUNCTION) {
				for (int i = 0; i < firstTable.getSymbolList().size(); i++) {
					tSym = firstTable.getSymbolList().get(i);
					if (tSym.symbolType == SYMBOLTYPE.CLASS) {
						for (int j = 0; j < tSym.getChildTable()
								.getSymbolList().size(); j++) {
							vSym = tSym.getChildTable().getSymbolList().get(j);
							if (vSym.getAddress().equals(symbol.getAddress())) {
								isValid = true;
								break;
							}
							if (vSym.isArray()) {
								offset += getArrayMemory(vSym);
							} else {
								offset += vSym.getMemory();
							}
						}
						if (isValid) {
							break;
						}
					}
					if (tSym.symbolType == SYMBOLTYPE.PROGRAM) {
						for (int j = 0; j < tSym.getChildTable()
								.getSymbolList().size(); j++) {
							vSym = tSym.getChildTable().getSymbolList().get(j);
							if (vSym.getAddress().equals(symbol.getAddress())) {
								isValid = true;
								break;
							}
							if (vSym.isArray()) {
								offset += getArrayMemory(vSym);
							} else {
								offset += vSym.getMemory();
							}
						}
						if (isValid) {
							break;
						}
					}
				}
			}
		}

		if (symbol.symbolType == SYMBOLTYPE.ISCLASSORFUNC
				|| symbol.symbolType != SYMBOLTYPE.UNKNOWN && symbol.isArray()) {
			moonCode.add("\t\t" + "lw\t" + reg + ",\t" + symbol.getAddress()
					+ "(r11)" + comment);
		} else {
			moonCode.add("\t\t" + "lw\t" + reg + ",\t" + symbol.getAddress()
					+ "(r0)" + comment);
		}

		// if(symbol.symbolType == SYMBOLTYPE.ISVARDECLARED)
		return true;
	}

	private boolean storeWord(Symbol symbol) {
		if (firstTable != null) {
			Symbol tSym = new Symbol();
			Symbol vSym = new Symbol();
			boolean isValid = false;
			int offset = 0;

			if (symbol.symbolType != SYMBOLTYPE.FUNCTION) {
				for (int i = 0; i < firstTable.getSymbolList().size(); i++) {
					tSym = firstTable.getSymbolList().get(i);
					if (tSym.symbolType == SYMBOLTYPE.CLASS) {
						for (int j = 0; j < tSym.getChildTable()
								.getSymbolList().size(); j++) {
							vSym = tSym.getChildTable().getSymbolList().get(j);
							if (vSym.getAddress().equals(symbol.getAddress())) {
								isValid = true;
								break;
							}
							if (vSym.isArray()) {
								offset += getArrayMemory(vSym);
							} else {
								offset += vSym.getMemory();
							}
						}
						if (isValid) {
							break;
						}
					}
					if (tSym.symbolType == SYMBOLTYPE.PROGRAM) {
						for (int j = 0; j < tSym.getChildTable()
								.getSymbolList().size(); j++) {
							vSym = tSym.getChildTable().getSymbolList().get(j);
							if (vSym.getAddress().equals(symbol.getAddress())) {
								isValid = true;
								break;
							}
							if (vSym.isArray()) {
								offset += getArrayMemory(vSym);
							} else {
								offset += vSym.getMemory();
							}
						}
						if (isValid) {
							break;
						}
					}
				}
			}
			if (symbol.symbolType == SYMBOLTYPE.ISCLASSORFUNC
					|| symbol.symbolType != SYMBOLTYPE.UNKNOWN
					&& symbol.isArray()) {
				popReg("r11");
				moonCode.add("\t\t" + "sw\t" + symbol.getAddress() + "(r11),\t"
						+ "r1");
			} else {
				moonCode.add("\t\t" + "sw\t" + symbol.getAddress() + "(r0),\t"
						+ "r1");
			}
		}
		return true;
	}

	public boolean genPutCode(Symbol symbol) {
		pushReg("r15");
		loadWord(symbol, "r1", "\t\t% PUT  " + symbol.getToken().getValue());
		moonCode.add("\t\t" + "jl\t" + "r15" + ",\t" + "putint");

		// print '\r\n'
		moonCode.add("" + "addi\t" + "r1," + "r0," + "13" + "% print LFCR");
		moonCode.add("" + "putc\t" + "r1" + "" + "");
		moonCode.add("" + "addi\t" + "r1," + "r0," + "10");
		moonCode.add("" + "putc\t" + "r1" + "" + "");
		// print '\r\n'

		popReg("r15");
		return true;
	}

	public boolean genGetCode(Symbol symbol) {
		pushReg("r15");
		pushOffset(symbol);
		moonCode.add("\t\t" + "jl\t" + "r15" + ",\t" + "getint");
		storeWord(symbol);
		popReg("r15");
		return true;
	}

	public boolean genNotCode(Symbol symbol) {
		String addr = "NOT_" + addrCount++;
		loadWord(symbol, "r1", "%not " + symbol.getToken().getValue());
		moonCode.add("\t\t" + "not\t" + "r3" + ",\t" + "r1");
		moonData.add(addr + "\t" + "dw" + "\t" + symbol.getToken().getValue());
		moonCode.add("\t\t" + "sw\t" + addr + "\t(r0),\t" + "r3");
		if (symbol.symbolType == SYMBOLTYPE.NUM) {
			symbol.symbolType = SYMBOLTYPE.UNKNOWN;
		}
		symbol.setAddress(addr);
		return true;
	}

	public boolean genSignCode(Symbol symbol) {
		String addr = "SIGN_" + addrCount++;
		moonCode.add("\t\t" + "add\t" + "r1,\t" + "r0,\t" + "r0");
		loadWord(symbol, "r2", "");
		moonCode.add("\t\t" + "sub\t" + "r3,\t" + "r1,\t" + "r2");
		moonData.add(addr + "\t" + "dw" + "\t0");
		moonCode.add("\t\t" + "sw\t" + addr + "\t(r0),\t" + "r3");
		if (symbol.symbolType == SYMBOLTYPE.NUM) {
			symbol.symbolType = SYMBOLTYPE.UNKNOWN;
		}
		symbol.setAddress(addr);
		return true;
	}

	public boolean resetOffset() {
		moonCode.add("");
		moonCode.add("\t\t" + "add\t" + "r11,\t" + "r0,\t" + "r0"
				+ "\t% RESET OFFSET");
		moonCode.add("");
		return true;
	}

	public boolean pushOffset(Symbol symbol) {
		if (symbol.symbolType == SYMBOLTYPE.ISCLASSORFUNC
				|| (symbol.symbolType != SYMBOLTYPE.UNKNOWN && symbol.isArray())) {
			pushReg("r11");
		}
		return true;
	}

	public boolean popOffset(Symbol symbol) {
		if (symbol.symbolType == SYMBOLTYPE.ISCLASSORFUNC
				|| (symbol.symbolType != SYMBOLTYPE.UNKNOWN && symbol.isArray())) {
			popReg("r11");
		}
		return true;
	}

	public boolean setClassOffset(int offSet) {
		moonCode.add("\t\t" + "addi\t" + "r11,\t" + "r11,\t" + offSet
				+ "\t% Class Variable Offset " + offSet);
		return true;
	}

	public boolean pushReg(String reg) {
		moonCode.add("");
		moonCode.add("\t\t" + "subi\t" + "r14,\t" + "r14,\t" + "4"
				+ "\t% PUSH " + reg);
		moonCode.add("\t\t" + "sw\t" + "topaddr(r14),\t" + reg + "" + "");
		moonCode.add("");
		return true;
	}

	public boolean popReg(String reg) {
		moonCode.add("");
		moonCode.add("\t\t" + "lw\t" + reg + ",\t" + "topaddr(r14)"
				+ "\t%\tPOP " + reg);
		moonCode.add("\t\t" + "addi\t" + "r14,\t" + "r14,\t" + "4" + "");
		moonCode.add("");
		return true;
	}

	public String genCodeIf(Symbol expr) {
		moonCode.add("\t% IF( " + expr + ") THEN");
		loadWord(expr, "r1", "");
		String elseAddr = "ELSE_" + addrCount++;
		moonCode.add("\t\t" + "bz\t" + "r1,\t" + elseAddr);
		return elseAddr;
	}

	public String genCodeElse(Symbol expr, String elseAddr) {
		String elseEndAddr = "ENDIF_" + addrCount++;
		moonCode.add("\t% IF THEN ELSE");
		moonCode.add("\t\t"+ "j\t" + elseEndAddr );
		moonCode.add(elseAddr);
		return elseEndAddr;
	}

	public boolean genCodeEndThen(Symbol expr, String elseEndAddr) {
		moonCode.add("\t% END IF");
		moonCode.add(elseEndAddr);
		return true;
	}
}
