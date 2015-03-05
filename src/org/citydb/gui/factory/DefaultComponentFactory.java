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
package org.citydb.gui.factory;

import org.citydb.api.gui.BoundingBoxPanel;
import org.citydb.api.gui.ComponentFactory;
import org.citydb.api.gui.DatabaseSrsComboBox;
import org.citydb.api.gui.StandardEditingPopupMenuDecorator;
import org.citydb.config.Config;
import org.citydb.gui.components.bbox.BoundingBoxPanelImpl;

public class DefaultComponentFactory implements ComponentFactory {
	private static DefaultComponentFactory instance;
	private final Config config;
	
	private DefaultComponentFactory(Config config) {
		this.config = config;
	}
	
	public static synchronized DefaultComponentFactory getInstance(Config config) {
		if (instance == null)
			instance = new DefaultComponentFactory(config);
		
		return instance;
	}
	
	@Override
	public DatabaseSrsComboBox createDatabaseSrsComboBox() {
		return SrsComboBoxFactory.getInstance(config).createSrsComboBox(true);
	}

	@Override
	public StandardEditingPopupMenuDecorator createPopupMenuDecorator() {
		return PopupMenuDecorator.getInstance();
	}

	@Override
	public BoundingBoxPanel createBoundingBoxPanel() {
		return new BoundingBoxPanelImpl(config);
	}

}
