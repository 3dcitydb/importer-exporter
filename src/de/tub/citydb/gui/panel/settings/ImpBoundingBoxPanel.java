package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.filter.BoundingBox;
import de.tub.citydb.config.project.filter.BoundingBoxMode;
import de.tub.citydb.config.project.importer.ImportFilterConfig;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class ImpBoundingBoxPanel extends PrefPanelBase {
	private JPanel block1;
	private JRadioButton impBBRadioInside;
	private JRadioButton impBBRadioIntersect;

	public ImpBoundingBoxPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportFilterConfig filter = config.getProject().getImporter().getFilter();
		
		if (impBBRadioIntersect.isSelected() && !filter.getComplexFilter().getBoundingBox().isSetOverlapMode()) return true;
		if (impBBRadioInside.isSelected() && !filter.getComplexFilter().getBoundingBox().isSetContainMode()) return true;
		return false;
	}

	private void initGui() {
		impBBRadioInside = new JRadioButton();
		impBBRadioIntersect = new JRadioButton();
		ButtonGroup impBBRadio = new ButtonGroup();
		impBBRadio.add(impBBRadioInside);
		impBBRadio.add(impBBRadioIntersect);

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			impBBRadioInside.setIconTextGap(10);
			impBBRadioIntersect.setIconTextGap(10);
			{
				block1.add(impBBRadioIntersect, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impBBRadioInside, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}

	}

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("common.pref.boundingBox.border.selection")));
		impBBRadioInside.setText(Internal.I18N.getString("common.pref.boundingBox.label.inside"));
		impBBRadioIntersect.setText(Internal.I18N.getString("common.pref.boundingBox.label.overlap"));
	}

	@Override
	public void loadSettings() {
		BoundingBox bbox = config.getProject().getImporter().getFilter().getComplexFilter().getBoundingBox();

		if (bbox.isSetOverlapMode())
			impBBRadioIntersect.setSelected(true);
		else
			impBBRadioInside.setSelected(true);
	}

	@Override
	public void setSettings() {
		BoundingBox bbox = config.getProject().getImporter().getFilter().getComplexFilter().getBoundingBox();
		bbox.setMode(impBBRadioInside.isSelected() ? BoundingBoxMode.CONTAIN : BoundingBoxMode.OVERLAP);
	}

}
