/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.modules.kml.gui.preferences;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import org.citygml.textureAtlasAPI.TextureAtlasGenerator;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.kmlExporter.ColladaOptions;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class WaterBodyRenderingPanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;

	private ArrayList<DisplayForm> internalDfs = new ArrayList<DisplayForm>();

	private JPanel footprintPanel;
	private JCheckBox footprintHighlightingCheckbox = new JCheckBox();
	private JLabel footprintFillColorLabel = new JLabel();
	private JButton footprintFillColorButton = new JButton(" ");
	private JLabel footprintLineColorLabel = new JLabel();
	private JButton footprintLineColorButton = new JButton(" ");
	private JLabel footprintHLFillColorLabel = new JLabel();
	private JButton footprintHLFillColorButton = new JButton(" ");
	private JLabel footprintHLLineColorLabel = new JLabel();
	private JButton footprintHLLineColorButton = new JButton(" ");
	private JLabel footprintAlphaLabel = new JLabel();
	private JSpinner footprintAlphaSpinner;

	private JPanel geometryPanel;
	private JLabel geometryAlphaLabel = new JLabel();
	private JSpinner geometryAlphaSpinner;
	private JLabel geometryWallFillColorLabel = new JLabel();
	private JButton geometryWallFillColorButton = new JButton(" ");
/*
	private JLabel geometryRoofFillColorLabel = new JLabel();
	private JButton geometryRoofFillColorButton = new JButton(" ");
*/
	private JLabel geometryWallLineColorLabel = new JLabel();
	private JButton geometryWallLineColorButton = new JButton(" ");
