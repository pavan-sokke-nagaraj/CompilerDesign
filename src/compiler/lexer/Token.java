package compiler.lexer;

import compiler.lexer.TokenType.TOKENTYPE;

/**
 * @author Pavan Sokke Nagaraj <pavansn8@gmail.com> Token Class to save the
 *         position of the token value of the token and type of token
 *
 */
public class Token {
	private String desc; // short description of the recognized token
	private String value;// value of the token
	private int position;// line number of the token
	private TOKENTYPE tokenType;// type of token

	/**
	 * @param position
	 * @param value
	 * @param tokenType
	 *            Initialize found token with the token position, value and type
	 *            of token
	 */
	public Token(int position, String value, String desc, TOKENTYPE tokenType) {
		this.position = position;
		this.value = value;
		this.desc = desc;
		this.tokenType = tokenType;
	}

	public Token() {
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String tokenType) {
		this.desc = tokenType;
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

	public TOKENTYPE getTokenType() {
		return tokenType;
	}

	public void setTokenType(TOKENTYPE tokenType) {
		this.tokenType = tokenType;
	}

	public String toString() {
		return "LINE:\t" + position + "\tVALUE:\t" + value + "\t\tTOKEN:\t"
				+ desc;
	}

}
