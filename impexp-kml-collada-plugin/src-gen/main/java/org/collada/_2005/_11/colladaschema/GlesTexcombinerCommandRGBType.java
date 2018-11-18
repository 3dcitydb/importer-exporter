//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Defines the RGB portion of a texture_pipeline command. This is a combiner-mode texturing operation.
 * 			
 * 
 * <p>Java-Klasse für gles_texcombiner_commandRGB_type complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="gles_texcombiner_commandRGB_type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="argument" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_argumentRGB_type" maxOccurs="3"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="operator" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_operatorRGB_enums" /&gt;
 *       &lt;attribute name="scale" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gles_texcombiner_commandRGB_type", propOrder = {
    "argument"
})
public class GlesTexcombinerCommandRGBType {

    @XmlElement(required = true)
    protected List<GlesTexcombinerArgumentRGBType> argument;
    @XmlAttribute(name = "operator")
    protected GlesTexcombinerOperatorRGBEnums operator;
    @XmlAttribute(name = "scale")
    protected Float scale;

    /**
     * Gets the value of the argument property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the argument property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArgument().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GlesTexcombinerArgumentRGBType }
     * 
     * 
     */
    public List<GlesTexcombinerArgumentRGBType> getArgument() {
        if (argument == null) {
            argument = new ArrayList<GlesTexcombinerArgumentRGBType>();
        }
        return this.argument;
    }

    public boolean isSetArgument() {
        return ((this.argument!= null)&&(!this.argument.isEmpty()));
    }

    public void unsetArgument() {
        this.argument = null;
    }

    /**
     * Ruft den Wert der operator-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlesTexcombinerOperatorRGBEnums }
     *     
     */
    public GlesTexcombinerOperatorRGBEnums getOperator() {
        return operator;
    }

    /**
     * Legt den Wert der operator-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesTexcombinerOperatorRGBEnums }
     *     
     */
    public void setOperator(GlesTexcombinerOperatorRGBEnums value) {
        this.operator = value;
    }

    public boolean isSetOperator() {
        return (this.operator!= null);
    }

    /**
     * Ruft den Wert der scale-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public float getScale() {
        return scale;
    }

    /**
     * Legt den Wert der scale-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setScale(float value) {
        this.scale = value;
    }

    public boolean isSetScale() {
        return (this.scale!= null);
    }

    public void unsetScale() {
        this.scale = null;
    }

    public void setArgument(List<GlesTexcombinerArgumentRGBType> value) {
        this.argument = value;
    }

}
