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
package de.tub.citydb.modules.citygml.exporter.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.util.gmlid.GMLIdManager;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.exporter.XLinkConfig;
import de.tub.citydb.config.project.exporter.XLinkFeatureConfig;
import de.tub.citydb.config.project.exporter.XLinkMode;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class XLinkPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JPanel block1_1;
	private JPanel block2;
	private JPanel block2_1;
	
	private JRadioButton xlinkToFeature;
	private JRadioButton copyFeature;
	private JLabel featureIdPrefixLabel;
	private JTextField featureIdPrefix;	
	private JCheckBox featureAppendId;
	private JCheckBox featureKeepExtRef;
	private JRadioButton xlinkToGeometry;
	private JRadioButton copyGeometry;
	private JLabel geometryIdPrefixLabel;
	private JTextField geometryIdPrefix;
	private JCheckBox geometryAppendId;
	
	private GMLIdManager gmlIdManager;
	
	public XLinkPanel(Config config) {
		super(config);
		gmlIdManager = DefaultGMLIdManager.getInstance();
		
		initGui();
	}
	
	@Override
	public boolean isModified() {
		XLinkFeatureConfig feature = config.getProject().getExporter().getXlink().getFeature();
		XLinkConfig geometry = config.getProject().getExporter().getXlink().getGeometry();

		if (!featureIdPrefix.getText().equals(feature.getIdPrefix())) return true;
		if (xlinkToFeature.isSelected() != feature.isModeXLink()) return true;
		if (copyFeature.isSelected() != feature.isModeCopy()) return true;
		if (featureAppendId.isSelected() != feature.isSetAppendId()) return true;
		if (featureKeepExtRef.isSelected() != feature.isSetKeepGmlIdAsExternalReference()) return true;
		
		if (!geometryIdPrefix.getText().equals(geometry.getIdPrefix())) return true;
		if (xlinkToGeometry.isSelected() != geometry.isModeXLink()) return true;
		if (copyGeometry.isSelected() != geometry.isModeCopy()) return true;
		if (geometryAppendId.isSelected() != geometry.isSetAppendId()) return true;
		
		return false;
	}
	
	private void initGui() {
		xlinkToFeature = new JRadioButton();
		copyFeature = new JRadioButton();
		ButtonGroup featureHandling = new ButtonGroup();
		featureHandling.add(xlinkToFeature);
		featureHandling.add(copyFeature);
		featureIdPrefixLabel = new JLabel();
		featureIdPrefix = new JTextField();
		featureAppendId = new JCheckBox();
		featureKeepExtRef = new JCheckBox();
	
		xlinkToGeometry = new JRadioButton();
		copyGeometry = new JRadioButton();
		ButtonGroup geometryHandling = new ButtonGroup();
		geometryHandling.add(xlinkToGeometry);
		geometryHandling.add(copyGeometry);
		geometryIdPrefixLabel = new JLabel();
		geometryIdPrefix = new JTextField();
		geometryAppendId = new JCheckBox();
		
		PopupMenuDecorator.getInstance().decorate(featureIdPrefix, geometryIdPrefix);
		
		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			block1_1 = new JPanel();
			block2 = new JPanel();
			block2_1 = new JPanel();
			
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			int lmargin = (int)(copyFeature.getPreferredSize().getWidth()) + 11;
			xlinkToFeature.setIconTextGap(10);
			copyFeature.setIconTextGap(10);
			{
				block1.add(xlinkToFeature, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(copyFeature, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(block1_1, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				
				block1_1.setLayout(new GridBagLayout());
				block1_1.setBorder(BorderFactory.createEmptyBorder());
				{
					block1_1.add(featureIdPrefixLabel, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.BOTH,0,0,0,5));
					block1_1.add(featureIdPrefix, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,0));
				}
				
				block1.add(featureKeepExtRef, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));
				block1.add(featureAppendId, GuiUtil.setConstraints(0,4,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));
			}
			
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			lmargin = (int)(copyGeometry.getPreferredSize().getWidth()) + 11;
			xlinkToGeometry.setIconTextGap(10);
			copyGeometry.setIconTextGap(10);
			geometryAppendId.setIconTextGap(10);
			{
				block2.add(xlinkToGeometry, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(copyGeometry, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(block2_1, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));
				
				block2_1.setLayout(new GridBagLayout());
				block2_1.setBorder(BorderFactory.createEmptyBorder());
				{
					block2_1.add(geometryIdPrefixLabel, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.BOTH,0,0,0,5));
					block2_1.add(geometryIdPrefix, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,0,0));
				}
				
				block2.add(geometryAppendId, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));
			}
		}
		
		ActionListener copyFeatureListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledCopyFeature();
			}
		};
		
		ActionListener copyGeometryListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledCopyGeometry();
			}
		};
		
		xlinkToFeature.addActionListener(copyFeatureListener);
		copyFeature.addActionListener(copyFeatureListener);
		
		xlinkToGeometry.addActionListener(copyGeometryListener);
		copyGeometry.addActionListener(copyGeometryListener);
	}
	
	private void setEnabledCopyFeature() {
		featureIdPrefixLabel.setEnabled(copyFeature.isSelected());
		featureIdPrefix.setEnabled(copyFeature.isSelected());
		featureAppendId.setEnabled(copyFeature.isSelected());
		featureKeepExtRef.setEnabled(copyFeature.isSelected());
	}
	
	private void setEnabledCopyGeometry() {
		geometryIdPrefixLabel.setEnabled(copyGeometry.isSelected());
		geometryIdPrefix.setEnabled(copyGeometry.isSelected());
		geometryAppendId.setEnabled(copyGeometry.isSelected());
	}
	
	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("pref.export.xlink.border.feature"));	
		((TitledBorder)block2.getBorder()).setTitle(Internal.I18N.getString("pref.export.xlink.border.geometry"));	

		xlinkToFeature.setText(Internal.I18N.getString("pref.export.xlink.label.feature.export"));
		copyFeature.setText(Internal.I18N.getString("pref.export.xlink.label.feature.copy"));	
		featureIdPrefixLabel.setText(Internal.I18N.getString("pref.export.xlink.label.copy.prefix"));
		featureAppendId.setText(Internal.I18N.getString("pref.export.xlink.label.append"));
		featureKeepExtRef.setText(Internal.I18N.getString("pref.export.xlink.label.feature.keepId"));

		xlinkToGeometry.setText(Internal.I18N.getString("pref.export.xlink.label.geometry.export"));
		copyGeometry.setText(Internal.I18N.getString("pref.export.xlink.label.geometry.copy"));	
		geometryIdPrefixLabel.setText(Internal.I18N.getString("pref.export.xlink.label.copy.prefix"));
		geometryAppendId.setText(Internal.I18N.getString("pref.export.xlink.label.append"));
	}

	@Override
	public void loadSettings() {
		XLinkFeatureConfig feature = config.getProject().getExporter().getXlink().getFeature();
		XLinkConfig geometry = config.getProject().getExporter().getXlink().getGeometry();
		
		if (feature.getIdPrefix() != null && feature.getIdPrefix().trim().length() != 0)
			featureIdPrefix.setText(feature.getIdPrefix());
		else {
			featureIdPrefix.setText(gmlIdManager.getDefaultPrefix());
			feature.setIdPrefix(gmlIdManager.getDefaultPrefix());
		}
			
		if (feature.isModeXLink())
			xlinkToFeature.setSelected(true);
		else
			copyFeature.setSelected(true);	
		
		featureAppendId.setSelected(feature.isSetAppendId());
		featureKeepExtRef.setSelected(feature.isSetKeepGmlIdAsExternalReference());

		if (geometry.getIdPrefix() != null && geometry.getIdPrefix().trim().length() != 0)
			geometryIdPrefix.setText(geometry.getIdPrefix());
		else {
			geometryIdPrefix.setText(gmlIdManager.getDefaultPrefix());
			geometry.setIdPrefix(gmlIdManager.getDefaultPrefix());
		}
		
		if (geometry.isModeXLink())
			xlinkToGeometry.setSelected(true);
		else
			copyGeometry.setSelected(true);		
		
		geometryAppendId.setSelected(geometry.isSetAppendId());
		
		setEnabledCopyFeature();
		setEnabledCopyGeometry();
	}

	@Override
	public void setSettings() {
		XLinkFeatureConfig feature = config.getProject().getExporter().getXlink().getFeature();
		XLinkConfig geometry = config.getProject().getExporter().getXlink().getGeometry();
		
		if (featureIdPrefix.getText() != null && featureIdPrefix.getText().trim().length() != 0)
			feature.setIdPrefix(featureIdPrefix.getText().trim());
		else {
			feature.setIdPrefix(gmlIdManager.getDefaultPrefix());
			featureIdPrefix.setText(gmlIdManager.getDefaultPrefix());
		}
		
		if (xlinkToFeature.isSelected())
			feature.setMode(XLinkMode.XLINK);
		else
			feature.setMode(XLinkMode.COPY);

		feature.setAppendId(featureAppendId.isSelected());
		feature.setKeepGmlIdAsExternalReference(featureKeepExtRef.isSelected());

		if (geometryIdPrefix.getText() != null && geometryIdPrefix.getText().trim().length() != 0)
			geometry.setIdPrefix(geometryIdPrefix.getText().trim());
		else {
			geometry.setIdPrefix(gmlIdManager.getDefaultPrefix());
			geometryIdPrefix.setText(gmlIdManager.getDefaultPrefix());
		}
		
		if (xlinkToGeometry.isSelected())
			geometry.setMode(XLinkMode.XLINK);
		else
			geometry.setMode(XLinkMode.COPY);
		
		geometry.setAppendId(geometryAppendId.isSelected());
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.export.xlink");
	}

}
