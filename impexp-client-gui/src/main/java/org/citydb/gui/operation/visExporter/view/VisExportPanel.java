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
package org.citydb.gui.operation.visExporter.view;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.gui.visExporter.VisExportGuiConfig;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.query.simple.SimpleAttributeFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilterMode;
import org.citydb.config.project.visExporter.*;
import org.citydb.core.ade.ADEExtension;
import org.citydb.core.ade.visExporter.ADEVisExportExtension;
import org.citydb.core.ade.visExporter.ADEVisExportExtensionManager;
import org.citydb.core.database.DatabaseController;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.CoreConstants;
import org.citydb.core.util.Util;
import org.citydb.gui.components.BlankNumberFormatter;
import org.citydb.gui.components.FeatureTypeTree;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.dialog.ConfirmationCheckDialog;
import org.citydb.gui.components.dialog.ExportStatusDialog;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.operation.common.filter.*;
import org.citydb.gui.plugin.util.DefaultViewComponent;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.DatabaseConnectionStateEvent;
import org.citydb.util.event.global.EventType;
import org.citydb.util.event.global.InterruptEvent;
import org.citydb.util.log.Logger;
import org.citydb.vis.controller.VisExportException;
import org.citydb.vis.controller.VisExporter;
import org.citygml4j.model.module.citygml.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;
import javax.xml.datatype.DatatypeConstants;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class VisExportPanel extends DefaultViewComponent implements EventHandler {
    private final Logger log = Logger.getInstance();
    private final ReentrantLock mainLock = new ReentrantLock();
    private final ViewController viewController;
    private final DatabaseController databaseController;
    private final Config config;

    private JTextField browseText;
    private JButton browseButton;

    private JCheckBox useFeatureVersionFilter;
    private JCheckBox useAttributeFilter;
    private JCheckBox useSQLFilter;
    private JCheckBox useBboxFilter;
    private JCheckBox useTilingFilter;
    private JCheckBox useFeatureFilter;

    private TitledPanel featureVersionPanel;
    private TitledPanel attributeFilterPanel;
    private TitledPanel sqlFilterPanel;
    private TitledPanel bboxFilterPanel;
    private TitledPanel tilingPanel;
    private TitledPanel lodPanel;
    private TitledPanel displayAsPanel;
    private TitledPanel featureFilterPanel;

    private FeatureVersionFilterView featureVersionFilter;
    private AttributeFilterView attributeFilter;
    private SQLFilterView sqlFilter;
    private BoundingBoxFilterView bboxFilter;
    private FeatureTypeFilterView featureTypeFilter;

    private JRadioButton automaticTilingRadioButton;
    private JRadioButton manualTilingRadioButton;
    private JFormattedTextField tileSizeText;
    private JLabel tileSizeUnit;
    private JFormattedTextField rowsText;
    private JLabel columnsLabel;
    private JFormattedTextField columnsText;

    private JComboBox<String> lodComboBox;

    private JCheckBox footprintCheckbox;
    private JCheckBox extrudedCheckbox;
    private JCheckBox geometryCheckbox;
    private JCheckBox colladaCheckbox;
    private JLabel visibleFromFootprintLabel;
    private JFormattedTextField footprintVisibleFromText;
    private JLabel pixelsFootprintLabel;
    private JLabel visibleFromExtrudedLabel;
    private JFormattedTextField extrudedVisibleFromText;
    private JLabel pixelsExtrudedLabel;
    private JLabel visibleFromGeometryLabel;
    private JFormattedTextField geometryVisibleFromText;
    private JLabel pixelsGeometryLabel;
    private JLabel visibleFromColladaLabel;
    private JFormattedTextField colladaVisibleFromText;
    private JLabel pixelsColladaLabel;

    private JLabel themeLabel;
    private JComboBox<String> themeComboBox;
    private JButton fetchThemesButton;

    private JButton exportButton;

    public VisExportPanel(ViewController viewController, Config config) {
        this.viewController = viewController;
        this.config = config;

        databaseController = ObjectRegistry.getInstance().getDatabaseController();
        ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);

        initGui();
    }

    private void initGui() {
        browseText = new JTextField();
        browseButton = new JButton();
        JPanel browsePanel = new JPanel();
        browsePanel.setLayout(new GridBagLayout());
        browsePanel.add(browseText, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 5));
        browsePanel.add(browseButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 0));

        useFeatureVersionFilter = new JCheckBox();
        useAttributeFilter = new JCheckBox();
        useSQLFilter = new JCheckBox();
        useBboxFilter = new JCheckBox();
        useTilingFilter = new JCheckBox();
        useFeatureFilter = new JCheckBox();

        automaticTilingRadioButton = new JRadioButton();
        manualTilingRadioButton = new JRadioButton();
        tileSizeUnit = new JLabel("m");
        columnsLabel = new JLabel();

        ButtonGroup tilingButtonGroup = new ButtonGroup();
        tilingButtonGroup.add(automaticTilingRadioButton);
        tilingButtonGroup.add(manualTilingRadioButton);

        NumberFormatter format = new NumberFormatter(new DecimalFormat("#"));
        format.setMinimum(0);
        format.setMaximum(9999999);
        tileSizeText = new JFormattedTextField(format);
        columnsText = new JFormattedTextField(format);
        rowsText = new JFormattedTextField(format);

        lodComboBox = new JComboBox<>();
        for (int index = 0; index < 5; index++) {
            lodComboBox.insertItemAt("LoD" + index, index);
        }

        lodComboBox.insertItemAt(Language.I18N.getString("visExport.label.highestLODAvailable"), lodComboBox.getItemCount());
        lodComboBox.setSelectedIndex(2);

        footprintCheckbox = new JCheckBox();
        extrudedCheckbox = new JCheckBox();
        geometryCheckbox = new JCheckBox();
        colladaCheckbox = new JCheckBox();
        visibleFromFootprintLabel = new JLabel();
        pixelsFootprintLabel = new JLabel();
        visibleFromExtrudedLabel = new JLabel();
        pixelsExtrudedLabel = new JLabel();
        visibleFromGeometryLabel = new JLabel();
        pixelsGeometryLabel = new JLabel();
        visibleFromColladaLabel = new JLabel();
        pixelsColladaLabel = new JLabel();

        BlankNumberFormatter visibleFromFormatter = new BlankNumberFormatter(new DecimalFormat("#"));
        visibleFromFormatter.setLimits(0, 9999);
        footprintVisibleFromText = new JFormattedTextField(visibleFromFormatter);
        extrudedVisibleFromText = new JFormattedTextField(visibleFromFormatter);
        geometryVisibleFromText = new JFormattedTextField(visibleFromFormatter);
        colladaVisibleFromText = new JFormattedTextField(visibleFromFormatter);
        footprintVisibleFromText.setColumns(10);
        extrudedVisibleFromText.setColumns(10);
        geometryVisibleFromText.setColumns(10);
        colladaVisibleFromText.setColumns(10);

        themeLabel = new JLabel();
        themeComboBox = new JComboBox<>();
        fetchThemesButton = new JButton();

        exportButton = new JButton();

        setLayout(new GridBagLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        {
            // lod to export from
            JPanel lodContent = new JPanel();
            lodContent.setLayout(new GridBagLayout());
            lodContent.add(lodComboBox, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0, 0, 0, 0));
            lodPanel = new TitledPanel().build(lodContent);

            // display forms
            JPanel displayAsContent = new JPanel();
            displayAsContent.setLayout(new GridBagLayout());
            displayAsContent.add(footprintCheckbox, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 5));
            displayAsContent.add(visibleFromFootprintLabel, GuiUtil.setConstraints(1, 0, 0, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 5, 0, 5));
            displayAsContent.add(footprintVisibleFromText, GuiUtil.setConstraints(2, 0, 0, 1, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
            displayAsContent.add(pixelsFootprintLabel, GuiUtil.setConstraints(3, 0, 0, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
            displayAsContent.add(extrudedCheckbox, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 5));
            displayAsContent.add(visibleFromExtrudedLabel, GuiUtil.setConstraints(1, 1, 0, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 5, 5, 0, 5));
            displayAsContent.add(extrudedVisibleFromText, GuiUtil.setConstraints(2, 1, 0, 1, GridBagConstraints.HORIZONTAL, 5, 5, 0, 5));
            displayAsContent.add(pixelsExtrudedLabel, GuiUtil.setConstraints(3, 1, 0, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
            displayAsContent.add(geometryCheckbox, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 5));
            displayAsContent.add(visibleFromGeometryLabel, GuiUtil.setConstraints(1, 2, 0, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 5, 5, 0, 5));
            displayAsContent.add(geometryVisibleFromText, GuiUtil.setConstraints(2, 2, 0, 1, GridBagConstraints.HORIZONTAL, 5, 5, 0, 5));
            displayAsContent.add(pixelsGeometryLabel, GuiUtil.setConstraints(3, 2, 0, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
            displayAsContent.add(colladaCheckbox, GuiUtil.setConstraints(0, 3, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 5));
            displayAsContent.add(visibleFromColladaLabel, GuiUtil.setConstraints(1, 3, 0, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 5, 5, 0, 5));
            displayAsContent.add(colladaVisibleFromText, GuiUtil.setConstraints(2, 3, 0, 1, GridBagConstraints.HORIZONTAL, 5, 5, 0, 5));
            displayAsContent.add(pixelsColladaLabel, GuiUtil.setConstraints(3, 3, 0, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));

            // appearance
            JPanel appearanceContent = new JPanel();
            appearanceContent.setLayout(new GridBagLayout());
            int lmargin = GuiUtil.getTextOffset(colladaCheckbox);
            appearanceContent.add(themeLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
            appearanceContent.add(themeComboBox, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.HORIZONTAL, 0, 5, 0, 15));
            themeComboBox.setPreferredSize(new Dimension(50, themeComboBox.getPreferredSize().height));

            displayAsContent.add(appearanceContent, GuiUtil.setConstraints(0, 4, 1, 1, GridBagConstraints.BOTH, 5, lmargin, 0, 0));
            displayAsContent.add(fetchThemesButton, GuiUtil.setConstraints(1, 4, 3, 1, 0, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 5, 0, 0));
            displayAsPanel = new TitledPanel().build(displayAsContent);

            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            content.add(lodPanel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
            content.add(displayAsPanel, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.BOTH, 0, 20, 0, 0));

            mainPanel.add(content, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
        {
            featureVersionFilter = new FeatureVersionFilterView();

            featureVersionPanel = new TitledPanel()
                    .withIcon(featureVersionFilter.getIcon())
                    .withToggleButton(useFeatureVersionFilter)
                    .withCollapseButton()
                    .build(featureVersionFilter.getViewComponent());

            mainPanel.add(featureVersionPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
        {
            // tiling
            JPanel tilingContent = new JPanel();
            tilingContent.setLayout(new GridBagLayout());
            tilingContent.add(automaticTilingRadioButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
            tilingContent.add(tileSizeText, GuiUtil.setConstraints(1, 0, 0.34, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            tilingContent.add(tileSizeUnit, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            tilingContent.add(manualTilingRadioButton, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 20, 0, 5));
            tilingContent.add(rowsText, GuiUtil.setConstraints(4, 0, 0.33, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
            tilingContent.add(columnsLabel, GuiUtil.setConstraints(5, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, 0, 5));
            tilingContent.add(columnsText, GuiUtil.setConstraints(6, 0, 0.33, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));

            tilingPanel = new TitledPanel()
                    .withIcon(new FlatSVGIcon("org/citydb/gui/filter/tiling.svg"))
                    .withToggleButton(useTilingFilter)
                    .withCollapseButton()
                    .build(tilingContent);

            mainPanel.add(tilingPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
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

            mainPanel.add(attributeFilterPanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
        {
            sqlFilter = new SQLFilterView(() -> config.getGuiConfig().getVisExportGuiConfig().getSQLExportFilterComponent());

            sqlFilterPanel = new TitledPanel()
                    .withIcon(sqlFilter.getIcon())
                    .withToggleButton(useSQLFilter)
                    .withCollapseButton()
                    .build(sqlFilter.getViewComponent());

            mainPanel.add(sqlFilterPanel, GuiUtil.setConstraints(0, 4, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
        {
            // bbox
            bboxFilter = new BoundingBoxFilterView(viewController);

            bboxFilterPanel = new TitledPanel()
                    .withIcon(bboxFilter.getIcon())
                    .withToggleButton(useBboxFilter)
                    .withCollapseButton()
                    .build(bboxFilter.getViewComponent());

            mainPanel.add(bboxFilterPanel, GuiUtil.setConstraints(0, 5, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
        {
            featureTypeFilter = new FeatureTypeFilterView(CityGMLVersion.v2_0_0,
                    e -> e instanceof ADEVisExportExtension);

            featureFilterPanel = new TitledPanel()
                    .withIcon(featureTypeFilter.getIcon())
                    .withToggleButton(useFeatureFilter)
                    .withCollapseButton()
                    .build(featureTypeFilter.getViewComponent());

            mainPanel.add(featureFilterPanel, GuiUtil.setConstraints(0, 6, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }

        JPanel view = new JPanel();
        view.setLayout(new GridBagLayout());
        view.add(mainPanel, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 10, 0, 10));

        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        add(browsePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 15, 10, 15, 10));
        add(scrollPane, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
        add(exportButton, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.NONE, 10, 10, 10, 10));

        browseButton.addActionListener(e -> saveFile(Language.I18N.getString("main.tabbedPane.visExport")));
        manualTilingRadioButton.addActionListener(e -> setEnabledTiling());
        automaticTilingRadioButton.addActionListener(e -> setEnabledTiling());
        footprintCheckbox.addItemListener(e -> setVisibilityEnabledValues());
        extrudedCheckbox.addItemListener(e -> setVisibilityEnabledValues());
        geometryCheckbox.addItemListener(e -> setVisibilityEnabledValues());
        colladaCheckbox.addItemListener(e -> setVisibilityEnabledValues());
        fetchThemesButton.addActionListener(e -> new ThemeUpdater().execute());

        useFeatureVersionFilter.addActionListener(e -> setEnabledFeatureVersionFilter());
        useAttributeFilter.addItemListener(e -> setEnabledAttributeFilter());
        useSQLFilter.addItemListener(e -> setEnabledSQLFilter());
        useBboxFilter.addItemListener(e -> setEnabledBBoxFilter());
        useTilingFilter.addItemListener(e -> setEnabledTiling());
        useFeatureFilter.addItemListener(e -> setEnabledFeatureFilter());

        lodComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setVisibilityEnabledValues();
            }
        });

        exportButton.addActionListener(e -> new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                doExport();
                return null;
            }
        }.execute());

        PopupMenuDecorator.getInstance().decorate(browseText, tileSizeText, rowsText, columnsText,
                footprintVisibleFromText, extrudedVisibleFromText, geometryVisibleFromText, colladaVisibleFromText);
        PopupMenuDecorator.getInstance().decorateAndGetCheckBoxGroup(footprintCheckbox, extrudedCheckbox,
                geometryCheckbox, colladaCheckbox);
        PopupMenuDecorator.getInstance().decorateTitledPanelGroup(featureVersionPanel, tilingPanel,
                attributeFilterPanel, sqlFilterPanel, bboxFilterPanel, featureFilterPanel);
    }

    private void setEnabledFeatureVersionFilter() {
        featureVersionFilter.setEnabled(useFeatureVersionFilter.isSelected());
    }

    private void setEnabledTiling() {
        automaticTilingRadioButton.setEnabled(useTilingFilter.isSelected());
        manualTilingRadioButton.setEnabled(useTilingFilter.isSelected());

        tileSizeText.setEnabled(automaticTilingRadioButton.isEnabled() && automaticTilingRadioButton.isSelected());
        tileSizeUnit.setEnabled(automaticTilingRadioButton.isEnabled() && automaticTilingRadioButton.isSelected());
        rowsText.setEnabled(manualTilingRadioButton.isEnabled() && manualTilingRadioButton.isSelected());
        columnsLabel.setEnabled(manualTilingRadioButton.isEnabled() && manualTilingRadioButton.isSelected());
        columnsText.setEnabled(manualTilingRadioButton.isEnabled() && manualTilingRadioButton.isSelected());
    }

    private void setEnabledAttributeFilter() {
        attributeFilter.setEnabled(useAttributeFilter.isSelected());
    }

    private void setEnabledSQLFilter() {
        sqlFilter.setEnabled(useSQLFilter.isSelected());
    }

    private void setEnabledBBoxFilter() {
        bboxFilter.setEnabled(useBboxFilter.isSelected());
    }

    private void setEnabledFeatureFilter() {
        featureTypeFilter.setEnabled(useFeatureFilter.isSelected());
    }

    @Override
    public void switchLocale(Locale locale) {
        browseButton.setText(Language.I18N.getString("common.button.browse"));
        featureVersionPanel.setTitle(featureVersionFilter.getLocalizedTitle());
        tilingPanel.setTitle(Language.I18N.getString("visExport.label.tiling"));
        attributeFilterPanel.setTitle(attributeFilter.getLocalizedTitle());
        sqlFilterPanel.setTitle(sqlFilter.getLocalizedTitle());
        bboxFilterPanel.setTitle(bboxFilter.getLocalizedTitle());
        featureFilterPanel.setTitle(featureTypeFilter.getLocalizedTitle());

        manualTilingRadioButton.setText(Language.I18N.getString("visExport.label.rows"));
        columnsLabel.setText(Language.I18N.getString("visExport.label.columns"));
        automaticTilingRadioButton.setText(Language.I18N.getString("visExport.label.automatic"));

        lodPanel.setTitle(Language.I18N.getString("visExport.label.fromLOD"));
        int selectedIndex = lodComboBox.getSelectedIndex();
        lodComboBox.removeItemAt(lodComboBox.getItemCount() - 1);
        lodComboBox.insertItemAt(Language.I18N.getString("visExport.label.highestLODAvailable"), lodComboBox.getItemCount());
        lodComboBox.setSelectedIndex(selectedIndex);
        lodComboBox.repaint();

        displayAsPanel.setTitle(Language.I18N.getString("visExport.label.displayAs"));
        footprintCheckbox.setText(Language.I18N.getString("visExport.label.footprint"));
        extrudedCheckbox.setText(Language.I18N.getString("visExport.label.extruded"));
        geometryCheckbox.setText(Language.I18N.getString("visExport.label.geometry"));
        colladaCheckbox.setText(Language.I18N.getString("visExport.label.collada"));

        visibleFromFootprintLabel.setText(Language.I18N.getString("visExport.label.visibleFrom"));
        pixelsFootprintLabel.setText(Language.I18N.getString("visExport.label.pixels"));
        visibleFromExtrudedLabel.setText(Language.I18N.getString("visExport.label.visibleFrom"));
        pixelsExtrudedLabel.setText(Language.I18N.getString("visExport.label.pixels"));
        visibleFromGeometryLabel.setText(Language.I18N.getString("visExport.label.visibleFrom"));
        pixelsGeometryLabel.setText(Language.I18N.getString("visExport.label.pixels"));
        visibleFromColladaLabel.setText(Language.I18N.getString("visExport.label.visibleFrom"));
        pixelsColladaLabel.setText(Language.I18N.getString("visExport.label.pixels"));

        themeLabel.setText(Language.I18N.getString("pref.visExport.label.theme"));
        fetchThemesButton.setText(Language.I18N.getString("common.button.query"));
        exportButton.setText(Language.I18N.getString("export.button.export"));

        featureVersionFilter.switchLocale(locale);
        attributeFilter.switchLocale(locale);
        sqlFilter.switchLocale(locale);
        featureTypeFilter.switchLocale(locale);
    }

    @Override
    public void loadSettings() {
        // filter
        SimpleVisQuery query = config.getVisExportConfig().getQuery();

        useFeatureVersionFilter.setSelected(query.isUseFeatureVersionFilter());
        useFeatureFilter.setSelected(query.isUseTypeNames());
        useAttributeFilter.setSelected(query.isUseAttributeFilter());
        useSQLFilter.setSelected(query.isUseSQLFilter());
        useBboxFilter.setSelected(query.isUseBboxFilter());

        // tiling
        VisTiling spatialFilter = query.getBboxFilter();
        useTilingFilter.setSelected(spatialFilter.getMode() != VisTilingMode.NO_TILING);
        if (spatialFilter.getMode() == VisTilingMode.MANUAL) {
            manualTilingRadioButton.setSelected(true);
        } else {
            automaticTilingRadioButton.setSelected(true);
        }

        tileSizeText.setValue(spatialFilter.getTilingOptions().getAutoTileSideLength());
        rowsText.setValue(spatialFilter.getRows());
        columnsText.setValue(spatialFilter.getColumns());

        featureVersionFilter.loadSettings(query.getFeatureVersionFilter());
        attributeFilter.loadSettings(query.getAttributeFilter());
        sqlFilter.loadSettings(query.getSQLFilter());
        bboxFilter.loadSettings(query.getBboxFilter().getExtent());
        featureTypeFilter.loadSettings(query.getFeatureTypeFilter());

        // display options
        VisExportConfig visExportConfig = config.getVisExportConfig();

        int lod = visExportConfig.getLodToExportFrom();
        lod = lod >= lodComboBox.getItemCount() ? lodComboBox.getItemCount() - 1 : lod;
        lodComboBox.setSelectedIndex(lod);

        for (DisplayForm displayForm : visExportConfig.getDisplayForms().values()) {
            switch (displayForm.getType()) {
                case FOOTPRINT:
                    if (displayForm.isActive()) {
                        footprintCheckbox.setSelected(true);
                        footprintVisibleFromText.setValue(displayForm.getVisibleFrom());
                        if (displayForm.getVisibleFrom() == 0) {
                            footprintVisibleFromText.setText("");
                        }
                    }
                    break;
                case EXTRUDED:
                    if (displayForm.isActive()) {
                        extrudedCheckbox.setSelected(true);
                        extrudedVisibleFromText.setValue(displayForm.getVisibleFrom());
                        if (displayForm.getVisibleFrom() == 0) {
                            extrudedVisibleFromText.setText("");
                        }
                    }
                    break;
                case GEOMETRY:
                    if (displayForm.isActive()) {
                        geometryCheckbox.setSelected(true);
                        geometryVisibleFromText.setValue(displayForm.getVisibleFrom());
                        if (displayForm.getVisibleFrom() == 0) {
                            geometryVisibleFromText.setText("");
                        }
                    }
                    break;
                case COLLADA:
                    if (displayForm.isActive()) {
                        colladaCheckbox.setSelected(true);
                        colladaVisibleFromText.setValue(displayForm.getVisibleFrom());
                        if (displayForm.getVisibleFrom() == 0) {
                            colladaVisibleFromText.setText("");
                        }
                    }
                    break;
            }
        }

        themeComboBox.removeAllItems();
        themeComboBox.addItem(VisExportConfig.THEME_NONE);
        themeComboBox.setSelectedItem(VisExportConfig.THEME_NONE);

        setEnabledFeatureVersionFilter();
        setEnabledTiling();
        setEnabledAttributeFilter();
        setEnabledSQLFilter();
        setEnabledBBoxFilter();
        setEnabledFeatureFilter();
        setVisibilityEnabledValues();

        // GUI specific settings
        VisExportGuiConfig guiConfig = config.getGuiConfig().getVisExportGuiConfig();
        featureVersionPanel.setCollapsed(guiConfig.isCollapseFeatureVersionFilter());
        tilingPanel.setCollapsed(guiConfig.isCollapseTilingFilter());
        attributeFilterPanel.setCollapsed(guiConfig.isCollapseAttributeFilter());
        sqlFilterPanel.setCollapsed(guiConfig.isCollapseSQLFilter());
        bboxFilterPanel.setCollapsed(guiConfig.isCollapseBoundingBoxFilter());
        featureFilterPanel.setCollapsed(guiConfig.isCollapseFeatureTypeFilter());
    }

    @Override
    public void setSettings() {
        try {
            Paths.get(browseText.getText());
        } catch (InvalidPathException e) {
            log.error("'" + browseText.getText().trim() + "' is not a valid file.");
            browseText.setText("");
        }

        // filter
        SimpleVisQuery query = config.getVisExportConfig().getQuery();

        query.setUseFeatureVersionFilter(useFeatureVersionFilter.isSelected());
        query.setUseAttributeFilter(useAttributeFilter.isSelected());
        query.setUseSQLFilter(useSQLFilter.isSelected());
        query.setUseBboxFilter(useBboxFilter.isSelected());
        query.setUseTypeNames(useFeatureFilter.isSelected());

        // tiling
        VisTiling spatialFilter = query.getBboxFilter();
        if (useTilingFilter.isSelected()) {
            if (manualTilingRadioButton.isSelected()) {
                spatialFilter.setMode(VisTilingMode.MANUAL);
            } else {
                spatialFilter.setMode(VisTilingMode.AUTOMATIC);
            }
        } else {
            spatialFilter.setMode(VisTilingMode.NO_TILING);
        }

        spatialFilter.getTilingOptions().setAutoTileSideLength(((Number) tileSizeText.getValue()).intValue());

        try {
            spatialFilter.setRows(((Number) rowsText.getValue()).intValue());
        } catch (NumberFormatException e) {
            spatialFilter.setRows(1);
        }

        try {
            spatialFilter.setColumns(((Number) columnsText.getValue()).intValue());
        } catch (NumberFormatException e) {
            spatialFilter.setColumns(1);
        }

        query.setFeatureVersionFilter(featureVersionFilter.toSettings());
        query.setAttributeFilter(attributeFilter.toSettings());
        query.setSQLFilter(sqlFilter.toSettings());
        query.getBboxFilter().setExtent(bboxFilter.toSettings());
        query.setFeatureTypeFilter(featureTypeFilter.toSettings());

        // display options
        VisExportConfig visExportConfig = config.getVisExportConfig();
        visExportConfig.setLodToExportFrom(lodComboBox.getSelectedIndex());
        visExportConfig.setAppearanceTheme((String) themeComboBox.getSelectedItem());

        for (DisplayFormType type : DisplayFormType.values()) {
            DisplayForm displayForm = DisplayForm.of(type);
            if (type.isAchievableFromLoD(visExportConfig.getLodToExportFrom())) {
                switch (type) {
                    case FOOTPRINT:
                        if (footprintCheckbox.isSelected()) {
                            displayForm.setActive(true);
                            displayForm.setVisibleFrom(footprintVisibleFromText.getValue() != null ?
                                    ((Number) footprintVisibleFromText.getValue()).intValue() : 0);
                        }
                        break;
                    case EXTRUDED:
                        if (extrudedCheckbox.isSelected()) {
                            displayForm.setActive(true);
                            displayForm.setVisibleFrom(extrudedVisibleFromText.getValue() != null ?
                                    ((Number) extrudedVisibleFromText.getValue()).intValue() : 0);
                        }
                        break;
                    case GEOMETRY:
                        if (geometryCheckbox.isSelected()) {
                            displayForm.setActive(true);
                            displayForm.setVisibleFrom(geometryVisibleFromText.getValue() != null ?
                                    ((Number) geometryVisibleFromText.getValue()).intValue() : 0);
                        }
                        break;
                    case COLLADA:
                        if (colladaCheckbox.isSelected()) {
                            displayForm.setActive(true);
                            displayForm.setVisibleFrom(colladaVisibleFromText.getValue() != null ?
                                    ((Number) colladaVisibleFromText.getValue()).intValue() : 0);
                        }
                        break;
                }
            }
            visExportConfig.getDisplayForms().add(displayForm);
        }

        int upperLevelVisibility = -1;
        DisplayFormType[] values = DisplayFormType.values();
        for (int i = values.length - 1; i >= 0; i--) {
            DisplayForm displayForm = visExportConfig.getDisplayForms().get(values[i]);
            if (displayForm.isActive()) {
                displayForm.setVisibleTo(upperLevelVisibility);
                upperLevelVisibility = displayForm.getVisibleFrom();
            }
        }

        // GUI specific settings
        VisExportGuiConfig guiConfig = config.getGuiConfig().getVisExportGuiConfig();
        guiConfig.setCollapseFeatureVersionFilter(featureVersionPanel.isCollapsed());
        guiConfig.setCollapseTilingFilter(tilingPanel.isCollapsed());
        guiConfig.setCollapseAttributeFilter(attributeFilterPanel.isCollapsed());
        guiConfig.setCollapseSQLFilter(sqlFilterPanel.isCollapsed());
        guiConfig.setCollapseBoundingBoxFilter(bboxFilterPanel.isCollapsed());
        guiConfig.setCollapseFeatureTypeFilter(featureFilterPanel.isCollapsed());
    }

    private void doExport() {
        final ReentrantLock lock = this.mainLock;
        lock.lock();
        try {
            viewController.clearConsole();
            setSettings();

            SimpleVisQuery query = config.getVisExportConfig().getQuery();

            // check all input values...
            if (browseText.getText().trim().isEmpty()) {
                viewController.errorMessage(Language.I18N.getString("visExport.dialog.error.incompleteData"),
                        Language.I18N.getString("visExport.dialog.error.incompleteData.dataset"));
                return;
            }

            // feature version filter
            if (query.isUseFeatureVersionFilter()) {
                SimpleFeatureVersionFilter featureVersionFilter = query.getFeatureVersionFilter();

                if (featureVersionFilter.getMode() == SimpleFeatureVersionFilterMode.AT
                        || featureVersionFilter.getMode() == SimpleFeatureVersionFilterMode.TERMINATED_AT) {
                    if (!featureVersionFilter.isSetStartDate()) {
                        viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
                                Language.I18N.getString("export.dialog.error.featureVersion.date"));
                        return;
                    }
                } else if (featureVersionFilter.getMode() == SimpleFeatureVersionFilterMode.BETWEEN) {
                    if (!featureVersionFilter.isSetStartDate()) {
                        viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
                                Language.I18N.getString("export.dialog.error.featureVersion.startDate"));
                        return;
                    } else if (!featureVersionFilter.isSetEndDate()) {
                        viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
                                Language.I18N.getString("export.dialog.error.featureVersion.endDate"));
                        return;
                    } else if (featureVersionFilter.getStartDate().compare(featureVersionFilter.getEndDate()) != DatatypeConstants.LESSER) {
                        viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
                                Language.I18N.getString("export.dialog.error.featureVersion.range"));
                        return;
                    }
                }
            }

            // attribute filter
            if (query.isUseAttributeFilter()) {
                SimpleAttributeFilter attributeFilter = query.getAttributeFilter();
                if (!attributeFilter.getResourceIdFilter().isSetResourceIds()
                        && !attributeFilter.getNameFilter().isSetLiteral()
                        && !attributeFilter.getLineageFilter().isSetLiteral()) {
                    viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
                            Language.I18N.getString("common.dialog.error.incorrectData.attributes"));
                    return;
                }
            }

            // SQL filter
            if (query.isUseSQLFilter()
                    && !query.getSQLFilter().isSetValue()) {
                viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
                        Language.I18N.getString("export.dialog.error.incorrectData.sql"));
                return;
            }

            // DisplayForms
            int activeDisplayFormsAmount = config.getVisExportConfig().getDisplayForms().getActiveDisplayFormsAmount();
            if (activeDisplayFormsAmount == 0) {
                viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
                        Language.I18N.getString("visExport.dialog.error.incorrectData.displayForms"));
                return;
            }

            // check API key when using the elevation API
            if (config.getVisExportConfig().getElevation().getAltitudeOffsetMode() == AltitudeOffsetMode.GENERIC_ATTRIBUTE
                    && config.getVisExportConfig().getElevation().isCallGElevationService()
                    && !config.getGlobalConfig().getApiKeys().isSetGoogleElevation()) {
                log.error("The Google Elevation API cannot be used due to a missing API key.");
                log.error("Please enter an API key or change the export preferences.");
                viewController.errorMessage(Language.I18N.getString("visExport.dialog.error.elevation"),
                        Language.I18N.getString("visExport.dialog.error.elevation.apiKey"));
                return;
            }

            // BoundingBox check
            if (query.isUseBboxFilter() && query.isSetBboxFilter()) {
                BoundingBox bbox = query.getBboxFilter().getExtent();
                Double xMin = bbox.getLowerCorner().getX();
                Double yMin = bbox.getLowerCorner().getY();
                Double xMax = bbox.getUpperCorner().getX();
                Double yMax = bbox.getUpperCorner().getY();

                if (xMin == null || yMin == null || xMax == null || yMax == null) {
                    viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
                            Language.I18N.getString("common.dialog.error.incorrectData.bbox"));
                    return;
                }
            }

            // Feature classes check
            if (query.isUseTypeNames() && query.getFeatureTypeFilter().getTypeNames().isEmpty()) {
                viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
                        Language.I18N.getString("common.dialog.error.incorrectData.featureClass"));
                return;
            }

            // check collada2gltf tool
            if (config.getVisExportConfig().getGltfOptions().isCreateGltfModel()) {
                Path collada2gltf = Paths.get(config.getVisExportConfig().getGltfOptions().getPathToConverter());
                if (!collada2gltf.isAbsolute())
                    collada2gltf = CoreConstants.IMPEXP_HOME.resolve(collada2gltf);

                if (!Files.exists(collada2gltf)) {
                    String text = Language.I18N.getString("visExport.dialog.error.collada2gltf.notExists");
                    Object[] args = new Object[]{collada2gltf.toString()};
                    String result = MessageFormat.format(text, args);
                    viewController.errorMessage(Language.I18N.getString("visExport.dialog.error.collada2gltf"), result);
                    return;
                } else if (!Files.isExecutable(collada2gltf)) {
                    // grant permission to COLLADA2GLTF binaries
                    log.info("Acquiring permission to execute the COLLADA2GLTF binary");

                    // file permissions 755
                    Set<PosixFilePermission> permissions = new HashSet<>();
                    permissions.add(PosixFilePermission.OWNER_READ);
                    permissions.add(PosixFilePermission.OWNER_WRITE);
                    permissions.add(PosixFilePermission.OWNER_EXECUTE);
                    permissions.add(PosixFilePermission.GROUP_READ);
                    permissions.add(PosixFilePermission.GROUP_EXECUTE);
                    permissions.add(PosixFilePermission.OTHERS_READ);
                    permissions.add(PosixFilePermission.OTHERS_EXECUTE);

                    try {
                        Files.setPosixFilePermissions(Paths.get(config.getVisExportConfig().getGltfOptions().getPathToConverter()), permissions);
                    } catch (IOException e) {
                        String text = Language.I18N.getString("visExport.dialog.error.collada2gltf.notExecutable");
                        Object[] args = new Object[]{collada2gltf.toString()};
                        String result = MessageFormat.format(text, args);
                        viewController.errorMessage(Language.I18N.getString("visExport.dialog.error.collada2gltf"), result);
                        return;
                    }
                }
            }

            if (!databaseController.connect()) {
                return;
            }

            // warn the non-supported CityGML ADEs
            if (showADEWarningDialog() != JOptionPane.OK_OPTION) {
                log.warn("Database export canceled.");
                return;
            }

            viewController.setStatusText(Language.I18N.getString("main.status.visExport.label"));
            log.info("Initializing database export...");

            VisExporter visExporter = new VisExporter();

            // initialize event dispatcher
            EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
            ExportStatusDialog exportDialog = new ExportStatusDialog(viewController.getTopFrame(),
                    Language.I18N.getString("visExport.dialog.window"),
                    Language.I18N.getString("export.dialog.msg"),
                    true);

            SwingUtilities.invokeLater(() -> {
                exportDialog.setLocationRelativeTo(viewController.getTopFrame());
                exportDialog.setVisible(true);
            });

            exportDialog.getCancelButton().addActionListener(e ->
                    SwingUtilities.invokeLater(() -> eventDispatcher.triggerEvent(new InterruptEvent(
                            "User abort of database export.",
                            LogLevel.WARN,
                            visExporter.getEventChannel()))));

            boolean success = false;
            try {
                success = visExporter.doExport(Paths.get(browseText.getText()));
            } catch (VisExportException e) {
                log.error(e.getMessage(), e.getCause());
                switch (e.getErrorCode()) {
                    case MISSING_GOOGLE_API_KEY:
                        log.error("Please enter an API key or change the export preferences.");
                        break;
                    case SPATIAL_INDEXES_NOT_ACTIVATED:
                        log.error("Please use the database tab to activate the spatial indexes.");
                        break;
                }
            }

            SwingUtilities.invokeLater(exportDialog::dispose);
            viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));

            if (success) {
                log.info("Database export successfully finished.");
            } else {
                log.warn("Database export aborted.");
            }
        } catch (Exception e) {
            log.logStackTrace(e);
        } finally {
            lock.unlock();
        }
    }

    private void setVisibilityEnabledValues() {
        extrudedCheckbox.setEnabled(DisplayFormType.EXTRUDED.isAchievableFromLoD(lodComboBox.getSelectedIndex()));
        geometryCheckbox.setEnabled(DisplayFormType.GEOMETRY.isAchievableFromLoD(lodComboBox.getSelectedIndex()));
        colladaCheckbox.setEnabled(DisplayFormType.COLLADA.isAchievableFromLoD(lodComboBox.getSelectedIndex()));

        visibleFromFootprintLabel.setEnabled(footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());
        footprintVisibleFromText.setEnabled(footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());
        pixelsFootprintLabel.setEnabled(footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());

        visibleFromExtrudedLabel.setEnabled(extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());
        extrudedVisibleFromText.setEnabled(extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());
        pixelsExtrudedLabel.setEnabled(extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());

        visibleFromGeometryLabel.setEnabled(geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());
        geometryVisibleFromText.setEnabled(geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());
        pixelsGeometryLabel.setEnabled(geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());

        visibleFromColladaLabel.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
        colladaVisibleFromText.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
        pixelsColladaLabel.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());

        themeLabel.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
        themeComboBox.setEnabled(databaseController.isConnected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
        fetchThemesButton.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());

        boolean enable = lodComboBox.getSelectedIndex() > 0;
        FeatureTypeTree featureTree = featureTypeFilter.getFeatureTypeTree();
        featureTree.setPathEnabled("Bridge", BridgeModule.v2_0_0.getNamespaceURI(), enable);
        featureTree.setPathEnabled("CityFurniture", CityFurnitureModule.v2_0_0.getNamespaceURI(), enable);
        featureTree.setPathEnabled("CityObjectGroup", CityObjectGroupModule.v2_0_0.getNamespaceURI(), enable);
        featureTree.setPathEnabled("ReliefFeature", ReliefModule.v2_0_0.getNamespaceURI(), enable);
        featureTree.setPathEnabled("Tunnel", TunnelModule.v2_0_0.getNamespaceURI(), enable);
        featureTree.setPathEnabled("SolitaryVegetationObject", VegetationModule.v2_0_0.getNamespaceURI(), enable);
        featureTree.setPathEnabled("PlantCover", VegetationModule.v2_0_0.getNamespaceURI(), enable);
        featureTree.repaint();
    }

    private void saveFile(String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("KML Files (*.kml)", "kml");
        chooser.addChoosableFileFilter(filter);
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.setFileFilter(filter);

        if (browseText.getText().trim().isEmpty()) {
            chooser.setCurrentDirectory(config.getVisExportConfig().getPath().isSetLastUsedMode() ?
                    new File(config.getVisExportConfig().getPath().getLastUsedPath()) :
                    new File(config.getVisExportConfig().getPath().getStandardPath()));
        } else {
            File file = new File(browseText.getText().trim());
            if (!file.isDirectory())
                file = file.getParentFile();

            chooser.setCurrentDirectory(file);
        }

        int result = chooser.showSaveDialog(getTopLevelAncestor());
        if (result == JFileChooser.CANCEL_OPTION) return;
        try {
            String exportString = chooser.getSelectedFile().toString();

            if (!exportString.trim().isEmpty() && chooser.getSelectedFile().getName().contains(".")) {
                exportString = Util.stripFileExtension(exportString);
            }

            exportString += ".kml";

            browseText.setText(exportString);
            config.getVisExportConfig().getPath().setLastUsedPath(chooser.getCurrentDirectory().getAbsolutePath());
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        DatabaseConnectionStateEvent state = (DatabaseConnectionStateEvent) event;
        themeComboBox.removeAllItems();
        themeComboBox.addItem(VisExportConfig.THEME_NONE);
        themeComboBox.setSelectedItem(VisExportConfig.THEME_NONE);
        if (!state.isConnected())
            themeComboBox.setEnabled(false);
    }

    private class ThemeUpdater extends SwingWorker<Void, Void> {
        protected Void doInBackground() {
            Thread.currentThread().setName(this.getClass().getSimpleName());
            fetchThemesButton.setEnabled(false);

            try {
                DatabaseConnection conn = config.getDatabaseConfig().getActiveConnection();

                if (!databaseController.isConnected() && viewController.showOptionDialog(
                        Language.I18N.getString("common.dialog.dbConnect.title"),
                        MessageFormat.format(Language.I18N.getString("common.dialog.dbConnect.message"),
                                conn.getDescription(), conn.toConnectString()),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    databaseController.connect();
                }

                if (databaseController.isConnected()) {
                    themeComboBox.removeAllItems();
                    themeComboBox.addItem(VisExportConfig.THEME_NONE);
                    themeComboBox.setSelectedItem(VisExportConfig.THEME_NONE);

                    // fetching themes
                    for (String theme : databaseController.getActiveDatabaseAdapter().getUtil().getAppearanceThemeList()) {
                        if (theme != null) {
                            themeComboBox.addItem(theme);
                            if (theme.equals(config.getVisExportConfig().getAppearanceTheme())) {
                                themeComboBox.setSelectedItem(theme);
                            }
                        }
                    }

                    themeComboBox.setEnabled(true);
                    themeComboBox.setPopupVisible(true);
                }
            } catch (SQLException e) {
                log.error("Failed to query appearance themes from database.", e);
            } finally {
                fetchThemesButton.setEnabled(true);
            }

            return null;
        }
    }

    private int showADEWarningDialog() {
        int selectedOption = JOptionPane.OK_OPTION;
        List<ADEExtension> unsupported = ADEVisExportExtensionManager.getInstance().getUnsupportedADEExtensions();

        if (config.getGuiConfig().getVisExportGuiConfig().isShowVisExportUnsupportedADEWarning() && !unsupported.isEmpty()) {
            String formattedMessage = MessageFormat.format(Language.I18N.getString("visExport.dialog.warn.ade.unsupported"),
                    Util.collection2string(unsupported.stream().map(ade -> ade.getMetadata().getName()).collect(Collectors.toList()), "<br>"));

            ConfirmationCheckDialog dialog = ConfirmationCheckDialog.defaults()
                    .withParentComponent(viewController.getTopFrame())
                    .withTitle(Language.I18N.getString("common.dialog.warning.title"))
                    .withOptionType(JOptionPane.YES_NO_OPTION)
                    .withMessageType(JOptionPane.WARNING_MESSAGE)
                    .addMessage(formattedMessage);

            selectedOption = dialog.show();
            config.getGuiConfig().getVisExportGuiConfig().setShowVisExportUnsupportedADEWarning(dialog.keepShowingDialog());
        }

        return selectedOption;
    }
}
