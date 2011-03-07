package de.tub.citydb.gui.panel.filter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.config.project.filter.FilterFeatureClass;
import de.tub.citydb.config.project.filter.FilterMode;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.checkboxtree.CheckboxTree;
import de.tub.citydb.gui.checkboxtree.DefaultCheckboxTreeCellRenderer;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.util.Util;

public class FilterPanel extends JPanel {
	private FilterConfig filter;

	//private JRadioButton noFilter;
	private JCheckBox gmlNameFilter;
	private JRadioButton gmlIdFilter;
	private JRadioButton complexFilter;
	private JTextField gmlNameText;
	private JTextField gmlIdText;
	private JCheckBox cityObjectFilter;
	private JCheckBox boundingBoxFilter;
	private JCheckBox featureClassFilter;
	private JTextField coStartText;
	private JTextField coEndText;
	private JTextField bbXMinText;
	private JTextField bbYMinText;
	private JTextField bbXMaxText;
	private JTextField bbYMaxText;
	private CheckboxTree fcTree;

	private JLabel row4col22col2_1;
	private JLabel row3col2_1;
	private JLabel row4col21row1_1;
	private JLabel row4col21row1_2;
	private JLabel row4col23row2_1;
	private JLabel row4col23row2_2;
	private JLabel row4col23row2_3;
	private JLabel row4col23row2_4;
	private JPanel row3col2;
	private JPanel row4col2;

	//internationalisierte Labels und Strings
	JLabel gmlNameLabel;
	JLabel gmlIdLabel;
	JLabel coStartLabel;
	JLabel coEndLabel;
	JLabel bbXMinLabel;
	JLabel bbYMinLabel;
	JLabel bbXMaxLabel;
	JLabel bbYMaxLabel;
	String gmlNameBorderString;
	String gmlIdBorderString;
	String complexFilterBorderString;
	String cityObjectsBorderString;
	String boundingBoxBorderString;
	String featureClassBorderString;

	DefaultMutableTreeNode cityObject;
	DefaultMutableTreeNode building;
	DefaultMutableTreeNode water;
	DefaultMutableTreeNode landuse;
	DefaultMutableTreeNode vegetation;
	DefaultMutableTreeNode transportation;
	DefaultMutableTreeNode reliefFeature;
	DefaultMutableTreeNode cityfurniture;
	DefaultMutableTreeNode genericCityObject;
	DefaultMutableTreeNode cityObjectGroup;

	public FilterPanel(FilterConfig inpFilter) {
		super();
		initGui();
		filter = inpFilter;
	}

	private void initGui() {

		//gui-elemente anlegen
		//noFilter = new JRadioButton("");
		gmlNameFilter = new JCheckBox("");
		gmlIdFilter = new JRadioButton("");
		complexFilter = new JRadioButton("");
		ButtonGroup filterRadio = new ButtonGroup();
		filterRadio.add(gmlIdFilter);
		filterRadio.add(complexFilter);
		complexFilter.setSelected(true);
		gmlNameText = new JTextField("");
		gmlIdText = new JTextField("");
		cityObjectFilter = new JCheckBox("");
		boundingBoxFilter = new JCheckBox("");
		featureClassFilter = new JCheckBox("");
		coStartText = new JTextField("");
		coEndText = new JTextField("");
		bbXMinText = new JTextField("");
		bbYMinText = new JTextField("");
		bbXMaxText = new JTextField("");
		bbYMaxText = new JTextField("");

		cityObject = new DefaultMutableTreeNode("CityObject");
		building = new DefaultMutableTreeNode("Building");
		water = new DefaultMutableTreeNode("WaterBody");
		landuse = new DefaultMutableTreeNode("LandUse");
		vegetation = new DefaultMutableTreeNode("Vegetation");
		transportation = new DefaultMutableTreeNode("Transportation");
		reliefFeature = new DefaultMutableTreeNode("ReliefFeature");
		cityfurniture = new DefaultMutableTreeNode("CityFurniture");
		genericCityObject = new DefaultMutableTreeNode("GenericCityObject");
		cityObjectGroup = new DefaultMutableTreeNode("CityObjectGroup");

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
				row3col2_1 = new JLabel();
				{
					row3col2.add(row3col2_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
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
						row4col22col2_1 = new JLabel();
						panel4.add(row4col22col2_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,0,0,5));
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
						row4col21row1_1 = new JLabel();
						row4col21row1_2 = new JLabel();
						coStartText.setPreferredSize(coEndText.getPreferredSize());
						coEndText.setPreferredSize(coStartText.getPreferredSize());
						panel2.add(row4col21row1_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,0,0,5));
						panel2.add(coStartText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
						panel2.add(row4col21row1_2, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,0,5));
						panel2.add(coEndText, GuiUtil.setConstraints(3,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));		
					}

