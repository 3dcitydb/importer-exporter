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
 * <p>Java-Klasse für gl_blend_equation_type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gl_blend_equation_type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="FUNC_ADD"/&gt;
 *     &lt;enumeration value="FUNC_SUBTRACT"/&gt;
 *     &lt;enumeration value="FUNC_REVERSE_SUBTRACT"/&gt;
 *     &lt;enumeration value="MIN"/&gt;
 *     &lt;enumeration value="MAX"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gl_blend_equation_type")
@XmlEnum
public enum GlBlendEquationType {

    FUNC_ADD,
    FUNC_SUBTRACT,
    FUNC_REVERSE_SUBTRACT,
    MIN,
    MAX;

    public String value() {
        return name();
    }

    public static GlBlendEquationType fromValue(String v) {
        return valueOf(v);
    }

}
