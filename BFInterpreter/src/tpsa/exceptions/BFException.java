package tpsa.exceptions;

/**
 * Ogólny exception dla BF
 * 
 * @author Duga Eye
 * 
 */
public class BFException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1955781345162254359L;

	private int offset;

	public BFException(String message, int offset) {
		super(message);
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

}
