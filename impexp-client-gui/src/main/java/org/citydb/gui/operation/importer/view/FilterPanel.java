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
package org.citydb.gui.operation.importer.view;

import org.citydb.config.Config;
import org.citydb.config.gui.importer.ImportGuiConfig;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.ImportFilter;
import org.citydb.config.project.importer.ImportIdList;
import org.citydb.config.project.importer.ImportIdListMode;
import org.citydb.config.project.importer.SimpleBBOXMode;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.operation.common.filter.*;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class FilterPanel extends JPanel {
	private final Config config;

	private JCheckBox useAttributeFilter;
	private JCheckBox useIdListFilter;
	private JCheckBox useCounterFilter;
	private JCheckBox useBBoxFilter;
	private JCheckBox useFeatureFilter;

	private TitledPanel attributeFilterPanel;
	private TitledPanel idListFilterPanel;
	private TitledPanel counterFilterPanel;
	private TitledPanel bboxFilterPanel;
	private TitledPanel featureFilterPanel;

	private AttributeFilterView attributeFilter;
	private IdListFilterView<ImportIdList> idListFilter;
	private CounterFilterView counterFilter;
	private BoundingBoxFilterView bboxFilter;
	private FeatureTypeFilterView featureTypeFilter;

	private JLabel idListMode;
	private JToggleButton idListImport;
	private JToggleButton idListSkip;
	private JRadioButton bboxOverlaps;
	private JRadioButton bboxWithin;

	public FilterPanel(ViewController viewController, Config config) {
		this.config = config;
		initGui(viewController);
	}

	private void initGui(ViewController viewController) {
		useAttributeFilter = new JCheckBox();
		useIdListFilter = new JCheckBox();
		useCounterFilter = new JCheckBox();
		useBBoxFilter = new JCheckBox();
		useFeatureFilter = new JCheckBox();

		idListMode = new JLabel();
		idListImport = new JToggleButton();
		idListSkip = new JToggleButton();

		ButtonGroup idListGroup = new ButtonGroup();
		idListGroup.add(idListImport);
		idListGroup.add(idListSkip);

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
			JToolBar toolBar = new JToolBar();
			toolBar.setBorder(BorderFactory.createEmptyBorder());
			toolBar.setFloatable(false);
			toolBar.add(idListImport);
			toolBar.add(idListSkip);
			toolBar.setEnabled(false);

			idListFilter = new IdListFilterView<>(viewController, ImportIdList::new)
					.addFooterRow(idListMode, toolBar);

			idListFilterPanel = new TitledPanel()
					.withIcon(idListFilter.getIcon())
					.withToggleButton(useIdListFilter)
					.withCollapseButton()
					.build(idListFilter.getViewComponent());

			add(idListFilterPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			counterFilter = new CounterFilterView();

			counterFilterPanel = new TitledPanel()
					.withIcon(counterFilter.getIcon())
					.withToggleButton(useCounterFilter)
					.withCollapseButton()
					.build(counterFilter.getViewComponent());

			add(counterFilterPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
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

			add(bboxFilterPanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			featureTypeFilter = new FeatureTypeFilterView();

			featureFilterPanel = new TitledPanel()
					.withIcon(featureTypeFilter.getIcon())
					.withToggleButton(useFeatureFilter)
					.withCollapseButton()
					.build(featureTypeFilter.getViewComponent());

			add(featureFilterPanel, GuiUtil.setConstraints(0, 4, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		useAttributeFilter.addItemListener(e -> setEnabledAttributeFilter());
		useIdListFilter.addItemListener(e -> setEnabledIdListFilter());
		useCounterFilter.addItemListener(e -> setEnabledCounterFilter());
		useBBoxFilter.addItemListener(e -> setEnabledBBoxFilter());
		useFeatureFilter.addItemListener(e -> setEnabledFeatureFilter());

		PopupMenuDecorator.getInstance().decorateTitledPanelGroup(attributeFilterPanel, idListFilterPanel,
				counterFilterPanel, bboxFilterPanel, featureFilterPanel);
	}

	private void setEnabledAttributeFilter() {
		attributeFilter.setEnabled(useAttributeFilter.isSelected());
	}

	private void setEnabledIdListFilter() {
		idListFilter.setEnabled(useIdListFilter.isSelected());
		idListMode.setEnabled(useIdListFilter.isSelected());
		idListImport.setEnabled(useIdListFilter.isSelected());
		idListSkip.setEnabled(useIdListFilter.isSelected());
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
		idListFilterPanel.setTitle(idListFilter.getLocalizedTitle());
		counterFilterPanel.setTitle(counterFilter.getLocalizedTitle());
		bboxFilterPanel.setTitle(bboxFilter.getLocalizedTitle());
		featureFilterPanel.setTitle(featureTypeFilter.getLocalizedTitle());

		idListMode.setText(Language.I18N.getString("import.idList.mode"));
		idListImport.setText(Language.I18N.getString("import.idList.mode.import"));
		idListSkip.setText(Language.I18N.getString("import.idList.mode.skip"));

		bboxOverlaps.setText(Language.I18N.getString("filter.label.boundingBox.overlaps"));
		bboxWithin.setText(Language.I18N.getString("filter.label.boundingBox.within"));

		attributeFilter.doTranslation();
		idListFilter.doTranslation();
		counterFilter.doTranslation();
		featureTypeFilter.doTranslation();
	}

	public void loadSettings() {
		ImportFilter filter = config.getImportConfig().getFilter();

		useAttributeFilter.setSelected(filter.isUseAttributeFilter());
		useIdListFilter.setSelected(filter.isUseIdListFilter());
		useCounterFilter.setSelected(filter.isUseCountFilter());
		useBBoxFilter.setSelected(filter.isUseBboxFilter());
		useFeatureFilter.setSelected(filter.isUseTypeNames());

		// ID list mode
		if (filter.getIdList().getMode() == ImportIdListMode.SKIP) {
			idListSkip.setSelected(true);
		} else {
			idListImport.setSelected(true);
		}

		// bbox filter
		if (filter.getBboxFilter().getMode() == SimpleBBOXMode.WITHIN) {
			bboxWithin.setSelected(true);
		} else {
			bboxOverlaps.setSelected(true);
		}

		attributeFilter.loadSettings(filter.getAttributeFilter());
		idListFilter.loadSettings(filter.getIdList());
		counterFilter.loadSettings(filter.getCounterFilter());
		bboxFilter.loadSettings(filter.getBboxFilter().getExtent());
		featureTypeFilter.loadSettings(filter.getFeatureTypeFilter());

		setEnabledAttributeFilter();
		setEnabledIdListFilter();
		setEnabledCounterFilter();
		setEnabledBBoxFilter();
		setEnabledFeatureFilter();

		// GUI specific settings
		ImportGuiConfig guiConfig = config.getGuiConfig().getImportGuiConfig();
		attributeFilterPanel.setCollapsed(guiConfig.isCollapseAttributeFilter());
		idListFilterPanel.setCollapsed(guiConfig.isCollapseIdListFilter());
		counterFilterPanel.setCollapsed(guiConfig.isCollapseCounterFilter());
		bboxFilterPanel.setCollapsed(guiConfig.isCollapseBoundingBoxFilter());
		featureFilterPanel.setCollapsed(guiConfig.isCollapseFeatureTypeFilter());
	}

	public void setSettings() {
		ImportFilter filter = config.getImportConfig().getFilter();

		filter.setUseAttributeFilter(useAttributeFilter.isSelected());
		filter.setUseIdListFilter(useIdListFilter.isSelected());
		filter.setUseCountFilter(useCounterFilter.isSelected());
		filter.setUseBboxFilter(useBBoxFilter.isSelected());
		filter.setUseTypeNames(useFeatureFilter.isSelected());

		// bbox filter
		filter.getBboxFilter().setMode(bboxOverlaps.isSelected() ?
				SimpleBBOXMode.BBOX :
				SimpleBBOXMode.WITHIN);

		filter.setAttributeFilter(attributeFilter.toSettings());
		filter.setIdList(idListFilter.toSettings());
		filter.setCounterFilter(counterFilter.toSettings());
		filter.getBboxFilter().setExtent(bboxFilter.toSettings());
		filter.setFeatureTypeFilter(featureTypeFilter.toSettings());

		// ID list mode
		filter.getIdList().setMode(idListSkip.isSelected() ?
				ImportIdListMode.SKIP :
				ImportIdListMode.IMPORT);

		// GUI specific settings
		ImportGuiConfig guiConfig = config.getGuiConfig().getImportGuiConfig();
		guiConfig.setCollapseAttributeFilter(attributeFilterPanel.isCollapsed());
		guiConfig.setCollapseIdListFilter(idListFilterPanel.isCollapsed());
		guiConfig.setCollapseCounterFilter(counterFilterPanel.isCollapsed());
		guiConfig.setCollapseBoundingBoxFilter(bboxFilterPanel.isCollapsed());
		guiConfig.setCollapseFeatureTypeFilter(featureFilterPanel.isCollapsed());
	}

	boolean checkIdListSettings() {
		return idListFilter.checkSettings(false);
	}
}
