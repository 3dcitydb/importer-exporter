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
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.geometry.Point;
import org.citygml4j.impl.citygml.core.GeneralizationRelationImpl;
import org.citygml4j.impl.gml.geometry.primitives.EnvelopeImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.GeneralizationRelation;
import org.citygml4j.model.gml.geometry.primitives.Envelope;

import de.tub.citydb.config.Config;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.BoundingBoxFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.modules.common.filter.feature.GmlIdFilter;
import de.tub.citydb.modules.common.filter.feature.GmlNameFilter;
import de.tub.citydb.util.Util;

public class DBGeneralization implements DBExporter {
	private final ExportFilter exportFilter;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psGeneralization;

	// filter
	private FeatureClassFilter featureClassFilter;
	private GmlIdFilter featureGmlIdFilter;
	private GmlNameFilter featureGmlNameFilter;
	private BoundingBoxFilter boundingBoxFilter;

	private boolean transformCoords;

	public DBGeneralization(Connection connection, ExportFilter exportFilter, Config config) throws SQLException {
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

		transformCoords = config.getInternal().isTransformCoordinates();
		if (!transformCoords) {	
			psGeneralization = connection.prepareStatement("select GMLID, CLASS_ID, ENVELOPE from CITYOBJECT where ID=?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			
			psGeneralization = connection.prepareStatement("select GMLID, CLASS_ID, " +
					"geodb_util.transform_or_null(co.ENVELOPE, " + srid + ") AS ENVELOPE " +
			"from CITYOBJECT where ID=?");
		}
	}

	public void read(AbstractCityObject cityObject, long cityObjectId, HashSet<Long> generalizesToSet) throws SQLException {		
		for (Long generalizationId : generalizesToSet) {
			ResultSet rs = null;

			try {
				psGeneralization.setLong(1, generalizationId);
				rs = psGeneralization.executeQuery();

				if (rs.next()) {
					String gmlId = rs.getString("GMLID");			
					if (rs.wasNull() || gmlId == null)
						continue;

					int classId = rs.getInt("CLASS_ID");			
					CityGMLClass type = Util.classId2cityObject(classId);			
					STRUCT struct = (STRUCT)rs.getObject("ENVELOPE");

					if (!rs.wasNull() && struct != null && boundingBoxFilter.isActive()) {
						JGeometry jGeom = JGeometry.load(struct);
						Envelope env = new EnvelopeImpl();

						double[] points = jGeom.getOrdinatesArray();
						Point lower = new Point(points[0], points[1], points[2]);
						Point upper = new Point(points[3], points[4], points[5]);

						env.setLowerCorner(lower);
						env.setUpperCorner(upper);

						if (boundingBoxFilter.filter(env))
							continue;
					}	

					if (featureGmlIdFilter.isActive() && featureGmlIdFilter.filter(gmlId))
						continue;

					if (featureClassFilter.isActive() && featureClassFilter.filter(type))
						continue;

					if (featureGmlNameFilter.isActive()) {
						// we need to get the gml:name of the feature 
						// we only check top-level features
						TableEnum table = null;

						switch (type) {
						case BUILDING:
							table = TableEnum.BUILDING;
							break;
						case CITY_FURNITURE:
							table = TableEnum.CITY_FURNITURE;
							break;
						case LAND_USE:
							table = TableEnum.LAND_USE;
							break;
						case WATER_BODY:
							table = TableEnum.WATERBODY;
							break;
						case PLANT_COVER:
							table = TableEnum.SOLITARY_VEGETAT_OBJECT;
							break;
						case SOLITARY_VEGETATION_OBJECT:
							table = TableEnum.PLANT_COVER;
							break;
						case TRANSPORTATION_COMPLEX:
						case ROAD:
						case RAILWAY:
						case TRACK:
						case SQUARE:
							table = TableEnum.TRANSPORTATION_COMPLEX;
							break;
						case RELIEF_FEATURE:
							table = TableEnum.RELIEF_FEATURE;
							break;
						case GENERIC_CITY_OBJECT:
							table = TableEnum.GENERIC_CITYOBJECT;
							break;
						case CITY_OBJECT_GROUP:
							table = TableEnum.CITYOBJECTGROUP;
							break;
						}

						if (table != null) {
							Statement stmt = null;
							ResultSet nameRs = null;

							try {
								String query = "select NAME from " + table.toString() + " where ID=" + generalizationId;
								stmt = connection.createStatement();

								nameRs = stmt.executeQuery(query);
								if (nameRs.next()) {
									String gmlName = nameRs.getString("NAME");
									if (gmlName != null && featureGmlNameFilter.filter(gmlName))
										continue;
								}

							} catch (SQLException sqlEx) {
								continue;
							} finally {
								if (nameRs != null) {
									try {
										nameRs.close();
									} catch (SQLException sqlEx) {
										//
									}

									nameRs = null;
								}

								if (stmt != null) {
									try {
										stmt.close();
									} catch (SQLException sqlEx) {
										//
									}

									stmt = null;
								}
							}
						}
					}

					GeneralizationRelation generalizesTo = new GeneralizationRelationImpl();
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
