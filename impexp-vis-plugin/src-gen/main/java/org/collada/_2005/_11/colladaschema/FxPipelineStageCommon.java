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
 * <p>Java-Klasse für fx_pipeline_stage_common.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="fx_pipeline_stage_common"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="VERTEXPROGRAM"/&gt;
 *     &lt;enumeration value="FRAGMENTPROGRAM"/&gt;
 *     &lt;enumeration value="VERTEXSHADER"/&gt;
 *     &lt;enumeration value="PIXELSHADER"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "fx_pipeline_stage_common")
@XmlEnum
public enum FxPipelineStageCommon {

    VERTEXPROGRAM,
    FRAGMENTPROGRAM,
    VERTEXSHADER,
    PIXELSHADER;

    public String value() {
        return name();
    }

    public static FxPipelineStageCommon fromValue(String v) {
        return valueOf(v);
    }

}
