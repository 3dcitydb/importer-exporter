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
package org.citydb.modules.preferences.gui.preferences;

import org.citydb.config.Config;
import org.citydb.config.gui.style.LogLevelStyle;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.global.Logging;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.common.AlphaButton;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.util.ClientConstants;
import org.citydb.util.CoreConstants;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.StringJoiner;

@SuppressWarnings("serial")
public class LoggingPanel extends AbstractPreferencesComponent {
    private final Logger log = Logger.getInstance();

    private JPanel consolePanel;
    private JPanel filePanel;

    private JLabel logLevelConsoleLabel;
    private JComboBox<LogLevel> logLevelConsoleCombo;
    private JCheckBox wrapTextConsole;
    private JCheckBox useLogFile;
    private JLabel logLevelFileLabel;
    private JComboBox<LogLevel> logLevelFileCombo;
    private JCheckBox useLogPath;
    private JTextField logPathText;
    private JButton logPathButton;

    private JLabel colorSchemeLabel;
    private JList<LogColor> logColors;
    private JCheckBox useForeground;
    private JButton foregroundColor;
    private JCheckBox useBackground;
    private JButton backgroundColor;
    private JTextPane preview;

    private ImpExpGui mainView;

    public LoggingPanel(ImpExpGui mainView, Config config) {
        super(config);
        this.mainView = mainView;
        initGui();
    }

    @Override
    public boolean isModified() {
        Logging logging = config.getProject().getGlobal().getLogging();

        if (logLevelConsoleCombo.getSelectedItem() != logging.getConsole().getLogLevel()) return true;
        if (wrapTextConsole.isSelected() != logging.getConsole().isWrapText()) return true;
        if (useLogFile.isSelected() != logging.getFile().isSet()) return true;
        if (useLogPath.isSelected() != logging.getFile().isSetUseAlternativeLogPath()) return true;
        if (!logPathText.getText().equals(logging.getFile().getAlternativeLogPath())) return true;
        if (logLevelFileCombo.getSelectedItem() != logging.getFile().getLogLevel()) return true;

        for (int i = 0; i < logColors.getModel().getSize(); i++)
            if (logColors.getModel().getElementAt(i).isModified()) return true;

        return false;
    }

    private boolean isLogFileModified() {
        Logging logging = config.getProject().getGlobal().getLogging();

        if (useLogFile.isSelected() != logging.getFile().isSet()) return true;
        if (useLogPath.isSelected() != logging.getFile().isSetUseAlternativeLogPath()) return true;
        if (!logPathText.getText().equals(logging.getFile().getAlternativeLogPath())) return true;

        return false;
    }

