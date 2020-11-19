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
package org.citydb.gui.modules.exporter.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.SimpleTilingOptions;
import org.citydb.config.project.exporter.TileNameSuffixMode;
import org.citydb.config.project.exporter.TileSuffixMode;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class TilingOptionsPanel extends AbstractPreferencesComponent {
	private TitledPanel filePanel;
	private TitledPanel optionsPanel;

	private JLabel tilePathInfo;
	private JLabel tilePathSuffixLabel;
	private JComboBox<TileSuffixMode> tilePathSuffixComboBox;
	private JLabel tileNameSuffixLabel;
	private JComboBox<TileNameSuffixMode> tileNameSuffixComboBox;
	private JLabel tilePathNameLabel;
	private JTextField tilePathName;
	private JCheckBox setGenAttr;
	private JLabel genAttrNameLabel;
	private JTextField genAttrNameText;
	private JLabel genAttrValueLabel;
	private JComboBox<TileSuffixMode> genAttrValueComboBox;

	public TilingOptionsPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		SimpleTilingOptions tilingOptions = config.getExportConfig().getSimpleQuery().getBboxFilter().getTilingOptions();
		if (!tilingOptions.getTilePath().equals(tilePathName.getText().trim())) return true;
		if (tilePathSuffixComboBox.getSelectedItem() != tilingOptions.getTilePathSuffix()) return true;
		if (tileNameSuffixComboBox.getSelectedItem() != tilingOptions.getTileNameSuffix()) return true;
		if (tilingOptions.isIncludeTileAsGenericAttribute() != setGenAttr.isSelected()) return true;
		if (genAttrValueComboBox.getSelectedItem() != tilingOptions.getGenericAttributeValue()) return true;

		return false;
	}

	private void initGui() {
		tilePathInfo = new JLabel();
		tilePathSuffixLabel = new JLabel();
		tilePathSuffixComboBox = new JComboBox<>();
		tileNameSuffixLabel = new JLabel();
		tileNameSuffixComboBox = new JComboBox<>();
		tilePathNameLabel = new JLabel();
		tilePathName = new JTextField();

		setGenAttr = new JCheckBox();
		genAttrNameLabel = new JLabel();
		genAttrNameText = new JTextField("tile");
		genAttrNameText.setEditable(false);
		genAttrValueLabel = new JLabel();
		genAttrValueComboBox = new JComboBox<>();

		PopupMenuDecorator.getInstance().decorate(tilePathName, genAttrNameText);

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				tilePathInfo.setFont(tilePathInfo.getFont().deriveFont(Font.ITALIC));
				content.add(tilePathInfo, GuiUtil.setConstraints(0, 0, 2, 1, 0, 1, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(tilePathNameLabel, GuiUtil.setConstraints(0, 1, 0, 1, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(tilePathName, GuiUtil.setConstraints(1, 1, 1, 1, GridBagConstraints.BOTH, 0, 5, 5, 0));
				content.add(tilePathSuffixLabel, GuiUtil.setConstraints(0, 2, 0, 1, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(tilePathSuffixComboBox, GuiUtil.setConstraints(1, 2, 1, 1, GridBagConstraints.BOTH, 0, 5, 5, 0));
				content.add(tileNameSuffixLabel, GuiUtil.setConstraints(0, 3, 0, 1, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(tileNameSuffixComboBox, GuiUtil.setConstraints(1, 3, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			filePanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(genAttrNameLabel, GuiUtil.setConstraints(0, 0, 0, 1, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(genAttrNameText, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 5, 0));
				content.add(genAttrValueLabel, GuiUtil.setConstraints(0, 1, 0, 1, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(genAttrValueComboBox, GuiUtil.setConstraints(1, 1, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			optionsPanel = new TitledPanel()
					.withToggleButton(setGenAttr)
					.build(content);
		}

		add(filePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(optionsPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		for (TileSuffixMode mode : TileSuffixMode.values())
			tilePathSuffixComboBox.addItem(mode);

		for (TileNameSuffixMode mode : TileNameSuffixMode.values())
			tileNameSuffixComboBox.addItem(mode);

		for (TileSuffixMode mode : TileSuffixMode.values())
			genAttrValueComboBox.addItem(mode);

		setGenAttr.addActionListener(e -> setEnabledGenAttr());
	}

	private void setEnabledGenAttr() {
		genAttrNameLabel.setEnabled(setGenAttr.isSelected());
		genAttrNameText.setEnabled(setGenAttr.isSelected());
		genAttrValueLabel.setEnabled(setGenAttr.isSelected());
		genAttrValueComboBox.setEnabled(setGenAttr.isSelected());
	}

	@Override
	public void doTranslation() {
		filePanel.setTitle(Language.I18N.getString("pref.export.boundingBox.border.path"));
		optionsPanel.setTitle(Language.I18N.getString("pref.export.boundingBox.label.tile.genericAttr"));

		tilePathNameLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.pathName"));

		genAttrNameLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.genericAttrName"));
		genAttrValueLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.genericAttrValue"));

		tilePathInfo.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.pathInfo"));
		tilePathSuffixLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.pathSuffix"));
		tileNameSuffixLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.nameSuffix"));
	}

	@Override
	public void loadSettings() {
		SimpleTilingOptions tilingOptions = config.getExportConfig().getSimpleQuery().getBboxFilter().getTilingOptions();
		tilePathName.setText(tilingOptions.getTilePath());
		tilePathSuffixComboBox.setSelectedItem(tilingOptions.getTilePathSuffix());
		tileNameSuffixComboBox.setSelectedItem(tilingOptions.getTileNameSuffix());		
		setGenAttr.setSelected(tilingOptions.isIncludeTileAsGenericAttribute());
		genAttrValueComboBox.setSelectedItem(tilingOptions.getGenericAttributeValue());

		setEnabledGenAttr();
	}

	@Override
	public void setSettings() {
		if (tilePathName.getText() == null || tilePathName.getText().trim().length() == 0)
			tilePathName.setText("tile");

		// tiling options
		SimpleTilingOptions tilingOptions = config.getExportConfig().getSimpleQuery().getBboxFilter().getTilingOptions();
		tilingOptions.setTilePath(tilePathName.getText());
		tilingOptions.setTilePathSuffix((TileSuffixMode)tilePathSuffixComboBox.getSelectedItem());
		tilingOptions.setTileNameSuffix((TileNameSuffixMode)tileNameSuffixComboBox.getSelectedItem());
		tilingOptions.setIncludeTileAsGenericAttribute(setGenAttr.isSelected());
		tilingOptions.setGenericAttributeValue((TileSuffixMode)genAttrValueComboBox.getSelectedItem());
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.export.tiling");
	}

}
