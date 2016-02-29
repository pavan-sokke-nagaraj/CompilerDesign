package compiler.syntacticAnalysis;

public class DerivationTree {
	String node = "";
	String nodeData= "";
	DerivationTree left;// = new DerivationTree();
	DerivationTree right;//= new DerivationTree();
	String leftTree= "";
	String rightTree= "";

	public DerivationTree(String node, String nodeData, String leftTree,
			String rightTree, DerivationTree left, DerivationTree right) {
		this.node = node;
		this.nodeData = nodeData;
		this.leftTree = leftTree;
		this.rightTree = rightTree;
		this.left = left;
		this.right = right;
	}

	public DerivationTree(String node, String nodeData) {
		this.node = node;
		this.nodeData = nodeData;
		
	}

	public DerivationTree() {
	}

	public String info() {
		return this.leftTree + " " + this.rightTree;
	}

}
