package de.tub.citydb.gui.components;

import java.awt.Color;
import java.awt.Dimension;
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

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.statistic.TextureImageCounterEvent;
import de.tub.citydb.event.statistic.TopLevelFeatureCounterEvent;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

public class ImpExpStatusDialog extends JDialog implements EventListener {
	private JLabel impExpLabel;
	private JLabel featureLabel;
	private JLabel textureLabel;
	private JLabel details;
	private JPanel main;
	private JPanel row;
	private JLabel featureCounterLabel;
	private JLabel textureCounterLabel;
	private JProgressBar progressBar;
	public JButton abbrechenButton;
	private long featureCounter;
	private long textureCounter;

	String featureLabelText;
	String textureLabelText;

	public ImpExpStatusDialog(JFrame frame, 
			String impExpTitle, 
			String impExpMessage, 
			EventDispatcher eventDispatcher) {
		super(frame, impExpTitle, true);

		eventDispatcher.addListener(EventType.TopLevelFeatureCounter, this);
		eventDispatcher.addListener(EventType.TextureImageCounter, this);
		initGUI(impExpTitle, impExpMessage);
	}

	private void initGUI(String impExpTitle, String impExpMessage) {
		featureLabelText = ImpExpGui.labels.getString("main.status.dialog.featureCounter");
		textureLabelText = ImpExpGui.labels.getString("main.status.dialog.textureCounter");

		//gui-elemente anlegen
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		impExpLabel = new JLabel(impExpMessage);
		abbrechenButton = new JButton(ImpExpGui.labels.getString("common.button.cancel"));
		featureLabel = new JLabel(featureLabelText);
		textureLabel = new JLabel(textureLabelText);
		featureCounterLabel = new JLabel("0", SwingConstants.TRAILING);
		textureCounterLabel = new JLabel("0", SwingConstants.TRAILING);
		progressBar = new JProgressBar();

		//gui-elemente mit funktionalität ausstatten
		progressBar.setIndeterminate(true);

		setLayout(new GridBagLayout()); 
		{			
			main = new JPanel();
			add(main, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			main.setLayout(new GridBagLayout());
			{

				main.add(impExpLabel, GuiUtil.setConstraints(0,0,0.0,0.5,GridBagConstraints.HORIZONTAL,5,5,5,5));
				main.add(progressBar, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));

				details = new JLabel("Details");
				main.add(details, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

				row = new JPanel();
				row.setBackground(new Color(255, 255, 255));
				row.setBorder(BorderFactory.createEtchedBorder());
				main.add(row, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row.setLayout(new GridBagLayout());
				{
					row.add(featureLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,5,5,1,5));
					row.add(featureCounterLabel, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,1,5));
					row.add(textureLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
					row.add(textureCounterLabel, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.HORIZONTAL,1,5,5,5));
				}
			}

			add(abbrechenButton, GuiUtil.setConstraints(0,1,0.0,0.5,GridBagConstraints.NONE,5,5,5,5));
		}

		setMinimumSize(new Dimension(200, 100));
		setResizable(false);
	}

	public JButton getAbbrechenButton() {
		return abbrechenButton;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JLabel getHeaderLabel() {
		return impExpLabel;
	}

	@Override
	public void handleEvent(Event e) throws Exception {

		if (e.getEventType() == EventType.TopLevelFeatureCounter) {
			featureCounter += ((TopLevelFeatureCounterEvent)e).getCounter();
			featureCounterLabel.setText(String.valueOf(featureCounter));
		}

		else if (e.getEventType() == EventType.TextureImageCounter) {
			textureCounter += ((TextureImageCounterEvent)e).getCounter();
			textureCounterLabel.setText(String.valueOf(textureCounter));
		}
	}
}
