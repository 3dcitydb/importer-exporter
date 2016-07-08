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
package org.citydb.modules.citygml.importer.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.filter.FilterBoundingBox;
import org.citydb.config.project.filter.FilterBoundingBoxMode;
import org.citydb.config.project.importer.ImportFilterConfig;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

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
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("common.pref.boundingBox.border.selection"));	
		impBBRadioInside.setText(Language.I18N.getString("common.pref.boundingBox.label.inside"));
		impBBRadioIntersect.setText(Language.I18N.getString("common.pref.boundingBox.label.overlap"));
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
		return Language.I18N.getString("pref.tree.import.boundingBox");
	}

}
