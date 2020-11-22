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

import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ColorPicker extends JButton {
    private final boolean isFlatLaf;
    private Color color;
    private String dialogTitle;

    private List<WeakReference<Consumer<Color>>> listeners;

    public ColorPicker() {
        isFlatLaf = UIManager.getLookAndFeel() instanceof FlatLaf;
        if (!isFlatLaf) {
            setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager.getColor("CheckBox.foreground")));
            setContentAreaFilled(false);
        }

        JCheckBox dummy = new JCheckBox();
        Dimension size = dummy.getPreferredSize();
        setPreferredSize(new Dimension(size.width * 2, (int) (size.height * 1.5)));

        addActionListener(e -> pickColor());
    }

    public ColorPicker(Color color, String dialogTitle) {
        this();
        setColor(color);
        this.dialogTitle = dialogTitle;
    }

    public void addColorChangedListener(Consumer<Color> listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }

        listeners.add(new WeakReference<>(listener));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        super.setBackground(color);
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    @Override
    public boolean isOpaque() {
        return isFlatLaf && super.isOpaque();
    }

    @Override
    public Color getForeground() {
        return color;
    }

    @Override
    public void setForeground(Color color) {
        setColor(color);
    }

    @Override
    public Color getBackground() {
        return color;
    }

    @Override
    public void setBackground(Color color) {
        setColor(color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isFlatLaf) {
            g.setColor(getBackground());
            g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
        }

        super.paintComponent(g);
    }

    private void pickColor() {
        Color color = JColorChooser.showDialog(getTopLevelAncestor(), dialogTitle, this.color);
        if (color != null) {
            this.color = color;
            setBackground(color);

            if (listeners != null) {
                listeners.stream().map(WeakReference::get)
                        .filter(Objects::nonNull)
                        .forEach(l -> SwingUtilities.invokeLater(() -> l.accept(color)));
                listeners.removeIf(r -> r.get() == null);
            }
        }
    }
}
