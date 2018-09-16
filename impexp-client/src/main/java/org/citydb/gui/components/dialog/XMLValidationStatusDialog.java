/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
package org.citydb.gui.components.dialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.citydb.config.i18n.Language;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.event.global.EventType;
import org.citydb.event.global.StatusDialogMessage;
import org.citydb.event.global.StatusDialogTitle;
import org.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class XMLValidationStatusDialog extends JDialog implements EventHandler {
	private EventDispatcher eventDispatcher;
	
	private JLabel titleLabel;
	private JLabel messageLabel;
	private JProgressBar progressBar;
	private JLabel detailsLabel;
	private JLabel fileCounter;
	private JLabel fileCounterLabel;
	private JPanel main;
	private JPanel row;
	private JButton button;
	
	private volatile boolean acceptStatusUpdate = true;

	public XMLValidationStatusDialog(JFrame frame, 
			String windowTitle, 
			String statusTitle,
			String statusMessage,
			String statusDetails, 
			boolean setButton,
			EventDispatcher eventDispatcher) {
		super(frame, windowTitle, true);
		
		this.eventDispatcher = eventDispatcher;
		eventDispatcher.addEventHandler(EventType.COUNTER, this);
		eventDispatcher.addEventHandler(EventType.STATUS_DIALOG_MESSAGE, this);
		eventDispatcher.addEventHandler(EventType.STATUS_DIALOG_TITLE, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
		
		initGUI(windowTitle, statusTitle, statusMessage, statusDetails, setButton);
	}
	
	public XMLValidationStatusDialog(JFrame frame, 
			String windowTitle, 
			String statusTitle,
			String statusMessage,
			String statusDetails, 
			boolean setButton) {
		super(frame, windowTitle, true);
		
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
		fileCounter = new JLabel(Language.I18N.getString("common.status.dialog.fileCounter"));
		fileCounterLabel = new JLabel("n/a", SwingConstants.TRAILING);
		button = new JButton(Language.I18N.getString("common.button.cancel"));		
		progressBar = new JProgressBar();

		setLayout(new GridBagLayout()); {
			main = new JPanel();
			add(main, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			main.setLayout(new GridBagLayout());
			{
				main.add(titleLabel, GuiUtil.setConstraints(0,0,0.0,0.5,GridBagConstraints.HORIZONTAL,5,5,5,5));
				main.add(messageLabel, GuiUtil.setConstraints(0,1,0.0,0.5,GridBagConstraints.HORIZONTAL,5,5,0,5));
				main.add(progressBar, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));

				if (details != null) {
					detailsLabel = new JLabel("Details");
					main.add(detailsLabel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

					row = new JPanel();
					row.setBackground(new Color(255, 255, 255));
					row.setBorder(BorderFactory.createEtchedBorder());
					main.add(row, GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					row.setLayout(new GridBagLayout());
					{				
						int i = 0;
						for ( ; i < details.length; ++i) {
							JLabel detail = new JLabel(details[i]);
							detail.setBackground(row.getBackground());
							GridBagConstraints c = GuiUtil.setConstraints(0,i,1.0,0.0,GridBagConstraints.HORIZONTAL,i == 0 ? 5 : 2,5,i == details.length - 1 ? 5 : 0,5);
							c.gridwidth = 2;
							row.add(detail, c);
						}
						
						row.add(fileCounter, GuiUtil.setConstraints(0,i+1,1.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
						row.add(fileCounterLabel, GuiUtil.setConstraints(1,i+1,1.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
					}
				}
			}

			if (setButton)
				add(button, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));

			pack();
			progressBar.setIndeterminate(true);
			
			addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					if (eventDispatcher != null)
						eventDispatcher.removeEventHandler(XMLValidationStatusDialog.this);
				}
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

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.INTERRUPT) {
			acceptStatusUpdate = false;
			messageLabel.setText(Language.I18N.getString("common.dialog.msg.abort"));
			progressBar.setIndeterminate(true);
		}

		else if (e.getEventType() == EventType.STATUS_DIALOG_MESSAGE && acceptStatusUpdate) {
			messageLabel.setText(((StatusDialogMessage)e).getMessage());
		}

		else if (e.getEventType() == EventType.STATUS_DIALOG_TITLE && acceptStatusUpdate) {
			titleLabel.setText(((StatusDialogTitle)e).getTitle());
		}
		
		else if (e.getEventType() == EventType.COUNTER &&
				((CounterEvent)e).getType() == CounterType.FILE) {
			fileCounterLabel.setText(String.valueOf(((CounterEvent)e).getCounter()));
		}
	}
	
}
