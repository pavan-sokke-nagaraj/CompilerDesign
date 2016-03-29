package compiler.semantics;

import java.util.ArrayList;

import compiler.lexer.Token;

public class Symbol {

	// each recognized symbol is set as a given structure - simple, array, class
	// or an array of class
	public enum STRUCTURE {
		SIMPLE, ARRAY, CLASS, CLASSARRAY
	};

	// each recognized symbol is set to a symbol type
	public enum SYMBOLTYPE {
		UNKNOWN, CLASS, PROGRAM, VARIABLE, PARAMETER, ARRAYSIZE, FUNCTION, QUITTABLE, ISVARDECLARED, ISCLASSORFUNC, FORLOOPVAR
	};

	public SYMBOLTYPE symbolType;
	public STRUCTURE structure;
	private Token dataType; // Symbol type: int/float/id/value
	private Token token; // associated token

	// address to which symbol tale it is listed in
	private SymbolTable selfTable = null;
	// address to its present symbol table
	private SymbolTable childTable = null;

	private boolean isDataTypeDefined = true;
	private boolean isDuplicate = false;
	private boolean isValidVarName = false;

	// if the Symbol contains array size
	private boolean isArray; // set isArray flag to true
	private int arrLength; // set the array length
	private ArrayList<Integer> arrSize = new ArrayList<Integer>();

	private int noOfParams;
	private ArrayList<String> params = new ArrayList<String>();
	// Address generated for the current Symbol
	private String address;

	public Token getDataType() {
		return dataType;
	}

	public void setDataType(Token dataType) {
		this.dataType = dataType;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public boolean isArray() {
		return isArray;
	}

	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}

	public int getArrLength() {
		return arrLength;
	}

	public SymbolTable getSelfTable() {
		return selfTable;
	}

	public void setSelfTable(SymbolTable selfTable) {
		this.selfTable = selfTable;
	}

	public SymbolTable getChildTable() {
		return childTable;
	}

	public void setChildTable(SymbolTable childTable) {
		this.childTable = childTable;
	}

	public void setArrLength(int arrLength) {
		this.arrLength = arrLength;
	}

	public ArrayList<Integer> getArrSize() {
		return arrSize;
	}

	public void setArrSize(ArrayList<Integer> arrSize) {
		this.arrSize = arrSize;
	}

	public boolean isDuplicate() {
		return isDuplicate;
	}

	public void setDuplicate(boolean isDuplicate) {
		this.isDuplicate = isDuplicate;
	}

	public boolean isDataTypeDefined() {
		return isDataTypeDefined;
	}

	public void setDataTypeDefined(boolean isDataTypeDefined) {
		this.isDataTypeDefined = isDataTypeDefined;
	}

	public int getNoOfParams() {
		return noOfParams;
	}

	public void setNoOfParams(int noOfParams) {
		this.noOfParams = noOfParams;
	}

	public ArrayList<String> getParams() {
		return params;
	}

	public void setParams(ArrayList<String> params) {
		this.params = params;
	}

	public boolean isValidVarName() {
		return isValidVarName;
	}

	public void setValidVarName(boolean isValidVarName) {
		this.isValidVarName = isValidVarName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
