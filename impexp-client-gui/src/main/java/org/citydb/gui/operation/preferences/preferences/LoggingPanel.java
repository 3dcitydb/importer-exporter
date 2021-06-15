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
package org.citydb.gui.operation.preferences.preferences;

import org.citydb.cli.util.CliConstants;
import org.citydb.config.Config;
import org.citydb.config.gui.style.LogLevelStyle;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.LogFileMode;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.global.Logging;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.ColorPicker;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.operation.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.log.Logger;
import org.citydb.core.util.CoreConstants;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;

public class LoggingPanel extends AbstractPreferencesComponent {
    private final Logger log = Logger.getInstance();

    private TitledPanel consolePanel;
    private TitledPanel filePanel;

    private JLabel logLevelConsoleLabel;
    private JComboBox<LogLevel> logLevelConsoleCombo;
    private JCheckBox wrapTextConsole;
    private JCheckBox useLogFile;
    private JLabel logLevelFileLabel;
    private JComboBox<LogLevel> logLevelFileCombo;
    private JCheckBox truncateLogFile;
    private JCheckBox useAlternativeLogFile;
    private JTextField alternativeLogFileText;
    private JButton alternativeLogFileButton;
    private JPanel listPanel;

    private JLabel colorSchemeLabel;
    private JList<LogColor> logColors;
    private JCheckBox useForeground;
    private ColorPicker foregroundColor;
    private JCheckBox useBackground;
    private ColorPicker backgroundColor;
    private JTextPane preview;

    private final ImpExpGui mainView;

    public LoggingPanel(ImpExpGui mainView, Config config) {
        super(config);
        this.mainView = mainView;
        initGui();
    }

    @Override
    public boolean isModified() {
        Logging logging = config.getGlobalConfig().getLogging();

        if (logLevelConsoleCombo.getSelectedItem() != logging.getConsole().getLogLevel()) return true;
        if (wrapTextConsole.isSelected() != logging.getConsole().isWrapText()) return true;
        if (isLogFileModified()) return true;
        if ((truncateLogFile.isSelected() && logging.getFile().getLogFileMode() == LogFileMode.APPEND) ||
                (!truncateLogFile.isSelected() && logging.getFile().getLogFileMode() == LogFileMode.TRUNCATE))
            return true;

        for (int i = 0; i < logColors.getModel().getSize(); i++)
            if (logColors.getModel().getElementAt(i).isModified()) return true;

        return false;
    }

    private boolean isLogFileModified() {
        Logging logging = config.getGlobalConfig().getLogging();

        if (useLogFile.isSelected() != logging.getFile().isActive()) return true;
        if (useAlternativeLogFile.isSelected() != logging.getFile().isUseAlternativeLogFile()) return true;
        if (!alternativeLogFileText.getText().equals(logging.getFile().getAlternativeLogFile())) return true;
        if (logLevelFileCombo.getSelectedItem() != logging.getFile().getLogLevel()) return true;

        return false;
    }

