package compiler.syntacticAnalysis;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import compiler.lexer.Lexer;
import compiler.lexer.Token;

public class SynatcticParser {

	Lexer lexer = new Lexer();
	FirstAndFollw firstAndFollw = null;
	Token token = null;
	private Logger grammarLog;
	private Logger parserLog;
	private Logger derivationLog;

	public SynatcticParser(Lexer lexer) {
		this.lexer = lexer;
		firstAndFollw = new FirstAndFollw();
		grammarLog = setLogger("GrammarLog.txt");
		parserLog = setLogger("ParserLog.txt");
		derivationLog = setLogger("DerivationLog.txt");
	}

	class FormatLog extends Formatter {
		public String format(LogRecord logRecord) {
			return logRecord.getLevel() + "| " + logRecord.getMessage() + "\n";
		}
	}

	/**
	 * @param logger
	 * 
	 */
	private Logger setLogger(String logFileName) {
		String logFile = System.getProperty("user.dir") + "\\logs\\"
				+ logFileName;
		Logger logger = Logger.getLogger(logFile);
		if (logger.getHandlers().length == 0) {
			FileHandler fileHandler = null;
			try {
				fileHandler = new FileHandler(logFile, true);
				SimpleFormatter textFormatter = new SimpleFormatter();
				fileHandler.setFormatter(textFormatter);
				fileHandler.setFormatter(new FormatLog());
				logger.addHandler(fileHandler);
				logger.setUseParentHandlers(false);
			} catch (SecurityException | IOException e) {
				System.out
						.println("Logger initialization error. Check File Permissions!!!");
				e.printStackTrace();
			} finally {
			}
		}
		return logger;
	}

	/**
	 * parse(){ lookahead = nextToken() if (startSymbol() ^ match(“$”) )
	 * return(true) else return(false) }
	 */
	public boolean parse() {
		boolean val = false;
		getNextToken();
		if (token == null) {
			System.out.println("Error in Input File");
			grammarLog.info("Error in Input File");
			return false;
		}
		return startSymbol();
	}

	private boolean startSymbol() {
		return prog();
	}
	
	DerivationTree  prog;

	// prog -> classDeclList progBody
	private boolean prog() {
		 prog = new DerivationTree("prog", "");
		 DerivationTree classDeclList = new DerivationTree("classDeclList", "");
		 DerivationTree progBody = new DerivationTree("progBody", "");
		 derivationLog.info(prog.info());
		if (classDeclList() && progBody()) {
			System.out.println("prog -> classDeclList progBody");
			grammarLog.info("prog -> classDeclList progBody");
			return true;
		} else
			return false;
	}

