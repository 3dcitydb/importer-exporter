/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class XMLValidationStatusDialog extends JDialog implements EventHandler {
    private final EventDispatcher eventDispatcher;

    private JLabel titleLabel;
    private JLabel messageLabel;
    private JProgressBar progressBar;
    private JLabel fileCounterLabel;
    private JButton button;

    private volatile boolean acceptStatusUpdate = true;

    public XMLValidationStatusDialog(JFrame frame,
                                     String windowTitle,
                                     String statusTitle,
                                     String statusDetails,
                                     EventDispatcher eventDispatcher) {
        super(frame, windowTitle, true);

        this.eventDispatcher = eventDispatcher;
        eventDispatcher.addEventHandler(EventType.COUNTER, this);
        eventDispatcher.addEventHandler(EventType.STATUS_DIALOG_MESSAGE, this);
        eventDispatcher.addEventHandler(EventType.STATUS_DIALOG_TITLE, this);
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

        initGUI(statusTitle, statusDetails);
    }

    private void initGUI(
            String statusTitle,
            String statusDetails) {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        Object arc = UIManager.get("ProgressBar.arc");
        UIManager.put("ProgressBar.arc", 0);

        String[] details = statusDetails != null ?
                statusDetails.split("<br\\s*/*>") :
                new String[0];

        titleLabel = new JLabel(statusTitle != null ? statusDetails : "");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        messageLabel = new JLabel(" ");
        button = new JButton(Language.I18N.getString("common.button.cancel"));

        JLabel fileCounter = new JLabel(Language.I18N.getString("common.status.dialog.fileCounter"));
        fileCounterLabel = new JLabel("n/a", SwingConstants.TRAILING);
        fileCounterLabel.setPreferredSize(new Dimension(100, fileCounterLabel.getPreferredSize().height));

        progressBar = new JProgressBar();

        setLayout(new GridBagLayout());
        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        {
            JPanel messagePanel = new JPanel();
            messagePanel.setBackground(UIManager.getColor("TextField.background"));
            messagePanel.setLayout(new GridBagLayout());
            {
                int i = 0;
                for (; i < details.length; i++) {
                    JLabel detail = new JLabel(details[i]);
                    messagePanel.add(detail, GuiUtil.setConstraints(0, i, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, i == 0 ? 5 : 3, 5, 0, 5));
                }

                messagePanel.add(fileCounter, GuiUtil.setConstraints(0, i, 1, 0, GridBagConstraints.HORIZONTAL, i == 0 ? 5 : 10, 5, 5, 5));
                messagePanel.add(fileCounterLabel, GuiUtil.setConstraints(1, i, 1, 0, GridBagConstraints.HORIZONTAL, i == 0 ? 5 : 10, 5, 5, 5));
            }

            main.add(titleLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 0));
            main.add(messageLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 5, 0));
            main.add(progressBar, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
            main.add(messagePanel, GuiUtil.setConstraints(0, 3, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
        }

        add(main, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 10, 10, 0, 10));
        add(button, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 15, 10, 10, 10));

        setMinimumSize(new Dimension(300, 100));
        pack();

        UIManager.put("ProgressBar.arc", arc);
        progressBar.setIndeterminate(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                eventDispatcher.removeEventHandler(XMLValidationStatusDialog.this);
            }
        });
    }

    public JButton getButton() {
        return button;
    }

    @Override
    public void handleEvent(Event e) throws Exception {
        if (e.getEventType() == EventType.INTERRUPT) {
            acceptStatusUpdate = false;
            messageLabel.setText(Language.I18N.getString("common.dialog.msg.abort"));
            progressBar.setIndeterminate(true);
        } else if (e.getEventType() == EventType.STATUS_DIALOG_MESSAGE && acceptStatusUpdate) {
            messageLabel.setText(((StatusDialogMessage) e).getMessage());
        } else if (e.getEventType() == EventType.STATUS_DIALOG_TITLE && acceptStatusUpdate) {
            titleLabel.setText(((StatusDialogTitle) e).getTitle());
        } else if (e.getEventType() == EventType.COUNTER && ((CounterEvent) e).getType() == CounterType.FILE) {
            fileCounterLabel.setText(String.valueOf(((CounterEvent) e).getCounter()));
        }
    }
}
