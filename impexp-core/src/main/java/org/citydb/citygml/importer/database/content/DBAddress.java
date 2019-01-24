/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.citygml.importer.database.content;

import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.SequenceEnum;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.util.CoreConstants;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.module.xal.XALModuleType;
import org.citygml4j.model.xal.CountryName;
import org.citygml4j.model.xal.LocalityName;
import org.citygml4j.model.xal.PostBoxNumber;
import org.citygml4j.model.xal.PostalCodeNumber;
import org.citygml4j.model.xal.ThoroughfareName;
import org.citygml4j.model.xal.ThoroughfareNumber;
import org.citygml4j.util.walker.XALWalker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DBAddress implements DBImporter {	
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psAddress;
	private DBAddressToBuilding addressToBuildingImporter;
	private DBAddressToBridge addressToBridgeImporter;
	private GeometryConverter geometryConverter;
	private XALAddressWalker addressWalker;

	private int batchCounter;
	private boolean importXALSource;
	private boolean hasGmlIdColumn;
	private boolean replaceGmlId;

	public DBAddress(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		importXALSource = config.getProject().getImporter().getAddress().isSetImportXAL();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasGmlIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(3, 1, 0) >= 0;
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		String gmlIdCodespace = null;

		if (hasGmlIdColumn) {
			gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();
			if (gmlIdCodespace != null)
				gmlIdCodespace = "'" + gmlIdCodespace + "', ";
		}

		String stmt = "insert into " + schema + ".address (id, " + (hasGmlIdColumn ? "gmlid, " : "") + (gmlIdCodespace != null ? "gmlid_codespace, " : "") +
				"street, house_number, po_box, zip_code, city, country, multi_point, xal_source) values " +
				"(?, " + (hasGmlIdColumn ? "?, " : "") + (gmlIdCodespace != null ? gmlIdCodespace : "") + "?, ?, ?, ?, ?, ?, ?, ?)";
		psAddress = batchConn.prepareStatement(stmt);

		addressToBuildingImporter = importer.getImporter(DBAddressToBuilding.class);
		addressToBridgeImporter = importer.getImporter(DBAddressToBridge.class);
		geometryConverter = importer.getGeometryConverter();
		addressWalker = new XALAddressWalker();
	}

	protected long doImport(Address address) throws CityGMLImportException, SQLException {
		if (!address.isSetXalAddress() || !address.getXalAddress().isSetAddressDetails())
			importer.logOrThrowErrorMessage(importer.getObjectSignature(address) + ": Failed to interpret xAL address element.");

		FeatureType featureType = importer.getFeatureType(address);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		long addressId = importer.getNextSequenceValue(SequenceEnum.ADDRESS_ID_SEQ.getName());

		// gml:id
		if (address.isSetId())
			address.setLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID, address.getId());

		if (replaceGmlId) {
			String gmlId = importer.generateNewGmlId();

			// mapping entry
			if (address.isSetId())
				importer.putObjectUID(address.getId(), addressId, gmlId, featureType.getObjectClassId());

			address.setId(gmlId);

		} else {
			if (address.isSetId())
				importer.putObjectUID(address.getId(), addressId, featureType.getObjectClassId());
			else
				address.setId(importer.generateNewGmlId());
		}

		int index = 1;			
		psAddress.setLong(index++, addressId);
		if (hasGmlIdColumn)
			psAddress.setString(index++, address.getId());

		// get address details
		addressWalker.reset();
		address.getXalAddress().getAddressDetails().accept(addressWalker);
		psAddress.setString(index++, addressWalker.street != null ? addressWalker.street.toString() : null);
		psAddress.setString(index++, addressWalker.houseNo != null ? addressWalker.houseNo.toString() : null);
		psAddress.setString(index++, addressWalker.poBox != null ? addressWalker.poBox.toString() : null);
		psAddress.setString(index++, addressWalker.zipCode != null ? addressWalker.zipCode.toString() : null);
		psAddress.setString(index++, addressWalker.city != null ? addressWalker.city.toString() : null);
		psAddress.setString(index++, addressWalker.country != null ? addressWalker.country.toString() : null);

		// multiPoint geometry
		GeometryObject multiPoint = null;
		if (address.isSetMultiPoint())
			multiPoint = geometryConverter.getMultiPoint(address.getMultiPoint());

		if (multiPoint != null) {
			Object multiPointObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiPoint, batchConn);
			psAddress.setObject(index++, multiPointObj);
		} else
			psAddress.setNull(index++, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		// get XML representation of <xal:AddressDetails>
		String xalSource = null;
		if (importXALSource)
			xalSource = importer.marshalObject(address.getXalAddress().getAddressDetails(), XALModuleType.CORE);

		if (xalSource != null && !xalSource.isEmpty())
			psAddress.setString(index++, xalSource);
		else
			psAddress.setNull(index++, Types.CLOB);

		psAddress.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.ADDRESS);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(address, addressId, featureType);

		return addressId;
	}

	public void importBuildingAddress(Address address, long parentId) throws CityGMLImportException, SQLException {
		long addressId = doImport(address);
		addressToBuildingImporter.doImport(addressId, parentId);
	}

	public void importBridgeAddress(Address address, long parentId) throws CityGMLImportException, SQLException {
		long addressId = doImport(address);
		addressToBridgeImporter.doImport(addressId, parentId);
	}

	private final class XALAddressWalker extends XALWalker {
		private StringBuilder street;
		private StringBuilder houseNo;
		private StringBuilder poBox;
		private StringBuilder zipCode;
		private StringBuilder city;
		private StringBuilder country;

		@Override
		public void reset() {
			super.reset();
			street = houseNo = poBox = zipCode = city = country = null;
		}

		@Override
		public void visit(CountryName countryName) {
			if (country == null)
				country = new StringBuilder(countryName.getContent());
			else
				country.append(",").append(countryName.getContent());

			super.visit(countryName);
		}

		@Override
		public void visit(LocalityName localityName) {
			if (city == null)
				city = new StringBuilder(localityName.getContent());
			else
				city.append(",").append(localityName.getContent());

			super.visit(localityName);
		}

		@Override
		public void visit(PostalCodeNumber postalCodeNumber) {
			if (zipCode == null)
				zipCode = new StringBuilder(postalCodeNumber.getContent());
			else
				zipCode.append(",").append(postalCodeNumber.getContent());

			super.visit(postalCodeNumber);
		}

		@Override
		public void visit(ThoroughfareName thoroughfareName) {
			if (street == null)
				street = new StringBuilder(thoroughfareName.getContent());
			else
				street.append(",").append(thoroughfareName.getContent());

			super.visit(thoroughfareName);
		}

		@Override
		public void visit(ThoroughfareNumber thoroughfareNumber) {
			if (houseNo == null)
				houseNo = new StringBuilder(thoroughfareNumber.getContent());
			else
				houseNo.append(",").append(thoroughfareNumber.getContent());

			super.visit(thoroughfareNumber);
		}

		@Override
		public void visit(PostBoxNumber postBoxNumber) {
			if (poBox == null)
				poBox = new StringBuilder(postBoxNumber.getContent());
			else
				poBox.append(",").append(postBoxNumber.getContent());

			super.visit(postBoxNumber);
		}
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psAddress.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psAddress.close();
	}

}
