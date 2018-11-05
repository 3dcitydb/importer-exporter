/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
package org.citydb.modules.citygml.importer.gui.view;

import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.ImportFilter;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import org.citydb.gui.components.feature.FeatureTypeTree;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;
import org.citydb.util.Util;
import org.jdesktop.swingx.JXTitledSeparator;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

@SuppressWarnings("serial")
public class FilterPanel extends JPanel {
	private final Config config;

	private JCheckBox useAttributeFilter;
	private JCheckBox useCounterFilter;
	private JCheckBox useBBoxFilter;
	private JCheckBox useFeatureFilter;

	private JXTitledSeparator attributeFilterSeparator;
	private JXTitledSeparator counterSeparator;
	private JXTitledSeparator bboxSeparator;
	private JXTitledSeparator featureSeparator;

	private JLabel gmlIdLabel;
	private JTextField gmlIdText;
	private JLabel gmlNameLabel;
	private JTextField gmlNameText;

	private JLabel counterStartLabel;
	private JLabel counterEndLabel;
	private JFormattedTextField counterStartText;
	private JFormattedTextField counterEndText;

	private BoundingBoxPanel bboxPanel;
	private FeatureTypeTree featureTree;

	public FilterPanel(ViewController viewController, Config config) {
		this.config = config;

		initGui(viewController);
	}

