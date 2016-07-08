/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
