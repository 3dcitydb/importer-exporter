package de.tub.citydb.config.project;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.exporter.Exporter;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.config.project.global.Global;
import de.tub.citydb.config.project.importer.Importer;
import de.tub.citydb.config.project.matching.Matching;

@XmlRootElement
@XmlType(name="ProjectType", propOrder={
		"database",
		"importer",
		"exporter",
		"kmlExporter",
		"matching",
		"global"
		})
public class Project {
	@XmlElement(required=true)
	private Database database;
	@XmlElement(name="import", required=true)
	private Importer importer;
	@XmlElement(name="export", required=true)
	private Exporter exporter;
	@XmlElement(name="kmlExport", required=true)
	private KmlExporter kmlExporter;
	@XmlElement(required=true)
	private Matching matching;
	private Global global;
	
	public Project() {
		database = new Database();
		importer = new Importer();
		exporter = new Exporter();
		kmlExporter = new KmlExporter();
		matching = new Matching();
		global = new Global();
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		if (database != null)
			this.database = database;
	}

	public Importer getImporter() {
		return importer;
	}

	public void setImporter(Importer importer) {
		if (importer != null)
			this.importer = importer;
	}

	public Exporter getExporter() {
		return exporter;
	}

	public void setExporter(Exporter exporter) {
		if (exporter != null)
			this.exporter = exporter;
	}
	
	public Matching getMatching() {
		return matching;
	}

	public void setMatching(Matching matching) {
		if (matching != null)
			this.matching = matching;
	}

	public Global getGlobal() {
		return global;
	}

	public void setGlobal(Global global) {
		if (global != null)
			this.global = global;
	}

	public void setKmlExporter(KmlExporter kmlExporter) {
		if (kmlExporter != null)
			this.kmlExporter = kmlExporter;
	}

	public KmlExporter getKmlExporter() {
		return kmlExporter;
	}
}
