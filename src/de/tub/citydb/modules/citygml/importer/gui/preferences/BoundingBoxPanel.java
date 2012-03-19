/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
import de.tub.citydb.config.project.filter.FilterBoundingBox;
import de.tub.citydb.config.project.filter.FilterBoundingBoxMode;
import de.tub.citydb.config.project.importer.ImportFilterConfig;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class BoundingBoxPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JRadioButton impBBRadioInside;
	private JRadioButton impBBRadioIntersect;

	public BoundingBoxPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportFilterConfig filter = config.getProject().getImporter().getFilter();
		
		if (impBBRadioIntersect.isSelected() && !filter.getComplexFilter().getBoundingBox().isSetOverlapMode()) return true;
		if (impBBRadioInside.isSelected() && !filter.getComplexFilter().getBoundingBox().isSetContainMode()) return true;
		return false;
	}

	private void initGui() {
		impBBRadioInside = new JRadioButton();
		impBBRadioIntersect = new JRadioButton();
		ButtonGroup impBBRadio = new ButtonGroup();
		impBBRadio.add(impBBRadioInside);
		impBBRadio.add(impBBRadioIntersect);

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			impBBRadioInside.setIconTextGap(10);
			impBBRadioIntersect.setIconTextGap(10);
			{
				block1.add(impBBRadioIntersect, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impBBRadioInside, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}

	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("common.pref.boundingBox.border.selection"));	
		impBBRadioInside.setText(Internal.I18N.getString("common.pref.boundingBox.label.inside"));
		impBBRadioIntersect.setText(Internal.I18N.getString("common.pref.boundingBox.label.overlap"));
	}

	@Override
	public void loadSettings() {
		FilterBoundingBox bbox = config.getProject().getImporter().getFilter().getComplexFilter().getBoundingBox();

		if (bbox.isSetOverlapMode())
			impBBRadioIntersect.setSelected(true);
		else
			impBBRadioInside.setSelected(true);
	}

	@Override
	public void setSettings() {
		FilterBoundingBox bbox = config.getProject().getImporter().getFilter().getComplexFilter().getBoundingBox();
		bbox.setMode(impBBRadioInside.isSelected() ? FilterBoundingBoxMode.CONTAIN : FilterBoundingBoxMode.OVERLAP);
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.import.boundingBox");
	}

}
