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
package org.citydb.gui.operation.preferences.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.common.Path;
import org.citydb.config.project.common.PathMode;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;

public class PathPanel extends InternalPreferencesComponent {
	private TitledPanel importPanel;
	private JRadioButton importPathRadioLast;
	private JRadioButton importPathRadioDef;
	private JTextField importPathText;
	private JButton importPathButton;
	private TitledPanel exportPanel;
	private JRadioButton exportPathRadioLast;
	private JRadioButton exportPathRadioDef;
	private JTextField exportPathText;
	private JButton exportPathButton;
	
	public PathPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		Path importPath = config.getImportConfig().getPath();
		Path exportPath = config.getExportConfig().getPath();;
		
		if (importPathRadioLast.isSelected() != importPath.isSetLastUsedMode()) return true;
		if (importPathRadioDef.isSelected() != importPath.isSetStandardMode()) return true;
		if (exportPathRadioLast.isSelected() != exportPath.isSetLastUsedMode()) return true;
		if (exportPathRadioDef.isSelected() != exportPath.isSetStandardMode()) return true;
		if (!importPathText.getText().equals(importPath.getStandardPath())) return true;
		if (!exportPathText.getText().equals(exportPath.getStandardPath())) return true;
		return false;
	}

	private void initGui() {
		importPathRadioLast = new JRadioButton();
		importPathRadioDef = new JRadioButton();
		ButtonGroup importPathRadio = new ButtonGroup();
		importPathRadio.add(importPathRadioLast);
		importPathRadio.add(importPathRadioDef);
		
		importPathText = new JTextField();
		importPathButton = new JButton();
		
		exportPathRadioLast = new JRadioButton();
		exportPathRadioDef = new JRadioButton();
		ButtonGroup exportPathRadio = new ButtonGroup();
		exportPathRadio.add(exportPathRadioLast);
		exportPathRadio.add(exportPathRadioDef);
		
		exportPathText = new JTextField();
		exportPathButton = new JButton();

		PopupMenuDecorator.getInstance().decorate(importPathText, exportPathText);

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(importPathRadioLast, GuiUtil.setConstraints(0, 0, 3, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(importPathRadioDef, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(importPathText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 5));
				content.add(importPathButton, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			importPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(exportPathRadioLast, GuiUtil.setConstraints(0, 0, 3, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(exportPathRadioDef, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(exportPathText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 5));
				content.add(exportPathButton, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			exportPanel = new TitledPanel().build(content);
		}

		add(importPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(exportPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		importPathButton.addActionListener(e -> {
			String sImp = browseFile(Language.I18N.getString("pref.general.path.label.importDefaultPath"), importPathText.getText());
			if (!sImp.isEmpty())
				importPathText.setText(sImp);
		});

		exportPathButton.addActionListener(e -> {
			String sExp = browseFile(Language.I18N.getString("pref.general.path.label.exportDefaultPath"), exportPathText.getText());
			if (!sExp.isEmpty())
				exportPathText.setText(sExp);
		});
		
		ActionListener importListener = e -> setEnabledImportPath();
		ActionListener exportListener = e -> setEnabledExportPath();
		
		importPathRadioLast.addActionListener(importListener);
		importPathRadioDef.addActionListener(importListener);
		
		exportPathRadioLast.addActionListener(exportListener);
		exportPathRadioDef.addActionListener(exportListener);
	}
	
	private void setEnabledImportPath() {
		importPathText.setEnabled(importPathRadioDef.isSelected());
		importPathButton.setEnabled(importPathRadioDef.isSelected());
	}
	
	private void setEnabledExportPath() {
		exportPathText.setEnabled(exportPathRadioDef.isSelected());
		exportPathButton.setEnabled(exportPathRadioDef.isSelected());
	}

	@Override
	public void switchLocale(Locale locale) {
		importPanel.setTitle(Language.I18N.getString("pref.general.path.border.importPath"));
		importPathRadioLast.setText(Language.I18N.getString("pref.general.path.label.importLastUsedPath"));
		importPathRadioDef.setText(Language.I18N.getString("pref.general.path.label.importDefaultPath"));
		importPathButton.setText(Language.I18N.getString("common.button.browse"));
		exportPanel.setTitle(Language.I18N.getString("pref.general.path.border.exportPath"));
		exportPathRadioLast.setText(Language.I18N.getString("pref.general.path.label.exportLastUsedPath"));
		exportPathRadioDef.setText(Language.I18N.getString("pref.general.path.label.exportDefaultPath"));
		exportPathButton.setText(Language.I18N.getString("common.button.browse"));
	}

	@Override
	public void loadSettings() {
		Path path = config.getImportConfig().getPath();

		if (path.isSetLastUsedMode()) {
			importPathRadioLast.setSelected(true);
		} else {
			importPathRadioDef.setSelected(true);
		}

		importPathText.setText(path.getStandardPath());

		path = config.getExportConfig().getPath();
		if (path.isSetLastUsedMode()) {
			exportPathRadioLast.setSelected(true);
		} else {
			exportPathRadioDef.setSelected(true);
		}

		exportPathText.setText(path.getStandardPath());

		setEnabledImportPath();
		setEnabledExportPath();
	}

	@Override
	public void setSettings() {
		Path path = config.getImportConfig().getPath();
		
		if (importPathRadioDef.isSelected()) {
			path.setPathMode(PathMode.STANDARD);
		} else {
			path.setPathMode(PathMode.LASTUSED);
		}

		path.setStandardPath(importPathText.getText());
		path = config.getExportConfig().getPath();
		
		if (exportPathRadioDef.isSelected()) {
			path.setPathMode(PathMode.STANDARD);
		} else {
			path.setPathMode(PathMode.LASTUSED);
		}

		path.setStandardPath(exportPathText.getText());
	}
	
	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("pref.tree.general.path");
	}

	private String browseFile(String title, String oldDir) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(oldDir));

		int result = chooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return "";
		return chooser.getSelectedFile().toString();
	}
}
