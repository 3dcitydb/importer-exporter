/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceConfigurationError;

public class PluginServiceFactory {
	private static ClassLoader loader = new ClassLoader();
	
	public static void addPluginDirectory(File directory) throws IOException {
		if (directory.exists())
			for (File file : directory.listFiles())
				if (file.getName().toLowerCase().endsWith(".jar"))
					loader.addURL(file.toURI().toURL());
	}

	public static PluginService getPluginService() throws ServiceConfigurationError {
		return DefaultPluginService.getInstance(loader);
	}

	private static class ClassLoader extends URLClassLoader {
		protected ClassLoader() {
			super(new URL[]{}, PluginServiceFactory.class.getClassLoader());
		}

		@Override
		protected void addURL(URL url) {
			super.addURL(url);
		}

	}
}
