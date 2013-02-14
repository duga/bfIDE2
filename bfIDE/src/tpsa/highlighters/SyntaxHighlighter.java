package tpsa.highlighters;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import tpsa.BrainFuckIDE;

/**
 * Wyróżnia kod od komentarzy
 * 
 * @author Duga Eye
 * 
 */
public class SyntaxHighlighter implements DocumentListener {

	public static ArrayList<Character> BFcharacterList = new ArrayList<Character>(
			Arrays.asList(new Character[] { '>', '<', '+', '-', '.', ',', '[',
					']', 'b' }));

	final ArrayList<StyleHighlight> list = new ArrayList<StyleHighlight>();

	public SyntaxHighlighter() {
	}

	private void addToTheList(StyledDocument doc, final int offset,
			Style comment, int from, final ArrayList<StyleHighlight> list,
			int position) {
		int length;

		length = position - from;
		if (length <= 0)
			return;
		synchronized (list) {
			list.add(new StyleHighlight(doc, offset + from, length, comment));
		}

	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// update(e);

	}

	private void checkForBugs(StyledDocument doc) {
		try {
			String s = doc.getText(0, doc.getLength());
			Style errorStyle = doc.getStyle("ERROR");

			int indentation = 0;
			int idx = 0;
			while (idx < s.length()) {
				char c = 0;
				do {
					c = s.charAt(idx++);
				} while (c != '[' && c != ']' && idx < s.length());
				switch (c) {
				case '[':
					indentation++;
					break;
				case ']':
					if (--indentation < 0) {
						indentation = 0;
						list.add(new StyleHighlight(doc, idx - 1, 1, errorStyle));
					}
				}

			}

			updateHighlights();

		} catch (BadLocationException excp) {
			BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		update(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		update(e);

	}

	private void setStyleComment(StyledDocument doc, final int offset,
			String s, Style comment) {

		BrainFuckIDE.lg.finest("Text: '" + s + "'");
		Style sCode = doc.getStyle("CODE");
		int state = 0;
		int from = 0;
		synchronized (sCode) {
			list.clear();
		}
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (state) {
			case 0: // seria kodu
				if (!BFcharacterList.contains(c)) {
					addToTheList(doc, offset, sCode, from, list, i);
					from = i;
					state = 1;
				}

				break;
			case 1: // seria komentarzy
				if (BFcharacterList.contains(c)) {
					addToTheList(doc, offset, comment, from, list, i);
					from = i;
					state = 0;
				}
				break;
			}
		}

		if (state == 1) {
			addToTheList(doc, offset, comment, from, list, s.length());
		} else {
			addToTheList(doc, offset, sCode, from, list, s.length());
		}

	}

	public void update(StyledDocument doc, int offset, int length,
			boolean machine) {
		Style comment = doc.getStyle("COMMENT");
		String s;
		try {
			BrainFuckIDE.lg.finer("Change to analyse: " + offset + ", "
					+ length + ", length: " + doc.getLength());

			s = doc.getText(offset, Math.min(length, doc.getLength() - offset));

			setStyleComment(doc, offset, s, comment);

			checkForBugs(doc);
			checkBreakpoints(doc);

			updateHighlights();

		} catch (BadLocationException excp) {
			BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
		}
	}

	private void checkBreakpoints(StyledDocument doc) {
		/*
		 * try { BrainFuckIDE.lg.finer("Check breapoints"); String s =
		 * doc.getText(0, doc.getLength()); int pos = -1; Style sBreakpoint =
		 * doc.getStyle("BREAKPOINT"); while ((pos = s.indexOf('b', pos + 1)) >=
		 * 0) { BrainFuckIDE.lg.finer("Breakpoint pos: " + pos); int i; for (i =
		 * 0; i < s.length() - pos && s.charAt(i + pos) != 'b'; i++) ;
		 * 
		 * synchronized (list) { list.add(new StyleHighlight(doc, pos, i - 1,
		 * sBreakpoint)); } pos += i; }
		 * 
		 * } catch (BadLocationException excp) { }
		 */
	}

	private void update(DocumentEvent e) {
		update((StyledDocument) e.getDocument(), e.getOffset(), e.getLength(),
				false);

	}

	private void updateHighlights() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				synchronized (list) {
					for (StyleHighlight h : list)
						h.getDoc().setCharacterAttributes(h.getOffset(),
								h.getLength(), h.getStyle(), true);
					list.clear();
				}

			}
		});
	}
}