					// bounding box filter
					boundingBoxFilter.setIconTextGap(10);
					row4col2.add(boundingBoxFilter, GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

					// content
					JPanel panel6 = new JPanel();
					row4col2.add(panel6, GuiUtil.setConstraints(0,5,1.0,0.0,GridBagConstraints.HORIZONTAL,0,lmargin,0,0));
					panel6.setLayout(new GridBagLayout());
					{
						row4col23row2_1 = new JLabel();
						row4col23row2_2 = new JLabel();
						row4col23row2_3 = new JLabel();
						row4col23row2_4 = new JLabel();
						bbXMinText.setPreferredSize(bbXMaxText.getPreferredSize());
						bbXMaxText.setPreferredSize(bbXMinText.getPreferredSize());
						bbYMinText.setPreferredSize(bbYMaxText.getPreferredSize());
						bbYMaxText.setPreferredSize(bbYMinText.getPreferredSize());
						panel6.add(row4col23row2_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,0,0,5));
						panel6.add(bbXMinText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
						panel6.add(row4col23row2_2, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,0,5));
						panel6.add(bbXMaxText, GuiUtil.setConstraints(3,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
						panel6.add(row4col23row2_3, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.NONE,2,0,0,5));
						panel6.add(bbYMinText, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.HORIZONTAL,2,5,0,5));
						panel6.add(row4col23row2_4, GuiUtil.setConstraints(2,1,0.0,0.0,GridBagConstraints.NONE,2,10,0,5));
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
		
		setVisible(true);
	}