/*
	private JLabel geometryRoofLineColorLabel = new JLabel();
	private JButton geometryRoofLineColorButton = new JButton(" ");
*/
	private JCheckBox geometryHighlightingCheckbox = new JCheckBox();
	private JLabel geometryHLSurfaceDistanceLabel = new JLabel();
	private JTextField geometryHLSurfaceDistanceText = new JTextField("", 3);
	private JLabel geometryHLFillColorLabel = new JLabel();
	private JButton geometryHLFillColorButton = new JButton(" ");
	private JLabel geometryHLLineColorLabel = new JLabel();
	private JButton geometryHLLineColorButton = new JButton(" ");

	private JPanel colladaPanel;
	private JCheckBox ignoreSurfaceOrientationCheckbox = new JCheckBox();
	private JCheckBox textureAtlasCheckbox = new JCheckBox();
	private JCheckBox textureAtlasPotsCheckbox = new JCheckBox();
	private JCheckBox scaleTexImagesCheckbox = new JCheckBox();
	private JTextField scaleFactorText = new JTextField("", 3);
	private JRadioButton groupObjectsRButton = new JRadioButton();
	private JTextField groupSizeText = new JTextField("", 3);
	private JRadioButton colladaHighlightingRButton = new JRadioButton();
	private JLabel colladaHLSurfaceDistanceLabel = new JLabel();
	private JTextField colladaHLSurfaceDistanceText = new JTextField("", 3);
	private JLabel colladaHLFillColorLabel = new JLabel();
	private JButton colladaHLFillColorButton = new JButton(" ");
	private JLabel colladaHLLineColorLabel = new JLabel();
	private JButton colladaHLLineColorButton = new JButton(" ");

	private HashMap<String, Integer> packingAlgorithms = new HashMap<String, Integer>();  
	private JComboBox packingAlgorithmsComboBox = new JComboBox();

	public WaterBodyRenderingPanel(Config config) {
		super(config);
		initGui();
	}

	private ColladaOptions getConfigColladaOptions() {
		return config.getProject().getKmlExporter().getWaterBodyColladaOptions();
	}

	private List<DisplayForm> getConfigDisplayForms() {
		return config.getProject().getKmlExporter().getWaterBodyDisplayForms();
	}

	private void setConfigDisplayForms(List<DisplayForm> displayForms) {
		config.getProject().getKmlExporter().setWaterBodyDisplayForms(displayForms);
	}

	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.kmlExport.waterBodyRendering");
	}

	@Override
	public boolean isModified() {
		setInternalDisplayFormValues();
		ColladaOptions colladaOptions = getConfigColladaOptions();
		List<DisplayForm> configDfs = getConfigDisplayForms();

		for (int form = DisplayForm.FOOTPRINT; form <= DisplayForm.COLLADA; form++) {
			DisplayForm configDf = new DisplayForm(form, -1, -1);
			int indexOfConfigDf = configDfs.indexOf(configDf); 
			if (indexOfConfigDf != -1) {
				configDf = configDfs.get(indexOfConfigDf);
			}
			DisplayForm internalDf = new DisplayForm(form, -1, -1);
			int indexOfInternalDf = internalDfs.indexOf(internalDf); 
			if (indexOfInternalDf != -1) {
				internalDf = internalDfs.get(indexOfInternalDf);
			}

			if (areDisplayFormsContentsDifferent(internalDf, configDf)) return true;
		}

		if (ignoreSurfaceOrientationCheckbox.isSelected() != colladaOptions.isIgnoreSurfaceOrientation()) return true;
		if (textureAtlasCheckbox.isSelected() != colladaOptions.isGenerateTextureAtlases()) return true;
		if (textureAtlasPotsCheckbox.isSelected() != colladaOptions.isTextureAtlasPots()) return true;
		if (packingAlgorithms.get(packingAlgorithmsComboBox.getSelectedItem()).intValue() != colladaOptions.getPackingAlgorithm()) return true;

		int groupSize = 1;
		try {
			groupSize = Integer.parseInt(groupSizeText.getText().trim());
			if (groupSize < 2) {
				groupSize = 1;
			}
		}
		catch (NumberFormatException nfe) {return true;}
		//		groupSizeText.setText(String.valueOf(groupSize));
		if (groupObjectsRButton.isSelected() != colladaOptions.isGroupObjects() ||
				groupSize != colladaOptions.getGroupSize()) return true;

		double imageScaleFactor = 1;
		try {
			imageScaleFactor = Double.parseDouble(scaleFactorText.getText().trim());
			if (imageScaleFactor <= 0 || imageScaleFactor > 1) {
				imageScaleFactor = 1;
			}
		}
		catch (NumberFormatException nfe) {return true;}
		//		scaleFactorText.setText(String.valueOf(imageScaleFactor));
		if (scaleTexImagesCheckbox.isSelected() != colladaOptions.isScaleImages() ||
				imageScaleFactor != colladaOptions.getImageScaleFactor()) return true;

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		footprintPanel = new JPanel();
		add(footprintPanel, GuiUtil.setConstraints(0,1,2,2,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		footprintPanel.setLayout(new GridBagLayout());
		footprintPanel.setBorder(BorderFactory.createTitledBorder(""));

		SpinnerModel falphaValueModel = new SpinnerNumberModel(200, 0, 255, 1);
		footprintAlphaSpinner = new JSpinner(falphaValueModel);
//		footprintAlphaSpinner.setMinimumSize(new Dimension(footprintAlphaSpinner.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));
//		footprintAlphaSpinner.setMaximumSize(new Dimension(footprintAlphaSpinner.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));

        GridBagConstraints fal = GuiUtil.setConstraints(0,0,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS);
		fal.anchor = GridBagConstraints.EAST;
		footprintPanel.add(footprintAlphaLabel, fal);
		footprintPanel.add(footprintAlphaSpinner, GuiUtil.setConstraints(1,0,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,BORDER_THICKNESS,0));

		GridBagConstraints ffcl = GuiUtil.setConstraints(0,1,0.25,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		ffcl.anchor = GridBagConstraints.EAST;
		footprintPanel.add(footprintFillColorLabel, ffcl);

		footprintFillColorButton.setPreferredSize(footprintAlphaSpinner.getPreferredSize());
		footprintFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_FILL_COLOR, true));
		footprintFillColorButton.setContentAreaFilled(false);
		footprintFillColorButton.setOpaque(true);
		footprintPanel.add(footprintFillColorButton, GuiUtil.setConstraints(1,1,0.25,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,0,2*BORDER_THICKNESS,0));
		
		GridBagConstraints flcl = GuiUtil.setConstraints(2,1,0.25,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		flcl.anchor = GridBagConstraints.EAST;
		footprintPanel.add(footprintLineColorLabel, flcl);

		footprintLineColorButton.setPreferredSize(footprintAlphaSpinner.getPreferredSize());
		footprintLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_COLOR, true));
		footprintLineColorButton.setContentAreaFilled(false);
		footprintLineColorButton.setOpaque(true);
		footprintPanel.add(footprintLineColorButton, GuiUtil.setConstraints(3,1,0.25,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,0,2*BORDER_THICKNESS,BORDER_THICKNESS));

		GridBagConstraints fhlcb = GuiUtil.setConstraints(0,2,0.5,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,0);
		fhlcb.anchor = GridBagConstraints.WEST;
		fhlcb.gridwidth = 2;
		footprintHighlightingCheckbox.setIconTextGap(10);
		footprintPanel.add(footprintHighlightingCheckbox, fhlcb);

		GridBagConstraints fhlfcl = GuiUtil.setConstraints(0,3,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		fhlfcl.anchor = GridBagConstraints.EAST;
		footprintPanel.add(footprintHLFillColorLabel, fhlfcl);

		footprintHLFillColorButton.setPreferredSize(footprintAlphaSpinner.getPreferredSize());
		footprintHLFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR, true));
		footprintHLFillColorButton.setContentAreaFilled(false);
		footprintHLFillColorButton.setOpaque(true);
		footprintPanel.add(footprintHLFillColorButton, GuiUtil.setConstraints(1,3,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0));

		GridBagConstraints fhllcl = GuiUtil.setConstraints(2,3,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		fhllcl.anchor = GridBagConstraints.EAST;
		footprintPanel.add(footprintHLLineColorLabel, fhllcl);

		footprintHLLineColorButton.setPreferredSize(footprintAlphaSpinner.getPreferredSize());
		footprintHLLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
		footprintHLLineColorButton.setContentAreaFilled(false);
		footprintHLLineColorButton.setOpaque(true);
		footprintPanel.add(footprintHLLineColorButton, GuiUtil.setConstraints(3,3,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,BORDER_THICKNESS));

		geometryPanel = new JPanel();
		geometryPanel.setLayout(new GridBagLayout());
		geometryPanel.setBorder(BorderFactory.createTitledBorder(""));

		SpinnerModel galphaValueModel = new SpinnerNumberModel(200, 0, 255, 1);
		geometryAlphaSpinner = new JSpinner(galphaValueModel);
		geometryAlphaSpinner.setMinimumSize(new Dimension(geometryAlphaSpinner.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));
		geometryAlphaSpinner.setMaximumSize(new Dimension(geometryAlphaSpinner.getPreferredSize().width, MAX_TEXTFIELD_HEIGHT));

        GridBagConstraints gal = GuiUtil.setConstraints(0,0,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS);
        gal.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryAlphaLabel, gal);
		geometryPanel.add(geometryAlphaSpinner, GuiUtil.setConstraints(1,0,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,BORDER_THICKNESS,0));

		GridBagConstraints gwcl = GuiUtil.setConstraints(0,1,0.25,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		gwcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryWallFillColorLabel, gwcl);

		geometryWallFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryWallFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_WALL_FILL_COLOR, true));
		geometryWallFillColorButton.setContentAreaFilled(false);
		geometryWallFillColorButton.setOpaque(true);
		geometryPanel.add(geometryWallFillColorButton, GuiUtil.setConstraints(1,1,0.25,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,0,2*BORDER_THICKNESS,0));
		
		GridBagConstraints grcl = GuiUtil.setConstraints(2,1,0.25,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		grcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryWallLineColorLabel, grcl);

		geometryWallLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryWallLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_WALL_LINE_COLOR, true));
		geometryWallLineColorButton.setContentAreaFilled(false);
		geometryWallLineColorButton.setOpaque(true);
		geometryPanel.add(geometryWallLineColorButton, GuiUtil.setConstraints(3,1,0.25,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,0,2*BORDER_THICKNESS,BORDER_THICKNESS));
