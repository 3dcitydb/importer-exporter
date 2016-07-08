/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.preferences.gui.preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.citydb.api.event.Event;
import org.citydb.api.event.EventHandler;
import org.citydb.api.event.global.GlobalEvents;
import org.citydb.api.io.ProxyConfig;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.global.Proxies;
import org.citydb.config.project.global.ProxyConfigImpl;
import org.citydb.event.ProxyServerUnavailableEventImpl;
import org.citydb.gui.factory.CheckBoxListDecorator;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.io.InternalProxySelector;
import org.citydb.log.Logger;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ProxyPanel extends AbstractPreferencesComponent implements EventHandler {
	private final Logger LOG = Logger.getInstance();
	private JList<ProxyConfigImpl> proxyList;
	private CheckBoxListDecorator<ProxyConfigImpl> listDecorator;
	private DisabledListCellRenderer renderer;
	private List<ProxyConfigImpl> proxies;

	private ProxyConfigImpl currentProxy;
	private ProxyConfigImpl previousSingleProxy;

	private JLabel configureLabel;
	private JPanel proxyListPanel;
	private JCheckBox useSingleProxy;

	private JPanel proxySettingsPanel;
	private JCheckBox requiresAuthenticationBox;
	private JLabel proxyHostLabel;
	private JTextField proxyHostText;
	private JLabel proxyPortLabel;
	private JFormattedTextField proxyPortText;	
	private JLabel proxyUserLabel;
	private JTextField proxyUserText;
	private JLabel proxyPasswordLabel;
	private JPasswordField proxyPasswordText;
	private JCheckBox passwordCheck;

	public ProxyPanel(Config config) {
		super(config);
		initGui();

		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.PROXY_SERVER_UNAVAILABLE, this);
	}

	@Override
	public boolean isModified() {
		try { proxyPortText.commitEdit(); } catch (ParseException e) { }

		if (requiresAuthenticationBox.isSelected() != currentProxy.requiresAuthentication()) return true;
		if (!proxyHostText.getText().trim().equals(currentProxy.getHost())) return true;
		if (((Number)proxyPortText.getValue()).intValue() != currentProxy.getPort()) return true;
		if (!proxyUserText.getText().trim().equals(currentProxy.getUsername())) return true;
		if (passwordCheck.isSelected() != currentProxy.isSavePassword()) return true;		
		if (!String.valueOf(proxyPasswordText.getPassword()).equals(currentProxy.getInternalPassword())) return true;
		if (previousSingleProxy != config.getProject().getGlobal().getProxies().getSingleProxy()) return true;

		disableInvalidProxies();
		if (listDecorator.isCheckBoxSelected(proxies.indexOf(currentProxy)) != currentProxy.isEnabled()) return true;

		return false;
	}

	private void initGui() {
		proxyList = new JList<ProxyConfigImpl>();
		proxyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		renderer = new DisabledListCellRenderer();
		proxyList.setCellRenderer(renderer);
		proxyList.setModel(new DefaultListModel<ProxyConfigImpl>());
		listDecorator = new CheckBoxListDecorator<ProxyConfigImpl>(proxyList);		

		proxies = new ArrayList<ProxyConfigImpl>(config.getProject().getGlobal().getProxies().getProxyList().size());

		proxyListPanel = new JPanel();
		proxySettingsPanel = new JPanel();
		useSingleProxy = new JCheckBox();
		useSingleProxy.setIconTextGap(10);
		configureLabel = new JLabel();
		requiresAuthenticationBox = new JCheckBox();
		proxyHostLabel = new JLabel();
		proxyPortLabel = new JLabel();
		proxyUserLabel = new JLabel();
		proxyPasswordLabel = new JLabel();

		DecimalFormat fiveIntFormat = new DecimalFormat("#####");	
		fiveIntFormat.setMaximumIntegerDigits(5);
		fiveIntFormat.setMinimumIntegerDigits(1);
		proxyHostText = new JTextField();
		proxyPortText = new JFormattedTextField(fiveIntFormat);
		proxyUserText = new JTextField();
		proxyPasswordText = new JPasswordField();
		passwordCheck = new JCheckBox();
		passwordCheck.setIconTextGap(10);

		setLayout(new GridBagLayout());
		add(configureLabel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,TitledBorder.LEFT,0,0));
		add(proxyListPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,TitledBorder.LEFT,5,0));
		add(useSingleProxy, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,0,0,5,0));
		add(proxySettingsPanel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));

		proxyList.setBorder(BorderFactory.createEtchedBorder());
		proxyListPanel.setLayout(new BorderLayout());
		proxyListPanel.add(proxyList);

		proxySettingsPanel.setBorder(BorderFactory.createTitledBorder(""));
		proxySettingsPanel.setLayout(new GridBagLayout());
		{
			requiresAuthenticationBox.setIconTextGap(10);
			proxySettingsPanel.add(proxyHostLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
			proxySettingsPanel.add(proxyHostText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			proxySettingsPanel.add(proxyPortLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			proxySettingsPanel.add(proxyPortText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			proxySettingsPanel.add(requiresAuthenticationBox, GuiUtil.setConstraints(0,2,2,1,1.0,1.0,GridBagConstraints.BOTH,0,0,5,0));
			proxySettingsPanel.add(proxyUserLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			proxySettingsPanel.add(proxyUserText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			proxySettingsPanel.add(proxyPasswordLabel, GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			proxySettingsPanel.add(proxyPasswordText, GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			proxySettingsPanel.add(passwordCheck, GuiUtil.setConstraints(1,5,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		}

		PopupMenuDecorator.getInstance().decorate(proxyHostText, proxyPortText, proxyUserText, proxyPasswordText);

		proxyList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (useSingleProxy.isSelected() && proxyList.getSelectedIndex() != renderer.singleIndex) {
						proxyList.setSelectedIndex(renderer.singleIndex);
						return;
					}

					ProxyConfigImpl selectedProxy = (ProxyConfigImpl)proxyList.getSelectedValue();
					if (selectedProxy != null) {
						proxySettingsPanel.setBorder(BorderFactory.createTitledBorder(selectedProxy.toString()));
						if (currentProxy != null && currentProxy != selectedProxy) {
							setProxySettings(currentProxy);
							loadProxySettings(selectedProxy);
							setEnabledUserSettings();
						}

						currentProxy = selectedProxy;
					}
				}
			}
		});

		proxyPortText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(proxyPortText, 0, 65535);
			}
		});

		requiresAuthenticationBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledUserSettings();
			}
		});

		proxyHostText.getDocument().addDocumentListener(new DocumentListener() {
			private void setEnabled(boolean enable) {
				int index = proxyList.getSelectedIndex();
				if (listDecorator.isCheckBoxSelected(index) != enable)
					listDecorator.setCheckBoxSelected(index, enable);
			}

			public void removeUpdate(DocumentEvent e) {
				setEnabled(proxyHostText.getText().length() > 0);
			}

			public void insertUpdate(DocumentEvent e) {
				setEnabled(true);
			}

			public void changedUpdate(DocumentEvent e) {
				setEnabled(proxyHostText.getText().length() > 0);
			}
		});

		useSingleProxy.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				renderer.enable = e.getStateChange() != ItemEvent.SELECTED;
				Proxies proxyConfig = config.getProject().getGlobal().getProxies();

				if (!renderer.enable) {
					renderer.singleIndex = proxies.indexOf(currentProxy);
					proxyConfig.setSingleProxy(currentProxy.getType());
					for (ProxyConfigImpl proxy : proxies)
						resetAuthenticationCache(proxy);
				} else {
					resetAuthenticationCache(currentProxy);
					proxyConfig.unsetSingleProxy();
				}

				proxyList.repaint();
			}
		});
	}

	private void initProxyList() {
		for (ProxyConfigImpl proxy : proxies)
			resetAuthenticationCache(proxy);

		proxies.clear();
		proxies.addAll(config.getProject().getGlobal().getProxies().getProxyList());

		DefaultListModel<ProxyConfigImpl> model = (DefaultListModel<ProxyConfigImpl>)proxyList.getModel();
		model.clear();
		model.setSize(proxies.size());

		for (int index = proxies.size() - 1; index >= 0; --index) {
			ProxyConfigImpl proxy = proxies.get(index);
			model.set(index, proxy);
			loadProxySettings(proxy);
		}

		proxyList.setSelectedIndex(0);
	}

	public void disableInvalidProxies() {
		for (int index = 0; index < proxies.size(); ++index) {
			ProxyConfigImpl proxy = proxies.get(index);
			if (listDecorator.isCheckBoxSelected(index) && !proxy.hasValidProxySettings()) {
				proxy.setEnabled(false);
				listDecorator.setCheckBoxSelected(index, false);
			} 
		}
	}

	private void checkNonNegativeRange(JFormattedTextField field, int min, int max) {
		if (((Number)field.getValue()).intValue() < min)
			field.setValue(min);
		else if (((Number)field.getValue()).intValue() > max)
			field.setValue(max);
	}

	private void setEnabledUserSettings() {
		boolean enabled = requiresAuthenticationBox.isSelected();
		proxyUserLabel.setEnabled(enabled);
		proxyPasswordLabel.setEnabled(enabled);
		passwordCheck.setEnabled(enabled);
		proxyUserText.setEnabled(enabled);
		proxyPasswordText.setEnabled(enabled);
	}

	private void resetAuthenticationCache(ProxyConfigImpl proxy) {
		if (proxy != null && proxy.hasValidProxySettings())
			InternalProxySelector.getInstance(config).resetAuthenticationCache(proxy);
	}

	@Override
	public void doTranslation() {
		ProxyConfigImpl proxy = (ProxyConfigImpl)proxyList.getSelectedValue();
		proxySettingsPanel.setBorder(BorderFactory.createTitledBorder(proxy.toString()));

		configureLabel.setText(Language.I18N.getString("pref.proxy.label.configure"));
		useSingleProxy.setText(Language.I18N.getString("pref.proxy.label.singleProxy"));
		requiresAuthenticationBox.setText(Language.I18N.getString("pref.proxy.label.auth"));
		proxyHostLabel.setText(Language.I18N.getString("common.label.server"));
		proxyPortLabel.setText(Language.I18N.getString("common.label.port"));
		proxyUserLabel.setText(Language.I18N.getString("common.label.username"));
		proxyPasswordLabel.setText(Language.I18N.getString("common.label.password"));
		passwordCheck.setText(Language.I18N.getString("common.label.passwordCheck"));
	}

	public void loadProxySettings(ProxyConfigImpl proxy) {
		requiresAuthenticationBox.setSelected(proxy.requiresAuthentication());
		proxyHostText.setText(proxy.getHost());
		proxyPortText.setValue(proxy.getPort());
		proxyUserText.setText(proxy.getUsername());
		passwordCheck.setSelected(proxy.isSavePassword());		
		listDecorator.setCheckBoxSelected(proxies.indexOf(proxy), proxy.isEnabled());	

		if (proxy.isSavePassword()) {
			proxyPasswordText.setText(proxy.getExternalPassword());
			proxy.setInternalPassword(proxy.getExternalPassword());
		} else
			proxyPasswordText.setText(proxy.getInternalPassword());
	}

	public void setProxySettings(ProxyConfigImpl proxy) {
		resetAuthenticationCache(proxy);

		proxy.setRequiresAuthentication(requiresAuthenticationBox.isSelected());
		proxy.setHost(proxyHostText.getText().trim());
		proxy.setPort(((Number)proxyPortText.getValue()).intValue());
		proxy.setUsername(proxyUserText.getText().trim());
		proxy.setInternalPassword(String.valueOf(proxyPasswordText.getPassword()));
		proxy.setSavePassword(passwordCheck.isSelected());
		proxy.setEnabled(listDecorator.isCheckBoxSelected(proxies.indexOf(proxy)));
		proxy.resetFailedConnectAttempts();

		if (passwordCheck.isSelected()) 
			proxy.setExternalPassword(String.valueOf(proxyPasswordText.getPassword()));
		else
			proxy.setExternalPassword("");
	}

	@Override
	public void loadSettings() {
		if (currentProxy == null) {
			initProxyList();

			// single proxy settings
			previousSingleProxy = config.getProject().getGlobal().getProxies().getSingleProxy();
			renderer.singleIndex = previousSingleProxy != null ? proxies.indexOf(previousSingleProxy) : 0;
			proxyList.setSelectedIndex(renderer.singleIndex);
			useSingleProxy.setSelected(previousSingleProxy != null);
		} else
			loadProxySettings(currentProxy);
		
		setEnabledUserSettings();
	}

	@Override
	public void setSettings() {		
		setProxySettings(currentProxy);
		disableInvalidProxies();
		previousSingleProxy = config.getProject().getGlobal().getProxies().getSingleProxy();
	}

	@Override
	public void resetSettings() {
		config.getProject().getGlobal().getProxies().reset();
		useSingleProxy.setSelected(false);
		previousSingleProxy = null;

		initProxyList();
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.general.proxies");
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		ProxyConfig proxy = ((ProxyServerUnavailableEventImpl)event).getProxy();
		listDecorator.setCheckBoxSelected(proxies.indexOf(proxy), false);
		LOG.error("Disabling " + proxy.getType().toString() + " proxy server settings in preferences.");
	}

	private final class DisabledListCellRenderer extends DefaultListCellRenderer { 
		private boolean enable = true;
		private int singleIndex = 0;
		
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			boolean select = isSelected;
			boolean focus = cellHasFocus;
			if (!enable)
				select = focus = index == singleIndex ? true : false;

			super.getListCellRendererComponent(list, value, index, select, focus); 
			this.setEnabled(index == singleIndex ? true : enable);
			return this; 
		} 
	}

}

