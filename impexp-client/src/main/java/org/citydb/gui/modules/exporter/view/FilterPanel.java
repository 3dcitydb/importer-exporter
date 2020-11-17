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
package org.citydb.gui.modules.exporter.view;

import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.exporter.SimpleTiling;
import org.citydb.config.project.exporter.SimpleTilingMode;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.lod.LodFilterMode;
import org.citydb.config.project.query.filter.lod.LodSearchMode;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.event.global.PropertyChangeEvent;
import org.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import org.citydb.gui.components.common.BlankNumberFormatter;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.components.feature.FeatureTypeTree;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.factory.SrsComboBoxFactory;
import org.citydb.gui.modules.exporter.view.filter.AttributeFilterView;
import org.citydb.gui.modules.exporter.view.filter.FilterView;
import org.citydb.gui.modules.exporter.view.filter.SQLFilterView;
import org.citydb.gui.modules.exporter.view.filter.XMLQueryView;
import org.citydb.gui.util.GuiUtil;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("serial")
public class FilterPanel extends JPanel implements EventHandler {
	private final Config config;

	private JPanel mainPanel;

	private JCheckBox useSelectionFilter;
	private JCheckBox useLodFilter;
	private JCheckBox useCounterFilter;
	private JCheckBox useBBoxFilter;
	private JCheckBox useFeatureFilter;

	private TitledPanel lodFilterPanel;
	private TitledPanel counterFilterPanel;
	private TitledPanel bboxFilterPanel;
	private TitledPanel featureFilterPanel;

	private XMLQueryView xmlQuery;
	private JTabbedPane filterTab;
	private FilterView[] filters;

	private JLabel srsComboBoxLabel;
	private SrsComboBoxFactory.SrsComboBox srsComboBox;

	private JLabel countLabel;
	private JLabel startIndexLabel;
	private JFormattedTextField countText;
	private JFormattedTextField startIndexText;

	private JCheckBox[] lods;
	private JLabel lodModeLabel;
	private JComboBox<LodFilterMode> lodMode;
	private JLabel lodDepthLabel;
	private JSpinner lodDepth;

	private BoundingBoxPanel bboxPanel;
	private JLabel bboxMode;
	private JRadioButton bboxOverlaps;
	private JRadioButton bboxWithin;
	private JRadioButton bboxTiling;
	private JLabel tilingRowsLabel;
	private JFormattedTextField tilingRowsText;
	private JLabel tilingColumnsLabel;
	private JFormattedTextField tilingColumnsText;

	private FeatureTypeTree featureTree;

