package org.citydb.config.project.deleter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.nio.charset.StandardCharsets;

@XmlType(name = "DeleteListType", propOrder = {})
public class DeleteList {
    public static final char DEFAULT_DELIMITER = ',';
    public static final char DEFAULT_QUOTE_CHARACTER = '"';
    public static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    @XmlElement(required = true)
    private String file;
    private String idColumnName;
    @XmlElement(defaultValue = "1")
    private Integer idColumnIndex;
    @XmlElement(defaultValue = "resource")
    private DeleteListIdType idType;
    @XmlElement(defaultValue = ",")
    private String delimiter;
    @XmlElement(defaultValue = "\"")
    private String quoteCharacter;
    private String commentCharacter;
    private String escapeCharacter;
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

    public String getIdColumnName() {
        return idColumnName;
    }

    public void setIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
    }

    public int getIdColumnIndex() {
        return idColumnIndex != null && idColumnIndex > 0 ? idColumnIndex : 1;
    }

    public void setIdColumnIndex(Integer idColumnIndex) {
        this.idColumnIndex = idColumnIndex;
    }

    public DeleteListIdType getIdType() {
        return idType != null ? idType : DeleteListIdType.RESOURCE_ID;
    }

    public void setIdType(DeleteListIdType idType) {
        this.idType = idType;
    }

    public Character getDelimiter() {
        return delimiter != null && !delimiter.isEmpty() ? delimiter.charAt(0) : DEFAULT_DELIMITER;
    }

    public void setDelimiter(Character delimiter) {
        this.delimiter = delimiter != null ? delimiter.toString() : null;
    }

    public Character getQuoteCharacter() {
        return quoteCharacter != null && !quoteCharacter.isEmpty() ? quoteCharacter.charAt(0) : DEFAULT_QUOTE_CHARACTER;
    }

    public void setQuoteCharacter(Character quoteCharacter) {
        this.quoteCharacter = quoteCharacter != null ? quoteCharacter.toString() : null;
    }

    public Character getCommentCharacter() {
        return commentCharacter != null && !commentCharacter.isEmpty() ? commentCharacter.charAt(0) : null;
    }

    public void setCommentCharacter(Character commentCharacter) {
        this.commentCharacter = commentCharacter != null ? commentCharacter.toString() : null;
    }

    public Character getEscapeCharacter() {
        return escapeCharacter != null && !escapeCharacter.isEmpty() ? escapeCharacter.charAt(0) : null;
    }

    public void setEscapeCharacter(Character escapeCharacter) {
        this.escapeCharacter = escapeCharacter != null ? escapeCharacter.toString() : null;
    }

    public boolean hasHeader() {
        return hasHeader != null ? hasHeader : false;
    }

    public void setHasHeader(Boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public String getEncoding() {
        return encoding != null ? encoding : DEFAULT_ENCODING;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
