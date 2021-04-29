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
package org.citydb.citygml.importer.filter.selection.spatial;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.importer.SimpleBBOXMode;
import org.citydb.config.project.importer.SimpleBBOXOperator;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.query.filter.FilterException;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.sql.SQLException;

public class SimpleBBOXFilter {
	private BoundingBox bbox;
	private SimpleBBOXMode mode;

	public SimpleBBOXFilter(SimpleBBOXOperator bboxOperator) throws FilterException {
		if (bboxOperator == null || !bboxOperator.isSetExtent())
			throw new FilterException("The bbox operator must not be null.");

		bbox = bboxOperator.getExtent();
		this.mode = bboxOperator.getMode();
	}

	public void transform(DatabaseSrs targetSrs, AbstractDatabaseAdapter databaseAdapter) throws FilterException {
		if (!targetSrs.isSupported())
			throw new FilterException("The reference system " + targetSrs.getDescription() + " is not supported.");

		DatabaseSrs bboxSrs = bbox.isSetSrs() ? bbox.getSrs() : databaseAdapter.getConnectionMetaData().getReferenceSystem();
		if (targetSrs.getSrid() == bboxSrs.getSrid())
			return;

		try {
			bbox = databaseAdapter.getUtil().transform2D(bbox, bboxSrs, targetSrs);
		} catch (SQLException e) {
			throw new FilterException("Failed to transform bounding box to SRS " + targetSrs.getDescription() + ".", e);
		}
	}

	public boolean isSatisfiedBy(AbstractFeature feature) throws FilterException {
		if (!feature.isSetBoundedBy() || !feature.getBoundedBy().isSetEnvelope())
			return false;

		org.citygml4j.geometry.BoundingBox candidate = feature.getBoundedBy().getEnvelope().toBoundingBox();
		if (candidate == null)
			return false;

		if (mode == SimpleBBOXMode.WITHIN) {
			return (candidate.getLowerCorner().getX() >= bbox.getLowerCorner().getX() &&
					candidate.getLowerCorner().getY() >= bbox.getLowerCorner().getY() &&
					candidate.getUpperCorner().getX() <= bbox.getUpperCorner().getX() &&
					candidate.getUpperCorner().getY() <= bbox.getUpperCorner().getY());
		} else {
			return !(candidate.getLowerCorner().getX() >= bbox.getUpperCorner().getX() ||
					candidate.getLowerCorner().getY() >= bbox.getUpperCorner().getY() ||
					candidate.getUpperCorner().getX() <= bbox.getLowerCorner().getX() ||
					candidate.getUpperCorner().getY() <= bbox.getLowerCorner().getY());
		}
	}

}
