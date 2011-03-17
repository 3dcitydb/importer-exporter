/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.gui.panel.kmlExporter;

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
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.filter.FilterMode;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.config.project.kmlExporter.DisplayLevel;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.gui.components.SrsComboBoxManager;
import de.tub.citydb.gui.components.SrsComboBoxManager.SrsComboBox;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class KmlExportPanel extends JPanel {
	
	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;
	protected static final int MAX_LABEL_WIDTH = 60;
	private static final int PREFERRED_WIDTH = 560;
	private static final int PREFERRED_HEIGHT = 780;

	private Config config;

	private Box jPanelInput;

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
	private JLabel srsLabel = new JLabel();
    private SrsComboBox srsComboBox;

	private JLabel bbXMinLabel = new JLabel();
	private JTextField bbXMinText = new JTextField("Xmin");
	private JLabel bbXMaxLabel = new JLabel();
	private JTextField bbXMaxText = new JTextField("Xmax");

	private JLabel bbYMinLabel = new JLabel();
	private JTextField bbYMinText = new JTextField("Ymin");
	private JLabel bbYMaxLabel = new JLabel();
	private JTextField bbYMaxText = new JTextField("Ymax");

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
	private JCheckBox whenDataMissingCheckBox = new JCheckBox();
    private JComboBox alternativeLodComboBox = new JComboBox();

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

	private JButton exportButton = new JButton("");

	public KmlExportPanel(Config config) {
		this.config = config;
		initGui();
		addListeners();
		clearGui();
	}

	private void initGui() {

	    jPanelInput = Box.createVerticalBox();

		Box filenameAndPathContentPanel = Box.createHorizontalBox();
		browseText.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		filenameAndPathContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		filenameAndPathContentPanel.add(browseText);
		filenameAndPathContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		filenameAndPathContentPanel.add(browseButton);
		filenameAndPathContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, BORDER_THICKNESS * 6)));

		browsePanel = new JPanel();
		browsePanel.setLayout(new BorderLayout());
