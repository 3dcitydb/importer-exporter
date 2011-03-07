package de.tub.citydb.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.concurrent.InterruptEnum;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.event.statistic.CounterEvent;
import de.tub.citydb.event.statistic.CounterType;
import de.tub.citydb.event.statistic.StatusDialogMessage;
import de.tub.citydb.event.statistic.StatusDialogProgressBar;
import de.tub.citydb.event.statistic.StatusDialogTitle;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class ImportStatusDialog extends JDialog implements EventListener {
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
			String impExpMessage,
			EventDispatcher eventDispatcher) {
		super(frame, impExpTitle, true);

		eventDispatcher.addListener(EventType.Counter, this);
		eventDispatcher.addListener(EventType.StatusDialogProgressBar, this);
		eventDispatcher.addListener(EventType.StatusDialogMessage, this);
		eventDispatcher.addListener(EventType.StatusDialogTitle, this);
		eventDispatcher.addListener(EventType.Interrupt, this);

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
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	@Override
	public void handleEvent(Event e) throws Exception {

		if (e.getEventType() == EventType.Counter &&
				((CounterEvent)e).getType() == CounterType.TOPLEVEL_FEATURE) {
			featureCounter += ((CounterEvent)e).getCounter();
			featureCounterLabel.setText(String.valueOf(featureCounter));
		}

		else if (e.getEventType() == EventType.Counter &&
				((CounterEvent)e).getType() == CounterType.TEXTURE_IMAGE) {
			textureCounter += ((CounterEvent)e).getCounter();
			textureCounterLabel.setText(String.valueOf(textureCounter));
		}

		else if (e.getEventType() == EventType.Interrupt) {
			if (((InterruptEvent)e).getInterruptType() != InterruptEnum.OUT_OF_RANGE) {
				acceptStatusUpdate = false;
				mesageLabel.setText(Internal.I18N.getString("common.dialog.msg.abort"));
				progressBar.setIndeterminate(true);
			}
		}

		else if (e.getEventType() == EventType.StatusDialogProgressBar && acceptStatusUpdate) {		
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

		else if (e.getEventType() == EventType.StatusDialogMessage && acceptStatusUpdate) {
			mesageLabel.setText(((StatusDialogMessage)e).getMessage());
		}

		else if (e.getEventType() == EventType.StatusDialogTitle && acceptStatusUpdate) {
			fileName.setText(((StatusDialogTitle)e).getTitle());
		}

		else if (e.getEventType() == EventType.Counter &&
				((CounterEvent)e).getType() == CounterType.FILE) {
			fileCounterLabel.setText(String.valueOf(((CounterEvent)e).getCounter()));
		}
	}
}
