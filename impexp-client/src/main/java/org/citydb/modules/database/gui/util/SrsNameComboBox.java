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


