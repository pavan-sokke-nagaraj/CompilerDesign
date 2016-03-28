package compiler.semantics;

import java.util.ArrayList;

public class SymbolTable implements Cloneable {

	private Symbol parent = null;
	private ArrayList<Symbol> symbolList = new ArrayList<Symbol>();
	private String prefixLink;

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

	public String getPrefixLink() {
		return prefixLink;
	}

	public void setPrefixLink(String prefixLink) {
		this.prefixLink = prefixLink;
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
