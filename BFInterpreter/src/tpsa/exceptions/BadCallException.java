package tpsa.exceptions;

/**
 * Wyjątek rzucany gdy funkcja rewindTheLoopToTheEnd zostanie wywołana nie
 * wtedy, gdy codePointer będzie wskazywał na '['
 * 
 * @author Duga Eye
 * 
 */
public class BadCallException extends BFException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2134149027354550342L;

	public BadCallException(String message, int offset) {
		super(message, offset);
	}

}
