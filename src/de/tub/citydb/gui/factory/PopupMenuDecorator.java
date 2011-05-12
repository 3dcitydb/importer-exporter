package de.tub.citydb.gui.factory;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.common.ApplicationEvent;
import de.tub.citydb.api.gui.StandardEditingPopupMenuDecorator;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.internal.Internal;

@SuppressWarnings("serial")
public class PopupMenuDecorator implements StandardEditingPopupMenuDecorator {
	private static PopupMenuDecorator instance;
	private static HashMap<Class<? extends Component>, StandardEditingPopupMenu> popupMenus = new HashMap<Class<? extends Component>, StandardEditingPopupMenu>();

	private PopupMenuDecorator() {
		// just to thwart instantiation
	}

	public static synchronized PopupMenuDecorator getInstance() {
		if (instance == null)
			instance = new PopupMenuDecorator();
		
		return instance;
	}
	
	public void decorate(final JComponent... components) {
		for (final JComponent component : components) {
			final StandardEditingPopupMenu popupMenu = getStandardEditingPopupMenu(component);

			component.addMouseListener(new MouseAdapter() {
				private void processMouseEvent(MouseEvent e) {
					if (e.isPopupTrigger()) {
						if (!e.getComponent().isEnabled())
							return;

						if (e.getComponent() instanceof JTextComponent &&
								!((JTextComponent)e.getComponent()).isEditable()) {
							popupMenu.cut.setEnabled(false);
							popupMenu.paste.setEnabled(false);
						}

						popupMenu.show(e.getComponent(), e.getX(), e.getY());
						popupMenu.setInvoker(e.getComponent());
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					processMouseEvent(e);
				}

				@Override
				public void mousePressed(MouseEvent e) {
					processMouseEvent(e);
				}

			});
		}
	}
	
	@Override
	public void decorate(JTextComponent... components) {
		decorate((JComponent[])components);
	}

	private StandardEditingPopupMenu getStandardEditingPopupMenu(JComponent component) {
		StandardEditingPopupMenu popupMenu = popupMenus.get(component.getClass());

		if (popupMenu == null) {
			popupMenu = new StandardEditingPopupMenu();
			popupMenu.init(component);
			popupMenus.put(component.getClass(), popupMenu);
		}

		popupMenu.doTranslation();
		return popupMenu;
	}

	private static class StandardEditingPopupMenu extends JPopupMenu implements EventHandler {
		private JMenuItem cut;
		private JMenuItem copy;
		private JMenuItem paste;
		private JMenuItem selectAll;

		private StandardEditingPopupMenu() {
			ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(ApplicationEvent.SWITCH_LOCALE, this);
		}

		private void init(Component component) {
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
			} else if (component instanceof JList)
				selectAll.setAction(new ListSelectAllAction());		

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

		public void doTranslation() {
			cut.setText(Internal.I18N.getString("common.popup.textfield.cut"));		
			copy.setText(Internal.I18N.getString("common.popup.textfield.copy"));		
			paste.setText(Internal.I18N.getString("common.popup.textfield.paste"));		
			selectAll.setText(Internal.I18N.getString("common.popup.textfield.selectAll"));		
		}

		private final class TextSelectAllAction extends AbstractAction {
			public void actionPerformed(ActionEvent e) {
				final Component c = getInvoker();
				if (c instanceof JTextComponent) {
					JTextComponent text = (JTextComponent)c;
					text.requestFocus();
					text.setText(text.getText());
					text.selectAll();
				}
			}
		}

		private final class ListSelectAllAction extends AbstractAction {
			public void actionPerformed(ActionEvent e) {
				final Component c = getInvoker();
				if (c instanceof JList) {
					JList list = (JList)c;
					int end = list.getModel().getSize() - 1;
					if (end >= 0)
						list.setSelectionInterval(0, end);
				}
			}
		}

		private final class TransferActionListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				final Component c = getInvoker();
				if (c instanceof JComponent) {
					JComponent invoker = (JComponent)c;				
					String action = (String)e.getActionCommand();
					Action a = invoker.getActionMap().get(action);
					if (a != null)
						a.actionPerformed(new ActionEvent(invoker, ActionEvent.ACTION_PERFORMED, null));
				}
			}
		}

		@Override
		public void handleEvent(Event event) throws Exception {
			doTranslation();
		}

	}

}
