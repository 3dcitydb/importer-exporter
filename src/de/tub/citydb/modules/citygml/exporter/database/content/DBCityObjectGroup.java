/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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

import org.citygml4j.impl.citygml.cityobjectgroup.CityObjectGroupImpl;
import org.citygml4j.impl.citygml.cityobjectgroup.CityObjectGroupMemberImpl;
import org.citygml4j.impl.citygml.cityobjectgroup.CityObjectGroupParentImpl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.geometry.GeometryPropertyImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupMember;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupParent;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.config.Config;
import de.tub.citydb.util.Util;

public class DBCityObjectGroup implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psCityObjectGroup;
	private PreparedStatement psParentGmlId;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;

	private boolean transformCoords;

	public DBCityObjectGroup(Connection connection, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		transformCoords = config.getInternal().isTransformCoordinates();

		if (!transformCoords) {		
			psCityObjectGroup = connection.prepareStatement("select grp.ID, grp.NAME, grp.NAME_CODESPACE, grp.DESCRIPTION, grp.CLASS, grp.FUNCTION, grp.USAGE, grp.GEOMETRY, grp.SURFACE_GEOMETRY_ID, grp.PARENT_CITYOBJECT_ID, " +
					"gtc.CITYOBJECT_ID, gtc.ROLE from CITYOBJECTGROUP grp " +
					"left join GROUP_TO_CITYOBJECT gtc on gtc.CITYOBJECTGROUP_ID=grp.ID where grp.ID=?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();

			psCityObjectGroup = connection.prepareStatement("select grp.ID, grp.NAME, grp.NAME_CODESPACE, grp.DESCRIPTION, grp.CLASS, grp.FUNCTION, grp.USAGE, " +
					"geodb_util.transform_or_null(grp.GEOMETRY, " + srid + ") AS GEOMETRY, " +
					"grp.SURFACE_GEOMETRY_ID, grp.PARENT_CITYOBJECT_ID, " +
					"gtc.CITYOBJECT_ID, gtc.ROLE from CITYOBJECTGROUP grp " +
					"left join GROUP_TO_CITYOBJECT gtc on gtc.CITYOBJECTGROUP_ID=grp.ID where grp.ID=?");
		}

		psParentGmlId = connection.prepareStatement("select GMLID from CITYOBJECT where CLASS_ID=23 AND ID=?");

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		CityObjectGroup cityObjectGroup = new CityObjectGroupImpl();
		long cityObjectGroupId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(cityObjectGroup, cityObjectGroupId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psCityObjectGroup.setLong(1, cityObjectGroupId);
			rs = psCityObjectGroup.executeQuery();
			boolean isInited = false;

			while (rs.next()) {
				if (!isInited) {
					String gmlName = rs.getString("NAME");
					String gmlNameCodespace = rs.getString("NAME_CODESPACE");

					Util.dbGmlName2featureName(cityObjectGroup, gmlName, gmlNameCodespace);

					String description = rs.getString("DESCRIPTION");
					if (description != null) {
						StringOrRef stringOrRef = new StringOrRefImpl();
						stringOrRef.setValue(description);
						cityObjectGroup.setDescription(stringOrRef);
					}

					String clazz = rs.getString("CLASS");
					if (clazz != null) {
						cityObjectGroup.setClazz(clazz);
					}

					String function = rs.getString("FUNCTION");
					if (function != null) 
						cityObjectGroup.addFunction(function);

					String usage = rs.getString("USAGE");
					if (usage != null) 
						cityObjectGroup.addUsage(usage);

					long lodGeometryId = rs.getLong("SURFACE_GEOMETRY_ID");
					if (!rs.wasNull() && lodGeometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodGeometryId);

						if (geometry != null) {
							GeometryProperty<AbstractGeometry> geometryProperty = new GeometryPropertyImpl<AbstractGeometry>();

							if (geometry.getAbstractGeometry() != null)
								geometryProperty.setGeometry(geometry.getAbstractGeometry());
							else
								geometryProperty.setHref(geometry.getTarget());

							cityObjectGroup.setGeometry(geometryProperty);
						}
					}

					long parentId = rs.getLong("PARENT_CITYOBJECT_ID");
					if (!rs.wasNull() && parentId != 0) {
						String gmlId = dbExporterManager.getGmlId(parentId, CityGMLClass.ABSTRACT_CITY_OBJECT);

						if (gmlId != null) {
							CityObjectGroupParent parent = new CityObjectGroupParentImpl();
							parent.setHref("#" + gmlId);
							cityObjectGroup.setGroupParent(parent);
						}
					}

					isInited = true;
				}

				long groupMemberId = rs.getLong("CITYOBJECT_ID");
				if (!rs.wasNull() && groupMemberId != 0) {
					String gmlId = dbExporterManager.getGmlId(groupMemberId, CityGMLClass.ABSTRACT_CITY_OBJECT);

					if (gmlId != null) {
						CityObjectGroupMember groupMember = new CityObjectGroupMemberImpl();
						groupMember.setHref("#" + gmlId);

						String role = rs.getString("ROLE");
						if (role != null)
							groupMember.setGroupRole(role);

						cityObjectGroup.addGroupMember(groupMember);
					} 
				}
			}

			dbExporterManager.print(cityObjectGroup);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psCityObjectGroup.close();
		psParentGmlId.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.CITYOBJECTGROUP;
	}

}
