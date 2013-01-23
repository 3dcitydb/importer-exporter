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
package de.tub.citydb.modules.citygml.importer.gui.preferences;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.importer.ImportAppearance;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class AppearancePanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JPanel block2;
	private JLabel impAppOldLabel;
	private JRadioButton impAppRadioNoImp;
	private JRadioButton impAppRadioAppImp;
	private JRadioButton impAppRadioImp;
	private JTextField impAppOldText;

	public AppearancePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportAppearance appearances = config.getProject().getImporter().getAppearances();
		
		if (!impAppOldText.getText().equals(appearances.getThemeForTexturedSurface())) return true;
		if (impAppRadioImp.isSelected() && !(appearances.isSetImportAppearance() && appearances.isSetImportTextureFiles())) return true;
		if (impAppRadioAppImp.isSelected() && !(appearances.isSetImportAppearance() && !appearances.isSetImportTextureFiles())) return true;
		if (impAppRadioNoImp.isSelected() && !(!appearances.isSetImportAppearance() && !appearances.isSetImportTextureFiles())) return true;
		
		return false;
	}

	private void initGui() {
		impAppRadioNoImp = new JRadioButton();
		impAppRadioAppImp = new JRadioButton();
		impAppRadioImp = new JRadioButton();
		ButtonGroup impAppRadio = new ButtonGroup();
		impAppRadio.add(impAppRadioNoImp);
		impAppRadio.add(impAppRadioImp);
		impAppRadio.add(impAppRadioAppImp);
		impAppOldLabel = new JLabel();
		impAppOldText = new JTextField();

		PopupMenuDecorator.getInstance().decorate(impAppOldText);
		
		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			impAppRadioImp.setIconTextGap(10);
			impAppRadioAppImp.setIconTextGap(10);
			impAppRadioNoImp.setIconTextGap(10);
			{
				block1.add(impAppRadioImp, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impAppRadioAppImp, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impAppRadioNoImp, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
			
			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			{
				Box themeBox = Box.createHorizontalBox();
				themeBox.add(impAppOldLabel);
				themeBox.add(Box.createRigidArea(new Dimension(10, 0)));
				themeBox.add(impAppOldText);

				block2.add(themeBox, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,0,5));
			}
		}
		
		ActionListener themeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledTheme();
			}
		};
		
		impAppRadioNoImp.addActionListener(themeListener);
		impAppRadioAppImp.addActionListener(themeListener);
		impAppRadioImp.addActionListener(themeListener);
	}
	
	private void setEnabledTheme() {
		((TitledBorder)block2.getBorder()).setTitleColor(!impAppRadioNoImp.isSelected() ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		block2.repaint();
		
		impAppOldLabel.setEnabled(!impAppRadioNoImp.isSelected());
		impAppOldText.setEnabled(!impAppRadioNoImp.isSelected());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("pref.import.appearance.border.import"));	
		((TitledBorder)block2.getBorder()).setTitle(Internal.I18N.getString("pref.import.appearance.border.texturedSurface"));	

		impAppRadioNoImp.setText(Internal.I18N.getString("pref.import.appearance.label.noImport"));
		impAppRadioAppImp.setText(Internal.I18N.getString("pref.import.appearance.label.importWithoutTexture"));
		impAppRadioImp.setText(Internal.I18N.getString("pref.import.appearance.label.importWithTexture"));
		impAppOldLabel.setText(Internal.I18N.getString("pref.import.appearance.label.theme"));
	}

	@Override
	public void loadSettings() {
		ImportAppearance appearances = config.getProject().getImporter().getAppearances();
		
		if (appearances.isSetImportAppearance()) {			
			if (appearances.isSetImportTextureFiles()) 
				impAppRadioImp.setSelected(true);
			else
				impAppRadioAppImp.setSelected(true);
		} else
			impAppRadioNoImp.setSelected(true);

		impAppOldText.setText(appearances.getThemeForTexturedSurface());
		
		setEnabledTheme();
	}

	@Override
	public void setSettings() {
		ImportAppearance appearances = config.getProject().getImporter().getAppearances();

		if (impAppRadioImp.isSelected()) {
			appearances.setImportAppearances(true);
			appearances.setImportTextureFiles(true);
		}
		if (impAppRadioAppImp.isSelected()) {
			appearances.setImportAppearances(true);
			appearances.setImportTextureFiles(false);
		}
		if (impAppRadioNoImp.isSelected()) {
			appearances.setImportAppearances(false);
			appearances.setImportTextureFiles(false);
		}
		
		String theme = impAppOldText.getText();
		if (theme == null || theme.trim().length() == 0)
			theme = "rgbTexture";

		appearances.setThemeForTexturedSurface(theme);
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.import.appearance");
	}

}
