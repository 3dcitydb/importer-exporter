/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.database.adapter;

import java.util.ArrayList;
import java.util.List;

import org.citydb.config.project.global.LogLevel;
import org.citydb.log.Logger;

public class IndexStatusInfo {
	private final Logger LOG = Logger.getInstance();
	private List<IndexInfoObject> indexes;
	private IndexType type;

	public enum IndexType {
		SPATIAL,
		NORMAL
	}

	public enum IndexStatus {
		VALID,
		DROPPED,
		INVALID,
		ERROR
	}
	
	private IndexStatusInfo() {
		// just to thwart instantiation
	}
	
	public static synchronized IndexStatusInfo createFromDatabaseQuery(String[] query, IndexType type) {
		IndexStatusInfo info = null;

		if (query != null) {
			info = new IndexStatusInfo();
			info.indexes = new ArrayList<IndexInfoObject>(query.length);
			info.type = type;
			
			for (String indexInfo : query) {
				String[] parts = indexInfo.split(":");

				if (parts.length > 4) {
					IndexInfoObject obj = new IndexInfoObject();				

					if (parts[0].equals("VALID"))
						obj.status = IndexStatus.VALID;
					else if (parts[0].equals("DROPPED"))
						obj.status = IndexStatus.DROPPED;
					else if (parts[0].equals("INVALID"))
						obj.status = IndexStatus.INVALID;
					else
						obj.status = IndexStatus.ERROR;

					obj.name = parts[1].toUpperCase();
					obj.table = parts[2].toUpperCase();
					obj.column = parts[3].toUpperCase();

					if (parts.length > 4 && !parts[4].equals("0"))
						obj.errorMessage = parts[4];
					else
						obj.errorMessage = "";
					
					info.indexes.add(obj);
				}
			}
		}

		return info;
	}

	public List<IndexInfoObject> getIndexObjects() {
		return indexes;
	}
	
	public List<IndexInfoObject> getIndexObjects(IndexStatus status) {
		List<IndexInfoObject> tmp = new ArrayList<IndexInfoObject>();		
		for (IndexInfoObject obj : indexes)
			if (obj.status == status)
				tmp.add(obj);
		
		return tmp;
	}
	
	public int getNumberOfIndexes() {
		return indexes.size();
	}
	
	public int getNumberOfValidIndexes() {
		return getIndexObjects(IndexStatus.VALID).size();
	}
	
	public int getNumberOfInvalidIndexes() {
		return getIndexObjects(IndexStatus.INVALID).size();
	}
	
	public int getNumberOfDroppedIndexes() {
		return getIndexObjects(IndexStatus.DROPPED).size();
	}
	
	public int getNumberOfFaultyIndexes() {
		return getIndexObjects(IndexStatus.ERROR).size();
	}
	
	public void printStatusToConsole() {
		int on = getNumberOfValidIndexes();
		int all = getIndexObjects().size();
		
		StringBuilder msg = new StringBuilder();
		
		msg.append(type == IndexType.SPATIAL ? "Spatial" : "Normal").append(" indexes are ");
		
		if (on == 0)
			msg.append("disabled.");
		else if (on == all)
			msg.append("enabled.");
		else
			msg.append("partly enabled (").append(on).append(" / ").append(all).append(").");
		
		LOG.all(LogLevel.INFO, msg.toString());
	}
	
	public static final class IndexInfoObject {
		private String name;
		private String table;
		private String column;
		private IndexStatus status;
		private String errorMessage;

		public String getName() {
			return name;
		}
		
		public String getTable() {
			return table;
		}
		
		public String getColumn() {
			return column;
		}

		public IndexStatus getStatus() {
			return status;
		}

		public String getErrorMessage() {
			return errorMessage;
		}
		
		public boolean hasErrorMessage() {
			return errorMessage != null && errorMessage.length() > 0;
		}
		
		public String toString() {
			return new StringBuilder().append(name).append(" on ").append(table).append("(").append(column).append(")").toString();
		}
	}

}
