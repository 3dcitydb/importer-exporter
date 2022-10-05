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
package org.citydb.core.operation.importer.database.content;

import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.schema.SequenceEnum;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.generics.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DBCityObjectGenericAttrib implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psAtomicGenericAttribute;
	private PreparedStatement psGenericAttributeSet;
	private PreparedStatement psGenericAttributeMember;
	private int batchCounter;

	public DBCityObjectGenericAttrib(Connection batchConn, Config config, CityGMLImportManager importer) throws SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		StringBuilder stmt = new StringBuilder()
				.append("insert into ").append(schema).append(".cityobject_genericattrib (id, parent_genattrib_id, root_genattrib_id, attrname, datatype, genattribset_codespace, cityobject_id) values ")
				.append("(?, ?, ?, ?, ?, ?, ?)");
		psGenericAttributeSet = batchConn.prepareStatement(stmt.toString());		

		stmt = new StringBuilder()
				.append("insert into ").append(schema).append(".cityobject_genericattrib (id, attrname, datatype, strval, intval, realval, urival, dateval, unit, cityobject_id, parent_genattrib_id, root_genattrib_id) values ")
				.append("(").append(importer.getDatabaseAdapter().getSQLAdapter().getNextSequenceValue(SequenceEnum.CITYOBJECT_GENERICATTRIB_ID_SEQ.getName()))
				.append(", ?, ?, ?, ?, ?, ?, ?, ?, ?, ");

		psGenericAttributeMember = batchConn.prepareStatement(stmt + "?, ?)");
		psAtomicGenericAttribute = batchConn.prepareStatement(stmt + "null, " +
				importer.getDatabaseAdapter().getSQLAdapter().getCurrentSequenceValue(SequenceEnum.CITYOBJECT_GENERICATTRIB_ID_SEQ.getName()) + ")");
	}

	public void doImport(AbstractGenericAttribute genericAttribute, long cityObjectId) throws CityGMLImportException, SQLException {
		doImport(genericAttribute, 0, 0, cityObjectId);
	}

	protected void doImport(AbstractGenericAttribute genericAttribute, long parentId, long rootId, long cityObjectId) throws CityGMLImportException, SQLException {
		// attribute name may not be null
		if (!genericAttribute.isSetName())
			return;

		if (genericAttribute.getCityGMLClass() == CityGMLClass.GENERIC_ATTRIBUTE_SET) {
			GenericAttributeSet attributeSet = (GenericAttributeSet)genericAttribute;

			// we do not import empty attribute sets
			if (attributeSet.getGenericAttribute().isEmpty())
				return;

			long attributeSetId = importer.getNextSequenceValue(SequenceEnum.CITYOBJECT_GENERICATTRIB_ID_SEQ.getName());
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
			if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
				importer.executeBatch(TableEnum.CITYOBJECT_GENERICATTRIB);

			// insert members of the attribute set
			for (AbstractGenericAttribute attribute : attributeSet.getGenericAttribute())
				doImport(attribute, attributeSetId, rootId, cityObjectId);			

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
					ps.setObject(7, OffsetDateTime.of(dateAttribute.getValue().atStartOfDay(), ZoneOffset.UTC));
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

			ps.setLong(9, cityObjectId);

			if (rootId != 0) {
				ps.setLong(10, parentId);
				ps.setLong(11, rootId);
			}

			ps.addBatch();
			if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
				importer.executeBatch(TableEnum.CITYOBJECT_GENERICATTRIB);
		}
	}

	public void doImport(String attributeName, GeometryObject geometry, long cityObjectId) throws CityGMLImportException, SQLException {
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
		psAtomicGenericAttribute.setObject(9, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometry, batchConn));
		psAtomicGenericAttribute.setLong(10, cityObjectId);

		psAtomicGenericAttribute.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.CITYOBJECT_GENERICATTRIB);
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psAtomicGenericAttribute.executeBatch();
			psGenericAttributeSet.executeBatch();
			psGenericAttributeMember.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psAtomicGenericAttribute.close();
		psGenericAttributeSet.close();
		psGenericAttributeMember.close();
	}

}
