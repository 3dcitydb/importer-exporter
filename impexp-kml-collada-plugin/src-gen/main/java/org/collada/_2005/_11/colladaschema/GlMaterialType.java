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
 * <p>Java-Klasse für gl_material_type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gl_material_type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="EMISSION"/&gt;
 *     &lt;enumeration value="AMBIENT"/&gt;
 *     &lt;enumeration value="DIFFUSE"/&gt;
 *     &lt;enumeration value="SPECULAR"/&gt;
 *     &lt;enumeration value="AMBIENT_AND_DIFFUSE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gl_material_type")
@XmlEnum
public enum GlMaterialType {

    EMISSION,
    AMBIENT,
    DIFFUSE,
    SPECULAR,
    AMBIENT_AND_DIFFUSE;

    public String value() {
        return name();
    }

    public static GlMaterialType fromValue(String v) {
        return valueOf(v);
    }

}
