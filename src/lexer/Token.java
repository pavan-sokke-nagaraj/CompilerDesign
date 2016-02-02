package lexer;

/**
 * @author Pavan Sokke Nagaraj <pavansn8@gmail.com> Token Class to save the
 *         position of the token value of the token and type of token
 *
 */
public class Token {
	private String tokenType;
	private String value;
	private int position;

	/**
	 * @param position
	 * @param value
	 * @param tokenType
	 *            Initialize found token with the token position, value and type
	 *            of token
	 */
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
		return "\nLINE:\t" + position + "\tVALUE:\t" + value + "\t\tTOKEN:\t" + tokenType;
		// return position + "\t" + value + "\t" + tokenType;
		
		
	}

}
