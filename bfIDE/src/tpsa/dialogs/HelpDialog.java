package tpsa.dialogs;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Dialog pomocy
 * 
 * @author Duga Eye
 * 
 */
public class HelpDialog extends JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5854396908923903514L;

	public HelpDialog(JFrame frame) {
		super(frame, "Help", ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(500, 400));

		pack();
	}

	public boolean showDialog() {
		setVisible(true);
		return true;
	}

}
