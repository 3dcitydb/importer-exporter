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
 * <p>Java-Klasse für gl_stencil_op_type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gl_stencil_op_type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="KEEP"/&gt;
 *     &lt;enumeration value="ZERO"/&gt;
 *     &lt;enumeration value="REPLACE"/&gt;
 *     &lt;enumeration value="INCR"/&gt;
 *     &lt;enumeration value="DECR"/&gt;
 *     &lt;enumeration value="INVERT"/&gt;
 *     &lt;enumeration value="INCR_WRAP"/&gt;
 *     &lt;enumeration value="DECR_WRAP"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gl_stencil_op_type")
@XmlEnum
public enum GlStencilOpType {

    KEEP,
    ZERO,
    REPLACE,
    INCR,
    DECR,
    INVERT,
    INCR_WRAP,
    DECR_WRAP;

    public String value() {
        return name();
    }

    public static GlStencilOpType fromValue(String v) {
        return valueOf(v);
    }

}
