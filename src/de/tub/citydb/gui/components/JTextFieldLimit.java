package de.tub.citydb.gui.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

@SuppressWarnings("serial")
public class JTextFieldLimit extends PlainDocument {
	private int maxLength = Integer.MAX_VALUE;
	private boolean digitsOnly;

	public JTextFieldLimit() {
	}

	public JTextFieldLimit(int maxLength, boolean digitsOnly) {
		this.digitsOnly = digitsOnly;
		if (maxLength > 0)
			this.maxLength = maxLength;
	}

	public void insertString(int offset, String str, AttributeSet a)
	throws BadLocationException {
		if (str == null)
			return;

		if (digitsOnly)
			str = str.replaceAll("\\D*", "");

		if ((getLength() + str.length()) <= maxLength)
			super.insertString(offset, str, a);
	}

}
