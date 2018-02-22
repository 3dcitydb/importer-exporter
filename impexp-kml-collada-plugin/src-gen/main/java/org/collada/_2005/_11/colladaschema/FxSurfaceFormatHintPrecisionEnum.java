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
 * <p>Java-Klasse für fx_surface_format_hint_precision_enum.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="fx_surface_format_hint_precision_enum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="LOW"/>
 *     &lt;enumeration value="MID"/>
 *     &lt;enumeration value="HIGH"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "fx_surface_format_hint_precision_enum")
@XmlEnum
public enum FxSurfaceFormatHintPrecisionEnum {


    /**
     * For integers this typically represents 8 bits.  For floats typically 16 bits.
     * 
     */
    LOW,

    /**
     * For integers this typically represents 8 to 24 bits.  For floats typically 16 to 32 bits.
     * 
     */
    MID,

    /**
     * For integers this typically represents 16 to 32 bits.  For floats typically 24 to 32 bits.
     * 
     */
    HIGH;

    public String value() {
        return name();
    }

    public static FxSurfaceFormatHintPrecisionEnum fromValue(String v) {
        return valueOf(v);
    }

}
