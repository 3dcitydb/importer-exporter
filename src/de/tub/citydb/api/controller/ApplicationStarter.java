package de.tub.citydb.api.controller;

import java.lang.reflect.Method;

import de.tub.citydb.api.plugin.Plugin;

public class ApplicationStarter {

	public void run(String[] args, Plugin... plugins) {
		Class<?> clazz = null;
		Object obj = null;
		Method log = null;
		
		try {
			// get class and instance
			clazz = Class.forName("de.tub.citydb.ImpExp");
			obj = clazz.newInstance();

			// getter logger handle
			log = clazz.getDeclaredMethod("printInfoMessage", new Class<?>[]{String.class});
			log.setAccessible(true);
			
		} catch (Exception e) {
			System.out.println("Failed to start main application. Check the following stack trace.");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			print(obj, log, "Starting 3D City Database Importer/Exporter through Plugin API");

			// invoke doMain method
			Class<?>[] parameterTypes = new Class<?>[]{String[].class, Plugin[].class};
			Method main = clazz.getDeclaredMethod("doMain", parameterTypes);
			main.setAccessible(true);
			main.invoke(obj, args, plugins);

		} catch (Exception e) {
			print(obj, log, "Failed to start main application. Check the following stack trace.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void print(Object obj, Method log, String msg) {
		try {
			log.invoke(obj, msg);
		} catch (Exception e) {
			System.out.println(msg);
		}
	}

}
