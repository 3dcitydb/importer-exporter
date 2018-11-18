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
 * <p>Java-Klasse für glsl_pipeline_stage.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="glsl_pipeline_stage"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="VERTEXPROGRAM"/&gt;
 *     &lt;enumeration value="FRAGMENTPROGRAM"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "glsl_pipeline_stage")
@XmlEnum
public enum GlslPipelineStage {

    VERTEXPROGRAM,
    FRAGMENTPROGRAM;

    public String value() {
        return name();
    }

    public static GlslPipelineStage fromValue(String v) {
        return valueOf(v);
    }

}
