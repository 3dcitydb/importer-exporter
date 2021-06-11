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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/&gt;
 *           &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}common_newparam_type"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="technique"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/&gt;
 *                   &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/&gt;
 *                     &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}common_newparam_type"/&gt;
 *                   &lt;/choice&gt;
 *                   &lt;choice&gt;
 *                     &lt;element name="constant"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                             &lt;sequence&gt;
 *                               &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                               &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
 *                               &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                               &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                             &lt;/sequence&gt;
 *                           &lt;/restriction&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="lambert"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                             &lt;sequence&gt;
 *                               &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="ambient" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="diffuse" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                               &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
 *                               &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                               &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                             &lt;/sequence&gt;
 *                           &lt;/restriction&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="phong"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                             &lt;sequence&gt;
 *                               &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="ambient" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="diffuse" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="specular" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="shininess" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                               &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                               &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
 *                               &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                               &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                             &lt;/sequence&gt;
 *                           &lt;/restriction&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="blinn"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                             &lt;sequence&gt;
 *                               &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="ambient" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="diffuse" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="specular" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="shininess" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                               &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
 *                               &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                               &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
 *                               &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                               &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
 *                             &lt;/sequence&gt;
 *                           &lt;/restriction&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                   &lt;/choice&gt;
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="sid" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "asset",
    "imageOrNewparam",
    "technique",
    "extra"
})
public class ProfileCOMMON {

