package de.tub.citydb.gui.panel.importer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.panel.filter.FilterPanel;
import de.tub.citydb.gui.panel.filter.FilterPanel.FilterPanelType;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class ImportPanel extends JPanel implements DropTargetListener {

	private JList fileList;
	private DefaultListModel fileListModel;
	private JButton browseButton;
	private JButton removeButton;
	private JButton importButton;
	private JButton validateButton;
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
		fileList = new JList();		
		browseButton = new JButton("");
		removeButton = new JButton("");
		filterPanel = new FilterPanel(config, FilterPanelType.IMPORT);
		importButton = new JButton("");
		validateButton = new JButton("");
		workspaceText = new JTextField("");

		fileListModel = new DefaultListModel();
		fileList.setModel(fileListModel);
		fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		DropTarget dropTarget = new DropTarget(fileList, this);
		fileList.setDropTarget(dropTarget);
		setDropTarget(dropTarget);

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile(Internal.I18N.getString("main.tabbedPane.import"));
			}
		});

		Action remove = new RemoveAction();
		fileList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove");
		fileList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "remove");
		fileList.getActionMap().put("remove", remove);
		removeButton.addActionListener(remove);

		//layout
		setLayout(new GridBagLayout());
		{
			JPanel row1 = new JPanel();
			JPanel buttons = new JPanel();
			add(row1,GuiUtil.setConstraints(0,0,1.0,.3,GridBagConstraints.BOTH,10,5,5,5));
			row1.setLayout(new GridBagLayout());
			{
				row1.add(new JScrollPane(fileList), GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
				row1.add(buttons, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
				buttons.setLayout(new GridBagLayout());
				{
					buttons.add(browseButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
					GridBagConstraints c = GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.HORIZONTAL,5,0,5,0);
					c.anchor = GridBagConstraints.NORTH;
					buttons.add(removeButton, c);
				}
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
			row3.setLayout(new GridBagLayout());
			{
				GridBagConstraints c = GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.NONE,5,5,5,5);
				c.gridwidth = 2;
				row3.add(importButton, c);				

				c = GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,5,5,5,0);
				c.anchor = GridBagConstraints.EAST;
				row3.add(validateButton, c);
			}
		}
	}

	public void doTranslation() {
		browseButton.setText(Internal.I18N.getString("common.button.browse"));
		removeButton.setText(Internal.I18N.getString("import.button.remove"));
		importButton.setText(Internal.I18N.getString("import.button.import"));
		validateButton.setText(Internal.I18N.getString("import.button.validate"));
		row2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("common.border.versioning")));
		row2_1.setText(Internal.I18N.getString("common.label.workspace"));

		filterPanel.doTranslation();
	}

	//public-methoden
	public void loadSettings() {
		workspaceText.setText(config.getProject().getDatabase().getWorkspaces().getImportWorkspace().getName());
		filterPanel.loadSettings();
	}

	public void setSettings() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fileListModel.size(); ++i) {
			builder.append(fileListModel.get(i).toString());
			builder.append("\n");
		}

		config.getInternal().setImportFileName(builder.toString());		
		
		if (workspaceText.getText().trim().length() == 0)
			workspaceText.setText("LIVE");
		
		config.getProject().getDatabase().getWorkspaces().getImportWorkspace().setName(workspaceText.getText());
		filterPanel.setSettings();
	}

	public JButton getImportButton() {
		return importButton;
	}

	public JButton getValidateButton() {
		return validateButton;
	}

	private void loadFile(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		FileNameExtensionFilter filter = new FileNameExtensionFilter("CityGML Files (*.gml, *.xml)", "xml", "gml");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);

		if (fileListModel.isEmpty()) {
			if (config.getProject().getImporter().getPath().isSetLastUsedMode()) {
				chooser.setCurrentDirectory(new File(config.getProject().getImporter().getPath().getLastUsedPath()));
			} else {
				chooser.setCurrentDirectory(new File(config.getProject().getImporter().getPath().getStandardPath()));
			}
		} else
			chooser.setCurrentDirectory(new File(fileListModel.get(0).toString()));

		int result = chooser.showOpenDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) 
			return;

		fileListModel.clear();
		for (File file : chooser.getSelectedFiles())
			fileListModel.addElement(file.toString());

		config.getProject().getImporter().getPath().setLastUsedPath(chooser.getCurrentDirectory().getAbsolutePath());
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		// nothing to do here
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		// nothing to do here
	}

	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		for (DataFlavor dataFlover : dtde.getCurrentDataFlavors()) {
			if (dataFlover.isFlavorJavaFileListType()) {
				try {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					List<String> fileNames = new ArrayList<String>();
					for (File file : (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor))
						if (file.canRead())
							fileNames.add(file.getCanonicalPath());

					if (!fileNames.isEmpty()) {
						if (dtde.getDropAction() != DnDConstants.ACTION_COPY)
							fileListModel.clear();

						for (String fileName : fileNames)
							fileListModel.addElement(fileName); 

						config.getProject().getImporter().getPath().setLastUsedPath(
								new File(fileListModel.getElementAt(fileListModel.size() - 1).toString()).getAbsolutePath());
					}
					
					dtde.getDropTargetContext().dropComplete(true);	
				} catch (UnsupportedFlavorException e1) {
					//
				} catch (IOException e2) {
					//
				}
			}
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// nothing to do here
	}

	private final class RemoveAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (fileList.getSelectedIndices().length > 0) {
				int[] selectedIndices = fileList.getSelectedIndices();
				int firstSelected = selectedIndices[0];		

				for (int i = selectedIndices.length - 1; i >= 0; --i) 
					fileListModel.removeElementAt(selectedIndices[i]);

				if (firstSelected > fileListModel.size() - 1)
					firstSelected = fileListModel.size() - 1;

				fileList.setSelectedIndex(firstSelected);
			}
		}		
	}
}