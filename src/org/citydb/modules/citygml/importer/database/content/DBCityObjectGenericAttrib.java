/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.citydb.api.geometry.GeometryObject;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.citygml.generics.DateAttribute;
import org.citygml4j.model.citygml.generics.DoubleAttribute;
import org.citygml4j.model.citygml.generics.GenericAttributeSet;
import org.citygml4j.model.citygml.generics.IntAttribute;
import org.citygml4j.model.citygml.generics.MeasureAttribute;
import org.citygml4j.model.citygml.generics.StringAttribute;
import org.citygml4j.model.citygml.generics.UriAttribute;

public class DBCityObjectGenericAttrib implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psAtomicGenericAttribute;
	private PreparedStatement psGenericAttributeSet;
	private PreparedStatement psGenericAttributeMember;
	private int batchCounter;

	public DBCityObjectGenericAttrib(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into CITYOBJECT_GENERICATTRIB (ID, PARENT_GENATTRIB_ID, ROOT_GENATTRIB_ID, ATTRNAME, DATATYPE, GENATTRIBSET_CODESPACE, CITYOBJECT_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?)");
		psGenericAttributeSet = batchConn.prepareStatement(stmt.toString());		

		stmt = new StringBuilder()
		.append("insert into CITYOBJECT_GENERICATTRIB (ID, ATTRNAME, DATATYPE, STRVAL, INTVAL, REALVAL, URIVAL, DATEVAL, UNIT, GEOMVAL, CITYOBJECT_ID, PARENT_GENATTRIB_ID, ROOT_GENATTRIB_ID) values ")
		.append("(").append(dbImporterManager.getDatabaseAdapter().getSQLAdapter().getNextSequenceValue(DBSequencerEnum.CITYOBJECT_GENERICATTRIB_ID_SEQ))
		.append(", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ");
		
		psGenericAttributeMember = batchConn.prepareStatement(new StringBuilder(stmt).append("?, ?)").toString());		
		psAtomicGenericAttribute = batchConn.prepareStatement(new StringBuilder(stmt).append("null, ")
		.append(dbImporterManager.getDatabaseAdapter().getSQLAdapter().getCurrentSequenceValue(DBSequencerEnum.CITYOBJECT_GENERICATTRIB_ID_SEQ)).append(")").toString());
	}

	public void insert(AbstractGenericAttribute genericAttribute, long cityObjectId) throws SQLException {
		insert(genericAttribute, 0, 0, cityObjectId);
	}

	public void insert(AbstractGenericAttribute genericAttribute,
			long parentId,
			long rootId,
			long cityObjectId) throws SQLException {
		// attribute name may not be null
		if (!genericAttribute.isSetName())
			return;

		if (genericAttribute.getCityGMLClass() == CityGMLClass.GENERIC_ATTRIBUTE_SET) {
			GenericAttributeSet attributeSet = (GenericAttributeSet)genericAttribute;
			
			// we do not import empty attribute sets
			if (attributeSet.getGenericAttribute().size() == 0)
				return;
			
			long attributeSetId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_GENERICATTRIB_ID_SEQ);
			if (rootId == 0)
				rootId = attributeSetId;
			
			psGenericAttributeSet.setLong(1, attributeSetId);
			psGenericAttributeSet.setLong(3, rootId);
			psGenericAttributeSet.setString(4, attributeSet.getName());
			psGenericAttributeSet.setInt(5, 7);
			psGenericAttributeSet.setString(6, attributeSet.getCodeSpace());
			psGenericAttributeSet.setLong(7, cityObjectId);
			if (parentId != 0)
				psGenericAttributeSet.setLong(2, parentId);
			else
				psGenericAttributeSet.setNull(2, Types.NULL);
			
			psGenericAttributeSet.addBatch();
			if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
				dbImporterManager.executeBatch(DBImporterEnum.CITYOBJECT_GENERICATTRIB);
			
			// insert members of the attribute set
			for (AbstractGenericAttribute attribute : attributeSet.getGenericAttribute())
				insert(attribute, attributeSetId, rootId, cityObjectId);			
		
		} else {
			@SuppressWarnings("resource")
			PreparedStatement ps = rootId == 0 ? psAtomicGenericAttribute : psGenericAttributeMember;
			ps.setString(1, genericAttribute.getName());

			switch (genericAttribute.getCityGMLClass()) {
			case STRING_ATTRIBUTE:
				ps.setInt(2, 1);

				StringAttribute stringAttribute = (StringAttribute)genericAttribute;
				if (stringAttribute.isSetValue())
					ps.setString(3, stringAttribute.getValue());
				else
					ps.setNull(3, Types.VARCHAR);

				ps.setNull(4, Types.NULL);
				ps.setNull(5, Types.NULL);
				ps.setNull(6, Types.VARCHAR);
				ps.setNull(7, Types.DATE);
				ps.setNull(8, Types.VARCHAR);
				break;
			case INT_ATTRIBUTE:
				ps.setInt(2, 2);

				IntAttribute intAttribute = (IntAttribute)genericAttribute;
				if (intAttribute.isSetValue())
					ps.setInt(4, intAttribute.getValue());
				else
					ps.setNull(4, Types.NULL);

				ps.setNull(3, Types.VARCHAR);
				ps.setNull(5, Types.NULL);
				ps.setNull(6, Types.VARCHAR);
				ps.setNull(7, Types.DATE);
				ps.setNull(8, Types.VARCHAR);
				break;
			case DOUBLE_ATTRIBUTE:
				ps.setInt(2, 3);

				DoubleAttribute doubleAttribute = (DoubleAttribute)genericAttribute;
				if (doubleAttribute.isSetValue())
					ps.setDouble(5, doubleAttribute.getValue());
				else
					ps.setNull(5, Types.NULL);

				ps.setNull(3, Types.VARCHAR);
				ps.setNull(4, Types.NULL);
				ps.setNull(6, Types.VARCHAR);
				ps.setNull(7, Types.DATE);
				ps.setNull(8, Types.VARCHAR);
				break;
			case URI_ATTRIBUTE:
				ps.setInt(2, 4);

				UriAttribute uriAttribute = (UriAttribute)genericAttribute;
				if (uriAttribute.isSetValue())
					ps.setString(6, uriAttribute.getValue());
				else
					ps.setNull(6, Types.VARCHAR);

				ps.setNull(3, Types.VARCHAR);
				ps.setNull(4, Types.NULL);
				ps.setNull(5, Types.NULL);
				ps.setNull(7, Types.DATE);
				ps.setNull(8, Types.VARCHAR);
				break;
			case DATE_ATTRIBUTE:
				ps.setInt(2, 5);

				DateAttribute dateAttribute = (DateAttribute)genericAttribute;
				if (dateAttribute.isSetValue())
					ps.setTimestamp(7, new Timestamp(dateAttribute.getValue().getTime().getTime()));
				else
					ps.setNull(7, Types.TIMESTAMP);

				ps.setNull(3, Types.VARCHAR);
				ps.setNull(4, Types.NULL);
				ps.setNull(5, Types.NULL);
				ps.setNull(6, Types.VARCHAR);
				ps.setNull(8, Types.VARCHAR);
				break;
			case MEASURE_ATTRIBUTE:
				ps.setInt(2, 6);

				MeasureAttribute measureAttribute = (MeasureAttribute)genericAttribute;
				if (measureAttribute.isSetValue()) {
					ps.setDouble(5, measureAttribute.getValue().getValue());
					ps.setString(8, measureAttribute.getValue().getUom());
				} else {
					ps.setNull(5, Types.NULL);
					ps.setNull(8, Types.VARCHAR);
				}

				ps.setNull(3, Types.VARCHAR);
				ps.setNull(4, Types.NULL);
				ps.setNull(6, Types.VARCHAR);
				ps.setNull(7, Types.DATE);
				break;
			default:
				ps.setNull(2, Types.NUMERIC);
			}

			ps.setNull(9, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());
			ps.setLong(10, cityObjectId);
			
			if (rootId != 0) {
				ps.setLong(11, parentId);
				ps.setLong(12, rootId);
			}

			ps.addBatch();
			if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
				dbImporterManager.executeBatch(DBImporterEnum.CITYOBJECT_GENERICATTRIB);
		}
	}

	public void insert(String attributeName, GeometryObject geometry, long cityObjectId) throws SQLException {
		if (attributeName == null || attributeName.length() == 0)
			return;

		psAtomicGenericAttribute.setString(1, attributeName);
		psAtomicGenericAttribute.setInt(2, 8);
		psAtomicGenericAttribute.setNull(3, Types.VARCHAR);
		psAtomicGenericAttribute.setNull(4, Types.NULL);
		psAtomicGenericAttribute.setNull(5, Types.NULL);
		psAtomicGenericAttribute.setNull(6, Types.VARCHAR);
		psAtomicGenericAttribute.setNull(7, Types.DATE);
		psAtomicGenericAttribute.setNull(8, Types.VARCHAR);	
		psAtomicGenericAttribute.setObject(9, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometry, batchConn));
		psAtomicGenericAttribute.setLong(10, cityObjectId);

		psAtomicGenericAttribute.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.CITYOBJECT_GENERICATTRIB);
	}

	@Override
	public void executeBatch() throws SQLException {
		psAtomicGenericAttribute.executeBatch();
		psGenericAttributeSet.executeBatch();
		psGenericAttributeMember.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psAtomicGenericAttribute.close();
		psGenericAttributeSet.close();
		psGenericAttributeMember.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.CITYOBJECT_GENERICATTRIB;
	}

}
