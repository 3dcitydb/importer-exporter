package de.tub.citydb.gui.components;

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

import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

public class StatusDialog extends JDialog {
	private JLabel headerLabel;
	private JProgressBar progressBar;
	private JLabel detailsLabel;
	private JPanel main;
	private JPanel row;
	private JButton button;

	public StatusDialog(JFrame frame, String title, String header, String msg, boolean setButton) {
		super(frame, title, true);
		initGUI(title, header, msg, setButton);
	}

	private void initGUI(String title, String header, String msg, boolean setButton) {
		//gui-elemente anlegen
		String[] details = null;
		if (msg != null)
			details = msg.split("<br\\s*/*>");
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		headerLabel = new JLabel(header);
		button = new JButton(ImpExpGui.labels.getString("common.button.cancel"));		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);

		setLayout(new GridBagLayout()); {
			main = new JPanel();
			add(main, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			main.setLayout(new GridBagLayout());
			{
				main.add(headerLabel, GuiUtil.setConstraints(0,0,0.0,0.5,GridBagConstraints.HORIZONTAL,5,5,5,5));
				main.add(progressBar, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));

				if (details != null) {
					detailsLabel = new JLabel("Details");
					main.add(detailsLabel, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

					row = new JPanel();
					row.setBackground(new Color(255, 255, 255));
					row.setBorder(BorderFactory.createEtchedBorder());
					main.add(row, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
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

		}

		setResizable(false);
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JLabel getHeaderLabel() {
		return headerLabel;
	}

	public JButton getButton() {
		return button;
	}
}
