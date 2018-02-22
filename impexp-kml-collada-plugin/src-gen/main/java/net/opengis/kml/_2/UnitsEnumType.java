//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package net.opengis.kml._2;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für unitsEnumType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="unitsEnumType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="fraction"/>
 *     &lt;enumeration value="pixels"/>
 *     &lt;enumeration value="insetPixels"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "unitsEnumType")
@XmlEnum
public enum UnitsEnumType {

    @XmlEnumValue("fraction")
    FRACTION("fraction"),
    @XmlEnumValue("pixels")
    PIXELS("pixels"),
    @XmlEnumValue("insetPixels")
    INSET_PIXELS("insetPixels");
    private final String value;

    UnitsEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UnitsEnumType fromValue(String v) {
        for (UnitsEnumType c: UnitsEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
