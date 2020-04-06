/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.importer.util;

import org.citydb.log.Logger;
import org.citydb.util.CoreConstants;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;

import java.util.List;

public class RingValidator {
	private final Logger log = Logger.getInstance();

	public boolean validate(List<Double> coordinates, AbstractRing ring) {
		if (coordinates == null || ring.hasLocalProperty(CoreConstants.GEOMETRY_INVALID))
			return false;

		// check closedness
		if (coordinates.size() >= 9 && !isClosed(coordinates))
			log.warn(getGeometrySignature(ring) + ": Ring is not closed. Appending first coordinate to fix it.");

		// too few coordinates
		if (coordinates.size() / 3 < 4) {
			ring.setLocalProperty(CoreConstants.GEOMETRY_INVALID, "Too few coordinates");
			log.error(getGeometrySignature(ring) + ": Ring contains less than 4 coordinates and will not be imported.");
			return false;
		}

		return true;
	}

	private boolean isClosed(List<Double> coords) {
		Double x = coords.get(0);
		Double y = coords.get(1);
		Double z = coords.get(2);

		int nrOfPoints = coords.size();
		if (!x.equals(coords.get(nrOfPoints - 3)) ||
				!y.equals(coords.get(nrOfPoints - 2)) ||
				!z.equals(coords.get(nrOfPoints - 1))) {

			// repair unclosed ring...
			coords.add(x);
			coords.add(y);
			coords.add(z);

			return false;
		}

		return true;
	}

	public String getGeometrySignature(AbstractGeometry object) {
		StringBuilder signature = new StringBuilder("gml:").append(object.getGMLClass().toString());
		String gmlId = object.hasLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID) ?
				(String)object.getLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID) :
				object.getId();

		if (gmlId != null)
			signature.append(" '").append(gmlId).append("'");
		else
			signature.append(" (unknown gml:id)");

		return signature.toString();
	}
}
