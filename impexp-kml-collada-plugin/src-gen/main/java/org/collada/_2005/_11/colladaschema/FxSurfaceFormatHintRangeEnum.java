//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für fx_surface_format_hint_range_enum.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="fx_surface_format_hint_range_enum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SNORM"/>
 *     &lt;enumeration value="UNORM"/>
 *     &lt;enumeration value="SINT"/>
 *     &lt;enumeration value="UINT"/>
 *     &lt;enumeration value="FLOAT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "fx_surface_format_hint_range_enum")
@XmlEnum
public enum FxSurfaceFormatHintRangeEnum {


    /**
     * Format is representing a decimal value that remains within the -1 to 1 range. Implimentation could be integer-fixedpoint or floats.
     * 
     */
    SNORM,

    /**
     * Format is representing a decimal value that remains within the 0 to 1 range. Implimentation could be integer-fixedpoint or floats.
     * 
     */
    UNORM,

    /**
     * Format is representing signed integer numbers.  (ex. 8bits = -128 to 127)
     * 
     */
    SINT,

    /**
     * Format is representing unsigned integer numbers.  (ex. 8bits = 0 to 255)
     * 
     */
    UINT,

    /**
     * Format should support full floating point ranges.  High precision is expected to be 32bit. Mid precision may be 16 to 32 bit.  Low precision is expected to be 16 bit.
     * 
     */
    FLOAT;

    public String value() {
        return name();
    }

    public static FxSurfaceFormatHintRangeEnum fromValue(String v) {
        return valueOf(v);
    }

}
