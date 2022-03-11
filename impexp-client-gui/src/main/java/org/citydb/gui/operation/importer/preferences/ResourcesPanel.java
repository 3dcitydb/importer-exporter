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
package org.citydb.gui.operation.importer.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.ImportBatching;
import org.citydb.config.project.resources.IdCacheConfig;
import org.citydb.config.project.resources.ThreadPool;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.util.DefaultPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

public class ResourcesPanel extends DefaultPreferencesComponent {
	private TitledPanel multithreadingPanel;
	private TitledPanel batchPanel;
	private TitledPanel idCachePanel;
	private TitledPanel textureCachePanel;

	private JLabel impResMinThreadsLabel;
	private JFormattedTextField impResMinThreadsText;
	private JLabel impResMaxThreadsLabel;
	private JFormattedTextField impResMaxThreadsText;
	private JLabel impResTransaktLabel;
	private JFormattedTextField impResTransaktFeatureText;
	private JLabel impResTransaktFeatureLabel;	
	private JFormattedTextField impResTransaktCacheText;
	private JLabel impResTransaktCacheLabel;
	private JFormattedTextField impResTransaktTempText;
	private JLabel impResTransaktTempLabel;
	private JLabel impResGeomLabel;
	private JFormattedTextField impResGeomCacheText;
	private JLabel impResGeomCacheLabel;	
	private JFormattedTextField impResGeomDrainText;
	private JLabel impResGeomDrainLabel;
	private JLabel impResGeomPartLabel;
	private JFormattedTextField impResGeomPartText;
	private JLabel impResFeatLabel;
	private JFormattedTextField impResFeatCacheText;
	private JLabel impResFeatCacheLabel;
	private JFormattedTextField impResFeatDrainText;
	private JLabel impResFeatDrainLabel;
	private JLabel impResFeatPartLabel;
	private JFormattedTextField impResFeatPartText;
	private JLabel impResTexLabel;
	private JFormattedTextField impResTexCacheText;
	private JLabel impResTexCacheLabel;	
	private JFormattedTextField impResTexDrainText;
	private JLabel impResTexDrainLabel;
	private JLabel impResTexPartLabel;
	private JFormattedTextField impResTexPartText;

	
	public ResourcesPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ThreadPool threadPool = config.getImportConfig().getResources().getThreadPool();
		ImportBatching commit = config.getDatabaseConfig().getImportBatching();
		IdCacheConfig geometry = config.getImportConfig().getResources().getIdCache().getGeometry();
		IdCacheConfig feature = config.getImportConfig().getResources().getIdCache().getFeature();
		IdCacheConfig texImage = config.getImportConfig().getResources().getTexImageCache();
		
		try { impResMinThreadsText.commitEdit(); } catch (ParseException e) { }
		try { impResMaxThreadsText.commitEdit(); } catch (ParseException e) { }
		try { impResTransaktFeatureText.commitEdit(); } catch (ParseException e) { }
		try { impResTransaktCacheText.commitEdit(); } catch (ParseException e) { }
		try { impResTransaktTempText.commitEdit(); } catch (ParseException e) { }
		try { impResGeomCacheText.commitEdit(); } catch (ParseException e) { }
		try { impResGeomDrainText.commitEdit(); } catch (ParseException e) { }
		try { impResGeomPartText.commitEdit(); } catch (ParseException e) { }
		try { impResFeatCacheText.commitEdit(); } catch (ParseException e) { }
		try { impResFeatDrainText.commitEdit(); } catch (ParseException e) { }
		try { impResFeatPartText.commitEdit(); } catch (ParseException e) { }
		try { impResTexCacheText.commitEdit(); } catch (ParseException e) { }
		try { impResTexDrainText.commitEdit(); } catch (ParseException e) { }
		try { impResTexPartText.commitEdit(); } catch (ParseException e) { }
		