/*
		GridBagConstraints grfcl = GuiUtil.setConstraints(0,2,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		grfcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryRoofFillColorLabel, grfcl);

		geometryRoofFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryRoofFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_ROOF_FILL_COLOR, true));
		geometryRoofFillColorButton.setContentAreaFilled(false);
		geometryRoofFillColorButton.setOpaque(true);
		geometryPanel.add(geometryRoofFillColorButton, GuiUtil.setConstraints(1,2,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0));

		GridBagConstraints grlcl = GuiUtil.setConstraints(2,2,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		grlcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryRoofLineColorLabel, grlcl);
		
		geometryRoofLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryRoofLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_ROOF_LINE_COLOR, true));
		geometryRoofLineColorButton.setContentAreaFilled(false);
		geometryRoofLineColorButton.setOpaque(true);
		geometryPanel.add(geometryRoofLineColorButton, GuiUtil.setConstraints(3,2,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,BORDER_THICKNESS));
*/
		geometryHighlightingCheckbox.setIconTextGap(10);
		GridBagConstraints ghcb = GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,2*BORDER_THICKNESS,0);
		ghcb.gridwidth = 2;
		geometryPanel.add(geometryHighlightingCheckbox, ghcb);

		GridBagConstraints ghlfcl = GuiUtil.setConstraints(0,3,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		ghlfcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryHLFillColorLabel, ghlfcl);

		geometryHLFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryHLFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR, true));
		geometryHLFillColorButton.setContentAreaFilled(false);
		geometryHLFillColorButton.setOpaque(true);
		geometryPanel.add(geometryHLFillColorButton, GuiUtil.setConstraints(1,3,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0));

		GridBagConstraints ghllcl = GuiUtil.setConstraints(2,3,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		ghllcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryHLLineColorLabel, ghllcl);

		geometryHLLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryHLLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
		geometryHLLineColorButton.setContentAreaFilled(false);
		geometryHLLineColorButton.setOpaque(true);
		geometryPanel.add(geometryHLLineColorButton, GuiUtil.setConstraints(3,3,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,BORDER_THICKNESS));

		GridBagConstraints ghdl = GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.NONE,0,2*BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		ghdl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryHLSurfaceDistanceLabel, ghdl);

		GridBagConstraints ghdt = GuiUtil.setConstraints(1,4,0.0,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0);
		geometryPanel.add(geometryHLSurfaceDistanceText, ghdt);

		colladaPanel = new JPanel();
		colladaPanel.setLayout(new GridBagLayout());
		colladaPanel.setBorder(BorderFactory.createTitledBorder(""));

		ignoreSurfaceOrientationCheckbox.setIconTextGap(10);
		GridBagConstraints isoc = GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0);
		isoc.gridwidth = 2;
		colladaPanel.add(ignoreSurfaceOrientationCheckbox, isoc);

		packingAlgorithms.put("BASIC", -1);
