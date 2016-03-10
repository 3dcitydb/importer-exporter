/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.api.controller;

import java.lang.reflect.Method;

import org.citydb.api.plugin.Plugin;

public class ApplicationStarter {

	public void run(String[] args, Plugin... plugins) {
		Class<?> clazz = null;
		Object obj = null;
		Method log = null;
		
		try {
			// get class and instance
			clazz = Class.forName("org.citydb.ImpExp");
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
