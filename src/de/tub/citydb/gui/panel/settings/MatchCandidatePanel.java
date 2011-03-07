package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.matching.MatchingDeleteMode;
import de.tub.citydb.config.project.matching.MergeConfig;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class MatchCandidatePanel extends PrefPanelBase {
	private JPanel block1;
	private JRadioButton deleteMerRadio;
	private JRadioButton deleteAllRadio;
	private JRadioButton deleteRenRadio;

	public MatchCandidatePanel(Config inpConfig) {
		super(inpConfig);
		initGui();
	}

	@Override
	public boolean isModified() {
		MergeConfig mergeConfig = config.getProject().getMatching().getMergeConfig();
		
		if (deleteMerRadio.isSelected() && !mergeConfig.isDeleteModeMerge()) return true;
		if (deleteAllRadio.isSelected() && !mergeConfig.isDeleteModeDelAll()) return true;
		if (deleteRenRadio.isSelected() && !mergeConfig.isDeleteModeRename()) return true;

		return false;
	}

	private void initGui() {
		deleteMerRadio = new JRadioButton();
		deleteAllRadio = new JRadioButton();
		deleteRenRadio = new JRadioButton();
		ButtonGroup deleteRadio = new ButtonGroup();
		deleteRadio.add(deleteMerRadio);
		deleteRadio.add(deleteAllRadio);
		deleteRadio.add(deleteRenRadio);

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			deleteMerRadio.setIconTextGap(10);
			deleteAllRadio.setIconTextGap(10);
			deleteRenRadio.setIconTextGap(10);
			{
				block1.add(deleteMerRadio, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(deleteAllRadio, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(deleteRenRadio, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
	}

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.matching.candidate.postMerge.border")));
		deleteMerRadio.setText(Internal.I18N.getString("pref.matching.candidate.postMerge.geom"));
		deleteAllRadio.setText(Internal.I18N.getString("pref.matching.candidate.postMerge.delete"));
		deleteRenRadio.setText(Internal.I18N.getString("pref.matching.candidate.postMerge.rename"));

	}

	@Override
	public void loadSettings() {
		MergeConfig mergeConfig = config.getProject().getMatching().getMergeConfig();

		if (mergeConfig.isDeleteModeDelAll())
			deleteAllRadio.setSelected(true);
		else if (mergeConfig.isDeleteModeRename())
			deleteRenRadio.setSelected(true);
		else
			deleteMerRadio.setSelected(true);
	}

	@Override
	public void setSettings() {
		MergeConfig mergeConfig = config.getProject().getMatching().getMergeConfig();
		
		if (deleteAllRadio.isSelected())
			mergeConfig.setDeleteMode(MatchingDeleteMode.DELALL);
		else if (deleteRenRadio.isSelected())
			mergeConfig.setDeleteMode(MatchingDeleteMode.RENAME);
		else
			mergeConfig.setDeleteMode(MatchingDeleteMode.MERGE);
	}

}
