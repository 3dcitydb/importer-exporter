//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package org.collada._2005._11.colladaschema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für gl_blend_type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gl_blend_type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ZERO"/&gt;
 *     &lt;enumeration value="ONE"/&gt;
 *     &lt;enumeration value="SRC_COLOR"/&gt;
 *     &lt;enumeration value="ONE_MINUS_SRC_COLOR"/&gt;
 *     &lt;enumeration value="DEST_COLOR"/&gt;
 *     &lt;enumeration value="ONE_MINUS_DEST_COLOR"/&gt;
 *     &lt;enumeration value="SRC_ALPHA"/&gt;
 *     &lt;enumeration value="ONE_MINUS_SRC_ALPHA"/&gt;
 *     &lt;enumeration value="DST_ALPHA"/&gt;
 *     &lt;enumeration value="ONE_MINUS_DST_ALPHA"/&gt;
 *     &lt;enumeration value="CONSTANT_COLOR"/&gt;
 *     &lt;enumeration value="ONE_MINUS_CONSTANT_COLOR"/&gt;
 *     &lt;enumeration value="CONSTANT_ALPHA"/&gt;
 *     &lt;enumeration value="ONE_MINUS_CONSTANT_ALPHA"/&gt;
 *     &lt;enumeration value="SRC_ALPHA_SATURATE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gl_blend_type")
@XmlEnum
public enum GlBlendType {

    ZERO,
    ONE,
    SRC_COLOR,
    ONE_MINUS_SRC_COLOR,
    DEST_COLOR,
    ONE_MINUS_DEST_COLOR,
    SRC_ALPHA,
    ONE_MINUS_SRC_ALPHA,
    DST_ALPHA,
    ONE_MINUS_DST_ALPHA,
    CONSTANT_COLOR,
    ONE_MINUS_CONSTANT_COLOR,
    CONSTANT_ALPHA,
    ONE_MINUS_CONSTANT_ALPHA,
    SRC_ALPHA_SATURATE;

    public String value() {
        return name();
    }

    public static GlBlendType fromValue(String v) {
        return valueOf(v);
    }

}
