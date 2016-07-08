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
package org.citydb.modules.citygml.exporter.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.exporter.ExportAppearance;
import org.citydb.config.project.exporter.TexturePathMode;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class AppearancePanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JPanel path1;

	private JCheckBox overwriteCheck;
	private JCheckBox generateUniqueCheck;
	private JRadioButton radioNoExp;
	private JRadioButton radioAppExp;
	private JRadioButton radioExp;
	private JRadioButton radioPathAbs;
	private JRadioButton radioPathRel;
	private JTextField pathAbsText;
	private JButton pathAbsButton;
	private JTextField pathRelText;
	private JCheckBox useBuckets;
	private JFormattedTextField noOfBuckets;
	
	public AppearancePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ExportAppearance appearances = config.getProject().getExporter().getAppearances();

		try { noOfBuckets.commitEdit(); } catch (ParseException e) { }

		if (!pathAbsText.getText().equals(appearances.getTexturePath().getAbsolutePath())) return true;
		if (!pathRelText.getText().equals(appearances.getTexturePath().getRelativePath())) return true;
		if (radioPathRel.isSelected() != appearances.getTexturePath().isRelative()) return true;
		if (radioPathAbs.isSelected() != appearances.getTexturePath().isAbsolute()) return true;
		if (radioExp.isSelected() && !(appearances.isSetExportAppearance() && appearances.isSetExportTextureFiles())) return true;
		if (radioNoExp.isSelected() && !(!appearances.isSetExportAppearance() && !appearances.isSetExportTextureFiles())) return true;
		if (radioAppExp.isSelected() && !(appearances.isSetExportAppearance() && !appearances.isSetExportTextureFiles())) return true;
		if (overwriteCheck.isSelected() != appearances.isSetOverwriteTextureFiles()) return true;
		if (generateUniqueCheck.isSelected() != appearances.isSetUniqueTextureFileNames()) return true;
		if (useBuckets.isSelected() != appearances.getTexturePath().isUseBuckets()) return true;
		if (((Number)noOfBuckets.getValue()).intValue() != appearances.getTexturePath().getNoOfBuckets()) return true;

		return false;
	}

	private void initGui() {
		overwriteCheck = new JCheckBox();
		generateUniqueCheck = new JCheckBox();
		radioNoExp = new JRadioButton();
		radioAppExp = new JRadioButton();
		radioExp = new JRadioButton();
		ButtonGroup expAppRadio = new ButtonGroup();
		expAppRadio.add(radioNoExp);
		expAppRadio.add(radioAppExp);
		expAppRadio.add(radioExp);
		radioPathAbs = new JRadioButton();
		radioPathRel = new JRadioButton();
		ButtonGroup expAppRadioPath = new ButtonGroup();
		expAppRadioPath.add(radioPathAbs);
		expAppRadioPath.add(radioPathRel);
		pathAbsText = new JTextField();
		pathAbsButton = new JButton();
		pathRelText = new JTextField();

		useBuckets = new JCheckBox();
		DecimalFormat bucketsFormat = new DecimalFormat("########");
		bucketsFormat.setMaximumIntegerDigits(8);
		bucketsFormat.setMinimumIntegerDigits(1);		
		noOfBuckets = new JFormattedTextField(bucketsFormat);

		PopupMenuDecorator.getInstance().decorate(pathAbsText, noOfBuckets);
		
		pathAbsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sImp = browseFile(Language.I18N.getString("pref.export.appearance.label.absPath"), pathAbsText.getText());
				if (!sImp.isEmpty())
					pathAbsText.setText(sImp);
			}
		});

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			path1 = new JPanel();

			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			radioExp.setIconTextGap(10);
			radioAppExp.setIconTextGap(10);
			radioNoExp.setIconTextGap(10);
			overwriteCheck.setIconTextGap(10);
			generateUniqueCheck.setIconTextGap(10);
			int lmargin = (int)(radioExp.getPreferredSize().getWidth()) + 11;
			{
				block1.add(radioExp, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(overwriteCheck, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));
				block1.add(generateUniqueCheck, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));
				block1.add(radioAppExp, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(radioNoExp, GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}

			add(path1, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			path1.setBorder(BorderFactory.createTitledBorder(""));
			path1.setLayout(new GridBagLayout());
			radioPathAbs.setIconTextGap(10);
			radioPathRel.setIconTextGap(10);
			pathAbsText.setPreferredSize(pathAbsText.getSize());
			{
				path1.add(radioPathAbs, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				path1.add(pathAbsText, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));
				path1.add(pathAbsButton, GuiUtil.setConstraints(1,1,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				path1.add(radioPathRel, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,5,5,0,5));
				path1.add(pathRelText, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				
				Box box = Box.createHorizontalBox();
				box.add(useBuckets);
				box.add(Box.createHorizontalStrut(5));
				box.add(noOfBuckets);
				useBuckets.setIconTextGap(10);
				path1.add(box, GuiUtil.setConstraints(0,4,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
				pathRelText.setPreferredSize(useBuckets.getPreferredSize());
			}

		}

		ActionListener textureExportListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledTextureExport();
			}
		};
		
		ActionListener texturePathListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledTexturePath();
			}
		};
		
		radioNoExp.addActionListener(textureExportListener);
		radioAppExp.addActionListener(textureExportListener);
		radioExp.addActionListener(textureExportListener);		
		radioPathAbs.addActionListener(texturePathListener);
		radioPathRel.addActionListener(texturePathListener);
		
		useBuckets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				noOfBuckets.setEnabled(useBuckets.isSelected());
			}
		});
		
		noOfBuckets.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (((Number)noOfBuckets.getValue()).intValue() < 0)
					noOfBuckets.setValue(-((Number)noOfBuckets.getValue()).intValue());
			}
		});
	}

	private void setEnabledTextureExport() {
		overwriteCheck.setEnabled(radioExp.isSelected());
		generateUniqueCheck.setEnabled(radioExp.isSelected());
		
		((TitledBorder)path1.getBorder()).setTitleColor(radioExp.isSelected() ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		path1.repaint();

		radioPathAbs.setEnabled(radioExp.isSelected());
		radioPathRel.setEnabled(radioExp.isSelected());
		pathAbsText.setEnabled(radioExp.isSelected() && radioPathAbs.isSelected());
		pathAbsButton.setEnabled(radioExp.isSelected() && radioPathAbs.isSelected());
		pathRelText.setEnabled(radioExp.isSelected() && radioPathRel.isSelected());
		useBuckets.setEnabled(radioExp.isSelected());
		noOfBuckets.setEnabled(radioExp.isSelected() && useBuckets.isSelected());
	}
	
	private void setEnabledTexturePath() {
		pathAbsText.setEnabled(radioPathAbs.isSelected());
		pathAbsButton.setEnabled(radioPathAbs.isSelected());
		pathRelText.setEnabled(radioPathRel.isSelected());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.export.appearance.border.export"));
		((TitledBorder)path1.getBorder()).setTitle(Language.I18N.getString("pref.export.appearance.border.path"));
		overwriteCheck.setText(Language.I18N.getString("pref.export.appearance.label.exportWithTexture.overwrite"));
		generateUniqueCheck.setText(Language.I18N.getString("pref.export.appearance.label.exportWithTexture.unique"));
		radioNoExp.setText(Language.I18N.getString("pref.export.appearance.label.noExport"));
		radioAppExp.setText(Language.I18N.getString("pref.export.appearance.label.exportWithoutTexture"));
		radioExp.setText(Language.I18N.getString("pref.export.appearance.label.exportWithTexture"));
		radioPathAbs.setText(Language.I18N.getString("pref.export.appearance.label.absPath"));
		radioPathRel.setText(Language.I18N.getString("pref.export.appearance.label.relPath"));
		pathAbsButton.setText(Language.I18N.getString("common.button.browse"));
		useBuckets.setText(Language.I18N.getString("pref.export.appearance.label.useBuckets"));
	}

	@Override
	public void loadSettings() {
		ExportAppearance appearances = config.getProject().getExporter().getAppearances();

		if (appearances.isSetExportAppearance()) {
			if (appearances.isSetExportTextureFiles())
				radioExp.setSelected(true);
			else
				radioAppExp.setSelected(true);
		} else
			radioNoExp.setSelected(true);
		
		overwriteCheck.setSelected(appearances.isSetOverwriteTextureFiles());
		generateUniqueCheck.setSelected(appearances.isSetUniqueTextureFileNames());
		pathRelText.setText(appearances.getTexturePath().getRelativePath());
		pathAbsText.setText(appearances.getTexturePath().getAbsolutePath());
		radioPathRel.setSelected(appearances.getTexturePath().isRelative());
		radioPathAbs.setSelected(!appearances.getTexturePath().isRelative());
		useBuckets.setSelected(appearances.getTexturePath().isUseBuckets());
		noOfBuckets.setValue(appearances.getTexturePath().getNoOfBuckets());
		
		setEnabledTextureExport();
	}

	@Override
	public void setSettings() {
		ExportAppearance appearances = config.getProject().getExporter().getAppearances();

		if (radioExp.isSelected()) {
			appearances.setExportAppearances(true);
			appearances.setExportTextureFiles(true);
		}
		if (radioAppExp.isSelected()) {
			appearances.setExportAppearances(true);
			appearances.setExportTextureFiles(false);
		}
		if (radioNoExp.isSelected()) {
			appearances.setExportAppearances(false);
			appearances.setExportTextureFiles(false);
		}

		appearances.setOverwriteTextureFiles(overwriteCheck.isSelected());
		appearances.setUniqueTextureFileNames(generateUniqueCheck.isSelected());
		appearances.getTexturePath().setAbsolutePath(pathAbsText.getText());
		
		if (pathRelText.getText() == null || pathRelText.getText().trim().length() == 0)
			pathRelText.setText("appearance"); 
		appearances.getTexturePath().setRelativePath(pathRelText.getText());

		if (radioPathRel.isSelected())
			appearances.getTexturePath().setMode(TexturePathMode.RELATIVE);
		else
			appearances.getTexturePath().setMode(TexturePathMode.ABSOLUTE);
		
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
		String browseString = chooser.getSelectedFile().toString();
		return browseString;
	}
}
