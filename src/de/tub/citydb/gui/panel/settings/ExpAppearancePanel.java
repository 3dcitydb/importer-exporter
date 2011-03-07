package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.exporter.ExportAppearance;
import de.tub.citydb.config.project.exporter.TexturePathMode;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class ExpAppearancePanel extends PrefPanelBase {
	private JPanel block1;
	private JPanel path1;

	private JCheckBox expAppOverwriteCheck;
	private JRadioButton expAppRadioNoExp;
	private JRadioButton expAppRadioAppExp;
	private JRadioButton expAppRadioExp;
	private JRadioButton expAppRadioPathAbs;
	private JRadioButton expAppRadioPathRel;
	private JTextField expAppPathAbsText;
	private JButton expAppPathAbsButton;
	private JTextField expAppPathRelText;

	public ExpAppearancePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ExportAppearance appearances = config.getProject().getExporter().getAppearances();

		if (!expAppPathAbsText.getText().equals(appearances.getAbsoluteTexturePath())) return true;
		if (!expAppPathRelText.getText().equals(appearances.getRelativeTexturePath())) return true;
		if (expAppRadioPathRel.isSelected() != appearances.isTexturePathRealtive()) return true;
		if (expAppRadioPathAbs.isSelected() != appearances.isTexturePathAbsolute()) return true;
		if (expAppRadioExp.isSelected() && !(appearances.isSetExportAppearance() && appearances.isSetExportTextureFiles())) return true;
		if (expAppRadioNoExp.isSelected() && !(!appearances.isSetExportAppearance() && !appearances.isSetExportTextureFiles())) return true;
		if (expAppRadioAppExp.isSelected() && !(appearances.isSetExportAppearance() && !appearances.isSetExportTextureFiles())) return true;
		if (expAppOverwriteCheck.isSelected() != appearances.isSetOverwriteTextureFiles()) return true;

		return false;
	}

	private void initGui() {
		expAppOverwriteCheck = new JCheckBox("");
		expAppRadioNoExp = new JRadioButton("");
		expAppRadioAppExp = new JRadioButton("");
		expAppRadioExp = new JRadioButton("");
		ButtonGroup expAppRadio = new ButtonGroup();
		expAppRadio.add(expAppRadioNoExp);
		expAppRadio.add(expAppRadioAppExp);
		expAppRadio.add(expAppRadioExp);
		expAppRadioPathAbs = new JRadioButton("");
		expAppRadioPathRel = new JRadioButton("");
		ButtonGroup expAppRadioPath = new ButtonGroup();
		expAppRadioPath.add(expAppRadioPathAbs);
		expAppRadioPath.add(expAppRadioPathRel);
		expAppPathAbsText = new JTextField("");
		expAppPathAbsButton = new JButton("");
		expAppPathRelText = new JTextField("");

		expAppPathAbsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sImp = browseFile(Internal.I18N.getString("pref.export.appearance.label.absPath"), expAppPathAbsText.getText());
				if (!sImp.isEmpty())
					expAppPathAbsText.setText(sImp);
			}
		});

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			path1 = new JPanel();

			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			expAppRadioExp.setIconTextGap(10);
			expAppRadioAppExp.setIconTextGap(10);
			expAppRadioNoExp.setIconTextGap(10);
			expAppOverwriteCheck.setIconTextGap(10);
			int lmargin = (int)(expAppRadioExp.getPreferredSize().getWidth()) + 11;
			{
				block1.add(expAppRadioExp, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(expAppOverwriteCheck, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));
				block1.add(expAppRadioAppExp, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(expAppRadioNoExp, GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}

			add(path1, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			path1.setBorder(BorderFactory.createTitledBorder(""));
			path1.setLayout(new GridBagLayout());
			expAppRadioPathAbs.setIconTextGap(10);
			expAppRadioPathRel.setIconTextGap(10);
			expAppPathAbsText.setPreferredSize(expAppPathAbsText.getSize());
			{
				path1.add(expAppRadioPathAbs, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				path1.add(expAppPathAbsText, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));
				path1.add(expAppPathAbsButton, GuiUtil.setConstraints(1,1,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				path1.add(expAppRadioPathRel, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,5,5,0,5));
				path1.add(expAppPathRelText, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
			}

		}

		ActionListener textureExportListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledTextureExport();
			}
		};
		
		ActionListener texturePathListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledTexturePath();
			}
		};
		
		expAppRadioNoExp.addActionListener(textureExportListener);
		expAppRadioAppExp.addActionListener(textureExportListener);
		expAppRadioExp.addActionListener(textureExportListener);		
		expAppRadioPathAbs.addActionListener(texturePathListener);
		expAppRadioPathRel.addActionListener(texturePathListener);
	}

	private void setEnabledTextureExport() {
		expAppOverwriteCheck.setEnabled(expAppRadioExp.isSelected());
		
		((TitledBorder)path1.getBorder()).setTitleColor(!expAppRadioNoExp.isSelected() ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		path1.repaint();

		expAppRadioPathAbs.setEnabled(!expAppRadioNoExp.isSelected());
		expAppRadioPathRel.setEnabled(!expAppRadioNoExp.isSelected());
		expAppPathAbsText.setEnabled(!expAppRadioNoExp.isSelected() && expAppRadioPathAbs.isSelected());
		expAppPathAbsButton.setEnabled(!expAppRadioNoExp.isSelected() && expAppRadioPathAbs.isSelected());
		expAppPathRelText.setEnabled(!expAppRadioNoExp.isSelected() && expAppRadioPathRel.isSelected());
	}
	
	private void setEnabledTexturePath() {
		expAppPathAbsText.setEnabled(expAppRadioPathAbs.isSelected());
		expAppPathAbsButton.setEnabled(expAppRadioPathAbs.isSelected());
		expAppPathRelText.setEnabled(expAppRadioPathRel.isSelected());
	}

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.export.appearance.border.export")));
		path1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.export.appearance.border.path")));
		expAppOverwriteCheck.setText(Internal.I18N.getString("pref.export.appearance.label.exportWithTexture.overwrite"));
		expAppRadioNoExp.setText(Internal.I18N.getString("pref.export.appearance.label.noExport"));
		expAppRadioAppExp.setText(Internal.I18N.getString("pref.export.appearance.label.exportWithoutTexture"));
		expAppRadioExp.setText(Internal.I18N.getString("pref.export.appearance.label.exportWithTexture"));
		expAppRadioPathAbs.setText(Internal.I18N.getString("pref.export.appearance.label.absPath"));
		expAppRadioPathRel.setText(Internal.I18N.getString("pref.export.appearance.label.relPath"));
		expAppPathAbsButton.setText(Internal.I18N.getString("common.button.browse"));
	}

	@Override
	public void loadSettings() {
		ExportAppearance appearances = config.getProject().getExporter().getAppearances();

		if (appearances.isSetExportAppearance()) {
			if (appearances.isSetExportTextureFiles())
				expAppRadioExp.setSelected(true);
			else
				expAppRadioAppExp.setSelected(true);
		} else
			expAppRadioNoExp.setSelected(true);


		expAppOverwriteCheck.setSelected(appearances.isSetOverwriteTextureFiles());
		expAppPathRelText.setText(appearances.getRelativeTexturePath());
		expAppPathAbsText.setText(appearances.getAbsoluteTexturePath());
		expAppRadioPathRel.setSelected(appearances.isTexturePathRealtive());
		expAppRadioPathAbs.setSelected(!appearances.isTexturePathRealtive());
		
		setEnabledTextureExport();
	}

	@Override
	public void setSettings() {
		ExportAppearance appearances = config.getProject().getExporter().getAppearances();

		if (expAppRadioExp.isSelected()) {
			appearances.setExportAppearances(true);
			appearances.setExportTextureFiles(true);
		}
		if (expAppRadioAppExp.isSelected()) {
			appearances.setExportAppearances(true);
			appearances.setExportTextureFiles(false);
		}
		if (expAppRadioNoExp.isSelected()) {
			appearances.setExportAppearances(false);
			appearances.setExportTextureFiles(false);
		}

		appearances.setOverwriteTextureFiles(expAppOverwriteCheck.isSelected());
		appearances.setAbsoluteTexturePath(expAppPathAbsText.getText());
		
		if (expAppPathRelText.getText() == null || expAppPathRelText.getText().trim().length() == 0)
			expAppPathRelText.setText("appearance"); 
		appearances.setRelativeTexturePath(expAppPathRelText.getText());

		if (expAppRadioPathRel.isSelected())
			appearances.setTexturePathMode(TexturePathMode.RELATIVE);
		else
			appearances.setTexturePathMode(TexturePathMode.ABSOLUTE);
	}

	private String browseFile(String title, String oldDir) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(oldDir));

		int result = chooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return "";
		String browseString = chooser.getSelectedFile().toString();
		return browseString;
	}
}