		if (((Number)impResMinThreadsText.getValue()).intValue() != threadPool.getMinThreads()) return true;
		if (((Number)impResMaxThreadsText.getValue()).intValue() != threadPool.getMaxThreads()) return true;
		if (((Number)impResTransaktFeatureText.getValue()).intValue() != commit.getFeatureBatchSize()) return true;
		if (((Number)impResTransaktCacheText.getValue()).intValue() != commit.getGmlIdCacheBatchSize()) return true;
		if (((Number)impResTransaktTempText.getValue()).intValue() != commit.getTempBatchSize()) return true;
		if (((Number)impResGeomCacheText.getValue()).intValue() != geometry.getCacheSize()) return true;
		if (((Number)impResGeomDrainText.getValue()).intValue() != (int)(geometry.getPageFactor() * 100)) return true;
		if (((Number)impResGeomPartText.getValue()).intValue() != geometry.getPartitions()) return true;
		if (((Number)impResFeatCacheText.getValue()).intValue() != feature.getCacheSize()) return true;
		if (((Number)impResFeatDrainText.getValue()).intValue() != (int)(feature.getPageFactor() * 100)) return true;
		if (((Number)impResFeatPartText.getValue()).intValue() != feature.getPartitions()) return true;
		if (((Number)impResTexCacheText.getValue()).intValue() != texImage.getCacheSize()) return true;
		if (((Number)impResTexDrainText.getValue()).intValue() != (int)(texImage.getPageFactor() * 100)) return true;
		if (((Number)impResTexPartText.getValue()).intValue() != texImage.getPartitions()) return true;

