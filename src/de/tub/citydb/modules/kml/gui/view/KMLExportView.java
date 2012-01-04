package de.tub.citydb.modules.kml.gui.view;

import java.awt.Component;

import javax.swing.Icon;
import javax.xml.bind.JAXBContext;

import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;

public class KMLExportView extends View {
	private final KmlExportPanel component;
	
	public KMLExportView(JAXBContext jaxbKmlContext, JAXBContext jaxbColladaContext, Config config, ImpExpGui mainView) {
		component = new KmlExportPanel(jaxbKmlContext, jaxbColladaContext, config, mainView);
	}
	
	@Override
	public String getLocalizedTitle() {
		return Internal.I18N.getString("main.tabbedPane.kmlExport");
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
