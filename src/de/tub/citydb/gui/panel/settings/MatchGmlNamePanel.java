package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.matching.MatchingGmlNameMode;
import de.tub.citydb.config.project.matching.MatchingSettings;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

public class MatchGmlNamePanel extends PrefPanelBase {
	private JPanel block1;
	private JRadioButton gmlNameAppRadio;
	private JRadioButton gmlNameIgnRadio;
	private JRadioButton gmlNameRepRadio;
	private ImpExpGui impExpGui;

	public MatchGmlNamePanel(Config inpConfig, ImpExpGui inpImpExpGui) {
		super(inpConfig);
		impExpGui = inpImpExpGui;
		initGui();
	}

	@Override
	public boolean isModified() {
		MatchingSettings gmlName = config.getProject().getMatching().getMatchingSettings();
		
		if (gmlNameAppRadio.isSelected() && !gmlName.isGmlNameModeAppend()) return true;
		if (gmlNameIgnRadio.isSelected() && !gmlName.isGmlNameModeIgnore()) return true;
		if (gmlNameRepRadio.isSelected() && !gmlName.isGmlNameModeReplace()) return true;
		return false;
	}

	private void initGui() {
		gmlNameAppRadio = new JRadioButton();
		gmlNameIgnRadio = new JRadioButton();
		gmlNameRepRadio = new JRadioButton();
		ButtonGroup gmlNameRadio = new ButtonGroup();
		gmlNameRadio.add(gmlNameAppRadio);
		gmlNameRadio.add(gmlNameIgnRadio);
		gmlNameRadio.add(gmlNameRepRadio);

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			gmlNameAppRadio.setIconTextGap(10);
			gmlNameIgnRadio.setIconTextGap(10);
			gmlNameRepRadio.setIconTextGap(10);
			{
				block1.add(gmlNameAppRadio, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(gmlNameIgnRadio, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(gmlNameRepRadio, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}

	}

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.matching.gmlName.border")));
		gmlNameAppRadio.setText(Internal.I18N.getString("pref.matching.gmlName.append"));
		gmlNameIgnRadio.setText(Internal.I18N.getString("pref.matching.gmlName.ignore"));
		gmlNameRepRadio.setText(Internal.I18N.getString("pref.matching.gmlName.replace"));
	}

	@Override
	public void loadSettings() {
		MatchingSettings gmlName = config.getProject().getMatching().getMatchingSettings();

		if (gmlName.isGmlNameModeIgnore())
			gmlNameIgnRadio.setSelected(true);
		else if (gmlName.isGmlNameModeReplace())
			gmlNameRepRadio.setSelected(true);
		else
			gmlNameAppRadio.setSelected(true);
	}

	@Override
	public void setSettings() {
		MatchingSettings gmlName = config.getProject().getMatching().getMatchingSettings();
		
		if (gmlNameIgnRadio.isSelected())
			gmlName.setGmlNameMode(MatchingGmlNameMode.IGNORE);
		else if (gmlNameRepRadio.isSelected())
			gmlName.setGmlNameMode(MatchingGmlNameMode.REPLACE);
		else
			gmlName.setGmlNameMode(MatchingGmlNameMode.APPEND);
	}

}
