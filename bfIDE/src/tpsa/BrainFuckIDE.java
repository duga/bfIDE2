package tpsa;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.UIManager;

public class BrainFuckIDE {

	static public Logger lg;
	// protected static final long milsToWaitBeforeKill = 60000;
	static private LogManager lm;
	static private Level lv = Level.OFF;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			UIManager
					.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception excp) {
			// TODO Auto-generated catch block
			BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
		}
		BrainFuckIDE.lm = LogManager.getLogManager();
		BrainFuckIDE.lm.addLogger(Logger.getLogger("BFIDE"));
		BrainFuckIDE.lg = lm.getLogger("BFIDE");
		BrainFuckIDE.lg.setLevel(lv);
		for (Handler h : BrainFuckIDE.lg.getHandlers())
			h.setLevel(lv);

		for (Handler h : Logger.getLogger("").getHandlers())
			h.setLevel(lv);

		BrainFuckIDE.lg.info("Brainfuck IDE is starting");

		MainFrame mf = new MainFrame();
		mf.setVisible(true);
	}

	/**
	 * @param excp
	 *            Wyjątek ;-)
	 * @return stringi opisujące ten wyjątek w strawnej postaci
	 */

	public static String ErrorsFormatter(Exception excp) {

		// System.setOut(null);
		// System.setErr(null);

		StringBuilder sb = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		sb.append("Exception catched: " + excp.getLocalizedMessage() + newLine);
		StackTraceElement[] stack = excp.getStackTrace();
		for (int i = 0; i < stack.length; i++) {
			sb.append("StackElement(" + i + ") : " + stack[i].getClassName()
					+ ", " + stack[i].getFileName() + ":"
					+ stack[i].getLineNumber() + ", "
					+ stack[i].getMethodName() + newLine);
		}

		return sb.toString();
	}

}
