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
package de.tub.citydb.modules.kml.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class GeneralPanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;

	private JCheckBox kmzCheckbox = new JCheckBox();
	private JCheckBox showBoundingBoxCheckbox = new JCheckBox();
	private JCheckBox showTileBordersCheckbox = new JCheckBox();
	private JLabel autoTileSideLengthLabel = new JLabel();
	private JTextField autoTileSideLengthText = new JTextField("", 4);
	private JCheckBox writeJSONCheckbox = new JCheckBox();

	public GeneralPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		if (kmzCheckbox.isSelected() != kmlExporter.isExportAsKmz()) return true;
		if (showBoundingBoxCheckbox.isSelected() != kmlExporter.isShowBoundingBox()) return true;
		if (showTileBordersCheckbox.isSelected() != kmlExporter.isShowTileBorders()) return true;

		double autoTileSideLength = 125.0;
		try {
			autoTileSideLength = Double.parseDouble(autoTileSideLengthText.getText().trim());
			if (autoTileSideLength <= 1.0) {
				autoTileSideLength = 125.0;
			}
		}
		catch (NumberFormatException nfe) {return true;}
		if (autoTileSideLength != kmlExporter.getAutoTileSideLength()) return true;

		if (writeJSONCheckbox.isSelected() != kmlExporter.isWriteJSONFile()) return true;

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		JPanel generalPanel = new JPanel();
		add(generalPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		generalPanel.setLayout(new GridBagLayout());
		kmzCheckbox.setIconTextGap(10);
		showBoundingBoxCheckbox.setIconTextGap(10);
		showTileBordersCheckbox.setIconTextGap(10);
		writeJSONCheckbox.setIconTextGap(10);

		generalPanel.add(kmzCheckbox, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		generalPanel.add(showBoundingBoxCheckbox, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		generalPanel.add(showTileBordersCheckbox, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		generalPanel.add(autoTileSideLengthLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS * 2,0,BORDER_THICKNESS));
		generalPanel.add(autoTileSideLengthText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
		generalPanel.add(writeJSONCheckbox, GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));

		PopupMenuDecorator.getInstance().decorate(autoTileSideLengthText);
	}

	@Override
	public void doTranslation() {
		kmzCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.exportAsKmz"));
		showBoundingBoxCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.showBoundingBox"));
		showTileBordersCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.showTileBorders"));
		autoTileSideLengthLabel.setText(Internal.I18N.getString("pref.kmlexport.label.autoTileSideLength"));
		writeJSONCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.writeJSONFile"));
	}

	@Override
	public void loadSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		kmzCheckbox.setSelected(kmlExporter.isExportAsKmz());
		showBoundingBoxCheckbox.setSelected(kmlExporter.isShowBoundingBox());
		showTileBordersCheckbox.setSelected(kmlExporter.isShowTileBorders());
		autoTileSideLengthText.setText(String.valueOf(kmlExporter.getAutoTileSideLength()));
		writeJSONCheckbox.setSelected(kmlExporter.isWriteJSONFile());
	}

	@Override
	public void setSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		kmlExporter.setExportAsKmz(kmzCheckbox.isSelected());
		kmlExporter.setShowBoundingBox(showBoundingBoxCheckbox.isEnabled() && showBoundingBoxCheckbox.isSelected());
		kmlExporter.setShowTileBorders(showTileBordersCheckbox.isEnabled() && showTileBordersCheckbox.isSelected());

		try {
			kmlExporter.setAutoTileSideLength(Double.parseDouble(autoTileSideLengthText.getText().trim()));
			if (kmlExporter.getAutoTileSideLength() <= 1.0) {
				kmlExporter.setAutoTileSideLength(125.0);
			}
		}
		catch (NumberFormatException nfe) {}

		kmlExporter.setWriteJSONFile(writeJSONCheckbox.isSelected());
	}

	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.kmlExport.general");
	}

}
