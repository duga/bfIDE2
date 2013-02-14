package tpsa.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import tpsa.BFInterpreter;

/**
 * Klasa strumień wyjściowy do JTextPane dla BF maszyny
 * 
 * @author Duga Eye
 * 
 */
public class JTextPaneOutputStream extends OutputStream {

	private Logger lg = Logger.getLogger("BF");
	private JTextPane pane;

	public JTextPaneOutputStream() {
	}

	public JTextPane getPane() {
		return pane;
	}

	public void setPane(JTextPane pane) {
		this.pane = pane;
	}

	@Override
	public void write(int b) throws IOException {
		lg.finest("Writing byte :" + b);
		StyledDocument doc = (StyledDocument) pane.getDocument();
		char c = (char) b;
		String s = "" + c;
		Style style = doc.getStyle("OUTPUT");
		try {
			doc.insertString(doc.getLength(), s, style);
		} catch (BadLocationException excp) {
			BFInterpreter.lg.severe(BFInterpreter.ErrorsFormatter(excp));
		}

	}

}
