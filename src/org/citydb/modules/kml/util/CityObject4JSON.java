/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.kml.util;

public class CityObject4JSON {

	private String gmlId;

	private double envelopeXmin;
	private double envelopeXmax;
	private double envelopeYmin;
	private double envelopeYmax;

	private int tileRow = 0;
	private int tileColumn = 0;

	public CityObject4JSON (String gmlId) {
		this.gmlId = gmlId;
	}

	@Override
	public String toString() {

		return "\t\"" + gmlId + "\": {" +
				"\n\t\"envelope\": [" + envelopeXmin + ", " + envelopeYmin + ", " + envelopeXmax + ", " + envelopeYmax +
				"],\n\t\"tile\": [" + tileRow + ", " + tileColumn + "]}";
		/*
		return "\n\t\"envelope\": [" + envelopeXmin + ", " + envelopeYmin + ", " + envelopeXmax + ", " + envelopeYmax +
		   	   "],\n\t\"tile\": [" + tileRow + ", " + tileColumn + "]}";
		 */
	}

	/*
	@Override
	public boolean equals(Object obj) {

		try {
			CityObject4JSON cityObject4Json = (CityObject4JSON) obj;
			return this.gmlId.equals(cityObject4Json.getGmlId());
		}
		catch (Exception e) {}
		return false;
	}

	@Override
	public int hashCode(){
		return this.gmlId.hashCode();
	}
	 */

	public void setEnvelope (double[] ordinatesArray) {
		if (ordinatesArray == null) return;
		envelopeXmin = ordinatesArray[0];
		envelopeYmin = ordinatesArray[1];
		envelopeXmax = ordinatesArray[3];
		envelopeYmax = ordinatesArray[4];
	}

	public void setEnvelopeXmin(double envelopeXmin) {
		this.envelopeXmin = envelopeXmin;
	}

	public double getEnvelopeXmin() {
		return envelopeXmin;
	}

	public void setEnvelopeXmax(double envelopeXmax) {
		this.envelopeXmax = envelopeXmax;
	}

	public double getEnvelopeXmax() {
		return envelopeXmax;
	}

	public void setEnvelopeYmin(double envelopeYmin) {
		this.envelopeYmin = envelopeYmin;
	}

	public double getEnvelopeYmin() {
		return envelopeYmin;
	}

	public void setEnvelopeYmax(double envelopeYmax) {
		this.envelopeYmax = envelopeYmax;
	}

	public double getEnvelopeYmax() {
		return envelopeYmax;
	}

	public void setTileRow(int tileRow) {
		this.tileRow = tileRow;
	}

	public int getTileRow() {
		return tileRow;
	}

	public void setTileColumn(int tileColumn) {
		this.tileColumn = tileColumn;
	}

	public int getTileColumn() {
		return tileColumn;
	}
	/*
	public String getGmlId() {
		return gmlId;
	}
	 */
}
