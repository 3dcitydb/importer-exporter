package de.tub.citydb.gui.preferences;

@SuppressWarnings("serial")
public class NullComponent extends AbstractPreferencesComponent {
	private static final NullComponent instance = new NullComponent();
	
	private NullComponent() {
		super(null);
	}
	
	public static NullComponent getInstance() {
		return instance;
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public void setSettings() {
		// nothing to do here
	}

	@Override
	public void loadSettings() {
		// nothing to do here
	}

	@Override
	public void doTranslation() {
		// nothing to do here
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public void resetSettings() {
		// nothing to do here
	}
	
}
