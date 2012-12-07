/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
package de.tub.citydb.modules.citygml.importer.gui.view;

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
import java.util.concurrent.locks.ReentrantLock;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.citygml4j.builder.jaxb.JAXBBuilder;

import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.importer.ImportFilterConfig;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.ImportStatusDialog;
import de.tub.citydb.gui.components.XMLValidationStatusDialog;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.gui.view.FilterPanel;
import de.tub.citydb.modules.citygml.common.gui.view.FilterPanel.FilterPanelType;
import de.tub.citydb.modules.citygml.importer.controller.Importer;
import de.tub.citydb.modules.citygml.importer.controller.XMLValidator;
import de.tub.citydb.modules.common.event.InterruptEnum;
import de.tub.citydb.modules.common.event.InterruptEvent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ImportPanel extends JPanel {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final JAXBBuilder jaxbBuilder;
	private final Config config;
	private final ImpExpGui mainView;
	private final DatabaseConnectionPool dbPool;

	private JList fileList;
	private DefaultListModel fileListModel;
	private JButton browseButton;
	private JButton removeButton;
	private JButton importButton;
	private JButton validateButton;
	private FilterPanel filterPanel;
	private JTextField workspaceText;

	private JPanel workspacePanel;
	private JLabel row2_1;

	public ImportPanel(JAXBBuilder jaxbBuilder, Config config, ImpExpGui mainView) {
		this.jaxbBuilder = jaxbBuilder;
		this.config = config;
		this.mainView = mainView;
		dbPool = DatabaseConnectionPool.getInstance();

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

		PopupMenuDecorator.getInstance().decorate(fileList, workspaceText);

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
		removeButton.setEnabled(false);

		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						doImport();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		validateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						doValidate();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		fileList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting())
					removeButton.setEnabled(true);
			}
		});

		setLayout(new GridBagLayout());

		JPanel filePanel = new JPanel();
		JPanel fileButton = new JPanel();
		add(filePanel,GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,10,5,5,5));
		filePanel.setLayout(new GridBagLayout());
		JScrollPane fileScroll = new JScrollPane(fileList);
		fileScroll.setPreferredSize(fileScroll.getPreferredSize());

		filePanel.add(fileScroll, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
		filePanel.add(fileButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
		fileButton.setLayout(new GridBagLayout());
		fileButton.add(browseButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
		fileButton.add(removeButton, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,5,0,15,0));

		JPanel view = new JPanel();
		view.setLayout(new GridBagLayout());
		JScrollPane scrollPane = new JScrollPane(view);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		add(scrollPane, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));

		workspacePanel = new JPanel();
		view.add(workspacePanel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
		workspacePanel.setBorder(BorderFactory.createTitledBorder(""));
		workspacePanel.setLayout(new GridBagLayout());
		row2_1 = new JLabel();
		workspacePanel.add(row2_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
		workspacePanel.add(workspaceText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));

		view.add(filterPanel, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,0,5,0,5));

		JPanel buttonPanel = new JPanel();
		add(buttonPanel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));	
		buttonPanel.setLayout(new GridBagLayout());
		buttonPanel.add(importButton, GuiUtil.setConstraints(0,0,2,1,1.0,0.0,GridBagConstraints.NONE,5,5,5,5));				
		buttonPanel.add(validateButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,5,5,5,0));
	}

	public void doTranslation() {
		browseButton.setText(Internal.I18N.getString("common.button.browse"));
		removeButton.setText(Internal.I18N.getString("import.button.remove"));
		importButton.setText(Internal.I18N.getString("import.button.import"));
		validateButton.setText(Internal.I18N.getString("import.button.validate"));
		((TitledBorder)workspacePanel.getBorder()).setTitle(Internal.I18N.getString("common.border.versioning"));
		row2_1.setText(Internal.I18N.getString("common.label.workspace"));

		filterPanel.doTranslation();
	}

	public void loadSettings() {
		workspaceText.setText(config.getProject().getDatabase().getWorkspaces().getImportWorkspace().getName());
		filterPanel.loadSettings();
	}

	public void setSettings() {
		File[] importFiles = new File[fileListModel.size()]; 
		for (int i = 0; i < fileListModel.size(); ++i)
			importFiles[i] = new File(fileListModel.get(i).toString());

		config.getInternal().setImportFiles(importFiles);		

		String workspace = workspaceText.getText().trim();
		if (!workspace.equals(Internal.ORACLE_DEFAULT_WORKSPACE) && 
				(workspace.length() == 0 || workspace.toUpperCase().equals(Internal.ORACLE_DEFAULT_WORKSPACE)))
			workspaceText.setText(Internal.ORACLE_DEFAULT_WORKSPACE);

		config.getProject().getDatabase().getWorkspaces().getImportWorkspace().setName(workspaceText.getText());
		filterPanel.setSettings();
	}

	private void doImport() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			mainView.clearConsole();
			setSettings();

			ImportFilterConfig filter = config.getProject().getImporter().getFilter();

			// check all input values...
			if (config.getInternal().getImportFiles() == null || config.getInternal().getImportFiles().length == 0) {
				mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incompleteData"), 
						Internal.I18N.getString("import.dialog.error.incompleteData.dataset"));
				return;
			}

			// gmlId
			if (filter.isSetSimpleFilter() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// cityObject
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getFeatureCount().isSet()) {
				Long coStart = filter.getComplexFilter().getFeatureCount().getFrom();
				Long coEnd = filter.getComplexFilter().getFeatureCount().getTo();
				String coEndValue = String.valueOf(filter.getComplexFilter().getFeatureCount().getTo());

				if (coStart == null || (!coEndValue.trim().equals("") && coEnd == null)) {
					mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"), 
							Internal.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}

				if ((coStart != null && coStart <= 0) || (coEnd != null && coEnd <= 0)) {
					mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
							Internal.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}

				if (coEnd != null && coEnd < coStart) {
					mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
							Internal.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}
			}

			// gmlName
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getGmlName().isSet() &&
					filter.getComplexFilter().getGmlName().getValue().trim().equals("")) {
				mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlName"));
				return;
			}

			// BoundingBox
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getBoundingBox().isSet()) {
				Double xMin = filter.getComplexFilter().getBoundingBox().getLowerLeftCorner().getX();
				Double yMin = filter.getComplexFilter().getBoundingBox().getLowerLeftCorner().getY();
				Double xMax = filter.getComplexFilter().getBoundingBox().getUpperRightCorner().getX();
				Double yMax = filter.getComplexFilter().getBoundingBox().getUpperRightCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
							Internal.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}
			}

			// affine transformation
			if (config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation()) {
				if (JOptionPane.showConfirmDialog(
						mainView, 
						Internal.I18N.getString("import.dialog.warning.affineTransformation"),
						Internal.I18N.getString("common.dialog.warning.title"), 
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
					return;				
			}

			if (!dbPool.isConnected()) {
				mainView.connectToDatabase();

				if (!dbPool.isConnected())
					return;
			}

			mainView.setStatusText(Internal.I18N.getString("main.status.import.label"));
			LOG.info("Initializing database import...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final ImportStatusDialog importDialog = new ImportStatusDialog(mainView, 
					Internal.I18N.getString("import.dialog.window"), 
					Internal.I18N.getString("import.dialog.msg"));

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					importDialog.setLocationRelativeTo(mainView);
					importDialog.setVisible(true);
				}
			});

			Importer importer = new Importer(jaxbBuilder, dbPool, config, eventDispatcher);

			importDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of database import.", 
									LogLevel.INFO, 
									this));
						}
					});
				}
			});

			boolean success = importer.doProcess();

			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					importDialog.dispose();
				}
			});

			// cleanup
			importer.cleanup();

			if (success) {
				LOG.info("Database import successfully finished.");
			} else {
				LOG.warn("Database import aborted.");
			}

			mainView.setStatusText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void doValidate() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			mainView.clearConsole();
			setSettings();

			// check for input files...
			if (config.getInternal().getImportFiles() == null || config.getInternal().getImportFiles().length == 0) {
				mainView.errorMessage(Internal.I18N.getString("validate.dialog.error.incompleteData"),
						Internal.I18N.getString("validate.dialog.error.incompleteData.dataset"));
				return;
			}

			mainView.setStatusText(Internal.I18N.getString("main.status.validate.label"));
			LOG.info("Initializing XML validation...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final XMLValidationStatusDialog validatorDialog = new XMLValidationStatusDialog(mainView, 
					Internal.I18N.getString("validate.dialog.window"), 
					Internal.I18N.getString("validate.dialog.title"), 
					" ", 
					Internal.I18N.getString("validate.dialog.details") , 
					true, 
					eventDispatcher);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					validatorDialog.setLocationRelativeTo(mainView);
					validatorDialog.setVisible(true);
				}
			});

			XMLValidator validator = new XMLValidator(jaxbBuilder, config, eventDispatcher);

			validatorDialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of XML validation.", 
									LogLevel.INFO, 
									this));
						}
					});
				}
			});

			boolean success = validator.doProcess();

			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					validatorDialog.dispose();
				}
			});

			// cleanup
			validator.cleanup();

			if (success) {
				LOG.info("XML validation finished.");
			} else {
				LOG.warn("XML validation aborted.");
			}

			mainView.setStatusText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
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

			if (!fileList.isSelectionEmpty()) {
				int[] indices = fileList.getSelectedIndices();
				int first = indices[0];		

				for (int i = indices.length - 1; i >= 0; i--)
					fileListModel.remove(indices[i]);

				if (first > fileListModel.size() - 1)
					first = fileListModel.size() - 1;

				if (fileListModel.isEmpty())
					removeButton.setEnabled(false);
				else
					fileList.setSelectedIndex(first);
			}
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
