/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.kml.gui.view;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

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
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBContext;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.DatabaseConnectionStateEvent;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.config.project.filter.FilterMode;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.config.project.general.FeatureClassMode;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import de.tub.citydb.gui.components.ExportStatusDialog;
import de.tub.citydb.gui.components.bbox.BoundingBoxPanelImpl;
import de.tub.citydb.gui.components.checkboxtree.CheckboxTree;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.event.InterruptEnum;
import de.tub.citydb.modules.common.event.InterruptEvent;
import de.tub.citydb.util.Util;
import de.tub.citydb.util.database.DBUtil;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class KmlExportPanel extends JPanel implements EventHandler {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;
	protected static final int MAX_LABEL_WIDTH = 60;
	private static final int PREFERRED_WIDTH = 560;
	private static final int PREFERRED_HEIGHT = 780;

	private final ReentrantLock mainLock = new ReentrantLock();
	private final JAXBContext jaxbKmlContext, jaxbColladaContext;
	private final Config config;
	private final ImpExpGui mainView;
	private final DatabaseConnectionPool dbPool;

	private JPanel browsePanel;
	private JTextField browseText = new JTextField("");
	private JButton browseButton = new JButton("");

	private JPanel versioningPanel;
	private JLabel workspaceLabel = new JLabel();
	private JTextField workspaceText = new JTextField("LIVE");
	private JLabel timestampLabel = new JLabel();
	private JTextField timestampText = new JTextField("");

	private ButtonGroup filterButtonGroup = new ButtonGroup();

	private JPanel filterPanel;
	private JRadioButton singleBuildingRadioButton = new JRadioButton("");
	private JLabel gmlIdLabel = new JLabel("gml:id");
	private JTextField gmlIdText = new JTextField("");

	private JRadioButton boundingBoxRadioButton = new JRadioButton("");
	private BoundingBoxPanelImpl bboxComponent;
	
	private JPanel tilingPanel;
	private ButtonGroup tilingButtonGroup = new ButtonGroup();
	private JRadioButton noTilingRadioButton = new JRadioButton("");
	private JRadioButton automaticTilingRadioButton = new JRadioButton("");
	private JRadioButton manualTilingRadioButton = new JRadioButton("");

	private JLabel rowsLabel = new JLabel();
	private JTextField rowsText = new JTextField("");
	private JLabel columnsLabel = new JLabel();
	private JTextField columnsText = new JTextField("");

	private JPanel exportFromLODPanel;
	private JComboBox lodComboBox = new JComboBox();

	private JPanel displayAsPanel;
	private JCheckBox footprintCheckbox = new JCheckBox();
	private JCheckBox extrudedCheckbox = new JCheckBox();
	private JCheckBox geometryCheckbox = new JCheckBox();
	private JCheckBox colladaCheckbox = new JCheckBox();

	private JLabel visibleFromFootprintLabel = new JLabel();
	private JTextField footprintVisibleFromText = new JTextField("", 3);
	private JLabel pixelsFootprintLabel = new JLabel();
	private JLabel visibleFromExtrudedLabel = new JLabel();
	private JTextField extrudedVisibleFromText = new JTextField("", 3);
	private JLabel pixelsExtrudedLabel = new JLabel();
	private JLabel visibleFromGeometryLabel = new JLabel();
	private JTextField geometryVisibleFromText = new JTextField("", 3);
	private JLabel pixelsGeometryLabel = new JLabel();
	private JLabel visibleFromColladaLabel = new JLabel();
	private JTextField colladaVisibleFromText = new JTextField("", 3);
	private JLabel pixelsColladaLabel = new JLabel();

	private JLabel themeLabel = new JLabel();
	private JComboBox themeComboBox = new JComboBox();
	private JButton fetchThemesButton = new JButton(" ");

	private JLabel featureClassesLabel = new JLabel();
	private CheckboxTree fcTree;
	private DefaultMutableTreeNode cityObject;
	private DefaultMutableTreeNode building;
	private DefaultMutableTreeNode cityObjectGroup;
	private DefaultMutableTreeNode vegetation;

	private JButton exportButton = new JButton("");

	public KmlExportPanel(JAXBContext jaxbKmlContext, JAXBContext jaxbColladaContext, Config config, ImpExpGui mainView) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.mainView = mainView;
		this.config = config;
		dbPool = DatabaseConnectionPool.getInstance();
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.DATABASE_CONNECTION_STATE, this);

		initGui();
		addListeners();
		clearGui();
	}

	private void initGui() {

		browsePanel = new JPanel();
		browsePanel.setLayout(new GridBagLayout());
		browsePanel.add(browseText, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		browsePanel.add(browseButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));

		versioningPanel = new JPanel();
		versioningPanel.setLayout(new GridBagLayout());
		versioningPanel.setBorder(BorderFactory.createTitledBorder(""));

		versioningPanel.add(workspaceLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		versioningPanel.add(workspaceText, GuiUtil.setConstraints(1,0,0.5,0.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		versioningPanel.add(timestampLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS * 2,BORDER_THICKNESS,BORDER_THICKNESS));
		versioningPanel.add(timestampText, GuiUtil.setConstraints(3,0,0.5,0.0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));

		Box filterContentPanel = Box.createVerticalBox();
		filterButtonGroup.add(singleBuildingRadioButton);
		singleBuildingRadioButton.setIconTextGap(10);
		filterButtonGroup.add(boundingBoxRadioButton);
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
		bboxComponent = new BoundingBoxPanelImpl(config);

		boundingBoxPanel.add(bboxComponent, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,2,lmargin,0,BORDER_THICKNESS));

		tilingButtonGroup.add(noTilingRadioButton);
		noTilingRadioButton.setIconTextGap(10);
		tilingButtonGroup.add(automaticTilingRadioButton);
		automaticTilingRadioButton.setIconTextGap(10);
		tilingButtonGroup.add(manualTilingRadioButton);
		manualTilingRadioButton.setIconTextGap(10);
		automaticTilingRadioButton.setSelected(true);

		tilingPanel = new JPanel();
		tilingPanel.setLayout(new GridBagLayout());
		tilingPanel.setBorder(BorderFactory.createTitledBorder(""));

		tilingPanel.add(noTilingRadioButton, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 2,BORDER_THICKNESS,0));
		tilingPanel.add(automaticTilingRadioButton, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 2,BORDER_THICKNESS,0));
		tilingPanel.add(manualTilingRadioButton, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 2,BORDER_THICKNESS,0));
		tilingPanel.add(rowsLabel, GuiUtil.setConstraints(3,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 6,BORDER_THICKNESS,0));
		tilingPanel.add(rowsText, GuiUtil.setConstraints(4,0,0.5,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
		tilingPanel.add(columnsLabel, GuiUtil.setConstraints(5,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 3,BORDER_THICKNESS,0));
		tilingPanel.add(columnsText, GuiUtil.setConstraints(6,0,0.5,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS * 2));

		Box tilingParentPanel = Box.createHorizontalBox();
		tilingParentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		tilingParentPanel.add(tilingPanel);
		tilingParentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));

		filterContentPanel.add(singleBuildingRadioPanel);
		filterContentPanel.add(singleBuildingPanel);
		filterContentPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));
		filterContentPanel.add(boundingBoxRadioPanel);
		filterContentPanel.add(boundingBoxPanel);
		filterContentPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));
		filterContentPanel.add(tilingParentPanel);
		filterContentPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));

		filterPanel = new JPanel();
		filterPanel.setLayout(new BorderLayout());
		filterPanel.setBorder(BorderFactory.createTitledBorder(""));
		filterPanel.add(filterContentPanel, BorderLayout.CENTER);

		exportFromLODPanel = new JPanel();
		exportFromLODPanel.setLayout(new GridBagLayout());
		exportFromLODPanel.setBorder(BorderFactory.createTitledBorder(""));

		for (int index = 1; index < 5; index++) { // exclude LoD0 for the time being
			lodComboBox.insertItemAt("LoD" + index, index - 1);
		}
		lodComboBox.insertItemAt(Internal.I18N.getString("kmlExport.label.highestLODAvailable"), lodComboBox.getItemCount());
		lodComboBox.setSelectedIndex(1);
		GridBagConstraints lcb = GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS + footprintCheckbox.getPreferredSize().height,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS);
		lcb.anchor = GridBagConstraints.NORTH;
		exportFromLODPanel.add(lodComboBox, lcb);
		lodComboBox.setMinimumSize(lodComboBox.getPreferredSize());
		exportFromLODPanel.setMinimumSize(exportFromLODPanel.getPreferredSize());

		displayAsPanel = new JPanel();
		displayAsPanel.setLayout(new GridBagLayout());
		displayAsPanel.setBorder(BorderFactory.createTitledBorder(""));

		footprintCheckbox.setIconTextGap(10);
		displayAsPanel.add(footprintCheckbox, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		GridBagConstraints vffl = GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0);
		vffl.anchor = GridBagConstraints.EAST;
		displayAsPanel.add(visibleFromFootprintLabel, vffl);
		displayAsPanel.add(footprintVisibleFromText, GuiUtil.setConstraints(3,0,0.25,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsFootprintLabel, GuiUtil.setConstraints(4,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));

		extrudedCheckbox.setIconTextGap(10);
		displayAsPanel.add(extrudedCheckbox, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		GridBagConstraints vfel = GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0);
		vfel.anchor = GridBagConstraints.EAST;
		displayAsPanel.add(visibleFromExtrudedLabel, vfel);
		displayAsPanel.add(extrudedVisibleFromText, GuiUtil.setConstraints(3,1,0.25,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsExtrudedLabel, GuiUtil.setConstraints(4,1,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,BORDER_THICKNESS));

		geometryCheckbox.setIconTextGap(10);
		displayAsPanel.add(geometryCheckbox, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		GridBagConstraints vfgl = GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0);
		vfgl.anchor = GridBagConstraints.EAST;
		displayAsPanel.add(visibleFromGeometryLabel, vfgl);
		displayAsPanel.add(geometryVisibleFromText, GuiUtil.setConstraints(3,2,0.25,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsGeometryLabel, GuiUtil.setConstraints(4,2,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,BORDER_THICKNESS));

		colladaCheckbox.setIconTextGap(10);
		displayAsPanel.add(colladaCheckbox, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		GridBagConstraints vfcl = GuiUtil.setConstraints(2,3,0.0,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0);
		vfcl.anchor = GridBagConstraints.EAST;
		displayAsPanel.add(visibleFromColladaLabel, vfcl);
		displayAsPanel.add(colladaVisibleFromText, GuiUtil.setConstraints(3,3,0.25,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsColladaLabel, GuiUtil.setConstraints(4,3,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,BORDER_THICKNESS));

		displayAsPanel.add(themeLabel, GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,32,BORDER_THICKNESS,0));
//		themeComboBox.setMinimumSize(new Dimension(80, (int)themeComboBox.getPreferredSize().getHeight()));
//		themeComboBox.setPreferredSize(new Dimension(80, (int)fetchThemesButton.getPreferredSize().getHeight()));
		GridBagConstraints tcb = GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,0);
		tcb.gridwidth = 1;
		displayAsPanel.add(themeComboBox, tcb);
		GridBagConstraints fb = GuiUtil.setConstraints(2,4,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS);
		fb.gridwidth = 3;
		displayAsPanel.add(fetchThemesButton, fb);


		JPanel exportAndDisplayPanel = new JPanel();
		exportAndDisplayPanel.setLayout(new GridBagLayout());
		exportAndDisplayPanel.add(exportFromLODPanel, GuiUtil.setConstraints(0,0,0.3,0,GridBagConstraints.BOTH,0,0,0,0));
		exportAndDisplayPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)), GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.NONE,0,0,0,0));
		exportAndDisplayPanel.add(displayAsPanel, GuiUtil.setConstraints(2,0,0.7,0,GridBagConstraints.BOTH,0,0,0,0));

		cityObject = new DefaultMutableTreeNode(FeatureClassMode.CITYOBJECT);
		building = new DefaultMutableTreeNode(FeatureClassMode.BUILDING);
		cityObjectGroup = new DefaultMutableTreeNode(FeatureClassMode.CITYOBJECTGROUP);
		vegetation = new DefaultMutableTreeNode(FeatureClassMode.VEGETATION);

		cityObject.add(building);
		cityObject.add(vegetation);
		cityObject.add(cityObjectGroup);

		fcTree = new CheckboxTree(cityObject);
		fcTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 4);		
		fcTree.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), 
				BorderFactory.createEmptyBorder(0,0,BORDER_THICKNESS,0)));
		
		// get rid of standard icons
		DefaultCheckboxTreeCellRenderer renderer = (DefaultCheckboxTreeCellRenderer)fcTree.getCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);

		JPanel scrollView = new JPanel();
		scrollView.setLayout(new GridBagLayout());
		scrollView.add(versioningPanel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
		scrollView.add(filterPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
		scrollView.add(exportAndDisplayPanel, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
		scrollView.add(featureClassesLabel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,8,0,0));
		scrollView.add(fcTree, GuiUtil.setConstraints(0,4,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,5,7,0,7));
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
				fcTree);		
	}

	// localized Labels und Strings
	public void doTranslation() {

		//		browsePanel.setBorder(BorderFactory.createTitledBorder("Output path and filename"));
		browseButton.setText(Internal.I18N.getString("common.button.browse"));

		((TitledBorder)versioningPanel.getBorder()).setTitle(Internal.I18N.getString("common.border.versioning"));
		workspaceLabel.setText(Internal.I18N.getString("common.label.workspace"));
		timestampLabel.setText(Internal.I18N.getString("common.label.timestamp"));

		((TitledBorder)filterPanel.getBorder()).setTitle(Internal.I18N.getString("kmlExport.label.exportContents"));
		singleBuildingRadioButton.setText(Internal.I18N.getString("kmlExport.label.singleBuilding"));
		boundingBoxRadioButton.setText(Internal.I18N.getString("filter.border.boundingBox"));

		((TitledBorder) tilingPanel.getBorder()).setTitle(Internal.I18N.getString("pref.export.boundingBox.border.tiling"));
		noTilingRadioButton.setText(Internal.I18N.getString("kmlExport.label.noTiling"));
		manualTilingRadioButton.setText(Internal.I18N.getString("kmlExport.label.manual"));
		rowsLabel.setText(Internal.I18N.getString("pref.export.boundingBox.label.rows"));
		columnsLabel.setText(Internal.I18N.getString("pref.export.boundingBox.label.columns"));
		automaticTilingRadioButton.setText(Internal.I18N.getString("kmlExport.label.automatic"));
		
		((TitledBorder)exportFromLODPanel.getBorder()).setTitle(Internal.I18N.getString("kmlExport.label.fromLOD"));
/**/
		int selectedIndex = lodComboBox.getSelectedIndex();
		if (!lodComboBox.getItemAt(lodComboBox.getItemCount() - 1).toString().endsWith("4")) {
			lodComboBox.removeItemAt(lodComboBox.getItemCount() - 1);
		}
		lodComboBox.insertItemAt(Internal.I18N.getString("kmlExport.label.highestLODAvailable"), lodComboBox.getItemCount());
		lodComboBox.setSelectedIndex(selectedIndex);
		lodComboBox.setMinimumSize(lodComboBox.getPreferredSize());
		exportFromLODPanel.setMinimumSize(exportFromLODPanel.getPreferredSize());
/**/
		((TitledBorder)displayAsPanel.getBorder()).setTitle(Internal.I18N.getString("kmlExport.label.displayAs"));
		footprintCheckbox.setText(Internal.I18N.getString("kmlExport.label.footprint"));
		extrudedCheckbox.setText(Internal.I18N.getString("kmlExport.label.extruded"));
		geometryCheckbox.setText(Internal.I18N.getString("kmlExport.label.geometry"));
		colladaCheckbox.setText(Internal.I18N.getString("kmlExport.label.collada"));

		visibleFromFootprintLabel.setText(Internal.I18N.getString("kmlExport.label.visibleFrom"));
		pixelsFootprintLabel.setText(Internal.I18N.getString("kmlExport.label.pixels"));
		visibleFromExtrudedLabel.setText(Internal.I18N.getString("kmlExport.label.visibleFrom"));
		pixelsExtrudedLabel.setText(Internal.I18N.getString("kmlExport.label.pixels"));
		visibleFromGeometryLabel.setText(Internal.I18N.getString("kmlExport.label.visibleFrom"));
		pixelsGeometryLabel.setText(Internal.I18N.getString("kmlExport.label.pixels"));
		visibleFromColladaLabel.setText(Internal.I18N.getString("kmlExport.label.visibleFrom"));
		pixelsColladaLabel.setText(Internal.I18N.getString("kmlExport.label.pixels"));

		themeLabel.setText(Internal.I18N.getString("pref.kmlexport.label.theme"));
		fetchThemesButton.setText(Internal.I18N.getString("pref.kmlexport.label.fetchTheme"));

		featureClassesLabel.setText(Internal.I18N.getString("filter.border.featureClass"));
		
		exportButton.setText(Internal.I18N.getString("export.button.export"));
	}

	private void clearGui() {
		browseText.setText("");

		workspaceText.setText("LIVE");
		timestampText.setText("");

		gmlIdText.setText("");

		setFilterEnabledValues();

		rowsText.setText("");
		columnsText.setText("");

		footprintCheckbox.setSelected(false);
		extrudedCheckbox.setSelected(false);
		geometryCheckbox.setSelected(false);
		colladaCheckbox.setSelected(false);

		footprintVisibleFromText.setText("");
		extrudedVisibleFromText.setText("");
		geometryVisibleFromText.setText("");
		colladaVisibleFromText.setText("");

	}

	public void loadSettings() {
		clearGui();

		workspaceText.setText(config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().getName());
		timestampText.setText(config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().getTimestamp());

		KmlExporter kmlExporter = config.getProject().getKmlExporter();
		if (kmlExporter == null) return;

		if (kmlExporter.getFilter().isSetSimpleFilter()) {
			singleBuildingRadioButton.setSelected(true);
		}
		else {
			boundingBoxRadioButton.setSelected(true);
		}

		// this block should be under the former else block
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetBuilding()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(building.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(building.getPath()));
		}
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetVegetation()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(vegetation.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(vegetation.getPath()));
		}
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetCityObjectGroup()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(cityObjectGroup.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(cityObjectGroup.getPath()));
		}
		// end of block

		boolean isFirst = true;
		String gmlIds = "";
		for (String gmlId: kmlExporter.getFilter().getSimpleFilter().getGmlIdFilter().getGmlIds()) {
			if (!isFirst) {
				gmlIds = gmlIds + ", ";
			}
			else {
				isFirst = false;
			}
			gmlIds = gmlIds + gmlId;
		}
		gmlIdText.setText(gmlIds);

		bboxComponent.setBoundingBox(kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox());

		String tilingMode = kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().getMode().value();

		if (tilingMode.equals(TilingMode.NO_TILING.value())) {
			noTilingRadioButton.setSelected(true);
		}
		else if (tilingMode.equals(TilingMode.AUTOMATIC.value())) {
			automaticTilingRadioButton.setSelected(true);
		}
		else {
			manualTilingRadioButton.setSelected(true);
		}

		rowsText.setText(String.valueOf(kmlExporter.getFilter().getComplexFilter().
				getTiledBoundingBox().getTiling().getRows()));
		columnsText.setText(String.valueOf(kmlExporter.getFilter().getComplexFilter().
				getTiledBoundingBox().getTiling().getColumns()));
		
		int lod = kmlExporter.getLodToExportFrom() - 1; // exclude LoD0 for the time being
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
		if (dbPool.isConnected()) {
			try {
				Workspace workspace = new Workspace();
				workspace.setName(workspaceText.getText().trim());
				workspace.setTimestamp(timestampText.getText().trim());
				for (String theme: DBUtil.getAppearanceThemeList(workspace)) {
					if (theme == null) continue; 
					themeComboBox.addItem(theme);
					if (theme.equals(kmlExporter.getAppearanceTheme())) {
						themeComboBox.setSelectedItem(theme);
					}
				}
				themeComboBox.setEnabled(true);
			}
			catch (SQLException sqlEx) { }
		}
		else {
			themeComboBox.setEnabled(false);
		}

		setFilterEnabledValues();
	}

	public void setSettings() {
		String workspace = workspaceText.getText().trim();
		if (!workspace.equals(Internal.ORACLE_DEFAULT_WORKSPACE) && 
				(workspace.length() == 0 || workspace.toUpperCase().equals(Internal.ORACLE_DEFAULT_WORKSPACE)))
			workspaceText.setText(Internal.ORACLE_DEFAULT_WORKSPACE);

		config.getInternal().setExportFileName(browseText.getText().trim());
		config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().setName(workspaceText.getText().trim());
		config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().setTimestamp(timestampText.getText().trim());

		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().setActive(!singleBuildingRadioButton.isSelected());
		if (singleBuildingRadioButton.isSelected()) {
			kmlExporter.getFilter().setMode(FilterMode.SIMPLE);
		}
		else {
			kmlExporter.getFilter().setMode(FilterMode.COMPLEX);

			if (noTilingRadioButton.isSelected()) {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().setMode(TilingMode.NO_TILING);
			}
			else if (automaticTilingRadioButton.isSelected()) {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().setMode(TilingMode.AUTOMATIC);
			}
			else {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().setMode(TilingMode.MANUAL);
			}
		}

		kmlExporter.getFilter().getSimpleFilter().getGmlIdFilter().getGmlIds().clear();
		StringTokenizer st = new StringTokenizer(gmlIdText.getText().trim(), ",");
		while (st.hasMoreTokens()) {
			kmlExporter.getFilter().getSimpleFilter().getGmlIdFilter().addGmlId(st.nextToken().trim());
		}

		kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().copyFrom(bboxComponent.getBoundingBox());

		try {
			kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
			getTiling().setRows(Integer.parseInt(rowsText.getText().trim()));
		}
		catch (NumberFormatException nfe) {
			kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().setRows(1);
		}
		try {
			kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
			getTiling().setColumns(Integer.parseInt(columnsText.getText().trim()));
		}
		catch (NumberFormatException nfe) {
			kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().setColumns(1);
		}

		kmlExporter.setLodToExportFrom(lodComboBox.getSelectedIndex() + 1); // exclude LoD0 for the time being


		DisplayForm df = new DisplayForm(DisplayForm.COLLADA, -1, -1);
		int indexOfDf = kmlExporter.getBuildingDisplayForms().indexOf(df); 
		if (indexOfDf != -1) {
			df = kmlExporter.getBuildingDisplayForms().get(indexOfDf);
		}
		else { // should never happen
			kmlExporter.getBuildingDisplayForms().add(df);
		}
		if (colladaCheckbox.isSelected() && kmlExporter.getLodToExportFrom()>1) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(colladaVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		}
		else {
			df.setActive(false);
		}

		df = new DisplayForm(DisplayForm.GEOMETRY, -1, -1);
		indexOfDf = kmlExporter.getBuildingDisplayForms().indexOf(df); 
		if (indexOfDf != -1) {
			df = kmlExporter.getBuildingDisplayForms().get(indexOfDf);
		}
		else { // should never happen
			kmlExporter.getBuildingDisplayForms().add(df);
		}
		if (geometryCheckbox.isSelected() && kmlExporter.getLodToExportFrom()>0) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(geometryVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		}
		else {
			df.setActive(false);
		}

		df = new DisplayForm(DisplayForm.EXTRUDED, -1, -1);
		indexOfDf = kmlExporter.getBuildingDisplayForms().indexOf(df); 
		if (indexOfDf != -1) {
			df = kmlExporter.getBuildingDisplayForms().get(indexOfDf);
		}
		else { // should never happen
			kmlExporter.getBuildingDisplayForms().add(df);
		}
		if (extrudedCheckbox.isSelected() && kmlExporter.getLodToExportFrom()>0) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(extrudedVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		}
		else {
			df.setActive(false);
		}

		df = new DisplayForm(DisplayForm.FOOTPRINT, -1, -1);
		indexOfDf = kmlExporter.getBuildingDisplayForms().indexOf(df); 
		if (indexOfDf != -1) {
			df = kmlExporter.getBuildingDisplayForms().get(indexOfDf);
		}
		else { // should never happen
			kmlExporter.getBuildingDisplayForms().add(df);
		}
		if (footprintCheckbox.isSelected()) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(footprintVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		}
		else {
			df.setActive(false);
		}

		//		if (themeComboBox.getItemCount() > 0) {
		kmlExporter.setAppearanceTheme(themeComboBox.getSelectedItem().toString());
		//		}

		kmlExporter.getFilter().getComplexFilter().getFeatureClass().setBuilding(fcTree.getCheckingModel().isPathChecked(new TreePath(building.getPath()))); 
		kmlExporter.getFilter().getComplexFilter().getFeatureClass().setVegetation(fcTree.getCheckingModel().isPathChecked(new TreePath(vegetation.getPath())));
		kmlExporter.getFilter().getComplexFilter().getFeatureClass().setCityObjectGroup(fcTree.getCheckingModel().isPathChecked(new TreePath(cityObjectGroup.getPath())));

		config.getProject().setKmlExporter(kmlExporter);
	}

	private void addListeners() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						doExport();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});

		ActionListener filterListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFilterEnabledValues();
			}
		};

		singleBuildingRadioButton.addActionListener(filterListener);
		boundingBoxRadioButton.addActionListener(filterListener);

		noTilingRadioButton.addActionListener(filterListener);
		manualTilingRadioButton.addActionListener(filterListener);
		automaticTilingRadioButton.addActionListener(filterListener);

		lodComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisibilityEnabledValues();
			}
		});

		footprintCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisibilityEnabledValues();
			}
		});

		extrudedCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisibilityEnabledValues();
			}
		});

		geometryCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisibilityEnabledValues();
			}
		});

		colladaCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisibilityEnabledValues();
			}
		});

		fetchThemesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ThemeUpdater themeUpdater = new ThemeUpdater();
				themeUpdater.setDaemon(true);
				themeUpdater.start();
			}
		});

	}

	private void doExport() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			mainView.clearConsole();
			setSettings();

			ExportFilterConfig filter = config.getProject().getKmlExporter().getFilter();
			Database db = config.getProject().getDatabase();

			// check all input values...
			if (config.getInternal().getExportFileName().trim().equals("")) {
				mainView.errorMessage(Internal.I18N.getString("kmlExport.dialog.error.incompleteData"), 
						Internal.I18N.getString("kmlExport.dialog.error.incompleteData.dataset"));
				return;
			}

			// workspace timestamp
			if (!Util.checkWorkspaceTimestamp(db.getWorkspaces().getExportWorkspace())) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("export.dialog.error.incorrectData.date"));
				return;
			}

			// gmlId
			if (filter.isSetSimpleFilter() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// DisplayForms
			int activeDisplayFormsAmount =
				KmlExporter.getActiveDisplayFormsAmount(config.getProject().getKmlExporter().getBuildingDisplayForms()); 
			if (activeDisplayFormsAmount == 0) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("kmlExport.dialog.error.incorrectData.displayForms"));
				return;
			}

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			de.tub.citydb.modules.kml.controller.KmlExporter kmlExporter = new de.tub.citydb.modules.kml.controller.KmlExporter(jaxbKmlContext, jaxbColladaContext, dbPool, config, eventDispatcher);

			// BoundingBox check
			if (filter.isSetComplexFilter() &&
				filter.getComplexFilter().getTiledBoundingBox().isSet()) {
				Double xMin = filter.getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getX();
				Double yMin = filter.getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getY();
				Double xMax = filter.getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getX();
				Double yMax = filter.getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"),
							Internal.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}
			}

			if (!dbPool.isConnected()) {
				mainView.connectToDatabase();

				if (!dbPool.isConnected())
					return;
			}

			// tile amount calculation
			int tileAmount = 1;
			if (filter.isSetComplexFilter() &&
				filter.getComplexFilter().getTiledBoundingBox().isSet()) {
				try {
					tileAmount = kmlExporter.calculateRowsColumnsAndDelta();
				}
				catch (SQLException sqle) {
					String srsDescription = filter.getComplexFilter().getBoundingBox().getSrs().getDescription();
					Logger.getInstance().error(srsDescription + " " + sqle.getMessage());
					return;
				}
			}
			tileAmount = tileAmount * activeDisplayFormsAmount;

			mainView.setStatusText(Internal.I18N.getString("main.status.kmlExport.label"));
			Logger.getInstance().info("Initializing database export...");

			final ExportStatusDialog exportDialog = new ExportStatusDialog(mainView, 
					Internal.I18N.getString("kmlExport.dialog.window"),
					Internal.I18N.getString("export.dialog.msg"),
					tileAmount);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					exportDialog.setLocationRelativeTo(mainView);
					exportDialog.setVisible(true);
				}
			});

			exportDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of database export.", 
									LogLevel.INFO, 
									this));
						}
					});
				}
			});

			boolean success = kmlExporter.doProcess();

			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					exportDialog.dispose();
				}
			});
			
			// cleanup
			kmlExporter.cleanup();

			if (success) {
				Logger.getInstance().info("Database export successfully finished.");
			} else {
				Logger.getInstance().warn("Database export aborted.");
			}

			mainView.setStatusText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void setFilterEnabledValues() {
		gmlIdLabel.setEnabled(singleBuildingRadioButton.isSelected());
		gmlIdText.setEnabled(singleBuildingRadioButton.isSelected());

		bboxComponent.setEnabled(boundingBoxRadioButton.isSelected());

		noTilingRadioButton.setEnabled(boundingBoxRadioButton.isSelected());
		automaticTilingRadioButton.setEnabled(boundingBoxRadioButton.isSelected());
		manualTilingRadioButton.setEnabled(boundingBoxRadioButton.isSelected());
		((TitledBorder) tilingPanel.getBorder()).setTitleColor(boundingBoxRadioButton.isSelected() ? 
				UIManager.getColor("Label.foreground"):
					UIManager.getColor("Label.disabledForeground"));
		tilingPanel.repaint();
		
		rowsLabel.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());
		rowsText.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());
		columnsLabel.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());
		columnsText.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());

		setVisibilityEnabledValues();

	}

	private void setVisibilityEnabledValues() {

		extrudedCheckbox.setEnabled(DisplayForm.isAchievableFromLoD(DisplayForm.EXTRUDED, lodComboBox.getSelectedIndex() + 1));
		geometryCheckbox.setEnabled(DisplayForm.isAchievableFromLoD(DisplayForm.GEOMETRY, lodComboBox.getSelectedIndex() + 1));
		colladaCheckbox.setEnabled(DisplayForm.isAchievableFromLoD(DisplayForm.COLLADA, lodComboBox.getSelectedIndex() + 1));

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
		themeComboBox.setEnabled(dbPool.isConnected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
		fetchThemesButton.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());

	}

	public static void centerOnScreen(Component component) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width - component.getSize().width)/2;
		int y = (screen.height - component.getSize().height)/2;
		component.setBounds(x, y, component.getSize().width, component.getSize().height);
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

		if (config.getProject().getExporter().getPath().isSetLastUsedMode()) {
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
		boolean isConnected = ((DatabaseConnectionStateEvent)event).isConnected();

		themeComboBox.removeAllItems();
		themeComboBox.addItem(KmlExporter.THEME_NONE);
		themeComboBox.setSelectedItem(KmlExporter.THEME_NONE);
		if (!isConnected) {
			themeComboBox.setEnabled(false);
		}
	}

	private class ThemeUpdater extends Thread {
		public void run() {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			fetchThemesButton.setEnabled(false);
			try {
				String text = Internal.I18N.getString("pref.kmlexport.connectDialog.line2");
				DBConnection conn = config.getProject().getDatabase().getActiveConnection();
				Object[] args = new Object[]{conn.getDescription(), conn.toConnectString()};
				String formattedMsg = MessageFormat.format(text, args);
				String[] connectConfirm = {Internal.I18N.getString("pref.kmlexport.connectDialog.line1"),
						formattedMsg,
						Internal.I18N.getString("pref.kmlexport.connectDialog.line3")};
	
				if (!dbPool.isConnected() &&
						JOptionPane.showConfirmDialog(getTopLevelAncestor(),
								connectConfirm,
								Internal.I18N.getString("pref.kmlexport.connectDialog.title"),
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					mainView.connectToDatabase();
				}
	
				if (dbPool.isConnected()) {
					themeComboBox.removeAllItems();
					themeComboBox.addItem(KmlExporter.THEME_NONE);
					themeComboBox.setSelectedItem(KmlExporter.THEME_NONE);
					Workspace workspace = new Workspace();
					workspace.setName(workspaceText.getText().trim());
					workspace.setTimestamp(timestampText.getText().trim());
					for (String theme: DBUtil.getAppearanceThemeList(workspace)) {
						if (theme == null) continue; 
						themeComboBox.addItem(theme);
						if (theme.equals(config.getProject().getKmlExporter().getAppearanceTheme())) {
							themeComboBox.setSelectedItem(theme);
						}
					}
					themeComboBox.setEnabled(true);
				}
			}
			catch (Exception e) { }
			finally {
				fetchThemesButton.setEnabled(true);
			}
		}
	}
}
