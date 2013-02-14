package tpsa.highlighters;

import java.awt.Color;
import java.util.Locale;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;

import tpsa.BrainFuckIDE;

public class WordsHighlighter extends DefaultHighlighter implements
		DocumentListener {

	/**
	 * 
	 */
	private Long last = 0L;
	/**
	 * Words to highlight
	 * Words to highlight TODO not implemented
	 */
	private String[] words;

	/**
	 * @param word
	 */
	public WordsHighlighter(String[] words) {
		// FIXME shallow copy
		this.words = words.clone();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateFrequent(e);
		updateNotToFrequent(e);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateFrequent(e);
		updateNotToFrequent(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateFrequent(e);
		updateNotToFrequent(e);
	}

	/**
	 * @param s
	 * @param start
	 * @param length
	 *            TODO
	 * @throws BadLocationException
	 */
	private void searchForOccurance(String s, int start, int length)
			throws BadLocationException {
		DefaultHighlightPainter todoHighLighter = new DefaultHighlightPainter(
				Color.LIGHT_GRAY);

		int pos = -1;

		String copy = new String(s).toUpperCase(Locale.forLanguageTag("pl_PL"));
		for (int i = 0; i < words.length; i++) {
			pos = -1;
			while ((pos = copy.indexOf(words[i], pos + 1)) >= 0) {
				int endOfHighlighting = Math.min(pos + words[i].length()
						+ start, length - 1);
				addHighlight(pos + start, endOfHighlighting,
						todoHighLighter);
			}
		}
	}

	/**
	 * @param e
	 */
	private void update(DocumentEvent e) {
		try {
			Document doc = e.getDocument();
			String s = doc.getText(0, doc.getLength());
			removeAllHighlights();

			searchForOccurance(s, 0, doc.getLength());

		} catch (BadLocationException excp) {

			BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
		}

	}

	/**
	 * @param e
	 */
	private void updateFrequent(DocumentEvent e) {
		int offset = e.getOffset();
		int length = e.getLength();
		Document doc = e.getDocument();

		try {
			int start = Math.max(0, offset - 10);
			int end = Math.min(doc.getLength() - start, length + 20);
			String s = doc.getText(start, end);
			Highlight[] highlights = getHighlights();
			for (int i = 0; i < highlights.length; i++)
				if (highlights[i].getStartOffset() >= offset
						&& highlights[i].getEndOffset() <= offset + length)
					removeHighlight(highlights[i]);

			searchForOccurance(s, start, doc.getLength());

		} catch (BadLocationException excp) {
			BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
		}

	}

	/**
	 * @param e
	 */
	private void updateNotToFrequent(DocumentEvent e) {
		long now = System.nanoTime();
		long diff = now - last;
		if (diff > 2e9) {
			last = now;
			update(e);
		}
	}
}
