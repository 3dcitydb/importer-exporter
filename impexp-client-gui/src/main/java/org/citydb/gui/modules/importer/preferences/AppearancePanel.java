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
package org.citydb.gui.modules.importer.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.ImportAppearance;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class AppearancePanel extends AbstractPreferencesComponent {
	private TitledPanel appearancePanel;
	private JLabel impAppOldLabel;
	private JCheckBox importAppearance;
	private JCheckBox importTextures;
	private JTextField impAppOldText;

	public AppearancePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportAppearance appearances = config.getImportConfig().getAppearances();

		if (importAppearance.isSelected() != appearances.isSetImportAppearance()) return true;
		if (importTextures.isSelected() != appearances.isSetImportTextureFiles()) return true;
		if (!impAppOldText.getText().equals(appearances.getThemeForTexturedSurface())) return true;

		return false;
	}

	private void initGui() {
		importAppearance = new JCheckBox();
		importTextures = new JCheckBox();
		impAppOldLabel = new JLabel();
		impAppOldText = new JTextField();

		PopupMenuDecorator.getInstance().decorate(impAppOldText);
		
		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(importTextures, GuiUtil.setConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(impAppOldLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 5));
				content.add(impAppOldText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.BOTH, 5, 5, 0, 0));
			}

			appearancePanel = new TitledPanel().withToggleButton(importAppearance).build(content);
		}

		add(appearancePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		importAppearance.addActionListener(e -> setEnabledTheme());
	}
	
	private void setEnabledTheme() {
		importTextures.setEnabled(importAppearance.isSelected());
		impAppOldLabel.setEnabled(importAppearance.isSelected());
		impAppOldText.setEnabled(importAppearance.isSelected());
	}

	@Override
	public void doTranslation() {
		appearancePanel.setTitle(Language.I18N.getString("pref.import.appearance.border.import"));
		importTextures.setText(Language.I18N.getString("pref.import.appearance.label.importTexture"));
		impAppOldLabel.setText(Language.I18N.getString("pref.import.appearance.label.texturedSurface.theme"));
	}

	@Override
	public void loadSettings() {
		ImportAppearance appearances = config.getImportConfig().getAppearances();

		importAppearance.setSelected(appearances.isSetImportAppearance());
		importTextures.setSelected(appearances.isSetImportTextureFiles());
		impAppOldText.setText(appearances.getThemeForTexturedSurface());
		
		setEnabledTheme();
	}

	@Override
	public void setSettings() {
		ImportAppearance appearances = config.getImportConfig().getAppearances();

		appearances.setImportAppearances(importAppearance.isSelected());
		appearances.setImportTextureFiles(importTextures.isSelected());

		String theme = impAppOldText.getText();
		if (theme == null || theme.trim().length() == 0)
			theme = "rgbTexture";

		appearances.setThemeForTexturedSurface(theme);
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.appearance");
	}

}
