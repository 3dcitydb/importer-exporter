package de.tub.citydb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.system.SysGmlIdLookupServerConfig;
import de.tub.citydb.config.project.system.SysThreadPoolConfig;
import de.tub.citydb.gui.components.DigitsOnlyDocument;

public class ExpResourcesPanel extends PrefPanelBase{

	//Variablendefinition
	private JPanel block1;
	private JPanel block2;
	private JLabel expResMinThreadsLabel;
	private JTextField expResMinThreadsText;
	private JLabel expResMaxThreadsLabel;
	private JTextField expResMaxThreadsText;	
	private JLabel expResGeomLabel;
	private JTextField expResGeomCacheText;
	private JLabel expResGeomCacheLabel;	
	private JTextField expResGeomDrainText;
	private JLabel expResGeomDrainLabel;
	private JLabel expResGeomPartLabel;
	private JTextField expResGeomPartText;
	private JLabel expResFeatLabel;
	private JTextField expResFeatCacheText;
	private JLabel expResFeatCacheLabel;
	private JTextField expResFeatDrainText;
	private JLabel expResFeatDrainLabel;
	private JLabel expResFeatPartLabel;
	private JTextField expResFeatPartText;


	//Konstruktor
	public ExpResourcesPanel(Config inpConfig) {
		super(inpConfig);
		initGui();
	}

