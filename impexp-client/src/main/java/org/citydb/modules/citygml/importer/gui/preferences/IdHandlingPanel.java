/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.CodeSpaceMode;
import org.citydb.config.project.importer.ImportGmlId;
import org.citydb.config.project.importer.UUIDMode;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.version.DatabaseVersion;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.EventType;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class IdHandlingPanel extends AbstractPreferencesComponent implements EventHandler {
	private JPanel block1;
	private JLabel idPrefixLabel;
	private JTextField idPrefix;
	private JCheckBox impIdCheckExtRef;
	private JRadioButton impIdRadioAdd;
	private JRadioButton impIdRadioExchange;
	private JPanel block2;
	private JRadioButton impIdCSRadioNone;
	private JRadioButton impIdCSRadioFile;
	private JRadioButton impIdCSRadioFilePath;
	private JRadioButton impIdCSRadioUser;
	private JTextField impIdCSUserText;

	public IdHandlingPanel(Config config) {
		super(config);
		initGui();

		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);
	}

	@Override
	public boolean isModified() {
		ImportGmlId gmlId = config.getProject().getImporter().getGmlId();

		if (!idPrefix.getText().equals(gmlId.getIdPrefix())) return true;
		if (impIdCheckExtRef.isSelected() != gmlId.isSetKeepGmlIdAsExternalReference()) return true;
		if (impIdRadioAdd.isSelected() != gmlId.isUUIDModeComplement()) return true;
		if (impIdRadioExchange.isSelected() != gmlId.isUUIDModeReplace()) return true;
		if (!impIdCSUserText.getText().equals(gmlId.getCodeSpace())) return true;
		if (impIdCSRadioNone.isSelected() != gmlId.isSetNoneCodeSpaceMode()) return true;
		if (impIdCSRadioFile.isSelected() != gmlId.isSetRelativeCodeSpaceMode()) return true;
		if (impIdCSRadioFilePath.isSelected() != gmlId.isSetAbsoluteCodeSpaceMode()) return true;
		if (impIdCSRadioUser.isSelected() != gmlId.isSetUserCodeSpaceMode()) return true;
		return false;
	}

	private void initGui(){
		idPrefixLabel = new JLabel();
		idPrefix = new JTextField();
		impIdRadioAdd = new JRadioButton();
		impIdRadioExchange = new JRadioButton();
		ButtonGroup impIdRadio = new ButtonGroup();
		impIdRadio.add(impIdRadioAdd);
		impIdRadio.add(impIdRadioExchange);
		impIdCheckExtRef = new JCheckBox();
		
		impIdCSRadioNone = new JRadioButton();
		impIdCSRadioFile = new JRadioButton();
		impIdCSRadioFilePath = new JRadioButton();
		impIdCSRadioUser = new JRadioButton();
		ButtonGroup impIdCSRadio = new ButtonGroup();
		impIdCSRadio.add(impIdCSRadioNone);
		impIdCSRadio.add(impIdCSRadioFile);
		impIdCSRadio.add(impIdCSRadioFilePath);
		impIdCSRadio.add(impIdCSRadioUser);
		impIdCSUserText = new JTextField();
		
		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			impIdRadioAdd.setIconTextGap(10);
			impIdRadioExchange.setIconTextGap(10);
			impIdCheckExtRef.setIconTextGap(10);
			JPanel block1_1 = new JPanel();
			int lmargin = (int)(impIdRadioAdd.getPreferredSize().getWidth()) + 11;
			{
				block1_1.setLayout(new GridBagLayout());
				block1_1.setBorder(BorderFactory.createEmptyBorder());
				block1_1.add(idPrefixLabel, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.BOTH,0,0,0,5));
				block1_1.add(idPrefix, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,0));

				block1.add(block1_1, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(impIdRadioAdd, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impIdRadioExchange, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impIdCheckExtRef, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));
			}
			
			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			impIdCSRadioNone.setIconTextGap(10);
			impIdCSRadioFile.setIconTextGap(10);
			impIdCSRadioFilePath.setIconTextGap(10);
			impIdCSRadioUser.setIconTextGap(10);
			{
				block2.add(impIdCSRadioNone, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impIdCSRadioFile, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impIdCSRadioFilePath, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impIdCSRadioUser, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impIdCSUserText, GuiUtil.setConstraints(0,4,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
			}
		}
		
		ActionListener gmlIdListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledGmlId();
			}
		};
		
		ActionListener codeSpaceListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledCodeSpace();
			}
		};
		
		impIdRadioAdd.addActionListener(gmlIdListener);
		impIdRadioExchange.addActionListener(gmlIdListener);
		
		impIdCSRadioNone.addActionListener(codeSpaceListener);
		impIdCSRadioFile.addActionListener(codeSpaceListener);
		impIdCSRadioFilePath.addActionListener(codeSpaceListener);
		impIdCSRadioUser.addActionListener(codeSpaceListener);
	}
	
	private void setEnabledGmlId() {
		impIdCheckExtRef.setEnabled(impIdRadioExchange.isSelected());
	}

	private void setEnabledCodeSpace() {
		impIdCSUserText.setEnabled(impIdCSRadioUser.isSelected());
	}
	
	private void setEnabledCodespaceDialog(boolean enable) {
		((TitledBorder)block2.getBorder()).setTitleColor(enable ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		block2.repaint();
		
		impIdCSRadioNone.setEnabled(enable);
		impIdCSRadioFile.setEnabled(enable);
		impIdCSRadioFilePath.setEnabled(enable);
		impIdCSRadioUser.setEnabled(enable);
		impIdCSUserText.setEnabled(enable && impIdCSRadioUser.isSelected());
	}
	
	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.import.idHandling.border.id"));
		idPrefixLabel.setText(Language.I18N.getString("pref.import.idHandling.label.id.prefix"));
		impIdCheckExtRef.setText(Language.I18N.getString("pref.import.idHandling.label.id.extReference"));
		impIdRadioAdd.setText(Language.I18N.getString("pref.import.idHandling.label.id.add"));
		impIdRadioExchange.setText(Language.I18N.getString("pref.import.idHandling.label.id.exchange"));

		((TitledBorder)block2.getBorder()).setTitle(Language.I18N.getString("pref.import.idHandling.border.idCodeSpace"));
		impIdCSRadioNone.setText(Language.I18N.getString("pref.import.idHandling.label.idCodeSpace.none"));
		impIdCSRadioFile.setText(Language.I18N.getString("pref.import.idHandling.label.idCodeSpace.file"));
		impIdCSRadioFilePath.setText(Language.I18N.getString("pref.import.idHandling.label.idCodeSpace.filePath"));
		impIdCSRadioUser.setText(Language.I18N.getString("pref.import.idHandling.label.idCodeSpace.user"));
	}

	@Override
	public void loadSettings() {
		ImportGmlId gmlId = config.getProject().getImporter().getGmlId();

		if (gmlId.getIdPrefix() != null && gmlId.getIdPrefix().trim().length() != 0)
			idPrefix.setText(gmlId.getIdPrefix());
		else {
			idPrefix.setText(DefaultGMLIdManager.getInstance().getDefaultPrefix());
			gmlId.setIdPrefix(DefaultGMLIdManager.getInstance().getDefaultPrefix());
		}
		
		if (gmlId.isUUIDModeReplace())
			impIdRadioExchange.setSelected(true);
		else
			impIdRadioAdd.setSelected(true);

		impIdCheckExtRef.setSelected(gmlId.isSetKeepGmlIdAsExternalReference());

		setEnabledGmlId();
		
		if (gmlId.isSetNoneCodeSpaceMode())
			impIdCSRadioNone.setSelected(true);
		else if (gmlId.isSetUserCodeSpaceMode())
			impIdCSRadioUser.setSelected(true);
		else if (gmlId.isSetRelativeCodeSpaceMode())
			impIdCSRadioFile.setSelected(true);
		else
			impIdCSRadioFilePath.setSelected(true);

		impIdCSUserText.setText(gmlId.getCodeSpace());
		
		setEnabledCodeSpace();
	}

	@Override
	public void setSettings() {
		ImportGmlId gmlId = config.getProject().getImporter().getGmlId();

		if (idPrefix.getText() != null && DefaultGMLIdManager.getInstance().isValidPrefix(idPrefix.getText()))
			gmlId.setIdPrefix(idPrefix.getText());
		else {
			gmlId.setIdPrefix(DefaultGMLIdManager.getInstance().getDefaultPrefix());
			idPrefix.setText(DefaultGMLIdManager.getInstance().getDefaultPrefix());
		}
		
		if (impIdRadioAdd.isSelected())
			gmlId.setUuidMode(UUIDMode.COMPLEMENT);
		else
			gmlId.setUuidMode(UUIDMode.REPLACE);

		gmlId.setKeepGmlIdAsExternalReference(impIdCheckExtRef.isSelected());
		
		if (impIdCSRadioNone.isSelected())
			gmlId.setCodeSpaceMode(CodeSpaceMode.NONE);
		else if (impIdCSRadioFile.isSelected())
			gmlId.setCodeSpaceMode(CodeSpaceMode.RELATIVE);
		else if (impIdCSRadioFilePath.isSelected())
			gmlId.setCodeSpaceMode(CodeSpaceMode.ABSOLUTE);
		else
			gmlId.setCodeSpaceMode(CodeSpaceMode.USER);
		
		String gmlIdCodeSpace = impIdCSUserText.getText().trim();
		if (gmlIdCodeSpace.length() > 0)
			gmlId.setCodeSpace(gmlIdCodeSpace);
		else
			gmlIdCodeSpace = gmlId.getCodeSpace();
		
		impIdCSUserText.setText(gmlIdCodeSpace);
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.idHandling");
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		boolean showCodespaceDialog = true;
		if (((DatabaseConnectionStateEvent)event).isConnected()) {
			AbstractDatabaseAdapter databaseAdapter = ObjectRegistry.getInstance().getDatabaseController().getActiveDatabaseAdapter();
			DatabaseVersion version = databaseAdapter.getConnectionMetaData().getCityDBVersion();
			showCodespaceDialog = version.compareTo(3, 1, 0) >= 0;
		}
		
		setEnabledCodespaceDialog(showCodespaceDialog);
	}

}

