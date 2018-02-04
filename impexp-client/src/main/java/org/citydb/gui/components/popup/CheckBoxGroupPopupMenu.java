package org.citydb.gui.components.popup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;

import org.citydb.config.i18n.Language;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.registry.ObjectRegistry;

@SuppressWarnings("serial")
public class CheckBoxGroupPopupMenu extends AbstractStandardPopupMenu implements EventHandler {
	private JMenuItem selectOthers;
	private JMenuItem deselectOthers;
	private JMenuItem selectAll;
	private JMenuItem deselectAll;
	private JMenuItem invert;
	
	public CheckBoxGroupPopupMenu() {
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
	}
	
	public void init(final int index, final JCheckBox... group) {
		selectOthers = new JMenuItem();
		deselectOthers = new JMenuItem();
		selectAll = new JMenuItem();
		deselectAll = new JMenuItem();
		invert = new JMenuItem();

		selectOthers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < group.length; i++) {
					if (i == index)
						continue;
					
					group[i].setSelected(true);
				}
			}
		});
		
		deselectOthers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < group.length; i++) {
					if (i == index)
						continue;
					
					group[i].setSelected(false);
				}
			}
		});
		
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < group.length; i++)
					group[i].setSelected(true);
			}
		});
		
		deselectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < group.length; i++)
					group[i].setSelected(false);
			}
		});
		
		invert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < group.length; i++)
					group[i].setSelected(!group[i].isSelected());
			}
		});
		
		add(selectOthers);
		add(deselectOthers);
		addSeparator();		
		add(selectAll);
		add(deselectAll);
		add(invert);
	}
	
	@Override
	public void doTranslation() {
		selectOthers.setText(Language.I18N.getString("export.popup.lod.selectOthers"));
		deselectOthers.setText(Language.I18N.getString("export.popup.lod.deselectOthers"));
		selectAll.setText(Language.I18N.getString("export.popup.lod.selectAll"));
		deselectAll.setText(Language.I18N.getString("export.popup.lod.deselectAll"));
		invert.setText(Language.I18N.getString("export.popup.lod.invert"));
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		doTranslation();
	}

}
