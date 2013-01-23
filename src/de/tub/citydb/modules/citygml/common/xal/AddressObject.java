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
