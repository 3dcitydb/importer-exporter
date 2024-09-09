/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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
import org.citydb.config.project.importer.ImportList;
import org.citydb.config.project.importer.ImportListMode;
import org.citydb.config.project.importer.SimpleBBOXMode;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.operation.common.filter.*;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class FilterPanel extends JPanel {
    private final Config config;

    private JCheckBox useAttributeFilter;
    private JCheckBox useImportListFilter;
    private JCheckBox useCounterFilter;
    private JCheckBox useBBoxFilter;
    private JCheckBox useFeatureFilter;

    private TitledPanel attributeFilterPanel;
    private TitledPanel importListFilterPanel;
    private TitledPanel counterFilterPanel;
    private TitledPanel bboxFilterPanel;
    private TitledPanel featureFilterPanel;

    private AttributeFilterView attributeFilter;
    private IdListFilterView<ImportList> importListFilter;
    private CounterFilterView counterFilter;
    private BoundingBoxFilterView bboxFilter;
    private FeatureTypeFilterView featureTypeFilter;

    private JLabel importListMode;
    private JToggleButton importListImport;
    private JToggleButton importListSkip;
    private JRadioButton bboxOverlaps;
    private JRadioButton bboxWithin;

    public FilterPanel(ViewController viewController, Config config) {
        this.config = config;
        initGui(viewController);
    }

    private void initGui(ViewController viewController) {
        useAttributeFilter = new JCheckBox();
        useImportListFilter = new JCheckBox();
        useCounterFilter = new JCheckBox();
        useBBoxFilter = new JCheckBox();
        useFeatureFilter = new JCheckBox();

        importListMode = new JLabel();
        importListImport = new JToggleButton();
        importListSkip = new JToggleButton();

        ButtonGroup importListGroup = new ButtonGroup();
        importListGroup.add(importListImport);
        importListGroup.add(importListSkip);

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
            toolBar.add(importListImport);
            toolBar.add(importListSkip);
            toolBar.setEnabled(false);

            importListFilter = new IdListFilterView<>(viewController, ImportList::new)
                    .withLocalizedTitle(() -> Language.I18N.getString("import.list.title"))
                    .addFooterRow(importListMode, toolBar);

            importListFilterPanel = new TitledPanel()
                    .withIcon(importListFilter.getIcon())
                    .withToggleButton(useImportListFilter)
                    .withCollapseButton()
                    .build(importListFilter.getViewComponent());

            add(importListFilterPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
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
        useImportListFilter.addItemListener(e -> setEnabledImportListFilter());
        useCounterFilter.addItemListener(e -> setEnabledCounterFilter());
        useBBoxFilter.addItemListener(e -> setEnabledBBoxFilter());
        useFeatureFilter.addItemListener(e -> setEnabledFeatureFilter());

        PopupMenuDecorator.getInstance().decorateTitledPanelGroup(attributeFilterPanel, importListFilterPanel,
                counterFilterPanel, bboxFilterPanel, featureFilterPanel);
    }

    private void setEnabledAttributeFilter() {
        attributeFilter.setEnabled(useAttributeFilter.isSelected());
    }

    private void setEnabledImportListFilter() {
        importListFilter.setEnabled(useImportListFilter.isSelected());
        importListMode.setEnabled(useImportListFilter.isSelected());
        importListImport.setEnabled(useImportListFilter.isSelected());
        importListSkip.setEnabled(useImportListFilter.isSelected());
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

    public void switchLocale(Locale locale) {
        attributeFilterPanel.setTitle(attributeFilter.getLocalizedTitle());
        importListFilterPanel.setTitle(importListFilter.getLocalizedTitle());
        counterFilterPanel.setTitle(counterFilter.getLocalizedTitle());
        bboxFilterPanel.setTitle(bboxFilter.getLocalizedTitle());
        featureFilterPanel.setTitle(featureTypeFilter.getLocalizedTitle());

        importListMode.setText(Language.I18N.getString("import.list.mode"));
        importListImport.setText(Language.I18N.getString("import.list.mode.import"));
        importListSkip.setText(Language.I18N.getString("import.list.mode.skip"));

        bboxOverlaps.setText(Language.I18N.getString("filter.label.boundingBox.overlaps"));
        bboxWithin.setText(Language.I18N.getString("filter.label.boundingBox.within"));

        attributeFilter.switchLocale(locale);
        importListFilter.switchLocale(locale);
        counterFilter.switchLocale(locale);
        featureTypeFilter.switchLocale(locale);
    }

    public void loadSettings() {
        ImportFilter filter = config.getImportConfig().getFilter();

        useAttributeFilter.setSelected(filter.isUseAttributeFilter());
        useImportListFilter.setSelected(filter.isUseImportListFilter());
        useCounterFilter.setSelected(filter.isUseCountFilter());
        useBBoxFilter.setSelected(filter.isUseBboxFilter());
        useFeatureFilter.setSelected(filter.isUseTypeNames());

        // ID list mode
        if (filter.getImportList().getMode() == ImportListMode.SKIP) {
            importListSkip.setSelected(true);
        } else {
            importListImport.setSelected(true);
        }

        // bbox filter
        if (filter.getBboxFilter().getMode() == SimpleBBOXMode.WITHIN) {
            bboxWithin.setSelected(true);
        } else {
            bboxOverlaps.setSelected(true);
        }

        attributeFilter.loadSettings(filter.getAttributeFilter());
        importListFilter.loadSettings(filter.getImportList());
        counterFilter.loadSettings(filter.getCounterFilter());
        bboxFilter.loadSettings(filter.getBboxFilter().getExtent());
        featureTypeFilter.loadSettings(filter.getFeatureTypeFilter());

        setEnabledAttributeFilter();
        setEnabledImportListFilter();
        setEnabledCounterFilter();
        setEnabledBBoxFilter();
        setEnabledFeatureFilter();

        // GUI specific settings
        ImportGuiConfig guiConfig = config.getGuiConfig().getImportGuiConfig();
        attributeFilterPanel.setCollapsed(guiConfig.isCollapseAttributeFilter());
        importListFilterPanel.setCollapsed(guiConfig.isCollapseImportListFilter());
        counterFilterPanel.setCollapsed(guiConfig.isCollapseCounterFilter());
        bboxFilterPanel.setCollapsed(guiConfig.isCollapseBoundingBoxFilter());
        featureFilterPanel.setCollapsed(guiConfig.isCollapseFeatureTypeFilter());
    }

    public void setSettings() {
        ImportFilter filter = config.getImportConfig().getFilter();

        filter.setUseAttributeFilter(useAttributeFilter.isSelected());
        filter.setUseImportListFilter(useImportListFilter.isSelected());
        filter.setUseCountFilter(useCounterFilter.isSelected());
        filter.setUseBboxFilter(useBBoxFilter.isSelected());
        filter.setUseTypeNames(useFeatureFilter.isSelected());

        // bbox filter
        filter.getBboxFilter().setMode(bboxOverlaps.isSelected() ?
                SimpleBBOXMode.BBOX :
                SimpleBBOXMode.WITHIN);

        filter.setAttributeFilter(attributeFilter.toSettings());
        filter.setImportList(importListFilter.toSettings());
        filter.setCounterFilter(counterFilter.toSettings());
        filter.getBboxFilter().setExtent(bboxFilter.toSettings());
        filter.setFeatureTypeFilter(featureTypeFilter.toSettings());

        // ID list mode
        filter.getImportList().setMode(importListSkip.isSelected() ?
                ImportListMode.SKIP :
                ImportListMode.IMPORT);

        // GUI specific settings
        ImportGuiConfig guiConfig = config.getGuiConfig().getImportGuiConfig();
        guiConfig.setCollapseAttributeFilter(attributeFilterPanel.isCollapsed());
        guiConfig.setCollapseImportListFilter(importListFilterPanel.isCollapsed());
        guiConfig.setCollapseCounterFilter(counterFilterPanel.isCollapsed());
        guiConfig.setCollapseBoundingBoxFilter(bboxFilterPanel.isCollapsed());
        guiConfig.setCollapseFeatureTypeFilter(featureFilterPanel.isCollapsed());
    }

    boolean checkImportListSettings() {
        return importListFilter.checkSettings(false);
    }
}
