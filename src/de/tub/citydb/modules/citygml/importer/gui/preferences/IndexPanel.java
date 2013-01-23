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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.importer.Index;
import de.tub.citydb.config.project.importer.IndexMode;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class IndexPanel extends AbstractPreferencesComponent {
	private JRadioButton impSIRadioDeacAc;
	private JRadioButton impSIRadioDeac;
	private JRadioButton impSIRadioNoDeac;
	private JRadioButton impNIRadioDeacAc;
	private JRadioButton impNIRadioDeac;
	private JRadioButton impNIRadioNoDeac;
	private JPanel block1;
	private JPanel block2;

	public IndexPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		Index index = config.getProject().getImporter().getIndexes();

		if (impSIRadioNoDeac.isSelected() != index.isSpatialIndexModeUnchanged()) return true;
		if (impSIRadioDeacAc.isSelected() != index.isSpatialIndexModeDeactivateActivate()) return true;
		if (impSIRadioDeac.isSelected() != index.isSpatialIndexModeDeactivate()) return true;

		if (impNIRadioNoDeac.isSelected() != index.isNormalIndexModeUnchanged()) return true;
		if (impNIRadioDeacAc.isSelected() != index.isNormalIndexModeDeactivateActivate()) return true;
		if (impNIRadioDeac.isSelected() != index.isNormalIndexModeDeactivate()) return true;

		return false;
	}

	private void initGui() {
		impSIRadioDeacAc = new JRadioButton();
		impSIRadioDeac = new JRadioButton();
		impSIRadioNoDeac = new JRadioButton();
		ButtonGroup impSIRadio = new ButtonGroup();
		impSIRadio.add(impSIRadioNoDeac);
		impSIRadio.add(impSIRadioDeacAc);
		impSIRadio.add(impSIRadioDeac);

		impNIRadioDeacAc = new JRadioButton();
		impNIRadioDeac = new JRadioButton();
		impNIRadioNoDeac = new JRadioButton();
		ButtonGroup impNIRadio = new ButtonGroup();
		impNIRadio.add(impNIRadioNoDeac);
		impNIRadio.add(impNIRadioDeacAc);
		impNIRadio.add(impNIRadioDeac);		

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			impSIRadioNoDeac.setIconTextGap(10);
			impSIRadioDeacAc.setIconTextGap(10);
			impSIRadioDeac.setIconTextGap(10);
			{
				block1.add(impSIRadioNoDeac, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impSIRadioDeacAc, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impSIRadioDeac, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}

			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			impNIRadioNoDeac.setIconTextGap(10);
			impNIRadioDeacAc.setIconTextGap(10);
			impNIRadioDeac.setIconTextGap(10);
			{
				block2.add(impNIRadioNoDeac, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impNIRadioDeacAc, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impNIRadioDeac, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("pref.import.index.spatial.border.handling"));	
		((TitledBorder)block2.getBorder()).setTitle(Internal.I18N.getString("pref.import.index.normal.border.handling"));	

		impSIRadioDeacAc.setText(Internal.I18N.getString("pref.import.index.spatial.label.autoActivate"));
		impSIRadioDeac.setText(Internal.I18N.getString("pref.import.index.spatial.label.manuActivate"));
		impSIRadioNoDeac.setText(Internal.I18N.getString("pref.import.index.spatial.label.keepState"));

		impNIRadioDeacAc.setText(Internal.I18N.getString("pref.import.index.normal.label.autoActivate"));
		impNIRadioDeac.setText(Internal.I18N.getString("pref.import.index.normal.label.manuActivate"));
		impNIRadioNoDeac.setText(Internal.I18N.getString("pref.import.index.normal.label.keepState"));
	}

	@Override
	public void loadSettings() {
		Index index = config.getProject().getImporter().getIndexes();

		if (index.isSpatialIndexModeUnchanged())
			impSIRadioNoDeac.setSelected(true);
		else if (index.isSpatialIndexModeDeactivateActivate())
			impSIRadioDeacAc.setSelected(true);
		else
			impSIRadioDeac.setSelected(true);

		if (index.isNormalIndexModeUnchanged())
			impNIRadioNoDeac.setSelected(true);
		else if (index.isNormalIndexModeDeactivateActivate())
			impNIRadioDeacAc.setSelected(true);
		else
			impNIRadioDeac.setSelected(true);
	}

	@Override
	public void setSettings() {
		Index index = config.getProject().getImporter().getIndexes();

		if (impSIRadioNoDeac.isSelected())
			index.setSpatial(IndexMode.UNCHANGED);
		else if (impSIRadioDeacAc.isSelected())
			index.setSpatial(IndexMode.DEACTIVATE_ACTIVATE);
		else
			index.setSpatial(IndexMode.DEACTIVATE);

		if (impNIRadioNoDeac.isSelected())
			index.setNormal(IndexMode.UNCHANGED);
		else if (impNIRadioDeacAc.isSelected())
			index.setNormal(IndexMode.DEACTIVATE_ACTIVATE);
		else
			index.setNormal(IndexMode.DEACTIVATE);
	}

	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.import.index");
	}

}
