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
package org.citydb.modules.kml.gui.view;

import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.kmlExporter.AltitudeOffsetMode;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.KmlExporter;
import org.citydb.config.project.kmlExporter.KmlTilingMode;
import org.citydb.config.project.kmlExporter.KmlTilingOptions;
import org.citydb.config.project.kmlExporter.SimpleKmlQuery;
import org.citydb.config.project.kmlExporter.SimpleKmlQueryMode;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.database.DatabaseController;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.version.DatabaseVersionException;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.EventType;
import org.citydb.event.global.InterruptEvent;
import org.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import org.citydb.gui.components.dialog.ExportStatusDialog;
import org.citydb.gui.components.feature.FeatureTypeTree;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.modules.kml.controller.KmlExportException;
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
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public class KmlExportPanel extends JPanel implements EventHandler {
	private final Logger log = Logger.getInstance();

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;
	private static final int PREFERRED_WIDTH = 560;
	private static final int PREFERRED_HEIGHT = 780;

	private final ReentrantLock mainLock = new ReentrantLock();
	private final JAXBContext jaxbKmlContext, jaxbColladaContext;
	private final ViewController viewController;
	private final DatabaseController databaseController;
	private final Config config;

	private JTextField browseText = new JTextField("");
	private JButton browseButton = new JButton("");

	private JPanel versioningPanel;
	private JLabel workspaceLabel = new JLabel();
	private JXTextField workspaceText = new JXTextField("");
	private JLabel timestampLabel = new JLabel();
	private JTextField timestampText = new JTextField("");

	private JPanel filterPanel;
	private JRadioButton singleBuildingRadioButton = new JRadioButton("");
	private JLabel gmlIdLabel = new JLabel("gml:id");
	private JTextField gmlIdText = new JTextField();

	private JRadioButton boundingBoxRadioButton = new JRadioButton("");
	private BoundingBoxPanel bboxComponent;

	private JLabel tilingLabel = new JLabel();
	private JRadioButton noTilingRadioButton = new JRadioButton("");
	private JRadioButton automaticTilingRadioButton = new JRadioButton("");
	private JRadioButton manualTilingRadioButton = new JRadioButton("");

	private JLabel rowsLabel = new JLabel();
	private JTextField rowsText = new JTextField("");
	private JLabel columnsLabel = new JLabel();
	private JTextField columnsText = new JTextField("");

	private JPanel exportFromLODPanel;
	private JComboBox<String> lodComboBox = new JComboBox<>();

	private JPanel displayAsPanel;
	private JCheckBox footprintCheckbox = new JCheckBox();
	private JCheckBox extrudedCheckbox = new JCheckBox();
	private JCheckBox geometryCheckbox = new JCheckBox();
	private JCheckBox colladaCheckbox = new JCheckBox();

	private JLabel visibleFromFootprintLabel = new JLabel();
	private JTextField footprintVisibleFromText = new JTextField("", 10);
	private JLabel pixelsFootprintLabel = new JLabel();
	private JLabel visibleFromExtrudedLabel = new JLabel();
	private JTextField extrudedVisibleFromText = new JTextField("", 10);
	private JLabel pixelsExtrudedLabel = new JLabel();
	private JLabel visibleFromGeometryLabel = new JLabel();
	private JTextField geometryVisibleFromText = new JTextField("", 10);
	private JLabel pixelsGeometryLabel = new JLabel();
	private JLabel visibleFromColladaLabel = new JLabel();
	private JTextField colladaVisibleFromText = new JTextField("", 10);
	private JLabel pixelsColladaLabel = new JLabel();

	private JLabel themeLabel = new JLabel();
	private JComboBox<String> themeComboBox = new JComboBox<>();
	private JButton fetchThemesButton = new JButton(" ");

	private JLabel featureClassesLabel = new JLabel();
	private FeatureTypeTree typeTree;
	private JButton exportButton = new JButton("");

	public KmlExportPanel(ViewController viewController, JAXBContext jaxbKmlContext, JAXBContext jaxbColladaContext, Config config) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.viewController = viewController;
		this.config = config;
		
		databaseController = ObjectRegistry.getInstance().getDatabaseController();
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);

		initGui();
		addListeners();
	}

	private void initGui() {
		JPanel browsePanel = new JPanel();
		browsePanel.setLayout(new GridBagLayout());
		browsePanel.add(browseText, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		browsePanel.add(browseButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));

		versioningPanel = new JPanel();
		versioningPanel.setLayout(new GridBagLayout());
		versioningPanel.setBorder(BorderFactory.createTitledBorder(""));
		
		workspaceText.setPromptForeground(Color.LIGHT_GRAY);
		workspaceText.setFocusBehavior(FocusBehavior.SHOW_PROMPT);
		versioningPanel.add(workspaceLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		versioningPanel.add(workspaceText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		versioningPanel.add(timestampLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS * 2,BORDER_THICKNESS,BORDER_THICKNESS));
		versioningPanel.add(timestampText, GuiUtil.setConstraints(3,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));

		ButtonGroup filterButtonGroup = new ButtonGroup();
		filterButtonGroup.add(singleBuildingRadioButton);
		filterButtonGroup.add(boundingBoxRadioButton);

		Box filterContentPanel = Box.createVerticalBox();
		singleBuildingRadioButton.setIconTextGap(10);
		boundingBoxRadioButton.setIconTextGap(10);
		boundingBoxRadioButton.setSelected(true);
		int lmargin = (int)(singleBuildingRadioButton.getPreferredSize().getWidth()) + 6;

		JPanel singleBuildingRadioPanel = new JPanel();
		singleBuildingRadioPanel.setLayout(new BorderLayout());
		singleBuildingRadioPanel.add(singleBuildingRadioButton, BorderLayout.WEST);

		Box singleBuildingPanel = Box.createHorizontalBox();
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(lmargin, 0)));
		singleBuildingPanel.add(gmlIdLabel);
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		singleBuildingPanel.add(gmlIdText);
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));

		JPanel boundingBoxRadioPanel = new JPanel();
		boundingBoxRadioPanel.setLayout(new GridBagLayout());
		boundingBoxRadioPanel.add(boundingBoxRadioButton, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,BORDER_THICKNESS));

		JPanel boundingBoxPanel = new JPanel();
		boundingBoxPanel.setLayout(new GridBagLayout());
		bboxComponent = viewController.getComponentFactory().createBoundingBoxPanel();

		boundingBoxPanel.add(bboxComponent, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,2,lmargin,0,BORDER_THICKNESS));

		ButtonGroup tilingButtonGroup = new ButtonGroup();
		tilingButtonGroup.add(noTilingRadioButton);
		tilingButtonGroup.add(automaticTilingRadioButton);
		tilingButtonGroup.add(manualTilingRadioButton);

		noTilingRadioButton.setIconTextGap(10);
		automaticTilingRadioButton.setIconTextGap(10);
		manualTilingRadioButton.setIconTextGap(10);
		automaticTilingRadioButton.setSelected(true);

		JPanel tilingPanel = new JPanel();
		tilingPanel.setLayout(new GridBagLayout());
		tilingPanel.add(tilingLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
		tilingPanel.add(noTilingRadioButton, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS * 4,0,0));
		tilingPanel.add(automaticTilingRadioButton, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS * 2,0,0));
		tilingPanel.add(manualTilingRadioButton, GuiUtil.setConstraints(3,0,0.0,1.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS * 2,0,0));
		tilingPanel.add(rowsLabel, GuiUtil.setConstraints(4,0,0.0,1.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS * 6,0,0));
		tilingPanel.add(rowsText, GuiUtil.setConstraints(5,0,0.5,1.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS * 2,0,0));
		tilingPanel.add(columnsLabel, GuiUtil.setConstraints(6,0,0.0,1.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS * 3,0,0));
		tilingPanel.add(columnsText, GuiUtil.setConstraints(7,0,0.5,1.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS * 2,0,0));

		// add tiling content to bbox panel
		bboxComponent.addComponent(tilingPanel);

		filterContentPanel.add(singleBuildingRadioPanel);
		filterContentPanel.add(singleBuildingPanel);
		filterContentPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));
		filterContentPanel.add(boundingBoxRadioPanel);
		filterContentPanel.add(boundingBoxPanel);
		filterContentPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));

		filterPanel = new JPanel();
		filterPanel.setLayout(new BorderLayout());
		filterPanel.setBorder(BorderFactory.createTitledBorder(""));
		filterPanel.add(filterContentPanel, BorderLayout.CENTER);

		exportFromLODPanel = new JPanel();
		exportFromLODPanel.setLayout(new GridBagLayout());
		exportFromLODPanel.setBorder(BorderFactory.createTitledBorder(""));

		for (int index = 0; index < 5; index++)
			lodComboBox.insertItemAt("LoD" + index, index);

		lodComboBox.insertItemAt(Language.I18N.getString("kmlExport.label.highestLODAvailable"), lodComboBox.getItemCount());
		lodComboBox.setSelectedIndex(2);
		exportFromLODPanel.add(lodComboBox, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS + footprintCheckbox.getPreferredSize().height,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		lodComboBox.setMinimumSize(lodComboBox.getPreferredSize());
		exportFromLODPanel.setMinimumSize(exportFromLODPanel.getPreferredSize());

		displayAsPanel = new JPanel();
		displayAsPanel.setLayout(new GridBagLayout());
		displayAsPanel.setBorder(BorderFactory.createTitledBorder(""));

		footprintCheckbox.setIconTextGap(10);
		displayAsPanel.add(footprintCheckbox, GuiUtil.setConstraints(0,0,1,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		displayAsPanel.add(visibleFromFootprintLabel, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0));
		displayAsPanel.add(footprintVisibleFromText, GuiUtil.setConstraints(2,0,0,1.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsFootprintLabel, GuiUtil.setConstraints(3,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));

		extrudedCheckbox.setIconTextGap(10);
		displayAsPanel.add(extrudedCheckbox, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(visibleFromExtrudedLabel, GuiUtil.setConstraints(1,1,0.0,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0));
		displayAsPanel.add(extrudedVisibleFromText, GuiUtil.setConstraints(2,1,0,1.0,GridBagConstraints.HORIZONTAL,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsExtrudedLabel, GuiUtil.setConstraints(3,1,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,BORDER_THICKNESS));

		geometryCheckbox.setIconTextGap(10);
		displayAsPanel.add(geometryCheckbox, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(visibleFromGeometryLabel, GuiUtil.setConstraints(1,2,0.0,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0));
		displayAsPanel.add(geometryVisibleFromText, GuiUtil.setConstraints(2,2,0,1.0,GridBagConstraints.HORIZONTAL,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsGeometryLabel, GuiUtil.setConstraints(3,2,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,BORDER_THICKNESS));

		colladaCheckbox.setIconTextGap(10);
		displayAsPanel.add(colladaCheckbox, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(visibleFromColladaLabel, GuiUtil.setConstraints(1,3,0.0,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0));
		displayAsPanel.add(colladaVisibleFromText, GuiUtil.setConstraints(2,3,0,1.0,GridBagConstraints.HORIZONTAL,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsColladaLabel, GuiUtil.setConstraints(3,3,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,BORDER_THICKNESS));

		JPanel appearancePanel = new JPanel();
		lmargin = colladaCheckbox.getPreferredSize().width + 11;
		appearancePanel.setLayout(new GridBagLayout());
		appearancePanel.add(themeLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		appearancePanel.add(themeComboBox, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,BORDER_THICKNESS * 2,BORDER_THICKNESS,0));
		appearancePanel.add(fetchThemesButton, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,BORDER_THICKNESS * 2,BORDER_THICKNESS,BORDER_THICKNESS));
		themeComboBox.setPreferredSize(new Dimension(50, themeComboBox.getPreferredSize().height));
		displayAsPanel.add(appearancePanel, GuiUtil.setConstraints(0,4,4,1,1.0,1.0,GridBagConstraints.HORIZONTAL,0,lmargin,0,0));

		JPanel exportAndDisplayPanel = new JPanel();
		exportAndDisplayPanel.setLayout(new GridBagLayout());
		exportAndDisplayPanel.add(exportFromLODPanel, GuiUtil.setConstraints(0,0,0.3,0,GridBagConstraints.BOTH,0,0,0,0));
		exportAndDisplayPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)), GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.NONE,0,0,0,0));
		exportAndDisplayPanel.add(displayAsPanel, GuiUtil.setConstraints(2,0,0.7,0,GridBagConstraints.BOTH,0,0,0,0));

		typeTree = new FeatureTypeTree(CityGMLVersion.v2_0_0, false);		
		typeTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 4);		
		typeTree.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), 
				BorderFactory.createEmptyBorder(0,0,BORDER_THICKNESS,0)));

		// get rid of standard icons
		DefaultCheckboxTreeCellRenderer renderer = (DefaultCheckboxTreeCellRenderer)typeTree.getCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);

		JPanel scrollView = new JPanel();
		scrollView.setLayout(new GridBagLayout());
		scrollView.add(versioningPanel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
		scrollView.add(filterPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
		scrollView.add(exportAndDisplayPanel, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
		scrollView.add(featureClassesLabel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,8,0,0));
		scrollView.add(typeTree, GuiUtil.setConstraints(0,4,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,5,7,0,7));
		JScrollPane scrollPane = new JScrollPane(scrollView);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

		JPanel exportButtonPanel = new JPanel();
		exportButtonPanel.add(exportButton);

		this.setLayout(new GridBagLayout());	
		this.add(browsePanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,10,5,5,5));
		this.add(scrollPane, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));
		this.add(exportButtonPanel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));

		PopupMenuDecorator.getInstance().decorate(browseText, workspaceText, timestampText,
				gmlIdText, rowsText, columnsText,
				footprintVisibleFromText, extrudedVisibleFromText, geometryVisibleFromText, colladaVisibleFromText,
				typeTree);		
	}

	public void setEnabledWorkspace(boolean enable) {
		((TitledBorder)versioningPanel.getBorder()).setTitleColor(enable ?
				UIManager.getColor("TitledBorder.titleColor"):
				UIManager.getColor("Label.disabledForeground"));
		versioningPanel.repaint();

		workspaceLabel.setEnabled(enable);
		workspaceText.setEnabled(enable);
		timestampLabel.setEnabled(enable);
		timestampText.setEnabled(enable);
	}

	// localized Labels und Strings
	public void doTranslation() {
		browseButton.setText(Language.I18N.getString("common.button.browse"));

		((TitledBorder)versioningPanel.getBorder()).setTitle(Language.I18N.getString("common.border.versioning"));
		workspaceLabel.setText(Language.I18N.getString("common.label.workspace"));
		workspaceText.setPrompt(Language.I18N.getString("common.label.workspace.prompt"));
		timestampLabel.setText(Language.I18N.getString("common.label.timestamp"));

		((TitledBorder)filterPanel.getBorder()).setTitle(Language.I18N.getString("kmlExport.label.exportContents"));
		singleBuildingRadioButton.setText(Language.I18N.getString("kmlExport.label.singleBuilding"));
		boundingBoxRadioButton.setText(Language.I18N.getString("filter.border.boundingBox"));

		tilingLabel.setText(Language.I18N.getString("pref.export.boundingBox.border.tiling"));
		noTilingRadioButton.setText(Language.I18N.getString("kmlExport.label.noTiling"));
		manualTilingRadioButton.setText(Language.I18N.getString("kmlExport.label.manual"));
		rowsLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.rows"));
		columnsLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.columns"));
		automaticTilingRadioButton.setText(Language.I18N.getString("kmlExport.label.automatic"));

		((TitledBorder)exportFromLODPanel.getBorder()).setTitle(Language.I18N.getString("kmlExport.label.fromLOD"));
		int selectedIndex = lodComboBox.getSelectedIndex();
		if (!lodComboBox.getItemAt(lodComboBox.getItemCount() - 1).endsWith("4"))
			lodComboBox.removeItemAt(lodComboBox.getItemCount() - 1);

		lodComboBox.insertItemAt(Language.I18N.getString("kmlExport.label.highestLODAvailable"), lodComboBox.getItemCount());
		lodComboBox.setSelectedIndex(selectedIndex);
		lodComboBox.setMinimumSize(lodComboBox.getPreferredSize());
		exportFromLODPanel.setMinimumSize(exportFromLODPanel.getPreferredSize());

		((TitledBorder)displayAsPanel.getBorder()).setTitle(Language.I18N.getString("kmlExport.label.displayAs"));
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

		featureClassesLabel.setText(Language.I18N.getString("filter.border.featureClass"));
		exportButton.setText(Language.I18N.getString("export.button.export"));
	}

	public void loadSettings() {
		// database-specific content
		workspaceText.setText(config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().getName());
		timestampText.setText(config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().getTimestamp());

		// filter
		SimpleKmlQuery query = config.getProject().getKmlExporter().getQuery();
		if (query.getMode() == SimpleKmlQueryMode.SINGLE)
			singleBuildingRadioButton.setSelected(true);
		else
			boundingBoxRadioButton.setSelected(true);

		// feature type filter
		FeatureTypeFilter featureTypeFilter = query.getFeatureTypeFilter();
		typeTree.getCheckingModel().clearChecking();
		typeTree.setSelected(featureTypeFilter.getTypeNames());

		// gml:id filter
		ResourceIdOperator gmlIdFilter = query.getGmlIdFilter();
		gmlIdText.setText(String.join(",", gmlIdFilter.getResourceIds()));

		// bbox filter
		BBOXOperator bboxFilter = query.getBboxFilter();
		BoundingBox bbox = bboxFilter.getEnvelope();
		if (bbox != null)
			bboxComponent.setBoundingBox(bboxFilter.getEnvelope());

		// tiling
		KmlTilingOptions tilingOptions = query.getTilingOptions();
		switch (tilingOptions.getMode()) {
		case AUTOMATIC:
			automaticTilingRadioButton.setSelected(true);
			break;
		case MANUAL:
			manualTilingRadioButton.setSelected(true);
			break;
		default:
			noTilingRadioButton.setSelected(true);
		}

		rowsText.setText(String.valueOf(tilingOptions.getRows()));
		columnsText.setText(String.valueOf(tilingOptions.getColumns()));

		// display options
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		int lod = kmlExporter.getLodToExportFrom();
		lod = lod >= lodComboBox.getItemCount() ? lodComboBox.getItemCount() - 1: lod; 
		lodComboBox.setSelectedIndex(lod);

		for (DisplayForm displayForm : kmlExporter.getBuildingDisplayForms()) {
			switch (displayForm.getForm()) {
			case DisplayForm.FOOTPRINT:
				if (displayForm.isActive()) {
					footprintCheckbox.setSelected(true);
					footprintVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
				}
				break;
			case DisplayForm.EXTRUDED:
				if (displayForm.isActive()) {
					extrudedCheckbox.setSelected(true);
					extrudedVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
				}
				break;
			case DisplayForm.GEOMETRY:
				if (displayForm.isActive()) {
					geometryCheckbox.setSelected(true);
					geometryVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
				}
				break;
			case DisplayForm.COLLADA:
				if (displayForm.isActive()) {
					colladaCheckbox.setSelected(true);
					colladaVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
				}
				break;
			}
		}

		themeComboBox.removeAllItems();
		themeComboBox.addItem(KmlExporter.THEME_NONE);
		themeComboBox.setSelectedItem(KmlExporter.THEME_NONE);
		if (databaseController.isConnected()) {
			try {
				Workspace workspace = new Workspace();
				workspace.setName(workspaceText.getText().trim());
				workspace.setTimestamp(timestampText.getText().trim());
				for (String theme : databaseController.getActiveDatabaseAdapter().getUtil().getAppearanceThemeList(workspace)) {
					if (theme == null) continue; 
					themeComboBox.addItem(theme);
					if (theme.equals(kmlExporter.getAppearanceTheme())) {
						themeComboBox.setSelectedItem(theme);
					}
				}
				themeComboBox.setEnabled(true);
			}
			catch (SQLException ignored) { }
		}
		else {
			themeComboBox.setEnabled(false);
		}

		setFilterEnabledValues();
	}

	public void setSettings() {
		config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().setName(workspaceText.getText().trim());
		config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().setTimestamp(timestampText.getText().trim());

		try {
			config.getInternal().setExportFile(new File(browseText.getText().trim()).toPath());
		} catch (Throwable e) {
			log.error("'" + browseText.getText().trim() + "' is not a valid file.");
			browseText.setText("");
		}

		// filter
		SimpleKmlQuery query = config.getProject().getKmlExporter().getQuery();
		query.setMode(singleBuildingRadioButton.isSelected() ? SimpleKmlQueryMode.SINGLE : SimpleKmlQueryMode.BBOX);

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
		query.getBboxFilter().setEnvelope(bboxComponent.getBoundingBox());

		// tiling
		KmlTilingOptions tilingOptions = query.getTilingOptions();
		if (automaticTilingRadioButton.isSelected())
			tilingOptions.setMode(KmlTilingMode.AUTOMATIC);
		else if (manualTilingRadioButton.isSelected())
			tilingOptions.setMode(KmlTilingMode.MANUAL);
		else
			tilingOptions.setMode(KmlTilingMode.NO_TILING);

		try {
			tilingOptions.setRows(Integer.parseInt(rowsText.getText().trim()));
		} catch (NumberFormatException e) {
			tilingOptions.setRows(1);
		}

		try {
			tilingOptions.setColumns(Integer.parseInt(columnsText.getText().trim()));
		} catch (NumberFormatException e) {
			tilingOptions.setColumns(1);
		}

		// display options
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		kmlExporter.setLodToExportFrom(lodComboBox.getSelectedIndex());

		setDisplayFormSettings(kmlExporter.getBuildingDisplayForms());
		setDisplayFormSettings(kmlExporter.getWaterBodyDisplayForms());
		setDisplayFormSettings(kmlExporter.getLandUseDisplayForms());
		setDisplayFormSettings(kmlExporter.getVegetationDisplayForms());
		setDisplayFormSettings(kmlExporter.getTransportationDisplayForms());
		setDisplayFormSettings(kmlExporter.getReliefDisplayForms());
		setDisplayFormSettings(kmlExporter.getCityFurnitureDisplayForms());
		setDisplayFormSettings(kmlExporter.getGenericCityObjectDisplayForms());
		setDisplayFormSettings(kmlExporter.getCityObjectGroupDisplayForms());
		setDisplayFormSettings(kmlExporter.getBridgeDisplayForms());
		setDisplayFormSettings(kmlExporter.getTunnelDisplayForms());

		kmlExporter.setAppearanceTheme((String) themeComboBox.getSelectedItem());
	}

	private void setDisplayFormSettings(List<DisplayForm> displayForms) {
		DisplayForm df = new DisplayForm(DisplayForm.COLLADA, -1, -1);
		int indexOfDf = displayForms.indexOf(df);
		if (indexOfDf != -1) {
			df = displayForms.get(indexOfDf);
		} else { // should never happen
			displayForms.add(df);
		}
		if (colladaCheckbox.isSelected() && config.getProject().getKmlExporter().getLodToExportFrom()>0) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(colladaVisibleFromText.getText().trim());
			} catch (NumberFormatException ignored) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		} else {
			df.setActive(false);
		}

		df = new DisplayForm(DisplayForm.GEOMETRY, -1, -1);
		indexOfDf = displayForms.indexOf(df); 
		if (indexOfDf != -1) {
			df = displayForms.get(indexOfDf);
		} else { // should never happen
			displayForms.add(df);
		}
		if (geometryCheckbox.isSelected() && config.getProject().getKmlExporter().getLodToExportFrom()>0) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(geometryVisibleFromText.getText().trim());
			} catch (NumberFormatException ignored) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		} else {
			df.setActive(false);
		}

		df = new DisplayForm(DisplayForm.EXTRUDED, -1, -1);
		indexOfDf = displayForms.indexOf(df); 
		if (indexOfDf != -1) {
			df = displayForms.get(indexOfDf);
		} else { // should never happen
			displayForms.add(df);
		}
		if (extrudedCheckbox.isSelected() && config.getProject().getKmlExporter().getLodToExportFrom()>0) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(extrudedVisibleFromText.getText().trim());
			} catch (NumberFormatException ignored) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		} else {
			df.setActive(false);
		}

		df = new DisplayForm(DisplayForm.FOOTPRINT, -1, -1);
		indexOfDf = displayForms.indexOf(df); 
		if (indexOfDf != -1) {
			df = displayForms.get(indexOfDf);
		} else { // should never happen
			displayForms.add(df);
		}
		if (footprintCheckbox.isSelected()) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(footprintVisibleFromText.getText().trim());
			} catch (NumberFormatException ignored) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		} else {
			df.setActive(false);
		}

		int upperLevelVisibility = -1; 
		for (int i = DisplayForm.COLLADA; i >= DisplayForm.FOOTPRINT; i--) {
			df = new DisplayForm(i, -1, -1);
			indexOfDf = displayForms.indexOf(df); 
			df = displayForms.get(indexOfDf);

			if (df.isActive()) {
				df.setVisibleUpTo(upperLevelVisibility);
				upperLevelVisibility = df.getVisibleFrom();
			}
		}
	}

	private void addListeners() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		exportButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				doExport();
				return null;
			}
		}.execute());

		browseButton.addActionListener(e -> saveFile());
		ActionListener filterListener = e -> setFilterEnabledValues();

		singleBuildingRadioButton.addActionListener(filterListener);
		boundingBoxRadioButton.addActionListener(filterListener);

		noTilingRadioButton.addActionListener(filterListener);
		manualTilingRadioButton.addActionListener(filterListener);
		automaticTilingRadioButton.addActionListener(filterListener);

		lodComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
				setVisibilityEnabledValues();
		});
		
		footprintCheckbox.addActionListener(e -> setVisibilityEnabledValues());
		extrudedCheckbox.addActionListener(e -> setVisibilityEnabledValues());
		geometryCheckbox.addActionListener(e -> setVisibilityEnabledValues());
		colladaCheckbox.addActionListener(e -> setVisibilityEnabledValues());
		fetchThemesButton.addActionListener(e -> new ThemeUpdater().execute());
	}

	private void doExport() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewController.clearConsole();
			setSettings();

			SimpleKmlQuery query = config.getProject().getKmlExporter().getQuery();
			Database db = config.getProject().getDatabase();

			// check all input values...
			if (browseText.getText().trim().isEmpty()) {
				viewController.errorMessage(Language.I18N.getString("kmlExport.dialog.error.incompleteData"), 
						Language.I18N.getString("kmlExport.dialog.error.incompleteData.dataset"));
				return;
			}

			// workspace timestamp
			if (!Util.checkWorkspaceTimestamp(db.getWorkspaces().getExportWorkspace())) {
				viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"), 
						Language.I18N.getString("export.dialog.error.incorrectData.date"));
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
			int activeDisplayFormsAmount = config.getProject().getKmlExporter().getActiveDisplayFormsAmount(config.getProject().getKmlExporter().getBuildingDisplayForms()); 
			if (activeDisplayFormsAmount == 0) {
				viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"), 
						Language.I18N.getString("kmlExport.dialog.error.incorrectData.displayForms"));
				return;
			}

			// check API key when using the elevation API
			if (config.getProject().getKmlExporter().getAltitudeOffsetMode() == AltitudeOffsetMode.GENERIC_ATTRIBUTE
					&& config.getProject().getKmlExporter().isCallGElevationService()
					&& !config.getProject().getGlobal().getApiKeys().isSetGoogleElevation()) {
				log.error("The Google Elevation API cannot be used due to a missing API key.");
				log.error("Please enter an API key or change the export preferences.");
				viewController.errorMessage(Language.I18N.getString("kmlExport.dialog.error.elevation"),
						Language.I18N.getString("kmlExport.dialog.error.elevation.apiKey"));
				return;
			}

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();

			// BoundingBox check
			if (query.getMode() == SimpleKmlQueryMode.BBOX
					&& query.isSetBboxFilter()) {
				BoundingBox bbox = query.getBboxFilter().getEnvelope();
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
			if (query.getMode() == SimpleKmlQueryMode.BBOX
					&& (!query.isSetFeatureTypeFilter() || query.getFeatureTypeFilter().getTypeNames().isEmpty())) {
				viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
						Language.I18N.getString("common.dialog.error.incorrectData.featureClass"));
				return;
			}

			// check collada2gltf tool
			if (config.getProject().getKmlExporter().isCreateGltfModel()) {
				Path collada2gltf = Paths.get(config.getProject().getKmlExporter().getPathOfGltfConverter());
				if (!collada2gltf.isAbsolute())
					collada2gltf = ClientConstants.IMPEXP_HOME.resolve(collada2gltf);

				if (!Files.exists(collada2gltf)) {
					String text = Language.I18N.getString("kmlExport.dialog.error.collada2gltf.notExists");
					Object[] args = new Object[]{ collada2gltf.toString() };
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
						Files.setPosixFilePermissions(Paths.get(config.getProject().getKmlExporter().getPathOfGltfConverter()), permissions);
					} catch (IOException e) {
						String text = Language.I18N.getString("kmlExport.dialog.error.collada2gltf.notExecutable");
						Object[] args = new Object[] { collada2gltf.toString() };
						String result = MessageFormat.format(text, args);
						viewController.errorMessage(Language.I18N.getString("kmlExport.dialog.error.collada2gltf"), result);
						return;
					}
				}
			}

			if (!databaseController.isConnected()) {
				try {
					databaseController.connect(true);
				} catch (DatabaseConfigurationException | DatabaseVersionException | SQLException e) {
					return;
				}				
			}

			viewController.setStatusText(Language.I18N.getString("main.status.kmlExport.label"));
			log.info("Initializing database export...");

			final ExportStatusDialog exportDialog = new ExportStatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("kmlExport.dialog.window"),
					Language.I18N.getString("export.dialog.msg"),
					true);

			SwingUtilities.invokeLater(() -> {
				exportDialog.setLocationRelativeTo(viewController.getTopFrame());
				exportDialog.setVisible(true);
			});
			
			// get schema mapping
			final SchemaMapping schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();

			org.citydb.modules.kml.controller.KmlExporter kmlExporter = new org.citydb.modules.kml.controller.KmlExporter(jaxbKmlContext, jaxbColladaContext, schemaMapping, config, eventDispatcher);

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
				success = kmlExporter.doProcess();
			} catch (KmlExportException e) {
				log.error(e.getMessage());

				Throwable cause = e.getCause();
				while (cause != null) {
					log.error(cause.getClass().getTypeName() + ": " + cause.getMessage());
					cause = cause.getCause();
				}
			}

			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(exportDialog::dispose);

			// cleanup
			kmlExporter.cleanup();

			if (success) {
				log.info("Database export successfully finished.");
			} else {
				log.warn("Database export aborted.");
			}

			viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void setFilterEnabledValues() {
		gmlIdLabel.setEnabled(singleBuildingRadioButton.isSelected());
		gmlIdText.setEnabled(singleBuildingRadioButton.isSelected());

		bboxComponent.setEnabled(boundingBoxRadioButton.isSelected());

		typeTree.setPathsEnabled(boundingBoxRadioButton.isSelected());
		typeTree.repaint();

		tilingLabel.setEnabled(boundingBoxRadioButton.isSelected());
		noTilingRadioButton.setEnabled(boundingBoxRadioButton.isSelected());
		automaticTilingRadioButton.setEnabled(boundingBoxRadioButton.isSelected());
		manualTilingRadioButton.setEnabled(boundingBoxRadioButton.isSelected());

		rowsLabel.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());
		rowsText.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());
		columnsLabel.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());
		columnsText.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());

		setVisibilityEnabledValues();
	}

	private void setVisibilityEnabledValues() {
		extrudedCheckbox.setEnabled(DisplayForm.isAchievableFromLoD(DisplayForm.EXTRUDED, lodComboBox.getSelectedIndex()));
		geometryCheckbox.setEnabled(DisplayForm.isAchievableFromLoD(DisplayForm.GEOMETRY, lodComboBox.getSelectedIndex()));
		colladaCheckbox.setEnabled(DisplayForm.isAchievableFromLoD(DisplayForm.COLLADA, lodComboBox.getSelectedIndex()));

		visibleFromFootprintLabel.setEnabled(boundingBoxRadioButton.isSelected() && footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());
		footprintVisibleFromText.setEnabled(boundingBoxRadioButton.isSelected() && footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());
		pixelsFootprintLabel.setEnabled(boundingBoxRadioButton.isSelected() && footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());

		visibleFromExtrudedLabel.setEnabled(boundingBoxRadioButton.isSelected() && extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());
		extrudedVisibleFromText.setEnabled(boundingBoxRadioButton.isSelected() && extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());
		pixelsExtrudedLabel.setEnabled(boundingBoxRadioButton.isSelected() && extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());

		visibleFromGeometryLabel.setEnabled(boundingBoxRadioButton.isSelected() && geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());
		geometryVisibleFromText.setEnabled(boundingBoxRadioButton.isSelected() && geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());
		pixelsGeometryLabel.setEnabled(boundingBoxRadioButton.isSelected() && geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());

		visibleFromColladaLabel.setEnabled(boundingBoxRadioButton.isSelected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
		colladaVisibleFromText.setEnabled(boundingBoxRadioButton.isSelected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
		pixelsColladaLabel.setEnabled(boundingBoxRadioButton.isSelected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());

		themeLabel.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
		themeComboBox.setEnabled(databaseController.isConnected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
		fetchThemesButton.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());

		if (boundingBoxRadioButton.isSelected()) {
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
	}

	public Dimension getPreferredSize() {
		return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
	}

	private void saveFile() {
		JFileChooser fileChooser = new JFileChooser();

		FileNameExtensionFilter filter = new FileNameExtensionFilter("KML Files (*.kml, *.kmz)", "kml", "kmz");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
		fileChooser.setFileFilter(filter);

		if (config.getProject().getKmlExporter().getPath().isSetLastUsedMode()) {
			fileChooser.setCurrentDirectory(new File(config.getProject().getKmlExporter().getPath().getLastUsedPath()));
		} else {
			fileChooser.setCurrentDirectory(new File(config.getProject().getExporter().getPath().getStandardPath()));
		}
		int result = fileChooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;
		try {
			String exportString = fileChooser.getSelectedFile().toString();
			if (exportString.lastIndexOf('.') != -1	&&
					exportString.lastIndexOf('.') > exportString.lastIndexOf(File.separator)) {
				exportString = exportString.substring(0, exportString.lastIndexOf('.'));
			}
			exportString = config.getProject().getKmlExporter().isExportAsKmz() ?
					exportString + ".kmz":
						exportString + ".kml";

			browseText.setText(exportString);
			config.getProject().getKmlExporter().getPath().setLastUsedPath(fileChooser.getCurrentDirectory().getAbsolutePath());
		}
		catch (Exception e) {
			//
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		DatabaseConnectionStateEvent state = (DatabaseConnectionStateEvent)event;

		themeComboBox.removeAllItems();
		themeComboBox.addItem(KmlExporter.THEME_NONE);
		themeComboBox.setSelectedItem(KmlExporter.THEME_NONE);
		if (!state.isConnected())
			themeComboBox.setEnabled(false);

		setEnabledWorkspace(!state.isConnected() || databaseController.getActiveDatabaseAdapter().hasVersioningSupport());
	}

	private class ThemeUpdater extends SwingWorker<Void, Void> {
		protected Void doInBackground() {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			fetchThemesButton.setEnabled(false);

			try {
				String text = Language.I18N.getString("pref.kmlexport.connectDialog.line2");
				DBConnection conn = config.getProject().getDatabase().getActiveConnection();
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
					try {
						databaseController.connect(true);
					} catch (DatabaseConfigurationException | DatabaseVersionException e) {
						//
					}
				}

				if (databaseController.isConnected()) {
					themeComboBox.removeAllItems();
					themeComboBox.addItem(KmlExporter.THEME_NONE);
					themeComboBox.setSelectedItem(KmlExporter.THEME_NONE);

					// checking workspace
					Workspace workspace = new Workspace(workspaceText.getText().trim(), timestampText.getText().trim());
					if (databaseController.getActiveDatabaseAdapter().hasVersioningSupport() && 
							!databaseController.getActiveDatabaseAdapter().getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName()) &&
							!databaseController.getActiveDatabaseAdapter().getWorkspaceManager().existsWorkspace(workspace, true)) {
						themeComboBox.setEnabled(false);
						return null;
					}

					// fetching themes
					for (String theme : databaseController.getActiveDatabaseAdapter().getUtil().getAppearanceThemeList(workspace)) {
						if (theme == null) continue; 
						themeComboBox.addItem(theme);
						if (theme.equals(config.getProject().getKmlExporter().getAppearanceTheme())) {
							themeComboBox.setSelectedItem(theme);
						}
					}

					themeComboBox.setEnabled(true);
				}
			} catch (SQLException e) {
				log.error("Failed to query appearance themes from database: " + e.getMessage());
			} finally {
				fetchThemesButton.setEnabled(true);
			}

			return null;
		}
	}

}
