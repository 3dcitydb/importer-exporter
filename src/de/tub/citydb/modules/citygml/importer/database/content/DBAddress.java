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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.model.citygml.CityGMLClass;
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

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

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
 * Additionally, <PostBox> elements are interpreted, iff being modelled as child element of <Locality>
 */

public class DBAddress implements DBImporter {	
	private final Logger LOG = Logger.getInstance();
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psAddress;
	private DBAddressToBuilding addressToBuildingImporter;
	private DBSdoGeometry sdoGeometry;
	private int batchCounter;

	private boolean importXalSource;

	public DBAddress(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		importXalSource = config.getProject().getImporter().getAddress().isSetImportXAL();

		psAddress = batchConn.prepareStatement("insert into ADDRESS (ID, STREET, HOUSE_NUMBER, PO_BOX, ZIP_CODE, CITY, COUNTRY, MULTI_POINT, XAL_SOURCE) values "+
				"(?, ?, ?, ?, ?, ?, ?, ?, ?)");

		addressToBuildingImporter = (DBAddressToBuilding)dbImporterManager.getDBImporter(DBImporterEnum.ADDRESS_TO_BUILDING);
		sdoGeometry = (DBSdoGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SDO_GEOMETRY);
	}

	public long insert(Address address) throws SQLException {
		if (!address.isSetXalAddress() || !address.getXalAddress().isSetAddressDetails())
			return 0;

		XalAddressProperty xalAddressProperty = address.getXalAddress();
		AddressDetails addressDetails = xalAddressProperty.getAddressDetails();

		// ok, let's start
		long addressId = dbImporterManager.getDBId(DBSequencerEnum.ADDRESS_SEQ);
		if (addressId == 0)
			return 0;

		boolean success = false;
		String streetAttr, houseNoAttr, poBoxAttr, zipCodeAttr, cityAttr, countryAttr, xalSource;
		streetAttr = houseNoAttr = poBoxAttr = zipCodeAttr = cityAttr = countryAttr = xalSource = null;
		JGeometry multiPoint = null;		

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
				multiPoint = sdoGeometry.getMultiPoint(address.getMultiPoint());
		
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
			psAddress.setLong(1, addressId);
			psAddress.setString(2, streetAttr);
			psAddress.setString(3, houseNoAttr);
			psAddress.setString(4, poBoxAttr);
			psAddress.setString(5, zipCodeAttr);
			psAddress.setString(6, cityAttr);
			psAddress.setString(7, countryAttr);

			if (multiPoint != null) {
				STRUCT multiPointObj = SyncJGeometry.syncStore(multiPoint, batchConn);
				psAddress.setObject(8, multiPointObj);
			} else
				psAddress.setNull(8, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

			if (xalSource != null)
				psAddress.setString(9, xalSource);
			else
				psAddress.setNull(9, Types.CLOB);

			psAddress.addBatch();
			if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
				dbImporterManager.executeBatch(DBImporterEnum.ADDRESS);

			// enable xlinks
			if (address.isSetId())
				dbImporterManager.putGmlId(address.getId(), addressId, address.getCityGMLClass());
		} else
			addressId = 0;		
		
		return addressId;
	}

	public long insert(Address address, long parentId) throws SQLException {
		long addressId = insert(address);

		if (addressId != 0)
			addressToBuildingImporter.insert(addressId, parentId);		

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
