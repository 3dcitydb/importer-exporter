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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 			A three-dimensional texture sampler.
 * 			
 * 
 * <p>Java-Klasse für fx_sampler3D_common complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fx_sampler3D_common"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}NCName"/&gt;
 *         &lt;element name="wrap_s" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_wrap_common" minOccurs="0"/&gt;
 *         &lt;element name="wrap_t" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_wrap_common" minOccurs="0"/&gt;
 *         &lt;element name="wrap_p" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_wrap_common" minOccurs="0"/&gt;
 *         &lt;element name="minfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/&gt;
 *         &lt;element name="magfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/&gt;
 *         &lt;element name="mipfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/&gt;
 *         &lt;element name="border_color" type="{http://www.collada.org/2005/11/COLLADASchema}fx_color_common" minOccurs="0"/&gt;
 *         &lt;element name="mipmap_maxlevel" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" minOccurs="0"/&gt;
 *         &lt;element name="mipmap_bias" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fx_sampler3D_common", propOrder = {
    "source",
    "wrapS",
    "wrapT",
    "wrapP",
    "minfilter",
    "magfilter",
    "mipfilter",
    "borderColor",
    "mipmapMaxlevel",
    "mipmapBias",
    "extra"
})
@XmlSeeAlso({
    GlSampler3D.class,
    CgSampler3D.class
})
public class FxSampler3DCommon {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String source;
    @XmlElement(name = "wrap_s", defaultValue = "WRAP")
    @XmlSchemaType(name = "NMTOKEN")
    protected FxSamplerWrapCommon wrapS;
    @XmlElement(name = "wrap_t", defaultValue = "WRAP")
    @XmlSchemaType(name = "NMTOKEN")
    protected FxSamplerWrapCommon wrapT;
    @XmlElement(name = "wrap_p", defaultValue = "WRAP")
    @XmlSchemaType(name = "NMTOKEN")
    protected FxSamplerWrapCommon wrapP;
    @XmlElement(defaultValue = "NONE")
    @XmlSchemaType(name = "NMTOKEN")
    protected FxSamplerFilterCommon minfilter;
    @XmlElement(defaultValue = "NONE")
    @XmlSchemaType(name = "NMTOKEN")
    protected FxSamplerFilterCommon magfilter;
    @XmlElement(defaultValue = "NONE")
    @XmlSchemaType(name = "NMTOKEN")
    protected FxSamplerFilterCommon mipfilter;
    @XmlList
    @XmlElement(name = "border_color", type = Double.class)
    protected List<Double> borderColor;
    @XmlElement(name = "mipmap_maxlevel", defaultValue = "255")
    @XmlSchemaType(name = "unsignedByte")
    protected Short mipmapMaxlevel;
    @XmlElement(name = "mipmap_bias", defaultValue = "0.0")
    protected Float mipmapBias;
    protected List<Extra> extra;

    /**
     * Ruft den Wert der source-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSource() {
        return source;
    }

    /**
     * Legt den Wert der source-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSource(String value) {
        this.source = value;
    }

    public boolean isSetSource() {
        return (this.source!= null);
    }

    /**
     * Ruft den Wert der wrapS-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSamplerWrapCommon }
     *     
     */
    public FxSamplerWrapCommon getWrapS() {
        return wrapS;
    }

    /**
     * Legt den Wert der wrapS-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerWrapCommon }
     *     
     */
    public void setWrapS(FxSamplerWrapCommon value) {
        this.wrapS = value;
    }

    public boolean isSetWrapS() {
        return (this.wrapS!= null);
    }

    /**
     * Ruft den Wert der wrapT-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSamplerWrapCommon }
     *     
     */
    public FxSamplerWrapCommon getWrapT() {
        return wrapT;
    }

    /**
     * Legt den Wert der wrapT-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerWrapCommon }
     *     
     */
    public void setWrapT(FxSamplerWrapCommon value) {
        this.wrapT = value;
    }

    public boolean isSetWrapT() {
        return (this.wrapT!= null);
    }

    /**
     * Ruft den Wert der wrapP-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSamplerWrapCommon }
     *     
     */
    public FxSamplerWrapCommon getWrapP() {
        return wrapP;
    }

    /**
     * Legt den Wert der wrapP-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerWrapCommon }
     *     
     */
    public void setWrapP(FxSamplerWrapCommon value) {
        this.wrapP = value;
    }

    public boolean isSetWrapP() {
        return (this.wrapP!= null);
    }

    /**
     * Ruft den Wert der minfilter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public FxSamplerFilterCommon getMinfilter() {
        return minfilter;
    }

    /**
     * Legt den Wert der minfilter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public void setMinfilter(FxSamplerFilterCommon value) {
        this.minfilter = value;
    }

    public boolean isSetMinfilter() {
        return (this.minfilter!= null);
    }

    /**
     * Ruft den Wert der magfilter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public FxSamplerFilterCommon getMagfilter() {
        return magfilter;
    }

    /**
     * Legt den Wert der magfilter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public void setMagfilter(FxSamplerFilterCommon value) {
        this.magfilter = value;
    }

    public boolean isSetMagfilter() {
        return (this.magfilter!= null);
    }

    /**
     * Ruft den Wert der mipfilter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public FxSamplerFilterCommon getMipfilter() {
        return mipfilter;
    }

    /**
     * Legt den Wert der mipfilter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerFilterCommon }
     *     
     */
    public void setMipfilter(FxSamplerFilterCommon value) {
        this.mipfilter = value;
    }

    public boolean isSetMipfilter() {
        return (this.mipfilter!= null);
    }

    /**
     * Gets the value of the borderColor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the borderColor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBorderColor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getBorderColor() {
        if (borderColor == null) {
            borderColor = new ArrayList<Double>();
        }
        return this.borderColor;
    }

    public boolean isSetBorderColor() {
        return ((this.borderColor!= null)&&(!this.borderColor.isEmpty()));
    }

    public void unsetBorderColor() {
        this.borderColor = null;
    }

    /**
     * Ruft den Wert der mipmapMaxlevel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getMipmapMaxlevel() {
        return mipmapMaxlevel;
    }

    /**
     * Legt den Wert der mipmapMaxlevel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMipmapMaxlevel(Short value) {
        this.mipmapMaxlevel = value;
    }

    public boolean isSetMipmapMaxlevel() {
        return (this.mipmapMaxlevel!= null);
    }

    /**
     * Ruft den Wert der mipmapBias-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMipmapBias() {
        return mipmapBias;
    }

    /**
     * Legt den Wert der mipmapBias-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMipmapBias(Float value) {
        this.mipmapBias = value;
    }

    public boolean isSetMipmapBias() {
        return (this.mipmapBias!= null);
    }

    /**
     * Gets the value of the extra property.
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

    public void setBorderColor(List<Double> value) {
        this.borderColor = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

}
