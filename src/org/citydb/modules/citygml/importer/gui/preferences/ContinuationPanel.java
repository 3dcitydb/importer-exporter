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
package org.citydb.modules.citygml.importer.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.importer.Continuation;
import org.citydb.config.project.importer.CreationDateMode;
import org.citydb.config.project.importer.TerminationDateMode;
import org.citydb.config.project.importer.UpdatingPersonMode;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ContinuationPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JPanel block2;
	private JPanel block3;
	private JPanel block4;
	
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
		Continuation continuation = config.getProject().getImporter().getContinuation();

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
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			{
				block1.add(lineageLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(lineageText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(reasonForUpdateLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(reasonForUpdateText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			}
			
			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			int lmargin = (int)(updatingPersonUser.getPreferredSize().getWidth()) + 11;
			updatingPersonDBAccount.setIconTextGap(10);
			updatingPersonUser.setIconTextGap(10);
			{
				block2.add(updatingPersonDBAccount, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(updatingPersonUser, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(updatingPersonText, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
			}
      
			block3 = new JPanel();
			add(block3, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block3.setBorder(BorderFactory.createTitledBorder(""));
			block3.setLayout(new GridBagLayout());
			creDateRadioInherit.setIconTextGap(10);
			creDateRadioOnlyMissing.setIconTextGap(10);
			creDateRadioAll.setIconTextGap(10);
			{
				block3.add(creDateRadioInherit, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block3.add(creDateRadioOnlyMissing, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block3.add(creDateRadioAll, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}

			block4 = new JPanel();
			add(block4, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block4.setBorder(BorderFactory.createTitledBorder(""));
			block4.setLayout(new GridBagLayout());
			termDateRadioInherit.setIconTextGap(10);
			termDateRadioOnlyMissing.setIconTextGap(10);
			termDateRadioAll.setIconTextGap(10);
			{
				block4.add(termDateRadioInherit, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block4.add(termDateRadioOnlyMissing, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block4.add(termDateRadioAll, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
		
		ActionListener updatingPersonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledUpdatingPerson();
			}
		};
		
		updatingPersonDBAccount.addActionListener(updatingPersonListener);
		updatingPersonUser.addActionListener(updatingPersonListener);
	}
	
	private void setEnabledUpdatingPerson() {
		updatingPersonText.setEnabled(updatingPersonUser.isSelected());
	}
	
	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.import.continuation.border.lineage"));	
		((TitledBorder)block2.getBorder()).setTitle(Language.I18N.getString("pref.import.continuation.border.updatingPerson"));	
		((TitledBorder)block3.getBorder()).setTitle(Language.I18N.getString("pref.import.continuation.border.creationDate"));	
		((TitledBorder)block4.getBorder()).setTitle(Language.I18N.getString("pref.import.continuation.border.terminationDate"));	

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
		Continuation continuation = config.getProject().getImporter().getContinuation();
		
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
		Continuation continuation = config.getProject().getImporter().getContinuation();
		
		continuation.setLineage(lineageText.getText());
		continuation.setReasonForUpdate(reasonForUpdateText.getText());
		continuation.setUpdatingPerson(updatingPersonText.getText());
		
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
