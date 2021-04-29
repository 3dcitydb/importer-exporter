/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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

import org.citydb.config.geometry.BoundingBox;

public class CityObject4JSON {
	private String gmlId;

	private BoundingBox envelope;
	private int tileRow = 0;
	private int tileColumn = 0;

	public CityObject4JSON (String gmlId) {
		this.gmlId = gmlId;
	}

	@Override
	public String toString() {
		return " \"" + gmlId + "\": {" +
				"\"envelope\": [" + getEnvelopeXmin() + "," + getEnvelopeYmin() + "," + getEnvelopeXmax() + "," + getEnvelopeYmax() + "]," +
				"\"tile\": [" + tileRow + "," + tileColumn + "]}";
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setEnvelope(BoundingBox envelope) {
		this.envelope = envelope;
	}

	public double getEnvelopeXmin() {
		return envelope != null ? envelope.getLowerCorner().getX() : 0;
	}

	public double getEnvelopeXmax() {
		return envelope != null ? envelope.getUpperCorner().getX() : 0;
	}

	public double getEnvelopeYmin() {
		return envelope != null ? envelope.getLowerCorner().getY() : 0;
	}

	public double getEnvelopeYmax() {
		return envelope != null ? envelope.getUpperCorner().getY() : 0;
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
}
