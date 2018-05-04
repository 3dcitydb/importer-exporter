/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.util.CoreConstants;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.SequenceEnum;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.module.xal.XALModuleType;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.model.xal.Country;
import org.citygml4j.model.xal.CountryName;
import org.citygml4j.model.xal.DependentLocality;
import org.citygml4j.model.xal.Locality;
import org.citygml4j.model.xal.LocalityName;
import org.citygml4j.model.xal.PostalCodeNumber;
import org.citygml4j.model.xal.Thoroughfare;
import org.citygml4j.model.xal.ThoroughfareName;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/*
 * PLEASE NOTE:
 * Currently, only addresses according to the following schema are supported and interpreted:
 * (taken from CityGML spec, OGC Doc No. 08-007r1) 
 * 		<Address>	
 * 			<xalAddress>
 * 				<!-- Bussardweg 7, 76356 Weingarten, Germany -->
 * 				<xAL:AddressDetails>
 * 					<xAL:Country>
 * 						<xAL:CountryName>Germany</xAL:CountryName>
 * 						<xAL:Locality Type="City">
 * 							<xAL:LocalityName>Weingarten</xAL:LocalityName>
 * 							<xAL:Thoroughfare Type="Street">
 * 								<xAL:ThoroughfareNumber>7</xAL:ThoroughfareNumber>
 * 								<xAL:ThoroughfareName>Bussardweg</xAL:ThoroughfareName>
 * 							</xAL:Thoroughfare>
 * 							<xAL:PostalCode>
 * 								<xAL:PostalCodeNumber>76356</xAL:PostalCodeNumber>
 * 							</xAL:PostalCode>
 * 						</xAL:Locality>
 * 					</xAL:Country>
 * 				</xAL:AddressDetails>
 * 			</xalAddress>
 * 		</Address>		
 * Additionally, <PostBox> elements are interpreted, if being modelled as child element of <Locality>
 */

