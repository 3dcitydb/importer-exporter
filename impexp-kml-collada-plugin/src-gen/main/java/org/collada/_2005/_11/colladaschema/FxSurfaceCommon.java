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
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 			The fx_surface_common type is used to declare a resource that can be used both as the source for texture samples and as the target of a rendering pass.
 * 			
 * 
 * <p>Java-Klasse für fx_surface_common complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fx_surface_common">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_init_common" minOccurs="0"/>
 *         &lt;element name="format" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="format_hint" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_format_hint_common" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="size" type="{http://www.collada.org/2005/11/COLLADASchema}int3"/>
 *           &lt;element name="viewport_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}float2"/>
 *         &lt;/choice>
 *         &lt;element name="mip_levels" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" minOccurs="0"/>
 *         &lt;element name="mipmap_generate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_type_enum" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fx_surface_common", propOrder = {
    "initAsNull",
    "initAsTarget",
    "initCube",
    "initVolume",
    "initPlanar",
    "initFrom",
    "format",
    "formatHint",
    "size",
    "viewportRatio",
    "mipLevels",
    "mipmapGenerate",
    "extra"
})
@XmlSeeAlso({
    GlslSurfaceType.class,
    CgSurfaceType.class
})
public class FxSurfaceCommon {

    @XmlElement(name = "init_as_null")
    protected Object initAsNull;
    @XmlElement(name = "init_as_target")
    protected Object initAsTarget;
    @XmlElement(name = "init_cube")
    protected FxSurfaceInitCubeCommon initCube;
    @XmlElement(name = "init_volume")
    protected FxSurfaceInitVolumeCommon initVolume;
    @XmlElement(name = "init_planar")
    protected FxSurfaceInitPlanarCommon initPlanar;
    @XmlElement(name = "init_from")
    protected List<FxSurfaceInitFromCommon> initFrom;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String format;
    @XmlElement(name = "format_hint")
    protected FxSurfaceFormatHintCommon formatHint;
    @XmlList
    @XmlElement(type = Long.class, defaultValue = "0 0 0")
    protected List<Long> size;
    @XmlList
    @XmlElement(name = "viewport_ratio", type = Double.class, defaultValue = "1 1")
    protected List<Double> viewportRatio;
    @XmlElement(name = "mip_levels", defaultValue = "0")
    @XmlSchemaType(name = "unsignedInt")
    protected Long mipLevels;
    @XmlElement(name = "mipmap_generate")
    protected Boolean mipmapGenerate;
    protected List<Extra> extra;
    @XmlAttribute(name = "type", required = true)
    protected String type;

    /**
     * Ruft den Wert der initAsNull-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getInitAsNull() {
        return initAsNull;
    }

    /**
     * Legt den Wert der initAsNull-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setInitAsNull(Object value) {
        this.initAsNull = value;
    }

    public boolean isSetInitAsNull() {
        return (this.initAsNull!= null);
    }

    /**
     * Ruft den Wert der initAsTarget-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getInitAsTarget() {
        return initAsTarget;
    }

    /**
     * Legt den Wert der initAsTarget-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setInitAsTarget(Object value) {
        this.initAsTarget = value;
    }

    public boolean isSetInitAsTarget() {
        return (this.initAsTarget!= null);
    }

    /**
     * Ruft den Wert der initCube-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceInitCubeCommon }
     *     
     */
    public FxSurfaceInitCubeCommon getInitCube() {
        return initCube;
    }

    /**
     * Legt den Wert der initCube-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceInitCubeCommon }
     *     
     */
    public void setInitCube(FxSurfaceInitCubeCommon value) {
        this.initCube = value;
    }

    public boolean isSetInitCube() {
        return (this.initCube!= null);
    }

    /**
     * Ruft den Wert der initVolume-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceInitVolumeCommon }
     *     
     */
    public FxSurfaceInitVolumeCommon getInitVolume() {
        return initVolume;
    }

