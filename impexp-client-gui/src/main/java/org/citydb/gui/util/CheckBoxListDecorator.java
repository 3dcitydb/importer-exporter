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
package org.citydb.gui.util;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

public class CheckBoxListDecorator<T> extends MouseAdapter implements ListSelectionListener, ActionListener, PropertyChangeListener {
	private final JList<T> list;
	private final ListSelectionModel checkBoxSelectionModel;
	private final Map<Integer, Boolean> enabled;
	private final Insets checkBoxMargin;
	private final int checkBoxWidth;

	private Insets margin;
	private int iconTextGap;

	public CheckBoxListDecorator(JList<T> list) {
		this.list = list;

		margin = UIManager.getInsets("List.cellMargins");
		checkBoxMargin = UIManager.getInsets("CheckBox.margin");
		checkBoxWidth = UIManager.getIcon("CheckBox.icon").getIconWidth();
		iconTextGap = UIManager.getInt("CheckBox.iconTextGap");

		list.setCellRenderer(new CheckBoxListCellRenderer<T>());
		list.addMouseListener(this);
		list.addPropertyChangeListener("enabled", this);
		list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);

		checkBoxSelectionModel = new DefaultListSelectionModel();
		checkBoxSelectionModel.addListSelectionListener(this);
		enabled = new HashMap<>();
	}

	public Insets getMargin() {
		return margin;
	}

	public void setMargin(Insets margin) {
		this.margin = margin;
	}

	public int getIconTextGap() {
		return iconTextGap;
	}

	public void setIconTextGap(int iconTextGap) {
		this.iconTextGap = iconTextGap;
	}

	public boolean isCheckBoxSelected(int index) {
		return checkBoxSelectionModel.isSelectedIndex(index);
	}

	public void setCheckBoxSelected(int index, boolean selected) {
		if (index >= 0) {
			if (!selected) {
				checkBoxSelectionModel.removeSelectionInterval(index, index);
			} else {
				checkBoxSelectionModel.addSelectionInterval(index, index);
			}
		}
	}

	public void deselectAllCheckBoxes() {
		for (int index = 0; index < list.getModel().getSize(); index++) {
			setCheckBoxSelected(index, false);
		}
	}

	public void selectAllCheckBoxes() {
		for (int index = 0; index < list.getModel().getSize(); index++) {
			setCheckBoxSelected(index, true);
		}
	}

	private void setCheckBoxSelected(int index) {
		if (index >= 0) {
			if (checkBoxSelectionModel.isSelectedIndex(index)) {
				checkBoxSelectionModel.removeSelectionInterval(index, index);
			} else {
				checkBoxSelectionModel.addSelectionInterval(index, index);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int index = list.locationToIndex(e.getPoint());
		if (index < 0) {
			return;
		}

		int left = list.getCellBounds(index, index).x + margin.left;
		if ((e.getX() < left || e.getX() > left + checkBoxWidth) && e.getClickCount() != 2) {
			return;
		}

		if (!enabled.get(index)) {
			return;
		}

		setCheckBoxSelected(index);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		list.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (enabled.get(list.getSelectedIndex())) {
			setCheckBoxSelected(list.getSelectedIndex());
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		list.repaint(list.getCellBounds(e.getFirstIndex(), e.getLastIndex()));
	}

	private final class CheckBoxListCellRenderer<E extends T> extends JPanel implements ListCellRenderer<E> {
		private final ListCellRenderer<? super T> renderer;
		private final JCheckBox checkBox;

		public CheckBoxListCellRenderer() {
			setLayout(new GridBagLayout());
			renderer = list.getCellRenderer();
			checkBox = new JCheckBox();
			checkBox.setOpaque(false);
			checkBox.setMargin(new Insets(checkBoxMargin.top, 0, checkBoxMargin.bottom, 0));
		}

		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
			Component component = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (component instanceof JComponent) {
				((JComponent) component).setBorder(BorderFactory.createEmptyBorder());
			}

			add(checkBox, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, margin.top, margin.left, margin.bottom, iconTextGap));
			add(component, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, margin.top, 0, margin.bottom, margin.right));

			boolean enable = component.isEnabled() && list.isEnabled();
			checkBox.setSelected(checkBoxSelectionModel.isSelectedIndex(index));
			checkBox.setEnabled(enable);
			enabled.put(index, enable);

			if (isSelected) {
				setBackground(list.getSelectionBackground());
			} else {
				setBackground(list.getBackground());
			}

			return this;
		}
	}
}
