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
 * <p>Java-Klasse für Common_profile_param.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="Common_profile_param">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="ANGLE"/>
 *     &lt;enumeration value="B"/>
 *     &lt;enumeration value="DOUBLE_SIDED"/>
 *     &lt;enumeration value="G"/>
 *     &lt;enumeration value="P"/>
 *     &lt;enumeration value="Q"/>
 *     &lt;enumeration value="R"/>
 *     &lt;enumeration value="S"/>
 *     &lt;enumeration value="T"/>
 *     &lt;enumeration value="TIME"/>
 *     &lt;enumeration value="U"/>
 *     &lt;enumeration value="V"/>
 *     &lt;enumeration value="W"/>
 *     &lt;enumeration value="X"/>
 *     &lt;enumeration value="Y"/>
 *     &lt;enumeration value="Z"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
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
