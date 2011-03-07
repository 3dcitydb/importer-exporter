package de.tub.citydb.util;

import java.util.UUID;

public class UUIDManager {
	public static String UUIDPrefix = "UUID_";

	public static String randomUUID() {
		StringBuffer uuid = new StringBuffer(UUIDPrefix);
		uuid.append(UUID.randomUUID());

		return uuid.toString();
	}
	
	public static String randomUUID(String prefix) {
		StringBuffer uuid = new StringBuffer(prefix);
		uuid.append(UUID.randomUUID());

		return uuid.toString();
	}

}