//		browsePanel.setBorder(BorderFactory.createTitledBorder(""));
		browsePanel.add(filenameAndPathContentPanel, BorderLayout.CENTER);

		
		Box versioningContentPanel = Box.createHorizontalBox();
		versioningContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		versioningContentPanel.add(workspaceLabel);
		versioningContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		workspaceText.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		versioningContentPanel.add(workspaceText);
		versioningContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		versioningContentPanel.add(timestampLabel);
		versioningContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		timestampText.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		versioningContentPanel.add(timestampText);
		versioningContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, BORDER_THICKNESS * 6)));
		
		versioningPanel = new JPanel();
		versioningPanel.setLayout(new BorderLayout());
		versioningPanel.setBorder(BorderFactory.createTitledBorder(""));
		versioningPanel.add(versioningContentPanel, BorderLayout.CENTER);


		Box filterContentPanel = Box.createVerticalBox();
		filterButtonGroup.add(singleBuildingRadioButton);
		filterButtonGroup.add(boundingBoxRadioButton);
		boundingBoxRadioButton.setSelected(true);

		JPanel singleBuildingRadioPanel = new JPanel();
		singleBuildingRadioPanel.setLayout(new BorderLayout());
		singleBuildingRadioPanel.add(singleBuildingRadioButton, BorderLayout.WEST);

		Box singleBuildingPanel = Box.createHorizontalBox();
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 4, 0)));
		singleBuildingPanel.add(gmlIdLabel);
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		singleBuildingPanel.add(gmlIdText);
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));

		JPanel boundingBoxRadioPanel = new JPanel();
		boundingBoxRadioPanel.setLayout(new BorderLayout());
		boundingBoxRadioPanel.add(boundingBoxRadioButton, BorderLayout.WEST);
		
		Box srsPanel = Box.createHorizontalBox();
	    srsComboBox = SrsComboBoxManager.getInstance(config).getSrsComboBox(true);;
		srsComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		srsPanel.add(srsLabel);
		srsPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		srsPanel.add(srsComboBox);
		srsPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		
		boundingBoxRadioPanel.add(srsPanel, BorderLayout.EAST);

		Box boundingBoxPanel = Box.createHorizontalBox();
		Box minLabelPanel = Box.createVerticalBox();
		Box minTextPanel = Box.createVerticalBox();
		Box maxLabelPanel = Box.createVerticalBox();
		Box maxTextPanel = Box.createVerticalBox();

		bbXMinLabel.setMaximumSize(new Dimension(MAX_LABEL_WIDTH, MAX_TEXTFIELD_HEIGHT));
		minLabelPanel.add(bbXMinLabel);
		minLabelPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		bbYMinLabel.setMaximumSize(new Dimension(MAX_LABEL_WIDTH, MAX_TEXTFIELD_HEIGHT));
		minLabelPanel.add(bbYMinLabel);

		bbXMinText.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		minTextPanel.add(bbXMinText);
		minTextPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		bbYMinText.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		minTextPanel.add(bbYMinText);

		bbXMaxLabel.setMaximumSize(new Dimension(MAX_LABEL_WIDTH, MAX_TEXTFIELD_HEIGHT));
		maxLabelPanel.add(bbXMaxLabel);
		maxLabelPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		bbYMaxLabel.setMaximumSize(new Dimension(MAX_LABEL_WIDTH, MAX_TEXTFIELD_HEIGHT));
		maxLabelPanel.add(bbYMaxLabel);

		bbXMaxText.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		maxTextPanel.add(bbXMaxText);
		maxTextPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		bbYMaxText.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		maxTextPanel.add(bbYMaxText);
		
		boundingBoxPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 4, 0)));
		boundingBoxPanel.add(minLabelPanel);
		boundingBoxPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		boundingBoxPanel.add(minTextPanel);
		boundingBoxPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 4, 0)));
		boundingBoxPanel.add(maxLabelPanel);
		boundingBoxPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		boundingBoxPanel.add(maxTextPanel);
		boundingBoxPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));


		tilingButtonGroup.add(noTilingRadioButton);
		tilingButtonGroup.add(automaticTilingRadioButton);
		tilingButtonGroup.add(manualTilingRadioButton);
		automaticTilingRadioButton.setSelected(true);
		
		rowsText.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		columnsText.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		Box tilingContentPanel = Box.createHorizontalBox();
		tilingContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		tilingContentPanel.add(noTilingRadioButton);
		tilingContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 6, 0)));
		tilingContentPanel.add(automaticTilingRadioButton);
		tilingContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 6, 0)));
		tilingContentPanel.add(manualTilingRadioButton);
		tilingContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 6, 0)));
		tilingContentPanel.add(rowsLabel);
		tilingContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		tilingContentPanel.add(rowsText);
		tilingContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		tilingContentPanel.add(columnsLabel);
		tilingContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		tilingContentPanel.add(columnsText);
		tilingContentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, BORDER_THICKNESS * 6)));
		
		tilingPanel = new JPanel();
		tilingPanel.setLayout(new BorderLayout());
		tilingPanel.setBorder(BorderFactory.createTitledBorder(""));
		tilingPanel.add(tilingContentPanel, BorderLayout.CENTER);

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

		
		Box mainLODPanel = Box.createHorizontalBox();
		mainLODPanel.add(Box.createRigidArea(new Dimension(10 * BORDER_THICKNESS, 0)));
		mainLODPanel.add(lodComboBox);
		mainLODPanel.add(Box.createRigidArea(new Dimension(10 * BORDER_THICKNESS, 0)));
		lodComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		for (int index = 0; index < 5; index++) {
			lodComboBox.insertItemAt("LoD" + index, index);
		}
		lodComboBox.setSelectedIndex(2);

		alternativeLodComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		fillAlternativeLodComboBox();
		Box alternativeLODPanel = Box.createHorizontalBox();
//		alternativeLODPanel.add(whenDataMissingCheckBox);
		alternativeLODPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
