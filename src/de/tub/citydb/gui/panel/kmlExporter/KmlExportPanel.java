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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.StringTokenizer;

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
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.filter.FilterMode;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.config.project.kmlExporter.DisplayLevel;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.SrsComboBoxManager;
import de.tub.citydb.gui.components.SrsComboBoxManager.SrsComboBox;
import de.tub.citydb.gui.components.StandardEditingPopupMenuDecorator;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.util.DBUtil;

@SuppressWarnings("serial")
public class KmlExportPanel extends JPanel implements PropertyChangeListener {
	
	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;
	protected static final int MAX_LABEL_WIDTH = 60;
	private static final int PREFERRED_WIDTH = 560;
	private static final int PREFERRED_HEIGHT = 780;

	private Config config;
	private final ImpExpGui topFrame;
	private boolean isConnected = false;

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

	private JButton exportButton = new JButton("");

	public KmlExportPanel(Config config, ImpExpGui topFrame) {
		this.config = config;
//		isConnected = config.getInternal().isConnected();
		this.topFrame = topFrame;
		initGui();
		addListeners();
		clearGui();
	}

	private void initGui() {

	    jPanelInput = Box.createVerticalBox();

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

		JPanel singleBuildingRadioPanel = new JPanel();
		singleBuildingRadioPanel.setLayout(new BorderLayout());
		singleBuildingRadioPanel.add(singleBuildingRadioButton, BorderLayout.WEST);

		Box singleBuildingPanel = Box.createHorizontalBox();
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 6, 0)));
		singleBuildingPanel.add(gmlIdLabel);
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		singleBuildingPanel.add(gmlIdText);
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));

		JPanel boundingBoxRadioPanel = new JPanel();
		boundingBoxRadioPanel.setLayout(new GridBagLayout());
		boundingBoxRadioPanel.add(boundingBoxRadioButton, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,BORDER_THICKNESS));
		boundingBoxRadioPanel.add(srsLabel, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
	    srsComboBox = SrsComboBoxManager.getInstance(config).getSrsComboBox(true);
	    boundingBoxRadioPanel.add(srsComboBox, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,BORDER_THICKNESS));

		JPanel boundingBoxPanel = new JPanel();
		boundingBoxPanel.setLayout(new GridBagLayout());

		bbXMinText.setPreferredSize(bbXMaxText.getPreferredSize());
		bbXMaxText.setPreferredSize(bbXMinText.getPreferredSize());
		bbYMinText.setPreferredSize(bbYMaxText.getPreferredSize());
		bbYMaxText.setPreferredSize(bbYMinText.getPreferredSize());
		boundingBoxPanel.add(bbXMinLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,2,BORDER_THICKNESS * 6,0,BORDER_THICKNESS));
		boundingBoxPanel.add(bbXMinText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,2,BORDER_THICKNESS,0,BORDER_THICKNESS));
		boundingBoxPanel.add(bbXMaxLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,2,BORDER_THICKNESS * 4 ,0,BORDER_THICKNESS));
		boundingBoxPanel.add(bbXMaxText, GuiUtil.setConstraints(3,0,1.0,0.0,GridBagConstraints.HORIZONTAL,2,BORDER_THICKNESS,0,BORDER_THICKNESS));
		boundingBoxPanel.add(bbYMinLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.NONE,2,BORDER_THICKNESS * 6,0,BORDER_THICKNESS));
		boundingBoxPanel.add(bbYMinText, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.HORIZONTAL,2,BORDER_THICKNESS,0,BORDER_THICKNESS));
		boundingBoxPanel.add(bbYMaxLabel, GuiUtil.setConstraints(2,1,0.0,0.0,GridBagConstraints.NONE,2,BORDER_THICKNESS * 4,0,BORDER_THICKNESS));
		boundingBoxPanel.add(bbYMaxText, GuiUtil.setConstraints(3,1,1.0,0.0,GridBagConstraints.HORIZONTAL,2,BORDER_THICKNESS,0,BORDER_THICKNESS));

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
		tilingPanel.add(automaticTilingRadioButton, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 6,BORDER_THICKNESS,0));
		tilingPanel.add(manualTilingRadioButton, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 6,BORDER_THICKNESS,0));
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

		
		Box mainLODPanel = Box.createHorizontalBox();
		mainLODPanel.add(Box.createRigidArea(new Dimension(10 * BORDER_THICKNESS, 0)));
		mainLODPanel.add(lodComboBox);
		mainLODPanel.add(Box.createRigidArea(new Dimension(10 * BORDER_THICKNESS, 0)));
		for (int index = 1; index < 5; index++) { // exclude LoD0 for the time being
			lodComboBox.insertItemAt("LoD" + index, index - 1);
		}
		lodComboBox.setSelectedIndex(1);
		lodComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)lodComboBox.getPreferredSize().getHeight()));
		lodComboBox.setMinimumSize(new Dimension((int)lodComboBox.getPreferredSize().getWidth(), (int)lodComboBox.getPreferredSize().getHeight()));
		
		Box exportFromLODContentPanel = Box.createVerticalBox();
		exportFromLODContentPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS
																	+ footprintCheckbox.getPreferredSize().height)));
		exportFromLODContentPanel.add(mainLODPanel);
		exportFromLODContentPanel.add(Box.createRigidArea(new Dimension(0, footprintCheckbox.getPreferredSize().height)));

		exportFromLODPanel = new JPanel();
		exportFromLODPanel.setLayout(new BorderLayout());
		exportFromLODPanel.setBorder(BorderFactory.createTitledBorder(""));
		exportFromLODPanel.add(exportFromLODContentPanel, BorderLayout.CENTER);

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
		themeComboBox.setMinimumSize(new Dimension(80, (int)themeComboBox.getPreferredSize().getHeight()));
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


		JPanel exportButtonPanel = new JPanel();
		exportButtonPanel.add(exportButton);
		
		jPanelInput.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));
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
		
		StandardEditingPopupMenuDecorator.decorate(browseText, workspaceText, timestampText, 
				gmlIdText, bbXMinText, bbXMaxText, bbYMinText, bbYMaxText, rowsText, columnsText,
				footprintVisibleFromText, extrudedVisibleFromText, geometryVisibleFromText, colladaVisibleFromText);		
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
/*
		int selectedIndex = lodComboBox.getSelectedIndex();
		if (!lodComboBox.getItemAt(lodComboBox.getItemCount() - 1).toString().endsWith("4")) {
			lodComboBox.removeItemAt(lodComboBox.getItemCount() - 1);
		}
		lodComboBox.insertItemAt(Internal.I18N.getString("kmlExport.label.highestLODAvailable"), lodComboBox.getItemCount());
		lodComboBox.setSelectedIndex(selectedIndex);
*/
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

		themeLabel.setText(Internal.I18N.getString("pref.kmlexport.label.theme"));
		fetchThemesButton.setText(Internal.I18N.getString("pref.kmlexport.label.fetchTheme"));

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
		
		int lod = kmlExporter.getLodToExportFrom() - 1; // exclude LoD0 for the time being
		lod = lod >= lodComboBox.getItemCount() ? lodComboBox.getItemCount() - 1: lod; 
		lodComboBox.setSelectedIndex(lod);

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

		themeComboBox.removeAllItems();
		themeComboBox.addItem(KmlExporter.THEME_NONE);
		themeComboBox.setSelectedItem(KmlExporter.THEME_NONE);
		isConnected = config.getInternal().isConnected();
		if (isConnected) {
			try {
				Workspace workspace = new Workspace();
				workspace.setName(workspaceText.getText().trim());
				workspace.setTimestamp(timestampText.getText().trim());
				for (String theme: DBUtil.getInstance(topFrame.getDBPool()).getAppearanceThemeList(workspace)) {
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
		
		kmlExporter.setLodToExportFrom(lodComboBox.getSelectedIndex() + 1); // exclude LoD0 for the time being

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

//		if (themeComboBox.getItemCount() > 0) {
			kmlExporter.setAppearanceTheme(themeComboBox.getSelectedItem().toString());
//		}

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
				// dialog preparation
				String text = Internal.I18N.getString("pref.kmlexport.connectDialog.line2");
				DBConnection conn = config.getProject().getDatabase().getActiveConnection();
				Object[] args = new Object[]{conn.getDescription(), conn.toConnectString()};
				String formattedMsg = MessageFormat.format(text, args);
				String[] connectConfirm = {Internal.I18N.getString("pref.kmlexport.connectDialog.line1"),
										   formattedMsg,
										   Internal.I18N.getString("pref.kmlexport.connectDialog.line3")};

				isConnected = config.getInternal().isConnected();
				if (!isConnected &&
					JOptionPane.showConfirmDialog(getTopLevelAncestor(),
												  connectConfirm,
												  Internal.I18N.getString("pref.kmlexport.connectDialog.title"),
												  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					topFrame.connectToDatabase();
				}

				if (isConnected) {
					themeComboBox.removeAllItems();
					themeComboBox.addItem(KmlExporter.THEME_NONE);
					themeComboBox.setSelectedItem(KmlExporter.THEME_NONE);
					try {
						Workspace workspace = new Workspace();
						workspace.setName(workspaceText.getText().trim());
						workspace.setTimestamp(timestampText.getText().trim());
						for (String theme: DBUtil.getInstance(topFrame.getDBPool()).getAppearanceThemeList(workspace)) {
							if (theme == null) continue; 
							themeComboBox.addItem(theme);
							if (theme.equals(config.getProject().getKmlExporter().getAppearanceTheme())) {
								themeComboBox.setSelectedItem(theme);
							}
						}
						themeComboBox.setEnabled(true);
					}
					catch (SQLException sqlEx) { }
				}
			}
		});

		config.getInternal().addPropertyChangeListener(this);
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

		setVisibilityEnabledValues();
		
	}

	private void setVisibilityEnabledValues() {

		extrudedCheckbox.setEnabled(DisplayLevel.isAchievableFromLoD(DisplayLevel.EXTRUDED, lodComboBox.getSelectedIndex() + 1));
		geometryCheckbox.setEnabled(DisplayLevel.isAchievableFromLoD(DisplayLevel.GEOMETRY, lodComboBox.getSelectedIndex() + 1));
		colladaCheckbox.setEnabled(DisplayLevel.isAchievableFromLoD(DisplayLevel.COLLADA, lodComboBox.getSelectedIndex() + 1));

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
		themeComboBox.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
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

		if (config.getProject().getKmlExporter().getPath().isSetLastUsedMode()) {
			fileChooser.setCurrentDirectory(new File(config.getProject().getKmlExporter().getPath().getLastUsedPath()));
		} else {
			fileChooser.setCurrentDirectory(new File(config.getProject().getKmlExporter().getPath().getStandardPath()));
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
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("database.isConnected")) {
			isConnected = (Boolean)evt.getNewValue();
			themeComboBox.removeAllItems();
			themeComboBox.addItem(KmlExporter.THEME_NONE);
			themeComboBox.setSelectedItem(KmlExporter.THEME_NONE);
			if (!isConnected) {
				themeComboBox.setEnabled(false);
			}
		}
	}

}