		return false;
	}

	private void initGui() {
		impResMinThreadsLabel = new JLabel();
		impResMaxThreadsLabel = new JLabel();
		impResTransaktLabel = new JLabel();
		impResTransaktFeatureLabel = new JLabel();	
		impResTransaktCacheLabel = new JLabel();
		impResTransaktTempLabel = new JLabel();
		impResGeomLabel = new JLabel();
		impResGeomCacheLabel = new JLabel();	
		impResGeomDrainLabel = new JLabel();	
		impResGeomPartLabel = new JLabel();
		impResFeatLabel = new JLabel();		
		impResFeatCacheLabel = new JLabel();
		impResFeatDrainLabel = new JLabel();
		impResFeatPartLabel = new JLabel();
		impResTexLabel = new JLabel();
		impResTexCacheLabel = new JLabel();
		impResTexDrainLabel = new JLabel();
		impResTexPartLabel = new JLabel();
		
		NumberFormatter threeIntFormat = new NumberFormatter(new DecimalFormat("#"));
		threeIntFormat.setMaximum(999);
		threeIntFormat.setMinimum(0);
		impResMinThreadsText = new JFormattedTextField(threeIntFormat);
		impResMaxThreadsText = new JFormattedTextField(threeIntFormat);
		impResGeomDrainText = new JFormattedTextField(threeIntFormat);
		impResFeatDrainText = new JFormattedTextField(threeIntFormat);
		impResGeomPartText = new JFormattedTextField(threeIntFormat);
		impResFeatPartText = new JFormattedTextField(threeIntFormat);
		impResTexDrainText = new JFormattedTextField(threeIntFormat);
		impResTexPartText = new JFormattedTextField(threeIntFormat);
		impResMinThreadsText.setColumns(8);
		impResMaxThreadsText.setColumns(8);
		impResGeomDrainText.setColumns(8);
		impResFeatDrainText.setColumns(8);
		impResGeomPartText.setColumns(8);
		impResFeatPartText.setColumns(8);
		impResTexDrainText.setColumns(8);
		impResTexPartText.setColumns(8);

		NumberFormatter batchFormat = new NumberFormatter(new DecimalFormat("#"));
		batchFormat.setMaximum(99999);
		batchFormat.setMinimum(0);
		impResTransaktFeatureText = new JFormattedTextField(batchFormat);
		impResTransaktCacheText = new JFormattedTextField(batchFormat);
		impResTransaktTempText = new JFormattedTextField(batchFormat);
		impResTransaktFeatureText.setColumns(8);
		impResTransaktCacheText.setColumns(8);
		impResTransaktTempText.setColumns(8);

		NumberFormatter cacheEntryFormat = new NumberFormatter(new DecimalFormat("#"));
		cacheEntryFormat.setMaximum(99999999);
		cacheEntryFormat.setMinimum(0);
		impResGeomCacheText = new JFormattedTextField(cacheEntryFormat);
		impResFeatCacheText = new JFormattedTextField(cacheEntryFormat);	
		impResTexCacheText = new JFormattedTextField(cacheEntryFormat);
		impResGeomCacheText.setColumns(8);
		impResFeatCacheText.setColumns(8);
		impResTexCacheText.setColumns(8);

		PopupMenuDecorator.getInstance().decorate(impResMinThreadsText, impResMaxThreadsText,
				impResGeomDrainText, impResFeatDrainText, impResTexDrainText, impResGeomPartText, impResFeatPartText, impResTexPartText,
				impResTransaktFeatureText, impResTransaktCacheText, impResTransaktTempText,
				impResGeomCacheText, impResFeatCacheText, impResTexCacheText);

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(impResMinThreadsLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(impResMinThreadsText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 5, 5, 0));
				content.add(impResMaxThreadsLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(impResMaxThreadsText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 5, 0, 0));
			}

			multithreadingPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(impResTransaktLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(impResTransaktFeatureText, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
				content.add(impResTransaktFeatureLabel, GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(impResTransaktCacheText, GuiUtil.setConstraints(1, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
				content.add(impResTransaktCacheLabel, GuiUtil.setConstraints(2, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(impResTransaktTempText, GuiUtil.setConstraints(1, 2, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 5));
				content.add(impResTransaktTempLabel, GuiUtil.setConstraints(2, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
			}

			batchPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(impResGeomLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(impResGeomCacheText, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
				content.add(impResGeomCacheLabel, GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(impResGeomDrainText, GuiUtil.setConstraints(1, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
				content.add(impResGeomDrainLabel, GuiUtil.setConstraints(2, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(impResGeomPartText, GuiUtil.setConstraints(1, 2, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
				content.add(impResGeomPartLabel, GuiUtil.setConstraints(2, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));

				content.add(impResFeatLabel, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.BOTH, 10, 0, 5, 5));
				content.add(impResFeatCacheText, GuiUtil.setConstraints(1, 3, 0, 0, GridBagConstraints.BOTH, 10, 5, 5, 5));
				content.add(impResFeatCacheLabel, GuiUtil.setConstraints(2, 3, 1, 0, GridBagConstraints.BOTH, 10, 0, 5, 0));
				content.add(impResFeatDrainText, GuiUtil.setConstraints(1, 4, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
				content.add(impResFeatDrainLabel, GuiUtil.setConstraints(2, 4, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(impResFeatPartText, GuiUtil.setConstraints(1, 5, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 5));
				content.add(impResFeatPartLabel, GuiUtil.setConstraints(2, 5, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
			}

			idCachePanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(impResTexLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(impResTexCacheText, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
				content.add(impResTexCacheLabel, GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(impResTexDrainText, GuiUtil.setConstraints(1, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
				content.add(impResTexDrainLabel, GuiUtil.setConstraints(2, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(impResTexPartText, GuiUtil.setConstraints(1, 2, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 5));
				content.add(impResTexPartLabel, GuiUtil.setConstraints(2, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
			}

			textureCachePanel = new TitledPanel().build(content);
		}

		add(multithreadingPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(batchPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(idCachePanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(textureCachePanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		impResMinThreadsText.addPropertyChangeListener("value", evt -> checkPositive(impResMinThreadsText, 1));
		impResMaxThreadsText.addPropertyChangeListener("value", evt -> checkPositive(impResMaxThreadsText, 1));
		impResTransaktFeatureText.addPropertyChangeListener("value", evt -> checkPositive(impResTransaktFeatureText, 20));
		impResTransaktCacheText.addPropertyChangeListener("value", evt -> checkPositive(impResTransaktCacheText, 1000));
		impResTransaktTempText.addPropertyChangeListener("value", evt -> checkPositive(impResTransaktTempText, 1000));
		impResGeomCacheText.addPropertyChangeListener("value", evt -> checkPositive(impResGeomCacheText, 200000));
		impResFeatCacheText.addPropertyChangeListener("value", evt -> checkPositive(impResFeatCacheText, 200000));
		impResTexCacheText.addPropertyChangeListener("value", evt -> checkPositive(impResTexCacheText, 200000));
		impResGeomDrainText.addPropertyChangeListener("value", evt -> checkPositiveRange(impResGeomDrainText, 85, 100));
		impResFeatDrainText.addPropertyChangeListener("value", evt -> checkPositiveRange(impResFeatDrainText, 85, 100));
		impResTexDrainText.addPropertyChangeListener("value", evt -> checkPositiveRange(impResTexDrainText, 85, 100));
		impResGeomPartText.addPropertyChangeListener("value", evt -> checkPositiveRange(impResGeomPartText, 10, 100));
		impResFeatPartText.addPropertyChangeListener("value", evt -> checkPositiveRange(impResFeatPartText, 10, 100));
		impResTexPartText.addPropertyChangeListener("value", evt -> checkPositiveRange(impResTexPartText, 10, 100));
	}

	private void checkPositive(JFormattedTextField field, int defaultValue) {
		if (field.getValue() == null || ((Number) field.getValue()).intValue() <= 0)
			field.setValue(defaultValue);
	}
	
	private void checkPositiveRange(JFormattedTextField field, int min, int max) {
		if (field.getValue() != null) {
			if (((Number) field.getValue()).intValue() <= 0)
				field.setValue(min);
			else if (((Number) field.getValue()).intValue() > 100)
				field.setValue(max);
		} else {
			field.setValue(min);
		}
	}

	@Override
	public void switchLocale(Locale locale) {
		multithreadingPanel.setTitle(Language.I18N.getString("common.pref.resources.border.multiCPU"));
		batchPanel.setTitle(Language.I18N.getString("pref.import.resources.border.commit"));
		idCachePanel.setTitle(Language.I18N.getString("common.pref.resources.border.idCache"));
		textureCachePanel.setTitle(Language.I18N.getString("pref.import.resources.border.texImageCache"));

		impResMinThreadsLabel.setText(Language.I18N.getString("common.pref.resources.label.minThreads"));
		impResMaxThreadsLabel.setText(Language.I18N.getString("common.pref.resources.label.maxThreads"));
		
		impResTransaktLabel.setText(Language.I18N.getString("pref.import.resources.label.commit"));
		impResTransaktFeatureLabel.setText(Language.I18N.getString("pref.import.resources.label.commit.feature"));
		impResTransaktCacheLabel.setText(Language.I18N.getString("pref.import.resources.label.commit.cache"));
		impResTransaktTempLabel.setText(Language.I18N.getString("pref.import.resources.label.commit.temp"));

		impResGeomLabel.setText(Language.I18N.getString("common.pref.resources.label.geometry"));
		impResGeomCacheLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.entry"));
		impResGeomDrainLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.drain"));
		impResGeomPartLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.partition"));
		impResFeatLabel.setText(Language.I18N.getString("common.pref.resources.label.feature"));
		impResFeatCacheLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.entry"));
		impResFeatDrainLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.drain"));
		impResFeatPartLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.partition"));
		impResTexLabel.setText(Language.I18N.getString("pref.import.resources.label.texImageCache"));
		impResTexCacheLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.entry"));
		impResTexDrainLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.drain"));
		impResTexPartLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.partition"));
	}

	@Override
	public void loadSettings() {
		ThreadPool threadPool = config.getImportConfig().getResources().getThreadPool();
		ImportBatching commit = config.getDatabaseConfig().getImportBatching();
		IdCacheConfig geometry = config.getImportConfig().getResources().getIdCache().getGeometry();
		IdCacheConfig feature = config.getImportConfig().getResources().getIdCache().getFeature();
		IdCacheConfig texImage = config.getImportConfig().getResources().getTexImageCache();

		int commitFeature = commit.getFeatureBatchSize();
		if (commitFeature > ImportBatching.MAX_BATCH_SIZE)
			commitFeature = ImportBatching.MAX_BATCH_SIZE;
		
		int commitCache = commit.getGmlIdCacheBatchSize();
		if (commitCache > ImportBatching.MAX_BATCH_SIZE)
			commitCache = ImportBatching.MAX_BATCH_SIZE;
		
		int commitTemp = commit.getTempBatchSize();
		if (commitTemp > ImportBatching.MAX_BATCH_SIZE)
			commitTemp = ImportBatching.MAX_BATCH_SIZE;
		
		impResMinThreadsText.setValue(threadPool.getMinThreads());
		impResMaxThreadsText.setValue(threadPool.getMaxThreads());
		impResTransaktFeatureText.setValue(commitFeature);		
		impResTransaktCacheText.setValue(commitCache);
		impResTransaktTempText.setValue(commitTemp);		
		impResGeomCacheText.setValue(geometry.getCacheSize());
		impResFeatCacheText.setValue(feature.getCacheSize());		
		impResTexCacheText.setValue(texImage.getCacheSize());		
		impResGeomDrainText.setValue((int)(geometry.getPageFactor() * 100));
		impResFeatDrainText.setValue((int)(feature.getPageFactor() * 100));		
		impResTexDrainText.setValue((int)(texImage.getPageFactor() * 100));		
		impResGeomPartText.setValue(geometry.getPartitions());
		impResFeatPartText.setValue(feature.getPartitions());
		impResTexPartText.setValue(texImage.getPartitions());
	}

	@Override
	public void setSettings() {
		ThreadPool threadPool = config.getImportConfig().getResources().getThreadPool();
		ImportBatching commit = config.getDatabaseConfig().getImportBatching();
		IdCacheConfig geometry = config.getImportConfig().getResources().getIdCache().getGeometry();
		IdCacheConfig feature = config.getImportConfig().getResources().getIdCache().getFeature();
		IdCacheConfig texImage = config.getImportConfig().getResources().getTexImageCache();

		int minThreads = ((Number)impResMinThreadsText.getValue()).intValue();
		int maxThreads = ((Number)impResMaxThreadsText.getValue()).intValue();
		int featBatch = ((Number)impResTransaktFeatureText.getValue()).intValue();
		int lookupBatch = ((Number)impResTransaktCacheText.getValue()).intValue();
		int tempBatch = ((Number)impResTransaktTempText.getValue()).intValue();

		if (minThreads > maxThreads) {
			minThreads = maxThreads;
			impResMinThreadsText.setValue(minThreads);
		}

		if (featBatch > ImportBatching.MAX_BATCH_SIZE) {
			featBatch = ImportBatching.MAX_BATCH_SIZE;
			impResTransaktFeatureText.setValue(featBatch);
		}
		
		if (lookupBatch > ImportBatching.MAX_BATCH_SIZE) {
			lookupBatch = ImportBatching.MAX_BATCH_SIZE;
			impResTransaktCacheText.setValue(lookupBatch);
		}
		
		if (tempBatch > ImportBatching.MAX_BATCH_SIZE) {
			tempBatch = ImportBatching.MAX_BATCH_SIZE;
			impResTransaktTempText.setValue(tempBatch);
		}
		
		threadPool.setMinThreads(minThreads);
		threadPool.setMaxThreads(maxThreads);
		
		commit.setFeatureBatchSize(featBatch);
		commit.setGmlIdCacheBatchSize(lookupBatch);
		commit.setTempBatchSize(tempBatch);

		geometry.setCacheSize(((Number)impResGeomCacheText.getValue()).intValue());			
		feature.setCacheSize(((Number)impResFeatCacheText.getValue()).intValue());
		texImage.setCacheSize(((Number)impResTexCacheText.getValue()).intValue());
		geometry.setPageFactor(((Number)impResGeomDrainText.getValue()).floatValue() / 100);
		feature.setPageFactor(((Number)impResFeatDrainText.getValue()).floatValue() / 100);
		texImage.setPageFactor(((Number)impResTexDrainText.getValue()).floatValue() / 100);
		geometry.setPartitions(((Number)impResGeomPartText.getValue()).intValue());
		feature.setPartitions(((Number)impResFeatPartText.getValue()).intValue());
		texImage.setPartitions(((Number)impResTexPartText.getValue()).intValue());
	}
	
	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("pref.tree.import.resources");
	}
}
