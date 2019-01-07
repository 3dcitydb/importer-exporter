/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.gui.components.bbox;

import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.Position;
import org.citydb.config.i18n.Language;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.gui.components.mapviewer.MapWindow;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.factory.SrsComboBoxFactory;
import org.citydb.gui.factory.SrsComboBoxFactory.SrsComboBox;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;
import org.citydb.plugin.extension.view.components.DatabaseSrsComboBox;
import org.citydb.registry.ObjectRegistry;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

@SuppressWarnings("serial")
public class BoundingBoxPanelImpl extends BoundingBoxPanel implements EventHandler, BoundingBoxListener {
    private final Logger log = Logger.getInstance();
    private boolean isEnabled;
    private boolean isEditable;

    private JButton map;
    private JButton copy;
    private JButton paste;
    private JLabel srsLabel;
    private SrsComboBox srsComboBox;
    private JFormattedTextField xmin;
    private JFormattedTextField ymin;
    private JFormattedTextField xmax;
    private JFormattedTextField ymax;
    private JLabel xminLabel;
    private JLabel xmaxLabel;
    private JLabel yminLabel;
    private JLabel ymaxLabel;

    private BBoxPopupMenuWrapper[] bboxPopups;
    private BoundingBoxClipboardHandler clipboardHandler;

