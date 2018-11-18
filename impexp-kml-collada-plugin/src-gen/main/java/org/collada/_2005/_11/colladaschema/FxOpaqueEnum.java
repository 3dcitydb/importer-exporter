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
 * <p>Java-Klasse für fx_opaque_enum.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="fx_opaque_enum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="A_ONE"/&gt;
 *     &lt;enumeration value="RGB_ZERO"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "fx_opaque_enum")
@XmlEnum
public enum FxOpaqueEnum {


    /**
     * 
     * 						When a transparent opaque attribute is set to A_ONE, it means the transparency information will be taken from the alpha channel of the color, texture, or parameter supplying the value. The value of 1.0 is opaque in this mode.
     * 					
     * 
     */
    A_ONE,

    /**
     * 
     * 						When a transparent opaque attribute is set to RGB_ZERO, it means the transparency information will be taken from the red, green, and blue channels of the color, texture, or parameter supplying the value. Each channel is modulated independently. The value of 0.0 is opaque in this mode.
     * 					
     * 
     */
    RGB_ZERO;

    public String value() {
        return name();
    }

    public static FxOpaqueEnum fromValue(String v) {
        return valueOf(v);
    }

}
