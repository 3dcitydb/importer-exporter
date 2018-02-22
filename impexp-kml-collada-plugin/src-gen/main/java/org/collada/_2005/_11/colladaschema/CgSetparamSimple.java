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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für cg_setparam_simple complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="cg_setparam_simple">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}cg_param_type"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ref" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}cg_identifier" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cg_setparam_simple", propOrder = {
    "annotate",
    "bool",
    "bool1",
    "bool2",
    "bool3",
    "bool4",
    "bool1X1",
    "bool1X2",
    "bool1X3",
    "bool1X4",
    "bool2X1",
    "bool2X2",
    "bool2X3",
    "bool2X4",
    "bool3X1",
    "bool3X2",
    "bool3X3",
    "bool3X4",
    "bool4X1",
    "bool4X2",
    "bool4X3",
    "bool4X4",
    "_float",
    "float1",
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
    "_int",
    "int1",
    "int2",
    "int3",
    "int4",
    "int1X1",
    "int1X2",
    "int1X3",
    "int1X4",
    "int2X1",
    "int2X2",
    "int2X3",
    "int2X4",
    "int3X1",
    "int3X2",
    "int3X3",
    "int3X4",
    "int4X1",
    "int4X2",
    "int4X3",
    "int4X4",
    "half",
    "half1",
    "half2",
    "half3",
    "half4",
    "half1X1",
    "half1X2",
    "half1X3",
    "half1X4",
    "half2X1",
    "half2X2",
    "half2X3",
    "half2X4",
    "half3X1",
    "half3X2",
    "half3X3",
    "half3X4",
    "half4X1",
    "half4X2",
    "half4X3",
    "half4X4",
    "fixed",
    "fixed1",
    "fixed2",
    "fixed3",
    "fixed4",
    "fixed1X1",
    "fixed1X2",
    "fixed1X3",
    "fixed1X4",
    "fixed2X1",
    "fixed2X2",
    "fixed2X3",
    "fixed2X4",
    "fixed3X1",
    "fixed3X2",
    "fixed3X3",
    "fixed3X4",
    "fixed4X1",
    "fixed4X2",
    "fixed4X3",
    "fixed4X4",
    "surface",
    "sampler1D",
    "sampler2D",
    "sampler3D",
    "samplerRECT",
    "samplerCUBE",
    "samplerDEPTH",
    "string",
    "_enum"
})
public class CgSetparamSimple {

