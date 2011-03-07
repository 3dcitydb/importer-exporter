package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.importer.CodeSpaceMode;
import de.tub.citydb.config.project.importer.ImportGmlId;
import de.tub.citydb.config.project.importer.UUIDMode;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class ImpIdHandlingPanel extends PrefPanelBase {
	private JPanel block2;
	private JPanel block1;
	private JCheckBox impIdCheckExtRef;
	private JRadioButton impIdCSRadioFile;
	private JRadioButton impIdCSRadioFilePath;
	private JRadioButton impIdCSRadioUser;
	private JTextField impIdCSUserText;

	private JRadioButton impIdRadioAdd;
	private JRadioButton impIdRadioExchange;

	public ImpIdHandlingPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportGmlId gmlId = config.getProject().getImporter().getGmlId();
		
		if (!impIdCSUserText.getText().equals(gmlId.getCodeSpace())) return true;
		if (impIdCheckExtRef.isSelected() != gmlId.isSetKeepGmlIdAsExternalReference()) return true;
		if (impIdRadioAdd.isSelected() != gmlId.isUUIDModeComplement()) return true;
		if (impIdRadioExchange.isSelected() != gmlId.isUUIDModeReplace()) return true;
		if (impIdCSRadioFile.isSelected() != gmlId.isSetRelativeCodeSpaceMode()) return true;
		if (impIdCSRadioFilePath.isSelected() != gmlId.isSetAbsoluteCodeSpaceMode()) return true;
		if (impIdCSRadioUser.isSelected() != gmlId.isSetUserCodeSpaceMode()) return true;
		return false;
	}

	private void initGui(){
		impIdRadioAdd = new JRadioButton("");
		impIdRadioExchange = new JRadioButton("");
		ButtonGroup impIdRadio = new ButtonGroup();
		impIdRadio.add(impIdRadioAdd);
		impIdRadio.add(impIdRadioExchange);
		impIdCheckExtRef = new JCheckBox("");
		impIdCSRadioFile = new JRadioButton("");
		impIdCSRadioFilePath = new JRadioButton("");
		impIdCSRadioUser = new JRadioButton("");
		ButtonGroup impIdCSRadio = new ButtonGroup();
		impIdCSRadio.add(impIdCSRadioFile);
		impIdCSRadio.add(impIdCSRadioFilePath);
		impIdCSRadio.add(impIdCSRadioUser);
		impIdCSUserText = new JTextField("");

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			impIdRadioAdd.setIconTextGap(10);
			impIdRadioExchange.setIconTextGap(10);
			impIdCheckExtRef.setIconTextGap(10);
			int lmargin = (int)(impIdRadioAdd.getPreferredSize().getWidth()) + 11;
			{
				block1.add(impIdRadioAdd, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impIdRadioExchange, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impIdCheckExtRef, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));		
			}

			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			impIdCSRadioFile.setIconTextGap(10);
			impIdCSRadioFilePath.setIconTextGap(10);
			impIdCSRadioUser.setIconTextGap(10);
			{
				block2.add(impIdCSRadioFile, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impIdCSRadioFilePath, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impIdCSRadioUser, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impIdCSUserText, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
			}
		}
		
		ActionListener gmlIdListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledGmlId();
			}
		};
		
		ActionListener codeSpaceListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledCodeSpace();
			}
		};
		
		impIdRadioAdd.addActionListener(gmlIdListener);
		impIdRadioExchange.addActionListener(gmlIdListener);
		
		impIdCSRadioFile.addActionListener(codeSpaceListener);
		impIdCSRadioFilePath.addActionListener(codeSpaceListener);
		impIdCSRadioUser.addActionListener(codeSpaceListener);
	}
	
	private void setEnabledGmlId() {
		impIdCheckExtRef.setEnabled(impIdRadioExchange.isSelected());
	}
	
	private void setEnabledCodeSpace() {
		impIdCSUserText.setEnabled(impIdCSRadioUser.isSelected());
	}

	@Override
	public void doTranslation() {
		block2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.import.idHandling.border.idCodeSpace")));
		impIdCSRadioFile.setText(Internal.I18N.getString("pref.import.idHandling.label.idCodeSpace.file"));
		impIdCSRadioFilePath.setText(Internal.I18N.getString("pref.import.idHandling.label.idCodeSpace.filePath"));
		impIdCSRadioUser.setText(Internal.I18N.getString("pref.import.idHandling.label.idCodeSpace.user"));
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.import.idHandling.border.idAllocation")));
		impIdCheckExtRef.setText(Internal.I18N.getString("pref.import.idHandling.label.idAllocation.extReference"));
		impIdRadioAdd.setText(Internal.I18N.getString("pref.import.idHandling.label.idAllocation.add"));
		impIdRadioExchange.setText(Internal.I18N.getString("pref.import.idHandling.label.idAllocation.exchange"));
	}

	@Override
	public void loadSettings() {
		ImportGmlId gmlId = config.getProject().getImporter().getGmlId();
		
		if (gmlId.isSetUserCodeSpaceMode())
			impIdCSRadioUser.setSelected(true);
		else if (gmlId.isSetRelativeCodeSpaceMode())
			impIdCSRadioFile.setSelected(true);
		else
			impIdCSRadioFilePath.setSelected(true);

		impIdCSUserText.setText(gmlId.getCodeSpace());

		if (gmlId.isUUIDModeReplace())
			impIdRadioExchange.setSelected(true);
		else
			impIdRadioAdd.setSelected(true);

		impIdCheckExtRef.setSelected(gmlId.isSetKeepGmlIdAsExternalReference());
		
		setEnabledGmlId();
		setEnabledCodeSpace();
	}

	@Override
	public void setSettings() {
		ImportGmlId gmlId = config.getProject().getImporter().getGmlId();
		
		if (impIdCSRadioFile.isSelected()) {
			gmlId.setCodeSpaceMode(CodeSpaceMode.RELATIVE);
		}
		else if (impIdCSRadioFilePath.isSelected()) {
			gmlId.setCodeSpaceMode(CodeSpaceMode.ABSOLUTE);
		}
		else {
			gmlId.setCodeSpaceMode(CodeSpaceMode.USER);
		}
		gmlId.setCodeSpace(impIdCSUserText.getText());

		if (impIdRadioAdd.isSelected()) {
			gmlId.setUuidMode(UUIDMode.COMPLEMENT);
		}
		else {
			gmlId.setUuidMode(UUIDMode.REPLACE);
		}

		gmlId.setKeepGmlIdAsExternalReference(impIdCheckExtRef.isSelected());
	}

}

