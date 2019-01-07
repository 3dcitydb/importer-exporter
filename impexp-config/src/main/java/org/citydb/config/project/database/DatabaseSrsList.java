/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
		HashMap<PredefinedSrsName, Boolean> addSrs = new HashMap<>(Database.PREDEFINED_SRS.size());
		for (PredefinedSrsName name : PredefinedSrsName.values())
			addSrs.put(name, Boolean.TRUE);
						
		for (DatabaseSrs refSys : items) {
			for (Entry<PredefinedSrsName, DatabaseSrs> entry : Database.PREDEFINED_SRS.entrySet()) {
				if (addSrs.get(entry.getKey()) && refSys.getSrid() == entry.getValue().getSrid()) {
					addSrs.put(entry.getKey(), Boolean.FALSE);
					break;
				}
			}
		}

		for (Entry<PredefinedSrsName, Boolean> entry : addSrs.entrySet()) {
			if (entry.getValue())
				items.add(Database.PREDEFINED_SRS.get(entry.getKey()));
		}
	}

}
