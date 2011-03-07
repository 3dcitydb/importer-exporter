package de.tub.citydb.config;

import java.io.File;

public class ConfigUtil {

	public static String createConfigPath(String configPath) {
		File createPath = new File(configPath);
		boolean success = true;
	
		if (!createPath.exists()) {
			success = createPath.mkdirs();
		} 
		
		if (success)
			return createPath.getAbsolutePath();
		else
			return null;
	}
	
}
