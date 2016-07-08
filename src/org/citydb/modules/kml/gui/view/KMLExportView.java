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
package org.citydb.modules.kml.gui.view;

import java.awt.Component;

import javax.swing.Icon;
import javax.xml.bind.JAXBContext;

import org.citydb.api.plugin.extension.view.View;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.gui.ImpExpGui;

public class KMLExportView extends View {
	private final KmlExportPanel component;
	
	public KMLExportView(JAXBContext jaxbKmlContext, JAXBContext jaxbColladaContext, Config config, ImpExpGui mainView) {
		component = new KmlExportPanel(jaxbKmlContext, jaxbColladaContext, config, mainView);
	}
	
	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("main.tabbedPane.kmlExport");
	}

	@Override
	public Component getViewComponent() {
		return component;
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public Icon getIcon() {
		return null;
	}
	
	public void loadSettings() {
		component.loadSettings();
	}
	
	public void setSettings() {
		component.setSettings();
	}
	
	public void doTranslation() {
		component.doTranslation();
	}

}
