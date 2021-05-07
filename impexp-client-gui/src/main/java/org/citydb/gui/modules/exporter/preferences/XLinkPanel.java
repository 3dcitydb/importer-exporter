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
package org.citydb.gui.modules.exporter.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.XLinkConfig;
import org.citydb.config.project.exporter.XLinkFeatureConfig;
import org.citydb.config.project.exporter.XLinkMode;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class XLinkPanel extends AbstractPreferencesComponent {
	private TitledPanel featurePanel;
	private TitledPanel geometryPanel;

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
	
	public XLinkPanel(Config config) {
		super(config);
		initGui();
	}
	
	@Override
	public boolean isModified() {
		XLinkFeatureConfig feature = config.getExportConfig().getCityGMLOptions().getXlink().getFeature();
		XLinkConfig geometry = config.getExportConfig().getCityGMLOptions().getXlink().getGeometry();

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
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			int lmargin = GuiUtil.getTextOffset(copyFeature);
			{
				content.add(xlinkToFeature, GuiUtil.setConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(copyFeature, GuiUtil.setConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(featureIdPrefixLabel, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 5, lmargin, 0, 5));
				content.add(featureIdPrefix, GuiUtil.setConstraints(1, 2, 1, 1, GridBagConstraints.BOTH, 5, 5, 0, 0));
				content.add(featureKeepExtRef, GuiUtil.setConstraints(0, 3, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, lmargin, 0, 0));
				content.add(featureAppendId, GuiUtil.setConstraints(0, 4, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, lmargin, 0, 0));
			}

			featurePanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			int lmargin = GuiUtil.getTextOffset(copyGeometry);
			{
				content.add(xlinkToGeometry, GuiUtil.setConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(copyGeometry, GuiUtil.setConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(geometryIdPrefixLabel, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 5, lmargin, 0, 5));
				content.add(geometryIdPrefix, GuiUtil.setConstraints(1, 2, 1, 0, GridBagConstraints.BOTH, 5, 5, 0, 0));
				content.add(geometryAppendId, GuiUtil.setConstraints(0, 3, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, lmargin, 0, 0));
			}

			geometryPanel = new TitledPanel().build(content);
		}

		add(featurePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(geometryPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		ActionListener copyFeatureListener = e -> setEnabledCopyFeature();
		ActionListener copyGeometryListener = e -> setEnabledCopyGeometry();
		
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
		featurePanel.setTitle(Language.I18N.getString("pref.export.xlink.border.feature"));
		geometryPanel.setTitle(Language.I18N.getString("pref.export.xlink.border.geometry"));

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
		XLinkFeatureConfig feature = config.getExportConfig().getCityGMLOptions().getXlink().getFeature();
		XLinkConfig geometry = config.getExportConfig().getCityGMLOptions().getXlink().getGeometry();
		
		if (feature.getIdPrefix() != null && feature.getIdPrefix().trim().length() != 0)
			featureIdPrefix.setText(feature.getIdPrefix());
		else {
			featureIdPrefix.setText(DefaultGMLIdManager.getInstance().getDefaultPrefix());
			feature.setIdPrefix(DefaultGMLIdManager.getInstance().getDefaultPrefix());
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
			geometryIdPrefix.setText(DefaultGMLIdManager.getInstance().getDefaultPrefix());
			geometry.setIdPrefix(DefaultGMLIdManager.getInstance().getDefaultPrefix());
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
		XLinkFeatureConfig feature = config.getExportConfig().getCityGMLOptions().getXlink().getFeature();
		XLinkConfig geometry = config.getExportConfig().getCityGMLOptions().getXlink().getGeometry();
		
		if (featureIdPrefix.getText() != null && DefaultGMLIdManager.getInstance().isValidPrefix(featureIdPrefix.getText()))
			feature.setIdPrefix(featureIdPrefix.getText().trim());
		else {
			feature.setIdPrefix(DefaultGMLIdManager.getInstance().getDefaultPrefix());
			featureIdPrefix.setText(DefaultGMLIdManager.getInstance().getDefaultPrefix());
		}
		
		if (xlinkToFeature.isSelected())
			feature.setMode(XLinkMode.XLINK);
		else
			feature.setMode(XLinkMode.COPY);

		feature.setAppendId(featureAppendId.isSelected());
		feature.setKeepGmlIdAsExternalReference(featureKeepExtRef.isSelected());

		if (geometryIdPrefix.getText() != null && DefaultGMLIdManager.getInstance().isValidPrefix(geometryIdPrefix.getText()))
			geometry.setIdPrefix(geometryIdPrefix.getText().trim());
		else {
			geometry.setIdPrefix(DefaultGMLIdManager.getInstance().getDefaultPrefix());
			geometryIdPrefix.setText(DefaultGMLIdManager.getInstance().getDefaultPrefix());
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
