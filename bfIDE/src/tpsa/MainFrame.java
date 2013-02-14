package tpsa;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;

import tpsa.dialogs.AboutProgramDialog;
import tpsa.dialogs.FindDialog;
import tpsa.dialogs.HelpDialog;
import tpsa.dialogs.MemorySizeDialog;
import tpsa.dialogs.ReplaceDialog;
import tpsa.dialogs.filters.BFFileFilter;
import tpsa.highlighters.SyntaxHighlighter;

// DONE liczenie klamerek (bilans)
// DONE uruchomienie ostatniej poprawnej wersji BrainFucka
// DONE zróżnicowanie podświetlenia (prawie zrobione - działa, ale więcej)
// DONE ustalenie skończonej pamięci
// DONE wyświetlanie ładnych wyjątków
// DONE wyróżnienie komentarzy
// DONE debugowanie
// DONE wyświetlanie stanu taśmy
// DONE scenariusze testowe - przykłady działające, i nie działające, poprawnie obsłużyć
// DONE krótki opis BrainFucka

/**
 * Klasa głowna edytora, odpowiedzialna za GUI edytora
 * 
 * @author tpsa
 */
public class MainFrame extends JFrame implements CaretListener,
		UndoableEditListener, ChangeListener {
	static int lastBFPositionShowing = -1;

	/**
	 * 
	 */
	private static final long serialVersionUID = -4950512615304159756L;

	/**
	 * 
	 */
	/**
	 * JTextPane w którym umieszczamy kod BF
	 */
	private AbstractAction findAction;
	private FindDialog findDialog;
	private Action newFileAction;
	private AbstractAction openAction;
	private AbstractAction pauseAction;
	private JMenuItem pauseMachine;
	/**
	 * Label, który pokazuje aktualną pozycję w tekście
	 */
	private JMenuItem redo;
	private RedoAction redoAction;
	private AbstractAction replaceAction;
	private ReplaceDialog replaceDialog;
	private AbstractAction resumeAction;
	private JMenuItem resumeMachine;
	private AbstractAction runAction;
	@SuppressWarnings("unused")
	private AbstractAction runLastWorkingCode = null;
	private JMenuItem runWithoutDebug;
	private AbstractAction saveAction;
	private AbstractAction saveKnownAction;
	private AbstractAction showLinesAction;
	private Action showStatusAction;

	private JMenuItem showStatusLabel;

	private Action stepAction;
	private JMenuItem stepMachine;
	private AbstractAction stopAction;
	private JMenuItem stopMachine;
	private JTabbedPane tabs = new JTabbedPane();
	private JMenuItem undo;

	private UndoAction undoAction;

	private UndoManager undoManager = new UndoManager();

	private AbstractAction runActionWithDebug;

	private AbstractAction showMemoryPanelAction;

	protected byte[] lastWorkingCode;

	/**
	 * Tworzy instancję klasy MainFrame
	 */
	public MainFrame() {
		super("Brainfuck IDE");

		BrainFuckIDE.lg.info("Creating main frame of Brainfuck IDE");

		tabs.addChangeListener(this);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));

		replaceDialog = new ReplaceDialog(this);
		findDialog = new FindDialog(this);

		BrainFuckIDE.lg.fine("Setting actions");
		setActions();

		JMenuBar menuBar = getMainFrameMenuBar();
		setJMenuBar(menuBar);

		// tab.getArea().addMouseListener(this);
		// tab.getArea().addMouseMotionListener(this);

		undoAction = new UndoAction(undoManager, undo, null);
		redoAction = new RedoAction(undoManager, redo, undoAction);
		undo.setAction(undoAction);
		undo.setText("Undo");
		redo.setAction(redoAction);
		redo.setText("Redo");

		undoAction.setRedoAction(redoAction);

		BFTab tab1 = new BFTab(this, pauseAction, resumeAction, stepAction,
				stopAction, runAction, undoAction, redoAction);
		tabs.addContainerListener(tab1);

		tabs.add(tab1);
		int idx = tabs.indexOfComponent(tab1);
		tabs.setTitleAt(idx, "Untitled");
		tabs.setTabComponentAt(idx, new ButtonTabComponent(tabs));
		add(tabs, BorderLayout.CENTER);
		// add(status, BorderLayout.SOUTH);

		BrainFuckIDE.lg.info("Adding document listener");

		pack();
	}

	@Override
	public void caretUpdate(final CaretEvent e) {

	}

	/**
	 * Generuje menu programu
	 * 
	 * @return Zwraca instancję JMenuBar (menu), dla programu
	 */
	private JMenuBar getMainFrameMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu file = new JMenu("File");
		JMenu edit = new JMenu("Edit");
		JMenu view = new JMenu("View");
		JMenu run = new JMenu("Run");
		JMenu help = new JMenu("Help");

		menuBar.add(file);
		menuBar.add(edit);
		menuBar.add(view);
		menuBar.add(run);
		menuBar.add(help);

		help.add(new JMenuItem(new AbstractAction("About program") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 5839320025951821584L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("About program action performed");
				Container c = tabs.getTopLevelAncestor();
				AboutProgramDialog dialog = new AboutProgramDialog((JFrame) c);
				dialog.setLocationRelativeTo(c);
				dialog.setLocation(c.getWidth() / 2, c.getHeight() / 2);
				dialog.showDialog();
			}
		}));

		help.add(new JMenuItem(new AbstractAction("Tutorial") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6114772901713788593L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("Help action performed");
				Container c = tabs.getTopLevelAncestor();
				HelpDialog dialog = new HelpDialog((JFrame) c);
				dialog.setLocationRelativeTo(c);
				dialog.setLocation(c.getWidth() / 2, c.getHeight() / 2);
				dialog.showDialog();

			}
		}));

		JMenuItem newFile = new JMenuItem("New file");
		newFile.setAction(newFileAction);
		JMenuItem open = new JMenuItem("Open");
		open.setAction(openAction);
		JMenuItem save = new JMenuItem("Save");
		JMenuItem saveAs = new JMenuItem("Save as");
		saveAs.setAction(saveAction);
		JMenuItem quit = new JMenuItem("Quit");

		quit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		file.add(newFile);
		file.add(open);
		save.setAction(saveKnownAction);
		file.add(save);
		file.add(saveAs);
		file.add(quit);

		undo = new JMenuItem("Undo");
		redo = new JMenuItem("Redo");

		JMenuItem cut = new JMenuItem("Cut");
		cut.addActionListener(new DefaultEditorKit.CutAction());
		// cut.setMnemonic(KeyEvent.VK_X);

		JMenuItem copy = new JMenuItem("Copy");
		copy.addActionListener(new DefaultEditorKit.CopyAction());
		// copy.setMnemonic(KeyEvent.VK_C);

		JMenuItem paste = new JMenuItem("Paste");
		paste.addActionListener(new DefaultEditorKit.PasteAction());
		// paste.setMnemonic(KeyEvent.VK_P);

		JMenuItem find = new JMenuItem("Find");
		find.setAction(findAction);

		KeyStroke findStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F,
				Event.CTRL_MASK);
		find.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(findStroke,
				"find");
		find.getActionMap().put("find", findAction);

		JMenuItem replace = new JMenuItem("Replace");
		replace.setAction(replaceAction);

		KeyStroke replaceStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R,
				Event.CTRL_MASK);
		find.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(replaceStroke,
				"replace");
		find.getActionMap().put("replace", replaceAction);

		KeyStroke newFileStroke = KeyStroke.getKeyStroke(KeyEvent.VK_N,
				Event.CTRL_MASK);
		newFile.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				newFileStroke, "newFile");
		newFile.getActionMap().put("newFile", newFileAction);

		KeyStroke saveFileStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Event.CTRL_MASK);
		save.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveFileStroke,
				"saveFile");
		save.getActionMap().put("saveFile", saveKnownAction);

		edit.add(undo);
		edit.add(redo);
		edit.addSeparator();
		edit.add(cut);
		edit.add(copy);
		edit.add(paste);
		edit.addSeparator();
		edit.add(find);
		edit.add(replace);
		edit.addSeparator();
		edit.add(new JMenuItem(new AbstractAction("Memory preferences") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6096201895928552672L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("Memory preferences action performed");
				MainFrame frame = (MainFrame) tabs.getTopLevelAncestor();

				MemorySizeDialog dialog = new MemorySizeDialog(frame);
				dialog.setSizeMemory(BFMachine.defaultMemorySize);
				if (!dialog.showDialog())
					return;

				BFMachine.defaultMemorySize = dialog.getSizeMemory();

			}
		}));

		JMenuItem showLineNumbers = new JMenuItem("Show line numbers");
		showStatusLabel = new JCheckBoxMenuItem("Show status label");

		view.add(showLineNumbers);

		showLineNumbers.setAction(showLinesAction);

		view.add(showMemoryPanelAction);

		view.add(showStatusLabel);

		showStatusLabel.setAction(showStatusAction);

		runWithoutDebug = new JMenuItem("Run without debug");
		pauseMachine = new JMenuItem("Pause machine");
		resumeMachine = new JMenuItem("Resume machine");
		stopMachine = new JMenuItem("Stop machine");
		stepMachine = new JMenuItem("Step machine");

		runWithoutDebug.setAction(runAction);
		pauseMachine.setAction(pauseAction);
		resumeMachine.setAction(resumeAction);
		stopMachine.setAction(stopAction);
		stepMachine.setAction(stepAction);

		run.add(runActionWithDebug);
		run.add(runWithoutDebug);
		// run.add(runLastWorkingCode);
		run.add(pauseMachine);
		run.add(resumeMachine);
		run.add(stopMachine);
		run.add(stepAction);

		return menuBar;
	}

	/**
	 * Ustawia akcje np. find, replace Każda akcja ma reakcję
	 */
	private void setActions() {

		showMemoryPanelAction = new AbstractAction("Show memory panel") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2012577125654615111L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("Show memory action performed");
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				if (tab == null)
					return;

				JSplitPane pane = tab.getSplitPane1();
				ArrayList<Component> components = new ArrayList<Component>(
						Arrays.asList(pane.getComponents()));
				BrainFuckIDE.lg.finest("Got components");

				boolean contains = false;
				for (Component c : components)
					if (c == tab.getEditor()) {
						contains = true;
						break;
					}

				System.err.println(components.size());
				if (contains) {
					BrainFuckIDE.lg.info("Removing memory panel");
					pane.remove(tab.getEditor());
				} else {
					BrainFuckIDE.lg.info("Adding memory panel");
					pane.add(tab.getEditor(), 1);
					pane.setDividerLocation(BFTab.startSplitterPosition);
				}

			}
		};

		newFileAction = new AbstractAction("New file") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 5483399276425267838L;

			@Override
			public void actionPerformed(ActionEvent e) {
				MainFrame frame = (MainFrame) tabs.getTopLevelAncestor();
				BFTab tab = new BFTab(frame, pauseAction, resumeAction,
						stepAction, stopAction, runAction, undoAction,
						redoAction);
				tabs.addContainerListener(tab);
				tabs.add(tab);
				int idx = tabs.indexOfComponent(tab);
				tabs.setTitleAt(idx, "Untitled");
				tabs.setTabComponentAt(idx, new ButtonTabComponent(tabs));

			}
		};

		runAction = new AbstractAction("RunWithoutDebug") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 5839320025951821584L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("Run action executed");

				final BFTab tab = (BFTab) tabs.getSelectedComponent();
				Document doc = tab.getPaneResult().getDocument();
				final BFMachine machine = tab.getMachine();

				final Thread t = prepareMachine(tab, doc, machine);
				if (t == null)
					return;
				machine.setStepping(false);
				t.start();

			}
		};

		runActionWithDebug = new AbstractAction("Run with debug") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6096201895928552672L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("Run action executed");

				final BFTab tab = (BFTab) tabs.getSelectedComponent();
				Document doc = tab.getPaneResult().getDocument();
				final BFMachine machine = tab.getMachine();

				final Thread t = prepareMachine(tab, doc, machine);
				if (t == null)
					return;

				machine.setStepping(true);
				t.start();

			}
		};

		showStatusAction = new AbstractAction("ShowStatusLabel") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2012577125654615111L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// BrainFuckIDE.lg.finest("showStatusLabel.isSelected());

				BFTab tab = (BFTab) tabs.getSelectedComponent();
				Component[] comp = tab.getComponents();
				ArrayList<Component> l = new ArrayList<Component>(
						Arrays.asList(comp));

				boolean contains = l.contains(tab.getStatus());

				if (!contains) {
					tab.add(tab.getStatus(), BorderLayout.NORTH);
				} else
					tab.remove(tab.getStatus());

			}
		};

		showLinesAction = new AbstractAction("ShowLines") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2012577125654615111L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				JScrollPane pane = tab.getPane();
				JViewport port = pane.getRowHeader();
				if (port == null)
					pane.setRowHeaderView(tab.getRows());
				else
					pane.setRowHeader(null);

			}
		};

		runLastWorkingCode = new AbstractAction("Run last working code") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1126695922005433078L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				if (tab == null)
					return;
				StyledDocument doc = tab.getArea().getStyledDocument();
				BFMachine machine = tab.getMachine();
				if (machine.isWorking()) {
					JOptionPane.showMessageDialog(tabs.getTopLevelAncestor(),
							"Machine is working");
					return;
				}
				Thread t = prepareMachine(tab, doc, machine);
				if (lastWorkingCode == null) {
					JOptionPane.showMessageDialog(tabs.getTopLevelAncestor(),
							"no working code");
					tab.getStatusLabel().setText("Ready");
					return;
				}
				machine.setCode(lastWorkingCode);
				t.start();
			}
		};

		pauseAction = new AbstractAction("Pause machine") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6096201895928552672L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("Pause action performed");
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				BFMachine machine = tab.getMachine();

				if (!machine.isWorking()) {
					JOptionPane.showMessageDialog(null, "No maching running");
					return;
				}
				machine.pause();
				System.err.println("Paused");
				tab.getStatusLabel().setText("Machine paused");
			}
		};

		resumeAction = new AbstractAction("Resume machine") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2012577125654615111L;

			@Override
			public void actionPerformed(ActionEvent e) {

				BrainFuckIDE.lg.info("Resume action performed");
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				BFMachine machine = tab.getMachine();

				if (!machine.isWorking()) {
					JOptionPane.showMessageDialog(null, "No maching running");
					return;
				}
				machine.setStepping(false);
				machine.resume();
				tab.getStatusLabel().setText("Running...");
			}
		};

		stopAction = new AbstractAction("Stop machine") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2599448412251509415L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("Stop action performed");
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				BFMachine machine = tab.getMachine();
				if (!machine.isWorking()) {
					JOptionPane.showMessageDialog(null, "No maching running");
					return;
				}
				machine.stop();
				tab.getStatusLabel().setText("Machine stopped - ready...");
				tab.getArea().setEditable(true);
			}
		};

		stepAction = new AbstractAction("StepAction") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 5880656879834148166L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("Step action performed");
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				BFMachine machine = tab.getMachine();
				if (!machine.isWorking()) {
					JOptionPane.showMessageDialog(getParent(),
							"Machine is not working");
					return;
				}
				machine.resume();
				machine.setStepping(true);
			}
		};

		openAction = new AbstractAction("open") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4575053887363265531L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser chooser = new JFileChooser(".");
					chooser.setFileFilter(new BFFileFilter());
					if (chooser.showDialog(null, "Otwórz") == JFileChooser.APPROVE_OPTION) {

						File f = chooser.getSelectedFile();
						BufferedReader r = new BufferedReader(
								new InputStreamReader(new FileInputStream(f)));
						StringBuilder sb = new StringBuilder();

						char[] buffer = new char[0x1000];

						while (r.read(buffer) >= 0)
							sb.append(buffer);

						r.close();

						MainFrame frame = (MainFrame) tabs
								.getTopLevelAncestor();
						BFTab tab = new BFTab(frame, pauseAction, resumeAction,
								stepAction, stopAction, runAction, undoAction,
								redoAction);
						tabs.add(tab);
						int idx = tabs.indexOfComponent(tab);
						tab.setFile(f);
						tabs.setTitleAt(idx, f.getName());
						tabs.setTabComponentAt(idx,
								new ButtonTabComponent(tabs));

						tabs.setSelectedIndex(idx);

						tab.getArea().setText(sb.toString());
						tab.getArea().setCaretPosition(0);
					}
				} catch (IOException excp) {
					BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
				}

			}
		};

		saveKnownAction = new AbstractAction("Save") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -985908773082887131L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				if (tab == null)
					return;

				File f = tab.getFile();
				if (f == null) {
					saveAction.actionPerformed(e);
					f = tab.getFile();
					if (f == null) {
						JOptionPane
								.showMessageDialog(tabs.getTopLevelAncestor(),
										"Coś się schrzaniło\nW zasadzie nie wiem, nawet co");
						return;
					}
				}

				OutputStreamWriter writer;
				try {
					writer = new OutputStreamWriter(new FileOutputStream(f));
					writer.write(tab.getArea().getText());
					writer.close();
				} catch (FileNotFoundException excp) {
					BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
				} catch (IOException excp) {
					BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
				}

			}
		};

		saveAction = new AbstractAction("Save as") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4575053887363265531L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					BFTab tab = (BFTab) tabs.getSelectedComponent();
					JFileChooser chooser = new JFileChooser(".");
					chooser.setFileFilter(new BFFileFilter());
					if (chooser.showDialog(null, "Zapisz") == JFileChooser.APPROVE_OPTION) {

						File f = chooser.getSelectedFile();
						tabs.setTitleAt(tabs.getSelectedIndex(), f.getName());
						tab.setFile(f);
						BufferedWriter r = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(f)));
						r.write(tab.getArea().getText());
						r.close();
					}
				} catch (IOException excp) {
					BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
				}

			}
		};

		findAction = new AbstractAction("find") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 8670201600032148368L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("Action find performed");

				findDialog.setVisible(true);

			}
		};

		replaceAction = new AbstractAction("replace") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4575053887363265531L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("Action action performed");
				replaceDialog.setVisible(true);

			}
		};

	}

	private void showBFProgress() {
		try {
			BrainFuckIDE.lg.info("Show BF progress");
			BFTab tab = (BFTab) tabs.getSelectedComponent();
			BFMachine machine = tab.getMachine();
			int pos = machine.getPosition();
			tab.getStatusLabel().setText(
					"Stepping pos: " + pos + "("
							+ (char) tab.getMachine().getCode()[pos]
							+ "), memory pos : "
							+ tab.getMachine().getMemoryPointer());
			tab.getEditor().setCurrentOffset(
					tab.getMachine().getMemoryPointer());
			StyledDocument doc = (StyledDocument) tab.getArea().getDocument();
			Style caretStyle = doc.getStyle("BFCaret");
			Style codeStyle = doc.getStyle("CODE");
			Style commentStyle = doc.getStyle("COMMENT");

			if (lastBFPositionShowing >= 0) {
				String s = doc.getText(pos, 1);
				Character c = s.charAt(0);
				if (c == '[' || c == ']' || c == '\n' || c == '\r'
						|| SyntaxHighlighter.BFcharacterList.contains(c))
					doc.setCharacterAttributes(lastBFPositionShowing, 1,
							codeStyle, true);
				else
					doc.setCharacterAttributes(lastBFPositionShowing, 1,
							commentStyle, true);
			}

			doc.setCharacterAttributes(pos, 1, caretStyle, true);
			lastBFPositionShowing = pos;
		} catch (BadLocationException excp) {
			BrainFuckIDE.lg.warning(BrainFuckIDE.ErrorsFormatter(excp));
		}
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		BrainFuckIDE.lg.info("Undoable edit happend: "
				+ e.getEdit().getPresentationName());

		// FIXME jak na razie, działa, ale nie wiem dlaczego, jeżeli coś się
		// sypie z UndoManager tu sprawdzać

		if (e.getEdit().getPresentationName().equals("style change") == false)
			undoManager.addEdit(e.getEdit());

		undoAction.update();
		redoAction.update();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		BFTab tab = (BFTab) tabs.getSelectedComponent();
		if (tab == null)
			return;
		if (tab.getComponentCount() <= 0)
			return;
		ArrayList<Component> components = new ArrayList<Component>(
				Arrays.asList(tab.getComponents()));
		showStatusLabel.setSelected(components.contains(tab.getStatus()));

	}

	/**
	 * Szykuje maszynę, na nowe zawody (reset)
	 * 
	 * @param tab
	 *            zakładka
	 * @param doc
	 *            dokument
	 * @param machine
	 *            maszyna
	 * @return
	 */
	private Thread prepareMachine(final BFTab tab, Document doc,
			final BFMachine machine) {
		if (machine.isWorking()) {
			JOptionPane.showMessageDialog(null,
					"Machine is working currently!!!");
			return null;
		}

		tab.getArea().setEditable(false);

		BrainFuckIDE.lg
				.info("Adding document listener to the input stream of console");
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException excp) {
			BrainFuckIDE.lg.severe("Exception: ");
		}

		BrainFuckIDE.lg.fine("Creating BF machine");

		machine.setCode(tab.getArea().getText());
		machine.setFinishHandler(new Runnable() {

			@Override
			public void run() {
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				String s = machine.getError();
				if (s != null && !s.equals(""))
					JOptionPane.showMessageDialog(tabs.getTopLevelAncestor(),
							"Whoops: BF machine" + " complaints: " + s,
							"BF error", JOptionPane.ERROR_MESSAGE);
				else
					lastWorkingCode = tab.getMachine().getCode();
				tab.getEditor().repaint();
				BrainFuckIDE.lg.info("Memory of BF machine is: "
						+ tab.getMachine().getMemory().length);
				tab.getArea().setEditable(true);
				tab.getStatusLabel().setText("Ready");

			}

		});

		machine.setBreakpointHandler(new Runnable() {

			@Override
			public void run() {
				showBFProgress();
				tab.getStatusLabel().setText(
						tab.getStatusLabel().getText()
								+ ". Breakpoint reached...");
			}
		});

		machine.setStepHandler(new Runnable() {

			@Override
			public void run() {
				showBFProgress();

			}
		});

		final Thread t = new Thread(machine);
		tab.getMachine().reset();
		tab.getStatusLabel().setText("Running...");
		return t;
	}

	public JTabbedPane getTabs() {
		return tabs;
	}

	public void setTabs(JTabbedPane tabs) {
		this.tabs = tabs;
	}

}
