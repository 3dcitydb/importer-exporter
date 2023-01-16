/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2023
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

package org.citydb.gui.operation.exporter.preferences;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.Namespace;
import org.citydb.config.project.exporter.NamespaceMode;
import org.citydb.config.project.exporter.Namespaces;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.*;

public class NamespacesPanel extends InternalPreferencesComponent {
    private TitledPanel namespacesPanel;
    private JPanel content;
    private JCheckBox customizeNamespaces;
    private JCheckBox skipOthers;
    private NamespaceComponent first;

    public NamespacesPanel(Config config) {
        super(config);
        initGui();
    }

    @Override
    public boolean isModified() {
        Namespaces namespaces = config.getExportConfig().getCityGMLOptions().getNamespaces();

        if (customizeNamespaces.isSelected() != namespaces.isEnabled()) return true;
        if (skipOthers.isSelected() != namespaces.isSkipOthers()) return true;
        if (!namespaces.hasNamespaces() && !getSelectedItem(first.namespace).trim().isEmpty()) return true;

        if (namespaces.hasNamespaces()) {
            NamespaceComponent current = first;
            for (Namespace namespace : namespaces.getNamespaces().values()) {
                if (current == null) return true;
                if (!getSelectedItem(current.namespace).equals(namespace.getURI())) return true;
                if (!getSelectedItem(current.schema).equals(namespace.getSchemaLocation())) return true;
                if (!getSelectedItem(current.prefix).equals(namespace.getPrefix())) return true;
                if (current.skipButton.isSelected() != (namespace.getMode() == NamespaceMode.SKIP)) return true;
                if (current.forceButton.isSelected() != (namespace.getMode() == NamespaceMode.FORCE)) return true;
                current = current.next;
            }

            return current != null;
        }

        return false;
    }

    private void initGui() {
        customizeNamespaces = new JCheckBox();
        skipOthers = new JCheckBox();
        first = new NamespaceComponent(true);

        content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.add(first.panel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));

        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        {
            main.add(skipOthers, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
            main.add(content, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.HORIZONTAL, 10, 0, 0, 0));
        }

        namespacesPanel = new TitledPanel()
                .withToggleButton(customizeNamespaces)
                .build(main);

        setLayout(new GridBagLayout());
        add(namespacesPanel, GuiUtil.setConstraints(0, 0, 1, 10, GridBagConstraints.BOTH, 0, 0, 0, 0));

