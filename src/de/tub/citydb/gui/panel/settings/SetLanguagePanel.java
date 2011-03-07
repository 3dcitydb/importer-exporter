package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.global.Global;
import de.tub.citydb.config.project.global.LanguageType;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class SetLanguagePanel extends PrefPanelBase {
	private JRadioButton importLanguageRadioDe;
	private JRadioButton importLanguageRadioEn;
	private JPanel language;
	private ImpExpGui impExpGui;
	
	public SetLanguagePanel(Config config, ImpExpGui inpImpExpGui) {
		super(config);
		impExpGui = inpImpExpGui;
		initGui();
	}
	
	@Override
	public boolean isModified() {
		LanguageType language = config.getProject().getGlobal().getLanguage();
		
		if (importLanguageRadioDe.isSelected() && !(language == LanguageType.DE)) return true;
		if (importLanguageRadioEn.isSelected() && !(language == LanguageType.EN)) return true;
		return false;
	}
	
	private void initGui() {		
		importLanguageRadioDe = new JRadioButton("");
		importLanguageRadioEn = new JRadioButton("");
		ButtonGroup importLanguageRadio = new ButtonGroup();
		importLanguageRadio.add(importLanguageRadioDe);
		importLanguageRadio.add(importLanguageRadioEn);
		
		setLayout(new GridBagLayout());
		{
			language = new JPanel();
			add(language, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			language.setBorder(BorderFactory.createTitledBorder(""));
			language.setLayout(new GridBagLayout());
			importLanguageRadioDe.setIconTextGap(10);
			importLanguageRadioEn.setIconTextGap(10);
			{
				language.add(importLanguageRadioDe, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				language.add(importLanguageRadioEn, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
	}
	
	@Override
	public void doTranslation() {
		language.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.general.language.border.selection")));
		importLanguageRadioDe.setText(Internal.I18N.getString("pref.general.language.label.de"));
		importLanguageRadioEn.setText(Internal.I18N.getString("pref.general.language.label.en"));
	}
	
	@Override
	public void loadSettings() {		
		LanguageType language = config.getProject().getGlobal().getLanguage();
		
		if (language == LanguageType.DE) {
			importLanguageRadioDe.setSelected(true);
		}
		else if (language == LanguageType.EN) {
			importLanguageRadioEn.setSelected(true);
		}
	}
	
	@Override
	public void setSettings() {
		Global global = config.getProject().getGlobal();
		
		if (importLanguageRadioDe.isSelected()) {
			global.setLanguage(LanguageType.DE);
		}
		else if (importLanguageRadioEn.isSelected()) {
			global.setLanguage(LanguageType.EN);
		}
		
		impExpGui.doTranslation();
	}
}
