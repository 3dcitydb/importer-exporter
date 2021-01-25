package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="GeneralExportOptionsType", propOrder={})
public class GeneralOptions {
    private OutputFormat compressedOutputFormat = OutputFormat.CITYGML;
    private ExportEnvelope envelope;

    public GeneralOptions() {
        envelope = new ExportEnvelope();
    }

    public OutputFormat getCompressedOutputFormat() {
        return compressedOutputFormat != null ? compressedOutputFormat : OutputFormat.CITYGML;
    }

    public void setCompressedOutputFormat(OutputFormat compressedOutputFormat) {
        this.compressedOutputFormat = compressedOutputFormat;
    }

    public ExportEnvelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(ExportEnvelope envelope) {
        if (envelope != null) {
            this.envelope = envelope;
        }
    }
}
