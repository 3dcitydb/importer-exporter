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
package org.citydb.modules.citygml.exporter.gui.view;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.controller.Exporter;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.global.LogLevel;
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
import org.citydb.gui.components.dialog.ExportStatusDialog;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.factory.SrsComboBoxFactory;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.DatabaseSrsComboBox;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public class ExportPanel extends JPanel implements DropTargetListener, EventHandler {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final CityGMLBuilder cityGMLBuilder;
	private final ViewController viewContoller;
	private final DatabaseController databaseController;
	private final Config config;

	private JTextField browseText;
	private JButton browseButton;
	private JXTextField workspaceText;
	private JFormattedTextField timestampText;
	private FilterPanel filterPanel;
	private JButton exportButton;

	private JPanel operations;
	private JLabel workspaceLabel;
	private JLabel timestampLabel;
	private JLabel srsComboBoxLabel;
	private DatabaseSrsComboBox srsComboBox;

	public ExportPanel(ViewController viewController, Config config) {
		this.viewContoller = viewController;
		this.config = config;

		databaseController = ObjectRegistry.getInstance().getDatabaseController();
		cityGMLBuilder = ObjectRegistry.getInstance().getCityGMLBuilder();		
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);

		initGui();
	}

	private void initGui() {
		browseText = new JTextField();
		browseButton = new JButton();
		workspaceText = new JXTextField();
		workspaceText.setPromptForeground(Color.LIGHT_GRAY);
		workspaceText.setFocusBehavior(FocusBehavior.SHOW_PROMPT);
		timestampText = new JFormattedTextField(new SimpleDateFormat("dd.MM.yyyy"));
		timestampText.setFocusLostBehavior(JFormattedTextField.COMMIT);
		timestampText.setColumns(10);
		filterPanel = new FilterPanel(viewContoller, config);
		exportButton = new JButton();

		workspaceText.setEnabled(true);
		timestampText.setEnabled(true);
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile(Language.I18N.getString("main.tabbedPane.export"));
			}
		});

		PopupMenuDecorator.getInstance().decorate(workspaceText, timestampText, browseText);

		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						doExport();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		setLayout(new GridBagLayout());

		JPanel filePanel = new JPanel();
		add(filePanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,10,5,5,5));
		filePanel.setLayout(new GridBagLayout());
		filePanel.add(browseText, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
		filePanel.add(browseButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));

		JPanel view = new JPanel();
		view.setLayout(new GridBagLayout());
		JScrollPane scrollPane = new JScrollPane(view);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		add(scrollPane, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));

		operations = new JPanel();
		view.add(operations, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
		operations.setBorder(BorderFactory.createTitledBorder(""));
		operations.setLayout(new GridBagLayout());
		workspaceLabel = new JLabel();
		timestampLabel = new JLabel();
		srsComboBoxLabel = new JLabel();
		srsComboBox = SrsComboBoxFactory.getInstance(config).createSrsComboBox(true);
		srsComboBox.setShowOnlySameDimension(true);
		srsComboBox.setPreferredSize(new Dimension(50, srsComboBox.getPreferredSize().height));

		operations.add(workspaceLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
		operations.add(workspaceText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
		operations.add(timestampLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,5,5));
		operations.add(timestampText, GuiUtil.setConstraints(3,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
		timestampText.setMinimumSize(timestampText.getPreferredSize());
		operations.add(srsComboBoxLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
		operations.add(srsComboBox, GuiUtil.setConstraints(1,1,3,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));

		view.add(filterPanel, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,0,5,0,5));

		JPanel buttons = new JPanel();
		add(buttons, GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
		buttons.add(exportButton);

		DropTarget dropTarget = new DropTarget(browseText, this);
		browseText.setDropTarget(dropTarget);
		setDropTarget(dropTarget);
	}

	public void setEnabledWorkspace(boolean enable) {
		workspaceLabel.setEnabled(enable);
		workspaceText.setEnabled(enable);
		timestampLabel.setEnabled(enable);
		timestampText.setEnabled(enable);
	}

	public void doTranslation() {
		((TitledBorder)operations.getBorder()).setTitle(Language.I18N.getString("export.border.settings"));
		workspaceLabel.setText(Language.I18N.getString("common.label.workspace"));
		workspaceText.setPrompt(Language.I18N.getString("common.label.workspace.prompt"));
		timestampLabel.setText(Language.I18N.getString("common.label.timestamp"));
		browseButton.setText(Language.I18N.getString("common.button.browse"));
		exportButton.setText(Language.I18N.getString("export.button.export"));
		srsComboBoxLabel.setText(Language.I18N.getString("common.label.boundingBox.crs"));
		filterPanel.doTranslation();
	}

	public void loadSettings() {
		browseText.setText(config.getInternal().getExportFileName());
		workspaceText.setText(config.getProject().getDatabase().getWorkspaces().getExportWorkspace().getName());
		timestampText.setText(config.getProject().getDatabase().getWorkspaces().getExportWorkspace().getTimestamp());
		srsComboBox.setSelectedItem(config.getProject().getExporter().getQuery().getTargetSRS());

		filterPanel.loadSettings();
	}

	public void setSettings() {
		config.getInternal().setExportFileName(browseText.getText());
		config.getProject().getDatabase().getWorkspaces().getExportWorkspace().setName(workspaceText.getText().trim());
		config.getProject().getDatabase().getWorkspaces().getExportWorkspace().setTimestamp(timestampText.getText().trim());
		config.getProject().getExporter().getQuery().setTargetSRS(srsComboBox.getSelectedItem());

		filterPanel.setSettings();
	}

	private void doExport() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewContoller.clearConsole();
			setSettings();

			SimpleQuery query = config.getProject().getExporter().getQuery();
			Database db = config.getProject().getDatabase();

			// check all input values...
			if (config.getInternal().getExportFileName().trim().equals("")) {
				viewContoller.errorMessage(Language.I18N.getString("export.dialog.error.incompleteData"), 
						Language.I18N.getString("export.dialog.error.incompleteData.dataset"));
				return;
			}

			// workspace timestamp
			if (!Util.checkWorkspaceTimestamp(db.getWorkspaces().getExportWorkspace())) {
				viewContoller.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"), 
						Language.I18N.getString("common.dialog.error.incorrectData.date"));
				return;
			}

			// gmlId
			if (query.getMode() == SimpleSelectionFilterMode.SIMPLE
					&& !query.getFilter().getGmlIdFilter().isSetResourceIds()) {
				viewContoller.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"), 
						Language.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// cityObject
			if (query.getMode() == SimpleSelectionFilterMode.COMPLEX
					&& query.isUseCountFilter()) {
				Long coStart = query.getCounterFilter().getLowerLimit();
				Long coEnd = query.getCounterFilter().getUpperLimit();

				if (coStart == null || coEnd == null) {
					viewContoller.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"), 
							Language.I18N.getString("export.dialog.error.incorrectData.range"));
					return;
				}

				if ((coStart != null && coStart <= 0) || (coEnd != null && coEnd <= 0)) {
					viewContoller.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"), 
							Language.I18N.getString("export.dialog.error.incorrectData.range"));
					return;
				}

				if (coEnd != null && coEnd < coStart) {
					viewContoller.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"), 
							Language.I18N.getString("export.dialog.error.incorrectData.range"));
					return;
				}
			}

			// gmlName
			if (query.getMode() == SimpleSelectionFilterMode.COMPLEX
					&& query.isUseGmlNameFilter()
					&& (!query.getFilter().getGmlNameFilter().isSetLiteral()
							|| query.getFilter().getGmlNameFilter().getLiteral().trim().equals(""))) {
				viewContoller.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
						Language.I18N.getString("common.dialog.error.incorrectData.gmlName"));
				return;
			}

			// lod filter
			if (query.isUseLodFilter() && !query.getLodFilter().isSetAnyLod()) {
				viewContoller.errorMessage(Language.I18N.getString("export.dialog.error.lod"),
						Language.I18N.getString("export.dialog.error.lod.noneSelected"));
				return;
			}

			// tiled bounding box
			int tileAmount = 0;
			if (query.getMode() == SimpleSelectionFilterMode.COMPLEX
					&& query.isUseBboxFilter()) {
				BoundingBox bbox = (BoundingBox)query.getFilter().getBboxFilter().getEnvelope();
				Double xMin = bbox.getLowerCorner().getX();
				Double yMin = bbox.getLowerCorner().getY();
				Double xMax = bbox.getUpperCorner().getX();
				Double yMax = bbox.getUpperCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					viewContoller.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
							Language.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}

				if (query.isUseTiling())
					tileAmount = query.getTilingOptions().getRows() * query.getTilingOptions().getColumns();
			}

			// feature types
			if (query.getMode() == SimpleSelectionFilterMode.COMPLEX 
					&& query.isUseTypeNames()
					&& query.getFeatureTypeFilter().getTypeNames().isEmpty()) {
				viewContoller.errorMessage(Language.I18N.getString("export.dialog.error.incorrectData"),
						Language.I18N.getString("common.dialog.error.incorrectData.featureClass"));
				return;
			}

			if (!databaseController.isConnected()) {
				try {
					databaseController.connect(true);
					if (!databaseController.isConnected())
						return;
				} catch (DatabaseConfigurationException | DatabaseVersionException | SQLException e) {
					//
				}
			}

			viewContoller.setStatusText(Language.I18N.getString("main.status.export.label"));
			LOG.info("Initializing database export...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final ExportStatusDialog exportDialog = new ExportStatusDialog(viewContoller.getTopFrame(), 
					Language.I18N.getString("export.dialog.window"),
					Language.I18N.getString("export.dialog.msg"),
					tileAmount > 1);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					exportDialog.setLocationRelativeTo(viewContoller.getTopFrame());
					exportDialog.setVisible(true);
				}
			});

			// get schema mapping
			final SchemaMapping schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();

			Exporter exporter = new Exporter(cityGMLBuilder, schemaMapping, config, eventDispatcher);

			exportDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									"User abort of database export.", 
									LogLevel.INFO, 
									Event.GLOBAL_CHANNEL,
									this));
						}
					});
				}
			});

			boolean success = false;
			try {
				success = exporter.doProcess();
			} catch (CityGMLExportException e) {
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
					exportDialog.dispose();
				}
			});

			// cleanup
			exporter.cleanup();

			if (success) {
				LOG.info("Database export successfully finished.");
			} else {
				LOG.warn("Database export aborted.");
			}

			viewContoller.setStatusText(Language.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void saveFile(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);

		FileNameExtensionFilter filter = new FileNameExtensionFilter("CityGML Files (*.gml, *.xml)", "gml", "xml");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);

		if (config.getProject().getExporter().getPath().isSetLastUsedMode()) {
			chooser.setCurrentDirectory(new File(config.getProject().getExporter().getPath().getLastUsedPath()));
		} else {
			chooser.setCurrentDirectory(new File(config.getProject().getExporter().getPath().getStandardPath()));
		}
		int result = chooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;
		try {
			String exportString = chooser.getSelectedFile().toString();
			if ((!chooser.getSelectedFile().getName().contains("."))&&(!exportString.equals(""))) exportString += ".gml";
			browseText.setText(exportString);
			config.getProject().getExporter().getPath().setLastUsedPath(chooser.getCurrentDirectory().getAbsolutePath());
		}
		catch (Exception e) {
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

	@Override
	public void handleEvent(Event event) throws Exception {
		DatabaseConnectionStateEvent state = (DatabaseConnectionStateEvent)event;
		setEnabledWorkspace(!state.isConnected() || (state.isConnected() && databaseController.getActiveDatabaseAdapter().hasVersioningSupport()));
	}

}


