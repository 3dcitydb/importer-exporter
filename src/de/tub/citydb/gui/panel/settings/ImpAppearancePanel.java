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
import de.tub.citydb.config.project.importer.ImpAppearance;
import de.tub.citydb.gui.util.GuiUtil;

public class ImpAppearancePanel extends PrefPanelBase {
	private JPanel block1;
	private JPanel block2;
	private JLabel impAppOldLabel;
	private JRadioButton impAppRadioNoImp;
	private JRadioButton impAppRadioAppImp;
	private JRadioButton impAppRadioImp;
	private JTextField impAppOldText;

	public ImpAppearancePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImpAppearance appearances = config.getProject().getImporter().getAppearances();
		
		if (!impAppOldText.getText().equals(appearances.getThemeForTexturedSurface())) return true;
		if (impAppRadioImp.isSelected() && !(appearances.isSetImportAppearance() && appearances.isSetImportTextureFiles())) return true;
		if (impAppRadioAppImp.isSelected() && !(appearances.isSetImportAppearance() && !appearances.isSetImportTextureFiles())) return true;
		if (impAppRadioNoImp.isSelected() && !(!appearances.isSetImportAppearance() && !appearances.isSetImportTextureFiles())) return true;
		return false;
	}

	private void initGui() {
		impAppRadioNoImp = new JRadioButton("");
		impAppRadioAppImp = new JRadioButton("");
		impAppRadioImp = new JRadioButton("");
		ButtonGroup impAppRadio = new ButtonGroup();
		impAppRadio.add(impAppRadioNoImp);
		impAppRadio.add(impAppRadioImp);
		impAppRadio.add(impAppRadioAppImp);
		impAppOldLabel = new JLabel("");
		impAppOldText = new JTextField("");

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

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.import.appearance.border.import")));
		impAppRadioNoImp.setText(Internal.I18N.getString("pref.import.appearance.label.noImport"));
		impAppRadioAppImp.setText(Internal.I18N.getString("pref.import.appearance.label.importWithoutTexture"));
		impAppRadioImp.setText(Internal.I18N.getString("pref.import.appearance.label.importWithTexture"));
		block2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.import.appearance.border.texturedSurface")));
		impAppOldLabel.setText(Internal.I18N.getString("pref.import.appearance.label.theme"));
	}

	@Override
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

	@Override
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
