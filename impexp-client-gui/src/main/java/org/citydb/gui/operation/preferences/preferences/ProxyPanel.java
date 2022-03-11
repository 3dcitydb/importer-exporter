/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.operation.preferences.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.Proxies;
import org.citydb.config.project.global.ProxyConfig;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.InternalProxySelector;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.util.DefaultPreferencesComponent;
import org.citydb.gui.util.CheckBoxListDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citydb.util.event.global.ProxyServerUnavailableEvent;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProxyPanel extends DefaultPreferencesComponent implements EventHandler {
	private final Logger log = Logger.getInstance();
	private JList<ProxyConfig> proxyList;
	private CheckBoxListDecorator<ProxyConfig> listDecorator;
	private DisabledListCellRenderer renderer;
	private List<ProxyConfig> proxies;

	private ProxyConfig currentProxy;
	private ProxyConfig previousSingleProxy;
	private JCheckBox useSingleProxy;

	private TitledPanel proxyProtocolPanel;
	private TitledPanel proxySettingsPanel;
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
	private JPanel proxyListPanel;

	public ProxyPanel(Config config) {
		super(config);
		initGui();

		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.PROXY_SERVER_UNAVAILABLE, this);
	}

	@Override
	public boolean isModified() {
		try { proxyPortText.commitEdit(); } catch (ParseException e) { }

		if (requiresAuthenticationBox.isSelected() != currentProxy.requiresAuthentication()) return true;
		if (!proxyHostText.getText().trim().equals(currentProxy.getHost())) return true;
		if (((Number)proxyPortText.getValue()).intValue() != currentProxy.getPort()) return true;
		if (!proxyUserText.getText().trim().equals(currentProxy.getUsername())) return true;
		if (passwordCheck.isSelected() != currentProxy.isSavePassword()) return true;		
		if (!String.valueOf(proxyPasswordText.getPassword()).equals(currentProxy.getPassword())) return true;
		if (previousSingleProxy != config.getGlobalConfig().getProxies().getSingleProxy()) return true;

		disableInvalidProxies();
		if (listDecorator.isCheckBoxSelected(proxies.indexOf(currentProxy)) != currentProxy.isEnabled()) return true;

		return false;
	}

	private void initGui() {
		proxyList = new JList<>();
		proxyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		renderer = new DisabledListCellRenderer();
		proxyList.setCellRenderer(renderer);
		proxyList.setModel(new DefaultListModel<>());
		listDecorator = new CheckBoxListDecorator<>(proxyList);

		proxies = new ArrayList<>(config.getGlobalConfig().getProxies().getProxyList().size());

		useSingleProxy = new JCheckBox();
		requiresAuthenticationBox = new JCheckBox();
		proxyHostLabel = new JLabel();
		proxyPortLabel = new JLabel();
		proxyUserLabel = new JLabel();
		proxyPasswordLabel = new JLabel();

		NumberFormatter portFormat = new NumberFormatter(new DecimalFormat("#"));
		portFormat.setMaximum(65535);
		portFormat.setMinimum(0);
		proxyPortText = new JFormattedTextField(portFormat);

		proxyHostText = new JTextField();
		proxyUserText = new JTextField();
		proxyPasswordText = new JPasswordField();
		passwordCheck = new JCheckBox();

		setLayout(new GridBagLayout());

		{
			JPanel content = new JPanel();

			proxyListPanel = new JPanel();
			proxyListPanel.setLayout(new BorderLayout());
			proxyListPanel.add(proxyList);

			content.setLayout(new GridBagLayout());
			{
				content.add(proxyListPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
				content.add(useSingleProxy, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
			}

			proxyProtocolPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(proxyHostLabel, GuiUtil.setConstraints(0, 0, 0, 1, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(proxyHostText, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 5, 0));
				content.add(proxyPortLabel, GuiUtil.setConstraints(0, 1, 0, 1, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(proxyPortText, GuiUtil.setConstraints(1, 1, 1, 1, GridBagConstraints.BOTH, 0, 5, 5, 0));
				content.add(requiresAuthenticationBox, GuiUtil.setConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(proxyUserLabel, GuiUtil.setConstraints(0, 3, 0, 1, GridBagConstraints.BOTH, 5, 0, 5, 5));
				content.add(proxyUserText, GuiUtil.setConstraints(1, 3, 1, 1, GridBagConstraints.BOTH, 5, 5, 5, 0));
				content.add(proxyPasswordLabel, GuiUtil.setConstraints(0, 4, 0, 1, GridBagConstraints.BOTH, 0, 0, 5, 5));
				content.add(proxyPasswordText, GuiUtil.setConstraints(1, 4, 1, 1, GridBagConstraints.BOTH, 0, 5, 5, 0));
				content.add(passwordCheck, GuiUtil.setConstraints(1, 5, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			proxySettingsPanel = new TitledPanel().build(content);
		}

		add(proxyProtocolPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(proxySettingsPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		PopupMenuDecorator.getInstance().decorate(proxyHostText, proxyPortText, proxyUserText, proxyPasswordText);

		proxyList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				if (useSingleProxy.isSelected() && proxyList.getSelectedIndex() != renderer.singleIndex) {
					proxyList.setSelectedIndex(renderer.singleIndex);
					return;
				}

				ProxyConfig selectedProxy = proxyList.getSelectedValue();
				if (selectedProxy != null) {
					proxySettingsPanel.setTitle(selectedProxy.toString());
					if (currentProxy != null && currentProxy != selectedProxy) {
						setProxySettings(currentProxy);
						loadProxySettings(selectedProxy);
						setEnabledUserSettings();
					}

					currentProxy = selectedProxy;
				}
			}
		});

		requiresAuthenticationBox.addActionListener(e -> setEnabledUserSettings());

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

		useSingleProxy.addItemListener(e -> {
			renderer.enable = e.getStateChange() != ItemEvent.SELECTED;
			Proxies proxyConfig = config.getGlobalConfig().getProxies();

			if (!renderer.enable) {
				renderer.singleIndex = proxies.indexOf(currentProxy);
				proxyConfig.setSingleProxy(currentProxy.getType());
			} else {
				proxyConfig.unsetSingleProxy();
			}

			proxyList.repaint();
		});

		UIManager.addPropertyChangeListener(e -> {
			if ("lookAndFeel".equals(e.getPropertyName())) {
				SwingUtilities.invokeLater(this::updateComponentUI);
			}
		});

		updateComponentUI();
	}

	private void updateComponentUI() {
		proxyListPanel.setBorder(UIManager.getBorder("ScrollPane.border"));
	}

	private void initProxyList() {
		proxies.clear();
		proxies.addAll(config.getGlobalConfig().getProxies().getProxyList());

		DefaultListModel<ProxyConfig> model = (DefaultListModel<ProxyConfig>)proxyList.getModel();
		model.clear();
		model.setSize(proxies.size());

		for (int index = proxies.size() - 1; index >= 0; --index) {
			ProxyConfig proxy = proxies.get(index);
			model.set(index, proxy);
			loadProxySettings(proxy);
		}

		proxyList.setSelectedIndex(0);
	}

	public void disableInvalidProxies() {
		for (int index = 0; index < proxies.size(); ++index) {
			ProxyConfig proxy = proxies.get(index);
			if (listDecorator.isCheckBoxSelected(index) && !proxy.hasValidProxySettings()) {
				proxy.setEnabled(false);
				listDecorator.setCheckBoxSelected(index, false);
			} 
		}
	}

	private void setEnabledUserSettings() {
		boolean enabled = requiresAuthenticationBox.isSelected();
		proxyUserLabel.setEnabled(enabled);
		proxyPasswordLabel.setEnabled(enabled);
		passwordCheck.setEnabled(enabled);
		proxyUserText.setEnabled(enabled);
		proxyPasswordText.setEnabled(enabled);
	}

	@Override
	public void switchLocale(Locale locale) {
		ProxyConfig proxy = proxyList.getSelectedValue();
		proxySettingsPanel.setTitle(proxy.toString());
		proxyProtocolPanel.setTitle(Language.I18N.getString("pref.proxy.label.configure"));
		useSingleProxy.setText(Language.I18N.getString("pref.proxy.label.singleProxy"));
		requiresAuthenticationBox.setText(Language.I18N.getString("pref.proxy.label.auth"));
		proxyHostLabel.setText(Language.I18N.getString("common.label.server"));
		proxyPortLabel.setText(Language.I18N.getString("common.label.port"));
		proxyUserLabel.setText(Language.I18N.getString("common.label.username"));
		proxyPasswordLabel.setText(Language.I18N.getString("common.label.password"));
		passwordCheck.setText(Language.I18N.getString("common.label.passwordCheck"));
	}

	public void loadProxySettings(ProxyConfig proxy) {
		requiresAuthenticationBox.setSelected(proxy.requiresAuthentication());
		proxyHostText.setText(proxy.getHost());
		proxyPortText.setValue(proxy.getPort());
		proxyUserText.setText(proxy.getUsername());
		passwordCheck.setSelected(proxy.isSavePassword());		
		listDecorator.setCheckBoxSelected(proxies.indexOf(proxy), proxy.isEnabled());
		proxyPasswordText.setText(proxy.getPassword());
	}

	public void setProxySettings(ProxyConfig proxy) {
		proxy.setRequiresAuthentication(requiresAuthenticationBox.isSelected());
		proxy.setHost(proxyHostText.getText().trim());
		proxy.setPort(((Number)proxyPortText.getValue()).intValue());
		proxy.setUsername(proxyUserText.getText().trim());
		proxy.setPassword(String.valueOf(proxyPasswordText.getPassword()));
		proxy.setSavePassword(passwordCheck.isSelected());
		proxy.setEnabled(listDecorator.isCheckBoxSelected(proxies.indexOf(proxy)));
		proxy.resetFailedConnectAttempts();
	}

	@Override
	public void loadSettings() {
		if (currentProxy == null) {
			initProxyList();

			// single proxy settings
			previousSingleProxy = config.getGlobalConfig().getProxies().getSingleProxy();
			renderer.singleIndex = previousSingleProxy != null ? proxies.indexOf(previousSingleProxy) : 0;
			proxyList.setSelectedIndex(renderer.singleIndex);
			useSingleProxy.setSelected(previousSingleProxy != null);
		} else
			loadProxySettings(currentProxy);

		setEnabledUserSettings();
		InternalProxySelector.getInstance().setDefaultAuthentication();
	}

	@Override
	public void setSettings() {		
		setProxySettings(currentProxy);
		disableInvalidProxies();
		previousSingleProxy = config.getGlobalConfig().getProxies().getSingleProxy();
		InternalProxySelector.getInstance().setDefaultAuthentication();
	}

	@Override
	public void resetSettings() {
		config.getGlobalConfig().getProxies().reset();
		useSingleProxy.setSelected(false);
		previousSingleProxy = null;

		initProxyList();
	}

	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("pref.tree.general.proxies");
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		ProxyConfig proxy = ((ProxyServerUnavailableEvent)event).getProxy();
		listDecorator.setCheckBoxSelected(proxies.indexOf(proxy), false);
		log.error("Disabling " + proxy.getType().toString() + " proxy server settings in preferences.");
	}

	private static final class DisabledListCellRenderer extends DefaultListCellRenderer {
		private boolean enable = true;
		private int singleIndex = 0;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			boolean select = isSelected;
			boolean focus = cellHasFocus;
			if (!enable)
				select = focus = index == singleIndex;

			super.getListCellRendererComponent(list, value, index, select, focus); 
			this.setEnabled(index == singleIndex || enable);
			return this; 
		} 
	}

}

