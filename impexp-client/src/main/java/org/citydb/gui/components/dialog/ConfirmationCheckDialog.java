package org.citydb.gui.components.dialog;

import org.citydb.config.i18n.Language;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class ConfirmationCheckDialog {
    private final Component parent;
    private final String title;
    private final int optionType;

    private JPanel content;
    private JCheckBox disableDialog;

    public ConfirmationCheckDialog(Component parent, String title, Component message, int optionType) {
        this.parent = parent;
        this.title = title;
        this.optionType = optionType;

        initGui(message);
    }

    public ConfirmationCheckDialog(Component parent, String title, String message, int optionType) {
        this(parent, title, new JLabel(message), optionType);
    }

    public ConfirmationCheckDialog(Component parent, String title, Component message) {
        this(parent, title, message, JOptionPane.YES_NO_CANCEL_OPTION);
    }

    public ConfirmationCheckDialog(Component parent, String title, String message) {
        this(parent, title, message, JOptionPane.YES_NO_CANCEL_OPTION);
    }

    private void initGui(Component message) {
        content = new JPanel(new GridBagLayout());
        disableDialog = new JCheckBox(Language.I18N.getString("common.dialog.msg.noShow"));

        content.add(message, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        content.add(disableDialog, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 10, 0, 0, 0));
    }

    public int show() {
        return JOptionPane.showConfirmDialog(parent, content, title, optionType);
    }

    public boolean keepShowingDialog() {
        return !disableDialog.isSelected();
    }
}