    private void initGui() {
        logLevelConsoleLabel = new JLabel();
        logLevelConsoleCombo = new JComboBox<>();
        wrapTextConsole = new JCheckBox();
        useLogFile = new JCheckBox();
        logLevelFileLabel = new JLabel();
        logLevelFileCombo = new JComboBox<>();
        useLogPath = new JCheckBox();
        logPathText = new JTextField();
        logPathButton = new JButton();

        wrapTextConsole.setIconTextGap(10);
        useLogFile.setIconTextGap(10);
        useLogPath.setIconTextGap(10);

        preview = new JTextPane();
        preview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, UIManager.getFont("Label.font").getSize()));
        preview.setEditable(false);
        preview.setBorder(BorderFactory.createEtchedBorder());

        useForeground = new JCheckBox();
        useForeground.setIconTextGap(10);
        foregroundColor = new AlphaButton();
        foregroundColor.setPreferredSize(new Dimension(80, 1));
        foregroundColor.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        foregroundColor.setContentAreaFilled(false);

        useBackground = new JCheckBox();
        useBackground.setIconTextGap(10);
        backgroundColor = new AlphaButton();
        backgroundColor.setPreferredSize(new Dimension(80, 1));
        backgroundColor.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        backgroundColor.setContentAreaFilled(false);

        colorSchemeLabel = new JLabel();
        logColors = new JList<>();
        logColors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logColors.setBorder(BorderFactory.createEtchedBorder());

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
            consolePanel = new JPanel();
            add(consolePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
            consolePanel.setBorder(BorderFactory.createTitledBorder(""));
            consolePanel.setLayout(new GridBagLayout());
            {
                consolePanel.add(logLevelConsoleLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 5));
                consolePanel.add(logLevelConsoleCombo, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 5));
                consolePanel.add(wrapTextConsole, GuiUtil.setConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 5));

                JPanel colorPanel = new JPanel();
                colorPanel.setLayout(new GridBagLayout());
                colorPanel.add(useForeground, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 5, 5));
                colorPanel.add(foregroundColor, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
                colorPanel.add(useBackground, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                colorPanel.add(backgroundColor, GuiUtil.setConstraints(1, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));

                JPanel stylingPanel = new JPanel();
                stylingPanel.setLayout(new GridBagLayout());
                stylingPanel.add(logColors, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
                stylingPanel.add(colorPanel, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 10, 0, 0));

                consolePanel.add(colorSchemeLabel, GuiUtil.setConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
                consolePanel.add(stylingPanel, GuiUtil.setConstraints(0, 3, 2, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
                consolePanel.add(preview, GuiUtil.setConstraints(0, 4, 2, 1, 0, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
            }

            filePanel = new JPanel();
            add(filePanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 5, 0));
            filePanel.setBorder(BorderFactory.createTitledBorder(""));
            filePanel.setLayout(new GridBagLayout());
            int lmargin = (int) (useLogPath.getPreferredSize().getWidth()) + 11;
            {
                filePanel.add(useLogFile, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
                JPanel sub1 = new JPanel();
                sub1.setLayout(new GridBagLayout());
                filePanel.add(sub1, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
                {
                    sub1.add(logLevelFileLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
                    sub1.add(logLevelFileCombo, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 5, 5));
                }
                filePanel.add(useLogPath, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
                JPanel sub2 = new JPanel();
                sub2.setLayout(new GridBagLayout());
                filePanel.add(sub2, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
                {
                    sub2.add(logPathText, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, lmargin, 5, 5));
                    sub2.add(logPathButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
                }
            }
        }

        useForeground.addActionListener(e -> {
            if (!logColors.isSelectionEmpty())
                logColors.getSelectedValue().selectForeground();
        });

        useBackground.addActionListener(e -> {
            if (!logColors.isSelectionEmpty())
                logColors.getSelectedValue().selectBackground();
        });

        foregroundColor.addActionListener(e -> {
            if (useForeground.isSelected() && !logColors.isSelectionEmpty())
                logColors.getSelectedValue().applyForeground();
        });

        backgroundColor.addActionListener(e -> {
            if (useBackground.isSelected() && !logColors.isSelectionEmpty())
                logColors.getSelectedValue().applyBackground();
        });

        logColors.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (!logColors.isSelectionEmpty())
                    logColors.getSelectedValue().applyListSelection();
                else {
                    useForeground.setSelected(false);
                    useBackground.setSelected(false);
                    foregroundColor.setBackground(null);
                    backgroundColor.setBackground(null);
                }
            }
        });

        logPathButton.addActionListener(e -> {
            String sExp = browseFile(Language.I18N.getString("pref.general.logging.label.useLogPath"), logPathText.getText());
            if (!sExp.isEmpty())
                logPathText.setText(sExp);
        });

        ActionListener logFileListener = e -> setEnabledLogFile();
        useLogFile.addActionListener(logFileListener);
        useLogPath.addActionListener(logFileListener);

        PopupMenuDecorator.getInstance().decorate(logPathText);
    }

    private void setEnabledLogFile() {
        useLogPath.setEnabled(useLogFile.isSelected());
        logLevelFileLabel.setEnabled(useLogFile.isSelected());
        logLevelFileCombo.setEnabled(useLogFile.isSelected());

        logPathText.setEnabled(useLogFile.isSelected() && useLogPath.isSelected());
        logPathButton.setEnabled(useLogFile.isSelected() && useLogPath.isSelected());
    }

    @Override
    public void doTranslation() {
        ((TitledBorder) consolePanel.getBorder()).setTitle(Language.I18N.getString("pref.general.logging.border.console"));
        wrapTextConsole.setText(Language.I18N.getString("pref.general.logging.label.wrapTextConsole"));
        logLevelConsoleLabel.setText(Language.I18N.getString("pref.general.logging.label.logLevel"));
        colorSchemeLabel.setText(Language.I18N.getString("pref.general.logging.title.colorScheme"));
        useForeground.setText(Language.I18N.getString("pref.general.logging.label.foreground"));
        useBackground.setText(Language.I18N.getString("pref.general.logging.label.background"));

        ((TitledBorder) filePanel.getBorder()).setTitle(Language.I18N.getString("pref.general.logging.border.file"));
        useLogFile.setText(Language.I18N.getString("pref.general.logging.label.useLogFile"));
        logLevelFileLabel.setText(Language.I18N.getString("pref.general.logging.label.logLevel"));
        useLogPath.setText(Language.I18N.getString("pref.general.logging.label.useLogPath"));
        logPathButton.setText(Language.I18N.getString("common.button.browse"));
    }

    @Override
    public void loadSettings() {
        Logging logging = config.getProject().getGlobal().getLogging();

        wrapTextConsole.setSelected(logging.getConsole().isWrapText());
        mainView.getConsole().setLineWrap(wrapTextConsole.isSelected());
        mainView.getConsole().repaint();

        for (int i = 0; i < logColors.getModel().getSize(); i++) {
            LogColor logColor = logColors.getModel().getElementAt(i);

            LogLevelStyle style = config.getGui().getConsoleWindow().getStyle().getLogLevelStyle(logColor.level);
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

        useLogFile.setSelected(logging.getFile().isSet());
        useLogPath.setSelected(logging.getFile().isSetUseAlternativeLogPath());
        logPathText.setText(logging.getFile().getAlternativeLogPath());

        logLevelConsoleCombo.removeAllItems();
        logLevelFileCombo.removeAllItems();
        for (LogLevel level : LogLevel.values()) {
            logLevelConsoleCombo.addItem(level);
            logLevelFileCombo.addItem(level);
        }

        logLevelConsoleCombo.setSelectedItem(logging.getConsole().getLogLevel());
        logLevelFileCombo.setSelectedItem(logging.getFile().getLogLevel());

        setEnabledLogFile();
    }

    @Override
    public void setSettings() {
        Logging logging = config.getProject().getGlobal().getLogging();
        boolean isModified = isLogFileModified();

        if (useLogPath.isSelected() && logPathText.getText().trim().length() == 0) {
            useLogPath.setSelected(false);
            setEnabledLogFile();
        }

        logging.getFile().setActive(useLogFile.isSelected());
        logging.getFile().setUseAlternativeLogPath(useLogPath.isSelected());
        logging.getFile().setAlternativeLogPath(logPathText.getText());

        LogLevel consoleLogLevel = (LogLevel) logLevelConsoleCombo.getSelectedItem();
        logging.getConsole().setLogLevel(consoleLogLevel);
        log.setDefaultConsoleLogLevel(consoleLogLevel);

        logging.getConsole().setWrapText(wrapTextConsole.isSelected());
        mainView.getConsole().setLineWrap(wrapTextConsole.isSelected());
        mainView.getConsole().repaint();

        for (int i = 0; i < logColors.getModel().getSize(); i++) {
            LogColor logColor = logColors.getModel().getElementAt(i);

            LogLevelStyle logLevelStyle = config.getGui().getConsoleWindow().getStyle().getLogLevelStyle(logColor.level);
            Style style = mainView.getStyledConsoleLogger().getStyle(logColor.level);
            logLevelStyle.setForeground(GuiUtil.colorToHex((Color) style.getAttribute(StyleConstants.Foreground)));
            logLevelStyle.setBackground(GuiUtil.colorToHex((Color) style.getAttribute(StyleConstants.Background)));

            logColor.reset();
        }

        LogLevel fileLogLevel = (LogLevel) logLevelFileCombo.getSelectedItem();
        logging.getFile().setLogLevel(fileLogLevel);
        log.setDefaultFileLogLevel(fileLogLevel);

        // change log file
        if (isModified && useLogFile.isSelected()) {
            String logPath = useLogPath.isSelected() ? logging.getFile().getAlternativeLogPath() :
                    CoreConstants.IMPEXP_DATA_DIR.resolve(ClientConstants.LOG_DIR).toString();

            if (!logPath.equals(config.getInternal().getCurrentLogPath())) {
                boolean success = log.appendLogFile(logPath, true);

                if (!success) {
                    useLogFile.setSelected(false);
                    useLogPath.setSelected(false);
                    logging.getFile().setActive(false);
                    logging.getFile().setUseAlternativeLogPath(false);

                    log.detachLogFile();
                } else
                    config.getInternal().setCurrentLogPath(logPath);
            }
        } else if (isModified && !useLogFile.isSelected()) {
            log.detachLogFile();
            config.getInternal().setCurrentLogPath("");
        }
    }

    @Override
    public String getTitle() {
        return Language.I18N.getString("pref.tree.general.logging");
    }

    private String browseFile(String title, String oldDir) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(oldDir));

        int result = chooser.showSaveDialog(mainView);
        return (result == JFileChooser.CANCEL_OPTION) ? "" : chooser.getSelectedFile().toString();
    }

    private class LogColor {
        final LogLevel level;
        String previewText;
        Color foreground;
        Color background;

        LogColor(LogLevel level) {
            this.level = level;
            previewText = log.getPrefix(level) + "This a " + level.value() + " log message.";
        }

        void applyForeground() {
            foreground = apply(StyleConstants.Foreground, foregroundColor);
        }

        void applyBackground() {
            background = apply(StyleConstants.Background, backgroundColor);
        }

        Color apply(Object key, JButton button) {
            Style style = mainView.getStyledConsoleLogger().getStyle(level);
            Color tmp = (Color) style.getAttribute(key);

            Color color = JColorChooser.showDialog(mainView, Language.I18N.getString("pref.general.logging.title.select"), tmp);
            if (color != null) {
                style.addAttribute(key, color);
                button.setBackground(color);
                updatePreview();

                return color;
            } else
                return tmp;
        }

        void selectForeground() {
            select(foreground, Color.BLACK, StyleConstants.Foreground, useForeground, foregroundColor);
        }

        void selectBackground() {
            select(background, Color.WHITE, StyleConstants.Background, useBackground, backgroundColor);
        }

        void select(Color color, Color defaultColor, Object key, JCheckBox checkBox, JButton button) {
            Style style = mainView.getStyledConsoleLogger().getStyle(level);
            button.setEnabled(checkBox.isSelected());

            if (checkBox.isSelected()) {
                style.addAttribute(key, color != null ? color : defaultColor);
                button.setBackground(color != null ? color : defaultColor);
            } else {
                style.removeAttribute(key);
                button.setBackground(null);
            }

            updatePreview();
        }

        void applyListSelection() {
            Style style = mainView.getStyledConsoleLogger().getStyle(level);
            applyListSelection((Color) style.getAttribute(StyleConstants.Foreground), useForeground, foregroundColor);
            applyListSelection((Color) style.getAttribute(StyleConstants.Background), useBackground, backgroundColor);
        }

        void applyListSelection(Color color, JCheckBox checkBox, JButton button) {
            checkBox.setSelected(color != null);
            button.setEnabled(color != null);
            button.setBackground(color);
        }

        boolean isModified() {
            LogLevelStyle logLevelStyle = config.getGui().getConsoleWindow().getStyle().getLogLevelStyle(level);
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
            return level.value();
        }
    }
}


