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
 * <p>Java-Klasse für altitudeModeEnumType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="altitudeModeEnumType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="clampToGround"/&gt;
 *     &lt;enumeration value="relativeToGround"/&gt;
 *     &lt;enumeration value="absolute"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "altitudeModeEnumType")
@XmlEnum
public enum AltitudeModeEnumType {

    @XmlEnumValue("clampToGround")
    CLAMP_TO_GROUND("clampToGround"),
    @XmlEnumValue("relativeToGround")
    RELATIVE_TO_GROUND("relativeToGround"),
    @XmlEnumValue("absolute")
    ABSOLUTE("absolute");
    private final String value;

    AltitudeModeEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AltitudeModeEnumType fromValue(String v) {
        for (AltitudeModeEnumType c: AltitudeModeEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