    private void initGui() {
        logLevelConsoleLabel = new JLabel();
        logLevelConsoleCombo = new JComboBox<>();
        wrapTextConsole = new JCheckBox();
        useLogFile = new JCheckBox();
        logLevelFileLabel = new JLabel();
        logLevelFileCombo = new JComboBox<>();
        truncateLogFile = new JCheckBox();
        useAlternativeLogFile = new JCheckBox();
        alternativeLogFileText = new JTextField();
        alternativeLogFileButton = new JButton();

        preview = new JTextPane();
        preview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, UIManager.getFont("Label.font").getSize()));
        preview.setEditable(false);

        useForeground = new JCheckBox();
        foregroundColor = new ColorPicker();
        useBackground = new JCheckBox();
        backgroundColor = new ColorPicker();

        colorSchemeLabel = new JLabel();
        logColors = new JList<>();
        logColors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultListModel<LogColor> model = new DefaultListModel<>();
        logColors.setModel(model);

        StringJoiner joiner = new StringJoiner("\n");
        for (LogLevel level : LogLevel.values()) {
            LogColor logColor = new LogColor(level);
            model.addElement(logColor);
            joiner.add(logColor.previewText);
        }

        preview.setText(joiner.toString());

        setLayout(new GridBagLayout());
        {
            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                content.add(logLevelConsoleLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                content.add(logLevelConsoleCombo, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
                content.add(wrapTextConsole, GuiUtil.setConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 0));

                JPanel colorPanel = new JPanel();
                colorPanel.setLayout(new GridBagLayout());
                colorPanel.add(useForeground, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                colorPanel.add(foregroundColor, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));
                colorPanel.add(useBackground, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                colorPanel.add(backgroundColor, GuiUtil.setConstraints(1, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));

                listPanel = new JPanel();
                listPanel.setLayout(new BorderLayout());
                listPanel.add(logColors);

                JPanel stylingPanel = new JPanel();
                stylingPanel.setLayout(new GridBagLayout());
                stylingPanel.add(listPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
                stylingPanel.add(colorPanel, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 10, 0, 0));

                content.add(colorSchemeLabel, GuiUtil.setConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 5, 0));
                content.add(stylingPanel, GuiUtil.setConstraints(0, 3, 2, 1, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
                content.add(preview, GuiUtil.setConstraints(0, 4, 2, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
            }

            consolePanel = new TitledPanel().build(content);
        }
        {
            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {

                JPanel sub1 = new JPanel();
                sub1.setLayout(new GridBagLayout());
                {
                    sub1.add(logLevelFileLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
                    sub1.add(logLevelFileCombo, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 0,0));
                }

                JPanel sub2 = new JPanel();
                sub2.setLayout(new GridBagLayout());
                {
                    sub2.add(useAlternativeLogFile, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
                    sub2.add(alternativeLogFileText, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
                    sub2.add(alternativeLogFileButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
                }

                content.add(useLogFile, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
                content.add(sub1, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
                content.add(truncateLogFile, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
                content.add(sub2, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
            }

            filePanel = new TitledPanel().build(content);
        }

        add(consolePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        add(filePanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

        useForeground.addActionListener(e -> {
            if (!logColors.isSelectionEmpty())
                logColors.getSelectedValue().selectForeground();
        });

        useBackground.addActionListener(e -> {
            if (!logColors.isSelectionEmpty())
                logColors.getSelectedValue().selectBackground();
        });

        foregroundColor.addColorPickedListener(c -> {
            if (useForeground.isSelected() && !logColors.isSelectionEmpty())
                logColors.getSelectedValue().applyForeground(c);
        });

        backgroundColor.addColorPickedListener(c -> {
            if (useBackground.isSelected() && !logColors.isSelectionEmpty())
                logColors.getSelectedValue().applyBackground(c);
        });

        logColors.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (!logColors.isSelectionEmpty())
                    logColors.getSelectedValue().applyListSelection();
                else {
                    useForeground.setSelected(false);
                    useBackground.setSelected(false);
                    foregroundColor.setColor(null);
                    backgroundColor.setColor(null);
                }
            }
        });

        alternativeLogFileButton.addActionListener(e -> {
            String sExp = browseFile(Language.I18N.getString("pref.general.logging.label.useAltLogFile"), alternativeLogFileText.getText());
            if (!sExp.isEmpty())
                alternativeLogFileText.setText(sExp);
        });

        ActionListener logFileListener = e -> setEnabledLogFile();
        useLogFile.addActionListener(logFileListener);
        useAlternativeLogFile.addActionListener(logFileListener);

        PopupMenuDecorator.getInstance().decorate(alternativeLogFileText);

        UIManager.addPropertyChangeListener(e -> {
            if ("lookAndFeel".equals(e.getPropertyName())) {
                SwingUtilities.invokeLater(this::updateComponentUI);
            }
        });

        updateComponentUI();
    }

    private void updateComponentUI() {
        preview.setBackground(UIManager.getColor("TextField.background"));
        preview.setBorder(UIManager.getBorder("ScrollPane.border"));
        listPanel.setBorder(UIManager.getBorder("ScrollPane.border"));
    }

    private void setEnabledLogFile() {
        useAlternativeLogFile.setEnabled(useLogFile.isSelected());
        logLevelFileLabel.setEnabled(useLogFile.isSelected());
        logLevelFileCombo.setEnabled(useLogFile.isSelected());
        truncateLogFile.setEnabled(useLogFile.isSelected());

        alternativeLogFileText.setEnabled(useLogFile.isSelected() && useAlternativeLogFile.isSelected());
        alternativeLogFileButton.setEnabled(useLogFile.isSelected() && useAlternativeLogFile.isSelected());
    }

    @Override
    public void doTranslation() {
        consolePanel.setTitle(Language.I18N.getString("pref.general.logging.border.console"));
        wrapTextConsole.setText(Language.I18N.getString("pref.general.logging.label.wrapTextConsole"));
        logLevelConsoleLabel.setText(Language.I18N.getString("pref.general.logging.label.logLevel"));
        colorSchemeLabel.setText(Language.I18N.getString("pref.general.logging.title.colorScheme"));
        useForeground.setText(Language.I18N.getString("pref.general.logging.label.foreground"));
        foregroundColor.setDialogTitle(Language.I18N.getString("pref.general.logging.title.select"));
        useBackground.setText(Language.I18N.getString("pref.general.logging.label.background"));
        backgroundColor.setDialogTitle(Language.I18N.getString("pref.general.logging.title.select"));

        filePanel.setTitle(Language.I18N.getString("pref.general.logging.border.file"));
        useLogFile.setText(Language.I18N.getString("pref.general.logging.label.useLogFile"));
        logLevelFileLabel.setText(Language.I18N.getString("pref.general.logging.label.logLevel"));
        truncateLogFile.setText(Language.I18N.getString("pref.general.logging.label.truncateLogFile"));
        useAlternativeLogFile.setText(Language.I18N.getString("pref.general.logging.label.useAltLogFile"));
        alternativeLogFileButton.setText(Language.I18N.getString("common.button.browse"));
    }

    @Override
    public void loadSettings() {
        Logging logging = config.getGlobalConfig().getLogging();

        wrapTextConsole.setSelected(logging.getConsole().isWrapText());
        mainView.getConsole().setLineWrap(wrapTextConsole.isSelected());
        mainView.getConsole().repaint();

        for (int i = 0; i < logColors.getModel().getSize(); i++) {
            LogColor logColor = logColors.getModel().getElementAt(i);
            LogLevelStyle style = config.getGuiConfig().getConsoleWindow().getStyle().getLogLevelStyle(logColor.level);
            mainView.getStyledConsoleLogger().applyLogLevelStyle(logColor.level, style);
            logColor.reset();
            logColor.updatePreview();
        }

        if (logColors.isSelectionEmpty())
            logColors.setSelectedIndex(0);
        else {
            int index = logColors.getSelectedIndex();
            logColors.clearSelection();
            logColors.setSelectedIndex(index);
        }

        logLevelConsoleCombo.removeAllItems();
        logLevelFileCombo.removeAllItems();
        for (LogLevel level : LogLevel.values()) {
            logLevelConsoleCombo.addItem(level);
            logLevelFileCombo.addItem(level);
        }

        logLevelConsoleCombo.setSelectedItem(logging.getConsole().getLogLevel());
        logLevelFileCombo.setSelectedItem(logging.getFile().getLogLevel());

        truncateLogFile.setSelected(logging.getFile().getLogFileMode() == LogFileMode.TRUNCATE);
        useLogFile.setSelected(logging.getFile().isActive());
        useAlternativeLogFile.setSelected(logging.getFile().isUseAlternativeLogFile());
        alternativeLogFileText.setText(logging.getFile().getAlternativeLogFile());

        setEnabledLogFile();
    }

    @Override
    public void setSettings() {
        Logging logging = config.getGlobalConfig().getLogging();
        boolean isModified = isLogFileModified();

        LogLevel consoleLogLevel = (LogLevel) logLevelConsoleCombo.getSelectedItem();
        logging.getConsole().setLogLevel(consoleLogLevel);
        log.setConsoleLogLevel(consoleLogLevel);

        logging.getConsole().setWrapText(wrapTextConsole.isSelected());
        mainView.getConsole().setLineWrap(wrapTextConsole.isSelected());
        mainView.getConsole().repaint();

        for (int i = 0; i < logColors.getModel().getSize(); i++) {
            LogColor logColor = logColors.getModel().getElementAt(i);
            LogLevelStyle logLevelStyle = config.getGuiConfig().getConsoleWindow().getStyle().getLogLevelStyle(logColor.level);
            Style style = mainView.getStyledConsoleLogger().getStyle(logColor.level);
            logLevelStyle.setForeground(GuiUtil.colorToHex((Color) style.getAttribute(StyleConstants.Foreground)));
            logLevelStyle.setBackground(GuiUtil.colorToHex((Color) style.getAttribute(StyleConstants.Background)));

            logColor.reset();
        }

        if (useAlternativeLogFile.isSelected() && alternativeLogFileText.getText().trim().length() == 0) {
            useAlternativeLogFile.setSelected(false);
            setEnabledLogFile();
        }

        LogLevel fileLogLevel = (LogLevel) logLevelFileCombo.getSelectedItem();
        log.setFileLogLevel(fileLogLevel);
        logging.getFile().setLogLevel(fileLogLevel);
        logging.getFile().setLogFileMode(truncateLogFile.isSelected() ? LogFileMode.TRUNCATE : LogFileMode.APPEND);
        logging.getFile().setActive(useLogFile.isSelected());
        logging.getFile().setUseAlternativeLogFile(useAlternativeLogFile.isSelected());
        logging.getFile().setAlternativeLogFile(alternativeLogFileText.getText());

        // change log file
        if (isModified) {
            if (useLogFile.isSelected()) {
                Path logFile = useAlternativeLogFile.isSelected() ?
                        Paths.get(logging.getFile().getAlternativeLogFile()) :
                        CoreConstants.IMPEXP_DATA_DIR.resolve(CliConstants.LOG_DIR).resolve(log.getDefaultLogFileName());

                boolean success = log.appendLogFile(logFile, logging.getFile().getLogFileMode());
                if (!success) {
                    useLogFile.setSelected(false);
                    useAlternativeLogFile.setSelected(false);
                    logging.getFile().setActive(false);
                    logging.getFile().setUseAlternativeLogFile(false);
                    log.detachLogFile();
                }
            } else {
                log.detachLogFile();
            }
        }
    }

    @Override
    public String getTitle() {
        return Language.I18N.getString("pref.tree.general.logging");
    }

    private String browseFile(String title, String currentFile) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setCurrentDirectory(new File(currentFile));

        int result = chooser.showSaveDialog(mainView);
        return result == JFileChooser.CANCEL_OPTION ? "" : chooser.getSelectedFile().toString();
    }

    private class LogColor {
        final LogLevel level;
        String previewText;
        Color foreground;
        Color background;

        LogColor(LogLevel level) {
            this.level = level;
            previewText = log.getPrefix(level) + "This a " + level.name() + " log message.";
        }

        void applyForeground(Color color) {
            foreground = apply(StyleConstants.Foreground, color);
        }

        void applyBackground(Color color) {
            background = apply(StyleConstants.Background, color);
        }

        Color apply(Object key, Color color) {
            Style style = mainView.getStyledConsoleLogger().getStyle(level);
            style.addAttribute(key, color);
            updatePreview();
            return color;
        }

        void selectForeground() {
            select(foreground, Color.BLACK, StyleConstants.Foreground, useForeground, foregroundColor);
        }

        void selectBackground() {
            select(background, Color.WHITE, StyleConstants.Background, useBackground, backgroundColor);
        }

        void select(Color color, Color defaultColor, Object key, JCheckBox checkBox, ColorPicker colorPicker) {
            Style style = mainView.getStyledConsoleLogger().getStyle(level);
            colorPicker.setEnabled(checkBox.isSelected());

            if (checkBox.isSelected()) {
                style.addAttribute(key, color != null ? color : defaultColor);
                colorPicker.setColor(color != null ? color : defaultColor);
            } else {
                style.removeAttribute(key);
                colorPicker.setColor(null);
            }

            updatePreview();
        }

        void applyListSelection() {
            Style style = mainView.getStyledConsoleLogger().getStyle(level);
            applyListSelection((Color) style.getAttribute(StyleConstants.Foreground), useForeground, foregroundColor);
            applyListSelection((Color) style.getAttribute(StyleConstants.Background), useBackground, backgroundColor);
        }

        void applyListSelection(Color color, JCheckBox checkBox, ColorPicker colorPicker) {
            checkBox.setSelected(color != null);
            colorPicker.setEnabled(color != null);
            colorPicker.setColor(color);
        }

        boolean isModified() {
            LogLevelStyle logLevelStyle = config.getGuiConfig().getConsoleWindow().getStyle().getLogLevelStyle(level);
            Style style = mainView.getStyledConsoleLogger().getStyle(level);

            if (logLevelStyle.isSetForeground() && !logLevelStyle.getForeground().equals(
                    GuiUtil.colorToHex((Color) style.getAttribute(StyleConstants.Foreground)))) return true;
            if (logLevelStyle.isSetBackground() && !logLevelStyle.getBackground().equals(
                    GuiUtil.colorToHex((Color) style.getAttribute(StyleConstants.Background)))) return true;

            if (!logLevelStyle.isSetForeground() && style.getAttribute(StyleConstants.Foreground) != null) return true;
            if (!logLevelStyle.isSetBackground() && style.getAttribute(StyleConstants.Background) != null) return true;

            return false;
        }

        void reset() {
            Style style = mainView.getStyledConsoleLogger().getStyle(level);
            foreground = (Color) style.getAttribute(StyleConstants.Foreground);
            background = (Color) style.getAttribute(StyleConstants.Background);
        }

        void updatePreview() {
            Style style = mainView.getStyledConsoleLogger().getStyle(level);
            int index = preview.getText().indexOf(previewText);
            if (index != -1)
                preview.getStyledDocument().setCharacterAttributes(index, previewText.length(), style, true);
        }

        @Override
        public String toString() {
            return level.name();
        }
    }
}


