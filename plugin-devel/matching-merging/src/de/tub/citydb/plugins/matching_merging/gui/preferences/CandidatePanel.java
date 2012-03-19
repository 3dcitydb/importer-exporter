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
package de.tub.citydb.plugins.matching_merging.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.tub.citydb.plugins.matching_merging.PluginImpl;
import de.tub.citydb.plugins.matching_merging.config.MatchingDeleteMode;
import de.tub.citydb.plugins.matching_merging.config.Merging;
import de.tub.citydb.plugins.matching_merging.util.Util;

@SuppressWarnings("serial")
public class CandidatePanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JRadioButton deleteMerRadio;
	private JRadioButton deleteAllRadio;
	private JRadioButton deleteRenRadio;

	public CandidatePanel(PluginImpl plugin) {
		super(plugin);
		initGui();
	}

	@Override
	public boolean isModified() {
		Merging merging = plugin.getConfig().getMerging();
		
		if (deleteMerRadio.isSelected() && !merging.isDeleteModeMerge()) return true;
		if (deleteAllRadio.isSelected() && !merging.isDeleteModeDelAll()) return true;
		if (deleteRenRadio.isSelected() && !merging.isDeleteModeRename()) return true;

		return false;
	}

	private void initGui() {
		deleteMerRadio = new JRadioButton();
		deleteAllRadio = new JRadioButton();
		deleteRenRadio = new JRadioButton();
		ButtonGroup deleteRadio = new ButtonGroup();
		deleteRadio.add(deleteMerRadio);
		deleteRadio.add(deleteAllRadio);
		deleteRadio.add(deleteRenRadio);

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, Util.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			deleteMerRadio.setIconTextGap(10);
			deleteAllRadio.setIconTextGap(10);
			deleteRenRadio.setIconTextGap(10);
			{
				block1.add(deleteMerRadio, Util.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(deleteAllRadio, Util.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(deleteRenRadio, Util.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
	}

	@Override
	public void switchLocale() {
		block1.setBorder(BorderFactory.createTitledBorder(Util.I18N.getString("pref.matching.candidate.postMerge.border")));
		deleteMerRadio.setText(Util.I18N.getString("pref.matching.candidate.postMerge.geom"));
		deleteAllRadio.setText(Util.I18N.getString("pref.matching.candidate.postMerge.delete"));
		deleteRenRadio.setText(Util.I18N.getString("pref.matching.candidate.postMerge.rename"));

	}

	@Override
	public void loadSettings() {
		Merging merging = plugin.getConfig().getMerging();
		
		if (merging.isDeleteModeDelAll())
			deleteAllRadio.setSelected(true);
		else if (merging.isDeleteModeRename())
			deleteRenRadio.setSelected(true);
		else
			deleteMerRadio.setSelected(true);
	}

	@Override
	public void setSettings() {
		Merging merging = plugin.getConfig().getMerging();
		
		if (deleteAllRadio.isSelected())
			merging.setDeleteMode(MatchingDeleteMode.DELALL);
		else if (deleteRenRadio.isSelected())
			merging.setDeleteMode(MatchingDeleteMode.RENAME);
		else
			merging.setDeleteMode(MatchingDeleteMode.MERGE);
	}
	
	@Override
	public String getTitle() {
		return Util.I18N.getString("pref.tree.matching.candidate");
	}

}
