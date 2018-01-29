package org.citydb.database.schema.mapping;

public interface Joinable {
	public boolean isSetJoin();
	public AbstractJoin getJoin();
}
