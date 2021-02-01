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
package org.citydb.gui.components.popup;

import org.citydb.config.i18n.Language;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.registry.ObjectRegistry;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class EditPopupMenu extends AbstractPopupMenu implements EventHandler {
	private JMenuItem cut;
	private JMenuItem copy;
	private JMenuItem paste;
	private JMenuItem selectAll;

	public EditPopupMenu() {
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
	}

	public void init(Component component) {
		cut = new JMenuItem();
		copy = new JMenuItem();		
		paste = new JMenuItem();
		selectAll = new JMenuItem();

		cut.setActionCommand((String)TransferHandler.getCutAction().getValue(Action.NAME));
		copy.setActionCommand((String)TransferHandler.getCopyAction().getValue(Action.NAME));
		paste.setActionCommand((String)TransferHandler.getPasteAction().getValue(Action.NAME));

		cut.addActionListener(new TransferActionListener());
		copy.addActionListener(new TransferActionListener());
		paste.addActionListener(new TransferActionListener());

		if (component instanceof JTextComponent) {
			selectAll.setAction(new TextSelectAllAction());
			if (component instanceof JPasswordField) {
				cut.setEnabled(false);
				copy.setEnabled(false);
			}
		} else if (component instanceof JList) {
			selectAll.setAction(new ListSelectAllAction());
		}

		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		add(cut);
		add(copy);
		add(paste);
		addSeparator();
		add(selectAll);
	}
	
	public void prepare(boolean enable) {
		cut.setEnabled(enable);
		paste.setEnabled(enable);
	}

	public void doTranslation() {
		cut.setText(Language.I18N.getString("common.popup.textfield.cut"));		
		copy.setText(Language.I18N.getString("common.popup.textfield.copy"));		
		paste.setText(Language.I18N.getString("common.popup.textfield.paste"));		
		selectAll.setText(Language.I18N.getString("common.popup.textfield.selectAll"));		
	}

	private final class TextSelectAllAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			final Component c = getInvoker();
			if (c instanceof JTextComponent) {
				JTextComponent text = (JTextComponent)c;
				text.requestFocus();
				if (c instanceof JFormattedTextField) {
					text.setText(text.getText());
				}

				text.selectAll();
			}
		}
	}

	private final class ListSelectAllAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			final Component c = getInvoker();
			if (c instanceof JList) {
				JList<?> list = (JList<?>)c;
				int end = list.getModel().getSize() - 1;
				if (end >= 0) {
					list.setSelectionInterval(0, end);
				}
			}
		}
	}

	private final class TransferActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			final Component c = getInvoker();
			if (c instanceof JComponent) {
				JComponent invoker = (JComponent)c;				
				Action a = invoker.getActionMap().get(e.getActionCommand());
				if (a != null) {
					a.actionPerformed(new ActionEvent(invoker, ActionEvent.ACTION_PERFORMED, null));
				}
			}
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		doTranslation();
	}

}
