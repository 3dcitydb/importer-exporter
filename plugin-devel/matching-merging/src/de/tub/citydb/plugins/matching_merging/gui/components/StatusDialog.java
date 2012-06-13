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
package de.tub.citydb.plugins.matching_merging.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.plugins.matching_merging.events.EventType;
import de.tub.citydb.plugins.matching_merging.events.StatusDialogMessage;
import de.tub.citydb.plugins.matching_merging.events.StatusDialogProgressBar;
import de.tub.citydb.plugins.matching_merging.events.StatusDialogTitle;
import de.tub.citydb.plugins.matching_merging.util.Util;

@SuppressWarnings("serial")
public class StatusDialog extends JDialog implements EventHandler {
	final EventDispatcher eventDispatcher;
	
	private JLabel titleLabel;
	private JLabel messageLabel;
	private JProgressBar progressBar;
	private JLabel detailsLabel;
	private JPanel main;
	private JPanel row;
	private JButton button;
	private volatile boolean acceptStatusUpdate = true;

	public StatusDialog(JFrame frame, 
			String windowTitle, 
			String statusTitle,
			String statusMessage,
			String statusDetails, 
			boolean setButton) {
		super(frame, windowTitle, true);
		
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(EventType.STATUS_DIALOG_PROGRESS_BAR, this);
		eventDispatcher.addEventHandler(EventType.STATUS_DIALOG_MESSAGE, this);
		eventDispatcher.addEventHandler(EventType.STATUS_DIALOG_TITLE, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
		
		initGUI(windowTitle, statusTitle, statusMessage, statusDetails, setButton);
	}

	private void initGUI(String windowTitle, 
			String statusTitle, 
			String statusMessage, 
			String statusDetails, 
			boolean setButton) {		
		if (statusTitle == null)
			statusTitle = "";
		
		if (statusMessage == null)
			statusMessage = "";
		
		String[] details = null;
		if (statusDetails != null)
			details = statusDetails.split("<br\\s*/*>");
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		titleLabel = new JLabel(statusTitle);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		messageLabel = new JLabel(statusMessage);
		button = new JButton(Util.I18N.getString("common.button.cancel"));		
		progressBar = new JProgressBar();

		setLayout(new GridBagLayout()); {
			main = new JPanel();
			add(main, Util.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			main.setLayout(new GridBagLayout());
			{
				main.add(titleLabel, Util.setConstraints(0,0,0.0,0.5,GridBagConstraints.HORIZONTAL,5,5,5,5));
				main.add(messageLabel, Util.setConstraints(0,1,0.0,0.5,GridBagConstraints.HORIZONTAL,5,5,0,5));
				main.add(progressBar, Util.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));

				if (details != null) {
					detailsLabel = new JLabel("Details");
					main.add(detailsLabel, Util.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

					row = new JPanel();
					row.setBackground(new Color(255, 255, 255));
					row.setBorder(BorderFactory.createEtchedBorder());
					main.add(row, Util.setConstraints(0,4,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					row.setLayout(new GridBagLayout());
					{				
						for (int i = 0; i < details.length; ++i) {
							JLabel detail = new JLabel(details[i]);
							detail.setBackground(row.getBackground());
							row.add(detail, Util.setConstraints(0,i,1.0,0.0,GridBagConstraints.HORIZONTAL,i == 0 ? 5 : 2,5,i == details.length - 1 ? 5 : 0,5));
						}
					}
				}
			}

			if (setButton)
				add(button, Util.setConstraints(0,1,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));

			pack();
			progressBar.setIndeterminate(true);
			
			addWindowListener(new WindowListener() {
				public void windowClosed(WindowEvent e) {
					eventDispatcher.removeEventHandler(StatusDialog.this);
				}
				public void windowActivated(WindowEvent e) {}
				public void windowClosing(WindowEvent e) {}
				public void windowDeactivated(WindowEvent e) {}
				public void windowDeiconified(WindowEvent e) {}
				public void windowIconified(WindowEvent e) {}
				public void windowOpened(WindowEvent e) {}
			});
		}
	}

	public JLabel getStatusTitleLabel() {
		return titleLabel;
	}

	public JLabel getStatusMessageLabel() {
		return messageLabel;
	}
	
	public JButton getButton() {
		return button;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.INTERRUPT) {
			acceptStatusUpdate = false;
			messageLabel.setText(Util.I18N.getString("common.dialog.msg.abort"));
			progressBar.setIndeterminate(true);
		}

		else if (e.getEventType() == EventType.STATUS_DIALOG_PROGRESS_BAR && acceptStatusUpdate) {		
			if (((StatusDialogProgressBar)e).isSetIntermediate()) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {		
						if (!progressBar.isIndeterminate())
							progressBar.setIndeterminate(true);
					}
				});

				return;
			}

			if (progressBar.isIndeterminate()) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						progressBar.setIndeterminate(false);
					}
				});
			} 

			int max = ((StatusDialogProgressBar)e).getMaxValue();
			int current = ((StatusDialogProgressBar)e).getCurrentValue();

			if (max != progressBar.getMaximum())
				progressBar.setMaximum(max);
			progressBar.setValue(current);
		}

		else if (e.getEventType() == EventType.STATUS_DIALOG_MESSAGE && acceptStatusUpdate) {
			messageLabel.setText(((StatusDialogMessage)e).getMessage());
		}

		else if (e.getEventType() == EventType.STATUS_DIALOG_TITLE && acceptStatusUpdate) {
			titleLabel.setText(((StatusDialogTitle)e).getTitle());
		}
	}
	
}
