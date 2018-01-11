package org.citydb.ade.importer;

import java.util.HashMap;

public class ForeignKeys {
	public static final ForeignKeys EMPTY_SET = new ForeignKeys(true);	
	private HashMap<String, Long> foreignKeys;

	private ForeignKeys(boolean isDefault) {
		if (!isDefault)
			foreignKeys = new HashMap<>();
	}

	public static final synchronized ForeignKeys create() {
		return new ForeignKeys(false);
	}

	public ForeignKeys with(String name, long value) {
		if (this != EMPTY_SET)
			foreignKeys.put(name, value);

		return this;
	}

	public boolean isEmpty() {
		return this != EMPTY_SET ? foreignKeys.isEmpty() : true;
	}

	public boolean contains(String name) {
		return this != EMPTY_SET ? foreignKeys.containsKey(name) : false;
	}

	public long get(String name) {
		if (this != EMPTY_SET) {
			Long value = foreignKeys.get(name);
			return value != null ? value.longValue() : 0;
		} else
			return 0;
	}
}