    protected Asset asset;
    @XmlElements({
        @XmlElement(name = "image", type = Image.class),
        @XmlElement(name = "newparam", type = CommonNewparamType.class)
    })
    protected List<Object> imageOrNewparam;
    @XmlElement(required = true)
    protected ProfileCOMMON.Technique technique;
    protected List<Extra> extra;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Ruft den Wert der asset-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Asset }
     *     
     */
    public Asset getAsset() {
        return asset;
    }

    /**
     * Legt den Wert der asset-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Asset }
     *     
     */
    public void setAsset(Asset value) {
        this.asset = value;
    }

    public boolean isSetAsset() {
        return (this.asset!= null);
    }

    /**
     * Gets the value of the imageOrNewparam property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the imageOrNewparam property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImageOrNewparam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Image }
     * {@link CommonNewparamType }
     * 
     * 
     */
    public List<Object> getImageOrNewparam() {
        if (imageOrNewparam == null) {
            imageOrNewparam = new ArrayList<Object>();
        }
        return this.imageOrNewparam;
    }

    public boolean isSetImageOrNewparam() {
        return ((this.imageOrNewparam!= null)&&(!this.imageOrNewparam.isEmpty()));
    }

    public void unsetImageOrNewparam() {
        this.imageOrNewparam = null;
    }

    /**
     * Ruft den Wert der technique-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProfileCOMMON.Technique }
     *     
     */
    public ProfileCOMMON.Technique getTechnique() {
        return technique;
    }

    /**
     * Legt den Wert der technique-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProfileCOMMON.Technique }
     *     
     */
    public void setTechnique(ProfileCOMMON.Technique value) {
        this.technique = value;
    }

    public boolean isSetTechnique() {
        return (this.technique!= null);
    }

    /**
     * 
     * 						The extra element may appear any number of times.
     * 						Gets the value of the extra property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extra property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtra().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Extra }
     * 
     * 
     */
    public List<Extra> getExtra() {
        if (extra == null) {
            extra = new ArrayList<Extra>();
        }
        return this.extra;
    }

    public boolean isSetExtra() {
        return ((this.extra!= null)&&(!this.extra.isEmpty()));
    }

    public void unsetExtra() {
        this.extra = null;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    public boolean isSetId() {
        return (this.id!= null);
    }

    public void setImageOrNewparam(List<Object> value) {
        this.imageOrNewparam = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/&gt;
     *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/&gt;
     *           &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}common_newparam_type"/&gt;
     *         &lt;/choice&gt;
     *         &lt;choice&gt;
     *           &lt;element name="constant"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                   &lt;sequence&gt;
     *                     &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                     &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
     *                     &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                     &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                   &lt;/sequence&gt;
     *                 &lt;/restriction&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="lambert"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                   &lt;sequence&gt;
     *                     &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="ambient" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="diffuse" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                     &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
     *                     &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                     &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                   &lt;/sequence&gt;
     *                 &lt;/restriction&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="phong"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                   &lt;sequence&gt;
     *                     &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="ambient" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="diffuse" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="specular" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="shininess" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                     &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                     &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
     *                     &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                     &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                   &lt;/sequence&gt;
     *                 &lt;/restriction&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="blinn"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                   &lt;sequence&gt;
     *                     &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="ambient" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="diffuse" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="specular" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="shininess" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                     &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
     *                     &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                     &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
     *                     &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                     &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
     *                   &lt;/sequence&gt;
     *                 &lt;/restriction&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *         &lt;/choice&gt;
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="sid" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "asset",
        "imageOrNewparam",
        "constant",
        "lambert",
        "phong",
        "blinn",
        "extra"
    })
    public static class Technique {

        protected Asset asset;
        @XmlElements({
            @XmlElement(name = "image", type = Image.class),
            @XmlElement(name = "newparam", type = CommonNewparamType.class)
        })
        protected List<Object> imageOrNewparam;
        protected ProfileCOMMON.Technique.Constant constant;
        protected ProfileCOMMON.Technique.Lambert lambert;
        protected ProfileCOMMON.Technique.Phong phong;
        protected ProfileCOMMON.Technique.Blinn blinn;
        protected List<Extra> extra;
        @XmlAttribute(name = "id")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "sid", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String sid;

        /**
         * 
         * 									The technique element may contain an asset element.
         * 									
         * 
         * @return
         *     possible object is
         *     {@link Asset }
         *     
         */
        public Asset getAsset() {
            return asset;
        }

        /**
         * Legt den Wert der asset-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Asset }
         *     
         */
        public void setAsset(Asset value) {
            this.asset = value;
        }

        public boolean isSetAsset() {
            return (this.asset!= null);
        }

        /**
         * Gets the value of the imageOrNewparam property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the imageOrNewparam property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getImageOrNewparam().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Image }
         * {@link CommonNewparamType }
         * 
         * 
         */
        public List<Object> getImageOrNewparam() {
            if (imageOrNewparam == null) {
                imageOrNewparam = new ArrayList<Object>();
            }
            return this.imageOrNewparam;
        }

        public boolean isSetImageOrNewparam() {
            return ((this.imageOrNewparam!= null)&&(!this.imageOrNewparam.isEmpty()));
        }

        public void unsetImageOrNewparam() {
            this.imageOrNewparam = null;
        }

        /**
         * Ruft den Wert der constant-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ProfileCOMMON.Technique.Constant }
         *     
         */
        public ProfileCOMMON.Technique.Constant getConstant() {
            return constant;
        }

        /**
         * Legt den Wert der constant-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ProfileCOMMON.Technique.Constant }
         *     
         */
        public void setConstant(ProfileCOMMON.Technique.Constant value) {
            this.constant = value;
        }

        public boolean isSetConstant() {
            return (this.constant!= null);
        }

        /**
         * Ruft den Wert der lambert-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ProfileCOMMON.Technique.Lambert }
         *     
         */
        public ProfileCOMMON.Technique.Lambert getLambert() {
            return lambert;
        }

        /**
         * Legt den Wert der lambert-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ProfileCOMMON.Technique.Lambert }
         *     
         */
        public void setLambert(ProfileCOMMON.Technique.Lambert value) {
            this.lambert = value;
        }

        public boolean isSetLambert() {
            return (this.lambert!= null);
        }

        /**
         * Ruft den Wert der phong-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ProfileCOMMON.Technique.Phong }
         *     
         */
        public ProfileCOMMON.Technique.Phong getPhong() {
            return phong;
        }

        /**
         * Legt den Wert der phong-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ProfileCOMMON.Technique.Phong }
         *     
         */
        public void setPhong(ProfileCOMMON.Technique.Phong value) {
            this.phong = value;
        }

        public boolean isSetPhong() {
            return (this.phong!= null);
        }

        /**
         * Ruft den Wert der blinn-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ProfileCOMMON.Technique.Blinn }
         *     
         */
        public ProfileCOMMON.Technique.Blinn getBlinn() {
            return blinn;
        }

        /**
         * Legt den Wert der blinn-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ProfileCOMMON.Technique.Blinn }
         *     
         */
        public void setBlinn(ProfileCOMMON.Technique.Blinn value) {
            this.blinn = value;
        }

        public boolean isSetBlinn() {
            return (this.blinn!= null);
        }

        /**
         * 
         * 									The extra element may appear any number of times.
         * 									Gets the value of the extra property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the extra property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getExtra().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Extra }
         * 
         * 
         */
        public List<Extra> getExtra() {
            if (extra == null) {
                extra = new ArrayList<Extra>();
            }
            return this.extra;
        }

        public boolean isSetExtra() {
            return ((this.extra!= null)&&(!this.extra.isEmpty()));
        }

        public void unsetExtra() {
            this.extra = null;
        }

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getId() {
            return id;
        }

        /**
         * Legt den Wert der id-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setId(String value) {
            this.id = value;
        }

        public boolean isSetId() {
            return (this.id!= null);
        }

        /**
         * Ruft den Wert der sid-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSid() {
            return sid;
        }

        /**
         * Legt den Wert der sid-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSid(String value) {
            this.sid = value;
        }

        public boolean isSetSid() {
            return (this.sid!= null);
        }

        public void setImageOrNewparam(List<Object> value) {
            this.imageOrNewparam = value;
        }

        public void setExtra(List<Extra> value) {
            this.extra = value;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="ambient" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="diffuse" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="specular" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="shininess" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *         &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *         &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
         *         &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *         &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "emission",
            "ambient",
            "diffuse",
            "specular",
            "shininess",
            "reflective",
            "reflectivity",
            "transparent",
            "transparency",
            "indexOfRefraction"
        })
        public static class Blinn {

            protected CommonColorOrTextureType emission;
            protected CommonColorOrTextureType ambient;
            protected CommonColorOrTextureType diffuse;
            protected CommonColorOrTextureType specular;
            protected CommonFloatOrParamType shininess;
            protected CommonColorOrTextureType reflective;
            protected CommonFloatOrParamType reflectivity;
            protected CommonTransparentType transparent;
            protected CommonFloatOrParamType transparency;
            @XmlElement(name = "index_of_refraction")
            protected CommonFloatOrParamType indexOfRefraction;

            /**
             * Ruft den Wert der emission-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getEmission() {
                return emission;
            }

            /**
             * Legt den Wert der emission-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setEmission(CommonColorOrTextureType value) {
                this.emission = value;
            }

            public boolean isSetEmission() {
                return (this.emission!= null);
            }

            /**
             * Ruft den Wert der ambient-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getAmbient() {
                return ambient;
            }

            /**
             * Legt den Wert der ambient-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setAmbient(CommonColorOrTextureType value) {
                this.ambient = value;
            }

            public boolean isSetAmbient() {
                return (this.ambient!= null);
            }

            /**
             * Ruft den Wert der diffuse-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getDiffuse() {
                return diffuse;
            }

            /**
             * Legt den Wert der diffuse-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setDiffuse(CommonColorOrTextureType value) {
                this.diffuse = value;
            }

            public boolean isSetDiffuse() {
                return (this.diffuse!= null);
            }

            /**
             * Ruft den Wert der specular-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getSpecular() {
                return specular;
            }

            /**
             * Legt den Wert der specular-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setSpecular(CommonColorOrTextureType value) {
                this.specular = value;
            }

            public boolean isSetSpecular() {
                return (this.specular!= null);
            }

            /**
             * Ruft den Wert der shininess-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getShininess() {
                return shininess;
            }

            /**
             * Legt den Wert der shininess-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setShininess(CommonFloatOrParamType value) {
                this.shininess = value;
            }

            public boolean isSetShininess() {
                return (this.shininess!= null);
            }

            /**
             * Ruft den Wert der reflective-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getReflective() {
                return reflective;
            }

            /**
             * Legt den Wert der reflective-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setReflective(CommonColorOrTextureType value) {
                this.reflective = value;
            }

            public boolean isSetReflective() {
                return (this.reflective!= null);
            }

            /**
             * Ruft den Wert der reflectivity-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getReflectivity() {
                return reflectivity;
            }

            /**
             * Legt den Wert der reflectivity-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setReflectivity(CommonFloatOrParamType value) {
                this.reflectivity = value;
            }

            public boolean isSetReflectivity() {
                return (this.reflectivity!= null);
            }

            /**
             * Ruft den Wert der transparent-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonTransparentType }
             *     
             */
            public CommonTransparentType getTransparent() {
                return transparent;
            }

            /**
             * Legt den Wert der transparent-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonTransparentType }
             *     
             */
            public void setTransparent(CommonTransparentType value) {
                this.transparent = value;
            }

            public boolean isSetTransparent() {
                return (this.transparent!= null);
            }

            /**
             * Ruft den Wert der transparency-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getTransparency() {
                return transparency;
            }

            /**
             * Legt den Wert der transparency-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setTransparency(CommonFloatOrParamType value) {
                this.transparency = value;
            }

            public boolean isSetTransparency() {
                return (this.transparency!= null);
            }

            /**
             * Ruft den Wert der indexOfRefraction-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getIndexOfRefraction() {
                return indexOfRefraction;
            }

            /**
             * Legt den Wert der indexOfRefraction-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setIndexOfRefraction(CommonFloatOrParamType value) {
                this.indexOfRefraction = value;
            }

            public boolean isSetIndexOfRefraction() {
                return (this.indexOfRefraction!= null);
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *         &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
         *         &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *         &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "emission",
            "reflective",
            "reflectivity",
            "transparent",
            "transparency",
            "indexOfRefraction"
        })
        public static class Constant {

            protected CommonColorOrTextureType emission;
            protected CommonColorOrTextureType reflective;
            protected CommonFloatOrParamType reflectivity;
            protected CommonTransparentType transparent;
            protected CommonFloatOrParamType transparency;
            @XmlElement(name = "index_of_refraction")
            protected CommonFloatOrParamType indexOfRefraction;

            /**
             * Ruft den Wert der emission-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getEmission() {
                return emission;
            }

            /**
             * Legt den Wert der emission-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setEmission(CommonColorOrTextureType value) {
                this.emission = value;
            }

            public boolean isSetEmission() {
                return (this.emission!= null);
            }

            /**
             * Ruft den Wert der reflective-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getReflective() {
                return reflective;
            }

            /**
             * Legt den Wert der reflective-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setReflective(CommonColorOrTextureType value) {
                this.reflective = value;
            }

            public boolean isSetReflective() {
                return (this.reflective!= null);
            }

            /**
             * Ruft den Wert der reflectivity-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getReflectivity() {
                return reflectivity;
            }

            /**
             * Legt den Wert der reflectivity-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setReflectivity(CommonFloatOrParamType value) {
                this.reflectivity = value;
            }

            public boolean isSetReflectivity() {
                return (this.reflectivity!= null);
            }

            /**
             * Ruft den Wert der transparent-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonTransparentType }
             *     
             */
            public CommonTransparentType getTransparent() {
                return transparent;
            }

            /**
             * Legt den Wert der transparent-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonTransparentType }
             *     
             */
            public void setTransparent(CommonTransparentType value) {
                this.transparent = value;
            }

            public boolean isSetTransparent() {
                return (this.transparent!= null);
            }

            /**
             * Ruft den Wert der transparency-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getTransparency() {
                return transparency;
            }

            /**
             * Legt den Wert der transparency-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setTransparency(CommonFloatOrParamType value) {
                this.transparency = value;
            }

            public boolean isSetTransparency() {
                return (this.transparency!= null);
            }

            /**
             * Ruft den Wert der indexOfRefraction-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getIndexOfRefraction() {
                return indexOfRefraction;
            }

            /**
             * Legt den Wert der indexOfRefraction-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setIndexOfRefraction(CommonFloatOrParamType value) {
                this.indexOfRefraction = value;
            }

            public boolean isSetIndexOfRefraction() {
                return (this.indexOfRefraction!= null);
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="ambient" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="diffuse" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *         &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
         *         &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *         &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "emission",
            "ambient",
            "diffuse",
            "reflective",
            "reflectivity",
            "transparent",
            "transparency",
            "indexOfRefraction"
        })
        public static class Lambert {

            protected CommonColorOrTextureType emission;
            protected CommonColorOrTextureType ambient;
            protected CommonColorOrTextureType diffuse;
            protected CommonColorOrTextureType reflective;
            protected CommonFloatOrParamType reflectivity;
            protected CommonTransparentType transparent;
            protected CommonFloatOrParamType transparency;
            @XmlElement(name = "index_of_refraction")
            protected CommonFloatOrParamType indexOfRefraction;

            /**
             * Ruft den Wert der emission-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getEmission() {
                return emission;
            }

            /**
             * Legt den Wert der emission-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setEmission(CommonColorOrTextureType value) {
                this.emission = value;
            }

            public boolean isSetEmission() {
                return (this.emission!= null);
            }

            /**
             * Ruft den Wert der ambient-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getAmbient() {
                return ambient;
            }

            /**
             * Legt den Wert der ambient-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setAmbient(CommonColorOrTextureType value) {
                this.ambient = value;
            }

            public boolean isSetAmbient() {
                return (this.ambient!= null);
            }

            /**
             * Ruft den Wert der diffuse-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getDiffuse() {
                return diffuse;
            }

            /**
             * Legt den Wert der diffuse-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setDiffuse(CommonColorOrTextureType value) {
                this.diffuse = value;
            }

            public boolean isSetDiffuse() {
                return (this.diffuse!= null);
            }

            /**
             * Ruft den Wert der reflective-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getReflective() {
                return reflective;
            }

            /**
             * Legt den Wert der reflective-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setReflective(CommonColorOrTextureType value) {
                this.reflective = value;
            }

            public boolean isSetReflective() {
                return (this.reflective!= null);
            }

            /**
             * Ruft den Wert der reflectivity-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getReflectivity() {
                return reflectivity;
            }

            /**
             * Legt den Wert der reflectivity-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setReflectivity(CommonFloatOrParamType value) {
                this.reflectivity = value;
            }

            public boolean isSetReflectivity() {
                return (this.reflectivity!= null);
            }

            /**
             * Ruft den Wert der transparent-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonTransparentType }
             *     
             */
            public CommonTransparentType getTransparent() {
                return transparent;
            }

            /**
             * Legt den Wert der transparent-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonTransparentType }
             *     
             */
            public void setTransparent(CommonTransparentType value) {
                this.transparent = value;
            }

            public boolean isSetTransparent() {
                return (this.transparent!= null);
            }

            /**
             * Ruft den Wert der transparency-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getTransparency() {
                return transparency;
            }

            /**
             * Legt den Wert der transparency-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setTransparency(CommonFloatOrParamType value) {
                this.transparency = value;
            }

            public boolean isSetTransparency() {
                return (this.transparency!= null);
            }

            /**
             * Ruft den Wert der indexOfRefraction-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getIndexOfRefraction() {
                return indexOfRefraction;
            }

            /**
             * Legt den Wert der indexOfRefraction-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setIndexOfRefraction(CommonFloatOrParamType value) {
                this.indexOfRefraction = value;
            }

            public boolean isSetIndexOfRefraction() {
                return (this.indexOfRefraction!= null);
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="emission" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="ambient" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="diffuse" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="specular" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="shininess" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *         &lt;element name="reflective" type="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type" minOccurs="0"/&gt;
         *         &lt;element name="reflectivity" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *         &lt;element name="transparent" type="{http://www.collada.org/2005/11/COLLADASchema}common_transparent_type" minOccurs="0"/&gt;
         *         &lt;element name="transparency" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *         &lt;element name="index_of_refraction" type="{http://www.collada.org/2005/11/COLLADASchema}common_float_or_param_type" minOccurs="0"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "emission",
            "ambient",
            "diffuse",
            "specular",
            "shininess",
            "reflective",
            "reflectivity",
            "transparent",
            "transparency",
            "indexOfRefraction"
        })
        public static class Phong {

            protected CommonColorOrTextureType emission;
            protected CommonColorOrTextureType ambient;
            protected CommonColorOrTextureType diffuse;
            protected CommonColorOrTextureType specular;
            protected CommonFloatOrParamType shininess;
            protected CommonColorOrTextureType reflective;
            protected CommonFloatOrParamType reflectivity;
            protected CommonTransparentType transparent;
            protected CommonFloatOrParamType transparency;
            @XmlElement(name = "index_of_refraction")
            protected CommonFloatOrParamType indexOfRefraction;

            /**
             * Ruft den Wert der emission-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getEmission() {
                return emission;
            }

            /**
             * Legt den Wert der emission-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setEmission(CommonColorOrTextureType value) {
                this.emission = value;
            }

            public boolean isSetEmission() {
                return (this.emission!= null);
            }

            /**
             * Ruft den Wert der ambient-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getAmbient() {
                return ambient;
            }

            /**
             * Legt den Wert der ambient-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setAmbient(CommonColorOrTextureType value) {
                this.ambient = value;
            }

            public boolean isSetAmbient() {
                return (this.ambient!= null);
            }

            /**
             * Ruft den Wert der diffuse-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getDiffuse() {
                return diffuse;
            }

            /**
             * Legt den Wert der diffuse-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setDiffuse(CommonColorOrTextureType value) {
                this.diffuse = value;
            }

            public boolean isSetDiffuse() {
                return (this.diffuse!= null);
            }

            /**
             * Ruft den Wert der specular-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getSpecular() {
                return specular;
            }

            /**
             * Legt den Wert der specular-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setSpecular(CommonColorOrTextureType value) {
                this.specular = value;
            }

            public boolean isSetSpecular() {
                return (this.specular!= null);
            }

            /**
             * Ruft den Wert der shininess-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getShininess() {
                return shininess;
            }

            /**
             * Legt den Wert der shininess-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setShininess(CommonFloatOrParamType value) {
                this.shininess = value;
            }

            public boolean isSetShininess() {
                return (this.shininess!= null);
            }

            /**
             * Ruft den Wert der reflective-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public CommonColorOrTextureType getReflective() {
                return reflective;
            }

            /**
             * Legt den Wert der reflective-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonColorOrTextureType }
             *     
             */
            public void setReflective(CommonColorOrTextureType value) {
                this.reflective = value;
            }

            public boolean isSetReflective() {
                return (this.reflective!= null);
            }

            /**
             * Ruft den Wert der reflectivity-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getReflectivity() {
                return reflectivity;
            }

            /**
             * Legt den Wert der reflectivity-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setReflectivity(CommonFloatOrParamType value) {
                this.reflectivity = value;
            }

            public boolean isSetReflectivity() {
                return (this.reflectivity!= null);
            }

            /**
             * Ruft den Wert der transparent-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonTransparentType }
             *     
             */
            public CommonTransparentType getTransparent() {
                return transparent;
            }

            /**
             * Legt den Wert der transparent-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonTransparentType }
             *     
             */
            public void setTransparent(CommonTransparentType value) {
                this.transparent = value;
            }

            public boolean isSetTransparent() {
                return (this.transparent!= null);
            }

            /**
             * Ruft den Wert der transparency-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getTransparency() {
                return transparency;
            }

            /**
             * Legt den Wert der transparency-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setTransparency(CommonFloatOrParamType value) {
                this.transparency = value;
            }

            public boolean isSetTransparency() {
                return (this.transparency!= null);
            }

            /**
             * Ruft den Wert der indexOfRefraction-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public CommonFloatOrParamType getIndexOfRefraction() {
                return indexOfRefraction;
            }

            /**
             * Legt den Wert der indexOfRefraction-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CommonFloatOrParamType }
             *     
             */
            public void setIndexOfRefraction(CommonFloatOrParamType value) {
                this.indexOfRefraction = value;
            }

            public boolean isSetIndexOfRefraction() {
                return (this.indexOfRefraction!= null);
            }

        }

    }

}
