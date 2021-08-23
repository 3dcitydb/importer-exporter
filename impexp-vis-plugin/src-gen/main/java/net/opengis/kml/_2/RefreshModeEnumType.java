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
 * <p>Java-Klasse für refreshModeEnumType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="refreshModeEnumType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="onChange"/&gt;
 *     &lt;enumeration value="onInterval"/&gt;
 *     &lt;enumeration value="onExpire"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
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
