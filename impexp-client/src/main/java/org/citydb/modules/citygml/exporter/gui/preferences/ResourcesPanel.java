/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.modules.citygml.exporter.gui.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.ExportBatching;
import org.citydb.config.project.resources.ThreadPoolConfig;
import org.citydb.config.project.resources.UIDCacheConfig;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;

@SuppressWarnings("serial")
public class ResourcesPanel extends AbstractPreferencesComponent{
	private JPanel block1;
	private JPanel block2;
	private JPanel block3;
	private JLabel expResMinThreadsLabel;
	private JFormattedTextField expResMinThreadsText;
	private JLabel expResMaxThreadsLabel;
	private JFormattedTextField expResMaxThreadsText;
	private JLabel expResBatchLabel;
	private JFormattedTextField expResFeatBatchText;
	private JLabel expResFeatBatchLabel;
	private JFormattedTextField expResGeomBatchText;
	private JLabel expResGeomBatchLabel;
	private JFormattedTextField expResBlobBatchText;
	private JLabel expResBlobBatchLabel;
	private JLabel expResGeomLabel;
	private JFormattedTextField expResGeomCacheText;
	private JLabel expResGeomCacheLabel;	
	private JFormattedTextField expResGeomDrainText;
	private JLabel expResGeomDrainLabel;
	private JLabel expResGeomPartLabel;
	private JFormattedTextField expResGeomPartText;
	private JLabel expResFeatLabel;
	private JFormattedTextField expResFeatCacheText;
	private JLabel expResFeatCacheLabel;
	private JFormattedTextField expResFeatDrainText;
	private JLabel expResFeatDrainLabel;
	private JLabel expResFeatPartLabel;
	private JFormattedTextField expResFeatPartText;

	public ResourcesPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ThreadPoolConfig threadPool = config.getProject().getExportConfig().getResources().getThreadPool().getDefaultPool();
		ExportBatching exportBatching = config.getProject().getDatabaseConfig().getExportBatching();
		UIDCacheConfig geometry = config.getProject().getExportConfig().getResources().getGmlIdCache().getGeometry();
		UIDCacheConfig feature = config.getProject().getExportConfig().getResources().getGmlIdCache().getFeature();

		try { expResMinThreadsText.commitEdit(); } catch (ParseException e) { }
		try { expResMaxThreadsText.commitEdit(); } catch (ParseException e) { }
		try { expResFeatBatchText.commitEdit(); } catch (ParseException e) { }
		try { expResGeomBatchText.commitEdit(); } catch (ParseException e) { }
		try { expResBlobBatchText.commitEdit(); } catch (ParseException e) { }
		try { expResGeomCacheText.commitEdit(); } catch (ParseException e) { }
		try { expResGeomDrainText.commitEdit(); } catch (ParseException e) { }
		try { expResGeomPartText.commitEdit(); } catch (ParseException e) { }
		try { expResFeatCacheText.commitEdit(); } catch (ParseException e) { }
		try { expResFeatDrainText.commitEdit(); } catch (ParseException e) { }
		try { expResFeatPartText.commitEdit(); } catch (ParseException e) { }
		
		if (((Number)expResMinThreadsText.getValue()).intValue() != threadPool.getMinThreads()) return true;
		if (((Number)expResMaxThreadsText.getValue()).intValue() != threadPool.getMaxThreads()) return true;
		if (((Number)expResFeatBatchText.getValue()).intValue() != exportBatching.getFeatureBatchSize()) return true;
		if (((Number)expResGeomBatchText.getValue()).intValue() != exportBatching.getGeometryBatchSize()) return true;
		if (((Number)expResBlobBatchText.getValue()).intValue() != exportBatching.getBlobBatchSize()) return true;
		if (((Number)expResGeomCacheText.getValue()).intValue() != geometry.getCacheSize()) return true;
		if (((Number)expResGeomDrainText.getValue()).intValue() != (int)(geometry.getPageFactor() * 100)) return true;
		if (((Number)expResGeomPartText.getValue()).intValue() != geometry.getPartitions()) return true;
		if (((Number)expResFeatCacheText.getValue()).intValue() != feature.getCacheSize()) return true;
		if (((Number)expResFeatDrainText.getValue()).intValue() != (int)(feature.getPageFactor() * 100)) return true;
		if (((Number)expResFeatPartText.getValue()).intValue() != feature.getPartitions()) return true;

