package tpsa.dialogs;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import tpsa.BrainFuckIDE;
import tpsa.MainFrame;

/**
 * Dialog ustalania pamięci VM BrainFucka
 * 
 * @author Duga Eye
 * 
 */
public class MemorySizeDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6179258766072502357L;
	private Action okAction;
	private Action cancelAction;
	private JTextField sizeEdit;
	private boolean result = false;

	public MemorySizeDialog(final MainFrame frame) {
		super(frame);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);

		setPreferredSize(new Dimension(200, 150));
		
		Box b = Box.createVerticalBox();

		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		p1.add(new JLabel("Podaj rozmiar VM Brainfucka: "));
		sizeEdit = new JTextField(8);
		p1.add(sizeEdit);

		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");

		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p2.add(ok);
		p2.add(cancel);

		b.add(p1);
		b.add(p2);

		okAction = new AbstractAction("OK") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5794707237614988673L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrainFuckIDE.lg.info("ok action performed");
				if (getSizeMemory() <= 0) {
					JOptionPane
							.showMessageDialog(frame,
									"Wartość rozmiaru pamieci musi być typu Integer i >= 0");
					return;
				}

				result = true;
				setVisible(false);

			}
		};

		cancelAction = new AbstractAction("Cancel") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 8441237664658329642L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};

		ok.setAction(okAction);
		cancel.setAction(cancelAction);

		JRootPane pane = getRootPane();
		KeyStroke acceptStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		KeyStroke cancelStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

		InputMap iMap = pane
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap aMap = pane.getActionMap();

		iMap.put(acceptStroke, "accept");
		aMap.put("accept", okAction);

		iMap.put(cancelStroke, "cancel");
		aMap.put("cancel", cancelAction);

		add(b);

		pack();

	}

	public int getSizeMemory() {
		return Integer.parseInt(sizeEdit.getText());
	}

	public void setSizeMemory(int s) {
		sizeEdit.setText("" + s);
	}

	/**
	 * Pokazuje ten wspaniały dialog
	 * 
	 * @return w zasadzie nic, czy użytkownik zaakceptował wprowadzone dane
	 */
	public boolean showDialog() {
		result = false;
		setVisible(true);
		return result;
	}


}
