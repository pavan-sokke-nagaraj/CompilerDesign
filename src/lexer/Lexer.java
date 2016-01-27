package lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

public class Lexer {
	
	static String fileName = "Input.java";
	Vector<Token> tokens;

	public static void main(String[] args) {

		try {
			
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StreamTokenizer st = new StreamTokenizer(bufferedReader);

			// All characters are Ordinary.
			st.resetSyntax();

			st.eolIsSignificant(true);

			st.wordChars('0', '9');
			st.wordChars('a', 'z');
			st.wordChars('A', 'Z');

			st.lowerCaseMode(true);

			// set all characters to space except new line(10)
			// ascii 9 - TAB issue
			st.whitespaceChars(0, 9);
			st.whitespaceChars(11, 32);
			st.ordinaryChar(32); // ascii SPACE
			st.ordinaryChar(9); // ascii TAB

			boolean eof = false;
			do {

				int token = st.nextToken();
				switch (token) {
				case StreamTokenizer.TT_WORD:
					tokenizeWords(st);
					break;
				case StreamTokenizer.TT_NUMBER:
					// tokenizeNumber(st);
					break;
				case StreamTokenizer.TT_EOF:
					System.out.println("End of File encountered.");
					eof = true;
					break;
				case StreamTokenizer.TT_EOL:
					// System.out.println("End of Line encountered.");
					break;
				default:
					char tokenValue = (char) token;
					// System.out.println("|"+ tokenValue + "|");
					// if (tokenValue > 31) {
					tokenizeOrdinarChars(st, tokenValue);
					// }
					// if (token == '!') {
					// eof = true;
					// }
				}
			} while (!eof);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static void tokenizeOrdinarChars(StreamTokenizer st, char tokenValue) {
		// System.out.println(tokenValue + "\t" + st.lineno());
		switch (tokenValue) {
		case TokenType.T_OP_ADD:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tADDITION OPERATOR");
			break;
		case TokenType.T_OP_SUB:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tSUBSTRACTION OPERATOR");
			break;
		case TokenType.T_OP_MUL:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tMULTIPLICATION OPERATOR");
			break;
		case TokenType.T_OP_DIV:
			int divLineNum = st.lineno();
			try {
				char nextToken = (char) st.nextToken();
				if (nextToken == TokenType.T_OP_DIV) {
					System.out.println("LINE:\t" + divLineNum + "\tLEXEME\t"
							+ "//" + "\tTOKEN:\tSINGLE LINE COMMENT");
					while (st.nextToken() != StreamTokenizer.TT_EOL) {
					}
				} else if (nextToken == TokenType.T_OP_MUL) {
					System.out.println("LINE:\t" + divLineNum + "\tLEXEME\t"
							+ "/*" + "\tTOKEN:\tMULTIPLE LINE COMMENT BEGIN");
					int commmentCount = 1;
					char n1Token = (char) st.nextToken();
					char n2Token = (char) st.nextToken();
					// System.out.println(n1Token + "\t" + n2Token);
					while (commmentCount != 0) {
						if (n1Token == '/' && n2Token == '*') {
							System.out
									.println("LINE:\t"
											+ st.lineno()
											+ "\tLEXEME\t"
											+ "/*"
											+ "\tTOKEN:\tNESTED MULTIPLE LINE COMMENT BEGIN - LEVEL:\t"
											+ commmentCount);
							commmentCount++;
							n1Token = (char) st.nextToken();
							n2Token = (char) st.nextToken();
						} else if (n1Token == '*' && n2Token == '/') {
							commmentCount--;
							if (commmentCount == 0) {
								System.out
										.println("LINE:\t"
												+ st.lineno()
												+ "\tLEXEME\t"
												+ "*/"
												+ "\tTOKEN:\tMULTIPLE LINE COMMENT END");
								break;
							} else {
								System.out
										.println("LINE:\t"
												+ st.lineno()
												+ "\tLEXEME\t"
												+ "*/"
												+ "\tTOKEN:\tNESTED MULTIPLE LINE COMMENT END - LEVEL:\t"
												+ commmentCount);
							}
							n1Token = (char) st.nextToken();
							n2Token = (char) st.nextToken();
						} else if (n2Token == (char) StreamTokenizer.TT_EOF
								|| n1Token == (char) StreamTokenizer.TT_EOF) {
							if (commmentCount != 0) {
								System.out
										.println("ERROR:\tEnd Of File Occured.\t"
												+ commmentCount
												+ " MULIPLE LINE COMMENTS ARE NOT ENDED");
							} else {
								System.out
										.println("All Is Well Call 911...!!!");
							}
							break;
						} else {
							n1Token = n2Token;
							n2Token = (char) st.nextToken();
						}
					}
					;
				} else {
					System.out.println("LINE:\t" + divLineNum + "\tLEXEME\t"
							+ tokenValue
							+ "\tTOKEN:\tASSIGNMENT EQUALS OPERATOR");
					st.pushBack();
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
			// System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t" +
			// tokenValue + "\tTOKEN:\tDIVISION OPERATOR");
			break;
		case TokenType.T_OP_EQUAL:
			try {
				char nextToken = (char) st.nextToken();
				if (nextToken == TokenType.T_OP_EQUAL) {
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
							+ "==" + "\tTOKEN:\tRELATIONAL EQUALS OPERATOR");
				} else {
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
							+ tokenValue
							+ "\tTOKEN:\tASSIGNMENT EQUALS OPERATOR");
					st.pushBack();
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
			break;
		case TokenType.T_OP_LT:
			try {
				char nextToken = (char) st.nextToken();
				if (nextToken == TokenType.T_OP_GT) {
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
							+ "<>" + "\tTOKEN:\tRELATIONALEQUALS OPERATOR");
				} else if (nextToken == TokenType.T_OP_EQUAL) {
					System.out
							.println("LINE:\t"
									+ st.lineno()
									+ "\tLEXEME\t"
									+ "<="
									+ "\tTOKEN:\tRELATIONAL LESS THAN OR EQUALS OPERATOR");
				} else {
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
							+ tokenValue
							+ "\tTOKEN:\tRELATIONAL LESS THAN OPERATOR");
					st.pushBack();
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
			break;
		case TokenType.T_OP_GT:
			try {
				char nextToken = (char) st.nextToken();
				if (nextToken == TokenType.T_OP_EQUAL) {
					System.out
							.println("LINE:\t"
									+ st.lineno()
									+ "\tLEXEME\t"
									+ ">="
									+ "\tTOKEN:\tRELATIONAL GREATER THAN OR EQUALS OPERATOR");
				} else {
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
							+ tokenValue
							+ "\tTOKEN:\tRELATIONAL GREATER THAN OPERATOR");
					st.pushBack();
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
			break;
		case TokenType.T_DEL_R_OP:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tDELIMETER"
					+ "\tOPEN ROUND PARANTHESIS");
			break;
		case TokenType.T_DEL_R_CL:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tDELIMETER"
					+ "\tCLOSE ROUND PARANTHESIS");
			break;
		case TokenType.T_DEL_S_OP:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tDELIMETER"
					+ "\tOPEN SQUARE PARANTHESIS");
			break;
		case TokenType.T_DEL_S_CL:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tDELIMETER"
					+ "\tCLOSE SQUARE PARANTHESIS");
			break;
		case TokenType.T_DEL_C_OP:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tDELIMETER"
					+ "\tOPEN CURLY PARANTHESIS");
			break;
		case TokenType.T_DEL_C_CL:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tDELIMETER"
					+ "\tCLOSE CURLY PARANTHESIS");
			break;
		case TokenType.T_DEL_SEMICOLON:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tDELIMETER" + "\tSEMI COLON");
			break;
		case TokenType.T_DEL_COMMA:
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
					+ tokenValue + "\tTOKEN:\tDELIMETER" + "\tCOMMA");
			break;
		case TokenType.T_DEL_DOT:
			int dotLineNum = st.lineno();
			try {
				if (st.nextToken() == StreamTokenizer.TT_WORD) {
					String nextStr = st.sval;

					String intPattern = "([0-9]+)";
					String idPattern = "([a-z]+[a-z0-9]*)";

					if (Pattern.matches(intPattern, nextStr)) {
						String floatVal = tokenValue + nextStr;
						System.out.println("LINE:\t" + dotLineNum
								+ "\tLEXEME\t" + floatVal
								+ "\tTOKEN:\tNUMBER - FLOAT");
					} else if (Pattern.matches(idPattern, nextStr)) {
						System.out.println("LINE:\t" + dotLineNum
								+ "\tLEXEME\t" + tokenValue
								+ "\tTOKEN:\tDELIMETER" + "\tDOT");
						st.pushBack();
					} else {
						int index = splitStrTokens(nextStr);
						String intString = nextStr.substring(0, index);
						String floatVal = tokenValue + intString;
						System.out.println("LINE:\t" + dotLineNum
								+ "\tLEXEME\t" + floatVal
								+ "\tTOKEN:\tNUMBER - FLOAT");
						String idString = nextStr.substring(index,
								nextStr.length());
						System.out.println("LINE:\t" + dotLineNum
								+ "\tLEXEME\t" + idString
								+ "\tTOKEN:\tIDENTIFIER");
					}
				} else {
					System.out.println("LINE:\t" + dotLineNum + "\tLEXEME\t"
							+ tokenValue + "\tTOKEN:\tDELIMETER" + "\tDOT");
					st.pushBack();
				}
			} catch (IOException e) {
				// System.out.println("LOL");
				st.pushBack();
				e.printStackTrace();
			}
			break;
		default:
			if (tokenValue > 32)
				System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
						+ tokenValue + "\tTOKEN:\tERROR");
		}
	}

	private static void tokenizeWords(StreamTokenizer st) {
		String strValue = st.sval;
		int lineNum = st.lineno();

		String intPattern = "([0-9]+)";
		String idPattern = "([a-z]+[a-z0-9]*)";

		if (TokenType.RESERVE_WORD.contains(strValue)) {
			System.out.println("LINE:\t" + lineNum + "\tLEXEME\t" + strValue
					+ "\tTOKEN:\tRESERVE WORD");
		} else if (strValue.equals(TokenType.T_OP_AND)) {
			System.out.println("LINE:\t" + lineNum + "\tLEXEME\t" + strValue
					+ "\tTOKEN:\tLOGICAL AND");
		} else if (strValue.equals(TokenType.T_OP_OR)) {
			System.out.println("LINE:\t" + lineNum + "\tLEXEME\t" + strValue
					+ "\tTOKEN:\tLOGICAL OR");
		} else if (strValue.equals(TokenType.T_OP_NOT)) {
			System.out.println("LINE:\t" + lineNum + "\tLEXEME\t" + strValue
					+ "\tTOKEN:\tLOGICAL NOT");
		} else if (Pattern.matches(idPattern, strValue)) {
			System.out.println("LINE:\t" + lineNum + "\tLEXEME\t" + strValue
					+ "\tTOKEN:\tIDENTIFIER");
		} else if (Pattern.matches(intPattern, strValue)) {
			try {
				char dotToken = (char) st.nextToken();
				// System.out.println(nextToken);
				if (dotToken == '.') {
					if (st.nextToken() == StreamTokenizer.TT_WORD) {
						String nextStr = st.sval;
						// System.out.println(nextStr);
						if (nextStr.matches("([a-z][a-z0-9]*)")) {
							st.pushBack();
							System.out.println("LINE:\t" + lineNum
									+ "\tLEXEME\t" + strValue + dotToken
									+ "\tTOKEN:\tERROR");
						} else if (Pattern.matches(intPattern, nextStr)) {
							String floatVal = strValue + dotToken + nextStr;
							System.out.println("LINE:\t" + lineNum
									+ "\tLEXEME\t" + floatVal
									+ "\tTOKEN:\tNUMBER - FLOAT");
						} else {
							int index = splitStrTokens(nextStr);
							String intString = nextStr.substring(0, index);
							String floatVal = strValue + dotToken + intString;
							System.out.println("LINE:\t" + lineNum
									+ "\tLEXEME\t" + floatVal
									+ "\tTOKEN:\tNUMBER - FLOAT");
							String idString = nextStr.substring(index,
									nextStr.length());
							System.out.println("LINE:\t" + lineNum
									+ "\tLEXEME\t" + idString
									+ "\tTOKEN:\tIDENTIFIER");
						}
					} else {
						st.pushBack();
						System.out.println("LINE:\t" + lineNum + "\tLEXEME\t"
								+ strValue + dotToken + "\tTOKEN:\tERROR");
					}
				} else {
					st.pushBack();
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME\t"
							+ strValue + "\tTOKEN:\tNUMBER - INTEGER");
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
		} else {
			int index = splitStrTokens(strValue);
			// System.out.println(index);
			String intString = strValue.substring(0, index);
			System.out.println("LINE:\t" + lineNum + "\tLEXEME\t" + intString
					+ "\tTOKEN:\tINTEGER");
			String idString = strValue.substring(index, strValue.length());
			System.out.println("LINE:\t" + lineNum + "\tLEXEME\t" + idString
					+ "\tTOKEN:\tIDENTIFIER");
			// System.out.println("tokenizeWords\t" + strValue + "\t");
		}
	}

	private static int splitStrTokens(String strValue) {
		int index = 0;
		for (index = 0; index < strValue.length(); index++) {
			char c = strValue.charAt(index);
			if (!('0' <= c && c <= '9'))
				break;
		}
		return index;
	}
}