//		packingAlgorithms.put("FFDH", TextureAtlasGenerator.FFDH);
//		packingAlgorithms.put("NFDH", TextureAtlasGenerator.NFDH);
		packingAlgorithms.put("SLEA", TextureAtlasGenerator.SLEA);
		packingAlgorithms.put("TPIM", TextureAtlasGenerator.TPIM);
		packingAlgorithms.put("TPIM_WO_R", TextureAtlasGenerator.TPIM_WITHOUT_ROTATION);

		for (String algorithm: packingAlgorithms.keySet()) {
			packingAlgorithmsComboBox.addItem(algorithm);
		}

		textureAtlasCheckbox.setIconTextGap(10);
		colladaPanel.add(textureAtlasCheckbox, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,2,0));
		colladaPanel.add(packingAlgorithmsComboBox, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,2,BORDER_THICKNESS));

		textureAtlasPotsCheckbox.setIconTextGap(10);
		GridBagConstraints tapc = GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS + 23,BORDER_THICKNESS,0);
		tapc.gridwidth = 2;
		colladaPanel.add(textureAtlasPotsCheckbox, tapc);
		
		scaleTexImagesCheckbox.setIconTextGap(10);
		colladaPanel.add(scaleTexImagesCheckbox, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,2,0));
		colladaPanel.add(scaleFactorText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,2,BORDER_THICKNESS));

		ButtonGroup colladaRadioGroup = new ButtonGroup();
		colladaRadioGroup.add(groupObjectsRButton);
		colladaRadioGroup.add(colladaHighlightingRButton);

		groupObjectsRButton.setIconTextGap(10);
		colladaPanel.add(groupObjectsRButton, GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,2,0));
		colladaPanel.add(groupSizeText, GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,2,BORDER_THICKNESS));

		colladaHighlightingRButton.setIconTextGap(10);
		GridBagConstraints chrb = GuiUtil.setConstraints(0,5,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,2*BORDER_THICKNESS,0);
		chrb.gridwidth = 2;
		colladaPanel.add(colladaHighlightingRButton, chrb);

		JPanel colladaHLSubPanel = new JPanel();
		colladaHLSubPanel.setLayout(new GridBagLayout());
		GridBagConstraints chlsp = GuiUtil.setConstraints(0,6,0.0,1.0,GridBagConstraints.BOTH,0,0,0,0);
		chlsp.gridwidth = 2;
		colladaPanel.add(colladaHLSubPanel, chlsp);
		
		GridBagConstraints chlfcl = GuiUtil.setConstraints(0,0,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		chlfcl.anchor = GridBagConstraints.EAST;
		colladaHLSubPanel.add(colladaHLFillColorLabel, chlfcl);

		colladaHLFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		colladaHLFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR, true));
		colladaHLFillColorButton.setContentAreaFilled(false);
		colladaHLFillColorButton.setOpaque(true);
		colladaHLSubPanel.add(colladaHLFillColorButton, GuiUtil.setConstraints(1,0,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0));

		GridBagConstraints chllcl = GuiUtil.setConstraints(2,0,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		chllcl.anchor = GridBagConstraints.EAST;
		colladaHLSubPanel.add(colladaHLLineColorLabel, chllcl);

		colladaHLLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		colladaHLLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
		colladaHLLineColorButton.setContentAreaFilled(false);
		colladaHLLineColorButton.setOpaque(true);
		colladaHLSubPanel.add(colladaHLLineColorButton, GuiUtil.setConstraints(3,0,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,BORDER_THICKNESS));
		
		GridBagConstraints chldl = GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.NONE,0,2*BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		chldl.anchor = GridBagConstraints.EAST;
		colladaHLSubPanel.add(colladaHLSurfaceDistanceLabel, chldl);

		GridBagConstraints chldt = GuiUtil.setConstraints(1,1,0.0,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0);
		colladaHLSubPanel.add(colladaHLSurfaceDistanceText, chldt);
	
		PopupMenuDecorator.getInstance().decorate(geometryHLSurfaceDistanceText, scaleFactorText, 
				groupSizeText, colladaHLSurfaceDistanceText);
		
		scaleTexImagesCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaleFactorText.setEnabled(scaleTexImagesCheckbox.isSelected());
			}
		});

		groupObjectsRButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledHighlighting();
			}
		});

		colladaHighlightingRButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledHighlighting();
			}
		});

		footprintFillColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color fillColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseFillColor"),
						footprintFillColorButton.getBackground());
				if (fillColor != null)
					footprintFillColorButton.setBackground(fillColor);
			}
		});

		footprintLineColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color lineColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseLineColor"),
						footprintLineColorButton.getBackground());
				if (lineColor != null)
					footprintLineColorButton.setBackground(lineColor);
			}
		});

		footprintHighlightingCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledHighlighting();
			}
		});

		footprintHLFillColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color hlFillColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseHighlightedFillColor"),
						footprintHLFillColorButton.getBackground());
				if (hlFillColor != null)
					footprintHLFillColorButton.setBackground(hlFillColor);
			}
		});

		footprintHLLineColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color hlLineColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseHighlightedLineColor"),
						footprintHLLineColorButton.getBackground());
				if (hlLineColor != null)
					footprintHLLineColorButton.setBackground(hlLineColor);
			}
		});

		geometryWallFillColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color wallFillColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseWallFillColor"),
						geometryWallFillColorButton.getBackground());
				if (wallFillColor != null)
					geometryWallFillColorButton.setBackground(wallFillColor);
			}
		});

		geometryWallLineColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color wallLineColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseWallLineColor"),
						geometryWallLineColorButton.getBackground());
				if (wallLineColor != null)
					geometryWallLineColorButton.setBackground(wallLineColor);
			}
		});
