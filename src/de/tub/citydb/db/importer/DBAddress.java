package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.model.citygml.core.Address;
import de.tub.citygml4j.model.citygml.core.XalAddressProperty;
import de.tub.citygml4j.model.xal.AddressDetails;
import de.tub.citygml4j.model.xal.Country;
import de.tub.citygml4j.model.xal.CountryName;
import de.tub.citygml4j.model.xal.Locality;
import de.tub.citygml4j.model.xal.LocalityName;
import de.tub.citygml4j.model.xal.PostBox;
import de.tub.citygml4j.model.xal.PostalCode;
import de.tub.citygml4j.model.xal.PostalCodeNumber;
import de.tub.citygml4j.model.xal.Thoroughfare;
import de.tub.citygml4j.model.xal.ThoroughfareName;
import de.tub.citygml4j.model.xal.ThoroughfareNumber;

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
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psAddress;
	private DBAddressToBuilding addressToBuildingImporter;
	private DBSdoGeometry sdoGeometry;
	private int batchCounter;

	public DBAddress(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psAddress = batchConn.prepareStatement("insert into ADDRESS (ID, STREET, HOUSE_NUMBER, PO_BOX, ZIP_CODE, CITY, COUNTRY, MULTI_POINT) values "+
				"(?, ?, ?, ?, ?, ?, ?, ?)");
		
		addressToBuildingImporter = (DBAddressToBuilding)dbImporterManager.getDBImporter(DBImporterEnum.ADDRESS_TO_BUILDING);
		sdoGeometry = (DBSdoGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SDO_GEOMETRY);
	}

	public long insert(Address address) throws SQLException {
		XalAddressProperty xalAddressProperty = address.getXalAddressProperty();
		if (xalAddressProperty == null)
			return 0;
		
		AddressDetails addressDetails = xalAddressProperty.getAddressDetails();
		if (addressDetails == null)
			return 0;
		
		// ok, let's start
		long addressId = dbImporterManager.getDBId(DBSequencerEnum.ADDRESS_SEQ);
		if (addressId == 0)
			return 0;
		
		// we just interpret addresses having a <country> child element
		if (addressDetails.getCountry() != null) {
			// this is the information we need...
			String streetAttr, houseNoAttr, poBoxAttr, zipCodeAttr, cityAttr, countryAttr;
			streetAttr = houseNoAttr = poBoxAttr = zipCodeAttr = cityAttr = countryAttr = null;
			JGeometry multiPoint = null;
			Country country = addressDetails.getCountry();
			
			// country name
			List<CountryName> countryNameList = country.getCountryName();
			if (countryNameList != null) {
				List<String> countryName = new ArrayList<String>();
				
				for (CountryName name : countryNameList)
					countryName.add(name.getContent());
				
				countryAttr = Util.collection2string(countryName, ",");
			} 
			
			// locality
			if (country.getLocality() != null) {
				Locality locality = country.getLocality();
				
				// check whether we deal with a city or a town
				if (locality.getType() != null && 
						(locality.getType().toUpperCase().equals("CITY") ||
								locality.getType().toUpperCase().equals("TOWN"))) {
					
					// city name
					List<LocalityName> localityNameList = locality.getLocalityName();
					if (localityNameList != null) {
						List<String> localityName = new ArrayList<String>();
						
						for (LocalityName name : localityNameList)
							localityName.add(name.getContent());
						
						cityAttr = Util.collection2string(localityName, ",");
					} 
					
					// thoroughfare - just streets are supported
					if (locality.getThoroughfare() != null) {
						Thoroughfare thoroughfare = locality.getThoroughfare();
						
						// check whether we deal with a street
						if (thoroughfare.getType() != null&& 
								(thoroughfare.getType().toUpperCase().equals("STREET") ||
										thoroughfare.getType().toUpperCase().equals("ROAD"))) {
							
							// street name
							List<ThoroughfareName> fareNameList = thoroughfare.getThoroughfareName();
							if (fareNameList != null) {
								List<String> fareName = new ArrayList<String>();
								
								for (ThoroughfareName name : fareNameList)
									fareName.add(name.getContent());
								
								streetAttr = Util.collection2string(fareName, ",");
							}
							
							// house number - we do not support number ranges so far...
							List<Object> numberList = thoroughfare.getThoroughfareNumberOrThoroughfareNumberRange();
							
							if (numberList != null) {
								List<String> houseNumber = new ArrayList<String>();
								
								for (Object object : numberList) {
									if (object instanceof ThoroughfareNumber) {
										ThoroughfareNumber number = (ThoroughfareNumber)object;										
										houseNumber.add(number.getContent());
									}
								}
								
								houseNoAttr = Util.collection2string(houseNumber, ",");
							}
						}
					}

					// postal code
					if (locality.getPostalCode() != null) {
						PostalCode postalCode = locality.getPostalCode();
						
						// get postal code number
						List<PostalCodeNumber> numberList = postalCode.getPostalCodeNumber();
						if (numberList != null) {
							List<String> zipCode = new ArrayList<String>();
							
							for (PostalCodeNumber number : numberList)
								zipCode.add(number.getContent());
							
							zipCodeAttr = Util.collection2string(zipCode, ",");
						}
					}
					
					// post box
					if (locality.getPostBox() != null) {
						PostBox postBox = locality.getPostBox();
						
						// get post box nummber
						if (postBox.getPostBoxNumber() != null)
							poBoxAttr = postBox.getPostBoxNumber().getContent();
					}
				}
			}
			
			// multiPoint geometry
			if (address.getMultiPoint() != null) {
				multiPoint = sdoGeometry.getMultiPoint(address.getMultiPoint());
			}
			
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
			
			psAddress.addBatch();
			if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
				dbImporterManager.executeBatch(DBImporterEnum.ADDRESS);
			
			// enable xlinks
			if (address.getId() != null)
				dbImporterManager.putGmlId(address.getId(), addressId, address.getCityGMLClass());
		}	
		
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
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.ADDRESS;
	}

}
