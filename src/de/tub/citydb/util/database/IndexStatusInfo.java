package de.tub.citydb.util.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.log.Logger;

public class IndexStatusInfo {
	private static final Logger LOG = Logger.getInstance();
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

				if (parts.length > 3) {
					IndexInfoObject obj = new IndexInfoObject();				

					if (parts[0].equals("VALID"))
						obj.status = IndexStatus.VALID;
					else if (parts[0].equals("DROPPED"))
						obj.status = IndexStatus.DROPPED;
					else if (parts[0].equals("INVALID"))
						obj.status = IndexStatus.INVALID;
					else
						obj.status = IndexStatus.ERROR;

					obj.name = parts[1];
					obj.table = parts[2];
					obj.column = parts[3];

					if (parts.length > 4)
						try {
							obj.errorMessage = DBUtil.errorMessage(parts[4]);
						} catch (SQLException e) {
							obj.errorMessage = "ORA-" + parts[4];
						}
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
			return name + " on " + table + "(" + column + ")";
		}
	}

}
