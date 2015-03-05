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
package org.citydb.api.gui;

import javax.swing.JComboBox;

import org.citydb.api.database.DatabaseSrs;

@SuppressWarnings("serial")
public abstract class DatabaseSrsComboBox extends JComboBox<DatabaseSrs> {
	
	public abstract void setShowOnlySameDimension(boolean show);
	public abstract void setShowOnlySupported(boolean show);
	
	@Override
	public DatabaseSrs getSelectedItem() {
		Object object = super.getSelectedItem();
		return (object instanceof DatabaseSrs) ? (DatabaseSrs)object : null;
	}
	
	@Override
	public DatabaseSrs getItemAt(int index) {
		Object object = super.getItemAt(index);
		return (object instanceof DatabaseSrs) ? (DatabaseSrs)object : null;
	}
	
	@Override
	public void addItem(DatabaseSrs srs) {
		super.addItem(srs);
	}

	@Override
	public void insertItemAt(DatabaseSrs srs, int index) {
		super.insertItemAt(srs, index);
	}
	
}
