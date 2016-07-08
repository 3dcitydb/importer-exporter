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
