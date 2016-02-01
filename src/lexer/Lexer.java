package lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

public class Lexer {

	static String inputFile = "INPUT.txt";
	static String tokenFile = "TOKEN.txt";
	static String errorFile = "ERROR.txt";
	static String commentFile = "COMMENT.txt";

	private static Vector<Token> tokens;
	private static Vector<Token> errors;
	private static Vector<Token> comments;

	public static void main(String[] args) {
		Lexer lexer = new Lexer();
		lexer.scanInputFile(inputFile, tokenFile, errorFile,commentFile);
	}

	public void scanInputFile(String inputFile, String tokenFile,
			String errorFile, String commentFile) {
		tokens = new Vector<Token>();
		errors = new Vector<Token>();
		comments = new Vector<Token>();
		try {

			FileReader fileReader = new FileReader(inputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StreamTokenizer st = new StreamTokenizer(bufferedReader);

			// All characters are Ordinary.
			st.resetSyntax();

			st.eolIsSignificant(true);

			st.wordChars('0', '9');
			st.wordChars('a', 'z');
			st.wordChars('A', 'Z');
			st.wordChars('_', '_');

			st.lowerCaseMode(false);
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
				case StreamTokenizer.TT_EOF:
					System.out.println("END OF FILE ENCOUNTERED.");
					eof = true;
					break;
				case StreamTokenizer.TT_EOL:
					break;
				default:
					char tokenValue = (char) token;
					tokenizeOrdinarChars(st, tokenValue);
				}
			} while (!eof);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		writeToFile(tokens, tokenFile);
		writeToFile(errors, errorFile);
		writeToFile(comments, commentFile);

	}

