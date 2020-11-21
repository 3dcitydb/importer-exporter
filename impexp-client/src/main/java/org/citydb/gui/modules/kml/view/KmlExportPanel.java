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
package org.citydb.gui.modules.kml.view;

import org.citydb.ade.kmlExporter.ADEKmlExportExtension;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.kmlExporter.AltitudeOffsetMode;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.DisplayFormType;
import org.citydb.config.project.kmlExporter.KmlExportConfig;
import org.citydb.config.project.kmlExporter.KmlTiling;
import org.citydb.config.project.kmlExporter.KmlTilingMode;
import org.citydb.config.project.kmlExporter.SimpleKmlQuery;
import org.citydb.config.project.kmlExporter.SimpleKmlQueryMode;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.database.DatabaseController;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.EventType;
import org.citydb.event.global.InterruptEvent;
import org.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import org.citydb.gui.components.common.BlankNumberFormatter;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.components.dialog.ExportStatusDialog;
import org.citydb.gui.components.feature.FeatureTypeTree;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.modules.kml.controller.KmlExportException;
import org.citydb.modules.kml.controller.KmlExporter;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.ClientConstants;
import org.citydb.util.Util;
import org.citygml4j.model.module.citygml.BridgeModule;
import org.citygml4j.model.module.citygml.CityFurnitureModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CityObjectGroupModule;
import org.citygml4j.model.module.citygml.ReliefModule;
import org.citygml4j.model.module.citygml.TunnelModule;
import org.citygml4j.model.module.citygml.VegetationModule;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class KmlExportPanel extends JPanel implements EventHandler {
    private final Logger log = Logger.getInstance();
    private final ReentrantLock mainLock = new ReentrantLock();
    private final ViewController viewController;
    private final DatabaseController databaseController;
    private final Config config;

    private JTextField browseText;
    private JButton browseButton;

    private TitledPanel gmlIdPanel;
    private TitledPanel bboxPanel;
    private TitledPanel lodPanel;
    private TitledPanel displayAsPanel;
	private TitledPanel featureFilterPanel;

    private JRadioButton gmlIdRadioButton;
    private JLabel gmlIdLabel;
    private JTextField gmlIdText;
    private JRadioButton bboxRadioButton;
    private BoundingBoxPanel bboxComponent;

    private JLabel tilingLabel;
    private JRadioButton noTilingRadioButton;
    private JRadioButton automaticTilingRadioButton;
    private JRadioButton manualTilingRadioButton;
    private JLabel rowsLabel;
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

    private FeatureTypeTree typeTree;
    private JCheckBox useFeatureFilter;

    private JButton exportButton;

    public KmlExportPanel(ViewController viewController, Config config) {
        this.viewController = viewController;
        this.config = config;

        databaseController = ObjectRegistry.getInstance().getDatabaseController();
        ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);

        initGui();
        addListeners();
    }

    private void initGui() {
        browseText = new JTextField();
        browseButton = new JButton();
        JPanel browsePanel = new JPanel();
        browsePanel.setLayout(new GridBagLayout());
        browsePanel.add(browseText, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 5));
        browsePanel.add(browseButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 0));

        gmlIdRadioButton = new JRadioButton();
        bboxRadioButton = new JRadioButton("");
        gmlIdLabel = new JLabel("gml:id");
        gmlIdText = new JTextField();

        ButtonGroup contentButtongGroup = new ButtonGroup();
        contentButtongGroup.add(gmlIdRadioButton);
        contentButtongGroup.add(bboxRadioButton);

        bboxComponent = viewController.getComponentFactory().createBoundingBoxPanel();
        tilingLabel = new JLabel();
        noTilingRadioButton = new JRadioButton();
        automaticTilingRadioButton = new JRadioButton();
        manualTilingRadioButton = new JRadioButton();
        rowsLabel = new JLabel();
        columnsLabel = new JLabel();

        ButtonGroup tilingButtonGroup = new ButtonGroup();
        tilingButtonGroup.add(noTilingRadioButton);
        tilingButtonGroup.add(automaticTilingRadioButton);
        tilingButtonGroup.add(manualTilingRadioButton);

        BlankNumberFormatter tileFormat = new BlankNumberFormatter(new DecimalFormat("#######"));
        tileFormat.setLimits(0, 9999999);
        columnsText = new JFormattedTextField(tileFormat);
        rowsText = new JFormattedTextField(tileFormat);

        lodComboBox = new JComboBox<>();
        for (int index = 0; index < 5; index++) {
            lodComboBox.insertItemAt("LoD" + index, index);
        }

        lodComboBox.insertItemAt(Language.I18N.getString("kmlExport.label.highestLODAvailable"), lodComboBox.getItemCount());
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

        BlankNumberFormatter visibleFromFormatter = new BlankNumberFormatter(new DecimalFormat("####"));
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

        useFeatureFilter = new JCheckBox();
        typeTree = new FeatureTypeTree(CityGMLVersion.v2_0_0, e -> e instanceof ADEKmlExportExtension);
        typeTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 1);

        // get rid of standard icons
        DefaultCheckboxTreeCellRenderer renderer = (DefaultCheckboxTreeCellRenderer) typeTree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);

        exportButton = new JButton();

        setLayout(new GridBagLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        {
            // gml:id
            JPanel gmlIdConent = new JPanel();
            gmlIdConent.setLayout(new GridBagLayout());
            gmlIdConent.add(gmlIdLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
            gmlIdConent.add(gmlIdText, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));

            gmlIdPanel = new TitledPanel()
                    .withToggleButton(gmlIdRadioButton)
                    .build(gmlIdConent);

            mainPanel.add(gmlIdPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
        {
            // bbox
            JPanel tilingPanel = new JPanel();
            tilingPanel.setLayout(new GridBagLayout());
            tilingPanel.add(tilingLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
            tilingPanel.add(noTilingRadioButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 15, 0, 5));
            tilingPanel.add(automaticTilingRadioButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
            tilingPanel.add(manualTilingRadioButton, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
            tilingPanel.add(rowsLabel, GuiUtil.setConstraints(4, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 25, 0, 5));
            tilingPanel.add(rowsText, GuiUtil.setConstraints(5, 0, 0.5, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
            tilingPanel.add(columnsLabel, GuiUtil.setConstraints(6, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, 0, 5));
            tilingPanel.add(columnsText, GuiUtil.setConstraints(7, 0, 0.5, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            bboxComponent.addComponent(tilingPanel);

            bboxPanel = new TitledPanel()
                    .withToggleButton(bboxRadioButton)
                    .build(bboxComponent);

            mainPanel.add(bboxPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
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
			displayAsContent.add(pixelsFootprintLabel, GuiUtil.setConstraints(3, 0, 0, 1, GridBagConstraints.BOTH, 0, 5, 0, 0));
			displayAsContent.add(extrudedCheckbox, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 5));
			displayAsContent.add(visibleFromExtrudedLabel, GuiUtil.setConstraints(1, 1, 0, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 5, 5, 0, 5));
			displayAsContent.add(extrudedVisibleFromText, GuiUtil.setConstraints(2, 1, 0, 1, GridBagConstraints.HORIZONTAL, 5, 5, 0, 5));
			displayAsContent.add(pixelsExtrudedLabel, GuiUtil.setConstraints(3, 1, 0, 1, GridBagConstraints.BOTH, 5, 5, 0, 0));
			displayAsContent.add(geometryCheckbox, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 5));
			displayAsContent.add(visibleFromGeometryLabel, GuiUtil.setConstraints(1, 2, 0, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 5, 5, 0, 5));
			displayAsContent.add(geometryVisibleFromText, GuiUtil.setConstraints(2, 2, 0, 1, GridBagConstraints.HORIZONTAL, 5, 5, 0, 5));
			displayAsContent.add(pixelsGeometryLabel, GuiUtil.setConstraints(3, 2, 0, 1, GridBagConstraints.BOTH, 5, 5, 0, 0));
			displayAsContent.add(colladaCheckbox, GuiUtil.setConstraints(0, 3, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 5));
			displayAsContent.add(visibleFromColladaLabel, GuiUtil.setConstraints(1, 3, 0, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 5, 5, 0, 5));
			displayAsContent.add(colladaVisibleFromText, GuiUtil.setConstraints(2, 3, 0, 1, GridBagConstraints.HORIZONTAL, 5, 5, 0, 5));
			displayAsContent.add(pixelsColladaLabel, GuiUtil.setConstraints(3, 3, 0, 1, GridBagConstraints.BOTH, 5, 5, 0, 0));

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

			mainPanel.add(content, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
        {
            JPanel content = new JPanel();
            content.setBorder(UIManager.getBorder("ScrollPane.border"));
            content.setLayout(new GridBagLayout());
            {
                content.add(typeTree, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
            }

            featureFilterPanel = new TitledPanel()
                    .withToggleButton(useFeatureFilter)
                    .build(content);

            mainPanel.add(featureFilterPanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
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

        PopupMenuDecorator.getInstance().decorate(browseText, gmlIdText, rowsText, columnsText,
                footprintVisibleFromText, extrudedVisibleFromText, geometryVisibleFromText, colladaVisibleFromText,
                typeTree);
    }

    public void doTranslation() {
        browseButton.setText(Language.I18N.getString("common.button.browse"));
        gmlIdPanel.setTitle(Language.I18N.getString("kmlExport.label.singleBuilding"));
        bboxPanel.setTitle(Language.I18N.getString("filter.border.boundingBox"));

        tilingLabel.setText(Language.I18N.getString("pref.export.boundingBox.border.tiling"));
        noTilingRadioButton.setText(Language.I18N.getString("kmlExport.label.noTiling"));
        manualTilingRadioButton.setText(Language.I18N.getString("kmlExport.label.manual"));
        rowsLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.rows"));
        columnsLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.columns"));
        automaticTilingRadioButton.setText(Language.I18N.getString("kmlExport.label.automatic"));

        lodPanel.setTitle(Language.I18N.getString("kmlExport.label.fromLOD"));
        int selectedIndex = lodComboBox.getSelectedIndex();
        lodComboBox.removeItemAt(lodComboBox.getItemCount() - 1);
        lodComboBox.insertItemAt(Language.I18N.getString("kmlExport.label.highestLODAvailable"), lodComboBox.getItemCount());
        lodComboBox.setSelectedIndex(selectedIndex);
        lodComboBox.repaint();

        displayAsPanel.setTitle(Language.I18N.getString("kmlExport.label.displayAs"));
        footprintCheckbox.setText(Language.I18N.getString("kmlExport.label.footprint"));
        extrudedCheckbox.setText(Language.I18N.getString("kmlExport.label.extruded"));
        geometryCheckbox.setText(Language.I18N.getString("kmlExport.label.geometry"));
        colladaCheckbox.setText(Language.I18N.getString("kmlExport.label.collada"));

        visibleFromFootprintLabel.setText(Language.I18N.getString("kmlExport.label.visibleFrom"));
        pixelsFootprintLabel.setText(Language.I18N.getString("kmlExport.label.pixels"));
        visibleFromExtrudedLabel.setText(Language.I18N.getString("kmlExport.label.visibleFrom"));
        pixelsExtrudedLabel.setText(Language.I18N.getString("kmlExport.label.pixels"));
        visibleFromGeometryLabel.setText(Language.I18N.getString("kmlExport.label.visibleFrom"));
        pixelsGeometryLabel.setText(Language.I18N.getString("kmlExport.label.pixels"));
        visibleFromColladaLabel.setText(Language.I18N.getString("kmlExport.label.visibleFrom"));
        pixelsColladaLabel.setText(Language.I18N.getString("kmlExport.label.pixels"));

        themeLabel.setText(Language.I18N.getString("pref.kmlexport.label.theme"));
        fetchThemesButton.setText(Language.I18N.getString("pref.kmlexport.label.fetchTheme"));

        featureFilterPanel.setTitle(Language.I18N.getString("filter.border.featureClass"));
        exportButton.setText(Language.I18N.getString("export.button.export"));
    }

    public void loadSettings() {
        // filter
        SimpleKmlQuery query = config.getKmlExportConfig().getQuery();

        useFeatureFilter.setSelected(query.isUseTypeNames());
        if (query.getMode() == SimpleKmlQueryMode.SINGLE)
            gmlIdRadioButton.setSelected(true);
        else
            bboxRadioButton.setSelected(true);

        // feature type filter
        FeatureTypeFilter featureTypeFilter = query.getFeatureTypeFilter();
        typeTree.getCheckingModel().clearChecking();
        typeTree.setSelected(featureTypeFilter.getTypeNames());

        // gml:id filter
        ResourceIdOperator gmlIdFilter = query.getGmlIdFilter();
        gmlIdText.setText(String.join(",", gmlIdFilter.getResourceIds()));

        // bbox filter
        KmlTiling bboxFilter = query.getBboxFilter();
        BoundingBox bbox = bboxFilter.getExtent();
        if (bbox != null)
            bboxComponent.setBoundingBox(bboxFilter.getExtent());

        // tiling
        switch (bboxFilter.getMode()) {
            case AUTOMATIC:
                automaticTilingRadioButton.setSelected(true);
                break;
            case MANUAL:
                manualTilingRadioButton.setSelected(true);
                break;
            default:
                noTilingRadioButton.setSelected(true);
        }

        rowsText.setText(String.valueOf(bboxFilter.getRows()));
        columnsText.setText(String.valueOf(bboxFilter.getColumns()));

        // display options
        KmlExportConfig kmlExportConfig = config.getKmlExportConfig();

        int lod = kmlExportConfig.getLodToExportFrom();
        lod = lod >= lodComboBox.getItemCount() ? lodComboBox.getItemCount() - 1 : lod;
        lodComboBox.setSelectedIndex(lod);

        for (DisplayForm displayForm : kmlExportConfig.getDisplayForms().values()) {
            switch (displayForm.getType()) {
                case FOOTPRINT:
                    if (displayForm.isActive()) {
                        footprintCheckbox.setSelected(true);
                        footprintVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
                    }
                    break;
                case EXTRUDED:
                    if (displayForm.isActive()) {
                        extrudedCheckbox.setSelected(true);
                        extrudedVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
                    }
                    break;
                case GEOMETRY:
                    if (displayForm.isActive()) {
                        geometryCheckbox.setSelected(true);
                        geometryVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
                    }
                    break;
                case COLLADA:
                    if (displayForm.isActive()) {
                        colladaCheckbox.setSelected(true);
                        colladaVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
                    }
                    break;
            }
        }

        themeComboBox.removeAllItems();
        themeComboBox.addItem(KmlExportConfig.THEME_NONE);
        themeComboBox.setSelectedItem(KmlExportConfig.THEME_NONE);

        setFilterEnabledValues();
    }

    public void setSettings() {
        try {
            Paths.get(browseText.getText());
        } catch (InvalidPathException e) {
            log.error("'" + browseText.getText().trim() + "' is not a valid file.");
            browseText.setText("");
        }

        // filter
        SimpleKmlQuery query = config.getKmlExportConfig().getQuery();

        query.setUseTypeNames(useFeatureFilter.isSelected());
        query.setMode(gmlIdRadioButton.isSelected() ? SimpleKmlQueryMode.SINGLE : SimpleKmlQueryMode.BBOX);

        // feature type filter
        FeatureTypeFilter featureTypeFilter = query.getFeatureTypeFilter();
        featureTypeFilter.reset();
        featureTypeFilter.setTypeNames(typeTree.getSelectedTypeNames());

        // gml:id filter
        ResourceIdOperator gmlIdFilter = query.getGmlIdFilter();
        gmlIdFilter.reset();
        if (gmlIdText.getText().trim().length() > 0) {
            String trimmed = gmlIdText.getText().replaceAll("\\s+", "");
            gmlIdFilter.setResourceIds(Util.string2string(trimmed, ","));
        }

        // bbox filter
        KmlTiling bboxFilter = query.getBboxFilter();
        bboxFilter.setExtent(bboxComponent.getBoundingBox());

        // tiling
        if (automaticTilingRadioButton.isSelected())
            bboxFilter.setMode(KmlTilingMode.AUTOMATIC);
        else if (manualTilingRadioButton.isSelected())
            bboxFilter.setMode(KmlTilingMode.MANUAL);
        else
            bboxFilter.setMode(KmlTilingMode.NO_TILING);

        try {
            bboxFilter.setRows(Integer.parseInt(rowsText.getText().trim()));
        } catch (NumberFormatException e) {
            bboxFilter.setRows(1);
        }

        try {
            bboxFilter.setColumns(Integer.parseInt(columnsText.getText().trim()));
        } catch (NumberFormatException e) {
            bboxFilter.setColumns(1);
        }

        // display options
        KmlExportConfig kmlExportConfig = config.getKmlExportConfig();
        kmlExportConfig.setLodToExportFrom(lodComboBox.getSelectedIndex());
        kmlExportConfig.setAppearanceTheme((String) themeComboBox.getSelectedItem());

        for (DisplayFormType type : DisplayFormType.values()) {
            DisplayForm displayForm = DisplayForm.of(type);
            if (type.isAchievableFromLoD(kmlExportConfig.getLodToExportFrom())) {
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
            kmlExportConfig.getDisplayForms().add(displayForm);
        }

        int upperLevelVisibility = -1;
        DisplayFormType[] values = DisplayFormType.values();
        for (int i = values.length - 1; i >= 0; i--) {
            DisplayForm displayForm = kmlExportConfig.getDisplayForms().get(values[i]);
            if (displayForm.isActive()) {
                displayForm.setVisibleTo(upperLevelVisibility);
                upperLevelVisibility = displayForm.getVisibleFrom();
            }
        }
    }

    private void addListeners() {
        exportButton.addActionListener(e -> new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                doExport();
                return null;
            }
        }.execute());

        browseButton.addActionListener(e -> saveFile());
        ActionListener filterListener = e -> setFilterEnabledValues();

        gmlIdRadioButton.addActionListener(filterListener);
        bboxRadioButton.addActionListener(filterListener);
        noTilingRadioButton.addActionListener(filterListener);
        manualTilingRadioButton.addActionListener(filterListener);
        automaticTilingRadioButton.addActionListener(filterListener);
        columnsText.addPropertyChangeListener("value", evt -> checkNonNegative(columnsText));
        rowsText.addPropertyChangeListener("value", evt -> checkNonNegative(rowsText));

        lodComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
                setVisibilityEnabledValues();
        });

        footprintCheckbox.addActionListener(e -> setVisibilityEnabledValues());
        extrudedCheckbox.addActionListener(e -> setVisibilityEnabledValues());
        geometryCheckbox.addActionListener(e -> setVisibilityEnabledValues());
        colladaCheckbox.addActionListener(e -> setVisibilityEnabledValues());
        fetchThemesButton.addActionListener(e -> new ThemeUpdater().execute());

        useFeatureFilter.addActionListener(e -> setEnabledFeatureFilter());
    }

    private void doExport() {
        final ReentrantLock lock = this.mainLock;
        lock.lock();
        try {
            viewController.clearConsole();
            setSettings();

            SimpleKmlQuery query = config.getKmlExportConfig().getQuery();

            // check all input values...
            if (browseText.getText().trim().isEmpty()) {
                viewController.errorMessage(Language.I18N.getString("kmlExport.dialog.error.incompleteData"),
                        Language.I18N.getString("kmlExport.dialog.error.incompleteData.dataset"));
                return;
            }

            // gmlId
            if (query.getMode() == SimpleKmlQueryMode.SINGLE
                    && !query.getGmlIdFilter().isSetResourceIds()) {
                viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
                        Language.I18N.getString("common.dialog.error.incorrectData.gmlId"));
                return;
            }

            // DisplayForms
            int activeDisplayFormsAmount = config.getKmlExportConfig().getDisplayForms().getActiveDisplayFormsAmount();
            if (activeDisplayFormsAmount == 0) {
                viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
                        Language.I18N.getString("kmlExport.dialog.error.incorrectData.displayForms"));
                return;
            }

            // check API key when using the elevation API
            if (config.getKmlExportConfig().getAltitudeOffsetMode() == AltitudeOffsetMode.GENERIC_ATTRIBUTE
                    && config.getKmlExportConfig().isCallGElevationService()
                    && !config.getGlobalConfig().getApiKeys().isSetGoogleElevation()) {
                log.error("The Google Elevation API cannot be used due to a missing API key.");
                log.error("Please enter an API key or change the export preferences.");
                viewController.errorMessage(Language.I18N.getString("kmlExport.dialog.error.elevation"),
                        Language.I18N.getString("kmlExport.dialog.error.elevation.apiKey"));
                return;
            }

            // BoundingBox check
            if (query.getMode() == SimpleKmlQueryMode.BBOX && query.isSetBboxFilter()) {
                BoundingBox bbox = query.getBboxFilter().getExtent();
                Double xMin = bbox.getLowerCorner().getX();
                Double yMin = bbox.getLowerCorner().getY();
                Double xMax = bbox.getUpperCorner().getX();
                Double yMax = bbox.getUpperCorner().getY();

                if (xMin == null || yMin == null || xMax == null || yMax == null) {
                    viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
                            Language.I18N.getString("common.dialog.error.incorrectData.bbox"));
                    return;
                }
            }

            // Feature classes check
            if (query.isUseTypeNames() && query.getFeatureTypeFilter().getTypeNames().isEmpty()) {
                viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
                        Language.I18N.getString("common.dialog.error.incorrectData.featureClass"));
                return;
            }

            // check collada2gltf tool
            if (config.getKmlExportConfig().isCreateGltfModel()) {
                Path collada2gltf = Paths.get(config.getKmlExportConfig().getPathOfGltfConverter());
                if (!collada2gltf.isAbsolute())
                    collada2gltf = ClientConstants.IMPEXP_HOME.resolve(collada2gltf);

                if (!Files.exists(collada2gltf)) {
                    String text = Language.I18N.getString("kmlExport.dialog.error.collada2gltf.notExists");
                    Object[] args = new Object[]{collada2gltf.toString()};
                    String result = MessageFormat.format(text, args);
                    viewController.errorMessage(Language.I18N.getString("kmlExport.dialog.error.collada2gltf"), result);
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
                        Files.setPosixFilePermissions(Paths.get(config.getKmlExportConfig().getPathOfGltfConverter()), permissions);
                    } catch (IOException e) {
                        String text = Language.I18N.getString("kmlExport.dialog.error.collada2gltf.notExecutable");
                        Object[] args = new Object[]{collada2gltf.toString()};
                        String result = MessageFormat.format(text, args);
                        viewController.errorMessage(Language.I18N.getString("kmlExport.dialog.error.collada2gltf"), result);
                        return;
                    }
                }
            }

            if (!databaseController.connect()) {
                return;
            }

            viewController.setStatusText(Language.I18N.getString("main.status.kmlExport.label"));
            log.info("Initializing database export...");

            // get event dispatcher
            final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
            final ExportStatusDialog exportDialog = new ExportStatusDialog(viewController.getTopFrame(),
                    Language.I18N.getString("kmlExport.dialog.window"),
                    Language.I18N.getString("export.dialog.msg"),
                    true);

            SwingUtilities.invokeLater(() -> {
                exportDialog.setLocationRelativeTo(viewController.getTopFrame());
                exportDialog.setVisible(true);
            });

            exportDialog.getCancelButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            eventDispatcher.triggerEvent(new InterruptEvent(
                                    "User abort of database export.",
                                    LogLevel.WARN,
                                    Event.GLOBAL_CHANNEL,
                                    this));
                        }
                    });
                }
            });

            boolean success = false;
            try {
                success = new KmlExporter().doExport(Paths.get(browseText.getText()));
            } catch (KmlExportException e) {
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

    private void setFilterEnabledValues() {
        gmlIdLabel.setEnabled(gmlIdRadioButton.isSelected());
        gmlIdText.setEnabled(gmlIdRadioButton.isSelected());
        bboxComponent.setEnabled(bboxRadioButton.isSelected());

        tilingLabel.setEnabled(bboxRadioButton.isSelected());
        noTilingRadioButton.setEnabled(bboxRadioButton.isSelected());
        automaticTilingRadioButton.setEnabled(bboxRadioButton.isSelected());
        manualTilingRadioButton.setEnabled(bboxRadioButton.isSelected());

        rowsLabel.setEnabled(manualTilingRadioButton.isEnabled() && manualTilingRadioButton.isSelected());
        rowsText.setEnabled(manualTilingRadioButton.isEnabled() && manualTilingRadioButton.isSelected());
        columnsLabel.setEnabled(manualTilingRadioButton.isEnabled() && manualTilingRadioButton.isSelected());
        columnsText.setEnabled(manualTilingRadioButton.isEnabled() && manualTilingRadioButton.isSelected());

        setVisibilityEnabledValues();
        setEnabledFeatureFilter();
    }

    private void setVisibilityEnabledValues() {
        extrudedCheckbox.setEnabled(DisplayFormType.EXTRUDED.isAchievableFromLoD(lodComboBox.getSelectedIndex()));
        geometryCheckbox.setEnabled(DisplayFormType.GEOMETRY.isAchievableFromLoD(lodComboBox.getSelectedIndex()));
        colladaCheckbox.setEnabled(DisplayFormType.COLLADA.isAchievableFromLoD(lodComboBox.getSelectedIndex()));

        visibleFromFootprintLabel.setEnabled(bboxRadioButton.isSelected() && footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());
        footprintVisibleFromText.setEnabled(bboxRadioButton.isSelected() && footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());
        pixelsFootprintLabel.setEnabled(bboxRadioButton.isSelected() && footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());

        visibleFromExtrudedLabel.setEnabled(bboxRadioButton.isSelected() && extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());
        extrudedVisibleFromText.setEnabled(bboxRadioButton.isSelected() && extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());
        pixelsExtrudedLabel.setEnabled(bboxRadioButton.isSelected() && extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());

        visibleFromGeometryLabel.setEnabled(bboxRadioButton.isSelected() && geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());
        geometryVisibleFromText.setEnabled(bboxRadioButton.isSelected() && geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());
        pixelsGeometryLabel.setEnabled(bboxRadioButton.isSelected() && geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());

        visibleFromColladaLabel.setEnabled(bboxRadioButton.isSelected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
        colladaVisibleFromText.setEnabled(bboxRadioButton.isSelected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
        pixelsColladaLabel.setEnabled(bboxRadioButton.isSelected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());

        themeLabel.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
        themeComboBox.setEnabled(databaseController.isConnected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
        fetchThemesButton.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());

        boolean enable = lodComboBox.getSelectedIndex() > 0;
        typeTree.setPathEnabled("Bridge", BridgeModule.v2_0_0.getNamespaceURI(), enable);
        typeTree.setPathEnabled("CityFurniture", CityFurnitureModule.v2_0_0.getNamespaceURI(), enable);
        typeTree.setPathEnabled("CityObjectGroup", CityObjectGroupModule.v2_0_0.getNamespaceURI(), enable);
        typeTree.setPathEnabled("ReliefFeature", ReliefModule.v2_0_0.getNamespaceURI(), enable);
        typeTree.setPathEnabled("Tunnel", TunnelModule.v2_0_0.getNamespaceURI(), enable);
        typeTree.setPathEnabled("SolitaryVegetationObject", VegetationModule.v2_0_0.getNamespaceURI(), enable);
        typeTree.setPathEnabled("PlantCover", VegetationModule.v2_0_0.getNamespaceURI(), enable);
        typeTree.repaint();
    }

    private void setEnabledFeatureFilter() {
        if (useFeatureFilter.isSelected()) {
            typeTree.expandRow(0);
        } else {
            typeTree.collapseRow(0);
            typeTree.setSelectionPath(null);
        }

        typeTree.setEnabled(useFeatureFilter.isSelected());
    }

    private void checkNonNegative(JFormattedTextField field) {
        if (field.getValue() == null || ((Number) field.getValue()).intValue() < 0)
            field.setValue(0);
    }

    private void saveFile() {
        JFileChooser chooser = new JFileChooser();

        FileNameExtensionFilter filter = new FileNameExtensionFilter("KML Files (*.kml, *.kmz)", "kml", "kmz");
        chooser.addChoosableFileFilter(filter);
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.setFileFilter(filter);

        if (browseText.getText().trim().isEmpty()) {
            chooser.setCurrentDirectory(config.getKmlExportConfig().getPath().isSetLastUsedMode() ?
                    new File(config.getKmlExportConfig().getPath().getLastUsedPath()) :
                    new File(config.getKmlExportConfig().getPath().getStandardPath()));
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

            exportString = config.getKmlExportConfig().isExportAsKmz() ?
                    exportString + ".kmz" :
                    exportString + ".kml";

            browseText.setText(exportString);
            config.getKmlExportConfig().getPath().setLastUsedPath(chooser.getCurrentDirectory().getAbsolutePath());
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        DatabaseConnectionStateEvent state = (DatabaseConnectionStateEvent) event;
        themeComboBox.removeAllItems();
        themeComboBox.addItem(KmlExportConfig.THEME_NONE);
        themeComboBox.setSelectedItem(KmlExportConfig.THEME_NONE);
        if (!state.isConnected())
            themeComboBox.setEnabled(false);
    }

    private class ThemeUpdater extends SwingWorker<Void, Void> {
        protected Void doInBackground() {
            Thread.currentThread().setName(this.getClass().getSimpleName());
            fetchThemesButton.setEnabled(false);

            try {
                String text = Language.I18N.getString("pref.kmlexport.connectDialog.line2");
                DatabaseConnection conn = config.getDatabaseConfig().getActiveConnection();
                Object[] args = new Object[]{conn.getDescription(), conn.toConnectString()};
                String formattedMsg = MessageFormat.format(text, args);
                String[] connectConfirm = {Language.I18N.getString("pref.kmlexport.connectDialog.line1"),
                        formattedMsg,
                        Language.I18N.getString("pref.kmlexport.connectDialog.line3")};

                if (!databaseController.isConnected() &&
                        JOptionPane.showConfirmDialog(getTopLevelAncestor(),
                                connectConfirm,
                                Language.I18N.getString("pref.kmlexport.connectDialog.title"),
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    databaseController.connect();
                }

                if (databaseController.isConnected()) {
                    themeComboBox.removeAllItems();
                    themeComboBox.addItem(KmlExportConfig.THEME_NONE);
                    themeComboBox.setSelectedItem(KmlExportConfig.THEME_NONE);

                    // checking workspace
                    Workspace workspace = config.getDatabaseConfig().getWorkspaces().getKmlExportWorkspace();
                    if (databaseController.getActiveDatabaseAdapter().hasVersioningSupport()
                            && !databaseController.getActiveDatabaseAdapter().getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName())) {
                        log.info("Switching to database workspace " + workspace + ".");
                        databaseController.getActiveDatabaseAdapter().getWorkspaceManager().checkWorkspace(workspace);
                    }

                    // fetching themes
                    for (String theme : databaseController.getActiveDatabaseAdapter().getUtil().getAppearanceThemeList(workspace)) {
                        if (theme != null) {
                            themeComboBox.addItem(theme);
                            if (theme.equals(config.getKmlExportConfig().getAppearanceTheme())) {
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

}
