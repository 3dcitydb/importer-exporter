/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.modules.citygml.exporter.gui.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.lod.LodFilterMode;
import org.citydb.config.project.query.filter.lod.LodSearchMode;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilterMode;
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.event.global.PropertyChangeEvent;
import org.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import org.citydb.gui.components.feature.FeatureTypeTree;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
import org.citygml4j.model.module.citygml.CityGMLVersion;

@SuppressWarnings("serial")
public class FilterPanel extends JPanel implements EventHandler {
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
	private FeatureTypeTree typeTree;

	private BoundingBoxPanel bboxPanel;

	private JLabel gmlNameLabel;
	private JLabel gmlIdLabel;
	private JLabel coStartLabel;
	private JLabel coEndLabel;
	private JPanel lodPanel;
	private JPanel row3col2;
	private JPanel row4col2;

	private JCheckBox lodFilter;
	private JCheckBox[] lods;
	private JLabel lodModeLabel;
	private JComboBox<LodFilterMode> lodMode;
	private JLabel lodDepthLabel;
	private JSpinner lodDepth;

	public FilterPanel(ViewController viewController, Config config) {
		this.config = config;

		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.PROPERTY_CHANGE_EVENT, this);		
		initGui(viewController);
	}

	private void initGui(ViewController viewController) {
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
		bboxPanel = viewController.getComponentFactory().createBoundingBoxPanel();

		lodFilter = new JCheckBox(); 
		lodModeLabel = new JLabel();
		lodDepthLabel = new JLabel();

		String[] values = new String[101];
		values[0] = "*";
		IntStream.range(0, 100).forEach(i -> values[i + 1] = String.valueOf(i));		
		lodDepth = new JSpinner(new SpinnerListModel(values));		

		lodMode = new JComboBox<>();
		for (LodFilterMode mode : LodFilterMode.values())
			lodMode.addItem(mode);

		PopupMenuDecorator.getInstance().decorate(gmlNameText, gmlIdText, coStartText, coEndText);

		featureClassFilter = new JCheckBox();
		typeTree = new FeatureTypeTree(Util.toCityGMLVersion(config.getProject().getExporter().getQuery().getVersion()));
		typeTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 4);		
		typeTree.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), 
				BorderFactory.createEmptyBorder(0, 0, 4, 4)));

		// get rid of standard icons
		DefaultCheckboxTreeCellRenderer renderer = (DefaultCheckboxTreeCellRenderer)typeTree.getCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);

		//layout
		setLayout(new GridBagLayout());
		{
			JPanel lodRow = new JPanel();
			add(lodRow, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
			lodRow.setLayout(new GridBagLayout());
			{
				lodPanel = new JPanel();		

				lodRow.add(lodFilter, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,5,0,5,5));
				lodRow.add(lodPanel, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,5,0,5,0));

				lodPanel.setBorder(BorderFactory.createTitledBorder(""));
				lodPanel.setLayout(new GridBagLayout());
				{
					lods = new JCheckBox[5];
					for (int lod = 0; lod < lods.length; lod++) {
						lods[lod] = new JCheckBox("LoD" + lod);
						lods[lod].setIconTextGap(10);
						lodPanel.add(lods[lod], GuiUtil.setConstraints(lod,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,10));
					}

					lodPanel.add(lodModeLabel, GuiUtil.setConstraints(5,0,0.0,0.0,GridBagConstraints.NONE,0,20,5,5));
					lodPanel.add(lodMode, GuiUtil.setConstraints(6,0,0.5,1.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
					lodPanel.add(lodDepthLabel, GuiUtil.setConstraints(7,0,0.0,0.0,GridBagConstraints.NONE,0,20,5,5));
					lodPanel.add(lodDepth, GuiUtil.setConstraints(8,0,0.5,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
				}
			}
		}

		{
			JPanel row3 = new JPanel();
			add(row3, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
			row3.setLayout(new GridBagLayout());
			{
				GridBagConstraints c = GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,5,0,5,5);
				c.anchor = GridBagConstraints.NORTH;
				row3.add(gmlIdFilter, c);
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
			add(row4, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
			row4.setLayout(new GridBagLayout());
			{
				GridBagConstraints c = GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,5,0,5,5);
				c.anchor = GridBagConstraints.NORTH;
				row4.add(complexFilter, c);
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
						panel8.add(typeTree, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,5));	
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

		lodFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledLodFilter();
			}
		});

		for (int lod = 0; lod < lods.length; lod++) {
			lods[lod].addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					setEnabledLodFilterMode();
				}
			});
		}

		PopupMenuDecorator.getInstance().decorate(typeTree);
		PopupMenuDecorator.getInstance().decorateCheckBoxGroup(lods);
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
		typeTree.setPathsEnabled(complexFilter.isSelected() && featureClassFilter.isSelected());
		typeTree.repaint();

		setEnabledLodFilter();
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

	private void setEnabledLodFilter() {
		((TitledBorder)lodPanel.getBorder()).setTitleColor(lodFilter.isSelected() ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		lodPanel.repaint();

		for (int lod = 0; lod < lods.length; lod++)
			lods[lod].setEnabled(lodFilter.isSelected());

		if (lodFilter.isSelected())
			setEnabledLodFilterMode();
		else {
			lodModeLabel.setEnabled(false);
			lodMode.setEnabled(false);
		}

		lodDepthLabel.setEnabled(lodFilter.isSelected());
		lodDepth.setEnabled(lodFilter.isSelected());
	}

	private void setEnabledLodFilterMode() {
		int selected = 0;
		for (int lod = 0; lod < lods.length; lod++) {
			if (lods[lod].isSelected())
				selected++;
		}

		lodModeLabel.setEnabled(selected > 1);
		lodMode.setEnabled(selected > 1);
	}

	private void setEnabledClassFilter() {
		typeTree.setPathsEnabled(featureClassFilter.isSelected());
		typeTree.repaint();
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
		((TitledBorder)lodPanel.getBorder()).setTitle(Language.I18N.getString("filter.border.lod"));
		lodModeLabel.setText(Language.I18N.getString("filter.label.lod.mode"));
		lodDepthLabel.setText(Language.I18N.getString("filter.label.lod.depth"));
	}

	public void loadSettings() {
		SimpleQuery query = config.getProject().getExporter().getQuery();

		if (query.getMode() == SimpleSelectionFilterMode.COMPLEX)
			complexFilter.setSelected(true);
		else
			gmlIdFilter.setSelected(true);

		featureClassFilter.setSelected(query.isUseTypeNames());
		lodFilter.setSelected(query.isUseLodFilter());
		cityObjectFilter.setSelected(query.isUseCountFilter());
		gmlNameFilter.setSelected(query.isUseGmlNameFilter());
		boundingBoxFilter.setSelected(query.isUseBboxFilter());

		// feature type filter
		FeatureTypeFilter featureTypeFilter = query.getFeatureTypeFilter();
		typeTree.getCheckingModel().clearChecking();
		typeTree.setSelected(featureTypeFilter.getTypeNames());

		// lod filter
		LodFilter lodFilter = query.getLodFilter();
		lodMode.setSelectedItem(lodFilter.getMode());
		for (int lod = 0; lod < lods.length; lod++)
			lods[lod].setSelected(lodFilter.isSetLod(lod));

		if (lodFilter.getSearchMode() == LodSearchMode.ALL)
			lodDepth.setValue("*");
		else {
			int searchDepth = lodFilter.getSearchDepth();
			lodDepth.setValue(searchDepth >= 0 && searchDepth < 100 ? String.valueOf(searchDepth) : "1");
		}
		
		// gml:id filter
		ResourceIdOperator gmlIdFilter = query.getFilter().getGmlIdFilter();
		gmlIdText.setText(String.join(",", gmlIdFilter.getResourceIds()));

		// gml:name
		LikeOperator gmlNameFilter = query.getFilter().getGmlNameFilter();
		gmlNameText.setText(gmlNameFilter.getLiteral());

		// counter filter
		CounterFilter counterFilter = query.getCounterFilter();
		coStartText.setValue(counterFilter.getLowerLimit());
		coEndText.setValue(counterFilter.getUpperLimit());

		// bbox filter
		BBOXOperator bboxFilter = query.getFilter().getBboxFilter();
		BoundingBox bbox = bboxFilter.getEnvelope();
		if (bbox != null)
			bboxPanel.setBoundingBox((BoundingBox)bboxFilter.getEnvelope());

		setEnabledLodFilterMode();
		setEnabledFilterSettings();
	}

	public void setSettings() {
		SimpleQuery query = config.getProject().getExporter().getQuery();
		query.setMode(complexFilter.isSelected() ? SimpleSelectionFilterMode.COMPLEX : SimpleSelectionFilterMode.SIMPLE);
		query.setUseTypeNames(featureClassFilter.isSelected());
		query.setUseLodFilter(lodFilter.isSelected());
		query.setUseCountFilter(cityObjectFilter.isSelected());
		query.setUseGmlNameFilter(gmlNameFilter.isSelected());
		query.setUseBboxFilter(boundingBoxFilter.isSelected());

		// feature type filter
		FeatureTypeFilter featureTypeFilter = query.getFeatureTypeFilter();
		featureTypeFilter.reset();
		featureTypeFilter.setTypeNames(typeTree.getSelectedTypeNames());

		// lod filter
		LodFilter lodFilter = query.getLodFilter();
		lodFilter.setMode(lodMode.getItemAt(lodMode.getSelectedIndex()));
		for (int lod = 0; lod < lods.length; lod++)
			lodFilter.setLod(lod, lods[lod].isSelected());

		String searchDepth = lodDepth.getValue().toString();
		if (searchDepth.equals("*")) {
			lodFilter.setSearchMode(LodSearchMode.ALL);
			lodFilter.unsetSearchDepth();
		} else {
			lodFilter.setSearchMode(LodSearchMode.DEPTH);
			try {
				lodFilter.setSearchDepth(Integer.parseInt(searchDepth));
			} catch (NumberFormatException e) {
				lodFilter.setSearchDepth(1);
			}
		}
		
		// gml:id filter
		ResourceIdOperator gmlIdFilter = query.getFilter().getGmlIdFilter();
		gmlIdFilter.reset();
		if (gmlIdText.getText().trim().length() > 0) {
			String trimmed = gmlIdText.getText().replaceAll("\\s+", "");
			gmlIdFilter.setResourceIds(Util.string2string(trimmed, ","));
		}

		// gml:name
		LikeOperator gmlNameFilter = query.getFilter().getGmlNameFilter();
		gmlNameFilter.reset();
		if (gmlNameText.getText().trim().length() > 0)
			gmlNameFilter.setLiteral(gmlNameText.getText().trim());

		// counter filter
		CounterFilter counterFilter = query.getCounterFilter();
		counterFilter.reset();
		if (coStartText.isEditValid() && coStartText.getValue() != null
				&& coEndText.isEditValid() && coEndText.getValue() != null) {
			counterFilter.setLowerLimit(((Number)coStartText.getValue()).longValue());
			counterFilter.setUpperLimit(((Number)coEndText.getValue()).longValue());
		} else {
			counterFilter.setLowerLimit(null);
			counterFilter.setUpperLimit(null);
		}

		// bbox filter
		query.getFilter().getBboxFilter().setEnvelope(bboxPanel.getBoundingBox());
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		PropertyChangeEvent e = (PropertyChangeEvent)event;
		if (e.getPropertyName().equals("citygml.version"))
			typeTree.updateCityGMLVersion((CityGMLVersion)e.getNewValue(), featureClassFilter.isSelected());
	}

}
