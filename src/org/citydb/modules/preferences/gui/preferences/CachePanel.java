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
package org.citydb.modules.preferences.gui.preferences;

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

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.global.Cache;
import org.citydb.config.project.global.CacheMode;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class CachePanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JRadioButton useDatabase;
	private JRadioButton useLocalCache;
	private JTextField localCachePath;
	private JButton browseButton;
	
	public CachePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		Cache cache = config.getProject().getGlobal().getCache();

		if (useDatabase.isSelected() != cache.isUseDatabase()) return true;
		if (useLocalCache.isSelected() != cache.isUseLocal()) return true;
		if (!localCachePath.getText().equals(cache.getLocalCachePath())) return true;
		return false;
	}

	private void initGui() {
		useDatabase = new JRadioButton();
		useLocalCache = new JRadioButton();
		ButtonGroup cacheRadioGroup = new ButtonGroup();
		cacheRadioGroup.add(useDatabase);
		cacheRadioGroup.add(useLocalCache);
		
		localCachePath = new JTextField();
		browseButton = new JButton();
		
		PopupMenuDecorator.getInstance().decorate(localCachePath);
		
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String dir = browseFile(Language.I18N.getString("pref.general.cache.label.useLocal"), localCachePath.getText());
				if (!dir.isEmpty())
					localCachePath.setText(dir);
			}
		});
		
		setLayout(new GridBagLayout());
		{

			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));

			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			useDatabase.setIconTextGap(10);
			useLocalCache.setIconTextGap(10);
			localCachePath.setPreferredSize(localCachePath.getSize());
			int lmargin = (int)(useDatabase.getPreferredSize().getWidth()) + 11;
			{
				block1.add(useDatabase, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(useLocalCache, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(localCachePath, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				block1.add(browseButton, GuiUtil.setConstraints(1,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
			}
		}
		
		ActionListener cacheListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledLocalCachePath();
			}
		};
		
		useDatabase.addActionListener(cacheListener);
		useLocalCache.addActionListener(cacheListener);
	}
	
	private void setEnabledLocalCachePath() {
		localCachePath.setEnabled(useLocalCache.isSelected());
		browseButton.setEnabled(useLocalCache.isSelected());
	}
	
	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.general.cache.border"));
		useDatabase.setText(Language.I18N.getString("pref.general.cache.label.useDatabase"));
		useLocalCache.setText(Language.I18N.getString("pref.general.cache.label.useLocal"));
		browseButton.setText(Language.I18N.getString("common.button.browse"));		
	}

	@Override
	public void loadSettings() {
		Cache cache = config.getProject().getGlobal().getCache();
		if (cache.isUseDatabase())
			useDatabase.setSelected(true);
		else
			useLocalCache.setSelected(true);
		
		localCachePath.setText(cache.getLocalCachePath());
		setEnabledLocalCachePath();
	}

	@Override
	public void setSettings() {		
		Cache cache = config.getProject().getGlobal().getCache();
		
		cache.setCacheMode(useDatabase.isSelected() ? CacheMode.DATABASE : CacheMode.LOCAL);
		cache.setLocalCachePath(localCachePath.getText());
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.general.cache");
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
