package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlType;
import java.nio.charset.StandardCharsets;

@XmlType(name="GeneralExportOptionsType", propOrder={})
public class GeneralOptions {
    private String fileEncoding;
    private OutputFormat compressedOutputFormat = OutputFormat.CITYGML;
    private ExportEnvelope envelope;

    public GeneralOptions() {
        envelope = new ExportEnvelope();
    }

    public boolean isSetFileEncoding() {
        return fileEncoding != null;
    }

    public String getFileEncoding() {
        return fileEncoding != null ? fileEncoding : StandardCharsets.UTF_8.name();
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
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
