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
package org.citydb.gui.operation.exporter.view;

import org.citydb.config.Config;
import org.citydb.config.gui.exporter.ExportGuiConfig;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.exporter.SimpleTiling;
import org.citydb.config.project.exporter.SimpleTilingMode;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.components.srs.SrsComboBox;
import org.citydb.gui.components.srs.SrsComboBoxFactory;
import org.citydb.gui.operation.common.filter.AttributeFilterView;
import org.citydb.gui.operation.common.filter.BoundingBoxFilterView;
import org.citydb.gui.operation.common.filter.CounterFilterView;
import org.citydb.gui.operation.common.filter.FeatureTypeFilterView;
import org.citydb.gui.operation.common.filter.FeatureVersionFilterView;
import org.citydb.gui.operation.common.filter.LodFilterView;
import org.citydb.gui.operation.common.filter.SQLFilterView;
import org.citydb.gui.operation.common.filter.XMLQueryView;
import org.citydb.gui.util.GuiUtil;
import org.citydb.core.plugin.extension.view.ViewController;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;

public class FilterPanel extends JPanel {
	private final Config config;

	private JPanel mainPanel;

	private JCheckBox useFeatureVersionFilter;
	private JCheckBox useAttributeFilter;
	private JCheckBox useSQLFilter;
	private JCheckBox useLodFilter;
	private JCheckBox useCounterFilter;
	private JCheckBox useBBoxFilter;
	private JCheckBox useFeatureFilter;

	private TitledPanel featureVersionPanel;
	private TitledPanel attributeFilterPanel;
	private TitledPanel sqlFilterPanel;
	private TitledPanel lodFilterPanel;
	private TitledPanel counterFilterPanel;
	private TitledPanel bboxFilterPanel;
	private TitledPanel featureFilterPanel;

	private FeatureVersionFilterView featureVersionFilter;
	private AttributeFilterView attributeFilter;
	private SQLFilterView sqlFilter;
	private LodFilterView lodFilter;
	private CounterFilterView counterFilter;
	private BoundingBoxFilterView bboxFilter;
	private FeatureTypeFilterView featureTypeFilter;
	private XMLQueryView xmlQuery;
	private JLabel srsComboBoxLabel;
	private SrsComboBox srsComboBox;

	private JRadioButton bboxOverlaps;
	private JRadioButton bboxWithin;
	private JRadioButton bboxTiling;
	private JFormattedTextField tilingRowsText;
	private JLabel tilingColumnsLabel;
	private JFormattedTextField tilingColumnsText;

	public FilterPanel(ViewController viewController, Config config) {
		this.config = config;
		initGui(viewController);
	}

	private void initGui(ViewController viewController) {
		useFeatureVersionFilter = new JCheckBox();
		useAttributeFilter = new JCheckBox();
		useSQLFilter = new JCheckBox();
		useLodFilter = new JCheckBox();
		useCounterFilter = new JCheckBox();
		useBBoxFilter = new JCheckBox();
		useFeatureFilter = new JCheckBox();

		srsComboBoxLabel = new JLabel();
		srsComboBox = SrsComboBoxFactory.getInstance().createSrsComboBox(true);
		srsComboBox.setShowOnlySameDimension(true);
		srsComboBox.setPreferredSize(new Dimension(50, srsComboBox.getPreferredSize().height));

		bboxOverlaps = new JRadioButton();
		bboxWithin = new JRadioButton();
		bboxTiling = new JRadioButton();
		tilingRowsText = new JFormattedTextField();
		tilingColumnsLabel = new JLabel();
		tilingColumnsText = new JFormattedTextField();

		ButtonGroup bboxModeGroup = new ButtonGroup();
		bboxModeGroup.add(bboxOverlaps);
		bboxModeGroup.add(bboxWithin);
		bboxModeGroup.add(bboxTiling);

		NumberFormatter tileFormat = new NumberFormatter(new DecimalFormat("#"));
		tileFormat.setMaximum(9999999);
		tileFormat.setMinimum(0);
		tilingRowsText = new JFormattedTextField(tileFormat);
		tilingColumnsText = new JFormattedTextField(tileFormat);

		mainPanel = new JPanel(new CardLayout());
		setLayout(new GridBagLayout());
		add(mainPanel, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));

		JPanel guiPanel = new JPanel();
		xmlQuery = new XMLQueryView(viewController)
				.withSimpleQuerySupplier(() -> config.getExportConfig().getSimpleQuery());
		mainPanel.add(guiPanel, "simple");
		mainPanel.add(xmlQuery.getViewComponent(), "advanced");

