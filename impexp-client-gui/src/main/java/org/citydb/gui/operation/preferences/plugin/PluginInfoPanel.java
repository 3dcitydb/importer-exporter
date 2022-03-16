package org.citydb.gui.operation.preferences.plugin;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.i18n.Language;
import org.citydb.core.plugin.Plugin;
import org.citydb.core.plugin.extension.Extension;
import org.citydb.core.plugin.metadata.PluginDescription;
import org.citydb.core.plugin.metadata.PluginMetadata;
import org.citydb.gui.components.ScrollablePanel;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PluginInfoPanel extends ScrollablePanel {
    private final Logger log = Logger.getInstance();
    private final JLabel logoLabel;
    private final JLabel nameLabel;
    private final JTextPane versionPane;
    private final JTextPane vendorPane;
    private final JTextPane homepagePane;
    private final JTextPane descriptionPane;
    private final JLabel adeSupportLabel;
    private final JCheckBox adeSupport;
    private final JLabel extensionPointsLabel;
    private final JTextPane extensionPointsPane;

    private PluginMetadata metadata;
    private TitledPanel extensionPointsPanel;

    PluginInfoPanel() {
        super(true, false);
        setLayout(new GridBagLayout());

        logoLabel = new JLabel();
        nameLabel = new JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(nameLabel.getFont().getSize() + 4f));
        versionPane = createTextPane(true);
        vendorPane = createTextPane(true);
        homepagePane = createTextPane(false);
        descriptionPane = createTextPane(false);
        adeSupportLabel = new JLabel();
        adeSupport = new JCheckBox();
        extensionPointsLabel = new JLabel();
        extensionPointsPane = createTextPane(false);

        PopupMenuDecorator.getInstance().decorate(versionPane, vendorPane, homepagePane, descriptionPane,
                extensionPointsPane);
    }

    void buildFor(Plugin plugin) {
        metadata = plugin.getMetadata();
        logoLabel.setIcon(getPluginLogo(plugin, 50, 50));
        nameLabel.setText(metadata.getName());

        removeAll();

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridBagLayout());
        {
            headerPanel.add(logoLabel, GuiUtil.setConstraints(0, 0, 1, 3, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 0, 0, 15));
            headerPanel.add(nameLabel, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));

            int row = 1;
            if (metadata.getVersion() != null) {
                versionPane.setText(metadata.getVersion());
                headerPanel.add(versionPane, GuiUtil.setConstraints(1, row++, 0, 0, GridBagConstraints.HORIZONTAL, 2, 0, 0, 0));
            }

            if (metadata.getVendor() != null && metadata.getVendor().getValue() != null) {
                vendorPane.setText(checkUrl(metadata.getVendor().getUrl()) ?
                        "<a href=\"" + metadata.getVendor().getUrl() + "\">" + metadata.getVendor().getValue() + "</a>" :
                        metadata.getVendor().getValue());
                headerPanel.add(vendorPane, GuiUtil.setConstraints(1, row, 1, 0, GridBagConstraints.HORIZONTAL, 2, 0, 0, 0));
            }
        }

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridBagLayout());
        {
            JPanel adeSupportPanel = new JPanel();
            adeSupportPanel.setLayout(new OverlayLayout(adeSupportPanel));
            {
                adeSupport.setSelected(metadata.hasADESupport());

                JPanel glassPane = new JPanel();
                glassPane.addMouseListener(new MouseAdapter() { });
                glassPane.setOpaque(false);
                adeSupportPanel.add(glassPane);

                JPanel content = new JPanel();
                content.setLayout(new BorderLayout());
                content.add(adeSupport);
                adeSupportPanel.add(content);
            }

            extensionPointsPane.setText(Arrays.stream(plugin.getClass().getInterfaces())
                    .filter(Extension.class::isAssignableFrom)
                    .map(Class::getSimpleName)
                    .sorted()
                    .collect(Collectors.joining(", ")));

            detailsPanel.add(adeSupportLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
            detailsPanel.add(adeSupportPanel, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));
            detailsPanel.add(extensionPointsLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
            detailsPanel.add(extensionPointsPane, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));

            extensionPointsPanel = new TitledPanel()
                    .withCollapseButton()
                    .build(detailsPanel);
        }

        int row = 1;
        add(headerPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 10, 10, 0, 10));

        if (checkUrl(metadata.getUrl())) {
            add(homepagePane, GuiUtil.setConstraints(0, row++, 1, 0, GridBagConstraints.HORIZONTAL, 20, 10, 0, 10));
        }

        if (!metadata.getDescriptions().isEmpty()) {
            add(descriptionPane, GuiUtil.setConstraints(0, row++, 1, 0, GridBagConstraints.HORIZONTAL, row == 2 ? 20 : 10, 10, 0, 10));
        }

        add(extensionPointsPanel, GuiUtil.setConstraints(0, row, 1, 0, GridBagConstraints.HORIZONTAL, 20, 10, 0, 10));

        switchLocale();
        revalidate();
    }

    void switchLocale() {
        if (metadata != null) {
            homepagePane.setText("<a href=\"" + metadata.getUrl() + "\">" + Language.I18N.getString("pref.plugins.homepage") + "</a>");
            PluginDescription description = metadata.getDescriptionForLocaleOrDefault(Language.I18N.getLocale());
            descriptionPane.setText(description != null ? description.getValue() : null);
            adeSupportLabel.setText(Language.I18N.getString("pref.plugins.adeSupport") + ":");
            extensionPointsLabel.setText(Language.I18N.getString("pref.plugins.extensions") + ":");
            extensionPointsPanel.setTitle(Language.I18N.getString("pref.plugins.details"));
        }
    }

    private JTextPane createTextPane(boolean disabledForeground) {
        JTextPane textPane = !disabledForeground ?
                new JTextPane() :
                new JTextPane() {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setForeground(UIManager.getColor("Label.disabledForeground"));
                    }
                };

        textPane.setContentType("text/html");
        textPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        textPane.setEditable(false);
        textPane.setFont(UIManager.getFont("Label.font"));
        textPane.setBorder(BorderFactory.createEmptyBorder());

        if (disabledForeground) {
            setForeground(UIManager.getColor("Label.disabledForeground"));
        }

        textPane.addHyperlinkListener(l -> {
            if (l.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (l.getURL() != null) {
                    try {
                        Desktop.getDesktop().browse(l.getURL().toURI());
                    } catch (Exception e) {
                        log.error("Failed to open the URL '" + l.getURL() + "'.", e);
                    }
                } else {
                    log.error("The hyperlink '" + l.getDescription() + "' cannot be opened.");
                }
            }
        });

        return textPane;
    }

    private boolean checkUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    ImageIcon getPluginLogo(Plugin plugin, int width, int height) {
        String logoName = plugin.getClass().getPackage().getName().replace('.', '/') + "/pluginLogo.svg";
        FlatSVGIcon icon = new FlatSVGIcon(logoName, width, height, plugin.getClass().getClassLoader());
        try {
            if (!icon.hasFound()) {
                icon = null;
            }
        } catch (Exception e) {
            icon = null;
        }

        if (icon == null) {
            icon = new FlatSVGIcon("org/citydb/gui/icons/plugin.svg", width, height);
        }

        return icon;
    }
}
