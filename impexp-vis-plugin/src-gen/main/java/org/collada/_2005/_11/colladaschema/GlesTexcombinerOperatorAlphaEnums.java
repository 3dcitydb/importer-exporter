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
 * <p>Java-Klasse für gles_texcombiner_operatorAlpha_enums.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gles_texcombiner_operatorAlpha_enums"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="REPLACE"/&gt;
 *     &lt;enumeration value="MODULATE"/&gt;
 *     &lt;enumeration value="ADD"/&gt;
 *     &lt;enumeration value="ADD_SIGNED"/&gt;
 *     &lt;enumeration value="INTERPOLATE"/&gt;
 *     &lt;enumeration value="SUBTRACT"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gles_texcombiner_operatorAlpha_enums")
@XmlEnum
public enum GlesTexcombinerOperatorAlphaEnums {

    REPLACE,
    MODULATE,
    ADD,
    ADD_SIGNED,
    INTERPOLATE,
    SUBTRACT;

    public String value() {
        return name();
    }

    public static GlesTexcombinerOperatorAlphaEnums fromValue(String v) {
        return valueOf(v);
    }

}
