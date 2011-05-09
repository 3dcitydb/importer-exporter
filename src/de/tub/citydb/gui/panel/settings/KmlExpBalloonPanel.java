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
package de.tub.citydb.gui.panel.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.general.PathMode;
import de.tub.citydb.config.project.kmlExporter.BalloonContentMode;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.gui.components.StandardEditingPopupMenuDecorator;

@SuppressWarnings("serial")
public class KmlExpBalloonPanel extends PrefPanelBase {

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

	public KmlExpBalloonPanel(Config config) {
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
		setLayout(new BorderLayout());
		Box contentsPanel = Box.createVerticalBox();

		includeDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
		includeDescription.setIconTextGap(10);
		contentsPanel.add(includeDescription);
		
		ButtonGroup contentSourceRadioGroup = new ButtonGroup();
		contentSourceRadioGroup.add(genAttribRadioButton);
		contentSourceRadioGroup.add(fileRadioButton);
		contentSourceRadioGroup.add(genAttribAndFileRadioButton);

		Box contentSourceBox = Box.createVerticalBox();

		genAttribRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		genAttribRadioButton.setIconTextGap(10);
		contentSourceBox.add(genAttribRadioButton);
		fileRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		fileRadioButton.setIconTextGap(10);
		contentSourceBox.add(fileRadioButton);
		
		Box filenameBox = Box.createHorizontalBox();
		browseText.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_TEXTFIELD_HEIGHT));
		browseText.setPreferredSize(browseText.getSize());
		filenameBox.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 5, 0)));
		filenameBox.add(browseText);
		filenameBox.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		filenameBox.add(browseButton);
		filenameBox.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, BORDER_THICKNESS * 6)));
		filenameBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentSourceBox.add(filenameBox);
		
		genAttribAndFileRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		genAttribAndFileRadioButton.setIconTextGap(10);
		contentSourceBox.add(genAttribAndFileRadioButton);

		contentSourcePanel = new JPanel();
		contentSourcePanel.setLayout(new BorderLayout());
		contentSourcePanel.setBorder(BorderFactory.createTitledBorder(""));
		contentSourcePanel.add(contentSourceBox, BorderLayout.CENTER);
		contentSourcePanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)), BorderLayout.SOUTH);
		contentSourcePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		contentsPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS*2)));
		contentsPanel.add(contentSourcePanel);

		contentInSeparateFile.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentInSeparateFile.setIconTextGap(10);
		contentsPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS*2)));
		contentsPanel.add(contentInSeparateFile);
		Box warningBox = Box.createHorizontalBox();
		warningBox.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 6, 0)));
		warningBox.add(warningLabel);
		warningBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentsPanel.add(warningBox);

		StandardEditingPopupMenuDecorator.decorate(browseText);
		
		add(contentsPanel, BorderLayout.NORTH);

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
		includeDescription.setText(Internal.I18N.getString("pref.kmlexport.balloon.label.includeDescription"));
		contentSourcePanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.kmlexport.balloon.contentSource.border")));
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
