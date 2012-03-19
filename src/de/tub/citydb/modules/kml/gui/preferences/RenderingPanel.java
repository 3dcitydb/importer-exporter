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
package de.tub.citydb.modules.kml.gui.preferences;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

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
import de.tub.citydb.config.project.kmlExporter.DisplayLevel;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class RenderingPanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;

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
	private JLabel geometryRoofFillColorLabel = new JLabel();
	private JButton geometryRoofFillColorButton = new JButton(" ");
	private JLabel geometryWallLineColorLabel = new JLabel();
	private JButton geometryWallLineColorButton = new JButton(" ");
	private JLabel geometryRoofLineColorLabel = new JLabel();
	private JButton geometryRoofLineColorButton = new JButton(" ");
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
	private JRadioButton groupBuildingsRButton = new JRadioButton();
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

	public RenderingPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		if (footprintHighlightingCheckbox.isSelected() != kmlExporter.isFootprintHighlighting()) return true;

		DisplayLevel dl = new DisplayLevel(DisplayLevel.FOOTPRINT, -1, -1);
		int indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			int alphaValue = Integer.valueOf(((JSpinner.DefaultEditor)footprintAlphaSpinner.getEditor()).getTextField().getText().trim());
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
			Color footprintFillColor = new Color(footprintFillColorButton.getBackground().getRed(),
					footprintFillColorButton.getBackground().getGreen(),
					footprintFillColorButton.getBackground().getBlue(),
					alphaValue);
			if ((dl.isSetRgba0() && footprintFillColor.getRGB() != dl.getRgba0()) || 
					(!dl.isSetRgba0() && footprintFillColor.getRGB() != DisplayLevel.DEFAULT_FILL_COLOR)) {
				return true;
			}

			Color footprintLineColor = new Color(footprintLineColorButton.getBackground().getRed(),
					footprintLineColorButton.getBackground().getGreen(),
					footprintLineColorButton.getBackground().getBlue(),
					alphaValue);
			if ((dl.isSetRgba1() && footprintLineColor.getRGB() != dl.getRgba1()) || 
					(!dl.isSetRgba1() && footprintLineColor.getRGB() != DisplayLevel.DEFAULT_LINE_COLOR)) {
				return true;
			}

			Color footprintHLFillColor = new Color(footprintHLFillColorButton.getBackground().getRed(),
					footprintHLFillColorButton.getBackground().getGreen(),
					footprintHLFillColorButton.getBackground().getBlue(),
					alphaValue);
			if ((dl.isSetRgba4() && footprintHLFillColor.getRGB() != dl.getRgba4()) || 
					(!dl.isSetRgba4() && footprintHLFillColor.getRGB() != DisplayLevel.DEFAULT_FILL_HIGHLIGHTED_COLOR)) {
				return true;
			}

			Color footprintHLLineColor = new Color(footprintHLLineColorButton.getBackground().getRed(),
					footprintHLLineColorButton.getBackground().getGreen(),
					footprintHLLineColorButton.getBackground().getBlue(),
					alphaValue);
			if ((dl.isSetRgba5() && footprintHLLineColor.getRGB() != dl.getRgba5()) || 
					(!dl.isSetRgba5() && footprintHLLineColor.getRGB() != DisplayLevel.DEFAULT_LINE_HIGHLIGHTED_COLOR)) {
				return true;
			}
		}

		if (geometryHighlightingCheckbox.isSelected() != kmlExporter.isGeometryHighlighting()) return true;

		dl = new DisplayLevel(DisplayLevel.GEOMETRY, -1, -1);
		indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			int alphaValue = Integer.valueOf(((JSpinner.DefaultEditor)geometryAlphaSpinner.getEditor()).getTextField().getText().trim());
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
			Color geometryWallFillColor = new Color(geometryWallFillColorButton.getBackground().getRed(),
					geometryWallFillColorButton.getBackground().getGreen(),
					geometryWallFillColorButton.getBackground().getBlue(),
					alphaValue);
			if ((dl.isSetRgba0() && geometryWallFillColor.getRGB() != dl.getRgba0()) || 
					(!dl.isSetRgba0() && geometryWallFillColor.getRGB() != DisplayLevel.DEFAULT_WALL_FILL_COLOR)) {
				return true;
			}

			Color geometryWallLineColor = new Color(geometryWallLineColorButton.getBackground().getRed(),
					geometryWallLineColorButton.getBackground().getGreen(),
					geometryWallLineColorButton.getBackground().getBlue(),
					alphaValue);
			if ((dl.isSetRgba1() && geometryWallLineColor.getRGB() != dl.getRgba1()) || 
					(!dl.isSetRgba1() && geometryWallLineColor.getRGB() != DisplayLevel.DEFAULT_WALL_LINE_COLOR)) {
				return true;
			}

			Color geometryRoofFillColor = new Color(geometryRoofFillColorButton.getBackground().getRed(),
					geometryRoofFillColorButton.getBackground().getGreen(),
					geometryRoofFillColorButton.getBackground().getBlue(),
					alphaValue);
			if ((dl.isSetRgba2() && geometryRoofFillColor.getRGB() != dl.getRgba2()) || 
					(!dl.isSetRgba2() && geometryRoofFillColor.getRGB() != DisplayLevel.DEFAULT_ROOF_FILL_COLOR)) {
				return true;
			}

			Color geometryRoofLineColor = new Color(geometryRoofLineColorButton.getBackground().getRed(),
					geometryRoofLineColorButton.getBackground().getGreen(),
					geometryRoofLineColorButton.getBackground().getBlue(),
					alphaValue);
			if ((dl.isSetRgba3() && geometryRoofLineColor.getRGB() != dl.getRgba3()) || 
					(!dl.isSetRgba3() && geometryRoofLineColor.getRGB() != DisplayLevel.DEFAULT_ROOF_LINE_COLOR)) {
				return true;
			}

			Color geometryHLFillColor = new Color(geometryHLFillColorButton.getBackground().getRed(),
					geometryHLFillColorButton.getBackground().getGreen(),
					geometryHLFillColorButton.getBackground().getBlue(),
					DisplayLevel.DEFAULT_ALPHA_VALUE);
			if ((dl.isSetRgba4() && geometryHLFillColor.getRGB() != dl.getRgba4()) || 
					(!dl.isSetRgba4() && geometryHLFillColor.getRGB() != DisplayLevel.DEFAULT_FILL_HIGHLIGHTED_COLOR)) {
				return true;
			}

			Color geometryHLLineColor = new Color(geometryHLLineColorButton.getBackground().getRed(),
					geometryHLLineColorButton.getBackground().getGreen(),
					geometryHLLineColorButton.getBackground().getBlue(),
					DisplayLevel.DEFAULT_ALPHA_VALUE);
			if ((dl.isSetRgba5() && geometryHLLineColor.getRGB() != dl.getRgba5()) || 
					(!dl.isSetRgba5() && geometryHLLineColor.getRGB() != DisplayLevel.DEFAULT_LINE_HIGHLIGHTED_COLOR)) {
				return true;
			}

		}

		int level = DisplayLevel.COLLADA;
		dl = new DisplayLevel(level, -1, -1);
		indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
			Color colladaHLFillColor = new Color(colladaHLFillColorButton.getBackground().getRed(),
					colladaHLFillColorButton.getBackground().getGreen(),
					colladaHLFillColorButton.getBackground().getBlue(),
					DisplayLevel.DEFAULT_ALPHA_VALUE);
			if ((dl.isSetRgba4() && colladaHLFillColor.getRGB() != dl.getRgba4()) || 
					(!dl.isSetRgba4() && colladaHLFillColor.getRGB() != DisplayLevel.DEFAULT_FILL_HIGHLIGHTED_COLOR)) {
				return true;
			}

			Color colladaHLLineColor = new Color(colladaHLLineColorButton.getBackground().getRed(),
					colladaHLLineColorButton.getBackground().getGreen(),
					colladaHLLineColorButton.getBackground().getBlue(),
					DisplayLevel.DEFAULT_ALPHA_VALUE);
			if ((dl.isSetRgba5() && colladaHLLineColor.getRGB() != dl.getRgba5()) || 
					(!dl.isSetRgba5() && colladaHLLineColor.getRGB() != DisplayLevel.DEFAULT_LINE_HIGHLIGHTED_COLOR)) {
				return true;
			}
		}

		if (ignoreSurfaceOrientationCheckbox.isSelected() != kmlExporter.isIgnoreSurfaceOrientation()) return true;
		if (textureAtlasCheckbox.isSelected() != kmlExporter.isGenerateTextureAtlases()) return true;
		if (textureAtlasPotsCheckbox.isSelected() != kmlExporter.isTextureAtlasPots()) return true;
		if (packingAlgorithms.get(packingAlgorithmsComboBox.getSelectedItem()).intValue() != kmlExporter.getPackingAlgorithm()) return true;

		int groupSize = 1;
		try {
			groupSize = Integer.parseInt(groupSizeText.getText().trim());
			if (groupSize < 2) {
				groupSize = 1;
			}
		}
		catch (NumberFormatException nfe) {return true;}
		//		groupSizeText.setText(String.valueOf(groupSize));
		if (groupBuildingsRButton.isSelected() != kmlExporter.isGroupBuildings() ||
				groupSize != kmlExporter.getGroupSize()) return true;

		double imageScaleFactor = 1;
		try {
			imageScaleFactor = Double.parseDouble(scaleFactorText.getText().trim());
			if (imageScaleFactor <= 0 || imageScaleFactor > 1) {
				imageScaleFactor = 1;
			}
		}
		catch (NumberFormatException nfe) {return true;}
		//		scaleFactorText.setText(String.valueOf(imageScaleFactor));
		if (scaleTexImagesCheckbox.isSelected() != kmlExporter.isScaleImages() ||
				imageScaleFactor != kmlExporter.getImageScaleFactor()) return true;

		double geometryHighlightingDistance = 1.0;
		try {
			geometryHighlightingDistance = Double.parseDouble(geometryHLSurfaceDistanceText.getText().trim());
			if (geometryHighlightingDistance <= 0 || geometryHighlightingDistance > 10) {
				geometryHighlightingDistance = 1.0;
			}
		}
		catch (NumberFormatException nfe) {return true;}
		//		geometryHLSurfaceDistanceText.setText(String.valueOf(geometryHighlightingDistance));
		if (geometryHighlightingCheckbox.isSelected() != kmlExporter.isGeometryHighlighting() ||
				geometryHighlightingDistance != kmlExporter.getGeometryHighlightingDistance()) return true;

		double colladaHighlightingDistance = 1.0;
		try {
			colladaHighlightingDistance = Double.parseDouble(colladaHLSurfaceDistanceText.getText().trim());
			if (colladaHighlightingDistance <= 0 || colladaHighlightingDistance > 10) {
				colladaHighlightingDistance = 1.0;
			}
		}
		catch (NumberFormatException nfe) {return true;}
		//		colladaHLSurfaceDistanceText.setText(String.valueOf(colladaHighlightingDistance));
		if (colladaHighlightingRButton.isSelected() != kmlExporter.isColladaHighlighting() ||
				colladaHighlightingDistance != kmlExporter.getColladaHighlightingDistance()) return true;

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		footprintPanel = new JPanel();
		add(footprintPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
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
		footprintFillColorButton.setBackground(new Color(DisplayLevel.DEFAULT_FILL_COLOR, true));
		footprintFillColorButton.setContentAreaFilled(false);
		footprintFillColorButton.setOpaque(true);
		footprintPanel.add(footprintFillColorButton, GuiUtil.setConstraints(1,1,0.25,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,0,2*BORDER_THICKNESS,0));
		
		GridBagConstraints flcl = GuiUtil.setConstraints(2,1,0.25,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		flcl.anchor = GridBagConstraints.EAST;
		footprintPanel.add(footprintLineColorLabel, flcl);

		footprintLineColorButton.setPreferredSize(footprintAlphaSpinner.getPreferredSize());
		footprintLineColorButton.setBackground(new Color(DisplayLevel.DEFAULT_LINE_COLOR, true));
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
		footprintHLFillColorButton.setBackground(new Color(DisplayLevel.DEFAULT_FILL_HIGHLIGHTED_COLOR, true));
		footprintHLFillColorButton.setContentAreaFilled(false);
		footprintHLFillColorButton.setOpaque(true);
		footprintPanel.add(footprintHLFillColorButton, GuiUtil.setConstraints(1,3,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0));

		GridBagConstraints fhllcl = GuiUtil.setConstraints(2,3,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		fhllcl.anchor = GridBagConstraints.EAST;
		footprintPanel.add(footprintHLLineColorLabel, fhllcl);

		footprintHLLineColorButton.setPreferredSize(footprintAlphaSpinner.getPreferredSize());
		footprintHLLineColorButton.setBackground(new Color(DisplayLevel.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
		footprintHLLineColorButton.setContentAreaFilled(false);
		footprintHLLineColorButton.setOpaque(true);
		footprintPanel.add(footprintHLLineColorButton, GuiUtil.setConstraints(3,3,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,BORDER_THICKNESS));

		geometryPanel = new JPanel();
		add(geometryPanel, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
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
		geometryWallFillColorButton.setBackground(new Color(DisplayLevel.DEFAULT_WALL_FILL_COLOR, true));
		geometryWallFillColorButton.setContentAreaFilled(false);
		geometryWallFillColorButton.setOpaque(true);
		geometryPanel.add(geometryWallFillColorButton, GuiUtil.setConstraints(1,1,0.25,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,0,2*BORDER_THICKNESS,0));
		
		GridBagConstraints grcl = GuiUtil.setConstraints(2,1,0.25,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		grcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryWallLineColorLabel, grcl);

		geometryWallLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryWallLineColorButton.setBackground(new Color(DisplayLevel.DEFAULT_WALL_LINE_COLOR, true));
		geometryWallLineColorButton.setContentAreaFilled(false);
		geometryWallLineColorButton.setOpaque(true);
		geometryPanel.add(geometryWallLineColorButton, GuiUtil.setConstraints(3,1,0.25,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,0,2*BORDER_THICKNESS,BORDER_THICKNESS));

		GridBagConstraints ghlwcl = GuiUtil.setConstraints(0,2,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		ghlwcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryRoofFillColorLabel, ghlwcl);

		geometryRoofFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryRoofFillColorButton.setBackground(new Color(DisplayLevel.DEFAULT_ROOF_FILL_COLOR, true));
		geometryRoofFillColorButton.setContentAreaFilled(false);
		geometryRoofFillColorButton.setOpaque(true);
		geometryPanel.add(geometryRoofFillColorButton, GuiUtil.setConstraints(1,2,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0));

		GridBagConstraints ghlrcl = GuiUtil.setConstraints(2,2,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		ghlrcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryRoofLineColorLabel, ghlrcl);
		
		geometryRoofLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryRoofLineColorButton.setBackground(new Color(DisplayLevel.DEFAULT_ROOF_LINE_COLOR, true));
		geometryRoofLineColorButton.setContentAreaFilled(false);
		geometryRoofLineColorButton.setOpaque(true);
		geometryPanel.add(geometryRoofLineColorButton, GuiUtil.setConstraints(3,2,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,BORDER_THICKNESS));

		geometryHighlightingCheckbox.setIconTextGap(10);
		GridBagConstraints ghcb = GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,2*BORDER_THICKNESS,0);
		ghcb.gridwidth = 2;
		geometryPanel.add(geometryHighlightingCheckbox, ghcb);

		GridBagConstraints ghlfcl = GuiUtil.setConstraints(0,4,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		ghlfcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryHLFillColorLabel, ghlfcl);

		geometryHLFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryHLFillColorButton.setBackground(new Color(DisplayLevel.DEFAULT_FILL_HIGHLIGHTED_COLOR, true));
		geometryHLFillColorButton.setContentAreaFilled(false);
		geometryHLFillColorButton.setOpaque(true);
		geometryPanel.add(geometryHLFillColorButton, GuiUtil.setConstraints(1,4,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0));

		GridBagConstraints ghllcl = GuiUtil.setConstraints(2,4,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		ghllcl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryHLLineColorLabel, ghllcl);

		geometryHLLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryHLLineColorButton.setBackground(new Color(DisplayLevel.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
		geometryHLLineColorButton.setContentAreaFilled(false);
		geometryHLLineColorButton.setOpaque(true);
		geometryPanel.add(geometryHLLineColorButton, GuiUtil.setConstraints(3,4,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,BORDER_THICKNESS));

		GridBagConstraints ghdl = GuiUtil.setConstraints(0,5,0.0,1.0,GridBagConstraints.NONE,0,2*BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		ghdl.anchor = GridBagConstraints.EAST;
		geometryPanel.add(geometryHLSurfaceDistanceLabel, ghdl);

		GridBagConstraints ghdt = GuiUtil.setConstraints(1,5,0.0,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0);
		geometryPanel.add(geometryHLSurfaceDistanceText, ghdt);

		colladaPanel = new JPanel();
		add(colladaPanel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
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
		colladaRadioGroup.add(groupBuildingsRButton);
		colladaRadioGroup.add(colladaHighlightingRButton);

		groupBuildingsRButton.setIconTextGap(10);
		colladaPanel.add(groupBuildingsRButton, GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,2,0));
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
		colladaHLFillColorButton.setBackground(new Color(DisplayLevel.DEFAULT_FILL_HIGHLIGHTED_COLOR, true));
		colladaHLFillColorButton.setContentAreaFilled(false);
		colladaHLFillColorButton.setOpaque(true);
		colladaHLSubPanel.add(colladaHLFillColorButton, GuiUtil.setConstraints(1,0,0.25,1.0,GridBagConstraints.HORIZONTAL,0,0,2*BORDER_THICKNESS,0));

		GridBagConstraints chllcl = GuiUtil.setConstraints(2,0,0.25,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,2*BORDER_THICKNESS,BORDER_THICKNESS);
		chllcl.anchor = GridBagConstraints.EAST;
		colladaHLSubPanel.add(colladaHLLineColorLabel, chllcl);

		colladaHLLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		colladaHLLineColorButton.setBackground(new Color(DisplayLevel.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
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

		groupBuildingsRButton.addActionListener(new ActionListener() {
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
		geometryWallFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.wallFillColor"));
		geometryWallLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.wallLineColor"));
		geometryRoofFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.roofFillColor"));
		geometryRoofLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.roofLineColor"));
		geometryHighlightingCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.highlighting"));
		geometryHLSurfaceDistanceLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightingDistance"));
		geometryHLFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
		geometryHLLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedLineColor"));

		ignoreSurfaceOrientationCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.ignoreSurfaceOrientation"));
		textureAtlasCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.generateTextureAtlases"));
		textureAtlasPotsCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.textureAtlasPots"));
		scaleTexImagesCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.scaleTexImages"));
		groupBuildingsRButton.setText(Internal.I18N.getString("pref.kmlexport.label.groupBuildings"));
		colladaHighlightingRButton.setText(Internal.I18N.getString("pref.kmlexport.label.highlighting"));
		colladaHLSurfaceDistanceLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightingDistance"));
		colladaHLFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
		colladaHLLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedLineColor"));
	}

	@Override
	public void loadSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		for (DisplayLevel displayLevel : kmlExporter.getDisplayLevels()) {
			switch (displayLevel.getLevel()) {
			case DisplayLevel.FOOTPRINT:
			case DisplayLevel.EXTRUDED:
				if (displayLevel.isSetRgba0()) {
					footprintFillColorButton.setBackground(new Color(displayLevel.getRgba0()));
					footprintAlphaSpinner.setValue(new Integer(new Color(displayLevel.getRgba0(), true).getAlpha()));
				}
				if (displayLevel.isSetRgba1())
					footprintLineColorButton.setBackground(new Color(displayLevel.getRgba1()));
				if (displayLevel.isSetRgba4())
					footprintHLFillColorButton.setBackground(new Color(displayLevel.getRgba4()));
				if (displayLevel.isSetRgba5())
					footprintHLLineColorButton.setBackground(new Color(displayLevel.getRgba5()));
				break;

			case DisplayLevel.GEOMETRY:
				if (displayLevel.isSetRgba0()) {
					geometryWallFillColorButton.setBackground(new Color(displayLevel.getRgba0()));
					geometryAlphaSpinner.setValue(new Integer(new Color(displayLevel.getRgba0(), true).getAlpha()));
				}
				if (displayLevel.isSetRgba1())
					geometryWallLineColorButton.setBackground(new Color(displayLevel.getRgba1()));
				if (displayLevel.isSetRgba2())
					geometryRoofFillColorButton.setBackground(new Color(displayLevel.getRgba2()));
				if (displayLevel.isSetRgba3())
					geometryRoofLineColorButton.setBackground(new Color(displayLevel.getRgba3()));
				if (displayLevel.isSetRgba4())
					geometryHLFillColorButton.setBackground(new Color(displayLevel.getRgba4()));
				if (displayLevel.isSetRgba5())
					geometryHLLineColorButton.setBackground(new Color(displayLevel.getRgba5()));
				break;

			case DisplayLevel.COLLADA:
				if (displayLevel.isSetRgba4())
					colladaHLFillColorButton.setBackground(new Color(displayLevel.getRgba4()));
				if (displayLevel.isSetRgba5())
					colladaHLLineColorButton.setBackground(new Color(displayLevel.getRgba5()));
				break;
			}
		}

		footprintHighlightingCheckbox.setSelected(kmlExporter.isFootprintHighlighting());
		geometryHighlightingCheckbox.setSelected(kmlExporter.isGeometryHighlighting());

		geometryHLSurfaceDistanceLabel.setEnabled(false);
		geometryHLSurfaceDistanceText.setEnabled(false);
		geometryHLSurfaceDistanceText.setText(String.valueOf(kmlExporter.getGeometryHighlightingDistance()));
		if (kmlExporter.isGeometryHighlighting()) {
			geometryHighlightingCheckbox.setSelected(true);
			geometryHLSurfaceDistanceLabel.setEnabled(true);
			geometryHLSurfaceDistanceText.setEnabled(true);
		}

		ignoreSurfaceOrientationCheckbox.setSelected(kmlExporter.isIgnoreSurfaceOrientation());
		textureAtlasCheckbox.setSelected(kmlExporter.isGenerateTextureAtlases());
		textureAtlasPotsCheckbox.setSelected(kmlExporter.isTextureAtlasPots());
		for (String key: packingAlgorithms.keySet()) {
			if (packingAlgorithms.get(key).intValue() == kmlExporter.getPackingAlgorithm()) {
				packingAlgorithmsComboBox.setSelectedItem(key);
				break;
			}
		}

		scaleTexImagesCheckbox.setSelected(false);
		scaleFactorText.setEnabled(false);
		scaleFactorText.setText(String.valueOf(kmlExporter.getImageScaleFactor()));
		if (kmlExporter.isScaleImages()) {
			scaleTexImagesCheckbox.setSelected(true);
			scaleFactorText.setEnabled(true);
		}

		groupSizeText.setEnabled(false);
		groupSizeText.setText(String.valueOf(kmlExporter.getGroupSize()));
		if (kmlExporter.isGroupBuildings()) {
			groupBuildingsRButton.setSelected(true);
			groupSizeText.setEnabled(true);
		}

		colladaHLSurfaceDistanceLabel.setEnabled(false);
		colladaHLSurfaceDistanceText.setEnabled(false);
		colladaHLSurfaceDistanceText.setText(String.valueOf(kmlExporter.getColladaHighlightingDistance()));
		if (kmlExporter.isColladaHighlighting()) {
			colladaHighlightingRButton.setSelected(true);
			colladaHLSurfaceDistanceLabel.setEnabled(true);
			colladaHLSurfaceDistanceText.setEnabled(true);
		}

		setEnabledHighlighting();
	}

	@Override
	public void setSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		for (int level = DisplayLevel.FOOTPRINT; level <= DisplayLevel.EXTRUDED; level++) {
			DisplayLevel dl = new DisplayLevel(level, -1, -1);
			int indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
			if (indexOfDl != -1) {
				dl = kmlExporter.getDisplayLevels().get(indexOfDl);
				Color rgba0 = new Color(footprintFillColorButton.getBackground().getRed(),
						footprintFillColorButton.getBackground().getGreen(),
						footprintFillColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				dl.setRgba0(rgba0.getRGB());
				Color rgba1 = new Color(footprintLineColorButton.getBackground().getRed(),
						footprintLineColorButton.getBackground().getGreen(),
						footprintLineColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				dl.setRgba1(rgba1.getRGB());
				Color rgba4 = new Color(footprintHLFillColorButton.getBackground().getRed(),
						footprintHLFillColorButton.getBackground().getGreen(),
						footprintHLFillColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				dl.setRgba4(rgba4.getRGB());
				Color rgba5 = new Color(footprintHLLineColorButton.getBackground().getRed(),
						footprintHLLineColorButton.getBackground().getGreen(),
						footprintHLLineColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				dl.setRgba5(rgba5.getRGB());
			}
		}

		DisplayLevel dl = new DisplayLevel(DisplayLevel.GEOMETRY, -1, -1);
		int indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
			Color rgba0 = new Color(geometryWallFillColorButton.getBackground().getRed(),
					geometryWallFillColorButton.getBackground().getGreen(),
					geometryWallFillColorButton.getBackground().getBlue(),
					((Integer)geometryAlphaSpinner.getValue()).intValue());
			dl.setRgba0(rgba0.getRGB());
			Color rgba1 = new Color(geometryWallLineColorButton.getBackground().getRed(),
					geometryWallLineColorButton.getBackground().getGreen(),
					geometryWallLineColorButton.getBackground().getBlue(),
					((Integer)geometryAlphaSpinner.getValue()).intValue());
			dl.setRgba1(rgba1.getRGB());
			Color rgba2 = new Color(geometryRoofFillColorButton.getBackground().getRed(),
					geometryRoofFillColorButton.getBackground().getGreen(),
					geometryRoofFillColorButton.getBackground().getBlue(),
					((Integer)geometryAlphaSpinner.getValue()).intValue());
			dl.setRgba2(rgba2.getRGB());
			Color rgba3 = new Color(geometryRoofLineColorButton.getBackground().getRed(),
					geometryRoofLineColorButton.getBackground().getGreen(),
					geometryRoofLineColorButton.getBackground().getBlue(),
					((Integer)geometryAlphaSpinner.getValue()).intValue());
			dl.setRgba3(rgba3.getRGB());
			Color rgba4 = new Color(geometryHLFillColorButton.getBackground().getRed(),
					geometryHLFillColorButton.getBackground().getGreen(),
					geometryHLFillColorButton.getBackground().getBlue(),
					DisplayLevel.DEFAULT_ALPHA_VALUE);
			dl.setRgba4(rgba4.getRGB());
			Color rgba5 = new Color(geometryHLLineColorButton.getBackground().getRed(),
					geometryHLLineColorButton.getBackground().getGreen(),
					geometryHLLineColorButton.getBackground().getBlue(),
					DisplayLevel.DEFAULT_ALPHA_VALUE);
			dl.setRgba5(rgba5.getRGB());
		}

		dl = new DisplayLevel(DisplayLevel.COLLADA, -1, -1);
		indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
			Color rgba4 = new Color(colladaHLFillColorButton.getBackground().getRed(),
					colladaHLFillColorButton.getBackground().getGreen(),
					colladaHLFillColorButton.getBackground().getBlue(),
					DisplayLevel.DEFAULT_ALPHA_VALUE);
			dl.setRgba4(rgba4.getRGB());
			Color rgba5 = new Color(colladaHLLineColorButton.getBackground().getRed(),
					colladaHLLineColorButton.getBackground().getGreen(),
					colladaHLLineColorButton.getBackground().getBlue(),
					DisplayLevel.DEFAULT_ALPHA_VALUE);
			dl.setRgba5(rgba5.getRGB());
		}

		kmlExporter.setFootprintHighlighting(footprintHighlightingCheckbox.isSelected());

		kmlExporter.setGeometryHighlighting(geometryHighlightingCheckbox.isSelected());
		try {
			kmlExporter.setGeometryHighlightingDistance(Double.parseDouble(geometryHLSurfaceDistanceText.getText().trim()));
			if (kmlExporter.getGeometryHighlightingDistance() <= 0 || kmlExporter.getGeometryHighlightingDistance() >10) {
				kmlExporter.setGeometryHighlightingDistance(1.0);
			}
		}
		catch (NumberFormatException nfe) {}

		kmlExporter.setIgnoreSurfaceOrientation(ignoreSurfaceOrientationCheckbox.isSelected());
		kmlExporter.setGenerateTextureAtlases(textureAtlasCheckbox.isSelected());
		kmlExporter.setTextureAtlasPots(textureAtlasPotsCheckbox.isSelected());
		kmlExporter.setPackingAlgorithm(packingAlgorithms.get(packingAlgorithmsComboBox.getSelectedItem()).intValue()); 

		kmlExporter.setScaleImages(scaleTexImagesCheckbox.isSelected());
		try {
			kmlExporter.setImageScaleFactor(Double.parseDouble(scaleFactorText.getText().trim()));
			if (kmlExporter.getImageScaleFactor() <= 0 || kmlExporter.getImageScaleFactor() > 1) {
				kmlExporter.setImageScaleFactor(1);
			}
		}
		catch (NumberFormatException nfe) {}

		kmlExporter.setGroupBuildings(groupBuildingsRButton.isSelected());
		try {
			kmlExporter.setGroupSize(Integer.parseInt(groupSizeText.getText().trim()));
			if (kmlExporter.getGroupSize() < 2) {
				kmlExporter.setGroupSize(1);
			}
		}
		catch (NumberFormatException nfe) {}

		kmlExporter.setColladaHighlighting(colladaHighlightingRButton.isSelected());
		try {
			kmlExporter.setColladaHighlightingDistance(Double.parseDouble(colladaHLSurfaceDistanceText.getText().trim()));
			if (kmlExporter.getColladaHighlightingDistance() <= 0 || kmlExporter.getColladaHighlightingDistance() >10) {
				kmlExporter.setColladaHighlightingDistance(1.0);
			}
		}
		catch (NumberFormatException nfe) {}

	}

	@Override
	public void resetSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		for (int level = DisplayLevel.FOOTPRINT; level <= DisplayLevel.EXTRUDED; level++) {
			DisplayLevel dl = new DisplayLevel(level, -1, -1);
			int indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
			if (indexOfDl != -1) {
				dl = kmlExporter.getDisplayLevels().get(indexOfDl);
				dl.setRgba0(DisplayLevel.DEFAULT_FILL_COLOR);
				dl.setRgba1(DisplayLevel.DEFAULT_LINE_COLOR);
				dl.setRgba4(DisplayLevel.DEFAULT_FILL_HIGHLIGHTED_COLOR);
				dl.setRgba5(DisplayLevel.DEFAULT_LINE_HIGHLIGHTED_COLOR);
			}
		}

		DisplayLevel dl = new DisplayLevel(DisplayLevel.GEOMETRY, -1, -1);
		int indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
			dl.setRgba0(DisplayLevel.DEFAULT_WALL_FILL_COLOR);
			dl.setRgba1(DisplayLevel.DEFAULT_WALL_LINE_COLOR);
			dl.setRgba2(DisplayLevel.DEFAULT_ROOF_FILL_COLOR);
			dl.setRgba3(DisplayLevel.DEFAULT_ROOF_LINE_COLOR);
			dl.setRgba4(DisplayLevel.DEFAULT_FILL_HIGHLIGHTED_COLOR);
			dl.setRgba5(DisplayLevel.DEFAULT_LINE_HIGHLIGHTED_COLOR);
		}

		dl = new DisplayLevel(DisplayLevel.COLLADA, -1, -1);
		indexOfDl = kmlExporter.getDisplayLevels().indexOf(dl); 
		if (indexOfDl != -1) {
			dl = kmlExporter.getDisplayLevels().get(indexOfDl);
			dl.setRgba4(DisplayLevel.DEFAULT_FILL_HIGHLIGHTED_COLOR);
			dl.setRgba5(DisplayLevel.DEFAULT_LINE_HIGHLIGHTED_COLOR);
		}

		kmlExporter.setFootprintHighlighting(false);

		kmlExporter.setGeometryHighlighting(false);
		kmlExporter.setGeometryHighlightingDistance(0.75);

		kmlExporter.setIgnoreSurfaceOrientation(false);
		kmlExporter.setGenerateTextureAtlases(true);
		kmlExporter.setTextureAtlasPots(true);
		kmlExporter.setPackingAlgorithm(TextureAtlasGenerator.TPIM); 

		kmlExporter.setScaleImages(false);
		kmlExporter.setImageScaleFactor(1);

		kmlExporter.setGroupBuildings(false);
		kmlExporter.setGroupSize(1);

		kmlExporter.setColladaHighlighting(true);
		kmlExporter.setColladaHighlightingDistance(0.75);

		loadSettings(); // update GUI
	}

	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.kmlExport.rendering");
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
		groupSizeText.setEnabled(groupBuildingsRButton.isSelected());

		colladaHLFillColorLabel.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLFillColorButton.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLLineColorLabel.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLLineColorButton.setEnabled(colladaHighlightingRButton.isSelected());

		colladaHLSurfaceDistanceLabel.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLSurfaceDistanceText.setEnabled(colladaHighlightingRButton.isSelected());
	}

}
