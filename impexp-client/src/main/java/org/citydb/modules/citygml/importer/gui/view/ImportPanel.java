/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.modules.citygml.importer.gui.view;

import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.controller.Importer;
import org.citydb.citygml.importer.controller.XMLValidator;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.importer.ImportFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilterMode;
import org.citydb.database.DatabaseController;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.version.DatabaseVersionException;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.EventType;
import org.citydb.event.global.InterruptEvent;
import org.citydb.gui.components.dialog.ImportStatusDialog;
import org.citydb.gui.components.dialog.XMLValidationStatusDialog;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public class ImportPanel extends JPanel implements EventHandler {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final CityGMLBuilder cityGMLBuilder;
	private final ViewController viewController;
	private final DatabaseController databaseController;
	private final Config config;

	private JList<String> fileList;
	private DefaultListModel<String> fileListModel;
	private JButton browseButton;
	private JButton removeButton;
	private JButton importButton;
	private JButton validateButton;
	private FilterPanel filterPanel;
	private JXTextField workspaceText;

	private JPanel workspacePanel;
	private JLabel workspaceLabel;

	public ImportPanel(ViewController viewController, Config config) {
		this.config = config;
		this.viewController = viewController;

		databaseController = ObjectRegistry.getInstance().getDatabaseController();
		cityGMLBuilder = ObjectRegistry.getInstance().getCityGMLBuilder();
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);

		initGui();
	}

	private void initGui() {
		fileList = new JList<String>();		
		browseButton = new JButton();
		removeButton = new JButton();
		filterPanel = new FilterPanel(viewController, config);
		importButton = new JButton();
		validateButton = new JButton();
		workspaceText = new JXTextField();
		workspaceText.setPromptForeground(Color.LIGHT_GRAY);
		workspaceText.setFocusBehavior(FocusBehavior.SHOW_PROMPT);

		DropCutCopyPasteHandler handler = new DropCutCopyPasteHandler();

		fileListModel = new DefaultListModel<String>();
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

		browseButton.addActionListener(e -> loadFile(Language.I18N.getString("main.tabbedPane.import")));

		removeButton.setActionCommand((String)TransferHandler.getCutAction().getValue(Action.NAME));
		removeButton.addActionListener(e -> {
			String action = (String)e.getActionCommand();
			Action a = fileList.getActionMap().get(action);
			if (a != null)
				a.actionPerformed(new ActionEvent(fileList, ActionEvent.ACTION_PERFORMED, null));
		});
		removeButton.setEnabled(false);

		importButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				doImport();
				return null;
			}
		}.execute());

		validateButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				doValidate();
				return null;
			}
		}.execute());

		fileList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
				removeButton.setEnabled(true);
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
		workspaceLabel = new JLabel();
		workspacePanel.add(workspaceLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		workspacePanel.add(workspaceText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));

		view.add(filterPanel, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,0,5,0,5));

		JPanel buttonPanel = new JPanel();
		add(buttonPanel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));	
		buttonPanel.setLayout(new GridBagLayout());
		buttonPanel.add(importButton, GuiUtil.setConstraints(0,0,2,1,1.0,0.0,GridBagConstraints.NONE,5,5,5,5));				
		buttonPanel.add(validateButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,5,5,5,0));
	}

	public void setEnabledWorkspace(boolean enable) {
		((TitledBorder)workspacePanel.getBorder()).setTitleColor(enable ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		workspacePanel.repaint();

		workspaceLabel.setEnabled(enable);
		workspaceText.setEnabled(enable);
	}

	public void doTranslation() {
		browseButton.setText(Language.I18N.getString("common.button.browse"));
		removeButton.setText(Language.I18N.getString("import.button.remove"));
		importButton.setText(Language.I18N.getString("import.button.import"));
		validateButton.setText(Language.I18N.getString("import.button.validate"));
		workspaceLabel.setText(Language.I18N.getString("common.label.workspace"));
		workspaceText.setPrompt(Language.I18N.getString("common.label.workspace.prompt"));
		((TitledBorder)workspacePanel.getBorder()).setTitle(Language.I18N.getString("common.border.versioning"));
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
		config.getProject().getDatabase().getWorkspaces().getImportWorkspace().setName(workspaceText.getText().trim());

		filterPanel.setSettings();
	}

	private void doImport() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewController.clearConsole();
			setSettings();

			ImportFilter filter = config.getProject().getImporter().getFilter();

			// check all input values...
			if (config.getInternal().getImportFiles() == null || config.getInternal().getImportFiles().length == 0) {
				viewController.errorMessage(Language.I18N.getString("import.dialog.error.incompleteData"), 
						Language.I18N.getString("import.dialog.error.incompleteData.dataset"));
				return;
			}

			// gmlId
			if (filter.getMode() == SimpleSelectionFilterMode.SIMPLE
					&& !filter.getFilter().getGmlIdFilter().isSetResourceIds()) {
				viewController.errorMessage(Language.I18N.getString("import.dialog.error.incorrectData"), 
						Language.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// cityObject
			if (filter.getMode() == SimpleSelectionFilterMode.COMPLEX
					&& filter.isUseCountFilter()) {
				Long coStart = filter.getCounterFilter().getLowerLimit();
				Long coEnd = filter.getCounterFilter().getUpperLimit();

				if (coStart == null || coEnd == null) {
					viewController.errorMessage(Language.I18N.getString("import.dialog.error.incorrectData"), 
							Language.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}

				if ((coStart != null && coStart <= 0) || (coEnd != null && coEnd <= 0)) {
					viewController.errorMessage(Language.I18N.getString("import.dialog.error.incorrectData"),
							Language.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}

				if (coEnd != null && coEnd < coStart) {
					viewController.errorMessage(Language.I18N.getString("import.dialog.error.incorrectData"),
							Language.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}
			}

			// gmlName
			if (filter.getMode() == SimpleSelectionFilterMode.COMPLEX
					&& filter.isUseGmlNameFilter()
					&& (!filter.getFilter().getGmlNameFilter().isSetLiteral()
							|| filter.getFilter().getGmlNameFilter().getLiteral().trim().equals(""))) {
				viewController.errorMessage(Language.I18N.getString("import.dialog.error.incorrectData"),
						Language.I18N.getString("common.dialog.error.incorrectData.gmlName"));
				return;
			}

			// BoundingBox
			if (filter.getMode() == SimpleSelectionFilterMode.COMPLEX
					&& filter.isUseBboxFilter()) {
				BoundingBox bbox = (BoundingBox)filter.getFilter().getBboxFilter().getEnvelope();
				Double xMin = bbox.getLowerCorner().getX();
				Double yMin = bbox.getLowerCorner().getY();
				Double xMax = bbox.getUpperCorner().getX();
				Double yMax = bbox.getUpperCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					viewController.errorMessage(Language.I18N.getString("import.dialog.error.incorrectData"),
							Language.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}
			}

			// feature types
			if (filter.getMode() == SimpleSelectionFilterMode.COMPLEX 
					&& filter.isUseTypeNames()
					&& filter.getFeatureTypeFilter().getTypeNames().isEmpty()) {
				viewController.errorMessage(Language.I18N.getString("import.dialog.error.incorrectData"),
						Language.I18N.getString("common.dialog.error.incorrectData.featureClass"));
				return;
			}

			// affine transformation
			if (config.getProject().getImporter().getAffineTransformation().isEnabled()) {
				if (JOptionPane.showConfirmDialog(
						viewController.getTopFrame(), 
						Language.I18N.getString("import.dialog.warning.affineTransformation"),
						Language.I18N.getString("common.dialog.warning.title"), 
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
					return;				
			}

			if (!databaseController.isConnected()) {
				try {
					databaseController.connect(true);
				} catch (DatabaseConfigurationException | DatabaseVersionException | SQLException e) {
					return;
				}
			}

			viewController.setStatusText(Language.I18N.getString("main.status.import.label"));
			LOG.info("Initializing database import...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final ImportStatusDialog importDialog = new ImportStatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("import.dialog.window"), 
					Language.I18N.getString("import.dialog.msg"));

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					importDialog.setLocationRelativeTo(viewController.getTopFrame());
					importDialog.setVisible(true);
				}
			});

			// get schema mapping
			final SchemaMapping schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();

			Importer importer = new Importer(cityGMLBuilder, schemaMapping, config, eventDispatcher);

			importDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									"User abort of database import.", 
									LogLevel.WARN, 
									Event.GLOBAL_CHANNEL,
									this));
						}
					});
				}
			});

			boolean success = false;
			try {
				success = importer.doProcess();
			} catch (CityGMLImportException e) {
				LOG.error(e.getMessage());

				Throwable cause = e.getCause();
				while (cause != null) {
					LOG.error("Cause: " + cause.getMessage());
					cause = cause.getCause();
				}
			}

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

			viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void doValidate() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewController.clearConsole();
			setSettings();

			// check for input files...
			if (config.getInternal().getImportFiles() == null || config.getInternal().getImportFiles().length == 0) {
				viewController.errorMessage(Language.I18N.getString("validate.dialog.error.incompleteData"),
						Language.I18N.getString("validate.dialog.error.incompleteData.dataset"));
				return;
			}

			viewController.setStatusText(Language.I18N.getString("main.status.validate.label"));
			LOG.info("Initializing XML validation...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final XMLValidationStatusDialog validatorDialog = new XMLValidationStatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("validate.dialog.window"), 
					Language.I18N.getString("validate.dialog.title"), 
					" ", 
					Language.I18N.getString("validate.dialog.details") , 
					true, 
					eventDispatcher);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					validatorDialog.setLocationRelativeTo(viewController.getTopFrame());
					validatorDialog.setVisible(true);
				}
			});

			XMLValidator validator = new XMLValidator(config, eventDispatcher);

			validatorDialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									"User abort of XML validation.", 
									LogLevel.INFO, 
									Event.GLOBAL_CHANNEL,
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

			viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
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
		public boolean importData(TransferSupport info) {
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

	@Override
	public void handleEvent(Event event) throws Exception {
		DatabaseConnectionStateEvent state = (DatabaseConnectionStateEvent)event;
		setEnabledWorkspace(!state.isConnected() || (state.isConnected() && databaseController.getActiveDatabaseAdapter().hasVersioningSupport()));
	}
}
