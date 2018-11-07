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
package org.citydb.modules.citygml.exporter.gui.view;

import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.exporter.TilingOptions;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.lod.LodFilterMode;
import org.citydb.config.project.query.filter.lod.LodSearchMode;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.project.query.simple.SimpleBBOXMode;
import org.citydb.config.project.query.simple.SimpleBBOXOperator;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.event.global.PropertyChangeEvent;
import org.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import org.citydb.gui.components.feature.FeatureTypeTree;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.modules.citygml.exporter.gui.view.filter.AttributeFilterView;
import org.citydb.modules.citygml.exporter.gui.view.filter.FilterView;
import org.citydb.modules.citygml.exporter.gui.view.filter.SQLFilterView;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.jdesktop.swingx.JXTitledSeparator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

	private JXTitledSeparator counterSeparator;
	private JXTitledSeparator lodSeparator;
	private JXTitledSeparator bboxSeparator;
	private JXTitledSeparator featureSeparator;

	private JTabbedPane filterTab;
	private FilterView[] filters;

	private JLabel counterStartLabel;
	private JLabel counterEndLabel;
	private JFormattedTextField counterStartText;
	private JFormattedTextField counterEndText;

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

		counterSeparator = new JXTitledSeparator();
		lodSeparator = new JXTitledSeparator();
		bboxSeparator = new JXTitledSeparator();
		featureSeparator = new JXTitledSeparator();

		counterStartLabel = new JLabel();
		counterEndLabel = new JLabel();
		DecimalFormat counterFormat = new DecimalFormat("###################");
		counterFormat.setMaximumIntegerDigits(19);
		counterStartText = new JFormattedTextField(counterFormat);
		counterEndText = new JFormattedTextField(counterFormat);
		counterStartText.setFocusLostBehavior(JFormattedTextField.COMMIT);
		counterEndText.setFocusLostBehavior(JFormattedTextField.COMMIT);

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

		DecimalFormat tileFormat = new DecimalFormat("#######");
		tileFormat.setMaximumIntegerDigits(7);
		tileFormat.setMinimumIntegerDigits(1);
		tilingRowsText = new JFormattedTextField(tileFormat);
		tilingColumnsText = new JFormattedTextField(tileFormat);

		featureTree = new FeatureTypeTree(Util.toCityGMLVersion(config.getProject().getExporter().getSimpleQuery().getVersion()));
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




		mainPanel = new JPanel(new CardLayout());
		add(mainPanel, GuiUtil.setConstraints(0,0,1,0,GridBagConstraints.HORIZONTAL,0,0,0,0));

		JButton switchButton = new JButton("switch");
		add(switchButton, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.EAST, GridBagConstraints.NONE,5,5,5,5));
		switchButton.addActionListener(l -> {
			CardLayout layout = (CardLayout) mainPanel.getLayout();
			layout.next(mainPanel);
		});

		JPanel xmlPanel = new JPanel();
		xmlPanel.setLayout(new GridBagLayout());
		{
			JTextArea xmlText = new JTextArea("", 10, 1);
			JScrollPane scrollPane = new JScrollPane(xmlText);
			xmlPanel.add(scrollPane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 10, 5, 10, 5));
		}

		JPanel guiPanel = new JPanel();
		mainPanel.add(guiPanel, "guiPanel");
		mainPanel.add(xmlPanel, "xmlPanel");


		guiPanel.setLayout(new GridBagLayout());
		{
			JPanel filterRow = new JPanel();
			guiPanel.add(filterRow, GuiUtil.setConstraints(0,0,1,0,GridBagConstraints.BOTH,0,0,0,0));
			filterRow.setLayout(new GridBagLayout());
			{
				filterTab = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

				filterRow.add(useSelectionFilter, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NORTH,GridBagConstraints.NONE,5,0,5,5));
				filterRow.add(filterTab, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,5,0,5,0));

				filters = new FilterView[] {
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
		}
		{
			JPanel lodRow = new JPanel();
			guiPanel.add(lodRow, GuiUtil.setConstraints(0,1,1,0,GridBagConstraints.HORIZONTAL,0,0,0,0));
			lodRow.setLayout(new GridBagLayout());
			{
				JPanel lodPanel = new JPanel();

				lodRow.add(useLodFilter, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,5,0,0,5));
				lodRow.add(lodSeparator, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,5,0,0,5));
				lodRow.add(lodPanel, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,0,0,5,5));

				lodPanel.setLayout(new GridBagLayout());
				{
					lods = new JCheckBox[5];
					for (int lod = 0; lod < lods.length; lod++) {
						lods[lod] = new JCheckBox("LoD" + lod);
						lods[lod].setIconTextGap(10);
						lodPanel.add(lods[lod], GuiUtil.setConstraints(lod,0,0,0,GridBagConstraints.NONE,0,0,0,15));
					}

					lodPanel.add(lodModeLabel, GuiUtil.setConstraints(5,0,0,0,GridBagConstraints.NONE,0,20,0,5));
					lodPanel.add(lodMode, GuiUtil.setConstraints(6,0,0.5,1,GridBagConstraints.HORIZONTAL,0,5,0,5));
					lodPanel.add(lodDepthLabel, GuiUtil.setConstraints(7,0,0,0,GridBagConstraints.NONE,0,20,0,5));
					lodPanel.add(lodDepth, GuiUtil.setConstraints(8,0,0.5,0,GridBagConstraints.HORIZONTAL,0,5,0,0));
				}
			}
		}
		{
			JPanel counterFilterRow = new JPanel();
			guiPanel.add(counterFilterRow, GuiUtil.setConstraints(0,2,1,0,GridBagConstraints.BOTH,0,0,0,0));
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
			guiPanel.add(bboxFilterRow, GuiUtil.setConstraints(0,3,1,0,GridBagConstraints.BOTH,0,0,0,0));
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
				bboxTiling.setIconTextGap(10);
				bboxModePanel.add(bboxMode, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.HORIZONTAL,0,0,0,5));
				bboxModePanel.add(bboxOverlaps, GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.HORIZONTAL,0,15,0,5));
				bboxModePanel.add(bboxWithin, GuiUtil.setConstraints(2,0,0,0,GridBagConstraints.HORIZONTAL,0,5,0,5));
				bboxModePanel.add(bboxTiling, GuiUtil.setConstraints(3,0,0,0,GridBagConstraints.HORIZONTAL,0,5,0,5));
				bboxModePanel.add(tilingRowsLabel, GuiUtil.setConstraints(4,0,0,0,GridBagConstraints.HORIZONTAL,0,25,0,5));
				bboxModePanel.add(tilingRowsText, GuiUtil.setConstraints(5,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,5));
				bboxModePanel.add(tilingColumnsLabel, GuiUtil.setConstraints(6,0,0,0,GridBagConstraints.HORIZONTAL,0,10,0,5));
				bboxModePanel.add(tilingColumnsText, GuiUtil.setConstraints(7,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,0));
				bboxPanel.addComponent(bboxModePanel);
			}
		}
		{
			JPanel featureClassRow = new JPanel();
			guiPanel.add(featureClassRow, GuiUtil.setConstraints(0,4,1,0,GridBagConstraints.HORIZONTAL,0,0,0,0));
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

		useSelectionFilter.addActionListener(e -> setEnabledFilterTab());
		useCounterFilter.addActionListener(e -> setEnabledCounterFilter());
		useLodFilter.addActionListener(e -> setEnabledLodFilter());
		useBBoxFilter.addActionListener(e -> setEnabledBBoxFilter());
		useFeatureFilter.addActionListener(e -> setEnabledFeatureFilter());

		lodSeparator.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				useLodFilter.doClick();
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

		for (JCheckBox lod : lods)
			lod.addItemListener(e -> setEnabledLodFilterMode());

		bboxOverlaps.addActionListener(e -> setEnabledTilingOptions());
		bboxWithin.addActionListener(e -> setEnabledTilingOptions());
		bboxTiling.addActionListener(e -> setEnabledTilingOptions());
		tilingRowsText.addPropertyChangeListener(evt -> checkNonNegative(tilingRowsText));
		tilingColumnsText.addPropertyChangeListener(evt -> checkNonNegative(tilingColumnsText));

		PopupMenuDecorator.getInstance().decorateCheckBoxGroup(lods);
		PopupMenuDecorator.getInstance().decorate(featureTree);
		PopupMenuDecorator.getInstance().decorate(counterStartText, counterEndText, tilingRowsText, tilingColumnsText);
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
		counterStartLabel.setEnabled(useCounterFilter.isSelected());
		counterEndLabel.setEnabled(useCounterFilter.isSelected());
		counterStartText.setEnabled(useCounterFilter.isSelected());
		counterEndText.setEnabled(useCounterFilter.isSelected());
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
		featureTree.repaint();
	}

	private void checkNonNegative(JFormattedTextField field) {
		if (field.getValue() != null && ((Number)field.getValue()).intValue() < 0)
			field.setValue(0);
	}

	public void doTranslation() {
		lodSeparator.setTitle(Language.I18N.getString("filter.border.lod"));
		counterSeparator.setTitle(Language.I18N.getString("filter.border.counter"));
		bboxSeparator.setTitle(Language.I18N.getString("filter.border.boundingBox"));
		featureSeparator.setTitle(Language.I18N.getString("filter.border.featureClass"));

		lodModeLabel.setText(Language.I18N.getString("filter.label.lod.mode"));
		lodDepthLabel.setText(Language.I18N.getString("filter.label.lod.depth"));
		counterStartLabel.setText(Language.I18N.getString("filter.label.counter.start"));
		counterEndLabel.setText(Language.I18N.getString("filter.label.counter.end"));
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
	}

	public void loadSettings() {
		SimpleQuery query = config.getProject().getExporter().getSimpleQuery();

		useSelectionFilter.setSelected(query.isUseSelectionFilter());
		useLodFilter.setSelected(query.isUseLodFilter());
		useCounterFilter.setSelected(query.isUseCountFilter());
		useBBoxFilter.setSelected(query.isUseBboxFilter());
		useFeatureFilter.setSelected(query.isUseTypeNames());

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
		counterStartText.setValue(counterFilter.getLowerLimit());
		counterEndText.setValue(counterFilter.getUpperLimit());

		// bbox filter
		SimpleBBOXOperator bboxFilter = query.getBboxFilter();
		BoundingBox bbox = bboxFilter.getEnvelope();
		if (bbox != null)
			bboxPanel.setBoundingBox(bboxFilter.getEnvelope());

		if (query.isUseTiling())
			bboxTiling.setSelected(query.isUseTiling());
		else if (bboxFilter.getBboxMode() == SimpleBBOXMode.WITHIN)
			bboxWithin.setSelected(true);
		else
			bboxOverlaps.setSelected(true);

		// tiling options
		TilingOptions tilingOptions = query.getTilingOptions();
		tilingRowsText.setValue(tilingOptions.getRows());
		tilingColumnsText.setValue(tilingOptions.getColumns());

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
	}

	public void setSettings() {
		SimpleQuery query = config.getProject().getExporter().getSimpleQuery();

		query.setUseSelectionFilter(useSelectionFilter.isSelected());
		query.setUseCountFilter(useCounterFilter.isSelected());
		query.setUseLodFilter(useLodFilter.isSelected());
		query.setUseBboxFilter(useBBoxFilter.isSelected());
		query.setUseTypeNames(useFeatureFilter.isSelected());

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
		if (counterStartText.isEditValid() && counterStartText.getValue() != null
				&& counterEndText.isEditValid() && counterEndText.getValue() != null) {
			counterFilter.setLowerLimit(((Number) counterStartText.getValue()).longValue());
			counterFilter.setUpperLimit(((Number) counterEndText.getValue()).longValue());
		} else {
			counterFilter.setLowerLimit(null);
			counterFilter.setUpperLimit(null);
		}

		// bbox filter
		query.getBboxFilter().setEnvelope(bboxPanel.getBoundingBox());
		query.getBboxFilter().setBboxMode(bboxOverlaps.isSelected() ? SimpleBBOXMode.BBOX : SimpleBBOXMode.WITHIN);

		// tiling options
		TilingOptions tilingOptions = query.getTilingOptions();
		tilingOptions.setRows(((Number) tilingRowsText.getValue()).intValue());
		tilingOptions.setColumns(((Number) tilingColumnsText.getValue()).intValue());
		query.setUseTiling(bboxTiling.isSelected());

		// feature type filter
		FeatureTypeFilter featureTypeFilter = query.getFeatureTypeFilter();
		featureTypeFilter.reset();
		featureTypeFilter.setTypeNames(featureTree.getSelectedTypeNames());

		query.getSelectionFilter().setUseSQLFilter(filterTab.getSelectedIndex() == 1);
		for (FilterView filter : filters)
			filter.setSettings();
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		PropertyChangeEvent e = (PropertyChangeEvent)event;
		if (e.getPropertyName().equals("citygml.version"))
			featureTree.updateCityGMLVersion((CityGMLVersion)e.getNewValue(), useFeatureFilter.isSelected());
	}

}
