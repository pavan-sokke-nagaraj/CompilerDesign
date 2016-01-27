package lexer;

public class Token {
	private String tokenType;
	private String value;
	private int position;

	public Token(int position, String value, String tokenType) {
		this.position = position;
		this.value = value;
		this.tokenType = tokenType;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String toString() {
		 return "\nLINE:\t" + position + "\tVALUE:\t" + value + "\t\tTOKEN:\t"
		 + tokenType;
//		return position + "\t" + value + "\t" + tokenType;
	}

}
