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
package de.tub.citydb.gui.components;

import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.SwingUtilities;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.common.event.EventType;
import de.tub.citydb.modules.common.event.StatusDialogMessage;
import de.tub.citydb.modules.common.event.StatusDialogProgressBar;
import de.tub.citydb.modules.common.event.StatusDialogTitle;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ImportStatusDialog extends JDialog implements EventHandler {
	private final EventDispatcher eventDispatcher;

	private JLabel fileName;
	private JLabel mesageLabel;
	private JLabel details;
	private JLabel fileCounter;
	private JLabel featureLabel;
	private JLabel textureLabel;
	private JPanel main;
	private JPanel row;
	private JLabel featureCounterLabel;
	private JLabel textureCounterLabel;
	private JLabel fileCounterLabel;
	private JProgressBar progressBar;
	public JButton cancelButton;
	private long featureCounter;
	private long textureCounter;
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
		cancelButton = new JButton(Internal.I18N.getString("common.button.cancel"));
		featureLabel = new JLabel(Internal.I18N.getString("common.status.dialog.featureCounter"));
		textureLabel = new JLabel(Internal.I18N.getString("common.status.dialog.textureCounter"));
		fileCounter = new JLabel(Internal.I18N.getString("common.status.dialog.fileCounter"));
		fileCounterLabel = new JLabel("n/a", SwingConstants.TRAILING);
		featureCounterLabel = new JLabel("0", SwingConstants.TRAILING);
		textureCounterLabel = new JLabel("0", SwingConstants.TRAILING);

		featureCounterLabel.setPreferredSize(new Dimension(100, featureLabel.getPreferredSize().height));
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
					row.add(textureLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
					row.add(textureCounterLabel, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
					row.add(fileCounter, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
					row.add(fileCounterLabel, GuiUtil.setConstraints(1,2,1.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
				}
			}

			add(cancelButton, GuiUtil.setConstraints(0,1,0.0,0.5,GridBagConstraints.NONE,5,5,5,5));
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
				((CounterEvent)e).getType() == CounterType.TEXTURE_IMAGE) {
			textureCounter += ((CounterEvent)e).getCounter();
			textureCounterLabel.setText(String.valueOf(textureCounter));
		}

		else if (e.getEventType() == EventType.INTERRUPT) {
			acceptStatusUpdate = false;
			mesageLabel.setText(Internal.I18N.getString("common.dialog.msg.abort"));
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
