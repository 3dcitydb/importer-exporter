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
package org.citydb.modules.citygml.common.xal;

import org.citydb.config.Config;
import org.citydb.config.project.exporter.AddressMode;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.XalAddressProperty;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.model.xal.Country;
import org.citygml4j.model.xal.CountryName;
import org.citygml4j.model.xal.Locality;
import org.citygml4j.model.xal.LocalityName;
import org.citygml4j.model.xal.PostBox;
import org.citygml4j.model.xal.PostBoxNumber;
import org.citygml4j.model.xal.PostalCode;
import org.citygml4j.model.xal.PostalCodeNumber;
import org.citygml4j.model.xal.Thoroughfare;
import org.citygml4j.model.xal.ThoroughfareName;
import org.citygml4j.model.xal.ThoroughfareNumber;

public class AddressExportFactory {
	protected final AddressMode primary;
	protected final AddressMode fallback;
	protected final boolean useFallback;

	public AddressExportFactory(Config config) {
		primary = config.getProject().getExporter().getAddress().getMode();
		fallback = primary == AddressMode.DB ? AddressMode.XAL : AddressMode.DB;		
		useFallback = config.getProject().getExporter().getAddress().isSetUseFallback();
	}
	
	public AddressObject newAddressObject() {
		return new AddressObject(this);
	}

	public AddressProperty create(AddressObject addressObject) {
		AddressProperty addressProperty = null;
		AddressMode mode = null;
		
		if (addressObject.canCreate(primary))
			mode = primary;
		else if (useFallback && addressObject.canCreate(fallback))
			mode = fallback;

		if (mode != null) {
			AddressDetails addressDetails = null;

			if (mode == AddressMode.XAL) {
				addressDetails = addressObject.addressDetails;						
			} else {				
				addressDetails = new AddressDetails();

				Locality locality = new Locality();
				locality.setType("Town");

				if (addressObject.city != null) {
					LocalityName localityName = new LocalityName();
					localityName.setContent(addressObject.city);
					locality.addLocalityName(localityName);
				}

				if (addressObject.street != null) {
					Thoroughfare thoroughfare = new Thoroughfare();
					thoroughfare.setType("Street");

					ThoroughfareName name = new ThoroughfareName();
					name.setContent(addressObject.street);
					thoroughfare.addThoroughfareName(name);

					if (addressObject.houseNumber != null) {
						ThoroughfareNumber number = new ThoroughfareNumber();
						number.setContent(addressObject.houseNumber);
						thoroughfare.addThoroughfareNumber(number);
					}

					locality.setThoroughfare(thoroughfare);
				}				

				if (addressObject.zipCode != null) {
					PostalCode postalCode = new PostalCode();
					PostalCodeNumber zipNumber = new PostalCodeNumber();
					zipNumber.setContent(addressObject.zipCode);

					postalCode.addPostalCodeNumber(zipNumber);
					locality.setPostalCode(postalCode);
				}

				if (addressObject.poBox != null) {
					PostBox postBox = new PostBox();
					PostBoxNumber postBoxNumber = new PostBoxNumber();
					postBoxNumber.setContent(addressObject.poBox);

					postBox.setPostBoxNumber(postBoxNumber);
					locality.setPostBox(postBox);
				}
				
				Country country = new Country();

				if (addressObject.country != null) {
					CountryName countryName = new CountryName();
					countryName.setContent(addressObject.country);
					country.addCountryName(countryName);
				}

				country.setLocality(locality);
				addressDetails.setCountry(country);
			}

			if (addressDetails != null) {
				XalAddressProperty xalAddressProperty = new XalAddressProperty();
				xalAddressProperty.setAddressDetails(addressDetails);

				Address address = new Address();
				address.setXalAddress(xalAddressProperty);

				// multiPointGeometry
				if (addressObject.multiPointProperty != null)
					address.setMultiPoint(addressObject.multiPointProperty);				

				addressProperty = new AddressProperty();
				addressProperty.setObject(address);
			}
		}

		return addressProperty;
	}

	public AddressMode getPrimaryMode() {
		return primary;
	}
	
	public AddressMode getFallbackMode() {
		return fallback;
	}
	
	public boolean isUseFallback() {
		return useFallback;
	}
	
}
