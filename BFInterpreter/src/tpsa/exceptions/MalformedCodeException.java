package tpsa.exceptions;

/**
 * W zasadzie ten wyjątek nigdy nie powinien wystąpić: "Nie wiem jakim cudem,
 * ale pointer wyszedł poza kod"
 * 
 * @author Duga Eye
 * 
 */
public class MalformedCodeException extends BFException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5072506605351465843L;

	public MalformedCodeException(String message, int offset) {
		super(message, offset);
	}

}
