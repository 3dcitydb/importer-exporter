//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 			Two-dimensional texture sampler state for profile_GLES. This is a bundle of sampler-specific states that will be referenced by one or more texture_units.
 * 			
 * 
 * <p>Java-Klasse für gles_sampler_state complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="gles_sampler_state">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="wrap_s" type="{http://www.collada.org/2005/11/COLLADASchema}gles_sampler_wrap" minOccurs="0"/>
 *         &lt;element name="wrap_t" type="{http://www.collada.org/2005/11/COLLADASchema}gles_sampler_wrap" minOccurs="0"/>
 *         &lt;element name="minfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/>
 *         &lt;element name="magfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/>
 *         &lt;element name="mipfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/>
 *         &lt;element name="mipmap_maxlevel" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" minOccurs="0"/>
 *         &lt;element name="mipmap_bias" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gles_sampler_state", propOrder = {
    "wrapS",
    "wrapT",
    "minfilter",
    "magfilter",
    "mipfilter",
    "mipmapMaxlevel",
    "mipmapBias",
    "extra"
})
public class GlesSamplerState {

    @XmlElement(name = "wrap_s", defaultValue = "REPEAT")
    protected GlesSamplerWrap wrapS;
    @XmlElement(name = "wrap_t", defaultValue = "REPEAT")
    protected GlesSamplerWrap wrapT;
    @XmlElement(defaultValue = "NONE")
    protected FxSamplerFilterCommon minfilter;
    @XmlElement(defaultValue = "NONE")
    protected FxSamplerFilterCommon magfilter;
    @XmlElement(defaultValue = "NONE")
    protected FxSamplerFilterCommon mipfilter;
    @XmlElement(name = "mipmap_maxlevel", defaultValue = "255")
    @XmlSchemaType(name = "unsignedByte")
    protected Short mipmapMaxlevel;
    @XmlElement(name = "mipmap_bias", defaultValue = "0.0")
    protected Float mipmapBias;
    protected List<Extra> extra;
    @XmlAttribute(name = "sid")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String sid;

    /**
     * Ruft den Wert der wrapS-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlesSamplerWrap }
     *     
     */
    public GlesSamplerWrap getWrapS() {
        return wrapS;
    }

    /**
     * Legt den Wert der wrapS-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesSamplerWrap }
     *     
     */
    public void setWrapS(GlesSamplerWrap value) {
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
     *     {@link GlesSamplerWrap }
     *     
     */
    public GlesSamplerWrap getWrapT() {
        return wrapT;
    }

    /**
     * Legt den Wert der wrapT-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesSamplerWrap }
     *     
     */
    public void setWrapT(GlesSamplerWrap value) {
        this.wrapT = value;
    }

    public boolean isSetWrapT() {
        return (this.wrapT!= null);
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
     * 
     * 					The extra element may appear any number of times.
     * 					OpenGL ES extensions may be used here.
     * 					Gets the value of the extra property.
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

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

}
