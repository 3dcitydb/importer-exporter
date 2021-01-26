package org.citydb.gui.components.dialog;

import org.citydb.config.i18n.Language;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConfirmationCheckDialog {
    private final List<Object> messages;
    private final JCheckBox disableDialog;

    private Component parent;
    private int messageType;
    private int optionType;
    private String title;

    public ConfirmationCheckDialog() {
        messages = new ArrayList<>();
        disableDialog = new JCheckBox(Language.I18N.getString("common.dialog.msg.noShow"));
    }

    public static ConfirmationCheckDialog defaults() {
        ConfirmationCheckDialog dialog = new ConfirmationCheckDialog();
        dialog.messageType = JOptionPane.QUESTION_MESSAGE;
        dialog.optionType = JOptionPane.YES_NO_CANCEL_OPTION;
        return dialog;
    }

    public ConfirmationCheckDialog withParentComponent(Component parent) {
        this.parent = parent;
        return this;
    }

    public ConfirmationCheckDialog withMessageType(int messageType) {
        this.messageType = messageType;
        return this;
    }

    public ConfirmationCheckDialog withOptionType(int optionType) {
        this.optionType = optionType;
        return this;
    }

    public ConfirmationCheckDialog withTitle(String title) {
        this.title = title;
        return this;
    }

    public ConfirmationCheckDialog addMessage(Object message) {
        messages.add(message);
        return this;
    }

    public int show() {
        messages.add("\n");
        messages.add(disableDialog);
        return JOptionPane.showConfirmDialog(parent, messages.toArray(), title, optionType, messageType);
    }

    public boolean keepShowingDialog() {
        return !disableDialog.isSelected();
    }
}
