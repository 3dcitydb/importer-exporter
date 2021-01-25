package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="GeneralExportOptionsType", propOrder={
        "compressedOutputFormat"
})
public class GeneralOptions {
    private OutputFormat compressedOutputFormat = OutputFormat.CITYGML;

    public OutputFormat getCompressedOutputFormat() {
        return compressedOutputFormat != null ? compressedOutputFormat : OutputFormat.CITYGML;
    }

    public void setCompressedOutputFormat(OutputFormat compressedOutputFormat) {
        this.compressedOutputFormat = compressedOutputFormat;
    }
}