	private void initGui(ViewController viewController) {
		useAttributeFilter = new JCheckBox();
		useCounterFilter = new JCheckBox();
		useBBoxFilter = new JCheckBox();
		useFeatureFilter = new JCheckBox();

		attributeFilterSeparator = new JXTitledSeparator();
		counterSeparator = new JXTitledSeparator();
		bboxSeparator = new JXTitledSeparator();
		featureSeparator = new JXTitledSeparator();

		gmlIdLabel = new JLabel();
		gmlIdText = new JTextField();
		gmlNameLabel = new JLabel();
		gmlNameText = new JTextField();

		counterStartLabel = new JLabel();
		counterEndLabel = new JLabel();
		DecimalFormat counterFormat = new DecimalFormat("###################");
		counterFormat.setMaximumIntegerDigits(19);
		counterStartText = new JFormattedTextField(counterFormat);
		counterEndText = new JFormattedTextField(counterFormat);
		counterStartText.setFocusLostBehavior(JFormattedTextField.COMMIT);
		counterEndText.setFocusLostBehavior(JFormattedTextField.COMMIT);

		bboxPanel = viewController.getComponentFactory().createBoundingBoxPanel();

		featureTree = new FeatureTypeTree();
		featureTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 4);
		featureTree.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(0, 0, 4, 4)));
		
		// get rid of standard icons
		DefaultCheckboxTreeCellRenderer renderer = (DefaultCheckboxTreeCellRenderer) featureTree.getCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);

		//layout
		setLayout(new GridBagLayout());
		{
			JPanel filterRow = new JPanel();
			add(filterRow, GuiUtil.setConstraints(0,0,1,0,GridBagConstraints.BOTH,0,0,0,0));
			filterRow.setLayout(new GridBagLayout());
			{
				JPanel filterPanel = new JPanel();

				filterRow.add(useAttributeFilter, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NORTH,GridBagConstraints.NONE,5,0,0,5));
				filterRow.add(attributeFilterSeparator, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,5,0,0,5));
				filterRow.add(filterPanel, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,0,0,5,5));

				filterPanel.setLayout(new GridBagLayout());
				{
					// gml:id filter
					filterPanel.add(gmlIdLabel, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.HORIZONTAL,0,0,5,5));
					filterPanel.add(gmlIdText, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,0,5,5,0));

					// gml:name filter
					filterPanel.add(gmlNameLabel, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.HORIZONTAL,0,0,0,5));
					filterPanel.add(gmlNameText, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,0,5,0,0));
				}
			}
		}
		{
			JPanel counterFilterRow = new JPanel();
			add(counterFilterRow, GuiUtil.setConstraints(0,1,1,0,GridBagConstraints.BOTH,0,0,0,0));
			counterFilterRow.setLayout(new GridBagLayout());
			{
				JPanel counterPanel = new JPanel();

				counterFilterRow.add(useCounterFilter, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,5,0,0,5));
				counterFilterRow.add(counterSeparator, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,5,0,0,5));
				counterFilterRow.add(counterPanel, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,0,0,5,5));

				counterPanel.setLayout(new GridBagLayout());
				{
					counterPanel.add(counterStartLabel, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,0,0,0,5));
					counterPanel.add(counterStartText, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,5));
					counterPanel.add(counterEndLabel, GuiUtil.setConstraints(2,0,0,0,GridBagConstraints.NONE,0,10,0,5));
					counterPanel.add(counterEndText, GuiUtil.setConstraints(3,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,0));
				}
			}
		}
		{
			JPanel bboxFilterRow = new JPanel();
			add(bboxFilterRow, GuiUtil.setConstraints(0,2,1,0,GridBagConstraints.BOTH,0,0,0,0));
			bboxFilterRow.setLayout(new GridBagLayout());
			{
				bboxFilterRow.add(useBBoxFilter, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,5,0,0,5));
				bboxFilterRow.add(bboxSeparator, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,5,0,0,5));
				bboxFilterRow.add(bboxPanel, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,0,0,5,5));
			}
		}
		{
			JPanel featureClassRow = new JPanel();
			add(featureClassRow, GuiUtil.setConstraints(0,3,1,0,GridBagConstraints.HORIZONTAL,0,0,0,0));
			featureClassRow.setLayout(new GridBagLayout());
			{
				JPanel featureClassPanel = new JPanel();

				featureClassRow.add(useFeatureFilter, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,5,0,0,5));
				featureClassRow.add(featureSeparator, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,5,0,0,5));
				featureClassRow.add(featureClassPanel, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,0,0,5,5));

				featureClassPanel.setLayout(new GridBagLayout());
				{
					featureClassPanel.add(featureTree, GuiUtil.setConstraints(0,0,1,1,GridBagConstraints.BOTH,0,0,0,0));
				}
			}
		}

		useAttributeFilter.addActionListener(e -> setEnabledAttributeFilter());
		useCounterFilter.addActionListener(e -> setEnabledCounterFilter());
		useBBoxFilter.addActionListener(e -> setEnabledBBoxFilter());
		useFeatureFilter.addActionListener(e -> setEnabledFeatureFilter());

		attributeFilterSeparator.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				useAttributeFilter.doClick();
			}
		});

		counterSeparator.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				useCounterFilter.doClick();
			}
		});

		bboxSeparator.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				useBBoxFilter.doClick();
			}
		});

		featureSeparator.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				useFeatureFilter.doClick();
			}
		});

		counterStartText.addPropertyChangeListener(e -> {
			if (counterStartText.getValue() != null && ((Number) counterStartText.getValue()).longValue() < 0)
				counterStartText.setValue(null);
		});

		counterEndText.addPropertyChangeListener(e -> {
			if (counterEndText.getValue() != null && ((Number) counterEndText.getValue()).longValue() < 0)
				counterEndText.setValue(null);
		});
		
		PopupMenuDecorator.getInstance().decorate(featureTree);
		PopupMenuDecorator.getInstance().decorate(gmlNameText, gmlIdText, counterStartText, counterEndText);
	}

	private void setEnabledAttributeFilter() {
		gmlIdLabel.setEnabled(useAttributeFilter.isSelected());
		gmlIdText.setEnabled(useAttributeFilter.isSelected());
		gmlNameLabel.setEnabled(useAttributeFilter.isSelected());
		gmlNameText.setEnabled(useAttributeFilter.isSelected());
	}

	private void setEnabledCounterFilter() {
		counterStartLabel.setEnabled(useCounterFilter.isSelected());
		counterEndLabel.setEnabled(useCounterFilter.isSelected());
		counterStartText.setEnabled(useCounterFilter.isSelected());
		counterEndText.setEnabled(useCounterFilter.isSelected());
	}

	private void setEnabledBBoxFilter() {
		bboxPanel.setEnabled(useBBoxFilter.isSelected());
	}

	private void setEnabledFeatureFilter() {
		featureTree.setPathsEnabled(useFeatureFilter.isSelected());
		featureTree.repaint();
	}

	public void doTranslation() {
		attributeFilterSeparator.setTitle(Language.I18N.getString("filter.border.attributes"));
		counterSeparator.setTitle(Language.I18N.getString("filter.border.counter"));
		bboxSeparator.setTitle(Language.I18N.getString("filter.border.boundingBox"));
		featureSeparator.setTitle(Language.I18N.getString("filter.border.featureClass"));

		gmlIdLabel.setText(Language.I18N.getString("filter.label.gmlId"));
		gmlNameLabel.setText(Language.I18N.getString("filter.label.gmlName"));
		counterStartLabel.setText(Language.I18N.getString("filter.label.counter.start"));
		counterEndLabel.setText(Language.I18N.getString("filter.label.counter.end"));
	}

	public void loadSettings() {
		ImportFilter filter = config.getProject().getImporter().getFilter();

		useAttributeFilter.setSelected(filter.isUseAttributeFilter());
		useCounterFilter.setSelected(filter.isUseCountFilter());
		useBBoxFilter.setSelected(filter.isUseBboxFilter());
		useFeatureFilter.setSelected(filter.isUseTypeNames());

		// gml:id filter
		ResourceIdOperator gmlIdFilter = filter.getAttributeFilter().getGmlIdFilter();
		gmlIdText.setText(String.join(",", gmlIdFilter.getResourceIds()));

		// gml:name
		LikeOperator gmlNameFilter = filter.getAttributeFilter().getGmlNameFilter();
		gmlNameText.setText(gmlNameFilter.getLiteral());

		// counter filter
		CounterFilter counterFilter = filter.getCounterFilter();
		counterStartText.setValue(counterFilter.getLowerLimit());
		counterEndText.setValue(counterFilter.getUpperLimit());

		// bbox filter
		BBOXOperator bboxFilter = filter.getBboxFilter();
		BoundingBox bbox = bboxFilter.getEnvelope();
		if (bbox != null)
			bboxPanel.setBoundingBox(bboxFilter.getEnvelope());

		// feature type filter
		FeatureTypeFilter featureTypeFilter = filter.getFeatureTypeFilter();
		featureTree.getCheckingModel().clearChecking();
		featureTree.setSelected(featureTypeFilter.getTypeNames());

		setEnabledAttributeFilter();
		setEnabledCounterFilter();
		setEnabledBBoxFilter();
		setEnabledFeatureFilter();
	}

	public void setSettings() {
		ImportFilter filter = config.getProject().getImporter().getFilter();

		filter.setUseAttributeFilter(useAttributeFilter.isSelected());
		filter.setUseCountFilter(useCounterFilter.isSelected());
		filter.setUseBboxFilter(useBBoxFilter.isSelected());
		filter.setUseTypeNames(useFeatureFilter.isSelected());

		// gml:id filter
		ResourceIdOperator gmlIdFilter = filter.getAttributeFilter().getGmlIdFilter();
		gmlIdFilter.reset();
		if (gmlIdText.getText().trim().length() > 0) {
			String trimmed = gmlIdText.getText().replaceAll("\\s+", "");
			gmlIdFilter.setResourceIds(Util.string2string(trimmed, ","));
		}

		// gml:name
		LikeOperator gmlNameFilter = filter.getAttributeFilter().getGmlNameFilter();
		gmlNameFilter.reset();
		if (gmlNameText.getText().trim().length() > 0)
			gmlNameFilter.setLiteral(gmlNameText.getText().trim());

		// counter filter
		CounterFilter counterFilter = filter.getCounterFilter();
		counterFilter.reset();
		if (counterStartText.isEditValid() && counterStartText.getValue() != null
				&& counterEndText.isEditValid() && counterEndText.getValue() != null) {
			counterFilter.setLowerLimit(((Number) counterStartText.getValue()).longValue());
			counterFilter.setUpperLimit(((Number) counterEndText.getValue()).longValue());
		} else {
			counterFilter.setLowerLimit(null);
			counterFilter.setUpperLimit(null);
		}

		// bbox filter
		filter.getBboxFilter().setEnvelope(bboxPanel.getBoundingBox());

		// feature type filter
		FeatureTypeFilter featureTypeFilter = filter.getFeatureTypeFilter();
		featureTypeFilter.reset();
		featureTypeFilter.setTypeNames(featureTree.getSelectedTypeNames());
	}

}
