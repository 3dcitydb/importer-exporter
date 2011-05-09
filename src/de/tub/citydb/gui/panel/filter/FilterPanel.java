/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.gui.panel.filter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.filter.AbstractFilterConfig;
import de.tub.citydb.config.project.filter.BoundingBox;
import de.tub.citydb.config.project.filter.FeatureClass;
import de.tub.citydb.config.project.filter.FeatureCount;
import de.tub.citydb.config.project.filter.FilterMode;
import de.tub.citydb.config.project.filter.GmlName;
import de.tub.citydb.config.project.general.FeatureClassMode;
import de.tub.citydb.gui.checkboxtree.CheckboxTree;
import de.tub.citydb.gui.checkboxtree.DefaultCheckboxTreeCellRenderer;
import de.tub.citydb.gui.checkboxtree.DefaultTreeCheckingModel;
import de.tub.citydb.gui.components.SrsComboBoxManager;
import de.tub.citydb.gui.components.SrsComboBoxManager.SrsComboBox;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.util.Util;

@SuppressWarnings("serial")
public class FilterPanel extends JPanel {
	private Config config;
	private FilterPanelType type;

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
	private JFormattedTextField bbXMinText;
	private JFormattedTextField bbYMinText;
	private JFormattedTextField bbXMaxText;
	private JFormattedTextField bbYMaxText;
	private CheckboxTree fcTree;

	private JLabel gmlNameLabel;
	private JLabel gmlIdLabel;
	private JLabel coStartLabel;
	private JLabel coEndLabel;
	private JLabel bbXMinLabel;
	private JLabel bbXMaxLabel;
	private JLabel bbYMinLabel;
	private JLabel bbYMaxLabel;
	private JPanel row3col2;
	private JPanel row4col2;

	private JLabel srsLabel;
	private SrsComboBox srsComboBox;

	private DefaultMutableTreeNode cityObject;
	private DefaultMutableTreeNode building;
	private DefaultMutableTreeNode water;
	private DefaultMutableTreeNode landuse;
	private DefaultMutableTreeNode vegetation;
	private DefaultMutableTreeNode transportation;
	private DefaultMutableTreeNode reliefFeature;
	private DefaultMutableTreeNode cityfurniture;
	private DefaultMutableTreeNode genericCityObject;
	private DefaultMutableTreeNode cityObjectGroup;

	public enum FilterPanelType {
		IMPORT,
		EXPORT
	}

