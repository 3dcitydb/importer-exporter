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
package org.citydb.gui.components.dialog;

import org.citydb.config.i18n.Language;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.event.global.EventType;
import org.citydb.event.global.ProgressBarEventType;
import org.citydb.event.global.StatusDialogMessage;
import org.citydb.event.global.StatusDialogProgressBar;
import org.citydb.event.global.StatusDialogTitle;
import org.citydb.gui.util.GuiUtil;
import org.citydb.registry.ObjectRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class ImportStatusDialog extends JDialog implements EventHandler {
	private final EventDispatcher eventDispatcher;

	private JLabel fileName;
	private JLabel mesageLabel;
	private JLabel details;
	private JLabel fileCounter;
	private JLabel featureLabel;
	private JLabel appearanceLabel;
	private JLabel textureLabel;
	private JPanel main;
	private JPanel row;
	private JLabel featureCounterLabel;
	private JLabel appearanceCounterLabel;
	private JLabel textureCounterLabel;
	private JLabel fileCounterLabel;
	private JProgressBar progressBar;
	public JButton cancelButton;
	
	private long featureCounter;
	private long appearanceCounter;
	private long textureCounter;
	private int progressBarCounter;
	private volatile boolean acceptStatusUpdate = true;

	public ImportStatusDialog(JFrame frame, 
			String impExpTitle,
			String impExpMessage) {
		super(frame, impExpTitle, true);

		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(EventType.COUNTER, this);
		eventDispatcher.addEventHandler(EventType.STATUS_DIALOG_PROGRESS_BAR, this);
		eventDispatcher.addEventHandler(EventType.STATUS_DIALOG_MESSAGE, this);
		eventDispatcher.addEventHandler(EventType.STATUS_DIALOG_TITLE, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		initGUI(impExpTitle, impExpMessage);
	}

	private void initGUI(String impExpTitle, String impExpMessage) {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		fileName = new JLabel(impExpMessage);
		fileName.setFont(fileName.getFont().deriveFont(Font.BOLD));
		mesageLabel = new JLabel(" ");
		cancelButton = new JButton(Language.I18N.getString("common.button.cancel"));
		featureLabel = new JLabel(Language.I18N.getString("common.status.dialog.featureCounter"));
		appearanceLabel = new JLabel(Language.I18N.getString("common.status.dialog.appearanceCounter"));
		textureLabel = new JLabel(Language.I18N.getString("common.status.dialog.textureCounter"));
		fileCounter = new JLabel(Language.I18N.getString("common.status.dialog.fileCounter"));
		fileCounterLabel = new JLabel("n/a", SwingConstants.TRAILING);
		featureCounterLabel = new JLabel("0", SwingConstants.TRAILING);
		appearanceCounterLabel = new JLabel("0", SwingConstants.TRAILING);
		textureCounterLabel = new JLabel("0", SwingConstants.TRAILING);

		featureCounterLabel.setPreferredSize(new Dimension(100, featureLabel.getPreferredSize().height));
		appearanceCounterLabel.setPreferredSize(new Dimension(100, appearanceLabel.getPreferredSize().height));
		textureCounterLabel.setPreferredSize(new Dimension(100, textureLabel.getPreferredSize().height));

		progressBar = new JProgressBar();

		setLayout(new GridBagLayout()); 
		{			
			main = new JPanel();
			add(main, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			main.setLayout(new GridBagLayout());
			{
				main.add(fileName, GuiUtil.setConstraints(0,0,0.0,0,GridBagConstraints.HORIZONTAL,5,5,5,5));
				main.add(mesageLabel, GuiUtil.setConstraints(0,1,0.0,0,GridBagConstraints.HORIZONTAL,5,5,0,5));
				main.add(progressBar, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));

				details = new JLabel("Details");
				main.add(details, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

				row = new JPanel();
				row.setBackground(new Color(255, 255, 255));
				row.setBorder(BorderFactory.createEtchedBorder());
				main.add(row, GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row.setLayout(new GridBagLayout());
				{
					row.add(featureLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,5,5,1,5));
					row.add(featureCounterLabel, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,1,5));
					row.add(appearanceLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,1,5,1,5));
					row.add(appearanceCounterLabel, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.HORIZONTAL,1,5,1,5));
					row.add(textureLabel, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
					row.add(textureCounterLabel, GuiUtil.setConstraints(1,2,1.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
					row.add(fileCounter, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
					row.add(fileCounterLabel, GuiUtil.setConstraints(1,3,1.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
				}
			}

			add(cancelButton, GuiUtil.setConstraints(0,1,0.0,0.5,GridBagConstraints.NONE,5,5,10,5));
		}

		pack();
		progressBar.setIndeterminate(true);

		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				eventDispatcher.removeEventHandler(ImportStatusDialog.this);
			}
		});
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	@Override
	public void handleEvent(Event e) throws Exception {

		if (e.getEventType() == EventType.COUNTER &&
				((CounterEvent)e).getType() == CounterType.TOPLEVEL_FEATURE) {
			featureCounter += ((CounterEvent)e).getCounter();
			featureCounterLabel.setText(String.valueOf(featureCounter));
		}

		else if (e.getEventType() == EventType.COUNTER &&
				((CounterEvent)e).getType() == CounterType.GLOBAL_APPEARANCE) {
			appearanceCounter += ((CounterEvent)e).getCounter();
			appearanceCounterLabel.setText(String.valueOf(appearanceCounter));
		}

		else if (e.getEventType() == EventType.COUNTER &&
				((CounterEvent)e).getType() == CounterType.TEXTURE_IMAGE) {
			textureCounter += ((CounterEvent)e).getCounter();
			textureCounterLabel.setText(String.valueOf(textureCounter));
		}

		else if (e.getEventType() == EventType.INTERRUPT) {
			acceptStatusUpdate = false;
			mesageLabel.setText(Language.I18N.getString("common.dialog.msg.abort"));
			progressBar.setIndeterminate(true);
		}

		else if (e.getEventType() == EventType.STATUS_DIALOG_PROGRESS_BAR && acceptStatusUpdate) {
			StatusDialogProgressBar progressBarEvent = (StatusDialogProgressBar)e;
			
			if (progressBarEvent.getType() == ProgressBarEventType.INIT) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {		
						progressBar.setIndeterminate(progressBarEvent.isSetIntermediate());
					}
				});
				
				if (!progressBarEvent.isSetIntermediate()) {
					progressBar.setMaximum(progressBarEvent.getValue());
					progressBar.setValue(0);
					progressBarCounter = 0;
				}
			} else {
				progressBarCounter += progressBarEvent.getValue();
				progressBar.setValue(progressBarCounter);
			}
		}

		else if (e.getEventType() == EventType.STATUS_DIALOG_MESSAGE && acceptStatusUpdate) {
			mesageLabel.setText(((StatusDialogMessage)e).getMessage());
		}

		else if (e.getEventType() == EventType.STATUS_DIALOG_TITLE && acceptStatusUpdate) {
			fileName.setText(((StatusDialogTitle)e).getTitle());
		}

		else if (e.getEventType() == EventType.COUNTER &&
				((CounterEvent)e).getType() == CounterType.FILE) {
			fileCounterLabel.setText(String.valueOf(((CounterEvent)e).getCounter()));
		}
	}
}
