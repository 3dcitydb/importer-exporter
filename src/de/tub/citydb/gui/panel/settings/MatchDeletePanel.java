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
import de.tub.citydb.config.project.matching.MatchingSettings;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

public class MatchDeletePanel extends PrefPanelBase {
	private JPanel block1;
	private JRadioButton deleteMerRadio;
	private JRadioButton deleteAllRadio;
	private JRadioButton deleteRenRadio;
	private ImpExpGui impExpGui;

	public MatchDeletePanel(Config inpConfig, ImpExpGui inpImpExpGui) {
		super(inpConfig);
		impExpGui = inpImpExpGui;
		initGui();
	}

	@Override
	public boolean isModified() {
		MatchingSettings delete = config.getProject().getMatching().getMatchingSettings();
		
		if (deleteMerRadio.isSelected() && !delete.isDeleteModeMerge()) return true;
		if (deleteAllRadio.isSelected() && !delete.isDeleteModeDelAll()) return true;
		if (deleteRenRadio.isSelected() && !delete.isDeleteModeRename()) return true;
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
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.matching.delete.border")));
		deleteMerRadio.setText(Internal.I18N.getString("pref.matching.delete.merge"));
		deleteAllRadio.setText(Internal.I18N.getString("pref.matching.delete.all"));
		deleteRenRadio.setText(Internal.I18N.getString("pref.matching.delete.rename"));
	}

	@Override
	public void loadSettings() {
		MatchingSettings delete = config.getProject().getMatching().getMatchingSettings();

		if (delete.isDeleteModeDelAll())
			deleteAllRadio.setSelected(true);
		else if (delete.isDeleteModeRename())
			deleteRenRadio.setSelected(true);
		else
			deleteMerRadio.setSelected(true);
	}

	@Override
	public void setSettings() {
		MatchingSettings delete = config.getProject().getMatching().getMatchingSettings();
		
		if (deleteAllRadio.isSelected())
			delete.setDeleteMode(MatchingDeleteMode.DELALL);
		else if (deleteRenRadio.isSelected())
			delete.setDeleteMode(MatchingDeleteMode.RENAME);
		else
			delete.setDeleteMode(MatchingDeleteMode.MERGE);
	}

}
