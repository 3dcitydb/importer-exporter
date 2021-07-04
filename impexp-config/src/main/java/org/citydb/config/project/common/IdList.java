/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citydb.config.project.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.nio.charset.StandardCharsets;

@XmlType(name = "IdListType", propOrder = {})
public class IdList {
    public static final String DEFAULT_DELIMITER = ",";
    public static final char DEFAULT_QUOTE_CHARACTER = '"';
    public static final char DEFAULT_COMMENT_CHARACTER = '#';
    public static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    @XmlElement(required = true)
    private String file;
    private String idColumnName;
    @XmlElement(defaultValue = "1")
    private Integer idColumnIndex;
    @XmlElement(defaultValue = "resource")
    private IdColumnType idColumnType;
    @XmlElement(defaultValue = ",")
    private String delimiter;
    @XmlElement(defaultValue = "\"")
    private String quoteCharacter;
    private String quoteEscapeCharacter;
    private String commentCharacter;
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

    public IdColumnType getIdColumnType() {
        return idColumnType != null ? idColumnType : IdColumnType.RESOURCE_ID;
    }

    public void setIdColumnType(IdColumnType idColumnType) {
        this.idColumnType = idColumnType;
    }

    public String getDelimiter() {
        return delimiter != null && !delimiter.isEmpty() ? delimiter : DEFAULT_DELIMITER;
    }

    public void setDelimiter(Character delimiter) {
        this.delimiter = delimiter != null ? delimiter.toString() : null;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
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

    public Character getQuoteEscapeCharacter() {
        return quoteEscapeCharacter != null && !quoteEscapeCharacter.isEmpty() ? quoteEscapeCharacter.charAt(0) : DEFAULT_QUOTE_CHARACTER;
    }

    public void setQuoteEscapeCharacter(Character quoteEscapeCharacter) {
        this.quoteEscapeCharacter = quoteEscapeCharacter != null ? quoteEscapeCharacter.toString() : null;
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
