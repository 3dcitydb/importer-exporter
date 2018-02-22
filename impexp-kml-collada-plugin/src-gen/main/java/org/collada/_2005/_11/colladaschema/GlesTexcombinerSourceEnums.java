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
 * <p>Java-Klasse für gles_texcombiner_source_enums.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gles_texcombiner_source_enums">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="TEXTURE"/>
 *     &lt;enumeration value="CONSTANT"/>
 *     &lt;enumeration value="PRIMARY"/>
 *     &lt;enumeration value="PREVIOUS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "gles_texcombiner_source_enums")
@XmlEnum
public enum GlesTexcombinerSourceEnums {

    TEXTURE,
    CONSTANT,
    PRIMARY,
    PREVIOUS;

    public String value() {
        return name();
    }

    public static GlesTexcombinerSourceEnums fromValue(String v) {
        return valueOf(v);
    }

}
