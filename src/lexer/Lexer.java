package lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * @author Pavan Sokke Nagaraj <pavansn8@gmail.com> Lexer class to tokenize the
 *         input file and write to tokens, errors and comments vector data
 *         structure
 * 
 *
 */
public class Lexer {

	static String inputFile = "INPUT.txt";
	static String tokenFile = "TOKEN.txt";
	static String errorFile = "ERROR.txt";
	static String commentFile = "COMMENT.txt";

	private static Vector<Token> tokens;
	private static Vector<Token> errors;
	private static Vector<Token> comments;

	static HashMap<String, String> RESERVE_WORD_MAP = new HashMap<String, String>();

	public static void main(String[] args) {
		Lexer lexer = new Lexer();
		HashMap<String, Vector> tokenMap = new HashMap<String, Vector>();

		tokenMap = lexer.scanInputFile(inputFile);

		writeToFile(tokens, tokenFile);
		writeToFile(errors, errorFile);
		writeToFile(comments, commentFile);
	}

	/**
	 * @param inputFile
	 * @return HasMap of Tokens,Errors and comments
	 */
	public HashMap scanInputFile(String inputFile) {

		RESERVE_WORD_MAP.put("if", "T_RESERVE_WORD_IF");
		RESERVE_WORD_MAP.put("then", "T_RESERVE_WORD_THEN");
		RESERVE_WORD_MAP.put("else", "T_RESERVE_WORD_ELSE");
		RESERVE_WORD_MAP.put("for", "T_RESERVE_WORD_FOR");
		RESERVE_WORD_MAP.put("class", "T_RESERVE_WORD_CLASS");
		RESERVE_WORD_MAP.put("int", "T_RESERVE_WORD_INT");
		RESERVE_WORD_MAP.put("float", "T_RESERVE_WORD_FLOAT");
		RESERVE_WORD_MAP.put("get", "T_RESERVE_WORD_GET");
		RESERVE_WORD_MAP.put("put", "T_RESERVE_WORD_PUT");
		RESERVE_WORD_MAP.put("return", "T_RESERVE_WORD_RETURN");

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

		HashMap<String, Vector> tokenMap = new HashMap<String, Vector>();
		tokenMap.put("TOKENS", tokens);
		tokenMap.put("ERRORS", errors);
		tokenMap.put("COMMENTS", comments);
		return tokenMap;
	}

