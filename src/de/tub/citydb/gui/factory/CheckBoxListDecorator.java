/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.gui.factory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class CheckBoxListDecorator extends MouseAdapter implements ListSelectionListener, ActionListener, PropertyChangeListener {
	private final JList list;
	private final ListSelectionModel checkBoxSelectionModel;
	private final Map<Integer, Boolean> enabled;
	private final int width;

	public CheckBoxListDecorator(JList list) {
		this.list = list;

		list.setCellRenderer(new CheckBoxListCellRenderer());
		list.addMouseListener(this); 
		list.addPropertyChangeListener(this);
		list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED); 

		checkBoxSelectionModel = new DefaultListSelectionModel();
		checkBoxSelectionModel.addListSelectionListener(this);

		enabled = new HashMap<Integer, Boolean>();
		width = new JCheckBox().getPreferredSize().width;
	}

	public boolean isCheckBoxSelected(int index) {
		return checkBoxSelectionModel.isSelectedIndex(index);
	}

	public void setCheckBoxSelected(int index, boolean selected) {
		if (index >= 0) {
			if (!selected) 
				checkBoxSelectionModel.removeSelectionInterval(index, index); 
			else 
				checkBoxSelectionModel.addSelectionInterval(index, index);
		}
	}

	public void deselectAllCheckBoxes() {
		for (int index = 0; index < list.getModel().getSize(); ++index)
			setCheckBoxSelected(index, false);
	}
	
	public void selectAllCheckBoxes() {
		for (int index = 0; index < list.getModel().getSize(); ++index)
			setCheckBoxSelected(index, true);
	}

	private void setCheckBoxSelected(int index) {
		if (index >= 0) {
			if (checkBoxSelectionModel.isSelectedIndex(index)) 
				checkBoxSelectionModel.removeSelectionInterval(index, index); 
			else 
				checkBoxSelectionModel.addSelectionInterval(index, index);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int index = list.locationToIndex(e.getPoint()); 
		if (index < 0) 
			return; 

		if (e.getX() > list.getCellBounds(index, index).x + width && e.getClickCount() != 2) 
			return; 

		if (!enabled.get(index))
			return;

		setCheckBoxSelected(index);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("enabled".equals(evt.getPropertyName()))
			list.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (enabled.get(list.getSelectedIndex()))
			setCheckBoxSelected(list.getSelectedIndex()); 
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		list.repaint(list.getCellBounds(e.getFirstIndex(), e.getLastIndex()));
	}

	@SuppressWarnings("serial")
	private final class CheckBoxListCellRenderer extends JPanel implements ListCellRenderer { 
		private final ListCellRenderer renderer; 
		private final JCheckBox checkBox; 

		public CheckBoxListCellRenderer() { 
			renderer = list.getCellRenderer(); 
			checkBox = new JCheckBox();

			setLayout(new BorderLayout()); 
			setOpaque(false); 
			checkBox.setOpaque(false); 

			Box box = Box.createHorizontalBox();
			box.add(checkBox);
			box.add(Box.createHorizontalStrut(5));
			add(box, BorderLayout.WEST);
		} 

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component component = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); 
			add(component, BorderLayout.CENTER); 

			boolean enable = component.isEnabled() && list.isEnabled();
			checkBox.setSelected(checkBoxSelectionModel.isSelectedIndex(index)); 
			checkBox.setEnabled(enable);
			enabled.put(index, enable);
			repaint();

			return this; 
		} 
	}
}