		return false;
	}

	private void initGui(){
		block1 = new JPanel();
		block2 = new JPanel();
		block3 = new JPanel();
		expResMinThreadsLabel = new JLabel();
		expResMaxThreadsLabel = new JLabel();
		expResBatchLabel = new JLabel();
		expResFeatBatchLabel = new JLabel();
		expResGeomBatchLabel = new JLabel();
		expResBlobBatchLabel = new JLabel();
		expResGeomLabel = new JLabel();
		expResGeomCacheLabel = new JLabel();	
		expResGeomDrainLabel = new JLabel();
		expResGeomPartLabel = new JLabel();
		expResFeatLabel = new JLabel();		
		expResFeatCacheLabel = new JLabel();
		expResFeatDrainLabel = new JLabel();
		expResFeatPartLabel = new JLabel();

		DecimalFormat threeIntFormat = new DecimalFormat("###");	
		threeIntFormat.setMaximumIntegerDigits(3);
		threeIntFormat.setMinimumIntegerDigits(1);
		expResMinThreadsText = new JFormattedTextField(threeIntFormat);
		expResMaxThreadsText = new JFormattedTextField(threeIntFormat);
		expResGeomDrainText = new JFormattedTextField(threeIntFormat);
		expResFeatDrainText = new JFormattedTextField(threeIntFormat);
		expResGeomPartText = new JFormattedTextField(threeIntFormat);
		expResFeatPartText = new JFormattedTextField(threeIntFormat);

		DecimalFormat batchFormat = new DecimalFormat("#####");
		batchFormat.setMaximumIntegerDigits(5);
		batchFormat.setMinimumIntegerDigits(1);
		expResFeatBatchText = new JFormattedTextField(batchFormat);
		expResGeomBatchText = new JFormattedTextField(batchFormat);
		expResBlobBatchText = new JFormattedTextField(batchFormat);
		
		DecimalFormat cacheEntryFormat = new DecimalFormat("########");
		cacheEntryFormat.setMaximumIntegerDigits(8);
		cacheEntryFormat.setMinimumIntegerDigits(1);		
		expResGeomCacheText = new JFormattedTextField(cacheEntryFormat);
		expResFeatCacheText = new JFormattedTextField(cacheEntryFormat);
		
		PopupMenuDecorator.getInstance().decorate(expResMinThreadsText, expResMaxThreadsText, expResGeomDrainText,
				expResFeatDrainText, expResGeomPartText, expResFeatPartText, expResFeatBatchText, expResGeomBatchText,
				expResBlobBatchText, expResGeomCacheText, expResFeatCacheText);
		
		expResMinThreadsText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositive(expResMinThreadsText, 1);
			}
		});
		
		expResMaxThreadsText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositive(expResMaxThreadsText, 1);
			}
		});

		expResFeatBatchText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositive(expResFeatBatchText, ExportBatching.DEFAULT_BATCH_SIZE);
			}
		});

		expResGeomBatchText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositive(expResGeomBatchText, ExportBatching.DEFAULT_BATCH_SIZE);
			}
		});

		expResBlobBatchText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositive(expResBlobBatchText, ExportBatching.DEFAULT_BATCH_SIZE);
			}
		});


		expResGeomCacheText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositive(expResGeomCacheText, 200000);
			}
		});
		
		expResFeatCacheText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositive(expResFeatCacheText, 200000);
			}
		});

		expResGeomDrainText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositiveRange(expResGeomDrainText, 85, 100);
			}
		});
		
		expResFeatDrainText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositiveRange(expResFeatDrainText, 85, 100);
			}
		});
		
		expResGeomPartText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositiveRange(expResGeomPartText, 10, 100);
			}
		});
		
		expResFeatPartText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkPositiveRange(expResFeatPartText, 10, 100);
			}
		});
		
		setLayout(new GridBagLayout());
		add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block1.setBorder(BorderFactory.createTitledBorder(""));
		block1.setLayout(new GridBagLayout());
		{
			block1.add(expResMinThreadsLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(expResMinThreadsText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(expResMaxThreadsLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(expResMaxThreadsText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
		add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block2.setBorder(BorderFactory.createTitledBorder(""));
		block2.setLayout(new GridBagLayout());
		{
			block2.add(expResBatchLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatBatchText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatBatchLabel, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomBatchText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomBatchLabel, GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResBlobBatchText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResBlobBatchLabel, GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
		add(block3, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block3.setBorder(BorderFactory.createTitledBorder(""));
		block3.setLayout(new GridBagLayout());
		{
			block3.add(expResGeomLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResGeomCacheText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResGeomCacheLabel, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResGeomDrainText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResGeomDrainLabel, GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResGeomPartText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResGeomPartLabel, GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResFeatLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResFeatCacheText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResFeatCacheLabel, GuiUtil.setConstraints(2,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResFeatDrainText, GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResFeatDrainLabel, GuiUtil.setConstraints(2,4,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResFeatPartText, GuiUtil.setConstraints(1,5,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(expResFeatPartLabel, GuiUtil.setConstraints(2,5,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
	}

	private void checkPositive(JFormattedTextField field, int defaultValue) {
		if (((Number)field.getValue()).intValue() <= 0)
			field.setValue(defaultValue);
	}
	
	private void checkPositiveRange(JFormattedTextField field, int min, int max) {
		if (((Number)field.getValue()).intValue() <= 0)
			field.setValue(min);
		else if (((Number)field.getValue()).intValue() > 100)
			field.setValue(max);
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("common.pref.resources.border.multiCPU"));
		((TitledBorder)block2.getBorder()).setTitle(Language.I18N.getString("pref.export.resources.border.batch"));
		((TitledBorder)block3.getBorder()).setTitle(Language.I18N.getString("common.pref.resources.border.idCache"));

		expResMinThreadsLabel.setText(Language.I18N.getString("common.pref.resources.label.minThreads"));
		expResMaxThreadsLabel.setText(Language.I18N.getString("common.pref.resources.label.maxThreads"));

		expResBatchLabel.setText(Language.I18N.getString("pref.export.resources.label.batch"));
		expResFeatBatchLabel.setText(Language.I18N.getString("pref.export.resources.label.batch.feature"));
		expResGeomBatchLabel.setText(Language.I18N.getString("pref.export.resources.label.batch.geometry"));
		expResBlobBatchLabel.setText(Language.I18N.getString("pref.export.resources.label.batch.blob"));

		expResGeomLabel.setText(Language.I18N.getString("common.pref.resources.label.geometry"));
		expResGeomCacheLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.entry"));
		expResGeomDrainLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.drain"));
		expResGeomPartLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.partition"));
		expResFeatLabel.setText(Language.I18N.getString("common.pref.resources.label.feature"));
		expResFeatCacheLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.entry"));
		expResFeatDrainLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.drain"));
		expResFeatPartLabel.setText(Language.I18N.getString("common.pref.resources.label.cache.partition"));
	}

	@Override
	public void loadSettings() {
		ThreadPoolConfig threadPool = config.getProject().getExportConfig().getResources().getThreadPool().getDefaultPool();
		ExportBatching exportBatching = config.getProject().getDatabaseConfig().getExportBatching();
		UIDCacheConfig geometry = config.getProject().getExportConfig().getResources().getGmlIdCache().getGeometry();
		UIDCacheConfig feature = config.getProject().getExportConfig().getResources().getGmlIdCache().getFeature();

		int featureBatchSize = exportBatching.getFeatureBatchSize();
		if (featureBatchSize >  ExportBatching.MAX_BATCH_SIZE)
			featureBatchSize = ExportBatching.MAX_BATCH_SIZE;

		int geometryBatchSize = exportBatching.getGeometryBatchSize();
		if (geometryBatchSize >  ExportBatching.MAX_BATCH_SIZE)
			geometryBatchSize = ExportBatching.MAX_BATCH_SIZE;

		int blobBatchSize = exportBatching.getBlobBatchSize();
		if (blobBatchSize > ExportBatching.MAX_BATCH_SIZE)
			blobBatchSize = ExportBatching.MAX_BATCH_SIZE;

		expResMinThreadsText.setValue(threadPool.getMinThreads());
		expResMaxThreadsText.setValue(threadPool.getMaxThreads());
		expResFeatBatchText.setValue(featureBatchSize);
		expResGeomBatchText.setValue(geometryBatchSize);
		expResBlobBatchText.setValue(blobBatchSize);
		expResGeomCacheText.setValue(geometry.getCacheSize());
		expResFeatCacheText.setValue(feature.getCacheSize());		
		expResGeomDrainText.setValue((int)(geometry.getPageFactor() * 100));
		expResFeatDrainText.setValue((int)(feature.getPageFactor() * 100));		
		expResGeomPartText.setValue(geometry.getPartitions());
		expResFeatPartText.setValue(feature.getPartitions());
	}

	@Override
	public void setSettings() {
		ThreadPoolConfig threadPool = config.getProject().getExportConfig().getResources().getThreadPool().getDefaultPool();
		ExportBatching exportBatching = config.getProject().getDatabaseConfig().getExportBatching();
		UIDCacheConfig geometry = config.getProject().getExportConfig().getResources().getGmlIdCache().getGeometry();
		UIDCacheConfig feature = config.getProject().getExportConfig().getResources().getGmlIdCache().getFeature();

		int minThreads = ((Number)expResMinThreadsText.getValue()).intValue();
		int maxThreads = ((Number)expResMaxThreadsText.getValue()).intValue();
		int featureBatchSize = ((Number) expResFeatBatchText.getValue()).intValue();
		int geometryBatchSize = ((Number) expResGeomBatchText.getValue()).intValue();
		int blobBatchSize = ((Number) expResBlobBatchText.getValue()).intValue();

		if (minThreads > maxThreads) {
			minThreads = maxThreads;
			expResMinThreadsText.setValue(minThreads);
		}

		if (featureBatchSize > ExportBatching.MAX_BATCH_SIZE) {
			featureBatchSize = ExportBatching.MAX_BATCH_SIZE;
			expResFeatBatchText.setValue(featureBatchSize);
		}

		if (geometryBatchSize > ExportBatching.MAX_BATCH_SIZE) {
			geometryBatchSize = ExportBatching.MAX_BATCH_SIZE;
			expResGeomBatchText.setValue(geometryBatchSize);
		}

		if (blobBatchSize > ExportBatching.MAX_BATCH_SIZE) {
			blobBatchSize = ExportBatching.MAX_BATCH_SIZE;
			expResBlobBatchText.setValue(blobBatchSize);
		}

		threadPool.setMinThreads(minThreads);
		threadPool.setMaxThreads(maxThreads);

		exportBatching.setFeatureBatchSize(featureBatchSize);
		exportBatching.setGeometryBatchSize(geometryBatchSize);
		exportBatching.setBlobBatchSize(blobBatchSize);

		geometry.setCacheSize(((Number)expResGeomCacheText.getValue()).intValue());			
		feature.setCacheSize(((Number)expResFeatCacheText.getValue()).intValue());
		geometry.setPageFactor(((Number)expResGeomDrainText.getValue()).floatValue() / 100);
		feature.setPageFactor(((Number)expResFeatDrainText.getValue()).floatValue() / 100);
		geometry.setPartitions(((Number)expResGeomPartText.getValue()).intValue());
		feature.setPartitions(((Number)expResFeatPartText.getValue()).intValue());
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.export.resources");
	}
}

