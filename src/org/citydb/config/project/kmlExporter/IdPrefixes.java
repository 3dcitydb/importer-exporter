/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
