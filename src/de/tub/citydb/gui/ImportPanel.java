package de.tub.citydb.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.tub.citydb.config.Config;

public class ImportPanel extends JPanel {

	private JTextArea browseText;
	private JButton browseButton;
	private JButton importButton;
	private FilterPanel filterPanel;
	private JTextField workspaceText;

	private JPanel row2;
	private JLabel row2_1;

	private Config config;

	public ImportPanel(Config config) {
		this.config = config;
		initGui();
	}

	private void initGui() {

		//gui-elemente anlegen
		browseText = new JTextArea("");
		browseButton = new JButton("");
		filterPanel = new FilterPanel(config.getProject().getImporter().getFilter());
		importButton = new JButton("");
		workspaceText = new JTextField("");

		//gui-elemente mit funktionalität ausstatten
		browseText.setFont(workspaceText.getFont());
		browseText.setRows(6);

		browseText.setTransferHandler(new ImportDropHandler(browseText));
		setTransferHandler(new ImportDropHandler(browseText));
		
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile();
			}
		});

		//layout
		setLayout(new GridBagLayout());
		{
			JPanel row1 = new JPanel();
			add(row1,GuiUtil.setConstraints(0,0,1.0,.3,GridBagConstraints.BOTH,10,5,5,10));
			row1.setLayout(new GridBagLayout());
			{
				row1.add(new JScrollPane(browseText), GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
				row1.add(browseButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));
			}
		}
		{
			row2 = new JPanel();
			add(row2, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
			row2.setBorder(BorderFactory.createTitledBorder(""));
			row2.setLayout(new GridBagLayout());
			row2_1 = new JLabel();
			{
				row2.add(row2_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
				row2.add(workspaceText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
			}
		}
		{
			add(filterPanel, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
		}
		{
			JPanel row3 = new JPanel();
			add(row3, GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
			row3.add(importButton);
		}
		this.setEnabled(false);
	}

	public void doTranslation() {
		browseButton.setText(ImpExpGui.labels.getString("common.button.browse"));
		importButton.setText(ImpExpGui.labels.getString("import.button.import"));
		row2.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("common.border.versioning")));
		row2_1.setText(ImpExpGui.labels.getString("common.label.workspace"));

		filterPanel.doTranslation();
	}


	//public-methoden
	public void loadSettings() {
		browseText.setText(config.getInternal().getImportFileName());
		workspaceText.setText(config.getProject().getDatabase().getWorkspace().getImportWorkspace());
		filterPanel.loadSettings();
	}

	public void setSettings() {
		config.getInternal().setImportFileName(browseText.getText());
		config.getProject().getDatabase().getWorkspace().setImportWorkspace(workspaceText.getText());
		filterPanel.setSettings();
	}

	public JButton getImportButton() {
		return importButton;
	}

	// private-methoden
	private void loadFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CityGML Files (*.gml, *.xml)", "xml", "gml");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);
		String importString = browseText.getText();
		//wenn Textfeld leer ist
		if (importString.isEmpty()) {
			if (config.getProject().getImporter().getPath().isSetLastUsedMode()) {
				chooser.setCurrentDirectory(new File(config.getProject().getImporter().getPath().getLastUsedPath()));
			} else {
				chooser.setCurrentDirectory(new File(config.getProject().getImporter().getPath().getStandardPath()));
			}
		}
		//wenn Textfeld nicht leer ist
		else {
			String[] importSubstrings = null;
			importSubstrings = importString.split(", ");
			File importFiles[] = new File[importSubstrings.length];
			for (int i = 0; i < importSubstrings.length; i++) importFiles[i] = new File(importSubstrings[i]);
			chooser.setSelectedFiles(importFiles);
		}
		int result = chooser.showOpenDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;
		try {
			importString = "";
			File[] importFiles2 = chooser.getSelectedFiles();
			int i;
			for (i = 0; i < importFiles2.length; i++) {
				importString += importFiles2[i].toString();
				if (i != importFiles2.length-1) importString += "\n";
			}
			browseText.setText(importString);
			config.getProject().getImporter().getPath().setLastUsedPath(chooser.getCurrentDirectory().getAbsolutePath());
		}
		catch (Exception e) {
			browseText.setText("did not work" + e);
		}
	}

	//eigenständige klasse importdrophandler
	private static class ImportDropHandler extends TransferHandler {
		public ImportDropHandler(JTextArea textAreaName) {
			target = textAreaName;
		}

		JTextArea target;
		public boolean canImport(JComponent dest, DataFlavor[] flavors) {
			// prüft ob eines der elemente eine fileliste ist
			for (int i = 0; i < flavors.length; i++) {
				if (flavors[i].isFlavorJavaFileListType()) return true;
			}
			return false;
		}

		public boolean importData(JComponent src, Transferable transferable) {
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			DataFlavor listFlavor = null;
			for (int i = 0; i < flavors.length; i++) {
				if (flavors[i].isFlavorJavaFileListType()) {
					listFlavor = flavors[i];
					break;
				}
			}
			if (listFlavor == null) return false;
			try {
				List list = (List)transferable.getTransferData(listFlavor);
				String importString = "";
				for (int i = 0; i < list.size(); i++) {
					File temp = new File(list.get(i).toString());
					if (temp.isFile()) {
						importString += list.get(i).toString();
						if (i<list.size()-1) importString += "\n";
					}
				}
				target.setText(importString);
			} catch (Exception e) {
				return false;
			}
			
			return true;
		}
	}
}

