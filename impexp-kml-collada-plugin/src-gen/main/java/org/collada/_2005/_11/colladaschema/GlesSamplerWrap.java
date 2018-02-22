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
 * <p>Java-Klasse für gles_sampler_wrap.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gles_sampler_wrap">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="REPEAT"/>
 *     &lt;enumeration value="CLAMP"/>
 *     &lt;enumeration value="CLAMP_TO_EDGE"/>
 *     &lt;enumeration value="MIRRORED_REPEAT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "gles_sampler_wrap")
@XmlEnum
public enum GlesSamplerWrap {

    REPEAT,
    CLAMP,
    CLAMP_TO_EDGE,

    /**
     * 
     * 					supported by GLES 1.1 only
     * 					
     * 
     */
    MIRRORED_REPEAT;

    public String value() {
        return name();
    }

    public static GlesSamplerWrap fromValue(String v) {
        return valueOf(v);
    }

}
