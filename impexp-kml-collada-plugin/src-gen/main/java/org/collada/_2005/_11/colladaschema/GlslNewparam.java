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
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für glsl_newparam complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="glsl_newparam"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="semantic" type="{http://www.w3.org/2001/XMLSchema}NCName" minOccurs="0"/&gt;
 *         &lt;element name="modifier" type="{http://www.collada.org/2005/11/COLLADASchema}fx_modifier_enum_common" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}glsl_param_type"/&gt;
 *           &lt;element name="array" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_newarray_type"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="sid" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_identifier" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "glsl_newparam", propOrder = {
    "annotate",
    "semantic",
    "modifier",
    "bool",
    "bool2",
    "bool3",
    "bool4",
    "_float",
    "float2",
    "float3",
    "float4",
    "float2X2",
    "float3X3",
    "float4X4",
    "_int",
    "int2",
    "int3",
    "int4",
    "surface",
    "sampler1D",
    "sampler2D",
    "sampler3D",
    "samplerCUBE",
    "samplerRECT",
    "samplerDEPTH",
    "_enum",
    "array"
})
public class GlslNewparam {

    protected List<FxAnnotateCommon> annotate;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String semantic;
    @XmlSchemaType(name = "NMTOKEN")
    protected FxModifierEnumCommon modifier;
    protected Boolean bool;
    @XmlList
    @XmlElement(type = Boolean.class)
    protected List<Boolean> bool2;
    @XmlList
    @XmlElement(type = Boolean.class)
    protected List<Boolean> bool3;
    @XmlList
    @XmlElement(type = Boolean.class)
    protected List<Boolean> bool4;
    @XmlElement(name = "float")
    protected Float _float;
    @XmlList
    @XmlElement(type = Float.class)
    protected List<Float> float2;
    @XmlList
    @XmlElement(type = Float.class)
    protected List<Float> float3;
    @XmlList
    @XmlElement(type = Float.class)
    protected List<Float> float4;
    @XmlList
    @XmlElement(name = "float2x2", type = Float.class)
    protected List<Float> float2X2;
    @XmlList
    @XmlElement(name = "float3x3", type = Float.class)
    protected List<Float> float3X3;
    @XmlList
    @XmlElement(name = "float4x4", type = Float.class)
    protected List<Float> float4X4;
    @XmlElement(name = "int")
    protected Integer _int;
    @XmlList
    @XmlElement(type = Integer.class)
    protected List<Integer> int2;
    @XmlList
    @XmlElement(type = Integer.class)
    protected List<Integer> int3;
    @XmlList
    @XmlElement(type = Integer.class)
    protected List<Integer> int4;
    protected GlslSurfaceType surface;
    protected GlSampler1D sampler1D;
    protected GlSampler2D sampler2D;
    protected GlSampler3D sampler3D;
    protected GlSamplerCUBE samplerCUBE;
    protected GlSamplerRECT samplerRECT;
    protected GlSamplerDEPTH samplerDEPTH;
    @XmlElement(name = "enum")
    protected String _enum;
    protected GlslNewarrayType array;
    @XmlAttribute(name = "sid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String sid;

    /**
     * Gets the value of the annotate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the annotate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnnotate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FxAnnotateCommon }
     * 
     * 
     */
    public List<FxAnnotateCommon> getAnnotate() {
        if (annotate == null) {
            annotate = new ArrayList<FxAnnotateCommon>();
        }
        return this.annotate;
    }

    public boolean isSetAnnotate() {
        return ((this.annotate!= null)&&(!this.annotate.isEmpty()));
    }

    public void unsetAnnotate() {
        this.annotate = null;
    }

    /**
     * Ruft den Wert der semantic-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSemantic() {
        return semantic;
    }

    /**
     * Legt den Wert der semantic-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSemantic(String value) {
        this.semantic = value;
    }

    public boolean isSetSemantic() {
        return (this.semantic!= null);
    }

    /**
     * Ruft den Wert der modifier-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxModifierEnumCommon }
     *     
     */
    public FxModifierEnumCommon getModifier() {
        return modifier;
    }

    /**
     * Legt den Wert der modifier-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxModifierEnumCommon }
     *     
     */
    public void setModifier(FxModifierEnumCommon value) {
        this.modifier = value;
    }

    public boolean isSetModifier() {
        return (this.modifier!= null);
    }

    /**
     * Ruft den Wert der bool-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBool() {
        return bool;
    }

    /**
     * Legt den Wert der bool-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBool(Boolean value) {
        this.bool = value;
    }

    public boolean isSetBool() {
        return (this.bool!= null);
    }

    /**
     * Gets the value of the bool2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool2() {
        if (bool2 == null) {
            bool2 = new ArrayList<Boolean>();
        }
        return this.bool2;
    }

    public boolean isSetBool2() {
        return ((this.bool2 != null)&&(!this.bool2 .isEmpty()));
    }

    public void unsetBool2() {
        this.bool2 = null;
    }

    /**
     * Gets the value of the bool3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool3() {
        if (bool3 == null) {
            bool3 = new ArrayList<Boolean>();
        }
        return this.bool3;
    }

    public boolean isSetBool3() {
        return ((this.bool3 != null)&&(!this.bool3 .isEmpty()));
    }

    public void unsetBool3() {
        this.bool3 = null;
    }

    /**
     * Gets the value of the bool4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool4() {
        if (bool4 == null) {
            bool4 = new ArrayList<Boolean>();
        }
        return this.bool4;
    }

    public boolean isSetBool4() {
        return ((this.bool4 != null)&&(!this.bool4 .isEmpty()));
    }

    public void unsetBool4() {
        this.bool4 = null;
    }

    /**
     * Ruft den Wert der float-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getFloat() {
        return _float;
    }

    /**
     * Legt den Wert der float-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setFloat(Float value) {
        this._float = value;
    }

    public boolean isSetFloat() {
        return (this._float!= null);
    }

    /**
     * Gets the value of the float2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat2() {
        if (float2 == null) {
            float2 = new ArrayList<Float>();
        }
        return this.float2;
    }

    public boolean isSetFloat2() {
        return ((this.float2 != null)&&(!this.float2 .isEmpty()));
    }

    public void unsetFloat2() {
        this.float2 = null;
    }

    /**
     * Gets the value of the float3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat3() {
        if (float3 == null) {
            float3 = new ArrayList<Float>();
        }
        return this.float3;
    }

    public boolean isSetFloat3() {
        return ((this.float3 != null)&&(!this.float3 .isEmpty()));
    }

    public void unsetFloat3() {
        this.float3 = null;
    }

    /**
     * Gets the value of the float4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat4() {
        if (float4 == null) {
            float4 = new ArrayList<Float>();
        }
        return this.float4;
    }

    public boolean isSetFloat4() {
        return ((this.float4 != null)&&(!this.float4 .isEmpty()));
    }

    public void unsetFloat4() {
        this.float4 = null;
    }

    /**
     * Gets the value of the float2X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float2X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat2X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat2X2() {
        if (float2X2 == null) {
            float2X2 = new ArrayList<Float>();
        }
        return this.float2X2;
    }

    public boolean isSetFloat2X2() {
        return ((this.float2X2 != null)&&(!this.float2X2 .isEmpty()));
    }

    public void unsetFloat2X2() {
        this.float2X2 = null;
    }

    /**
     * Gets the value of the float3X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float3X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat3X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat3X3() {
        if (float3X3 == null) {
            float3X3 = new ArrayList<Float>();
        }
        return this.float3X3;
    }

    public boolean isSetFloat3X3() {
        return ((this.float3X3 != null)&&(!this.float3X3 .isEmpty()));
    }

    public void unsetFloat3X3() {
        this.float3X3 = null;
    }

    /**
     * Gets the value of the float4X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float4X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat4X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat4X4() {
        if (float4X4 == null) {
            float4X4 = new ArrayList<Float>();
        }
        return this.float4X4;
    }

    public boolean isSetFloat4X4() {
        return ((this.float4X4 != null)&&(!this.float4X4 .isEmpty()));
    }

    public void unsetFloat4X4() {
        this.float4X4 = null;
    }

    /**
     * Ruft den Wert der int-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getInt() {
        return _int;
    }

    /**
     * Legt den Wert der int-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setInt(Integer value) {
        this._int = value;
    }

    public boolean isSetInt() {
        return (this._int!= null);
    }

    /**
     * Gets the value of the int2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt2() {
        if (int2 == null) {
            int2 = new ArrayList<Integer>();
        }
        return this.int2;
    }

    public boolean isSetInt2() {
        return ((this.int2 != null)&&(!this.int2 .isEmpty()));
    }

    public void unsetInt2() {
        this.int2 = null;
    }

    /**
     * Gets the value of the int3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt3() {
        if (int3 == null) {
            int3 = new ArrayList<Integer>();
        }
        return this.int3;
    }

    public boolean isSetInt3() {
        return ((this.int3 != null)&&(!this.int3 .isEmpty()));
    }

    public void unsetInt3() {
        this.int3 = null;
    }

    /**
     * Gets the value of the int4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt4() {
        if (int4 == null) {
            int4 = new ArrayList<Integer>();
        }
        return this.int4;
    }

    public boolean isSetInt4() {
        return ((this.int4 != null)&&(!this.int4 .isEmpty()));
    }

    public void unsetInt4() {
        this.int4 = null;
    }

    /**
     * Ruft den Wert der surface-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlslSurfaceType }
     *     
     */
    public GlslSurfaceType getSurface() {
        return surface;
    }

    /**
     * Legt den Wert der surface-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlslSurfaceType }
     *     
     */
    public void setSurface(GlslSurfaceType value) {
        this.surface = value;
    }

    public boolean isSetSurface() {
        return (this.surface!= null);
    }

    /**
     * Ruft den Wert der sampler1D-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlSampler1D }
     *     
     */
    public GlSampler1D getSampler1D() {
        return sampler1D;
    }

    /**
     * Legt den Wert der sampler1D-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlSampler1D }
     *     
     */
    public void setSampler1D(GlSampler1D value) {
        this.sampler1D = value;
    }

    public boolean isSetSampler1D() {
        return (this.sampler1D!= null);
    }

    /**
     * Ruft den Wert der sampler2D-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlSampler2D }
     *     
     */
    public GlSampler2D getSampler2D() {
        return sampler2D;
    }

    /**
     * Legt den Wert der sampler2D-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlSampler2D }
     *     
     */
    public void setSampler2D(GlSampler2D value) {
        this.sampler2D = value;
    }

    public boolean isSetSampler2D() {
        return (this.sampler2D!= null);
    }

    /**
     * Ruft den Wert der sampler3D-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlSampler3D }
     *     
     */
    public GlSampler3D getSampler3D() {
        return sampler3D;
    }

    /**
     * Legt den Wert der sampler3D-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlSampler3D }
     *     
     */
    public void setSampler3D(GlSampler3D value) {
        this.sampler3D = value;
    }

    public boolean isSetSampler3D() {
        return (this.sampler3D!= null);
    }

    /**
     * Ruft den Wert der samplerCUBE-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlSamplerCUBE }
     *     
     */
    public GlSamplerCUBE getSamplerCUBE() {
        return samplerCUBE;
    }

    /**
     * Legt den Wert der samplerCUBE-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlSamplerCUBE }
     *     
     */
    public void setSamplerCUBE(GlSamplerCUBE value) {
        this.samplerCUBE = value;
    }

    public boolean isSetSamplerCUBE() {
        return (this.samplerCUBE!= null);
    }

    /**
     * Ruft den Wert der samplerRECT-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlSamplerRECT }
     *     
     */
    public GlSamplerRECT getSamplerRECT() {
        return samplerRECT;
    }

    /**
     * Legt den Wert der samplerRECT-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlSamplerRECT }
     *     
     */
    public void setSamplerRECT(GlSamplerRECT value) {
        this.samplerRECT = value;
    }

    public boolean isSetSamplerRECT() {
        return (this.samplerRECT!= null);
    }

    /**
     * Ruft den Wert der samplerDEPTH-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlSamplerDEPTH }
     *     
     */
    public GlSamplerDEPTH getSamplerDEPTH() {
        return samplerDEPTH;
    }

    /**
     * Legt den Wert der samplerDEPTH-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlSamplerDEPTH }
     *     
     */
    public void setSamplerDEPTH(GlSamplerDEPTH value) {
        this.samplerDEPTH = value;
    }

    public boolean isSetSamplerDEPTH() {
        return (this.samplerDEPTH!= null);
    }

    /**
     * Ruft den Wert der enum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnum() {
        return _enum;
    }

    /**
     * Legt den Wert der enum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnum(String value) {
        this._enum = value;
    }

    public boolean isSetEnum() {
        return (this._enum!= null);
    }

    /**
     * Ruft den Wert der array-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GlslNewarrayType }
     *     
     */
    public GlslNewarrayType getArray() {
        return array;
    }

    /**
     * Legt den Wert der array-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GlslNewarrayType }
     *     
     */
    public void setArray(GlslNewarrayType value) {
        this.array = value;
    }

    public boolean isSetArray() {
        return (this.array!= null);
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

    public void setAnnotate(List<FxAnnotateCommon> value) {
        this.annotate = value;
    }

    public void setBool2(List<Boolean> value) {
        this.bool2 = value;
    }

    public void setBool3(List<Boolean> value) {
        this.bool3 = value;
    }

    public void setBool4(List<Boolean> value) {
        this.bool4 = value;
    }

    public void setFloat2(List<Float> value) {
        this.float2 = value;
    }

    public void setFloat3(List<Float> value) {
        this.float3 = value;
    }

    public void setFloat4(List<Float> value) {
        this.float4 = value;
    }

    public void setFloat2X2(List<Float> value) {
        this.float2X2 = value;
    }

    public void setFloat3X3(List<Float> value) {
        this.float3X3 = value;
    }

    public void setFloat4X4(List<Float> value) {
        this.float4X4 = value;
    }

    public void setInt2(List<Integer> value) {
        this.int2 = value;
    }

    public void setInt3(List<Integer> value) {
        this.int3 = value;
    }

    public void setInt4(List<Integer> value) {
        this.int4 = value;
    }

}
