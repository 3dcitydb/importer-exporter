package de.tub.citydb.gui.panel.console;

import java.awt.Component;
import java.awt.Dialog.ModalExclusionType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.gui.window.WindowSize;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class ConsoleWindow extends JFrame {
	private final Component content;
	private final Config config;
	private final ImpExpGui topFrame;

	public ConsoleWindow(Component content, Config config, ImpExpGui topFrame) {
		this.content = content;
		this.config = config;
		this.topFrame = topFrame;

		init();
		doTranslation();
		loadSettings();
	}

	private void init() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ConsoleWindow.class.getResource("/resources/img/logo_small.png")));
		setLayout(new GridBagLayout());
	}

	public void activate() {
		if (getWidth() == 0 && getHeight() == 0) {
			// if the console window has not been opened before
			int width = content.getWidth();
			if (width == 0)
				width = topFrame.getWidth();

			setLocation(topFrame.getX() + topFrame.getWidth(), topFrame.getY());
			setSize(width, topFrame.getHeight());
		}

		add(content, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));			
		doTranslation();
		setVisible(true);
	}

	public void loadSettings() {
		WindowSize size = config.getGui().getConsoleWindow().getSize();
		if (size.getX() != null && size.getY() != null && size.getWidth() != null & size.getHeight() != null) {
			setLocation(size.getX(), size.getY());
			setSize(size.getWidth(), size.getHeight());
		}
	}

	public void setSettings() {
		if (config.getGui().getConsoleWindow().isDetached()) {
			WindowSize size = config.getGui().getConsoleWindow().getSize();
			size.setX(getX());
			size.setY(getY());
			size.setWidth(getWidth());
			size.setHeight(getHeight());
		} else
			config.getGui().getConsoleWindow().setSize(new WindowSize());
	}

	public void doTranslation() {
		setTitle(Internal.I18N.getString("main.window.title") + " - " + Internal.I18N.getString("main.label.console"));
	}

}
