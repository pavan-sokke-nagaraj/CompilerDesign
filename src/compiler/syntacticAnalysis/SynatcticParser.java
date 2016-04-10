package compiler.syntacticAnalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.semantics.SemanticAnalysis;
import compiler.semantics.Symbol;
import compiler.semantics.Symbol.SYMBOLTYPE;
import compiler.semantics.SymbolTable;
import compiler.utils.PrintUtil;
import compiler.utils.PrintUtil.LOGTYPE;

public class SynatcticParser {

	Lexer lexer = new Lexer();
	// Syntax Analysis
	FirstAndFollw firstAndFollw = null;
	Token token = null;
	Token prevToken = new Token();
	// Semantics
	private Symbol symbol = null;
	public SemanticAnalysis semantics = new SemanticAnalysis();
	// logging
	private Logger grammarLog;
	private Logger parserLog;
	private Logger derivationLog;
	private StringBuilder derivationTree = new StringBuilder("prog");

	public void printGrammar(String grammarLHS, String grammarRHS) {
		derivationTree = new StringBuilder(derivationTree.toString()
				.replaceFirst(grammarLHS, grammarRHS));
		PrintUtil.print(derivationLog, LOGTYPE.SYNTAX,
				derivationTree.toString());
	}

	public SynatcticParser(Lexer lexer) {
		this.lexer = lexer;
		firstAndFollw = new FirstAndFollw();
		grammarLog = PrintUtil.setLogger("GRAMMAR.log");
		parserLog = PrintUtil.setLogger("PARSER.log");
		derivationLog = PrintUtil.setLogger("DERIVATION.log");
	}

	public boolean parse(SymbolTable firstTable) {
		semantics.firstTable = (SymbolTable) firstTable.clone();
		return parse();
	}

	/**
	 * parse(){ lookahead = nextToken() if (startSymbol() ^ match(“$”) )
	 * return(true) else return(false) }
	 */
	public boolean parse() {
		boolean val = false;
		getNextToken();
		if (token == null) {
			String msg = "Error in Input File";
			PrintUtil.print(grammarLog, LOGTYPE.SYNTAX, msg);
			return false;
		}
		return startSymbol();
	}

	private boolean startSymbol() {
		printGrammar("prog", "prog");
		semantics.moonData.add("%DATA");
		semantics.moonCode.add("%CODE");
		return prog();
	}

