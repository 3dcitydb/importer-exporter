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
package org.citydb.gui.modules.importer.view;

import org.citydb.config.Config;
import org.citydb.config.gui.importer.ImportGuiConfig;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.ImportFilter;
import org.citydb.config.project.importer.SimpleBBOXMode;
import org.citydb.config.project.importer.SimpleBBOXOperator;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.filter.AttributeFilterView;
import org.citydb.gui.modules.common.filter.BoundingBoxFilterView;
import org.citydb.gui.modules.common.filter.CounterFilterView;
import org.citydb.gui.modules.common.filter.FeatureTypeFilterView;
import org.citydb.gui.util.GuiUtil;
import org.citydb.plugin.extension.view.ViewController;

import javax.swing.*;
import java.awt.*;

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

	private AttributeFilterView attributeFilter;
	private CounterFilterView counterFilter;
	private BoundingBoxFilterView bboxFilter;
	private FeatureTypeFilterView featureTypeFilter;

	private JRadioButton bboxOverlaps;
	private JRadioButton bboxWithin;

	public FilterPanel(ViewController viewController, Config config) {
		this.config = config;
		initGui(viewController);
	}

	private void initGui(ViewController viewController) {
		useAttributeFilter = new JCheckBox();
		useCounterFilter = new JCheckBox();
		useBBoxFilter = new JCheckBox();
		useFeatureFilter = new JCheckBox();

		bboxOverlaps = new JRadioButton();
		bboxWithin = new JRadioButton();

		ButtonGroup bboxModeGroup = new ButtonGroup();
		bboxModeGroup.add(bboxOverlaps);
		bboxModeGroup.add(bboxWithin);

		// layout
		setLayout(new GridBagLayout());
		{
			attributeFilter = new AttributeFilterView()
					.withNameFilter();

			attributeFilterPanel = new TitledPanel()
					.withIcon(attributeFilter.getIcon())
					.withToggleButton(useAttributeFilter)
					.withCollapseButton()
					.build(attributeFilter.getViewComponent());

			add(attributeFilterPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			counterFilter = new CounterFilterView();

			counterFilterPanel = new TitledPanel()
					.withIcon(counterFilter.getIcon())
					.withToggleButton(useCounterFilter)
					.withCollapseButton()
					.build(counterFilter.getViewComponent());

			add(counterFilterPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			bboxFilter = new BoundingBoxFilterView(viewController);

			JPanel bboxModePanel = new JPanel();
			bboxModePanel.setLayout(new GridBagLayout());
			{
				bboxModePanel.add(bboxOverlaps, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
				bboxModePanel.add(bboxWithin, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 20, 0, 0));
				bboxFilter.getViewComponent().addComponent(bboxModePanel, true);
			}

			bboxFilterPanel = new TitledPanel()
					.withIcon(bboxFilter.getIcon())
					.withToggleButton(useBBoxFilter)
					.withCollapseButton()
					.build(bboxFilter.getViewComponent());

			add(bboxFilterPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			featureTypeFilter = new FeatureTypeFilterView();

			featureFilterPanel = new TitledPanel()
					.withIcon(featureTypeFilter.getIcon())
					.withToggleButton(useFeatureFilter)
					.withCollapseButton()
					.build(featureTypeFilter.getViewComponent());

			add(featureFilterPanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		useAttributeFilter.addItemListener(e -> setEnabledAttributeFilter());
		useCounterFilter.addItemListener(e -> setEnabledCounterFilter());
		useBBoxFilter.addItemListener(e -> setEnabledBBoxFilter());
		useFeatureFilter.addItemListener(e -> setEnabledFeatureFilter());

		PopupMenuDecorator.getInstance().decorateTitledPanelGroup(attributeFilterPanel, counterFilterPanel,
				bboxFilterPanel, featureFilterPanel);
	}

	private void setEnabledAttributeFilter() {
		attributeFilter.setEnabled(useAttributeFilter.isSelected());
	}

	private void setEnabledCounterFilter() {
		counterFilter.setEnabled(useCounterFilter.isSelected());
	}

	private void setEnabledBBoxFilter() {
		bboxFilter.setEnabled(useBBoxFilter.isSelected());
		bboxOverlaps.setEnabled(useBBoxFilter.isSelected());
		bboxWithin.setEnabled(useBBoxFilter.isSelected());
	}

	private void setEnabledFeatureFilter() {
		featureTypeFilter.setEnabled(useFeatureFilter.isSelected());
	}

	public void doTranslation() {
		attributeFilterPanel.setTitle(attributeFilter.getLocalizedTitle());
		counterFilterPanel.setTitle(counterFilter.getLocalizedTitle());
		bboxFilterPanel.setTitle(bboxFilter.getLocalizedTitle());
		featureFilterPanel.setTitle(featureTypeFilter.getLocalizedTitle());

		bboxOverlaps.setText(Language.I18N.getString("filter.label.boundingBox.overlaps"));
		bboxWithin.setText(Language.I18N.getString("filter.label.boundingBox.within"));

		attributeFilter.doTranslation();
		counterFilter.doTranslation();
		featureTypeFilter.doTranslation();
	}

	public void loadSettings() {
		ImportFilter filter = config.getImportConfig().getFilter();

		useAttributeFilter.setSelected(filter.isUseAttributeFilter());
		useCounterFilter.setSelected(filter.isUseCountFilter());
		useBBoxFilter.setSelected(filter.isUseBboxFilter());
		useFeatureFilter.setSelected(filter.isUseTypeNames());

		// bbox filter
		SimpleBBOXOperator bboxOperator = filter.getBboxFilter();
		if (bboxOperator.getMode() == SimpleBBOXMode.WITHIN) {
			bboxWithin.setSelected(true);
		} else {
			bboxOverlaps.setSelected(true);
		}

		setEnabledAttributeFilter();
		setEnabledCounterFilter();
		setEnabledBBoxFilter();
		setEnabledFeatureFilter();

		attributeFilter.loadSettings(filter.getAttributeFilter());
		counterFilter.loadSettings(filter.getCounterFilter());
		bboxFilter.loadSettings(filter.getBboxFilter().getExtent());
		featureTypeFilter.loadSettings(filter.getFeatureTypeFilter());

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

		// bbox filter
		filter.getBboxFilter().setMode(bboxOverlaps.isSelected() ? SimpleBBOXMode.BBOX : SimpleBBOXMode.WITHIN);

		filter.setAttributeFilter(attributeFilter.toSettings());
		filter.setCounterFilter(counterFilter.toSettings());
		filter.getBboxFilter().setExtent(bboxFilter.toSettings());
		filter.setFeatureTypeFilter(featureTypeFilter.toSettings());

		// GUI specific settings
		ImportGuiConfig guiConfig = config.getGuiConfig().getImportGuiConfig();
		guiConfig.setCollapseAttributeFilter(attributeFilterPanel.isCollapsed());
		guiConfig.setCollapseCounterFilter(counterFilterPanel.isCollapsed());
		guiConfig.setCollapseBoundingBoxFilter(bboxFilterPanel.isCollapsed());
		guiConfig.setCollapseFeatureTypeFilter(featureFilterPanel.isCollapsed());
	}

}
