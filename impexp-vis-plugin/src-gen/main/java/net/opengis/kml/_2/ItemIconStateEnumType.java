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
 * <p>Java-Klasse für itemIconStateEnumType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="itemIconStateEnumType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="open"/&gt;
 *     &lt;enumeration value="closed"/&gt;
 *     &lt;enumeration value="error"/&gt;
 *     &lt;enumeration value="fetching0"/&gt;
 *     &lt;enumeration value="fetching1"/&gt;
 *     &lt;enumeration value="fetching2"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "itemIconStateEnumType")
@XmlEnum
public enum ItemIconStateEnumType {

    @XmlEnumValue("open")
    OPEN("open"),
    @XmlEnumValue("closed")
    CLOSED("closed"),
    @XmlEnumValue("error")
    ERROR("error"),
    @XmlEnumValue("fetching0")
    FETCHING_0("fetching0"),
    @XmlEnumValue("fetching1")
    FETCHING_1("fetching1"),
    @XmlEnumValue("fetching2")
    FETCHING_2("fetching2");
    private final String value;

    ItemIconStateEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ItemIconStateEnumType fromValue(String v) {
        for (ItemIconStateEnumType c: ItemIconStateEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
