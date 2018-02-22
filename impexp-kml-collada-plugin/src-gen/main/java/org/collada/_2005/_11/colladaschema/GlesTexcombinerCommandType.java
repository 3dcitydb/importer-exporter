//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für gles_texcombiner_command_type complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="gles_texcombiner_command_type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="constant" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texture_constant_type" minOccurs="0"/>
 *         &lt;element name="RGB" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_commandRGB_type" minOccurs="0"/>
 *         &lt;element name="alpha" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_commandAlpha_type" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gles_texcombiner_command_type", propOrder = {
    "constant",
    "rgb",
    "alpha"
})
public class GlesTexcombinerCommandType {

    protected GlesTextureConstantType constant;
    @XmlElement(name = "RGB")
    protected GlesTexcombinerCommandRGBType rgb;
    protected GlesTexcombinerCommandAlphaType alpha;

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
     * Ruft den Wert der rgb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlesTexcombinerCommandRGBType }
     *     
     */
    public GlesTexcombinerCommandRGBType getRGB() {
        return rgb;
    }

    /**
     * Legt den Wert der rgb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesTexcombinerCommandRGBType }
     *     
     */
    public void setRGB(GlesTexcombinerCommandRGBType value) {
        this.rgb = value;
    }

    public boolean isSetRGB() {
        return (this.rgb!= null);
    }

    /**
     * Ruft den Wert der alpha-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlesTexcombinerCommandAlphaType }
     *     
     */
    public GlesTexcombinerCommandAlphaType getAlpha() {
        return alpha;
    }

    /**
     * Legt den Wert der alpha-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesTexcombinerCommandAlphaType }
     *     
     */
    public void setAlpha(GlesTexcombinerCommandAlphaType value) {
        this.alpha = value;
    }

    public boolean isSetAlpha() {
        return (this.alpha!= null);
    }

}
