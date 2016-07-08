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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.UpdateBatching;
import org.citydb.config.project.resources.ThreadPoolConfig;
import org.citydb.config.project.resources.UIDCacheConfig;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ResourcesPanel extends AbstractPreferencesComponent{
	private JPanel block1;
	private JPanel block2;
	private JPanel block3;
	private JPanel block4;
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
		ThreadPoolConfig threadPool = config.getProject().getImporter().getResources().getThreadPool().getDefaultPool();
		UpdateBatching commit = config.getProject().getDatabase().getUpdateBatching();
		UIDCacheConfig geometry = config.getProject().getImporter().getResources().getGmlIdCache().getGeometry();
		UIDCacheConfig feature = config.getProject().getImporter().getResources().getGmlIdCache().getFeature();
		UIDCacheConfig texImage = config.getProject().getImporter().getResources().getTexImageCache();
		
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
		if (((Number)impResTransaktFeatureText.getValue()).intValue() != commit.getFeatureBatchValue()) return true;
		if (((Number)impResTransaktCacheText.getValue()).intValue() != commit.getGmlIdCacheBatchValue()) return true;
		if (((Number)impResTransaktTempText.getValue()).intValue() != commit.getTempBatchValue()) return true;
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

	private void initGui(){
		block1 = new JPanel();
		block2 = new JPanel();
		block3 = new JPanel();
		block4 = new JPanel();
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
		
		DecimalFormat threeIntFormat = new DecimalFormat("###");	
		threeIntFormat.setMaximumIntegerDigits(3);
		threeIntFormat.setMinimumIntegerDigits(1);
		impResMinThreadsText = new JFormattedTextField(threeIntFormat);
		impResMaxThreadsText = new JFormattedTextField(threeIntFormat);
		impResGeomDrainText = new JFormattedTextField(threeIntFormat);
		impResFeatDrainText = new JFormattedTextField(threeIntFormat);
		impResGeomPartText = new JFormattedTextField(threeIntFormat);
		impResFeatPartText = new JFormattedTextField(threeIntFormat);
		impResTexDrainText = new JFormattedTextField(threeIntFormat);
		impResTexPartText = new JFormattedTextField(threeIntFormat);
		
		DecimalFormat batchFormat = new DecimalFormat("#####");
		batchFormat.setMaximumIntegerDigits(5);
		batchFormat.setMinimumIntegerDigits(1);		
		impResTransaktFeatureText = new JFormattedTextField(batchFormat);
		impResTransaktCacheText = new JFormattedTextField(batchFormat);
		impResTransaktTempText = new JFormattedTextField(batchFormat);
		
		DecimalFormat cacheEntryFormat = new DecimalFormat("########");
		cacheEntryFormat.setMaximumIntegerDigits(8);
		cacheEntryFormat.setMinimumIntegerDigits(1);		
		impResGeomCacheText = new JFormattedTextField(cacheEntryFormat);
		impResFeatCacheText = new JFormattedTextField(cacheEntryFormat);	
		impResTexCacheText = new JFormattedTextField(cacheEntryFormat);

		PopupMenuDecorator.getInstance().decorate(impResMinThreadsText, impResMaxThreadsText,
				impResGeomDrainText, impResFeatDrainText, impResTexDrainText, impResGeomPartText, impResFeatPartText, impResTexPartText,
				impResTransaktFeatureText, impResTransaktCacheText, impResTransaktTempText,
				impResGeomCacheText, impResFeatCacheText, impResTexCacheText);
		
		impResMinThreadsText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(impResMinThreadsText, 1);
			}
		});
		
		impResMaxThreadsText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(impResMaxThreadsText, 1);
			}
		});
		
		impResTransaktFeatureText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(impResTransaktFeatureText, 20);
			}
		});
		
		impResTransaktCacheText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(impResTransaktCacheText, 10000);
			}
		});
		
		impResTransaktTempText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(impResTransaktTempText, 10000);
			}
		});
		
		impResGeomCacheText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(impResGeomCacheText, 200000);
			}
		});
		
		impResFeatCacheText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(impResFeatCacheText, 200000);
			}
		});
		
		impResTexCacheText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(impResTexCacheText, 200000);
			}
		});

		impResGeomDrainText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(impResGeomDrainText, 85, 100);
			}
		});
		
		impResFeatDrainText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(impResFeatDrainText, 85, 100);
			}
		});
		
		impResTexDrainText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(impResTexDrainText, 85, 100);
			}
		});
		
		impResGeomPartText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(impResGeomPartText, 10, 100);
			}
		});
		
		impResFeatPartText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(impResFeatPartText, 10, 100);
			}
		});
		
		impResTexPartText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(impResTexPartText, 10, 100);
			}
		});
		
		setLayout(new GridBagLayout());
		add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block1.setBorder(BorderFactory.createTitledBorder(""));
		block1.setLayout(new GridBagLayout());
		{
			block1.add(impResMinThreadsLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(impResMinThreadsText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(impResMaxThreadsLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(impResMaxThreadsText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
		add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block2.setBorder(BorderFactory.createTitledBorder(""));
		block2.setLayout(new GridBagLayout());
		{
			block2.add(impResTransaktLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktFeatureText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktFeatureLabel, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktCacheText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktCacheLabel, GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktTempText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktTempLabel, GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
		add(block3, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block3.setBorder(BorderFactory.createTitledBorder(""));
		block3.setLayout(new GridBagLayout());
		{
			block3.add(impResGeomLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomCacheText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomCacheLabel, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomDrainText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomDrainLabel, GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomPartText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomPartLabel, GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatCacheText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatCacheLabel, GuiUtil.setConstraints(2,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatDrainText, GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatDrainLabel, GuiUtil.setConstraints(2,4,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatPartText, GuiUtil.setConstraints(1,5,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatPartLabel, GuiUtil.setConstraints(2,5,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
		add(block4, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block4.setBorder(BorderFactory.createTitledBorder(""));
		block4.setLayout(new GridBagLayout());
		{
			block4.add(impResTexLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block4.add(impResTexCacheText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block4.add(impResTexCacheLabel, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block4.add(impResTexDrainText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block4.add(impResTexDrainLabel, GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block4.add(impResTexPartText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block4.add(impResTexPartLabel, GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
	}
	
	private void checkNonNegative(JFormattedTextField field, int defaultValue) {
		if (((Number)field.getValue()).intValue() < 0)
			field.setValue(defaultValue);
	}
	
	private void checkNonNegativeRange(JFormattedTextField field, int min, int max) {
		if (((Number)field.getValue()).intValue() < 0)
			field.setValue(min);
		else if (((Number)field.getValue()).intValue() > 100)
			field.setValue(max);
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("common.pref.resources.border.multiCPU"));	
		((TitledBorder)block2.getBorder()).setTitle(Language.I18N.getString("pref.import.resources.border.commit"));	
		((TitledBorder)block3.getBorder()).setTitle(Language.I18N.getString("common.pref.resources.border.idCache"));	
		((TitledBorder)block4.getBorder()).setTitle(Language.I18N.getString("pref.import.resources.border.texImageCache"));	

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
		ThreadPoolConfig threadPool = config.getProject().getImporter().getResources().getThreadPool().getDefaultPool();
		UpdateBatching commit = config.getProject().getDatabase().getUpdateBatching();
		UIDCacheConfig geometry = config.getProject().getImporter().getResources().getGmlIdCache().getGeometry();
		UIDCacheConfig feature = config.getProject().getImporter().getResources().getGmlIdCache().getFeature();
		UIDCacheConfig texImage = config.getProject().getImporter().getResources().getTexImageCache();

		int commitFeature = commit.getFeatureBatchValue();
		if (commitFeature > Database.MAX_BATCH_SIZE)
			commitFeature = Database.MAX_BATCH_SIZE;
		
		int commitCache = commit.getGmlIdCacheBatchValue();
		if (commitCache > Database.MAX_BATCH_SIZE)
			commitCache = Database.MAX_BATCH_SIZE;
		
		int commitTemp = commit.getTempBatchValue();
		if (commitTemp > Database.MAX_BATCH_SIZE)
			commitTemp = Database.MAX_BATCH_SIZE;
		
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
		ThreadPoolConfig threadPool = config.getProject().getImporter().getResources().getThreadPool().getDefaultPool();
		UpdateBatching commit = config.getProject().getDatabase().getUpdateBatching();
		UIDCacheConfig geometry = config.getProject().getImporter().getResources().getGmlIdCache().getGeometry();
		UIDCacheConfig feature = config.getProject().getImporter().getResources().getGmlIdCache().getFeature();
		UIDCacheConfig texImage = config.getProject().getImporter().getResources().getTexImageCache();

		int minThreads = ((Number)impResMinThreadsText.getValue()).intValue();
		int maxThreads = ((Number)impResMaxThreadsText.getValue()).intValue();
		int featBatch = ((Number)impResTransaktFeatureText.getValue()).intValue();
		int lookupBatch = ((Number)impResTransaktCacheText.getValue()).intValue();
		int tempBatch = ((Number)impResTransaktTempText.getValue()).intValue();

		if (minThreads > maxThreads) {
			minThreads = maxThreads;
			impResMinThreadsText.setValue(minThreads);
		}

		if (featBatch > Database.MAX_BATCH_SIZE) {
			featBatch = Database.MAX_BATCH_SIZE;
			impResTransaktFeatureText.setValue(featBatch);
		}
		
		if (lookupBatch > Database.MAX_BATCH_SIZE) {
			lookupBatch = Database.MAX_BATCH_SIZE;
			impResTransaktCacheText.setValue(lookupBatch);
		}
		
		if (tempBatch > Database.MAX_BATCH_SIZE) {
			tempBatch = Database.MAX_BATCH_SIZE;
			impResTransaktTempText.setValue(tempBatch);
		}
		
		threadPool.setMinThreads(minThreads);
		threadPool.setMaxThreads(maxThreads);
		
		commit.setFeatureBatchValue(featBatch);
		commit.setGmlIdCacheBatchValue(lookupBatch);
		commit.setTempBatchValue(tempBatch);

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
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.resources");
	}
}
