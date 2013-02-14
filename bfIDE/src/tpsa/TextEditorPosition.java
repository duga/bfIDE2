package tpsa;

/**
 * Klasa pozycji w dokumencie
 * 
 * @author Duga Eye
 * 
 */
public class TextEditorPosition {
	/**
	 * Linia w dokumencie
	 */
	public int Line;
	/**
	 * Offset w linii dokumentu
	 */
	public int Offset;
	
	/**
	 * 
	 */
	public TextEditorPosition()
	{
		Line = 0;
		Offset = 0;
	}

	public TextEditorPosition(int line, int offset) {
		this.Line = line;
		this.Offset = offset;
	}

}
