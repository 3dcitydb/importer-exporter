package de.tub.citydb.config.gui.window;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ConsoleWindowType", propOrder={
		"size"
		})
public class ConsoleWindow {
	private WindowSize size;
	@XmlAttribute(required=true)
	private boolean isDetached = false;

	public ConsoleWindow() {
		size = new WindowSize();
	}

	public WindowSize getSize() {
		return size;
	}

	public void setSize(WindowSize size) {
		if (size != null)
			this.size = size;
	}

	public boolean isDetached() {
		return isDetached;
	}

	public void setDetached(boolean isDetached) {
		this.isDetached = isDetached;
	}
	
}
