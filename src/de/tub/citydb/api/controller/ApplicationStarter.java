package de.tub.citydb.api.controller;

import java.lang.reflect.Method;

import de.tub.citydb.api.log.Logger;
import de.tub.citydb.api.plugin.Plugin;

public class ApplicationStarter {
	private final Logger LOG = Logger.getInstance();

	public void run(String[] args, Plugin... plugins) {
		LOG.info("Starting 3D City Database Importer/Exporter through Plugin API");
		
		try {
			Class<?> app = Class.forName("de.tub.citydb.ImpExp");
			Class<?>[] parameterTypes = new Class<?>[]{String[].class, Plugin[].class};
			
			Method main = app.getDeclaredMethod("doMain", parameterTypes);
			main.setAccessible(true);
			main.invoke(app.newInstance(), args, plugins);
			
		} catch (Exception e) {
			LOG.error("Failed to start main application. Check the following stack trace.");
			e.printStackTrace();
		}
	}
	
}
