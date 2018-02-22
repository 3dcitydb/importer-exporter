//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
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
 * &lt;complexType name="gles_texcombiner_argumentRGB_type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="source" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_source_enums" />
 *       &lt;attribute name="operand" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_operandRGB_enums" default="SRC_COLOR" />
 *       &lt;attribute name="unit" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
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
