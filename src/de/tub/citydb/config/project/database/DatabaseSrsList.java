/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.config.project.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.config.project.database.Database.PredefinedSrsName;

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
