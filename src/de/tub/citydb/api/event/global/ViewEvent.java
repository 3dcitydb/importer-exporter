package de.tub.citydb.api.event.global;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.plugin.extension.view.View;

public class ViewEvent extends Event {
	public enum ViewState {
		VIEW_ACTIVATED,
		VIEW_DEACTIVATED
	}
	
	private final View view;
	private final ViewState viewState;
	
	public ViewEvent(View view, ViewState viewState, Object source) {
		super(GlobalEvents.VIEW_STATE, source);
		this.view = view;
		this.viewState = viewState;
	}
	
	public View getView() {
		return view;
	}

	public ViewState getViewState() {
		return viewState;
	}
	
}
