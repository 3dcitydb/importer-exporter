/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.log.Logger;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.XalAddressProperty;
import org.citygml4j.model.module.xal.XALModuleType;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.model.xal.Country;
import org.citygml4j.model.xal.CountryName;
import org.citygml4j.model.xal.DependentLocality;
import org.citygml4j.model.xal.Locality;
import org.citygml4j.model.xal.LocalityName;
import org.citygml4j.model.xal.PostBox;
import org.citygml4j.model.xal.PostalCode;
import org.citygml4j.model.xal.PostalCodeNumber;
import org.citygml4j.model.xal.Thoroughfare;
import org.citygml4j.model.xal.ThoroughfareName;
import org.citygml4j.model.xal.ThoroughfareNumberOrRange;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

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
	private final Logger LOG = Logger.getInstance();
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psAddress;
	private DBAddressToBuilding addressToBuildingImporter;
	private DBAddressToBridge addressToBridgeImporter;
	private DBOtherGeometry geometryImporter;

	private int batchCounter;
	private boolean importXalSource;

	private boolean handleGmlId;
	private boolean replaceGmlId;

	public DBAddress(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		importXalSource = config.getProject().getImporter().getAddress().isSetImportXAL();
		handleGmlId = dbImporterManager.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(3, 1, 0) >= 0;
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		String gmlIdCodespace = null;

		if (handleGmlId) {
			gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();

			if (gmlIdCodespace != null && gmlIdCodespace.length() > 0)
				gmlIdCodespace = "'" + gmlIdCodespace + "', ";
			else
				gmlIdCodespace = null;
		}

		StringBuilder stmt = new StringBuilder()
				.append("insert into ADDRESS (ID, ").append(handleGmlId ? "GMLID, " : "").append(gmlIdCodespace != null ? "GMLID_CODESPACE, " : "").append("STREET, HOUSE_NUMBER, PO_BOX, ZIP_CODE, CITY, COUNTRY, MULTI_POINT, XAL_SOURCE) values ")
				.append("(?, ").append(handleGmlId ? "?, " : "").append(gmlIdCodespace != null ? gmlIdCodespace : "").append("?, ?, ?, ?, ?, ?, ?, ?)");
		psAddress = batchConn.prepareStatement(stmt.toString());

		addressToBuildingImporter = (DBAddressToBuilding)dbImporterManager.getDBImporter(DBImporterEnum.ADDRESS_TO_BUILDING);
		addressToBridgeImporter = (DBAddressToBridge)dbImporterManager.getDBImporter(DBImporterEnum.ADDRESS_TO_BRIDGE);
		geometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(Address address) throws SQLException {
		if (!address.isSetXalAddress() || !address.getXalAddress().isSetAddressDetails())
			return 0;

		XalAddressProperty xalAddressProperty = address.getXalAddress();
		AddressDetails addressDetails = xalAddressProperty.getAddressDetails();

		// ok, let's start
		long addressId = dbImporterManager.getDBId(DBSequencerEnum.ADDRESS_ID_SEQ);
		if (addressId == 0)
			return 0;
		
		boolean success = false;
		String streetAttr, houseNoAttr, poBoxAttr, zipCodeAttr, cityAttr, countryAttr, xalSource;
		streetAttr = houseNoAttr = poBoxAttr = zipCodeAttr = cityAttr = countryAttr = xalSource = null;
		GeometryObject multiPoint = null;		

		// try and interpret <country> child element
		if (addressDetails.isSetCountry()) {
			Country country = addressDetails.getCountry();

			// country name
			if (country.isSetCountryName()) {
				List<String> countryName = new ArrayList<String>();				
				for (CountryName name : country.getCountryName())
					countryName.add(name.getContent());

				countryAttr = Util.collection2string(countryName, ",");
			} 

			// locality
			if (country.isSetLocality()) {
				Locality locality = country.getLocality();

				// check whether we deal with a city or a town
				if (locality.isSetType() && 
						(locality.getType().toUpperCase().equals("CITY") ||
								locality.getType().toUpperCase().equals("TOWN"))) {

					// city name
					if (locality.isSetLocalityName()) {
						List<String> localityName = new ArrayList<String>();						
						for (LocalityName name : locality.getLocalityName())
							localityName.add(name.getContent());

						cityAttr = Util.collection2string(localityName, ",");
					} 

					// thoroughfare - just streets are supported
					if (locality.isSetThoroughfare()) {
						Thoroughfare thoroughfare = locality.getThoroughfare();

						// check whether we deal with a street
						if (thoroughfare.isSetType() && 
								(thoroughfare.getType().toUpperCase().equals("STREET") ||
										thoroughfare.getType().toUpperCase().equals("ROAD"))) {

							// street name
							if (thoroughfare.isSetThoroughfareName()) {
								List<String> fareName = new ArrayList<String>();								
								for (ThoroughfareName name : thoroughfare.getThoroughfareName())
									fareName.add(name.getContent());

								streetAttr = Util.collection2string(fareName, ",");
							}

							// house number - we do not support number ranges so far...						
							if (thoroughfare.isSetThoroughfareNumberOrThoroughfareNumberRange()) {
								List<String> houseNumber = new ArrayList<String>();								
								for (ThoroughfareNumberOrRange number : thoroughfare.getThoroughfareNumberOrThoroughfareNumberRange()) {
									if (number.isSetThoroughfareNumber())
										houseNumber.add(number.getThoroughfareNumber().getContent());
								}

								houseNoAttr = Util.collection2string(houseNumber, ",");
							}
						}
					}

					// dependent locality
					if (streetAttr == null && houseNoAttr == null && locality.isSetDependentLocality()) {
						DependentLocality dependentLocality = locality.getDependentLocality();

						if (dependentLocality.isSetType() && 
								dependentLocality.getType().toUpperCase().equals("DISTRICT")) {

							if (dependentLocality.isSetThoroughfare()) {
								Thoroughfare thoroughfare = dependentLocality.getThoroughfare();

								// street name
								if (streetAttr == null && thoroughfare.isSetThoroughfareName()) {
									List<String> fareName = new ArrayList<String>();								
									for (ThoroughfareName name : thoroughfare.getThoroughfareName())
										fareName.add(name.getContent());

									streetAttr = Util.collection2string(fareName, ",");
								}

								// house number - we do not support number ranges so far...						
								if (houseNoAttr == null && thoroughfare.isSetThoroughfareNumberOrThoroughfareNumberRange()) {
									List<String> houseNumber = new ArrayList<String>();								
									for (ThoroughfareNumberOrRange number : thoroughfare.getThoroughfareNumberOrThoroughfareNumberRange()) {
										if (number.isSetThoroughfareNumber())
											houseNumber.add(number.getThoroughfareNumber().getContent());
									}

									houseNoAttr = Util.collection2string(houseNumber, ",");
								}
							}
						}
					}

					// postal code
					if (locality.isSetPostalCode()) {
						PostalCode postalCode = locality.getPostalCode();

						// get postal code number
						if (postalCode.isSetPostalCodeNumber()) {
							List<String> zipCode = new ArrayList<String>();							
							for (PostalCodeNumber number : postalCode.getPostalCodeNumber())
								zipCode.add(number.getContent());

							zipCodeAttr = Util.collection2string(zipCode, ",");
						}
					}

					// post box
					if (locality.isSetPostBox()) {
						PostBox postBox = locality.getPostBox();

						// get post box nummber
						if (postBox.isSetPostBoxNumber())
							poBoxAttr = postBox.getPostBoxNumber().getContent();
					}
				}
			}

			// multiPoint geometry
			if (address.isSetMultiPoint())
				multiPoint = geometryImporter.getMultiPoint(address.getMultiPoint());

			success = true;
		} else {
			StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
					address.getCityGMLClass(), 
					address.getId()));
			msg.append(": Failed to interpret xAL address element.");
			LOG.error(msg.toString());
		}

		// get XML representation of <xal:AddressDetails>
		if (importXalSource) {
			xalSource = dbImporterManager.marshal(addressDetails, XALModuleType.CORE);
			if (xalSource != null && xalSource.length() > 0)
				success = true;
		}		

		if (success) {
			// gml:id
			if (replaceGmlId) {
				String gmlId = DefaultGMLIdManager.getInstance().generateUUID();

				// mapping entry
				if (address.isSetId())
					dbImporterManager.putUID(address.getId(), addressId, -1, false, gmlId, address.getCityGMLClass());

				address.setId(gmlId);

			} else {
				if (address.isSetId())
					dbImporterManager.putUID(address.getId(), addressId, address.getCityGMLClass());
				else
					address.setId(DefaultGMLIdManager.getInstance().generateUUID());
			}
			
			int index = 1;			
			psAddress.setLong(index++, addressId);
			if (handleGmlId)
				psAddress.setString(index++, address.getId());

			psAddress.setString(index++, streetAttr);
			psAddress.setString(index++, houseNoAttr);
			psAddress.setString(index++, poBoxAttr);
			psAddress.setString(index++, zipCodeAttr);
			psAddress.setString(index++, cityAttr);
			psAddress.setString(index++, countryAttr);

			if (multiPoint != null) {
				Object multiPointObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiPoint, batchConn);
				psAddress.setObject(index++, multiPointObj);
			} else
				psAddress.setNull(index++, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
						dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

			if (xalSource != null)
				psAddress.setString(index++, xalSource);
			else
				psAddress.setNull(index++, Types.CLOB);

			psAddress.addBatch();
			if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
				dbImporterManager.executeBatch(DBImporterEnum.ADDRESS);

		} else
			addressId = 0;		

		return addressId;
	}

	public long insertBuildingAddress(Address address, long parentId) throws SQLException {
		long addressId = insert(address);
		if (addressId != 0)
			addressToBuildingImporter.insert(addressId, parentId);		

		return addressId;
	}

	public long insertBridgeAddress(Address address, long parentId) throws SQLException {
		long addressId = insert(address);
		if (addressId != 0)
			addressToBridgeImporter.insert(addressId, parentId);		

		return addressId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psAddress.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psAddress.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.ADDRESS;
	}

}
