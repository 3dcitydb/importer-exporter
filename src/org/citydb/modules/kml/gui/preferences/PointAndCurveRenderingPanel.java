/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.kml.gui.preferences;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
// import javax.swing.filechooser.FileNameExtensionFilter;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.kmlExporter.AltitudeMode;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.PointAndCurve;
import org.citydb.config.project.kmlExporter.PointDisplayMode;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

// import org.citydb.config.project.general.Path;
// import org.citydb.config.project.general.PathMode;

@SuppressWarnings("serial")
public class PointAndCurveRenderingPanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
    
	private JPanel pointPanel;
	private JLabel pointAltitudeModeLabel = new JLabel();
	private JComboBox pointAltitudeModeComboBox = new JComboBox();
	private JLabel pointIconColorLabel = new JLabel();
	private JButton pointIconColorButton = new JButton(" ");
	private JLabel pointIconScaleLabel = new JLabel();
	private JSpinner pointIconScaleSpinner;	
	
	private JLabel pointCrossLineThicknessLabel = new JLabel();
	private JSpinner pointCrossLineThicknessSpinner;
	private JLabel pointCrossLineNormalColorLabel = new JLabel();
	private JButton pointCrossLineNormalColorButton = new JButton(" ");
	private JCheckBox pointCrossLineHighlightingCheckbox = new JCheckBox();
	private JLabel pointCrossLineHighlightingThicknessLabel = new JLabel();
	private JSpinner pointCrossLineHighlightingThicknessSpinner;
	private JLabel pointCrossLineHighlightingColorLabel = new JLabel();
	private JButton pointCrossLineHighlightingColorButton = new JButton(" ");
	
	
	private JRadioButton iconRButton = new JRadioButton();
	private JRadioButton crossLineRButton = new JRadioButton();
	private JRadioButton cubeRButton = new JRadioButton();

	private JLabel pointCubeLengthOfSideLabel = new JLabel();
	private JSpinner pointCubeLengthOfSideSpinner;
	private JLabel pointCubeFillColorLabel = new JLabel();
	private JButton pointCubeFillColorButton = new JButton(" ");
	private JCheckBox pointCubeHighlightingCheckbox = new JCheckBox();
	private JLabel pointCubeHighlightingColorLabel = new JLabel();
	private JButton pointCubeHighlightingColorButton = new JButton(" ");

	private JPanel curvePanel;
	private JLabel curveAltitudeModeLabel = new JLabel();
	private JComboBox curveAltitudeModeComboBox = new JComboBox();
	private JLabel curveThicknessLabel = new JLabel();
	private JSpinner curveThicknessSpinner;
	private JLabel curveNormalColorLabel = new JLabel();
	private JButton curveNormalColorButton = new JButton(" ");
	private JCheckBox curveHighlightingCheckbox = new JCheckBox();
	private JLabel curveHighlightingThicknessLabel = new JLabel();
	private JSpinner curveHighlightingThicknessSpinner;
	private JLabel curveHighlightingColorLabel = new JLabel();
	private JButton curveHighlightingColorButton = new JButton(" ");


	public PointAndCurveRenderingPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.kmlExport.gcoPointAndCurveRendering");
	}

	@Override
	public boolean isModified() {

		PointAndCurve pacSettings = config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve();

		switch (pacSettings.getPointDisplayMode()) {
		case CROSS_LINE:
			if (!crossLineRButton.isSelected())
				return true;
			break;
		case ICON:
			if (!iconRButton.isSelected())
				return true;
			break;
		case CUBE:
			if (!cubeRButton.isSelected())
				return true;
			break;
		}

		if (!pacSettings.getPointAltitudeMode().equals(pointAltitudeModeComboBox.getSelectedItem())) return true;
		if (pacSettings.getPointThickness() != ((Double)pointCrossLineThicknessSpinner.getValue()).doubleValue()) return true;
		if (pacSettings.getPointNormalColor() != (new Color(pointCrossLineNormalColorButton.getBackground().getRed(),
															pointCrossLineNormalColorButton.getBackground().getGreen(),
															pointCrossLineNormalColorButton.getBackground().getBlue(),
				  								   			DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()) return true; //
		if (pacSettings.isPointHighlightingEnabled() != pointCrossLineHighlightingCheckbox.isSelected()) return true;
		if (pacSettings.getPointHighlightedThickness() != ((Double)pointCrossLineHighlightingThicknessSpinner.getValue()).doubleValue()) return true;
		if (pacSettings.getPointHighlightedColor() != (new Color(pointCrossLineHighlightingColorButton.getBackground().getRed(),
																 pointCrossLineHighlightingColorButton.getBackground().getGreen(),
																 pointCrossLineHighlightingColorButton.getBackground().getBlue(),
																 DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()) return true;
		
		if (pacSettings.getPointIconColor() != (new Color(pointIconColorButton.getBackground().getRed(),
															pointIconColorButton.getBackground().getGreen(),
															pointIconColorButton.getBackground().getBlue(),
																 DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()) return true;
		if (pacSettings.getPointIconScale() != ((Double)pointIconScaleSpinner.getValue()).doubleValue()) return true;
		
		if (pacSettings.getPointCubeLengthOfSide() != ((Double)pointCubeLengthOfSideSpinner.getValue()).doubleValue()) return true;
		if (pacSettings.getPointCubeFillColor() != (new Color(pointCubeFillColorButton.getBackground().getRed(),
															pointCubeFillColorButton.getBackground().getGreen(),
															pointCubeFillColorButton.getBackground().getBlue(),
																 DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()) return true;	
		if (pacSettings.isPointCubeHighlightingEnabled() != pointCubeHighlightingCheckbox.isSelected()) return true;
		if (pacSettings.getPointCubeHighlightedColor() != (new Color(pointCubeHighlightingColorButton.getBackground().getRed(),
															pointCubeHighlightingColorButton.getBackground().getGreen(),
															pointCubeHighlightingColorButton.getBackground().getBlue(),
																 DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()) return true;			

		if (!pacSettings.getCurveAltitudeMode().equals(curveAltitudeModeComboBox.getSelectedItem())) return true;
		if (pacSettings.getCurveThickness() != ((Double)curveThicknessSpinner.getValue()).doubleValue()) return true;
		if (pacSettings.getCurveNormalColor() != (new Color(curveNormalColorButton.getBackground().getRed(),
				  								   			curveNormalColorButton.getBackground().getGreen(),
				  								   			curveNormalColorButton.getBackground().getBlue(),
				  								   			DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()) return true; //
		if (pacSettings.isCurveHighlightingEnabled() != curveHighlightingCheckbox.isSelected()) return true;
		if (pacSettings.getCurveHighlightedThickness() != ((Double)curveHighlightingThicknessSpinner.getValue()).doubleValue()) return true;
		if (pacSettings.getCurveHighlightedColor() != (new Color(curveHighlightingColorButton.getBackground().getRed(),
																 curveHighlightingColorButton.getBackground().getGreen(),
																 curveHighlightingColorButton.getBackground().getBlue(),
																 DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()) return true;
		return false;
	}

	private void initGui() {

		setLayout(new GridBagLayout());

		pointPanel = new JPanel();
		add(pointPanel, GuiUtil.setConstraints(0,0,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		pointPanel.setLayout(new GridBagLayout());
		pointPanel.setBorder(BorderFactory.createTitledBorder(""));

		pointPanel.add(pointAltitudeModeLabel, GuiUtil.setConstraints(0,0,3,1,0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,0));
		pointPanel.add(pointAltitudeModeComboBox, GuiUtil.setConstraints(0,1,3,1,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		
		ButtonGroup pointRadioGroup = new ButtonGroup();		
		pointRadioGroup.add(crossLineRButton);
		pointRadioGroup.add(iconRButton);
		pointRadioGroup.add(cubeRButton);
		iconRButton.setIconTextGap(10);
		crossLineRButton.setIconTextGap(10);
		cubeRButton.setIconTextGap(10);
		
		GridBagConstraints gc = GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);		
		gc.anchor = GridBagConstraints.WEST;
		pointPanel.add(crossLineRButton, gc);		

		pointPanel.add(pointCrossLineThicknessLabel, GuiUtil.setConstraints(1,3,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
		SpinnerModel pointThicknessModel = new SpinnerNumberModel(1.0,0.1,9.9,0.1);
		pointCrossLineThicknessSpinner = new JSpinner(pointThicknessModel);
        GridBagConstraints pts = GuiUtil.setConstraints(2,3,1.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,BORDER_THICKNESS);
        pts.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointCrossLineThicknessSpinner, pts);

        pointPanel.add(pointCrossLineNormalColorLabel, GuiUtil.setConstraints(1,4,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
        pointCrossLineNormalColorButton.setPreferredSize(pointCrossLineThicknessSpinner.getPreferredSize());
        pointCrossLineNormalColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_COLOR, true));
        pointCrossLineNormalColorButton.setContentAreaFilled(false);
        pointCrossLineNormalColorButton.setOpaque(true);
        GridBagConstraints pcb = GuiUtil.setConstraints(2,4,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        pcb.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointCrossLineNormalColorButton, pcb);
		
        pointCrossLineHighlightingCheckbox.setIconTextGap(10);
		GridBagConstraints phlc = GuiUtil.setConstraints(1,5,0.0,1.0,GridBagConstraints.BOTH,0,0,BORDER_THICKNESS,0);
		phlc.gridwidth = 3;
		pointPanel.add(pointCrossLineHighlightingCheckbox, phlc);

		pointPanel.add(pointCrossLineHighlightingThicknessLabel, GuiUtil.setConstraints(1,6,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		SpinnerModel pointHighlightingThicknessModel = new SpinnerNumberModel(2.0,0.1,9.9,0.1);
		pointCrossLineHighlightingThicknessSpinner = new JSpinner(pointHighlightingThicknessModel);
        GridBagConstraints phts = GuiUtil.setConstraints(2,6,1.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,BORDER_THICKNESS);
        phts.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointCrossLineHighlightingThicknessSpinner, phts);

        pointPanel.add(pointCrossLineHighlightingColorLabel, GuiUtil.setConstraints(1,7,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
        pointCrossLineHighlightingColorButton.setPreferredSize(pointCrossLineThicknessSpinner.getPreferredSize());
        pointCrossLineHighlightingColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
        pointCrossLineHighlightingColorButton.setContentAreaFilled(false);
        pointCrossLineHighlightingColorButton.setOpaque(true);
        GridBagConstraints phlcb = GuiUtil.setConstraints(2,7,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        phlcb.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointCrossLineHighlightingColorButton, phlcb);       
		pointPanel.add(iconRButton, GuiUtil.setConstraints(0,8,0.0,1.0,GridBagConstraints.BOTH,0,0,BORDER_THICKNESS,0));
				
		pointPanel.add(pointIconColorLabel, GuiUtil.setConstraints(1,9,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
        pointIconColorButton.setPreferredSize(pointCrossLineThicknessSpinner.getPreferredSize());
        pointIconColorButton.setContentAreaFilled(false);
        pointIconColorButton.setOpaque(true);
        GridBagConstraints picb = GuiUtil.setConstraints(2,9,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        picb.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointIconColorButton, picb);
        
        pointPanel.add(pointIconScaleLabel, GuiUtil.setConstraints(1,10,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		SpinnerModel pointIconScaleModel = new SpinnerNumberModel(1.0,0.1,9.9,0.1);
		pointIconScaleSpinner = new JSpinner(pointIconScaleModel);
        GridBagConstraints piss = GuiUtil.setConstraints(2,10,1.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,BORDER_THICKNESS);
        piss.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointIconScaleSpinner, piss);
        
        pointPanel.add(cubeRButton, GuiUtil.setConstraints(0,11,0.0,1.0,GridBagConstraints.BOTH,0,0,BORDER_THICKNESS,0));
        
		pointPanel.add(pointCubeLengthOfSideLabel, GuiUtil.setConstraints(1,12,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
		SpinnerModel pointCubeLengthOfSideModel = new SpinnerNumberModel(1.0,0.1,9.9,0.1);
		pointCubeLengthOfSideSpinner = new JSpinner(pointCubeLengthOfSideModel);
        GridBagConstraints pcloss = GuiUtil.setConstraints(2,12,1.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,BORDER_THICKNESS);
        pcloss.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointCubeLengthOfSideSpinner, pcloss);
        
        pointPanel.add(pointCubeFillColorLabel, GuiUtil.setConstraints(1,13,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
        pointCubeFillColorButton.setPreferredSize(pointCrossLineThicknessSpinner.getPreferredSize());
        pointCubeFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_FILL_COLOR, true));
        pointCubeFillColorButton.setContentAreaFilled(false);
        pointCubeFillColorButton.setOpaque(true);
        GridBagConstraints pcfcb = GuiUtil.setConstraints(2,13,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        pcfcb.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointCubeFillColorButton, pcfcb);
        
        pointCubeHighlightingCheckbox.setIconTextGap(10);
		GridBagConstraints pchc = GuiUtil.setConstraints(1,14,0.0,1.0,GridBagConstraints.BOTH,0,0,BORDER_THICKNESS,0);
		pchc.gridwidth = 3;
		pointPanel.add(pointCubeHighlightingCheckbox, pchc);
		
        pointPanel.add(pointCubeHighlightingColorLabel, GuiUtil.setConstraints(1,15,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
        pointCubeHighlightingColorButton.setPreferredSize(pointCrossLineThicknessSpinner.getPreferredSize());
        pointCubeHighlightingColorButton.setBackground(new Color(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR, true));
        pointCubeHighlightingColorButton.setContentAreaFilled(false);
        pointCubeHighlightingColorButton.setOpaque(true);
        GridBagConstraints pchcb = GuiUtil.setConstraints(2,15,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        pchcb.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointCubeHighlightingColorButton, pchcb);

		
		// Curve Panel...
		curvePanel = new JPanel();
		add(curvePanel, GuiUtil.setConstraints(0,1,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		curvePanel.setLayout(new GridBagLayout());
		curvePanel.setBorder(BorderFactory.createTitledBorder(""));

		curvePanel.add(curveAltitudeModeLabel, GuiUtil.setConstraints(0,0,0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,0));
		curvePanel.add(curveAltitudeModeComboBox, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));

		JPanel curveDisplayFormPanel = new JPanel();
		curveDisplayFormPanel.setLayout(new GridBagLayout());
		curvePanel.add(curveDisplayFormPanel, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,BORDER_THICKNESS));
		
		curveDisplayFormPanel.add(curveThicknessLabel, GuiUtil.setConstraints(0,2,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
		SpinnerModel curveThicknessModel = new SpinnerNumberModel(1.0,0.1,9.9,0.1);
		curveThicknessSpinner = new JSpinner(curveThicknessModel);
        GridBagConstraints cts = GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,BORDER_THICKNESS);
        cts.anchor = GridBagConstraints.WEST;
        curveDisplayFormPanel.add(curveThicknessSpinner, cts);

        curveDisplayFormPanel.add(curveNormalColorLabel, GuiUtil.setConstraints(0,3,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
		curveNormalColorButton.setPreferredSize(curveThicknessSpinner.getPreferredSize());
		curveNormalColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_COLOR, true));
		curveNormalColorButton.setContentAreaFilled(false);
		curveNormalColorButton.setOpaque(true);
        GridBagConstraints ccb = GuiUtil.setConstraints(1,3,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        ccb.anchor = GridBagConstraints.WEST;
        curveDisplayFormPanel.add(curveNormalColorButton, ccb);
		
		curveHighlightingCheckbox.setIconTextGap(10);
		GridBagConstraints chlc = GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,0,0,BORDER_THICKNESS,0);
		chlc.gridwidth = 2;
		curveDisplayFormPanel.add(curveHighlightingCheckbox, chlc);

		curveDisplayFormPanel.add(curveHighlightingThicknessLabel, GuiUtil.setConstraints(0,5,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		SpinnerModel curveHighlightingThicknessModel = new SpinnerNumberModel(2.0,0.1,9.9,0.1);
		curveHighlightingThicknessSpinner = new JSpinner(curveHighlightingThicknessModel);
        GridBagConstraints chts = GuiUtil.setConstraints(1,5,1.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,BORDER_THICKNESS);
        chts.anchor = GridBagConstraints.WEST;
        curveDisplayFormPanel.add(curveHighlightingThicknessSpinner, chts);

        curveDisplayFormPanel.add(curveHighlightingColorLabel, GuiUtil.setConstraints(0,6,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		curveHighlightingColorButton.setPreferredSize(curveThicknessSpinner.getPreferredSize());
		curveHighlightingColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
		curveHighlightingColorButton.setContentAreaFilled(false);
		curveHighlightingColorButton.setOpaque(true);
        GridBagConstraints chlcb = GuiUtil.setConstraints(1,6,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        chlcb.anchor = GridBagConstraints.WEST;
        curveDisplayFormPanel.add(curveHighlightingColorButton, chlcb);

        
        iconRButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});
        
        crossLineRButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});
        cubeRButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});       

		pointCrossLineNormalColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color pointNormalColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.choosePointColor"),
													 pointCrossLineNormalColorButton.getBackground());
				if (pointNormalColor != null)
					pointCrossLineNormalColorButton.setBackground(pointNormalColor);
			}
		});

		pointCrossLineHighlightingCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		pointCrossLineHighlightingColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color pointHighlightingColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.choosePointHighlightingColor"),
														   pointCrossLineHighlightingColorButton.getBackground());
				if (pointHighlightingColor != null)
					pointCrossLineHighlightingColorButton.setBackground(pointHighlightingColor);
			}
		});
		
		pointIconColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color pointIconColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.choosePointIconColor"),
						pointIconColorButton.getBackground());
				if (pointIconColor != null)
					pointIconColorButton.setBackground(pointIconColor);
			}
		});
		
		pointCubeHighlightingCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});
		
		pointCubeFillColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color pointCubeFillColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"),
						pointCubeFillColorButton.getBackground());
				if (pointCubeFillColor != null)
					pointCubeFillColorButton.setBackground(pointCubeFillColor);
			}
		});	
		
		pointCubeHighlightingColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color pointCubeHighlightingColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedFillColor"),
						pointCubeHighlightingColorButton.getBackground());
				if (pointCubeHighlightingColor != null)
					pointCubeHighlightingColorButton.setBackground(pointCubeHighlightingColor);
			}
		});	
		
		curveNormalColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color curveNormalColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseCurveColor"),
													 curveNormalColorButton.getBackground());
				if (curveNormalColor != null)
					curveNormalColorButton.setBackground(curveNormalColor);
			}
		});

		curveHighlightingCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		curveHighlightingColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color curveHighlightingColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseCurveHighlightingColor"),
														   curveHighlightingColorButton.getBackground());
				if (curveHighlightingColor != null)
					curveHighlightingColorButton.setBackground(curveHighlightingColor);
			}
		});
	}
	
	private Color chooseColor(String title, Color initialColor){
		return JColorChooser.showDialog(getTopLevelAncestor(), title, initialColor);
	}


	@Override
	public void doTranslation() {

		((TitledBorder)pointPanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.border.point"));	

		pointAltitudeModeLabel.setText(Language.I18N.getString("pref.kmlexport.label.curveAltitudeMode"));
		pointAltitudeModeComboBox.removeAllItems();
        for (AltitudeMode c: AltitudeMode.values()) {
        	pointAltitudeModeComboBox.addItem(c);
        }
        
        iconRButton.setText(Language.I18N.getString("pref.kmlexport.pointdisplay.mode.label.icon"));
        crossLineRButton.setText(Language.I18N.getString("pref.kmlexport.pointdisplay.mode.label.cross"));
        cubeRButton.setText("Cube");
        pointCubeLengthOfSideLabel.setText("Length of Side");
        pointCubeFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.fillColor"));
        pointCubeHighlightingCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.highlighting"));
        pointCubeHighlightingColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
        
        pointIconColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.pointIconColor"));
        pointIconScaleLabel.setText(Language.I18N.getString("pref.kmlexport.label.pointIconScale"));
        pointAltitudeModeComboBox.setSelectedItem(config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getPointAltitudeMode());
       
        pointCrossLineThicknessLabel.setText(Language.I18N.getString("pref.kmlexport.label.curveThickness"));
        pointCrossLineNormalColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.curveColor"));
		pointCrossLineHighlightingCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.highlighting"));
		pointCrossLineHighlightingThicknessLabel.setText(Language.I18N.getString("pref.kmlexport.label.curveHighlightingThickness"));
		pointCrossLineHighlightingColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.curveHighlightingColor"));

		((TitledBorder)curvePanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.border.curve"));	
    	curveAltitudeModeLabel.setText(Language.I18N.getString("pref.kmlexport.label.curveAltitudeMode"));
		curveAltitudeModeComboBox.removeAllItems();
        for (AltitudeMode c: AltitudeMode.values()) {
        	curveAltitudeModeComboBox.addItem(c);
        }

        curveAltitudeModeComboBox.setSelectedItem(config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getCurveAltitudeMode());
    	curveThicknessLabel.setText(Language.I18N.getString("pref.kmlexport.label.curveThickness"));
    	curveNormalColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.curveColor"));
		curveHighlightingCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.highlighting"));
    	curveHighlightingThicknessLabel.setText(Language.I18N.getString("pref.kmlexport.label.curveHighlightingThickness"));
    	curveHighlightingColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.curveHighlightingColor"));
	}

	@Override
	public void loadSettings() {

		PointAndCurve pacSettings = config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve();
		
		switch (pacSettings.getPointDisplayMode()) {
		case ICON:
			iconRButton.setSelected(true);
			break;
		case CROSS_LINE:
			crossLineRButton.setSelected(true);
			break;
		case CUBE:
			cubeRButton.setSelected(true);
			break;
		}

		pointAltitudeModeComboBox.setSelectedItem(pacSettings.getPointAltitudeMode());
		pointCrossLineThicknessSpinner.setValue(pacSettings.getPointThickness());
		pointCrossLineNormalColorButton.setBackground(new Color(pacSettings.getPointNormalColor()));
		pointCrossLineHighlightingCheckbox.setSelected(pacSettings.isPointHighlightingEnabled());
		pointCrossLineHighlightingThicknessSpinner.setValue(pacSettings.getPointHighlightedThickness());
		pointCrossLineHighlightingColorButton.setBackground(new Color(pacSettings.getPointHighlightedColor()));
		
		pointIconColorButton.setBackground(new Color(pacSettings.getPointIconColor()));
		pointIconScaleSpinner.setValue(pacSettings.getPointIconScale());		
		
		pointCubeLengthOfSideSpinner.setValue(pacSettings.getPointCubeLengthOfSide());
		pointCubeFillColorButton.setBackground(new Color(pacSettings.getPointCubeFillColor()));
		pointCubeHighlightingCheckbox.setSelected(pacSettings.isPointCubeHighlightingEnabled());
		pointCubeHighlightingColorButton.setBackground(new Color(pacSettings.getPointCubeHighlightedColor()));

		curveAltitudeModeComboBox.setSelectedItem(pacSettings.getCurveAltitudeMode());
		curveThicknessSpinner.setValue(pacSettings.getCurveThickness());
		curveNormalColorButton.setBackground(new Color(pacSettings.getCurveNormalColor()));
		curveHighlightingCheckbox.setSelected(pacSettings.isCurveHighlightingEnabled());
		curveHighlightingThicknessSpinner.setValue(pacSettings.getCurveHighlightedThickness());
		curveHighlightingColorButton.setBackground(new Color(pacSettings.getCurveHighlightedColor()));

		setEnabledComponents();
	}

	@Override
	public void setSettings() {

		PointAndCurve pacSettings = config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve();

		if (iconRButton.isSelected()) {
			pacSettings.setPointDisplayMode(PointDisplayMode.ICON);
		}
		else if (crossLineRButton.isSelected()) {
			pacSettings.setPointDisplayMode(PointDisplayMode.CROSS_LINE);
		}
		else if (cubeRButton.isSelected()) {
			pacSettings.setPointDisplayMode(PointDisplayMode.CUBE);
		}
		
		pacSettings.setPointAltitudeMode((AltitudeMode)pointAltitudeModeComboBox.getSelectedItem());
		pacSettings.setPointThickness(((Double)pointCrossLineThicknessSpinner.getValue()).doubleValue());
		pacSettings.setPointNormalColor((new Color(pointCrossLineNormalColorButton.getBackground().getRed(),
												   pointCrossLineNormalColorButton.getBackground().getGreen(),
												   pointCrossLineNormalColorButton.getBackground().getBlue(),
				  								   DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()); //
		pacSettings.setPointHighlightingEnabled(pointCrossLineHighlightingCheckbox.isSelected());
		pacSettings.setPointHighlightedThickness(((Double)pointCrossLineHighlightingThicknessSpinner.getValue()).doubleValue());
		pacSettings.setPointHighlightedColor((new Color(pointCrossLineHighlightingColorButton.getBackground().getRed(),
														pointCrossLineHighlightingColorButton.getBackground().getGreen(),
														pointCrossLineHighlightingColorButton.getBackground().getBlue(),
														DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()); //

		pacSettings.setPointIconColor((new Color(pointIconColorButton.getBackground().getRed(),
														pointIconColorButton.getBackground().getGreen(),
														pointIconColorButton.getBackground().getBlue(),
														DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB());
		pacSettings.setPointIconScale(((Double)pointIconScaleSpinner.getValue()).doubleValue());
		
		pacSettings.setPointCubeLengthOfSide(((Double)pointCubeLengthOfSideSpinner.getValue()).doubleValue());
		pacSettings.setPointCubeFillColor((new Color(pointCubeFillColorButton.getBackground().getRed(),
														pointCubeFillColorButton.getBackground().getGreen(),
														pointCubeFillColorButton.getBackground().getBlue(),
														DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB());
		pacSettings.setPointCubeHighlightingEnabled(pointCubeHighlightingCheckbox.isSelected());
		pacSettings.setPointCubeHighlightedColor((new Color(pointCubeHighlightingColorButton.getBackground().getRed(),
														pointCubeHighlightingColorButton.getBackground().getGreen(),
														pointCubeHighlightingColorButton.getBackground().getBlue(),
														DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB());
						
		
		pacSettings.setCurveAltitudeMode((AltitudeMode)curveAltitudeModeComboBox.getSelectedItem());
		pacSettings.setCurveThickness(((Double)curveThicknessSpinner.getValue()).doubleValue());
		pacSettings.setCurveNormalColor((new Color(curveNormalColorButton.getBackground().getRed(),
				  								   curveNormalColorButton.getBackground().getGreen(),
				  								   curveNormalColorButton.getBackground().getBlue(),
				  								   DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()); //
		pacSettings.setCurveHighlightingEnabled(curveHighlightingCheckbox.isSelected());
		pacSettings.setCurveHighlightedThickness(((Double)curveHighlightingThicknessSpinner.getValue()).doubleValue());
		pacSettings.setCurveHighlightedColor((new Color(curveHighlightingColorButton.getBackground().getRed(),
														curveHighlightingColorButton.getBackground().getGreen(),
														curveHighlightingColorButton.getBackground().getBlue(),
														DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()); //
	}

	private void setEnabledComponents() {
		
		pointIconColorLabel.setEnabled(iconRButton.isSelected());
		pointIconColorButton.setEnabled(iconRButton.isSelected());
		pointIconScaleLabel.setEnabled(iconRButton.isSelected());
		pointIconScaleSpinner.setEnabled(iconRButton.isSelected());
				
		pointCrossLineThicknessLabel.setEnabled(crossLineRButton.isSelected());
		pointCrossLineThicknessSpinner.setEnabled(crossLineRButton.isSelected());
		pointCrossLineNormalColorLabel.setEnabled(crossLineRButton.isSelected());
		pointCrossLineNormalColorButton.setEnabled(crossLineRButton.isSelected());
		pointCrossLineHighlightingCheckbox.setEnabled(crossLineRButton.isSelected());
		pointCrossLineHighlightingColorLabel.setEnabled(pointCrossLineHighlightingCheckbox.isSelected()&&crossLineRButton.isSelected());
		pointCrossLineHighlightingColorButton.setEnabled(pointCrossLineHighlightingCheckbox.isSelected()&&crossLineRButton.isSelected());
		pointCrossLineHighlightingThicknessLabel.setEnabled(pointCrossLineHighlightingCheckbox.isSelected()&&crossLineRButton.isSelected());
		pointCrossLineHighlightingThicknessSpinner.setEnabled(pointCrossLineHighlightingCheckbox.isSelected()&&crossLineRButton.isSelected());
		
		pointCubeLengthOfSideLabel.setEnabled(cubeRButton.isSelected());
		pointCubeLengthOfSideSpinner.setEnabled(cubeRButton.isSelected());
		pointCubeFillColorLabel.setEnabled(cubeRButton.isSelected());
		pointCubeFillColorButton.setEnabled(cubeRButton.isSelected());
		pointCubeHighlightingCheckbox.setEnabled(cubeRButton.isSelected());
		pointCubeHighlightingColorLabel.setEnabled(pointCubeHighlightingCheckbox.isSelected()&&cubeRButton.isSelected());
		pointCubeHighlightingColorButton.setEnabled(pointCubeHighlightingCheckbox.isSelected()&&cubeRButton.isSelected());
		
		curveHighlightingColorLabel.setEnabled(curveHighlightingCheckbox.isSelected());
		curveHighlightingColorButton.setEnabled(curveHighlightingCheckbox.isSelected());
		curveHighlightingThicknessLabel.setEnabled(curveHighlightingCheckbox.isSelected());
		curveHighlightingThicknessSpinner.setEnabled(curveHighlightingCheckbox.isSelected());
	}

}
