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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class CityObjectGroupRenderingPanel extends AbstractPreferencesComponent {

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


	public CityObjectGroupRenderingPanel(Config config) {
		super(config);
		initGui();
	}

	private List<DisplayForm> getConfigDisplayForms() {
		return config.getProject().getKmlExporter().getCityObjectGroupDisplayForms();
	}

	private void setConfigDisplayForms(List<DisplayForm> displayForms) {
		config.getProject().getKmlExporter().setCityObjectGroupDisplayForms(displayForms);
	}

	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.kmlExport.cityObjectGroupRendering");
	}

	@Override
	public boolean isModified() {
		setInternalDisplayFormValues();
		List<DisplayForm> configDfs = getConfigDisplayForms();

		DisplayForm configDf = new DisplayForm(DisplayForm.FOOTPRINT, -1, -1);
		int indexOfConfigDf = configDfs.indexOf(configDf); 
		if (indexOfConfigDf != -1) {
			configDf = configDfs.get(indexOfConfigDf);
		}
		DisplayForm internalDf = new DisplayForm(DisplayForm.FOOTPRINT, -1, -1);
		int indexOfInternalDf = internalDfs.indexOf(internalDf); 
		if (indexOfInternalDf != -1) {
			internalDf = internalDfs.get(indexOfInternalDf);
		}

		return areDisplayFormsContentsDifferent(internalDf, configDf);
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


		add(footprintPanel, GuiUtil.setConstraints(0,1,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
	}

	private Color chooseColor(String title, Color initialColor){
		return JColorChooser.showDialog(getTopLevelAncestor(), title, initialColor);
	}


	@Override
	public void doTranslation() {

		((TitledBorder)footprintPanel.getBorder()).setTitle(Internal.I18N.getString("pref.kmlexport.border.footprint"));	

		footprintAlphaLabel.setText(Internal.I18N.getString("pref.kmlexport.label.alpha"));
		footprintFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.fillColor"));
		footprintLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.lineColor"));
		footprintHighlightingCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.highlighting"));
		footprintHLFillColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
		footprintHLLineColorLabel.setText(Internal.I18N.getString("pref.kmlexport.label.highlightedLineColor"));

	}

	@Override
	public void loadSettings() {
		internalDfs.clear();
		List<DisplayForm> configDfs = getConfigDisplayForms();

		for (int form = DisplayForm.FOOTPRINT; form <= DisplayForm.FOOTPRINT; form++) {
			DisplayForm configDf = new DisplayForm(form, -1, -1);
			int indexOfConfigDf = configDfs.indexOf(configDf); 
			if (indexOfConfigDf != -1) {
				configDf = configDfs.get(indexOfConfigDf);
			}
			DisplayForm internalDf = configDf.clone();
			internalDfs.add(internalDf);
		}

		for (DisplayForm displayForm : internalDfs) {
			switch (displayForm.getForm()) {
			case DisplayForm.FOOTPRINT:
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
			}
		}
		setEnabledHighlighting();
	}


	@Override
	public void setSettings() {
		setInternalDisplayFormValues();
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
	}


	private void setInternalDisplayFormValues() {
		DisplayForm df = new DisplayForm(DisplayForm.FOOTPRINT, -1, -1);
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


	@Override
	public void resetSettings() {
		List<DisplayForm> configDfs = getConfigDisplayForms();

		DisplayForm df = new DisplayForm(DisplayForm.FOOTPRINT, -1, -1);
		int indexOfDf = configDfs.indexOf(df); 
		if (indexOfDf != -1) {
			df = configDfs.get(indexOfDf);
			df.setHighlightingEnabled(false);

			df.setRgba0(DisplayForm.DEFAULT_FILL_COLOR);
			df.setRgba1(DisplayForm.DEFAULT_LINE_COLOR);
			df.setRgba4(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
			df.setRgba5(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
		}
		loadSettings(); // update GUI
	}

	private void setEnabledHighlighting() {
		footprintHLFillColorLabel.setEnabled(footprintHighlightingCheckbox.isSelected());
		footprintHLFillColorButton.setEnabled(footprintHighlightingCheckbox.isSelected());
		footprintHLLineColorLabel.setEnabled(footprintHighlightingCheckbox.isSelected());
		footprintHLLineColorButton.setEnabled(footprintHighlightingCheckbox.isSelected());
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
