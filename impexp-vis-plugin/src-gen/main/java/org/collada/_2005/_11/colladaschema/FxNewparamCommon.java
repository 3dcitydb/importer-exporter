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
 * 
 * 			This element creates a new, named param object in the FX Runtime, assigns it a type, an initial value, and additional attributes at declaration time.
 * 			
 * 
 * <p>Java-Klasse für fx_newparam_common complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fx_newparam_common"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="semantic" type="{http://www.w3.org/2001/XMLSchema}NCName" minOccurs="0"/&gt;
 *         &lt;element name="modifier" type="{http://www.collada.org/2005/11/COLLADASchema}fx_modifier_enum_common" minOccurs="0"/&gt;
 *         &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}fx_basic_type_common"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="sid" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fx_newparam_common", propOrder = {
    "annotate",
    "semantic",
    "modifier",
    "bool",
    "bool2",
    "bool3",
    "bool4",
    "_int",
    "int2",
    "int3",
    "int4",
    "_float",
    "float2",
    "float3",
    "float4",
    "float1X1",
    "float1X2",
    "float1X3",
    "float1X4",
    "float2X1",
    "float2X2",
    "float2X3",
    "float2X4",
    "float3X1",
    "float3X2",
    "float3X3",
    "float3X4",
    "float4X1",
    "float4X2",
    "float4X3",
    "float4X4",
    "surface",
    "sampler1D",
    "sampler2D",
    "sampler3D",
    "samplerCUBE",
    "samplerRECT",
    "samplerDEPTH",
    "_enum"
})
public class FxNewparamCommon {

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
    @XmlElement(name = "int")
    protected Long _int;
    @XmlList
    @XmlElement(type = Long.class)
    protected List<Long> int2;
    @XmlList
    @XmlElement(type = Long.class)
    protected List<Long> int3;
    @XmlList
    @XmlElement(type = Long.class)
    protected List<Long> int4;
    @XmlElement(name = "float")
    protected Double _float;
    @XmlList
    @XmlElement(type = Double.class)
    protected List<Double> float2;
    @XmlList
    @XmlElement(type = Double.class)
    protected List<Double> float3;
    @XmlList
    @XmlElement(type = Double.class)
    protected List<Double> float4;
    @XmlElement(name = "float1x1")
    protected Double float1X1;
    @XmlList
    @XmlElement(name = "float1x2", type = Double.class)
    protected List<Double> float1X2;
    @XmlList
    @XmlElement(name = "float1x3", type = Double.class)
    protected List<Double> float1X3;
    @XmlList
    @XmlElement(name = "float1x4", type = Double.class)
    protected List<Double> float1X4;
    @XmlList
    @XmlElement(name = "float2x1", type = Double.class)
    protected List<Double> float2X1;
    @XmlList
    @XmlElement(name = "float2x2", type = Double.class)
    protected List<Double> float2X2;
    @XmlList
    @XmlElement(name = "float2x3", type = Double.class)
    protected List<Double> float2X3;
    @XmlList
    @XmlElement(name = "float2x4", type = Double.class)
    protected List<Double> float2X4;
    @XmlList
    @XmlElement(name = "float3x1", type = Double.class)
    protected List<Double> float3X1;
    @XmlList
    @XmlElement(name = "float3x2", type = Double.class)
    protected List<Double> float3X2;
    @XmlList
    @XmlElement(name = "float3x3", type = Double.class)
    protected List<Double> float3X3;
    @XmlList
    @XmlElement(name = "float3x4", type = Double.class)
    protected List<Double> float3X4;
    @XmlList
    @XmlElement(name = "float4x1", type = Double.class)
    protected List<Double> float4X1;
    @XmlList
    @XmlElement(name = "float4x2", type = Double.class)
    protected List<Double> float4X2;
    @XmlList
    @XmlElement(name = "float4x3", type = Double.class)
    protected List<Double> float4X3;
    @XmlList
    @XmlElement(name = "float4x4", type = Double.class)
    protected List<Double> float4X4;
    protected FxSurfaceCommon surface;
    protected FxSampler1DCommon sampler1D;
    protected FxSampler2DCommon sampler2D;
    protected FxSampler3DCommon sampler3D;
    protected FxSamplerCUBECommon samplerCUBE;
    protected FxSamplerRECTCommon samplerRECT;
    protected FxSamplerDEPTHCommon samplerDEPTH;
    @XmlElement(name = "enum")
    protected String _enum;
    @XmlAttribute(name = "sid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
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
     * Ruft den Wert der int-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getInt() {
        return _int;
    }

    /**
     * Legt den Wert der int-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setInt(Long value) {
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
     * {@link Long }
     * 
     * 
     */
    public List<Long> getInt2() {
        if (int2 == null) {
            int2 = new ArrayList<Long>();
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
     * {@link Long }
     * 
     * 
     */
    public List<Long> getInt3() {
        if (int3 == null) {
            int3 = new ArrayList<Long>();
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
     * {@link Long }
     * 
     * 
     */
    public List<Long> getInt4() {
        if (int4 == null) {
            int4 = new ArrayList<Long>();
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
     * Ruft den Wert der float-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getFloat() {
        return _float;
    }

    /**
     * Legt den Wert der float-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setFloat(Double value) {
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
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat2() {
        if (float2 == null) {
            float2 = new ArrayList<Double>();
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
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat3() {
        if (float3 == null) {
            float3 = new ArrayList<Double>();
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
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat4() {
        if (float4 == null) {
            float4 = new ArrayList<Double>();
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
     * Ruft den Wert der float1X1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getFloat1X1() {
        return float1X1;
    }

    /**
     * Legt den Wert der float1X1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setFloat1X1(Double value) {
        this.float1X1 = value;
    }

    public boolean isSetFloat1X1() {
        return (this.float1X1 != null);
    }

    /**
     * Gets the value of the float1X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float1X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat1X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat1X2() {
        if (float1X2 == null) {
            float1X2 = new ArrayList<Double>();
        }
        return this.float1X2;
    }

    public boolean isSetFloat1X2() {
        return ((this.float1X2 != null)&&(!this.float1X2 .isEmpty()));
    }

    public void unsetFloat1X2() {
        this.float1X2 = null;
    }

    /**
     * Gets the value of the float1X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float1X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat1X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat1X3() {
        if (float1X3 == null) {
            float1X3 = new ArrayList<Double>();
        }
        return this.float1X3;
    }

    public boolean isSetFloat1X3() {
        return ((this.float1X3 != null)&&(!this.float1X3 .isEmpty()));
    }

    public void unsetFloat1X3() {
        this.float1X3 = null;
    }

    /**
     * Gets the value of the float1X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float1X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat1X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat1X4() {
        if (float1X4 == null) {
            float1X4 = new ArrayList<Double>();
        }
        return this.float1X4;
    }

    public boolean isSetFloat1X4() {
        return ((this.float1X4 != null)&&(!this.float1X4 .isEmpty()));
    }

    public void unsetFloat1X4() {
        this.float1X4 = null;
    }

    /**
     * Gets the value of the float2X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float2X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat2X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat2X1() {
        if (float2X1 == null) {
            float2X1 = new ArrayList<Double>();
        }
        return this.float2X1;
    }

    public boolean isSetFloat2X1() {
        return ((this.float2X1 != null)&&(!this.float2X1 .isEmpty()));
    }

    public void unsetFloat2X1() {
        this.float2X1 = null;
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
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat2X2() {
        if (float2X2 == null) {
            float2X2 = new ArrayList<Double>();
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
     * Gets the value of the float2X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float2X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat2X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat2X3() {
        if (float2X3 == null) {
            float2X3 = new ArrayList<Double>();
        }
        return this.float2X3;
    }

    public boolean isSetFloat2X3() {
        return ((this.float2X3 != null)&&(!this.float2X3 .isEmpty()));
    }

    public void unsetFloat2X3() {
        this.float2X3 = null;
    }

    /**
     * Gets the value of the float2X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float2X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat2X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat2X4() {
        if (float2X4 == null) {
            float2X4 = new ArrayList<Double>();
        }
        return this.float2X4;
    }

    public boolean isSetFloat2X4() {
        return ((this.float2X4 != null)&&(!this.float2X4 .isEmpty()));
    }

    public void unsetFloat2X4() {
        this.float2X4 = null;
    }

    /**
     * Gets the value of the float3X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float3X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat3X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat3X1() {
        if (float3X1 == null) {
            float3X1 = new ArrayList<Double>();
        }
        return this.float3X1;
    }

    public boolean isSetFloat3X1() {
        return ((this.float3X1 != null)&&(!this.float3X1 .isEmpty()));
    }

    public void unsetFloat3X1() {
        this.float3X1 = null;
    }

    /**
     * Gets the value of the float3X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float3X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat3X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat3X2() {
        if (float3X2 == null) {
            float3X2 = new ArrayList<Double>();
        }
        return this.float3X2;
    }

    public boolean isSetFloat3X2() {
        return ((this.float3X2 != null)&&(!this.float3X2 .isEmpty()));
    }

    public void unsetFloat3X2() {
        this.float3X2 = null;
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
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat3X3() {
        if (float3X3 == null) {
            float3X3 = new ArrayList<Double>();
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
     * Gets the value of the float3X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float3X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat3X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat3X4() {
        if (float3X4 == null) {
            float3X4 = new ArrayList<Double>();
        }
        return this.float3X4;
    }

    public boolean isSetFloat3X4() {
        return ((this.float3X4 != null)&&(!this.float3X4 .isEmpty()));
    }

    public void unsetFloat3X4() {
        this.float3X4 = null;
    }

    /**
     * Gets the value of the float4X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float4X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat4X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat4X1() {
        if (float4X1 == null) {
            float4X1 = new ArrayList<Double>();
        }
        return this.float4X1;
    }

    public boolean isSetFloat4X1() {
        return ((this.float4X1 != null)&&(!this.float4X1 .isEmpty()));
    }

    public void unsetFloat4X1() {
        this.float4X1 = null;
    }

    /**
     * Gets the value of the float4X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float4X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat4X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat4X2() {
        if (float4X2 == null) {
            float4X2 = new ArrayList<Double>();
        }
        return this.float4X2;
    }

    public boolean isSetFloat4X2() {
        return ((this.float4X2 != null)&&(!this.float4X2 .isEmpty()));
    }

    public void unsetFloat4X2() {
        this.float4X2 = null;
    }

    /**
     * Gets the value of the float4X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float4X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat4X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat4X3() {
        if (float4X3 == null) {
            float4X3 = new ArrayList<Double>();
        }
        return this.float4X3;
    }

    public boolean isSetFloat4X3() {
        return ((this.float4X3 != null)&&(!this.float4X3 .isEmpty()));
    }

    public void unsetFloat4X3() {
        this.float4X3 = null;
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
     * {@link Double }
     * 
     * 
     */
    public List<Double> getFloat4X4() {
        if (float4X4 == null) {
            float4X4 = new ArrayList<Double>();
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
     * Ruft den Wert der surface-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceCommon }
     *     
     */
    public FxSurfaceCommon getSurface() {
        return surface;
    }

    /**
     * Legt den Wert der surface-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceCommon }
     *     
     */
    public void setSurface(FxSurfaceCommon value) {
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
     *     {@link FxSampler1DCommon }
     *     
     */
    public FxSampler1DCommon getSampler1D() {
        return sampler1D;
    }

    /**
     * Legt den Wert der sampler1D-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSampler1DCommon }
     *     
     */
    public void setSampler1D(FxSampler1DCommon value) {
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
     *     {@link FxSampler2DCommon }
     *     
     */
    public FxSampler2DCommon getSampler2D() {
        return sampler2D;
    }

    /**
     * Legt den Wert der sampler2D-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSampler2DCommon }
     *     
     */
    public void setSampler2D(FxSampler2DCommon value) {
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
     *     {@link FxSampler3DCommon }
     *     
     */
    public FxSampler3DCommon getSampler3D() {
        return sampler3D;
    }

    /**
     * Legt den Wert der sampler3D-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSampler3DCommon }
     *     
     */
    public void setSampler3D(FxSampler3DCommon value) {
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
     *     {@link FxSamplerCUBECommon }
     *     
     */
    public FxSamplerCUBECommon getSamplerCUBE() {
        return samplerCUBE;
    }

    /**
     * Legt den Wert der samplerCUBE-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerCUBECommon }
     *     
     */
    public void setSamplerCUBE(FxSamplerCUBECommon value) {
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
     *     {@link FxSamplerRECTCommon }
     *     
     */
    public FxSamplerRECTCommon getSamplerRECT() {
        return samplerRECT;
    }

    /**
     * Legt den Wert der samplerRECT-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerRECTCommon }
     *     
     */
    public void setSamplerRECT(FxSamplerRECTCommon value) {
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
     *     {@link FxSamplerDEPTHCommon }
     *     
     */
    public FxSamplerDEPTHCommon getSamplerDEPTH() {
        return samplerDEPTH;
    }

    /**
     * Legt den Wert der samplerDEPTH-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSamplerDEPTHCommon }
     *     
     */
    public void setSamplerDEPTH(FxSamplerDEPTHCommon value) {
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

    public void setInt2(List<Long> value) {
        this.int2 = value;
    }

    public void setInt3(List<Long> value) {
        this.int3 = value;
    }

    public void setInt4(List<Long> value) {
        this.int4 = value;
    }

    public void setFloat2(List<Double> value) {
        this.float2 = value;
    }

    public void setFloat3(List<Double> value) {
        this.float3 = value;
    }

    public void setFloat4(List<Double> value) {
        this.float4 = value;
    }

    public void setFloat1X2(List<Double> value) {
        this.float1X2 = value;
    }

    public void setFloat1X3(List<Double> value) {
        this.float1X3 = value;
    }

    public void setFloat1X4(List<Double> value) {
        this.float1X4 = value;
    }

    public void setFloat2X1(List<Double> value) {
        this.float2X1 = value;
    }

    public void setFloat2X2(List<Double> value) {
        this.float2X2 = value;
    }

    public void setFloat2X3(List<Double> value) {
        this.float2X3 = value;
    }

    public void setFloat2X4(List<Double> value) {
        this.float2X4 = value;
    }

    public void setFloat3X1(List<Double> value) {
        this.float3X1 = value;
    }

    public void setFloat3X2(List<Double> value) {
        this.float3X2 = value;
    }

    public void setFloat3X3(List<Double> value) {
        this.float3X3 = value;
    }

    public void setFloat3X4(List<Double> value) {
        this.float3X4 = value;
    }

    public void setFloat4X1(List<Double> value) {
        this.float4X1 = value;
    }

    public void setFloat4X2(List<Double> value) {
        this.float4X2 = value;
    }

    public void setFloat4X3(List<Double> value) {
        this.float4X3 = value;
    }

    public void setFloat4X4(List<Double> value) {
        this.float4X4 = value;
    }

}
