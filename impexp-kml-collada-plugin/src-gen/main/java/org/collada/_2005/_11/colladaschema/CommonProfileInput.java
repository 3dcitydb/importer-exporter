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
 * <p>Java-Klasse für Common_profile_input.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="Common_profile_input"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="BINORMAL"/&gt;
 *     &lt;enumeration value="COLOR"/&gt;
 *     &lt;enumeration value="CONTINUITY"/&gt;
 *     &lt;enumeration value="IMAGE"/&gt;
 *     &lt;enumeration value="IN_TANGENT"/&gt;
 *     &lt;enumeration value="INPUT"/&gt;
 *     &lt;enumeration value="INTERPOLATION"/&gt;
 *     &lt;enumeration value="INV_BIND_MATRIX"/&gt;
 *     &lt;enumeration value="JOINT"/&gt;
 *     &lt;enumeration value="LINEAR_STEPS"/&gt;
 *     &lt;enumeration value="MORPH_TARGET"/&gt;
 *     &lt;enumeration value="MORPH_WEIGHT"/&gt;
 *     &lt;enumeration value="NORMAL"/&gt;
 *     &lt;enumeration value="OUTPUT"/&gt;
 *     &lt;enumeration value="OUT_TANGENT"/&gt;
 *     &lt;enumeration value="POSITION"/&gt;
 *     &lt;enumeration value="TANGENT"/&gt;
 *     &lt;enumeration value="TEXBINORMAL"/&gt;
 *     &lt;enumeration value="TEXCOORD"/&gt;
 *     &lt;enumeration value="TEXTANGENT"/&gt;
 *     &lt;enumeration value="UV"/&gt;
 *     &lt;enumeration value="VERTEX"/&gt;
 *     &lt;enumeration value="WEIGHT"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "Common_profile_input")
@XmlEnum
public enum CommonProfileInput {

    BINORMAL,
    COLOR,
    CONTINUITY,
    IMAGE,
    IN_TANGENT,
    INPUT,
    INTERPOLATION,
    INV_BIND_MATRIX,
    JOINT,
    LINEAR_STEPS,
    MORPH_TARGET,
    MORPH_WEIGHT,
    NORMAL,
    OUTPUT,
    OUT_TANGENT,
    POSITION,
    TANGENT,
    TEXBINORMAL,
    TEXCOORD,
    TEXTANGENT,
    UV,
    VERTEX,
    WEIGHT;

    public String value() {
        return name();
    }

    public static CommonProfileInput fromValue(String v) {
        return valueOf(v);
    }

}
