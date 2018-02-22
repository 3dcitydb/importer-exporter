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
 * <p>Java-Klasse für fx_modifier_enum_common.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="fx_modifier_enum_common">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="CONST"/>
 *     &lt;enumeration value="UNIFORM"/>
 *     &lt;enumeration value="VARYING"/>
 *     &lt;enumeration value="STATIC"/>
 *     &lt;enumeration value="VOLATILE"/>
 *     &lt;enumeration value="EXTERN"/>
 *     &lt;enumeration value="SHARED"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
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