	// prog -> classDeclList progBody lol
	private boolean prog() {
		printGrammar("prog", "classDeclList progBody");
		semantics.progDecl();
		if (classDeclList() && progBody()) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"prog -> classDeclList progBody");
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
			printGrammar("classDeclList", "ClassDecl classDeclList");
			if (classDecl() && classDeclList()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"classDeclList    -> classDecl classDeclList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("classDeclList")) {
			printGrammar("classDeclList", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"classDeclList    -> EPSILON");
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
			printGrammar("ClassDecl", "class id { varDecFunDef } ;");
			if (matchTokenType("T_RESERVE_WORD_CLASS", SYMBOLTYPE.UNKNOWN)
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.UNKNOWN)
					&& matchTokenType("T_DEL_C_LPAREN", SYMBOLTYPE.CLASS)) {
				Symbol classSymb = symbol;
				if (varDecFunDef() && matchTokenType("T_DEL_C_RPAREN")
						&& matchTokenType("T_DEL_SEMICOLON")) {
					PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
							"classDecl        -> class id { varDecFunDef } ;");
					semantics.setClassMemory(classSymb);
					semantics.QuitPresentTable();
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
			printGrammar("varDecFunDef", "type id var_DecFunDef1");
			if (matchType()
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.UNKNOWN)
					&& varDecFunDef1()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"varDecFunDef -> type id varDecFunDef1");
				return true;
			} else {
				return false;
			}
		} else if (checkFollowSet("varDecFunDef")) {
			printGrammar("varDecFunDef", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"varDecFunDef    -> EPSILON");
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
			printGrammar("var_DecFunDef1", "( fParams ) funcBody ; funcDefList");
			if (matchTokenType("T_DEL_R_LPAREN", SYMBOLTYPE.FUNCTION)
					&& fParams() && matchTokenType("T_DEL_R_RPAREN")) {
				// A -4 Semantic rules - function overloading checking
				// System.out.println(symbol.getToken().getValue());
				semantics.isFunctionReDefined(symbol);
				if (funcBody()) {
					semantics.genFuncEndCode();
					if (matchTokenType("T_DEL_SEMICOLON", SYMBOLTYPE.QUITTABLE)
							&& funcDefList()) {
						PrintUtil
								.info(grammarLog, LOGTYPE.SYNTAX,
										"varDecFunDef1 -> ( fParams ) funcBody ; funcDefList");
						return true;
					}
				}
			}
		} else if (checkFirstSet("varDecFunDef1")) {
			printGrammar("var_DecFunDef1", "arraySizeList ; varDecFunDef ");
			if (arraySizeList() && matchTokenType("T_DEL_SEMICOLON")
					&& varDecFunDef()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"varDecFunDef1 -> arraySizeList ; varDecFunDef");
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
			semantics.moonCode.add("");
			semantics.moonCode.add("\t\t" + "entry\t\t%PROGRAM EXECUTION");
			semantics.moonCode.add("\t\t" + "add" + "\t" + "r14,\t" + "r0,\t"
					+ "r0" + "\t% SET STACK POINTER\n");
			printGrammar("progBody", "program funcBody ; funcDefList");
			if (matchTokenValue("program") && funcBody()
					&& matchTokenType("T_DEL_SEMICOLON", SYMBOLTYPE.QUITTABLE)) {
				semantics.moonCode.add("\t\t" + "hlt");
				if (funcDefList()) {
					PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
							"progBody -> program funcBody ; funcDefList");
					return true;
				}
			}
		}
		return false;
	}

	// funcDefList -> funcDef funcDefList
	// funcDefList -> EPSILON
	private boolean funcDefList() {
		if (checkFirstSet("funcDef")) {
			printGrammar("funcDefList", "func_Def funcDefList");
			if (funcDef() && funcDefList()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"funcDefList -> funcDef funcDefList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("funcDefList")) {
			printGrammar("funcDefList", "");
			PrintUtil
					.info(grammarLog, LOGTYPE.SYNTAX, "funcDefList -> EPSILON");
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
			printGrammar("func_Def", "funcHead funcBody ;");
			if (funcHead() && funcBody()
					&& matchTokenType("T_DEL_SEMICOLON", SYMBOLTYPE.QUITTABLE)) {
				semantics.genFuncEndCode();
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"funcDef -> funcHead funcBody ;");
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
			printGrammar("funcHead", "type id ( fParams )");
			if (matchType()
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.UNKNOWN)
					&& matchTokenType("T_DEL_R_LPAREN", SYMBOLTYPE.FUNCTION)
					&& fParams() && matchTokenType("T_DEL_R_RPAREN")) {
				// A -4 Semantic rules - function overloading checking
				// System.out.println(symbol.getToken().getValue());
				semantics.isFunctionReDefined(symbol);
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"funcHead -> type id ( fParams )");
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
			printGrammar("fParams", "type id arraySizeList f_Params_TailList");
			if (matchType()
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.PARAMETER)
					&& arraySizeList() && fParamsTailList()) {
				// A -4 Semantic rules - function overloading checking
				// System.out.println(symbol.getToken().getValue());
				// semantics.isFunctionReDefined(symbol);
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"fParams -> type id arraySizeList fParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("fParams")) {
			printGrammar("fParams", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "fParams -> EPSILON");
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
			printGrammar("f_Params_TailList", "f_Param_Tail f_Params_TailList");
			if (fParamsTail() && fParamsTailList()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"fParamsTailList -> fParamsTail fParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("fParamsTailList")) {
			printGrammar("f_Params_TailList", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"fParamsTailList -> EPSILON");
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
			printGrammar("f_Param_Tail", ", type id arraySizeList");
			if (matchTokenType("T_DEL_COMMA") && matchType()
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.PARAMETER)
					&& arraySizeList()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"fParamsTail -> , type id arraySizeList");
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
			printGrammar("funcBody", "{ bodyCode }");
			if (matchTokenType("T_DEL_C_LPAREN") && bodyCode()
					&& matchTokenType("T_DEL_C_RPAREN")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"funcBody -> { bodyCode }");
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
			printGrammar("bodyCode", "float id arraySizeList ; bodyCode");
			if (matchType()
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.UNKNOWN)
					&& arraySizeList() && matchTokenType("T_DEL_SEMICOLON")
					&& bodyCode()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"bodyCode -> float id arraySizeList ; bodyCode");
				return true;
			} else
				return false;
		} else if (checkFirstSet("int")) {
			printGrammar("bodyCode", "int id arraySizeList ; bodyCode");
			if (matchType()
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.UNKNOWN)
					&& arraySizeList() && matchTokenType("T_DEL_SEMICOLON")
					&& bodyCode()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"bodyCode -> int id arraySizeList ; bodyCode");
				return true;
			} else
				return false;
		} else if (checkFirstSet("id")) {
			printGrammar("bodyCode", "id body_Code2");
			if (matchTokenType("T_IDENTIFIER", SYMBOLTYPE.UNKNOWN)
					&& bodyCode2()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"bodyCode -> id bodyCode2");
				return true;
			} else
				return false;
		} else if (checkFirstSet("ctrlStat")) {
			printGrammar("bodyCode", "ctrlStat ; S_LIST");
			if (ctrlStat() && matchTokenType("T_DEL_SEMICOLON")
					&& statementList()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"bodyCode -> ctrlStat ; statementList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("bodyCode")) {
			printGrammar("bodyCode", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "bodyCode -> EPSILON");
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
			printGrammar("body_Code2", "id arraySizeList ; bodyCode");
			if (matchTokenType("T_IDENTIFIER", SYMBOLTYPE.UNKNOWN)
					&& arraySizeList() && matchTokenType("T_DEL_SEMICOLON")
					&& bodyCode()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"bodyCode2 -> id arraySizeList ; bodyCode");
				return true;
			} else
				return false;
		} else if (checkFirstSet("bodyCode2")) {
			semantics.isVarDeclared(symbol);
			semantics.resetOffset();
			Symbol expr = new Symbol();
			printGrammar("body_Code2", "i_ndiceList dotList = expr ; S_LIST");
			if (indiceList()) {
				if (dotIdList() && matchTokenValue("=")) {
					Symbol prevSymb = new Symbol();
					copySymbol(prevSymb, symbol);
					semantics.pushOffset(symbol);
					if (expr(expr)) {
						semantics.checkDataTypes(prevSymb, expr);
						if (matchTokenType("T_DEL_SEMICOLON")
								&& statementList()) {
							PrintUtil
									.info(grammarLog, LOGTYPE.SYNTAX,
											"bodyCode2 -> indiceList dotIdList assignOp expr ; statementList");
							return true;
						}
					}
				}
			}
			// else
			// return false;
		} else if (checkFollowSet("bodyCode2")) {
			printGrammar("bodyCode2", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "bodyCode2 -> EPSILON");
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
			printGrammar("S_LIST", "statement S_LIST");
			if (statement() && statementList()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"statementList -> statement statementList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("statementList")) {
			printGrammar("S_LIST", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"statementList -> EPSILON");
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
			printGrammar("statement", "ctrlStat ;");
			if (ctrlStat() && matchTokenType("T_DEL_SEMICOLON")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"statement -> ctrlStat ;");
				return true;
			} else
				return false;
		} else if (checkFirstSet("assignStat")) {
			printGrammar("statement", "assignStat ;");
			if (assignStat() && matchTokenType("T_DEL_SEMICOLON")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"statement -> assignStat ;");
				return true;
			} else
				return false;
		}
		return false;
	}

	// statBlock -> { statementList }
	// statBlock -> statement
	// statBlock -> EPSILON
	private boolean statBlock() {
		if (!skipErrors("statBlock", "statBlock")) {
			return false;
		}
		if (checkFirstSet("{")) {
			printGrammar("statBlock", "{ S_LIST }");
			if (matchTokenType("T_DEL_C_LPAREN") && statementList()
					&& matchTokenType("T_DEL_C_RPAREN")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"statBlock -> { statementList } ");
				return true;
			} else
				return false;
		} else if (checkFirstSet("statBlock")) {
			printGrammar("statBlock", "statement");
			if (statement()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"statBlock -> statement ");
				return true;
			} else
				return false;
		} else if (checkFollowSet("statBlock")) {
			printGrammar("statBlock", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "statBlock -> EPSILON");
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
		Symbol expr = new Symbol();
		if (!skipErrors("ctrlStat", "")) {
			return false;
		}
		if (checkFirstSet("if")) {
			printGrammar("ctrlStat",
					"if ( expr ) then statBlock else statBlock");
			if (matchTokenType("T_RESERVE_WORD_IF")
					&& matchTokenType("T_DEL_R_LPAREN") && expr(expr)
					&& matchTokenType("T_DEL_R_RPAREN")
					&& matchTokenType("T_RESERVE_WORD_THEN")) {
				String elseAddr = semantics.genCodeIf(expr);
				if (statBlock() && matchTokenType("T_RESERVE_WORD_ELSE")) {
					String elseEndAddr = semantics.genCodeElse(expr, elseAddr);
					if (statBlock()) {
						PrintUtil
								.info(grammarLog, LOGTYPE.SYNTAX,
										"ctrlStat -> if ( expr ) then statBlock else statBlock");
						semantics.genCodeEndThen(expr, elseEndAddr);
						return true;
					} else
						return false;
				}
			}

		} else if (checkFirstSet("for")) {
			printGrammar("ctrlStat",
					"for ( type id = expr ; relExpr ; assignStat ) statBlock");
			if (matchTokenType("T_RESERVE_WORD_FOR")
					&& matchTokenType("T_DEL_R_LPAREN") && matchType()
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.FORLOOPVAR)
					&& matchTokenType("T_OP_ASSIGN_EQUAL")) {
				Symbol prevSymb = new Symbol();
				copySymbol(prevSymb, symbol);
				if (expr(expr)) {
					semantics.checkDataTypes(prevSymb, expr);
					if (matchTokenType("T_DEL_SEMICOLON")) {
						String forAddr = "FOR_" + semantics.addrCount++;
						semantics.moonCode.add(forAddr + "  %  FOR LOOP BEGIN");
						if (relExpr() && matchTokenType("T_DEL_SEMICOLON")) {
							if (assignStat()
									&& matchTokenType("T_DEL_R_RPAREN")
									&& statBlock()) {
								PrintUtil
										.info(grammarLog, LOGTYPE.SYNTAX,
												"ctrlStat -> for ( type id assignOp expr ; relExpr ; assignStat ) statBlock");
								return true;
							} else
								return false;
						}
					}
				}
			}

		} else if (checkFirstSet("get")) {
			printGrammar("ctrlStat", "get ( variable )");
			if (matchTokenType("T_RESERVE_WORD_GET")
					&& matchTokenType("T_DEL_R_LPAREN") && variable()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"ctrlStat -> get ( variable )");
				semantics.genGetCode(symbol);
				return true;
			} else
				return false;
		} else if (checkFirstSet("put")) {
			printGrammar("ctrlStat", "put ( expr )");
			if (matchTokenType("T_RESERVE_WORD_PUT")
					&& matchTokenType("T_DEL_R_LPAREN") && expr(expr)
					&& matchTokenType("T_DEL_R_RPAREN")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"ctrlStat -> put ( expr )");
				semantics.genPutCode(expr);
				return true;
			} else
				return false;
		} else if (checkFirstSet("return")) {
			printGrammar("ctrlStat", "return ( expr )");
			if (matchTokenValue("return") && matchTokenType("T_DEL_R_LPAREN")
					&& expr(expr) && matchTokenType("T_DEL_R_RPAREN")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"ctrlStat -> return ( expr )");
				return true;
			} else
				return false;
		}
		return false;
	}

	// assignStat -> variable assignOp expr
	private boolean assignStat() {
		Symbol expr = new Symbol();
		if (!skipErrors("assignStat", "")) {
			return false;
		}
		if (checkFirstSet("variable")) {
			printGrammar("assignStat", "variable = expr");
			if (variable()) {
				System.out.println(symbol.toString());
				Symbol variable = new Symbol();
				copySymbol(variable, symbol);
				semantics.pushOffset(variable);
				if (matchTokenType("T_OP_ASSIGN_EQUAL")) {
					if (expr(expr)) {
						PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
								"assignStat -> variable assignOp expr");
						semantics.checkDataTypes(variable, expr);
						return true;
					}
				} else
					return false;
			}
		}
		return false;
	}

	// expr -> arithExpr subExpr
	// private boolean expr() {
	// if (!skipErrors("arithExpr", "")) {
	// return false;
	// }
	// if (checkFirstSet("arithExpr")) {
	// printGrammar("expr", "arithExpr subExpr");
	// if (arithExpr() && subExpr()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "expr -> arithExpr subExpr");
	// return true;
	// } else
	// return false;
	// }
	// return false;
	// }

	private boolean expr(Symbol expr) {
		if (!skipErrors("arithExpr", "")) {
			return false;
		}
		if (checkFirstSet("arithExpr")) {
			printGrammar("expr", "arithExpr subExpr");
			Symbol arithExpr = new Symbol();
			Symbol subExpr = new Symbol();
			if (arithExpr(arithExpr) && subExpr(arithExpr, subExpr)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"expr -> arithExpr subExpr");
				copySymbol(expr, subExpr);
				return true;
			} else
				return false;
		}
		return false;
	}

	// arithExpr -> term arithExprRight
	// private boolean arithExpr() {
	// if (!skipErrors("term", "")) {
	// return false;
	// }
	// if (checkFirstSet("term")) {
	// printGrammar("arithExpr", "term arithERR");
	// if (term() && arithExprRight()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "arithExpr -> term arithExprRight");
	// return true;
	// } else
	// return false;
	// }
	// return false;
	// }

	// arithExpr -> term arithExprRight
	private boolean arithExpr(Symbol arithExpr) {
		if (!skipErrors("term", "")) {
			return false;
		}
		if (checkFirstSet("term")) {
			Symbol term = new Symbol();
			Symbol arithExprRight = new Symbol();
			printGrammar("arithExpr", "term arithERR");
			if (term(term) && arithExprRight(term, arithExprRight)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"arithExpr -> term arithExprRight");
				copySymbol(arithExpr, arithExprRight);
				return true;
			} else
				return false;
		}
		return false;
	}

	// subExpr -> relOp arithExpr
	// subExpr -> EPSILON
	// relOp -> < | <= | <> | == | > | >=
	private boolean subExpr(Symbol arithExpr, Symbol subExpr) {
		if (!skipErrors("relOp", "subExpr")) {
			return false;
		}
		if (checkFirstSet("relOp")) {
			printGrammar("subExpr", "relOp arithExpr");
			Symbol arithExpr2 = new Symbol();
			Token operToken = copyToken(token);
			semantics.pushOffset(arithExpr);
			if (matchTokenType("relOp") && arithExpr(arithExpr2)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"subExpr -> relOp arithExpr");
				Symbol result = semantics.addOpComp(arithExpr, operToken,
						arithExpr2);
				copySymbol(subExpr, result);
				return true;
			} else
				return false;
		} else if (checkFollowSet("subExpr")) {
			printGrammar("subExpr", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "subExpr -> EPSILON");
			copySymbol(subExpr, arithExpr);
			return true;
		}
		return false;
	}

	// arithExprRight -> addOp term arithExprRight
	// arithExprRight -> EPSILON
	// private boolean arithExprRight() {
	// if (!skipErrors("addOp", "arithExprRight")) {
	// return false;
	// }
	// if (checkFirstSet("addOp")) {
	// printGrammar("arithERR", "addOp term arithERR");
	// if (matchTokenType("addOp") && term() && arithExprRight()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "arithExprRight -> addOp term arithExprRight");
	// return true;
	// } else
	// return false;
	// } else if (checkFollowSet("arithExprRight")) {
	// printGrammar("arithERR", "");
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "arithExprRight -> EPSILON");
	// return true;
	// }
	// return false;
	// }

	private boolean arithExprRight(Symbol term, Symbol arithExprRight) {
		if (!skipErrors("addOp", "arithExprRight")) {
			return false;
		}
		if (checkFirstSet("addOp")) {
			printGrammar("arithERR", "addOp term arithERR");
			Symbol arithExprRight2 = new Symbol();
			Symbol term2 = new Symbol();
			Token operToken = copyToken(token);
			semantics.pushOffset(term);
			if (matchTokenType("addOp") && term(term2)
					&& arithExprRight(term2, arithExprRight2)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"arithExprRight -> addOp term arithExprRight");
				// Symbol compSymb = new Symbol();
				// copySymbol(compSymb, arithExprRight2);
				// compare addition data types
				Symbol result = semantics.addOpComp(term, operToken,
						arithExprRight2);
				copySymbol(arithExprRight, result);
				return true;
			} else
				return false;
		} else if (checkFollowSet("arithExprRight")) {
			printGrammar("arithERR", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"arithExprRight -> EPSILON");
			copySymbol(arithExprRight, term);
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
			Symbol arithExprL = new Symbol();
			Symbol arithExprR = new Symbol();
			printGrammar("relExpr", "arithExpr relOp arithExpr");
			if (arithExpr(arithExprL)) {
				Token operToken = copyToken(token);
				if (matchTokenType("relOp") && arithExpr(arithExprR)) {
					PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
							"relExpr -> arithExpr relOp arithExpr");
					Symbol result = semantics.addOpComp(arithExprL, operToken,
							arithExprR);
					copySymbol(result, arithExprR);
					return true;
				}
			} else
				return false;
		}
		return false;
	}

	// term -> factor termFactor
	// private boolean term() {
	// if (!skipErrors("factor", "")) {
	// return false;
	// }
	// if (checkFirstSet("factor")) {
	// printGrammar("term", "factor terFac");
	// if (factor() && termFactor()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "term -> factor termFactor");
	// return true;
	// } else
	// return false;
	// }
	// return false;
	// }

	private boolean term(Symbol term) {
		if (!skipErrors("factor", "")) {
			return false;
		}
		if (checkFirstSet("factor")) {
			Symbol factor = new Symbol();
			Symbol termFactor = new Symbol();
			printGrammar("term", "factor terFac");
			if (factor(factor) && termFactor(factor, termFactor)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"term -> factor termFactor");
				copySymbol(term, termFactor);
				return true;
			} else
				return false;
		}
		return false;
	}

	// termFactor -> multOp factor termFactor
	// termFactor -> EPSILON
	// private boolean termFactor() {
	// if (!skipErrors("multOp", "termFactor")) {
	// return false;
	// }
	// if (checkFirstSet("multOp")) {
	// printGrammar("terFac", "multOp factor terFac");
	// if (matchTokenType("multOp") && factor() && termFactor()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "termFactor -> multOp factor termFactor");
	// return true;
	// } else
	// return false;
	// } else if (checkFollowSet("termFactor")) {
	// printGrammar("terFac", "");
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "termFactor -> EPSILON");
	// return true;
	// }
	// return false;
	// }

	private boolean termFactor(Symbol factor, Symbol termFactor) {
		if (!skipErrors("multOp", "termFactor")) {
			return false;
		}
		if (checkFirstSet("multOp")) {
			Symbol factor2 = new Symbol();
			Symbol termFactor2 = new Symbol();
			Token operToken = copyToken(token);
			semantics.pushOffset(factor);
			printGrammar("terFac", "multOp factor terFac");
			if (matchTokenType("multOp") && factor(factor2)
					&& termFactor(factor2, termFactor2)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"termFactor -> multOp factor termFactor");
				Symbol result = semantics.addOpComp(factor, operToken,
						termFactor2);
				copySymbol(termFactor, result);
				return true;
			} else
				return false;
		} else if (checkFollowSet("termFactor")) {
			printGrammar("terFac", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "termFactor -> EPSILON");
			copySymbol(termFactor, factor);
			return true;
		}
		return false;
	}

	// factor -> ( arithExpr )
	// factor -> not factor
	// factor -> num
	// factor -> sign factor
	// factor -> id indiceOrParam
	// private boolean factor() {
	// if (!skipErrors("factor", "")) {
	// return false;
	// }
	// if (checkFirstSet("(")) {
	// printGrammar("factor", "( arithExpr )");
	// if (matchTokenType("T_DEL_R_LPAREN") && arithExpr()
	// && matchTokenType("T_DEL_R_RPAREN")) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "factor -> ( arithExpr )");
	// return true;
	// } else
	// return false;
	// } else if (checkFirstSet("not")) {
	// printGrammar("factor", "not factor");
	// if (matchTokenType("T_LOGICAL_NOT") && factor()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "factor -> not factor");
	// return true;
	// } else
	// return false;
	// } else if (checkFirstSet("num")) {
	// printGrammar("factor", "num");
	// if (matchNum()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "factor -> num");
	// return true;
	// } else
	// return false;
	// } else if (checkFirstSet("sign")) {
	// printGrammar("factor", "sign factor");
	// if (matchSign() && factor()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "factor -> sign factor");
	// return true;
	// } else
	// return false;
	// } else if (checkFirstSet("id")) {
	// printGrammar("factor", "id IorP");
	// if (matchTokenType("T_IDENTIFIER", SYMBOLTYPE.ISVARDECLARED)
	// && indiceOrParam()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "factor -> id indiceOrParam");
	// return true;
	// } else
	// return false;
	// }
	// return false;
	// }

	private boolean factor(Symbol factor) {
		if (!skipErrors("factor", "")) {
			return false;
		}
		if (checkFirstSet("(")) {
			Symbol arithExpr = new Symbol();
			printGrammar("factor", "( arithExpr )");
			if (matchTokenType("T_DEL_R_LPAREN") && arithExpr(arithExpr)
					&& matchTokenType("T_DEL_R_RPAREN")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"factor -> ( arithExpr )");
				copySymbol(factor, arithExpr);
				return true;
			} else
				return false;
		} else if (checkFirstSet("not")) {
			printGrammar("factor", "not factor");
			if (matchTokenType("T_LOGICAL_NOT") && factor(factor)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"factor -> not factor");
				semantics.genNotCode(factor);
				return true;
			} else
				return false;
		} else if (checkFirstSet("num")) {
			printGrammar("factor", "num");
			if (matchNum()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "factor -> num");
				// A - 4 set the num type according to the input
				factor.setDataType(copyToken(prevToken));
				factor.setToken(copyToken(prevToken));
				factor.symbolType = SYMBOLTYPE.NUM;
				if (prevToken.getDesc().equals("T_INTEGER")) {
					factor.getDataType().setValue("int");
				} else if (prevToken.getDesc().equals("T_FLOAT")) {
					factor.getDataType().setValue("float");
				}
				return true;
			} else
				return false;
		} else if (checkFirstSet("sign")) {
			printGrammar("factor", "sign factor");
			Token sign = copyToken(token);
			if (matchSign() && factor(factor)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"factor -> sign factor");
				if (sign.getValue().equals("-")) {
					semantics.genSignCode(factor);
				}
				return true;
			} else
				return false;
		} else if (checkFirstSet("id")) {
			printGrammar("factor", "id IorP");
			Symbol indiceOrParam = new Symbol();
			if (matchTokenType("T_IDENTIFIER", SYMBOLTYPE.ISVARDECLARED)
					&& indiceOrParam(symbol, indiceOrParam)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"factor -> id indiceOrParam");
				copySymbol(factor, indiceOrParam);
				return true;
			} else
				return false;
		}
		return false;
	}

	// indiceOrParam -> indiceList idFactor
	// indiceOrParam -> ( aParams )
	// private boolean indiceOrParam() {
	// if (!skipErrors("indiceOrParam", "indiceOrParam")) {
	// return false;
	// }
	// if (checkFirstSet("(")) {
	// printGrammar("IorP", "( aParams )");
	// if (matchTokenType("T_DEL_R_LPAREN") && aParams()
	// && matchTokenType("T_DEL_R_RPAREN")) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "indiceOrParam -> ( aParams )");
	// return true;
	// } else
	// return false;
	// } else if (checkFirstSet("indiceOrParam")) {
	// printGrammar("IorP", "i_ndiceList I_F");
	// // A - 4
	// // To check array indices validation...!!!!!
	// if (symbol.isArray()) {
	// arrayVar = new Symbol();
	// System.out.println("---------->>>  ARRAY");
	// Token arrayToken = copyToken(symbol.getToken());
	// copySymbol(arrayVar, symbol);
	// arrayVar.setToken(arrayToken);
	// }
	// if (indiceList()) {
	// // A - 4
	// // To check array indices validation...!!!!!
	// if (arrayVar != null && arrayVar.isArray()) {
	// if (arrayVar.getArrLength() != 0) {
	// String msg = "ARRAY DIMENSION MISSING: NO OF MISSING DIMENSIONS:\t"
	// + arrayVar.getArrLength()
	// + "\t FOR VARIABLE:\t"
	// + arrayVar.getToken().getValue()
	// + "\tAT LINE NUMBER:\t"
	// + arrayVar.getToken().getPosition();
	// PrintUtil.error(semantics.semanticLog,
	// LOGTYPE.SEMATICS, msg);
	// }
	// arrayVar = null;
	// System.out.println("<<<----------  ARRAY");
	// }
	// if (idFactor()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "indiceOrParam -> indiceList idFactor");
	// return true;
	// } else
	// return false;
	// }
	// } else if (checkFollowSet("indiceOrParam")) {
	// printGrammar("IorP", "");
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "indiceOrParam -> EPSILON");
	// return true;
	// }
	// return false;
	// }

	private boolean indiceOrParam(Symbol iSymbol, Symbol indiceOrParam) {
		if (!skipErrors("indiceOrParam", "indiceOrParam")) {
			return false;
		}
		if (checkFirstSet("(")) {
			printGrammar("IorP", "( aParams )");
			ArrayList<Symbol> prevParams = semantics.aParams;
			Symbol prevSymb = new Symbol();
			copySymbol(prevSymb, symbol);
			if (matchTokenType("T_DEL_R_LPAREN") && aParams()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"indiceOrParam -> ( aParams )");
				// A - 4 to check aParams
				Symbol classSymb = new Symbol();
				copySymbol(classSymb, prevSymb);
				semantics.checkParams(prevSymb);
				// A - 4 to check aParams
				semantics.aParams = prevParams;
				copySymbol(indiceOrParam, iSymbol);
				return true;
			} else
				return false;
		} else if (checkFirstSet("indiceOrParam")) {
			printGrammar("IorP", "i_ndiceList I_F");
			if (indiceList()) {
				Symbol idFactor = new Symbol();
				if (idFactor(iSymbol, idFactor)) {
					PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
							"indiceOrParam -> indiceList idFactor");
					copySymbol(indiceOrParam, idFactor);
					return true;
				}
				// else
				// return false;
			} else
				return false;
		} else if (checkFollowSet("indiceOrParam")) {
			printGrammar("IorP", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"indiceOrParam -> EPSILON");
			if (iSymbol.isArray()) {
				semantics.checkArray(iSymbol);
			}
			copySymbol(indiceOrParam, iSymbol);
			return true;
		}
		return false;
	}

	// idFactor -> . id indiceOrParam
	// idFactor -> EPSILON
	// private boolean idFactor() {
	// if (!skipErrors(".", "idFactor")) {
	// return false;
	// }
	// if (checkFirstSet(".")) {
	// printGrammar("I_F", ". id IorP");
	// if (matchTokenType("T_DEL_DOT")
	// && matchTokenType("T_IDENTIFIER", SYMBOLTYPE.ISCLASSORFUNC)
	// && indiceOrParam()) {
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
	// "idFactor -> . id indiceOrParam");
	// return true;
	// } else
	// return false;
	// } else if (checkFollowSet("idFactor")) {
	// printGrammar("I_F", "");
	// PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "idFactor -> EPSILON");
	// return true;
	// }
	// return false;
	// }

	// iSymbol, idFactor
	private boolean idFactor(Symbol iSymbol, Symbol idFactor) {
		if (!skipErrors(".", "idFactor")) {
			return false;
		}
		if (checkFirstSet(".")) {
			printGrammar("I_F", ". id IorP");
			Symbol indiceOrParam = new Symbol();
			if (matchTokenType("T_DEL_DOT")
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.ISCLASSORFUNC)
					&& indiceOrParam(symbol, indiceOrParam)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"idFactor -> . id indiceOrParam");
				copySymbol(idFactor, indiceOrParam);
				return true;
			} else
				return false;
		} else if (checkFollowSet("idFactor")) {
			printGrammar("I_F", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "idFactor -> EPSILON");
			copySymbol(idFactor, iSymbol);
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
			printGrammar("i_ndiceList", "indice i_ndiceList");
			if (indice() && indiceList()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"indiceList -> indice indiceList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("indiceList")) {
			printGrammar("i_ndiceList", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "indiceList -> EPSILON");
			semantics.checkArray(symbol);
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
			printGrammar("dotList", ". id i_ndiceList");
			if (matchTokenType("T_DEL_DOT")
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.ISCLASSORFUNC)) {
				if (indiceList()) {
					PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
							"dotIdList -> . id indiceList");
					return true;
				}
			} else
				return false;
		} else if (checkFollowSet("dotIdList")) {
			printGrammar("dotList", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "dotIdList -> EPSILON");
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
			printGrammar("variable", "id i_ndiceList dotNest");
			if (matchTokenType("T_IDENTIFIER", SYMBOLTYPE.ISVARDECLARED)) {
				if (indiceList()) {
					if (dotIdNest()) {
						PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
								"variable -> id indiceList dotIdNest");
						return true;
					} else
						return false;
				}
			}
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
			printGrammar("dotNest", ". id i_ndiceList dotNest");
			if (matchTokenType("T_DEL_DOT")
					&& matchTokenType("T_IDENTIFIER", SYMBOLTYPE.ISCLASSORFUNC)) {
				if (indiceList()) {
					if (dotIdNest()) {
						PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
								"dotIdNest -> . id indiceList dotIdNest");
						return true;
					}
				}
			} else
				return false;
		} else if (checkFollowSet("dotIdNest")) {
			printGrammar("dotNest", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "dotIdNest -> EPSILON");
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
			// A -4 attribute migration
			Symbol prevSymb = new Symbol();
			copySymbol(prevSymb, symbol);
			Symbol arithExpr = new Symbol();
			semantics.pushReg("r11");
			// A - 4
			printGrammar("indice", "[ arithExpr ]");
			if (matchTokenType("T_DEL_S_LPAREN") && arithExpr(arithExpr)
					&& matchTokenType("T_DEL_S_RPAREN")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"indice -> [ arithExpr ]");
				semantics.checkIndex(arithExpr);
				copySymbol(symbol, prevSymb);
				semantics.popReg("r11");
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
			printGrammar("aParams", "expr aPTailList");
			// A - 4
			Symbol expr = new Symbol();
			semantics.aParams.add(expr);
			// A - 4
			if (expr(expr)) {
				if (aParamsTailList()) {
					PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
							"aParams -> expr aParamsTailList");
					return true;
				}
			} else
				return false;
		} else if (checkFollowSet("aParams")) {
			printGrammar("aParams", "[ arithExpr ]");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "aParams -> EPSILON");
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
			printGrammar("aPTailList", "A_P_T aPTailList");
			if (aParamsTail() && aParamsTailList()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"aParamsTailList -> aParamsTail aParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("aParamsTailList")) {
			printGrammar("aPTailList", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"aParamsTailList -> EPSILON");
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
			printGrammar("A_P_T", ", expr");
			Symbol expr = new Symbol();
			if (matchTokenType("T_DEL_COMMA") && expr(expr)) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"aParamsTail -> , expr");
				semantics.aParams.add(expr);
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
			printGrammar("arraySizeList", "array_Size arraySizeList");
			if (arraySize() && arraySizeList()) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"arraySizeList -> arraySize arraySizeList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("arraySizeList")) {
			printGrammar("arraySizeList", "");
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"arraySizeList ->  EPSILON");
			semantics.variableDecl(symbol);
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
			printGrammar("array_Size", " [ T_INTEGER ] ");
			if (matchTokenType("T_DEL_S_LPAREN")
					&& matchTokenType("T_INTEGER", SYMBOLTYPE.ARRAYSIZE)
					&& matchTokenType("T_DEL_S_RPAREN")) {
				PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
						"arraySizeList -> arraySize arraySizeList");
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
			printGrammar("relOp", token.getValue());
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"relOp" + " -> " + token.getValue());
			getNextToken();
			return true;
		} else if (tokenType.equals("addOp")
				&& (token.getValue().equals("+")
						|| token.getValue().equals("-") || token.getValue()
						.equals("or"))) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"addOp" + " -> " + token.getValue());
			printGrammar("addOp", token.getValue());
			getNextToken();
			return true;
		} else if (tokenType.equals("multOp")
				&& (token.getValue().equals("*")
						|| token.getValue().equals("/") || token.getValue()
						.equals("and"))) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "multOp" + " -> "
					+ token.getValue());
			printGrammar("multOp", token.getValue());
			getNextToken();
			return true;
		} else if (tokenType.equals(token.getDesc())) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, token.getDesc() + " -> "
					+ token.getValue());
			if (token.getDesc().equals("T_IDENTIFIER")) {
				printGrammar("id", token.getValue());
			} else if (token.getDesc().equals("T_INTEGER")) {
				printGrammar("T_INTEGER", token.getValue());
			} else if (token.getDesc().equals("T_FLOAT")) {
				printGrammar("T_FLOAT", token.getValue());
			}
			getNextToken();
			return true;
		} else {
			PrintUtil.error(parserLog, LOGTYPE.SYNTAX,
					"ERROR: IN LINE NUMBER:\t" + token.getPosition()
							+ ":\tExpected One of these token Type:\t"
							+ tokenType);
		}
		if (token.getValue().equals("$")) {
			PrintUtil.warning(parserLog, LOGTYPE.SYNTAX, "REACHED END OF FILE");
			PrintUtil
					.warning(grammarLog, LOGTYPE.SYNTAX, "REACHED END OF FILE");
		}
		return false;
	}

	// relOp -> < | <= | <> | == | > | >=
	// addOp -> + | - | or
	private boolean matchTokenType(String tokenType, SYMBOLTYPE symbolType) {
		if (tokenType.equals("relOp")
				&& token.getDesc().toString().startsWith("T_OP_REL_")) {
			printGrammar("relOp", token.getValue());
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"relOp" + " -> " + token.getValue());
			getNextToken();
			return true;
		} else if (tokenType.equals("addOp")
				&& (token.getValue().equals("+")
						|| token.getValue().equals("-") || token.getValue()
						.equals("or"))) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"addOp" + " -> " + token.getValue());
			printGrammar("addOp", token.getValue());
			getNextToken();
			return true;
		} else if (tokenType.equals("multOp")
				&& (token.getValue().equals("*")
						|| token.getValue().equals("/") || token.getValue()
						.equals("and"))) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "multOp" + " -> "
					+ token.getValue());
			printGrammar("multOp", token.getValue());
			getNextToken();
			return true;
		} else if (tokenType.equals(token.getDesc())) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, token.getDesc() + " -> "
					+ token.getValue());
			Symbol tempSymb = new Symbol();
			if (token.getDesc().equals("T_IDENTIFIER")) {
				printGrammar("id", token.getValue());
				if (symbolType == SYMBOLTYPE.ISCLASSORFUNC) {
					tempSymb.setToken(copyToken(token));
				} else {
					symbol = new Symbol();
					symbol.setDataType(copyToken(prevToken));
					symbol.setToken(copyToken(token));
					symbol.symbolType = symbolType;
				}
				// System.out.println("NEW SYMBOL: ID\t"
				// + symbol.getToken().getValue() + "\tDATA TYPE:\t"
				// + symbol.getDataType().getValue());
			} else if (token.getDesc().equals("T_INTEGER")) {
				printGrammar("T_INTEGER", token.getValue());
				if (symbolType == SYMBOLTYPE.ARRAYSIZE) {
					symbol.setArray(true);
					symbol.setArrLength(symbol.getArrLength() + 1);
					int arrValue = Integer.parseInt(token.getValue());
					symbol.getArrSize().add(arrValue);
				}
			} else if (token.getDesc().equals("T_FLOAT")) {
				printGrammar("T_FLOAT", token.getValue());
			}

			if (symbolType == SYMBOLTYPE.CLASS) {
				semantics.classDecl(symbol);
			} else if (symbolType == SYMBOLTYPE.FUNCTION) {
				semantics.functionDecl(symbol);
			} else if (symbolType == SYMBOLTYPE.QUITTABLE) {
				semantics.QuitPresentTable();
			} else if (symbolType == SYMBOLTYPE.ISVARDECLARED) {
				semantics.isVarDeclared(symbol);
				semantics.resetOffset();
			} else if (symbolType == SYMBOLTYPE.ISCLASSORFUNC) {
				// tempSymb.setToken(copyToken(token));
				if (semantics.isClassType(symbol, tempSymb)) {
					copySymbol(symbol, tempSymb);
					symbol.setToken(token);
					symbol.setDataType(tempSymb.getDataType());
					symbol.symbolType = symbolType;
				}
			} else if (symbolType == SYMBOLTYPE.FORLOOPVAR) {
				semantics.variableDecl(symbol);
			}
			getNextToken();
			return true;
		} else {
			PrintUtil.error(parserLog, LOGTYPE.SYNTAX,
					"ERROR: IN LINE NUMBER:\t" + token.getPosition()
							+ ":\tExpected One of these token Type:\t"
							+ tokenType);
		}
		if (token.getValue().equals("$")) {
			PrintUtil.warning(parserLog, LOGTYPE.SYNTAX, "REACHED END OF FILE");
			PrintUtil
					.warning(grammarLog, LOGTYPE.SYNTAX, "REACHED END OF FILE");
		}
		return false;
	}

	private Token copyToken(Token token) {
		Token cpyToken = new Token(token.getPosition(), token.getValue(),
				token.getDesc(), token.getTokenType());
		return cpyToken;
	}

	private boolean matchTokenValue(String tokenType) {
		if (tokenType.equals(token.getValue())) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, token.getDesc() + " -> "
					+ token.getValue());
			if (token.getValue().equals("program")) {
				symbol = new Symbol();
				symbol.setDataType(copyToken(prevToken));
				symbol.setToken(copyToken(token));
				semantics.programDecl(symbol);
			}
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
			PrintUtil
					.info(grammarLog, LOGTYPE.SYNTAX, "type            -> int");
			printGrammar("type", token.getValue());
			getNextToken();
			return true;
		} else if (token.getDesc().equals("T_RESERVE_WORD_FLOAT")) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"type            -> float");
			printGrammar("type", token.getValue());
			getNextToken();
			return true;
		} else if (token.getDesc().equals("T_IDENTIFIER")) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "type            -> id"
					+ "\t->\t" + token.getValue());
			printGrammar("type", token.getValue());
			// if (!semantics.isDataTypeDefined(token)) {
			// System.out.println("UNDEFINED DATA TYPE:\t" + token.getValue());
			// }
			getNextToken();
			return true;
		} else {
			PrintUtil.error(parserLog, LOGTYPE.SYNTAX,
					"ERROR: IN LINE NUMBER:\t" + token.getPosition()
							+ ":\tExpected One of these token Type:\t"
							+ "float | id | int");
			if (token.getValue().equals("$")) {
				PrintUtil.warning(grammarLog, LOGTYPE.SYNTAX,
						"REACHED END OF FILE");
				PrintUtil.warning(parserLog, LOGTYPE.SYNTAX,
						"REACHED END OF FILE");
				return false;
			}
			PrintUtil.error(parserLog, LOGTYPE.SYNTAX,
					"SKIPPING TOKEN:\t" + token.getValue()
							+ "\tAT LINE NUMBER:\t" + token.getPosition());
			getNextToken();
		}

		return true;
	}

	// num -> float | int
	private boolean matchNum() {
		if (token.getDesc().equals("T_INTEGER")) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"num            -> T_INTEGER");
			printGrammar("num", token.getValue());
			getNextToken();
			return true;
		} else if (token.getDesc().equals("T_FLOAT")) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX,
					"num            -> T_FLOAT");
			printGrammar("num", token.getValue());
			getNextToken();
			return true;
		} else {
			PrintUtil.error(parserLog, LOGTYPE.SYNTAX,
					"ERROR: IN LINE NUMBER:\t" + token.getPosition()
							+ ":\tExpected One of these token Type:\t"
							+ "float | int");
		}
		if (token.getValue().equals("$")) {
			grammarLog.warning("REACHED END OF FILE");
			PrintUtil.warning(parserLog, LOGTYPE.SYNTAX, "REACHED END OF FILE");
		}
		return false;
	}

	// sign -> +
	// sign -> -
	private boolean matchSign() {
		if (token.getValue().equals("+")) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "sign -> +");
			printGrammar("sign", token.getValue());
			getNextToken();
			return true;
		} else if (token.getValue().equals("-")) {
			PrintUtil.info(grammarLog, LOGTYPE.SYNTAX, "sign -> -");
			printGrammar("sign", token.getValue());
			getNextToken();
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
		PrintUtil.error(parserLog, LOGTYPE.SYNTAX, "ERROR: IN LINE NUMBER:\t"
				+ token.getPosition() + ":\tExpected One of these tokens:\t"
				+ ffSet);
		while (!token.getValue().equals("$")) {
			if (checkFirstSet(firstG) || checkFollowSet(followG)) {
				PrintUtil.warning(parserLog, LOGTYPE.SYNTAX,
						"RESUME PARSING FROM  TOKEN:\t" + token.getValue()
								+ "\tAT LINE NUMBER:\t" + token.getPosition());
				return true;
			} else {
				PrintUtil.warning(parserLog, LOGTYPE.SYNTAX,
						"SKIPPING TOKEN:\t" + token.getValue()
								+ "\tAT LINE NUMBER:\t" + token.getPosition());
				getNextToken();
			}
		}
		if (token.getValue().equals("$")) {
			grammarLog.warning("REACHED END OF FILE");
			PrintUtil.warning(parserLog, LOGTYPE.SYNTAX, "REACHED END OF FILE");
		}

		return false;
	}

	private void getNextToken() {
		prevToken = token;
		try {
			token = lexer.getNextToken();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ASSIGNMENT 4
	// copy all the symbol related values
	// except the token and symbol table details
	private void copySymbol(Symbol symbol, Symbol tableSymbol) {
		symbol.symbolType = tableSymbol.symbolType;
		symbol.structure = tableSymbol.structure;
		// set token and data type
		symbol.setDataType(copyToken(tableSymbol.getDataType()));
		symbol.setToken(copyToken(tableSymbol.getToken()));
		// set other values
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
}
