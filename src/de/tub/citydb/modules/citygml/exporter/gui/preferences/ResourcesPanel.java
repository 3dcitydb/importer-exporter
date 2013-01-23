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
package de.tub.citydb.modules.citygml.exporter.gui.preferences;

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
import de.tub.citydb.config.project.system.GmlIdLookupServerConfig;
import de.tub.citydb.config.project.system.ThreadPoolConfig;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ResourcesPanel extends AbstractPreferencesComponent{
	private JPanel block1;
	private JPanel block2;
	private JLabel expResMinThreadsLabel;
	private JFormattedTextField expResMinThreadsText;
	private JLabel expResMaxThreadsLabel;
	private JFormattedTextField expResMaxThreadsText;	
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
		ThreadPoolConfig threadPool = config.getProject().getExporter().getSystem().getThreadPool().getDefaultPool();
		GmlIdLookupServerConfig geometry = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getGeometry();
		GmlIdLookupServerConfig feature = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getFeature();

		try { expResMinThreadsText.commitEdit(); } catch (ParseException e) { }
		try { expResMaxThreadsText.commitEdit(); } catch (ParseException e) { }
		try { expResGeomCacheText.commitEdit(); } catch (ParseException e) { }
		try { expResGeomDrainText.commitEdit(); } catch (ParseException e) { }
		try { expResGeomPartText.commitEdit(); } catch (ParseException e) { }
		try { expResFeatCacheText.commitEdit(); } catch (ParseException e) { }
		try { expResFeatDrainText.commitEdit(); } catch (ParseException e) { }
		try { expResFeatPartText.commitEdit(); } catch (ParseException e) { }
		
		if (((Number)expResMinThreadsText.getValue()).intValue() != threadPool.getMinThreads()) return true;
		if (((Number)expResMaxThreadsText.getValue()).intValue() != threadPool.getMaxThreads()) return true;
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
		expResMinThreadsLabel = new JLabel();
		expResMaxThreadsLabel = new JLabel();
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
		
		DecimalFormat cacheEntryFormat = new DecimalFormat("########");
		cacheEntryFormat.setMaximumIntegerDigits(8);
		cacheEntryFormat.setMinimumIntegerDigits(1);		
		expResGeomCacheText = new JFormattedTextField(cacheEntryFormat);
		expResFeatCacheText = new JFormattedTextField(cacheEntryFormat);
		
		PopupMenuDecorator.getInstance().decorate(expResMinThreadsText, expResMaxThreadsText, expResGeomDrainText, 
				expResFeatDrainText, expResGeomPartText, expResFeatPartText, expResGeomCacheText, expResFeatCacheText);
		
		expResMinThreadsText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(expResMinThreadsText, 1);
			}
		});
		
		expResMaxThreadsText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(expResMaxThreadsText, 1);
			}
		});
		
		expResGeomCacheText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(expResGeomCacheText, 200000);
			}
		});
		
		expResFeatCacheText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(expResFeatCacheText, 200000);
			}
		});

		expResGeomDrainText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(expResGeomDrainText, 85, 100);
			}
		});
		
		expResFeatDrainText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(expResFeatDrainText, 85, 100);
			}
		});
		
		expResGeomPartText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(expResGeomPartText, 10, 100);
			}
		});
		
		expResFeatPartText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(expResFeatPartText, 10, 100);
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
			block2.add(expResGeomLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomCacheText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomCacheLabel, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomDrainText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomDrainLabel, GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomPartText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomPartLabel, GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatCacheText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatCacheLabel, GuiUtil.setConstraints(2,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatDrainText, GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatDrainLabel, GuiUtil.setConstraints(2,4,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatPartText, GuiUtil.setConstraints(1,5,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatPartLabel, GuiUtil.setConstraints(2,5,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
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
		((TitledBorder)block2.getBorder()).setTitle(Internal.I18N.getString("common.pref.resources.border.idCache"));

		expResMinThreadsLabel.setText(Internal.I18N.getString("common.pref.resources.label.minThreads"));
		expResMaxThreadsLabel.setText(Internal.I18N.getString("common.pref.resources.label.maxThreads"));

		expResGeomLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry"));
		expResGeomCacheLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.entry"));
		expResGeomDrainLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.drain"));
		expResGeomPartLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.partition"));
		expResFeatLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature"));
		expResFeatCacheLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.entry"));
		expResFeatDrainLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.drain"));
		expResFeatPartLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.partition"));
	}

	@Override
	public void loadSettings() {
		ThreadPoolConfig threadPool = config.getProject().getExporter().getSystem().getThreadPool().getDefaultPool();
		GmlIdLookupServerConfig geometry = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getGeometry();
		GmlIdLookupServerConfig feature = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getFeature();

		expResMinThreadsText.setValue(threadPool.getMinThreads());
		expResMaxThreadsText.setValue(threadPool.getMaxThreads());
		expResGeomCacheText.setValue(geometry.getCacheSize());
		expResFeatCacheText.setValue(feature.getCacheSize());		
		expResGeomDrainText.setValue((int)(geometry.getPageFactor() * 100));
		expResFeatDrainText.setValue((int)(feature.getPageFactor() * 100));		
		expResGeomPartText.setValue(geometry.getPartitions());
		expResFeatPartText.setValue(feature.getPartitions());
	}

	@Override
	public void setSettings() {
		ThreadPoolConfig threadPool = config.getProject().getExporter().getSystem().getThreadPool().getDefaultPool();
		GmlIdLookupServerConfig geometry = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getGeometry();
		GmlIdLookupServerConfig feature = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getFeature();

		int minThreads = ((Number)expResMinThreadsText.getValue()).intValue();
		int maxThreads = ((Number)expResMaxThreadsText.getValue()).intValue();

		if (minThreads > maxThreads) {
			minThreads = maxThreads;
			expResMinThreadsText.setValue(minThreads);
		}
		
		threadPool.setMinThreads(minThreads);
		threadPool.setMaxThreads(maxThreads);

		geometry.setCacheSize(((Number)expResGeomCacheText.getValue()).intValue());			
		feature.setCacheSize(((Number)expResFeatCacheText.getValue()).intValue());
		geometry.setPageFactor(((Number)expResGeomDrainText.getValue()).floatValue() / 100);
		feature.setPageFactor(((Number)expResFeatDrainText.getValue()).floatValue() / 100);
		geometry.setPartitions(((Number)expResGeomPartText.getValue()).intValue());
		feature.setPartitions(((Number)expResFeatPartText.getValue()).intValue());
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.export.resources");
	}
}

