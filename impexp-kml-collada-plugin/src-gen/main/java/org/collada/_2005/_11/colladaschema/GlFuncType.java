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
 * <p>Java-Klasse für gl_func_type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gl_func_type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NEVER"/>
 *     &lt;enumeration value="LESS"/>
 *     &lt;enumeration value="LEQUAL"/>
 *     &lt;enumeration value="EQUAL"/>
 *     &lt;enumeration value="GREATER"/>
 *     &lt;enumeration value="NOTEQUAL"/>
 *     &lt;enumeration value="GEQUAL"/>
 *     &lt;enumeration value="ALWAYS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "gl_func_type")
@XmlEnum
public enum GlFuncType {

    NEVER,
    LESS,
    LEQUAL,
    EQUAL,
    GREATER,
    NOTEQUAL,
    GEQUAL,
    ALWAYS;

    public String value() {
        return name();
    }

    public static GlFuncType fromValue(String v) {
        return valueOf(v);
    }

}
