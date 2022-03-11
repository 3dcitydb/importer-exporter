/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.operation.preferences.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.Cache;
import org.citydb.config.project.global.CacheMode;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.util.DefaultPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;

public class CachePanel extends DefaultPreferencesComponent {
	private TitledPanel cachePanel;
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
		Cache cache = config.getGlobalConfig().getCache();

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

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(useDatabase, GuiUtil.setConstraints(0, 0, 3, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(useLocalCache, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(localCachePath, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 5));
				content.add(browseButton, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			cachePanel = new TitledPanel().build(content);
		}

		add(cachePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		browseButton.addActionListener(e -> {
			String dir = browseFile(Language.I18N.getString("pref.general.cache.label.useLocal"), localCachePath.getText());
			if (!dir.isEmpty())
				localCachePath.setText(dir);
		});

		ActionListener cacheListener = e -> setEnabledLocalCachePath();
		useDatabase.addActionListener(cacheListener);
		useLocalCache.addActionListener(cacheListener);
	}
	
	private void setEnabledLocalCachePath() {
		localCachePath.setEnabled(useLocalCache.isSelected());
		browseButton.setEnabled(useLocalCache.isSelected());
	}
	
	@Override
	public void switchLocale(Locale locale) {
		cachePanel.setTitle(Language.I18N.getString("pref.general.cache.border"));
		useDatabase.setText(Language.I18N.getString("pref.general.cache.label.useDatabase"));
		useLocalCache.setText(Language.I18N.getString("pref.general.cache.label.useLocal"));
		browseButton.setText(Language.I18N.getString("common.button.browse"));		
	}

	@Override
	public void loadSettings() {
		Cache cache = config.getGlobalConfig().getCache();
		if (cache.isUseDatabase())
			useDatabase.setSelected(true);
		else
			useLocalCache.setSelected(true);

		if (cache.isSetLocalCachePath()) {
			localCachePath.setText(cache.getLocalCachePath());
		} else {
			String defaultDir = Cache.DEFAULT_LOCAL_CACHE_DIR.toString();
			localCachePath.setText(defaultDir);
			cache.setLocalCachePath(defaultDir);
		}

		setEnabledLocalCachePath();
	}

	@Override
	public void setSettings() {		
		Cache cache = config.getGlobalConfig().getCache();
		cache.setCacheMode(useDatabase.isSelected() ? CacheMode.DATABASE : CacheMode.LOCAL);

		if (!localCachePath.getText().isEmpty()) {
			cache.setLocalCachePath(localCachePath.getText());
		} else {
			String defaultDir = Cache.DEFAULT_LOCAL_CACHE_DIR.toString();
			localCachePath.setText(defaultDir);
			cache.setLocalCachePath(defaultDir);
		}
	}
	
	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("pref.tree.general.cache");
	}

	private String browseFile(String title, String oldDir) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(oldDir));

		int result = chooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return "";
		return chooser.getSelectedFile().toString();
	}
}
