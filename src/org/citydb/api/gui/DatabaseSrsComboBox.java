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