	public boolean isModified() {
		if (super.isModified()) return true;

		SysThreadPoolConfig threadPool = config.getProject().getExporter().getSystem().getThreadPool().getDefaultPool();
		SysGmlIdLookupServerConfig geometry = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getGeometry();
		SysGmlIdLookupServerConfig feature = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getFeature();

		try {
			if (!Integer.valueOf(expResMinThreadsText.getText()).equals(threadPool.getMinThreads())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(expResMaxThreadsText.getText()).equals(threadPool.getMaxThreads())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(expResGeomCacheText.getText()).equals(geometry.getCacheSize())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Float.valueOf(expResGeomDrainText.getText()).equals(geometry.getPageFactor() * 100)) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}
		
		try {
			if (!Integer.valueOf(expResGeomPartText.getText()).equals(geometry.getPartitions())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(expResFeatCacheText.getText()).equals(feature.getCacheSize())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Float.valueOf(expResFeatDrainText.getText()).equals(feature.getPageFactor() * 100)) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}
		
		try {
			if (!Integer.valueOf(expResFeatPartText.getText()).equals(feature.getPartitions())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		return false;

	}


	//InitGui-Methode
	public void initGui(){
		//Variablendeklaration
		block1 = new JPanel();
		block2 = new JPanel();
		expResMinThreadsLabel = new JLabel("");
		expResMinThreadsText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		expResMaxThreadsLabel = new JLabel("");
		expResMaxThreadsText = new JTextField(new DigitsOnlyDocument(3), "" , 0);	
		expResGeomLabel = new JLabel("");
		expResGeomCacheText = new JTextField(new DigitsOnlyDocument(), "" , 0);
		expResGeomCacheLabel = new JLabel("");	
		expResGeomDrainText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		expResGeomDrainLabel = new JLabel("");
		expResGeomPartText = new JTextField(new DigitsOnlyDocument(3), "", 0);
		expResGeomPartLabel = new JLabel("");
		expResFeatLabel = new JLabel("");		
		expResFeatCacheText = new JTextField(new DigitsOnlyDocument(), "" , 0);
		expResFeatCacheLabel = new JLabel("");
		expResFeatDrainText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		expResFeatDrainLabel = new JLabel("");
		expResFeatPartText = new JTextField(new DigitsOnlyDocument(3), "", 0);
		expResFeatPartLabel = new JLabel("");
		
		//Layout
		setLayout(new GridBagLayout());
		add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block1.setBorder(BorderFactory.createTitledBorder(""));
		block1.setLayout(new GridBagLayout());
		{
			block1.add(expResMinThreadsLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(expResMinThreadsText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(expResMaxThreadsLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(expResMaxThreadsText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}

		add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block2.setBorder(BorderFactory.createTitledBorder(""));
		block2.setLayout(new GridBagLayout());
		{
			block2.add(expResGeomLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomCacheText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomCacheLabel, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomDrainText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomDrainLabel, GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomPartText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomPartLabel, GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatCacheText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatCacheLabel, GuiUtil.setConstraints(2,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatDrainText, GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatDrainLabel, GuiUtil.setConstraints(2,4,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatPartText, GuiUtil.setConstraints(1,5,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatPartLabel, GuiUtil.setConstraints(2,5,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
	}

	//doTranslation-Methode
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("pref.export.resources.border.multiCPU")));
		expResMinThreadsLabel.setText(ImpExpGui.labels.getString("pref.export.resources.label.minThreads"));
		expResMaxThreadsLabel.setText(ImpExpGui.labels.getString("pref.export.resources.label.maxThreads"));

		block2.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("pref.export.resources.border.idCache")));
		expResGeomLabel.setText(ImpExpGui.labels.getString("pref.export.resources.label.geometry"));
		expResGeomCacheLabel.setText(ImpExpGui.labels.getString("pref.export.resources.label.geometry.entry"));
		expResGeomDrainLabel.setText(ImpExpGui.labels.getString("pref.export.resources.label.geometry.drain"));
		expResGeomPartLabel.setText(ImpExpGui.labels.getString("pref.export.resources.label.geometry.partition"));
		expResFeatLabel.setText(ImpExpGui.labels.getString("pref.export.resources.label.feature"));
		expResFeatCacheLabel.setText(ImpExpGui.labels.getString("pref.export.resources.label.feature.entry"));
		expResFeatDrainLabel.setText(ImpExpGui.labels.getString("pref.export.resources.label.feature.drain"));
		expResFeatPartLabel.setText(ImpExpGui.labels.getString("pref.export.resources.label.feature.partition"));
	}

	//Config
	public void loadSettings() {
		SysThreadPoolConfig threadPool = config.getProject().getExporter().getSystem().getThreadPool().getDefaultPool();
		SysGmlIdLookupServerConfig geometry = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getGeometry();
		SysGmlIdLookupServerConfig feature = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getFeature();

		expResMinThreadsText.setText(String.valueOf(threadPool.getMinThreads()));
		expResMaxThreadsText.setText(String.valueOf(threadPool.getMaxThreads()));
		expResGeomCacheText.setText(String.valueOf(geometry.getCacheSize()));
		expResGeomDrainText.setText(String.valueOf((int)(geometry.getPageFactor() * 100)));
		expResGeomPartText.setText(String.valueOf((int)geometry.getPartitions()));
		expResFeatCacheText.setText(String.valueOf(feature.getCacheSize()));
		expResFeatDrainText.setText(String.valueOf((int)(feature.getPageFactor() * 100)));
		expResFeatPartText.setText(String.valueOf((int)feature.getPartitions()));
	}


	public void setSettings() {

		SysThreadPoolConfig threadPool = config.getProject().getExporter().getSystem().getThreadPool().getDefaultPool();
		SysGmlIdLookupServerConfig geometry = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getGeometry();
		SysGmlIdLookupServerConfig feature = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getFeature();

		try {
			threadPool.setMinThreads(Integer.valueOf(expResMinThreadsText.getText()));
		} catch (NumberFormatException nfe) {
			expResMinThreadsText.setText(String.valueOf(threadPool.getMinThreads()));
		}
		try {
			threadPool.setMaxThreads(Integer.valueOf(expResMaxThreadsText.getText()));
		} catch (NumberFormatException nfe) {
			expResMaxThreadsText.setText(String.valueOf(threadPool.getMaxThreads()));
		}
		try {
			geometry.setCacheSize(Integer.valueOf(expResGeomCacheText.getText()));
		} catch (NumberFormatException nfe) {
			expResGeomCacheText.setText(String.valueOf(geometry.getCacheSize()));
		}
		try {
			float pageFactor = Float.valueOf(expResGeomDrainText.getText()) / 100f;
			if (pageFactor > 1) {
				pageFactor = 1.0f;
				expResGeomDrainText.setText("100");				
			}

			geometry.setPageFactor(pageFactor);
		} catch (NumberFormatException nfe) {
			expResGeomDrainText.setText(String.valueOf((int)(geometry.getPageFactor() * 100)));
		}
		try {
			geometry.setPartitions(Integer.valueOf(expResGeomPartText.getText()));
		} catch (NumberFormatException nfe) {
			expResGeomPartText.setText(String.valueOf(geometry.getPartitions()));
		}
		try {
			feature.setCacheSize(Integer.valueOf(expResFeatCacheText.getText()));
		} catch (NumberFormatException nfe) {
			expResFeatCacheText.setText(String.valueOf(feature.getCacheSize()));
		}
		try {
			float pageFactor = Float.valueOf(expResFeatDrainText.getText()) / 100f;
			if (pageFactor > 1) {
				pageFactor = 1.0f;
				expResFeatDrainText.setText("100");				
			}

			feature.setPageFactor(pageFactor);
		} catch (NumberFormatException nfe) {
			expResFeatDrainText.setText(String.valueOf((int)(feature.getPageFactor() * 100)));
		}
		try {
			feature.setPartitions(Integer.valueOf(expResFeatPartText.getText()));
		} catch (NumberFormatException nfe) {
			expResFeatPartText.setText(String.valueOf(feature.getPartitions()));
		}
	}
}

