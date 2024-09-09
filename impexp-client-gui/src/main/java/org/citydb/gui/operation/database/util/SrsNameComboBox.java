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
package org.citydb.gui.operation.database.util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class SrsNameComboBox extends JComboBox<String> {
    private final SrsNameEditor editor;

    public SrsNameComboBox() {
        editor = new SrsNameEditor();
        ((JTextField) editor.getEditorComponent()).setBorder(BorderFactory.createEmptyBorder());
        setEditor(editor);
        setEditable(true);

        addItem("urn:ogc:def:crs:EPSG::<SRID>");
        addItem("urn:ogc:def:crs,crs:EPSG::<SRID>,crs:EPSG::<HEIGHT_SRID>");
        addItem("EPSG:<SRID>");
        setSelectedItem(null);
    }

    public void setText(String value) {
        editor.setItem(value);
    }

    public String getText() {
        return editor.getItem().toString();
    }

    public void updateSrid(int srid) {
        editor.srid = srid;
    }

    public String getSridPattern() {
        return "<SRID>";
    }

    public static final class SrsNameEditor extends BasicComboBoxEditor {
        private int srid;

        @Override
        public void setItem(Object anObject) {
            if (anObject != null)
                super.setItem(anObject.toString().replace("<SRID>", String.valueOf(srid)));
            else
                super.setItem("");
        }
    }

    @Override
    public void updateUI() {
        String text = editor != null ? getText() : null;
        super.updateUI();

        if (text != null) {
            setText(text);
        }
    }
}


