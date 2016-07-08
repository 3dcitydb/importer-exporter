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
public class PointAndCurveBalloonPanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;

	private JCheckBox pointIncludeDescription = new JCheckBox();
	private JPanel pointContentSourcePanel;
	private JRadioButton pointGenAttribRadioButton = new JRadioButton("");
	private JRadioButton pointFileRadioButton = new JRadioButton("");
	private JRadioButton pointGenAttribAndFileRadioButton = new JRadioButton("");
	private JTextField pointBrowseText = new JTextField("");
	private JButton pointBrowseButton = new JButton("");
//	private JCheckBox pointContentInSeparateFile = new JCheckBox();
//	private JLabel pointWarningLabel = new JLabel();

	private JCheckBox curveIncludeDescription = new JCheckBox();
	private JPanel curveContentSourcePanel;
	private JRadioButton curveGenAttribRadioButton = new JRadioButton("");
	private JRadioButton curveFileRadioButton = new JRadioButton("");
	private JRadioButton curveGenAttribAndFileRadioButton = new JRadioButton("");
	private JTextField curveBrowseText = new JTextField("");
	private JButton curveBrowseButton = new JButton("");
//	private JCheckBox curveContentInSeparateFile = new JCheckBox();
//	private JLabel curveWarningLabel = new JLabel();

	private JLabel warningLabel1 = new JLabel();
	private JLabel warningLabel2 = new JLabel();

	private Balloon pointBalloon; 
	private Balloon curveBalloon;
		
	public PointAndCurveBalloonPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.kmlExport.gcoPointAndCurveBalloon");
	}

	@Override
	public boolean isModified() {
		if (pointIncludeDescription.isSelected() != pointBalloon.isIncludeDescription()) return true;
		switch (pointBalloon.getBalloonContentMode()) {
			case GEN_ATTRIB:
				if (!pointGenAttribRadioButton.isSelected()) return true;
				break;
			case FILE:
				if (!pointFileRadioButton.isSelected()) return true;
				break;
			case GEN_ATTRIB_AND_FILE:
				if (!pointGenAttribAndFileRadioButton.isSelected()) return true;
				break;
		}
		if (!pointBrowseText.getText().trim().equals(pointBalloon.getBalloonContentTemplateFile())) return true;
//		if (pointContentInSeparateFile.isSelected() != pointBalloon.isBalloonContentInSeparateFile()) return true;

		if (curveIncludeDescription.isSelected() != curveBalloon.isIncludeDescription()) return true;
		switch (curveBalloon.getBalloonContentMode()) {
			case GEN_ATTRIB:
				if (!curveGenAttribRadioButton.isSelected()) return true;
				break;
			case FILE:
				if (!curveFileRadioButton.isSelected()) return true;
				break;
			case GEN_ATTRIB_AND_FILE:
				if (!curveGenAttribAndFileRadioButton.isSelected()) return true;
				break;
		}
		if (!curveBrowseText.getText().trim().equals(curveBalloon.getBalloonContentTemplateFile())) return true;
//		if (curveContentInSeparateFile.isSelected() != curveBalloon.isBalloonContentInSeparateFile()) return true;

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		ButtonGroup pointContentSourceRadioGroup = new ButtonGroup();
		pointContentSourceRadioGroup.add(pointGenAttribRadioButton);
		pointContentSourceRadioGroup.add(pointFileRadioButton);
		pointContentSourceRadioGroup.add(pointGenAttribAndFileRadioButton);

		pointContentSourcePanel = new JPanel();
		pointContentSourcePanel.setLayout(new GridBagLayout());
		pointContentSourcePanel.setBorder(BorderFactory.createTitledBorder(""));
		add(pointContentSourcePanel, GuiUtil.setConstraints(0,0,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));

		pointGenAttribRadioButton.setIconTextGap(10);
		pointFileRadioButton.setIconTextGap(10);
		pointGenAttribAndFileRadioButton.setIconTextGap(10);

		pointIncludeDescription.setIconTextGap(10);
		pointContentSourcePanel.add(pointIncludeDescription, GuiUtil.setConstraints(0,0,2,1,1.0,0.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		GridBagConstraints pgarb = GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*5,0,BORDER_THICKNESS);
		pgarb.gridwidth = 2;
		pointContentSourcePanel.add(pointGenAttribRadioButton, pgarb);
		GridBagConstraints pfrb = GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*5,0,BORDER_THICKNESS);
		pfrb.gridwidth = 2;
		pointContentSourcePanel.add(pointFileRadioButton, pfrb);
		pointBrowseText.setPreferredSize(pointBrowseText.getSize());
		pointContentSourcePanel.add(pointBrowseText, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*10,0,0));
		pointContentSourcePanel.add(pointBrowseButton, GuiUtil.setConstraints(1,3,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*2,0,BORDER_THICKNESS));
		GridBagConstraints pgaafrb = GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*5,0,BORDER_THICKNESS);
		pgaafrb.gridwidth = 2;
		pointContentSourcePanel.add(pointGenAttribAndFileRadioButton, pgaafrb);

