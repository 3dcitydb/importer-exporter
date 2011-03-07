package de.tub.citydb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.general.Path;
import de.tub.citydb.config.project.general.PathMode;

public class SetPathPanel extends PrefPanelBase {

	private JPanel block1;
	private JRadioButton importPathRadioLast;
	private JRadioButton importPathRadioDef;
	private JTextField importPathText;
	private JButton importPathButton;
	private JPanel block2;
	private JRadioButton exportPathRadioLast;
	private JRadioButton exportPathRadioDef;
	private JTextField exportPathText;
	private JButton exportPathButton;
	
	public SetPathPanel(Config inpConfig) {
		super(inpConfig);
		initGui();
	}

	public boolean isModified() {
		if (super.isModified()) return true;
		
		Path importPath = config.getProject().getImporter().getPath();
		Path exportPath = config.getProject().getExporter().getPath();;
		
		if (importPathRadioLast.isSelected() != importPath.isSetLastUsedMode()) return true;
		if (importPathRadioDef.isSelected() != importPath.isSetStandardMode()) return true;
		if (exportPathRadioLast.isSelected() != exportPath.isSetLastUsedMode()) return true;
		if (exportPathRadioDef.isSelected() != exportPath.isSetStandardMode()) return true;
		if (!importPathText.getText().equals(importPath.getStandardPath())) return true;
		if (!exportPathText.getText().equals(exportPath.getStandardPath())) return true;
		return false;
	}

	public void initGui() {
		
		importPathRadioLast = new JRadioButton("");
		importPathRadioDef = new JRadioButton("");
		ButtonGroup importPathRadio = new ButtonGroup();
		importPathRadio.add(importPathRadioLast);
		importPathRadio.add(importPathRadioDef);
		
		importPathText = new JTextField("");
		importPathButton = new JButton("");
		
		exportPathRadioLast = new JRadioButton("");
		exportPathRadioDef = new JRadioButton("");
		ButtonGroup exportPathRadio = new ButtonGroup();
		exportPathRadio.add(exportPathRadioLast);
		exportPathRadio.add(exportPathRadioDef);
		
		exportPathText = new JTextField("");
		exportPathButton = new JButton("");

		importPathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sImp = browseFile(importPathText.getText());
				if (!sImp.isEmpty()) {
					importPathText.setText(sImp);
					setModified();
				}
			}
		});
		exportPathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sExp = browseFile(exportPathText.getText());
				if (!sExp.isEmpty()){
					exportPathText.setText(sExp);
					setModified();
				}
			}
		});

		setLayout(new GridBagLayout());
		{

			block1 = new JPanel();
			block2 = new JPanel();

			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));

			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			importPathRadioLast.setIconTextGap(10);
			importPathRadioDef.setIconTextGap(10);
			int lmargin = (int)(importPathRadioLast.getPreferredSize().getWidth()) + 11;
			{
				block1.add(importPathRadioLast, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(importPathRadioDef, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(importPathText, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				block1.add(importPathButton, GuiUtil.setConstraints(1,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,0));
			}
			
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			exportPathRadioLast.setIconTextGap(10);
			exportPathRadioDef.setIconTextGap(10);
			{
				block2.add(exportPathRadioLast, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(exportPathRadioDef, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(exportPathText, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				block2.add(exportPathButton, GuiUtil.setConstraints(1,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
			}
		}
	}

	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("pref.general.path.border.importPath")));
		importPathRadioLast.setText(ImpExpGui.labels.getString("pref.general.path.label.importLastUsedPath"));
		importPathRadioDef.setText(ImpExpGui.labels.getString("pref.general.path.label.importDefaultPath"));
		importPathButton.setText(ImpExpGui.labels.getString("common.button.browse"));		
		block2.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("pref.general.path.border.exportPath")));
		exportPathRadioLast.setText(ImpExpGui.labels.getString("pref.general.path.label.exportLastUsedPath"));
		exportPathRadioDef.setText(ImpExpGui.labels.getString("pref.general.path.label.exportDefaultPath"));
		exportPathButton.setText(ImpExpGui.labels.getString("common.button.browse"));
	}

	public void loadSettings() {
		Path path = config.getProject().getImporter().getPath();

		if (path.isSetLastUsedMode())
			importPathRadioLast.setSelected(true);
		else
			importPathRadioDef.setSelected(true);

		importPathText.setText(path.getStandardPath());

		path = config.getProject().getExporter().getPath();
		if (path.isSetLastUsedMode())
			exportPathRadioLast.setSelected(true);
		else
			exportPathRadioDef.setSelected(true);

		//PREF_GENERAL_EXPORTPATH_DEFAULT
		exportPathText.setText(path.getStandardPath());

	}

	public void setSettings() {
		Path path = config.getProject().getImporter().getPath();
		
		if (importPathRadioDef.isSelected()) {
			path.setPathMode(PathMode.STANDARD);
		}
		else {
			path.setPathMode(PathMode.LASTUSED);
		}
		//PREF_GENERAL_IMPORTPATH_DEFAULT
		path.setStandardPath(importPathText.getText());

		path = config.getProject().getExporter().getPath();
		
		if (exportPathRadioDef.isSelected()) {
			path.setPathMode(PathMode.STANDARD);
		}
		else {
			path.setPathMode(PathMode.LASTUSED);
		}
		//PREF_GENERAL_EXPORTPATH_DEFAULT
		path.setStandardPath(exportPathText.getText());
	}

	private String browseFile(String oldDir) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(oldDir));

		int result = chooser.showSaveDialog(this);
		if (result == JFileChooser.CANCEL_OPTION) return "";
		String browseString = chooser.getSelectedFile().toString();
		return browseString;
	}
}
