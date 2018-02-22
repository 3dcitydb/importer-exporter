//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für gles_texcombiner_operandAlpha_enums.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="gles_texcombiner_operandAlpha_enums">
 *   &lt;restriction base="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type">
 *     &lt;enumeration value="SRC_ALPHA"/>
 *     &lt;enumeration value="ONE_MINUS_SRC_ALPHA"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "gles_texcombiner_operandAlpha_enums")
@XmlEnum(GlBlendType.class)
public enum GlesTexcombinerOperandAlphaEnums {

    SRC_ALPHA,
    ONE_MINUS_SRC_ALPHA;

    public String value() {
        return name();
    }

    public static GlesTexcombinerOperandAlphaEnums fromValue(String v) {
        return valueOf(v);
    }

}
