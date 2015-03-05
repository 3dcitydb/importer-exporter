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
package org.citydb.config.project.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.config.project.database.Database.PredefinedSrsName;

@XmlRootElement(name="referenceSystems")
public class DatabaseSrsList {
	@XmlElement(name="referenceSystem")
	private List<DatabaseSrs> items;

	public DatabaseSrsList() {
		items = new ArrayList<DatabaseSrs>();
	}

	public List<DatabaseSrs> getItems() {
		return items;
	}

	public void setItems(List<DatabaseSrs> items) {
		this.items = items;
	}

	public boolean addItem(DatabaseSrs item) {
		return items.add(item);
	}

	public void addDefaultItems() {		
		HashMap<PredefinedSrsName, Boolean> addSRS = new HashMap<Database.PredefinedSrsName, Boolean>(Database.PREDEFINED_SRS.size());
		for (PredefinedSrsName name : PredefinedSrsName.values())
			addSRS.put(name, Boolean.TRUE);
						
		for (DatabaseSrs refSys : items) {
			Iterator<Entry<PredefinedSrsName, DatabaseSrs>> iter = Database.PREDEFINED_SRS.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<PredefinedSrsName, DatabaseSrs> entry = iter.next();
				if (addSRS.get(entry.getKey()) && refSys.getSrid() == entry.getValue().getSrid()) {
					addSRS.put(entry.getKey(), Boolean.FALSE);
					break;
				}
			}
		}
		
		Iterator<Entry<PredefinedSrsName, Boolean>> iter = addSRS.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<PredefinedSrsName, Boolean> entry = iter.next();
			if (entry.getValue())
				items.add(Database.PREDEFINED_SRS.get(entry.getKey()));
		}
	}

}
