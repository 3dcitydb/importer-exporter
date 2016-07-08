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
package org.citydb.modules.citygml.exporter.gui.preferences;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.exporter.ExportFilterConfig;
import org.citydb.config.project.filter.FilterBoundingBoxMode;
import org.citydb.config.project.filter.TileNameSuffixMode;
import org.citydb.config.project.filter.TileSuffixMode;
import org.citydb.config.project.filter.TiledBoundingBox;
import org.citydb.config.project.filter.Tiling;
import org.citydb.config.project.filter.TilingMode;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class BoundingBoxPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JPanel block2;
	private JPanel block3;
	private JPanel block4;

	private JCheckBox useTiling;
	private JRadioButton expBBRadioInside;
	private JRadioButton expBBRadioIntersect;
	private JLabel tilePathInfo;
	private JLabel tilePathSuffixLabel;
	private JComboBox<TileSuffixMode> tilePathSuffixComboBox;
	private JLabel tileNameSuffixLabel;
	private JComboBox<TileNameSuffixMode> tileNameSuffixComboBox;
	private JLabel rowsLabel;
	private JFormattedTextField rowsText;
	private JLabel columnsLabel;
	private JFormattedTextField columnsText;
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
		ExportFilterConfig filter = config.getProject().getExporter().getFilter();

		if (expBBRadioIntersect.isSelected() && !filter.getComplexFilter().getTiledBoundingBox().isSetOverlapMode()) return true;
		if (expBBRadioInside.isSelected() && !filter.getComplexFilter().getTiledBoundingBox().isSetContainMode()) return true;

		Tiling tiling = filter.getComplexFilter().getTiledBoundingBox().getTiling();
		try { rowsText.commitEdit(); } catch (ParseException e) { }
		try { columnsText.commitEdit(); } catch (ParseException e) { }
		if (((Number)rowsText.getValue()).intValue() != tiling.getRows()) return true;
		if (((Number)columnsText.getValue()).intValue() != tiling.getColumns()) return true;

		if ((tiling.getMode() != TilingMode.NO_TILING) != useTiling.isSelected()) return true;
		if (!tiling.getTilePath().equals(tilePathName.getText().trim())) return true;

		if ((TileSuffixMode)tilePathSuffixComboBox.getSelectedItem() != tiling.getTilePathSuffix()) return true;
		if ((TileNameSuffixMode)tileNameSuffixComboBox.getSelectedItem() != tiling.getTileNameSuffix()) return true;

		if (tiling.isIncludeTileAsGenericAttribute() != setGenAttr.isSelected()) return true;
		if ((TileSuffixMode)genAttrValueComboBox.getSelectedItem() != tiling.getGenericAttributeValue()) return true;
		
		return false;
	}

	private void initGui() {
		expBBRadioInside = new JRadioButton();
		expBBRadioIntersect = new JRadioButton();
		ButtonGroup expBBRadio = new ButtonGroup();
		expBBRadio.add(expBBRadioInside);
		expBBRadio.add(expBBRadioIntersect);
		
		useTiling = new JCheckBox();
		rowsLabel = new JLabel();
		columnsLabel = new JLabel();

		DecimalFormat tileFormat = new DecimalFormat("#######");	
		tileFormat.setMaximumIntegerDigits(7);
		tileFormat.setMinimumIntegerDigits(1);
		rowsText = new JFormattedTextField(tileFormat);
		columnsText = new JFormattedTextField(tileFormat);
		
		tilePathInfo = new JLabel();
		tilePathSuffixLabel = new JLabel();
		tilePathSuffixComboBox = new JComboBox<TileSuffixMode>();
		tileNameSuffixLabel = new JLabel();
		tileNameSuffixComboBox = new JComboBox<TileNameSuffixMode>();
		tilePathNameLabel = new JLabel();
		tilePathName = new JTextField();
		
		setGenAttr = new JCheckBox();
		genAttrNameLabel = new JLabel();
		genAttrNameText = new JTextField("TILE");
		genAttrNameText.setEditable(false);
		genAttrValueLabel = new JLabel();
		genAttrValueComboBox = new JComboBox<TileSuffixMode>();
		
		PopupMenuDecorator.getInstance().decorate(rowsText, columnsText, tilePathName, genAttrNameText);
		
		rowsText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(rowsText);
			}
		});
		
		columnsText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegative(columnsText);
			}
		});
		
		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			expBBRadioInside.setIconTextGap(10);
			expBBRadioIntersect.setIconTextGap(10);
			{
				block1.add(expBBRadioIntersect, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(expBBRadioInside, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}

			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			{
				Box rowsColumnsBox = Box.createHorizontalBox();
				rowsColumnsBox.add(rowsLabel);
				rowsColumnsBox.add(Box.createRigidArea(new Dimension(10, 0)));
				rowsColumnsBox.add(rowsText);
				rowsColumnsBox.add(Box.createRigidArea(new Dimension(15, 0)));
				rowsColumnsBox.add(columnsLabel);
				rowsColumnsBox.add(Box.createRigidArea(new Dimension(10, 0)));
				rowsColumnsBox.add(columnsText);
				
				useTiling.setIconTextGap(10);
				int lmargin = (int)(useTiling.getPreferredSize().getWidth()) + 11;
				block2.add(useTiling, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(rowsColumnsBox, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
			}

			block3 = new JPanel();
			add(block3, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block3.setBorder(BorderFactory.createTitledBorder(""));
			block3.setLayout(new GridBagLayout());
			{
				tilePathInfo.setFont(tilePathInfo.getFont().deriveFont(Font.ITALIC));
				GridBagConstraints c = GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5);
				c.gridwidth = 2;
				
				block3.add(tilePathInfo, c);
				block3.add(tilePathNameLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block3.add(tilePathName, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block3.add(tilePathSuffixLabel, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block3.add(tilePathSuffixComboBox, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block3.add(tileNameSuffixLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block3.add(tileNameSuffixComboBox, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			}

			block4 = new JPanel();
			add(block4, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block4.setBorder(BorderFactory.createTitledBorder(""));
			block4.setLayout(new GridBagLayout());
			{	
				setGenAttr.setIconTextGap(10);
				int lmargin = (int)(setGenAttr.getPreferredSize().getWidth()) + 11;
				GridBagConstraints c = GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5);
				c.gridwidth = 2;

				block4.add(setGenAttr, c);
				block4.add(genAttrNameLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				block4.add(genAttrNameText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block4.add(genAttrValueLabel, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));				
				block4.add(genAttrValueComboBox, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));				
			}
		}

		ActionListener tilingListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledTiling();
			}
		};
		
		ActionListener genAttrListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledGenAttr();
			}
		};
		
		for (TileSuffixMode mode : TileSuffixMode.values())
			tilePathSuffixComboBox.addItem(mode);
		
		for (TileNameSuffixMode mode : TileNameSuffixMode.values())
			tileNameSuffixComboBox.addItem(mode);
		
		for (TileSuffixMode mode : TileSuffixMode.values())
			genAttrValueComboBox.addItem(mode);

		useTiling.addActionListener(tilingListener);
		setGenAttr.addActionListener(genAttrListener);
	}
	
	private void checkNonNegative(JFormattedTextField field) {
		if (field.getValue() != null && ((Number)field.getValue()).intValue() < 0)
			field.setValue(0);
	}

	private void setEnabledTiling() {
		((TitledBorder)block1.getBorder()).setTitleColor(!useTiling.isSelected() ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		block1.repaint();
		
		((TitledBorder)block3.getBorder()).setTitleColor(useTiling.isSelected() ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		block3.repaint();
		
		((TitledBorder)block4.getBorder()).setTitleColor(useTiling.isSelected() ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		block4.repaint();
		
		expBBRadioInside.setEnabled(!useTiling.isSelected());
		expBBRadioIntersect.setEnabled(!useTiling.isSelected());
		
		rowsLabel.setEnabled(useTiling.isSelected());
		rowsText.setEnabled(useTiling.isSelected());
		columnsLabel.setEnabled(useTiling.isSelected());
		columnsText.setEnabled(useTiling.isSelected());

		tilePathInfo.setEnabled(useTiling.isSelected());
		tilePathNameLabel.setEnabled(useTiling.isSelected());
		tilePathName.setEnabled(useTiling.isSelected());
		tilePathSuffixLabel.setEnabled(useTiling.isSelected());
		tilePathSuffixComboBox.setEnabled(useTiling.isSelected());
		tileNameSuffixLabel.setEnabled(useTiling.isSelected());
		tileNameSuffixComboBox.setEnabled(useTiling.isSelected());
		
		setGenAttr.setEnabled(useTiling.isSelected());
		boolean enable = useTiling.isSelected() && setGenAttr.isSelected();
		genAttrNameLabel.setEnabled(enable);
		genAttrNameText.setEnabled(enable);
		genAttrValueLabel.setEnabled(enable);
		genAttrValueComboBox.setEnabled(enable);
	}
	
	private void setEnabledGenAttr() {
		genAttrNameLabel.setEnabled(setGenAttr.isSelected());
		genAttrNameText.setEnabled(setGenAttr.isSelected());
		genAttrValueLabel.setEnabled(setGenAttr.isSelected());
		genAttrValueComboBox.setEnabled(setGenAttr.isSelected());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("common.pref.boundingBox.border.selection"));
		((TitledBorder)block2.getBorder()).setTitle(Language.I18N.getString("pref.export.boundingBox.border.tiling"));
		((TitledBorder)block3.getBorder()).setTitle(Language.I18N.getString("pref.export.boundingBox.border.path"));
		((TitledBorder)block4.getBorder()).setTitle(Language.I18N.getString("pref.export.boundingBox.border.options"));
		
		expBBRadioInside.setText(Language.I18N.getString("common.pref.boundingBox.label.inside"));
		expBBRadioIntersect.setText(Language.I18N.getString("common.pref.boundingBox.label.overlap"));

		useTiling.setText(Language.I18N.getString("pref.export.boundingBox.label.useTiling"));
		rowsLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.rows"));
		columnsLabel.setText(Language.I18N.getString("pref.export.boundingBox.label.columns"));
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
		TiledBoundingBox bbox = config.getProject().getExporter().getFilter().getComplexFilter().getTiledBoundingBox();

		if (bbox.isSetOverlapMode())
			expBBRadioIntersect.setSelected(true);
		else
			expBBRadioInside.setSelected(true);

		Tiling tiling = bbox.getTiling();
		useTiling.setSelected(tiling.getMode() != TilingMode.NO_TILING);
		rowsText.setValue(tiling.getRows());
		columnsText.setValue(tiling.getColumns());
		tilePathName.setText(tiling.getTilePath());
		tilePathSuffixComboBox.setSelectedItem(tiling.getTilePathSuffix());
		tileNameSuffixComboBox.setSelectedItem(tiling.getTileNameSuffix());		
		setGenAttr.setSelected(tiling.isIncludeTileAsGenericAttribute());
		genAttrValueComboBox.setSelectedItem(tiling.getGenericAttributeValue());
		
		setEnabledTiling();
	}

	@Override
	public void setSettings() {
		TiledBoundingBox bbox = config.getProject().getExporter().getFilter().getComplexFilter().getTiledBoundingBox();

		bbox.setMode(expBBRadioInside.isSelected() ? FilterBoundingBoxMode.CONTAIN : FilterBoundingBoxMode.OVERLAP);

		Tiling tiling = bbox.getTiling();
		tiling.setMode(useTiling.isSelected() ? TilingMode.MANUAL : TilingMode.NO_TILING);
		
		if (tilePathName.getText() == null || tilePathName.getText().trim().length() == 0)
			tilePathName.setText("tile"); 
		tiling.setTilePath(tilePathName.getText());

		tiling.setTilePathSuffix((TileSuffixMode)tilePathSuffixComboBox.getSelectedItem());
		tiling.setTileNameSuffix((TileNameSuffixMode)tileNameSuffixComboBox.getSelectedItem());
		tiling.setIncludeTileAsGenericAttribute(setGenAttr.isSelected());
		tiling.setGenericAttributeValue((TileSuffixMode)genAttrValueComboBox.getSelectedItem());
		
		tiling.setRows(((Number)rowsText.getValue()).intValue());
		tiling.setColumns(((Number)columnsText.getValue()).intValue());
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.export.boundingBox");
	}

}