	// classDeclList -> classDecl classDeclList
	// classDeclList -> EPSILON
	private boolean classDeclList() {
		if (!skipErrors("classDecl", "classDeclList")) {
			return false;
		}
		if (checkFirstSet("classDecl")) {
			if (classDecl() && classDeclList()) {
				System.out
						.println("classDeclList    -> classDecl classDeclList");
				grammarLog
						.info("classDeclList    -> classDecl classDeclList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("classDeclList")) {
			System.out.println("classDeclList    -> EPSILON");
			grammarLog.info("classDeclList    -> EPSILON");
			return true;
		}
		return false;
	}

	// classDecl -> class id { varDecFunDef } ;
	private boolean classDecl() {
		if (!skipErrors("classDecl", "")) {
			return false;
		}
		if (checkFirstSet("class")) {
			if (matchTokenType("T_RESERVE_WORD_CLASS")
					&& matchTokenType("T_IDENTIFIER")
					&& matchTokenType("T_DEL_C_LPAREN")) {
				if (varDecFunDef() && matchTokenType("T_DEL_C_RPAREN")
						&& matchTokenType("T_DEL_SEMICOLON")) {
					System.out
							.println("classDecl        -> class id { varDecFunDef } ;");
					grammarLog
							.info("classDecl        -> class id { varDecFunDef } ;");
					return true;
				}
			}
		}
		return false;
	}

	// varDecFunDef -> type id varDecFunDef1
	// varDecFunDef -> EPSILON
	private boolean varDecFunDef() {
		if (!skipErrors("type", "varDecFunDef")) {
			return false;
		}
		if (checkFirstSet("type")) {
			if (matchType() && matchTokenType("T_IDENTIFIER")
					&& varDecFunDef1()) {
				System.out.println("varDecFunDef -> type id varDecFunDef1");
				grammarLog.info("varDecFunDef -> type id varDecFunDef1");
				return true;
			} else {
				return false;
			}
		} else if (checkFollowSet("varDecFunDef")) {
			System.out.println("varDecFunDef    -> EPSILON");
			grammarLog.info("varDecFunDef    -> EPSILON");
			return true;
		}
		return false;
	}

	// varDecFunDef1 -> ( fParams ) funcBody ; funcDefList
	// varDecFunDef1 -> arraySizeList ; varDecFunDef
	private boolean varDecFunDef1() {
		if (!skipErrors("varDecFunDef1", "")) {
			return false;
		}
		if (checkFirstSet("(")) {
			if (matchTokenType("T_DEL_R_LPAREN") && fParams()
					&& matchTokenType("T_DEL_R_RPAREN") && funcBody()
					&& matchTokenType("T_DEL_SEMICOLON") && funcDefList()) {
				System.out
						.println("varDecFunDef1 -> ( fParams ) funcBody ; funcDefList");
				grammarLog
						.info("varDecFunDef1 -> ( fParams ) funcBody ; funcDefList");
				return true;
			}
		} else if (checkFirstSet("varDecFunDef1")) {
			if (arraySizeList() && matchTokenType("T_DEL_SEMICOLON")
					&& varDecFunDef()) {
				System.out
						.println("varDecFunDef1 -> arraySizeList ; varDecFunDef");
				grammarLog
						.info("varDecFunDef1 -> arraySizeList ; varDecFunDef");
				return true;
			} else
				return false;
		}
		return false;
	}

	// progBody -> program funcBody ; funcDefList
	private boolean progBody() {
		if (!skipErrors("program", "")) {
			return false;
		}
		if (checkFirstSet("program")) {
			if (matchTokenValue("program") && funcBody()
					&& matchTokenType("T_DEL_SEMICOLON") && funcDefList()) {
				System.out
						.println("progBody -> program funcBody ; funcDefList");
				grammarLog
						.info("progBody -> program funcBody ; funcDefList");
				return true;
			}
		}
		return false;
	}

	// funcDefList -> funcDef funcDefList
	// funcDefList -> EPSILON
	private boolean funcDefList() {
		if (checkFirstSet("funcDef")) {
			if (funcDef() && funcDefList()) {
				System.out.println("funcDefList -> funcDef funcDefList");
				grammarLog.info("funcDefList -> funcDef funcDefList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("funcDefList")) {
			System.out.println("funcDefList -> EPSILON");
			grammarLog.info("funcDefList -> EPSILON");
			return true;
		}
		return false;
	}

	// funcDef -> funcHead funcBody ;
	private boolean funcDef() {
		if (!skipErrors("funcHead", "")) {
			return false;
		}
		if (checkFirstSet("funcHead")) {
			if (funcHead() && funcBody() && matchTokenType("T_DEL_SEMICOLON")) {
				System.out.println("funcDef -> funcHead funcBody ;");
				grammarLog.info("funcDef -> funcHead funcBody ;");
				return true;
			}
		}
		return false;
	}

	// funcHead -> type id ( fParams )
	private boolean funcHead() {
		if (!skipErrors("type", "")) {
			return false;
		}
		if (checkFirstSet("type")) {
			if (matchType() && matchTokenType("T_IDENTIFIER")
					&& matchTokenType("T_DEL_R_LPAREN") && fParams()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("funcHead -> type id ( fParams )");
				grammarLog.info("funcHead -> type id ( fParams )");
				return true;
			} else
				return false;
		}
		return false;
	}

	// fParams -> type id arraySizeList fParamsTailList
	// fParams -> EPSILON
	private boolean fParams() {
		if (!skipErrors("type", "fParams")) {
			return false;
		}
		if (checkFirstSet("type")) {
			if (matchType() && matchTokenType("T_IDENTIFIER")
					&& arraySizeList() && fParamsTailList()) {
				System.out
						.println("fParams -> type id arraySizeList fParamsTailList");
				grammarLog
						.info("fParams -> type id arraySizeList fParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("fParams")) {
			System.out.println("funcDefList -> EPSILON");
			grammarLog.info("funcDefList -> EPSILON");
			return true;
		}
		return false;
	}

	// fParamsTailList -> fParamsTail fParamsTailList
	// fParamsTailList -> EPSILON
	private boolean fParamsTailList() {
		if (!skipErrors("fParamsTail", "fParamsTailList")) {
			return false;
		}
		if (checkFirstSet("fParamsTail")) {
			if (fParamsTail() && fParamsTailList()) {
				System.out
						.println("fParamsTailList -> fParamsTail fParamsTailList");
				grammarLog
						.info("fParamsTailList -> fParamsTail fParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("fParamsTailList")) {
			System.out.println("fParamsTailList -> EPSILON");
			grammarLog.info("fParamsTailList -> EPSILON");
			return true;
		}
		return false;
	}

	// fParamsTail -> , type id arraySizeList
	private boolean fParamsTail() {
		if (!skipErrors(",", "")) {
			return false;
		}
		if (checkFirstSet(",")) {
			if (matchTokenType("T_DEL_COMMA") && matchType()
					&& matchTokenType("T_IDENTIFIER") && arraySizeList()) {
				System.out.println("fParamsTail -> , type id arraySizeList");
				grammarLog.info("fParamsTail -> , type id arraySizeList");
				return true;
			} else
				return false;
		}
		return false;
	}

	// funcBody -> { bodyCode }
	private boolean funcBody() {
		if (!skipErrors("{", "")) {
			return false;
		}
		if (checkFirstSet("{")) {
			if (matchTokenType("T_DEL_C_LPAREN") && bodyCode()
					&& matchTokenType("T_DEL_C_RPAREN")) {
				System.out.println("funcBody -> { bodyCode }");
				grammarLog.info("funcBody -> { bodyCode }");
				return true;
			} else
				return false;
		}
		return false;
	}

	// bodyCode -> float id arraySizeList ; bodyCode
	// bodyCode -> int id arraySizeList ; bodyCode
	// bodyCode -> id bodyCode2
	// bodyCode -> ctrlStat ; statementList
	// bodyCode -> EPSILON
	// first Set == int float id if get put return for
	// follow Set == }
	private boolean bodyCode() {
		if (!skipErrors("bodyCode", "bodyCode")) {
			return false;
		}
		if (checkFirstSet("float")) {
			if (matchType() && matchTokenType("T_IDENTIFIER")
					&& arraySizeList() && matchTokenType("T_DEL_SEMICOLON")
					&& bodyCode()) {
				System.out
						.println("bodyCode -> float id arraySizeList ; bodyCode");
				grammarLog
						.info("bodyCode -> float id arraySizeList ; bodyCode");
				return true;
			} else
				return false;
		} else if (checkFirstSet("int")) {
			if (matchType() && matchTokenType("T_IDENTIFIER")
					&& arraySizeList() && matchTokenType("T_DEL_SEMICOLON")
					&& bodyCode()) {
				System.out
						.println("bodyCode -> int id arraySizeList ; bodyCode");
				grammarLog
						.info("bodyCode -> int id arraySizeList ; bodyCode");
				return true;
			} else
				return false;
		} else if (checkFirstSet("id")) {
			if (matchTokenType("T_IDENTIFIER") && bodyCode2()) {
				System.out.println("bodyCode -> id bodyCode2");
				grammarLog.info("bodyCode -> id bodyCode2");
				return true;
			} else
				return false;
		} else if (checkFirstSet("ctrlStat")) {
			if (ctrlStat() && matchTokenType("T_DEL_SEMICOLON")
					&& statementList()) {
				System.out.println("bodyCode -> ctrlStat ; statementList");
				grammarLog.info("bodyCode -> ctrlStat ; statementList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("bodyCode")) {
			System.out.println("bodyCode -> EPSILON");
			grammarLog.info("bodyCode -> EPSILON");
			return true;
		}
		return false;
	}

	// bodyCode2 -> id arraySizeList ; bodyCode
	// bodyCode2 -> indiceList dotIdList assignOp expr ; statementList
	// bodyCode2 -> EPSILON
	private boolean bodyCode2() {
		if (!skipErrors("bodyCode2", "bodyCode2")) {
			return false;
		}
		if (checkFirstSet("id")) {
			if (matchTokenType("T_IDENTIFIER") && arraySizeList()
					&& matchTokenType("T_DEL_SEMICOLON") && bodyCode()) {
				System.out.println("bodyCode2 -> id arraySizeList ; bodyCode");
				grammarLog.info("bodyCode2 -> id arraySizeList ; bodyCode");
				return true;
			} else
				return false;
		} else if (checkFirstSet("bodyCode2")) {
			if (indiceList() && dotIdList() && matchTokenValue("=") && expr()
					&& matchTokenType("T_DEL_SEMICOLON") && statementList()) {
				System.out
						.println("bodyCode2 -> indiceList dotIdList assignOp expr ; statementList");
				grammarLog
						.info("bodyCode2 -> indiceList dotIdList assignOp expr ; statementList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("bodyCode2")) {
			System.out.println("bodyCode2 -> EPSILON");
			grammarLog.info("bodyCode2 -> EPSILON");
			return true;
		}
		return false;
	}

	// statementList -> statement statementList
	// statementList -> EPSILON
	private boolean statementList() {
		if (!skipErrors("statement", "statementList")) {
			return false;
		}
		if (checkFirstSet("statement")) {
			if (statement() && statementList()) {
				System.out.println("statementList -> statement statementList");
				grammarLog.info("statementList -> statement statementList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("statementList")) {
			System.out.println("statementList -> EPSILON");
			grammarLog.info("statementList -> EPSILON");
			return true;
		}
		return false;
	}

	// statement -> ctrlStat ;
	// statement -> assignStat ;
	private boolean statement() {
		if (!skipErrors("statement", ";")) {
			return false;
		}
		if (checkFirstSet("ctrlStat")) {
			if (ctrlStat() && matchTokenType("T_DEL_SEMICOLON")) {
				System.out.println("statement -> ctrlStat ;");
				grammarLog.info("statement -> ctrlStat ;");
				return true;
			} else
				return false;
		} else if (checkFirstSet("assignStat")) {
			if (assignStat() && matchTokenType("T_DEL_SEMICOLON")) {
				System.out.println("statement -> assignStat ;");
				grammarLog.info("statement -> assignStat ;");
				return true;
			} else
				return false;
		}
		return false;
	}

	// statBlock -> { statementList } | statement
	// statBlock -> EPSILON
	private boolean statBlock() {
		if (!skipErrors("statBlock", "statBlock")) {
			return false;
		}
		if (checkFirstSet("{")) {
			if (matchTokenType("T_DEL_C_LPAREN") && statementList()
					&& matchTokenType("T_DEL_C_RPAREN")) {
				System.out.println("statBlock -> { statementList } ");
				grammarLog.info("statBlock -> { statementList } ");
				return true;
			} else
				return false;
		} else if (checkFirstSet("statBlock")) {
			if (statement()) {
				System.out.println("statBlock -> { statementList } ");
				grammarLog.info("statBlock -> { statementList } ");
				return true;
			} else
				return false;
		} else if (checkFollowSet("statBlock")) {
			System.out.println("statBlock -> EPSILON");
			grammarLog.info("statBlock -> EPSILON");
			return true;
		}
		return false;
	}

	// ctrlStat -> for ( type id assignOp expr ; relExpr ; assignStat )
	// statBlock
	// ctrlStat -> if ( expr ) then statBlock else statBlock
	// ctrlStat -> get ( variable )
	// ctrlStat -> put ( expr )
	// ctrlStat -> return ( expr )
	private boolean ctrlStat() {
		if (!skipErrors("ctrlStat", "")) {
			return false;
		}
		if (checkFirstSet("if")) {
			if (matchTokenType("T_RESERVE_WORD_IF")
					&& matchTokenType("T_DEL_R_LPAREN") && expr()
					&& matchTokenType("T_DEL_R_RPAREN")
					&& matchTokenType("T_RESERVE_WORD_THEN") && statBlock()
					&& matchTokenType("T_RESERVE_WORD_ELSE") && statBlock()) {
				System.out
						.println("ctrlStat -> if ( expr ) then statBlock else statBlock");
				grammarLog
						.info("ctrlStat -> if ( expr ) then statBlock else statBlock");
				return true;
			} else
				return false;
		} else if (checkFirstSet("for")) {
			if (matchTokenType("T_RESERVE_WORD_FOR")
					&& matchTokenType("T_DEL_R_LPAREN") && matchType()
					&& matchTokenType("T_IDENTIFIER")
					&& matchTokenType("T_OP_ASSIGN_EQUAL") && expr()
					&& matchTokenType("T_DEL_SEMICOLON") && relExpr()
					&& matchTokenType("T_DEL_SEMICOLON") && assignStat()
					&& matchTokenType("T_DEL_R_RPAREN") && statBlock()) {
				System.out
						.println("ctrlStat -> for ( type id assignOp expr ; relExpr ; assignStat ) statBlock");
				grammarLog
						.info("ctrlStat -> for ( type id assignOp expr ; relExpr ; assignStat ) statBlock");
				return true;
			} else
				return false;
		} else if (checkFirstSet("get")) {
			if (matchTokenType("T_RESERVE_WORD_GET")
					&& matchTokenType("T_DEL_R_LPAREN") && variable()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("ctrlStat -> get ( variable )");
				grammarLog.info("ctrlStat -> get ( variable )");
				return true;
			} else
				return false;
		} else if (checkFirstSet("put")) {
			if (matchTokenType("T_RESERVE_WORD_PUT")
					&& matchTokenType("T_DEL_R_LPAREN") && expr()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("ctrlStat -> put ( expr )");
				grammarLog.info("ctrlStat -> put ( expr )");
				return true;
			} else
				return false;
		} else if (checkFirstSet("return")) {
			if (matchTokenValue("return") && matchTokenType("T_DEL_R_LPAREN")
					&& expr() && matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("ctrlStat -> return ( expr )");
				grammarLog.info("ctrlStat -> return ( expr )");
				return true;
			} else
				return false;
		}
		return false;
	}

	// assignStat -> variable assignOp expr
	private boolean assignStat() {
		if (!skipErrors("assignStat", "")) {
			return false;
		}
		if (checkFirstSet("variable")) {
			if (variable() && matchTokenType("T_OP_ASSIGN_EQUAL") && expr()) {
				System.out.println("assignStat -> variable assignOp expr");
				grammarLog.info("assignStat -> variable assignOp expr");
				return true;
			} else
				return false;
		}
		return false;
	}

	// expr -> arithExpr subExpr
	private boolean expr() {
		if (!skipErrors("arithExpr", "")) {
			return false;
		}
		if (checkFirstSet("arithExpr")) {
			if (arithExpr() && subExpr()) {
				System.out.println("expr -> arithExpr subExpr");
				grammarLog.info("expr -> arithExpr subExpr");
				return true;
			} else
				return false;
		}
		return false;
	}

	// arithExpr -> term arithExprRight
	private boolean arithExpr() {
		if (!skipErrors("term", "")) {
			return false;
		}
		if (checkFirstSet("term")) {
			if (term() && arithExprRight()) {
				System.out.println("arithExpr -> term arithExprRight");
				grammarLog.info("arithExpr -> term arithExprRight");
				return true;
			} else
				return false;
		}
		return false;
	}

	// subExpr -> relOp arithExpr
	// subExpr -> EPSILON
	// relOp -> < | <= | <> | == | > | >=
	private boolean subExpr() {
		if (!skipErrors("relOp", "subExpr")) {
			return false;
		}
		if (checkFirstSet("relOp")) {
			if (matchTokenType("relOp") && arithExpr()) {
				System.out.println("subExpr -> relOp arithExpr");
				grammarLog.info("subExpr -> relOp arithExpr");
				return true;
			} else
				return false;
		} else if (checkFollowSet("subExpr")) {
			System.out.println("subExpr -> EPSILON");
			grammarLog.info("subExpr -> EPSILON");
			return true;
		}
		return false;
	}

	// arithExprRight -> addOp term arithExprRight
	// arithExprRight -> EPSILON
	private boolean arithExprRight() {
		if (!skipErrors("addOp", "arithExprRight")) {
			return false;
		}
		if (checkFirstSet("addOp")) {
			if (matchTokenType("addOp") && term() && arithExprRight()) {
				System.out
						.println("arithExprRight -> addOp term arithExprRight");
				grammarLog
						.info("arithExprRight -> addOp term arithExprRight");
				return true;
			} else
				return false;
		} else if (checkFollowSet("arithExprRight")) {
			System.out.println("arithExprRight -> EPSILON");
			grammarLog.info("arithExprRight -> EPSILON");
			return true;
		}
		return false;
	}

	// relExpr -> arithExpr relOp arithExpr
	private boolean relExpr() {
		if (!skipErrors("arithExpr", "")) {
			return false;
		}
		if (checkFirstSet("arithExpr")) {
			if (arithExpr() && matchTokenType("relOp") && arithExpr()) {
				System.out.println("relExpr -> arithExpr relOp arithExpr");
				grammarLog.info("relExpr -> arithExpr relOp arithExpr");
				return true;
			} else
				return false;
		}
		return false;
	}

	// term -> factor termFactor
	private boolean term() {
		if (!skipErrors("factor", "")) {
			return false;
		}
		if (checkFirstSet("factor")) {
			if (factor() && termFactor()) {
				System.out.println("term -> factor termFactor");
				grammarLog.info("term -> factor termFactor");
				return true;
			} else
				return false;
		}
		return false;
	}

	// termFactor -> multOp factor termFactor
	// termFactor -> EPSILON
	private boolean termFactor() {
		if (!skipErrors("multOp", "termFactor")) {
			return false;
		}
		if (checkFirstSet("multOp")) {
			if (matchTokenType("multOp") && factor() && termFactor()) {
				System.out.println("termFactor -> multOp factor termFactor");
				grammarLog.info("termFactor -> multOp factor termFactor");
				return true;
			} else
				return false;
		} else if (checkFollowSet("termFactor")) {
			System.out.println("termFactor -> EPSILON");
			grammarLog.info("termFactor -> EPSILON");
			return true;
		}
		return false;
	}

	// factor -> ( arithExpr )
	// factor -> not factor
	// factor -> num
	// factor -> sign factor
	// factor -> id indiceOrParam
	private boolean factor() {
		if (!skipErrors("factor", "")) {
			return false;
		}
		if (checkFirstSet("(")) {
			if (matchTokenType("T_DEL_R_LPAREN") && arithExpr()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("factor -> ( arithExpr )");
				grammarLog.info("factor -> ( arithExpr )");
				return true;
			} else
				return false;
		} else if (checkFirstSet("not")) {
			if (matchTokenType("T_LOGICAL_NOT") && factor()) {
				System.out.println("factor -> not factor");
				grammarLog.info("factor -> not factor");
				return true;
			} else
				return false;
		} else if (checkFirstSet("num")) {
			if (matchNum()) {
				System.out.println("factor -> num");
				grammarLog.info("factor -> num");
				return true;
			} else
				return false;
		} else if (checkFirstSet("sign")) {
			if (matchSign() && factor()) {
				System.out.println("factor -> sign factor");
				grammarLog.info("factor -> sign factor");
				return true;
			} else
				return false;
		} else if (checkFirstSet("id")) {
			if (matchType() && indiceOrParam()) {
				System.out.println("factor -> id indiceOrParam");
				grammarLog.info("factor -> id indiceOrParam");
				return true;
			} else
				return false;
		}
		return false;
	}

	// indiceOrParam -> indiceList idFactor
	// indiceOrParam -> ( aParams )
	private boolean indiceOrParam() {
		if (!skipErrors("indiceOrParam", "indiceOrParam")) {
			return false;
		}
		if (checkFirstSet("(")) {
			if (matchTokenType("T_DEL_R_LPAREN") && aParams()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("indiceOrParam -> ( aParams )");
				grammarLog.info("indiceOrParam -> ( aParams )");
				return true;
			} else
				return false;
		} else if (checkFirstSet("indiceOrParam")) {
			if (indiceList() && idFactor()) {
				System.out.println("indiceOrParam -> indiceList idFactor");
				grammarLog.info("indiceOrParam -> indiceList idFactor");
				return true;
			} else
				return false;
		} else if (checkFollowSet("indiceOrParam")) {
			System.out.println("indiceOrParam -> EPSILON");
			grammarLog.info("indiceOrParam -> EPSILON");
			return true;
		}
		return false;
	}

	// idFactor -> . id indiceOrParam
	// idFactor -> EPSILON
	private boolean idFactor() {
		if (!skipErrors(".", "idFactor")) {
			return false;
		}
		if (checkFirstSet(".")) {
			if (matchTokenType("T_DEL_DOT") && matchTokenType("T_IDENTIFIER")
					&& indiceOrParam()) {
				System.out.println("idFactor -> . id indiceOrParam");
				grammarLog.info("idFactor -> . id indiceOrParam");
				return true;
			} else
				return false;
		} else if (checkFollowSet("idFactor")) {
			System.out.println("idFactor -> EPSILON");
			grammarLog.info("idFactor -> EPSILON");
			return true;
		}
		return false;
	}

	// indiceList -> indice indiceList
	// indiceList -> EPSILON
	private boolean indiceList() {
		if (!skipErrors("indice", "indiceList")) {
			return false;
		}
		if (checkFirstSet("indice")) {
			if (indice() && indiceList()) {
				System.out.println("indiceList -> indice indiceList");
				grammarLog.info("indiceList -> indice indiceList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("indiceList")) {
			System.out.println("indiceList -> EPSILON");
			grammarLog.info("indiceList -> EPSILON");
			return true;
		}
		return false;
	}

	// dotIdList -> . id indiceList
	// dotIdList -> EPSILON
	private boolean dotIdList() {
		if (!skipErrors(".", "dotIdList")) {
			return false;
		}
		if (checkFirstSet(".")) {
			if (matchTokenType("T_DEL_DOT") && matchTokenType("T_IDENTIFIER")
					&& indiceList()) {
				System.out.println("dotIdList -> . id indiceList");
				grammarLog.info("dotIdList -> . id indiceList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("dotIdList")) {
			System.out.println("dotIdList -> EPSILON");
			grammarLog.info("dotIdList -> EPSILON");
			return true;
		}
		return false;
	}

	// variable -> id indiceList dotIdNest
	private boolean variable() {
		if (!skipErrors("id", "")) {
			return false;
		}
		if (checkFirstSet("id")) {
			if (matchTokenType("T_IDENTIFIER") && indiceList() && dotIdNest()) {
				System.out.println("variable -> id indiceList dotIdNest");
				grammarLog.info("variable -> id indiceList dotIdNest");
				return true;
			} else
				return false;
		}
		return false;
	}

	// dotIdNest -> . id indiceList dotIdNest
	// dotIdNest -> EPSILON
	private boolean dotIdNest() {
		if (!skipErrors(".", "dotIdNest")) {
			return false;
		}
		if (checkFirstSet(".")) {
			if (matchTokenType("T_DEL_DOT") && matchTokenType("T_IDENTIFIER")
					&& indiceList() && dotIdNest()) {
				System.out.println("dotIdNest -> . id indiceList dotIdNest");
				grammarLog.info("dotIdNest -> . id indiceList dotIdNest");
				return true;
			} else
				return false;
		} else if (checkFollowSet("dotIdNest")) {
			System.out.println("dotIdNest -> EPSILON");
			grammarLog.info("dotIdNest -> EPSILON");
			return true;
		}
		return false;
	}

	// indice -> [ arithExpr ]
	private boolean indice() {
		if (!skipErrors("[", "")) {
			return false;
		}
		if (checkFirstSet("[")) {
			if (matchTokenType("T_DEL_S_LPAREN") && arithExpr()
					&& matchTokenType("T_DEL_S_RPAREN")) {
				System.out.println("indice -> [ arithExpr ]");
				grammarLog.info("indice -> [ arithExpr ]");
				return true;
			} else
				return false;
		}
		return false;
	}

	// aParams -> expr aParamsTailList
	// aParams -> EPSILON
	private boolean aParams() {
		if (!skipErrors("expr", "aParams")) {
			return false;
		}
		if (checkFirstSet("expr")) {
			if (expr() && aParamsTailList()) {
				System.out.println("aParams -> expr aParamsTailList");
				grammarLog.info("aParams -> expr aParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("aParams")) {
			System.out.println("aParams -> EPSILON");
			grammarLog.info("aParams -> EPSILON");
			return true;
		}
		return false;
	}

	// aParamsTailList -> aParamsTail aParamsTailList
	// aParamsTailList -> EPSILON
	private boolean aParamsTailList() {
		if (!skipErrors("aParamsTail", "aParamsTailList")) {
			return false;
		}
		if (checkFirstSet("aParamsTail")) {
			if (aParamsTail() && aParamsTailList()) {
				System.out
						.println("aParamsTailList -> aParamsTail aParamsTailList");
				grammarLog
						.info("aParamsTailList -> aParamsTail aParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("aParamsTailList")) {
			System.out.println("aParamsTailList -> EPSILON");
			grammarLog.info("aParamsTailList -> EPSILON");
			return true;
		}
		return false;
	}

	// aParamsTail -> , expr
	private boolean aParamsTail() {
		if (!skipErrors(",", "")) {
			return false;
		}
		if (checkFirstSet(",")) {
			if (matchTokenType("T_DEL_COMMA") && expr()) {
				System.out.println("aParamsTail -> , expr");
				grammarLog.info("aParamsTail -> , expr");
				return true;
			} else
				return false;
		}
		return false;
	}

	// arraySizeList -> arraySize arraySizeList
	// arraySizeList -> EPSILON
	private boolean arraySizeList() {
		if (!skipErrors("arraySize", "arraySizeList")) {
			return false;
		}
		if (checkFirstSet("arraySize")) {
			if (arraySize() && arraySizeList()) {
				System.out.println("arraySizeList -> arraySize arraySizeList");
				grammarLog.info("arraySizeList -> arraySize arraySizeList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("arraySizeList")) {
			System.out.println("arraySizeList ->  EPSILON");
			grammarLog.info("arraySizeList ->  EPSILON");
			return true;
		}
		return false;
	}

	// arraySize -> [ int ]
	private boolean arraySize() {
		if (!skipErrors("[", "")) {
			return false;
		}
		if (checkFirstSet("arraySize")) {
			if (matchTokenType("T_DEL_S_LPAREN") && matchTokenType("T_INTEGER")
					&& matchTokenType("T_DEL_S_RPAREN")) {
				System.out.println("arraySizeList -> arraySize arraySizeList");
				grammarLog.info("arraySizeList -> arraySize arraySizeList");
				return true;
			} else
				return false;
		}
		return false;
	}

	// relOp -> < | <= | <> | == | > | >=
	// addOp -> + | - | or
	private boolean matchTokenType(String tokenType) {
		if (tokenType.equals("relOp")
				&& token.getDesc().toString().startsWith("T_OP_REL_")) {
			System.out.println("relOp" + " -> " + token.getValue());
			grammarLog.info("relOp" + " -> " + token.getValue());
			getNextToken();
			return true;
		} else if (tokenType.equals("addOp")
				&& (token.getValue().equals("+")
						|| token.getValue().equals("-") || token.getValue()
						.equals("or"))) {
			System.out.println("addOp" + " -> " + token.getValue());
			grammarLog.info("addOp" + " -> " + token.getValue());
			getNextToken();
			return true;
		} else if (tokenType.equals("multOp")
				&& (token.getValue().equals("*")
						|| token.getValue().equals("/") || token.getValue()
						.equals("and"))) {
			System.out.println("multOp" + " -> " + token.getValue());
			grammarLog.info("multOp" + " -> " + token.getValue());
			getNextToken();
			return true;
		} else if (tokenType.equals(token.getDesc())) {
			System.out.println(token.getDesc() + " -> " + token.getValue());
			grammarLog.info(token.getDesc() + " -> " + token.getValue());
			getNextToken();
			return true;
		} else {
			System.out.println("ERROR: IN LINE NUMBER:\t" + token.getPosition()
					+ ":\tExpected One of these token Type:\t" + tokenType);
			parserLog.warning("ERROR: IN LINE NUMBER:\t"
					+ token.getPosition()
					+ ":\tExpected One of these token Type:\t" + tokenType);
			while (!token.getValue().equals("$")) {
				if (tokenType.equals("relOp")
						&& token.getDesc().toString().startsWith("T_OP_REL_")) {
					System.out.println("relOp" + " -> " + token.getValue());
					grammarLog.info("relOp" + " -> " + token.getValue());
					System.out.println("RESUME PARSING FROM TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					parserLog.info("RESUME PARSING FROM  TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
					return true;
				} else if (tokenType.equals("addOp")
						&& (token.getValue().equals("+")
								|| token.getValue().equals("-") || token
								.getValue().equals("or"))) {
					System.out.println("addOp" + " -> " + token.getValue());
					grammarLog.info("addOp" + " -> " + token.getValue());
					System.out.println("RESUME PARSING FROM TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					parserLog.info("RESUME PARSING FROM  TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
					return true;
				} else if (tokenType.equals("multOp")
						&& (token.getValue().equals("*")
								|| token.getValue().equals("/") || token
								.getValue().equals("and"))) {
					System.out.println("multOp" + " -> " + token.getValue());
					grammarLog.info("multOp" + " -> " + token.getValue());
					System.out.println("RESUME PARSING FROM TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					parserLog.info("RESUME PARSING FROM  TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
					return true;
				} else if (tokenType.equals(token.getDesc())) {
					System.out.println(token.getDesc() + " -> "
							+ token.getValue());
					grammarLog.info(token.getDesc() + " -> "
							+ token.getValue());
					System.out.println("RESUME PARSING FROM TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					parserLog.info("RESUME PARSING FROM  TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
					return true;
				} else {
					System.out.println("SKIPPING TOKEN:\t" + token.getValue()
							+ "\tAT LINE NUMBER:\t" + token.getPosition());
					parserLog.warning("SKIPPING TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
				}
				getNextToken();
			}
		}
		if (token.getValue().equals("$")) {
			System.out.println("REACHED END OF FILE");
			grammarLog.warning("REACHED END OF FILE");
			parserLog.warning("REACHED END OF FILE");
		}
		return false;
	}

	private boolean matchTokenValue(String tokenType) {
		if (tokenType.equals(token.getValue())) {
			System.out.println(token.getDesc() + " -> " + token.getValue());
			grammarLog.info(token.getDesc() + " -> " + token.getValue());
			getNextToken();
			return true;
		} else {
			return false;
		}
	}

	// check the first set
	private boolean checkFirstSet(String grmrSymbol) {
		String firstValue = firstAndFollw.matchFirst(grmrSymbol, token);
		if (firstValue == "") {
			if (grmrSymbol.equals(token.getValue()))
				return true;
			return false;
		} else if (firstValue != null)
			return false;
		return true;
	}

	private boolean checkFollowSet(String grmrSymbol) {
		String followValue = firstAndFollw.matchFollow(grmrSymbol, token);
		if (followValue == null)
			return true;
		return false;
	}

	// implemented according to class and from slides
	// match(token){
	// if (lookahead == token)
	// lookahead = nextToken(); return(true)
	// else
	// lookahead = nextToken(); return(false)
	// }

	// type -> float | id | int
	private boolean matchType() {
		if (token.getDesc().equals("T_RESERVE_WORD_INT")) {
			System.out.println("type            -> int");
			grammarLog.info("type            -> int");
			getNextToken();
			return true;
		} else if (token.getDesc().equals("T_RESERVE_WORD_FLOAT")) {
			System.out.println("type            -> float");
			grammarLog.info("type            -> float");
			getNextToken();
			return true;
		} else if (token.getDesc().equals("T_IDENTIFIER")) {
			System.out.println("type            -> id" + "\t->\t"
					+ token.getValue());
			grammarLog.info("type            -> id" + "\t->\t"
					+ token.getValue());
			getNextToken();
			return true;
		} else {
			System.out.println("ERROR: IN LINE NUMBER:\t" + token.getPosition()
					+ ":\tExpected One of these token Type:\t"
					+ "float | id | int");
			parserLog.warning("ERROR: IN LINE NUMBER:\t"
					+ token.getPosition()
					+ ":\tExpected One of these token Type:\t"
					+ "float | id | int");
			while (!token.getValue().equals("$")) {
				if (token.getDesc().equals("T_RESERVE_WORD_INT")) {
					System.out.println("type            -> int");
					grammarLog.info("type            -> int");
					System.out.println("RESUME PARSING FROM TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					parserLog.info("RESUME PARSING FROM  TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
					return true;
				} else if (token.getDesc().equals("T_RESERVE_WORD_FLOAT")) {
					System.out.println("type            -> float");
					grammarLog.info("type            -> float");
					System.out.println("RESUME PARSING FROM TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					parserLog.info("RESUME PARSING FROM  TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
					return true;
				} else if (token.getDesc().equals("T_IDENTIFIER")) {
					System.out.println("type            -> id" + "\t->\t"
							+ token.getValue());
					grammarLog.info("type            -> id" + "\t->\t"
							+ token.getValue());
					System.out.println("RESUME PARSING FROM TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					parserLog.info("RESUME PARSING FROM  TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
					return true;
				} else {
					System.out.println("SKIPPING TOKEN:\t" + token.getValue()
							+ "\tAT LINE NUMBER:\t" + token.getPosition());
					parserLog.warning("SKIPPING TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
				}
			}
		}
		if (token.getValue().equals("$")) {
			System.out.println("REACHED END OF FILE");
			grammarLog.warning("REACHED END OF FILE");
			parserLog.warning("REACHED END OF FILE");
		}
		return false;
	}

	// num -> float | int
	private boolean matchNum() {
		if (token.getDesc().equals("T_INTEGER")) {
			System.out.println("num            -> T_INTEGER");
			grammarLog.info("num            -> T_INTEGER");
			getNextToken();
			return true;
		} else if (token.getDesc().equals("T_FLOAT")) {
			System.out.println("num            -> T_FLOAT");
			grammarLog.info("num            -> T_FLOAT");
			getNextToken();
			return true;
		} else {
			System.out.println("ERROR: IN LINE NUMBER:\t" + token.getPosition()
					+ ":\tExpected One of these token Type:\t" + "float | int");
			parserLog.warning("ERROR: IN LINE NUMBER:\t"
					+ token.getPosition()
					+ ":\tExpected One of these token Type:\t" + "float | int");
			while (!token.getValue().equals("$")) {
				if (token.getDesc().equals("T_INTEGER")) {
					System.out.println("num            -> T_INTEGER");
					grammarLog.info("num            -> T_INTEGER");
					System.out.println("RESUME PARSING FROM TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					parserLog.info("RESUME PARSING FROM  TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
					return true;
				} else if (token.getDesc().equals("T_FLOAT")) {
					System.out.println("num            -> T_FLOAT");
					grammarLog.info("num            -> T_FLOAT");
					System.out.println("RESUME PARSING FROM TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					parserLog.info("RESUME PARSING FROM  TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
					return true;
				} else {
					System.out.println("SKIPPING TOKEN:\t" + token.getValue()
							+ "\tAT LINE NUMBER:\t" + token.getPosition());
					parserLog.warning("SKIPPING TOKEN:\t"
							+ token.getValue() + "\tAT LINE NUMBER:\t"
							+ token.getPosition());
					getNextToken();
				}
			}
		}
		if (token.getValue().equals("$")) {
			System.out.println("REACHED END OF FILE");
			grammarLog.warning("REACHED END OF FILE");
			parserLog.warning("REACHED END OF FILE");
		}
		return false;
	}

	// sign -> +
	// sign -> -
	private boolean matchSign() {
		if (token.getDesc().equals("+")) {
			System.out.println("sign -> +");
			grammarLog.info("sign -> +");
			return true;
		} else if (token.getDesc().equals("-")) {
			System.out.println("sign -> -");
			grammarLog.info("sign -> -");
			return true;
		}
		return false;
	}

	private boolean skipErrors(String firstG, String followG) {
		if (checkFirstSet(firstG) || checkFollowSet(followG))
			return true;

		String firstSet = firstAndFollw.getFirstSet(firstG);
		String followSet = firstAndFollw.getFollowSet(followG);

		String ffSet = "";
		if (firstSet != null) {
			ffSet += firstSet;
		}
		if (followSet != null) {
			ffSet += " " + followSet;
		}
		String ffArraySet[] = ffSet.split(" ");
		System.out.println("ERROR: IN LINE NUMBER:\t" + token.getPosition()
				+ ":\tExpected One of these tokens:\t" + ffSet);
		parserLog.warning("ERROR: IN LINE NUMBER:\t" + token.getPosition()
				+ ":\tExpected One of these tokens:\t" + ffSet);
		while (!token.getValue().equals("$")) {
			if (checkFirstSet(firstG) || checkFollowSet(followG)) {
				System.out.println("RESUME PARSING FROM TOKEN:\t"
						+ token.getValue() + "\tAT LINE NUMBER:\t"
						+ token.getPosition());
				parserLog.info("RESUME PARSING FROM  TOKEN:\t"
						+ token.getValue() + "\tAT LINE NUMBER:\t"
						+ token.getPosition());
				return true;
			} else {
				System.out.println("SKIPPING TOKEN:\t" + token.getValue()
						+ "\tAT LINE NUMBER:\t" + token.getPosition());
				parserLog.warning("SKIPPING TOKEN:\t" + token.getValue()
						+ "\tAT LINE NUMBER:\t" + token.getPosition());
				getNextToken();
			}
		}
		if (token.getValue().equals("$")) {
			System.out.println("REACHED END OF FILE");
			grammarLog.warning("REACHED END OF FILE");
			parserLog.warning("REACHED END OF FILE");
		}

		return false;
	}

	private void getNextToken() {
		try {
			token = lexer.getNextToken();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
