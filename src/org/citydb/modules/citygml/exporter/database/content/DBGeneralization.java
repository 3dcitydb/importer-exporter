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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.feature.BoundingBoxFilter;
import org.citydb.modules.common.filter.feature.FeatureClassFilter;
import org.citydb.modules.common.filter.feature.GmlIdFilter;
import org.citydb.modules.common.filter.feature.GmlNameFilter;
import org.citydb.util.Util;
import org.citygml4j.geometry.Point;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.GeneralizationRelation;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.primitives.Envelope;

public class DBGeneralization implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final ExportFilter exportFilter;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psGeneralization;

	// filter
	private FeatureClassFilter featureClassFilter;
	private GmlIdFilter featureGmlIdFilter;
	private GmlNameFilter featureGmlNameFilter;
	private BoundingBoxFilter boundingBoxFilter;

	public DBGeneralization(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.exportFilter = exportFilter;
		this.config = config;
		this.connection = connection;

		init();
	}

	private void init() throws SQLException {
		featureClassFilter = exportFilter.getFeatureClassFilter();
		featureGmlIdFilter = exportFilter.getGmlIdFilter();
		featureGmlNameFilter = exportFilter.getGmlNameFilter();
		boundingBoxFilter = exportFilter.getBoundingBoxFilter();

		if (!config.getInternal().isTransformCoordinates()) {	
			psGeneralization = connection.prepareStatement("select GMLID, OBJECTCLASS_ID, NAME, ENVELOPE from CITYOBJECT where ID=?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select GMLID, OBJECTCLASS_ID, ")
			.append(transformOrNull).append("(co.ENVELOPE, ").append(srid).append(") AS ENVELOPE ")
			.append("from CITYOBJECT where ID=?");
			psGeneralization = connection.prepareStatement(query.toString());
		}
	}

	public void read(AbstractCityObject cityObject, long cityObjectId, HashSet<Long> generalizesToSet) throws SQLException {		
		for (Long generalizationId : generalizesToSet) {
			ResultSet rs = null;

			try {
				psGeneralization.setLong(1, generalizationId);
				rs = psGeneralization.executeQuery();

				if (rs.next()) {
					String gmlId = rs.getString(1);			
					if (rs.wasNull() || gmlId == null)
						continue;

					int classId = rs.getInt(2);
					if (rs.wasNull() || classId == 0)
						continue;
					
					CityGMLClass type = Util.classId2cityObject(classId);			
					
					String name = rs.getString(3);
					
					Object object = rs.getObject(4);
					if (!rs.wasNull() && object != null && boundingBoxFilter.isActive()) {
						GeometryObject geomObj = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getEnvelope(object);
						double[] coordinates = geomObj.getCoordinates(0);
						
						Envelope envelope = new Envelope();
						envelope.setLowerCorner(new Point(coordinates[0], coordinates[1], coordinates[2]));
						envelope.setUpperCorner(new Point(coordinates[3], coordinates[4], coordinates[5]));

						if (boundingBoxFilter.filter(envelope))
							continue;
					}	

					if (featureGmlIdFilter.isActive() && featureGmlIdFilter.filter(gmlId))
						continue;

					if (featureClassFilter.isActive() && featureClassFilter.filter(type))
						continue;
					
					if (featureGmlNameFilter.isActive()) {
						for (Code code : Util.string2codeList(name, null))
							if (code.getValue() != null && featureGmlNameFilter.filter(code.getValue()))
								continue;						
					}

					GeneralizationRelation generalizesTo = new GeneralizationRelation();
					generalizesTo.setHref("#" + gmlId);
					cityObject.addGeneralizesTo(generalizesTo);
				}
			} finally {
				if (rs != null)
					rs.close();
			}
		}
	}

	@Override
	public void close() throws SQLException {
		psGeneralization.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.GENERALIZATION;
	}

}
