package tpsa.dialogs;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * O programie dialog
 * 
 * @author Duga Eye
 * 
 */
public class AboutProgramDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1176985069310756058L;

	public AboutProgramDialog(JFrame frame)
	{
		super(frame, "About program", ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(500, 400));

		pack();
	}

	public boolean showDialog() {
		setVisible(true);
		return true;
	}

}