	private static void writeToFile(Vector<Token> tokens, String fileName) {
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(fileName));
			out.println(tokens);
			// for(Token token:tokens){
			// out.println(token);
			// }
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void tokenizeOrdinarChars(StreamTokenizer st, char tokenValue) {
		switch (tokenValue) {
		case TokenType.T_OP_ADD:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_OP_ADD"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_OP_ADD");
			break;
		case TokenType.T_OP_SUB:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_OP_SUB"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_OP_SUB");
			break;
		case TokenType.T_OP_MUL:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_OP_MUL"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_OP_MUL");
			break;
		case TokenType.T_OP_DIV:
			int divLineNum = st.lineno();
			try {
				char nextToken = (char) st.nextToken();
				if (nextToken == TokenType.T_OP_DIV) {
					comments.add(new Token(st.lineno(), "//",
							"T_SINGLE_LINE_COMMENT"));
					System.out.println("LINE:\t" + divLineNum + "\tLEXEME:\t"
							+ "//" + "\tTOKEN:\tT_SINGLE_LINE_COMMENT");
					while (st.nextToken() != StreamTokenizer.TT_EOL) {
					}
				} else if (nextToken == TokenType.T_OP_MUL) {
					comments.add(new Token(st.lineno(), "/*",
							"T_MULTIPLE_LINE_COMMENT_START"));
					System.out.println("LINE:\t" + divLineNum + "\tLEXEME:\t"
							+ "/*" + "\tTOKEN:\tT_MULTIPLE_LINE_COMMENT_START");
					int commmentCount = 1;
					char n1Token = (char) st.nextToken();
					char n2Token = (char) st.nextToken();
					while (commmentCount != 0) {
						if (n1Token == '/' && n2Token == '*') {
							comments.add(new Token(st.lineno(), "/*",
									"T_NESTED_MULTIPLE_LINE_COMMENT_START LEVEL:\t"
											+ commmentCount));
							System.out
									.println("LINE:\t"
											+ st.lineno()
											+ "\tLEXEME:\t"
											+ "/*"
											+ "\tTOKEN:\tT_NESTED_MULTIPLE_LINE_COMMENT_START LEVEL:\t"
											+ commmentCount);
							commmentCount++;
							n1Token = (char) st.nextToken();
							n2Token = (char) st.nextToken();
						} else if (n1Token == '*' && n2Token == '/') {
							commmentCount--;
							if (commmentCount == 0) {
								comments.add(new Token(st.lineno(), "*/",
										"T_MULTIPLE_LINE_COMMENT_END"));
								System.out
										.println("LINE:\t"
												+ st.lineno()
												+ "\tLEXEME:\t"
												+ "*/"
												+ "\tTOKEN:\tT_MULTIPLE_LINE_COMMENT_END");
								break;
							} else {
								comments.add(new Token(st.lineno(), "*/",
										"T_NESTED_MULTIPLE_LINE_COMMENT_END LEVEL:\t"
												+ commmentCount));
								System.out
										.println("LINE:\t"
												+ st.lineno()
												+ "\tLEXEME:\t"
												+ "*/"
												+ "\tTOKEN:\tT_NESTED_MULTIPLE_LINE_COMMENT_END LEVEL:\t"
												+ commmentCount);
							}
							n1Token = (char) st.nextToken();
							n2Token = (char) st.nextToken();
						} else if (n2Token == (char) StreamTokenizer.TT_EOF
								|| n1Token == (char) StreamTokenizer.TT_EOF) {
							if (commmentCount != 0) {
								errors.add(new Token(
										st.lineno(),
										"END OF FILE ENCOUNTERED",
										commmentCount
												+ "\tMULIPLE LINE COMMENTS ARE NOT ENDED"));
								System.out
										.println("ERROR:\tEND OF FILE ENCOUNTERED.\t"
												+ commmentCount
												+ "\tMULIPLE LINE COMMENTS ARE NOT ENDED");
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
				} else {
					tokens.add(new Token(divLineNum, "" + tokenValue,
							"T_OP_DIV"));
					System.out.println("LINE:\t" + divLineNum + "\tLEXEME:\t"
							+ tokenValue + "\tTOKEN:\tT_OP_DIV");
					st.pushBack();
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
			break;
		case TokenType.T_OP_EQUAL:
			try {
				char nextToken = (char) st.nextToken();
				if (nextToken == TokenType.T_OP_EQUAL) {
					tokens.add(new Token(st.lineno(), "==", "T_OP_REL_EQUAL"));
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
							+ "==" + "\tTOKEN:\tT_OP_REL_EQUAL");
				} else {
					tokens.add(new Token(st.lineno(), "" + tokenValue,
							"T_OP_ASSIGN_EQUAL"));
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
							+ tokenValue + "\tTOKEN:\tT_OP_ASSIGN_EQUAL");
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
					tokens.add(new Token(st.lineno(), "<>", "T_OP_REL_EQUAL"));
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
							+ "<>" + "\tTOKEN:\tT_OP_REL_EQUAL");
				} else if (nextToken == TokenType.T_OP_EQUAL) {
					tokens.add(new Token(st.lineno(), "<=",
							"T_OP_REL_LESSTHAN_EQUAL"));
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
							+ "<=" + "\tTOKEN:\tT_OP_REL_LESSTHAN_EQUAL");
				} else {
					tokens.add(new Token(st.lineno(), "<", "T_OP_REL_LESSTHAN"));
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
							+ tokenValue + "\tTOKEN:\tT_OP_REL_LESSTHAN");
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
					tokens.add(new Token(st.lineno(), ">=",
							"T_OP_REL_GREATERTHAN_EQUAL"));
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
							+ ">=" + "\tTOKEN:\tT_OP_REL_GREATERTHAN_EQUAL");
				} else {
					tokens.add(new Token(st.lineno(), "" + tokenValue,
							"T_OP_REL_GREATERTHAN"));
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
							+ tokenValue + "\tTOKEN:\tT_OP_REL_GREATERTHAN");
					st.pushBack();
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
			break;
		case TokenType.T_DEL_R_LPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_R_LPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_DEL_R_LPAREN");
			break;
		case TokenType.T_DEL_R_RPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_R_RPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_DEL_R_RPAREN");
			break;
		case TokenType.T_DEL_S_LPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_S_LPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_DEL_S_LPAREN");
			break;
		case TokenType.T_DEL_S_RPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_S_RPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_DEL_S_RPAREN");
			break;
		case TokenType.T_DEL_C_LPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_C_LPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_DEL_C_LPAREN");
			break;
		case TokenType.T_DEL_C_RPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_C_RPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_DEL_C_RPAREN");
			break;
		case TokenType.T_DEL_SEMICOLON:
			tokens.add(new Token(st.lineno(), "" + tokenValue,
					"T_DEL_SEMICOLON"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_DEL_SEMICOLON");
			break;
		case TokenType.T_DEL_COMMA:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_COMMA"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
					+ tokenValue + "\tTOKEN:\tT_DEL_COMMA");
			break;
		case TokenType.T_DEL_DOT:
			int dotLineNum = st.lineno();
			try {
				if (st.nextToken() == StreamTokenizer.TT_WORD) {
					String nextStr = st.sval;

					String intPattern = "([0-9]+)";
					String idPattern = "([a-zA-Z]+[_a-zA-Z0-9]*)";

					if (Pattern.matches(intPattern, nextStr)) {
						String fractionVal = tokenValue + nextStr;
						tokens.add(new Token(dotLineNum, fractionVal,
								"T_FRACTION"));
						System.out.println("LINE:\t" + dotLineNum
								+ "\tLEXEME:\t" + fractionVal
								+ "\tTOKEN:\tT_FRACTION");
					} else if (Pattern.matches(idPattern, nextStr)) {
						tokens.add(new Token(dotLineNum, "" + tokenValue,
								"T_DEL_DOT"));
						System.out.println("LINE:\t" + dotLineNum
								+ "\tLEXEME:\t" + tokenValue
								+ "\tTOKEN:\tT_DEL_DOT");
						st.pushBack();
					} else {
						int index = splitStrTokens(nextStr);
						String intString = nextStr.substring(0, index);
						String fractionVal = tokenValue + intString;
						String idString = nextStr.substring(index,
								nextStr.length());
						tokens.add(new Token(dotLineNum, fractionVal,
								"T_FRACTION"));
						tokens.add(new Token(dotLineNum, idString,
								"T_IDENTIFIER"));
						System.out.println("LINE:\t" + dotLineNum
								+ "\tLEXEME:\t" + fractionVal
								+ "\tTOKEN:\tT_FRACTION");
						System.out.println("LINE:\t" + dotLineNum
								+ "\tLEXEME:\t" + idString
								+ "\tTOKEN:\tT_IDENTIFIER");
					}
				} else {
					tokens.add(new Token(dotLineNum, "" + tokenValue,
							"T_DEL_DOT"));
					System.out.println("LINE:\t" + dotLineNum + "\tLEXEME:\t"
							+ tokenValue + "\tTOKEN:\tT_DEL_DOT");
					st.pushBack();
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
			break;
		default:
			if (tokenValue > 32) {
				errors.add(new Token(st.lineno(), "" + tokenValue, "ERROR"));
				System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
						+ tokenValue + "\tTOKEN:\tERROR");
			}
		}
	}

	private static void tokenizeWords(StreamTokenizer st) {
		String strValue = st.sval;
		int lineNum = st.lineno();

		String intPattern = "([0-9]+)";
		String idPattern = "([a-zA-Z]+[_a-zA-Z0-9]*)";

		if (TokenType.RESERVE_WORD.contains(strValue)) {
			tokens.add(new Token(lineNum, strValue, "T_RESERVE_WORD"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue
					+ "\tTOKEN:\tT_RESERVE_WORD");
		} else if (strValue.equals(TokenType.T_OP_AND)) {
			tokens.add(new Token(lineNum, strValue, "T_LOGICAL_AND"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue
					+ "\tTOKEN:\tT_LOGICAL_AND");
		} else if (strValue.equals(TokenType.T_OP_OR)) {
			tokens.add(new Token(lineNum, strValue, "T_LOGICAL_OR"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue
					+ "\tTOKEN:\tT_LOGICAL_OR");
		} else if (strValue.equals(TokenType.T_OP_NOT)) {
			tokens.add(new Token(lineNum, strValue, "T_LOGICAL_NOT"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue
					+ "\tTOKEN:\tT_LOGICAL_NOT");
		} else if (Pattern.matches(idPattern, strValue)) {
			tokens.add(new Token(lineNum, strValue, "T_IDENTIFIER"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue
					+ "\tTOKEN:\tT_IDENTIFIER");
		} else if (Pattern.matches(intPattern, strValue)) {
			try {
				char dotToken = (char) st.nextToken();
				if (dotToken == '.') {
					if (st.nextToken() == StreamTokenizer.TT_WORD) {
						String nextStr = st.sval;
						if (nextStr.matches("([a-zA-Z][_a-zA-Z0-9]*)")) {
							st.pushBack();
							errors.add(new Token(lineNum, strValue, "ERROR"));
							System.out.println("LINE:\t" + lineNum
									+ "\tLEXEME:\t" + strValue + dotToken
									+ "\tTOKEN:\tERROR");
						} else if (Pattern.matches(intPattern, nextStr)) {
							String floatVal = strValue + dotToken + nextStr;
							tokens.add(new Token(lineNum, floatVal, "T_FLOAT"));
							System.out.println("LINE:\t" + lineNum
									+ "\tLEXEME:\t" + floatVal
									+ "\tTOKEN:\tT_FLOAT");
						} else {
							int index = splitStrTokens(nextStr);
							String intString = nextStr.substring(0, index);
							String floatVal = strValue + dotToken + intString;
							String idString = nextStr.substring(index,
									nextStr.length());
							tokens.add(new Token(lineNum, floatVal, "T_FLOAT"));
							tokens.add(new Token(lineNum, idString,
									"T_IDENTIFIER"));
							System.out.println("LINE:\t" + lineNum
									+ "\tLEXEME:\t" + floatVal
									+ "\tTOKEN:\tT_FLOAT");
							System.out.println("LINE:\t" + lineNum
									+ "\tLEXEME:\t" + idString
									+ "\tTOKEN:\tT_IDENTIFIER");
						}
					} else {
						st.pushBack();
						String errorStr = strValue + dotToken;
						errors.add(new Token(lineNum, errorStr, "ERROR"));
						System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t"
								+ errorStr + "\tTOKEN:\tERROR");
					}
				} else {
					st.pushBack();
					tokens.add(new Token(st.lineno(), strValue, "T_INTEGER"));
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t"
							+ strValue + "\tTOKEN:\tT_INTEGER");
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
		} else if(strValue.startsWith("_")){
			int index = 1;
			String _String = strValue.substring(0, index);
			String idString = strValue.substring(index, strValue.length());
			errors.add(new Token(lineNum, _String, "T_ERROR"));
			tokens.add(new Token(lineNum, idString, "T_IDENTIFIER"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + _String
					+ "\tTOKEN:\tT_ERROR");
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + idString
					+ "\tTOKEN:\tT_IDENTIFIER");
			
		}else {
			int index = splitStrTokens(strValue);
			String intString = strValue.substring(0, index);
			String idString = strValue.substring(index, strValue.length());
			tokens.add(new Token(lineNum, intString, "T_INTEGER"));
			tokens.add(new Token(lineNum, idString, "T_IDENTIFIER"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + intString
					+ "\tTOKEN:\tT_INTEGER");
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + idString
					+ "\tTOKEN:\tT_IDENTIFIER");
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
