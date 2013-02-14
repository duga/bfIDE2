package tpsa.dialogs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;

import tpsa.BFTab;
import tpsa.BrainFuckIDE;
import tpsa.MainFrame;

/**
 * Dialog do zastępowania treści jednej drugą
 * 
 * @author Duga Eye
 * 
 */
public class ReplaceDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 103076309978197148L;
	/**
	 * Wyrażenie, które ma znaleść
	 */
	private JTextField match;
	/**
	 * Wyrażenie, którym ma zastąpić match
	 */
	private JTextField replace;
	/**
	 * Czy użytkownik, zaakceptował dane
	 */
	private boolean success;
	public ReplaceDialog(final MainFrame frame) {
		super(frame);
		setTitle("Replace");
		setPreferredSize(new Dimension(250, 100));
		setModalityType(ModalityType.APPLICATION_MODAL);

		setTitle("Find");

		setLayout(new GridLayout(3, 2));

		JRootPane pane = getRootPane();
		KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

		pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke,
				"escape");
		pane.getActionMap().put("escape", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3277168784395957597L;

			@Override
			public void actionPerformed(ActionEvent e) {
				success = false;
				setVisible(false);
			}
		});

		add(new JLabel("Find: "));
		match = new JTextField(10);
		add(match);

		replace = new JTextField(10);
		add(new JLabel("Replace: "));
		add(replace);


		JButton replace = new JButton("Replace");
		Action replaceAct = new AbstractAction("Replace one") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
				JTabbedPane tabs = frame.getTabs();
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				JTextPane area = tab.getArea();
				int cursorPos = area.getCaretPosition();
				StringBuilder s = new StringBuilder(area.getText());
				int posToChange = s.indexOf(getMatch(), cursorPos);
				if (posToChange < 0) {
					JOptionPane.showMessageDialog(frame,
							"Nope, nie znalazłem ciągu do zastąpienia");
					return;
					// ретурн;
				}
					s.replace(posToChange, posToChange + getMatch().length(),
						getReplace());
				tab.getArea().setText(s.toString());
				Rectangle rect = tab.getArea().modelToView(posToChange);
				BrainFuckIDE.lg.finest("rect: " + rect.toString());
				area.scrollRectToVisible(rect);
				area.setCaretPosition(posToChange);
				} catch (BadLocationException excp) {
					BrainFuckIDE.lg.warning(BrainFuckIDE.ErrorsFormatter(excp));
					JOptionPane.showMessageDialog(frame,
							"Coś się stało, i właściwie nie chcę wiedzieć, co",
							"Powitanie", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}; 

		replace.setAction(replaceAct);
		KeyStroke replaceStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				replaceStroke, "replace");
		getRootPane().getActionMap().put("replace", replaceAct);

		JButton replaceAll = new JButton("Replace all");

		replaceAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JTabbedPane tabs = frame.getTabs();
				BFTab tab = (BFTab) tabs.getSelectedComponent();
				String s = tab.getArea().getText();
				int count = ReplaceDialog.countOccurences(s, getMatch());
				s = s.replaceAll(getMatch(), getReplace());
				tab.getArea().setText(s);

				JOptionPane.showMessageDialog(frame, "Sucess. Changed all "
						+ count + " occurences of phrase: " + getMatch(),
						"Abnormal error", JOptionPane.INFORMATION_MESSAGE);

			}
		});

		add(replace);
		add(replaceAll);

		pack();
	}

	/**
	 * @return Wyrażenie, które użytkownik chce zastąpić
	 */
	public String getMatch() {
		return new String(match.getText());
	}

	/**
	 * @return Wyrażenie, którym użytkownik chce zastąpić match
	 */
	public String getReplace() {
		return new String(replace.getText());
	}

	/**
	 * @return Czy potwierdzono zmiany
	 */
	public boolean getSuccess() {
		return success;
	}

	@Override
	public void setVisible(boolean b) {
		if (b == true)
			success = false;
		super.setVisible(b);
	}

	static int countOccurences(String s, String match) {
		int count = 0;
		int pos = -1;
		while ((pos = s.indexOf(match, pos + 1)) >= 0)
			++count;
		return count;
	}

}
