package tpsa;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class BFInterpreter {

	static LogManager lm;
	public static Logger lg;
	static Level lv = Level.OFF;

	static {
		lm = LogManager.getLogManager();
		lm.addLogger(Logger.getLogger("BFInterpreter"));
		lg = lm.getLogger("BFInterpreter");
		lg.setLevel(lv);
		for (Handler h : BFInterpreter.lg.getHandlers())
			h.setLevel(lv);

		for (Handler h : Logger.getLogger("").getHandlers())
			h.setLevel(lv);

		for (Handler h : Logger.getLogger("").getHandlers())
			h.setLevel(lv);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Thread t = new Thread(
				new BFMachine(
						"+[,+[-[>+>+<<-]>[<+>-]+>>++++++++[<-------->-]<-[<[-]>>>+[<+<+>>-]<[>+<-]<[<++>\n"
								+ ">>+[<+<->>-]<[>+<-]]>[<]<]>>[-]<<<[[-]<[>>+>+<<<-]>>[<<+>>-]>>++++++++[<-------\n"
								+ "->-]<->>++++[<++++++++>-]<-<[>>>+<<[>+>[-]<<-]>[<+>-]>[<<<<<+>>>>++++[<++++++++\n"
								+ ">-]>-]<<-<-]>[<<<<[-]>>>>[<<<<->>>>-]]<<++++[<<++++++++>>-]<<-[>>+>+<<<-]>>[<<+\n"
								+ ">>-]+>>+++++[<----->-]<-[<[-]>>>+[<+<->>-]<[>+<-]<[<++>>>+[<+<+>>-]<[>+<-]]>[<]\n"
								+ "<]>>[-]<<<[[-]<<[>>+>+<<<-]>>[<<+>>-]+>------------[<[-]>>>+[<+<->>-]<[>+<-]<[<\n"
								+ "++>>>+[<+<+>>-]<[>+<-]]>[<]<]>>[-]<<<<<------------->>[[-]+++++[<<+++++>>-]<<+>\n"
								+ ">]<[>++++[<<++++++++>>-]<-]>]<[-]++++++++[<++++++++>-]<+>]<.[-]+>>+<]>[[-]<]<]"));
		try {
			t.start();
			t.join();
		} catch (InterruptedException e) {
			BFInterpreter.lg.severe(BFInterpreter.ErrorsFormatter(e));
		}

	}

	public static String ErrorsFormatter(Exception excp) {
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
