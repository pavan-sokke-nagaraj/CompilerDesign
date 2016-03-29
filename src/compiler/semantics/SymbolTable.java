package compiler.semantics;

import java.util.ArrayList;

public class SymbolTable implements Cloneable {

	// holds the Parent symbol of the current symbol data type
	private Symbol parent = null;
	// list of symbols recognized are set into this symbolList
	private ArrayList<Symbol> symbolList = new ArrayList<Symbol>();

	public Symbol getParent() {
		return parent;
	}

	public void setParent(Symbol parent) {
		this.parent = parent;
	}

	public ArrayList<Symbol> getSymbolList() {
		return symbolList;
	}

	public void setSymbolList(ArrayList<Symbol> symbolList) {
		this.symbolList = symbolList;
	}

	public SymbolTable clone() {
		SymbolTable SymbolTable = null;
		try {
			SymbolTable = (SymbolTable) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return SymbolTable;
	}

}
