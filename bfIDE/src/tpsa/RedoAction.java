package tpsa;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

/**
 * Klasa umożliwia działanie ReDo
 * 
 * @author Duga Eye
 */
public class RedoAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2467991386706847458L;
	private JMenuItem redoButton;
	private UndoAction undoAction;
	private UndoManager undoManager;

	/**
	 * @param manager
	 * @param button
	 * @param action
	 */
	public RedoAction(UndoManager manager, JMenuItem button, UndoAction action) {
		super("Redo");
		setEnabled(false);
		undoManager = manager;
		redoButton = button;
		undoAction = action;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			undoManager.redo();
		} catch (CannotRedoException ex) {
		}
		update();
		undoAction.update();
	}

	/**
	 * @param action
	 *            Akcja analogiczna do ReDo Undo
	 */
	public void setUndoAction(UndoAction action) {
		undoAction = action;
	}

	/**
	 * Uaktualnia historię edycji
	 */
	protected void update() {
		BrainFuckIDE.lg.fine("Updating state of undo/redo action");
		if (undoManager.canRedo()) {
			setEnabled(true);
			redoButton.setEnabled(true);
			putValue(Action.NAME, undoManager.getRedoPresentationName());
		} else {
			setEnabled(false);
			redoButton.setEnabled(false);
			putValue(Action.NAME, "Redo");
		}
	}

}
