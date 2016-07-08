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
