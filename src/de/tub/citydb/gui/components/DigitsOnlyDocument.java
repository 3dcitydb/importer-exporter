package de.tub.citydb.gui.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class DigitsOnlyDocument extends PlainDocument {
	private int maxLength = Integer.MAX_VALUE;
	
	public DigitsOnlyDocument() {
	}
	
	public DigitsOnlyDocument(int maxLength) {
		if (maxLength > 0)
			this.maxLength = maxLength;
	}
	
	public void insertString(int offset, String str, AttributeSet a)
	throws BadLocationException {
		if (str == null)
			return;
		
		str = str.replaceAll("\\D*", "");
		
		if ((getLength() + str.length()) <= maxLength)
			super.insertString(offset, str, a);
	}

}
