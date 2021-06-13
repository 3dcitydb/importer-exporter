/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.operation.importer.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.CodeSpaceMode;
import org.citydb.config.project.importer.ImportResourceId;
import org.citydb.config.project.importer.UUIDMode;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.version.DatabaseVersion;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.EventType;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.operation.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ResourceIdPanel extends AbstractPreferencesComponent implements EventHandler {
	private TitledPanel idAssignmentPanel;
	private JLabel idPrefixLabel;
	private JTextField idPrefix;
	private JCheckBox impIdCheckExtRef;
	private JRadioButton impIdRadioAdd;
	private JRadioButton impIdRadioExchange;
	private TitledPanel codespacePanel;
	private JRadioButton impIdCSRadioNone;
	private JRadioButton impIdCSRadioFile;
	private JRadioButton impIdCSRadioFilePath;
	private JRadioButton impIdCSRadioUser;
	private JTextField impIdCSUserText;

	public ResourceIdPanel(Config config) {
		super(config);
		initGui();

		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);
	}

	@Override
	public boolean isModified() {
		ImportResourceId resourceId = config.getImportConfig().getResourceId();

		if (!idPrefix.getText().equals(resourceId.getIdPrefix())) return true;
		if (impIdCheckExtRef.isSelected() != resourceId.isSetKeepIdAsExternalReference()) return true;
		if (impIdRadioAdd.isSelected() != resourceId.isUUIDModeComplement()) return true;
		if (impIdRadioExchange.isSelected() != resourceId.isUUIDModeReplace()) return true;
		if (!impIdCSUserText.getText().equals(resourceId.getCodeSpace())) return true;
		if (impIdCSRadioNone.isSelected() != resourceId.isSetNoneCodeSpaceMode()) return true;
		if (impIdCSRadioFile.isSelected() != resourceId.isSetRelativeCodeSpaceMode()) return true;
		if (impIdCSRadioFilePath.isSelected() != resourceId.isSetAbsoluteCodeSpaceMode()) return true;
		if (impIdCSRadioUser.isSelected() != resourceId.isSetUserCodeSpaceMode()) return true;
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
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			int lmargin = GuiUtil.getTextOffset(impIdRadioAdd);

			JPanel prefixContent = new JPanel();
			prefixContent.setLayout(new GridBagLayout());
			{
				prefixContent.setBorder(BorderFactory.createEmptyBorder());
				prefixContent.add(idPrefixLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				prefixContent.add(idPrefix, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			content.add(prefixContent, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
			content.add(impIdRadioAdd, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(impIdRadioExchange, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(impIdCheckExtRef, GuiUtil.setConstraints(0, 3, 1, 1, GridBagConstraints.BOTH, 5, lmargin, 0, 0));

			idAssignmentPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			int lmargin = GuiUtil.getTextOffset(impIdCSRadioUser);
			content.setLayout(new GridBagLayout());
			{
				content.add(impIdCSRadioNone, GuiUtil.setConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(impIdCSRadioFile, GuiUtil.setConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(impIdCSRadioFilePath, GuiUtil.setConstraints(0, 2, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(impIdCSRadioUser, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 5));
				content.add(impIdCSUserText, GuiUtil.setConstraints(1, 3, 1, 1, GridBagConstraints.BOTH, 5, 5, 0, 0));
			}

			codespacePanel = new TitledPanel().build(content);
		}

		add(idAssignmentPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(codespacePanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		PopupMenuDecorator.getInstance().decorate(idPrefix, impIdCSUserText);

		ActionListener resourceIdListener = e -> setEnabledResourceId();
		ActionListener codeSpaceListener = e -> setEnabledCodeSpace();
		
		impIdRadioAdd.addActionListener(resourceIdListener);
		impIdRadioExchange.addActionListener(resourceIdListener);

		impIdCSRadioNone.addActionListener(codeSpaceListener);
		impIdCSRadioFile.addActionListener(codeSpaceListener);
		impIdCSRadioFilePath.addActionListener(codeSpaceListener);
		impIdCSRadioUser.addActionListener(codeSpaceListener);
	}
	
	private void setEnabledResourceId() {
		impIdCheckExtRef.setEnabled(impIdRadioExchange.isSelected());
	}

	private void setEnabledCodeSpace() {
		impIdCSUserText.setEnabled(impIdCSRadioUser.isSelected());
	}
	
	private void setEnabledCodespaceDialog(boolean enable) {
		impIdCSRadioNone.setEnabled(enable);
		impIdCSRadioFile.setEnabled(enable);
		impIdCSRadioFilePath.setEnabled(enable);
		impIdCSRadioUser.setEnabled(enable);
		impIdCSUserText.setEnabled(enable && impIdCSRadioUser.isSelected());
	}
	
	@Override
	public void doTranslation() {
		idAssignmentPanel.setTitle(Language.I18N.getString("pref.import.id.border.id"));
		idPrefixLabel.setText(Language.I18N.getString("pref.import.id.label.id.prefix"));
		impIdCheckExtRef.setText(Language.I18N.getString("pref.import.id.label.id.extReference"));
		impIdRadioAdd.setText(Language.I18N.getString("pref.import.id.label.id.add"));
		impIdRadioExchange.setText(Language.I18N.getString("pref.import.id.label.id.exchange"));

		codespacePanel.setTitle(Language.I18N.getString("pref.import.id.border.idCodeSpace"));
		impIdCSRadioNone.setText(Language.I18N.getString("pref.import.id.label.idCodeSpace.none"));
		impIdCSRadioFile.setText(Language.I18N.getString("pref.import.id.label.idCodeSpace.file"));
		impIdCSRadioFilePath.setText(Language.I18N.getString("pref.import.id.label.idCodeSpace.filePath"));
		impIdCSRadioUser.setText(Language.I18N.getString("pref.import.id.label.idCodeSpace.user"));
	}

	@Override
	public void loadSettings() {
		ImportResourceId resourceId = config.getImportConfig().getResourceId();

		if (resourceId.getIdPrefix() != null && resourceId.getIdPrefix().trim().length() != 0)
			idPrefix.setText(resourceId.getIdPrefix());
		else {
			idPrefix.setText(DefaultGMLIdManager.getInstance().getDefaultPrefix());
			resourceId.setIdPrefix(DefaultGMLIdManager.getInstance().getDefaultPrefix());
		}
		
		if (resourceId.isUUIDModeReplace())
			impIdRadioExchange.setSelected(true);
		else
			impIdRadioAdd.setSelected(true);

		impIdCheckExtRef.setSelected(resourceId.isSetKeepIdAsExternalReference());

		setEnabledResourceId();
		
		if (resourceId.isSetNoneCodeSpaceMode())
			impIdCSRadioNone.setSelected(true);
		else if (resourceId.isSetUserCodeSpaceMode())
			impIdCSRadioUser.setSelected(true);
		else if (resourceId.isSetRelativeCodeSpaceMode())
			impIdCSRadioFile.setSelected(true);
		else
			impIdCSRadioFilePath.setSelected(true);

		impIdCSUserText.setText(resourceId.getCodeSpace());
		
		setEnabledCodeSpace();
	}

	@Override
	public void setSettings() {
		ImportResourceId resourceId = config.getImportConfig().getResourceId();

		if (idPrefix.getText() != null && DefaultGMLIdManager.getInstance().isValidPrefix(idPrefix.getText()))
			resourceId.setIdPrefix(idPrefix.getText());
		else {
			resourceId.setIdPrefix(DefaultGMLIdManager.getInstance().getDefaultPrefix());
			idPrefix.setText(DefaultGMLIdManager.getInstance().getDefaultPrefix());
		}
		
		if (impIdRadioAdd.isSelected())
			resourceId.setUuidMode(UUIDMode.COMPLEMENT);
		else
			resourceId.setUuidMode(UUIDMode.REPLACE);

		resourceId.setKeepIdAsExternalReference(impIdCheckExtRef.isSelected());
		
		if (impIdCSRadioNone.isSelected())
			resourceId.setCodeSpaceMode(CodeSpaceMode.NONE);
		else if (impIdCSRadioFile.isSelected())
			resourceId.setCodeSpaceMode(CodeSpaceMode.RELATIVE);
		else if (impIdCSRadioFilePath.isSelected())
			resourceId.setCodeSpaceMode(CodeSpaceMode.ABSOLUTE);
		else
			resourceId.setCodeSpaceMode(CodeSpaceMode.USER);
		
		String codeSpace = impIdCSUserText.getText().trim();
		if (codeSpace.length() > 0)
			resourceId.setCodeSpace(codeSpace);
		else
			codeSpace = resourceId.getCodeSpace();
		
		impIdCSUserText.setText(codeSpace);
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.id");
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		boolean showCodespaceDialog = true;
		if (((DatabaseConnectionStateEvent) event).isConnected()) {
			AbstractDatabaseAdapter databaseAdapter = ObjectRegistry.getInstance().getDatabaseController().getActiveDatabaseAdapter();
			DatabaseVersion version = databaseAdapter.getConnectionMetaData().getCityDBVersion();
			showCodespaceDialog = version.compareTo(3, 1, 0) >= 0;
		}
		
		setEnabledCodespaceDialog(showCodespaceDialog);
	}

}

