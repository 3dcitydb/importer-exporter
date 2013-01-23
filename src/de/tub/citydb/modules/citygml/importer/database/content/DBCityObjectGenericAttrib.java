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
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.citygml.generics.DateAttribute;
import org.citygml4j.model.citygml.generics.DoubleAttribute;
import org.citygml4j.model.citygml.generics.IntAttribute;
import org.citygml4j.model.citygml.generics.StringAttribute;
import org.citygml4j.model.citygml.generics.UriAttribute;

import de.tub.citydb.config.internal.Internal;

public class DBCityObjectGenericAttrib implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psGenericAttribute;
	private int batchCounter;

	public DBCityObjectGenericAttrib(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psGenericAttribute = batchConn.prepareStatement("insert into CITYOBJECT_GENERICATTRIB (ID, ATTRNAME, DATATYPE, STRVAL, INTVAL, REALVAL, URIVAL, DATEVAL, GEOMVAL, BLOBVAL, CITYOBJECT_ID, SURFACE_GEOMETRY_ID) values " +
				"(CITYOBJECT_GENERICATT_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, null, ?, null)");
	}

	public void insert(AbstractGenericAttribute genericAttribute, long cityObjectId) throws SQLException {
		// attribute name may not be null
		if (!genericAttribute.isSetName())
			return;

		psGenericAttribute.setString(1, genericAttribute.getName());

		switch (genericAttribute.getCityGMLClass()) {
		case STRING_ATTRIBUTE:
			psGenericAttribute.setInt(2, 1);

			StringAttribute stringAttribute = (StringAttribute)genericAttribute;
			if (stringAttribute.isSetValue())
				psGenericAttribute.setString(3, stringAttribute.getValue());
			else
				psGenericAttribute.setNull(3, Types.VARCHAR);

			psGenericAttribute.setNull(4, 0);
			psGenericAttribute.setNull(5, 0);
			psGenericAttribute.setNull(6, Types.VARCHAR);
			psGenericAttribute.setNull(7, Types.DATE);
			break;
		case INT_ATTRIBUTE:
			psGenericAttribute.setInt(2, 2);

			IntAttribute intAttribute = (IntAttribute)genericAttribute;
			if (intAttribute.isSetValue())
				psGenericAttribute.setInt(4, intAttribute.getValue());
			else
				psGenericAttribute.setNull(4, 0);

			psGenericAttribute.setNull(3, Types.VARCHAR);
			psGenericAttribute.setNull(5, 0);
			psGenericAttribute.setNull(6, Types.VARCHAR);
			psGenericAttribute.setNull(7, Types.DATE);
			break;
		case DOUBLE_ATTRIBUTE:
			psGenericAttribute.setInt(2, 3);

			DoubleAttribute doubleAttribute = (DoubleAttribute)genericAttribute;
			if (doubleAttribute.isSetValue())
				psGenericAttribute.setDouble(5, doubleAttribute.getValue());
			else
				psGenericAttribute.setNull(5, 0);

			psGenericAttribute.setNull(3, Types.VARCHAR);
			psGenericAttribute.setNull(4, 0);
			psGenericAttribute.setNull(6, Types.VARCHAR);
			psGenericAttribute.setNull(7, Types.DATE);
			break;
		case URI_ATTRIBUTE:
			psGenericAttribute.setInt(2, 4);

			UriAttribute uriAttribute = (UriAttribute)genericAttribute;
			if (uriAttribute.isSetValue())
				psGenericAttribute.setString(6, uriAttribute.getValue());
			else
				psGenericAttribute.setNull(6, Types.VARCHAR);

			psGenericAttribute.setNull(3, Types.VARCHAR);
			psGenericAttribute.setNull(4, 0);
			psGenericAttribute.setNull(5, 0);
			psGenericAttribute.setNull(7, Types.DATE);
			break;
		case DATE_ATTRIBUTE:
			psGenericAttribute.setInt(2, 5);

			DateAttribute dateAttribute = (DateAttribute)genericAttribute;
			if (dateAttribute.isSetValue())
				psGenericAttribute.setDate(7, new Date(dateAttribute.getValue().getTimeInMillis()));
			else
				psGenericAttribute.setNull(7, Types.DATE);

			psGenericAttribute.setNull(3, Types.VARCHAR);
			psGenericAttribute.setNull(4, 0);
			psGenericAttribute.setNull(5, 0);
			psGenericAttribute.setNull(6, Types.VARCHAR);
			break;
		default:
			psGenericAttribute.setNull(2, Types.NUMERIC);
		}

		psGenericAttribute.setNull(8, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
		psGenericAttribute.setLong(9, cityObjectId);

		psGenericAttribute.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.CITYOBJECT_GENERICATTRIB);
	}

	public void insert(String attributeName, JGeometry geometry, long cityObjectId) throws SQLException {
		if (attributeName == null || attributeName.length() == 0)
			return;

		psGenericAttribute.setString(1, attributeName);
		psGenericAttribute.setInt(2, 6);
		psGenericAttribute.setNull(3, Types.VARCHAR);
		psGenericAttribute.setNull(4, 0);
		psGenericAttribute.setNull(5, 0);
		psGenericAttribute.setNull(6, Types.VARCHAR);
		psGenericAttribute.setNull(7, Types.DATE);
		psGenericAttribute.setLong(9, cityObjectId);
		
		STRUCT obj = SyncJGeometry.syncStore(geometry, batchConn);
		psGenericAttribute.setObject(8, obj);
		
		psGenericAttribute.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.CITYOBJECT_GENERICATTRIB);
	}

	@Override
	public void executeBatch() throws SQLException {
		psGenericAttribute.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psGenericAttribute.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.CITYOBJECT_GENERICATTRIB;
	}

}
