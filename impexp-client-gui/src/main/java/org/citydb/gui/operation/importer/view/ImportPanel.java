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
import org.citydb.config.project.importer.ImportMode;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.core.database.DatabaseController;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.controller.Importer;
import org.citydb.core.operation.validator.ValidationException;
import org.citydb.core.operation.validator.controller.Validator;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.components.FileListTransferHandler;
import org.citydb.gui.components.ScrollablePanel;
import org.citydb.gui.components.dialog.ConfirmationCheckDialog;
import org.citydb.gui.components.dialog.ImportStatusDialog;
import org.citydb.gui.components.dialog.XMLValidationStatusDialog;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.util.DefaultViewComponent;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.global.InterruptEvent;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ImportPanel extends DefaultViewComponent {
    private final ReentrantLock mainLock = new ReentrantLock();
    private final Logger log = Logger.getInstance();
    private final ViewController viewController;
    private final DatabaseController databaseController;
    private final Config config;

    private JList<File> fileList;
    private JButton browseButton;
    private JButton removeButton;
    private JButton importButton;
    private JButton validateButton;
    private JLabel importModeLabel;
    private JComboBox<ImportMode> importMode;
    private FilterPanel filterPanel;

    public ImportPanel(ViewController viewController, Config config) {
        this.config = config;
        this.viewController = viewController;

        databaseController = ObjectRegistry.getInstance().getDatabaseController();
        initGui();
    }

    private void initGui() {
        fileList = new JList<>(new DefaultListModel<>());
        browseButton = new JButton();
        removeButton = new JButton();
        filterPanel = new FilterPanel(viewController, config);
        importButton = new JButton();
        validateButton = new JButton();

        importModeLabel = new JLabel();
        importMode = new JComboBox<>();
        Arrays.stream(ImportMode.values()).forEach(importMode::addItem);

        FileListTransferHandler transferHandler = new FileListTransferHandler(fileList)
                .withFilesAddedHandler(model -> config.getImportConfig().getPath().setLastUsedPath(model.get(0).getAbsolutePath()))
                .withFilesRemovedHandler(model -> removeButton.setEnabled(false));
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileList.setTransferHandler(transferHandler);
        fileList.setVisibleRowCount(6);

        setDropTarget(fileList.getDropTarget());

        browseButton.addActionListener(e -> loadFile(Language.I18N.getString("main.tabbedPane.import")));

        removeButton.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));
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
        {
            JScrollPane fileScroll = new JScrollPane(fileList);
            fileScroll.setMinimumSize(fileList.getPreferredScrollableViewportSize());
            fileScroll.setPreferredSize(fileList.getPreferredScrollableViewportSize());

            filePanel.add(fileScroll, GuiUtil.setConstraints(0, 0, 1, 2, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 5));
            filePanel.add(browseButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
            filePanel.add(removeButton, GuiUtil.setConstraints(1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 5, 20, 0));
        }

        JPanel importModePanel = new JPanel();
        importModePanel.setLayout(new GridBagLayout());
        {
            importModePanel.add(importModeLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
            importModePanel.add(importMode, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        }

        JPanel view = new ScrollablePanel(true, false);
        view.setLayout(new GridBagLayout());
        {
            view.add(filterPanel, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 10, 0, 10));
        }

        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        {
            buttonPanel.add(importButton, GuiUtil.setConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.NONE, 5, 5, 5, 5));
            buttonPanel.add(validateButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 5, 5, 5, 0));
        }

        add(filePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 15, 10, 15, 10));
        add(importModePanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 10, 10, 10));
        add(scrollPane, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
        add(buttonPanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.HORIZONTAL, 5, 10, 5, 10));

        PopupMenuDecorator.getInstance().decorate(fileList);
    }

    @Override
    public void switchLocale(Locale locale) {
        browseButton.setText(Language.I18N.getString("common.button.browse"));
        removeButton.setText(Language.I18N.getString("import.button.remove"));
        importButton.setText(Language.I18N.getString("import.button.import"));
        validateButton.setText(Language.I18N.getString("import.button.validate"));
        importModeLabel.setText(Language.I18N.getString("import.mode"));
        importMode.updateUI();
        filterPanel.switchLocale(locale);
    }

    @Override
    public void loadSettings() {
        importMode.setSelectedItem(config.getImportConfig().getMode());
        filterPanel.loadSettings();
    }

    @Override
    public void setSettings() {
        config.getImportConfig().setMode((ImportMode) importMode.getSelectedItem());
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

            // ID list filter
            if (filter.isUseImportListFilter() && !filterPanel.checkImportListSettings()) {
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
            if (config.getImportConfig().getAffineTransformation().isEnabled()
                    && config.getGuiConfig().getImportGuiConfig().isShowAffineTransformationWarning()) {
                ConfirmationCheckDialog dialog = ConfirmationCheckDialog.defaults()
                        .withParentComponent(viewController.getTopFrame())
                        .withMessageType(JOptionPane.WARNING_MESSAGE)
                        .withOptionType(JOptionPane.YES_NO_OPTION)
                        .withTitle(Language.I18N.getString("common.dialog.warning.title"))
                        .addMessage(Language.I18N.getString("import.dialog.warn.affineTransformation"));

                int result = dialog.show();
                config.getGuiConfig().getImportGuiConfig().setShowAffineTransformationWarning(dialog.keepShowingDialog());
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            if (!databaseController.connect()) {
                return;
            }

            viewController.setStatusText(Language.I18N.getString("main.status.import.label"));
            log.info("Initializing database import...");

            Importer importer = new Importer();

            // initialize event dispatcher
            EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
            ImportStatusDialog importDialog = new ImportStatusDialog(viewController.getTopFrame(),
                    Language.I18N.getString("import.dialog.window"),
                    Language.I18N.getString("import.dialog.msg"));

            SwingUtilities.invokeLater(() -> {
                importDialog.setLocationRelativeTo(viewController.getTopFrame());
                importDialog.setVisible(true);
            });

            importDialog.getCancelButton().addActionListener(e ->
                    SwingUtilities.invokeLater(() -> eventDispatcher.triggerEvent(new InterruptEvent(
                            "User abort of database import.",
                            LogLevel.WARN,
                            importer.getEventChannel()))));

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

            Validator validator = new Validator();

            // initialize event dispatcher
            EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
            XMLValidationStatusDialog validatorDialog = new XMLValidationStatusDialog(viewController.getTopFrame(),
                    Language.I18N.getString("validate.dialog.window"),
                    Language.I18N.getString("validate.dialog.title"),
                    Language.I18N.getString("validate.dialog.details"),
                    eventDispatcher);

            SwingUtilities.invokeLater(() -> {
                validatorDialog.setLocationRelativeTo(viewController.getTopFrame());
                validatorDialog.setVisible(true);
            });

            validatorDialog.getButton().addActionListener(e ->
                    SwingUtilities.invokeLater(() -> eventDispatcher.triggerEvent(new InterruptEvent(
                            "User abort of data validation.",
                            LogLevel.WARN,
                            validator.getEventChannel()))));

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
        DefaultListModel<File> model = (DefaultListModel<File>) fileList.getModel();
        List<Path> importFiles = new ArrayList<>();
        for (int i = 0; i < model.size(); ++i) {
            try {
                importFiles.add(model.get(i).toPath());
            } catch (InvalidPathException e) {
                log.error("'" + model.get(i) + "' is not a valid file or folder.");
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

        DefaultListModel<File> model = (DefaultListModel<File>) fileList.getModel();
        if (model.isEmpty()) {
            chooser.setCurrentDirectory(config.getImportConfig().getPath().isSetLastUsedMode() ?
                    new File(config.getImportConfig().getPath().getLastUsedPath()) :
                    new File(config.getImportConfig().getPath().getStandardPath()));
        } else
            chooser.setCurrentDirectory(model.get(0));

        int result = chooser.showOpenDialog(getTopLevelAncestor());
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        model.clear();
        for (File file : chooser.getSelectedFiles())
            model.addElement(file);

        config.getImportConfig().getPath().setLastUsedPath(chooser.getCurrentDirectory().getAbsolutePath());
    }
}
