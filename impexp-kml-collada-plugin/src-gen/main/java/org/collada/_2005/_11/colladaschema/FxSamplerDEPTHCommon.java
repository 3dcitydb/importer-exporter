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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 			A texture sampler for depth maps.
 * 			
 * 
 * <p>Java-Klasse für fx_samplerDEPTH_common complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fx_samplerDEPTH_common">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}NCName"/>
 *         &lt;element name="wrap_s" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_wrap_common" minOccurs="0"/>
 *         &lt;element name="wrap_t" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_wrap_common" minOccurs="0"/>
 *         &lt;element name="minfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/>
 *         &lt;element name="magfilter" type="{http://www.collada.org/2005/11/COLLADASchema}fx_sampler_filter_common" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fx_samplerDEPTH_common", propOrder = {
    "source",
    "wrapS",
    "wrapT",
    "minfilter",
    "magfilter",
    "extra"
})
@XmlSeeAlso({
    CgSamplerDEPTH.class,
    GlSamplerDEPTH.class
})
public class FxSamplerDEPTHCommon {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String source;
    @XmlElement(name = "wrap_s", defaultValue = "WRAP")
    protected FxSamplerWrapCommon wrapS;
    @XmlElement(name = "wrap_t", defaultValue = "WRAP")
    protected FxSamplerWrapCommon wrapT;
    @XmlElement(defaultValue = "NONE")
    protected FxSamplerFilterCommon minfilter;
    @XmlElement(defaultValue = "NONE")
    protected FxSamplerFilterCommon magfilter;
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

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

}
