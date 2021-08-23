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
 * <p>Java-Klasse für gles_sampler_wrap.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gles_sampler_wrap"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="REPEAT"/&gt;
 *     &lt;enumeration value="CLAMP"/&gt;
 *     &lt;enumeration value="CLAMP_TO_EDGE"/&gt;
 *     &lt;enumeration value="MIRRORED_REPEAT"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
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
