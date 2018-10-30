/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.modules.database.gui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;

public class SrsNameComboBox extends JComboBox<String> {
    private final SrsNameEditor editor;

    public SrsNameComboBox() {
        editor = new SrsNameEditor();
        setEditor(editor);
        setEditable(true);
        setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));

        addItem("urn:ogc:def:crs:EPSG::<SRID>");
        addItem("urn:ogc:def:crs,crs:EPSG::<SRID>,crs:EPSG::<HEIGHT_SRID>");
        addItem("EPSG:<SRID>");
        setSelectedItem(null);
    }

    public void updateSrid(int srid) {
        editor.srid = srid;
    }

    public String getSridPattern() {
        return "<SRID>";
    }

    public void setText(String value) {
        editor.component.setText(value);
    }

    public String getText() {
        return editor.component.getText();
    }

    @Override
    public SrsNameEditor getEditor() {
        return editor;
    }

    public void setEditorEditable(boolean value) {
        super.setEnabled(value);
        editor.component.setEnabled(true);
        editor.component.setEditable(value);
    }

    public final class SrsNameEditor implements ComboBoxEditor {
        private final JTextField component;
        private int srid;

        private SrsNameEditor() {
            component = new JTextField();
            component.setBorder(new EmptyBorder(0, 2, 0,2));
        }

        @Override
        public JTextField getEditorComponent() {
            return component;
        }

        @Override
        public void setItem(Object anObject) {
            if (anObject != null)
                component.setText(anObject.toString().replace("<SRID>", String.valueOf(srid)));
            else
                component.setText("");
        }

        @Override
        public Object getItem() {
            return component.getText();
        }

        @Override
        public void selectAll() { }

        @Override
        public void addActionListener(ActionListener ignored) { }

        @Override
        public void removeActionListener(ActionListener ignored) { }
    }
}


