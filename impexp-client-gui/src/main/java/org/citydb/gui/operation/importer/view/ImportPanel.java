/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.operation.importer.view;

import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.importer.ImportFilter;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.database.DatabaseController;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.InterruptEvent;
import org.citydb.gui.components.dialog.ImportStatusDialog;
import org.citydb.gui.components.dialog.XMLValidationStatusDialog;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.operation.importer.CityGMLImportException;
import org.citydb.operation.importer.controller.Importer;
import org.citydb.operation.validator.ValidationException;
import org.citydb.operation.validator.controller.Validator;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.registry.ObjectRegistry;

import javax.swing.*;
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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ImportPanel extends JPanel {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();
	private final ViewController viewController;
	private final DatabaseController databaseController;
	private final Config config;

	private JList<File> fileList;
	private DefaultListModel<File> fileListModel;
	private JButton browseButton;
	private JButton removeButton;
	private JButton importButton;
	private JButton validateButton;
	private FilterPanel filterPanel;

	public ImportPanel(ViewController viewController, Config config) {
		this.config = config;
		this.viewController = viewController;

		databaseController = ObjectRegistry.getInstance().getDatabaseController();
		initGui();
	}

	private void initGui() {
		fileList = new JList<>();
		browseButton = new JButton();
		removeButton = new JButton();
		filterPanel = new FilterPanel(viewController, config);
		importButton = new JButton();
		validateButton = new JButton();

		DropCutCopyPasteHandler handler = new DropCutCopyPasteHandler();

		fileListModel = new DefaultListModel<>();
		fileList.setModel(fileListModel);
		fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fileList.setTransferHandler(handler);
		fileList.setVisibleRowCount(6);

		DropTarget dropTarget = new DropTarget(fileList, handler);
		fileList.setDropTarget(dropTarget);
		setDropTarget(dropTarget);

		ActionMap actionMap = fileList.getActionMap();
		actionMap.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
		actionMap.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
		actionMap.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());

		InputMap inputMap = fileList.getInputMap();
		inputMap.put(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK), TransferHandler.getCutAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK), TransferHandler.getCopyAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK), TransferHandler.getPasteAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), TransferHandler.getCutAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), TransferHandler.getCutAction().getValue(Action.NAME));

		browseButton.addActionListener(e -> loadFile(Language.I18N.getString("main.tabbedPane.import")));

		removeButton.setActionCommand((String)TransferHandler.getCutAction().getValue(Action.NAME));
		removeButton.addActionListener(e -> {
			String action = e.getActionCommand();
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
        filePanel.setLayout(new GridBagLayout());
        JScrollPane fileScroll = new JScrollPane(fileList);
		fileScroll.setPreferredSize(fileScroll.getPreferredSize());

        filePanel.add(fileScroll, GuiUtil.setConstraints(0, 0, 1, 0, 1, 2, GridBagConstraints.BOTH, 0, 0, 0, 5));
		filePanel.add(browseButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
		filePanel.add(removeButton, GuiUtil.setConstraints(1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 5, 20, 0));

        JPanel view = new JPanel();
        view.setLayout(new GridBagLayout());
		view.add(filterPanel, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 10, 0, 10));

        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.add(importButton, GuiUtil.setConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.NONE, 5, 5, 5, 5));
        buttonPanel.add(validateButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 5, 5, 5, 0));

        add(filePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 15, 10, 15, 10));
        add(scrollPane, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(buttonPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.HORIZONTAL, 5, 10, 5, 10));
    }

	public void doTranslation() {
		browseButton.setText(Language.I18N.getString("common.button.browse"));
		removeButton.setText(Language.I18N.getString("import.button.remove"));
		importButton.setText(Language.I18N.getString("import.button.import"));
		validateButton.setText(Language.I18N.getString("import.button.validate"));
		filterPanel.doTranslation();
	}

	public void loadSettings() {
		filterPanel.loadSettings();
	}

	public void setSettings() {
		filterPanel.setSettings();
	}

	private void doImport() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewController.clearConsole();
			setSettings();

			ImportFilter filter = config.getImportConfig().getFilter();

			// check all input values...
			List<Path> inputFiles = getInputFiles();
			if (inputFiles.isEmpty()) {
				viewController.errorMessage(Language.I18N.getString("import.dialog.error.incompleteData"),
						Language.I18N.getString("import.dialog.error.incompleteData.dataset"));
				return;
			}

			// attribute filter
			if (filter.isUseAttributeFilter()
					&& !filter.getAttributeFilter().getResourceIdFilter().isSetResourceIds()
					&& !filter.getAttributeFilter().getNameFilter().isSetLiteral()) {
				viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
						Language.I18N.getString("common.dialog.error.incorrectData.attributes"));
				return;
			}

			// counter filter
			if (filter.isUseCountFilter()) {
				CounterFilter counterFilter = filter.getCounterFilter();
				if ((!counterFilter.isSetCount() && !counterFilter.isSetStartIndex())
						|| (counterFilter.isSetCount() && counterFilter.getCount() < 0)
						|| (counterFilter.isSetStartIndex() && counterFilter.getStartIndex() < 0)) {
					viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
							Language.I18N.getString("import.dialog.error.incorrectData.counter"));
					return;
				}
			}

			// bounding box
			if (filter.isUseBboxFilter()) {
				BoundingBox bbox = filter.getBboxFilter().getExtent();
				Double xMin = bbox.getLowerCorner().getX();
				Double yMin = bbox.getLowerCorner().getY();
				Double xMax = bbox.getUpperCorner().getX();
				Double yMax = bbox.getUpperCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
							Language.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}
			}

			// feature types
			if (filter.isUseTypeNames() && filter.getFeatureTypeFilter().getTypeNames().isEmpty()) {
				viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
						Language.I18N.getString("common.dialog.error.incorrectData.featureClass"));
				return;
			}

			// affine transformation
			if (config.getImportConfig().getAffineTransformation().isEnabled()) {
				if (viewController.showOptionDialog(Language.I18N.getString("common.dialog.warning.title"),
						Language.I18N.getString("import.dialog.warning.affineTransformation"),
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
					return;
				}
			}

			if (!databaseController.connect()) {
				return;
			}

			viewController.setStatusText(Language.I18N.getString("main.status.import.label"));
			log.info("Initializing database import...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final ImportStatusDialog importDialog = new ImportStatusDialog(viewController.getTopFrame(),
					Language.I18N.getString("import.dialog.window"),
					Language.I18N.getString("import.dialog.msg"));

			SwingUtilities.invokeLater(() -> {
				importDialog.setLocationRelativeTo(viewController.getTopFrame());
				importDialog.setVisible(true);
			});

			Importer importer = new Importer();

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
				success = importer.doImport(inputFiles);
			} catch (CityGMLImportException e) {
				log.error(e.getMessage(), e.getCause());
			}

			SwingUtilities.invokeLater(importDialog::dispose);
			viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));

			if (success) {
				log.info("Database import successfully finished.");
			} else {
				log.warn("Database import aborted.");
			}
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
			List<Path> inputFiles = getInputFiles();
			if (inputFiles.isEmpty()) {
				viewController.errorMessage(Language.I18N.getString("validate.dialog.error.incompleteData"),
						Language.I18N.getString("validate.dialog.error.incompleteData.dataset"));
				return;
			}

			viewController.setStatusText(Language.I18N.getString("main.status.validate.label"));
			log.info("Initializing data validation...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final XMLValidationStatusDialog validatorDialog = new XMLValidationStatusDialog(viewController.getTopFrame(),
					Language.I18N.getString("validate.dialog.window"),
					Language.I18N.getString("validate.dialog.title"),
					Language.I18N.getString("validate.dialog.details") ,
					eventDispatcher);

			SwingUtilities.invokeLater(() -> {
				validatorDialog.setLocationRelativeTo(viewController.getTopFrame());
				validatorDialog.setVisible(true);
			});

			Validator validator = new Validator();

			validatorDialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									"User abort of data validation.",
									LogLevel.WARN,
									Event.GLOBAL_CHANNEL,
									this));
						}
					});
				}
			});

			boolean success = false;
			try {
				success = validator.doValidate(inputFiles);
			} catch (ValidationException e) {
				log.error(e.getMessage(), e.getCause());
			}

			SwingUtilities.invokeLater(validatorDialog::dispose);

			if (success) {
				log.info("Data validation successfully finished.");
			} else {
				log.warn("Data validation aborted.");
			}

			viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private List<Path> getInputFiles() {
		List<Path> importFiles = new ArrayList<>();
		for (int i = 0; i < fileListModel.size(); ++i) {
			try {
				importFiles.add(fileListModel.get(i).toPath());
			} catch (InvalidPathException e) {
				log.error("'" + fileListModel.get(i) + "' is not a valid file or folder.");
				importFiles = Collections.emptyList();
				break;
			}
		}

		return importFiles;
	}

	private void loadFile(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		FileNameExtensionFilter filter = new FileNameExtensionFilter("CityGML Files (*.gml, *.xml, *.json, *.zip, *.gz, *.gzip)",
				"gml", "xml", "json", "zip", "gz", "gzip");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("CityGML GML Files (*.gml, *.xml)", "gml", "xml"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("CityJSON Files (*.json)", "json"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("ZIP Files (*.zip)", "zip"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Compressed Files (*.gz, *.gzip)", "gz", "gzip"));
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);

		if (fileListModel.isEmpty()) {
			chooser.setCurrentDirectory(config.getImportConfig().getPath().isSetLastUsedMode() ?
					new File(config.getImportConfig().getPath().getLastUsedPath()) :
					new File(config.getImportConfig().getPath().getStandardPath()));
		} else
			chooser.setCurrentDirectory(fileListModel.get(0));

		int result = chooser.showOpenDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION)
			return;

		fileListModel.clear();
		for (File file : chooser.getSelectedFiles())
			fileListModel.addElement(file);

		config.getImportConfig().getPath().setLastUsedPath(chooser.getCurrentDirectory().getAbsolutePath());
	}

	// JList handler for drop, cut, copy, and paste support
	private final class DropCutCopyPasteHandler extends TransferHandler implements DropTargetListener {

		@Override
		public boolean importData(TransferSupport info) {
			if (!info.isDataFlavorSupported(DataFlavor.stringFlavor))
				return false;

			if (info.isDrop())
				return false;

			List<File> files = new ArrayList<>();
			try {
				String value = (String)info.getTransferable().getTransferData(DataFlavor.stringFlavor);

				for (String token : value.split(System.getProperty("line.separator"))) {
					File file = new File(token);
					if (file.exists())
						files.add(file.getAbsoluteFile());
					else
						log.warn("Failed to paste from clipboard: '" + file.getAbsolutePath() + "' does not exist.");
				}

				if (!files.isEmpty()) {
					addFiles(files);
					return true;
				}
			} catch (UnsupportedFlavorException | IOException ufe) {
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
				builder.append(fileList.getModel().getElementAt(indices[i]));
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

						List<File> files = new ArrayList<>();
						for (File file : (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor))
							if (file.exists())
								files.add(file.getAbsoluteFile());
							else
								log.warn("Failed to drop from clipboard: '" + file.getAbsolutePath() + "' is not a file.");

						if (!files.isEmpty()) {
							if (dtde.getDropAction() != DnDConstants.ACTION_COPY)
								fileListModel.clear();

							addFiles(files);
						}

						dtde.getDropTargetContext().dropComplete(true);
					} catch (UnsupportedFlavorException | IOException e) {
						//
					}
				}
			}
		}

		private void addFiles(List<File> files) {
			int index = fileList.getSelectedIndex() + 1;
			for (File file : files)
				fileListModel.add(index++, file);

			config.getImportConfig().getPath().setLastUsedPath(fileListModel.getElementAt(0).getAbsolutePath());
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
