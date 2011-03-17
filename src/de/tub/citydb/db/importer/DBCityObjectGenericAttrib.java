/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.generics.GenericAttribute;
import org.citygml4j.model.citygml.generics.GenericDateAttribute;
import org.citygml4j.model.citygml.generics.GenericDoubleAttribute;
import org.citygml4j.model.citygml.generics.GenericIntAttribute;
import org.citygml4j.model.citygml.generics.GenericStringAttribute;
import org.citygml4j.model.citygml.generics.GenericUriAttribute;

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
			"(CITYOBJECT_GENERICATT_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, null, null, ?, null)");
	}

	public void insert(GenericAttribute genericAttribute, long cityObjectId) throws SQLException {
		// attribute name may not be null
		if (!genericAttribute.isSetName())
			return;
			
		psGenericAttribute.setString(1, genericAttribute.getName());

		switch (genericAttribute.getCityGMLClass()) {
		case STRINGATTRIBUTE:
			psGenericAttribute.setInt(2, 1);

			GenericStringAttribute stringAttribute = (GenericStringAttribute)genericAttribute;
			if (stringAttribute.isSetValue())
				psGenericAttribute.setString(3, stringAttribute.getValue());
			else
				psGenericAttribute.setNull(3, Types.VARCHAR);

			psGenericAttribute.setNull(4, 0);
			psGenericAttribute.setNull(5, 0);
			psGenericAttribute.setNull(6, Types.VARCHAR);
			psGenericAttribute.setNull(7, Types.DATE);
			break;
		case INTATTRIBUTE:
			psGenericAttribute.setInt(2, 2);

			GenericIntAttribute intAttribute = (GenericIntAttribute)genericAttribute;
			if (intAttribute.isSetValue())
				psGenericAttribute.setInt(4, intAttribute.getValue());
			else
				psGenericAttribute.setNull(4, 0);

			psGenericAttribute.setNull(3, Types.VARCHAR);
			psGenericAttribute.setNull(5, 0);
			psGenericAttribute.setNull(6, Types.VARCHAR);
			psGenericAttribute.setNull(7, Types.DATE);
			break;
		case DOUBLEATTRIBUTE:
			psGenericAttribute.setInt(2, 3);

			GenericDoubleAttribute doubleAttribute = (GenericDoubleAttribute)genericAttribute;
			if (doubleAttribute.isSetValue())
				psGenericAttribute.setDouble(5, doubleAttribute.getValue());
			else
				psGenericAttribute.setNull(5, 0);

			psGenericAttribute.setNull(3, Types.VARCHAR);
			psGenericAttribute.setNull(4, 0);
			psGenericAttribute.setNull(6, Types.VARCHAR);
			psGenericAttribute.setNull(7, Types.DATE);
			break;
		case URIATTRIBUTE:
			psGenericAttribute.setInt(2, 4);

			GenericUriAttribute uriAttribute = (GenericUriAttribute)genericAttribute;
			if (uriAttribute.isSetValue())
				psGenericAttribute.setString(6, uriAttribute.getValue());
			else
				psGenericAttribute.setNull(6, Types.VARCHAR);

			psGenericAttribute.setNull(3, Types.VARCHAR);
			psGenericAttribute.setNull(4, 0);
			psGenericAttribute.setNull(5, 0);
			psGenericAttribute.setNull(7, Types.DATE);
			break;
		case DATEATTRIBUTE:
			psGenericAttribute.setInt(2, 5);

			GenericDateAttribute dateAttribute = (GenericDateAttribute)genericAttribute;
			if (dateAttribute.isSetValue())
				psGenericAttribute.setDate(7, new Date(dateAttribute.getValue().toGregorianCalendar().getTimeInMillis()));
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

		// cityobjectId
		psGenericAttribute.setLong(8, cityObjectId);

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
