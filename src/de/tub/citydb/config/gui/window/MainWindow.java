package de.tub.citydb.config.gui.window;

import javax.xml.bind.annotation.XmlType;


@XmlType(name="MainWindowType", propOrder={
		"size",
		"dividerLocation"
		})
public class MainWindow {
	private WindowSize size;
	private Integer dividerLocation;
	
	public MainWindow() {
		size = new WindowSize();
	}

	public WindowSize getSize() {
		return size;
	}

	public void setSize(WindowSize size) {
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
