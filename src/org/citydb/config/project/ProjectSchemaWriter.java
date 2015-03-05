/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
package org.citydb.config.project;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class ProjectSchemaWriter extends SchemaOutputResolver {
	private String path;

	public ProjectSchemaWriter(File path) {
		this.path = path.getAbsolutePath();
	}

	@Override
	public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
		File file;

		if (namespaceUri.equals("http://www.3dcitydb.org/importer-exporter/config"))
			file = new File(path + File.separator + "config.xsd");
		else
			file = new File(path + File.separator + "plugin_" + suggestedFileName);

		StreamResult res = new StreamResult(file);
		res.setSystemId(file.toURI().toString());
		return res;
	}

}
