package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlType;
import java.nio.charset.StandardCharsets;

@XmlType(name="GeneralImportOptionsType", propOrder={})
public class GeneralOptions {
    private String fileEncoding;

    public boolean isSetFileEncoding() {
        return fileEncoding != null;
    }

    public String getFileEncoding() {
        return fileEncoding != null ? fileEncoding : StandardCharsets.UTF_8.name();
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }
}
