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
package org.citydb.modules.citygml.importer.gui.view;

import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.ImportFilter;
import org.citydb.config.project.importer.SimpleBBOXMode;
import org.citydb.config.project.importer.SimpleBBOXOperator;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import org.citydb.gui.components.common.BlankNumberFormatter;
import org.citydb.gui.components.feature.FeatureTypeTree;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;
import org.citydb.util.Util;
import org.jdesktop.swingx.JXTitledSeparator;

import javax.swing.*;
import java.awt.*;
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

	private JLabel countLabel;
	private JLabel startIndexLabel;
	private JFormattedTextField countText;
	private JFormattedTextField startIndexText;

	private BoundingBoxPanel bboxPanel;
	private JLabel bboxMode;
	private JRadioButton bboxOverlaps;
	private JRadioButton bboxWithin;
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

		countLabel = new JLabel();
		startIndexLabel = new JLabel();

		BlankNumberFormatter countFormatter = new BlankNumberFormatter(new DecimalFormat("##########"));
		countFormatter.setLimits(0, Integer.MAX_VALUE);
		countText = new JFormattedTextField(countFormatter);
		countText.setColumns(10);

		BlankNumberFormatter startIndexFormatter = new BlankNumberFormatter(new DecimalFormat("###################"));
		startIndexFormatter.setLimits(0L, Long.MAX_VALUE);
		startIndexText = new JFormattedTextField(startIndexFormatter);
		startIndexText.setColumns(10);

		bboxPanel = viewController.getComponentFactory().createBoundingBoxPanel();
		bboxMode = new JLabel();
		bboxOverlaps = new JRadioButton();
		bboxWithin = new JRadioButton();

		ButtonGroup bboxModeGroup = new ButtonGroup();
		bboxModeGroup.add(bboxOverlaps);
		bboxModeGroup.add(bboxWithin);

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
					counterPanel.add(countLabel, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,0,0,0,5));
					counterPanel.add(countText, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,5));
					counterPanel.add(startIndexLabel, GuiUtil.setConstraints(2,0,0,0,GridBagConstraints.NONE,0,10,0,5));
					counterPanel.add(startIndexText, GuiUtil.setConstraints(3,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,0));
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
				bboxFilterRow.add(bboxPanel, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,0,0,0,5));
			}

			JPanel bboxModePanel = new JPanel();
			bboxModePanel.setLayout(new GridBagLayout());
			{
				bboxOverlaps.setIconTextGap(10);
				bboxWithin.setIconTextGap(10);
				bboxModePanel.add(bboxMode, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.HORIZONTAL,0,0,0,5));
				bboxModePanel.add(bboxOverlaps, GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.HORIZONTAL,0,15,0,5));
				bboxModePanel.add(bboxWithin, GuiUtil.setConstraints(2,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,0));
				bboxPanel.addComponent(bboxModePanel);
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

		countText.addPropertyChangeListener(e -> {
			if (countText.getValue() != null && ((Number) countText.getValue()).longValue() < 0)
				countText.setValue(null);
		});

		startIndexText.addPropertyChangeListener(e -> {
			if (startIndexText.getValue() != null && ((Number) startIndexText.getValue()).longValue() < 0)
				startIndexText.setValue(null);
		});
		
		PopupMenuDecorator.getInstance().decorate(featureTree);
		PopupMenuDecorator.getInstance().decorate(gmlNameText, gmlIdText, countText, startIndexText);
	}

	private void setEnabledAttributeFilter() {
		gmlIdLabel.setEnabled(useAttributeFilter.isSelected());
		gmlIdText.setEnabled(useAttributeFilter.isSelected());
		gmlNameLabel.setEnabled(useAttributeFilter.isSelected());
		gmlNameText.setEnabled(useAttributeFilter.isSelected());
	}

	private void setEnabledCounterFilter() {
		countLabel.setEnabled(useCounterFilter.isSelected());
		startIndexLabel.setEnabled(useCounterFilter.isSelected());
		countText.setEnabled(useCounterFilter.isSelected());
		startIndexText.setEnabled(useCounterFilter.isSelected());
	}

	private void setEnabledBBoxFilter() {
		bboxPanel.setEnabled(useBBoxFilter.isSelected());
		bboxMode.setEnabled(useBBoxFilter.isSelected());
		bboxOverlaps.setEnabled(useBBoxFilter.isSelected());
		bboxWithin.setEnabled(useBBoxFilter.isSelected());
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
		countLabel.setText(Language.I18N.getString("filter.label.counter.count"));
		startIndexLabel.setText(Language.I18N.getString("filter.label.counter.startIndex"));
		bboxMode.setText(Language.I18N.getString("filter.label.boundingBox.mode"));
		bboxOverlaps.setText(Language.I18N.getString("filter.label.boundingBox.overlaps"));
		bboxWithin.setText(Language.I18N.getString("filter.label.boundingBox.within"));
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
		countText.setValue(counterFilter.getCount());
		startIndexText.setValue(counterFilter.getStartIndex());

		// bbox filter
		SimpleBBOXOperator bboxFilter = filter.getBboxFilter();
		BoundingBox bbox = bboxFilter.getExtent();
		if (bbox != null)
			bboxPanel.setBoundingBox(bboxFilter.getExtent());

		if (bboxFilter.getMode() == SimpleBBOXMode.WITHIN)
			bboxWithin.setSelected(true);
		else
			bboxOverlaps.setSelected(true);

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
		if (countText.isEditValid() && countText.getValue() != null)
			counterFilter.setCount(((Number) countText.getValue()).longValue());
		else
			counterFilter.setCount(null);

		if (startIndexText.isEditValid() && startIndexText.getValue() != null)
			counterFilter.setStartIndex(((Number) startIndexText.getValue()).longValue());
		else
			counterFilter.setStartIndex(null);

		// bbox filter
		filter.getBboxFilter().setExtent(bboxPanel.getBoundingBox());
		filter.getBboxFilter().setMode(bboxOverlaps.isSelected() ? SimpleBBOXMode.BBOX : SimpleBBOXMode.WITHIN);

		// feature type filter
		FeatureTypeFilter featureTypeFilter = filter.getFeatureTypeFilter();
		featureTypeFilter.reset();
		featureTypeFilter.setTypeNames(featureTree.getSelectedTypeNames());
	}

}
