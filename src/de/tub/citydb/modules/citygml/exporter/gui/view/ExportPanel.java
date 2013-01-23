/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.modules.citygml.exporter.gui.view;

import java.awt.Dimension;
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.citygml4j.builder.jaxb.JAXBBuilder;

import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.ExportStatusDialog;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.factory.SrsComboBoxFactory;
import de.tub.citydb.gui.factory.SrsComboBoxFactory.SrsComboBox;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.gui.view.FilterPanel;
import de.tub.citydb.modules.citygml.common.gui.view.FilterPanel.FilterPanelType;
import de.tub.citydb.modules.citygml.exporter.controller.Exporter;
import de.tub.citydb.modules.common.event.InterruptEnum;
import de.tub.citydb.modules.common.event.InterruptEvent;
import de.tub.citydb.util.Util;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ExportPanel extends JPanel implements DropTargetListener {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final JAXBBuilder jaxbBuilder;
	private final Config config;
	private final ImpExpGui mainView;
	private final DatabaseConnectionPool dbPool;

	private JTextField browseText;
	private JButton browseButton;
	private JTextField workspaceText;
	private JFormattedTextField timestampText;
	private FilterPanel filterPanel;
	private JButton exportButton;

	private JPanel operations;
	private JLabel workspaceLabel;
	private JLabel timestampLabel;
	private JLabel srsComboBoxLabel;
	private SrsComboBox srsComboBox;

	public ExportPanel(JAXBBuilder jaxbBuilder, Config config, ImpExpGui mainView) {
		this.jaxbBuilder = jaxbBuilder;
		this.config = config;
		this.mainView = mainView;
		dbPool = DatabaseConnectionPool.getInstance();

		initGui();
	}

	private void initGui() {
		browseText = new JTextField();
		browseButton = new JButton();
		workspaceText = new JTextField();
		timestampText = new JFormattedTextField(new SimpleDateFormat("dd.MM.yyyy"));
		timestampText.setFocusLostBehavior(JFormattedTextField.COMMIT);
		timestampText.setColumns(10);
		filterPanel = new FilterPanel(config, FilterPanelType.EXPORT);
		exportButton = new JButton();

		workspaceText.setEnabled(true);
		timestampText.setEnabled(true);
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile(Internal.I18N.getString("main.tabbedPane.export"));
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

		view.add(filterPanel, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,0,5,0,5));

		JPanel buttons = new JPanel();
		add(buttons, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
		buttons.add(exportButton);

		DropTarget dropTarget = new DropTarget(browseText, this);
		browseText.setDropTarget(dropTarget);
		setDropTarget(dropTarget);
	}

	public void doTranslation() {
		((TitledBorder)operations.getBorder()).setTitle(Internal.I18N.getString("export.border.settings"));
		workspaceLabel.setText(Internal.I18N.getString("common.label.workspace"));
		timestampLabel.setText(Internal.I18N.getString("common.label.timestamp"));
		browseButton.setText(Internal.I18N.getString("common.button.browse"));
		exportButton.setText(Internal.I18N.getString("export.button.export"));
		srsComboBoxLabel.setText(Internal.I18N.getString("common.label.boundingBox.crs"));
		filterPanel.doTranslation();
	}

	public void loadSettings() {
		browseText.setText(config.getInternal().getExportFileName());
		workspaceText.setText(config.getProject().getDatabase().getWorkspaces().getExportWorkspace().getName());
		timestampText.setText(config.getProject().getDatabase().getWorkspaces().getExportWorkspace().getTimestamp());

		srsComboBox.setSelectedItem(config.getProject().getExporter().getTargetSRS());

		filterPanel.loadSettings();
	}

	public void setSettings() {
		config.getInternal().setExportFileName(browseText.getText());

		String workspace = workspaceText.getText().trim();
		if (!workspace.equals(Internal.ORACLE_DEFAULT_WORKSPACE) && 
				(workspace.length() == 0 || workspace.toUpperCase().equals(Internal.ORACLE_DEFAULT_WORKSPACE)))
			workspaceText.setText(Internal.ORACLE_DEFAULT_WORKSPACE);

		config.getProject().getDatabase().getWorkspaces().getExportWorkspace().setName(workspaceText.getText());
		config.getProject().getDatabase().getWorkspaces().getExportWorkspace().setTimestamp(timestampText.getText());
		config.getProject().getExporter().setTargetSRS(srsComboBox.getSelectedItem());
		filterPanel.setSettings();
	}

	private void doExport() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			mainView.clearConsole();
			setSettings();

			ExportFilterConfig filter = config.getProject().getExporter().getFilter();
			Database db = config.getProject().getDatabase();

			// check all input values...
			if (config.getInternal().getExportFileName().trim().equals("")) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incompleteData"), 
						Internal.I18N.getString("export.dialog.error.incompleteData.dataset"));
				return;
			}

			// workspace timestamp
			if (!Util.checkWorkspaceTimestamp(db.getWorkspaces().getExportWorkspace())) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.date"));
				return;
			}

			// gmlId
			if (filter.isSetSimpleFilter() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
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
					mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
							Internal.I18N.getString("export.dialog.error.incorrectData.range"));
					return;
				}

				if ((coStart != null && coStart <= 0) || (coEnd != null && coEnd <= 0)) {
					mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
							Internal.I18N.getString("export.dialog.error.incorrectData.range"));
					return;
				}

				if (coEnd != null && coEnd < coStart) {
					mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
							Internal.I18N.getString("export.dialog.error.incorrectData.range"));
					return;
				}
			}

			// gmlName
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getGmlName().isSet() &&
					filter.getComplexFilter().getGmlName().getValue().trim().equals("")) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"),
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlName"));
				return;
			}

			// tiled bounding box
			int tileAmount = 0;
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getTiledBoundingBox().isSet()) {
				Double xMin = filter.getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getX();
				Double yMin = filter.getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getY();
				Double xMax = filter.getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getX();
				Double yMax = filter.getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"),
							Internal.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}

				if (filter.getComplexFilter().getTiledBoundingBox().getTiling().getMode() != TilingMode.NO_TILING) {
					int rows = filter.getComplexFilter().getTiledBoundingBox().getTiling().getRows();
					int columns = filter.getComplexFilter().getTiledBoundingBox().getTiling().getColumns(); 
					tileAmount = rows * columns;
				}
			}

			if (!dbPool.isConnected()) {
				mainView.connectToDatabase();

				if (!dbPool.isConnected())
					return;
			}

			mainView.setStatusText(Internal.I18N.getString("main.status.export.label"));
			LOG.info("Initializing database export...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final ExportStatusDialog exportDialog = new ExportStatusDialog(mainView, 
					Internal.I18N.getString("export.dialog.window"),
					Internal.I18N.getString("export.dialog.msg"),
					tileAmount);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					exportDialog.setLocationRelativeTo(mainView);
					exportDialog.setVisible(true);
				}
			});

			Exporter exporter = new Exporter(jaxbBuilder, dbPool, config, eventDispatcher);

			exportDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of database export.", 
									LogLevel.INFO, 
									this));
						}
					});
				}
			});

			boolean success = exporter.doProcess();

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

			mainView.setStatusText(Internal.I18N.getString("main.status.ready.label"));
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

}


