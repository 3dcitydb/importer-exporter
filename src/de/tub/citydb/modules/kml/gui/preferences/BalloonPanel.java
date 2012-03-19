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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.general.PathMode;
import de.tub.citydb.config.project.kmlExporter.BalloonContentMode;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class BalloonPanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;

	private JCheckBox includeDescription = new JCheckBox();
	private JPanel contentSourcePanel;
	private JRadioButton genAttribRadioButton = new JRadioButton("");
	private JRadioButton fileRadioButton = new JRadioButton("");
	private JRadioButton genAttribAndFileRadioButton = new JRadioButton("");
	private JTextField browseText = new JTextField("");
	private JButton browseButton = new JButton("");
	private JCheckBox contentInSeparateFile = new JCheckBox();
	private JLabel warningLabel = new JLabel();

	public BalloonPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();
		if (includeDescription.isSelected() != kmlExporter.isIncludeDescription()) return true;

		switch (kmlExporter.getBalloonContentMode()) {
		case GEN_ATTRIB:
			if (!genAttribRadioButton.isSelected())
				return true;
			break;
		case FILE:
			if (!fileRadioButton.isSelected())
				return true;
			break;
		case GEN_ATTRIB_AND_FILE:
			if (!genAttribAndFileRadioButton.isSelected())
				return true;
			break;
		}

		if (!kmlExporter.getBalloonContentTemplateFile().equals(browseText.getText().trim())) return true;
		if (contentInSeparateFile.isSelected() != kmlExporter.isBalloonContentInSeparateFile()) return true;

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		ButtonGroup contentSourceRadioGroup = new ButtonGroup();
		contentSourceRadioGroup.add(genAttribRadioButton);
		contentSourceRadioGroup.add(fileRadioButton);
		contentSourceRadioGroup.add(genAttribAndFileRadioButton);

		includeDescription.setIconTextGap(10);
		add(includeDescription, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));

		contentSourcePanel = new JPanel();
		contentSourcePanel.setLayout(new GridBagLayout());
		contentSourcePanel.setBorder(BorderFactory.createTitledBorder(""));
		add(contentSourcePanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));

		genAttribRadioButton.setIconTextGap(10);
		fileRadioButton.setIconTextGap(10);
		genAttribAndFileRadioButton.setIconTextGap(10);

		GridBagConstraints garb = GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS);
		garb.gridwidth = 2;
		contentSourcePanel.add(genAttribRadioButton, garb);
		GridBagConstraints frb = GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS);
		frb.gridwidth = 2;
		contentSourcePanel.add(fileRadioButton, frb);
		browseText.setPreferredSize(browseText.getSize());
		contentSourcePanel.add(browseText, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 6,0,0));
		contentSourcePanel.add(browseButton, GuiUtil.setConstraints(1,2,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 2,0,BORDER_THICKNESS));
		GridBagConstraints gaafrb = GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS);
		gaafrb.gridwidth = 2;
		contentSourcePanel.add(genAttribAndFileRadioButton, gaafrb);

		contentInSeparateFile.setIconTextGap(10);
		add(contentInSeparateFile, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,0,0));
		add(warningLabel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 6,0,0));

		PopupMenuDecorator.getInstance().decorate(browseText);

		includeDescription.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile();
			}
		});

		genAttribRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		fileRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		genAttribAndFileRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});
	}

	@Override
	public void doTranslation() {
		((TitledBorder)contentSourcePanel.getBorder()).setTitle(Internal.I18N.getString("pref.kmlexport.balloon.contentSource.border"));	

		includeDescription.setText(Internal.I18N.getString("pref.kmlexport.balloon.label.includeDescription"));
		genAttribRadioButton.setText(Internal.I18N.getString("pref.kmlexport.balloon.label.genAttrib"));
		fileRadioButton.setText(Internal.I18N.getString("pref.kmlexport.balloon.label.file"));
		genAttribAndFileRadioButton.setText(Internal.I18N.getString("pref.kmlexport.balloon.label.genAttribAndFile"));
		browseButton.setText(Internal.I18N.getString("common.button.browse"));
		contentInSeparateFile.setText(Internal.I18N.getString("pref.kmlexport.balloon.label.contentInSeparateFile"));
		warningLabel.setText(Internal.I18N.getString("pref.kmlexport.balloon.label.warningLabel"));
	}

	@Override
	public void loadSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();
		includeDescription.setSelected(kmlExporter.isIncludeDescription());
		switch (kmlExporter.getBalloonContentMode()) {
			case GEN_ATTRIB:
				genAttribRadioButton.setSelected(true);
				break;
			case FILE:
				fileRadioButton.setSelected(true);
				break;
			case GEN_ATTRIB_AND_FILE:
				genAttribAndFileRadioButton.setSelected(true);
				break;
		}
		browseText.setText(kmlExporter.getBalloonContentTemplateFile());
		contentInSeparateFile.setSelected(kmlExporter.isBalloonContentInSeparateFile());
		setEnabledComponents();
	}

	@Override
	public void setSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();
		kmlExporter.setIncludeDescription(includeDescription.isSelected());
		if (genAttribRadioButton.isSelected()) {
			kmlExporter.setBalloonContentMode(BalloonContentMode.GEN_ATTRIB);
		}
		else if (fileRadioButton.isSelected()) {
			kmlExporter.setBalloonContentMode(BalloonContentMode.FILE);
		}
		else if (genAttribAndFileRadioButton.isSelected()) {
			kmlExporter.setBalloonContentMode(BalloonContentMode.GEN_ATTRIB_AND_FILE);
		}
		kmlExporter.getBalloonContentPath().setLastUsedPath(browseText.getText().trim());
		kmlExporter.setBalloonContentTemplateFile(browseText.getText().trim());
		kmlExporter.setBalloonContentInSeparateFile(contentInSeparateFile.isSelected());
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.kmlExport.balloon");
	}

	private void loadFile() {
		JFileChooser fileChooser = new JFileChooser();

		FileNameExtensionFilter filter = new FileNameExtensionFilter("HTML Files (*.htm, *.html)", "htm", "html");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
		fileChooser.setFileFilter(filter);

		if (config.getProject().getKmlExporter().getBalloonContentPath().isSetLastUsedMode()) {
			fileChooser.setCurrentDirectory(new File(config.getProject().getKmlExporter().getBalloonContentPath().getLastUsedPath()));
		} else {
			fileChooser.setCurrentDirectory(new File(config.getProject().getKmlExporter().getBalloonContentPath().getStandardPath()));
		}
		int result = fileChooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;
		try {
			String exportString = fileChooser.getSelectedFile().toString();
			browseText.setText(exportString);
			config.getProject().getKmlExporter().getBalloonContentPath().setLastUsedPath(fileChooser.getCurrentDirectory().getAbsolutePath());
			config.getProject().getKmlExporter().getBalloonContentPath().setPathMode(PathMode.LASTUSED);
		}
		catch (Exception e) {
			//
		}
	}

	private void setEnabledComponents() {
		genAttribRadioButton.setEnabled(includeDescription.isSelected());
		fileRadioButton.setEnabled(includeDescription.isSelected());
		genAttribAndFileRadioButton.setEnabled(includeDescription.isSelected());
		contentInSeparateFile.setEnabled(includeDescription.isSelected());
		warningLabel.setEnabled(includeDescription.isSelected());
		
		boolean browseEnabled = (fileRadioButton.isEnabled() && fileRadioButton.isSelected()) ||
								(genAttribAndFileRadioButton.isEnabled() && genAttribAndFileRadioButton.isSelected());

		browseText.setEnabled(browseEnabled);
		browseButton.setEnabled(browseEnabled);
	}

}