    /**
     * Legt den Wert der initVolume-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceInitVolumeCommon }
     *     
     */
    public void setInitVolume(FxSurfaceInitVolumeCommon value) {
        this.initVolume = value;
    }

    public boolean isSetInitVolume() {
        return (this.initVolume!= null);
    }

    /**
     * Ruft den Wert der initPlanar-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceInitPlanarCommon }
     *     
     */
    public FxSurfaceInitPlanarCommon getInitPlanar() {
        return initPlanar;
    }

    /**
     * Legt den Wert der initPlanar-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceInitPlanarCommon }
     *     
     */
    public void setInitPlanar(FxSurfaceInitPlanarCommon value) {
        this.initPlanar = value;
    }

    public boolean isSetInitPlanar() {
        return (this.initPlanar!= null);
    }

    /**
     * Gets the value of the initFrom property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the initFrom property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInitFrom().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FxSurfaceInitFromCommon }
     * 
     * 
     */
    public List<FxSurfaceInitFromCommon> getInitFrom() {
        if (initFrom == null) {
            initFrom = new ArrayList<FxSurfaceInitFromCommon>();
        }
        return this.initFrom;
    }

    public boolean isSetInitFrom() {
        return ((this.initFrom!= null)&&(!this.initFrom.isEmpty()));
    }

    public void unsetInitFrom() {
        this.initFrom = null;
    }

    /**
     * Ruft den Wert der format-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormat() {
        return format;
    }

    /**
     * Legt den Wert der format-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormat(String value) {
        this.format = value;
    }

    public boolean isSetFormat() {
        return (this.format!= null);
    }

    /**
     * Ruft den Wert der formatHint-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceFormatHintCommon }
     *     
     */
    public FxSurfaceFormatHintCommon getFormatHint() {
        return formatHint;
    }

    /**
     * Legt den Wert der formatHint-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceFormatHintCommon }
     *     
     */
    public void setFormatHint(FxSurfaceFormatHintCommon value) {
        this.formatHint = value;
    }

    public boolean isSetFormatHint() {
        return (this.formatHint!= null);
    }

    /**
     * Gets the value of the size property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the size property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSize().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getSize() {
        if (size == null) {
            size = new ArrayList<Long>();
        }
        return this.size;
    }

    public boolean isSetSize() {
        return ((this.size!= null)&&(!this.size.isEmpty()));
    }

    public void unsetSize() {
        this.size = null;
    }

    /**
     * Gets the value of the viewportRatio property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the viewportRatio property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getViewportRatio().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getViewportRatio() {
        if (viewportRatio == null) {
            viewportRatio = new ArrayList<Double>();
        }
        return this.viewportRatio;
    }

    public boolean isSetViewportRatio() {
        return ((this.viewportRatio!= null)&&(!this.viewportRatio.isEmpty()));
    }

    public void unsetViewportRatio() {
        this.viewportRatio = null;
    }

    /**
     * Ruft den Wert der mipLevels-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getMipLevels() {
        return mipLevels;
    }

    /**
     * Legt den Wert der mipLevels-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMipLevels(Long value) {
        this.mipLevels = value;
    }

    public boolean isSetMipLevels() {
        return (this.mipLevels!= null);
    }

    /**
     * Ruft den Wert der mipmapGenerate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMipmapGenerate() {
        return mipmapGenerate;
    }

    /**
     * Legt den Wert der mipmapGenerate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMipmapGenerate(Boolean value) {
        this.mipmapGenerate = value;
    }

    public boolean isSetMipmapGenerate() {
        return (this.mipmapGenerate!= null);
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

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    public boolean isSetType() {
        return (this.type!= null);
    }

    public void setInitFrom(List<FxSurfaceInitFromCommon> value) {
        this.initFrom = value;
    }

    public void setSize(List<Long> value) {
        this.size = value;
    }

    public void setViewportRatio(List<Double> value) {
        this.viewportRatio = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

}