//		alternativeLODPanel.add(alternativeLodComboBox);
		alternativeLODPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		
		Box exportFromLODContentPanel = Box.createVerticalBox();
		exportFromLODContentPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS
																	+ footprintCheckbox.getPreferredSize().height)));
		exportFromLODContentPanel.add(mainLODPanel);
		exportFromLODContentPanel.add(Box.createRigidArea(new Dimension(0, footprintCheckbox.getPreferredSize().height)));
		exportFromLODContentPanel.add(alternativeLODPanel);

		exportFromLODPanel = new JPanel();
		exportFromLODPanel.setLayout(new BorderLayout());
		exportFromLODPanel.setBorder(BorderFactory.createTitledBorder(""));
		exportFromLODPanel.add(exportFromLODContentPanel, BorderLayout.CENTER);


		JPanel displayAsFootprintPanel = new JPanel();
		displayAsFootprintPanel.setLayout(new BorderLayout());
		Box footprintVisibilityPanel = Box.createHorizontalBox();
		footprintVisibilityPanel.add(visibleFromFootprintLabel);
		footprintVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		footprintVisibleFromText.setMinimumSize(new Dimension(footprintVisibleFromText.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));
		footprintVisibleFromText.setMaximumSize(new Dimension(footprintVisibleFromText.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));
		footprintVisibilityPanel.add(footprintVisibleFromText);
		footprintVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		footprintVisibilityPanel.add(pixelsFootprintLabel);
		footprintVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		displayAsFootprintPanel.add(footprintCheckbox, BorderLayout.WEST);
		displayAsFootprintPanel.add(footprintVisibilityPanel, BorderLayout.EAST);

		JPanel displayAsExtrudedPanel = new JPanel();
		displayAsExtrudedPanel.setLayout(new BorderLayout());
		Box extrudedVisibilityPanel = Box.createHorizontalBox();
		extrudedVisibilityPanel.add(visibleFromExtrudedLabel);
		extrudedVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		extrudedVisibleFromText.setMinimumSize(new Dimension(extrudedVisibleFromText.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));
		extrudedVisibleFromText.setMaximumSize(new Dimension(extrudedVisibleFromText.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));
		extrudedVisibilityPanel.add(extrudedVisibleFromText);
		extrudedVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		extrudedVisibilityPanel.add(pixelsExtrudedLabel);
		extrudedVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		displayAsExtrudedPanel.add(extrudedCheckbox, BorderLayout.WEST);
		displayAsExtrudedPanel.add(extrudedVisibilityPanel, BorderLayout.EAST);

		JPanel displayAsGeometryPanel = new JPanel();
		displayAsGeometryPanel.setLayout(new BorderLayout());
		Box geometryVisibilityPanel = Box.createHorizontalBox();
		geometryVisibilityPanel.add(visibleFromGeometryLabel);
		geometryVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		geometryVisibleFromText.setMinimumSize(new Dimension(geometryVisibleFromText.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));
		geometryVisibleFromText.setMaximumSize(new Dimension(geometryVisibleFromText.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));
		geometryVisibilityPanel.add(geometryVisibleFromText);
		geometryVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		geometryVisibilityPanel.add(pixelsGeometryLabel);
		geometryVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		displayAsGeometryPanel.add(geometryCheckbox, BorderLayout.WEST);
		displayAsGeometryPanel.add(geometryVisibilityPanel, BorderLayout.EAST);

		JPanel displayAsColladaPanel = new JPanel();
		displayAsColladaPanel.setLayout(new BorderLayout());
		Box colladaVisibilityPanel = Box.createHorizontalBox();
		colladaVisibilityPanel.add(visibleFromColladaLabel);
		colladaVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		colladaVisibleFromText.setMinimumSize(new Dimension(colladaVisibleFromText.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));
		colladaVisibleFromText.setMaximumSize(new Dimension(colladaVisibleFromText.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));
		colladaVisibilityPanel.add(colladaVisibleFromText);
		colladaVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		colladaVisibilityPanel.add(pixelsColladaLabel);
		colladaVisibilityPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		displayAsColladaPanel.add(colladaCheckbox, BorderLayout.WEST);
		displayAsColladaPanel.add(colladaVisibilityPanel, BorderLayout.EAST);

		Box displayAsContentPanel = Box.createVerticalBox();
		displayAsContentPanel.add(displayAsFootprintPanel);
		displayAsContentPanel.add(displayAsExtrudedPanel);
		displayAsContentPanel.add(displayAsGeometryPanel);
		displayAsContentPanel.add(displayAsColladaPanel);
		displayAsContentPanel.add(Box.createRigidArea(new Dimension(0,BORDER_THICKNESS)));

		displayAsPanel = new JPanel();
		displayAsPanel.setLayout(new BorderLayout());
		displayAsPanel.setBorder(BorderFactory.createTitledBorder(""));
		displayAsPanel.add(displayAsContentPanel, BorderLayout.CENTER);

		JPanel exportAndDisplayPanel = new JPanel();
		exportAndDisplayPanel.setLayout(new GridBagLayout());
		exportAndDisplayPanel.add(exportFromLODPanel, GuiUtil.setConstraints(0,0,0.3,0,GridBagConstraints.BOTH,0,0,0,0));
		exportAndDisplayPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)), GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.NONE,0,0,0,0));
		exportAndDisplayPanel.add(displayAsPanel, GuiUtil.setConstraints(2,0,0.7,0,GridBagConstraints.BOTH,0,0,0,0));


		JPanel exportButtonPanel = new JPanel();
		exportButtonPanel.add(exportButton);
		
		jPanelInput.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS * 2)));
		jPanelInput.add(browsePanel);
		jPanelInput.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));
		jPanelInput.add(versioningPanel);
		jPanelInput.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));
		jPanelInput.add(filterPanel);
		jPanelInput.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));
		jPanelInput.add(exportAndDisplayPanel);
		jPanelInput.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));
		
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS));
		this.add(jPanelInput, BorderLayout.NORTH);
		this.add(exportButtonPanel, BorderLayout.SOUTH);
		
	}

	// localized Labels und Strings
	public void doTranslation() {

//		browsePanel.setBorder(BorderFactory.createTitledBorder("Output path and filename"));
		browseButton.setText(Internal.I18N.getString("common.button.browse"));

		versioningPanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("common.border.versioning")));
		workspaceLabel.setText(Internal.I18N.getString("common.label.workspace"));
		timestampLabel.setText(Internal.I18N.getString("common.label.timestamp"));

		filterPanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("kmlExport.label.exportContents")));
		singleBuildingRadioButton.setText(Internal.I18N.getString("kmlExport.label.singleBuilding"));
		boundingBoxRadioButton.setText(Internal.I18N.getString("filter.border.boundingBox"));

		srsLabel.setText(Internal.I18N.getString("common.label.boundingBox.crs"));
		srsComboBox.doTranslation();

		bbXMinLabel.setText(Internal.I18N.getString("filter.label.boundingBox.xMin"));
		bbXMaxLabel.setText(Internal.I18N.getString("filter.label.boundingBox.xMax"));
		bbYMinLabel.setText(Internal.I18N.getString("filter.label.boundingBox.yMin"));
		bbYMaxLabel.setText(Internal.I18N.getString("filter.label.boundingBox.yMax"));

		tilingPanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.export.boundingBox.border.tiling")));
		noTilingRadioButton.setText(Internal.I18N.getString("kmlExport.label.noTiling"));
		automaticTilingRadioButton.setText(Internal.I18N.getString("kmlExport.label.automatic"));
		manualTilingRadioButton.setText(Internal.I18N.getString("kmlExport.label.manual"));
		rowsLabel.setText(Internal.I18N.getString("pref.export.boundingBox.label.rows"));
		columnsLabel.setText(Internal.I18N.getString("pref.export.boundingBox.label.columns"));
		
		exportFromLODPanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("kmlExport.label.fromLOD")));
		whenDataMissingCheckBox.setText("when data missing use");
		
		displayAsPanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("kmlExport.label.displayAs")));
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
		
		exportButton.setText(Internal.I18N.getString("export.button.export"));
	}

	private void clearGui() {
		browseText.setText("");

		workspaceText.setText("LIVE");
		timestampText.setText("");

		gmlIdText.setText("");

		bbXMinText.setText("");
		bbXMaxText.setText("");
		bbYMinText.setText("");
		bbYMaxText.setText("");
		
		setFilterEnabledValues();

		rowsText.setText("");
		columnsText.setText("");
		
		whenDataMissingCheckBox.setSelected(false);

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
		
		srsComboBox.updateContent();
		srsComboBox.setSelectedIndex(0);
		
		workspaceText.setText(config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().getName());
		timestampText.setText(config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().getTimestamp());

		KmlExporter kmlExporter = config.getProject().getKmlExporter();
		if (kmlExporter == null) return;

		if (kmlExporter.getFilter().isSetSimpleFilter()) {
			singleBuildingRadioButton.setSelected(true);
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
		}
		else {
			boundingBoxRadioButton.setSelected(true);
			srsComboBox.setSelectedItem(kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getSRS());

			if (kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getX() != null)
				bbXMinText.setText(String.valueOf(kmlExporter.getFilter().getComplexFilter().
													getTiledBoundingBox().getLowerLeftCorner().getX()));

			if (kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getX() != null)
				bbXMaxText.setText(String.valueOf(kmlExporter.getFilter().getComplexFilter().
													getTiledBoundingBox().getUpperRightCorner().getX()));

			if (kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getY() != null)
				bbYMinText.setText(String.valueOf(kmlExporter.getFilter().getComplexFilter().
													getTiledBoundingBox().getLowerLeftCorner().getY()));

			if (kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getY() != null)
				bbYMaxText.setText(String.valueOf(kmlExporter.getFilter().getComplexFilter().
													getTiledBoundingBox().getUpperRightCorner().getY()));

			String tilingMode = kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().getMode().value();

			if (tilingMode.equals(TilingMode.NO_TILING.value())) {
				noTilingRadioButton.setSelected(true);
			}
			else if (tilingMode.equals(TilingMode.AUTOMATIC.value())) {
				automaticTilingRadioButton.setSelected(true);
			}
			else {
				manualTilingRadioButton.setSelected(true);
				rowsText.setText(String.valueOf(kmlExporter.getFilter().getComplexFilter().
						getTiledBoundingBox().getTiling().getRows()));
				columnsText.setText(String.valueOf(kmlExporter.getFilter().getComplexFilter().
						getTiledBoundingBox().getTiling().getColumns()));
			}
		}
		
		lodComboBox.setSelectedIndex(kmlExporter.getLodToExportFrom());

		for (DisplayLevel displayLevel : kmlExporter.getDisplayLevels()) {
			switch (displayLevel.getLevel()) {
			case DisplayLevel.FOOTPRINT:
				if (displayLevel.isActive()) {
					footprintCheckbox.setSelected(true);
					footprintVisibleFromText.setText(String.valueOf(displayLevel.getVisibleFrom()));
				}
				break;
			case DisplayLevel.EXTRUDED:
				if (displayLevel.isActive()) {
					extrudedCheckbox.setSelected(true);
					extrudedVisibleFromText.setText(String.valueOf(displayLevel.getVisibleFrom()));
				}
				break;
			case DisplayLevel.GEOMETRY:
				if (displayLevel.isActive()) {
					geometryCheckbox.setSelected(true);
					geometryVisibleFromText.setText(String.valueOf(displayLevel.getVisibleFrom()));
				}
				break;
			case DisplayLevel.COLLADA:
				if (displayLevel.isActive()) {
					colladaCheckbox.setSelected(true);
					colladaVisibleFromText.setText(String.valueOf(displayLevel.getVisibleFrom()));
				}
				break;
			}
		}

		setFilterEnabledValues();

	}

	public void setSettings() {

		config.getInternal().setExportFileName(browseText.getText().trim());
		config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().setName(workspaceText.getText().trim());
		config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace().setTimestamp(timestampText.getText().trim());
		
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		if (singleBuildingRadioButton.isSelected()) {
			kmlExporter.getFilter().setMode(FilterMode.SIMPLE);
			kmlExporter.getFilter().getSimpleFilter().getGmlIdFilter().getGmlIds().clear();
			StringTokenizer st = new StringTokenizer(gmlIdText.getText().trim(), ",");
			while (st.hasMoreTokens()) {
				kmlExporter.getFilter().getSimpleFilter().getGmlIdFilter().addGmlId(st.nextToken().trim());
			}
		}
		else {
			kmlExporter.getFilter().setMode(FilterMode.COMPLEX);
			kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().setActive(true);
			if (srsComboBox.getSelectedItem() != null) {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().setSRS(srsComboBox.getSelectedItem());
			}

			try {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
					getLowerLeftCorner().setX(Double.parseDouble(bbXMinText.getText().trim()));
			}
			catch (NumberFormatException nfe) {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
					getLowerLeftCorner().setX(0d);
			}
			try {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
					getUpperRightCorner().setX(Double.parseDouble(bbXMaxText.getText().trim()));
			}
			catch (NumberFormatException nfe) {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
					getUpperRightCorner().setX(0d);
			}
			try {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
					getLowerLeftCorner().setY(Double.parseDouble(bbYMinText.getText().trim()));
			}
			catch (NumberFormatException nfe) {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
					getLowerLeftCorner().setY(0d);
			}
			try {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
					getUpperRightCorner().setY(Double.parseDouble(bbYMaxText.getText().trim()));
			}
			catch (NumberFormatException nfe) {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
					getUpperRightCorner().setY(0d);
			}

			if (noTilingRadioButton.isSelected()) {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().setMode(TilingMode.NO_TILING);
			}
			else if (automaticTilingRadioButton.isSelected()) {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().setMode(TilingMode.AUTOMATIC);
			}
			else {
				kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().setMode(TilingMode.MANUAL);
				try {
					kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
						getTiling().setRows(Integer.parseInt(rowsText.getText().trim()));
				}
				catch (NumberFormatException nfe) {
					kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
						getTiling().setRows(1);
				}
				try {
					kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
						getTiling().setColumns(Integer.parseInt(columnsText.getText().trim()));
				}
				catch (NumberFormatException nfe) {
					kmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().
						getTiling().setColumns(1);
				}
			}
		}
		
		kmlExporter.setLodToExportFrom(lodComboBox.getSelectedIndex());

		int previousLevelVisibility = -1;

		DisplayLevel dl = new DisplayLevel(DisplayLevel.COLLADA, -1, -1);
		int indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
		}
		else {
			kmlExporter.getDisplayLevels().add(dl);
		}
		if (colladaCheckbox.isSelected() && kmlExporter.getLodToExportFrom()>1) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(colladaVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			dl.setActive(true);
			dl.setVisibleFrom(levelVisibility);
			dl.setVisibleUpTo(previousLevelVisibility);
			previousLevelVisibility = levelVisibility;
		}
		else {
			dl.setActive(false);
		}

		dl = new DisplayLevel(DisplayLevel.GEOMETRY, -1, -1);
		indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
		}
		else {
			kmlExporter.getDisplayLevels().add(dl);
		}
		if (geometryCheckbox.isSelected() && kmlExporter.getLodToExportFrom()>0) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(geometryVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			dl.setActive(true);
			dl.setVisibleFrom(levelVisibility);
			dl.setVisibleUpTo(previousLevelVisibility);
			previousLevelVisibility = levelVisibility;
		}
		else {
			dl.setActive(false);
		}

		dl = new DisplayLevel(DisplayLevel.EXTRUDED, -1, -1);
		indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
		}
		else {
			kmlExporter.getDisplayLevels().add(dl);
		}
		if (extrudedCheckbox.isSelected() && kmlExporter.getLodToExportFrom()>0) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(extrudedVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			dl.setActive(true);
			dl.setVisibleFrom(levelVisibility);
			dl.setVisibleUpTo(previousLevelVisibility);
			previousLevelVisibility = levelVisibility;
		}
		else {
			dl.setActive(false);
		}

		dl = new DisplayLevel(DisplayLevel.FOOTPRINT, -1, -1);
		indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
		}
		else {
			kmlExporter.getDisplayLevels().add(dl);
		}
		if (footprintCheckbox.isSelected()) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(footprintVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			dl.setActive(true);
			dl.setVisibleFrom(levelVisibility);
			dl.setVisibleUpTo(previousLevelVisibility);
		}
		else {
			dl.setActive(false);
		}

		config.getProject().setKmlExporter(kmlExporter);
	}

	public JButton getExportButton() {
		return exportButton;
	}

	private void addListeners() {
	    enableEvents(AWTEvent.WINDOW_EVENT_MASK);

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
		automaticTilingRadioButton.addActionListener(filterListener);
		manualTilingRadioButton.addActionListener(filterListener);

		lodComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int lod = lodComboBox.getSelectedIndex();
				extrudedCheckbox.setEnabled(lod>0);
				geometryCheckbox.setEnabled(lod>0);
				colladaCheckbox.setEnabled(lod>1);
				setVisibilityEnabledValues();
				fillAlternativeLodComboBox();
			}
		});

		whenDataMissingCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				alternativeLodComboBox.setEnabled(whenDataMissingCheckBox.isSelected());
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
		
	}

	private void setFilterEnabledValues() {
		gmlIdText.setEnabled(singleBuildingRadioButton.isSelected());

		srsLabel.setEnabled(boundingBoxRadioButton.isSelected());
		srsComboBox.setEnabled(boundingBoxRadioButton.isSelected());

		bbXMinLabel.setEnabled(boundingBoxRadioButton.isSelected());
		bbXMaxLabel.setEnabled(boundingBoxRadioButton.isSelected());
		bbYMinLabel.setEnabled(boundingBoxRadioButton.isSelected());
		bbYMaxLabel.setEnabled(boundingBoxRadioButton.isSelected());
		bbXMinText.setEnabled(boundingBoxRadioButton.isSelected());
		bbXMaxText.setEnabled(boundingBoxRadioButton.isSelected());
		bbYMinText.setEnabled(boundingBoxRadioButton.isSelected());
		bbYMaxText.setEnabled(boundingBoxRadioButton.isSelected());

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

		alternativeLodComboBox.setEnabled(whenDataMissingCheckBox.isSelected());

		extrudedCheckbox.setEnabled(lodComboBox.getSelectedIndex()>0);
		geometryCheckbox.setEnabled(lodComboBox.getSelectedIndex()>0);
		colladaCheckbox.setEnabled(lodComboBox.getSelectedIndex()>1);
/*
		lodComboBox.setEnabled(boundingBoxRadioButton.isSelected());
		whenDataMissingCheckBox.setEnabled(boundingBoxRadioButton.isSelected());
		((TitledBorder) exportFromLODPanel.getBorder()).setTitleColor(boundingBoxRadioButton.isSelected() ? 
				  											  		  UIManager.getColor("Label.foreground"):
				  											  		  UIManager.getColor("Label.disabledForeground"));
		exportFromLODPanel.repaint();

		footprintCheckbox.setEnabled(boundingBoxRadioButton.isSelected());
		extrudedCheckbox.setEnabled(boundingBoxRadioButton.isSelected() && lodComboBox.getSelectedIndex()>0);
		geometryCheckbox.setEnabled(boundingBoxRadioButton.isSelected() && lodComboBox.getSelectedIndex()>1);
		colladaCheckbox.setEnabled(boundingBoxRadioButton.isSelected() && lodComboBox.getSelectedIndex()>1);
		colladaHiResCheckbox.setEnabled(boundingBoxRadioButton.isSelected() && lodComboBox.getSelectedIndex()>2);
		((TitledBorder) displayAsPanel.getBorder()).setTitleColor(boundingBoxRadioButton.isSelected() ? 
															  	  UIManager.getColor("Label.foreground"):
															  	  UIManager.getColor("Label.disabledForeground"));
		displayAsPanel.repaint();
*/
		setVisibilityEnabledValues();
		
	}

	private void setVisibilityEnabledValues() {

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

	}
	
	private void fillAlternativeLodComboBox() {
		int selectedLodIndex = lodComboBox.getSelectedIndex();
		alternativeLodComboBox.removeAllItems();
		if (selectedLodIndex > 0) {
			alternativeLodComboBox.addItem(lodComboBox.getItemAt(selectedLodIndex - 1));
		}
		if (selectedLodIndex < (lodComboBox.getItemCount()-1)) {
			alternativeLodComboBox.addItem(lodComboBox.getItemAt(selectedLodIndex + 1));
		}
		alternativeLodComboBox.setSelectedIndex(0);
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

		if (config.getProject().getKmlExporter().getPath().isSetLastUsedMode()) {
			fileChooser.setCurrentDirectory(new File(config.getProject().getKmlExporter().getPath().getLastUsedPath()));
		} else {
			fileChooser.setCurrentDirectory(new File(config.getProject().getKmlExporter().getPath().getStandardPath()));
		}
		int result = fileChooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;
		try {
			String exportString = fileChooser.getSelectedFile().toString();
			if (exportString.lastIndexOf('.') != -1) {
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


}
