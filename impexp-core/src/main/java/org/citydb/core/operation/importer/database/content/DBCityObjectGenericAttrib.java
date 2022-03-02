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
import org.citydb.core.operation.common.property.*;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.generics.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DBCityObjectGenericAttrib implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;
	private DBProperty propertyImporter;

	public DBCityObjectGenericAttrib(Connection batchConn, Config config, CityGMLImportManager importer) throws SQLException, CityGMLImportException {
		this.batchConn = batchConn;
		this.importer = importer;
		propertyImporter = importer.getImporter(DBProperty.class);
	}

	public void doImport(AbstractGenericAttribute genericAttribute, long cityObjectId) throws CityGMLImportException, SQLException {
		doImport(genericAttribute, 0, 0, cityObjectId);
	}

	protected void doImport(AbstractGenericAttribute genericAttribute, long parentId, long rootId, long cityObjectId) throws CityGMLImportException, SQLException {
		// attribute name may not be null
		if (!genericAttribute.isSetName())
			return;

		AbstractProperty property = buildProperty(genericAttribute);
		if (property != null) {
			propertyImporter.doImport(property, cityObjectId);
		}
	}

	private AbstractProperty buildProperty(AbstractGenericAttribute genericAttribute) {
		AbstractProperty property = null;
		if (genericAttribute.getCityGMLClass() == CityGMLClass.GENERIC_ATTRIBUTE_SET) {
			GenericAttributeSet attributeSet = (GenericAttributeSet)genericAttribute;
			if (attributeSet.getGenericAttribute().isEmpty()) {
				return null;
			}
			property = new ComplexProperty();
			for (AbstractGenericAttribute attribute : attributeSet.getGenericAttribute()) {
				AbstractProperty childProperty = buildProperty(attribute);
				if (childProperty != null) {
					((ComplexProperty)property).addChild(childProperty);
				}
			}
		} else {
			switch (genericAttribute.getCityGMLClass()) {
				case STRING_ATTRIBUTE:
					StringAttribute stringAttribute = (StringAttribute)genericAttribute;
					property = new StringProperty();
					((StringProperty)property).setValue(stringAttribute.getValue());
					break;
				case INT_ATTRIBUTE:
					IntAttribute intAttribute = (IntAttribute)genericAttribute;
					property = new IntegerProperty();
					((IntegerProperty)property).setValue(intAttribute.getValue());
					break;
				case DOUBLE_ATTRIBUTE:
					DoubleAttribute doubleAttribute = (DoubleAttribute)genericAttribute;
					property = new DoubleProperty();
					((DoubleProperty)property).setValue(doubleAttribute.getValue());
					break;
				case URI_ATTRIBUTE:
					UriAttribute uriAttribute = (UriAttribute)genericAttribute;
					property = new UriProperty();
					((UriProperty)property).setValue(uriAttribute.getValue());
					break;
				case DATE_ATTRIBUTE:
					DateAttribute dateAttribute = (DateAttribute)genericAttribute;
					property = new DateProperty();
					((DateProperty)property).setValue(OffsetDateTime.of(dateAttribute.getValue().atStartOfDay(), ZoneOffset.UTC));
					break;
				case MEASURE_ATTRIBUTE:
					MeasureAttribute measureAttribute = (MeasureAttribute)genericAttribute;
					property = new MeasureProperty();
					((MeasureProperty)property).setValue(measureAttribute.getValue().getValue());
					((MeasureProperty)property).setUom(measureAttribute.getValue().getUom());
					break;
			}
		}

		if (property != null) {
			property.setName(genericAttribute.getName());
			property.setNamespace("gen");
			property.setDataType("gen:" + genericAttribute.getClass().getSimpleName());
		}

		return property;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		// nothing to do
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		// nothing to do
	}

}