/*
		geometryRoofFillColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color roofFillColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseRoofFillColor"),
						geometryRoofFillColorButton.getBackground());
				if (roofFillColor != null)
					geometryRoofFillColorButton.setBackground(roofFillColor);
			}
		});

		geometryRoofLineColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color roofLineColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseRoofLineColor"),
						geometryRoofLineColorButton.getBackground());
				if (roofLineColor != null)
					geometryRoofLineColorButton.setBackground(roofLineColor);
			}
		});
*/
		geometryHighlightingCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledHighlighting();
			}
		});

		geometryHLFillColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color hlFillColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseHighlightedFillColor"),
						geometryHLFillColorButton.getBackground());
				if (hlFillColor != null)
					geometryHLFillColorButton.setBackground(hlFillColor);
			}
		});

		geometryHLLineColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color hlLineColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseHighlightedLineColor"),
						geometryHLLineColorButton.getBackground());
				if (hlLineColor != null)
					geometryHLLineColorButton.setBackground(hlLineColor);
			}
		});

		textureAtlasCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledHighlighting();
			}
		});

		colladaHighlightingRButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledHighlighting();
			}
		});

		colladaHLFillColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color hlFillColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseHighlightedFillColor"),
						colladaHLFillColorButton.getBackground());
				if (hlFillColor != null)
					colladaHLFillColorButton.setBackground(hlFillColor);
			}
		});

		colladaHLLineColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color hlLineColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseHighlightedLineColor"),
						colladaHLLineColorButton.getBackground());
				if (hlLineColor != null)
					colladaHLLineColorButton.setBackground(hlLineColor);
			}
		});

		add(footprintPanel, GuiUtil.setConstraints(0,1,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		add(geometryPanel, GuiUtil.setConstraints(0,2,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		add(colladaPanel, GuiUtil.setConstraints(0,3,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
	}
	
	private Color chooseColor(String title, Color initialColor){
		return JColorChooser.showDialog(getTopLevelAncestor(), title, initialColor);
	}


	@Override
	public void doTranslation() {

		((TitledBorder)footprintPanel.getBorder()).setTitle(Internal.I18N.getString("pref.kmlexport.border.footprint"));	
		((TitledBorder)geometryPanel.getBorder()).setTitle(Internal.I18N.getString("pref.kmlexport.border.geometry"));	
		((TitledBorder)colladaPanel.getBorder()).setTitle(Internal.I18N.getString("pref.kmlexport.border.collada"));	

		footprintAlphaLabel.setText(Internal.I18N.getString("pref.kmlexport.label.alpha"));
		footprintFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.fillColor"));
		footprintLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.lineColor"));
		footprintHighlightingCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.highlighting"));
		footprintHLFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
		footprintHLLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedLineColor"));

		geometryAlphaLabel.setText(Internal.I18N.getString("pref.kmlexport.label.alpha"));
		geometryWallFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.fillColor"));
		geometryWallLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.lineColor"));
/*
		geometryRoofFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.roofFillColor"));
		geometryRoofLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.roofLineColor"));
*/
		geometryHighlightingCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.highlighting"));
		geometryHLSurfaceDistanceLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightingDistance"));
		geometryHLFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
		geometryHLLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedLineColor"));

		ignoreSurfaceOrientationCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.ignoreSurfaceOrientation"));
		textureAtlasCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.generateTextureAtlases"));
		textureAtlasPotsCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.textureAtlasPots"));
		scaleTexImagesCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.scaleTexImages"));
		groupObjectsRButton.setText(Internal.I18N.getString("pref.kmlexport.label.groupObjects"));
		colladaHighlightingRButton.setText(Internal.I18N.getString("pref.kmlexport.label.highlighting"));
		colladaHLSurfaceDistanceLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightingDistance"));
		colladaHLFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
		colladaHLLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedLineColor"));
	}

	@Override
	public void loadSettings() {
		internalDfs.clear();
		List<DisplayForm> configDfs = getConfigDisplayForms();
		ColladaOptions colladaOptions = getConfigColladaOptions();

		for (int form = DisplayForm.FOOTPRINT; form <= DisplayForm.COLLADA; form++) {
			DisplayForm configDf = new DisplayForm(form, -1, -1);
			int indexOfConfigDf = configDfs.indexOf(configDf); 
			if (indexOfConfigDf != -1) {
				configDf = configDfs.get(indexOfConfigDf);
			}
			DisplayForm internalDf = configDf.clone();
			internalDfs.add(internalDf);
		}

		geometryHLSurfaceDistanceLabel.setEnabled(false);
		geometryHLSurfaceDistanceText.setEnabled(false);

		colladaHLSurfaceDistanceLabel.setEnabled(false);
		colladaHLSurfaceDistanceText.setEnabled(false);

		for (DisplayForm displayForm : internalDfs) {
			switch (displayForm.getForm()) {
			case DisplayForm.FOOTPRINT:
			case DisplayForm.EXTRUDED:
				footprintHighlightingCheckbox.setSelected(displayForm.isHighlightingEnabled());

				if (displayForm.isSetRgba0()) {
					footprintFillColorButton.setBackground(new Color(displayForm.getRgba0()));
					footprintAlphaSpinner.setValue(new Integer(new Color(displayForm.getRgba0(), true).getAlpha()));
				}
				if (displayForm.isSetRgba1())
					footprintLineColorButton.setBackground(new Color(displayForm.getRgba1()));
				if (displayForm.isSetRgba4())
					footprintHLFillColorButton.setBackground(new Color(displayForm.getRgba4()));
				if (displayForm.isSetRgba5())
					footprintHLLineColorButton.setBackground(new Color(displayForm.getRgba5()));
				break;

			case DisplayForm.GEOMETRY:
				geometryHighlightingCheckbox.setSelected(displayForm.isHighlightingEnabled());
				geometryHLSurfaceDistanceText.setText(String.valueOf(displayForm.getHighlightingDistance()));
				if (displayForm.isHighlightingEnabled()) {
					geometryHighlightingCheckbox.setSelected(true);
					geometryHLSurfaceDistanceLabel.setEnabled(true);
					geometryHLSurfaceDistanceText.setEnabled(true);
				}

				if (displayForm.isSetRgba0()) {
					geometryWallFillColorButton.setBackground(new Color(displayForm.getRgba0()));
					geometryAlphaSpinner.setValue(new Integer(new Color(displayForm.getRgba0(), true).getAlpha()));
				}
				if (displayForm.isSetRgba1())
					geometryWallLineColorButton.setBackground(new Color(displayForm.getRgba1()));
/*
				if (displayForm.isSetRgba2())
					geometryRoofFillColorButton.setBackground(new Color(displayForm.getRgba2()));
				if (displayForm.isSetRgba3())
					geometryRoofLineColorButton.setBackground(new Color(displayForm.getRgba3()));
*/
				if (displayForm.isSetRgba4())
					geometryHLFillColorButton.setBackground(new Color(displayForm.getRgba4()));
				if (displayForm.isSetRgba5())
					geometryHLLineColorButton.setBackground(new Color(displayForm.getRgba5()));
				break;

			case DisplayForm.COLLADA:
				colladaHLSurfaceDistanceText.setText(String.valueOf(displayForm.getHighlightingDistance()));
				if (displayForm.isHighlightingEnabled()) {
					colladaHighlightingRButton.setSelected(true);
					colladaHLSurfaceDistanceLabel.setEnabled(true);
					colladaHLSurfaceDistanceText.setEnabled(true);
				}
				
				if (displayForm.isSetRgba4())
					colladaHLFillColorButton.setBackground(new Color(displayForm.getRgba4()));
				if (displayForm.isSetRgba5())
					colladaHLLineColorButton.setBackground(new Color(displayForm.getRgba5()));
				break;
			}
		}

		ignoreSurfaceOrientationCheckbox.setSelected(colladaOptions.isIgnoreSurfaceOrientation());
		textureAtlasCheckbox.setSelected(colladaOptions.isGenerateTextureAtlases());
		textureAtlasPotsCheckbox.setSelected(colladaOptions.isTextureAtlasPots());
		for (String key: packingAlgorithms.keySet()) {
			if (packingAlgorithms.get(key).intValue() == colladaOptions.getPackingAlgorithm()) {
				packingAlgorithmsComboBox.setSelectedItem(key);
				break;
			}
		}

		scaleTexImagesCheckbox.setSelected(false);
		scaleFactorText.setEnabled(false);
		scaleFactorText.setText(String.valueOf(colladaOptions.getImageScaleFactor()));
		if (colladaOptions.isScaleImages()) {
			scaleTexImagesCheckbox.setSelected(true);
			scaleFactorText.setEnabled(true);
		}

		groupSizeText.setEnabled(false);
		groupSizeText.setText(String.valueOf(colladaOptions.getGroupSize()));
		if (colladaOptions.isGroupObjects()) {
			groupObjectsRButton.setSelected(true);
			groupSizeText.setEnabled(true);
		}

		setEnabledHighlighting();
	}

	@Override
	public void setSettings() {
		setInternalDisplayFormValues();
		ColladaOptions colladaOptions = getConfigColladaOptions();
		List<DisplayForm> configDfs = getConfigDisplayForms();

		if (configDfs.isEmpty()) {
			setConfigDisplayForms(internalDfs);
		}
		else {
			for (DisplayForm internalDf : internalDfs) {
				int indexOfConfigDf = configDfs.indexOf(internalDf); 
				if (indexOfConfigDf != -1) {
					DisplayForm configDf = configDfs.get(indexOfConfigDf);
					// clone cannot be used here because of isActive() and visibleFrom()
					copyColorAndHighlightingValues(internalDf, configDf);
				}
			}
		}

		colladaOptions.setIgnoreSurfaceOrientation(ignoreSurfaceOrientationCheckbox.isSelected());
		colladaOptions.setGenerateTextureAtlases(textureAtlasCheckbox.isSelected());
		colladaOptions.setTextureAtlasPots(textureAtlasPotsCheckbox.isSelected());
		colladaOptions.setPackingAlgorithm(packingAlgorithms.get(packingAlgorithmsComboBox.getSelectedItem()).intValue()); 

		colladaOptions.setScaleImages(scaleTexImagesCheckbox.isSelected());
		try {
			colladaOptions.setImageScaleFactor(Double.parseDouble(scaleFactorText.getText().trim()));
			if (colladaOptions.getImageScaleFactor() <= 0 || colladaOptions.getImageScaleFactor() > 1) {
				colladaOptions.setImageScaleFactor(1);
			}
		}
		catch (NumberFormatException nfe) {}

		colladaOptions.setGroupObjects(groupObjectsRButton.isSelected());
		try {
			colladaOptions.setGroupSize(Integer.parseInt(groupSizeText.getText().trim()));
			if (colladaOptions.getGroupSize() < 2) {
				colladaOptions.setGroupSize(1);
			}
		}
		catch (NumberFormatException nfe) {}
	}


	private void setInternalDisplayFormValues() {
		for (int form = DisplayForm.FOOTPRINT; form <= DisplayForm.EXTRUDED; form++) {
			DisplayForm df = new DisplayForm(form, -1, -1);
			int indexOfDf = internalDfs.indexOf(df); 
			if (indexOfDf != -1) {
				df = internalDfs.get(indexOfDf);
				df.setHighlightingEnabled(footprintHighlightingCheckbox.isSelected());

				Color rgba0 = new Color(footprintFillColorButton.getBackground().getRed(),
						footprintFillColorButton.getBackground().getGreen(),
						footprintFillColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				df.setRgba0(rgba0.getRGB());
				Color rgba1 = new Color(footprintLineColorButton.getBackground().getRed(),
						footprintLineColorButton.getBackground().getGreen(),
						footprintLineColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				df.setRgba1(rgba1.getRGB());
				Color rgba4 = new Color(footprintHLFillColorButton.getBackground().getRed(),
						footprintHLFillColorButton.getBackground().getGreen(),
						footprintHLFillColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				df.setRgba4(rgba4.getRGB());
				Color rgba5 = new Color(footprintHLLineColorButton.getBackground().getRed(),
						footprintHLLineColorButton.getBackground().getGreen(),
						footprintHLLineColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				df.setRgba5(rgba5.getRGB());
			}
		}

		DisplayForm df = new DisplayForm(DisplayForm.GEOMETRY, -1, -1);
		int indexOfDf = internalDfs.indexOf(df); 
		if (indexOfDf != -1) {
			df = internalDfs.get(indexOfDf);
			df.setHighlightingEnabled(geometryHighlightingCheckbox.isSelected());
			try {
				df.setHighlightingDistance(Double.parseDouble(geometryHLSurfaceDistanceText.getText().trim()));
				if (df.getHighlightingDistance() <= 0 || df.getHighlightingDistance() > 10) {
					df.setHighlightingDistance(1.0);
				}
			}
			catch (NumberFormatException nfe) {}

			Color rgba0 = new Color(geometryWallFillColorButton.getBackground().getRed(),
					geometryWallFillColorButton.getBackground().getGreen(),
					geometryWallFillColorButton.getBackground().getBlue(),
					((Integer)geometryAlphaSpinner.getValue()).intValue());
			df.setRgba0(rgba0.getRGB());
			Color rgba1 = new Color(geometryWallLineColorButton.getBackground().getRed(),
					geometryWallLineColorButton.getBackground().getGreen(),
					geometryWallLineColorButton.getBackground().getBlue(),
					((Integer)geometryAlphaSpinner.getValue()).intValue());
			df.setRgba1(rgba1.getRGB());
/*
			Color rgba2 = new Color(geometryRoofFillColorButton.getBackground().getRed(),
					geometryRoofFillColorButton.getBackground().getGreen(),
					geometryRoofFillColorButton.getBackground().getBlue(),
					((Integer)geometryAlphaSpinner.getValue()).intValue());
			df.setRgba2(rgba2.getRGB());
			Color rgba3 = new Color(geometryRoofLineColorButton.getBackground().getRed(),
					geometryRoofLineColorButton.getBackground().getGreen(),
					geometryRoofLineColorButton.getBackground().getBlue(),
					((Integer)geometryAlphaSpinner.getValue()).intValue());
			df.setRgba3(rgba3.getRGB());
*/
			Color rgba4 = new Color(geometryHLFillColorButton.getBackground().getRed(),
					geometryHLFillColorButton.getBackground().getGreen(),
					geometryHLFillColorButton.getBackground().getBlue(),
					DisplayForm.DEFAULT_ALPHA_VALUE);
			df.setRgba4(rgba4.getRGB());
			Color rgba5 = new Color(geometryHLLineColorButton.getBackground().getRed(),
					geometryHLLineColorButton.getBackground().getGreen(),
					geometryHLLineColorButton.getBackground().getBlue(),
					DisplayForm.DEFAULT_ALPHA_VALUE);
			df.setRgba5(rgba5.getRGB());
		}

		df = new DisplayForm(DisplayForm.COLLADA, -1, -1);
		indexOfDf = internalDfs.indexOf(df); 
		if (indexOfDf != -1) {
			df = internalDfs.get(indexOfDf);
			df.setHighlightingEnabled(colladaHighlightingRButton.isSelected());
			try {
				df.setHighlightingDistance(Double.parseDouble(colladaHLSurfaceDistanceText.getText().trim()));
				if (df.getHighlightingDistance() <= 0 || df.getHighlightingDistance() >10) {
					df.setHighlightingDistance(1.0);
				}
			}
			catch (NumberFormatException nfe) {}

			Color rgba4 = new Color(colladaHLFillColorButton.getBackground().getRed(),
					colladaHLFillColorButton.getBackground().getGreen(),
					colladaHLFillColorButton.getBackground().getBlue(),
					DisplayForm.DEFAULT_ALPHA_VALUE);
			df.setRgba4(rgba4.getRGB());
			Color rgba5 = new Color(colladaHLLineColorButton.getBackground().getRed(),
					colladaHLLineColorButton.getBackground().getGreen(),
					colladaHLLineColorButton.getBackground().getBlue(),
					DisplayForm.DEFAULT_ALPHA_VALUE);
			df.setRgba5(rgba5.getRGB());
		}
	}


	@Override
	public void resetSettings() {
		ColladaOptions colladaOptions = getConfigColladaOptions();
		List<DisplayForm> configDfs = getConfigDisplayForms();

		for (int form = DisplayForm.FOOTPRINT; form <= DisplayForm.EXTRUDED; form++) {
			DisplayForm df = new DisplayForm(form, -1, -1);
			int indexOfDf = configDfs.indexOf(df); 
			if (indexOfDf != -1) {
				df = configDfs.get(indexOfDf);
				df.setHighlightingEnabled(false);

				df.setRgba0(DisplayForm.DEFAULT_FILL_COLOR);
				df.setRgba1(DisplayForm.DEFAULT_LINE_COLOR);
				df.setRgba4(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
				df.setRgba5(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
			}
		}

		DisplayForm df = new DisplayForm(DisplayForm.GEOMETRY, -1, -1);
		int indexOfDf = configDfs.indexOf(df); 
		if (indexOfDf != -1) {
			df = configDfs.get(indexOfDf);
			df.setHighlightingEnabled(false);
			df.setHighlightingDistance(0.75);

			df.setRgba0(DisplayForm.DEFAULT_WALL_FILL_COLOR);
			df.setRgba1(DisplayForm.DEFAULT_WALL_LINE_COLOR);
			df.setRgba2(DisplayForm.DEFAULT_ROOF_FILL_COLOR);
			df.setRgba3(DisplayForm.DEFAULT_ROOF_LINE_COLOR);
			df.setRgba4(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
			df.setRgba5(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
		}

		df = new DisplayForm(DisplayForm.COLLADA, -1, -1);
		indexOfDf = configDfs.indexOf(df); 
		if (indexOfDf != -1) {
			df = configDfs.get(indexOfDf);
			df.setHighlightingEnabled(false);
			df.setHighlightingDistance(0.75);
			
			df.setRgba4(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
			df.setRgba5(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
		}

		colladaOptions.setIgnoreSurfaceOrientation(false);
		colladaOptions.setGenerateTextureAtlases(true);
		colladaOptions.setTextureAtlasPots(true);
		colladaOptions.setPackingAlgorithm(TextureAtlasGenerator.TPIM); 

		colladaOptions.setScaleImages(false);
		colladaOptions.setImageScaleFactor(1);

		colladaOptions.setGroupObjects(false);
		colladaOptions.setGroupSize(1);

		loadSettings(); // update GUI
	}

	private void setEnabledHighlighting() {
		footprintHLFillColorLabel.setEnabled(footprintHighlightingCheckbox.isSelected());
		footprintHLFillColorButton.setEnabled(footprintHighlightingCheckbox.isSelected());
		footprintHLLineColorLabel.setEnabled(footprintHighlightingCheckbox.isSelected());
		footprintHLLineColorButton.setEnabled(footprintHighlightingCheckbox.isSelected());

		geometryHLFillColorLabel.setEnabled(geometryHighlightingCheckbox.isSelected());
		geometryHLFillColorButton.setEnabled(geometryHighlightingCheckbox.isSelected());
		geometryHLLineColorLabel.setEnabled(geometryHighlightingCheckbox.isSelected());
		geometryHLLineColorButton.setEnabled(geometryHighlightingCheckbox.isSelected());

		geometryHLSurfaceDistanceLabel.setEnabled(geometryHighlightingCheckbox.isSelected());
		geometryHLSurfaceDistanceText.setEnabled(geometryHighlightingCheckbox.isSelected());

		packingAlgorithmsComboBox.setEnabled(textureAtlasCheckbox.isSelected());
		textureAtlasPotsCheckbox.setEnabled(textureAtlasCheckbox.isSelected());
		groupSizeText.setEnabled(groupObjectsRButton.isSelected());

		colladaHLFillColorLabel.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLFillColorButton.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLLineColorLabel.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLLineColorButton.setEnabled(colladaHighlightingRButton.isSelected());

		colladaHLSurfaceDistanceLabel.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLSurfaceDistanceText.setEnabled(colladaHighlightingRButton.isSelected());
	}

	// equals cannot be used, for internal reasons it only compares the form value (FOOTPRINT, EXTRUDED...)
	private boolean areDisplayFormsContentsDifferent (DisplayForm df1, DisplayForm df2) {
		if (df1 == null || df2 == null) return true;
		if (df1.isHighlightingEnabled() != df2.isHighlightingEnabled()) return true;
		if (df1.getHighlightingDistance() != df2.getHighlightingDistance()) return true;
		if (df1.getRgba0() != df2.getRgba0()) return true;
		if (df1.getRgba1() != df2.getRgba1()) return true;
		if (df1.getRgba2() != df2.getRgba2()) return true;
		if (df1.getRgba3() != df2.getRgba3()) return true;
		if (df1.getRgba4() != df2.getRgba4()) return true;
		if (df1.getRgba5() != df2.getRgba5()) return true;
		return false;
	}

	private void copyColorAndHighlightingValues (DisplayForm original, DisplayForm copy) {
		copy.setHighlightingDistance(original.getHighlightingDistance());
		copy.setHighlightingEnabled(original.isHighlightingEnabled());
		copy.setRgba0(original.getRgba0());
		copy.setRgba1(original.getRgba1());
		copy.setRgba2(original.getRgba2());
		copy.setRgba3(original.getRgba3());
		copy.setRgba4(original.getRgba4());
		copy.setRgba5(original.getRgba5());
	}
}
