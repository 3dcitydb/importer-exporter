package de.tub.citydb.components.citygml.exporter.gui.view;

import java.awt.Component;

import javax.swing.Icon;
import javax.xml.bind.JAXBContext;

import de.tub.citydb.components.citygml.exporter.gui.view.components.ExportPanel;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.plugin.api.extension.view.View;

public class CityGMLExportView implements View {
	private final ExportPanel component;
	
	public CityGMLExportView(JAXBContext jaxbContext, Config config, ImpExpGui mainView) {
		component = new ExportPanel(jaxbContext, config, mainView);
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("main.tabbedPane.export");
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
