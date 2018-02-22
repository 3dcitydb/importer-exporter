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
 * <p>Java-Klasse für gl_logic_op_type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gl_logic_op_type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CLEAR"/>
 *     &lt;enumeration value="AND"/>
 *     &lt;enumeration value="AND_REVERSE"/>
 *     &lt;enumeration value="COPY"/>
 *     &lt;enumeration value="AND_INVERTED"/>
 *     &lt;enumeration value="NOOP"/>
 *     &lt;enumeration value="XOR"/>
 *     &lt;enumeration value="OR"/>
 *     &lt;enumeration value="NOR"/>
 *     &lt;enumeration value="EQUIV"/>
 *     &lt;enumeration value="INVERT"/>
 *     &lt;enumeration value="OR_REVERSE"/>
 *     &lt;enumeration value="COPY_INVERTED"/>
 *     &lt;enumeration value="NAND"/>
 *     &lt;enumeration value="SET"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "gl_logic_op_type")
@XmlEnum
public enum GlLogicOpType {

    CLEAR,
    AND,
    AND_REVERSE,
    COPY,
    AND_INVERTED,
    NOOP,
    XOR,
    OR,
    NOR,
    EQUIV,
    INVERT,
    OR_REVERSE,
    COPY_INVERTED,
    NAND,
    SET;

    public String value() {
        return name();
    }

    public static GlLogicOpType fromValue(String v) {
        return valueOf(v);
    }

}
