package de.tub.citydb.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class StatusDialog extends JDialog {
	private JLabel headerLabel;
	private JProgressBar progressBar;
	private JLabel details;
	private JPanel main;
	private JPanel row;
	private JButton button;

	public StatusDialog(JFrame frame, String title, String header, String[] msg, boolean setButton) {
		super(frame, title, true);
		initGUI(title, header, msg, setButton);
	}

	private void initGUI(String impExpTitle, String impExpMessage, String[] msg, boolean setButton) {
		//gui-elemente anlegen
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		headerLabel = new JLabel(impExpMessage);
		button = new JButton("Abbrechen");		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);

		setLayout(new GridBagLayout()); {
			main = new JPanel();
			add(main, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			main.setLayout(new GridBagLayout());
			{
				main.add(headerLabel, GuiUtil.setConstraints(0,0,0.0,0.5,GridBagConstraints.HORIZONTAL,5,5,5,5));
				main.add(progressBar, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));

				if (msg != null) {
					details = new JLabel("Details");
					main.add(details, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

					row = new JPanel();
					row.setBackground(new Color(255, 255, 255));
					row.setBorder(BorderFactory.createEtchedBorder());
					main.add(row, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					row.setLayout(new GridBagLayout());
					{				
						for (int i = 0; i < msg.length; ++i) {
							JLabel text = new JLabel(msg[i]);
							text.setBackground(row.getBackground());
							row.add(text, GuiUtil.setConstraints(0,i,1.0,0.0,GridBagConstraints.HORIZONTAL,i == 0 ? 5 : 2,5,i == msg.length - 1 ? 5 : 0,5));
						}
					}
				}
			}

			if (setButton)
				add(button, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));

		}

		setResizable(false);
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public void setHeader(String header) {
		headerLabel.setText(header);
	}

	public JButton getButton() {
		return button;
	}
}
