package de.tub.citydb.config.gui.window;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlType(name="WindowSizeType", propOrder={
		"x",
		"y",
		"width",
		"height"
})
public class WindowSize {
	@XmlElement(required=true)
	private Integer x;
	@XmlElement(required=true)
	private Integer y;
	@XmlElement(required=true)
	private Integer width;
	@XmlElement(required=true)
	private Integer height;

	public WindowSize() {
	}

	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

}
