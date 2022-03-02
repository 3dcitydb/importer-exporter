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
import org.citydb.core.database.schema.SequenceEnum;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.operation.common.property.*;
import org.citydb.core.operation.importer.CityGMLImportException;

import java.sql.*;

public class DBProperty implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psAtomicProperty;
	private PreparedStatement psComplexProperty;
	private PreparedStatement psPropertyMember;
	private int batchCounter;

	public DBProperty(Connection batchConn, Config config, CityGMLImportManager importer) throws SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		StringBuilder stmt = new StringBuilder().append("insert into " + schema + ".property (id, feature_id, relation_id, parent_id, root_id, namespace, name, " +
				"index_number, datatype, data_valtype, val_int, val_double, val_string, val_date, val_uri, val_geometry, val_surface_geometry, " +
				"val_implicitgeom_id, val_implicitgeom_refpoint, val_implicitgeom_transform, val_grid_coverage, val_appearance, " +
				"val_dynamizer, val_feature, val_feature_is_xlink, val_code, val_codelist, val_uom, val_complex, val_xml) " +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		psComplexProperty = batchConn.prepareStatement(stmt.toString());

		stmt = new StringBuilder()
				.append("insert into " + schema + ".property (id, feature_id, relation_id, namespace, name, " +
						"index_number, datatype, data_valtype, val_int, val_double, val_string, val_date, val_uri, val_geometry, val_surface_geometry, " +
						"val_implicitgeom_id, val_implicitgeom_refpoint, val_implicitgeom_transform, val_grid_coverage, val_appearance, " +
						"val_dynamizer, val_feature, val_feature_is_xlink, val_code, val_codelist, val_uom, val_complex, val_xml, parent_id, root_id) values ")
				.append("(").append(importer.getDatabaseAdapter().getSQLAdapter().getNextSequenceValue(SequenceEnum.PROPERTY_ID_SEQ.getName()))
				.append(", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ");

		psPropertyMember = batchConn.prepareStatement(stmt + "?, ?)");
		psAtomicProperty = batchConn.prepareStatement(stmt + "null, " +
				importer.getDatabaseAdapter().getSQLAdapter().getCurrentSequenceValue(SequenceEnum.PROPERTY_ID_SEQ.getName()) + ")");
	}

	public void doImport(AbstractProperty genericAttribute, long cityObjectId) throws CityGMLImportException, SQLException {
		doImport(genericAttribute, 0, 0, cityObjectId);
	}

	protected void doImport(AbstractProperty property, long parentId, long rootId, long cityObjectId) throws CityGMLImportException, SQLException {
		if (property instanceof ComplexProperty) {
			ComplexProperty complexProperty = (ComplexProperty)property;

			// we do not import empty attribute sets
			if (complexProperty.isEmpty())
				return;

			long propertyId = importer.getNextSequenceValue(SequenceEnum.PROPERTY_ID_SEQ.getName());
			if (rootId == 0)
				rootId = propertyId;

			psComplexProperty.setLong(1, propertyId);

			// feature id
			psComplexProperty.setLong(2, cityObjectId);

			// relation id
			psComplexProperty.setNull(3, Types.NULL);

			// parent id
			if (parentId != 0)
				psComplexProperty.setLong(4, parentId);
			else
				psComplexProperty.setNull(4, Types.NULL);

			// root id
			psComplexProperty.setLong(5, rootId);

			// namespace
			psComplexProperty.setString(6, property.getNamespace());

			// name
			psComplexProperty.setString(7, property.getName());

			// index number
			psComplexProperty.setNull(8, property.getIndexNumber());

			// datatype
			psComplexProperty.setString(9, property.getDataType());

			// data val type
			psComplexProperty.setInt(10, property.getType().getValue());

			// other val columns
			psComplexProperty.setNull(11, Types.NULL);
			psComplexProperty.setNull(12, Types.NULL);
			psComplexProperty.setNull(13, Types.VARCHAR);
			psComplexProperty.setNull(14, Types.DATE);
			psComplexProperty.setNull(15, Types.VARCHAR);
			psComplexProperty.setNull(16, Types.NULL);
			psComplexProperty.setNull(17, Types.NULL);
			psComplexProperty.setNull(18, Types.NULL);
			psComplexProperty.setNull(19, Types.NULL);
			psComplexProperty.setNull(20, Types.NULL);
			psComplexProperty.setNull(21, Types.NULL);
			psComplexProperty.setNull(22, Types.NULL);
			psComplexProperty.setNull(23, Types.NULL);
			psComplexProperty.setNull(24, Types.NULL);
			psComplexProperty.setNull(25, Types.NULL);
			psComplexProperty.setNull(26, Types.NULL);
			psComplexProperty.setNull(27, Types.NULL);
			psComplexProperty.setNull(28, Types.NULL);
			psComplexProperty.setNull(29, Types.NULL);
			psComplexProperty.setNull(30, Types.NULL);

			psComplexProperty.addBatch();
			if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
				importer.executeBatch(TableEnum.PROPERTY);

			// insert members of the attribute set
			for (AbstractProperty prop : complexProperty.getChildren()) {
				doImport(prop, propertyId, rootId, cityObjectId);
			}
		} else {
			@SuppressWarnings("resource")
			PreparedStatement ps = rootId == 0 ? psAtomicProperty : psPropertyMember;

			// feature id
			ps.setLong(1, cityObjectId);

			// relation id
			ps.setNull(2, Types.NULL);

			// namespace
			ps.setString(3, property.getNamespace());

			// name
			ps.setString(4, property.getName());

			// index number
			ps.setNull(5, property.getIndexNumber());

			// datatype
			ps.setString(6, property.getDataType());

			// data val type
			ps.setInt(7, property.getType().getValue());

			switch (property.getType()) {
				case INTEGER:
					// set null to all other val columns
					IntegerProperty integerProperty = (IntegerProperty) property;
					if (integerProperty.isSetValue())
						ps.setInt(8, integerProperty.getValue());
					else
						ps.setNull(8, Types.NULL);
					ps.setNull(9, Types.NULL);
					ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case DOUBLE:
					ps.setNull(8, Types.NULL);
					DoubleProperty doubleProperty = (DoubleProperty) property;
					if (doubleProperty.isSetValue())
						ps.setDouble(9, doubleProperty.getValue());
					else
						ps.setNull(9, Types.NULL);
					ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case STRING:
					// set null to all other val columns
					ps.setNull(8, Types.NULL);
					ps.setNull(9, Types.NULL);
					StringProperty stringProperty = (StringProperty) property;
					if (stringProperty.isSetValue())
						ps.setString(10, stringProperty.getValue());
					else
						ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case DATE:
					// set null to all other val columns
					ps.setNull(8, Types.NULL);
					ps.setNull(9, Types.NULL);
					ps.setNull(10, Types.VARCHAR);
					DateProperty dateProperty = (DateProperty) property;
					if (dateProperty.isSetValue())
						ps.setObject(11, dateProperty.getValue());
					else
						ps.setNull(11, Types.TIMESTAMP);
					ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case URI:
					// set null to all other val columns
					ps.setNull(8, Types.NULL);
					ps.setNull(9, Types.NULL);
					ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					UriProperty uriProperty = (UriProperty) property;
					if (uriProperty.isSetValue())
						ps.setString(12, uriProperty.getValue());
					else
						ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case GEOMETRY:
					// set null to all other val columns
					ps.setNull(8, Types.NULL);
					ps.setNull(9, Types.NULL);
					ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					ps.setNull(12, Types.VARCHAR);
					GeometryProperty geometryProperty = (GeometryProperty) property;
					if (geometryProperty.isSetValue())
						ps.setObject(13, geometryProperty.getValue());
					else
						ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case SURFACE_GEOMETRY:
					// set null to all other val columns
					ps.setNull(8, Types.NULL);
					ps.setNull(9, Types.NULL);
					ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					SurfaceGeometryProperty surfaceGeometryProperty = (SurfaceGeometryProperty) property;
					if (surfaceGeometryProperty.isSetValue())
						ps.setLong(14, surfaceGeometryProperty.getValue());
					else
						ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case APPEARANCE:
					// set null to all other val columns
					ps.setNull(8, Types.NULL);
					ps.setNull(9, Types.NULL);
					ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					AppearanceProperty appearanceProperty = (AppearanceProperty) property;
					if (appearanceProperty.isSetValue())
						ps.setLong(19, appearanceProperty.getValue());
					else
						ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case FEATURE:
					// set null to all other val columns
					ps.setNull(8, Types.NULL);
					ps.setNull(9, Types.NULL);
					ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					FeatureProperty featureProperty = (FeatureProperty) property;
					if (featureProperty.isSetValue())
						ps.setLong(21, featureProperty.getValue());
					else
						ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case CODE_LIST:
					// set null to all other val columns
					ps.setNull(8, Types.NULL);
					ps.setNull(9, Types.NULL);
					ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					CodeListProperty codeListProperty = (CodeListProperty) property;
					if (codeListProperty.isSetValue())
						ps.setString(23, codeListProperty.getValue());
					else
						ps.setNull(23, Types.VARCHAR);
					if (codeListProperty.isSetCodeList())
						ps.setInt(24, codeListProperty.getCodeList());
					else
						ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case MEASURE:
					MeasureProperty measureProperty = (MeasureProperty) property;
					ps.setNull(8, Types.NULL);
					if (measureProperty.isSetValue())
						ps.setDouble(9, measureProperty.getValue());
					else
						ps.setNull(9, Types.VARCHAR);
					ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					if (measureProperty.isSetUom())
						ps.setString(25, measureProperty.getUom());
					else
						ps.setNull(25, Types.VARCHAR);
					ps.setNull(26, Types.NULL);
					ps.setNull(27, Types.NULL);
					break;
				case XML_CONTENT:
					// set null to all other val columns
					ps.setNull(8, Types.NULL);
					ps.setNull(9, Types.NULL);
					ps.setNull(10, Types.VARCHAR);
					ps.setNull(11, Types.DATE);
					ps.setNull(12, Types.VARCHAR);
					ps.setNull(13, Types.NULL);
					ps.setNull(14, Types.NULL);
					ps.setNull(15, Types.NULL);
					ps.setNull(16, Types.NULL);
					ps.setNull(17, Types.NULL);
					ps.setNull(18, Types.NULL);
					ps.setNull(19, Types.NULL);
					ps.setNull(20, Types.NULL);
					ps.setNull(21, Types.NULL);
					ps.setNull(22, Types.NULL);
					ps.setNull(23, Types.NULL);
					ps.setNull(24, Types.NULL);
					ps.setNull(25, Types.NULL);
					ps.setNull(26, Types.NULL);
					XmlContentProperty xmlContentProperty = (XmlContentProperty) property;
					if (xmlContentProperty.isSetValue()) {
						SQLXML sqlxml = batchConn.createSQLXML();
						sqlxml.setString(xmlContentProperty.getValue());
						ps.setSQLXML(27, sqlxml);
					}
					else
						ps.setNull(27, Types.VARCHAR);
					break;
			}

			if (rootId != 0) {
				ps.setLong(28, parentId);
				ps.setLong(29, rootId);
			}

			ps.addBatch();
			if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
				importer.executeBatch(TableEnum.PROPERTY);
		}
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psAtomicProperty.executeBatch();
			psComplexProperty.executeBatch();
			psPropertyMember.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psAtomicProperty.close();
		psComplexProperty.close();
		psPropertyMember.close();
	}

}
