package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.importer.ImpContinuation;
import de.tub.citydb.config.project.importer.ImpUpdatingPersonMode;
import de.tub.citydb.gui.util.GuiUtil;

public class ImpContinuationPanel extends PrefPanelBase {
	private JPanel block1;
	private JPanel block2;
	
	private JTextField lineageText;
	private JLabel lineageLabel;
	private JTextField reasonForUpdateText;
	private JLabel reasonForUpdateLabel;
	private JRadioButton updatingPersonDBAccount;
	private JRadioButton updatingPersonUser;
	private JTextField updatingPersonText;
	
	public ImpContinuationPanel(Config config) {
		super(config);
		initGui();
	}
	
	@Override
	public boolean isModified() {
		ImpContinuation continuation = config.getProject().getImporter().getContinuation();

		if (!lineageText.getText().equals(continuation.getLineage())) return true;
		if (!reasonForUpdateText.getText().equals(continuation.getReasonForUpdate())) return true;
		if (!updatingPersonText.getText().equals(continuation.getUpdatingPerson())) return true;
		if (updatingPersonDBAccount.isSelected() != continuation.isUpdatingPersonModeDatabase()) return true;
		if (updatingPersonUser.isSelected() != continuation.isUpdatingPersonModeUser()) return true;

		return false;
	}
	
	private void initGui() {
		lineageText = new JTextField("");
		lineageLabel = new JLabel("");
		reasonForUpdateText = new JTextField("");
		reasonForUpdateLabel = new JLabel("");
		
		updatingPersonDBAccount = new JRadioButton("");
		updatingPersonUser = new JRadioButton("");
		ButtonGroup updatingPerson = new ButtonGroup();
		updatingPerson.add(updatingPersonDBAccount);
		updatingPerson.add(updatingPersonUser);
		updatingPersonText = new JTextField("");
		
		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			{
				block1.add(lineageLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(lineageText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(reasonForUpdateLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(reasonForUpdateText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			}
			
			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			int lmargin = (int)(updatingPersonUser.getPreferredSize().getWidth()) + 11;
			updatingPersonDBAccount.setIconTextGap(10);
			updatingPersonUser.setIconTextGap(10);
			{
				block2.add(updatingPersonDBAccount, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(updatingPersonUser, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(updatingPersonText, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
			}
		}
	}
	
	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.import.continuation.border.lineage")));
		lineageLabel.setText(Internal.I18N.getString("pref.import.continuation.label.lineage"));
		reasonForUpdateLabel.setText(Internal.I18N.getString("pref.import.continuation.label.reasonForUpdate"));

		block2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.import.continuation.border.updatingPerson")));
		updatingPersonDBAccount.setText(Internal.I18N.getString("pref.import.continuation.label.updatingPerson.database"));
		updatingPersonUser.setText(Internal.I18N.getString("pref.import.continuation.label.updatingPerson.user"));		
	}

	@Override
	public void loadSettings() {
		ImpContinuation continuation = config.getProject().getImporter().getContinuation();
		
		lineageText.setText(continuation.getLineage());
		reasonForUpdateText.setText(continuation.getReasonForUpdate());
		updatingPersonText.setText(continuation.getUpdatingPerson());
		
		if (continuation.isUpdatingPersonModeDatabase())
			updatingPersonDBAccount.setSelected(true);
		else
			updatingPersonUser.setSelected(true);		
	}

	@Override
	public void setSettings() {
		ImpContinuation continuation = config.getProject().getImporter().getContinuation();
		
		continuation.setLineage(lineageText.getText());
		continuation.setReasonForUpdate(reasonForUpdateText.getText());
		continuation.setUpdatingPerson(updatingPersonText.getText());
		
		if (updatingPersonDBAccount.isSelected())
			continuation.setUpdatingPersonMode(ImpUpdatingPersonMode.DATABASE);
		else
			continuation.setUpdatingPersonMode(ImpUpdatingPersonMode.USER);
	}

}
