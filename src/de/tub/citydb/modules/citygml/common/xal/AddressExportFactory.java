package de.tub.citydb.modules.citygml.common.xal;

import org.citygml4j.impl.citygml.core.AddressImpl;
import org.citygml4j.impl.citygml.core.AddressPropertyImpl;
import org.citygml4j.impl.citygml.core.XalAddressPropertyImpl;
import org.citygml4j.impl.xal.AddressDetailsImpl;
import org.citygml4j.impl.xal.CountryImpl;
import org.citygml4j.impl.xal.CountryNameImpl;
import org.citygml4j.impl.xal.LocalityImpl;
import org.citygml4j.impl.xal.LocalityNameImpl;
import org.citygml4j.impl.xal.PostBoxImpl;
import org.citygml4j.impl.xal.PostBoxNumberImpl;
import org.citygml4j.impl.xal.PostalCodeImpl;
import org.citygml4j.impl.xal.PostalCodeNumberImpl;
import org.citygml4j.impl.xal.ThoroughfareImpl;
import org.citygml4j.impl.xal.ThoroughfareNameImpl;
import org.citygml4j.impl.xal.ThoroughfareNumberImpl;
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

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.exporter.AddressMode;

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
				addressDetails = new AddressDetailsImpl();

				Locality locality = new LocalityImpl();
				locality.setType("Town");

				if (addressObject.city != null) {
					LocalityName localityName = new LocalityNameImpl();
					localityName.setContent(addressObject.city);
					locality.addLocalityName(localityName);
				}

				if (addressObject.street != null) {
					Thoroughfare thoroughfare = new ThoroughfareImpl();
					thoroughfare.setType("Street");

					ThoroughfareName name = new ThoroughfareNameImpl();
					name.setContent(addressObject.street);
					thoroughfare.addThoroughfareName(name);

					if (addressObject.houseNumber != null) {
						ThoroughfareNumber number = new ThoroughfareNumberImpl();
						number.setContent(addressObject.houseNumber);
						thoroughfare.addThoroughfareNumber(number);
					}

					locality.setThoroughfare(thoroughfare);
				}				

				if (addressObject.zipCode != null) {
					PostalCode postalCode = new PostalCodeImpl();
					PostalCodeNumber zipNumber = new PostalCodeNumberImpl();
					zipNumber.setContent(addressObject.zipCode);

					postalCode.addPostalCodeNumber(zipNumber);
					locality.setPostalCode(postalCode);
				}

				if (addressObject.poBox != null) {
					PostBox postBox = new PostBoxImpl();
					PostBoxNumber postBoxNumber = new PostBoxNumberImpl();
					postBoxNumber.setContent(addressObject.poBox);

					postBox.setPostBoxNumber(postBoxNumber);
					locality.setPostBox(postBox);
				}
				
				Country country = new CountryImpl();

				if (addressObject.country != null) {
					CountryName countryName = new CountryNameImpl();
					countryName.setContent(addressObject.country);
					country.addCountryName(countryName);
				}

				country.setLocality(locality);
				addressDetails.setCountry(country);
			}

			if (addressDetails != null) {
				XalAddressProperty xalAddressProperty = new XalAddressPropertyImpl();
				xalAddressProperty.setAddressDetails(addressDetails);

				Address address = new AddressImpl();
				address.setXalAddress(xalAddressProperty);

				// multiPointGeometry
				if (addressObject.multiPointProperty != null)
					address.setMultiPoint(addressObject.multiPointProperty);				

				addressProperty = new AddressPropertyImpl();
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
