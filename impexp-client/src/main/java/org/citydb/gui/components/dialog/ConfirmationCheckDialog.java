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

    private ConfirmationCheckDialog() {
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

        if (SwingUtilities.isEventDispatchThread()) {
            return JOptionPane.showConfirmDialog(parent, messages.toArray(), title, optionType, messageType);
        } else {
            int[] option = new int[1];
            try {
                SwingUtilities.invokeAndWait(() -> option[0] = JOptionPane.showConfirmDialog(
                        parent, messages.toArray(), title, optionType, messageType));
            } catch (Exception e) {
                option[0] = JOptionPane.CANCEL_OPTION;
            }

            return option[0];
        }
    }

    public boolean keepShowingDialog() {
        return !disableDialog.isSelected();
    }
}
