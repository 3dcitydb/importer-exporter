/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.modules.kml.gui.preferences;

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

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.general.PathMode;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.BalloonContentMode;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class TunnelBalloonPanel extends AbstractPreferencesComponent {

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

	private Balloon internalBalloon = new Balloon();

	public TunnelBalloonPanel(Config config) {
		super(config);
		initGui();
	}

	private Balloon getConfigBalloon() {
		return config.getProject().getKmlExporter().getTunnelBalloon();
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.kmlExport.tunnelBalloon");
	}

	@Override
	public boolean isModified() {
		setInternBalloonValues();
		if (!getConfigBalloon().equals(internalBalloon)) return true;
		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		ButtonGroup contentSourceRadioGroup = new ButtonGroup();
		contentSourceRadioGroup.add(genAttribRadioButton);
		contentSourceRadioGroup.add(fileRadioButton);
		contentSourceRadioGroup.add(genAttribAndFileRadioButton);

		includeDescription.setIconTextGap(10);
		add(includeDescription, GuiUtil.setConstraints(0,1,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));

		contentSourcePanel = new JPanel();
		contentSourcePanel.setLayout(new GridBagLayout());
		contentSourcePanel.setBorder(BorderFactory.createTitledBorder(""));
		add(contentSourcePanel, GuiUtil.setConstraints(0,2,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));

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
		add(contentInSeparateFile, GuiUtil.setConstraints(0,3,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,0,0));
		add(warningLabel, GuiUtil.setConstraints(0,4,2,1,1.0,0.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 6,0,0));

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
		((TitledBorder)contentSourcePanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.balloon.contentSource.border"));	

		includeDescription.setText(Language.I18N.getString("pref.kmlexport.balloon.label.includeDescription"));
		genAttribRadioButton.setText(Language.I18N.getString("pref.kmlexport.balloon.label.genAttrib"));
		fileRadioButton.setText(Language.I18N.getString("pref.kmlexport.balloon.label.file"));
		genAttribAndFileRadioButton.setText(Language.I18N.getString("pref.kmlexport.balloon.label.genAttribAndFile"));
		browseButton.setText(Language.I18N.getString("common.button.browse"));
		contentInSeparateFile.setText(Language.I18N.getString("pref.kmlexport.balloon.label.contentInSeparateFile"));
		warningLabel.setText(Language.I18N.getString("pref.kmlexport.balloon.label.warningLabel"));
	}

	@Override
	public void loadSettings() {
		Balloon configBalloon = getConfigBalloon();
		copyBalloonContents(configBalloon, internalBalloon);

		includeDescription.setSelected(configBalloon.isIncludeDescription());
		switch (configBalloon.getBalloonContentMode()) {
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
		browseText.setText(configBalloon.getBalloonContentTemplateFile());
		contentInSeparateFile.setSelected(configBalloon.isBalloonContentInSeparateFile());
		setEnabledComponents();
	}

	private void setInternBalloonValues() {
		internalBalloon.setIncludeDescription(includeDescription.isSelected());
		if (genAttribRadioButton.isSelected()) {
			internalBalloon.setBalloonContentMode(BalloonContentMode.GEN_ATTRIB);
		}
		else if (fileRadioButton.isSelected()) {
			internalBalloon.setBalloonContentMode(BalloonContentMode.FILE);
		}
		else if (genAttribAndFileRadioButton.isSelected()) {
			internalBalloon.setBalloonContentMode(BalloonContentMode.GEN_ATTRIB_AND_FILE);
		}
		internalBalloon.getBalloonContentPath().setLastUsedPath(browseText.getText().trim());
		internalBalloon.setBalloonContentTemplateFile(browseText.getText().trim());
		internalBalloon.setBalloonContentInSeparateFile(contentInSeparateFile.isSelected());
	}

	@Override
	public void setSettings() {
		setInternBalloonValues();
		Balloon configBalloon = getConfigBalloon();
		copyBalloonContents(internalBalloon, configBalloon);
	}
	
	private void loadFile() {
		JFileChooser fileChooser = new JFileChooser();

		FileNameExtensionFilter filter = new FileNameExtensionFilter("HTML Files (*.htm, *.html)", "htm", "html");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
		fileChooser.setFileFilter(filter);

		if (internalBalloon.getBalloonContentPath().isSetLastUsedMode()) {
			fileChooser.setCurrentDirectory(new File(internalBalloon.getBalloonContentPath().getLastUsedPath()));
		} else {
			fileChooser.setCurrentDirectory(new File(internalBalloon.getBalloonContentPath().getStandardPath()));
		}
		int result = fileChooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;
		try {
			String exportString = fileChooser.getSelectedFile().toString();
			browseText.setText(exportString);
			internalBalloon.getBalloonContentPath().setLastUsedPath(fileChooser.getCurrentDirectory().getAbsolutePath());
			internalBalloon.getBalloonContentPath().setPathMode(PathMode.LASTUSED);
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

	private void copyBalloonContents(Balloon sourceBalloon, Balloon targetBalloon) {
		targetBalloon.setBalloonContentInSeparateFile(sourceBalloon.isBalloonContentInSeparateFile());
		targetBalloon.setBalloonContentMode(sourceBalloon.getBalloonContentMode());
		targetBalloon.setBalloonContentTemplateFile(sourceBalloon.getBalloonContentTemplateFile());
		targetBalloon.setIncludeDescription(sourceBalloon.isIncludeDescription());
		targetBalloon.getBalloonContentPath().setPathMode(sourceBalloon.getBalloonContentPath().getPathMode());
		targetBalloon.getBalloonContentPath().setLastUsedPath(sourceBalloon.getBalloonContentPath().getLastUsedPath());
		targetBalloon.getBalloonContentPath().setStandardPath(sourceBalloon.getBalloonContentPath().getStandardPath());
	}
	
}
