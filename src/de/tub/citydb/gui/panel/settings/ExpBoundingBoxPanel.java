package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.FilterBoundingBox;
import de.tub.citydb.config.project.filter.FilterBoundingBoxMode;
import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

public class ExpBoundingBoxPanel extends PrefPanelBase {

	//Variablendefinition
	private JPanel block1;
	private JRadioButton expBBRadioInside;
	private JRadioButton expBBRadioIntersect;

	//Konstruktor
	public ExpBoundingBoxPanel(Config config) {
		super(config);
		initGui();
	}

	public boolean isModified() {
		if (super.isModified()) return true;
		
		FilterConfig filter = config.getProject().getExporter().getFilter();
		
		if (expBBRadioIntersect.isSelected() && !filter.getComplexFilter().getBoundingBoxFilter().isSetOverlapMode()) return true;
		if (expBBRadioInside.isSelected() && !filter.getComplexFilter().getBoundingBoxFilter().isSetContainMode()) return true;
		return false;
	}

	//initGui-Methode
	public void initGui() {

		//Variablendeklaration
		expBBRadioInside = new JRadioButton();
		expBBRadioIntersect = new JRadioButton();
		ButtonGroup expBBRadio = new ButtonGroup();
		expBBRadio.add(expBBRadioInside);
		expBBRadio.add(expBBRadioIntersect);

		//Layout
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
		}

	}

	//doTranslation-Methode
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("common.pref.boundingBox.border.selection")));
		expBBRadioInside.setText(ImpExpGui.labels.getString("common.pref.boundingBox.label.inside"));
		expBBRadioIntersect.setText(ImpExpGui.labels.getString("common.pref.boundingBox.label.overlap"));
	}

	//Config
	public void loadSettings() {
		FilterBoundingBox bbFilter = config.getProject().getExporter().getFilter().getComplexFilter().getBoundingBoxFilter();

		//PREF_EXPORT_BB_HANDLING
		if (bbFilter.isSetOverlapMode())
			expBBRadioIntersect.setSelected(true);
		else
			expBBRadioInside.setSelected(true);

	}

	public void setSettings() {
		FilterBoundingBox bbFilter = config.getProject().getExporter().getFilter().getComplexFilter().getBoundingBoxFilter();
		
		//PREF_EXPORT_BB_HANDLING
		if (expBBRadioInside.isSelected())
			bbFilter.setMode(FilterBoundingBoxMode.CONTAIN);
		else 
			bbFilter.setMode(FilterBoundingBoxMode.OVERLAP);
	}

}