public class DBAddress implements DBImporter {	
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psAddress;
	private DBAddressToBuilding addressToBuildingImporter;
	private DBAddressToBridge addressToBridgeImporter;
	private GeometryConverter geometryConverter;
	private AttributeValueJoiner valueJoiner;

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
			if (gmlIdCodespace != null && gmlIdCodespace.length() > 0)
				gmlIdCodespace = "'" + gmlIdCodespace + "', ";
			else
				gmlIdCodespace = null;
		}

		String stmt = "insert into " + schema + ".address (id, " + (hasGmlIdColumn ? "gmlid, " : "") + (gmlIdCodespace != null ? "gmlid_codespace, " : "") +
				"street, house_number, po_box, zip_code, city, country, multi_point, xal_source) values " +
				"(?, " + (hasGmlIdColumn ? "?, " : "") + (gmlIdCodespace != null ? gmlIdCodespace : "") + "?, ?, ?, ?, ?, ?, ?, ?)";
		psAddress = batchConn.prepareStatement(stmt);

		addressToBuildingImporter = importer.getImporter(DBAddressToBuilding.class);
		addressToBridgeImporter = importer.getImporter(DBAddressToBridge.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(Address address) throws CityGMLImportException, SQLException {
		long addressId = importer.getNextSequenceValue(SequenceEnum.ADDRESS_ID_SEQ.getName());

		FeatureType featureType = importer.getFeatureType(address);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		String streetAttr, houseNoAttr, poBoxAttr, zipCodeAttr, cityAttr, countryAttr, xalSource;
		streetAttr = houseNoAttr = poBoxAttr = zipCodeAttr = cityAttr = countryAttr = xalSource = null;
		GeometryObject multiPoint = null;		

		// retrieve address information from xAL address
		if (address.isSetXalAddress() && address.getXalAddress().isSetAddressDetails()) {
			AddressDetails addressDetails = address.getXalAddress().getAddressDetails();

			// try and parse <country> child element
			if (addressDetails.isSetCountry()) {			
				Country country = addressDetails.getCountry();

				// country name
				if (country.isSetCountryName())
					countryAttr = valueJoiner.join(",", country.getCountryName(), CountryName::getContent).result(0);

				// locality
				if (country.isSetLocality()) {
					Locality locality = country.getLocality();

					// check whether we deal with a city or a town
					if (locality.isSetType() 
							&& (locality.getType().toUpperCase().equals("CITY") || locality.getType().toUpperCase().equals("TOWN"))) {

						// city name
						if (locality.isSetLocalityName())
							cityAttr = valueJoiner.join(",", locality.getLocalityName(), LocalityName::getContent).result(0);

						// thoroughfare - just streets are supported
						if (locality.isSetThoroughfare()) {
							Thoroughfare thoroughfare = locality.getThoroughfare();

							// check whether we deal with a street
							if (thoroughfare.isSetType() 
									&& (thoroughfare.getType().toUpperCase().equals("STREET") || thoroughfare.getType().toUpperCase().equals("ROAD"))) {

								// street name
								if (thoroughfare.isSetThoroughfareName())
									streetAttr = valueJoiner.join(",", thoroughfare.getThoroughfareName(), ThoroughfareName::getContent).result(0);

								// house number - we do not support number ranges so far...						
								if (thoroughfare.isSetThoroughfareNumberOrThoroughfareNumberRange())
									houseNoAttr = valueJoiner.join(",", thoroughfare.getThoroughfareNumberOrThoroughfareNumberRange(),
											v -> v.isSetThoroughfareNumber() ? v.getThoroughfareNumber().getContent() : null).result(0);
							}
						}

						// dependent locality
						if (streetAttr == null && houseNoAttr == null && locality.isSetDependentLocality()) {
							DependentLocality dependentLocality = locality.getDependentLocality();

							if (dependentLocality.isSetType() 
									&& dependentLocality.getType().toUpperCase().equals("DISTRICT")
									&& dependentLocality.isSetThoroughfare()) {

								Thoroughfare thoroughfare = dependentLocality.getThoroughfare();

								// street name
								if (streetAttr == null && thoroughfare.isSetThoroughfareName())
									streetAttr = valueJoiner.join(",", thoroughfare.getThoroughfareName(), ThoroughfareName::getContent).result(0);

								// house number - we do not support number ranges so far...						
								if (houseNoAttr == null && thoroughfare.isSetThoroughfareNumberOrThoroughfareNumberRange())
									houseNoAttr = valueJoiner.join(",", thoroughfare.getThoroughfareNumberOrThoroughfareNumberRange(),
											v -> v.isSetThoroughfareNumber() ? v.getThoroughfareNumber().getContent() : null).result(0);
							}
						}

						// postal code
						if (locality.isSetPostalCode() && locality.getPostalCode().isSetPostalCodeNumber())
							zipCodeAttr = valueJoiner.join(",", locality.getPostalCode().getPostalCodeNumber(), 
									PostalCodeNumber::getContent).result(0);

						// post box
						if (locality.isSetPostBox() && locality.getPostBox().isSetPostBoxNumber())
							poBoxAttr = locality.getPostBox().getPostBoxNumber().getContent();
					}
				}

				// multiPoint geometry
				if (address.isSetMultiPoint())
					multiPoint = geometryConverter.getMultiPoint(address.getMultiPoint());
			}

			// get XML representation of <xal:AddressDetails>
			if (importXALSource)
				xalSource = importer.marshalObject(addressDetails, XALModuleType.CORE);

		} else 
			importer.logOrThrowErrorMessage(importer.getObjectSignature(address) +
					": Failed to interpret xAL address element.");

		// gml:id
		if (address.isSetId())
			address.setLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID, address.getId());

		if (replaceGmlId) {
			String gmlId = DefaultGMLIdManager.getInstance().generateUUID();

			// mapping entry
			if (address.isSetId())
				importer.putObjectUID(address.getId(), addressId, gmlId, featureType.getObjectClassId());

			address.setId(gmlId);

		} else {
			if (address.isSetId())
				importer.putObjectUID(address.getId(), addressId, featureType.getObjectClassId());
			else
				address.setId(DefaultGMLIdManager.getInstance().generateUUID());
		}

		int index = 1;			
		psAddress.setLong(index++, addressId);
		if (hasGmlIdColumn)
			psAddress.setString(index++, address.getId());

		psAddress.setString(index++, streetAttr);
		psAddress.setString(index++, houseNoAttr);
		psAddress.setString(index++, poBoxAttr);
		psAddress.setString(index++, zipCodeAttr);
		psAddress.setString(index++, cityAttr);
		psAddress.setString(index++, countryAttr);

		if (multiPoint != null) {
			Object multiPointObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiPoint, batchConn);
			psAddress.setObject(index++, multiPointObj);
		} else
			psAddress.setNull(index++, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

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
