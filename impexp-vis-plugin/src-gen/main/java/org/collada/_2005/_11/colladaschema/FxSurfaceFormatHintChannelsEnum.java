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
 * <p>Java-Klasse für fx_surface_format_hint_channels_enum.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="fx_surface_format_hint_channels_enum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="RGB"/&gt;
 *     &lt;enumeration value="RGBA"/&gt;
 *     &lt;enumeration value="L"/&gt;
 *     &lt;enumeration value="LA"/&gt;
 *     &lt;enumeration value="D"/&gt;
 *     &lt;enumeration value="XYZ"/&gt;
 *     &lt;enumeration value="XYZW"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "fx_surface_format_hint_channels_enum")
@XmlEnum
public enum FxSurfaceFormatHintChannelsEnum {


    /**
     * RGB color  map
     * 
     */
    RGB,

    /**
     * RGB color + Alpha map often used for color + transparency or other things packed into channel A like specular power 
     * 
     */
    RGBA,

    /**
     * Luminance map often used for light mapping 
     * 
     */
    L,

    /**
     * Luminance+Alpha map often used for light mapping 
     * 
     */
    LA,

    /**
     * Depth map often used for displacement, parellax, relief, or shadow mapping 
     * 
     */
    D,

    /**
     * Typically used for normal maps or 3component displacement maps.
     * 
     */
    XYZ,

    /**
     * Typically used for normal maps where W is the depth for relief or parrallax mapping 
     * 
     */
    XYZW;

    public String value() {
        return name();
    }

    public static FxSurfaceFormatHintChannelsEnum fromValue(String v) {
        return valueOf(v);
    }

}
