package de.tub.citydb.modules.citygml.exporter.gui.view;

import java.awt.Component;

import javax.swing.Icon;

import org.citygml4j.builder.jaxb.JAXBBuilder;

import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;

public class CityGMLExportView implements View {
	private final ExportPanel component;
	
	public CityGMLExportView(JAXBBuilder jaxbBuilder, Config config, ImpExpGui mainView) {
		component = new ExportPanel(jaxbBuilder, config, mainView);
	}
	
	@Override
	public String getLocalizedTitle() {
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
