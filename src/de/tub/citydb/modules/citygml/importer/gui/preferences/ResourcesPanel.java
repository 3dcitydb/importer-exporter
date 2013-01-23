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
package de.tub.citydb.modules.citygml.importer.gui.preferences;

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

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.UpdateBatching;
import de.tub.citydb.config.project.system.GmlIdLookupServerConfig;
import de.tub.citydb.config.project.system.ThreadPoolConfig;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ResourcesPanel extends AbstractPreferencesComponent{
	private JPanel block1;
	private JPanel block2;
	private JPanel block3;
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
	
	public ResourcesPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ThreadPoolConfig threadPool = config.getProject().getImporter().getSystem().getThreadPool().getDefaultPool();
		UpdateBatching commit = config.getProject().getDatabase().getUpdateBatching();
		GmlIdLookupServerConfig geometry = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getGeometry();
		GmlIdLookupServerConfig feature = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getFeature();

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
		
		if (((Number)impResMinThreadsText.getValue()).intValue() != threadPool.getMinThreads()) return true;
		if (((Number)impResMaxThreadsText.getValue()).intValue() != threadPool.getMaxThreads()) return true;
		if (((Number)impResTransaktFeatureText.getValue()).intValue() != commit.getFeatureBatchValue()) return true;
		if (((Number)impResTransaktCacheText.getValue()).intValue() != commit.getGmlIdLookupServerBatchValue()) return true;
		if (((Number)impResTransaktTempText.getValue()).intValue() != commit.getTempBatchValue()) return true;
		if (((Number)impResGeomCacheText.getValue()).intValue() != geometry.getCacheSize()) return true;
		if (((Number)impResGeomDrainText.getValue()).intValue() != (int)(geometry.getPageFactor() * 100)) return true;
		if (((Number)impResGeomPartText.getValue()).intValue() != geometry.getPartitions()) return true;
		if (((Number)impResFeatCacheText.getValue()).intValue() != feature.getCacheSize()) return true;
		if (((Number)impResFeatDrainText.getValue()).intValue() != (int)(feature.getPageFactor() * 100)) return true;
		if (((Number)impResFeatPartText.getValue()).intValue() != feature.getPartitions()) return true;

		return false;
	}

	private void initGui(){
		block1 = new JPanel();
		block2 = new JPanel();
		block3 = new JPanel();
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
		
		DecimalFormat threeIntFormat = new DecimalFormat("###");	
		threeIntFormat.setMaximumIntegerDigits(3);
		threeIntFormat.setMinimumIntegerDigits(1);
		impResMinThreadsText = new JFormattedTextField(threeIntFormat);
		impResMaxThreadsText = new JFormattedTextField(threeIntFormat);
		impResGeomDrainText = new JFormattedTextField(threeIntFormat);
		impResFeatDrainText = new JFormattedTextField(threeIntFormat);
		impResGeomPartText = new JFormattedTextField(threeIntFormat);
		impResFeatPartText = new JFormattedTextField(threeIntFormat);
	
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

		PopupMenuDecorator.getInstance().decorate(impResMinThreadsText, impResMaxThreadsText,
				impResGeomDrainText, impResFeatDrainText, impResGeomPartText, impResFeatPartText,
				impResTransaktFeatureText, impResTransaktCacheText, impResTransaktTempText,
				impResGeomCacheText, impResFeatCacheText);
		
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
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("common.pref.resources.border.multiCPU"));	
		((TitledBorder)block2.getBorder()).setTitle(Internal.I18N.getString("pref.import.resources.border.commit"));	
		((TitledBorder)block3.getBorder()).setTitle(Internal.I18N.getString("common.pref.resources.border.idCache"));	

		impResMinThreadsLabel.setText(Internal.I18N.getString("common.pref.resources.label.minThreads"));
		impResMaxThreadsLabel.setText(Internal.I18N.getString("common.pref.resources.label.maxThreads"));
		
		impResTransaktLabel.setText(Internal.I18N.getString("pref.import.resources.label.commit"));
		impResTransaktFeatureLabel.setText(Internal.I18N.getString("pref.import.resources.label.commit.feature"));
		impResTransaktCacheLabel.setText(Internal.I18N.getString("pref.import.resources.label.commit.cache"));
		impResTransaktTempLabel.setText(Internal.I18N.getString("pref.import.resources.label.commit.temp"));

		impResGeomLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry"));
		impResGeomCacheLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.entry"));
		impResGeomDrainLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.drain"));
		impResGeomPartLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.partition"));
		impResFeatLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature"));
		impResFeatCacheLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.entry"));
		impResFeatDrainLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.drain"));
		impResFeatPartLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.partition"));
	}

	@Override
	public void loadSettings() {
		ThreadPoolConfig threadPool = config.getProject().getImporter().getSystem().getThreadPool().getDefaultPool();
		UpdateBatching commit = config.getProject().getDatabase().getUpdateBatching();
		GmlIdLookupServerConfig geometry = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getGeometry();
		GmlIdLookupServerConfig feature = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getFeature();

		int commitFeature = commit.getFeatureBatchValue();
		if (commitFeature > Internal.ORACLE_MAX_BATCH_SIZE)
			commitFeature = Internal.ORACLE_MAX_BATCH_SIZE;
		
		int commitCache = commit.getGmlIdLookupServerBatchValue();
		if (commitCache > Internal.ORACLE_MAX_BATCH_SIZE)
			commitCache = Internal.ORACLE_MAX_BATCH_SIZE;
		
		int commitTemp = commit.getTempBatchValue();
		if (commitTemp > Internal.ORACLE_MAX_BATCH_SIZE)
			commitTemp = Internal.ORACLE_MAX_BATCH_SIZE;
		
		impResMinThreadsText.setValue(threadPool.getMinThreads());
		impResMaxThreadsText.setValue(threadPool.getMaxThreads());
		impResTransaktFeatureText.setValue(commitFeature);		
		impResTransaktCacheText.setValue(commitCache);
		impResTransaktTempText.setValue(commitTemp);		
		impResGeomCacheText.setValue(geometry.getCacheSize());
		impResFeatCacheText.setValue(feature.getCacheSize());		
		impResGeomDrainText.setValue((int)(geometry.getPageFactor() * 100));
		impResFeatDrainText.setValue((int)(feature.getPageFactor() * 100));		
		impResGeomPartText.setValue(geometry.getPartitions());
		impResFeatPartText.setValue(feature.getPartitions());
	}

	@Override
	public void setSettings() {
		ThreadPoolConfig threadPool = config.getProject().getImporter().getSystem().getThreadPool().getDefaultPool();
		UpdateBatching commit = config.getProject().getDatabase().getUpdateBatching();
		GmlIdLookupServerConfig geometry = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getGeometry();
		GmlIdLookupServerConfig feature = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getFeature();

		int minThreads = ((Number)impResMinThreadsText.getValue()).intValue();
		int maxThreads = ((Number)impResMaxThreadsText.getValue()).intValue();
		int featBatch = ((Number)impResTransaktFeatureText.getValue()).intValue();
		int lookupBatch = ((Number)impResTransaktCacheText.getValue()).intValue();
		int tempBatch = ((Number)impResTransaktTempText.getValue()).intValue();

		if (minThreads > maxThreads) {
			minThreads = maxThreads;
			impResMinThreadsText.setValue(minThreads);
		}

		if (featBatch > Internal.ORACLE_MAX_BATCH_SIZE) {
			featBatch = Internal.ORACLE_MAX_BATCH_SIZE;
			impResTransaktFeatureText.setValue(featBatch);
		}
		
		if (lookupBatch > Internal.ORACLE_MAX_BATCH_SIZE) {
			lookupBatch = Internal.ORACLE_MAX_BATCH_SIZE;
			impResTransaktCacheText.setValue(lookupBatch);
		}
		
		if (tempBatch > Internal.ORACLE_MAX_BATCH_SIZE) {
			tempBatch = Internal.ORACLE_MAX_BATCH_SIZE;
			impResTransaktTempText.setValue(tempBatch);
		}
		
		threadPool.setMinThreads(minThreads);
		threadPool.setMaxThreads(maxThreads);
		
		commit.setFeatureBatchValue(featBatch);
		commit.setGmlIdLookupServerBatchValue(lookupBatch);
		commit.setTempBatchValue(tempBatch);

		geometry.setCacheSize(((Number)impResGeomCacheText.getValue()).intValue());			
		feature.setCacheSize(((Number)impResFeatCacheText.getValue()).intValue());
		geometry.setPageFactor(((Number)impResGeomDrainText.getValue()).floatValue() / 100);
		feature.setPageFactor(((Number)impResFeatDrainText.getValue()).floatValue() / 100);
		geometry.setPartitions(((Number)impResGeomPartText.getValue()).intValue());
		feature.setPartitions(((Number)impResFeatPartText.getValue()).intValue());
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.import.resources");
	}
}
