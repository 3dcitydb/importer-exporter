//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package org.collada._2005._11.colladaschema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für gles_texcombiner_operatorRGB_enums.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gles_texcombiner_operatorRGB_enums"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="REPLACE"/&gt;
 *     &lt;enumeration value="MODULATE"/&gt;
 *     &lt;enumeration value="ADD"/&gt;
 *     &lt;enumeration value="ADD_SIGNED"/&gt;
 *     &lt;enumeration value="INTERPOLATE"/&gt;
 *     &lt;enumeration value="SUBTRACT"/&gt;
 *     &lt;enumeration value="DOT3_RGB"/&gt;
 *     &lt;enumeration value="DOT3_RGBA"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gles_texcombiner_operatorRGB_enums")
@XmlEnum
public enum GlesTexcombinerOperatorRGBEnums {

    REPLACE("REPLACE"),
    MODULATE("MODULATE"),
    ADD("ADD"),
    ADD_SIGNED("ADD_SIGNED"),
    INTERPOLATE("INTERPOLATE"),
    SUBTRACT("SUBTRACT"),
    @XmlEnumValue("DOT3_RGB")
    DOT_3_RGB("DOT3_RGB"),
    @XmlEnumValue("DOT3_RGBA")
    DOT_3_RGBA("DOT3_RGBA");
    private final String value;

    GlesTexcombinerOperatorRGBEnums(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static GlesTexcombinerOperatorRGBEnums fromValue(String v) {
        for (GlesTexcombinerOperatorRGBEnums c: GlesTexcombinerOperatorRGBEnums.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
