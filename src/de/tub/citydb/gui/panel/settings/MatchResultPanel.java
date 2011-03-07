package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.matching.MatchingResultMode;
import de.tub.citydb.config.project.matching.MatchingSettings;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

public class MatchResultPanel extends PrefPanelBase {

	//Variablendefinition
	private JPanel block1;
	private JRadioButton resultFixRadio;
	private JRadioButton resultUserRadio;
	private JRadioButton resultAllRadio;
	private JFormattedTextField resultUserText;

	//Konstruktor
	public MatchResultPanel(Config inpConfig) {
		super(inpConfig);
		initGui();
	}

	public boolean isModified() {
		if (super.isModified()) return true;

		MatchingSettings result = config.getProject().getMatching().getMatchingSettings();

		if (resultFixRadio.isSelected() && !result.isResultModeFix()) return true;
		if (resultUserRadio.isSelected() && !result.isResultModeUser()) return true;
		if (resultAllRadio.isSelected() && !result.isResultModeAll()) return true;
		return false;
	}

	//initGui-Methode
	public void initGui() {

		//Variablendeklaration
		resultFixRadio = new JRadioButton();
		resultUserRadio = new JRadioButton();
		resultAllRadio = new JRadioButton();
		ButtonGroup resultRadio = new ButtonGroup();
		resultRadio.add(resultFixRadio);
		resultRadio.add(resultUserRadio);
		resultRadio.add(resultAllRadio);
		resultUserText = new JFormattedTextField();
		resultUserText.setValue(new Integer(100));

		//Layout
		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			resultFixRadio.setIconTextGap(10);
			resultUserRadio.setIconTextGap(10);
			resultAllRadio.setIconTextGap(10);
			{
				GridBagConstraints c = GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5);
				c.gridwidth = 2;
				block1.add(resultFixRadio, c);

				block1.add(resultUserRadio, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,0,0));
				block1.add(resultUserText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));

				c = GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5);
				c.gridwidth = 2;
				block1.add(resultAllRadio, c);
			}
		}

	}

	//doTranslation-Methode
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("pref.matching.result.border")));
		resultFixRadio.setText(ImpExpGui.labels.getString("pref.matching.result.fix"));
		resultUserRadio.setText(ImpExpGui.labels.getString("pref.matching.result.user"));
		resultAllRadio.setText(ImpExpGui.labels.getString("pref.matching.result.all"));
	}

	//Config
	public void loadSettings() {
		MatchingSettings result = config.getProject().getMatching().getMatchingSettings();

		if (result.isResultModeUser())
			resultUserRadio.setSelected(true);
		else if (result.isResultModeAll())
			resultAllRadio.setSelected(true);
		else
			resultFixRadio.setSelected(true);
		
		resultUserText.setValue(new Integer(result.getResultUser())); 
	}

	public void setSettings() {
		MatchingSettings result = config.getProject().getMatching().getMatchingSettings();

		if (resultUserRadio.isSelected())
			result.setResultMode(MatchingResultMode.USER);
		else if (resultAllRadio.isSelected())
			result.setResultMode(MatchingResultMode.ALL);
		else
			result.setResultMode(MatchingResultMode.FIX);

		if ((Integer)resultUserText.getValue() > 0)
			result.setResultUser((Integer)resultUserText.getValue());
		else {
			result.setResultUser(100);
			resultUserText.setValue(100);
		}
	}

}
