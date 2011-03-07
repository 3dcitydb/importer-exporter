package de.tub.citydb.config.gui.window;

import javax.xml.bind.annotation.XmlType;


@XmlType(name="GuiWindowType", propOrder={
		"size",
		"dividerLocation"
		})
public class GuiWindow {
	private GuiWindowSize size;
	private Integer dividerLocation;
	
	public GuiWindow() {
		size = new GuiWindowSize();
	}

	public GuiWindowSize getSize() {
		return size;
	}

	public void setSize(GuiWindowSize size) {
		if (size != null)
			this.size = size;
	}

	public Integer getDividerLocation() {
		return dividerLocation;
	}

	public void setDividerLocation(Integer dividerLocation) {
		if (dividerLocation != null)
			this.dividerLocation = dividerLocation;
	}
	
}
