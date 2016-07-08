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
				address.setId(addressObject.getGmlId());
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
