package compiler.syntacticAnalysis;

import java.util.HashMap;

import compiler.lexer.Token;

public class FirstAndFollw {
	
	HashMap<String, String> firstMap= new HashMap<String, String>(80);
	HashMap<String, String> followMap = new HashMap<String, String>(80);
	

//	prog, classDeclList, classDecl, varDecFunDef1, varDecFunDef2, varDecFunDef3, varDecFunDef4, varDecFunDef5, 
//	varDecFunDef6, varDecFunDef7, varDec, progBody, funcHead, funcDef, funcDefList, fParams, fParamsTailList, 
//	fParamsTail, funcBody, bodyCode, bodyCode2, bodyCode3, statementList, statement, statement1, ctrlStat, 
//	statBlock, assignStat, expr, subExpr, relExpr, arithExpr, arithExprRight, term, termFactor, factor, 
//	subFactor, idFactor, idFactor2, idNest, dotIdNest2, aParamsList, variable, idOrIndice, dotId, dotIdNest, 
//	indiceNest, indiceList, indice, arraySizeList, arraySize, aParams, aParamsTailList, aParamsTail, sign, 
//	addOp, multOp, assignOp, relOp, num, type
	
	static String ffSet[][] = {
		{"prog",			"class program",		""},
		{"classDeclList", 	"class",				"program"},
		{"classDecl",		"class",				""},	
		{"varDecFunDef",	"float id int",			"}"},
		{"varDecFunDef1",	"[ ( ;",				""},
		{"progBody", 		"program", 				""},
		{"funcHead", 		"float id int", 		""},
		{"funcDef", 		"float id int", 		""},
		{"funcDefList", 	"float id int", 		"} $"},
		{"fParams", 		"float id int", 		")"},
		{"fParamsTailList", ",", 					")"},
		{"fParamsTail", 	",", 					""},
		{"funcBody", 		"{", 					""},
		{"bodyCode", 		"id for if get put return int float", 	"}"},
		{"bodyCode2", 		"[ = id .", 					"}"},
		{"statementList", 	"id for if get put return", 	"}"},	
		{"statement", 		"id for if get put return", 	""},
		{"statBlock", 		"{ id for if get put return", 	"; else"},
		{"ctrlStat", 		"for if get put return", 		""},
		{"assignStat", 		"id", 							""},
		{"expr", 			"( not id num + -", 			""},
		{"arithExpr",		"( not id num + -",				""},
		{"subExpr", 		"< <= <> == > >=", 				"; ) ,"},
		{"arithExprRight",	"+ - or",						"; ) , < <= <> == > >= ]"},
		{"relExpr",			"( not id num + -",				""},		
		{"term",			"( not id num + -",				""},
		{"termFactor",		"* / and",						"; ) , < <= <> == > >= ] + - or"},
		{"factor",			"+ - num id not (",				""},
		{"indiceOrParam",	"[ ( .",						"; ) , < <= <> == > >= ] + - or * / and"},
		{"idFactor",		".",							"; ) , < <= <> == > >= ] + - or * / and"},
		{"indiceList",		"[",							". = ; ) , < <= <> == > >= ] + - or * / and"},
		{"dotIdList",		".",	"= )"},
		{"variable",		"id",	""},
		{"dotIdNest",		".",	"= )"},
		{"indice",			"[",	""},		
		{"aParams",			"( not id num + -",	")"},
		{"aParamsTailList",	",",				")"},
		{"aParamsTail",		",",				""},
		{"arraySizeList",	"[",				"; float id int } ) ,"},
		{"arraySize",		"[",				""},
		{"sign",			"+ -",				""},
		{"addOp",			"or + -",			""},
		{"multOp",			"and / *",			""},
		{"assignOp",		"=",				""},
		{"relOp",			">= > == <> <= <",	""},
		{"num",				"int float",		""},
		{"type",			"int id float",		""},
		{"id",				"id",		""},
		{"num",				"num",		""},
	};		
	
	public FirstAndFollw() {		
		for (int i = 0; i < ffSet.length; i++) {
			for (int j = 0; j < 3; j++) {
				firstMap.put(ffSet[i][0], ffSet[i][1]);
				followMap.put(ffSet[i][0], ffSet[i][2]);
			}
		}
	}

	public String matchFirst(String grmrSymbol, Token token) {
		String matchFirst = "";
		String firstSet = firstMap.get(grmrSymbol);
		// if the grammar symbol are terminal symbols
		// eg: class program
		if(firstSet == null){
			return matchFirst;
		}
		String firstValues[] = firstSet.split(" ");
		for (int i = 0; i < firstValues.length; i++) {
			if(firstValues[i].equals("id")){
				if(token.getDesc().equals("T_IDENTIFIER"))
					return null;
			}else if(firstValues[i].equals("num")){
				if(token.getDesc().equals("T_INTEGER") || token.getDesc().equals("T_FLOAT"))
					return null;
			}
			else if(token.getValue().equals(firstValues[i])){
				return null;
			}
		}
		return firstSet;
	}

	public String matchFollow(String grmrSymbol, Token token) {
		String matchFollow = "";
		String followSet = followMap.get(grmrSymbol);
		if(followSet == null){
			return matchFollow;
		}
		String followValues[] = followSet.split(" ");
		for (int i = 0; i < followValues.length; i++) {
			if(followValues[i].equals("id")){
				if(token.getDesc().equals("T_IDENTIFIER"))
					return null;
			}else if(followValues[i].equals("num")){
				if(token.getDesc().equals("T_INTEGER") || token.getDesc().equals("T_FLOAT"))
					return null;
			}
			else if(token.getValue().equals(followValues[i])){
				return null;
			}
		}
		return followSet;
	}
	
	public String getFirstSet(String firstG){
		return firstMap.get(firstG);
	}
	
	public String getFollowSet(String firstG){
		return followMap.get(firstG);
	}
}
