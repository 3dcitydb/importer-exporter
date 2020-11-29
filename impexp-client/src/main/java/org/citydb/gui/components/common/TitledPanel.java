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
    public static final int TOP = 0;
    public static final int LEFT = 0;
    public static final int BOTTOM = 15;
    public static final int RIGHT = 0;

    private String title = "";
    private Icon icon;
    private int horizontalAlignment = SwingConstants.LEADING;
    private boolean showSeparator = true;
    private Insets margin;

    private JComponent header;
    private JToggleButton toggleButton;

    public TitledPanel withTitle(String title, Icon icon, int horizontalAlignment) {
        this.title = title;
        this.icon = icon;
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }

    public TitledPanel withTitle(String title) {
        return withTitle(title, null, SwingConstants.LEADING);
    }

    public TitledPanel withTitle(String title, Icon icon) {
        return withTitle(title, icon, SwingConstants.LEADING);
    }

    public TitledPanel withToggleButton(JToggleButton toggleButton) {
        this.toggleButton = adaptMargin(toggleButton);
        return this;
    }

    public TitledPanel showSeparator(boolean showSeparator) {
        this.showSeparator = showSeparator;
        return this;
    }

    public TitledPanel withMargin(Insets margin) {
        this.margin = margin;
        return this;
    }

    public TitledPanel buildWithoutContent() {
        return build(Box.createVerticalGlue());
    }

    public TitledPanel build(Component content) {
        setLayout(new GridBagLayout());

        int top = margin != null ? margin.top : TOP;
        int left = margin != null ? margin.left : LEFT;
        int bottom = margin != null ? margin.bottom : BOTTOM;
        int right = margin != null ? margin.right : RIGHT;

        Component leading;
        int iconTextGap;
        int paddingLeft;

        if (toggleButton == null) {
            JToggleButton dummy = adaptMargin(new JCheckBox());
            Dimension dimension = dummy.getPreferredSize();

            leading = Box.createVerticalStrut(dimension.height);
            iconTextGap = 0;
            paddingLeft = dimension.width + dummy.getIconTextGap();
        } else {
            leading = toggleButton;
            iconTextGap = toggleButton.getIconTextGap();
            paddingLeft = 0;
        }

        header = showSeparator ?
                new JXTitledSeparator(title, horizontalAlignment, icon) :
                new JLabel(title, icon, horizontalAlignment);

        add(leading, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, top, left, 5, iconTextGap));
        add(header, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, top, 0, 5, right));
        add(content, GuiUtil.setConstraints(1, 1, 1, 1, GridBagConstraints.BOTH, 0, paddingLeft, bottom, right));

        if (toggleButton != null) {
            header.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    toggleButton.doClick();
                }
            });
        }

        return this;
    }

    public void setTitle(String title) {
        if (showSeparator) {
            ((JXTitledSeparator) header).setTitle(title);
        } else {
            ((JLabel) header).setText(title);
        }
    }

    private JToggleButton adaptMargin(JToggleButton toggleButton) {
        Insets margin = toggleButton.getMargin();
        toggleButton.setMargin(new Insets(margin.top, 0, margin.bottom, 0));
        return toggleButton;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        header.setEnabled(enabled);

        if (toggleButton != null) {
            toggleButton.setEnabled(enabled);
        }
    }
}
