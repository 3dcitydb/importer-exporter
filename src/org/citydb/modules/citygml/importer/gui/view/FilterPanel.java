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
package org.citydb.modules.citygml.importer.gui.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.filter.AbstractFilterConfig;
import org.citydb.config.project.filter.FeatureClass;
import org.citydb.config.project.filter.FeatureCount;
import org.citydb.config.project.filter.FilterBoundingBox;
import org.citydb.config.project.filter.FilterMode;
import org.citydb.config.project.filter.GmlName;
import org.citydb.config.project.general.FeatureClassMode;
import org.citydb.gui.components.bbox.BoundingBoxPanelImpl;
import org.citydb.gui.components.checkboxtree.CheckboxTree;
import org.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import org.citydb.gui.components.checkboxtree.DefaultTreeCheckingModel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.util.Util;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class FilterPanel extends JPanel {
	private final Config config;
	
	private JCheckBox gmlNameFilter;
	private JRadioButton gmlIdFilter;
	private JRadioButton complexFilter;
	private JTextField gmlNameText;
	private JTextField gmlIdText;
	private JCheckBox cityObjectFilter;
	private JCheckBox boundingBoxFilter;
	private JCheckBox featureClassFilter;
	private JFormattedTextField coStartText;
	private JFormattedTextField coEndText;
	private CheckboxTree fcTree;

	private BoundingBoxPanelImpl bboxPanel;
	
	private JLabel gmlNameLabel;
	private JLabel gmlIdLabel;
	private JLabel coStartLabel;
	private JLabel coEndLabel;
	private JPanel row3col2;
	private JPanel row4col2;

	private DefaultMutableTreeNode cityObject;
	private DefaultMutableTreeNode building;
	private DefaultMutableTreeNode bridge;
	private DefaultMutableTreeNode tunnel;
	private DefaultMutableTreeNode water;
	private DefaultMutableTreeNode landuse;
	private DefaultMutableTreeNode vegetation;
	private DefaultMutableTreeNode transportation;
	private DefaultMutableTreeNode reliefFeature;
	private DefaultMutableTreeNode cityfurniture;
	private DefaultMutableTreeNode genericCityObject;
	private DefaultMutableTreeNode cityObjectGroup;

	public FilterPanel(Config config) {
		this.config = config;

		initGui();
	}

	private void initGui() {
		gmlIdFilter = new JRadioButton();
		complexFilter = new JRadioButton();
		ButtonGroup filterRadio = new ButtonGroup();
		filterRadio.add(gmlIdFilter);
		filterRadio.add(complexFilter);

		gmlIdLabel = new JLabel();
		gmlIdText = new JTextField();

		gmlNameFilter = new JCheckBox();
		gmlNameLabel = new JLabel();
		gmlNameText = new JTextField();

		cityObjectFilter = new JCheckBox();
		coStartLabel = new JLabel();
		coEndLabel = new JLabel();

		DecimalFormat counterFormat = new DecimalFormat("###################");
		counterFormat.setMaximumIntegerDigits(19);
		coStartText = new JFormattedTextField(counterFormat);
		coEndText = new JFormattedTextField(counterFormat);

		coStartText.setFocusLostBehavior(JFormattedTextField.COMMIT);
		coEndText.setFocusLostBehavior(JFormattedTextField.COMMIT);

		boundingBoxFilter = new JCheckBox();
		bboxPanel = new BoundingBoxPanelImpl(config);
		
		PopupMenuDecorator.getInstance().decorate(gmlNameText, gmlIdText, coStartText, coEndText);
		
		featureClassFilter = new JCheckBox();		
		cityObject = new DefaultMutableTreeNode(FeatureClassMode.CITYOBJECT);
		building = new DefaultMutableTreeNode(FeatureClassMode.BUILDING);
		bridge = new DefaultMutableTreeNode(FeatureClassMode.BRIDGE);
		tunnel = new DefaultMutableTreeNode(FeatureClassMode.TUNNEL);
		water = new DefaultMutableTreeNode(FeatureClassMode.WATERBODY);
		landuse = new DefaultMutableTreeNode(FeatureClassMode.LANDUSE);
		vegetation = new DefaultMutableTreeNode(FeatureClassMode.VEGETATION);
		transportation = new DefaultMutableTreeNode(FeatureClassMode.TRANSPORTATION);
		reliefFeature = new DefaultMutableTreeNode(FeatureClassMode.RELIEFFEATURE);
		cityfurniture = new DefaultMutableTreeNode(FeatureClassMode.CITYFURNITURE);
		genericCityObject = new DefaultMutableTreeNode(FeatureClassMode.GENERICCITYOBJECT);
		cityObjectGroup = new DefaultMutableTreeNode(FeatureClassMode.CITYOBJECTGROUP);

		cityObject.add(bridge);
		cityObject.add(building);
		cityObject.add(cityfurniture);
		cityObject.add(cityObjectGroup);
		cityObject.add(genericCityObject);
		cityObject.add(landuse);
		cityObject.add(reliefFeature);
		cityObject.add(transportation);
		cityObject.add(tunnel);
		cityObject.add(vegetation);
		cityObject.add(water);

		fcTree = new CheckboxTree(cityObject);
		fcTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 4);		
		fcTree.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), 
				BorderFactory.createEmptyBorder(0, 0, 4, 4)));
		
		// get rid of standard icons
		DefaultCheckboxTreeCellRenderer renderer = (DefaultCheckboxTreeCellRenderer)fcTree.getCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);

		//layout
		setLayout(new GridBagLayout());
		{
			JPanel row3 = new JPanel();
			add(row3, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
			row3.setLayout(new GridBagLayout());
			{
				row3.add(gmlIdFilter, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,5,0,5,5));
				row3col2 = new JPanel();
				row3.add(row3col2, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,5,0,5,0));
				row3col2.setBorder(BorderFactory.createTitledBorder(""));
				row3col2.setLayout(new GridBagLayout());
				{
					row3col2.add(gmlIdLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
					row3col2.add(gmlIdText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
				}
			}

		}
		{
			JPanel row4 = new JPanel();
			add(row4, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
			row4.setLayout(new GridBagLayout());
			{
				row4.add(complexFilter, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,5,0,5,5));
				row4col2 = new JPanel();

				row4.add(row4col2, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,5,0,5,0));
				row4col2.setBorder(BorderFactory.createTitledBorder(""));
				row4col2.setLayout(new GridBagLayout());
				{
					// gml:name filter
					gmlNameFilter.setIconTextGap(10);
					row4col2.add(gmlNameFilter, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
					int lmargin = (int)(gmlNameFilter.getPreferredSize().getWidth()) + 11;

					// content
					JPanel panel4 = new JPanel();
					row4col2.add(panel4, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,0,lmargin,0,0));
					panel4.setLayout(new GridBagLayout());
					{
						panel4.add(gmlNameLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,0,0,5));
						panel4.add(gmlNameText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));						
					}

					// CityObject
					// checkbox
					cityObjectFilter.setIconTextGap(10);					
					row4col2.add(cityObjectFilter, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

					// content
					JPanel panel2 = new JPanel();
					row4col2.add(panel2, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,0,lmargin,0,0));
					panel2.setLayout(new GridBagLayout());
					{
						coStartText.setPreferredSize(coEndText.getPreferredSize());
						coEndText.setPreferredSize(coStartText.getPreferredSize());
						panel2.add(coStartLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,0,0,5));
						panel2.add(coStartText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
						panel2.add(coEndLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,0,5));
						panel2.add(coEndText, GuiUtil.setConstraints(3,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));		
					}

					// bounding box filter
					boundingBoxFilter.setIconTextGap(10);
					row4col2.add(boundingBoxFilter, GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));				
					row4col2.add(bboxPanel, GuiUtil.setConstraints(0,5,1.0,0.0,GridBagConstraints.HORIZONTAL,0,lmargin,0,5));

					// feature class filter
					featureClassFilter.setIconTextGap(10);
					row4col2.add(featureClassFilter, GuiUtil.setConstraints(0,6,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

					// content
					JPanel panel8 = new JPanel();
					row4col2.add(panel8, GuiUtil.setConstraints(0,7,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,0));
					panel8.setLayout(new GridBagLayout());
					{
						panel8.add(fcTree, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,5));	
					}
				}
			}
		}

		coStartText.addPropertyChangeListener(new PropertyChangeListener() {			
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
				if (coStartText.getValue() != null && ((Number)coStartText.getValue()).longValue() < 0)
					coStartText.setValue(null);
			}
		});

		coEndText.addPropertyChangeListener(new PropertyChangeListener() {			
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
				if (coEndText.getValue() != null && ((Number)coEndText.getValue()).longValue() < 0)
					coEndText.setValue(null);
			}
		});

		ActionListener filterSettingsListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledFilterSettings();
			}
		};

		ActionListener gmlNameListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledGmlNameFilter();
			}
		};

		ActionListener countListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledCountFilter();
			}
		};

		ActionListener bboxListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledBBox();				
			}
		};

		ActionListener classListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledClassFilter();
			}
		};

		gmlIdFilter.addActionListener(filterSettingsListener);
		complexFilter.addActionListener(filterSettingsListener);		
		gmlNameFilter.addActionListener(gmlNameListener);
		cityObjectFilter.addActionListener(countListener);
		boundingBoxFilter.addActionListener(bboxListener);
		featureClassFilter.addActionListener(classListener);
		
		PopupMenuDecorator.getInstance().decorate(fcTree);
	}

	private void setEnabledFilterSettings() {
		((TitledBorder)row3col2.getBorder()).setTitleColor(gmlIdFilter.isSelected() ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		row3col2.repaint();

		((TitledBorder)row4col2.getBorder()).setTitleColor(complexFilter.isSelected() ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		row4col2.repaint();

		gmlIdText.setEnabled(gmlIdFilter.isSelected());
		gmlIdLabel.setEnabled(gmlIdFilter.isSelected());

		gmlNameFilter.setEnabled(complexFilter.isSelected());
		boolean enable = complexFilter.isSelected() && gmlNameFilter.isSelected();
		gmlNameLabel.setEnabled(enable);
		gmlNameText.setEnabled(enable);

		cityObjectFilter.setEnabled(complexFilter.isSelected());
		enable = complexFilter.isSelected() && cityObjectFilter.isSelected();
		coStartLabel.setEnabled(enable);
		coEndLabel.setEnabled(enable);
		coStartText.setEnabled(enable);
		coEndText.setEnabled(enable);

		boundingBoxFilter.setEnabled(complexFilter.isSelected());
		enable = complexFilter.isSelected() && boundingBoxFilter.isSelected();
		bboxPanel.setEnabled(enable);

		featureClassFilter.setEnabled(complexFilter.isSelected());
		enable = complexFilter.isSelected() && featureClassFilter.isSelected();
		DefaultTreeCheckingModel model = (DefaultTreeCheckingModel)fcTree.getCheckingModel();
		model.setPathEnabled(new TreePath(cityObject), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, building}), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, bridge}), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, tunnel}), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, water}), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, landuse}), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, vegetation}), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, transportation}), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, reliefFeature}), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, cityfurniture}), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, genericCityObject}), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, cityObjectGroup}), enable);
		fcTree.repaint();
	}

	private void setEnabledGmlNameFilter() {
		gmlNameLabel.setEnabled(gmlNameFilter.isSelected());
		gmlNameText.setEnabled(gmlNameFilter.isSelected());
	}

	private void setEnabledCountFilter() {
		coStartLabel.setEnabled(cityObjectFilter.isSelected());
		coEndLabel.setEnabled(cityObjectFilter.isSelected());
		coStartText.setEnabled(cityObjectFilter.isSelected());
		coEndText.setEnabled(cityObjectFilter.isSelected());
	}

	private void setEnabledBBox() {
		bboxPanel.setEnabled(boundingBoxFilter.isSelected());
	}

	private void setEnabledClassFilter() {
		DefaultTreeCheckingModel model = (DefaultTreeCheckingModel)fcTree.getCheckingModel();
		model.setPathEnabled(new TreePath(cityObject), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, building}), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, bridge}), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, tunnel}), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, water}), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, landuse}), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, vegetation}), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, transportation}), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, reliefFeature}), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, cityfurniture}), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, genericCityObject}), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, cityObjectGroup}), featureClassFilter.isSelected());
		fcTree.repaint();
	}

	public void doTranslation() {
		gmlNameLabel.setText(Language.I18N.getString("filter.label.gmlName"));
		gmlIdLabel.setText(Language.I18N.getString("filter.label.gmlId"));
		coStartLabel.setText(Language.I18N.getString("filter.label.counter.start"));
		coEndLabel.setText(Language.I18N.getString("filter.label.counter.end"));	
		((TitledBorder)row3col2.getBorder()).setTitle(Language.I18N.getString("filter.border.gmlId"));
		((TitledBorder)row4col2.getBorder()).setTitle(Language.I18N.getString("filter.border.complexFilter"));
		cityObjectFilter.setText(Language.I18N.getString("filter.border.counter"));
		gmlNameFilter.setText(Language.I18N.getString("filter.border.gmlName"));
		boundingBoxFilter.setText(Language.I18N.getString("filter.border.boundingBox"));
		featureClassFilter.setText(Language.I18N.getString("filter.border.featureClass"));
	}

	public void loadSettings() {
		AbstractFilterConfig filterConfig = config.getProject().getImporter().getFilter();

		FilterBoundingBox bbox = filterConfig.getComplexFilter().getBoundingBox();
		GmlName gmlName = filterConfig.getComplexFilter().getGmlName();
		FeatureCount featureCount = filterConfig.getComplexFilter().getFeatureCount();
		FeatureClass featureClass = filterConfig.getComplexFilter().getFeatureClass();

		if (filterConfig.isSetComplexFilter())
			complexFilter.setSelected(true);
		else
			gmlIdFilter.setSelected(true);

		boundingBoxFilter.setSelected(bbox.isSet());
		gmlNameFilter.setSelected(gmlName.isSet());
		cityObjectFilter.setSelected(featureCount.isSet());
		featureClassFilter.setSelected(featureClass.isSet());

		gmlNameText.setText(gmlName.getValue());		
		gmlIdText.setText(Util.collection2string(filterConfig.getSimpleFilter().getGmlIdFilter().getGmlIds(), ","));

		coStartText.setValue(featureCount.getFrom());
		coEndText.setValue(featureCount.getTo());

		bboxPanel.setBoundingBox(bbox);
		
		if (!featureClass.isSetBuilding())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(building.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(building.getPath()));

		if (!featureClass.isSetBridge())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(bridge.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(bridge.getPath()));
		
		if (!featureClass.isSetTunnel())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(tunnel.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(tunnel.getPath()));

		if (!featureClass.isSetWaterBody())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(water.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(water.getPath()));

		if (!featureClass.isSetLandUse())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(landuse.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(landuse.getPath()));

		if (!featureClass.isSetVegetation())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(vegetation.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(vegetation.getPath()));

		if (!featureClass.isSetTransportation())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(transportation.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(transportation.getPath()));

		if (!featureClass.isSetReliefFeature())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(reliefFeature.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(reliefFeature.getPath()));

		if (!featureClass.isSetCityFurniture())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(cityfurniture.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(cityfurniture.getPath()));

		if (!featureClass.isSetGenericCityObject())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(genericCityObject.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(genericCityObject.getPath()));

		if (!featureClass.isSetCityObjectGroup())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(cityObjectGroup.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(cityObjectGroup.getPath()));

		setEnabledFilterSettings();
	}

	public void setSettings() {
		AbstractFilterConfig filterConfig = config.getProject().getImporter().getFilter();

		FilterBoundingBox bbox = filterConfig.getComplexFilter().getBoundingBox();
		GmlName gmlName = filterConfig.getComplexFilter().getGmlName();
		FeatureCount featureCount = filterConfig.getComplexFilter().getFeatureCount();
		FeatureClass featureClass = filterConfig.getComplexFilter().getFeatureClass();		

		filterConfig.setMode(complexFilter.isSelected() ? FilterMode.COMPLEX : FilterMode.SIMPLE);

		gmlName.setActive(gmlNameFilter.isSelected());
		featureCount.setActive(cityObjectFilter.isSelected());
		featureClass.setActive(featureClassFilter.isSelected());
		bbox.setActive(boundingBoxFilter.isSelected());

		gmlName.setValue(gmlNameText.getText());

		if (gmlIdText.getText() != null && gmlIdText.getText().trim().length() > 0) {
			String trimmed = gmlIdText.getText().replaceAll("\\s*", "");
			filterConfig.getSimpleFilter().getGmlIdFilter().setGmlIds(Util.string2string(trimmed, ","));
		} else
			filterConfig.getSimpleFilter().getGmlIdFilter().setGmlIds(new ArrayList<String>());

		if (coStartText.isEditValid() && coStartText.getValue() != null)
			featureCount.setFrom(((Number)coStartText.getValue()).longValue());
		else
			featureCount.setFrom(null);

		if (coEndText.isEditValid() && coEndText.getValue() != null)
			featureCount.setTo(((Number)coEndText.getValue()).longValue());
		else
			featureCount.setTo(null);

		bbox.copyFrom(bboxPanel.getBoundingBox());

		featureClass.setBuilding(!fcTree.getCheckingModel().isPathChecked(new TreePath(building.getPath()))); 
		featureClass.setBridge(!fcTree.getCheckingModel().isPathChecked(new TreePath(bridge.getPath()))); 
		featureClass.setTunnel(!fcTree.getCheckingModel().isPathChecked(new TreePath(tunnel.getPath()))); 
		featureClass.setWaterBody(!fcTree.getCheckingModel().isPathChecked(new TreePath(water.getPath())));
		featureClass.setLandUse(!fcTree.getCheckingModel().isPathChecked(new TreePath(landuse.getPath())));
		featureClass.setVegetation(!fcTree.getCheckingModel().isPathChecked(new TreePath(vegetation.getPath())));
		featureClass.setTransportation(!fcTree.getCheckingModel().isPathChecked(new TreePath(transportation.getPath())));
		featureClass.setReliefFeature(!fcTree.getCheckingModel().isPathChecked(new TreePath(reliefFeature.getPath())));	
		featureClass.setCityFurniture(!fcTree.getCheckingModel().isPathChecked(new TreePath(cityfurniture.getPath())));
		featureClass.setGenericCityObject(!fcTree.getCheckingModel().isPathChecked(new TreePath(genericCityObject.getPath())));
		featureClass.setCityObjectGroup(!fcTree.getCheckingModel().isPathChecked(new TreePath(cityObjectGroup.getPath())));
	}

}
