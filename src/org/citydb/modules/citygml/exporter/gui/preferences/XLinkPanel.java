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
package org.citydb.modules.citygml.exporter.gui.preferences;

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

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.exporter.XLinkConfig;
import org.citydb.config.project.exporter.XLinkFeatureConfig;
import org.citydb.config.project.exporter.XLinkMode;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.util.gmlid.GMLIdManager;

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
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.export.xlink.border.feature"));	
		((TitledBorder)block2.getBorder()).setTitle(Language.I18N.getString("pref.export.xlink.border.geometry"));	

		xlinkToFeature.setText(Language.I18N.getString("pref.export.xlink.label.feature.export"));
		copyFeature.setText(Language.I18N.getString("pref.export.xlink.label.feature.copy"));	
		featureIdPrefixLabel.setText(Language.I18N.getString("pref.export.xlink.label.copy.prefix"));
		featureAppendId.setText(Language.I18N.getString("pref.export.xlink.label.append"));
		featureKeepExtRef.setText(Language.I18N.getString("pref.export.xlink.label.feature.keepId"));

		xlinkToGeometry.setText(Language.I18N.getString("pref.export.xlink.label.geometry.export"));
		copyGeometry.setText(Language.I18N.getString("pref.export.xlink.label.geometry.copy"));	
		geometryIdPrefixLabel.setText(Language.I18N.getString("pref.export.xlink.label.copy.prefix"));
		geometryAppendId.setText(Language.I18N.getString("pref.export.xlink.label.append"));
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
		return Language.I18N.getString("pref.tree.export.xlink");
	}

}
