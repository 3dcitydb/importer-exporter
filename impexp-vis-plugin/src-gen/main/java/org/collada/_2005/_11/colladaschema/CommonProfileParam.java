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
 * <p>Java-Klasse für Common_profile_param.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="Common_profile_param"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="A"/&gt;
 *     &lt;enumeration value="ANGLE"/&gt;
 *     &lt;enumeration value="B"/&gt;
 *     &lt;enumeration value="DOUBLE_SIDED"/&gt;
 *     &lt;enumeration value="G"/&gt;
 *     &lt;enumeration value="P"/&gt;
 *     &lt;enumeration value="Q"/&gt;
 *     &lt;enumeration value="R"/&gt;
 *     &lt;enumeration value="S"/&gt;
 *     &lt;enumeration value="T"/&gt;
 *     &lt;enumeration value="TIME"/&gt;
 *     &lt;enumeration value="U"/&gt;
 *     &lt;enumeration value="V"/&gt;
 *     &lt;enumeration value="W"/&gt;
 *     &lt;enumeration value="X"/&gt;
 *     &lt;enumeration value="Y"/&gt;
 *     &lt;enumeration value="Z"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "Common_profile_param")
@XmlEnum
public enum CommonProfileParam {

    A,
    ANGLE,
    B,
    DOUBLE_SIDED,
    G,
    P,
    Q,
    R,
    S,
    T,
    TIME,
    U,
    V,
    W,
    X,
    Y,
    Z;

    public String value() {
        return name();
    }

    public static CommonProfileParam fromValue(String v) {
        return valueOf(v);
    }

}
