package org.citydb.config.project.query.filter.tiling;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.exporter.TilingOptions;
import org.citydb.config.project.kmlExporter.KmlTilingOptions;

@XmlType(name="AbstractTilingOptionsType")
@XmlSeeAlso({
	TilingOptions.class,
	KmlTilingOptions.class
})
public abstract class AbstractTilingOptions {

}
