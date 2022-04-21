/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2022
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

package org.citydb.config.project.exporter;

import org.citydb.config.geometry.BoundingBox;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.util.IllegalFormatException;
import java.util.Locale;

public class TileTokenValue {
    public static final String ROW_TOKEN = "%ROW%";
    public static final String COLUMN_TOKEN = "%COLUMN%";
    public static final String X_MIN_TOKEN = "%X_MIN%";
    public static final String Y_MIN_TOKEN = "%Y_MIN%";
    public static final String X_MAX_TOKEN = "%X_MAX%";
    public static final String Y_MAX_TOKEN = "%Y_MAX%";
    public static final String DEFAULT_TOKEN_FORMAT = "%s";

    @XmlAttribute(name = "row")
    private String rowFormat;
    @XmlAttribute(name = "column")
    private String columnFormat;
    @XmlAttribute(name = "xmin")
    private String xminFormat;
    @XmlAttribute(name = "ymin")
    private String yminFormat;
    @XmlAttribute(name = "xmax")
    private String xmaxFormat;
    @XmlAttribute(name = "ymax")
    private String ymaxFormat;
    @XmlValue
    private String value;

    public TileTokenValue() {
    }

    public TileTokenValue(String value) {
        this.value = value;
    }

    public String getRowFormat() {
        return rowFormat != null ? rowFormat : DEFAULT_TOKEN_FORMAT;
    }

    public void setRowFormat(String rowFormat) {
        this.rowFormat = checkFormat(rowFormat);
    }

    public String getColumnFormat() {
        return columnFormat != null ? columnFormat : DEFAULT_TOKEN_FORMAT;
    }

    public void setColumnFormat(String columnFormat) {
        this.columnFormat = checkFormat(columnFormat);
    }

    public String getXminFormat() {
        return xminFormat != null ? xminFormat : DEFAULT_TOKEN_FORMAT;
    }

    public void setXminFormat(String xminFormat) {
        this.xminFormat = checkFormat(xminFormat);
    }

    public String getYminFormat() {
        return yminFormat != null ? yminFormat : DEFAULT_TOKEN_FORMAT;
    }

    public void setYminFormat(String yminFormat) {
        this.yminFormat = checkFormat(yminFormat);
    }

    public String getXmaxFormat() {
        return xmaxFormat != null ? xmaxFormat : DEFAULT_TOKEN_FORMAT;
    }

    public void setXmaxFormat(String xmaxFormat) {
        this.xmaxFormat = checkFormat(xmaxFormat);
    }

    public String getYmaxFormat() {
        return ymaxFormat != null ? ymaxFormat : DEFAULT_TOKEN_FORMAT;
    }

    public void setYmaxFormat(String ymaxFormat) {
        this.ymaxFormat = checkFormat(ymaxFormat);
    }

    public boolean isSetValue() {
        return value != null && !value.isEmpty();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String checkFormat(String format) {
        return format == null || format.trim().isEmpty() || DEFAULT_TOKEN_FORMAT.equals(format) ? null : format;
    }

    public String formatAndResolveTokens(int row, int column, double xmin, double ymin, double xmax, double ymax) {
        return value.replaceAll(ROW_TOKEN, getFormattedString(getRowFormat(), row))
                .replaceAll(COLUMN_TOKEN, getFormattedString(getColumnFormat(), column))
                .replaceAll(X_MIN_TOKEN, getFormattedString(getXminFormat(), xmin))
                .replaceAll(Y_MIN_TOKEN, getFormattedString(getYminFormat(), ymin))
                .replaceAll(X_MAX_TOKEN, getFormattedString(getXmaxFormat(), xmax))
                .replaceAll(Y_MAX_TOKEN, getFormattedString(getYmaxFormat(), ymax));
    }

    public String formatAndResolveTokens(int row, int column, BoundingBox extent) {
        return formatAndResolveTokens(row, column,
                extent.getLowerCorner().getX(),
                extent.getLowerCorner().getY(),
                extent.getUpperCorner().getX(),
                extent.getUpperCorner().getY());
    }

    private String getFormattedString(String format, Object value) {
        try {
            return String.format(Locale.ENGLISH, format, value);
        } catch (IllegalFormatException e) {
            return String.valueOf(value);
        }
    }
}
