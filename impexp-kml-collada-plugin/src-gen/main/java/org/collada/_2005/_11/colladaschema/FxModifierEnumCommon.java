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
 * <p>Java-Klasse für fx_modifier_enum_common.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="fx_modifier_enum_common"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="CONST"/&gt;
 *     &lt;enumeration value="UNIFORM"/&gt;
 *     &lt;enumeration value="VARYING"/&gt;
 *     &lt;enumeration value="STATIC"/&gt;
 *     &lt;enumeration value="VOLATILE"/&gt;
 *     &lt;enumeration value="EXTERN"/&gt;
 *     &lt;enumeration value="SHARED"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "fx_modifier_enum_common")
@XmlEnum
public enum FxModifierEnumCommon {

    CONST,
    UNIFORM,
    VARYING,
    STATIC,
    VOLATILE,
    EXTERN,
    SHARED;

    public String value() {
        return name();
    }

    public static FxModifierEnumCommon fromValue(String v) {
        return valueOf(v);
    }

}
