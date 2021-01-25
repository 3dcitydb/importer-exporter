package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="GeneralExportOptionsType", propOrder={
        "compressionFormat"
})
public class GeneralOptions {
    private OutputFormat compressionFormat = OutputFormat.CITYGML;

    public OutputFormat getCompressionFormat() {
        return compressionFormat != null ? compressionFormat : OutputFormat.CITYGML;
    }

    public void setCompressionFormat(OutputFormat compressionFormat) {
        this.compressionFormat = compressionFormat;
    }
}