//		pointContentInSeparateFile.setIconTextGap(10);
//		pointContentSourcePanel.add(pointContentInSeparateFile, GuiUtil.setConstraints(0,5,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS*2,BORDER_THICKNESS,0,0));
//		pointContentSourcePanel.add(pointWarningLabel, GuiUtil.setConstraints(0,6,2,1,1.0,0.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*6,BORDER_THICKNESS,0));


		ButtonGroup curveContentSourceRadioGroup = new ButtonGroup();
		curveContentSourceRadioGroup.add(curveGenAttribRadioButton);
		curveContentSourceRadioGroup.add(curveFileRadioButton);
		curveContentSourceRadioGroup.add(curveGenAttribAndFileRadioButton);

		curveContentSourcePanel = new JPanel();
		curveContentSourcePanel.setLayout(new GridBagLayout());
		curveContentSourcePanel.setBorder(BorderFactory.createTitledBorder(""));
		add(curveContentSourcePanel, GuiUtil.setConstraints(0,1,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));

		curveGenAttribRadioButton.setIconTextGap(10);
		curveFileRadioButton.setIconTextGap(10);
		curveGenAttribAndFileRadioButton.setIconTextGap(10);

		curveIncludeDescription.setIconTextGap(10);
		curveContentSourcePanel.add(curveIncludeDescription, GuiUtil.setConstraints(0,0,2,1,1.0,0.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		GridBagConstraints cgarb = GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*5,0,BORDER_THICKNESS);
		cgarb.gridwidth = 2;
		curveContentSourcePanel.add(curveGenAttribRadioButton, cgarb);
		GridBagConstraints cfrb = GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*5,0,BORDER_THICKNESS);
		cfrb.gridwidth = 2;
		curveContentSourcePanel.add(curveFileRadioButton, cfrb);
		curveBrowseText.setPreferredSize(curveBrowseText.getSize());
		curveContentSourcePanel.add(curveBrowseText, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*10,0,0));
		curveContentSourcePanel.add(curveBrowseButton, GuiUtil.setConstraints(1,3,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*2,0,BORDER_THICKNESS));
		GridBagConstraints cgaafrb = GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*5,0,BORDER_THICKNESS);
		cgaafrb.gridwidth = 2;
		curveContentSourcePanel.add(curveGenAttribAndFileRadioButton, cgaafrb);

