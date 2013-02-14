package tpsa.exceptions;

/**
 * Wskaźnik pamięci wyszedł poza pamięć
 * 
 * @author Duga Eye
 * 
 */
public class OutOfMemoryPointer extends BFException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6323824785754255254L;

	public OutOfMemoryPointer(String message, int offset) {
		super(message, offset);
	}

}