	public FilterPanel(Config config, FilterPanelType type) {
		this.config = config;
		this.type = type;

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
		bbXMinLabel = new JLabel();
		bbXMaxLabel = new JLabel();
		bbYMinLabel = new JLabel();
		bbYMaxLabel = new JLabel();

		DecimalFormat bboxFormat = new DecimalFormat("##########.##############", DecimalFormatSymbols.getInstance(Locale.ENGLISH));	
		bbXMinText = new JFormattedTextField(bboxFormat);	
		bbYMinText = new JFormattedTextField(bboxFormat);
		bbXMaxText = new JFormattedTextField(bboxFormat);
		bbYMaxText = new JFormattedTextField(bboxFormat);

		bbXMinText.setFocusLostBehavior(JFormattedTextField.COMMIT);
		bbYMinText.setFocusLostBehavior(JFormattedTextField.COMMIT);
		bbXMaxText.setFocusLostBehavior(JFormattedTextField.COMMIT);
		bbYMaxText.setFocusLostBehavior(JFormattedTextField.COMMIT);

		srsLabel = new JLabel();
		srsComboBox = SrsComboBoxManager.getInstance(config).getSrsComboBox(true);

		GuiUtil.addStandardEditingPopupMenu(gmlNameText, gmlIdText, 
				coStartText, coEndText, bbXMinText, bbYMinText,  bbXMaxText, bbYMaxText);
		
		featureClassFilter = new JCheckBox();		

		cityObject = new DefaultMutableTreeNode(FeatureClassMode.CITYOBJECT);
		building = new DefaultMutableTreeNode(FeatureClassMode.BUILDING);
		water = new DefaultMutableTreeNode(FeatureClassMode.WATERBODY);
		landuse = new DefaultMutableTreeNode(FeatureClassMode.LANDUSE);
		vegetation = new DefaultMutableTreeNode(FeatureClassMode.VEGETATION);
		transportation = new DefaultMutableTreeNode(FeatureClassMode.TRANSPORTATION);
		reliefFeature = new DefaultMutableTreeNode(FeatureClassMode.RELIEFFEATURE);
		cityfurniture = new DefaultMutableTreeNode(FeatureClassMode.CITYFURNITURE);
		genericCityObject = new DefaultMutableTreeNode(FeatureClassMode.GENERICCITYOBJECT);
		cityObjectGroup = new DefaultMutableTreeNode(FeatureClassMode.CITYOBJECTGROUP);

		cityObject.add(building);
		cityObject.add(water);
		cityObject.add(landuse);
		cityObject.add(vegetation);
		cityObject.add(transportation);
		cityObject.add(reliefFeature);
		cityObject.add(cityfurniture);
		cityObject.add(genericCityObject);
		cityObject.add(cityObjectGroup);

		fcTree = new CheckboxTree(cityObject);
		fcTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 4);

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
			add(row4, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));
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
					JPanel bboxFilterPanel = new JPanel();
					row4col2.add(bboxFilterPanel, GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,1,5));
					bboxFilterPanel.setLayout(new GridBagLayout());
					{
						boundingBoxFilter.setIconTextGap(10);
						bboxFilterPanel.add(boundingBoxFilter, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,5));
						bboxFilterPanel.add(srsLabel, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,0,5,0,5));			
						bboxFilterPanel.add(srsComboBox, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,5,0,0));
					}

					// content
					JPanel panel6 = new JPanel();
					row4col2.add(panel6, GuiUtil.setConstraints(0,5,1.0,0.0,GridBagConstraints.HORIZONTAL,0,lmargin,0,0));
					panel6.setLayout(new GridBagLayout());
					{
						bbXMinText.setPreferredSize(bbXMaxText.getPreferredSize());
						bbXMaxText.setPreferredSize(bbXMinText.getPreferredSize());
						bbYMinText.setPreferredSize(bbYMaxText.getPreferredSize());
						bbYMaxText.setPreferredSize(bbYMinText.getPreferredSize());
						panel6.add(bbXMinLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,0,0,5));
						panel6.add(bbXMinText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
						panel6.add(bbXMaxLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,0,5));
						panel6.add(bbXMaxText, GuiUtil.setConstraints(3,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
						panel6.add(bbYMinLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.NONE,2,0,0,5));
						panel6.add(bbYMinText, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.HORIZONTAL,2,5,0,5));
						panel6.add(bbYMaxLabel, GuiUtil.setConstraints(2,1,0.0,0.0,GridBagConstraints.NONE,2,10,0,5));
						panel6.add(bbYMaxText, GuiUtil.setConstraints(3,1,1.0,0.0,GridBagConstraints.HORIZONTAL,2,5,0,5));
					}

					// feature class filter
					featureClassFilter.setIconTextGap(10);
					row4col2.add(featureClassFilter, GuiUtil.setConstraints(0,6,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

					// content
					JPanel panel8 = new JPanel();
					row4col2.add(panel8, GuiUtil.setConstraints(0,7,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,0));
					panel8.setLayout(new GridBagLayout());
					{
						JScrollPane scroll = new JScrollPane(fcTree);
						scroll.setBorder(BorderFactory.createEtchedBorder());
						panel8.add(scroll, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,5));	
					}
				}
			}
		}

		coStartText.addPropertyChangeListener(new PropertyChangeListener() {			
			public void propertyChange(PropertyChangeEvent evt) {
				if (coStartText.getValue() != null && ((Number)coStartText.getValue()).longValue() < 0)
					coStartText.setValue(null);
			}
		});

		coEndText.addPropertyChangeListener(new PropertyChangeListener() {			
			public void propertyChange(PropertyChangeEvent evt) {
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
		bbXMinLabel.setEnabled(enable);
		bbXMaxLabel.setEnabled(enable);
		bbYMinLabel.setEnabled(enable);
		bbYMaxLabel.setEnabled(enable);
		bbXMinText.setEnabled(enable);
		bbYMinText.setEnabled(enable);
		bbXMaxText.setEnabled(enable);
		bbYMaxText.setEnabled(enable);
		srsLabel.setEnabled(enable);
		srsComboBox.setEnabled(enable);

		featureClassFilter.setEnabled(complexFilter.isSelected());
		enable = complexFilter.isSelected() && featureClassFilter.isSelected();
		DefaultTreeCheckingModel model = (DefaultTreeCheckingModel)fcTree.getCheckingModel();
		model.setPathEnabled(new TreePath(cityObject), enable);
		model.setPathEnabled(new TreePath(new Object[]{cityObject, building}), enable);
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
		bbXMinLabel.setEnabled(boundingBoxFilter.isSelected());
		bbXMaxLabel.setEnabled(boundingBoxFilter.isSelected());
		bbYMinLabel.setEnabled(boundingBoxFilter.isSelected());
		bbYMaxLabel.setEnabled(boundingBoxFilter.isSelected());
		bbXMinText.setEnabled(boundingBoxFilter.isSelected());
		bbYMinText.setEnabled(boundingBoxFilter.isSelected());
		bbXMaxText.setEnabled(boundingBoxFilter.isSelected());
		bbYMaxText.setEnabled(boundingBoxFilter.isSelected());
		srsLabel.setEnabled(boundingBoxFilter.isSelected());
		srsComboBox.setEnabled(boundingBoxFilter.isSelected());
	}

	private void setEnabledClassFilter() {
		DefaultTreeCheckingModel model = (DefaultTreeCheckingModel)fcTree.getCheckingModel();
		model.setPathEnabled(new TreePath(cityObject), featureClassFilter.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, building}), featureClassFilter.isSelected());
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
		gmlNameLabel.setText(Internal.I18N.getString("filter.label.gmlName"));
		gmlIdLabel.setText(Internal.I18N.getString("filter.label.gmlId"));
		coStartLabel.setText(Internal.I18N.getString("filter.label.counter.start"));
		coEndLabel.setText(Internal.I18N.getString("filter.label.counter.end"));
		bbXMinLabel.setText(Internal.I18N.getString("filter.label.boundingBox.xMin"));
		bbXMaxLabel.setText(Internal.I18N.getString("filter.label.boundingBox.xMax"));
		bbYMinLabel.setText(Internal.I18N.getString("filter.label.boundingBox.yMin"));
		bbYMaxLabel.setText(Internal.I18N.getString("filter.label.boundingBox.yMax"));
		row3col2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("filter.border.gmlId")));
		row4col2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("filter.border.complexFilter")));
		cityObjectFilter.setText(Internal.I18N.getString("filter.border.counter"));
		gmlNameFilter.setText(Internal.I18N.getString("filter.border.gmlName"));
		boundingBoxFilter.setText(Internal.I18N.getString("filter.border.boundingBox"));
		featureClassFilter.setText(Internal.I18N.getString("filter.border.featureClass"));
		srsLabel.setText(Internal.I18N.getString("common.label.boundingBox.crs"));
		srsComboBox.doTranslation();
	}

	public void loadSettings() {
		AbstractFilterConfig filterConfig = type == FilterPanelType.IMPORT ? config.getProject().getImporter().getFilter() : 
			config.getProject().getExporter().getFilter();

		BoundingBox bbox = filterConfig.getComplexFilter().getBoundingBox();
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

		srsComboBox.updateContent();
		srsComboBox.setSelectedItem(bbox.getSRS());

		coStartText.setValue(featureCount.getFrom());
		coEndText.setValue(featureCount.getTo());

		bbXMinText.setValue(bbox.getLowerLeftCorner().getX());
		bbYMinText.setValue(bbox.getLowerLeftCorner().getY());
		bbXMaxText.setValue(bbox.getUpperRightCorner().getX());
		bbYMaxText.setValue(bbox.getUpperRightCorner().getY());

		if (!featureClass.isSetBuilding())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(building.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(building.getPath()));

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
		AbstractFilterConfig filterConfig = type == FilterPanelType.IMPORT ? config.getProject().getImporter().getFilter() :
			config.getProject().getExporter().getFilter();

		BoundingBox bbox = filterConfig.getComplexFilter().getBoundingBox();
		GmlName gmlName = filterConfig.getComplexFilter().getGmlName();
		FeatureCount featureCount = filterConfig.getComplexFilter().getFeatureCount();
		FeatureClass featureClass = filterConfig.getComplexFilter().getFeatureClass();		

		filterConfig.setMode(complexFilter.isSelected() ? FilterMode.COMPLEX : FilterMode.SIMPLE);

		gmlName.setActive(gmlNameFilter.isSelected());
		featureCount.setActive(cityObjectFilter.isSelected());
		featureClass.setActive(featureClassFilter.isSelected());
		bbox.setActive(boundingBoxFilter.isSelected());

		gmlName.setValue(gmlNameText.getText());
		bbox.setSRS(srsComboBox.getSelectedItem());

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

		if (bbXMinText.isEditValid() && bbXMinText.getValue() != null)
			bbox.getLowerLeftCorner().setX(((Number)bbXMinText.getValue()).doubleValue());
		else
			bbox.getLowerLeftCorner().setX(null);

		if (bbYMinText.isEditValid() && bbYMinText.getValue() != null)
			bbox.getLowerLeftCorner().setY(((Number)bbYMinText.getValue()).doubleValue());
		else
			bbox.getLowerLeftCorner().setY(null);

		if (bbXMaxText.isEditValid() && bbXMaxText.getValue() != null)
			bbox.getUpperRightCorner().setX(((Number)bbXMaxText.getValue()).doubleValue());
		else
			bbox.getUpperRightCorner().setX(null);

		if (bbYMaxText.isEditValid() && bbYMaxText.getValue() != null)
			bbox.getUpperRightCorner().setY(((Number)bbYMaxText.getValue()).doubleValue());
		else
			bbox.getUpperRightCorner().setY(null);

		featureClass.setBuilding(!fcTree.getCheckingModel().isPathChecked(new TreePath(building.getPath()))); 
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
