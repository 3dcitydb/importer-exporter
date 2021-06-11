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
 * <p>Java-Klasse für gl_logic_op_type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gl_logic_op_type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="CLEAR"/&gt;
 *     &lt;enumeration value="AND"/&gt;
 *     &lt;enumeration value="AND_REVERSE"/&gt;
 *     &lt;enumeration value="COPY"/&gt;
 *     &lt;enumeration value="AND_INVERTED"/&gt;
 *     &lt;enumeration value="NOOP"/&gt;
 *     &lt;enumeration value="XOR"/&gt;
 *     &lt;enumeration value="OR"/&gt;
 *     &lt;enumeration value="NOR"/&gt;
 *     &lt;enumeration value="EQUIV"/&gt;
 *     &lt;enumeration value="INVERT"/&gt;
 *     &lt;enumeration value="OR_REVERSE"/&gt;
 *     &lt;enumeration value="COPY_INVERTED"/&gt;
 *     &lt;enumeration value="NAND"/&gt;
 *     &lt;enumeration value="SET"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
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
