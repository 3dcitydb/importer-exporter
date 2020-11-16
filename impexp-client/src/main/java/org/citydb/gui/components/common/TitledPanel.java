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

package org.citydb.gui.components.common;

import org.citydb.gui.util.GuiUtil;
import org.jdesktop.swingx.JXTitledSeparator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TitledPanel extends JPanel {
    public static final int PADDING_TOP = 0;
    public static final int PADDING_BOTTOM = 15;
    private final JXTitledSeparator separator;

    public TitledPanel(String title) {
        setLayout(new GridBagLayout());
        separator = new JXTitledSeparator(title);
        add(separator, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, PADDING_TOP, 0, 5, 0));
    }

    public TitledPanel(String title, JComponent content) {
        this(title);
        setContent(content);
    }

    public TitledPanel() {
        this("");
    }

    public TitledPanel(JComponent content) {
        this("", content);
    }

    public void setTitle(String title) {
        separator.setTitle(title);
    }

    public void setContent(JComponent content) {
        add(content, GuiUtil.setConstraints(1, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, PADDING_BOTTOM, 0));
    }

    public TitledPanel withToggleButton(JToggleButton toggleButton) {
        int right = toggleButton.getIconTextGap() - toggleButton.getMargin().right;
        add(toggleButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, PADDING_TOP, 0, 5, right));
        separator.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                toggleButton.doClick();
            }
        });
        return this;
    }

    public int getIconTextGap() {
        return separator.getIconTextGap();
    }

    public void setIconTextGap(int iconTextGap) {
        separator.setIconTextGap(iconTextGap);
    }
}
