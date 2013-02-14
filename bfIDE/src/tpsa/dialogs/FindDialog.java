package tpsa.dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

import tpsa.BFTab;
import tpsa.BrainFuckIDE;
import tpsa.MainFrame;

/**
 * Dialog wyszukiwania wyrażenia
 * 
 * @author Duga Eye
 * 
 */
public class FindDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -486529548871883227L;
	private JTextField match;
	private boolean result;
	/**
	 * @param frame
	 */
	public FindDialog(final MainFrame frame) {
		super(frame);
		setTitle("Find string");

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(250, 100));

		Box b = Box.createVerticalBox();

		JRootPane pane = getRootPane();
		KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

		pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke,
				"escape");
		pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterStroke,
				"EnterFind");
		pane.getActionMap().put("escape", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -375905298222684550L;

			@Override
			public void actionPerformed(ActionEvent e) {
				result = false;
				setVisible(false);
			}
		});
		pane.getActionMap().put("EnterFind", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -8373549218006477790L;

			@Override
			public void actionPerformed(ActionEvent e) {

				try {

					BrainFuckIDE.lg.fine("Searching...");
					JTabbedPane tabs = frame.getTabs();
					BFTab tab = (BFTab) tabs.getSelectedComponent();
					if (tab == null)
						return;

					String s = tab.getArea().getText();
					String search = getMatch();
					int oldPos = tab.getArea().getCaretPosition();
					int pos = -1;
					pos = s.indexOf(search, oldPos + 1);
					if (pos < 0) {
						BrainFuckIDE.lg.info("Rewinding to the beginning");
						pos = s.indexOf(search);
					}

					BrainFuckIDE.lg.fine("Searching for: " + search);

					BrainFuckIDE.lg.finest("found: " + pos);
					if (pos >= 0) {
						Rectangle rect = tab.getArea().modelToView(pos);
						BrainFuckIDE.lg.finest("rect: " + rect.toString());
						JTextPane area = tab.getArea();
						area.scrollRectToVisible(rect);
						area.setCaretPosition(pos);
					} else
						JOptionPane.showMessageDialog(frame,
								"Not found from cursor phrase: " + search);

				} catch (BadLocationException excp) {
					BrainFuckIDE.lg.warning(BrainFuckIDE.ErrorsFormatter(excp));
				}

			}
		});

		Box b1 = Box.createHorizontalBox();

		b1.add(new JLabel("Find: "));
		match = new JTextField(10);
		b1.add(match);

		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.CENTER));

		JButton find = new JButton("Find");
		// JButton findNext = new JButton("Find next");
		p2.add(find);
		// p2.add(findNext);

		b.add(b1);
		Component gl = Box.createHorizontalGlue();
		gl.setPreferredSize(new Dimension(10, 10));
		b.add(gl);
		b.add(p2);

		find.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				result = true;
				setVisible(false);

			}
		});

		// add(find);
		// add(findNext);

		Component glue = Box.createVerticalGlue();
		glue.setPreferredSize(new Dimension(Integer.MAX_VALUE,
				Integer.MAX_VALUE));
		b.add(glue);

		add(b);

		pack();
	}

	/**
	 * Wyrażenie do wyszukania
	 * 
	 * @return Wyrażenie, które użytkownik chce wyszukać
	 */
	public String getMatch() {
		return new String(match.getText());
	}

	/**
	 * Pokaż dialog
	 * 
	 * @return Potwierdzenie/odrzucenie zmian
	 */
	public boolean showDialog() {
		result = false;
		setVisible(true);
		return result;
	}

}
