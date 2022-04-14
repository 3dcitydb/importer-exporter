/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.operation.exporter.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.ExportResourceId;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class ResourceIdPanel extends InternalPreferencesComponent {
	private TitledPanel resourceIdPanel;
	private JCheckBox replaceWithUUIDs;
	private JLabel idPrefixLabel;
	private JTextField idPrefix;

	public ResourceIdPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ExportResourceId resourceId = config.getExportConfig().getResourceId();

		if (replaceWithUUIDs.isSelected() != resourceId.isReplaceWithUUIDs()) return true;
		if (!idPrefix.getText().equals(resourceId.getIdPrefix())) return true;

		return false;
	}

	private void initGui() {
		replaceWithUUIDs = new JCheckBox();
		idPrefixLabel = new JLabel();
		idPrefix = new JTextField();

		PopupMenuDecorator.getInstance().decorate(idPrefix);
		
		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(idPrefixLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(idPrefix, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			resourceIdPanel = new TitledPanel().withToggleButton(replaceWithUUIDs).build(content);
		}

		add(resourceIdPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		replaceWithUUIDs.addActionListener(e -> setEnabledPrefix());
	}
	
	private void setEnabledPrefix() {
		idPrefixLabel.setEnabled(replaceWithUUIDs.isSelected());
		idPrefix.setEnabled(replaceWithUUIDs.isSelected());
	}

	@Override
	public void switchLocale(Locale locale) {
		resourceIdPanel.setTitle(Language.I18N.getString("pref.export.id.label.replace"));
		idPrefixLabel.setText(Language.I18N.getString("pref.export.id.label.prefix"));
	}

	@Override
	public void loadSettings() {
		ExportResourceId resourceId = config.getExportConfig().getResourceId();

		replaceWithUUIDs.setSelected(resourceId.isReplaceWithUUIDs());

		if (resourceId.getIdPrefix() != null && resourceId.getIdPrefix().trim().length() != 0) {
			idPrefix.setText(resourceId.getIdPrefix());
		} else {
			idPrefix.setText(DefaultGMLIdManager.getInstance().getDefaultPrefix());
			resourceId.setIdPrefix(DefaultGMLIdManager.getInstance().getDefaultPrefix());
		}
		
		setEnabledPrefix();
	}

	@Override
	public void setSettings() {
		ExportResourceId resourceId = config.getExportConfig().getResourceId();

		resourceId.setReplaceWithUUIDs(replaceWithUUIDs.isSelected());

		if (idPrefix.getText() != null && DefaultGMLIdManager.getInstance().isValidPrefix(idPrefix.getText())) {
			resourceId.setIdPrefix(idPrefix.getText());
		} else {
			resourceId.setIdPrefix(DefaultGMLIdManager.getInstance().getDefaultPrefix());
			idPrefix.setText(DefaultGMLIdManager.getInstance().getDefaultPrefix());
		}
	}
	
	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("pref.tree.export.id");
	}

}
