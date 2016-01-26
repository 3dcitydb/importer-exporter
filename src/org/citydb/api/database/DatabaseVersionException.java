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
package org.citydb.api.database;

import java.util.List;

@SuppressWarnings("serial")
public class DatabaseVersionException extends Exception {
	private final String formattedMessage;
	private final String productName;
	private final List<DatabaseVersion> supportedVersions;
	
	public DatabaseVersionException(String message, String formattedMessage, String productName, List<DatabaseVersion> supportedVersions, Throwable cause) {
		super(message, cause);
		this.formattedMessage = formattedMessage;
		this.productName = productName;
		this.supportedVersions = supportedVersions;
	}

	public DatabaseVersionException(String message, String formattedMessage, String productName, List<DatabaseVersion> supportedVersions) {
		this(message, formattedMessage, productName, supportedVersions, null);
	}
	
	public String getFormattedMessage() {
		return formattedMessage;
	}

	public String getProductName() {
		return productName;
	}

	public List<DatabaseVersion> getSupportedVersions() {
		return supportedVersions;
	}
}
