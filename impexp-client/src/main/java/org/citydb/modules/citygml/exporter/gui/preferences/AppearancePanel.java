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
package org.citydb.modules.citygml.exporter.gui.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.ExportAppearance;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;

@SuppressWarnings("serial")
public class AppearancePanel extends AbstractPreferencesComponent {
	private JPanel exportBlock;
	private JPanel pathBlock;

	private JCheckBox overwriteCheck;
	private JCheckBox noTexturesCheck;
	private JCheckBox generateUniqueCheck;
	private JRadioButton exportAll;
	private JRadioButton noExport;
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
		if (exportAll.isSelected() && !appearances.isSetExportAppearance()) return true;
		if (noExport.isSelected() && appearances.isSetExportAppearance()) return true;
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
		exportAll = new JRadioButton();
		noExport = new JRadioButton();

		ButtonGroup expAppRadio = new ButtonGroup();
		expAppRadio.add(noExport);
		expAppRadio.add(exportAll);

		pathLabel = new JLabel();
		pathText = new JTextField();
		browseButton = new JButton();

		useBuckets = new JCheckBox();
		DecimalFormat bucketsFormat = new DecimalFormat("########");
		bucketsFormat.setMaximumIntegerDigits(8);
		bucketsFormat.setMinimumIntegerDigits(1);		
		noOfBuckets = new JFormattedTextField(bucketsFormat);

		PopupMenuDecorator.getInstance().decorate(pathText, noOfBuckets);
		
		browseButton.addActionListener(e -> {
			String path = browseFile(Language.I18N.getString("pref.export.appearance.label.absPath"), pathText.getText());
			if (!path.isEmpty())
				pathText.setText(path);
		});

		setLayout(new GridBagLayout());
		{
			exportBlock = new JPanel();
			add(exportBlock, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			exportBlock.setBorder(BorderFactory.createTitledBorder(""));
			exportBlock.setLayout(new GridBagLayout());
			{
				exportAll.setIconTextGap(10);
				noTexturesCheck.setIconTextGap(10);
				noExport.setIconTextGap(10);
				overwriteCheck.setIconTextGap(10);
				generateUniqueCheck.setIconTextGap(10);
				int lmargin = (int) (exportAll.getPreferredSize().getWidth()) + 11;
				{
					exportBlock.add(exportAll, GuiUtil.setConstraints(0, 0, 0, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
					exportBlock.add(overwriteCheck, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 0, lmargin, 0, 5));
					exportBlock.add(generateUniqueCheck, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 0, lmargin, 0, 5));
					exportBlock.add(noTexturesCheck, GuiUtil.setConstraints(0, 3, 1, 1, GridBagConstraints.BOTH, 0, lmargin, 0, 5));
					exportBlock.add(noExport, GuiUtil.setConstraints(0, 4, 0, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
				}
			}

			pathBlock = new JPanel();
			add(pathBlock, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			pathBlock.setBorder(BorderFactory.createTitledBorder(""));
			pathBlock.setLayout(new GridBagLayout());
			{
				pathBlock.add(pathLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 5));
				pathBlock.add(pathText, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
				pathBlock.add(browseButton, GuiUtil.setConstraints(2, 0, 0, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));

				Box box = Box.createHorizontalBox();
				box.add(useBuckets);
				box.add(Box.createHorizontalStrut(5));
				box.add(noOfBuckets);
				useBuckets.setIconTextGap(10);
				pathBlock.add(box, GuiUtil.setConstraints(0, 1, 2, 1, 0, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
			}

		}

		noExport.addActionListener(e -> setEnabledTextureExport());
		exportAll.addActionListener(e -> setEnabledTextureExport());
		overwriteCheck.addActionListener(e -> {if (overwriteCheck.isSelected()) noTexturesCheck.setSelected(false); });
		noTexturesCheck.addActionListener(e -> {if (noTexturesCheck.isSelected()) overwriteCheck.setSelected(false); });
		useBuckets.addActionListener(e -> noOfBuckets.setEnabled(useBuckets.isSelected()));
		
		noOfBuckets.addPropertyChangeListener(evt -> {
			if (((Number)noOfBuckets.getValue()).intValue() < 0)
				noOfBuckets.setValue(-((Number)noOfBuckets.getValue()).intValue());
		});
	}

	private void setEnabledTextureExport() {
		overwriteCheck.setEnabled(exportAll.isSelected());
		noTexturesCheck.setEnabled(exportAll.isSelected());
		generateUniqueCheck.setEnabled(exportAll.isSelected());

		((TitledBorder) pathBlock.getBorder()).setTitleColor(exportAll.isSelected() ?
				UIManager.getColor("TitledBorder.titleColor") :
				UIManager.getColor("Label.disabledForeground"));
		pathBlock.repaint();

		pathLabel.setEnabled(exportAll.isSelected());
		pathText.setEnabled(exportAll.isSelected());
		browseButton.setEnabled(exportAll.isSelected());
		useBuckets.setEnabled(exportAll.isSelected());
		noOfBuckets.setEnabled(exportAll.isSelected() && useBuckets.isSelected());
	}

	@Override
	public void doTranslation() {
		((TitledBorder) exportBlock.getBorder()).setTitle(Language.I18N.getString("pref.export.appearance.border.export"));
		((TitledBorder) pathBlock.getBorder()).setTitle(Language.I18N.getString("pref.export.appearance.border.path"));
		overwriteCheck.setText(Language.I18N.getString("pref.export.appearance.label.exportWithTexture.overwrite"));
		generateUniqueCheck.setText(Language.I18N.getString("pref.export.appearance.label.exportWithTexture.unique"));
		noExport.setText(Language.I18N.getString("pref.export.appearance.label.noExport"));
		noTexturesCheck.setText(Language.I18N.getString("pref.export.appearance.label.exportWithoutTexture"));
		exportAll.setText(Language.I18N.getString("pref.export.appearance.label.exportWithTexture"));
		pathLabel.setText(Language.I18N.getString("pref.export.appearance.label.path"));
		browseButton.setText(Language.I18N.getString("common.button.browse"));
		useBuckets.setText(Language.I18N.getString("pref.export.appearance.label.useBuckets"));
	}

	@Override
	public void loadSettings() {
		ExportAppearance appearances = config.getExportConfig().getAppearances();

		if (appearances.isSetExportAppearance())
			exportAll.setSelected(true);
		else
			noExport.setSelected(true);

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

		if (exportAll.isSelected())
			appearances.setExportAppearances(true);
		else if (noExport.isSelected())
			appearances.setExportAppearances(false);

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
