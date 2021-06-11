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
 * <p>Java-Klasse für fx_surface_face_enum.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="fx_surface_face_enum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="POSITIVE_X"/&gt;
 *     &lt;enumeration value="NEGATIVE_X"/&gt;
 *     &lt;enumeration value="POSITIVE_Y"/&gt;
 *     &lt;enumeration value="NEGATIVE_Y"/&gt;
 *     &lt;enumeration value="POSITIVE_Z"/&gt;
 *     &lt;enumeration value="NEGATIVE_Z"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "fx_surface_face_enum")
@XmlEnum
public enum FxSurfaceFaceEnum {

    POSITIVE_X,
    NEGATIVE_X,
    POSITIVE_Y,
    NEGATIVE_Y,
    POSITIVE_Z,
    NEGATIVE_Z;

    public String value() {
        return name();
    }

    public static FxSurfaceFaceEnum fromValue(String v) {
        return valueOf(v);
    }

}
