package de.tub.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DisplayLevelType", propOrder={
		"level",
		"active",
		"visibleFrom",
		"rgba0",
		"rgba1",
		"rgba2",
		"rgba3",
		"rgba4",
		"rgba5"
})
public class DisplayLevel {

	public static final int FOOTPRINT = 1;  
	public static final int EXTRUDED = 2;  
	public static final int GEOMETRY = 3;
	public static final int COLLADA = 4;

	public static final String FOOTPRINT_STR = "footprint";  
	public static final String EXTRUDED_STR = "extruded";  
	public static final String GEOMETRY_STR = "geometry";
	public static final String COLLADA_STR = "collada";

	public static final String FOOTPRINT_PLACEMARK_ID = "KMLFootp_";  
	public static final String EXTRUDED_PLACEMARK_ID = "KMLExtr_";  
	public static final String GEOMETRY_PLACEMARK_ID = "KMLGeom_";
	public static final String GEOMETRY_HIGHLIGHTED_PLACEMARK_ID = "KMLGeomHi_";
	public static final String COLLADA_PLACEMARK_ID = "COLLADA_";

	public static final int DEFAULT_FILL_COLOR = 0xc8ffff00;
	public static final int DEFAULT_LINE_COLOR = 0xc8ff6400;
	public static final int DEFAULT_FILL_HIGHLIGHTED_COLOR = 0xc8ffffff;
	public static final int DEFAULT_LINE_HIGHLIGHTED_COLOR = 0xc8ffcc66;
	public static final int DEFAULT_WALL_FILL_COLOR = 0xff878787;
 	public static final int DEFAULT_WALL_LINE_COLOR = DEFAULT_LINE_COLOR;
	public static final int DEFAULT_ROOF_FILL_COLOR = 0xffcc0000;
	public static final int DEFAULT_ROOF_LINE_COLOR = DEFAULT_LINE_COLOR;

	public static final int INVISIBLE_COLOR = 0x0100aaff;
	public static final int EXPLOSION_HIGHLIGHTED_FILL_COLOR = 0x8c00aaff;
	public static final int EXPLOSION_HIGHLIGHTED_LINE_COLOR = 0x8cffffff;
	public static final int DEFAULT_ALPHA_VALUE = 140; // 0x8c;

	private int level;
	private boolean active = false;
	private int visibleFrom = 0;
	@XmlTransient
	private int visibleUpTo = -1;
	//colors
	private int rgba0 = -1;
	private int rgba1 = -1;
	private int rgba2 = -1;
	private int rgba3 = -1;
	private int rgba4 = -1;
	private int rgba5 = -1;
	
	public DisplayLevel() {}

	public DisplayLevel(int level, int visibleFrom, int visibleUpTo) {
		this.level = level;
		this.setVisibleFrom(visibleFrom);
		this.setVisibleUpTo(visibleUpTo);
	}

	public String getName() {
		String levelOfDetailByName = "unknown";
		switch (level) {
		case FOOTPRINT:
			levelOfDetailByName = FOOTPRINT_STR;
			break;
		case EXTRUDED:
			levelOfDetailByName = EXTRUDED_STR;
			break;
		case GEOMETRY:
			levelOfDetailByName = GEOMETRY_STR;
			break;
		case COLLADA:
			levelOfDetailByName = COLLADA_STR;
			break;
		}
		return levelOfDetailByName;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setVisibleFrom(int visibleFrom) {
		this.visibleFrom = visibleFrom;
	}

	public int getVisibleFrom() {
		return visibleFrom;
	}

	public void setVisibleUpTo(int visibleUpTo) {
		this.visibleUpTo = visibleUpTo;
	}

	public int getVisibleUpTo() {
		return visibleUpTo;
	}

	public void setRgba0(int rgba0) {
		this.rgba0 = rgba0;
	}

	public int getRgba0() {
		return rgba0;
	}

	public boolean isSetRgba0() {
		return this.rgba0 != -1;
	}

	public void setRgba1(int rgba1) {
		this.rgba1 = rgba1;
	}

	public int getRgba1() {
		return rgba1;
	}

	public boolean isSetRgba1() {
		return this.rgba1 != -1;
	}

	public void setRgba2(int rgba2) {
		this.rgba2 = rgba2;
	}

	public int getRgba2() {
		return rgba2;
	}

	public boolean isSetRgba2() {
		return this.rgba2 != -1;
	}

	public void setRgba3(int rgba3) {
		this.rgba3 = rgba3;
	}

	public int getRgba3() {
		return rgba3;
	}

	public boolean isSetRgba3() {
		return this.rgba3 != -1;
	}
	
	public void setRgba4(int rgba4) {
		this.rgba4 = rgba4;
	}

	public int getRgba4() {
		return rgba4;
	}

	public boolean isSetRgba4() {
		return this.rgba4 != -1;
	}
	
	public void setRgba5(int rgba5) {
		this.rgba5 = rgba5;
	}

	public int getRgba5() {
		return rgba5;
	}

	public boolean isSetRgba5() {
		return this.rgba5 != -1;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public boolean equals(Object obj) {
		boolean value = false;
		if (obj instanceof DisplayLevel) {
			DisplayLevel dl = (DisplayLevel)obj;
			value = dl.getLevel() == this.getLevel();
		}
		return value;
	}
	
	public static String formatColorStringForKML (String rgbColor) {
		String bgrColor;
		// red and blue component change places in KML
		String red = rgbColor.substring(2, 4);
		String blue = rgbColor.substring(6);
		bgrColor = rgbColor.substring(0, 2) + blue + rgbColor.substring(4, 6) + red; 
		return bgrColor;
	}

}
