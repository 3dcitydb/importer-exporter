//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package net.opengis.kml._2;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für colorModeEnumType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="colorModeEnumType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="normal"/&gt;
 *     &lt;enumeration value="random"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "colorModeEnumType")
@XmlEnum
public enum ColorModeEnumType {

    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("random")
    RANDOM("random");
    private final String value;

    ColorModeEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ColorModeEnumType fromValue(String v) {
        for (ColorModeEnumType c: ColorModeEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
