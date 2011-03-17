/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.matching.MatchingGmlNameMode;
import de.tub.citydb.config.project.matching.MergeConfig;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class MatchMasterPanel extends PrefPanelBase {
	private JPanel block1;
	private JRadioButton candNameAppend;
	private JRadioButton candNameIgnore;
	private JRadioButton candNameReplace;

	public MatchMasterPanel(Config inpConfig) {
		super(inpConfig);
		initGui();
	}

	@Override
	public boolean isModified() {
		MergeConfig mergeConfig = config.getProject().getMatching().getMergeConfig();
		
		if (candNameAppend.isSelected() && !mergeConfig.isGmlNameModeAppend()) return true;
		if (candNameIgnore.isSelected() && !mergeConfig.isGmlNameModeIgnore()) return true;
		if (candNameReplace.isSelected() && !mergeConfig.isGmlNameModeReplace()) return true;

		return false;
	}

	private void initGui() {
		candNameAppend = new JRadioButton();
		candNameIgnore = new JRadioButton();
		candNameReplace = new JRadioButton();
		ButtonGroup gmlNameRadio = new ButtonGroup();
		gmlNameRadio.add(candNameAppend);
		gmlNameRadio.add(candNameIgnore);
		gmlNameRadio.add(candNameReplace);

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			candNameAppend.setIconTextGap(10);
			candNameIgnore.setIconTextGap(10);
			candNameReplace.setIconTextGap(10);
			{
				block1.add(candNameAppend, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(candNameIgnore, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(candNameReplace, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}			
		}
	}

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.matching.master.name.border")));
		candNameAppend.setText(Internal.I18N.getString("pref.matching.master.name.append"));
		candNameIgnore.setText(Internal.I18N.getString("pref.matching.master.name.ignore"));
		candNameReplace.setText(Internal.I18N.getString("pref.matching.master.name.replace"));
	}

	@Override
	public void loadSettings() {
		MergeConfig mergeConfig = config.getProject().getMatching().getMergeConfig();

		if (mergeConfig.isGmlNameModeIgnore())
			candNameIgnore.setSelected(true);
		else if (mergeConfig.isGmlNameModeReplace())
			candNameReplace.setSelected(true);
		else
			candNameAppend.setSelected(true);
	}

	@Override
	public void setSettings() {
		MergeConfig mergeConfig = config.getProject().getMatching().getMergeConfig();
		
		if (candNameIgnore.isSelected())
			mergeConfig.setGmlNameMode(MatchingGmlNameMode.IGNORE);
		else if (candNameReplace.isSelected())
			mergeConfig.setGmlNameMode(MatchingGmlNameMode.REPLACE);
		else
			mergeConfig.setGmlNameMode(MatchingGmlNameMode.APPEND);
	}

}
