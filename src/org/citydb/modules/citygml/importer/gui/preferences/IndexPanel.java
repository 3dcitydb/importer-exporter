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
import org.citydb.config.project.importer.Index;
import org.citydb.config.project.importer.IndexMode;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

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
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.import.index.spatial.border.handling"));	
		((TitledBorder)block2.getBorder()).setTitle(Language.I18N.getString("pref.import.index.normal.border.handling"));	

		impSIRadioDeacAc.setText(Language.I18N.getString("pref.import.index.spatial.label.autoActivate"));
		impSIRadioDeac.setText(Language.I18N.getString("pref.import.index.spatial.label.manuActivate"));
		impSIRadioNoDeac.setText(Language.I18N.getString("pref.import.index.spatial.label.keepState"));

		impNIRadioDeacAc.setText(Language.I18N.getString("pref.import.index.normal.label.autoActivate"));
		impNIRadioDeac.setText(Language.I18N.getString("pref.import.index.normal.label.manuActivate"));
		impNIRadioNoDeac.setText(Language.I18N.getString("pref.import.index.normal.label.keepState"));
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
		return Language.I18N.getString("pref.tree.import.index");
	}

}
