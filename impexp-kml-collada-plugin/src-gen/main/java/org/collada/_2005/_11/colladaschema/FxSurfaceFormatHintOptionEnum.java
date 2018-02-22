//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für fx_surface_format_hint_option_enum.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="fx_surface_format_hint_option_enum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SRGB_GAMMA"/>
 *     &lt;enumeration value="NORMALIZED3"/>
 *     &lt;enumeration value="NORMALIZED4"/>
 *     &lt;enumeration value="COMPRESSABLE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "fx_surface_format_hint_option_enum")
@XmlEnum
public enum FxSurfaceFormatHintOptionEnum {


    /**
     * colors are stored with respect to the sRGB 2.2 gamma curve rather than linear
     * 
     */
    SRGB_GAMMA("SRGB_GAMMA"),

    /**
     * the texel's XYZ/RGB should be normalized such as in a normal map.
     * 
     */
    @XmlEnumValue("NORMALIZED3")
    NORMALIZED_3("NORMALIZED3"),

    /**
     * the texel's XYZW/RGBA should be normalized such as in a normal map.
     * 
     */
    @XmlEnumValue("NORMALIZED4")
    NORMALIZED_4("NORMALIZED4"),

    /**
     * The surface may use run-time compression.  Considering the best compression based on desired, channel, range, precision, and options 
     * 
     */
    COMPRESSABLE("COMPRESSABLE");
    private final String value;

    FxSurfaceFormatHintOptionEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FxSurfaceFormatHintOptionEnum fromValue(String v) {
        for (FxSurfaceFormatHintOptionEnum c: FxSurfaceFormatHintOptionEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
