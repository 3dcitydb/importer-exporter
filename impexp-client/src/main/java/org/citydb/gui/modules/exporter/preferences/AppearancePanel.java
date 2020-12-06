/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.gui.modules.exporter.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.ExportAppearance;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;

public class AppearancePanel extends AbstractPreferencesComponent {
	private TitledPanel exportPanel;
	private TitledPanel pathPanel;

	private JCheckBox exportAppearances;
	private JCheckBox overwriteCheck;
	private JCheckBox noTexturesCheck;
	private JCheckBox generateUniqueCheck;
	private JLabel pathLabel;
	private JTextField pathText;
	private JButton browseButton;
	private JCheckBox useBuckets;
	private JFormattedTextField noOfBuckets;
	
	public AppearancePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ExportAppearance appearances = config.getExportConfig().getAppearances();

		try { noOfBuckets.commitEdit(); } catch (ParseException ignored) { }

		if (!pathText.getText().equals(appearances.getTexturePath().getPath())) return true;
		if (exportAppearances.isSelected() != appearances.isSetExportAppearance()) return true;
		if (noTexturesCheck.isSelected() == appearances.isSetExportTextureFiles()) return true;
		if (overwriteCheck.isSelected() != appearances.isSetOverwriteTextureFiles()) return true;
		if (generateUniqueCheck.isSelected() != appearances.isSetUniqueTextureFileNames()) return true;
		if (useBuckets.isSelected() != appearances.getTexturePath().isUseBuckets()) return true;
		if (((Number)noOfBuckets.getValue()).intValue() != appearances.getTexturePath().getNoOfBuckets()) return true;

		return false;
	}

	private void initGui() {
		overwriteCheck = new JCheckBox();
		noTexturesCheck = new JCheckBox();
		generateUniqueCheck = new JCheckBox();
		exportAppearances = new JCheckBox();

		ButtonGroup group = new ButtonGroup();
		group.add(overwriteCheck);
		group.add(noTexturesCheck);

		pathLabel = new JLabel();
		pathText = new JTextField();
		browseButton = new JButton();

		useBuckets = new JCheckBox();
		NumberFormatter bucketsFormat = new NumberFormatter(new DecimalFormat("#"));
		bucketsFormat.setMinimum(99999999);
		bucketsFormat.setMinimum(0);
		noOfBuckets = new JFormattedTextField(bucketsFormat);

		PopupMenuDecorator.getInstance().decorate(pathText, noOfBuckets);
		
		browseButton.addActionListener(e -> {
			String path = browseFile(Language.I18N.getString("pref.export.appearance.label.absPath"), pathText.getText());
			if (!path.isEmpty())
				pathText.setText(path);
		});

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(overwriteCheck, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(generateUniqueCheck, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(noTexturesCheck, GuiUtil.setConstraints(0, 3, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			}

			exportPanel = new TitledPanel()
					.withToggleButton(exportAppearances)
					.build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(pathLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(pathText, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
				content.add(browseButton, GuiUtil.setConstraints(2, 0, 0, 1, GridBagConstraints.BOTH, 0, 5, 0, 0));

				Box box = Box.createHorizontalBox();
				box.add(useBuckets);
				box.add(Box.createHorizontalStrut(10));
				box.add(noOfBuckets);
				content.add(box, GuiUtil.setConstraints(0, 1, 2, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 5));
			}

			pathPanel = new TitledPanel().build(content);
		}

		add(exportPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(pathPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		exportAppearances.addActionListener(e -> setEnabledTextureExport());
		useBuckets.addActionListener(e -> noOfBuckets.setEnabled(useBuckets.isSelected()));
	}

	private void setEnabledTextureExport() {
		overwriteCheck.setEnabled(exportAppearances.isSelected());
		noTexturesCheck.setEnabled(exportAppearances.isSelected());
		generateUniqueCheck.setEnabled(exportAppearances.isSelected());
		pathLabel.setEnabled(exportAppearances.isSelected());
		pathText.setEnabled(exportAppearances.isSelected());
		browseButton.setEnabled(exportAppearances.isSelected());
		useBuckets.setEnabled(exportAppearances.isSelected());
		noOfBuckets.setEnabled(exportAppearances.isSelected() && useBuckets.isSelected());
	}

	@Override
	public void doTranslation() {
		exportPanel.setTitle(Language.I18N.getString("pref.export.appearance.border.export"));
		pathPanel.setTitle(Language.I18N.getString("pref.export.appearance.border.path"));
		overwriteCheck.setText(Language.I18N.getString("pref.export.appearance.label.overwriteTextures"));
		generateUniqueCheck.setText(Language.I18N.getString("pref.export.appearance.label.uniqueTextures"));
		noTexturesCheck.setText(Language.I18N.getString("pref.export.appearance.label.noTextures"));
		pathLabel.setText(Language.I18N.getString("pref.export.appearance.label.path"));
		browseButton.setText(Language.I18N.getString("common.button.browse"));
		useBuckets.setText(Language.I18N.getString("pref.export.appearance.label.useBuckets"));
	}

	@Override
	public void loadSettings() {
		ExportAppearance appearances = config.getExportConfig().getAppearances();

		exportAppearances.setSelected(appearances.isSetExportAppearance());
		noTexturesCheck.setSelected(!appearances.isSetExportTextureFiles());
		overwriteCheck.setSelected(appearances.isSetOverwriteTextureFiles());
		generateUniqueCheck.setSelected(appearances.isSetUniqueTextureFileNames());
		pathText.setText(appearances.getTexturePath().getPath());
		useBuckets.setSelected(appearances.getTexturePath().isUseBuckets());
		noOfBuckets.setValue(appearances.getTexturePath().getNoOfBuckets());
		
		setEnabledTextureExport();
	}

	@Override
	public void setSettings() {
		ExportAppearance appearances = config.getExportConfig().getAppearances();

		appearances.setExportAppearances(exportAppearances.isSelected());
		appearances.setExportTextureFiles(!noTexturesCheck.isSelected());
		appearances.setOverwriteTextureFiles(overwriteCheck.isSelected());
		appearances.setUniqueTextureFileNames(generateUniqueCheck.isSelected());
		appearances.getTexturePath().setPath(pathText.getText());
		appearances.getTexturePath().setUseBuckets(useBuckets.isSelected());
		appearances.getTexturePath().setNoOfBuckets(((Number)noOfBuckets.getValue()).intValue());
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.export.appearance");
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
