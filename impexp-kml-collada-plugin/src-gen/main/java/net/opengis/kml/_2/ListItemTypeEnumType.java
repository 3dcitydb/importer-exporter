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
 * <p>Java-Klasse für listItemTypeEnumType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="listItemTypeEnumType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="radioFolder"/>
 *     &lt;enumeration value="check"/>
 *     &lt;enumeration value="checkHideChildren"/>
 *     &lt;enumeration value="checkOffOnly"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "listItemTypeEnumType")
@XmlEnum
public enum ListItemTypeEnumType {

    @XmlEnumValue("radioFolder")
    RADIO_FOLDER("radioFolder"),
    @XmlEnumValue("check")
    CHECK("check"),
    @XmlEnumValue("checkHideChildren")
    CHECK_HIDE_CHILDREN("checkHideChildren"),
    @XmlEnumValue("checkOffOnly")
    CHECK_OFF_ONLY("checkOffOnly");
    private final String value;

    ListItemTypeEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ListItemTypeEnumType fromValue(String v) {
        for (ListItemTypeEnumType c: ListItemTypeEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
