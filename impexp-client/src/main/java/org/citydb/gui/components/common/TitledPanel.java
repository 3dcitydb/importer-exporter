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

import javax.swing.*;
import java.awt.*;

public class TitledPanel extends JPanel {
    public static final int TOP = 0;
    public static final int LEFT = 0;
    public static final int BOTTOM = 15;
    public static final int RIGHT = 0;

    private String title = "";
    private Icon icon;
    private boolean showSeparator = true;
    private Insets margin;

    private JToggleButton toggleButton;
    private JComponent titleComponent;
    private JSeparator separator;

    public TitledPanel withTitle(String title, Icon icon) {
        this.title = title;
        this.icon = icon;
        return this;
    }

    public TitledPanel withTitle(String title) {
        return withTitle(title, null);
    }

    public TitledPanel withToggleButton(JToggleButton toggleButton) {
        Insets margin = UIManager.getInsets("CheckBox.margin");
        toggleButton.setMargin(new Insets(margin.top, 0, margin.bottom, 0));
        this.toggleButton = toggleButton;
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

        Insets padding;
        if (toggleButton == null) {
            titleComponent = new JLabel(title, icon, SwingConstants.LEADING);
            padding = (Insets) UIManager.getInsets("CheckBox.margin").clone();
        } else {
            titleComponent = toggleButton;
            toggleButton.setText(title);
            padding = new Insets(0, 0, 0, 0);
        }

        padding.left = UIManager.getIcon("CheckBox.icon").getIconWidth() + UIManager.getInt("CheckBox.iconTextGap");

        JComponent header;
        if (showSeparator) {
            separator = new JSeparator();
            header = new JPanel();
            header.setLayout(new GridBagLayout());
            header.add(titleComponent, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 0));
            header.add(separator, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        } else {
            header = titleComponent;
        }

        add(header, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, top + padding.top, left, 5 + padding.bottom, right));
        add(content, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 0, left + padding.left, bottom, right));

        return this;
    }

    public void setTitle(String title) {
        if (toggleButton != null) {
            ((JToggleButton) titleComponent).setText(title);
        } else {
            ((JLabel) titleComponent).setText(title);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        titleComponent.setEnabled(enabled);

        if (separator != null) {
            separator.setEnabled(enabled);
        }

        if (toggleButton != null) {
            toggleButton.setEnabled(enabled);
        }
    }
}