    public BoundingBoxPanelImpl(ViewController viewController, Config config) {
        ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
        clipboardHandler = BoundingBoxClipboardHandler.getInstance(config);
        isEnabled = isEditable = true;

        srsLabel = new JLabel();
        srsComboBox = SrsComboBoxFactory.getInstance(config).createSrsComboBox(true);

        DecimalFormat bboxFormat = new DecimalFormat("##########.##############", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        xmin = new JFormattedTextField(bboxFormat);
        ymin = new JFormattedTextField(bboxFormat);
        xmax = new JFormattedTextField(bboxFormat);
        ymax = new JFormattedTextField(bboxFormat);

        xmin.setFocusLostBehavior(JFormattedTextField.COMMIT);
        ymin.setFocusLostBehavior(JFormattedTextField.COMMIT);
        xmax.setFocusLostBehavior(JFormattedTextField.COMMIT);
        ymax.setFocusLostBehavior(JFormattedTextField.COMMIT);

        xminLabel = new JLabel("<html>x<sub>min</sub></html>");
        xmaxLabel = new JLabel("<html>x<sub>max</sub></html>");
        yminLabel = new JLabel("<html>y<sub>min</sub></html>");
        ymaxLabel = new JLabel("<html>y<sub>max</sub></html>");

        // buttons and srs combo box
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridBagLayout());

        map = new JButton();
        ImageIcon mapIcon = new ImageIcon(getClass().getResource("/org/citydb/gui/images/common/map_select.png"));
        map.setIcon(mapIcon);
        map.setMargin(new Insets(1, 1, 1, 1));

        copy = new JButton();
        ImageIcon copyIcon = new ImageIcon(getClass().getResource("/org/citydb/gui/images/common/bbox_copy.png"));
        copy.setIcon(copyIcon);
        copy.setMargin(new Insets(1, 1, 1, 1));

        paste = new JButton();
        ImageIcon pasteIcon = new ImageIcon(getClass().getResource("/org/citydb/gui/images/common/bbox_paste.png"));
        paste.setIcon(pasteIcon);
        paste.setMargin(new Insets(1, 1, 1, 1));

        actionPanel.add(map, GuiUtil.setConstraints(0, 0, 0.0, 0.0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
        actionPanel.add(copy, GuiUtil.setConstraints(1, 0, 0.0, 0.0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
        actionPanel.add(paste, GuiUtil.setConstraints(2, 0, 0.0, 0.0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
        actionPanel.add(srsLabel, GuiUtil.setConstraints(3, 0, 0.0, 0.0, GridBagConstraints.HORIZONTAL, 0, 40, 0, 5));
        actionPanel.add(srsComboBox, GuiUtil.setConstraints(4, 0, 1.0, 1.0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        srsComboBox.setPreferredSize(new Dimension(50, srsComboBox.getPreferredSize().height));

        // input fields
        JPanel inputFieldsPanel = new JPanel();
        inputFieldsPanel.setLayout(new GridBagLayout());
        inputFieldsPanel.add(xminLabel, GuiUtil.setConstraints(0, 0, 0.0, 0.0, GridBagConstraints.NONE, 0, 0, 0, 5));
        inputFieldsPanel.add(xmin, GuiUtil.setConstraints(1, 0, 1.0, 0.0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
        inputFieldsPanel.add(xmaxLabel, GuiUtil.setConstraints(2, 0, 0.0, 0.0, GridBagConstraints.NONE, 0, 10, 0, 5));
        inputFieldsPanel.add(xmax, GuiUtil.setConstraints(3, 0, 1.0, 0.0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        inputFieldsPanel.add(yminLabel, GuiUtil.setConstraints(0, 1, 0.0, 0.0, GridBagConstraints.NONE, 5, 0, 0, 5));
        inputFieldsPanel.add(ymin, GuiUtil.setConstraints(1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 5));
        inputFieldsPanel.add(ymaxLabel, GuiUtil.setConstraints(2, 1, 0.0, 0.0, GridBagConstraints.NONE, 5, 10, 0, 5));
        inputFieldsPanel.add(ymax, GuiUtil.setConstraints(3, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));

        setLayout(new GridBagLayout());
        add(actionPanel, GuiUtil.setConstraints(0, 0, 1.0, 0.0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
        add(inputFieldsPanel, GuiUtil.setConstraints(0, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));

        // popup menus
        PopupMenuDecorator popupMenuDecorator = PopupMenuDecorator.getInstance();
        bboxPopups = new BBoxPopupMenuWrapper[4];

        bboxPopups[0] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(xmin));
        bboxPopups[1] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(ymin));
        bboxPopups[2] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(xmax));
        bboxPopups[3] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(ymax));

        // button actions
        map.addActionListener(e -> {
            final MapWindow map = MapWindow.getInstance(viewController, isEditable ? BoundingBoxPanelImpl.this : null, config);
            SwingUtilities.invokeLater(() -> map.setVisible(true));
            map.setBoundingBox(getBoundingBox());
        });

        copy.addActionListener(e -> copyBoundingBoxToClipboard());
        paste.addActionListener(e -> pasteBoundingBoxFromClipboard());

        Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(e -> {
            if (isEnabled) {
                boolean enable = clipboardHandler.containsPossibleBoundingBox();
                paste.setEnabled(enable);
                for (BBoxPopupMenuWrapper bboxPopup : bboxPopups)
                    bboxPopup.paste.setEnabled(enable);
            }
        });
    }

    @Override
    public void addComponent(JComponent component) {
        add(component, GuiUtil.setConstraints(0, 2, 1.0, 0.0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
    }

    private void doTranslation() {
        map.setToolTipText(Language.I18N.getString("common.tooltip.boundingBox.map"));
        copy.setToolTipText(Language.I18N.getString("common.tooltip.boundingBox.copy"));
        paste.setToolTipText(Language.I18N.getString("common.tooltip.boundingBox.paste"));
        srsLabel.setText(Language.I18N.getString("common.label.boundingBox.crs"));

        for (BBoxPopupMenuWrapper bboxPopup : bboxPopups)
            bboxPopup.doTranslation();
    }

    private void copyBoundingBoxToClipboard() {
        try {
            xmin.commitEdit();
            ymin.commitEdit();
            xmax.commitEdit();
            ymax.commitEdit();

            clipboardHandler.putBoundingBox(getBoundingBox());
        } catch (ParseException e1) {
            log.error("Failed to interpret values of input fields as bounding box.");
        }
    }

    private void pasteBoundingBoxFromClipboard() {
        BoundingBox bbox = clipboardHandler.getBoundingBox();

        if (bbox != null) {
            xmin.setValue(bbox.getLowerCorner().getX());
            ymin.setValue(bbox.getLowerCorner().getY());
            xmax.setValue(bbox.getUpperCorner().getX());
            ymax.setValue(bbox.getUpperCorner().getY());

            if (bbox.isSetSrs())
                srsComboBox.setSelectedItem(bbox.getSrs());
            else
                srsComboBox.setDBReferenceSystem();
        }
    }

    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        isEnabled = enable;

        map.setEnabled(enable);
        copy.setEnabled(enable);
        paste.setEnabled(enable && clipboardHandler.containsPossibleBoundingBox());
        xminLabel.setEnabled(enable);
        xmaxLabel.setEnabled(enable);
        yminLabel.setEnabled(enable);
        ymaxLabel.setEnabled(enable);
        xmin.setEnabled(enable);
        xmax.setEnabled(enable);
        ymin.setEnabled(enable);
        ymax.setEnabled(enable);
        srsLabel.setEnabled(enable);
        srsComboBox.setEnabled(enable);
    }

    @Override
    public void setEditable(boolean editable) {
        isEditable = editable;

        xmin.setEditable(editable);
        ymin.setEditable(editable);
        xmax.setEditable(editable);
        ymax.setEditable(editable);
        paste.setVisible(editable);

        for (BBoxPopupMenuWrapper bboxPopup : bboxPopups)
            bboxPopup.paste.setEnabled(false);
    }

    @Override
    public BoundingBox getBoundingBox() {
        BoundingBox bbox = new BoundingBox();
        bbox.setSrs(srsComboBox.getSelectedItem());

        bbox.getLowerCorner().setX(xmin.isEditValid() && xmin.getValue() != null ? ((Number) xmin.getValue()).doubleValue() : null);
        bbox.getLowerCorner().setY(ymin.isEditValid() && ymin.getValue() != null ? ((Number) ymin.getValue()).doubleValue() : null);
        bbox.getUpperCorner().setX(xmax.isEditValid() && xmax.getValue() != null ? ((Number) xmax.getValue()).doubleValue() : null);
        bbox.getUpperCorner().setY(ymax.isEditValid() && ymax.getValue() != null ? ((Number) ymax.getValue()).doubleValue() : null);

        return bbox;
    }

    @Override
    public void setBoundingBox(BoundingBox boundingBox) {
        if (boundingBox != null) {
            Position lowerLeft = boundingBox.getLowerCorner();
            Position upperRight = boundingBox.getUpperCorner();

            xmin.setValue(lowerLeft.getX());
            ymin.setValue(lowerLeft.getY());
            xmax.setValue(upperRight.getX());
            ymax.setValue(upperRight.getY());

            if (boundingBox.isSetSrs())
                srsComboBox.setSelectedItem(boundingBox.getSrs());
        }
    }

    @Override
    public void clearBoundingBox() {
        xmin.setText(null);
        ymin.setText(null);
        xmax.setText(null);
        ymax.setText(null);
    }

    @Override
    public DatabaseSrsComboBox getSrsComboBox() {
        return srsComboBox;
    }

    @Override
    public void showMapButton(boolean show) {
        map.setVisible(show);
    }

    @Override
    public void showCopyBoundingBoxButton(boolean show) {
        copy.setVisible(show);
    }

    @Override
    public void showPasteBoundingBoxButton(boolean show) {
        paste.setVisible(show);
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        doTranslation();
    }

    private final class BBoxPopupMenuWrapper {
        private JMenuItem copy;
        private JMenuItem paste;

        BBoxPopupMenuWrapper(JPopupMenu popupMenu) {
            copy = new JMenuItem();
            paste = new JMenuItem();

            paste.setEnabled(clipboardHandler.containsPossibleBoundingBox());

            popupMenu.addSeparator();
            popupMenu.add(copy);
            popupMenu.add(paste);

            copy.addActionListener(e -> copyBoundingBoxToClipboard());
            paste.addActionListener(e -> pasteBoundingBoxFromClipboard());
        }

        private void doTranslation() {
            copy.setText(Language.I18N.getString("common.popup.boundingBox.copy"));
            paste.setText(Language.I18N.getString("common.popup.boundingBox.paste"));
        }
    }

}