	public static void writeToFile(Vector<Token> tokens, String fileName) {
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
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_OP_ADD");
			break;
		case TokenType.T_OP_SUB:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_OP_SUB"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_OP_SUB");
			break;
		case TokenType.T_OP_MUL:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_OP_MUL"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_OP_MUL");
			break;
		case TokenType.T_OP_DIV:
			int divLineNum = st.lineno();
			try {
				char nextToken = (char) st.nextToken();
				if (nextToken == TokenType.T_OP_DIV) {
					comments.add(new Token(st.lineno(), "//", "T_SINGLE_LINE_COMMENT"));
					System.out
							.println("LINE:\t" + divLineNum + "\tLEXEME:\t" + "//" + "\tTOKEN:\tT_SINGLE_LINE_COMMENT");
					while (st.nextToken() != StreamTokenizer.TT_EOL) {
					}
				} else if (nextToken == TokenType.T_OP_MUL) {
					comments.add(new Token(st.lineno(), "/*", "T_MULTIPLE_LINE_COMMENT_START"));
					System.out.println(
							"LINE:\t" + divLineNum + "\tLEXEME:\t" + "/*" + "\tTOKEN:\tT_MULTIPLE_LINE_COMMENT_START");
					int commmentCount = 1;
					char n1Token = (char) st.nextToken();
					char n2Token = (char) st.nextToken();
					while (commmentCount != 0) {
						if (n1Token == '/' && n2Token == '*') {
							comments.add(new Token(st.lineno(), "/*",
									"T_NESTED_MULTIPLE_LINE_COMMENT_START LEVEL:\t" + commmentCount));
							System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + "/*"
									+ "\tTOKEN:\tT_NESTED_MULTIPLE_LINE_COMMENT_START LEVEL:\t" + commmentCount);
							commmentCount++;
							n1Token = (char) st.nextToken();
							n2Token = (char) st.nextToken();
						} else if (n1Token == '*' && n2Token == '/') {
							commmentCount--;
							if (commmentCount == 0) {
								comments.add(new Token(st.lineno(), "*/", "T_MULTIPLE_LINE_COMMENT_END"));
								System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + "*/"
										+ "\tTOKEN:\tT_MULTIPLE_LINE_COMMENT_END");
								break;
							} else {
								comments.add(new Token(st.lineno(), "*/",
										"T_NESTED_MULTIPLE_LINE_COMMENT_END LEVEL:\t" + commmentCount));
								System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + "*/"
										+ "\tTOKEN:\tT_NESTED_MULTIPLE_LINE_COMMENT_END LEVEL:\t" + commmentCount);
							}
							n1Token = (char) st.nextToken();
							n2Token = (char) st.nextToken();
						} else if (n2Token == (char) StreamTokenizer.TT_EOF
								|| n1Token == (char) StreamTokenizer.TT_EOF) {
							if (commmentCount != 0) {
								errors.add(new Token(st.lineno(), "END OF FILE ENCOUNTERED",
										commmentCount + "\tMULIPLE LINE COMMENTS ARE NOT ENDED"));
								System.out.println("ERROR:\tEND OF FILE ENCOUNTERED.\t" + commmentCount
										+ "\tMULIPLE LINE COMMENTS ARE NOT ENDED");
							} else {
								System.out.println("All Is Well Call 911...!!!");
							}
							break;
						} else {
							n1Token = n2Token;
							n2Token = (char) st.nextToken();
						}
					}
				} else {
					tokens.add(new Token(divLineNum, "" + tokenValue, "T_OP_DIV"));
					System.out.println("LINE:\t" + divLineNum + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_OP_DIV");
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
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + "==" + "\tTOKEN:\tT_OP_REL_EQUAL");
				} else {
					tokens.add(new Token(st.lineno(), "" + tokenValue, "T_OP_ASSIGN_EQUAL"));
					System.out.println(
							"LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_OP_ASSIGN_EQUAL");
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
					System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + "<>" + "\tTOKEN:\tT_OP_REL_EQUAL");
				} else if (nextToken == TokenType.T_OP_EQUAL) {
					tokens.add(new Token(st.lineno(), "<=", "T_OP_REL_LESSTHAN_EQUAL"));
					System.out.println(
							"LINE:\t" + st.lineno() + "\tLEXEME:\t" + "<=" + "\tTOKEN:\tT_OP_REL_LESSTHAN_EQUAL");
				} else {
					tokens.add(new Token(st.lineno(), "<", "T_OP_REL_LESSTHAN"));
					System.out.println(
							"LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_OP_REL_LESSTHAN");
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
					tokens.add(new Token(st.lineno(), ">=", "T_OP_REL_GREATERTHAN_EQUAL"));
					System.out.println(
							"LINE:\t" + st.lineno() + "\tLEXEME:\t" + ">=" + "\tTOKEN:\tT_OP_REL_GREATERTHAN_EQUAL");
				} else {
					tokens.add(new Token(st.lineno(), "" + tokenValue, "T_OP_REL_GREATERTHAN"));
					System.out.println(
							"LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_OP_REL_GREATERTHAN");
					st.pushBack();
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
			break;
		case TokenType.T_DEL_R_LPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_R_LPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_DEL_R_LPAREN");
			break;
		case TokenType.T_DEL_R_RPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_R_RPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_DEL_R_RPAREN");
			break;
		case TokenType.T_DEL_S_LPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_S_LPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_DEL_S_LPAREN");
			break;
		case TokenType.T_DEL_S_RPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_S_RPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_DEL_S_RPAREN");
			break;
		case TokenType.T_DEL_C_LPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_C_LPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_DEL_C_LPAREN");
			break;
		case TokenType.T_DEL_C_RPAREN:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_C_RPAREN"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_DEL_C_RPAREN");
			break;
		case TokenType.T_DEL_SEMICOLON:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_SEMICOLON"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_DEL_SEMICOLON");
			break;
		case TokenType.T_DEL_COMMA:
			tokens.add(new Token(st.lineno(), "" + tokenValue, "T_DEL_COMMA"));
			System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_DEL_COMMA");
			break;
		case TokenType.T_DEL_DOT:
			int dotLineNum = st.lineno();
			tokens.add(new Token(dotLineNum, "" + tokenValue, "T_DEL_DOT"));
			System.out.println("LINE:\t" + dotLineNum + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tT_DEL_DOT");
			break;
		default:
			if (tokenValue > 32) {
				errors.add(new Token(st.lineno(), "" + tokenValue, "ERROR"));
				System.out.println("LINE:\t" + st.lineno() + "\tLEXEME:\t" + tokenValue + "\tTOKEN:\tERROR");
			}
		}
	}

	private static void tokenizeWords(StreamTokenizer st) {
		String strValue = st.sval;
		int lineNum = st.lineno();

		String intPattern = "([0-9]+)";
		String idPattern = "([a-zA-Z]+[_a-zA-Z0-9]*)";

		if (TokenType.RESERVE_WORD.contains(strValue)) {
			tokens.add(new Token(lineNum, strValue, RESERVE_WORD_MAP.get(strValue)));
			System.out.println(
					"LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\t" + RESERVE_WORD_MAP.get(strValue));
		} else if (strValue.equals(TokenType.T_OP_AND)) {
			tokens.add(new Token(lineNum, strValue, "T_LOGICAL_AND"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\tT_LOGICAL_AND");
		} else if (strValue.equals(TokenType.T_OP_OR)) {
			tokens.add(new Token(lineNum, strValue, "T_LOGICAL_OR"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\tT_LOGICAL_OR");
		} else if (strValue.equals(TokenType.T_OP_NOT)) {
			tokens.add(new Token(lineNum, strValue, "T_LOGICAL_NOT"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\tT_LOGICAL_NOT");
		} else if (Pattern.matches(idPattern, strValue)) {
			tokens.add(new Token(lineNum, strValue, "T_IDENTIFIER"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\tT_IDENTIFIER");
		} else if (Pattern.matches(intPattern, strValue)) {
			try {
				String iString = strValue;
				char dotToken = (char) st.nextToken();
				for (int index = 0; index < strValue.length() - 1; index++) {
					char c = strValue.charAt(index);
					if (c == '0') {
						tokens.add(new Token(lineNum, "" + c, "T_INTEGER"));
						System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + "" + c + "\tTOKEN:\tT_INTEGER");
						iString = strValue.substring(index + 1, strValue.length());
					} else {
						iString = strValue.substring(index, strValue.length());
						break;
					}
				}
				// System.out.println("iString\t" + iString);
				strValue = iString;
				if (dotToken == '.') {
					if (st.nextToken() == StreamTokenizer.TT_WORD) {
						String nextStr = st.sval;
						if (nextStr.matches("([a-zA-Z][_a-zA-Z0-9]*)")) {
							st.pushBack();
							errors.add(new Token(lineNum, strValue + dotToken, "ERROR"));
							System.out.println(
									"LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + dotToken + "\tTOKEN:\tERROR");
						} else if (Pattern.matches(intPattern, nextStr)) {

							int nIndex = nextStr.length() - 1;
							while (nIndex > 0) {
								char c = nextStr.charAt(nIndex);
								if (c != '0') {
									break;
								} else {
									nIndex--;
								}
							}
							// if there are no 0's eg: 0.001 -> ending with a
							// integer[1-9]
							if (nIndex + 1 == nextStr.length()) {
								String fraction = nextStr.substring(0, nIndex + 1);
								String floatVal = strValue + dotToken + fraction;
								tokens.add(new Token(lineNum, floatVal, "T_FLOAT"));
								System.out
										.println("LINE:\t" + lineNum + "\tLEXEME:\t" + floatVal + "\tTOKEN:\tT_FLOAT");
							} else {
								String fraction = nextStr.substring(0, nIndex + 1);
								String intStr = nextStr.substring(nIndex + 1, nextStr.length());
								// System.out.println("fraction\t" + fraction);
								// System.out.println("intStr\t" + intStr);
								String floatVal = strValue + dotToken + fraction;
								tokens.add(new Token(lineNum, floatVal, "T_FLOAT"));
								System.out
										.println("LINE:\t" + lineNum + "\tLEXEME:\t" + floatVal + "\tTOKEN:\tT_FLOAT");
								for (int index = 0; index < intStr.length(); index++) {
									char c = intStr.charAt(index);
									if (c == '0') {
										// eg 0.0010.0010.0001 -> 0.001
										// to parse 0.001
										if (index == intStr.length() - 1) {
											tokenizeFractions("" + c, st, lineNum);
										} else {
											tokens.add(new Token(lineNum, "" + c, "T_INTEGER"));
											System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + "" + c
													+ "\tTOKEN:\tT_INTEGER");
										}
									} else {
										System.out.println("SOMETHING IS NOT GOOD...!! CALL 911");
										break;
									}
								}
							}
						} else {
							// split to numbers and identifiers
							int index = splitStrTokens(nextStr);
							String numString = nextStr.substring(0, index);
							String idString = nextStr.substring(index, nextStr.length());
							int nIndex = numString.length() - 1;
							while (nIndex > 0) {
								char c = numString.charAt(nIndex);
								if (c != '0') {
									break;
								} else {
									nIndex--;
								}
							}
							// if there are no 0's eg: 0.001 -> ending with a
							// integer[1-9]
							if (nIndex + 1 == numString.length()) {
								String fraction = numString.substring(0, nIndex + 1);
								String floatVal = strValue + dotToken + fraction;
								// [0-9]+._A
								if (nIndex >= 0) {
									tokens.add(new Token(lineNum, floatVal, "T_FLOAT"));
									System.out.println(
											"LINE:\t" + lineNum + "\tLEXEME:\t" + floatVal + "\tTOKEN:\tT_FLOAT");
								} else {
									errors.add(new Token(lineNum, floatVal, "ERROR"));
									System.out.println(
											"LINE:\t" + lineNum + "\tLEXEME:\t" + floatVal + "\tTOKEN:\tERROR");
								}
							} else {
								String fraction = numString.substring(0, nIndex + 1);
								String intStr = numString.substring(nIndex + 1, numString.length());
								// System.out.println("fraction\t" + fraction);
								// System.out.println("intStr\t" + intStr);
								String floatVal = strValue + dotToken + fraction;
								tokens.add(new Token(lineNum, floatVal, "T_FLOAT"));
								System.out
										.println("LINE:\t" + lineNum + "\tLEXEME:\t" + floatVal + "\tTOKEN:\tT_FLOAT");
								for (int index_0 = 0; index_0 < intStr.length(); index_0++) {
									char c = intStr.charAt(index_0);
									if (c == '0') {
										tokens.add(new Token(lineNum, "" + c, "T_INTEGER"));
										System.out.println(
												"LINE:\t" + lineNum + "\tLEXEME:\t" + "" + c + "\tTOKEN:\tT_INTEGER");
									} else {
										System.out.println("SOMETHING IS NOT GOOD...!! CALL 911");
										break;
									}
								}
							}
							if (TokenType.RESERVE_WORD.contains(idString)) {
								tokens.add(new Token(lineNum, idString, RESERVE_WORD_MAP.get(idString)));
								System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + idString + "\tTOKEN:\t"
										+ RESERVE_WORD_MAP.get(idString));
							} else if (Pattern.matches(idPattern, idString)) {
								tokens.add(new Token(lineNum, idString, "T_IDENTIFIER"));
								System.out.println(
										"LINE:\t" + lineNum + "\tLEXEME:\t" + idString + "\tTOKEN:\tT_IDENTIFIER");
							} else if (idString.startsWith("_")) {
								int _index = 1;
								String _String = idString.substring(0, _index);
								String idString2 = idString.substring(_index, idString.length());
								errors.add(new Token(lineNum, _String, "T_ERROR"));
								System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + _String + "\tTOKEN:\tT_ERROR");
								tokenizeIds(lineNum, idString2, st);
								// tokens.add(new Token(lineNum, idString2,
								// "T_IDENTIFIER"));
								// System.out.println(
								// "LINE:\t" + lineNum + "\tLEXEME:\t" +
								// idString2 + "\tTOKEN:\tT_IDENTIFIER");
							}
						}
					} else {
						st.pushBack();
						String errorStr = strValue + dotToken;
						errors.add(new Token(lineNum, errorStr, "ERROR"));
						System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + errorStr + "\tTOKEN:\tERROR");
					}
				} else {
					st.pushBack();
					tokens.add(new Token(lineNum, strValue, "T_INTEGER"));
					System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\tT_INTEGER");
				}
			} catch (IOException e) {
				st.pushBack();
				e.printStackTrace();
			}
		} else if (strValue.startsWith("_")) {
			int index = 1;
			String _String = strValue.substring(0, index);
			String idString = strValue.substring(index, strValue.length());
			errors.add(new Token(lineNum, _String, "T_ERROR"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + _String + "\tTOKEN:\tT_ERROR");
			tokenizeIds(lineNum, idString, st);
		} else {
			int index = splitStrTokens(strValue);
			String intString = strValue.substring(0, index);
			String idString = strValue.substring(index, strValue.length());
			tokens.add(new Token(lineNum, intString, "T_INTEGER"));
			System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + intString + "\tTOKEN:\tT_INTEGER");
			if (idString.startsWith("_")) {
				int _index = 1;
				String _String = idString.substring(0, _index);
				String idString2 = idString.substring(_index, idString.length());
				errors.add(new Token(lineNum, _String, "T_ERROR"));
				System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + _String + "\tTOKEN:\tT_ERROR");
				tokenizeIds(lineNum, idString2, st);
			} else if (TokenType.RESERVE_WORD.contains(strValue)) {
				tokens.add(new Token(lineNum, strValue, RESERVE_WORD_MAP.get(strValue)));
				System.out.println(
						"LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\t" + RESERVE_WORD_MAP.get(strValue));
			} else {
				tokens.add(new Token(lineNum, idString, "T_IDENTIFIER"));
				System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + idString + "\tTOKEN:\tT_IDENTIFIER");
			}
		}
	}

	private static void tokenizeIds(int lineNum, String strValue, StreamTokenizer st) {
		String intPattern = "([0-9]+)";
		String idPattern = "([a-zA-Z]+[_a-zA-Z0-9]*)";
		if (strValue.length() > 1) {
			if (TokenType.RESERVE_WORD.contains(strValue)) {
				tokens.add(new Token(lineNum, strValue, RESERVE_WORD_MAP.get(strValue)));
				System.out.println(
						"LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\t" + RESERVE_WORD_MAP.get(strValue));
			} else if (strValue.equals(TokenType.T_OP_AND)) {
				tokens.add(new Token(lineNum, strValue, "T_LOGICAL_AND"));
				System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\tT_LOGICAL_AND");
			} else if (strValue.equals(TokenType.T_OP_OR)) {
				tokens.add(new Token(lineNum, strValue, "T_LOGICAL_OR"));
				System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\tT_LOGICAL_OR");
			} else if (strValue.equals(TokenType.T_OP_NOT)) {
				tokens.add(new Token(lineNum, strValue, "T_LOGICAL_NOT"));
				System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\tT_LOGICAL_NOT");
			} else if (Pattern.matches(idPattern, strValue)) {
				tokens.add(new Token(lineNum, strValue, "T_IDENTIFIER"));
				System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\tT_IDENTIFIER");
			} else if (Pattern.matches(intPattern, strValue)) {
				for (int index = 0; index < strValue.length(); index++) {
					char c = strValue.charAt(index);
					if (c == '0') {
						tokens.add(new Token(lineNum, "" + c, "T_INTEGER"));
						System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + "" + c + "\tTOKEN:\tT_INTEGER");
					} else {
						String intStr = strValue.substring(index, strValue.length());
						tokens.add(new Token(lineNum, "" + c, "T_INTEGER"));
						System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + intStr + "\tTOKEN:\tT_INTEGER");
						break;
					}
				}
			} else {
				int index = splitStrTokens(strValue);
				String intString = strValue.substring(0, index);
				String idString = strValue.substring(index, strValue.length());
				tokens.add(new Token(lineNum, intString, "T_INTEGER"));
				System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + intString + "\tTOKEN:\tT_INTEGER");
				if (TokenType.RESERVE_WORD.contains(strValue)) {
					tokens.add(new Token(lineNum, strValue, RESERVE_WORD_MAP.get(strValue)));
					System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + strValue + "\tTOKEN:\t"
							+ RESERVE_WORD_MAP.get(strValue));
				} else {
					tokens.add(new Token(lineNum, idString, "T_IDENTIFIER"));
					System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + idString + "\tTOKEN:\tT_IDENTIFIER");
				}
			}
		}

	}

	private static void tokenizeFractions(String string_0, StreamTokenizer st, int lineNum) {
		String intPattern = "([0-9]+)";
		String idPattern = "([a-zA-Z]+[_a-zA-Z0-9]*)";
		String numStrPattern = "([0-9][_a-zA-Z0-9]*)";

		try {
			char dotToken = (char) st.nextToken();
			if (dotToken == '.') {
				if (st.nextToken() == StreamTokenizer.TT_WORD) {
					String numStr = st.sval;
					if (Pattern.matches(intPattern, numStr)) {
						int nIndex = numStr.length() - 1;
						while (nIndex > 0) {
							char c = numStr.charAt(nIndex);
							if (c != '0') {
								break;
							} else {
								nIndex--;
							}
						}
						// if there are no 0's eg: 0.001 -> ending with a
						// integer[1-9]
						if (nIndex + 1 == numStr.length()) {
							String fraction = numStr.substring(0, nIndex + 1);
							String floatVal = string_0 + dotToken + fraction;
							// [0-9]+._A
							if (nIndex >= 0) {
								tokens.add(new Token(lineNum, floatVal, "T_FLOAT"));
								System.out
										.println("LINE:\t" + lineNum + "\tLEXEME:\t" + floatVal + "\tTOKEN:\tT_FLOAT");
							} else {
								errors.add(new Token(lineNum, floatVal, "ERROR"));
								System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + floatVal + "\tTOKEN:\tERROR");
							}
						} else {
							String fraction = numStr.substring(0, nIndex + 1);
							String intStr = numStr.substring(nIndex + 1, numStr.length());
							// System.out.println("fraction\t" + fraction);
							// System.out.println("intStr\t" + intStr);
							String floatVal = string_0 + dotToken + fraction;
							tokens.add(new Token(lineNum, floatVal, "T_FLOAT"));
							System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + floatVal + "\tTOKEN:\tT_FLOAT");
							for (int index = 0; index < intStr.length(); index++) {
								char c = intStr.charAt(index);
								if (c == '0') {
									// eg 0.0010.0010.0001 -> 0.001
									// to parse 0.001
									if (index == intStr.length() - 1) {
										tokenizeFractions("" + c, st, lineNum);
										break;
									} else {
										tokens.add(new Token(lineNum, "" + c, "T_INTEGER"));
										System.out.println(
												"LINE:\t" + lineNum + "\tLEXEME:\t" + "" + c + "\tTOKEN:\tT_INTEGER");
									}
								} else {
									System.out.println("SOMETHING IS NOT GOOD...!! CALL 911");
									break;
								}
							}
						}
					} else if (Pattern.matches(numStrPattern, numStr)) {
						// split to numbers and identifiers
						int index = splitStrTokens(numStr);
						String numString = numStr.substring(0, index);
						String idString = numStr.substring(index, numStr.length());
						int nIndex = numString.length() - 1;
						while (nIndex > 0) {
							char c = numString.charAt(nIndex);
							if (c != '0') {
								break;
							} else {
								nIndex--;
							}
						}
						// if there are no 0's eg: 0.001 -> ending with a
						// integer[1-9]
						if (nIndex + 1 == numString.length()) {
							String fraction = numString.substring(0, nIndex + 1);
							String floatVal = string_0 + dotToken + fraction;
							tokens.add(new Token(lineNum, floatVal, "T_FLOAT"));
							System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + floatVal + "\tTOKEN:\tT_FLOAT");
						} else {
							String fraction = numString.substring(0, nIndex + 1);
							String intStr = numString.substring(nIndex + 1, numString.length());
							// System.out.println("fraction\t" + fraction);
							// System.out.println("intStr\t" + intStr);
							String floatVal = string_0 + dotToken + fraction;
							tokens.add(new Token(lineNum, floatVal, "T_FLOAT"));
							System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + floatVal + "\tTOKEN:\tT_FLOAT");
							for (int index_0 = 0; index_0 < intStr.length(); index_0++) {
								char c = intStr.charAt(index_0);
								if (c == '0') {
									tokens.add(new Token(lineNum, "" + c, "T_INTEGER"));
									System.out.println(
											"LINE:\t" + lineNum + "\tLEXEME:\t" + "" + c + "\tTOKEN:\tT_INTEGER");
								} else {
									System.out.println("SOMETHING IS NOT GOOD...!! CALL 911");
									break;
								}
							}
						}
						if (TokenType.RESERVE_WORD.contains(idString)) {
							tokens.add(new Token(lineNum, idString, RESERVE_WORD_MAP.get(idString)));
							System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + idString + "\tTOKEN:\t"
									+ RESERVE_WORD_MAP.get(idString));
						} else if (Pattern.matches(idPattern, idString)) {
							tokens.add(new Token(lineNum, idString, "T_IDENTIFIER"));
							System.out
									.println("LINE:\t" + lineNum + "\tLEXEME:\t" + idString + "\tTOKEN:\tT_IDENTIFIER");
						} else if (idString.startsWith("_")) {
							int _index = 1;
							String _String = idString.substring(0, _index);
							String idString2 = idString.substring(_index, idString.length());
							errors.add(new Token(lineNum, _String, "T_ERROR"));
							System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + _String + "\tTOKEN:\tT_ERROR");
							tokenizeIds(lineNum, idString2, st);
							// tokens.add(new Token(lineNum, idString2,
							// "T_IDENTIFIER"));
							// System.out.println(
							// "LINE:\t" + lineNum + "\tLEXEME:\t" + idString2 +
							// "\tTOKEN:\tT_IDENTIFIER");
						}
					} else {
						errors.add(new Token(lineNum, string_0 + dotToken, "T_ERROR"));
						System.out.println(
								"LINE:\t" + lineNum + "\tLEXEME:\t" + string_0 + dotToken + "\tTOKEN:\tT_ERROR");
						st.pushBack();
					}
				} else {
					errors.add(new Token(lineNum, string_0 + dotToken, "T_ERROR"));
					System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + string_0 + dotToken + "\tTOKEN:\tT_ERROR");
					st.pushBack();
				}
			} else {
				tokens.add(new Token(lineNum, string_0, "T_INTEGER"));
				System.out.println("LINE:\t" + lineNum + "\tLEXEME:\t" + string_0 + "\tTOKEN:\tT_INTEGER");
				st.pushBack();
			}
		} catch (

		IOException e)

		{
			st.pushBack();
			e.printStackTrace();
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
