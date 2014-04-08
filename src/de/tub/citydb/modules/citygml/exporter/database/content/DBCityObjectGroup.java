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

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupMember;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupParent;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.config.Config;
import de.tub.citydb.modules.citygml.exporter.util.FeatureProcessException;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.ProjectionPropertyFilter;
import de.tub.citydb.util.Util;

public class DBCityObjectGroup implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psCityObjectGroup;
	private PreparedStatement psParentGmlId;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBOtherGeometry geometryExporter;

	private ProjectionPropertyFilter projectionFilter;

	public DBCityObjectGroup(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		projectionFilter = exportFilter.getProjectionPropertyFilter(CityGMLClass.CITY_OBJECT_GROUP);

		init();
	}

	private void init() throws SQLException {
		if (!config.getInternal().isTransformCoordinates()) {		
			StringBuilder query = new StringBuilder()
			.append("select grp.CLASS, grp.CLASS_CODESPACE, grp.FUNCTION, grp.FUNCTION_CODESPACE, grp.USAGE, grp.USAGE_CODESPACE, ")
			.append("grp.BREP_ID, grp.OTHER_GEOM, grp.PARENT_CITYOBJECT_ID, ")
			.append("gtc.CITYOBJECT_ID, gtc.ROLE, co.GMLID from CITYOBJECTGROUP grp ")
			.append("left join GROUP_TO_CITYOBJECT gtc on gtc.CITYOBJECTGROUP_ID=grp.ID left join CITYOBJECT co on co.ID=gtc.CITYOBJECT_ID where grp.ID=?");
			psCityObjectGroup = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("geodb_util.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select grp.CLASS, grp.CLASS_CODESPACE, grp.FUNCTION, grp.FUNCTION_CODESPACE, grp.USAGE, grp.USAGE_CODESPACE, ")
			.append("grp.BREP_ID, ")
			.append(transformOrNull).append("(grp.OTHER_GEOM, ").append(srid).append(") AS OTHER_GEOM, ")
			.append("grp.PARENT_CITYOBJECT_ID, ")
			.append("gtc.CITYOBJECT_ID, gtc.ROLE, co.GMLID from CITYOBJECTGROUP grp ")
			.append("left join GROUP_TO_CITYOBJECT gtc on gtc.CITYOBJECTGROUP_ID=grp.ID left join CITYOBJECT co on co.ID=gtc.CITYOBJECT_ID where grp.ID=?");
			psCityObjectGroup = connection.prepareStatement(query.toString());
		}

		if (config.getProject().getExporter().getCityObjectGroup().isExportMemberAsXLinks())
			psParentGmlId = connection.prepareStatement("select GMLID from CITYOBJECT where ID = ?");

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		CityObjectGroup cityObjectGroup = new CityObjectGroup();
		long cityObjectGroupId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(cityObjectGroup, cityObjectGroupId, true, projectionFilter);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psCityObjectGroup.setLong(1, cityObjectGroupId);
			rs = psCityObjectGroup.executeQuery();
			boolean isInited = false;

			while (rs.next()) {
				if (!isInited) {
					if (projectionFilter.pass(CityGMLModuleType.CITY_OBJECT_GROUP, "class")) {
						String clazz = rs.getString(1);
						if (clazz != null) {
							Code code = new Code(clazz);
							code.setCodeSpace(rs.getString(2));
							cityObjectGroup.setClazz(code);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.CITY_OBJECT_GROUP, "function")) {
						String function = rs.getString(3);
						String functionCodeSpace = rs.getString(4);
						if (function != null)
							cityObjectGroup.setFunction(Util.string2codeList(function, functionCodeSpace));
					}

					if (projectionFilter.pass(CityGMLModuleType.CITY_OBJECT_GROUP, "usage")) {
						String usage = rs.getString(5);
						String usageCodeSpace = rs.getString(6);
						if (usage != null)
							cityObjectGroup.setUsage(Util.string2codeList(usage, usageCodeSpace));
					}

					// geometry
					if (projectionFilter.pass(CityGMLModuleType.CITY_OBJECT_GROUP, "geometry")) {
						long surfaceGeometryId = rs.getLong(7);
						Object geomObj = rs.getObject(8);
						if (surfaceGeometryId != 0 || geomObj != null) {
							GeometryProperty<AbstractGeometry> geometryProperty = null;
							if (surfaceGeometryId != 0) {
								DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
								if (geometry != null) {
									geometryProperty = new GeometryProperty<AbstractGeometry>();
									if (geometry.getAbstractGeometry() != null)
										geometryProperty.setGeometry(geometry.getAbstractGeometry());
									else
										geometryProperty.setHref(geometry.getTarget());
								}
							} else {
								GeometryObject geometry = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getGeometry(geomObj);
								if (geometry != null) {
									geometryProperty = new GeometryProperty<AbstractGeometry>();
									geometryProperty.setGeometry(geometryExporter.getPointOrCurveGeometry(geometry, true));
								}	
							}

							if (geometryProperty != null)
								cityObjectGroup.setGeometry(geometryProperty);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.CITY_OBJECT_GROUP, "parent")) {
						long parentId = rs.getLong(9);
						if (!rs.wasNull() && parentId != 0) {
							String gmlId = null;

							if (!config.getProject().getExporter().getCityObjectGroup().isExportMemberAsXLinks())
								gmlId = dbExporterManager.getUID(parentId, CityGMLClass.ABSTRACT_CITY_OBJECT);						
							else
								gmlId = getParentGMLId(parentId);

							if (gmlId != null) {
								CityObjectGroupParent parent = new CityObjectGroupParent();
								parent.setHref("#" + gmlId);
								cityObjectGroup.setGroupParent(parent);
							}
						}
					}

					isInited = true;
				}

				if (projectionFilter.pass(CityGMLModuleType.CITY_OBJECT_GROUP, "groupMember")) {
					long groupMemberId = rs.getLong(10);
					if (!rs.wasNull() && groupMemberId != 0) {
						String gmlId = null;

						if (!config.getProject().getExporter().getCityObjectGroup().isExportMemberAsXLinks())
							gmlId = dbExporterManager.getUID(groupMemberId, CityGMLClass.ABSTRACT_CITY_OBJECT);
						else
							gmlId = rs.getString(12);

						if (gmlId != null) {
							CityObjectGroupMember groupMember = new CityObjectGroupMember();
							groupMember.setHref("#" + gmlId);

							String role = rs.getString("ROLE");
							if (role != null)
								groupMember.setGroupRole(role);

							cityObjectGroup.addGroupMember(groupMember);
						} 
					}
				}
			}

			dbExporterManager.processFeature(cityObjectGroup);

			if (cityObjectGroup.isSetId() && config.getInternal().isRegisterGmlIdInCache())
				dbExporterManager.putUID(cityObjectGroup.getId(), cityObjectGroupId, cityObjectGroup.getCityGMLClass());

			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	private String getParentGMLId(long parentId) throws SQLException {
		ResultSet rs = null;

		try {
			psParentGmlId.setLong(1, parentId);
			rs = psParentGmlId.executeQuery();

			if (rs.next()) {
				String parentGMLId = rs.getString(1);
				if (!rs.wasNull() && parentGMLId.length() > 0) 
					return parentGMLId;
			}

			return null;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psCityObjectGroup.close();
		if (psParentGmlId != null)
			psParentGmlId.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.CITYOBJECTGROUP;
	}

}
