package de.tub.citydb.config;

import java.io.File;

public class ConfigUtil {

	public static String createConfigPath(String userPath, String configPath) {
		File createPath = new File(userPath + File.separator + configPath);
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