		guiPanel.setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(srsComboBoxLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				content.add(srsComboBox, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			guiPanel.add(content, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, TitledPanel.BOTTOM, 0));
		}
		{
			featureVersionFilter = new FeatureVersionFilterView();

			featureVersionPanel = new TitledPanel()
					.withIcon(featureVersionFilter.getIcon())
					.withToggleButton(useFeatureVersionFilter)
					.withCollapseButton()
					.build(featureVersionFilter.getViewComponent());

			guiPanel.add(featureVersionPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			attributeFilter = new AttributeFilterView()
					.withNameFilter()
					.withLineageFilter();

			attributeFilterPanel = new TitledPanel()
					.withIcon(attributeFilter.getIcon())
					.withToggleButton(useAttributeFilter)
					.withCollapseButton()
					.build(attributeFilter.getViewComponent());

			guiPanel.add(attributeFilterPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			sqlFilter = new SQLFilterView(() -> config.getGuiConfig().getExportGuiConfig().getSQLExportFilterComponent());

			sqlFilterPanel = new TitledPanel()
					.withIcon(sqlFilter.getIcon())
					.withToggleButton(useSQLFilter)
					.withCollapseButton()
					.build(sqlFilter.getViewComponent());

			guiPanel.add(sqlFilterPanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			lodFilter = new LodFilterView();

			lodFilterPanel = new TitledPanel()
					.withIcon(lodFilter.getIcon())
					.withToggleButton(useLodFilter)
					.withCollapseButton()
					.build(lodFilter.getViewComponent());

			guiPanel.add(lodFilterPanel, GuiUtil.setConstraints(0, 4, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			counterFilter = new CounterFilterView();

			counterFilterPanel = new TitledPanel()
					.withIcon(counterFilter.getIcon())
					.withToggleButton(useCounterFilter)
					.withCollapseButton()
					.build(counterFilter.getViewComponent());

			guiPanel.add(counterFilterPanel, GuiUtil.setConstraints(0, 5, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			bboxFilter = new BoundingBoxFilterView(viewController);

			JPanel bboxModePanel = new JPanel();
			bboxModePanel.setLayout(new GridBagLayout());
			{
				bboxModePanel.add(bboxOverlaps, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
				bboxModePanel.add(bboxWithin, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 20, 0, 0));
				bboxModePanel.add(bboxTiling, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 20, 0, 5));
				bboxModePanel.add(tilingRowsText, GuiUtil.setConstraints(3, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
				bboxModePanel.add(tilingColumnsLabel, GuiUtil.setConstraints(4, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, 0, 5));
				bboxModePanel.add(tilingColumnsText, GuiUtil.setConstraints(5, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
				bboxFilter.getViewComponent().addComponent(bboxModePanel, true);
			}

			bboxFilterPanel = new TitledPanel()
					.withIcon(bboxFilter.getIcon())
					.withToggleButton(useBBoxFilter)
					.withCollapseButton()
					.build(bboxFilter.getViewComponent());

			guiPanel.add(bboxFilterPanel, GuiUtil.setConstraints(0, 6, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			featureTypeFilter = new FeatureTypeFilterView(config.getExportConfig().getSimpleQuery().getVersion())
					.adaptToCityGMLVersionChange(v -> config.getExportConfig().getSimpleQuery().setFeatureTypeFilter(v));

			featureFilterPanel = new TitledPanel()
					.withIcon(featureTypeFilter.getIcon())
					.withToggleButton(useFeatureFilter)
					.withCollapseButton()
					.build(featureTypeFilter.getViewComponent());

			guiPanel.add(featureFilterPanel, GuiUtil.setConstraints(0, 7, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			guiPanel.add(Box.createVerticalGlue(), GuiUtil.setConstraints(0, 8, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		useFeatureVersionFilter.addActionListener(e -> setEnabledFeatureVersionFilter());
		useAttributeFilter.addItemListener(e -> setEnabledAttributeFilter());
		useSQLFilter.addItemListener(e -> setEnabledSQLFilter());
		useLodFilter.addItemListener(e -> setEnabledLodFilter());
		useCounterFilter.addItemListener(e -> setEnabledCounterFilter());
		useBBoxFilter.addItemListener(e -> setEnabledBBoxFilter());
		useFeatureFilter.addItemListener(e -> setEnabledFeatureFilter());

		bboxOverlaps.addActionListener(e -> setEnabledTilingOptions());
		bboxWithin.addActionListener(e -> setEnabledTilingOptions());
		bboxTiling.addActionListener(e -> setEnabledTilingOptions());

		xmlQuery.getViewComponent().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				setSimpleQuerySettings();
			}
		});

		PopupMenuDecorator.getInstance().decorate(tilingRowsText, tilingColumnsText);
		PopupMenuDecorator.getInstance().decorateTitledPanelGroup(featureVersionPanel, attributeFilterPanel,
				sqlFilterPanel, counterFilterPanel, lodFilterPanel, bboxFilterPanel, featureFilterPanel);
	}

	private void setEnabledFeatureVersionFilter() {
		featureVersionFilter.setEnabled(useFeatureVersionFilter.isSelected());
	}

	private void setEnabledLodFilter() {
		lodFilter.setEnabled(useLodFilter.isSelected());
	}

	private void setEnabledAttributeFilter() {
		attributeFilter.setEnabled(useAttributeFilter.isSelected());
	}

	private void setEnabledSQLFilter() {
		sqlFilter.setEnabled(useSQLFilter.isSelected());
	}

	private void setEnabledCounterFilter() {
		counterFilter.setEnabled(useCounterFilter.isSelected());
	}

	private void setEnabledBBoxFilter() {
		bboxFilter.setEnabled(useBBoxFilter.isSelected());
		bboxOverlaps.setEnabled(useBBoxFilter.isSelected());
		bboxWithin.setEnabled(useBBoxFilter.isSelected());
		bboxTiling.setEnabled(useBBoxFilter.isSelected());
		setEnabledTilingOptions();
	}

	private void setEnabledTilingOptions() {
		tilingRowsText.setEnabled(useBBoxFilter.isSelected() && bboxTiling.isSelected());
		tilingColumnsLabel.setEnabled(useBBoxFilter.isSelected() && bboxTiling.isSelected());
		tilingColumnsText.setEnabled(useBBoxFilter.isSelected() && bboxTiling.isSelected());
	}

	private void setEnabledFeatureFilter() {
		featureTypeFilter.setEnabled(useFeatureFilter.isSelected());
	}

	void showFilterDialog(boolean showSimple) {
		CardLayout layout = (CardLayout) mainPanel.getLayout();
		layout.show(mainPanel, showSimple ? "simple" : "advanced");
	}

	public void doTranslation() {
		featureVersionPanel.setTitle(featureVersionFilter.getLocalizedTitle());
		attributeFilterPanel.setTitle(attributeFilter.getLocalizedTitle());
		sqlFilterPanel.setTitle(sqlFilter.getLocalizedTitle());
		lodFilterPanel.setTitle(lodFilter.getLocalizedTitle());
		counterFilterPanel.setTitle(counterFilter.getLocalizedTitle());
		bboxFilterPanel.setTitle(bboxFilter.getLocalizedTitle());
		featureFilterPanel.setTitle(featureTypeFilter.getLocalizedTitle());

		srsComboBoxLabel.setText(Language.I18N.getString("export.label.targetSrs"));
		bboxOverlaps.setText(Language.I18N.getString("filter.label.boundingBox.overlaps"));
		bboxWithin.setText(Language.I18N.getString("filter.label.boundingBox.within"));
		bboxTiling.setText(Language.I18N.getString("filter.label.boundingBox.rows"));
		tilingColumnsLabel.setText(Language.I18N.getString("filter.label.boundingBox.columns"));

		featureVersionFilter.doTranslation();
		attributeFilter.doTranslation();
		sqlFilter.doTranslation();
		lodFilter.doTranslation();
		counterFilter.doTranslation();
		featureTypeFilter.doTranslation();
		xmlQuery.doTranslation();
	}

	public void loadSettings() {
		SimpleQuery query = config.getExportConfig().getSimpleQuery();

		useFeatureVersionFilter.setSelected(query.isUseFeatureVersionFilter());
		useAttributeFilter.setSelected(query.isUseAttributeFilter());
		useSQLFilter.setSelected(query.isUseSQLFilter());
		useLodFilter.setSelected(query.isUseLodFilter());
		useCounterFilter.setSelected(query.isUseCountFilter());
		useBBoxFilter.setSelected(query.isUseBboxFilter());
		useFeatureFilter.setSelected(query.isUseTypeNames());

		// target SRS
		srsComboBox.setSelectedItem(query.getTargetSrs());

		// bbox filter
		SimpleTiling tiling = query.getBboxFilter();
		if (tiling.getMode() == SimpleTilingMode.TILING) {
			bboxTiling.setSelected(true);
		} else if (tiling.getMode() == SimpleTilingMode.WITHIN) {
			bboxWithin.setSelected(true);
		} else {
			bboxOverlaps.setSelected(true);
		}

		// tiling options
		tilingRowsText.setValue(tiling.getRows());
		tilingColumnsText.setValue(tiling.getColumns());

		setEnabledFeatureVersionFilter();
		setEnabledAttributeFilter();
		setEnabledSQLFilter();
		setEnabledCounterFilter();
		setEnabledLodFilter();
		setEnabledBBoxFilter();
		setEnabledFeatureFilter();

		featureVersionFilter.loadSettings(query.getFeatureVersionFilter());
		attributeFilter.loadSettings(query.getAttributeFilter());
		sqlFilter.loadSettings(query.getSQLFilter());
		lodFilter.loadSettings(query.getLodFilter());
		counterFilter.loadSettings(query.getCounterFilter());
		bboxFilter.loadSettings(query.getBboxFilter().getExtent());
		featureTypeFilter.loadSettings(query.getFeatureTypeFilter());
		xmlQuery.loadSettings(config.getExportConfig().getQuery());

		// GUI specific settings
		ExportGuiConfig guiConfig = config.getGuiConfig().getExportGuiConfig();
		featureVersionPanel.setCollapsed(guiConfig.isCollapseFeatureVersionFilter());
		attributeFilterPanel.setCollapsed(guiConfig.isCollapseAttributeFilter());
		sqlFilterPanel.setCollapsed(guiConfig.isCollapseSQLFilter());
		lodFilterPanel.setCollapsed(guiConfig.isCollapseLodFilter());
		counterFilterPanel.setCollapsed(guiConfig.isCollapseCounterFilter());
		bboxFilterPanel.setCollapsed(guiConfig.isCollapseBoundingBoxFilter());
		featureFilterPanel.setCollapsed(guiConfig.isCollapseFeatureTypeFilter());
	}

	public void setSimpleQuerySettings() {
		SimpleQuery query = config.getExportConfig().getSimpleQuery();

		query.setUseFeatureVersionFilter(useFeatureVersionFilter.isSelected());
		query.setUseAttributeFilter(useAttributeFilter.isSelected());
		query.setUseSQLFilter(useSQLFilter.isSelected());
		query.setUseLodFilter(useLodFilter.isSelected());
		query.setUseCountFilter(useCounterFilter.isSelected());
		query.setUseBboxFilter(useBBoxFilter.isSelected());
		query.setUseTypeNames(useFeatureFilter.isSelected());

		// target SRS
		query.setTargetSrs(srsComboBox.getSelectedItem());

		// bbox filter
		SimpleTiling tiling = query.getBboxFilter();
		if (bboxTiling.isSelected()) {
			tiling.setMode(SimpleTilingMode.TILING);
		} else if (bboxWithin.isSelected()) {
			tiling.setMode(SimpleTilingMode.WITHIN);
		} else {
			tiling.setMode(SimpleTilingMode.BBOX);
		}

		// tiling options
		tiling.setRows(((Number) tilingRowsText.getValue()).intValue());
		tiling.setColumns(((Number) tilingColumnsText.getValue()).intValue());

		query.setFeatureVersionFilter(featureVersionFilter.toSettings());
		query.setAttributeFilter(attributeFilter.toSettings());
		query.setSQLFilter(sqlFilter.toSettings());
		query.setLodFilter(lodFilter.toSettings());
		query.setCounterFilter(counterFilter.toSettings());
		query.getBboxFilter().setExtent(bboxFilter.toSettings());
		query.setFeatureTypeFilter(featureTypeFilter.toSettings());

		// GUI specific settings
		ExportGuiConfig guiConfig = config.getGuiConfig().getExportGuiConfig();
		guiConfig.setCollapseFeatureVersionFilter(featureVersionPanel.isCollapsed());
		guiConfig.setCollapseAttributeFilter(attributeFilterPanel.isCollapsed());
		guiConfig.setCollapseSQLFilter(sqlFilterPanel.isCollapsed());
		guiConfig.setCollapseLodFilter(lodFilterPanel.isCollapsed());
		guiConfig.setCollapseCounterFilter(counterFilterPanel.isCollapsed());
		guiConfig.setCollapseBoundingBoxFilter(bboxFilterPanel.isCollapsed());
		guiConfig.setCollapseFeatureTypeFilter(featureFilterPanel.isCollapsed());
	}

	public void setSettings() {
		setSimpleQuerySettings();
		config.getExportConfig().setQuery(xmlQuery.toSettings());
	}
}
