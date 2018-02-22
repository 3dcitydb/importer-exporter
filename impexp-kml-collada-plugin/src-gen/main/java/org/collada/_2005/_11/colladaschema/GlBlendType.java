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
 * <p>Java-Klasse für gl_blend_type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gl_blend_type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ZERO"/>
 *     &lt;enumeration value="ONE"/>
 *     &lt;enumeration value="SRC_COLOR"/>
 *     &lt;enumeration value="ONE_MINUS_SRC_COLOR"/>
 *     &lt;enumeration value="DEST_COLOR"/>
 *     &lt;enumeration value="ONE_MINUS_DEST_COLOR"/>
 *     &lt;enumeration value="SRC_ALPHA"/>
 *     &lt;enumeration value="ONE_MINUS_SRC_ALPHA"/>
 *     &lt;enumeration value="DST_ALPHA"/>
 *     &lt;enumeration value="ONE_MINUS_DST_ALPHA"/>
 *     &lt;enumeration value="CONSTANT_COLOR"/>
 *     &lt;enumeration value="ONE_MINUS_CONSTANT_COLOR"/>
 *     &lt;enumeration value="CONSTANT_ALPHA"/>
 *     &lt;enumeration value="ONE_MINUS_CONSTANT_ALPHA"/>
 *     &lt;enumeration value="SRC_ALPHA_SATURATE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
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