    protected List<FxAnnotateCommon> annotate;
    protected Boolean bool;
    protected Boolean bool1;
    @XmlList
    @XmlElement(type = Boolean.class)
    protected List<Boolean> bool2;
    @XmlList
    @XmlElement(type = Boolean.class)
    protected List<Boolean> bool3;
    @XmlList
    @XmlElement(type = Boolean.class)
    protected List<Boolean> bool4;
    @XmlList
    @XmlElement(name = "bool1x1", type = Boolean.class)
    protected List<Boolean> bool1X1;
    @XmlList
    @XmlElement(name = "bool1x2", type = Boolean.class)
    protected List<Boolean> bool1X2;
    @XmlList
    @XmlElement(name = "bool1x3", type = Boolean.class)
    protected List<Boolean> bool1X3;
    @XmlList
    @XmlElement(name = "bool1x4", type = Boolean.class)
    protected List<Boolean> bool1X4;
    @XmlList
    @XmlElement(name = "bool2x1", type = Boolean.class)
    protected List<Boolean> bool2X1;
    @XmlList
    @XmlElement(name = "bool2x2", type = Boolean.class)
    protected List<Boolean> bool2X2;
    @XmlList
    @XmlElement(name = "bool2x3", type = Boolean.class)
    protected List<Boolean> bool2X3;
    @XmlList
    @XmlElement(name = "bool2x4", type = Boolean.class)
    protected List<Boolean> bool2X4;
    @XmlList
    @XmlElement(name = "bool3x1", type = Boolean.class)
    protected List<Boolean> bool3X1;
    @XmlList
    @XmlElement(name = "bool3x2", type = Boolean.class)
    protected List<Boolean> bool3X2;
    @XmlList
    @XmlElement(name = "bool3x3", type = Boolean.class)
    protected List<Boolean> bool3X3;
    @XmlList
    @XmlElement(name = "bool3x4", type = Boolean.class)
    protected List<Boolean> bool3X4;
    @XmlList
    @XmlElement(name = "bool4x1", type = Boolean.class)
    protected List<Boolean> bool4X1;
    @XmlList
    @XmlElement(name = "bool4x2", type = Boolean.class)
    protected List<Boolean> bool4X2;
    @XmlList
    @XmlElement(name = "bool4x3", type = Boolean.class)
    protected List<Boolean> bool4X3;
    @XmlList
    @XmlElement(name = "bool4x4", type = Boolean.class)
    protected List<Boolean> bool4X4;
    @XmlElement(name = "float")
    protected Float _float;
    protected Float float1;
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
    @XmlElement(name = "float1x1", type = Float.class)
    protected List<Float> float1X1;
    @XmlList
    @XmlElement(name = "float1x2", type = Float.class)
    protected List<Float> float1X2;
    @XmlList
    @XmlElement(name = "float1x3", type = Float.class)
    protected List<Float> float1X3;
    @XmlList
    @XmlElement(name = "float1x4", type = Float.class)
    protected List<Float> float1X4;
    @XmlList
    @XmlElement(name = "float2x1", type = Float.class)
    protected List<Float> float2X1;
    @XmlList
    @XmlElement(name = "float2x2", type = Float.class)
    protected List<Float> float2X2;
    @XmlList
    @XmlElement(name = "float2x3", type = Float.class)
    protected List<Float> float2X3;
    @XmlList
    @XmlElement(name = "float2x4", type = Float.class)
    protected List<Float> float2X4;
    @XmlList
    @XmlElement(name = "float3x1", type = Float.class)
    protected List<Float> float3X1;
    @XmlList
    @XmlElement(name = "float3x2", type = Float.class)
    protected List<Float> float3X2;
    @XmlList
    @XmlElement(name = "float3x3", type = Float.class)
    protected List<Float> float3X3;
    @XmlList
    @XmlElement(name = "float3x4", type = Float.class)
    protected List<Float> float3X4;
    @XmlList
    @XmlElement(name = "float4x1", type = Float.class)
    protected List<Float> float4X1;
    @XmlList
    @XmlElement(name = "float4x2", type = Float.class)
    protected List<Float> float4X2;
    @XmlList
    @XmlElement(name = "float4x3", type = Float.class)
    protected List<Float> float4X3;
    @XmlList
    @XmlElement(name = "float4x4", type = Float.class)
    protected List<Float> float4X4;
    @XmlElement(name = "int")
    protected Integer _int;
    protected Integer int1;
    @XmlList
    @XmlElement(type = Integer.class)
    protected List<Integer> int2;
    @XmlList
    @XmlElement(type = Integer.class)
    protected List<Integer> int3;
    @XmlList
    @XmlElement(type = Integer.class)
    protected List<Integer> int4;
    @XmlList
    @XmlElement(name = "int1x1", type = Integer.class)
    protected List<Integer> int1X1;
    @XmlList
    @XmlElement(name = "int1x2", type = Integer.class)
    protected List<Integer> int1X2;
    @XmlList
    @XmlElement(name = "int1x3", type = Integer.class)
    protected List<Integer> int1X3;
    @XmlList
    @XmlElement(name = "int1x4", type = Integer.class)
    protected List<Integer> int1X4;
    @XmlList
    @XmlElement(name = "int2x1", type = Integer.class)
    protected List<Integer> int2X1;
    @XmlList
    @XmlElement(name = "int2x2", type = Integer.class)
    protected List<Integer> int2X2;
    @XmlList
    @XmlElement(name = "int2x3", type = Integer.class)
    protected List<Integer> int2X3;
    @XmlList
    @XmlElement(name = "int2x4", type = Integer.class)
    protected List<Integer> int2X4;
    @XmlList
    @XmlElement(name = "int3x1", type = Integer.class)
    protected List<Integer> int3X1;
    @XmlList
    @XmlElement(name = "int3x2", type = Integer.class)
    protected List<Integer> int3X2;
    @XmlList
    @XmlElement(name = "int3x3", type = Integer.class)
    protected List<Integer> int3X3;
    @XmlList
    @XmlElement(name = "int3x4", type = Integer.class)
    protected List<Integer> int3X4;
    @XmlList
    @XmlElement(name = "int4x1", type = Integer.class)
    protected List<Integer> int4X1;
    @XmlList
    @XmlElement(name = "int4x2", type = Integer.class)
    protected List<Integer> int4X2;
    @XmlList
    @XmlElement(name = "int4x3", type = Integer.class)
    protected List<Integer> int4X3;
    @XmlList
    @XmlElement(name = "int4x4", type = Integer.class)
    protected List<Integer> int4X4;
    protected Float half;
    protected Float half1;
    @XmlList
    @XmlElement(type = Float.class)
    protected List<Float> half2;
    @XmlList
    @XmlElement(type = Float.class)
    protected List<Float> half3;
    @XmlList
    @XmlElement(type = Float.class)
    protected List<Float> half4;
    @XmlList
    @XmlElement(name = "half1x1", type = Float.class)
    protected List<Float> half1X1;
    @XmlList
    @XmlElement(name = "half1x2", type = Float.class)
    protected List<Float> half1X2;
    @XmlList
    @XmlElement(name = "half1x3", type = Float.class)
    protected List<Float> half1X3;
    @XmlList
    @XmlElement(name = "half1x4", type = Float.class)
    protected List<Float> half1X4;
    @XmlList
    @XmlElement(name = "half2x1", type = Float.class)
    protected List<Float> half2X1;
    @XmlList
    @XmlElement(name = "half2x2", type = Float.class)
    protected List<Float> half2X2;
    @XmlList
    @XmlElement(name = "half2x3", type = Float.class)
    protected List<Float> half2X3;
    @XmlList
    @XmlElement(name = "half2x4", type = Float.class)
    protected List<Float> half2X4;
    @XmlList
    @XmlElement(name = "half3x1", type = Float.class)
    protected List<Float> half3X1;
    @XmlList
    @XmlElement(name = "half3x2", type = Float.class)
    protected List<Float> half3X2;
    @XmlList
    @XmlElement(name = "half3x3", type = Float.class)
    protected List<Float> half3X3;
    @XmlList
    @XmlElement(name = "half3x4", type = Float.class)
    protected List<Float> half3X4;
    @XmlList
    @XmlElement(name = "half4x1", type = Float.class)
    protected List<Float> half4X1;
    @XmlList
    @XmlElement(name = "half4x2", type = Float.class)
    protected List<Float> half4X2;
    @XmlList
    @XmlElement(name = "half4x3", type = Float.class)
    protected List<Float> half4X3;
    @XmlList
    @XmlElement(name = "half4x4", type = Float.class)
    protected List<Float> half4X4;
    protected Float fixed;
    protected Float fixed1;
    @XmlList
    @XmlElement(type = Float.class)
    protected List<Float> fixed2;
    @XmlList
    @XmlElement(type = Float.class)
    protected List<Float> fixed3;
    @XmlList
    @XmlElement(type = Float.class)
    protected List<Float> fixed4;
    @XmlList
    @XmlElement(name = "fixed1x1", type = Float.class)
    protected List<Float> fixed1X1;
    @XmlList
    @XmlElement(name = "fixed1x2", type = Float.class)
    protected List<Float> fixed1X2;
    @XmlList
    @XmlElement(name = "fixed1x3", type = Float.class)
    protected List<Float> fixed1X3;
    @XmlList
    @XmlElement(name = "fixed1x4", type = Float.class)
    protected List<Float> fixed1X4;
    @XmlList
    @XmlElement(name = "fixed2x1", type = Float.class)
    protected List<Float> fixed2X1;
    @XmlList
    @XmlElement(name = "fixed2x2", type = Float.class)
    protected List<Float> fixed2X2;
    @XmlList
    @XmlElement(name = "fixed2x3", type = Float.class)
    protected List<Float> fixed2X3;
    @XmlList
    @XmlElement(name = "fixed2x4", type = Float.class)
    protected List<Float> fixed2X4;
    @XmlList
    @XmlElement(name = "fixed3x1", type = Float.class)
    protected List<Float> fixed3X1;
    @XmlList
    @XmlElement(name = "fixed3x2", type = Float.class)
    protected List<Float> fixed3X2;
    @XmlList
    @XmlElement(name = "fixed3x3", type = Float.class)
    protected List<Float> fixed3X3;
    @XmlList
    @XmlElement(name = "fixed3x4", type = Float.class)
    protected List<Float> fixed3X4;
    @XmlList
    @XmlElement(name = "fixed4x1", type = Float.class)
    protected List<Float> fixed4X1;
    @XmlList
    @XmlElement(name = "fixed4x2", type = Float.class)
    protected List<Float> fixed4X2;
    @XmlList
    @XmlElement(name = "fixed4x3", type = Float.class)
    protected List<Float> fixed4X3;
    @XmlList
    @XmlElement(name = "fixed4x4", type = Float.class)
    protected List<Float> fixed4X4;
    protected CgSurfaceType surface;
    protected CgSampler1D sampler1D;
    protected CgSampler2D sampler2D;
    protected CgSampler3D sampler3D;
    protected CgSamplerRECT samplerRECT;
    protected CgSamplerCUBE samplerCUBE;
    protected CgSamplerDEPTH samplerDEPTH;
    protected String string;
    @XmlElement(name = "enum")
    protected String _enum;
    @XmlAttribute(name = "ref", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String ref;

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
     * Ruft den Wert der bool1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBool1() {
        return bool1;
    }

    /**
     * Legt den Wert der bool1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBool1(Boolean value) {
        this.bool1 = value;
    }

    public boolean isSetBool1() {
        return (this.bool1 != null);
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
     * Gets the value of the bool1X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool1X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool1X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool1X1() {
        if (bool1X1 == null) {
            bool1X1 = new ArrayList<Boolean>();
        }
        return this.bool1X1;
    }

    public boolean isSetBool1X1() {
        return ((this.bool1X1 != null)&&(!this.bool1X1 .isEmpty()));
    }

    public void unsetBool1X1() {
        this.bool1X1 = null;
    }

    /**
     * Gets the value of the bool1X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool1X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool1X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool1X2() {
        if (bool1X2 == null) {
            bool1X2 = new ArrayList<Boolean>();
        }
        return this.bool1X2;
    }

    public boolean isSetBool1X2() {
        return ((this.bool1X2 != null)&&(!this.bool1X2 .isEmpty()));
    }

    public void unsetBool1X2() {
        this.bool1X2 = null;
    }

    /**
     * Gets the value of the bool1X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool1X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool1X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool1X3() {
        if (bool1X3 == null) {
            bool1X3 = new ArrayList<Boolean>();
        }
        return this.bool1X3;
    }

    public boolean isSetBool1X3() {
        return ((this.bool1X3 != null)&&(!this.bool1X3 .isEmpty()));
    }

    public void unsetBool1X3() {
        this.bool1X3 = null;
    }

    /**
     * Gets the value of the bool1X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool1X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool1X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool1X4() {
        if (bool1X4 == null) {
            bool1X4 = new ArrayList<Boolean>();
        }
        return this.bool1X4;
    }

    public boolean isSetBool1X4() {
        return ((this.bool1X4 != null)&&(!this.bool1X4 .isEmpty()));
    }

    public void unsetBool1X4() {
        this.bool1X4 = null;
    }

    /**
     * Gets the value of the bool2X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool2X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool2X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool2X1() {
        if (bool2X1 == null) {
            bool2X1 = new ArrayList<Boolean>();
        }
        return this.bool2X1;
    }

    public boolean isSetBool2X1() {
        return ((this.bool2X1 != null)&&(!this.bool2X1 .isEmpty()));
    }

    public void unsetBool2X1() {
        this.bool2X1 = null;
    }

    /**
     * Gets the value of the bool2X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool2X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool2X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool2X2() {
        if (bool2X2 == null) {
            bool2X2 = new ArrayList<Boolean>();
        }
        return this.bool2X2;
    }

    public boolean isSetBool2X2() {
        return ((this.bool2X2 != null)&&(!this.bool2X2 .isEmpty()));
    }

    public void unsetBool2X2() {
        this.bool2X2 = null;
    }

    /**
     * Gets the value of the bool2X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool2X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool2X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool2X3() {
        if (bool2X3 == null) {
            bool2X3 = new ArrayList<Boolean>();
        }
        return this.bool2X3;
    }

    public boolean isSetBool2X3() {
        return ((this.bool2X3 != null)&&(!this.bool2X3 .isEmpty()));
    }

    public void unsetBool2X3() {
        this.bool2X3 = null;
    }

    /**
     * Gets the value of the bool2X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool2X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool2X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool2X4() {
        if (bool2X4 == null) {
            bool2X4 = new ArrayList<Boolean>();
        }
        return this.bool2X4;
    }

    public boolean isSetBool2X4() {
        return ((this.bool2X4 != null)&&(!this.bool2X4 .isEmpty()));
    }

    public void unsetBool2X4() {
        this.bool2X4 = null;
    }

    /**
     * Gets the value of the bool3X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool3X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool3X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool3X1() {
        if (bool3X1 == null) {
            bool3X1 = new ArrayList<Boolean>();
        }
        return this.bool3X1;
    }

    public boolean isSetBool3X1() {
        return ((this.bool3X1 != null)&&(!this.bool3X1 .isEmpty()));
    }

    public void unsetBool3X1() {
        this.bool3X1 = null;
    }

    /**
     * Gets the value of the bool3X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool3X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool3X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool3X2() {
        if (bool3X2 == null) {
            bool3X2 = new ArrayList<Boolean>();
        }
        return this.bool3X2;
    }

    public boolean isSetBool3X2() {
        return ((this.bool3X2 != null)&&(!this.bool3X2 .isEmpty()));
    }

    public void unsetBool3X2() {
        this.bool3X2 = null;
    }

    /**
     * Gets the value of the bool3X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool3X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool3X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool3X3() {
        if (bool3X3 == null) {
            bool3X3 = new ArrayList<Boolean>();
        }
        return this.bool3X3;
    }

    public boolean isSetBool3X3() {
        return ((this.bool3X3 != null)&&(!this.bool3X3 .isEmpty()));
    }

    public void unsetBool3X3() {
        this.bool3X3 = null;
    }

    /**
     * Gets the value of the bool3X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool3X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool3X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool3X4() {
        if (bool3X4 == null) {
            bool3X4 = new ArrayList<Boolean>();
        }
        return this.bool3X4;
    }

    public boolean isSetBool3X4() {
        return ((this.bool3X4 != null)&&(!this.bool3X4 .isEmpty()));
    }

    public void unsetBool3X4() {
        this.bool3X4 = null;
    }

    /**
     * Gets the value of the bool4X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool4X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool4X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool4X1() {
        if (bool4X1 == null) {
            bool4X1 = new ArrayList<Boolean>();
        }
        return this.bool4X1;
    }

    public boolean isSetBool4X1() {
        return ((this.bool4X1 != null)&&(!this.bool4X1 .isEmpty()));
    }

    public void unsetBool4X1() {
        this.bool4X1 = null;
    }

    /**
     * Gets the value of the bool4X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool4X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool4X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool4X2() {
        if (bool4X2 == null) {
            bool4X2 = new ArrayList<Boolean>();
        }
        return this.bool4X2;
    }

    public boolean isSetBool4X2() {
        return ((this.bool4X2 != null)&&(!this.bool4X2 .isEmpty()));
    }

    public void unsetBool4X2() {
        this.bool4X2 = null;
    }

    /**
     * Gets the value of the bool4X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool4X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool4X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool4X3() {
        if (bool4X3 == null) {
            bool4X3 = new ArrayList<Boolean>();
        }
        return this.bool4X3;
    }

    public boolean isSetBool4X3() {
        return ((this.bool4X3 != null)&&(!this.bool4X3 .isEmpty()));
    }

    public void unsetBool4X3() {
        this.bool4X3 = null;
    }

    /**
     * Gets the value of the bool4X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bool4X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBool4X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getBool4X4() {
        if (bool4X4 == null) {
            bool4X4 = new ArrayList<Boolean>();
        }
        return this.bool4X4;
    }

    public boolean isSetBool4X4() {
        return ((this.bool4X4 != null)&&(!this.bool4X4 .isEmpty()));
    }

    public void unsetBool4X4() {
        this.bool4X4 = null;
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
     * Ruft den Wert der float1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getFloat1() {
        return float1;
    }

    /**
     * Legt den Wert der float1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setFloat1(Float value) {
        this.float1 = value;
    }

    public boolean isSetFloat1() {
        return (this.float1 != null);
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
     * Gets the value of the float1X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the float1X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFloat1X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat1X1() {
        if (float1X1 == null) {
            float1X1 = new ArrayList<Float>();
        }
        return this.float1X1;
    }

    public boolean isSetFloat1X1() {
        return ((this.float1X1 != null)&&(!this.float1X1 .isEmpty()));
    }

    public void unsetFloat1X1() {
        this.float1X1 = null;
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat1X2() {
        if (float1X2 == null) {
            float1X2 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat1X3() {
        if (float1X3 == null) {
            float1X3 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat1X4() {
        if (float1X4 == null) {
            float1X4 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat2X1() {
        if (float2X1 == null) {
            float2X1 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat2X3() {
        if (float2X3 == null) {
            float2X3 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat2X4() {
        if (float2X4 == null) {
            float2X4 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat3X1() {
        if (float3X1 == null) {
            float3X1 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat3X2() {
        if (float3X2 == null) {
            float3X2 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat3X4() {
        if (float3X4 == null) {
            float3X4 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat4X1() {
        if (float4X1 == null) {
            float4X1 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat4X2() {
        if (float4X2 == null) {
            float4X2 = new ArrayList<Float>();
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
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFloat4X3() {
        if (float4X3 == null) {
            float4X3 = new ArrayList<Float>();
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
     * Ruft den Wert der int1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getInt1() {
        return int1;
    }

    /**
     * Legt den Wert der int1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setInt1(Integer value) {
        this.int1 = value;
    }

    public boolean isSetInt1() {
        return (this.int1 != null);
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
     * Gets the value of the int1X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int1X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt1X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt1X1() {
        if (int1X1 == null) {
            int1X1 = new ArrayList<Integer>();
        }
        return this.int1X1;
    }

    public boolean isSetInt1X1() {
        return ((this.int1X1 != null)&&(!this.int1X1 .isEmpty()));
    }

    public void unsetInt1X1() {
        this.int1X1 = null;
    }

    /**
     * Gets the value of the int1X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int1X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt1X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt1X2() {
        if (int1X2 == null) {
            int1X2 = new ArrayList<Integer>();
        }
        return this.int1X2;
    }

    public boolean isSetInt1X2() {
        return ((this.int1X2 != null)&&(!this.int1X2 .isEmpty()));
    }

    public void unsetInt1X2() {
        this.int1X2 = null;
    }

    /**
     * Gets the value of the int1X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int1X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt1X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt1X3() {
        if (int1X3 == null) {
            int1X3 = new ArrayList<Integer>();
        }
        return this.int1X3;
    }

    public boolean isSetInt1X3() {
        return ((this.int1X3 != null)&&(!this.int1X3 .isEmpty()));
    }

    public void unsetInt1X3() {
        this.int1X3 = null;
    }

    /**
     * Gets the value of the int1X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int1X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt1X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt1X4() {
        if (int1X4 == null) {
            int1X4 = new ArrayList<Integer>();
        }
        return this.int1X4;
    }

    public boolean isSetInt1X4() {
        return ((this.int1X4 != null)&&(!this.int1X4 .isEmpty()));
    }

    public void unsetInt1X4() {
        this.int1X4 = null;
    }

    /**
     * Gets the value of the int2X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int2X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt2X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt2X1() {
        if (int2X1 == null) {
            int2X1 = new ArrayList<Integer>();
        }
        return this.int2X1;
    }

    public boolean isSetInt2X1() {
        return ((this.int2X1 != null)&&(!this.int2X1 .isEmpty()));
    }

    public void unsetInt2X1() {
        this.int2X1 = null;
    }

    /**
     * Gets the value of the int2X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int2X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt2X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt2X2() {
        if (int2X2 == null) {
            int2X2 = new ArrayList<Integer>();
        }
        return this.int2X2;
    }

    public boolean isSetInt2X2() {
        return ((this.int2X2 != null)&&(!this.int2X2 .isEmpty()));
    }

    public void unsetInt2X2() {
        this.int2X2 = null;
    }

    /**
     * Gets the value of the int2X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int2X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt2X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt2X3() {
        if (int2X3 == null) {
            int2X3 = new ArrayList<Integer>();
        }
        return this.int2X3;
    }

    public boolean isSetInt2X3() {
        return ((this.int2X3 != null)&&(!this.int2X3 .isEmpty()));
    }

    public void unsetInt2X3() {
        this.int2X3 = null;
    }

    /**
     * Gets the value of the int2X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int2X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt2X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt2X4() {
        if (int2X4 == null) {
            int2X4 = new ArrayList<Integer>();
        }
        return this.int2X4;
    }

    public boolean isSetInt2X4() {
        return ((this.int2X4 != null)&&(!this.int2X4 .isEmpty()));
    }

    public void unsetInt2X4() {
        this.int2X4 = null;
    }

    /**
     * Gets the value of the int3X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int3X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt3X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt3X1() {
        if (int3X1 == null) {
            int3X1 = new ArrayList<Integer>();
        }
        return this.int3X1;
    }

    public boolean isSetInt3X1() {
        return ((this.int3X1 != null)&&(!this.int3X1 .isEmpty()));
    }

    public void unsetInt3X1() {
        this.int3X1 = null;
    }

    /**
     * Gets the value of the int3X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int3X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt3X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt3X2() {
        if (int3X2 == null) {
            int3X2 = new ArrayList<Integer>();
        }
        return this.int3X2;
    }

    public boolean isSetInt3X2() {
        return ((this.int3X2 != null)&&(!this.int3X2 .isEmpty()));
    }

    public void unsetInt3X2() {
        this.int3X2 = null;
    }

    /**
     * Gets the value of the int3X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int3X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt3X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt3X3() {
        if (int3X3 == null) {
            int3X3 = new ArrayList<Integer>();
        }
        return this.int3X3;
    }

    public boolean isSetInt3X3() {
        return ((this.int3X3 != null)&&(!this.int3X3 .isEmpty()));
    }

    public void unsetInt3X3() {
        this.int3X3 = null;
    }

    /**
     * Gets the value of the int3X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int3X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt3X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt3X4() {
        if (int3X4 == null) {
            int3X4 = new ArrayList<Integer>();
        }
        return this.int3X4;
    }

    public boolean isSetInt3X4() {
        return ((this.int3X4 != null)&&(!this.int3X4 .isEmpty()));
    }

    public void unsetInt3X4() {
        this.int3X4 = null;
    }

    /**
     * Gets the value of the int4X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int4X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt4X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt4X1() {
        if (int4X1 == null) {
            int4X1 = new ArrayList<Integer>();
        }
        return this.int4X1;
    }

    public boolean isSetInt4X1() {
        return ((this.int4X1 != null)&&(!this.int4X1 .isEmpty()));
    }

    public void unsetInt4X1() {
        this.int4X1 = null;
    }

    /**
     * Gets the value of the int4X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int4X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt4X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt4X2() {
        if (int4X2 == null) {
            int4X2 = new ArrayList<Integer>();
        }
        return this.int4X2;
    }

    public boolean isSetInt4X2() {
        return ((this.int4X2 != null)&&(!this.int4X2 .isEmpty()));
    }

    public void unsetInt4X2() {
        this.int4X2 = null;
    }

    /**
     * Gets the value of the int4X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int4X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt4X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt4X3() {
        if (int4X3 == null) {
            int4X3 = new ArrayList<Integer>();
        }
        return this.int4X3;
    }

    public boolean isSetInt4X3() {
        return ((this.int4X3 != null)&&(!this.int4X3 .isEmpty()));
    }

    public void unsetInt4X3() {
        this.int4X3 = null;
    }

    /**
     * Gets the value of the int4X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int4X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInt4X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getInt4X4() {
        if (int4X4 == null) {
            int4X4 = new ArrayList<Integer>();
        }
        return this.int4X4;
    }

    public boolean isSetInt4X4() {
        return ((this.int4X4 != null)&&(!this.int4X4 .isEmpty()));
    }

    public void unsetInt4X4() {
        this.int4X4 = null;
    }

    /**
     * Ruft den Wert der half-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getHalf() {
        return half;
    }

    /**
     * Legt den Wert der half-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setHalf(Float value) {
        this.half = value;
    }

    public boolean isSetHalf() {
        return (this.half!= null);
    }

    /**
     * Ruft den Wert der half1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getHalf1() {
        return half1;
    }

    /**
     * Legt den Wert der half1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setHalf1(Float value) {
        this.half1 = value;
    }

    public boolean isSetHalf1() {
        return (this.half1 != null);
    }

    /**
     * Gets the value of the half2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf2() {
        if (half2 == null) {
            half2 = new ArrayList<Float>();
        }
        return this.half2;
    }

    public boolean isSetHalf2() {
        return ((this.half2 != null)&&(!this.half2 .isEmpty()));
    }

    public void unsetHalf2() {
        this.half2 = null;
    }

    /**
     * Gets the value of the half3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf3() {
        if (half3 == null) {
            half3 = new ArrayList<Float>();
        }
        return this.half3;
    }

    public boolean isSetHalf3() {
        return ((this.half3 != null)&&(!this.half3 .isEmpty()));
    }

    public void unsetHalf3() {
        this.half3 = null;
    }

    /**
     * Gets the value of the half4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf4() {
        if (half4 == null) {
            half4 = new ArrayList<Float>();
        }
        return this.half4;
    }

    public boolean isSetHalf4() {
        return ((this.half4 != null)&&(!this.half4 .isEmpty()));
    }

    public void unsetHalf4() {
        this.half4 = null;
    }

    /**
     * Gets the value of the half1X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half1X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf1X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf1X1() {
        if (half1X1 == null) {
            half1X1 = new ArrayList<Float>();
        }
        return this.half1X1;
    }

    public boolean isSetHalf1X1() {
        return ((this.half1X1 != null)&&(!this.half1X1 .isEmpty()));
    }

    public void unsetHalf1X1() {
        this.half1X1 = null;
    }

    /**
     * Gets the value of the half1X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half1X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf1X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf1X2() {
        if (half1X2 == null) {
            half1X2 = new ArrayList<Float>();
        }
        return this.half1X2;
    }

    public boolean isSetHalf1X2() {
        return ((this.half1X2 != null)&&(!this.half1X2 .isEmpty()));
    }

    public void unsetHalf1X2() {
        this.half1X2 = null;
    }

    /**
     * Gets the value of the half1X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half1X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf1X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf1X3() {
        if (half1X3 == null) {
            half1X3 = new ArrayList<Float>();
        }
        return this.half1X3;
    }

    public boolean isSetHalf1X3() {
        return ((this.half1X3 != null)&&(!this.half1X3 .isEmpty()));
    }

    public void unsetHalf1X3() {
        this.half1X3 = null;
    }

    /**
     * Gets the value of the half1X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half1X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf1X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf1X4() {
        if (half1X4 == null) {
            half1X4 = new ArrayList<Float>();
        }
        return this.half1X4;
    }

    public boolean isSetHalf1X4() {
        return ((this.half1X4 != null)&&(!this.half1X4 .isEmpty()));
    }

    public void unsetHalf1X4() {
        this.half1X4 = null;
    }

    /**
     * Gets the value of the half2X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half2X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf2X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf2X1() {
        if (half2X1 == null) {
            half2X1 = new ArrayList<Float>();
        }
        return this.half2X1;
    }

    public boolean isSetHalf2X1() {
        return ((this.half2X1 != null)&&(!this.half2X1 .isEmpty()));
    }

    public void unsetHalf2X1() {
        this.half2X1 = null;
    }

    /**
     * Gets the value of the half2X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half2X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf2X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf2X2() {
        if (half2X2 == null) {
            half2X2 = new ArrayList<Float>();
        }
        return this.half2X2;
    }

    public boolean isSetHalf2X2() {
        return ((this.half2X2 != null)&&(!this.half2X2 .isEmpty()));
    }

    public void unsetHalf2X2() {
        this.half2X2 = null;
    }

    /**
     * Gets the value of the half2X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half2X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf2X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf2X3() {
        if (half2X3 == null) {
            half2X3 = new ArrayList<Float>();
        }
        return this.half2X3;
    }

    public boolean isSetHalf2X3() {
        return ((this.half2X3 != null)&&(!this.half2X3 .isEmpty()));
    }

    public void unsetHalf2X3() {
        this.half2X3 = null;
    }

    /**
     * Gets the value of the half2X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half2X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf2X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf2X4() {
        if (half2X4 == null) {
            half2X4 = new ArrayList<Float>();
        }
        return this.half2X4;
    }

    public boolean isSetHalf2X4() {
        return ((this.half2X4 != null)&&(!this.half2X4 .isEmpty()));
    }

    public void unsetHalf2X4() {
        this.half2X4 = null;
    }

    /**
     * Gets the value of the half3X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half3X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf3X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf3X1() {
        if (half3X1 == null) {
            half3X1 = new ArrayList<Float>();
        }
        return this.half3X1;
    }

    public boolean isSetHalf3X1() {
        return ((this.half3X1 != null)&&(!this.half3X1 .isEmpty()));
    }

    public void unsetHalf3X1() {
        this.half3X1 = null;
    }

    /**
     * Gets the value of the half3X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half3X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf3X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf3X2() {
        if (half3X2 == null) {
            half3X2 = new ArrayList<Float>();
        }
        return this.half3X2;
    }

    public boolean isSetHalf3X2() {
        return ((this.half3X2 != null)&&(!this.half3X2 .isEmpty()));
    }

    public void unsetHalf3X2() {
        this.half3X2 = null;
    }

    /**
     * Gets the value of the half3X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half3X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf3X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf3X3() {
        if (half3X3 == null) {
            half3X3 = new ArrayList<Float>();
        }
        return this.half3X3;
    }

    public boolean isSetHalf3X3() {
        return ((this.half3X3 != null)&&(!this.half3X3 .isEmpty()));
    }

    public void unsetHalf3X3() {
        this.half3X3 = null;
    }

    /**
     * Gets the value of the half3X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half3X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf3X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf3X4() {
        if (half3X4 == null) {
            half3X4 = new ArrayList<Float>();
        }
        return this.half3X4;
    }

    public boolean isSetHalf3X4() {
        return ((this.half3X4 != null)&&(!this.half3X4 .isEmpty()));
    }

    public void unsetHalf3X4() {
        this.half3X4 = null;
    }

    /**
     * Gets the value of the half4X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half4X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf4X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf4X1() {
        if (half4X1 == null) {
            half4X1 = new ArrayList<Float>();
        }
        return this.half4X1;
    }

    public boolean isSetHalf4X1() {
        return ((this.half4X1 != null)&&(!this.half4X1 .isEmpty()));
    }

    public void unsetHalf4X1() {
        this.half4X1 = null;
    }

    /**
     * Gets the value of the half4X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half4X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf4X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf4X2() {
        if (half4X2 == null) {
            half4X2 = new ArrayList<Float>();
        }
        return this.half4X2;
    }

    public boolean isSetHalf4X2() {
        return ((this.half4X2 != null)&&(!this.half4X2 .isEmpty()));
    }

    public void unsetHalf4X2() {
        this.half4X2 = null;
    }

    /**
     * Gets the value of the half4X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half4X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf4X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf4X3() {
        if (half4X3 == null) {
            half4X3 = new ArrayList<Float>();
        }
        return this.half4X3;
    }

    public boolean isSetHalf4X3() {
        return ((this.half4X3 != null)&&(!this.half4X3 .isEmpty()));
    }

    public void unsetHalf4X3() {
        this.half4X3 = null;
    }

    /**
     * Gets the value of the half4X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the half4X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHalf4X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getHalf4X4() {
        if (half4X4 == null) {
            half4X4 = new ArrayList<Float>();
        }
        return this.half4X4;
    }

    public boolean isSetHalf4X4() {
        return ((this.half4X4 != null)&&(!this.half4X4 .isEmpty()));
    }

    public void unsetHalf4X4() {
        this.half4X4 = null;
    }

    /**
     * Ruft den Wert der fixed-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getFixed() {
        return fixed;
    }

    /**
     * Legt den Wert der fixed-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setFixed(Float value) {
        this.fixed = value;
    }

    public boolean isSetFixed() {
        return (this.fixed!= null);
    }

    /**
     * Ruft den Wert der fixed1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getFixed1() {
        return fixed1;
    }

    /**
     * Legt den Wert der fixed1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setFixed1(Float value) {
        this.fixed1 = value;
    }

    public boolean isSetFixed1() {
        return (this.fixed1 != null);
    }

    /**
     * Gets the value of the fixed2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed2() {
        if (fixed2 == null) {
            fixed2 = new ArrayList<Float>();
        }
        return this.fixed2;
    }

    public boolean isSetFixed2() {
        return ((this.fixed2 != null)&&(!this.fixed2 .isEmpty()));
    }

    public void unsetFixed2() {
        this.fixed2 = null;
    }

    /**
     * Gets the value of the fixed3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed3() {
        if (fixed3 == null) {
            fixed3 = new ArrayList<Float>();
        }
        return this.fixed3;
    }

    public boolean isSetFixed3() {
        return ((this.fixed3 != null)&&(!this.fixed3 .isEmpty()));
    }

    public void unsetFixed3() {
        this.fixed3 = null;
    }

    /**
     * Gets the value of the fixed4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed4() {
        if (fixed4 == null) {
            fixed4 = new ArrayList<Float>();
        }
        return this.fixed4;
    }

    public boolean isSetFixed4() {
        return ((this.fixed4 != null)&&(!this.fixed4 .isEmpty()));
    }

    public void unsetFixed4() {
        this.fixed4 = null;
    }

    /**
     * Gets the value of the fixed1X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed1X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed1X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed1X1() {
        if (fixed1X1 == null) {
            fixed1X1 = new ArrayList<Float>();
        }
        return this.fixed1X1;
    }

    public boolean isSetFixed1X1() {
        return ((this.fixed1X1 != null)&&(!this.fixed1X1 .isEmpty()));
    }

    public void unsetFixed1X1() {
        this.fixed1X1 = null;
    }

    /**
     * Gets the value of the fixed1X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed1X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed1X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed1X2() {
        if (fixed1X2 == null) {
            fixed1X2 = new ArrayList<Float>();
        }
        return this.fixed1X2;
    }

    public boolean isSetFixed1X2() {
        return ((this.fixed1X2 != null)&&(!this.fixed1X2 .isEmpty()));
    }

    public void unsetFixed1X2() {
        this.fixed1X2 = null;
    }

    /**
     * Gets the value of the fixed1X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed1X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed1X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed1X3() {
        if (fixed1X3 == null) {
            fixed1X3 = new ArrayList<Float>();
        }
        return this.fixed1X3;
    }

    public boolean isSetFixed1X3() {
        return ((this.fixed1X3 != null)&&(!this.fixed1X3 .isEmpty()));
    }

    public void unsetFixed1X3() {
        this.fixed1X3 = null;
    }

    /**
     * Gets the value of the fixed1X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed1X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed1X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed1X4() {
        if (fixed1X4 == null) {
            fixed1X4 = new ArrayList<Float>();
        }
        return this.fixed1X4;
    }

    public boolean isSetFixed1X4() {
        return ((this.fixed1X4 != null)&&(!this.fixed1X4 .isEmpty()));
    }

    public void unsetFixed1X4() {
        this.fixed1X4 = null;
    }

    /**
     * Gets the value of the fixed2X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed2X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed2X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed2X1() {
        if (fixed2X1 == null) {
            fixed2X1 = new ArrayList<Float>();
        }
        return this.fixed2X1;
    }

    public boolean isSetFixed2X1() {
        return ((this.fixed2X1 != null)&&(!this.fixed2X1 .isEmpty()));
    }

    public void unsetFixed2X1() {
        this.fixed2X1 = null;
    }

    /**
     * Gets the value of the fixed2X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed2X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed2X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed2X2() {
        if (fixed2X2 == null) {
            fixed2X2 = new ArrayList<Float>();
        }
        return this.fixed2X2;
    }

    public boolean isSetFixed2X2() {
        return ((this.fixed2X2 != null)&&(!this.fixed2X2 .isEmpty()));
    }

    public void unsetFixed2X2() {
        this.fixed2X2 = null;
    }

    /**
     * Gets the value of the fixed2X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed2X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed2X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed2X3() {
        if (fixed2X3 == null) {
            fixed2X3 = new ArrayList<Float>();
        }
        return this.fixed2X3;
    }

    public boolean isSetFixed2X3() {
        return ((this.fixed2X3 != null)&&(!this.fixed2X3 .isEmpty()));
    }

    public void unsetFixed2X3() {
        this.fixed2X3 = null;
    }

    /**
     * Gets the value of the fixed2X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed2X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed2X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed2X4() {
        if (fixed2X4 == null) {
            fixed2X4 = new ArrayList<Float>();
        }
        return this.fixed2X4;
    }

    public boolean isSetFixed2X4() {
        return ((this.fixed2X4 != null)&&(!this.fixed2X4 .isEmpty()));
    }

    public void unsetFixed2X4() {
        this.fixed2X4 = null;
    }

    /**
     * Gets the value of the fixed3X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed3X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed3X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed3X1() {
        if (fixed3X1 == null) {
            fixed3X1 = new ArrayList<Float>();
        }
        return this.fixed3X1;
    }

    public boolean isSetFixed3X1() {
        return ((this.fixed3X1 != null)&&(!this.fixed3X1 .isEmpty()));
    }

    public void unsetFixed3X1() {
        this.fixed3X1 = null;
    }

    /**
     * Gets the value of the fixed3X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed3X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed3X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed3X2() {
        if (fixed3X2 == null) {
            fixed3X2 = new ArrayList<Float>();
        }
        return this.fixed3X2;
    }

    public boolean isSetFixed3X2() {
        return ((this.fixed3X2 != null)&&(!this.fixed3X2 .isEmpty()));
    }

    public void unsetFixed3X2() {
        this.fixed3X2 = null;
    }

    /**
     * Gets the value of the fixed3X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed3X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed3X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed3X3() {
        if (fixed3X3 == null) {
            fixed3X3 = new ArrayList<Float>();
        }
        return this.fixed3X3;
    }

    public boolean isSetFixed3X3() {
        return ((this.fixed3X3 != null)&&(!this.fixed3X3 .isEmpty()));
    }

    public void unsetFixed3X3() {
        this.fixed3X3 = null;
    }

    /**
     * Gets the value of the fixed3X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed3X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed3X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed3X4() {
        if (fixed3X4 == null) {
            fixed3X4 = new ArrayList<Float>();
        }
        return this.fixed3X4;
    }

    public boolean isSetFixed3X4() {
        return ((this.fixed3X4 != null)&&(!this.fixed3X4 .isEmpty()));
    }

    public void unsetFixed3X4() {
        this.fixed3X4 = null;
    }

    /**
     * Gets the value of the fixed4X1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed4X1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed4X1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed4X1() {
        if (fixed4X1 == null) {
            fixed4X1 = new ArrayList<Float>();
        }
        return this.fixed4X1;
    }

    public boolean isSetFixed4X1() {
        return ((this.fixed4X1 != null)&&(!this.fixed4X1 .isEmpty()));
    }

    public void unsetFixed4X1() {
        this.fixed4X1 = null;
    }

    /**
     * Gets the value of the fixed4X2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed4X2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed4X2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed4X2() {
        if (fixed4X2 == null) {
            fixed4X2 = new ArrayList<Float>();
        }
        return this.fixed4X2;
    }

    public boolean isSetFixed4X2() {
        return ((this.fixed4X2 != null)&&(!this.fixed4X2 .isEmpty()));
    }

    public void unsetFixed4X2() {
        this.fixed4X2 = null;
    }

    /**
     * Gets the value of the fixed4X3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed4X3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed4X3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed4X3() {
        if (fixed4X3 == null) {
            fixed4X3 = new ArrayList<Float>();
        }
        return this.fixed4X3;
    }

    public boolean isSetFixed4X3() {
        return ((this.fixed4X3 != null)&&(!this.fixed4X3 .isEmpty()));
    }

    public void unsetFixed4X3() {
        this.fixed4X3 = null;
    }

    /**
     * Gets the value of the fixed4X4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixed4X4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixed4X4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Float }
     * 
     * 
     */
    public List<Float> getFixed4X4() {
        if (fixed4X4 == null) {
            fixed4X4 = new ArrayList<Float>();
        }
        return this.fixed4X4;
    }

    public boolean isSetFixed4X4() {
        return ((this.fixed4X4 != null)&&(!this.fixed4X4 .isEmpty()));
    }

    public void unsetFixed4X4() {
        this.fixed4X4 = null;
    }

    /**
     * Ruft den Wert der surface-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CgSurfaceType }
     *     
     */
    public CgSurfaceType getSurface() {
        return surface;
    }

    /**
     * Legt den Wert der surface-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CgSurfaceType }
     *     
     */
    public void setSurface(CgSurfaceType value) {
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
     *     {@link CgSampler1D }
     *     
     */
    public CgSampler1D getSampler1D() {
        return sampler1D;
    }

    /**
     * Legt den Wert der sampler1D-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CgSampler1D }
     *     
     */
    public void setSampler1D(CgSampler1D value) {
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
     *     {@link CgSampler2D }
     *     
     */
    public CgSampler2D getSampler2D() {
        return sampler2D;
    }

    /**
     * Legt den Wert der sampler2D-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CgSampler2D }
     *     
     */
    public void setSampler2D(CgSampler2D value) {
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
     *     {@link CgSampler3D }
     *     
     */
    public CgSampler3D getSampler3D() {
        return sampler3D;
    }

    /**
     * Legt den Wert der sampler3D-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CgSampler3D }
     *     
     */
    public void setSampler3D(CgSampler3D value) {
        this.sampler3D = value;
    }

    public boolean isSetSampler3D() {
        return (this.sampler3D!= null);
    }

    /**
     * Ruft den Wert der samplerRECT-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CgSamplerRECT }
     *     
     */
    public CgSamplerRECT getSamplerRECT() {
        return samplerRECT;
    }

    /**
     * Legt den Wert der samplerRECT-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CgSamplerRECT }
     *     
     */
    public void setSamplerRECT(CgSamplerRECT value) {
        this.samplerRECT = value;
    }

    public boolean isSetSamplerRECT() {
        return (this.samplerRECT!= null);
    }

    /**
     * Ruft den Wert der samplerCUBE-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CgSamplerCUBE }
     *     
     */
    public CgSamplerCUBE getSamplerCUBE() {
        return samplerCUBE;
    }

    /**
     * Legt den Wert der samplerCUBE-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CgSamplerCUBE }
     *     
     */
    public void setSamplerCUBE(CgSamplerCUBE value) {
        this.samplerCUBE = value;
    }

    public boolean isSetSamplerCUBE() {
        return (this.samplerCUBE!= null);
    }

    /**
     * Ruft den Wert der samplerDEPTH-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CgSamplerDEPTH }
     *     
     */
    public CgSamplerDEPTH getSamplerDEPTH() {
        return samplerDEPTH;
    }

    /**
     * Legt den Wert der samplerDEPTH-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CgSamplerDEPTH }
     *     
     */
    public void setSamplerDEPTH(CgSamplerDEPTH value) {
        this.samplerDEPTH = value;
    }

    public boolean isSetSamplerDEPTH() {
        return (this.samplerDEPTH!= null);
    }

    /**
     * Ruft den Wert der string-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getString() {
        return string;
    }

    /**
     * Legt den Wert der string-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setString(String value) {
        this.string = value;
    }

    public boolean isSetString() {
        return (this.string!= null);
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
     * Ruft den Wert der ref-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRef() {
        return ref;
    }

    /**
     * Legt den Wert der ref-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRef(String value) {
        this.ref = value;
    }

    public boolean isSetRef() {
        return (this.ref!= null);
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

    public void setBool1X1(List<Boolean> value) {
        this.bool1X1 = value;
    }

    public void setBool1X2(List<Boolean> value) {
        this.bool1X2 = value;
    }

    public void setBool1X3(List<Boolean> value) {
        this.bool1X3 = value;
    }

    public void setBool1X4(List<Boolean> value) {
        this.bool1X4 = value;
    }

    public void setBool2X1(List<Boolean> value) {
        this.bool2X1 = value;
    }

    public void setBool2X2(List<Boolean> value) {
        this.bool2X2 = value;
    }

    public void setBool2X3(List<Boolean> value) {
        this.bool2X3 = value;
    }

    public void setBool2X4(List<Boolean> value) {
        this.bool2X4 = value;
    }

    public void setBool3X1(List<Boolean> value) {
        this.bool3X1 = value;
    }

    public void setBool3X2(List<Boolean> value) {
        this.bool3X2 = value;
    }

    public void setBool3X3(List<Boolean> value) {
        this.bool3X3 = value;
    }

    public void setBool3X4(List<Boolean> value) {
        this.bool3X4 = value;
    }

    public void setBool4X1(List<Boolean> value) {
        this.bool4X1 = value;
    }

    public void setBool4X2(List<Boolean> value) {
        this.bool4X2 = value;
    }

    public void setBool4X3(List<Boolean> value) {
        this.bool4X3 = value;
    }

    public void setBool4X4(List<Boolean> value) {
        this.bool4X4 = value;
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

    public void setFloat1X1(List<Float> value) {
        this.float1X1 = value;
    }

    public void setFloat1X2(List<Float> value) {
        this.float1X2 = value;
    }

    public void setFloat1X3(List<Float> value) {
        this.float1X3 = value;
    }

    public void setFloat1X4(List<Float> value) {
        this.float1X4 = value;
    }

    public void setFloat2X1(List<Float> value) {
        this.float2X1 = value;
    }

    public void setFloat2X2(List<Float> value) {
        this.float2X2 = value;
    }

    public void setFloat2X3(List<Float> value) {
        this.float2X3 = value;
    }

    public void setFloat2X4(List<Float> value) {
        this.float2X4 = value;
    }

    public void setFloat3X1(List<Float> value) {
        this.float3X1 = value;
    }

    public void setFloat3X2(List<Float> value) {
        this.float3X2 = value;
    }

    public void setFloat3X3(List<Float> value) {
        this.float3X3 = value;
    }

    public void setFloat3X4(List<Float> value) {
        this.float3X4 = value;
    }

    public void setFloat4X1(List<Float> value) {
        this.float4X1 = value;
    }

    public void setFloat4X2(List<Float> value) {
        this.float4X2 = value;
    }

    public void setFloat4X3(List<Float> value) {
        this.float4X3 = value;
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

    public void setInt1X1(List<Integer> value) {
        this.int1X1 = value;
    }

    public void setInt1X2(List<Integer> value) {
        this.int1X2 = value;
    }

    public void setInt1X3(List<Integer> value) {
        this.int1X3 = value;
    }

    public void setInt1X4(List<Integer> value) {
        this.int1X4 = value;
    }

    public void setInt2X1(List<Integer> value) {
        this.int2X1 = value;
    }

    public void setInt2X2(List<Integer> value) {
        this.int2X2 = value;
    }

    public void setInt2X3(List<Integer> value) {
        this.int2X3 = value;
    }

    public void setInt2X4(List<Integer> value) {
        this.int2X4 = value;
    }

    public void setInt3X1(List<Integer> value) {
        this.int3X1 = value;
    }

    public void setInt3X2(List<Integer> value) {
        this.int3X2 = value;
    }

    public void setInt3X3(List<Integer> value) {
        this.int3X3 = value;
    }

    public void setInt3X4(List<Integer> value) {
        this.int3X4 = value;
    }

    public void setInt4X1(List<Integer> value) {
        this.int4X1 = value;
    }

    public void setInt4X2(List<Integer> value) {
        this.int4X2 = value;
    }

    public void setInt4X3(List<Integer> value) {
        this.int4X3 = value;
    }

    public void setInt4X4(List<Integer> value) {
        this.int4X4 = value;
    }

    public void setHalf2(List<Float> value) {
        this.half2 = value;
    }

    public void setHalf3(List<Float> value) {
        this.half3 = value;
    }

    public void setHalf4(List<Float> value) {
        this.half4 = value;
    }

    public void setHalf1X1(List<Float> value) {
        this.half1X1 = value;
    }

    public void setHalf1X2(List<Float> value) {
        this.half1X2 = value;
    }

    public void setHalf1X3(List<Float> value) {
        this.half1X3 = value;
    }

    public void setHalf1X4(List<Float> value) {
        this.half1X4 = value;
    }

    public void setHalf2X1(List<Float> value) {
        this.half2X1 = value;
    }

    public void setHalf2X2(List<Float> value) {
        this.half2X2 = value;
    }

    public void setHalf2X3(List<Float> value) {
        this.half2X3 = value;
    }

    public void setHalf2X4(List<Float> value) {
        this.half2X4 = value;
    }

    public void setHalf3X1(List<Float> value) {
        this.half3X1 = value;
    }

    public void setHalf3X2(List<Float> value) {
        this.half3X2 = value;
    }

    public void setHalf3X3(List<Float> value) {
        this.half3X3 = value;
    }

    public void setHalf3X4(List<Float> value) {
        this.half3X4 = value;
    }

    public void setHalf4X1(List<Float> value) {
        this.half4X1 = value;
    }

    public void setHalf4X2(List<Float> value) {
        this.half4X2 = value;
    }

    public void setHalf4X3(List<Float> value) {
        this.half4X3 = value;
    }

    public void setHalf4X4(List<Float> value) {
        this.half4X4 = value;
    }

    public void setFixed2(List<Float> value) {
        this.fixed2 = value;
    }

    public void setFixed3(List<Float> value) {
        this.fixed3 = value;
    }

    public void setFixed4(List<Float> value) {
        this.fixed4 = value;
    }

    public void setFixed1X1(List<Float> value) {
        this.fixed1X1 = value;
    }

    public void setFixed1X2(List<Float> value) {
        this.fixed1X2 = value;
    }

    public void setFixed1X3(List<Float> value) {
        this.fixed1X3 = value;
    }

    public void setFixed1X4(List<Float> value) {
        this.fixed1X4 = value;
    }

    public void setFixed2X1(List<Float> value) {
        this.fixed2X1 = value;
    }

    public void setFixed2X2(List<Float> value) {
        this.fixed2X2 = value;
    }

    public void setFixed2X3(List<Float> value) {
        this.fixed2X3 = value;
    }

    public void setFixed2X4(List<Float> value) {
        this.fixed2X4 = value;
    }

    public void setFixed3X1(List<Float> value) {
        this.fixed3X1 = value;
    }

    public void setFixed3X2(List<Float> value) {
        this.fixed3X2 = value;
    }

    public void setFixed3X3(List<Float> value) {
        this.fixed3X3 = value;
    }

    public void setFixed3X4(List<Float> value) {
        this.fixed3X4 = value;
    }

    public void setFixed4X1(List<Float> value) {
        this.fixed4X1 = value;
    }

    public void setFixed4X2(List<Float> value) {
        this.fixed4X2 = value;
    }

    public void setFixed4X3(List<Float> value) {
        this.fixed4X3 = value;
    }

    public void setFixed4X4(List<Float> value) {
        this.fixed4X4 = value;
    }

}
