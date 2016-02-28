package compiler.syntacticAnalysis;

import java.io.IOException;

import compiler.lexer.Lexer;
import compiler.lexer.Token;

public class SynatcticParser {

	Lexer lexer = new Lexer();
	FirstAndFollw firstAndFollw = null;
	Token token = null;

	public SynatcticParser(Lexer lexer) {
		this.lexer = lexer;
		firstAndFollw = new FirstAndFollw();
	}

	public boolean parse() {
		boolean val = false;
		getNextToken();
		if (token == null) {
			System.out.println("Error in Input File");
			return false;
		}
		return startSymbol();
	}

	private boolean startSymbol() {
		return prog();
	}

	// prog -> classDeclList progBody
	private boolean prog() {
		if (classDeclList() && progBody()) {
			System.out.println("prog -> classDeclList progBody");
			return true;
		} else
			return false;
	}

	// classDeclList -> classDecl classDeclList
	// classDeclList -> EPSILON
	private boolean classDeclList() {
		if (checkFirstSet("classDecl")) {
			if (classDecl() && classDeclList()) {
				System.out
						.println("classDeclList    -> classDecl classDeclList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("classDeclList")) {
			System.out.println("classDeclList    -> EPSILON");
			return true;
		}
		return false;
	}

	// classDecl -> class id { varDecFunDef } ;
	private boolean classDecl() {
		if (checkFirstSet("class")) {
			if (matchTokenType("T_RESERVE_WORD_CLASS")
					&& matchTokenType("T_IDENTIFIER")
					&& matchTokenType("T_DEL_C_LPAREN")) {
				if (varDecFunDef() && matchTokenType("T_DEL_C_RPAREN")
						&& matchTokenType("T_DEL_SEMICOLON")) {
					System.out
							.println("classDecl        -> class id { varDecFunDef } ;");
					return true;
				}
			}
		}
		return false;
	}

	// varDecFunDef -> type id varDecFunDef1
	// varDecFunDef -> EPSILON
	private boolean varDecFunDef() {
		if (checkFirstSet("type")) {
			if (matchType() && matchTokenType("T_IDENTIFIER")
					&& varDecFunDef1()) {
				System.out.println("varDecFunDef -> type id varDecFunDef1");
				return true;
			} else {
				return false;
			}
		} else if (checkFollowSet("varDecFunDef")) {
			System.out.println("varDecFunDef    -> EPSILON");
			return true;
		}
		return false;
	}

	// varDecFunDef1 -> ( fParams ) funcBody ; funcDefList
	// varDecFunDef1 -> arraySizeList ; varDecFunDef
	private boolean varDecFunDef1() {
		if (checkFirstSet("(")) {
			if (matchTokenType("T_DEL_R_LPAREN") && fParams()
					&& matchTokenType("T_DEL_R_RPAREN") && funcBody()
					&& matchTokenType("T_DEL_SEMICOLON") && funcDefList()) {
				System.out
						.println("varDecFunDef1 -> ( fParams ) funcBody ; funcDefList");
				return true;
			}
		} else if (checkFirstSet("varDecFunDef1")) {
			if (arraySizeList() && matchTokenType("T_DEL_SEMICOLON")
					&& varDecFunDef()) {
				System.out
						.println("varDecFunDef1 -> arraySizeList ; varDecFunDef");
				return true;
			} else
				return false;
		}
		return false;
	}

	// progBody -> program funcBody ; funcDefList
	private boolean progBody() {
		if (checkFirstSet("program")) {
			if (matchTokenValue("program") && funcBody()
					&& matchTokenType("T_DEL_SEMICOLON") && funcDefList()) {
				System.out
						.println("progBody -> program funcBody ; funcDefList");
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
				return true;
			} else
				return false;
		} else if (checkFollowSet("funcDefList")) {
			System.out.println("funcDefList -> EPSILON");
			return true;
		}
		return false;
	}

	// funcDef -> funcHead funcBody ;
	private boolean funcDef() {
		if (checkFirstSet("funcHead")) {
			if (funcHead() && funcBody() && matchTokenType("T_DEL_SEMICOLON")) {
				System.out.println("funcDef -> funcHead funcBody ;");
				return true;
			}
		}
		return false;
	}

	// funcHead -> type id ( fParams )
	private boolean funcHead() {
		if (checkFirstSet("type")) {
			if (matchType() && matchTokenType("T_IDENTIFIER")
					&& matchTokenType("T_DEL_R_LPAREN") && fParams()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("funcHead -> type id ( fParams )");
				return true;
			} else
				return false;
		}
		return false;
	}

	// fParams -> type id arraySizeList fParamsTailList
	// fParams -> EPSILON
	private boolean fParams() {
		if (checkFirstSet("type")) {
			if (matchType() && matchTokenType("T_IDENTIFIER")
					&& arraySizeList() && fParamsTailList()) {
				System.out
						.println("fParams -> type id arraySizeList fParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("fParams")) {
			System.out.println("funcDefList -> EPSILON");
			return true;
		}
		return false;
	}

	// fParamsTailList -> fParamsTail fParamsTailList
	// fParamsTailList -> EPSILON
	private boolean fParamsTailList() {
		if (checkFirstSet("fParamsTail")) {
			if (fParamsTail() && fParamsTailList()) {
				System.out
						.println("fParamsTailList -> fParamsTail fParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("fParamsTailList")) {
			System.out.println("fParamsTailList -> EPSILON");
			return true;
		}
		return false;
	}

	// fParamsTail -> , type id arraySizeList
	private boolean fParamsTail() {
		if (checkFirstSet(",")) {
			if (matchTokenType("T_DEL_COMMA") && matchType()
					&& matchTokenType("T_IDENTIFIER") && arraySizeList()) {
				System.out.println("fParamsTail -> , type id arraySizeList");
				return true;
			} else
				return false;
		}
		return false;
	}

	// funcBody -> { bodyCode }
	private boolean funcBody() {
		if (checkFirstSet("{")) {
			if (matchTokenType("T_DEL_C_LPAREN") && bodyCode()
					&& matchTokenType("T_DEL_C_RPAREN")) {
				System.out.println("funcBody -> { bodyCode }");
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
		if (checkFirstSet("float")) {
			if (matchType() && matchTokenType("T_IDENTIFIER")
					&& arraySizeList() && matchTokenType("T_DEL_SEMICOLON")
					&& bodyCode()) {
				System.out
						.println("bodyCode -> float id arraySizeList ; bodyCode");
				return true;
			} else
				return false;
		} else if (checkFirstSet("int")) {
			if (matchType() && matchTokenType("T_IDENTIFIER")
					&& arraySizeList() && matchTokenType("T_DEL_SEMICOLON")
					&& bodyCode()) {
				System.out
						.println("bodyCode -> int id arraySizeList ; bodyCode");
				return true;
			} else
				return false;
		} else if (checkFirstSet("id")) {
			if (matchTokenType("T_IDENTIFIER") && bodyCode2()) {
				System.out.println("bodyCode -> id bodyCode2");
				return true;
			} else
				return false;
		} else if (checkFirstSet("ctrlStat")) {
			if (ctrlStat() && matchTokenType("T_DEL_SEMICOLON")
					&& statementList()) {
				System.out.println("bodyCode -> ctrlStat ; statementList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("bodyCode")) {
			System.out.println("bodyCode -> EPSILON");
			return true;
		}
		return false;
	}

	// bodyCode2 -> id arraySizeList ; bodyCode
	// bodyCode2 -> indiceList dotIdList assignOp expr ; statementList
	// bodyCode2 -> EPSILON
	//
	private boolean bodyCode2() {
		if (checkFirstSet("id")) {
			if (matchTokenType("T_IDENTIFIER") && arraySizeList()
					&& matchTokenType("T_DEL_SEMICOLON") && bodyCode()) {
				System.out.println("bodyCode2 -> id arraySizeList ; bodyCode");
				return true;
			} else
				return false;
		} else if (checkFirstSet("bodyCode2")) {
			if (indiceList() && dotIdList() && matchTokenValue("=") && expr()
					&& matchTokenType("T_DEL_SEMICOLON") && statementList()) {
				System.out
						.println("bodyCode2 -> indiceList dotIdList assignOp expr ; statementList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("bodyCode2")) {
			System.out.println("bodyCode2 -> EPSILON");
			return true;
		}
		return false;
	}

	// statementList -> statement statementList
	// statementList -> EPSILON
	private boolean statementList() {
		if (checkFirstSet("statement")) {
			if (statement() && statementList()) {
				System.out.println("statementList -> statement statementList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("statementList")) {
			System.out.println("statementList -> EPSILON");
			return true;
		}
		return false;
	}

	// statement -> ctrlStat ;
	// statement -> assignStat ;
	private boolean statement() {
		if (checkFirstSet("ctrlStat")) {
			if (ctrlStat() && matchTokenType("T_DEL_SEMICOLON")) {
				System.out.println("statement -> ctrlStat ;");
				return true;
			} else
				return false;
		} else if (checkFirstSet("assignStat")) {
			if (assignStat() && matchTokenType("T_DEL_SEMICOLON")) {
				System.out.println("statement -> assignStat ;");
				return true;
			} else
				return false;
		}
		return false;
	}

	// statBlock -> { statementList } | statement
	// statBlock -> EPSILON
	private boolean statBlock() {
		if (checkFirstSet("{")) {
			if (matchTokenType("T_DEL_C_LPAREN") && statementList()
					&& matchTokenType("T_DEL_C_RPAREN")) {
				System.out.println("statBlock -> { statementList } ");
				return true;
			} else
				return false;
		} else if (checkFirstSet("statBlock")) {
			if (statement()) {
				System.out.println("statBlock -> { statementList } ");
				return true;
			} else
				return false;
		} else if (checkFollowSet("statBlock")) {
			System.out.println("statBlock -> EPSILON");
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
		if (checkFirstSet("if")) {
			if (matchTokenType("T_RESERVE_WORD_IF")
					&& matchTokenType("T_DEL_R_LPAREN") && expr()
					&& matchTokenType("T_DEL_R_RPAREN")
					&& matchTokenType("T_RESERVE_WORD_THEN") && statBlock()
					&& matchTokenType("T_RESERVE_WORD_ELSE") && statBlock()) {
				System.out
						.println("ctrlStat -> if ( expr ) then statBlock else statBlock");
				return true;
			} else
				return false;
		} else if (checkFirstSet("for")) {
			if (matchTokenType("T_RESERVE_WORD_FOR")
					&& matchTokenType("T_DEL_R_LPAREN")) {
				if (matchType() && checkFirstSet("id")
						&& matchTokenType("T_IDENTIFIER")) {
					if (matchTokenType("T_OP_ASSIGN_EQUAL") && expr()) {
						if (matchTokenType("T_DEL_SEMICOLON") && relExpr()) {
							if (matchTokenType("T_DEL_SEMICOLON")
									&& assignStat()) {
								if (matchTokenType("T_DEL_R_RPAREN")
										&& statBlock()) {
									System.out
											.println("ctrlStat -> for ( type id assignOp expr ; relExpr ; assignStat ) statBlock");
									return true;
								}
							}
						}
					}
				}
			} else
				return false;
		} else if (checkFirstSet("get")) {
			if (matchTokenType("T_RESERVE_WORD_GET")
					&& matchTokenType("T_DEL_R_LPAREN") && variable()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("ctrlStat -> get ( variable )");
				return true;
			} else
				return false;
		} else if (checkFirstSet("put")) {
			if (matchTokenType("T_RESERVE_WORD_PUT")
					&& matchTokenType("T_DEL_R_LPAREN") && expr()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("ctrlStat -> put ( expr )");
				return true;
			} else
				return false;
		} else if (checkFirstSet("return")) {
			if (matchTokenValue("return") && matchTokenType("T_DEL_R_LPAREN")
					&& expr() && matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("ctrlStat -> return ( expr )");
				return true;
			} else
				return false;
		}
		return false;
	}

	// assignStat -> variable assignOp expr
	private boolean assignStat() {
		if (checkFirstSet("variable")) {
			if (variable() && matchTokenType("T_OP_ASSIGN_EQUAL") && expr()) {
				System.out.println("assignStat -> variable assignOp expr");
				return true;
			} else
				return false;
		}
		return false;
	}

	// expr -> arithExpr subExpr
	private boolean expr() {
		if (checkFirstSet("arithExpr")) {
			if (arithExpr() && subExpr()) {
				System.out.println("expr -> arithExpr subExpr");
				return true;
			} else
				return false;
		}
		return false;
	}

	// arithExpr -> term arithExprRight
	private boolean arithExpr() {
		if (checkFirstSet("term")) {
			if (term() && arithExprRight()) {
				System.out.println("arithExpr -> term arithExprRight");
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
		if (checkFirstSet("relOp")) {
			if (matchTokenType("relOp") && arithExpr()) {
				System.out.println("subExpr -> relOp arithExpr");
				return true;
			} else
				return false;
		} else if (checkFollowSet("subExpr")) {
			System.out.println("subExpr -> EPSILON");
			return true;
		}
		return false;
	}

	// arithExprRight -> addOp term arithExprRight
	// arithExprRight -> EPSILON
	private boolean arithExprRight() {
		if (checkFirstSet("addOp")) {
			if (matchTokenType("addOp") && term() && arithExprRight()) {
				System.out
						.println("arithExprRight -> addOp term arithExprRight");
				return true;
			} else
				return false;
		} else if (checkFollowSet("arithExprRight")) {
			System.out.println("arithExprRight -> EPSILON");
			return true;
		}
		return false;
	}

	// relExpr -> arithExpr relOp arithExpr
	private boolean relExpr() {
		if (checkFirstSet("arithExpr")) {
			if (arithExpr() && matchTokenType("relOp") && arithExpr()) {
				System.out.println("relExpr -> arithExpr relOp arithExpr");
				return true;
			} else
				return false;
		}
		return false;
	}

	// term -> factor termFactor
	private boolean term() {
		if (checkFirstSet("factor")) {
			if (factor() && termFactor()) {
				System.out.println("term -> factor termFactor");
				return true;
			} else
				return false;
		}
		return false;
	}

	// termFactor -> multOp factor termFactor
	// termFactor -> EPSILON
	private boolean termFactor() {
		if (checkFirstSet("multOp")) {
			if (matchTokenType("multOp") && factor() && termFactor()) {
				System.out.println("termFactor -> multOp factor termFactor");
				return true;
			} else
				return false;
		} else if (checkFollowSet("termFactor")) {
			System.out.println("termFactor -> EPSILON");
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
		if (checkFirstSet("(")) {
			if (matchTokenType("T_DEL_R_LPAREN") && arithExpr()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("factor -> ( arithExpr )");
				return true;
			} else
				return false;
		} else if (checkFirstSet("not")) {
			if (matchTokenType("T_LOGICAL_NOT") && factor()) {
				System.out.println("factor -> not factor");
				return true;
			} else
				return false;
		} else if (checkFirstSet("num")) {
			if (matchNum()) {
				System.out.println("factor -> num");
				return true;
			} else
				return false;
		} else if (checkFirstSet("sign")) {
			if (matchSign() && factor()) {
				System.out.println("factor -> sign factor");
				return true;
			} else
				return false;
		} else if (checkFirstSet("id")) {
			if (matchType() && indiceOrParam()) {
				System.out.println("factor -> id indiceOrParam");
				return true;
			} else
				return false;
		}
		return false;
	}

	// indiceOrParam -> indiceList idFactor
	// indiceOrParam -> ( aParams )
	private boolean indiceOrParam() {
		if (checkFirstSet("(")) {
			if (matchTokenType("T_DEL_R_LPAREN") && aParams()
					&& matchTokenType("T_DEL_R_RPAREN")) {
				System.out.println("indiceOrParam -> ( aParams )");
				return true;
			} else
				return false;
		} else if (checkFirstSet("indiceOrParam")) {
			if (indiceList() && idFactor()) {
				System.out.println("indiceOrParam -> indiceList idFactor");
				return true;
			} else
				return false;
		}else if (checkFollowSet("indiceOrParam")) {
			System.out.println("indiceOrParam -> EPSILON");
			return true;
		}
		return false;
	}

	// idFactor -> . id indiceOrParam
	// idFactor -> EPSILON
	private boolean idFactor() {
		if (checkFirstSet(".")) {
			if (matchTokenType("T_DEL_DOT") && matchTokenType("T_IDENTIFIER")
					&& indiceOrParam()) {
				System.out.println("idFactor -> . id indiceOrParam");
				return true;
			} else
				return false;
		} else if (checkFollowSet("idFactor")) {
			System.out.println("idFactor -> EPSILON");
			return true;
		}
		return false;
	}

	// indiceList -> indice indiceList
	// indiceList -> EPSILON
	private boolean indiceList() {
		if (checkFirstSet("indice")) {
			if (indice() && indiceList()) {
				System.out.println("indiceList -> indice indiceList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("indiceList")) {
			System.out.println("indiceList -> EPSILON");
			return true;
		}
		return false;
	}

	// dotIdList -> . id indiceList
	// dotIdList -> EPSILON
	private boolean dotIdList() {
		if (checkFirstSet(".")) {
			if (matchTokenType("T_DEL_DOT") && matchTokenType("T_IDENTIFIER")
					&& indiceList()) {
				System.out.println("dotIdList -> . id indiceList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("dotIdList")) {
			System.out.println("dotIdList -> EPSILON");
			return true;
		}
		return false;
	}

	// variable -> id indiceList dotIdNest
	private boolean variable() {
		if (checkFirstSet("id")) {
			if (matchTokenType("T_IDENTIFIER") && indiceList() && dotIdNest()) {
				System.out.println("variable -> id indiceList dotIdNest");
				return true;
			} else
				return false;
		}
		return false;
	}

	// dotIdNest -> . id indiceList dotIdNest
	// dotIdNest -> EPSILON
	private boolean dotIdNest() {
		if (checkFirstSet(".")) {
			if (matchTokenType("T_DEL_DOT") && matchTokenType("T_IDENTIFIER")
					&& indiceList() && dotIdNest()) {
				System.out.println("dotIdNest -> . id indiceList dotIdNest");
				return true;
			} else
				return false;
		} else if (checkFollowSet("dotIdNest")) {
			System.out.println("dotIdNest -> EPSILON");
			return true;
		}
		return false;
	}

	// indice -> [ arithExpr ]
	private boolean indice() {
		if (checkFirstSet("[")) {
			if (matchTokenType("T_DEL_S_LPAREN") && arithExpr()
					&& matchTokenType("T_DEL_S_RPAREN")) {
				System.out.println("indice -> [ arithExpr ]");
				return true;
			} else
				return false;
		}
		return false;
	}

	// aParams -> expr aParamsTailList
	// aParams -> EPSILON
	private boolean aParams() {
		if (checkFirstSet("expr")) {
			if (expr() && aParamsTailList()) {
				System.out.println("aParams -> expr aParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("aParams")) {
			System.out.println("aParams -> EPSILON");
			return true;
		}
		return false;
	}

	// aParamsTailList -> aParamsTail aParamsTailList
	// aParamsTailList -> EPSILON
	private boolean aParamsTailList() {
		if (checkFirstSet("aParamsTail")) {
			if (aParamsTail() && aParamsTailList()) {
				System.out
						.println("aParamsTailList -> aParamsTail aParamsTailList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("aParamsTailList")) {
			System.out.println("aParamsTailList -> EPSILON");
			return true;
		}
		return false;
	}

	// aParamsTail -> , expr
	private boolean aParamsTail() {
		if (checkFirstSet(",")) {
			if (matchTokenType("T_DEL_COMMA") && expr()) {
				System.out.println("aParamsTail -> , expr");
				return true;
			} else
				return false;
		}
		return false;
	}

	// arraySizeList -> arraySize arraySizeList
	// arraySizeList -> EPSILON
	private boolean arraySizeList() {
		if (checkFirstSet("arraySize")) {
			if (arraySize() && arraySizeList()) {
				System.out.println("arraySizeList -> arraySize arraySizeList");
				return true;
			} else
				return false;
		} else if (checkFollowSet("arraySizeList")) {
			System.out.println("arraySizeList ->  EPSILON");
			return true;
		}
		return false;
	}

	// arraySize -> [ int ]
	private boolean arraySize() {
		if (checkFirstSet("arraySize")) {
			if (matchTokenType("T_DEL_S_LPAREN") && matchTokenType("T_INTEGER")
					&& matchTokenType("T_DEL_S_RPAREN")) {
				System.out.println("arraySizeList -> arraySize arraySizeList");
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
			getNextToken();
			return true;
		} else if (tokenType.equals("addOp") && (token.getValue().equals("+")
				|| token.getValue().equals("-")
				|| token.getValue().equals("or"))) {
			getNextToken();
			return true;
		} else if (tokenType.equals("multOp") && (token.getValue().equals("*")
				|| token.getValue().equals("/")
				|| token.getValue().equals("and"))) {
			getNextToken();
			return true;
		} else if (tokenType.equals(token.getDesc())) {
			getNextToken();
			return true;
		} else {
			return false;
		}
	}

	private boolean matchTokenValue(String tokenType) {
		if (tokenType.equals(token.getValue())) {
			getNextToken();
			return true;
		} else {
			return false;
		}
	}

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

	// type -> float | id | int
	private boolean matchType() {
		if (matchTokenType("T_RESERVE_WORD_INT")) {
			System.out.println("type            -> int");
			return true;
		} else if (matchTokenType("T_RESERVE_WORD_FLOAT")) {
			System.out.println("type            -> float");
			return true;
		} else if (matchTokenType("T_IDENTIFIER")) {
			System.out.println("type            -> id");
			return true;
		}
		return false;
	}

	// num -> float | int
	private boolean matchNum() {
		if (matchTokenType("T_INTEGER")) {
			System.out.println("num            -> T_INTEGER");
			return true;
		} else if (matchTokenType("T_FLOAT")) {
			System.out.println("num            -> T_FLOAT");
			return true;
		}
		return false;
	}

	// sign -> +
	// sign -> -
	private boolean matchSign() {
		if (matchTokenValue("+")) {
			System.out.println("sign -> +");
			return true;
		} else if (matchTokenValue("-")) {
			System.out.println("sign -> -");
			return true;
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
