/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.exporter.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.exporter.ExportAppearance;
import de.tub.citydb.config.project.exporter.TexturePathMode;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

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

	public AppearancePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ExportAppearance appearances = config.getProject().getExporter().getAppearances();

		if (!pathAbsText.getText().equals(appearances.getAbsoluteTexturePath())) return true;
		if (!pathRelText.getText().equals(appearances.getRelativeTexturePath())) return true;
		if (radioPathRel.isSelected() != appearances.isTexturePathRealtive()) return true;
		if (radioPathAbs.isSelected() != appearances.isTexturePathAbsolute()) return true;
		if (radioExp.isSelected() && !(appearances.isSetExportAppearance() && appearances.isSetExportTextureFiles())) return true;
		if (radioNoExp.isSelected() && !(!appearances.isSetExportAppearance() && !appearances.isSetExportTextureFiles())) return true;
		if (radioAppExp.isSelected() && !(appearances.isSetExportAppearance() && !appearances.isSetExportTextureFiles())) return true;
		if (overwriteCheck.isSelected() != appearances.isSetOverwriteTextureFiles()) return true;
		if (generateUniqueCheck.isSelected() != appearances.isSetUniqueTextureFileNames()) return true;
		
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

		PopupMenuDecorator.getInstance().decorate(pathAbsText);
		
		pathAbsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sImp = browseFile(Internal.I18N.getString("pref.export.appearance.label.absPath"), pathAbsText.getText());
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
	}

	private void setEnabledTextureExport() {
		overwriteCheck.setEnabled(radioExp.isSelected());
		generateUniqueCheck.setEnabled(radioExp.isSelected());
		
		((TitledBorder)path1.getBorder()).setTitleColor(!radioNoExp.isSelected() ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		path1.repaint();

		radioPathAbs.setEnabled(!radioNoExp.isSelected());
		radioPathRel.setEnabled(!radioNoExp.isSelected());
		pathAbsText.setEnabled(!radioNoExp.isSelected() && radioPathAbs.isSelected());
		pathAbsButton.setEnabled(!radioNoExp.isSelected() && radioPathAbs.isSelected());
		pathRelText.setEnabled(!radioNoExp.isSelected() && radioPathRel.isSelected());
	}
	
	private void setEnabledTexturePath() {
		pathAbsText.setEnabled(radioPathAbs.isSelected());
		pathAbsButton.setEnabled(radioPathAbs.isSelected());
		pathRelText.setEnabled(radioPathRel.isSelected());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("pref.export.appearance.border.export"));
		((TitledBorder)path1.getBorder()).setTitle(Internal.I18N.getString("pref.export.appearance.border.path"));
		overwriteCheck.setText(Internal.I18N.getString("pref.export.appearance.label.exportWithTexture.overwrite"));
		generateUniqueCheck.setText(Internal.I18N.getString("pref.export.appearance.label.exportWithTexture.unique"));
		radioNoExp.setText(Internal.I18N.getString("pref.export.appearance.label.noExport"));
		radioAppExp.setText(Internal.I18N.getString("pref.export.appearance.label.exportWithoutTexture"));
		radioExp.setText(Internal.I18N.getString("pref.export.appearance.label.exportWithTexture"));
		radioPathAbs.setText(Internal.I18N.getString("pref.export.appearance.label.absPath"));
		radioPathRel.setText(Internal.I18N.getString("pref.export.appearance.label.relPath"));
		pathAbsButton.setText(Internal.I18N.getString("common.button.browse"));
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
		pathRelText.setText(appearances.getRelativeTexturePath());
		pathAbsText.setText(appearances.getAbsoluteTexturePath());
		radioPathRel.setSelected(appearances.isTexturePathRealtive());
		radioPathAbs.setSelected(!appearances.isTexturePathRealtive());
		
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
		appearances.setAbsoluteTexturePath(pathAbsText.getText());
		
		if (pathRelText.getText() == null || pathRelText.getText().trim().length() == 0)
			pathRelText.setText("appearance"); 
		appearances.setRelativeTexturePath(pathRelText.getText());

		if (radioPathRel.isSelected())
			appearances.setTexturePathMode(TexturePathMode.RELATIVE);
		else
			appearances.setTexturePathMode(TexturePathMode.ABSOLUTE);
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.export.appearance");
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
