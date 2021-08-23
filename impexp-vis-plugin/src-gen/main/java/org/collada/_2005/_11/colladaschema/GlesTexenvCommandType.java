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
 * <p>Java-Klasse für gles_texenv_command_type complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="gles_texenv_command_type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="constant" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texture_constant_type" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="operator" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texenv_mode_enums" /&gt;
 *       &lt;attribute name="unit" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gles_texenv_command_type", propOrder = {
    "constant"
})
public class GlesTexenvCommandType {

    protected GlesTextureConstantType constant;
    @XmlAttribute(name = "operator")
    protected GlesTexenvModeEnums operator;
    @XmlAttribute(name = "unit")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String unit;

    /**
     * Ruft den Wert der constant-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlesTextureConstantType }
     *     
     */
    public GlesTextureConstantType getConstant() {
        return constant;
    }

    /**
     * Legt den Wert der constant-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesTextureConstantType }
     *     
     */
    public void setConstant(GlesTextureConstantType value) {
        this.constant = value;
    }

    public boolean isSetConstant() {
        return (this.constant!= null);
    }

    /**
     * Ruft den Wert der operator-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlesTexenvModeEnums }
     *     
     */
    public GlesTexenvModeEnums getOperator() {
        return operator;
    }

    /**
     * Legt den Wert der operator-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesTexenvModeEnums }
     *     
     */
    public void setOperator(GlesTexenvModeEnums value) {
        this.operator = value;
    }

    public boolean isSetOperator() {
        return (this.operator!= null);
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
