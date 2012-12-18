package de.tub.citydb.modules.citygml.common.xal;

import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.xal.AddressDetails;

import de.tub.citydb.config.project.exporter.AddressMode;

public class AddressObject {	
	private final AddressExportFactory factory;
	
	protected String street;
	protected String houseNumber;
	protected String poBox;
	protected String zipCode;
	protected String city;
	protected String state;
	protected String country;
	protected MultiPointProperty multiPointProperty;
	protected AddressDetails addressDetails;

	protected AddressObject(AddressExportFactory factory) {
		this.factory = factory;
	}
	
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getHouseNumber() {
		return houseNumber;
	}

	public void setHouseNumber(String houseNumber) {
		this.houseNumber = houseNumber;
	}

	public String getPOBox() {
		return poBox;
	}

	public void setPOBox(String poBox) {
		this.poBox = poBox;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public MultiPointProperty getMultiPointProperty() {
		return multiPointProperty;
	}

	public void setMultiPointProperty(MultiPointProperty multiPointProperty) {
		this.multiPointProperty = multiPointProperty;
	}

	public AddressDetails getAddressDetails() {
		return addressDetails;
	}

	public void setAddressDetails(AddressDetails addressDetails) {
		this.addressDetails = addressDetails;
	}
	
	public boolean canCreate() {
		boolean canCreate = canCreate(factory.primary);
		if (!canCreate && factory.useFallback)
			canCreate = canCreate(factory.fallback);
		
		return canCreate;
	}
	
	public boolean canCreate(AddressMode mode) {
		switch (mode) {
		case DB:
			return street != null || houseNumber != null || poBox != null || 
			zipCode != null || city != null || state != null || 
			country != null;
		case XAL:
			return addressDetails != null;
		default:
			return false;
		}
	}

}
