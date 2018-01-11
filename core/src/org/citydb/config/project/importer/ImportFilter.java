package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilterMode;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;

@XmlType(name="SimpleImportFilterType", propOrder={
		"featureTypeFilter",
		"filter",
		"counterFilter"
})
public class ImportFilter {
	@XmlAttribute
	private boolean useTypeNames;
	@XmlAttribute
	private SimpleSelectionFilterMode mode = SimpleSelectionFilterMode.COMPLEX;
	@XmlAttribute
	private boolean useCountFilter;
	@XmlAttribute
	private boolean useGmlNameFilter;
	@XmlAttribute
	private boolean useBboxFilter;
	
	@XmlElement(name = "typeNames")
	private FeatureTypeFilter featureTypeFilter;
	@XmlElement(name="simpleFilter")
	private SimpleSelectionFilter filter;
	@XmlElement(name = "count")
	private CounterFilter counterFilter;

	public ImportFilter() {
		featureTypeFilter = new FeatureTypeFilter();
		filter = new SimpleSelectionFilter();
		counterFilter = new CounterFilter();
	}
	
	public boolean isUseTypeNames() {
		return useTypeNames;
	}

	public void setUseTypeNames(boolean useTypeNames) {
		this.useTypeNames = useTypeNames;
	}
	
	public SimpleSelectionFilterMode getMode() {
		return mode;
	}

	public void setMode(SimpleSelectionFilterMode mode) {
		this.mode = mode;
	}

	public boolean isUseCountFilter() {
		return useCountFilter;
	}

	public void setUseCountFilter(boolean useCountFilter) {
		this.useCountFilter = useCountFilter;
	}

	public boolean isUseGmlNameFilter() {
		return useGmlNameFilter;
	}

	public void setUseGmlNameFilter(boolean useGmlNameFilter) {
		this.useGmlNameFilter = useGmlNameFilter;
	}

	public boolean isUseBboxFilter() {
		return useBboxFilter;
	}

	public void setUseBboxFilter(boolean useBboxFilter) {
		this.useBboxFilter = useBboxFilter;
	}
	
	public FeatureTypeFilter getFeatureTypeFilter() {
		return featureTypeFilter;
	}
	
	public boolean isSetFeatureTypeFilter() {
		return featureTypeFilter != null;
	}
	
	public void setFeatureTypeFilter(FeatureTypeFilter featureTypeFilter) {
		this.featureTypeFilter = featureTypeFilter;
	}

	public SimpleSelectionFilter getFilter() {
		return filter;
	}
	
	public boolean isSetFilter() {
		return filter != null;
	}

	public void setFilter(SimpleSelectionFilter filter) {
		this.filter = filter;
	}
	
	public CounterFilter getCounterFilter() {
		return counterFilter;
	}
	
	public boolean isSetCounterFilter() {
		return counterFilter != null;
	}

	public void setCounterFilter(CounterFilter counterFilter) {
		this.counterFilter = counterFilter;
	}
	
}
