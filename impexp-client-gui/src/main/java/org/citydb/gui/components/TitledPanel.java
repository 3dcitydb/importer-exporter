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

package org.citydb.gui.components;

import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TitledPanel extends JPanel {
    public static final int TOP = 0;
    public static final int LEFT = 0;
    public static final int BOTTOM = 15;
    public static final int RIGHT = 0;

    private String title = "";
    private Icon icon;
    private boolean showSeparator = true;
    private boolean collapsible;
    private boolean collapsed = true;
    private boolean showSeparatorWhenCollapsed;
    private Insets margin;

    private JLabel collapseButton;
    private JToggleButton toggleButton;
    private JLabel titleLabel;
    private JPanel separator;
    private Component content;

    public TitledPanel withTitle(String title) {
        this.title = title;
        return this;
    }

    public TitledPanel withIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    public TitledPanel withCollapseButton() {
        collapsible = true;
        return this;
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

    public TitledPanel showSeparatorWhenCollapsed(boolean showSeparatorWhenCollapsed) {
        this.showSeparatorWhenCollapsed = showSeparatorWhenCollapsed;
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
        this.content = content;
        setLayout(new GridBagLayout());

        int top = margin != null ? margin.top : TOP;
        int left = margin != null ? margin.left : LEFT;
        int bottom = margin != null ? margin.bottom : BOTTOM;
        int right = margin != null ? margin.right : RIGHT;

        int iconTextGap = UIManager.getInt("CheckBox.iconTextGap");
        int padding = UIManager.getIcon("CheckBox.icon").getIconWidth() + iconTextGap;

        titleLabel = new JLabel(title, icon, SwingConstants.LEADING);

        Component buttonComponent;
        if (toggleButton == null) {
            Insets buttonMargin = UIManager.getInsets("CheckBox.margin");
            int height = buttonMargin.top + buttonMargin.bottom + UIManager.getIcon("CheckBox.icon").getIconHeight();
            buttonComponent = Box.createVerticalStrut(height);
            iconTextGap = 0;
        } else {
            toggleButton.setText(title);
            buttonComponent = toggleButton;
        }

        JComponent titleComponent = new JPanel();
        titleComponent.setLayout(new GridBagLayout());
        titleComponent.add(buttonComponent, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 0));
        titleComponent.add(titleLabel, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, iconTextGap, 0, 0));

        JComponent headerComponent;
        if (showSeparator) {
            separator = new JPanel();
            separator.setLayout(new CardLayout());
            separator.add(new JSeparator(), "visible");
            if (!showSeparatorWhenCollapsed) {
                separator.add(Box.createVerticalGlue(), "invisible");
            }

            headerComponent = new JPanel();
            headerComponent.setLayout(new GridBagLayout());
            headerComponent.add(titleComponent, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 0));
            headerComponent.add(separator, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        } else {
            headerComponent = titleComponent;
        }

        int column = 0;
        if (collapsible) {
            collapseButton = new JLabel();
            iconTextGap = UIManager.getInt("MenuItem.iconTextGap");
            add(collapseButton, GuiUtil.setConstraints(column++, 0, 0, 0, GridBagConstraints.NONE, top, left, 5, iconTextGap));
            left = 0;
        }

        add(headerComponent, GuiUtil.setConstraints(column, 0, 1, 0, GridBagConstraints.HORIZONTAL, top, left, 5, right));
        add(content, GuiUtil.setConstraints(column, 1, 1, 1, GridBagConstraints.BOTH, 0, left + padding, bottom, right));

        if (toggleButton != null) {
            MouseAdapter adapter = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        toggleButton.doClick();
                    }
                }
            };

            titleLabel.addMouseListener(adapter);
            titleComponent.addMouseListener(adapter);
        }

        if (collapsible) {
            setCollapsed(collapsed);

            MouseAdapter adapter = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        setCollapsed(!collapsed);
                    }
                }
            };

            collapseButton.addMouseListener(adapter);
            headerComponent.addMouseListener(adapter);

            if (toggleButton != null) {
                toggleButton.addItemListener(e -> {
                    if (collapsed && e.getStateChange() == ItemEvent.SELECTED) {
                        setCollapsed(false);
                    }
                });
            }
        }

        return this;
    }

    public JLabel getTitleLabel() {
        return titleLabel;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public boolean isCollapsible() {
        return collapsible;
    }

    public boolean isCollapsed() {
        return collapsible && collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        if (collapsible) {
            this.collapsed = collapsed;
            collapseButton.setIcon(getCollapseIcon());
            content.setVisible(!collapsed);

            if (showSeparator) {
                String constraint = !showSeparatorWhenCollapsed && collapsed ? "invisible" : "visible";
                CardLayout layout = (CardLayout) separator.getLayout();
                layout.show(separator, constraint);
            }
        }
    }

    public boolean hasToggleButton() {
        return toggleButton != null;
    }

    public JToggleButton getToggleButton() {
        return toggleButton;
    }

    @Override
    public void setEnabled(boolean enabled) {
        titleLabel.setEnabled(enabled);

        if (separator != null) {
            separator.setEnabled(enabled);
        }

        if (toggleButton != null) {
            toggleButton.setEnabled(enabled);
        }
    }

    private Icon getCollapseIcon() {
        return UIManager.getIcon(collapsed ? "Tree.collapsedIcon" : "Tree.expandedIcon");
    }
}
