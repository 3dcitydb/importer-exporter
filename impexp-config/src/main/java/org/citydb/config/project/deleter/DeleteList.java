package org.citydb.config.project.deleter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.nio.charset.StandardCharsets;

@XmlType(name = "DeleteListType", propOrder = {})
public class DeleteList {
    @XmlElement(required = true)
    private String file;
    private String name;
    @XmlElement(defaultValue = "1")
    private Integer index;
    @XmlElement(defaultValue = "resource")
    private DeleteListIdType idType;
    @XmlElement(defaultValue = ",")
    private String delimiter;
    @XmlElement(defaultValue = "#")
    private String commentStart;
    @XmlElement(defaultValue = "\"")
    private String quoteCharacter;
    @XmlElement(name = "header", defaultValue = "false")
    private Boolean hasHeader;
    @XmlElement(defaultValue = "UTF-8")
    private String encoding;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index != null && index > 0 ? index : 1;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public DeleteListIdType getIdType() {
        return idType != null ? idType : DeleteListIdType.RESOURCE_ID;
    }

    public void setIdType(DeleteListIdType idType) {
        this.idType = idType;
    }

    public String getDelimiter() {
        return delimiter != null ? delimiter : ",";
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getCommentStart() {
        return commentStart != null ? commentStart : "#";
    }

    public void setCommentStart(String commentStart) {
        this.commentStart = commentStart;
    }

    public String getQuoteCharacter() {
        return quoteCharacter != null ? quoteCharacter : "\"";
    }

    public void setQuoteCharacter(String quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }

    public boolean hasHeader() {
        return hasHeader != null ? hasHeader : false;
    }

    public void setHasHeader(Boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public String getEncoding() {
        return encoding != null ? encoding : StandardCharsets.UTF_8.name();
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
