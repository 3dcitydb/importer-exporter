package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="PrefixesType", propOrder={
		"placemarkFootprint",
		"placemarkExtruded",
		"placemarkGeometry",
		"placemarkCollada",
		"placemarkHighlight"
})
public class IdPrefixes {
	@XmlTransient private final String PLACEMARK_FOOTPRINT_PREFIX = "KMLFootp_";  
	@XmlTransient private final String PLACEMARK_EXTRUDED_PREFIX = "KMLExtr_";  
	@XmlTransient private final String PLACEMARK_GEOMETRY_PREFIX = "KMLGeom_";
	@XmlTransient private final String PLACEMARK_HIGHLIGHT_PREFIX = "KMLGeomHi_";
	@XmlTransient private final String PLACEMARK_COLLADA_PREFIX = "COLLADA_";
	
	private String placemarkFootprint;
	private String placemarkExtruded;
	private String placemarkGeometry;
	private String placemarkCollada;
	private String placemarkHighlight;
	
	public String getPlacemarkFootprint() {
		return placemarkFootprint == null ? PLACEMARK_FOOTPRINT_PREFIX : placemarkFootprint;
	}
	
	public void setPlacemarkFootprint(String placemarkIdFootprint) {
		this.placemarkFootprint = placemarkIdFootprint;
	}
	
	public String getPlacemarkExtruded() {
		return placemarkExtruded == null ? PLACEMARK_EXTRUDED_PREFIX : placemarkExtruded;
	}
	
	public void setPlacemarkExtruded(String placemarkIdExtruded) {
		this.placemarkExtruded = placemarkIdExtruded;
	}
	
	public String getPlacemarkGeometry() {
		return placemarkGeometry == null ? PLACEMARK_GEOMETRY_PREFIX : placemarkGeometry;
	}
	
	public void setPlacemarkGeometry(String placemarkIdGeometry) {
		this.placemarkGeometry = placemarkIdGeometry;
	}
	
	public String getPlacemarkCollada() {
		return placemarkCollada == null ? PLACEMARK_COLLADA_PREFIX : placemarkCollada;
	}
	
	public void setPlacemarkCollada(String placemarkIdCollada) {
		this.placemarkCollada = placemarkIdCollada;
	}
	
	public String getPlacemarkHighlight() {
		return placemarkHighlight == null ? PLACEMARK_HIGHLIGHT_PREFIX : placemarkHighlight;
	}
	
	public void setPlacemarkHighlight(String placemarkIdHighlight) {
		this.placemarkHighlight = placemarkIdHighlight;
	}
	
}
