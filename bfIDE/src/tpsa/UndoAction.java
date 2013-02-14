package tpsa;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Klasa implementująca UnDo
 * 
 * @author Duga Eye
 * 
 */
public class UndoAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5058740336403951058L;

	private RedoAction redoAction;
	private JMenuItem undoButton;

	private UndoManager undoManager;

	/**
	 * @param manager
	 * @param button
	 * @param action
	 */
	public UndoAction(UndoManager manager, JMenuItem button, RedoAction action) {
		super("Undo");
		BrainFuckIDE.lg.info("Creating instance of class");
		setEnabled(false);
		undoManager = manager;
		undoButton = button;
		redoAction = action;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			// System.err.println("YEP");
			undoManager.undo();
			// System.err.println("WORKS");
		} catch (CannotUndoException excp) {
			BrainFuckIDE.lg.severe(BrainFuckIDE.ErrorsFormatter(excp));
		}
		update();
		redoAction.update();

	}

	/**
	 * @param action
	 */
	public void setRedoAction(RedoAction action) {
		redoAction = action;
	}

	protected void update() {
		BrainFuckIDE.lg.fine("Updating undo/redo state");
		if (undoManager.canUndo()) {
			setEnabled(true);
			undoButton.setEnabled(true);
			putValue(Action.NAME, undoManager.getUndoPresentationName());
		} else {
			setEnabled(false);
			undoButton.setEnabled(false);
			putValue(Action.NAME, "Undo");
		}
	}

}
