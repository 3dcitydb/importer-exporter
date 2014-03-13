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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
// import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
// import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
// import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
// import javax.swing.filechooser.FileNameExtensionFilter;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
// import de.tub.citydb.config.project.general.Path;
// import de.tub.citydb.config.project.general.PathMode;
import de.tub.citydb.config.project.kmlExporter.AltitudeMode;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.config.project.kmlExporter.PointAndCurve;
//todo...
//import de.tub.citydb.config.project.kmlExporter.PointAndCurve;
// import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class PointAndCurveRenderingPanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
    
	private JPanel pointPanel;
	private JLabel pointAltitudeModeLabel = new JLabel();
	private JComboBox pointAltitudeModeComboBox = new JComboBox();
	private JLabel pointThicknessLabel = new JLabel();
	private JSpinner pointThicknessSpinner;
	private JLabel pointNormalColorLabel = new JLabel();
	private JButton pointNormalColorButton = new JButton(" ");
	private JCheckBox pointHighlightingCheckbox = new JCheckBox();
	private JLabel pointHighlightingThicknessLabel = new JLabel();
	private JSpinner pointHighlightingThicknessSpinner;
	private JLabel pointHighlightingColorLabel = new JLabel();
	private JButton pointHighlightingColorButton = new JButton(" ");

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
		return Internal.I18N.getString("pref.tree.kmlExport.gcoPointAndCurveRendering");
	}

	@Override
	public boolean isModified() {

		PointAndCurve pacSettings = config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve();

		if (!pacSettings.getPointAltitudeMode().equals(pointAltitudeModeComboBox.getSelectedItem())) return true;
		if (pacSettings.getPointThickness() != ((Double)pointThicknessSpinner.getValue()).doubleValue()) return true;
		if (pacSettings.getPointNormalColor() != (new Color(pointNormalColorButton.getBackground().getRed(),
															pointNormalColorButton.getBackground().getGreen(),
															pointNormalColorButton.getBackground().getBlue(),
				  								   			DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()) return true; //
		if (pacSettings.isPointHighlightingEnabled() != pointHighlightingCheckbox.isSelected()) return true;
		if (pacSettings.getPointHighlightedThickness() != ((Double)pointHighlightingThicknessSpinner.getValue()).doubleValue()) return true;
		if (pacSettings.getPointHighlightedColor() != (new Color(pointHighlightingColorButton.getBackground().getRed(),
																 pointHighlightingColorButton.getBackground().getGreen(),
																 pointHighlightingColorButton.getBackground().getBlue(),
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

		pointPanel.add(pointAltitudeModeLabel, GuiUtil.setConstraints(0,0,0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,0));
		pointPanel.add(pointAltitudeModeComboBox, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,0,BORDER_THICKNESS,BORDER_THICKNESS));

		pointPanel.add(pointThicknessLabel, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
		SpinnerModel pointThicknessModel = new SpinnerNumberModel(1.0,0.1,9.9,0.1);
		pointThicknessSpinner = new JSpinner(pointThicknessModel);
        GridBagConstraints pts = GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,BORDER_THICKNESS);
        pts.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointThicknessSpinner, pts);

        pointPanel.add(pointNormalColorLabel, GuiUtil.setConstraints(0,2,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
        pointNormalColorButton.setPreferredSize(pointThicknessSpinner.getPreferredSize());
        pointNormalColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_COLOR, true));
        pointNormalColorButton.setContentAreaFilled(false);
        pointNormalColorButton.setOpaque(true);
        GridBagConstraints pcb = GuiUtil.setConstraints(1,2,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        pcb.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointNormalColorButton, pcb);
		
        pointHighlightingCheckbox.setIconTextGap(10);
		GridBagConstraints phlc = GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,0,BORDER_THICKNESS,0);
		phlc.gridwidth = 2;
		pointPanel.add(pointHighlightingCheckbox, phlc);

		pointPanel.add(pointHighlightingThicknessLabel, GuiUtil.setConstraints(0,4,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		SpinnerModel pointHighlightingThicknessModel = new SpinnerNumberModel(2.0,0.1,9.9,0.1);
		pointHighlightingThicknessSpinner = new JSpinner(pointHighlightingThicknessModel);
        GridBagConstraints phts = GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,BORDER_THICKNESS);
        phts.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointHighlightingThicknessSpinner, phts);

        pointPanel.add(pointHighlightingColorLabel, GuiUtil.setConstraints(0,5,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
        pointHighlightingColorButton.setPreferredSize(pointThicknessSpinner.getPreferredSize());
        pointHighlightingColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
        pointHighlightingColorButton.setContentAreaFilled(false);
        pointHighlightingColorButton.setOpaque(true);
        GridBagConstraints phlcb = GuiUtil.setConstraints(1,5,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        phlcb.anchor = GridBagConstraints.WEST;
        pointPanel.add(pointHighlightingColorButton, phlcb);
		
		
		curvePanel = new JPanel();
		add(curvePanel, GuiUtil.setConstraints(0,1,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		curvePanel.setLayout(new GridBagLayout());
		curvePanel.setBorder(BorderFactory.createTitledBorder(""));

		curvePanel.add(curveAltitudeModeLabel, GuiUtil.setConstraints(0,0,0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,0));
		curvePanel.add(curveAltitudeModeComboBox, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,0,BORDER_THICKNESS,BORDER_THICKNESS));

		curvePanel.add(curveThicknessLabel, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
		SpinnerModel curveThicknessModel = new SpinnerNumberModel(1.0,0.1,9.9,0.1);
		curveThicknessSpinner = new JSpinner(curveThicknessModel);
        GridBagConstraints cts = GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,BORDER_THICKNESS);
        cts.anchor = GridBagConstraints.WEST;
        curvePanel.add(curveThicknessSpinner, cts);

		curvePanel.add(curveNormalColorLabel, GuiUtil.setConstraints(0,2,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
		curveNormalColorButton.setPreferredSize(curveThicknessSpinner.getPreferredSize());
		curveNormalColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_COLOR, true));
		curveNormalColorButton.setContentAreaFilled(false);
		curveNormalColorButton.setOpaque(true);
        GridBagConstraints ccb = GuiUtil.setConstraints(1,2,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        ccb.anchor = GridBagConstraints.WEST;
		curvePanel.add(curveNormalColorButton, ccb);
		
		curveHighlightingCheckbox.setIconTextGap(10);
		GridBagConstraints chlc = GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,0,BORDER_THICKNESS,0);
		chlc.gridwidth = 2;
		curvePanel.add(curveHighlightingCheckbox, chlc);

		curvePanel.add(curveHighlightingThicknessLabel, GuiUtil.setConstraints(0,4,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		SpinnerModel curveHighlightingThicknessModel = new SpinnerNumberModel(2.0,0.1,9.9,0.1);
		curveHighlightingThicknessSpinner = new JSpinner(curveHighlightingThicknessModel);
        GridBagConstraints chts = GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,BORDER_THICKNESS);
        chts.anchor = GridBagConstraints.WEST;
        curvePanel.add(curveHighlightingThicknessSpinner, chts);

        curvePanel.add(curveHighlightingColorLabel, GuiUtil.setConstraints(0,5,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		curveHighlightingColorButton.setPreferredSize(curveThicknessSpinner.getPreferredSize());
		curveHighlightingColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
		curveHighlightingColorButton.setContentAreaFilled(false);
		curveHighlightingColorButton.setOpaque(true);
        GridBagConstraints chlcb = GuiUtil.setConstraints(1,5,0.25,1.0,GridBagConstraints.NONE,0,0,BORDER_THICKNESS,0);
        chlcb.anchor = GridBagConstraints.WEST;
		curvePanel.add(curveHighlightingColorButton, chlcb);
		

		pointNormalColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color pointNormalColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.choosePointColor"),
													 pointNormalColorButton.getBackground());
				if (pointNormalColor != null)
					pointNormalColorButton.setBackground(pointNormalColor);
			}
		});

		pointHighlightingCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		pointHighlightingColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color pointHighlightingColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.choosePointHighlightingColor"),
														   pointHighlightingColorButton.getBackground());
				if (pointHighlightingColor != null)
					pointHighlightingColorButton.setBackground(pointHighlightingColor);
			}
		});

		curveNormalColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color curveNormalColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseCurveColor"),
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
				Color curveHighlightingColor = chooseColor(Internal.I18N.getString("pref.kmlexport.label.chooseCurveHighlightingColor"),
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

		((TitledBorder)pointPanel.getBorder()).setTitle(Internal.I18N.getString("pref.kmlexport.border.point"));	

		pointAltitudeModeLabel.setText(Internal.I18N.getString("pref.kmlexport.label.curveAltitudeMode"));
		pointAltitudeModeComboBox.removeAllItems();
        for (AltitudeMode c: AltitudeMode.values()) {
        	pointAltitudeModeComboBox.addItem(c);
        }

        pointAltitudeModeComboBox.setSelectedItem(config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getCurveAltitudeMode());
        pointThicknessLabel.setText(Internal.I18N.getString("pref.kmlexport.label.curveThickness"));
        pointNormalColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.curveColor"));
		pointHighlightingCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.highlighting"));
		pointHighlightingThicknessLabel.setText(Internal.I18N.getString("pref.kmlexport.label.curveHighlightingThickness"));
		pointHighlightingColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.curveHighlightingColor"));

		((TitledBorder)curvePanel.getBorder()).setTitle(Internal.I18N.getString("pref.kmlexport.border.curve"));	
    	curveAltitudeModeLabel.setText(Internal.I18N.getString("pref.kmlexport.label.curveAltitudeMode"));
		curveAltitudeModeComboBox.removeAllItems();
        for (AltitudeMode c: AltitudeMode.values()) {
        	curveAltitudeModeComboBox.addItem(c);
        }

        curveAltitudeModeComboBox.setSelectedItem(config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getCurveAltitudeMode());
    	curveThicknessLabel.setText(Internal.I18N.getString("pref.kmlexport.label.curveThickness"));
    	curveNormalColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.curveColor"));
		curveHighlightingCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.highlighting"));
    	curveHighlightingThicknessLabel.setText(Internal.I18N.getString("pref.kmlexport.label.curveHighlightingThickness"));
    	curveHighlightingColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.curveHighlightingColor"));
	}

	@Override
	public void loadSettings() {

		PointAndCurve pacSettings = config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve();

		pointAltitudeModeComboBox.setSelectedItem(pacSettings.getPointAltitudeMode());
		pointThicknessSpinner.setValue(pacSettings.getPointThickness());
		pointNormalColorButton.setBackground(new Color(pacSettings.getPointNormalColor()));
		pointHighlightingCheckbox.setSelected(pacSettings.isPointHighlightingEnabled());
		pointHighlightingThicknessSpinner.setValue(pacSettings.getPointHighlightedThickness());
		pointHighlightingColorButton.setBackground(new Color(pacSettings.getPointHighlightedColor()));

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

		pacSettings.setPointAltitudeMode((AltitudeMode)pointAltitudeModeComboBox.getSelectedItem());
		pacSettings.setPointThickness(((Double)pointThicknessSpinner.getValue()).doubleValue());
		pacSettings.setPointNormalColor((new Color(pointNormalColorButton.getBackground().getRed(),
												   pointNormalColorButton.getBackground().getGreen(),
												   pointNormalColorButton.getBackground().getBlue(),
				  								   DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()); //
		pacSettings.setPointHighlightingEnabled(pointHighlightingCheckbox.isSelected());
		pacSettings.setPointHighlightedThickness(((Double)pointHighlightingThicknessSpinner.getValue()).doubleValue());
		pacSettings.setPointHighlightedColor((new Color(pointHighlightingColorButton.getBackground().getRed(),
														pointHighlightingColorButton.getBackground().getGreen(),
														pointHighlightingColorButton.getBackground().getBlue(),
														DisplayForm.DEFAULT_ALPHA_VALUE)).getRGB()); //


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

/*
	@Override
	public void resetSettings() {
		config.getProject().getKmlExporter().setGenericCityObjectPointAndCurve(new PointAndCurve());
		loadSettings(); // update GUI
	}
*/

	private void setEnabledComponents() {
		pointHighlightingColorLabel.setEnabled(pointHighlightingCheckbox.isSelected());
		pointHighlightingColorButton.setEnabled(pointHighlightingCheckbox.isSelected());
		pointHighlightingThicknessLabel.setEnabled(pointHighlightingCheckbox.isSelected());
		pointHighlightingThicknessSpinner.setEnabled(pointHighlightingCheckbox.isSelected());

		curveHighlightingColorLabel.setEnabled(curveHighlightingCheckbox.isSelected());
		curveHighlightingColorButton.setEnabled(curveHighlightingCheckbox.isSelected());
		curveHighlightingThicknessLabel.setEnabled(curveHighlightingCheckbox.isSelected());
		curveHighlightingThicknessSpinner.setEnabled(curveHighlightingCheckbox.isSelected());
	}

}
