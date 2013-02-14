package tpsa.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import tpsa.BFInterpreter;

/**
 * Klasa dająca strumień wejściowy z JTextPane dla BF maszyny
 * 
 * @author Duga Eye
 * 
 */
public class JTextPaneInputStream extends InputStream implements
		DocumentListener {

	private boolean eof = false;
	private Logger lg = Logger.getLogger("BF");
	private Semaphore sem = new Semaphore(0);
	private StringBuilder string = new StringBuilder();

	public JTextPaneInputStream() {
	}

	public void reset() {
		eof = false;
		string.replace(0, string.length(), "");
		try {
			if (sem.availablePermits() > 0)
				sem.acquire(sem.availablePermits());
		} catch (InterruptedException excp) {

		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {

	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		try {
			lg.info("Strumień (handler) otrzymał nowe powiadomienie o wprowadzonych danych");
			String s = e.getDocument().getText(e.getOffset(), e.getLength());
			string.append(s);
			sem.release(s.length());
			lg.info("Wartość semafora: " + sem.availablePermits());
			lg.info("Strumień dodał parę bajtów do bufora");

			final StyledDocument doc = (StyledDocument) e.getDocument();
			final Style set = doc.getStyle("INPUT");
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					doc.setParagraphAttributes(e.getOffset(), e.getLength(),
							set, false);

				}
			});

		} catch (BadLocationException excp) {
			BFInterpreter.lg.severe(BFInterpreter.ErrorsFormatter(excp));
		}
	}

	@Override
	public int read() throws IOException {
		if (eof == true) {
			return -1;
		}

		try {
			lg.info("Strumień ma dać jeden bajt");
			sem.acquire();
			lg.info("Strumień uzyskał bajt na semaforze");
			String c = string.substring(0, 1);
			char c2 = c.charAt(0);
			Character c3 = '\u001a';
			if (c3.equals(c2)) {
				lg.info("ERROR SUCCESS");
				eof = true;
				return -1;
			}
			string.delete(0, 1);
			lg.info("Strumień usunął wczytany bajt z bufora");
			return c2;
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {

	}

}
