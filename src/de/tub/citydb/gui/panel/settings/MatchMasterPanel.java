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
import de.tub.citydb.config.project.matching.MergeConfig;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class MatchMasterPanel extends PrefPanelBase {
	private JPanel block1;
	private JRadioButton candNameAppend;
	private JRadioButton candNameIgnore;
	private JRadioButton candNameReplace;

	public MatchMasterPanel(Config inpConfig) {
		super(inpConfig);
		initGui();
	}

	@Override
	public boolean isModified() {
		MergeConfig mergeConfig = config.getProject().getMatching().getMergeConfig();
		
		if (candNameAppend.isSelected() && !mergeConfig.isGmlNameModeAppend()) return true;
		if (candNameIgnore.isSelected() && !mergeConfig.isGmlNameModeIgnore()) return true;
		if (candNameReplace.isSelected() && !mergeConfig.isGmlNameModeReplace()) return true;

		return false;
	}

	private void initGui() {
		candNameAppend = new JRadioButton();
		candNameIgnore = new JRadioButton();
		candNameReplace = new JRadioButton();
		ButtonGroup gmlNameRadio = new ButtonGroup();
		gmlNameRadio.add(candNameAppend);
		gmlNameRadio.add(candNameIgnore);
		gmlNameRadio.add(candNameReplace);

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			candNameAppend.setIconTextGap(10);
			candNameIgnore.setIconTextGap(10);
			candNameReplace.setIconTextGap(10);
			{
				block1.add(candNameAppend, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(candNameIgnore, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(candNameReplace, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}			
		}
	}

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.matching.master.name.border")));
		candNameAppend.setText(Internal.I18N.getString("pref.matching.master.name.append"));
		candNameIgnore.setText(Internal.I18N.getString("pref.matching.master.name.ignore"));
		candNameReplace.setText(Internal.I18N.getString("pref.matching.master.name.replace"));
	}

	@Override
	public void loadSettings() {
		MergeConfig mergeConfig = config.getProject().getMatching().getMergeConfig();

		if (mergeConfig.isGmlNameModeIgnore())
			candNameIgnore.setSelected(true);
		else if (mergeConfig.isGmlNameModeReplace())
			candNameReplace.setSelected(true);
		else
			candNameAppend.setSelected(true);
	}

	@Override
	public void setSettings() {
		MergeConfig mergeConfig = config.getProject().getMatching().getMergeConfig();
		
		if (candNameIgnore.isSelected())
			mergeConfig.setGmlNameMode(MatchingGmlNameMode.IGNORE);
		else if (candNameReplace.isSelected())
			mergeConfig.setGmlNameMode(MatchingGmlNameMode.REPLACE);
		else
			mergeConfig.setGmlNameMode(MatchingGmlNameMode.APPEND);
	}

}
