/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.gui.panel.importer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.components.StandardEditingPopupMenuDecorator;
import de.tub.citydb.gui.panel.filter.FilterPanel;
import de.tub.citydb.gui.panel.filter.FilterPanel.FilterPanelType;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.log.Logger;

@SuppressWarnings("serial")
public class ImportPanel extends JPanel {
	private final Logger LOG = Logger.getInstance();

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
		fileList = new JList();		
		browseButton = new JButton();
		removeButton = new JButton();
		filterPanel = new FilterPanel(config, FilterPanelType.IMPORT);
		importButton = new JButton();
		validateButton = new JButton();
		workspaceText = new JTextField();

		DropCutCopyPasteHandler handler = new DropCutCopyPasteHandler();
		
		fileListModel = new DefaultListModel();
		fileList.setModel(fileListModel);
		fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fileList.setTransferHandler(handler);

		DropTarget dropTarget = new DropTarget(fileList, handler);
		fileList.setDropTarget(dropTarget);
		setDropTarget(dropTarget);

		ActionMap actionMap = fileList.getActionMap();
		actionMap.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
		actionMap.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
		actionMap.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());

		InputMap inputMap = fileList.getInputMap();
		inputMap.put(KeyStroke.getKeyStroke('X', InputEvent.CTRL_MASK), TransferHandler.getCutAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK), TransferHandler.getCopyAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke('V', InputEvent.CTRL_MASK), TransferHandler.getPasteAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), TransferHandler.getCutAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), TransferHandler.getCutAction().getValue(Action.NAME));

		StandardEditingPopupMenuDecorator.decorate(fileList, workspaceText);
		
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile(Internal.I18N.getString("main.tabbedPane.import"));
			}
		});

		removeButton.setActionCommand((String)TransferHandler.getCutAction().getValue(Action.NAME));
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String action = (String)e.getActionCommand();
				Action a = fileList.getActionMap().get(action);
				if (a != null)
					a.actionPerformed(new ActionEvent(fileList, ActionEvent.ACTION_PERFORMED, null));
			}
		});

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

	// JList handler for drop, cut, copy, and paste support
	private final class DropCutCopyPasteHandler extends TransferHandler implements DropTargetListener {

		@Override
		public boolean importData(TransferHandler.TransferSupport info) {	    	
			if (!info.isDataFlavorSupported(DataFlavor.stringFlavor))
				return false;

			if (info.isDrop())
				return false;

			List<String> fileNames = new ArrayList<String>();
			try {
				String value = (String)info.getTransferable().getTransferData(DataFlavor.stringFlavor);
				StringTokenizer t = new StringTokenizer(value, System.getProperty("line.separator"));

				while (t.hasMoreTokens()) {
					File file = new File(t.nextToken());
					if (file.exists())
						fileNames.add(file.getCanonicalPath());
					else
						LOG.warn("Failed to paste from clipboard: '" + file.getAbsolutePath() + "' is not a file.");
				}

				if (!fileNames.isEmpty()) {
					addFileNames(fileNames);
					return true;
				}
			} catch (UnsupportedFlavorException ufe) {
				//
			} catch (IOException ioe) {
				//
			}

			return false;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			int[] indices = fileList.getSelectedIndices();
			String newLine = System.getProperty("line.separator");

			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < indices.length; i++) {
				builder.append((String)fileList.getModel().getElementAt(indices[i]));
				if (i < indices.length - 1)
					builder.append(newLine);
			}

			return new StringSelection(builder.toString());
		}

		@Override
		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE;
		}

		@Override
		protected void exportDone(JComponent c, Transferable data, int action) {
			if (action != MOVE)
				return;

			int[] indices = fileList.getSelectedIndices();
			int first = indices[0];		

			for (int i = indices.length - 1; i >= 0; i--)
				fileListModel.remove(indices[i]);

			if (first > fileListModel.size() - 1)
				first = fileListModel.size() - 1;

			fileList.setSelectedIndex(first);
		}
		
		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
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
							if (file.exists())
								fileNames.add(file.getCanonicalPath());
							else
								LOG.warn("Failed to drop from clipboard: '" + file.getAbsolutePath() + "' is not a file.");

						if (!fileNames.isEmpty()) {
							if (dtde.getDropAction() != DnDConstants.ACTION_COPY)
								fileListModel.clear();

							addFileNames(fileNames);
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

		private void addFileNames(List<String> fileNames) {
			int index = fileList.getSelectedIndex() + 1;
			for (String fileName : fileNames)
				fileListModel.add(index++, fileName);

			config.getProject().getImporter().getPath().setLastUsedPath(
					new File(fileListModel.getElementAt(0).toString()).getAbsolutePath());
		}
		
		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
			// nothing to do here
		}
		
		@Override
		public void dragExit(DropTargetEvent dte) {
			// nothing to do here
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			// nothing to do here
		}
	}
}