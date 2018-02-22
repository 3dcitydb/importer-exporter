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
 * <p>Java-Klasse für shapeEnumType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="shapeEnumType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="rectangle"/>
 *     &lt;enumeration value="cylinder"/>
 *     &lt;enumeration value="sphere"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "shapeEnumType")
@XmlEnum
public enum ShapeEnumType {

    @XmlEnumValue("rectangle")
    RECTANGLE("rectangle"),
    @XmlEnumValue("cylinder")
    CYLINDER("cylinder"),
    @XmlEnumValue("sphere")
    SPHERE("sphere");
    private final String value;

    ShapeEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ShapeEnumType fromValue(String v) {
        for (ShapeEnumType c: ShapeEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
