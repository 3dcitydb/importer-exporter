/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
package org.citydb.modules.citygml.exporter.gui.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.TileNameSuffixMode;
import org.citydb.config.project.exporter.TileSuffixMode;
import org.citydb.config.project.exporter.TilingOptions;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

@SuppressWarnings("serial")
public class BoundingBoxPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JPanel block2;

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

	public BoundingBoxPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		TilingOptions tilingOptions = config.getProject().getExporter().getSimpleQuery().getTilingOptions();
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
		genAttrNameText = new JTextField("TILE");
		genAttrNameText.setEditable(false);
		genAttrValueLabel = new JLabel();
		genAttrValueComboBox = new JComboBox<>();

		PopupMenuDecorator.getInstance().decorate(tilePathName, genAttrNameText);

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			{
				tilePathInfo.setFont(tilePathInfo.getFont().deriveFont(Font.ITALIC));
				GridBagConstraints c = GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5);
				c.gridwidth = 2;

				block1.add(tilePathInfo, c);
				block1.add(tilePathNameLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(tilePathName, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(tilePathSuffixLabel, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(tilePathSuffixComboBox, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(tileNameSuffixLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(tileNameSuffixComboBox, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			}

			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			{	
				setGenAttr.setIconTextGap(10);
				int lmargin = (int)(setGenAttr.getPreferredSize().getWidth()) + 11;
				GridBagConstraints c = GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5);
				c.gridwidth = 2;

				block2.add(setGenAttr, c);
				block2.add(genAttrNameLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				block2.add(genAttrNameText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block2.add(genAttrValueLabel, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				block2.add(genAttrValueComboBox, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			}
		}

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
		((TitledBorder) block1.getBorder()).setTitle(Language.I18N.getString("pref.export.boundingBox.border.path"));
		((TitledBorder) block2.getBorder()).setTitle(Language.I18N.getString("pref.export.boundingBox.border.options"));

		tilePathNameLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.pathName"));

		setGenAttr.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.genericAttr"));
		genAttrNameLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.genericAttrName"));
		genAttrValueLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.genericAttrValue"));

		tilePathInfo.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.pathInfo"));
		tilePathSuffixLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.pathSuffix"));
		tileNameSuffixLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.tile.nameSuffix"));
	}

	@Override
	public void loadSettings() {
		TilingOptions tilingOptions = config.getProject().getExporter().getSimpleQuery().getTilingOptions();
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
		TilingOptions tilingOptions = config.getProject().getExporter().getSimpleQuery().getTilingOptions();
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
