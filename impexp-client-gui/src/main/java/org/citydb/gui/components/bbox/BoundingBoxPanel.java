/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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
package org.citydb.gui.components.bbox;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.Position;
import org.citydb.config.i18n.Language;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.components.BlankNumberFormatter;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.components.srs.SrsComboBox;
import org.citydb.gui.components.srs.SrsComboBoxFactory;
import org.citydb.gui.map.MapWindow;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class BoundingBoxPanel extends JPanel implements EventHandler, BoundingBoxListener {
    private final Logger log = Logger.getInstance();
    private boolean isEnabled;
    private boolean isEditable;

    private final JButton map;
    private final JButton copy;
    private final JButton paste;
    private final JLabel srsLabel;
    private final SrsComboBox srsComboBox;
    private final JFormattedTextField xmin;
    private final JFormattedTextField ymin;
    private final JFormattedTextField xmax;
    private final JFormattedTextField ymax;
    private final JLabel xminLabel;
    private final JLabel xmaxLabel;
    private final JLabel yminLabel;
    private final JLabel ymaxLabel;

    private final BBoxPopupMenuWrapper[] bboxPopups;
    private final BoundingBoxClipboardHandler clipboardHandler;

    public BoundingBoxPanel(ViewController viewController) {
        ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
        clipboardHandler = BoundingBoxClipboardHandler.getInstance();
        isEnabled = isEditable = true;

        srsLabel = new JLabel();
        srsComboBox = SrsComboBoxFactory.getInstance().createSrsComboBox(true);

        NumberFormatter bboxFormat = new BlankNumberFormatter(new DecimalFormat("##########.##############",
                DecimalFormatSymbols.getInstance(Locale.ENGLISH)));
        xmin = new JFormattedTextField(bboxFormat);
        ymin = new JFormattedTextField(bboxFormat);
        xmax = new JFormattedTextField(bboxFormat);
        ymax = new JFormattedTextField(bboxFormat);

        xmin.setColumns(10);
        ymin.setColumns(10);
        xmax.setColumns(10);
        ymax.setColumns(10);

        xminLabel = new JLabel("<html>x<sub>min</sub></html>");
        xmaxLabel = new JLabel("<html>x<sub>max</sub></html>");
        yminLabel = new JLabel("<html>y<sub>min</sub></html>");
        ymaxLabel = new JLabel("<html>y<sub>max</sub></html>");

        // buttons and srs combo box
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridBagLayout());

        map = new JButton(new FlatSVGIcon("org/citydb/gui/icons/map.svg"));
        copy = new JButton(new FlatSVGIcon("org/citydb/gui/icons/copy.svg"));
        paste = new JButton(new FlatSVGIcon("org/citydb/gui/icons/paste.svg"));

        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(BorderFactory.createEmptyBorder());
        toolBar.add(map);
        toolBar.addSeparator();
        toolBar.add(copy);
        toolBar.add(paste);

        actionPanel.add(toolBar, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
        actionPanel.add(srsLabel, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 30, 0, 5));
        actionPanel.add(srsComboBox, GuiUtil.setConstraints(4, 0, 1, 1, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        srsComboBox.setPreferredSize(new Dimension(50, srsComboBox.getPreferredSize().height));

        // input fields
        JPanel inputFieldsPanel = new JPanel();
        inputFieldsPanel.setLayout(new GridBagLayout());
        inputFieldsPanel.add(xminLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 5));
        inputFieldsPanel.add(xmin, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
        inputFieldsPanel.add(xmaxLabel, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.NONE, 0, 10, 0, 5));
        inputFieldsPanel.add(xmax, GuiUtil.setConstraints(3, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        inputFieldsPanel.add(yminLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.NONE, 5, 0, 0, 5));
        inputFieldsPanel.add(ymin, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 5));
        inputFieldsPanel.add(ymaxLabel, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.NONE, 5, 10, 0, 5));
        inputFieldsPanel.add(ymax, GuiUtil.setConstraints(3, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));

        setLayout(new GridBagLayout());
        add(actionPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
        add(inputFieldsPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));

        // popup menus
        PopupMenuDecorator popupMenuDecorator = PopupMenuDecorator.getInstance();
        bboxPopups = new BBoxPopupMenuWrapper[4];

        bboxPopups[0] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(xmin));
        bboxPopups[1] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(ymin));
        bboxPopups[2] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(xmax));
        bboxPopups[3] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(ymax));

        // button actions
        map.addActionListener(e -> {
            commitBoundingBox();
            SwingUtilities.invokeLater(() -> MapWindow.getInstance(viewController)
                    .withBoundingBoxListener(isEditable ? this : null)
                    .withBoundingBox(getBoundingBox())
                    .setVisible(true));
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

    public void addComponent(JComponent component, boolean indent) {
        int left = indent ? xminLabel.getPreferredSize().width + 10 : 0;
        add(component, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.HORIZONTAL, 5, left, 0, 0));
    }

    public void addComponent(JComponent component) {
        addComponent(component, false);
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
        commitBoundingBox();
        clipboardHandler.putBoundingBox(getBoundingBox());
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

    public void clearBoundingBox() {
        xmin.setText(null);
        ymin.setText(null);
        xmax.setText(null);
        ymax.setText(null);
    }

    public SrsComboBox getSrsComboBox() {
        return srsComboBox;
    }

    public void showMapButton(boolean show) {
        map.setVisible(show);
    }

    public void showCopyBoundingBoxButton(boolean show) {
        copy.setVisible(show);
    }

    public void showPasteBoundingBoxButton(boolean show) {
        paste.setVisible(show);
    }

    public void handleEvent(Event event) throws Exception {
        doTranslation();
    }

    private void commitBoundingBox() {
        try {
            xmin.commitEdit();
            ymin.commitEdit();
            xmax.commitEdit();
            ymax.commitEdit();
        } catch (ParseException e) {
            log.error("Failed to interpret [" + xmin.getText() + "," + ymin.getText() + "," +
                    xmax.getText() + "," + ymax.getText() + "] as bounding box.");
        }
    }

    private final class BBoxPopupMenuWrapper {
        private final JMenuItem copy;
        private final JMenuItem paste;

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
