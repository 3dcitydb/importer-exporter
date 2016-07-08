/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.gui.factory;

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

public class CheckBoxListDecorator<T> extends MouseAdapter implements ListSelectionListener, ActionListener, PropertyChangeListener {
	private final JList<T> list;
	private final ListSelectionModel checkBoxSelectionModel;
	private final Map<Integer, Boolean> enabled;
	private final int width;

	public CheckBoxListDecorator(JList<T> list) {
		this.list = list;

		list.setCellRenderer(new CheckBoxListCellRenderer<T>());
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
	private final class CheckBoxListCellRenderer<E extends T> extends JPanel implements ListCellRenderer<E> { 
		private final ListCellRenderer<? super T> renderer; 
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

		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
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
