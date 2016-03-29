package compiler.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

class FormatLog extends Formatter {
	public String format(LogRecord logRecord) {
		return "[" + logRecord.getLevel() + "]" + logRecord.getMessage() + "\n";
		// return logRecord.getMessage();
	}
}

class HtmlFormatLog extends Formatter {
	public String format(LogRecord logRecord) {
		// return "[" + logRecord.getLevel() + "]" + logRecord.getMessage() +
		// "\n";
		return logRecord.getMessage();
	}
}

public class PrintUtil {

	public enum LOGTYPE {
		LEXER, SYNTAX, SEMATICS, HTML
	};

	public static boolean isLog = false;

	/**
	 * @param logger
	 * 
	 */
	public static Logger setLogger(String logFileName) {
		String logFile = System.getProperty("user.dir") + "\\logs\\"
				+ logFileName;
		Logger logger = Logger.getLogger(logFile);
		if (logger.getHandlers().length == 0) {
			FileHandler fileHandler = null;
			try {
				fileHandler = new FileHandler(logFile, false);
				SimpleFormatter textFormatter = new SimpleFormatter();
				// fileHandler.setFormatter(textFormatter);
				if (logFileName.equals("SymbolTables.html")) {
					fileHandler.setFormatter(new HtmlFormatLog());
				} else {
					fileHandler.setFormatter(new FormatLog());
				}
				// fileHandler.setFormatter(new FormatLog());
				logger.addHandler(fileHandler);
				logger.setUseParentHandlers(false);
			} catch (SecurityException | IOException e) {
				System.out
						.println("Logger initialization error. Check File Permissions!!!");
				e.printStackTrace();
			} finally {
			}
		}
		return logger;
	}

	private static String getLogMsg(String msg, LOGTYPE logType) {
		if (logType == LOGTYPE.LEXER) {
			msg = "[LEXER]" + msg;
		} else if (logType == LOGTYPE.SYNTAX) {
			msg = "[SYNTAX]" + msg;
		} else if (logType == LOGTYPE.SEMATICS) {
			msg = "[SEMANTICS]" + msg;
		} else {
			// msg = "[COMPILER]" + msg;
		}
		return msg;
	}

	public static void print(Logger logger, LOGTYPE logType, String msg) {
		if (isLog) {
			msg = getLogMsg(msg, logType);
			logger.info(msg);
		}
	}

	public static void info(Logger logger, LOGTYPE logType, String msg) {
		if (isLog) {
			msg = getLogMsg(msg, logType);
			System.out.println(msg);
			logger.info(msg);
		}
	}

	public static void error(Logger logger, LOGTYPE logType, String msg) {
		if (isLog) {
			msg = getLogMsg(msg, logType);
			System.out.println(msg);
			logger.severe(msg);
		}
	}

	public static void warning(Logger logger, LOGTYPE logType, String msg) {
		if (isLog) {
			msg = getLogMsg(msg, logType);
			System.out.println(msg);
			logger.warning(msg);
		}
	}

	public static String htmlStart = "<html><head><title>SYMBOL TABLES</title></head><body><header><h1>SYMBOL TABLES</h1></header><p>";
	public static String htmlEnd = "</p></body></html>";

	public static String h2O = "<h2>";
	public static String h2C = "</h2>";

	public static String tableO = "<table border = \"1\"><tr><th>NAME</th><th>TYPE</th><th>KIND</th><th>STRUCTURE</th><th>ARRAY DIMENSIONS</th><th>NO OF PARAMS</th><th>ADDRESS</th>"
			+ "<th>IS DUPLICATE</th><th>IS ARRAY</th><th>IS DATA TYPE VALID</th></tr>";
	public static String tableC = "</table>";

	public static String trO = "<tr>";
	public static String trC = "</tr>";

	public static String tdO = "<td>";
	public static String tdC = "<\td>";

}
