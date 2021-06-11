//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package org.collada._2005._11.colladaschema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für gles_texcombiner_argumentRGB_type complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="gles_texcombiner_argumentRGB_type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="source" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_source_enums" /&gt;
 *       &lt;attribute name="operand" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_operandRGB_enums" default="SRC_COLOR" /&gt;
 *       &lt;attribute name="unit" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gles_texcombiner_argumentRGB_type")
public class GlesTexcombinerArgumentRGBType {

    @XmlAttribute(name = "source")
    protected GlesTexcombinerSourceEnums source;
    @XmlAttribute(name = "operand")
    protected GlesTexcombinerOperandRGBEnums operand;
    @XmlAttribute(name = "unit")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String unit;

    /**
     * Ruft den Wert der source-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlesTexcombinerSourceEnums }
     *     
     */
    public GlesTexcombinerSourceEnums getSource() {
        return source;
    }

    /**
     * Legt den Wert der source-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesTexcombinerSourceEnums }
     *     
     */
    public void setSource(GlesTexcombinerSourceEnums value) {
        this.source = value;
    }

    public boolean isSetSource() {
        return (this.source!= null);
    }

    /**
     * Ruft den Wert der operand-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlesTexcombinerOperandRGBEnums }
     *     
     */
    public GlesTexcombinerOperandRGBEnums getOperand() {
        if (operand == null) {
            return GlesTexcombinerOperandRGBEnums.SRC_COLOR;
        } else {
            return operand;
        }
    }

    /**
     * Legt den Wert der operand-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesTexcombinerOperandRGBEnums }
     *     
     */
    public void setOperand(GlesTexcombinerOperandRGBEnums value) {
        this.operand = value;
    }

    public boolean isSetOperand() {
        return (this.operand!= null);
    }

    /**
     * Ruft den Wert der unit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Legt den Wert der unit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    public boolean isSetUnit() {
        return (this.unit!= null);
    }

}
