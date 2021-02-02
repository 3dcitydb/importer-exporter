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
package org.citydb.gui.modules.common;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.common.XSLTransformation;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.ClientConstants;

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
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XSLTransformationPanel extends AbstractPreferencesComponent {
    private final boolean isExport;

    private TitledPanel transformationPanel;
    private JPanel content;
    private StylesheetComponent first;
    private JCheckBox applyStylesheets;
    private File lastPath;

    public XSLTransformationPanel(boolean isExport, Config config) {
        super(config);
        this.isExport = isExport;

        initGui();
    }

    @Override
    public boolean isModified() {
        XSLTransformation transformation = isExport ?
                config.getExportConfig().getCityGMLOptions().getXSLTransformation() :
                config.getImportConfig().getCityGMLOptions().getXSLTransformation();

        if (transformation.isEnabled() != applyStylesheets.isSelected()) return true;
        if (!transformation.isSetStylesheets() && !first.stylesheet.getText().trim().isEmpty()) return true;

        if (transformation.isSetStylesheets()) {
            StylesheetComponent current = first;
            for (String stylesheet : transformation.getStylesheets()) {
                if (current == null) return true;
                if (!stylesheet.equals(current.stylesheet.getText())) return true;
                current = current.next;
            }

            return current != null;
        }

        return false;
    }

    private void initGui() {
        applyStylesheets = new JCheckBox();
        first = new StylesheetComponent(true);

        content = new JPanel();
        content.setLayout(new GridLayout(0, 1));
        content.add(first.panel);

        transformationPanel = new TitledPanel()
                .withToggleButton(applyStylesheets)
                .build(content);

        setLayout(new GridBagLayout());
        add(transformationPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

        applyStylesheets.addActionListener(l -> setEnabledComponents());
    }

    @Override
    public void setSettings() {
        XSLTransformation transformation = isExport ?
                config.getExportConfig().getCityGMLOptions().getXSLTransformation() :
                config.getImportConfig().getCityGMLOptions().getXSLTransformation();

        transformation.setEnabled(applyStylesheets.isSelected());

        List<String> stylesheets = new ArrayList<>();
        StylesheetComponent current = first;
        boolean updateFirst = false;
        boolean componentsChanged = false;

        do {
            String path = current.stylesheet.getText().trim();
            if (!path.isEmpty()) {
                stylesheets.add(path);
                if (updateFirst) {
                    first.stylesheet.setText(path);
                    current.remove();
                    updateFirst = false;
                    componentsChanged = true;
                }
            } else {
                if (current == first)
                    updateFirst = true;
                else {
                    current.remove();
                    componentsChanged = true;
                }
            }
        } while ((current = current.next) != null);

        transformation.setStylesheets(stylesheets);
        if (componentsChanged)
            updateComponents();

        if (stylesheets.isEmpty()) {
            transformation.setEnabled(false);
            applyStylesheets.setSelected(false);
            setEnabledComponents();
        }
    }

    @Override
    public void loadSettings() {
        XSLTransformation transformation = isExport ?
                config.getExportConfig().getCityGMLOptions().getXSLTransformation() :
                config.getImportConfig().getCityGMLOptions().getXSLTransformation();

        applyStylesheets.setSelected(transformation.isEnabled());

        first.next = null;
        if (transformation.isSetStylesheets()) {
            StylesheetComponent current = first;
            Iterator<String> iter = transformation.getStylesheets().iterator();
            while (iter.hasNext()) {
                current.stylesheet.setText(iter.next());
                if (iter.hasNext()) {
                    current = current.add();
                }
            }
        }

        updateComponents();
        setEnabledComponents();
    }

    @Override
    public void doTranslation() {
        transformationPanel.setTitle(Language.I18N.getString("common.pref.xslt.label.applyStylesheets"));

        StylesheetComponent current = first;
        do {
            current.doTranslation();
        } while ((current = current.next) != null);
    }

    @Override
    public String getTitle() {
        return Language.I18N.getString("common.pref.tree.xslt");
    }

    private void setEnabledComponents() {
        StylesheetComponent current = first;
        do {
            current.setEnabled(applyStylesheets.isSelected());
        } while ((current = current.next) != null);
    }

    private void updateComponents() {
        SwingUtilities.invokeLater(() -> {
            content.removeAll();

            StylesheetComponent current = first;
            do {
                content.add(current.panel);
            } while ((current = current.next) != null);

            content.revalidate();
        });
    }

    private void browseStylesheet(StylesheetComponent component) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Language.I18N.getString("common.pref.xslt.label.file"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("XSLT Stylesheet (*.xsl)", "xsl");
        chooser.addChoosableFileFilter(filter);
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.setFileFilter(filter);

        File currentDirectory = null;
        String stylesheet = component.stylesheet.getText().trim();
        if (!stylesheet.isEmpty()) {
            Path path = Paths.get(stylesheet).getParent();
            if (path != null)
                currentDirectory = path.toFile();
        }

        if (currentDirectory == null) {
            if (lastPath != null)
                currentDirectory = lastPath;
            else
                currentDirectory = ClientConstants.IMPEXP_HOME.resolve(ClientConstants.XSLT_TEMPLATES_DIR).toFile();
        }

        chooser.setCurrentDirectory(currentDirectory);

        int result = chooser.showOpenDialog(getTopLevelAncestor());
        if (result != JFileChooser.CANCEL_OPTION) {
            component.stylesheet.setText(chooser.getSelectedFile().getAbsolutePath());
            lastPath = chooser.getCurrentDirectory();
        }
    }

    private class StylesheetComponent implements DropTargetListener {
        private StylesheetComponent previous;
        private StylesheetComponent next;

        private final JPanel panel;
        private final JLabel label;
        private final JTextField stylesheet;
        private final JButton browseButton;
        private final JButton addButton;
        private final JButton removeButton;

        private StylesheetComponent() {
            this(false);
        }

        private StylesheetComponent(boolean first) {
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            label = new JLabel(Language.I18N.getString("common.pref.xslt.label.stylesheets"));
            stylesheet = new JTextField();
            browseButton = new JButton(Language.I18N.getString("common.button.browse"));
            addButton = new JButton(new FlatSVGIcon("org/citydb/gui/icons/add.svg"));
            removeButton = new JButton(new FlatSVGIcon("org/citydb/gui/icons/remove.svg"));

            JToolBar toolBar = new JToolBar();
            toolBar.setBorder(BorderFactory.createEmptyBorder());
            toolBar.setFloatable(false);
            toolBar.add(addButton);
            toolBar.add(removeButton);

            stylesheet.setPreferredSize(new Dimension(0, stylesheet.getPreferredSize().height));

            panel.add(label, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
            panel.add(stylesheet, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 5));
            panel.add(browseButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 5));
            panel.add(toolBar, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.NONE, 0, 10, 5, 0));

            if (first) {
                Dimension size = toolBar.getPreferredSize();
                toolBar.remove(removeButton);
                Dimension reduced = toolBar.getPreferredSize();

                panel.add(Box.createRigidArea(new Dimension(size.width - reduced.width, size.height - reduced.height)),
                        GuiUtil.setConstraints(4, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 5, 0));
            }

            addButton.addActionListener(e -> {
                add();
                updateComponents();
            });

            removeButton.addActionListener(e -> {
                remove();
                updateComponents();
            });

            browseButton.addActionListener(e -> browseStylesheet(this));
            stylesheet.setDropTarget(new DropTarget(stylesheet, this));
            PopupMenuDecorator.getInstance().decorate(stylesheet);
        }

        private StylesheetComponent add() {
            StylesheetComponent component = new StylesheetComponent();
            if (next != null) {
                component.next = next;
                next.previous = component;
            }

            next = component;
            component.previous = this;

            return component;
        }

        private void remove() {
            if (previous != null)
                previous.next = next;

            if (next != null)
                next.previous = previous;
        }

        private void setEnabled(boolean enable) {
            label.setEnabled(enable);
            stylesheet.setEnabled(enable);
            browseButton.setEnabled(enable);
            addButton.setEnabled(enable);
            removeButton.setEnabled(enable);
        }

        private void doTranslation() {
            label.setText(Language.I18N.getString("common.pref.xslt.label.stylesheets"));
            browseButton.setText(Language.I18N.getString("common.button.browse"));
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            // nothing to do here
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
        @SuppressWarnings("unchecked")
        public void drop(DropTargetDropEvent dtde) {
            if (!stylesheet.isEnabled())
                return;

            for (DataFlavor dataFlover : dtde.getCurrentDataFlavors()) {
                if (dataFlover.isFlavorJavaFileListType()) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                        for (File file : (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
                            if (file.isFile() && file.canRead()) {
                                stylesheet.setText(file.getCanonicalPath());
                                lastPath = file.getParentFile();
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
    }
}