	public FilterPanel(ViewController viewController, Config config) {
		this.config = config;

		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.PROPERTY_CHANGE_EVENT, this);		
		initGui(viewController);
	}

	private void initGui(ViewController viewController) {
		useSelectionFilter = new JCheckBox();
		useCounterFilter = new JCheckBox();
		useLodFilter = new JCheckBox();
		useBBoxFilter = new JCheckBox();
		useFeatureFilter = new JCheckBox();

		counterFilterPanel = new TitledPanel().withToggleButton(useCounterFilter);
		lodFilterPanel = new TitledPanel().withToggleButton(useLodFilter);
		bboxFilterPanel = new TitledPanel().withToggleButton(useBBoxFilter);
		featureFilterPanel = new TitledPanel().withToggleButton(useFeatureFilter);

		srsComboBoxLabel = new JLabel();
		srsComboBox = SrsComboBoxFactory.getInstance().createSrsComboBox(true);
		srsComboBox.setShowOnlySameDimension(true);
		srsComboBox.setPreferredSize(new Dimension(50, srsComboBox.getPreferredSize().height));

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

		lodModeLabel = new JLabel();
		lodDepthLabel = new JLabel();
		lodDepth = new JSpinner(new SpinnerListModel(Stream.concat(Stream.of("*"),
				IntStream.rangeClosed(0, 10).mapToObj(String::valueOf)).collect(Collectors.toList())));

		lodMode = new JComboBox<>();
		for (LodFilterMode mode : LodFilterMode.values())
			lodMode.addItem(mode);

		bboxPanel = viewController.getComponentFactory().createBoundingBoxPanel();
		bboxMode = new JLabel();
		bboxOverlaps = new JRadioButton();
		bboxWithin = new JRadioButton();
		bboxTiling = new JRadioButton();
		tilingRowsLabel = new JLabel();
		tilingRowsText = new JFormattedTextField();
		tilingColumnsLabel = new JLabel();
		tilingColumnsText = new JFormattedTextField();

		ButtonGroup bboxModeGroup = new ButtonGroup();
		bboxModeGroup.add(bboxOverlaps);
		bboxModeGroup.add(bboxWithin);
		bboxModeGroup.add(bboxTiling);

		BlankNumberFormatter tileFormat = new BlankNumberFormatter(new DecimalFormat("#######"));
		tileFormat.setLimits(0, 9999999);
		tilingRowsText = new JFormattedTextField(tileFormat);
		tilingColumnsText = new JFormattedTextField(tileFormat);

		featureTree = new FeatureTypeTree(Util.toCityGMLVersion(config.getExportConfig().getSimpleQuery().getVersion()));
		featureTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 1);

		// get rid of standard icons
		DefaultCheckboxTreeCellRenderer renderer = (DefaultCheckboxTreeCellRenderer) featureTree.getCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);

		//layout
		mainPanel = new JPanel(new CardLayout());
		setLayout(new GridBagLayout());
		add(mainPanel, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));

		JPanel guiPanel = new JPanel();
		xmlQuery = new XMLQueryView(this, viewController, config);
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
			JPanel filterRow = new JPanel();
			filterRow.setLayout(new GridBagLayout());
			{
				filterTab = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

				// adapt checkbox to tab height
				useSelectionFilter.setMargin(new Insets(0, 0, 0, 0));
				int right = useSelectionFilter.getIconTextGap();
				int top = (UIManager.getInt("TabbedPane.tabHeight") - useSelectionFilter.getPreferredSize().height) / 2;

				Component toogleButton;
				if (top > 0) {
					toogleButton = Box.createVerticalBox();
					((Box) toogleButton).add(Box.createVerticalStrut(top));
					((Box) toogleButton).add(useSelectionFilter);
				} else {
					toogleButton = useSelectionFilter;
				}

				filterRow.add(toogleButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0, 0, 0, right));
				filterRow.add(filterTab, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));

				filters = new FilterView[]{
						new AttributeFilterView(config),
						new SQLFilterView(config)
				};

				for (int i = 0; i < filters.length; ++i)
					filterTab.insertTab(null, filters[i].getIcon(), null, filters[i].getToolTip(), i);

				filterTab.addChangeListener(e -> {
					int index = filterTab.getSelectedIndex();
					for (int i = 0; i < filterTab.getTabCount(); i++)
						filterTab.setComponentAt(i, index == i ? filters[index].getViewComponent() : null);
				});
			}

			guiPanel.add(filterRow, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, TitledPanel.BOTTOM, 0));
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				lods = new JCheckBox[5];
				for (int lod = 0; lod < lods.length; lod++) {
					lods[lod] = new JCheckBox("LoD" + lod);
					content.add(lods[lod], GuiUtil.setConstraints(lod, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 15));
				}

				content.add(lodModeLabel, GuiUtil.setConstraints(5, 0, 0, 0, GridBagConstraints.NONE, 0, 20, 0, 5));
				content.add(lodMode, GuiUtil.setConstraints(6, 0, 0.5, 1, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
				content.add(lodDepthLabel, GuiUtil.setConstraints(7, 0, 0, 0, GridBagConstraints.NONE, 0, 20, 0, 5));
				content.add(lodDepth, GuiUtil.setConstraints(8, 0, 0.5, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			}

			lodFilterPanel.build(content);
			guiPanel.add(lodFilterPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
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

			counterFilterPanel.build(content);
			guiPanel.add(counterFilterPanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			JPanel bboxModePanel = new JPanel();
			bboxModePanel.setLayout(new GridBagLayout());
			{
				bboxModePanel.add(bboxMode, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				bboxModePanel.add(bboxOverlaps, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 15, 0, 5));
				bboxModePanel.add(bboxWithin, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
				bboxModePanel.add(bboxTiling, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
				bboxModePanel.add(tilingRowsLabel, GuiUtil.setConstraints(4, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 25, 0, 5));
				bboxModePanel.add(tilingRowsText, GuiUtil.setConstraints(5, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
				bboxModePanel.add(tilingColumnsLabel, GuiUtil.setConstraints(6, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, 0, 5));
				bboxModePanel.add(tilingColumnsText, GuiUtil.setConstraints(7, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
				bboxPanel.addComponent(bboxModePanel);
			}

			bboxFilterPanel.build(bboxPanel);
			guiPanel.add(bboxFilterPanel, GuiUtil.setConstraints(0, 4, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			JPanel content = new JPanel();
			content.setBorder(UIManager.getBorder("TextField.border"));
			content.setLayout(new GridBagLayout());
			{
				content.add(featureTree, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
			}

			featureFilterPanel.build(content);
			guiPanel.add(featureFilterPanel, GuiUtil.setConstraints(0, 5, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
		{
			guiPanel.add(Box.createVerticalGlue(), GuiUtil.setConstraints(0, 6, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		useSelectionFilter.addActionListener(e -> setEnabledFilterTab());
		useCounterFilter.addActionListener(e -> setEnabledCounterFilter());
		useLodFilter.addActionListener(e -> setEnabledLodFilter());
		useBBoxFilter.addActionListener(e -> setEnabledBBoxFilter());
		useFeatureFilter.addActionListener(e -> setEnabledFeatureFilter());

		for (JCheckBox lod : lods)
			lod.addItemListener(e -> setEnabledLodFilterMode());

		bboxOverlaps.addActionListener(e -> setEnabledTilingOptions());
		bboxWithin.addActionListener(e -> setEnabledTilingOptions());
		bboxTiling.addActionListener(e -> setEnabledTilingOptions());
		tilingRowsText.addPropertyChangeListener("value", evt -> checkNonNegative(tilingRowsText));
		tilingColumnsText.addPropertyChangeListener("value", evt -> checkNonNegative(tilingColumnsText));

		PopupMenuDecorator.getInstance().decorateCheckBoxGroup(lods);
		PopupMenuDecorator.getInstance().decorate(featureTree);
		PopupMenuDecorator.getInstance().decorate(countText, startIndexText, tilingRowsText, tilingColumnsText);
	}

	private void setEnabledFilterTab() {
		for (FilterView filter : filters)
			filter.setEnabled(useSelectionFilter.isSelected());
	}

	private void setEnabledLodFilter() {
		for (JCheckBox lod : lods)
			lod.setEnabled(useLodFilter.isSelected());

		if (useLodFilter.isSelected())
			setEnabledLodFilterMode();
		else {
			lodModeLabel.setEnabled(false);
			lodMode.setEnabled(false);
		}

		lodDepthLabel.setEnabled(useLodFilter.isSelected());
		lodDepth.setEnabled(useLodFilter.isSelected());
	}

	private void setEnabledLodFilterMode() {
		int selected = 0;
		for (JCheckBox lod : lods) {
			if (lod.isSelected())
				selected++;
		}

		lodModeLabel.setEnabled(selected > 1);
		lodMode.setEnabled(selected > 1);
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
		bboxTiling.setEnabled(useBBoxFilter.isSelected());
		setEnabledTilingOptions();
	}

	private void setEnabledTilingOptions() {
		tilingRowsLabel.setEnabled(useBBoxFilter.isSelected() && bboxTiling.isSelected());
		tilingRowsText.setEnabled(useBBoxFilter.isSelected() && bboxTiling.isSelected());
		tilingColumnsLabel.setEnabled(useBBoxFilter.isSelected() && bboxTiling.isSelected());
		tilingColumnsText.setEnabled(useBBoxFilter.isSelected() && bboxTiling.isSelected());
	}

	private void setEnabledFeatureFilter() {
		featureTree.setPathsEnabled(useFeatureFilter.isSelected());
		featureTree.setEnabled(useFeatureFilter.isSelected());
		if (useFeatureFilter.isSelected()) {
			featureTree.expandRow(0);
		} else {
			featureTree.collapseRow(0);
			featureTree.setSelectionPath(null);
		}
	}

	private void checkNonNegative(JFormattedTextField field) {
		if (field.getValue() == null || ((Number)field.getValue()).intValue() < 0)
			field.setValue(0);
	}

	void showFilterDialog(boolean showSimple) {
		CardLayout layout = (CardLayout) mainPanel.getLayout();
		layout.show(mainPanel, showSimple ? "simple" : "advanced");
	}

	public void doTranslation() {
		lodFilterPanel.setTitle(Language.I18N.getString("filter.border.lod"));
		counterFilterPanel.setTitle(Language.I18N.getString("filter.border.counter"));
		bboxFilterPanel.setTitle(Language.I18N.getString("filter.border.boundingBox"));
		featureFilterPanel.setTitle(Language.I18N.getString("filter.border.featureClass"));

		srsComboBoxLabel.setText(Language.I18N.getString("export.label.targetSrs"));
		lodModeLabel.setText(Language.I18N.getString("filter.label.lod.mode"));
		lodDepthLabel.setText(Language.I18N.getString("filter.label.lod.depth"));
		countLabel.setText(Language.I18N.getString("filter.label.counter.count"));
		startIndexLabel.setText(Language.I18N.getString("filter.label.counter.startIndex"));
		bboxMode.setText(Language.I18N.getString("filter.label.boundingBox.mode"));
		bboxOverlaps.setText(Language.I18N.getString("filter.label.boundingBox.overlaps"));
		bboxWithin.setText(Language.I18N.getString("filter.label.boundingBox.within"));
		bboxTiling.setText(Language.I18N.getString("filter.label.boundingBox.tiling"));
		tilingRowsLabel.setText(Language.I18N.getString("filter.label.boundingBox.rows"));
		tilingColumnsLabel.setText(Language.I18N.getString("filter.label.boundingBox.columns"));

		for (int i = 0; i < filters.length; ++i) {
			filterTab.setTitleAt(i, filters[i].getLocalizedTitle());
			filters[i].doTranslation();
		}

		xmlQuery.doTranslation();
	}

	public void loadSettings() {
		SimpleQuery query = config.getExportConfig().getSimpleQuery();

		useSelectionFilter.setSelected(query.isUseSelectionFilter());
		useLodFilter.setSelected(query.isUseLodFilter());
		useCounterFilter.setSelected(query.isUseCountFilter());
		useBBoxFilter.setSelected(query.isUseBboxFilter());
		useFeatureFilter.setSelected(query.isUseTypeNames());

		// target SRS
		srsComboBox.setSelectedItem(query.getTargetSrs());

		// lod filter
		LodFilter lodFilter = query.getLodFilter();
		lodMode.setSelectedItem(lodFilter.getMode());
		for (int lod = 0; lod < lods.length; lod++)
			lods[lod].setSelected(lodFilter.isSetLod(lod));

		if (lodFilter.getSearchMode() == LodSearchMode.ALL)
			lodDepth.setValue("*");
		else {
			int searchDepth = lodFilter.getSearchDepth();
			lodDepth.setValue(searchDepth >= 0 && searchDepth < 10 ? String.valueOf(searchDepth) : "1");
		}

		// counter filter
		CounterFilter counterFilter = query.getCounterFilter();
		countText.setValue(counterFilter.getCount());
		startIndexText.setValue(counterFilter.getStartIndex());

		// bbox filter
		SimpleTiling bboxFilter = query.getBboxFilter();
		BoundingBox bbox = bboxFilter.getExtent();
		if (bbox != null)
			bboxPanel.setBoundingBox(bboxFilter.getExtent());

		if (bboxFilter.getMode() == SimpleTilingMode.TILING)
			bboxTiling.setSelected(true);
		else if (bboxFilter.getMode() == SimpleTilingMode.WITHIN)
			bboxWithin.setSelected(true);
		else
			bboxOverlaps.setSelected(true);

		// tiling options
		tilingRowsText.setValue(bboxFilter.getRows());
		tilingColumnsText.setValue(bboxFilter.getColumns());

		// feature type filter
		FeatureTypeFilter featureTypeFilter = query.getFeatureTypeFilter();
		featureTree.getCheckingModel().clearChecking();
		featureTree.setSelected(featureTypeFilter.getTypeNames());

		setEnabledFilterTab();
		setEnabledCounterFilter();
		setEnabledLodFilter();
		setEnabledBBoxFilter();
		setEnabledFeatureFilter();

		// load filter settings
		for (FilterView filter : filters)
			filter.loadSettings();

		filterTab.setSelectedIndex(-1);
		filterTab.setSelectedIndex(query.getSelectionFilter().isUseSQLFilter() ? 1 : 0);

		// load xml query
		xmlQuery.loadSettings();
	}

	public void setSimpleQuerySettings() {
		SimpleQuery query = config.getExportConfig().getSimpleQuery();

		query.setUseSelectionFilter(useSelectionFilter.isSelected());
		query.setUseCountFilter(useCounterFilter.isSelected());
		query.setUseLodFilter(useLodFilter.isSelected());
		query.setUseBboxFilter(useBBoxFilter.isSelected());
		query.setUseTypeNames(useFeatureFilter.isSelected());

		// target SRS
		query.setTargetSrs(srsComboBox.getSelectedItem());

		// lod filter
		LodFilter lodFilter = query.getLodFilter();
		lodFilter.setMode(lodMode.getItemAt(lodMode.getSelectedIndex()));
		for (int lod = 0; lod < lods.length; lod++)
			lodFilter.setLod(lod, lods[lod].isSelected());

		String searchDepth = lodDepth.getValue().toString();
		if (searchDepth.equals("*")) {
			lodFilter.setSearchMode(LodSearchMode.ALL);
			lodFilter.unsetSearchDepth();
		} else {
			lodFilter.setSearchMode(LodSearchMode.DEPTH);
			try {
				lodFilter.setSearchDepth(Integer.parseInt(searchDepth));
			} catch (NumberFormatException e) {
				lodFilter.setSearchDepth(1);
			}
		}

		// counter filter
		CounterFilter counterFilter = query.getCounterFilter();
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
		SimpleTiling bboxFilter = query.getBboxFilter();
		bboxFilter.setExtent(bboxPanel.getBoundingBox());

		if (bboxTiling.isSelected())
			bboxFilter.setMode(SimpleTilingMode.TILING);
		else if (bboxWithin.isSelected())
			bboxFilter.setMode(SimpleTilingMode.WITHIN);
		else
			bboxFilter.setMode(SimpleTilingMode.BBOX);

		// tiling options
		bboxFilter.setRows(((Number) tilingRowsText.getValue()).intValue());
		bboxFilter.setColumns(((Number) tilingColumnsText.getValue()).intValue());

		// feature type filter
		FeatureTypeFilter featureTypeFilter = query.getFeatureTypeFilter();
		featureTypeFilter.reset();
		featureTypeFilter.setTypeNames(featureTree.getSelectedTypeNames());

		query.getSelectionFilter().setUseSQLFilter(filterTab.getSelectedIndex() == 1);
		for (FilterView filter : filters)
			filter.setSettings();
	}

	public void setSettings() {
		setSimpleQuerySettings();
		xmlQuery.setSettings();
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		PropertyChangeEvent e = (PropertyChangeEvent)event;
		if (e.getPropertyName().equals("citygml.version"))
			featureTree.updateCityGMLVersion((CityGMLVersion)e.getNewValue(), useFeatureFilter.isSelected());
	}
}