        customizeNamespaces.addActionListener(l -> setEnabledComponents());
        setEnabledComponents();
    }

    @Override
    public void switchLocale(Locale locale) {
        namespacesPanel.setTitle(Language.I18N.getString("pref.export.namespaces.label.customizeNamespaces"));
        skipOthers.setText(Language.I18N.getString("pref.export.namespaces.label.skipOthers"));

        NamespaceComponent current = first;
        do {
            current.switchLocale();
        } while ((current = current.next) != null);
    }

    @Override
    public void loadSettings() {
        Namespaces namespaces = config.getExportConfig().getCityGMLOptions().getNamespaces();
        customizeNamespaces.setSelected(namespaces.isEnabled());
        skipOthers.setSelected(namespaces.isSkipOthers());

        first.next = null;
        if (namespaces.hasNamespaces()) {
            NamespaceComponent current = first;
            Iterator<Namespace> iterator = namespaces.getNamespaces().values().iterator();
            while (iterator.hasNext()) {
                Namespace namespace = iterator.next();
                if (namespace.isSetURI()) {
                    current.namespace.setSelectedItem(namespace.getURI());
                    current.schema.setSelectedItem(namespace.getSchemaLocation());
                    current.prefix.setSelectedItem(namespace.getPrefix());
                    current.skipButton.setSelected(namespace.getMode() == NamespaceMode.SKIP);
                    current.forceButton.setSelected(namespace.getMode() == NamespaceMode.FORCE);
                    current.setVisible(!current.skipButton.isSelected());
                    if (iterator.hasNext()) {
                        current = current.add();
                    }
                }
            }
        }

        updateComponents();
        setEnabledComponents();
    }

    @Override
    public void setSettings() {
        Namespaces namespaces = config.getExportConfig().getCityGMLOptions().getNamespaces();
        namespaces.setEnabled(customizeNamespaces.isSelected());
        namespaces.setSkipOthers(skipOthers.isSelected());

        Map<String, Namespace> result = new LinkedHashMap<>();
        NamespaceComponent current = first;
        boolean updateFirst = false;
        boolean componentsChanged = false;

        do {
            String namespaceURI = getSelectedItem(current.namespace);
            if (!namespaceURI.trim().isEmpty() && !result.containsKey(namespaceURI)) {
                Namespace namespace = new Namespace();
                namespace.setURI(namespaceURI);
                namespace.setSchemaLocation(getSelectedItem(current.schema));
                namespace.setPrefix(getSelectedItem(current.prefix));
                if (current.skipButton.isSelected()) {
                    namespace.setMode(NamespaceMode.SKIP);
                } else if (current.forceButton.isSelected()) {
                    namespace.setMode(NamespaceMode.FORCE);
                }

                result.put(namespaceURI, namespace);
                if (updateFirst) {
                    first.namespace.setSelectedItem(namespaceURI);
                    current.remove();
                    updateFirst = false;
                    componentsChanged = true;
                }
            } else {
                if (current == first) {
                    updateFirst = true;
                } else {
                    current.remove();
                    componentsChanged = true;
                }
            }
        } while ((current = current.next) != null);

        namespaces.setNamespaces(result.values());
        if (componentsChanged) {
            updateComponents();
        }

        if (result.isEmpty()) {
            namespaces.setEnabled(false);
            customizeNamespaces.setSelected(false);
            setEnabledComponents();
        }
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("pref.tree.export.namespaces");
    }

    private void setEnabledComponents() {
        skipOthers.setEnabled(customizeNamespaces.isSelected());

        NamespaceComponent current = first;
        do {
            current.setEnabled(customizeNamespaces.isSelected());
        } while ((current = current.next) != null);
    }

    private void updateComponents() {
        SwingUtilities.invokeLater(() -> {
            content.removeAll();
            int index = 0;

            NamespaceComponent current = first;
            do {
                content.add(current.panel, GuiUtil.setConstraints(0, index++, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
            } while ((current = current.next) != null);

            content.revalidate();
        });
    }

    private String getSelectedItem(JComboBox<String> comboBox) {
        return comboBox.isEditable() ?
                ((JTextComponent) comboBox.getEditor().getEditorComponent()).getText() :
                (String) comboBox.getSelectedItem();
    }

    private class NamespaceComponent {
        private final JPanel panel;
        private final JPanel optionsPanel;
        private final JLabel namespaceLabel;
        private final JComboBox<String> namespace;
        private final JLabel schemaLabel;
        private final JComboBox<String> schema;
        private final JLabel prefixLabel;
        private final JComboBox<String> prefix;

        private final JToggleButton skipButton;
        private final JToggleButton forceButton;
        private final JButton addButton;
        private final JButton removeButton;

        private NamespaceComponent previous;
        private NamespaceComponent next;

        NamespaceComponent() {
            this(false);
        }

        NamespaceComponent(boolean first) {
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            namespaceLabel = new JLabel(Language.I18N.getString("pref.export.namespaces.label.namespace")) {
                @Override
                public Dimension getPreferredSize() {
                    return alignLabelSize();
                }

                @Override
                public Dimension getMinimumSize() {
                    return alignLabelSize();
                }
            };

            schemaLabel = new JLabel(Language.I18N.getString("pref.export.namespaces.label.schema")) {
                @Override
                public Dimension getPreferredSize() {
                    return alignLabelSize();
                }

                @Override
                public Dimension getMinimumSize() {
                    return alignLabelSize();
                }
            };

            namespace = new JComboBox<>();
            namespace.setEditable(true);
            schema = new JComboBox<>();
            schema.setEditable(true);
            prefixLabel = new JLabel(Language.I18N.getString("pref.export.namespaces.label.prefix"));
            prefix = new JComboBox<>();
            prefix.setEditable(true);

            Modules.getModules().stream()
                    .map(Module::getNamespaceURI)
                    .filter(Objects::nonNull)
                    .sorted().forEach(namespace::addItem);
            namespace.setSelectedItem(null);

            Modules.getModules().stream()
                    .map(Module::getSchemaLocation)
                    .filter(Objects::nonNull)
                    .sorted().forEach(schema::addItem);
            schema.setSelectedItem(null);

            Modules.getModules().stream()
                    .map(Module::getNamespacePrefix)
                    .filter(Objects::nonNull)
                    .distinct().sorted().forEach(prefix::addItem);
            prefix.setSelectedItem(null);

            skipButton = new JToggleButton(new FlatSVGIcon("org/citydb/gui/icons/skip.svg"));
            skipButton.setToolTipText(Language.I18N.getString("pref.export.namespaces.label.skip"));
            forceButton = new JToggleButton(new FlatSVGIcon("org/citydb/gui/icons/priority.svg"));
            forceButton.setToolTipText(Language.I18N.getString("pref.export.namespaces.label.force"));
            addButton = new JButton(new FlatSVGIcon("org/citydb/gui/icons/add.svg"));
            removeButton = new JButton(new FlatSVGIcon("org/citydb/gui/icons/remove.svg"));

            JToolBar toolBar = new JToolBar();
            toolBar.setBorder(BorderFactory.createEmptyBorder());
            toolBar.add(skipButton);
            toolBar.add(forceButton);
            toolBar.addSeparator();
            toolBar.add(addButton);
            toolBar.add(removeButton);

            JPanel namespacePanel = new JPanel();
            namespacePanel.setLayout(new GridBagLayout());
            {
                namespacePanel.add(namespaceLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
                namespacePanel.add(namespace, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
                namespacePanel.add(toolBar, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, 0, 0));

                if (first) {
                    Dimension size = toolBar.getPreferredSize();
                    toolBar.remove(removeButton);
                    Dimension reduced = toolBar.getPreferredSize();
                    namespacePanel.add(Box.createRigidArea(new Dimension(size.width - reduced.width, size.height - reduced.height)),
                            GuiUtil.setConstraints(3, 1, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 0));
                }
            }

            optionsPanel = new JPanel();
            optionsPanel.setLayout(new GridBagLayout());
            {
                optionsPanel.add(schemaLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
                optionsPanel.add(schema, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
                optionsPanel.add(prefixLabel, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, 0, 5));
                optionsPanel.add(prefix, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            }

            if (!first) {
                JSeparator separator = new JSeparator();
                separator.setMinimumSize(new Dimension(1, 2));
                separator.setPreferredSize(separator.getMinimumSize());
                panel.add(separator, GuiUtil.setConstraints(0, 0, 3, 1, 1, 0, GridBagConstraints.HORIZONTAL, 10, 0, 10, 0));
            }

            panel.add(namespacePanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
            panel.add(optionsPanel, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));

            toolBar.setMinimumSize(toolBar.getPreferredSize());

            skipButton.addActionListener(e -> {
                setVisible(!skipButton.isSelected());
                forceButton.setSelected(false);
            });

            forceButton.addActionListener(e -> {
                setVisible(true);
                skipButton.setSelected(false);
            });

            addButton.addActionListener(e -> {
                add();
                updateComponents();
            });

            removeButton.addActionListener(e -> {
                remove();
                updateComponents();
            });

            PopupMenuDecorator.getInstance().decorate(namespace, schema, prefix);
        }

        private NamespaceComponent add() {
            NamespaceComponent component = new NamespaceComponent();
            if (next != null) {
                component.next = next;
                next.previous = component;
            }

            next = component;
            component.previous = this;

            return component;
        }

        private void remove() {
            if (previous != null) {
                previous.next = next;
            }

            if (next != null) {
                next.previous = previous;
            }
        }

        private void setEnabled(boolean enable) {
            namespaceLabel.setEnabled(enable);
            namespace.setEnabled(enable);
            skipButton.setEnabled(enable);
            forceButton.setEnabled(enable);
            addButton.setEnabled(enable);
            removeButton.setEnabled(enable);
            schemaLabel.setEnabled(enable);
            schema.setEnabled(enable);
            prefixLabel.setEnabled(enable);
            prefix.setEnabled(enable);
        }

        private void setVisible(boolean visible) {
            optionsPanel.setVisible(visible);
        }

        private void switchLocale() {
            namespaceLabel.setText(Language.I18N.getString("pref.export.namespaces.label.namespace"));
            schemaLabel.setText(Language.I18N.getString("pref.export.namespaces.label.schema"));
            prefixLabel.setText(Language.I18N.getString("pref.export.namespaces.label.prefix"));
            skipButton.setToolTipText(Language.I18N.getString("pref.export.namespaces.label.skip"));
            forceButton.setToolTipText(Language.I18N.getString("pref.export.namespaces.label.force"));
        }

        private Dimension alignLabelSize() {
            Dimension size = new JLabel(Language.I18N.getString("pref.export.namespaces.label.namespace")).getPreferredSize();
            Dimension other = new JLabel(Language.I18N.getString("pref.export.namespaces.label.schema")).getPreferredSize();
            return size.width > other.width ? size : other;
        }
    }
}
