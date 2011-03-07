package de.tub.citydb.config.gui;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.gui.window.GuiWindow;


@XmlRootElement
@XmlType(name="GuiType", propOrder={
		"window"
		})
public class Gui {
	
	private GuiWindow window; 
	
	public Gui() {
		window = new GuiWindow();
	}

	public GuiWindow getWindow() {
		return window;
	}

	public void setWindow(GuiWindow window) {
		if (window != null)
			this.window = window;
	}
	
}
