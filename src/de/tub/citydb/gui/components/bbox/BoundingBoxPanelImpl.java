/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.gui.components.bbox;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.api.gui.BoundingBoxCorner;
import de.tub.citydb.api.gui.BoundingBoxPanel;
import de.tub.citydb.api.gui.DatabaseSrsComboBox;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.components.mapviewer.MapWindow;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.factory.SrsComboBoxFactory;
import de.tub.citydb.gui.factory.SrsComboBoxFactory.SrsComboBox;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class BoundingBoxPanelImpl extends BoundingBoxPanel implements EventHandler, BoundingBoxListener {
	private final Logger LOG = Logger.getInstance();
	private final Config config;
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

	public BoundingBoxPanelImpl(Config config) {
		this.config = config;

		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.SWITCH_LOCALE, this);		
		clipboardHandler = BoundingBoxClipboardHandler.getInstance(config);
		isEnabled = isEditable = true;
		
		init();
	}

	private void init() {
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

		xminLabel = new JLabel("Xmin");
		xmaxLabel = new JLabel("Xmax");
		yminLabel = new JLabel("Ymin");
		ymaxLabel = new JLabel("Ymax");

		// buttons and srs combo box
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new GridBagLayout());

		map = new JButton();
		ImageIcon mapIcon = new ImageIcon(getClass().getResource("/resources/img/common/map_select.png")); 
		map.setIcon(mapIcon);
		map.setPreferredSize(new Dimension(mapIcon.getIconWidth() + 6, mapIcon.getIconHeight() + 6));

		copy = new JButton();
		ImageIcon copyIcon = new ImageIcon(getClass().getResource("/resources/img/common/bbox_copy.png")); 
		copy.setIcon(copyIcon);
		copy.setPreferredSize(new Dimension(copyIcon.getIconWidth() + 6, copyIcon.getIconHeight() + 6));

		paste = new JButton();
		ImageIcon pasteIcon = new ImageIcon(getClass().getResource("/resources/img/common/bbox_paste.png")); 
		paste.setIcon(pasteIcon);
		paste.setPreferredSize(new Dimension(pasteIcon.getIconWidth() + 6, pasteIcon.getIconHeight() + 6));

		actionPanel.add(map, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,5));
		actionPanel.add(copy, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,5));
		actionPanel.add(paste, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,5));
		actionPanel.add(srsLabel, GuiUtil.setConstraints(3,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,40,0,5));			
		actionPanel.add(srsComboBox, GuiUtil.setConstraints(4,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,0));
		srsComboBox.setPreferredSize(new Dimension(50, srsComboBox.getPreferredSize().height));
				
		// input fields
		JPanel inputFieldsPanel = new JPanel();
		inputFieldsPanel.setLayout(new GridBagLayout());
		xmin.setPreferredSize(xmax.getPreferredSize());
		xmax.setPreferredSize(xmin.getPreferredSize());
		ymin.setPreferredSize(ymax.getPreferredSize());
		ymax.setPreferredSize(ymin.getPreferredSize());
		inputFieldsPanel.add(xminLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,0,0,5));
		inputFieldsPanel.add(xmin, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
		inputFieldsPanel.add(xmaxLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,0,5));
		inputFieldsPanel.add(xmax, GuiUtil.setConstraints(3,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,0));
		inputFieldsPanel.add(yminLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.NONE,2,0,0,5));
		inputFieldsPanel.add(ymin, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.HORIZONTAL,2,5,0,5));
		inputFieldsPanel.add(ymaxLabel, GuiUtil.setConstraints(2,1,0.0,0.0,GridBagConstraints.NONE,2,10,0,5));
		inputFieldsPanel.add(ymax, GuiUtil.setConstraints(3,1,1.0,0.0,GridBagConstraints.HORIZONTAL,2,5,0,0));

		setLayout(new GridBagLayout());
		add(actionPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
		add(inputFieldsPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,5,0,0,0));

		// popup menus
		PopupMenuDecorator popupMenuDecorator = PopupMenuDecorator.getInstance();
		bboxPopups = new BBoxPopupMenuWrapper[4];

		bboxPopups[0] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(xmin));
		bboxPopups[1] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(ymin));
		bboxPopups[2] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(xmax));
		bboxPopups[3] = new BBoxPopupMenuWrapper(popupMenuDecorator.decorateAndGet(ymax));

		// button actions
		map.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final MapWindow map = MapWindow.getInstance(isEditable ?  BoundingBoxPanelImpl.this : null, config);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						map.setVisible(true);
					}
				});

				map.setBoundingBox(getBoundingBox());
			}
		});

		copy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyBoundingBoxToClipboard();
			}
		});

		paste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pasteBoundingBoxFromClipboard();
			}
		});

		Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(new FlavorListener() {
			public void flavorsChanged(FlavorEvent e) {
				if (isEnabled) {
					boolean enable = clipboardHandler.containsPossibleBoundingBox();
					paste.setEnabled(enable);
					for (int i = 0; i < bboxPopups.length; ++i)
						bboxPopups[i].paste.setEnabled(enable);			
				}
			}
		});
	}

	private void doTranslation() {
		map.setToolTipText(Internal.I18N.getString("common.tooltip.boundingBox.map"));
		copy.setToolTipText(Internal.I18N.getString("common.tooltip.boundingBox.copy"));
		paste.setToolTipText(Internal.I18N.getString("common.tooltip.boundingBox.paste"));
		srsLabel.setText(Internal.I18N.getString("common.label.boundingBox.crs"));

		for (int i = 0; i < bboxPopups.length; ++i)
			bboxPopups[i].doTranslation();
	}

	private void copyBoundingBoxToClipboard() {
		try {
			xmin.commitEdit();
			ymin.commitEdit();
			xmax.commitEdit();
			ymax.commitEdit();

			clipboardHandler.putBoundingBox(getBoundingBox());			
		} catch (ParseException e1) {
			LOG.error("Failed to interpret values of input fields as bounding box.");
		}
	}

	private void pasteBoundingBoxFromClipboard() {
		BoundingBox bbox = clipboardHandler.getBoundingBox();

		if (bbox != null) {
			xmin.setValue(bbox.getLowerLeftCorner().getX());
			ymin.setValue(bbox.getLowerLeftCorner().getY());
			xmax.setValue(bbox.getUpperRightCorner().getX());
			ymax.setValue(bbox.getUpperRightCorner().getY());

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
		paste.setEnabled(enable ? clipboardHandler.containsPossibleBoundingBox() : false);
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
		
		for (int i = 0; i < bboxPopups.length; ++i)
			bboxPopups[i].paste.setEnabled(false);
	}

	@Override
	public BoundingBox getBoundingBox() {
		BoundingBox bbox = new BoundingBox();
		bbox.setSrs(srsComboBox.getSelectedItem());

		bbox.getLowerLeftCorner().setX(xmin.isEditValid() && xmin.getValue() != null ? ((Number)xmin.getValue()).doubleValue() : null);
		bbox.getLowerLeftCorner().setY(ymin.isEditValid() && ymin.getValue() != null ? ((Number)ymin.getValue()).doubleValue() : null);
		bbox.getUpperRightCorner().setX(xmax.isEditValid() && xmax.getValue() != null ? ((Number)xmax.getValue()).doubleValue() : null);
		bbox.getUpperRightCorner().setY(ymax.isEditValid() && ymax.getValue() != null ? ((Number)ymax.getValue()).doubleValue() : null);

		return bbox;
	}

	@Override
	public void setBoundingBox(BoundingBox boundingBox) {
		if (boundingBox != null) {
			BoundingBoxCorner lowerLeft = boundingBox.getLowerLeftCorner();
			BoundingBoxCorner upperRight = boundingBox.getUpperRightCorner();

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

		public BBoxPopupMenuWrapper(JPopupMenu popupMenu) {
			copy = new JMenuItem();	
			paste = new JMenuItem();

			paste.setEnabled(clipboardHandler.containsPossibleBoundingBox());

			popupMenu.addSeparator();
			popupMenu.add(copy);
			popupMenu.add(paste);

			copy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					copyBoundingBoxToClipboard();
				}
			});

			paste.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pasteBoundingBoxFromClipboard();
				}
			});
		}

		private void doTranslation() {
			copy.setText(Internal.I18N.getString("common.popup.boundingBox.copy"));
			paste.setText(Internal.I18N.getString("common.popup.boundingBox.paste"));
		}
	}

}