//		curveContentInSeparateFile.setIconTextGap(10);
//		curveContentSourcePanel.add(curveContentInSeparateFile, GuiUtil.setConstraints(0,5,2,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS*2,BORDER_THICKNESS,0,0));
//		curveContentSourcePanel.add(curveWarningLabel, GuiUtil.setConstraints(0,6,2,1,1.0,0.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*6,BORDER_THICKNESS,0));

		add(warningLabel1, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.NONE,BORDER_THICKNESS*3,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		add(warningLabel2, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));

		PopupMenuDecorator.getInstance().decorate(pointBrowseText, curveBrowseText);

		pointIncludeDescription.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		pointBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile(pointBalloon, pointBrowseText);
			}
		});

		pointGenAttribRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		pointFileRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		pointGenAttribAndFileRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		curveIncludeDescription.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		curveBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile(curveBalloon, curveBrowseText);
			}
		});

		curveGenAttribRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		curveFileRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		curveGenAttribAndFileRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});
	}

	@Override
	public void doTranslation() {
		((TitledBorder)pointContentSourcePanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.balloon.contentSourceForPoint.border"));	
		pointIncludeDescription.setText(Language.I18N.getString("pref.kmlexport.balloon.label.includeDescription"));
		pointGenAttribRadioButton.setText(Language.I18N.getString("pref.kmlexport.balloon.label.genAttrib"));
		pointFileRadioButton.setText(Language.I18N.getString("pref.kmlexport.balloon.label.file"));
		pointGenAttribAndFileRadioButton.setText(Language.I18N.getString("pref.kmlexport.balloon.label.genAttribAndFile"));
		pointBrowseButton.setText(Language.I18N.getString("common.button.browse"));
//		pointContentInSeparateFile.setText(Language.I18N.getString("pref.kmlexport.balloon.label.contentInSeparateFile"));
//		pointWarningLabel.setText(Language.I18N.getString("pref.kmlexport.balloon.label.warningLabel"));

		((TitledBorder)curveContentSourcePanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.balloon.contentSourceForCurve.border"));	
		curveIncludeDescription.setText(Language.I18N.getString("pref.kmlexport.balloon.label.includeDescription"));
		curveGenAttribRadioButton.setText(Language.I18N.getString("pref.kmlexport.balloon.label.genAttrib"));
		curveFileRadioButton.setText(Language.I18N.getString("pref.kmlexport.balloon.label.file"));
		curveGenAttribAndFileRadioButton.setText(Language.I18N.getString("pref.kmlexport.balloon.label.genAttribAndFile"));
		curveBrowseButton.setText(Language.I18N.getString("common.button.browse"));
//		curveContentInSeparateFile.setText(Language.I18N.getString("pref.kmlexport.balloon.label.contentInSeparateFile"));
//		curveWarningLabel.setText(Language.I18N.getString("pref.kmlexport.balloon.label.warningLabel"));

		warningLabel1.setText(Language.I18N.getString("pref.kmlexport.balloon.label.settingsApplyFrom1"));
		warningLabel2.setText(Language.I18N.getString("pref.kmlexport.balloon.label.settingsApplyFrom2"));
	}

	@Override
	public void loadSettings() {
		pointBalloon = config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getPointBalloon(); 

		pointIncludeDescription.setSelected(pointBalloon.isIncludeDescription());
		switch (pointBalloon.getBalloonContentMode()) {
			case GEN_ATTRIB:
				pointGenAttribRadioButton.setSelected(true);
				break;
			case FILE:
				pointFileRadioButton.setSelected(true);
				break;
			case GEN_ATTRIB_AND_FILE:
				pointGenAttribAndFileRadioButton.setSelected(true);
				break;
		}
		pointBrowseText.setText(pointBalloon.getBalloonContentTemplateFile());
	
		curveBalloon = config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getCurveBalloon();

		curveIncludeDescription.setSelected(curveBalloon.isIncludeDescription());
		switch (curveBalloon.getBalloonContentMode()) {
			case GEN_ATTRIB:
				curveGenAttribRadioButton.setSelected(true);
				break;
			case FILE:
				curveFileRadioButton.setSelected(true);
				break;
			case GEN_ATTRIB_AND_FILE:
				curveGenAttribAndFileRadioButton.setSelected(true);
				break;
		}
		curveBrowseText.setText(curveBalloon.getBalloonContentTemplateFile());

		setEnabledComponents();
	}

	@Override
	public void setSettings() {
		pointBalloon.setIncludeDescription(pointIncludeDescription.isSelected());
		if (pointGenAttribRadioButton.isSelected()) {
			pointBalloon.setBalloonContentMode(BalloonContentMode.GEN_ATTRIB);
		}
		else if (pointFileRadioButton.isSelected()) {
			pointBalloon.setBalloonContentMode(BalloonContentMode.FILE);
		}
		else if (pointGenAttribAndFileRadioButton.isSelected()) {
			pointBalloon.setBalloonContentMode(BalloonContentMode.GEN_ATTRIB_AND_FILE);
		}
		pointBalloon.getBalloonContentPath().setLastUsedPath(pointBrowseText.getText().trim());
		pointBalloon.setBalloonContentTemplateFile(pointBrowseText.getText().trim());
//		pointBalloon.setBalloonContentInSeparateFile(pointContentInSeparateFile.isSelected());

		curveBalloon.setIncludeDescription(curveIncludeDescription.isSelected());
		if (curveGenAttribRadioButton.isSelected()) {
			curveBalloon.setBalloonContentMode(BalloonContentMode.GEN_ATTRIB);
		}
		else if (curveFileRadioButton.isSelected()) {
			curveBalloon.setBalloonContentMode(BalloonContentMode.FILE);
		}
		else if (curveGenAttribAndFileRadioButton.isSelected()) {
			curveBalloon.setBalloonContentMode(BalloonContentMode.GEN_ATTRIB_AND_FILE);
		}
		curveBalloon.getBalloonContentPath().setLastUsedPath(curveBrowseText.getText().trim());
		curveBalloon.setBalloonContentTemplateFile(curveBrowseText.getText().trim());
//		curveBalloon.setBalloonContentInSeparateFile(curveContentInSeparateFile.isSelected());
	}
	
	private void loadFile(Balloon balloon, JTextField browseText) {
		JFileChooser fileChooser = new JFileChooser();

		FileNameExtensionFilter filter = new FileNameExtensionFilter("HTML Files (*.htm, *.html)", "htm", "html");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
		fileChooser.setFileFilter(filter);

		if (balloon.getBalloonContentPath().isSetLastUsedMode()) {
			fileChooser.setCurrentDirectory(new File(balloon.getBalloonContentPath().getLastUsedPath()));
		} else {
			fileChooser.setCurrentDirectory(new File(balloon.getBalloonContentPath().getStandardPath()));
		}
		int result = fileChooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;
		try {
			String exportString = fileChooser.getSelectedFile().toString();
			browseText.setText(exportString);
			balloon.getBalloonContentPath().setLastUsedPath(fileChooser.getCurrentDirectory().getAbsolutePath());
			balloon.getBalloonContentPath().setPathMode(PathMode.LASTUSED);
		}
		catch (Exception e) {
			//
		}
	}

	private void setEnabledComponents() {
		pointGenAttribRadioButton.setEnabled(pointIncludeDescription.isSelected());
		pointFileRadioButton.setEnabled(pointIncludeDescription.isSelected());
		pointGenAttribAndFileRadioButton.setEnabled(pointIncludeDescription.isSelected());
//		pointContentInSeparateFile.setEnabled(pointIncludeDescription.isSelected());
//		pointWarningLabel.setEnabled(pointIncludeDescription.isSelected());
		
		boolean pointBrowseEnabled = (pointFileRadioButton.isEnabled() && pointFileRadioButton.isSelected()) ||
									 (pointGenAttribAndFileRadioButton.isEnabled() && pointGenAttribAndFileRadioButton.isSelected());

		pointBrowseText.setEnabled(pointBrowseEnabled);
		pointBrowseButton.setEnabled(pointBrowseEnabled);


		curveGenAttribRadioButton.setEnabled(curveIncludeDescription.isSelected());
		curveFileRadioButton.setEnabled(curveIncludeDescription.isSelected());
		curveGenAttribAndFileRadioButton.setEnabled(curveIncludeDescription.isSelected());
//		curveContentInSeparateFile.setEnabled(curveIncludeDescription.isSelected());
//		curveWarningLabel.setEnabled(curveIncludeDescription.isSelected());
		
		boolean curveBrowseEnabled = (curveFileRadioButton.isEnabled() && curveFileRadioButton.isSelected()) ||
									 (curveGenAttribAndFileRadioButton.isEnabled() && curveGenAttribAndFileRadioButton.isSelected());

		curveBrowseText.setEnabled(curveBrowseEnabled);
		curveBrowseButton.setEnabled(curveBrowseEnabled);
	}

}
