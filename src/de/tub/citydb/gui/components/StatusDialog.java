package de.tub.citydb.gui.components;

import java.awt.Color;
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
import javax.swing.SwingUtilities;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.statistic.StatusDialogMessage;
import de.tub.citydb.event.statistic.StatusDialogProgressBar;
import de.tub.citydb.event.statistic.StatusDialogTitle;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class StatusDialog extends JDialog implements EventListener {
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
			boolean setButton,
			EventDispatcher eventDispatcher) {
		super(frame, windowTitle, true);
		
		eventDispatcher.addListener(EventType.StatusDialogProgressBar, this);
		eventDispatcher.addListener(EventType.StatusDialogMessage, this);
		eventDispatcher.addListener(EventType.StatusDialogTitle, this);
		eventDispatcher.addListener(EventType.Interrupt, this);
		
		initGUI(windowTitle, statusTitle, statusMessage, statusDetails, setButton);
	}
	
	public StatusDialog(JFrame frame, 
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
		button = new JButton(Internal.I18N.getString("common.button.cancel"));		
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
						for (int i = 0; i < details.length; ++i) {
							JLabel detail = new JLabel(details[i]);
							detail.setBackground(row.getBackground());
							row.add(detail, GuiUtil.setConstraints(0,i,1.0,0.0,GridBagConstraints.HORIZONTAL,i == 0 ? 5 : 2,5,i == details.length - 1 ? 5 : 0,5));
						}
					}
				}
			}

			if (setButton)
				add(button, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));

			pack();
			progressBar.setIndeterminate(true);
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
		if (e.getEventType() == EventType.Interrupt) {
			acceptStatusUpdate = false;
			messageLabel.setText(Internal.I18N.getString("common.dialog.msg.abort"));
			progressBar.setIndeterminate(true);
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
			messageLabel.setText(((StatusDialogMessage)e).getMessage());
		}

		else if (e.getEventType() == EventType.StatusDialogTitle && acceptStatusUpdate) {
			titleLabel.setText(((StatusDialogTitle)e).getTitle());
		}
	}
	
}
