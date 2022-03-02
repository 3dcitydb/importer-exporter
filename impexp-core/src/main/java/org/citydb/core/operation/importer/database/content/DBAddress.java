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
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.file.InputFile;
import org.citydb.core.operation.common.property.FeatureProperty;
import org.citydb.core.operation.common.property.GeometryProperty;
import org.citydb.core.operation.common.property.XmlContentProperty;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.util.GeometryConverter;
import org.citydb.core.util.CoreConstants;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.module.xal.XALModuleType;
import org.citygml4j.model.xal.*;
import org.citygml4j.util.walker.XALWalker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DBAddress implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private DBFeature featureImporter;
	private GeometryConverter geometryConverter;
	private DBProperty propertyImporter;

	public DBAddress(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		geometryConverter = importer.getGeometryConverter();
		featureImporter = importer.getImporter(DBFeature.class);
		propertyImporter = importer.getImporter(DBProperty.class);
	}

	public long doImport(Address address) throws CityGMLImportException, SQLException {
		return doImport(address, 0);
	}

	public long doImport(Address address, long parentId) throws CityGMLImportException, SQLException {
		if (!address.isSetXalAddress() || !address.getXalAddress().isSetAddressDetails()) {
			importer.logOrThrowErrorMessage(importer.getObjectSignature(address) + ": Skipping address due to missing xAL address details.");
			return 0;
		}

		FeatureType featureType = importer.getFeatureType(address);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		long addressId = featureImporter.doImport(address, featureType);

		// get XML representation of <xal:AddressDetails>
		String xalSource = importer.marshalObject(address.getXalAddress().getAddressDetails(), XALModuleType.CORE);
		if (xalSource != null && !xalSource.isEmpty()) {
			XmlContentProperty xalAddress = new XmlContentProperty();
			xalAddress.setName("xalAddress");
			xalAddress.setNamespace("core");
			xalAddress.setDataType("xAL:AddressDetails");
			xalAddress.setValue(xalSource);
			propertyImporter.doImport(xalAddress, addressId);
		}

		// multiPoint geometry
		if (address.isSetMultiPoint()) {
			GeometryObject multiPoint = geometryConverter.getMultiPoint(address.getMultiPoint());
			if (multiPoint != null) {
				Object multiPointObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiPoint, batchConn);
				GeometryProperty multiPointProperty = new GeometryProperty();
				multiPointProperty.setName("multiPoint");
				multiPointProperty.setNamespace("core");
				multiPointProperty.setDataType("gml:MultiPoint");
				multiPointProperty.setValue(multiPointObj);
				propertyImporter.doImport(multiPointProperty, addressId);
			}
		}

		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(address, addressId, featureType);

		return addressId;
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
