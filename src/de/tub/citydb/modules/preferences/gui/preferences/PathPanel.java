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
package de.tub.citydb.modules.preferences.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.general.Path;
import de.tub.citydb.config.project.general.PathMode;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class PathPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JRadioButton importPathRadioLast;
	private JRadioButton importPathRadioDef;
	private JTextField importPathText;
	private JButton importPathButton;
	private JPanel block2;
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
		Path importPath = config.getProject().getImporter().getPath();
		Path exportPath = config.getProject().getExporter().getPath();;
		
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
		
		importPathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sImp = browseFile(Internal.I18N.getString("pref.general.path.label.importDefaultPath"), importPathText.getText());
				if (!sImp.isEmpty())
					importPathText.setText(sImp);
			}
		});
		
		exportPathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sExp = browseFile(Internal.I18N.getString("pref.general.path.label.exportDefaultPath"), exportPathText.getText());
				if (!sExp.isEmpty())
					exportPathText.setText(sExp);
			}
		});

		setLayout(new GridBagLayout());
		{

			block1 = new JPanel();
			block2 = new JPanel();

			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));

			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			importPathRadioLast.setIconTextGap(10);
			importPathRadioDef.setIconTextGap(10);
			importPathText.setPreferredSize(importPathText.getSize());
			int lmargin = (int)(importPathRadioLast.getPreferredSize().getWidth()) + 11;
			{
				block1.add(importPathRadioLast, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(importPathRadioDef, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(importPathText, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				block1.add(importPathButton, GuiUtil.setConstraints(1,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
			}
			
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			exportPathRadioLast.setIconTextGap(10);
			exportPathRadioDef.setIconTextGap(10);
			exportPathText.setPreferredSize(exportPathText.getSize());
			{
				block2.add(exportPathRadioLast, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(exportPathRadioDef, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(exportPathText, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				block2.add(exportPathButton, GuiUtil.setConstraints(1,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
			}
		}
		
		ActionListener importListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledImportPath();
			}
		};
		
		ActionListener exportListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledExportPath();
			}
		};
		
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
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("pref.general.path.border.importPath"));
		importPathRadioLast.setText(Internal.I18N.getString("pref.general.path.label.importLastUsedPath"));
		importPathRadioDef.setText(Internal.I18N.getString("pref.general.path.label.importDefaultPath"));
		importPathButton.setText(Internal.I18N.getString("common.button.browse"));		
		((TitledBorder)block2.getBorder()).setTitle(Internal.I18N.getString("pref.general.path.border.exportPath"));
		exportPathRadioLast.setText(Internal.I18N.getString("pref.general.path.label.exportLastUsedPath"));
		exportPathRadioDef.setText(Internal.I18N.getString("pref.general.path.label.exportDefaultPath"));
		exportPathButton.setText(Internal.I18N.getString("common.button.browse"));
	}

	@Override
	public void loadSettings() {
		Path path = config.getProject().getImporter().getPath();

		if (path.isSetLastUsedMode())
			importPathRadioLast.setSelected(true);
		else
			importPathRadioDef.setSelected(true);

		importPathText.setText(path.getStandardPath());

		path = config.getProject().getExporter().getPath();
		if (path.isSetLastUsedMode())
			exportPathRadioLast.setSelected(true);
		else
			exportPathRadioDef.setSelected(true);

		exportPathText.setText(path.getStandardPath());

		setEnabledImportPath();
		setEnabledExportPath();
	}

	@Override
	public void setSettings() {
		Path path = config.getProject().getImporter().getPath();
		
		if (importPathRadioDef.isSelected()) {
			path.setPathMode(PathMode.STANDARD);
		}
		else {
			path.setPathMode(PathMode.LASTUSED);
		}

		path.setStandardPath(importPathText.getText());
		path = config.getProject().getExporter().getPath();
		
		if (exportPathRadioDef.isSelected()) {
			path.setPathMode(PathMode.STANDARD);
		}
		else {
			path.setPathMode(PathMode.LASTUSED);
		}

		path.setStandardPath(exportPathText.getText());
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.general.path");
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
