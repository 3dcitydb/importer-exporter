package org.citydb.gui.operation.preferences.plugin;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.core.plugin.Plugin;
import org.citydb.core.plugin.PluginManager;
import org.citydb.gui.operation.common.DefaultPreferencesComponent;
import org.citydb.gui.util.CheckBoxListDecorator;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PluginsOverviewPanel extends DefaultPreferencesComponent {
    private final PluginsOverviewPlugin plugin;
    private JList<Plugin> plugins;
    private CheckBoxListDecorator<Plugin> decorator;
    private PluginInfoPanel infoPanel;

    protected PluginsOverviewPanel(PluginsOverviewPlugin plugin, Config config) {
        super(config);
        this.plugin = plugin;
        setScrollable(false);
        initGui();
    }

    @Override
    public boolean isModified() {
        for (int i = 0; i < plugins.getModel().getSize(); i++) {
            Plugin plugin = plugins.getModel().getElementAt(i);
            if (decorator.isCheckBoxSelected(i) != plugin.isEnabled()) {
                return true;
            }
        }

        return false;
    }

    private void initGui() {
        infoPanel = new PluginInfoPanel();

        DefaultListModel<Plugin> model = new DefaultListModel<>();
        PluginManager.getInstance().getExternalPlugins().stream()
                .sorted(Comparator.comparing(p -> p.getMetadata().getName()))
                .forEach(model::addElement);

        plugins = new JList<>(model);
        plugins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plugins.addListSelectionListener(this::pluginSelected);
        plugins.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Plugin plugin = (Plugin) value;
                super.getListCellRendererComponent(list, plugin.getMetadata().getName(), index, isSelected, cellHasFocus);
                setFont(getFont().deriveFont(Font.BOLD));
                setIcon(infoPanel.getPluginLogo(plugin, 30, 30));
                return this;
            }
        });

        decorator = new CheckBoxListDecorator<>(plugins);

        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setContinuousLayout(true);
        splitPane.setLeftComponent(plugins);
        splitPane.setRightComponent(scrollPane);

        setLayout(new GridBagLayout());
        add(splitPane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));

        if (plugins.getModel().getSize() > 0) {
            plugins.setSelectedIndex(0);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setBorder(UIManager.getBorder("ScrollPane.border"));
    }

    @Override
    public void doTranslation() {
        infoPanel.doTranslation();
    }

    public void pluginSelected(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            infoPanel.buildFor(plugins.getSelectedValue());
        }
    }

    @Override
    public void loadSettings() {
        for (int i = 0; i < plugins.getModel().getSize(); i++) {
            Plugin plugin = plugins.getModel().getElementAt(i);
            decorator.setCheckBoxSelected(i, plugin.isEnabled());
        }
    }

    @Override
    public void setSettings() {
        Map<Plugin, Boolean> pluginStates = new HashMap<>();
        for (int i = 0; i < plugins.getModel().getSize(); i++) {
            Plugin plugin = plugins.getModel().getElementAt(i);
            if (decorator.isCheckBoxSelected(i) != plugin.isEnabled()) {
                pluginStates.put(plugin, decorator.isCheckBoxSelected(i));
            }

            config.setPluginEnabled(plugin.getClass().getName(), decorator.isCheckBoxSelected(i));
        }

        if (!plugin.isShuttingDown() && !pluginStates.isEmpty()) {
            PluginManager.getInstance().setPluginsEnabled(pluginStates);
        }
    }

    @Override
    public String getTitle() {
        return Language.I18N.getString("pref.tree.plugins");
    }
}
