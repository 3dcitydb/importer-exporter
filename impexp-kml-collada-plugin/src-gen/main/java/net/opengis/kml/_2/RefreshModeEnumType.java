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
 * <p>Java-Klasse für refreshModeEnumType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="refreshModeEnumType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="onChange"/>
 *     &lt;enumeration value="onInterval"/>
 *     &lt;enumeration value="onExpire"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "refreshModeEnumType")
@XmlEnum
public enum RefreshModeEnumType {

    @XmlEnumValue("onChange")
    ON_CHANGE("onChange"),
    @XmlEnumValue("onInterval")
    ON_INTERVAL("onInterval"),
    @XmlEnumValue("onExpire")
    ON_EXPIRE("onExpire");
    private final String value;

    RefreshModeEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RefreshModeEnumType fromValue(String v) {
        for (RefreshModeEnumType c: RefreshModeEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
