/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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
package org.citydb.gui.modules.importer.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.UpdatingPersonMode;
import org.citydb.config.project.importer.Continuation;
import org.citydb.config.project.importer.CreationDateMode;
import org.citydb.config.project.importer.TerminationDateMode;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class ContinuationPanel extends AbstractPreferencesComponent {
	private TitledPanel metadataPanel;
	private TitledPanel personPanel;
	private TitledPanel creationDatePanel;
	private TitledPanel terminationDatePanel;
	
	private JTextField lineageText;
	private JLabel lineageLabel;
	private JTextField reasonForUpdateText;
	private JLabel reasonForUpdateLabel;
	private JRadioButton updatingPersonDBAccount;
	private JRadioButton updatingPersonUser;
	private JTextField updatingPersonText;
	
	private JRadioButton creDateRadioInherit;
	private JRadioButton creDateRadioOnlyMissing;
	private JRadioButton creDateRadioAll;
	private JRadioButton termDateRadioInherit;
	private JRadioButton termDateRadioOnlyMissing;
	private JRadioButton termDateRadioAll;
	
	public ContinuationPanel(Config config) {
		super(config);
		initGui();
	}
	
	@Override
	public boolean isModified() {
		Continuation continuation = config.getImportConfig().getContinuation();

		if (!lineageText.getText().equals(continuation.getLineage())) return true;
		if (!reasonForUpdateText.getText().equals(continuation.getReasonForUpdate())) return true;
		if (!updatingPersonText.getText().equals(continuation.getUpdatingPerson())) return true;
		if (updatingPersonDBAccount.isSelected() != continuation.isUpdatingPersonModeDatabase()) return true;
		if (updatingPersonUser.isSelected() != continuation.isUpdatingPersonModeUser()) return true;
		if (creDateRadioInherit.isSelected() != continuation.isCreationDateModeInherit()) return true;
		if (creDateRadioOnlyMissing.isSelected() != continuation.isCreationDateModeComplement()) return true;
		if (creDateRadioAll.isSelected() != continuation.isCreationDateModeReplace()) return true;
		if (termDateRadioInherit.isSelected() != continuation.isTerminationDateModeInherit()) return true;
		if (termDateRadioOnlyMissing.isSelected() != continuation.isTerminationDateModeComplement()) return true;
		if (termDateRadioAll.isSelected() != continuation.isTerminationDateModeReplace()) return true;
    
		return false;
	}
	
	private void initGui() {
		lineageText = new JTextField();
		lineageLabel = new JLabel();
		reasonForUpdateText = new JTextField();
		reasonForUpdateLabel = new JLabel();
		
		updatingPersonDBAccount = new JRadioButton();
		updatingPersonUser = new JRadioButton();
		ButtonGroup updatingPerson = new ButtonGroup();
		updatingPerson.add(updatingPersonDBAccount);
		updatingPerson.add(updatingPersonUser);
		updatingPersonText = new JTextField();
		
		creDateRadioInherit = new JRadioButton();
		creDateRadioOnlyMissing = new JRadioButton();
		creDateRadioAll = new JRadioButton();
		ButtonGroup creDateRadio = new ButtonGroup();
		creDateRadio.add(creDateRadioInherit);
		creDateRadio.add(creDateRadioOnlyMissing);
		creDateRadio.add(creDateRadioAll);
    
		termDateRadioInherit = new JRadioButton();
		termDateRadioOnlyMissing = new JRadioButton();
		termDateRadioAll = new JRadioButton();
		ButtonGroup trmDateRadio = new ButtonGroup();
		trmDateRadio.add(termDateRadioInherit);
		trmDateRadio.add(termDateRadioOnlyMissing);
		trmDateRadio.add(termDateRadioAll);
    
		PopupMenuDecorator.getInstance().decorate(lineageText, reasonForUpdateText, updatingPersonText);
		
		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(lineageLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(lineageText, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 5, 0));
				content.add(reasonForUpdateLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(reasonForUpdateText, GuiUtil.setConstraints(1, 1, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			metadataPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(updatingPersonDBAccount, GuiUtil.setConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(updatingPersonUser, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 5));
				content.add(updatingPersonText, GuiUtil.setConstraints(1, 1, 1, 1, GridBagConstraints.BOTH, 5, 5, 0, 0));
			}

			personPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(creDateRadioInherit, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(creDateRadioOnlyMissing, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(creDateRadioAll, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
			}

			creationDatePanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(termDateRadioInherit, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(termDateRadioOnlyMissing, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(termDateRadioAll, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
			}

			terminationDatePanel = new TitledPanel().build(content);
		}

		add(metadataPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(personPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(creationDatePanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(terminationDatePanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		ActionListener updatingPersonListener = e -> setEnabledUpdatingPerson();
		updatingPersonDBAccount.addActionListener(updatingPersonListener);
		updatingPersonUser.addActionListener(updatingPersonListener);
	}
	
	private void setEnabledUpdatingPerson() {
		updatingPersonText.setEnabled(updatingPersonUser.isSelected());
	}
	
	@Override
	public void doTranslation() {
		metadataPanel.setTitle(Language.I18N.getString("pref.import.continuation.border.lineage"));
		personPanel.setTitle(Language.I18N.getString("pref.import.continuation.border.updatingPerson"));
		creationDatePanel.setTitle(Language.I18N.getString("pref.import.continuation.border.creationDate"));
		terminationDatePanel.setTitle(Language.I18N.getString("pref.import.continuation.border.terminationDate"));

		lineageLabel.setText(Language.I18N.getString("pref.import.continuation.label.lineage"));
		reasonForUpdateLabel.setText(Language.I18N.getString("pref.import.continuation.label.reasonForUpdate"));

		updatingPersonDBAccount.setText(Language.I18N.getString("pref.import.continuation.label.updatingPerson.database"));
		updatingPersonUser.setText(Language.I18N.getString("pref.import.continuation.label.updatingPerson.user"));		

		creDateRadioInherit.setText(Language.I18N.getString("pref.import.continuation.label.creationDate.inherit"));
		creDateRadioOnlyMissing.setText(Language.I18N.getString("pref.import.continuation.label.creationDate.onlyMissing"));
		creDateRadioAll.setText(Language.I18N.getString("pref.import.continuation.label.creationDate.all"));
    
		termDateRadioInherit.setText(Language.I18N.getString("pref.import.continuation.label.terminationDate.inherit"));
		termDateRadioOnlyMissing.setText(Language.I18N.getString("pref.import.continuation.label.terminationDate.onlyMissing"));
		termDateRadioAll.setText(Language.I18N.getString("pref.import.continuation.label.terminationDate.all"));
	}

	@Override
	public void loadSettings() {
		Continuation continuation = config.getImportConfig().getContinuation();
		
		lineageText.setText(continuation.getLineage());
		reasonForUpdateText.setText(continuation.getReasonForUpdate());
		updatingPersonText.setText(continuation.getUpdatingPerson());
		
		if (continuation.isUpdatingPersonModeDatabase())
			updatingPersonDBAccount.setSelected(true);
		else
			updatingPersonUser.setSelected(true);
		
		setEnabledUpdatingPerson();
    
		if (continuation.isCreationDateModeInherit())
			creDateRadioInherit.setSelected(true);
		else if (continuation.isCreationDateModeComplement())
			creDateRadioOnlyMissing.setSelected(true);
		else
			creDateRadioAll.setSelected(true);
    
		if (continuation.isTerminationDateModeInherit())
			termDateRadioInherit.setSelected(true);
		else if (continuation.isTerminationDateModeComplement())
			termDateRadioOnlyMissing.setSelected(true);
		else
			termDateRadioAll.setSelected(true);
	}

	@Override
	public void setSettings() {
		Continuation continuation = config.getImportConfig().getContinuation();

		String lineage = lineageText.getText().trim();
		continuation.setLineage(lineage);
		lineageText.setText(lineage);

		String reasonForUpdate = reasonForUpdateText.getText().trim();
		continuation.setReasonForUpdate(reasonForUpdate);
		reasonForUpdateText.setText(reasonForUpdate);

		String updatingPerson = updatingPersonText.getText().trim();
		continuation.setUpdatingPerson(updatingPerson);
		updatingPersonText.setText(updatingPerson);
		
		if (updatingPersonDBAccount.isSelected())
			continuation.setUpdatingPersonMode(UpdatingPersonMode.DATABASE);
		else
			continuation.setUpdatingPersonMode(UpdatingPersonMode.USER);
    
		if (creDateRadioInherit.isSelected())
			continuation.setCreationDateMode(CreationDateMode.INHERIT);
		else if (creDateRadioOnlyMissing.isSelected())
			continuation.setCreationDateMode(CreationDateMode.COMPLEMENT);
		else
			continuation.setCreationDateMode(CreationDateMode.REPLACE);
    
		if (termDateRadioInherit.isSelected())
			continuation.setTerminationDateMode(TerminationDateMode.INHERIT);
		else if (termDateRadioOnlyMissing.isSelected())
			continuation.setTerminationDateMode(TerminationDateMode.COMPLEMENT);
		else
			continuation.setTerminationDateMode(TerminationDateMode.REPLACE);
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.continuation");
	}

}
