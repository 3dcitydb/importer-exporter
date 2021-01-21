/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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
package org.citydb.gui.modules.exporter.view;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.controller.Exporter;
import org.citydb.config.Config;
import org.citydb.config.exception.ErrorCode;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.exporter.SimpleTiling;
import org.citydb.config.project.exporter.SimpleTilingMode;
import org.citydb.config.project.exporter.SimpleTilingOptions;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.query.QueryConfig;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.simple.SimpleSelectionFilter;
import org.citydb.database.DatabaseController;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.InterruptEvent;
import org.citydb.gui.components.dialog.ExportStatusDialog;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ExportPanel extends JPanel implements DropTargetListener {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();
	private final ViewController viewController;
	private final DatabaseController databaseController;
	private final Config config;

	private JTextField browseText;
	private JButton browseButton;
	private FilterPanel filterPanel;
	private JButton exportButton;
	private JButton switchFilterModeButton;
	private boolean useSimpleFilter;

	public ExportPanel(ViewController viewController, Config config) {
		this.viewController = viewController;
		this.config = config;

		databaseController = ObjectRegistry.getInstance().getDatabaseController();
		initGui();
	}

	private void initGui() {
		browseText = new JTextField();
		browseButton = new JButton();
		filterPanel = new FilterPanel(viewController, config);
		exportButton = new JButton();
		switchFilterModeButton = new JButton();

		browseButton.addActionListener(e -> saveFile(Language.I18N.getString("main.tabbedPane.export")));

		PopupMenuDecorator.getInstance().decorate(browseText);

		exportButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				doExport();
				return null;
			}
		}.execute());

		setLayout(new GridBagLayout());

		JPanel filePanel = new JPanel();
		filePanel.setLayout(new GridBagLayout());
		filePanel.add(browseText, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 5));
		filePanel.add(browseButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 0));

		JPanel view = new JPanel();
		view.setLayout(new GridBagLayout());
		view.add(filterPanel, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 0, 10, 0, 10));

		JScrollPane scrollPane = new JScrollPane(view);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		buttonPanel.add(exportButton, GuiUtil.setConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.NONE, 5, 5, 5, 5));
		buttonPanel.add(switchFilterModeButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 5, 5, 5, 0));

		add(filePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 15, 10, 15, 10));
		add(scrollPane, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(buttonPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.HORIZONTAL, 5, 10, 5, 10));

		DropTarget dropTarget = new DropTarget(browseText, this);
		browseText.setDropTarget(dropTarget);
		setDropTarget(dropTarget);

		switchFilterModeButton.addActionListener(e -> switchFilterMode());
	}

	private void switchFilterMode() {
		useSimpleFilter = !useSimpleFilter;
		filterPanel.showFilterDialog(useSimpleFilter);
		switchFilterModeButton.setText(Language.I18N.getString(useSimpleFilter ? "filter.label.mode.xml" : "filter.label.mode.simple"));
	}

	public void doTranslation() {
		browseButton.setText(Language.I18N.getString("common.button.browse"));
		exportButton.setText(Language.I18N.getString("export.button.export"));
		switchFilterModeButton.setText(Language.I18N.getString(useSimpleFilter ? "filter.label.mode.xml" : "filter.label.mode.simple"));
		filterPanel.doTranslation();
	}

	public void loadSettings() {
		useSimpleFilter = config.getExportConfig().isUseSimpleQuery();
		filterPanel.loadSettings();
		filterPanel.showFilterDialog(useSimpleFilter);
		switchFilterModeButton.setText(Language.I18N.getString(useSimpleFilter ? "filter.label.mode.xml" : "filter.label.mode.simple"));
	}

	public void setSettings() {
		config.getExportConfig().setUseSimpleQuery(useSimpleFilter);

		try {
			Paths.get(browseText.getText());
		} catch (InvalidPathException e) {
			log.error("The provided output file '" + browseText.getText() + "' is not a valid file.");
			browseText.setText("");
		}

		filterPanel.setSettings();
	}

	private void doExport() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewController.clearConsole();
			setSettings();

			int tileAmount = 0;

			if (browseText.getText().trim().isEmpty()) {
				viewController.errorMessage(Language.I18N.getString("export.dialog.error.incompleteData"),
						Language.I18N.getString("export.dialog.error.incompleteData.dataset"));
				return;
			}

			if (config.getExportConfig().isUseSimpleQuery()) {
				SimpleQuery query = config.getExportConfig().getSimpleQuery();

				// simple selection filter
				if (query.isUseSelectionFilter()) {
					SimpleSelectionFilter selectionFilter = query.getSelectionFilter();
					if (!selectionFilter.isUseSQLFilter()
							&& !selectionFilter.getResourceIdFilter().isSetResourceIds()
							&& !selectionFilter.getNameFilter().isSetLiteral()
							&& !selectionFilter.getLineageFilter().isSetLiteral()) {
						viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
								Language.I18N.getString("export.dialog.error.incorrectData.attributes"));
						return;
					} else if (selectionFilter.isUseSQLFilter()
							&& !selectionFilter.getSQLFilter().isSetValue()) {
						viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
								Language.I18N.getString("export.dialog.error.incorrectData.sql"));
						return;
					}
				}

				// lod filter
				if (query.isUseLodFilter() && !query.getLodFilter().isSetAnyLod()) {
					viewController.errorMessage(Language.I18N.getString("export.dialog.error.lod"),
							Language.I18N.getString("export.dialog.error.lod.noneSelected"));
					return;
				}

				// counter filter
				if (query.isUseCountFilter()) {
					CounterFilter counterFilter = query.getCounterFilter();
					if ((!counterFilter.isSetCount() && !counterFilter.isSetStartIndex())
							|| (counterFilter.isSetCount() && counterFilter.getCount() < 0)
							|| (counterFilter.isSetStartIndex() && counterFilter.getStartIndex() < 0)) {
						viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
								Language.I18N.getString("export.dialog.error.incorrectData.counter"));
						return;
					}
				}

				// tiled bounding box
				if (query.isUseBboxFilter()) {
					SimpleTiling bboxFilter = query.getBboxFilter();

					BoundingBox bbox = query.getBboxFilter().getExtent();
					Double xMin = bbox.getLowerCorner().getX();
					Double yMin = bbox.getLowerCorner().getY();
					Double xMax = bbox.getUpperCorner().getX();
					Double yMax = bbox.getUpperCorner().getY();

					if (xMin == null || yMin == null || xMax == null || yMax == null) {
						viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
								Language.I18N.getString("common.dialog.error.incorrectData.bbox"));
						return;
					}

					if (bboxFilter.getMode() == SimpleTilingMode.TILING)
						tileAmount = bboxFilter.getRows() * bboxFilter.getColumns();
				}

				// feature types
				if (query.isUseTypeNames() && query.getFeatureTypeFilter().getTypeNames().isEmpty()) {
					viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
							Language.I18N.getString("common.dialog.error.incorrectData.featureClass"));
					return;
				}
			} else {
				QueryConfig query = config.getExportConfig().getQuery();
				if (query.hasLocalProperty("unmarshallingFailed")) {
					viewController.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
							Language.I18N.getString("common.dialog.error.incorrectData.xmlQuery"));
					return;
				}

				// copy tiling options if required
				if (query.isSetTiling() && !(query.getTiling().getTilingOptions() instanceof SimpleTilingOptions)) {
					query.getTiling().setTilingOptions(config.getExportConfig().getSimpleQuery().getBboxFilter().getTilingOptions());
					tileAmount = query.getTiling().getRows() * query.getTiling().getColumns();
				}
			}

			if (!databaseController.connect()) {
				return;
			}

			viewController.setStatusText(Language.I18N.getString("main.status.export.label"));
			log.info("Initializing database export...");

			// get event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final ExportStatusDialog exportDialog = new ExportStatusDialog(viewController.getTopFrame(),
					Language.I18N.getString("export.dialog.window"),
					Language.I18N.getString("export.dialog.msg"),
					tileAmount > 1);

			SwingUtilities.invokeLater(() -> {
				exportDialog.setLocationRelativeTo(viewController.getTopFrame());
				exportDialog.setVisible(true);
			});

			exportDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									"User abort of database export.", 
									LogLevel.WARN,
									Event.GLOBAL_CHANNEL,
									this));
						}
					});
				}
			});

			boolean success = false;
			try {
				success = new Exporter().doExport(Paths.get(browseText.getText()));
			} catch (CityGMLExportException e) {
				log.error(e.getMessage(), e.getCause());
				if (e.getErrorCode() == ErrorCode.SPATIAL_INDEXES_NOT_ACTIVATED) {
					log.error("Please use the database tab to activate the spatial indexes.");
				}
			}

			SwingUtilities.invokeLater(exportDialog::dispose);
			viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));

			if (success) {
				log.info("Database export successfully finished.");
			} else {
				log.warn("Database export aborted.");
			}
		} finally {
			lock.unlock();
		}
	}

	private void saveFile(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);

		FileNameExtensionFilter filter = new FileNameExtensionFilter("CityGML Files (*.gml, *.xml, *.json, *.zip, *.gz, *.gzip)",
				"gml", "xml", "json", "zip", "gz", "gzip");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("CityGML GML Files (*.gml, *.xml)", "gml", "xml"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("CityJSON Files (*.json)", "json"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("ZIP Files (*.zip)", "zip"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Compressed Files (*.gz, *.gzip)", "gz", "gzip"));
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);

		if (browseText.getText().trim().isEmpty()) {
			chooser.setCurrentDirectory(config.getExportConfig().getPath().isSetLastUsedMode() ?
					new File(config.getExportConfig().getPath().getLastUsedPath()) :
					new File(config.getExportConfig().getPath().getStandardPath()));
		} else {
			File file = new File(browseText.getText().trim());
			if (!file.isDirectory())
				file = file.getParentFile();

			chooser.setCurrentDirectory(file);
		}

		int result = chooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;
		try {
			String exportString = chooser.getSelectedFile().getAbsolutePath();
			if (Util.getFileExtension(exportString).isEmpty()) {
				exportString += ".gml";
			}

			browseText.setText(exportString);
			config.getExportConfig().getPath().setLastUsedPath(chooser.getCurrentDirectory().getAbsolutePath());
		} catch (Exception e) {
			//
		}
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

					for (File file : (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
						if (file.isFile() && file.canRead()) {
							browseText.setText(file.getCanonicalPath());
							break;
						}
					}

					dtde.getDropTargetContext().dropComplete(true);	
				} catch (UnsupportedFlavorException | IOException e) {
					//
				}
			}
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// nothing to do here
	}
}


