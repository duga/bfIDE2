package tpsa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import tpsa.highlighters.SyntaxHighlighter;
import tpsa.streams.JTextPaneInputStream;
import tpsa.streams.JTextPaneOutputStream;
import tv.porst.jhexview.JHexView;
import tv.porst.jhexview.JHexView.DefinitionStatus;
import tv.porst.jhexview.SimpleDataProvider;

/**
 * Zakładka tego ide. Każda zakładka posiada swoją maszynę
 * 
 * @author Duga Eye
 * 
 */
public class BFTab extends JPanel implements CaretListener, ComponentListener,
		ActionListener, ContainerListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3256469085182527862L;
	protected static final double startSplitterPosition = 0.5;
	/**
	 * JTextPane w którym umieszczamy kod BF
	 */
	private JTextPane codePanel;
	private File file;
	private Style foundStyle;
	private SyntaxHighlighter high;
	private String lastCodeWorking;
	private BFMachine machine;
	private JScrollPane pane;
	private JTextPane paneResult;
	/**
	 * Label, który pokazuje aktualną pozycję w tekście
	 */
	private JLabel positionLabel;
	private TextLineNumber rows;
	private JScrollPane scrollPaneResult;
	private JPanel status;
	private JLabel statusLabel;
	private JTextPane memoryPanel = new JTextPane();
	private JSplitPane splitPane1;
	private Timer t;
	private JHexView editor;

	/**
	 * @param undoListener
	 *            undo/redo listener
	 * @param pauseAction
	 *            pause akcja
	 * @param resumeAction
	 *            resume akcja
	 * @param stepAction
	 *            step akcja
	 * @param stopAction
	 *            stop akcja
	 * @param runAction
	 *            run akcja
	 * @param undoAction
	 *            undo akcja
	 * @param redoAction
	 *            redo akcja
	 */
	public BFTab(UndoableEditListener undoListener, Action pauseAction,
			Action resumeAction, Action stepAction, Action stopAction,
			Action runAction, Action undoAction, Action redoAction) {

		t = new Timer(2000, this);

		lastCodeWorking = new String();
		codePanel = new JTextPane();
		pane = new JScrollPane(codePanel);
		rows = new TextLineNumber(codePanel);

		status = new JPanel();
		status.setLayout(new BorderLayout());

		statusLabel = new JLabel("Ready");
		positionLabel = new JLabel("0:0");
		status.add(statusLabel, BorderLayout.WEST);
		status.add(positionLabel, BorderLayout.EAST);

		StyledDocument doc = (StyledDocument) codePanel.getDocument();

		doc.addDocumentListener(rows);
		doc.addUndoableEditListener(undoListener);

		codePanel.addCaretListener(this);

		pane.setRowHeaderView(rows);

		pane.setFocusable(true);

		KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				Event.CTRL_MASK);
		KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				Event.CTRL_MASK);

		codePanel.getInputMap().put(undoKeyStroke, "undo");
		codePanel.getInputMap().put(redoKeyStroke, "redo");
		codePanel.getActionMap().put("undo", undoAction);
		codePanel.getActionMap().put("redo", redoAction);
		KeyStroke runWithoutStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9,
				Event.CTRL_MASK);

		KeyStroke stopStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				Event.CTRL_MASK);

		codePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				runWithoutStroke, "RunWithoutDebug");
		codePanel.getActionMap().put("RunWithoutDebug", runAction);

		codePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				stopStroke, "StopAction");
		codePanel.getActionMap().put("StopAction", stopAction);

		KeyStroke stepStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F6,
				Event.CTRL_MASK);
		codePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				stepStroke, "stepAction");
		codePanel.getActionMap().put("stepAction", stepAction);

		// panel2.add(pane);

		paneResult = new JTextPane();
		KeyStroke eofStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D,
				Event.CTRL_MASK);
		paneResult.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				eofStroke, "EOF");

		KeyStroke pauseStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F5,
				Event.CTRL_MASK);
		codePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				pauseStroke, "pauseAction");
		codePanel.getActionMap().put("pauseAction", pauseAction);

		KeyStroke resumeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F7,
				Event.CTRL_MASK);
		codePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				resumeStroke, "resumeAction");
		codePanel.getActionMap().put("resumeAction", resumeAction);

		paneResult.getActionMap().put("EOF", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Document doc = paneResult.getDocument();
				String s = "\u001a";
				try {
					doc.insertString(doc.getLength(), s, null);
				} catch (BadLocationException excp) {
					BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
					BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
				}

			}
		});

		StyledDocument doc2 = (StyledDocument) paneResult.getDocument();
		Style sInput = doc2.addStyle("INPUT", null);
		StyleConstants.setForeground(sInput, Color.red);
		Style sOutput = doc2.addStyle("OUTPUT", null);
		StyleConstants.setForeground(sOutput, Color.GREEN);
		Style sComment = doc.addStyle("COMMENT", null);
		StyleConstants.setItalic(sComment, true);
		StyleConstants.setForeground(sComment, Color.BLUE);
		Style sCode = doc.addStyle("CODE", null);
		StyleConstants.setForeground(sCode, Color.BLACK);
		Style sError = doc.addStyle("ERROR", null);
		StyleConstants.setUnderline(sError, true);
		StyleConstants.setStrikeThrough(sError, true);
		StyleConstants.setForeground(sError, Color.RED);
		Style sBFCaret = doc.addStyle("BFCaret", null);
		StyleConstants.setBackground(sBFCaret, Color.RED);
		Style sBreakpoint = doc.addStyle("BREAKPOINT", null);
		StyleConstants.setBackground(sBreakpoint, Color.CYAN);

		Style empty = doc.addStyle("empty", null);
		StyleConstants.setForeground(empty, Color.YELLOW);

		Style foundStyle = doc.addStyle("found", null);
		StyleConstants.setBackground(foundStyle, Color.BLUE);

		high = new SyntaxHighlighter();
		doc.addDocumentListener(high);

		setLayout(new BorderLayout());

		scrollPaneResult = new JScrollPane(paneResult);

		scrollPaneResult.setPreferredSize(new Dimension(80, 80));

		add(status, BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
		add(scrollPaneResult, BorderLayout.SOUTH);

		/*
		 * WordsHighlighter w = new WordsHighlighter(new String[] { "BF",
		 * "TODO", "FIXME", "XXX", "WARNING", "ERROR", "CRITICAL", "DEBUG",
		 * "FUNCTION", "PROCEDURE" }); codePanel.setHighlighter(w);
		 * codePanel.getDocument().addDocumentListener(w);
		 */

		final JTextPaneInputStream is = new JTextPaneInputStream();
		final JTextPaneOutputStream os = new JTextPaneOutputStream();
		paneResult.getDocument().addDocumentListener(is);
		os.setPane(paneResult);

		machine = new BFMachine(is, os, "", BFMachine.defaultMemorySize);
		memoryPanel.setEditable(false);
		memoryPanel.setPreferredSize(new Dimension(200, 200));

		splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		editor = new JHexView();
		editor.setData(new SimpleDataProvider(machine.getMemory()));
		editor.setDefinitionStatus(DefinitionStatus.DEFINED);
		editor.setEnabled(true);

		// editor.set
		splitPane1.add(pane);
		splitPane1.add(editor);
		splitPane1.setSize(new Dimension(800, 600));

		addComponentListener(this);
		// t.start();

		add(splitPane1, BorderLayout.CENTER);
		splitPane1.setDividerLocation(BFTab.startSplitterPosition);

	}

	@Override
	public void caretUpdate(final CaretEvent e) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				JTextPane pane = (JTextPane) e.getSource();
				BrainFuckIDE.lg.fine("Caret update" + e.getDot());

				Document doc = pane.getDocument();
				try {
					String s = doc.getText(0, e.getDot());
					TextEditorPosition pos = getLineNumer(s);
					positionLabel.setText("" + pos.Line + ":" + pos.Offset);
				} catch (BadLocationException excp) {
					BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
				}

			}
		});

	}

	public JTextPane getArea() {
		return codePanel;
	}

	public File getFile() {
		return file;
	}

	public Style getFoundStyle() {
		return foundStyle;
	}

	public SyntaxHighlighter getHigh() {
		return high;
	}

	public String getLastCodeWorking() {
		return lastCodeWorking;
	}

	protected TextEditorPosition getLineNumer(String s) {
		String lineSeparator = System.getProperty("line.separator");

		int line = 1;
		int pos = -1, last = 0;

		BrainFuckIDE.lg.finer("contents: " + s);

		while ((pos = s.indexOf(lineSeparator, pos + lineSeparator.length())) >= 0) {
			BrainFuckIDE.lg.finer("found separator: " + pos);
			line++;
			last = pos + lineSeparator.length();
		}

		return new TextEditorPosition(line, s.length() - last);
	}

	public BFMachine getMachine() {
		return machine;
	}

	public JScrollPane getPane() {
		return pane;
	}

	public JTextPane getPaneResult() {
		return paneResult;
	}

	public JLabel getPositionLabel() {
		return positionLabel;
	}

	public TextLineNumber getRows() {
		return rows;
	}

	public JScrollPane getScrollPaneResult() {
		return scrollPaneResult;
	}

	public JPanel getStatus() {
		return status;
	}

	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public void setArea(JTextPane area) {
		this.codePanel = area;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setFoundStyle(Style foundStyle) {
		this.foundStyle = foundStyle;
	}

	public void setHigh(SyntaxHighlighter high) {
		this.high = high;
	}

	public void setLastCodeWorking(String lastCodeWorking) {
		this.lastCodeWorking = lastCodeWorking;
	}

	public void setMachine(BFMachine machine) {
		this.machine = machine;
	}

	public void setPane(JScrollPane pane) {
		this.pane = pane;
	}

	public void setPaneResult(JTextPane paneResult) {
		this.paneResult = paneResult;
	}

	public void setPositionLabel(JLabel positionLabel) {
		this.positionLabel = positionLabel;
	}

	public void setRows(TextLineNumber rows) {
		this.rows = rows;
	}

	public void setScrollPaneResult(JScrollPane scrollPaneResult) {
		this.scrollPaneResult = scrollPaneResult;
	}

	public void setStatus(JPanel status) {
		this.status = status;
	}

	public void setStatusLabel(JLabel statusLabel) {
		this.statusLabel = statusLabel;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// splitPane1.setDividerLocation(BFTab.startSplitterPosition);
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		BrainFuckIDE.lg.fine("Refreshing code syntax");
		StyledDocument doc = codePanel.getStyledDocument();
		high.update(doc, 0, doc.getLength(), machine.isWorking());
	}

	@Override
	public void componentAdded(ContainerEvent e) {
		if (e.getChild() == this) {
			BrainFuckIDE.lg.info("Component added");
			t.start();
		}

	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		if (e.getChild() == this) {
			machine.stop();
			e.getContainer().removeContainerListener(this);
			BrainFuckIDE.lg.info("Component removed");
			t.stop();
		}
	}

	public JHexView getEditor() {
		return editor;
	}

	public void setEditor(JHexView editor) {
		this.editor = editor;
	}

	public JTextPane getCodePanel() {
		return codePanel;
	}

	public void setCodePanel(JTextPane codePanel) {
		this.codePanel = codePanel;
	}

	public JTextPane getMemoryPanel() {
		return memoryPanel;
	}

	public void setMemoryPanel(JTextPane memoryPanel) {
		this.memoryPanel = memoryPanel;
	}

	public JSplitPane getSplitPane1() {
		return splitPane1;
	}

	public void setSplitPane1(JSplitPane splitPane1) {
		this.splitPane1 = splitPane1;
	}

	/**
	 * Pobiera timer dla syntaxu
	 * 
	 * @return timer
	 */
	public Timer getT() {
		return t;
	}

	/**
	 * @param t
	 *            Timer
	 */
	public void setT(Timer t) {
		this.t = t;
	}
}