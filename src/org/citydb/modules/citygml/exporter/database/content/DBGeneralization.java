/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
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
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("geodb_util.transform_or_null");

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
