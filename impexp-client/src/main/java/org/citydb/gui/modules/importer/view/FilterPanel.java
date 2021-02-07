/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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
package org.citydb.gui.modules.importer.view;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.gui.importer.ImportGuiConfig;
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
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.components.feature.FeatureTypeTree;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;
import org.citydb.util.Util;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class FilterPanel extends JPanel {
	private final Config config;

	private JCheckBox useAttributeFilter;
	private JCheckBox useCounterFilter;
	private JCheckBox useBBoxFilter;
	private JCheckBox useFeatureFilter;

	private TitledPanel attributeFilterPanel;
	private TitledPanel counterFilterPanel;
	private TitledPanel bboxFilterPanel;
	private TitledPanel featureFilterPanel;

	private JLabel resourceIdLabel;
	private JTextField resourceIdText;
	private JLabel nameLabel;
	private JTextField nameText;

	private JLabel countLabel;
	private JLabel startIndexLabel;
	private JFormattedTextField countText;
	private JFormattedTextField startIndexText;

	private BoundingBoxPanel bboxPanel;
	private JRadioButton bboxOverlaps;
	private JRadioButton bboxWithin;
	private FeatureTypeTree featureTree;
	private JPanel featureTreePanel;

	public FilterPanel(ViewController viewController, Config config) {
		this.config = config;

		initGui(viewController);
	}

	private void initGui(ViewController viewController) {
		useAttributeFilter = new JCheckBox();
		useCounterFilter = new JCheckBox();
		useBBoxFilter = new JCheckBox();
		useFeatureFilter = new JCheckBox();

		resourceIdLabel = new JLabel();
		resourceIdText = new JTextField();
		nameLabel = new JLabel();
		nameText = new JTextField();

		countLabel = new JLabel();
		startIndexLabel = new JLabel();

		BlankNumberFormatter countFormatter = new BlankNumberFormatter(new DecimalFormat("#"));
		countFormatter.setLimits(0L, Long.MAX_VALUE);
		countText = new JFormattedTextField(countFormatter);
		countText.setColumns(10);

		BlankNumberFormatter startIndexFormatter = new BlankNumberFormatter(new DecimalFormat("#"));
		startIndexFormatter.setLimits(0L, Long.MAX_VALUE);
		startIndexText = new JFormattedTextField(startIndexFormatter);
		startIndexText.setColumns(10);

		bboxPanel = viewController.getComponentFactory().createBoundingBoxPanel();
		bboxOverlaps = new JRadioButton();
		bboxWithin = new JRadioButton();

		ButtonGroup bboxModeGroup = new ButtonGroup();
		bboxModeGroup.add(bboxOverlaps);
		bboxModeGroup.add(bboxWithin);

		featureTree = new FeatureTypeTree();
		featureTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 1);

		// get rid of standard icons
		DefaultCheckboxTreeCellRenderer renderer = (DefaultCheckboxTreeCellRenderer) featureTree.getCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);

		// layout
		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(resourceIdLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
				content.add(resourceIdText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
				content.add(nameLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				content.add(nameText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			}

			attributeFilterPanel = new TitledPanel()
					.withIcon(new FlatSVGIcon("org/citydb/gui/filter/attribute.svg"))
					.withToggleButton(useAttributeFilter)
					.withCollapseButton()
					.build(content);

			add(attributeFilterPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(countLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 5));
				content.add(countText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
				content.add(startIndexLabel, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.NONE, 0, 10, 0, 5));
				content.add(startIndexText, GuiUtil.setConstraints(3, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			}

			counterFilterPanel = new TitledPanel()
					.withIcon(new FlatSVGIcon("org/citydb/gui/filter/counter.svg"))
					.withToggleButton(useCounterFilter)
					.withCollapseButton()
					.build(content);

			add(counterFilterPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			JPanel bboxModePanel = new JPanel();
			bboxModePanel.setLayout(new GridBagLayout());
			{
				bboxModePanel.add(bboxOverlaps, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
				bboxModePanel.add(bboxWithin, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 25, 0, 0));
				bboxPanel.addComponent(bboxModePanel, true);
			}

			bboxFilterPanel = new TitledPanel()
					.withIcon(new FlatSVGIcon("org/citydb/gui/filter/bbox.svg"))
					.withToggleButton(useBBoxFilter)
					.withCollapseButton()
					.build(bboxPanel);

			add(bboxFilterPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			featureTreePanel = new JPanel();
			featureTreePanel.setLayout(new GridBagLayout());
			{
				featureTreePanel.add(featureTree, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
			}

			featureFilterPanel = new TitledPanel()
					.withIcon(new FlatSVGIcon("org/citydb/gui/filter/featureType.svg"))
					.withToggleButton(useFeatureFilter)
					.withCollapseButton()
					.build(featureTreePanel);

			add(featureFilterPanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		useAttributeFilter.addItemListener(e -> setEnabledAttributeFilter());
		useCounterFilter.addItemListener(e -> setEnabledCounterFilter());
		useBBoxFilter.addItemListener(e -> setEnabledBBoxFilter());
		useFeatureFilter.addItemListener(e -> setEnabledFeatureFilter());

		PopupMenuDecorator.getInstance().decorate(featureTree, nameText, resourceIdText, countText, startIndexText);
		PopupMenuDecorator.getInstance().decorateTitledPanelGroup(attributeFilterPanel, counterFilterPanel,
				bboxFilterPanel, featureFilterPanel);

		UIManager.addPropertyChangeListener(e -> {
			if ("lookAndFeel".equals(e.getPropertyName())) {
				SwingUtilities.invokeLater(this::updateComponentUI);
			}
		});

		updateComponentUI();
	}

	private void updateComponentUI() {
		featureTreePanel.setBorder(UIManager.getBorder("ScrollPane.border"));
	}

	private void setEnabledAttributeFilter() {
		resourceIdLabel.setEnabled(useAttributeFilter.isSelected());
		resourceIdText.setEnabled(useAttributeFilter.isSelected());
		nameLabel.setEnabled(useAttributeFilter.isSelected());
		nameText.setEnabled(useAttributeFilter.isSelected());
	}

	private void setEnabledCounterFilter() {
		countLabel.setEnabled(useCounterFilter.isSelected());
		startIndexLabel.setEnabled(useCounterFilter.isSelected());
		countText.setEnabled(useCounterFilter.isSelected());
		startIndexText.setEnabled(useCounterFilter.isSelected());
	}

	private void setEnabledBBoxFilter() {
		bboxPanel.setEnabled(useBBoxFilter.isSelected());
		bboxOverlaps.setEnabled(useBBoxFilter.isSelected());
		bboxWithin.setEnabled(useBBoxFilter.isSelected());
	}

	private void setEnabledFeatureFilter() {
		if (useFeatureFilter.isSelected()) {
			featureTree.expandRow(0);
		} else {
			featureTree.collapseRow(0);
			featureTree.setSelectionPath(null);
		}

		featureTree.setEnabled(useFeatureFilter.isSelected());
	}

	public void doTranslation() {
		attributeFilterPanel.setTitle(Language.I18N.getString("filter.border.attributes"));
		counterFilterPanel.setTitle(Language.I18N.getString("filter.border.counter"));
		bboxFilterPanel.setTitle(Language.I18N.getString("filter.border.boundingBox"));
		featureFilterPanel.setTitle(Language.I18N.getString("filter.border.featureClass"));

		resourceIdLabel.setText(Language.I18N.getString("filter.label.id"));
		nameLabel.setText(Language.I18N.getString("filter.label.name"));
		countLabel.setText(Language.I18N.getString("filter.label.counter.count"));
		startIndexLabel.setText(Language.I18N.getString("filter.label.counter.startIndex"));
		bboxOverlaps.setText(Language.I18N.getString("filter.label.boundingBox.overlaps"));
		bboxWithin.setText(Language.I18N.getString("filter.label.boundingBox.within"));
	}

	public void loadSettings() {
		ImportFilter filter = config.getImportConfig().getFilter();

		useAttributeFilter.setSelected(filter.isUseAttributeFilter());
		useCounterFilter.setSelected(filter.isUseCountFilter());
		useBBoxFilter.setSelected(filter.isUseBboxFilter());
		useFeatureFilter.setSelected(filter.isUseTypeNames());

		// resource id filter
		ResourceIdOperator resourceIdFilter = filter.getAttributeFilter().getResourceIdFilter();
		resourceIdText.setText(String.join(",", resourceIdFilter.getResourceIds()));

		// name filter
		LikeOperator nameFilter = filter.getAttributeFilter().getNameFilter();
		nameText.setText(nameFilter.getLiteral());

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

		// GUI specific settings
		ImportGuiConfig guiConfig = config.getGuiConfig().getImportGuiConfig();
		attributeFilterPanel.setCollapsed(guiConfig.isCollapseAttributeFilter());
		counterFilterPanel.setCollapsed(guiConfig.isCollapseCounterFilter());
		bboxFilterPanel.setCollapsed(guiConfig.isCollapseBoundingBoxFilter());
		featureFilterPanel.setCollapsed(guiConfig.isCollapseFeatureTypeFilter());
	}

	public void setSettings() {
		ImportFilter filter = config.getImportConfig().getFilter();

		filter.setUseAttributeFilter(useAttributeFilter.isSelected());
		filter.setUseCountFilter(useCounterFilter.isSelected());
		filter.setUseBboxFilter(useBBoxFilter.isSelected());
		filter.setUseTypeNames(useFeatureFilter.isSelected());

		// resource id filter
		ResourceIdOperator resourceIdFilter = filter.getAttributeFilter().getResourceIdFilter();
		resourceIdFilter.reset();
		if (resourceIdText.getText().trim().length() > 0) {
			String trimmed = resourceIdText.getText().replaceAll("\\s+", "");
			resourceIdFilter.setResourceIds(Util.string2string(trimmed, ","));
		}

		// name filter
		LikeOperator nameFilter = filter.getAttributeFilter().getNameFilter();
		nameFilter.reset();
		if (nameText.getText().trim().length() > 0)
			nameFilter.setLiteral(nameText.getText().trim());

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

		// GUI specific settings
		ImportGuiConfig guiConfig = config.getGuiConfig().getImportGuiConfig();
		guiConfig.setCollapseAttributeFilter(attributeFilterPanel.isCollapsed());
		guiConfig.setCollapseCounterFilter(counterFilterPanel.isCollapsed());
		guiConfig.setCollapseBoundingBoxFilter(bboxFilterPanel.isCollapsed());
		guiConfig.setCollapseFeatureTypeFilter(featureFilterPanel.isCollapsed());
	}

}
