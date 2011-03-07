package de.tub.citydb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.importer.ImpAppearance;

public class ImpAppearancePanel extends PrefPanelBase {

	//Variablendefinition
	private JPanel block1;
	private JPanel block2;
	private JLabel impAppOldLabel;
	private JRadioButton impAppRadioNoImp;
	private JRadioButton impAppRadioAppImp;
	private JRadioButton impAppRadioImp;
	private JTextField impAppOldText;

	//Konstruktor
	public ImpAppearancePanel(Config inpConfig) {
		super(inpConfig);
		initGui();
	}

	public boolean isModified() {
		if (super.isModified()) return true;
		
		ImpAppearance appearances = config.getProject().getImporter().getAppearances();
		
		if (!impAppOldText.getText().equals(appearances.getThemeForTexturedSurface())) return true;
		if (impAppRadioImp.isSelected() && !(appearances.isSetImportAppearance() && appearances.isSetImportTextureFiles())) return true;
		if (impAppRadioAppImp.isSelected() && !(appearances.isSetImportAppearance() && !appearances.isSetImportTextureFiles())) return true;
		if (impAppRadioNoImp.isSelected() && !(!appearances.isSetImportAppearance() && !appearances.isSetImportTextureFiles())) return true;
		return false;
	}

	//initGui-Methode
	public void initGui() {

		//Variablendeklaration
		impAppRadioNoImp = new JRadioButton("");
		impAppRadioAppImp = new JRadioButton("");
		impAppRadioImp = new JRadioButton("");
		ButtonGroup impAppRadio = new ButtonGroup();
		impAppRadio.add(impAppRadioNoImp);
		impAppRadio.add(impAppRadioImp);
		impAppRadio.add(impAppRadioAppImp);
		impAppOldLabel = new JLabel("");
		impAppOldText = new JTextField("");

		//Layout
		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			impAppRadioImp.setIconTextGap(10);
			impAppRadioAppImp.setIconTextGap(10);
			impAppRadioNoImp.setIconTextGap(10);
			{
				block1.add(impAppRadioImp, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impAppRadioAppImp, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impAppRadioNoImp, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			{
				block2.add(impAppOldLabel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,0,5));
				block2.add(impAppOldText, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			}

		}
	}

	//doTranslation-Methode
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("pref.import.appearance.border.import")));
		impAppRadioNoImp.setText(ImpExpGui.labels.getString("pref.import.appearance.label.noImport"));
		impAppRadioAppImp.setText(ImpExpGui.labels.getString("pref.import.appearance.label.importWithoutTexture"));
		impAppRadioImp.setText(ImpExpGui.labels.getString("pref.import.appearance.label.importWithTexture"));
		block2.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("pref.import.appearance.border.texturedSurface")));
		impAppOldLabel.setText(ImpExpGui.labels.getString("pref.import.appearance.label.theme"));
	}

	//Config
	public void loadSettings() {
		ImpAppearance appearances = config.getProject().getImporter().getAppearances();
		
		if (appearances.isSetImportAppearance()) {			
			if (appearances.isSetImportTextureFiles()) 
				impAppRadioImp.setSelected(true);
			else
				impAppRadioAppImp.setSelected(true);
		} else
			impAppRadioNoImp.setSelected(true);

		impAppOldText.setText(appearances.getThemeForTexturedSurface());
	}

	public void setSettings() {
		ImpAppearance appearances = config.getProject().getImporter().getAppearances();

		if (impAppRadioImp.isSelected()) {
			appearances.setImportAppearances(true);
			appearances.setImportTextureFiles(true);
		}
		if (impAppRadioAppImp.isSelected()) {
			appearances.setImportAppearances(true);
			appearances.setImportTextureFiles(false);
		}
		if (impAppRadioNoImp.isSelected()) {
			appearances.setImportAppearances(false);
			appearances.setImportTextureFiles(false);
		}
		
		String theme = impAppOldText.getText();
		if (theme == null || theme.trim().length() == 0)
			theme = "rgbTexture";

		appearances.setThemeForTexturedSurface(theme);
	}

}