	public void doTranslation() {
		//internationalisierte Labels und Strings
		row4col22col2_1.setText(ImpExpGui.labels.getString("filter.label.gmlName"));
		row3col2_1.setText(ImpExpGui.labels.getString("filter.label.gmlId"));
		row4col21row1_1.setText(ImpExpGui.labels.getString("filter.label.counter.start"));
		row4col21row1_2.setText(ImpExpGui.labels.getString("filter.label.counter.end"));
		row4col23row2_1.setText(ImpExpGui.labels.getString("filter.label.boundingBox.xMin"));
		row4col23row2_2.setText(ImpExpGui.labels.getString("filter.label.boundingBox.xMax"));
		row4col23row2_3.setText(ImpExpGui.labels.getString("filter.label.boundingBox.yMin"));
		row4col23row2_4.setText(ImpExpGui.labels.getString("filter.label.boundingBox.yMax"));
		row3col2.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("filter.border.gmlId")));
		row4col2.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("filter.border.complexFilter")));
		cityObjectFilter.setText(ImpExpGui.labels.getString("filter.border.counter"));
		gmlNameFilter.setText(ImpExpGui.labels.getString("filter.border.gmlName"));
		boundingBoxFilter.setText(ImpExpGui.labels.getString("filter.border.boundingBox"));
		featureClassFilter.setText(ImpExpGui.labels.getString("filter.border.featureClass"));
	}


	//private-methoden

	//public-methoden
	public void loadSettings() {

		if (filter.isSetComplex())
			complexFilter.setSelected(true);
		else
			gmlIdFilter.setSelected(true);

		boundingBoxFilter.setSelected(filter.getComplexFilter().getBoundingBoxFilter().isSet());
		gmlNameFilter.setSelected(filter.getComplexFilter().getGmlNameFilter().isSet());
		cityObjectFilter.setSelected(filter.getComplexFilter().getFeatureCountFilter().isSet());
		featureClassFilter.setSelected(filter.getComplexFilter().getFeatureClassFilter().isSet());

		gmlNameText.setText(filter.getComplexFilter().getGmlNameFilter().getValue());		
		gmlIdText.setText(Util.collection2string(filter.getSimpleFilter().getGmlIdFilter().getGmlIds(), ","));

		if (filter.getComplexFilter().getFeatureCountFilter().getFrom() != null)
			coStartText.setText(String.valueOf(filter.getComplexFilter().getFeatureCountFilter().getFrom()));
		else
			coStartText.setText(null);

		if (filter.getComplexFilter().getFeatureCountFilter().getTo() != null)
			coEndText.setText(String.valueOf(filter.getComplexFilter().getFeatureCountFilter().getTo()));
		else coEndText.setText(null);

		if (filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().getX() != null)
			bbXMinText.setText(String.valueOf(filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().getX()));
		else
			bbXMinText.setText(null);

		if (filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().getY() != null)
			bbYMinText.setText(String.valueOf(filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().getY()));
		else
			bbYMinText.setText(null);

		if (filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().getX() != null)
			bbXMaxText.setText(String.valueOf(filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().getX()));
		else
			bbXMaxText.setText(null);

		if (filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().getY() != null)
			bbYMaxText.setText(String.valueOf(filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().getY()));
		else
			bbYMaxText.setText(null);

		FilterFeatureClass featureClassFilter = filter.getComplexFilter().getFeatureClassFilter();		

		if (!featureClassFilter.isSetBuilding())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(building.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(building.getPath()));

		if (!featureClassFilter.isSetWaterBody())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(water.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(water.getPath()));

		if (!featureClassFilter.isSetLandUse())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(landuse.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(landuse.getPath()));

		if (!featureClassFilter.isSetVegetation())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(vegetation.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(vegetation.getPath()));

		if (!featureClassFilter.isSetTransportation())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(transportation.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(transportation.getPath()));

		if (!featureClassFilter.isSetReliefFeature())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(reliefFeature.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(reliefFeature.getPath()));

		if (!featureClassFilter.isSetCityFurniture())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(cityfurniture.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(cityfurniture.getPath()));

		if (!featureClassFilter.isSetGenericCityObject())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(genericCityObject.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(genericCityObject.getPath()));

		if (!featureClassFilter.isSetCityObjectGroup())
			fcTree.getCheckingModel().addCheckingPath(new TreePath(cityObjectGroup.getPath()));
		else
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(cityObjectGroup.getPath()));
	}


	public void setSettings() {

		if (complexFilter.isSelected()) 
			filter.setMode(FilterMode.COMPLEX);
		else
			filter.setMode(FilterMode.SIMPLE);

		filter.getComplexFilter().getBoundingBoxFilter().setActive(boundingBoxFilter.isSelected());
		filter.getComplexFilter().getGmlNameFilter().setActive(gmlNameFilter.isSelected());
		filter.getComplexFilter().getFeatureCountFilter().setActive(cityObjectFilter.isSelected());
		filter.getComplexFilter().getFeatureClassFilter().setActive(featureClassFilter.isSelected());

		filter.getComplexFilter().getGmlNameFilter().setValue(gmlNameText.getText());

		if (gmlIdText.getText() != null && gmlIdText.getText().trim().length() > 0) {
			String trimmed = gmlIdText.getText().replaceAll("\\s*", "");
			filter.getSimpleFilter().getGmlIdFilter().setGmlIds(Util.string2string(trimmed, ","));
		} else
			filter.getSimpleFilter().getGmlIdFilter().setGmlIds(new ArrayList<String>());

		try {
			filter.getComplexFilter().getFeatureCountFilter().setFrom(Long.valueOf(coStartText.getText()));
		} catch (NumberFormatException nfe) {
			filter.getComplexFilter().getFeatureCountFilter().setFrom(null);
		}

		try {
			filter.getComplexFilter().getFeatureCountFilter().setTo(Long.valueOf(coEndText.getText()));
		} catch (NumberFormatException nfe) {
			filter.getComplexFilter().getFeatureCountFilter().setTo(null);
		}

		try {
			filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().setX(Double.valueOf(bbXMinText.getText()));
		} catch (NumberFormatException nfe) {
			filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().setX(null);
		}

		try {
			filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().setY(Double.valueOf(bbYMinText.getText()));
		} catch (NumberFormatException nfe) {
			filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().setY(null);
		}

		try {
			filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().setX(Double.valueOf(bbXMaxText.getText()));
		} catch (NumberFormatException nfe) {
			filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().setX(null);
		}

		try {
			filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().setY(Double.valueOf(bbYMaxText.getText()));
		} catch (NumberFormatException nfe) {
			filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().setY(null);
		}

		FilterFeatureClass featureClassFilter = filter.getComplexFilter().getFeatureClassFilter();		
		featureClassFilter.setBuilding(!fcTree.getCheckingModel().isPathChecked(new TreePath(building.getPath()))); 
		featureClassFilter.setWaterBody(!fcTree.getCheckingModel().isPathChecked(new TreePath(water.getPath())));
		featureClassFilter.setLandUse(!fcTree.getCheckingModel().isPathChecked(new TreePath(landuse.getPath())));
		featureClassFilter.setVegetation(!fcTree.getCheckingModel().isPathChecked(new TreePath(vegetation.getPath())));
		featureClassFilter.setTransportation(!fcTree.getCheckingModel().isPathChecked(new TreePath(transportation.getPath())));
		featureClassFilter.setReliefFeature(!fcTree.getCheckingModel().isPathChecked(new TreePath(reliefFeature.getPath())));	
		featureClassFilter.setCityFurniture(!fcTree.getCheckingModel().isPathChecked(new TreePath(cityfurniture.getPath())));
		featureClassFilter.setGenericCityObject(!fcTree.getCheckingModel().isPathChecked(new TreePath(genericCityObject.getPath())));
		featureClassFilter.setCityObjectGroup(!fcTree.getCheckingModel().isPathChecked(new TreePath(cityObjectGroup.getPath())));
	}
}
