package tpsa.highlighters;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

/**
 * Reprezentuje klasę coś ala TODO dla swing utilities co ma odmalować
 * odpowiednim kolorem
 * 
 * @author Duga Eye
 */
public class StyleHighlight {
	private StyledDocument doc;

	private int length;

	private int offset;

	private Style style;

	public StyleHighlight() {

	}

	public StyleHighlight(StyledDocument doc, int offset, int length,
			Style style) {
		this.doc = doc;
		this.offset = offset;
		this.length = length;
		this.style = style;
	}

	public StyledDocument getDoc() {
		return doc;
	}
	public int getLength() {
		return length;
	}
	public int getOffset() {
		return offset;
	}
	public Style getStyle() {
		return style;
	}

	public void setDoc(StyledDocument doc) {
		this.doc = doc;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setStyle(Style style) {
		this.style = style;
	}

}
