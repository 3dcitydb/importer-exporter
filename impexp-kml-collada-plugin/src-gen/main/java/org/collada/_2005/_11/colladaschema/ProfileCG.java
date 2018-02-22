//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="code" type="{http://www.collada.org/2005/11/COLLADASchema}fx_code_profile"/>
 *           &lt;element name="include" type="{http://www.collada.org/2005/11/COLLADASchema}fx_include_common"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/>
 *           &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}cg_newparam"/>
 *         &lt;/choice>
 *         &lt;element name="technique" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/>
 *                   &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;choice maxOccurs="unbounded" minOccurs="0">
 *                     &lt;element name="code" type="{http://www.collada.org/2005/11/COLLADASchema}fx_code_profile"/>
 *                     &lt;element name="include" type="{http://www.collada.org/2005/11/COLLADASchema}fx_include_common"/>
 *                   &lt;/choice>
 *                   &lt;choice maxOccurs="unbounded" minOccurs="0">
 *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/>
 *                     &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}cg_newparam"/>
 *                     &lt;element name="setparam" type="{http://www.collada.org/2005/11/COLLADASchema}cg_setparam"/>
 *                   &lt;/choice>
 *                   &lt;element name="pass" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="color_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_colortarget_common" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="depth_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_depthtarget_common" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="stencil_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_stenciltarget_common" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="color_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearcolor_common" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="depth_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_cleardepth_common" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="stencil_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearstencil_common" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="draw" type="{http://www.collada.org/2005/11/COLLADASchema}fx_draw_common" minOccurs="0"/>
 *                             &lt;choice maxOccurs="unbounded">
 *                               &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gl_pipeline_settings"/>
 *                               &lt;element name="shader">
 *                                 &lt;complexType>
 *                                   &lt;complexContent>
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                       &lt;sequence>
 *                                         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
 *                                         &lt;sequence minOccurs="0">
 *                                           &lt;element name="compiler_target">
 *                                             &lt;complexType>
 *                                               &lt;simpleContent>
 *                                                 &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NMTOKEN">
 *                                                 &lt;/extension>
 *                                               &lt;/simpleContent>
 *                                             &lt;/complexType>
 *                                           &lt;/element>
 *                                           &lt;element name="compiler_options" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                                         &lt;/sequence>
 *                                         &lt;element name="name">
 *                                           &lt;complexType>
 *                                             &lt;simpleContent>
 *                                               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NCName">
 *                                                 &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                                               &lt;/extension>
 *                                             &lt;/simpleContent>
 *                                           &lt;/complexType>
 *                                         &lt;/element>
 *                                         &lt;element name="bind" maxOccurs="unbounded" minOccurs="0">
 *                                           &lt;complexType>
 *                                             &lt;complexContent>
 *                                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                                 &lt;choice>
 *                                                   &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}cg_param_type"/>
 *                                                   &lt;element name="param">
 *                                                     &lt;complexType>
 *                                                       &lt;complexContent>
 *                                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                                           &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                                                         &lt;/restriction>
 *                                                       &lt;/complexContent>
 *                                                     &lt;/complexType>
 *                                                   &lt;/element>
 *                                                 &lt;/choice>
 *                                                 &lt;attribute name="symbol" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                                               &lt;/restriction>
 *                                             &lt;/complexContent>
 *                                           &lt;/complexType>
 *                                         &lt;/element>
 *                                       &lt;/sequence>
 *                                       &lt;attribute name="stage" type="{http://www.collada.org/2005/11/COLLADASchema}cg_pipeline_stage" />
 *                                     &lt;/restriction>
 *                                   &lt;/complexContent>
 *                                 &lt;/complexType>
 *                               &lt;/element>
 *                             &lt;/choice>
 *                             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="sid" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="platform" type="{http://www.w3.org/2001/XMLSchema}NCName" default="PC" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "asset",
    "codeOrInclude",
    "imageOrNewparam",
    "technique",
    "extra"
})
public class ProfileCG {

    protected Asset asset;
    @XmlElements({
        @XmlElement(name = "code", type = FxCodeProfile.class),
        @XmlElement(name = "include", type = FxIncludeCommon.class)
    })
    protected List<Object> codeOrInclude;
    @XmlElements({
        @XmlElement(name = "image", type = Image.class),
        @XmlElement(name = "newparam", type = CgNewparam.class)
    })
    protected List<Object> imageOrNewparam;
    @XmlElement(required = true)
    protected List<ProfileCG.Technique> technique;
    protected List<Extra> extra;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "platform")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String platform;

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
     * Gets the value of the codeOrInclude property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeOrInclude property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeOrInclude().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FxCodeProfile }
     * {@link FxIncludeCommon }
     * 
     * 
     */
    public List<Object> getCodeOrInclude() {
        if (codeOrInclude == null) {
            codeOrInclude = new ArrayList<Object>();
        }
        return this.codeOrInclude;
    }

    public boolean isSetCodeOrInclude() {
        return ((this.codeOrInclude!= null)&&(!this.codeOrInclude.isEmpty()));
    }

    public void unsetCodeOrInclude() {
        this.codeOrInclude = null;
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
     * {@link CgNewparam }
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
     * Gets the value of the technique property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the technique property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTechnique().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProfileCG.Technique }
     * 
     * 
     */
    public List<ProfileCG.Technique> getTechnique() {
        if (technique == null) {
            technique = new ArrayList<ProfileCG.Technique>();
        }
        return this.technique;
    }

    public boolean isSetTechnique() {
        return ((this.technique!= null)&&(!this.technique.isEmpty()));
    }

    public void unsetTechnique() {
        this.technique = null;
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
     * Ruft den Wert der platform-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlatform() {
        if (platform == null) {
            return "PC";
        } else {
            return platform;
        }
    }

    /**
     * Legt den Wert der platform-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlatform(String value) {
        this.platform = value;
    }

    public boolean isSetPlatform() {
        return (this.platform!= null);
    }

    public void setCodeOrInclude(List<Object> value) {
        this.codeOrInclude = value;
    }

    public void setImageOrNewparam(List<Object> value) {
        this.imageOrNewparam = value;
    }

    public void setTechnique(List<ProfileCG.Technique> value) {
        this.technique = value;
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
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/>
     *         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;choice maxOccurs="unbounded" minOccurs="0">
     *           &lt;element name="code" type="{http://www.collada.org/2005/11/COLLADASchema}fx_code_profile"/>
     *           &lt;element name="include" type="{http://www.collada.org/2005/11/COLLADASchema}fx_include_common"/>
     *         &lt;/choice>
     *         &lt;choice maxOccurs="unbounded" minOccurs="0">
     *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/>
     *           &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}cg_newparam"/>
     *           &lt;element name="setparam" type="{http://www.collada.org/2005/11/COLLADASchema}cg_setparam"/>
     *         &lt;/choice>
     *         &lt;element name="pass" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
     *                   &lt;element name="color_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_colortarget_common" maxOccurs="unbounded" minOccurs="0"/>
     *                   &lt;element name="depth_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_depthtarget_common" maxOccurs="unbounded" minOccurs="0"/>
     *                   &lt;element name="stencil_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_stenciltarget_common" maxOccurs="unbounded" minOccurs="0"/>
     *                   &lt;element name="color_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearcolor_common" maxOccurs="unbounded" minOccurs="0"/>
     *                   &lt;element name="depth_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_cleardepth_common" maxOccurs="unbounded" minOccurs="0"/>
     *                   &lt;element name="stencil_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearstencil_common" maxOccurs="unbounded" minOccurs="0"/>
     *                   &lt;element name="draw" type="{http://www.collada.org/2005/11/COLLADASchema}fx_draw_common" minOccurs="0"/>
     *                   &lt;choice maxOccurs="unbounded">
     *                     &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gl_pipeline_settings"/>
     *                     &lt;element name="shader">
     *                       &lt;complexType>
     *                         &lt;complexContent>
     *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                             &lt;sequence>
     *                               &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
     *                               &lt;sequence minOccurs="0">
     *                                 &lt;element name="compiler_target">
     *                                   &lt;complexType>
     *                                     &lt;simpleContent>
     *                                       &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NMTOKEN">
     *                                       &lt;/extension>
     *                                     &lt;/simpleContent>
     *                                   &lt;/complexType>
     *                                 &lt;/element>
     *                                 &lt;element name="compiler_options" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                               &lt;/sequence>
     *                               &lt;element name="name">
     *                                 &lt;complexType>
     *                                   &lt;simpleContent>
     *                                     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NCName">
     *                                       &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *                                     &lt;/extension>
     *                                   &lt;/simpleContent>
     *                                 &lt;/complexType>
     *                               &lt;/element>
     *                               &lt;element name="bind" maxOccurs="unbounded" minOccurs="0">
     *                                 &lt;complexType>
     *                                   &lt;complexContent>
     *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                       &lt;choice>
     *                                         &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}cg_param_type"/>
     *                                         &lt;element name="param">
     *                                           &lt;complexType>
     *                                             &lt;complexContent>
     *                                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                                 &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *                                               &lt;/restriction>
     *                                             &lt;/complexContent>
     *                                           &lt;/complexType>
     *                                         &lt;/element>
     *                                       &lt;/choice>
     *                                       &lt;attribute name="symbol" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *                                     &lt;/restriction>
     *                                   &lt;/complexContent>
     *                                 &lt;/complexType>
     *                               &lt;/element>
     *                             &lt;/sequence>
     *                             &lt;attribute name="stage" type="{http://www.collada.org/2005/11/COLLADASchema}cg_pipeline_stage" />
     *                           &lt;/restriction>
     *                         &lt;/complexContent>
     *                       &lt;/complexType>
     *                     &lt;/element>
     *                   &lt;/choice>
     *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *                 &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="sid" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "asset",
        "annotate",
        "codeOrInclude",
        "imageOrNewparamOrSetparam",
        "pass",
        "extra"
    })
    public static class Technique {

        protected Asset asset;
        protected List<FxAnnotateCommon> annotate;
        @XmlElements({
            @XmlElement(name = "code", type = FxCodeProfile.class),
            @XmlElement(name = "include", type = FxIncludeCommon.class)
        })
        protected List<Object> codeOrInclude;
        @XmlElements({
            @XmlElement(name = "image", type = Image.class),
            @XmlElement(name = "newparam", type = CgNewparam.class),
            @XmlElement(name = "setparam", type = CgSetparam.class)
        })
        protected List<Object> imageOrNewparamOrSetparam;
        @XmlElement(required = true)
        protected List<ProfileCG.Technique.Pass> pass;
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
         * Gets the value of the codeOrInclude property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the codeOrInclude property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCodeOrInclude().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FxCodeProfile }
         * {@link FxIncludeCommon }
         * 
         * 
         */
        public List<Object> getCodeOrInclude() {
            if (codeOrInclude == null) {
                codeOrInclude = new ArrayList<Object>();
            }
            return this.codeOrInclude;
        }

        public boolean isSetCodeOrInclude() {
            return ((this.codeOrInclude!= null)&&(!this.codeOrInclude.isEmpty()));
        }

        public void unsetCodeOrInclude() {
            this.codeOrInclude = null;
        }

        /**
         * Gets the value of the imageOrNewparamOrSetparam property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the imageOrNewparamOrSetparam property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getImageOrNewparamOrSetparam().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Image }
         * {@link CgNewparam }
         * {@link CgSetparam }
         * 
         * 
         */
        public List<Object> getImageOrNewparamOrSetparam() {
            if (imageOrNewparamOrSetparam == null) {
                imageOrNewparamOrSetparam = new ArrayList<Object>();
            }
            return this.imageOrNewparamOrSetparam;
        }

        public boolean isSetImageOrNewparamOrSetparam() {
            return ((this.imageOrNewparamOrSetparam!= null)&&(!this.imageOrNewparamOrSetparam.isEmpty()));
        }

        public void unsetImageOrNewparamOrSetparam() {
            this.imageOrNewparamOrSetparam = null;
        }

        /**
         * Gets the value of the pass property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the pass property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPass().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ProfileCG.Technique.Pass }
         * 
         * 
         */
        public List<ProfileCG.Technique.Pass> getPass() {
            if (pass == null) {
                pass = new ArrayList<ProfileCG.Technique.Pass>();
            }
            return this.pass;
        }

        public boolean isSetPass() {
            return ((this.pass!= null)&&(!this.pass.isEmpty()));
        }

        public void unsetPass() {
            this.pass = null;
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

        public void setAnnotate(List<FxAnnotateCommon> value) {
            this.annotate = value;
        }

        public void setCodeOrInclude(List<Object> value) {
            this.codeOrInclude = value;
        }

        public void setImageOrNewparamOrSetparam(List<Object> value) {
            this.imageOrNewparamOrSetparam = value;
        }

        public void setPass(List<ProfileCG.Technique.Pass> value) {
            this.pass = value;
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
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
         *         &lt;element name="color_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_colortarget_common" maxOccurs="unbounded" minOccurs="0"/>
         *         &lt;element name="depth_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_depthtarget_common" maxOccurs="unbounded" minOccurs="0"/>
         *         &lt;element name="stencil_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_stenciltarget_common" maxOccurs="unbounded" minOccurs="0"/>
         *         &lt;element name="color_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearcolor_common" maxOccurs="unbounded" minOccurs="0"/>
         *         &lt;element name="depth_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_cleardepth_common" maxOccurs="unbounded" minOccurs="0"/>
         *         &lt;element name="stencil_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearstencil_common" maxOccurs="unbounded" minOccurs="0"/>
         *         &lt;element name="draw" type="{http://www.collada.org/2005/11/COLLADASchema}fx_draw_common" minOccurs="0"/>
         *         &lt;choice maxOccurs="unbounded">
         *           &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gl_pipeline_settings"/>
         *           &lt;element name="shader">
         *             &lt;complexType>
         *               &lt;complexContent>
         *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                   &lt;sequence>
         *                     &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
         *                     &lt;sequence minOccurs="0">
         *                       &lt;element name="compiler_target">
         *                         &lt;complexType>
         *                           &lt;simpleContent>
         *                             &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NMTOKEN">
         *                             &lt;/extension>
         *                           &lt;/simpleContent>
         *                         &lt;/complexType>
         *                       &lt;/element>
         *                       &lt;element name="compiler_options" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                     &lt;/sequence>
         *                     &lt;element name="name">
         *                       &lt;complexType>
         *                         &lt;simpleContent>
         *                           &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NCName">
         *                             &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" />
         *                           &lt;/extension>
         *                         &lt;/simpleContent>
         *                       &lt;/complexType>
         *                     &lt;/element>
         *                     &lt;element name="bind" maxOccurs="unbounded" minOccurs="0">
         *                       &lt;complexType>
         *                         &lt;complexContent>
         *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                             &lt;choice>
         *                               &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}cg_param_type"/>
         *                               &lt;element name="param">
         *                                 &lt;complexType>
         *                                   &lt;complexContent>
         *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                                       &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
         *                                     &lt;/restriction>
         *                                   &lt;/complexContent>
         *                                 &lt;/complexType>
         *                               &lt;/element>
         *                             &lt;/choice>
         *                             &lt;attribute name="symbol" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
         *                           &lt;/restriction>
         *                         &lt;/complexContent>
         *                       &lt;/complexType>
         *                     &lt;/element>
         *                   &lt;/sequence>
         *                   &lt;attribute name="stage" type="{http://www.collada.org/2005/11/COLLADASchema}cg_pipeline_stage" />
         *                 &lt;/restriction>
         *               &lt;/complexContent>
         *             &lt;/complexType>
         *           &lt;/element>
         *         &lt;/choice>
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
        @XmlType(name = "", propOrder = {
            "annotate",
            "colorTarget",
            "depthTarget",
            "stencilTarget",
            "colorClear",
            "depthClear",
            "stencilClear",
            "draw",
            "alphaFuncOrBlendFuncOrBlendFuncSeparate",
            "extra"
        })
        public static class Pass {

            protected List<FxAnnotateCommon> annotate;
            @XmlElement(name = "color_target")
            protected List<FxColortargetCommon> colorTarget;
            @XmlElement(name = "depth_target")
            protected List<FxDepthtargetCommon> depthTarget;
            @XmlElement(name = "stencil_target")
            protected List<FxStenciltargetCommon> stencilTarget;
            @XmlElement(name = "color_clear")
            protected List<FxClearcolorCommon> colorClear;
            @XmlElement(name = "depth_clear")
            protected List<FxCleardepthCommon> depthClear;
            @XmlElement(name = "stencil_clear")
            protected List<FxClearstencilCommon> stencilClear;
            protected String draw;
            @XmlElements({
                @XmlElement(name = "alpha_func", type = ProfileCG.Technique.Pass.AlphaFunc.class),
                @XmlElement(name = "blend_func", type = ProfileCG.Technique.Pass.BlendFunc.class),
                @XmlElement(name = "blend_func_separate", type = ProfileCG.Technique.Pass.BlendFuncSeparate.class),
                @XmlElement(name = "blend_equation", type = ProfileCG.Technique.Pass.BlendEquation.class),
                @XmlElement(name = "blend_equation_separate", type = ProfileCG.Technique.Pass.BlendEquationSeparate.class),
                @XmlElement(name = "color_material", type = ProfileCG.Technique.Pass.ColorMaterial.class),
                @XmlElement(name = "cull_face", type = ProfileCG.Technique.Pass.CullFace.class),
                @XmlElement(name = "depth_func", type = ProfileCG.Technique.Pass.DepthFunc.class),
                @XmlElement(name = "fog_mode", type = ProfileCG.Technique.Pass.FogMode.class),
                @XmlElement(name = "fog_coord_src", type = ProfileCG.Technique.Pass.FogCoordSrc.class),
                @XmlElement(name = "front_face", type = ProfileCG.Technique.Pass.FrontFace.class),
                @XmlElement(name = "light_model_color_control", type = ProfileCG.Technique.Pass.LightModelColorControl.class),
                @XmlElement(name = "logic_op", type = ProfileCG.Technique.Pass.LogicOp.class),
                @XmlElement(name = "polygon_mode", type = ProfileCG.Technique.Pass.PolygonMode.class),
                @XmlElement(name = "shade_model", type = ProfileCG.Technique.Pass.ShadeModel.class),
                @XmlElement(name = "stencil_func", type = ProfileCG.Technique.Pass.StencilFunc.class),
                @XmlElement(name = "stencil_op", type = ProfileCG.Technique.Pass.StencilOp.class),
                @XmlElement(name = "stencil_func_separate", type = ProfileCG.Technique.Pass.StencilFuncSeparate.class),
                @XmlElement(name = "stencil_op_separate", type = ProfileCG.Technique.Pass.StencilOpSeparate.class),
                @XmlElement(name = "stencil_mask_separate", type = ProfileCG.Technique.Pass.StencilMaskSeparate.class),
                @XmlElement(name = "light_enable", type = ProfileCG.Technique.Pass.LightEnable.class),
                @XmlElement(name = "light_ambient", type = ProfileCG.Technique.Pass.LightAmbient.class),
                @XmlElement(name = "light_diffuse", type = ProfileCG.Technique.Pass.LightDiffuse.class),
                @XmlElement(name = "light_specular", type = ProfileCG.Technique.Pass.LightSpecular.class),
                @XmlElement(name = "light_position", type = ProfileCG.Technique.Pass.LightPosition.class),
                @XmlElement(name = "light_constant_attenuation", type = ProfileCG.Technique.Pass.LightConstantAttenuation.class),
                @XmlElement(name = "light_linear_attenuation", type = ProfileCG.Technique.Pass.LightLinearAttenuation.class),
                @XmlElement(name = "light_quadratic_attenuation", type = ProfileCG.Technique.Pass.LightQuadraticAttenuation.class),
                @XmlElement(name = "light_spot_cutoff", type = ProfileCG.Technique.Pass.LightSpotCutoff.class),
                @XmlElement(name = "light_spot_direction", type = ProfileCG.Technique.Pass.LightSpotDirection.class),
                @XmlElement(name = "light_spot_exponent", type = ProfileCG.Technique.Pass.LightSpotExponent.class),
                @XmlElement(name = "texture1D", type = ProfileCG.Technique.Pass.Texture1D.class),
                @XmlElement(name = "texture2D", type = ProfileCG.Technique.Pass.Texture2D.class),
                @XmlElement(name = "texture3D", type = ProfileCG.Technique.Pass.Texture3D.class),
                @XmlElement(name = "textureCUBE", type = ProfileCG.Technique.Pass.TextureCUBE.class),
                @XmlElement(name = "textureRECT", type = ProfileCG.Technique.Pass.TextureRECT.class),
                @XmlElement(name = "textureDEPTH", type = ProfileCG.Technique.Pass.TextureDEPTH.class),
                @XmlElement(name = "texture1D_enable", type = ProfileCG.Technique.Pass.Texture1DEnable.class),
                @XmlElement(name = "texture2D_enable", type = ProfileCG.Technique.Pass.Texture2DEnable.class),
                @XmlElement(name = "texture3D_enable", type = ProfileCG.Technique.Pass.Texture3DEnable.class),
                @XmlElement(name = "textureCUBE_enable", type = ProfileCG.Technique.Pass.TextureCUBEEnable.class),
                @XmlElement(name = "textureRECT_enable", type = ProfileCG.Technique.Pass.TextureRECTEnable.class),
                @XmlElement(name = "textureDEPTH_enable", type = ProfileCG.Technique.Pass.TextureDEPTHEnable.class),
                @XmlElement(name = "texture_env_color", type = ProfileCG.Technique.Pass.TextureEnvColor.class),
                @XmlElement(name = "texture_env_mode", type = ProfileCG.Technique.Pass.TextureEnvMode.class),
                @XmlElement(name = "clip_plane", type = ProfileCG.Technique.Pass.ClipPlane.class),
                @XmlElement(name = "clip_plane_enable", type = ProfileCG.Technique.Pass.ClipPlaneEnable.class),
                @XmlElement(name = "blend_color", type = ProfileCG.Technique.Pass.BlendColor.class),
                @XmlElement(name = "clear_color", type = ProfileCG.Technique.Pass.ClearColor.class),
                @XmlElement(name = "clear_stencil", type = ProfileCG.Technique.Pass.ClearStencil.class),
                @XmlElement(name = "clear_depth", type = ProfileCG.Technique.Pass.ClearDepth.class),
                @XmlElement(name = "color_mask", type = ProfileCG.Technique.Pass.ColorMask.class),
                @XmlElement(name = "depth_bounds", type = ProfileCG.Technique.Pass.DepthBounds.class),
                @XmlElement(name = "depth_mask", type = ProfileCG.Technique.Pass.DepthMask.class),
                @XmlElement(name = "depth_range", type = ProfileCG.Technique.Pass.DepthRange.class),
                @XmlElement(name = "fog_density", type = ProfileCG.Technique.Pass.FogDensity.class),
                @XmlElement(name = "fog_start", type = ProfileCG.Technique.Pass.FogStart.class),
                @XmlElement(name = "fog_end", type = ProfileCG.Technique.Pass.FogEnd.class),
                @XmlElement(name = "fog_color", type = ProfileCG.Technique.Pass.FogColor.class),
                @XmlElement(name = "light_model_ambient", type = ProfileCG.Technique.Pass.LightModelAmbient.class),
                @XmlElement(name = "lighting_enable", type = ProfileCG.Technique.Pass.LightingEnable.class),
                @XmlElement(name = "line_stipple", type = ProfileCG.Technique.Pass.LineStipple.class),
                @XmlElement(name = "line_width", type = ProfileCG.Technique.Pass.LineWidth.class),
                @XmlElement(name = "material_ambient", type = ProfileCG.Technique.Pass.MaterialAmbient.class),
                @XmlElement(name = "material_diffuse", type = ProfileCG.Technique.Pass.MaterialDiffuse.class),
                @XmlElement(name = "material_emission", type = ProfileCG.Technique.Pass.MaterialEmission.class),
                @XmlElement(name = "material_shininess", type = ProfileCG.Technique.Pass.MaterialShininess.class),
                @XmlElement(name = "material_specular", type = ProfileCG.Technique.Pass.MaterialSpecular.class),
                @XmlElement(name = "model_view_matrix", type = ProfileCG.Technique.Pass.ModelViewMatrix.class),
                @XmlElement(name = "point_distance_attenuation", type = ProfileCG.Technique.Pass.PointDistanceAttenuation.class),
                @XmlElement(name = "point_fade_threshold_size", type = ProfileCG.Technique.Pass.PointFadeThresholdSize.class),
                @XmlElement(name = "point_size", type = ProfileCG.Technique.Pass.PointSize.class),
                @XmlElement(name = "point_size_min", type = ProfileCG.Technique.Pass.PointSizeMin.class),
                @XmlElement(name = "point_size_max", type = ProfileCG.Technique.Pass.PointSizeMax.class),
                @XmlElement(name = "polygon_offset", type = ProfileCG.Technique.Pass.PolygonOffset.class),
                @XmlElement(name = "projection_matrix", type = ProfileCG.Technique.Pass.ProjectionMatrix.class),
                @XmlElement(name = "scissor", type = ProfileCG.Technique.Pass.Scissor.class),
                @XmlElement(name = "stencil_mask", type = ProfileCG.Technique.Pass.StencilMask.class),
                @XmlElement(name = "alpha_test_enable", type = ProfileCG.Technique.Pass.AlphaTestEnable.class),
                @XmlElement(name = "auto_normal_enable", type = ProfileCG.Technique.Pass.AutoNormalEnable.class),
                @XmlElement(name = "blend_enable", type = ProfileCG.Technique.Pass.BlendEnable.class),
                @XmlElement(name = "color_logic_op_enable", type = ProfileCG.Technique.Pass.ColorLogicOpEnable.class),
                @XmlElement(name = "color_material_enable", type = ProfileCG.Technique.Pass.ColorMaterialEnable.class),
                @XmlElement(name = "cull_face_enable", type = ProfileCG.Technique.Pass.CullFaceEnable.class),
                @XmlElement(name = "depth_bounds_enable", type = ProfileCG.Technique.Pass.DepthBoundsEnable.class),
                @XmlElement(name = "depth_clamp_enable", type = ProfileCG.Technique.Pass.DepthClampEnable.class),
                @XmlElement(name = "depth_test_enable", type = ProfileCG.Technique.Pass.DepthTestEnable.class),
                @XmlElement(name = "dither_enable", type = ProfileCG.Technique.Pass.DitherEnable.class),
                @XmlElement(name = "fog_enable", type = ProfileCG.Technique.Pass.FogEnable.class),
                @XmlElement(name = "light_model_local_viewer_enable", type = ProfileCG.Technique.Pass.LightModelLocalViewerEnable.class),
                @XmlElement(name = "light_model_two_side_enable", type = ProfileCG.Technique.Pass.LightModelTwoSideEnable.class),
                @XmlElement(name = "line_smooth_enable", type = ProfileCG.Technique.Pass.LineSmoothEnable.class),
                @XmlElement(name = "line_stipple_enable", type = ProfileCG.Technique.Pass.LineStippleEnable.class),
                @XmlElement(name = "logic_op_enable", type = ProfileCG.Technique.Pass.LogicOpEnable.class),
                @XmlElement(name = "multisample_enable", type = ProfileCG.Technique.Pass.MultisampleEnable.class),
                @XmlElement(name = "normalize_enable", type = ProfileCG.Technique.Pass.NormalizeEnable.class),
                @XmlElement(name = "point_smooth_enable", type = ProfileCG.Technique.Pass.PointSmoothEnable.class),
                @XmlElement(name = "polygon_offset_fill_enable", type = ProfileCG.Technique.Pass.PolygonOffsetFillEnable.class),
                @XmlElement(name = "polygon_offset_line_enable", type = ProfileCG.Technique.Pass.PolygonOffsetLineEnable.class),
                @XmlElement(name = "polygon_offset_point_enable", type = ProfileCG.Technique.Pass.PolygonOffsetPointEnable.class),
                @XmlElement(name = "polygon_smooth_enable", type = ProfileCG.Technique.Pass.PolygonSmoothEnable.class),
                @XmlElement(name = "polygon_stipple_enable", type = ProfileCG.Technique.Pass.PolygonStippleEnable.class),
                @XmlElement(name = "rescale_normal_enable", type = ProfileCG.Technique.Pass.RescaleNormalEnable.class),
                @XmlElement(name = "sample_alpha_to_coverage_enable", type = ProfileCG.Technique.Pass.SampleAlphaToCoverageEnable.class),
                @XmlElement(name = "sample_alpha_to_one_enable", type = ProfileCG.Technique.Pass.SampleAlphaToOneEnable.class),
                @XmlElement(name = "sample_coverage_enable", type = ProfileCG.Technique.Pass.SampleCoverageEnable.class),
                @XmlElement(name = "scissor_test_enable", type = ProfileCG.Technique.Pass.ScissorTestEnable.class),
                @XmlElement(name = "stencil_test_enable", type = ProfileCG.Technique.Pass.StencilTestEnable.class),
                @XmlElement(name = "gl_hook_abstract"),
                @XmlElement(name = "shader", type = ProfileCG.Technique.Pass.Shader.class)
            })
            protected List<Object> alphaFuncOrBlendFuncOrBlendFuncSeparate;
            protected List<Extra> extra;
            @XmlAttribute(name = "sid")
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
             * Gets the value of the colorTarget property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the colorTarget property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getColorTarget().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link FxColortargetCommon }
             * 
             * 
             */
            public List<FxColortargetCommon> getColorTarget() {
                if (colorTarget == null) {
                    colorTarget = new ArrayList<FxColortargetCommon>();
                }
                return this.colorTarget;
            }

            public boolean isSetColorTarget() {
                return ((this.colorTarget!= null)&&(!this.colorTarget.isEmpty()));
            }

            public void unsetColorTarget() {
                this.colorTarget = null;
            }

            /**
             * Gets the value of the depthTarget property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the depthTarget property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getDepthTarget().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link FxDepthtargetCommon }
             * 
             * 
             */
            public List<FxDepthtargetCommon> getDepthTarget() {
                if (depthTarget == null) {
                    depthTarget = new ArrayList<FxDepthtargetCommon>();
                }
                return this.depthTarget;
            }

            public boolean isSetDepthTarget() {
                return ((this.depthTarget!= null)&&(!this.depthTarget.isEmpty()));
            }

            public void unsetDepthTarget() {
                this.depthTarget = null;
            }

            /**
             * Gets the value of the stencilTarget property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the stencilTarget property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getStencilTarget().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link FxStenciltargetCommon }
             * 
             * 
             */
            public List<FxStenciltargetCommon> getStencilTarget() {
                if (stencilTarget == null) {
                    stencilTarget = new ArrayList<FxStenciltargetCommon>();
                }
                return this.stencilTarget;
            }

            public boolean isSetStencilTarget() {
                return ((this.stencilTarget!= null)&&(!this.stencilTarget.isEmpty()));
            }

            public void unsetStencilTarget() {
                this.stencilTarget = null;
            }

            /**
             * Gets the value of the colorClear property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the colorClear property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getColorClear().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link FxClearcolorCommon }
             * 
             * 
             */
            public List<FxClearcolorCommon> getColorClear() {
                if (colorClear == null) {
                    colorClear = new ArrayList<FxClearcolorCommon>();
                }
                return this.colorClear;
            }

            public boolean isSetColorClear() {
                return ((this.colorClear!= null)&&(!this.colorClear.isEmpty()));
            }

            public void unsetColorClear() {
                this.colorClear = null;
            }

            /**
             * Gets the value of the depthClear property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the depthClear property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getDepthClear().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link FxCleardepthCommon }
             * 
             * 
             */
            public List<FxCleardepthCommon> getDepthClear() {
                if (depthClear == null) {
                    depthClear = new ArrayList<FxCleardepthCommon>();
                }
                return this.depthClear;
            }

            public boolean isSetDepthClear() {
                return ((this.depthClear!= null)&&(!this.depthClear.isEmpty()));
            }

            public void unsetDepthClear() {
                this.depthClear = null;
            }

            /**
             * Gets the value of the stencilClear property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the stencilClear property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getStencilClear().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link FxClearstencilCommon }
             * 
             * 
             */
            public List<FxClearstencilCommon> getStencilClear() {
                if (stencilClear == null) {
                    stencilClear = new ArrayList<FxClearstencilCommon>();
                }
                return this.stencilClear;
            }

            public boolean isSetStencilClear() {
                return ((this.stencilClear!= null)&&(!this.stencilClear.isEmpty()));
            }

            public void unsetStencilClear() {
                this.stencilClear = null;
            }

            /**
             * Ruft den Wert der draw-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDraw() {
                return draw;
            }

            /**
             * Legt den Wert der draw-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDraw(String value) {
                this.draw = value;
            }

            public boolean isSetDraw() {
                return (this.draw!= null);
            }

            /**
             * Gets the value of the alphaFuncOrBlendFuncOrBlendFuncSeparate property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the alphaFuncOrBlendFuncOrBlendFuncSeparate property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAlphaFuncOrBlendFuncOrBlendFuncSeparate().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link ProfileCG.Technique.Pass.AlphaFunc }
             * {@link ProfileCG.Technique.Pass.BlendFunc }
             * {@link ProfileCG.Technique.Pass.BlendFuncSeparate }
             * {@link ProfileCG.Technique.Pass.BlendEquation }
             * {@link ProfileCG.Technique.Pass.BlendEquationSeparate }
             * {@link ProfileCG.Technique.Pass.ColorMaterial }
             * {@link ProfileCG.Technique.Pass.CullFace }
             * {@link ProfileCG.Technique.Pass.DepthFunc }
             * {@link ProfileCG.Technique.Pass.FogMode }
             * {@link ProfileCG.Technique.Pass.FogCoordSrc }
             * {@link ProfileCG.Technique.Pass.FrontFace }
             * {@link ProfileCG.Technique.Pass.LightModelColorControl }
             * {@link ProfileCG.Technique.Pass.LogicOp }
             * {@link ProfileCG.Technique.Pass.PolygonMode }
             * {@link ProfileCG.Technique.Pass.ShadeModel }
             * {@link ProfileCG.Technique.Pass.StencilFunc }
             * {@link ProfileCG.Technique.Pass.StencilOp }
             * {@link ProfileCG.Technique.Pass.StencilFuncSeparate }
             * {@link ProfileCG.Technique.Pass.StencilOpSeparate }
             * {@link ProfileCG.Technique.Pass.StencilMaskSeparate }
             * {@link ProfileCG.Technique.Pass.LightEnable }
             * {@link ProfileCG.Technique.Pass.LightAmbient }
             * {@link ProfileCG.Technique.Pass.LightDiffuse }
             * {@link ProfileCG.Technique.Pass.LightSpecular }
             * {@link ProfileCG.Technique.Pass.LightPosition }
             * {@link ProfileCG.Technique.Pass.LightConstantAttenuation }
             * {@link ProfileCG.Technique.Pass.LightLinearAttenuation }
             * {@link ProfileCG.Technique.Pass.LightQuadraticAttenuation }
             * {@link ProfileCG.Technique.Pass.LightSpotCutoff }
             * {@link ProfileCG.Technique.Pass.LightSpotDirection }
             * {@link ProfileCG.Technique.Pass.LightSpotExponent }
             * {@link ProfileCG.Technique.Pass.Texture1D }
             * {@link ProfileCG.Technique.Pass.Texture2D }
             * {@link ProfileCG.Technique.Pass.Texture3D }
             * {@link ProfileCG.Technique.Pass.TextureCUBE }
             * {@link ProfileCG.Technique.Pass.TextureRECT }
             * {@link ProfileCG.Technique.Pass.TextureDEPTH }
             * {@link ProfileCG.Technique.Pass.Texture1DEnable }
             * {@link ProfileCG.Technique.Pass.Texture2DEnable }
             * {@link ProfileCG.Technique.Pass.Texture3DEnable }
             * {@link ProfileCG.Technique.Pass.TextureCUBEEnable }
             * {@link ProfileCG.Technique.Pass.TextureRECTEnable }
             * {@link ProfileCG.Technique.Pass.TextureDEPTHEnable }
             * {@link ProfileCG.Technique.Pass.TextureEnvColor }
             * {@link ProfileCG.Technique.Pass.TextureEnvMode }
             * {@link ProfileCG.Technique.Pass.ClipPlane }
             * {@link ProfileCG.Technique.Pass.ClipPlaneEnable }
             * {@link ProfileCG.Technique.Pass.BlendColor }
             * {@link ProfileCG.Technique.Pass.ClearColor }
             * {@link ProfileCG.Technique.Pass.ClearStencil }
             * {@link ProfileCG.Technique.Pass.ClearDepth }
             * {@link ProfileCG.Technique.Pass.ColorMask }
             * {@link ProfileCG.Technique.Pass.DepthBounds }
             * {@link ProfileCG.Technique.Pass.DepthMask }
             * {@link ProfileCG.Technique.Pass.DepthRange }
             * {@link ProfileCG.Technique.Pass.FogDensity }
             * {@link ProfileCG.Technique.Pass.FogStart }
             * {@link ProfileCG.Technique.Pass.FogEnd }
             * {@link ProfileCG.Technique.Pass.FogColor }
             * {@link ProfileCG.Technique.Pass.LightModelAmbient }
             * {@link ProfileCG.Technique.Pass.LightingEnable }
             * {@link ProfileCG.Technique.Pass.LineStipple }
             * {@link ProfileCG.Technique.Pass.LineWidth }
             * {@link ProfileCG.Technique.Pass.MaterialAmbient }
             * {@link ProfileCG.Technique.Pass.MaterialDiffuse }
             * {@link ProfileCG.Technique.Pass.MaterialEmission }
             * {@link ProfileCG.Technique.Pass.MaterialShininess }
             * {@link ProfileCG.Technique.Pass.MaterialSpecular }
             * {@link ProfileCG.Technique.Pass.ModelViewMatrix }
             * {@link ProfileCG.Technique.Pass.PointDistanceAttenuation }
             * {@link ProfileCG.Technique.Pass.PointFadeThresholdSize }
             * {@link ProfileCG.Technique.Pass.PointSize }
             * {@link ProfileCG.Technique.Pass.PointSizeMin }
             * {@link ProfileCG.Technique.Pass.PointSizeMax }
             * {@link ProfileCG.Technique.Pass.PolygonOffset }
             * {@link ProfileCG.Technique.Pass.ProjectionMatrix }
             * {@link ProfileCG.Technique.Pass.Scissor }
             * {@link ProfileCG.Technique.Pass.StencilMask }
             * {@link ProfileCG.Technique.Pass.AlphaTestEnable }
             * {@link ProfileCG.Technique.Pass.AutoNormalEnable }
             * {@link ProfileCG.Technique.Pass.BlendEnable }
             * {@link ProfileCG.Technique.Pass.ColorLogicOpEnable }
             * {@link ProfileCG.Technique.Pass.ColorMaterialEnable }
             * {@link ProfileCG.Technique.Pass.CullFaceEnable }
             * {@link ProfileCG.Technique.Pass.DepthBoundsEnable }
             * {@link ProfileCG.Technique.Pass.DepthClampEnable }
             * {@link ProfileCG.Technique.Pass.DepthTestEnable }
             * {@link ProfileCG.Technique.Pass.DitherEnable }
             * {@link ProfileCG.Technique.Pass.FogEnable }
             * {@link ProfileCG.Technique.Pass.LightModelLocalViewerEnable }
             * {@link ProfileCG.Technique.Pass.LightModelTwoSideEnable }
             * {@link ProfileCG.Technique.Pass.LineSmoothEnable }
             * {@link ProfileCG.Technique.Pass.LineStippleEnable }
             * {@link ProfileCG.Technique.Pass.LogicOpEnable }
             * {@link ProfileCG.Technique.Pass.MultisampleEnable }
             * {@link ProfileCG.Technique.Pass.NormalizeEnable }
             * {@link ProfileCG.Technique.Pass.PointSmoothEnable }
             * {@link ProfileCG.Technique.Pass.PolygonOffsetFillEnable }
             * {@link ProfileCG.Technique.Pass.PolygonOffsetLineEnable }
             * {@link ProfileCG.Technique.Pass.PolygonOffsetPointEnable }
             * {@link ProfileCG.Technique.Pass.PolygonSmoothEnable }
             * {@link ProfileCG.Technique.Pass.PolygonStippleEnable }
             * {@link ProfileCG.Technique.Pass.RescaleNormalEnable }
             * {@link ProfileCG.Technique.Pass.SampleAlphaToCoverageEnable }
             * {@link ProfileCG.Technique.Pass.SampleAlphaToOneEnable }
             * {@link ProfileCG.Technique.Pass.SampleCoverageEnable }
             * {@link ProfileCG.Technique.Pass.ScissorTestEnable }
             * {@link ProfileCG.Technique.Pass.StencilTestEnable }
             * {@link Object }
             * {@link ProfileCG.Technique.Pass.Shader }
             * 
             * 
             */
            public List<Object> getAlphaFuncOrBlendFuncOrBlendFuncSeparate() {
                if (alphaFuncOrBlendFuncOrBlendFuncSeparate == null) {
                    alphaFuncOrBlendFuncOrBlendFuncSeparate = new ArrayList<Object>();
                }
                return this.alphaFuncOrBlendFuncOrBlendFuncSeparate;
            }

            public boolean isSetAlphaFuncOrBlendFuncOrBlendFuncSeparate() {
                return ((this.alphaFuncOrBlendFuncOrBlendFuncSeparate!= null)&&(!this.alphaFuncOrBlendFuncOrBlendFuncSeparate.isEmpty()));
            }

            public void unsetAlphaFuncOrBlendFuncOrBlendFuncSeparate() {
                this.alphaFuncOrBlendFuncOrBlendFuncSeparate = null;
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

            public void setColorTarget(List<FxColortargetCommon> value) {
                this.colorTarget = value;
            }

            public void setDepthTarget(List<FxDepthtargetCommon> value) {
                this.depthTarget = value;
            }

            public void setStencilTarget(List<FxStenciltargetCommon> value) {
                this.stencilTarget = value;
            }

            public void setColorClear(List<FxClearcolorCommon> value) {
                this.colorClear = value;
            }

            public void setDepthClear(List<FxCleardepthCommon> value) {
                this.depthClear = value;
            }

            public void setStencilClear(List<FxClearstencilCommon> value) {
                this.stencilClear = value;
            }

            public void setAlphaFuncOrBlendFuncOrBlendFuncSeparate(List<Object> value) {
                this.alphaFuncOrBlendFuncOrBlendFuncSeparate = value;
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
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="func">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_func_type" default="ALWAYS" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="value">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_alpha_value_type" default="0.0" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "func",
                "value"
            })
            public static class AlphaFunc {

                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.AlphaFunc.Func func;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.AlphaFunc.Value value;

                /**
                 * Ruft den Wert der func-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.AlphaFunc.Func }
                 *     
                 */
                public ProfileCG.Technique.Pass.AlphaFunc.Func getFunc() {
                    return func;
                }

                /**
                 * Legt den Wert der func-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.AlphaFunc.Func }
                 *     
                 */
                public void setFunc(ProfileCG.Technique.Pass.AlphaFunc.Func value) {
                    this.func = value;
                }

                public boolean isSetFunc() {
                    return (this.func!= null);
                }

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.AlphaFunc.Value }
                 *     
                 */
                public ProfileCG.Technique.Pass.AlphaFunc.Value getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.AlphaFunc.Value }
                 *     
                 */
                public void setValue(ProfileCG.Technique.Pass.AlphaFunc.Value value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_func_type" default="ALWAYS" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Func {

                    @XmlAttribute(name = "value")
                    protected GlFuncType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlFuncType }
                     *     
                     */
                    public GlFuncType getValue() {
                        if (value == null) {
                            return GlFuncType.ALWAYS;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlFuncType }
                     *     
                     */
                    public void setValue(GlFuncType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_alpha_value_type" default="0.0" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Value {

                    @XmlAttribute(name = "value")
                    protected Float value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link Float }
                     *     
                     */
                    public float getValue() {
                        if (value == null) {
                            return  0.0F;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link Float }
                     *     
                     */
                    public void setValue(float value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    public void unsetValue() {
                        this.value = null;
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class AlphaTestEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class AutoNormalEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0 0 0 0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class BlendColor {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class BlendEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_equation_type" default="FUNC_ADD" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class BlendEquation {

                @XmlAttribute(name = "value")
                protected GlBlendEquationType value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlBlendEquationType }
                 *     
                 */
                public GlBlendEquationType getValue() {
                    if (value == null) {
                        return GlBlendEquationType.FUNC_ADD;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlBlendEquationType }
                 *     
                 */
                public void setValue(GlBlendEquationType value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="rgb">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_equation_type" default="FUNC_ADD" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="alpha">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_equation_type" default="FUNC_ADD" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "rgb",
                "alpha"
            })
            public static class BlendEquationSeparate {

                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.BlendEquationSeparate.Rgb rgb;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.BlendEquationSeparate.Alpha alpha;

                /**
                 * Ruft den Wert der rgb-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.BlendEquationSeparate.Rgb }
                 *     
                 */
                public ProfileCG.Technique.Pass.BlendEquationSeparate.Rgb getRgb() {
                    return rgb;
                }

                /**
                 * Legt den Wert der rgb-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.BlendEquationSeparate.Rgb }
                 *     
                 */
                public void setRgb(ProfileCG.Technique.Pass.BlendEquationSeparate.Rgb value) {
                    this.rgb = value;
                }

                public boolean isSetRgb() {
                    return (this.rgb!= null);
                }

                /**
                 * Ruft den Wert der alpha-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.BlendEquationSeparate.Alpha }
                 *     
                 */
                public ProfileCG.Technique.Pass.BlendEquationSeparate.Alpha getAlpha() {
                    return alpha;
                }

                /**
                 * Legt den Wert der alpha-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.BlendEquationSeparate.Alpha }
                 *     
                 */
                public void setAlpha(ProfileCG.Technique.Pass.BlendEquationSeparate.Alpha value) {
                    this.alpha = value;
                }

                public boolean isSetAlpha() {
                    return (this.alpha!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_equation_type" default="FUNC_ADD" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Alpha {

                    @XmlAttribute(name = "value")
                    protected GlBlendEquationType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlBlendEquationType }
                     *     
                     */
                    public GlBlendEquationType getValue() {
                        if (value == null) {
                            return GlBlendEquationType.FUNC_ADD;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlBlendEquationType }
                     *     
                     */
                    public void setValue(GlBlendEquationType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_equation_type" default="FUNC_ADD" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Rgb {

                    @XmlAttribute(name = "value")
                    protected GlBlendEquationType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlBlendEquationType }
                     *     
                     */
                    public GlBlendEquationType getValue() {
                        if (value == null) {
                            return GlBlendEquationType.FUNC_ADD;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlBlendEquationType }
                     *     
                     */
                    public void setValue(GlBlendEquationType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="src">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ONE" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="dest">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ZERO" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "src",
                "dest"
            })
            public static class BlendFunc {

                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.BlendFunc.Src src;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.BlendFunc.Dest dest;

                /**
                 * Ruft den Wert der src-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.BlendFunc.Src }
                 *     
                 */
                public ProfileCG.Technique.Pass.BlendFunc.Src getSrc() {
                    return src;
                }

                /**
                 * Legt den Wert der src-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.BlendFunc.Src }
                 *     
                 */
                public void setSrc(ProfileCG.Technique.Pass.BlendFunc.Src value) {
                    this.src = value;
                }

                public boolean isSetSrc() {
                    return (this.src!= null);
                }

                /**
                 * Ruft den Wert der dest-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.BlendFunc.Dest }
                 *     
                 */
                public ProfileCG.Technique.Pass.BlendFunc.Dest getDest() {
                    return dest;
                }

                /**
                 * Legt den Wert der dest-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.BlendFunc.Dest }
                 *     
                 */
                public void setDest(ProfileCG.Technique.Pass.BlendFunc.Dest value) {
                    this.dest = value;
                }

                public boolean isSetDest() {
                    return (this.dest!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ZERO" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Dest {

                    @XmlAttribute(name = "value")
                    protected GlBlendType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public GlBlendType getValue() {
                        if (value == null) {
                            return GlBlendType.ZERO;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public void setValue(GlBlendType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ONE" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Src {

                    @XmlAttribute(name = "value")
                    protected GlBlendType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public GlBlendType getValue() {
                        if (value == null) {
                            return GlBlendType.ONE;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public void setValue(GlBlendType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="src_rgb">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ONE" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="dest_rgb">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ZERO" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="src_alpha">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ONE" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="dest_alpha">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ZERO" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "srcRgb",
                "destRgb",
                "srcAlpha",
                "destAlpha"
            })
            public static class BlendFuncSeparate {

                @XmlElement(name = "src_rgb", required = true)
                protected ProfileCG.Technique.Pass.BlendFuncSeparate.SrcRgb srcRgb;
                @XmlElement(name = "dest_rgb", required = true)
                protected ProfileCG.Technique.Pass.BlendFuncSeparate.DestRgb destRgb;
                @XmlElement(name = "src_alpha", required = true)
                protected ProfileCG.Technique.Pass.BlendFuncSeparate.SrcAlpha srcAlpha;
                @XmlElement(name = "dest_alpha", required = true)
                protected ProfileCG.Technique.Pass.BlendFuncSeparate.DestAlpha destAlpha;

                /**
                 * Ruft den Wert der srcRgb-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.BlendFuncSeparate.SrcRgb }
                 *     
                 */
                public ProfileCG.Technique.Pass.BlendFuncSeparate.SrcRgb getSrcRgb() {
                    return srcRgb;
                }

                /**
                 * Legt den Wert der srcRgb-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.BlendFuncSeparate.SrcRgb }
                 *     
                 */
                public void setSrcRgb(ProfileCG.Technique.Pass.BlendFuncSeparate.SrcRgb value) {
                    this.srcRgb = value;
                }

                public boolean isSetSrcRgb() {
                    return (this.srcRgb!= null);
                }

                /**
                 * Ruft den Wert der destRgb-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.BlendFuncSeparate.DestRgb }
                 *     
                 */
                public ProfileCG.Technique.Pass.BlendFuncSeparate.DestRgb getDestRgb() {
                    return destRgb;
                }

                /**
                 * Legt den Wert der destRgb-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.BlendFuncSeparate.DestRgb }
                 *     
                 */
                public void setDestRgb(ProfileCG.Technique.Pass.BlendFuncSeparate.DestRgb value) {
                    this.destRgb = value;
                }

                public boolean isSetDestRgb() {
                    return (this.destRgb!= null);
                }

                /**
                 * Ruft den Wert der srcAlpha-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.BlendFuncSeparate.SrcAlpha }
                 *     
                 */
                public ProfileCG.Technique.Pass.BlendFuncSeparate.SrcAlpha getSrcAlpha() {
                    return srcAlpha;
                }

                /**
                 * Legt den Wert der srcAlpha-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.BlendFuncSeparate.SrcAlpha }
                 *     
                 */
                public void setSrcAlpha(ProfileCG.Technique.Pass.BlendFuncSeparate.SrcAlpha value) {
                    this.srcAlpha = value;
                }

                public boolean isSetSrcAlpha() {
                    return (this.srcAlpha!= null);
                }

                /**
                 * Ruft den Wert der destAlpha-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.BlendFuncSeparate.DestAlpha }
                 *     
                 */
                public ProfileCG.Technique.Pass.BlendFuncSeparate.DestAlpha getDestAlpha() {
                    return destAlpha;
                }

                /**
                 * Legt den Wert der destAlpha-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.BlendFuncSeparate.DestAlpha }
                 *     
                 */
                public void setDestAlpha(ProfileCG.Technique.Pass.BlendFuncSeparate.DestAlpha value) {
                    this.destAlpha = value;
                }

                public boolean isSetDestAlpha() {
                    return (this.destAlpha!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ZERO" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class DestAlpha {

                    @XmlAttribute(name = "value")
                    protected GlBlendType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public GlBlendType getValue() {
                        if (value == null) {
                            return GlBlendType.ZERO;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public void setValue(GlBlendType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ZERO" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class DestRgb {

                    @XmlAttribute(name = "value")
                    protected GlBlendType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public GlBlendType getValue() {
                        if (value == null) {
                            return GlBlendType.ZERO;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public void setValue(GlBlendType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ONE" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class SrcAlpha {

                    @XmlAttribute(name = "value")
                    protected GlBlendType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public GlBlendType getValue() {
                        if (value == null) {
                            return GlBlendType.ONE;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public void setValue(GlBlendType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_blend_type" default="ONE" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class SrcRgb {

                    @XmlAttribute(name = "value")
                    protected GlBlendType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public GlBlendType getValue() {
                        if (value == null) {
                            return GlBlendType.ONE;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlBlendType }
                     *     
                     */
                    public void setValue(GlBlendType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0 0 0 0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ClearColor {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ClearDepth {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  1.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}int" default="0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ClearStencil {

                @XmlAttribute(name = "value")
                protected Long value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Long }
                 *     
                 */
                public long getValue() {
                    if (value == null) {
                        return  0L;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Long }
                 *     
                 */
                public void setValue(long value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0 0 0 0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_CLIP_PLANES_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ClipPlane {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index")
                protected BigInteger index;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_CLIP_PLANES_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ClipPlaneEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index")
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ColorLogicOpEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool4" default="true true true true" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ColorMask {

                @XmlAttribute(name = "value")
                protected List<Boolean> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Boolean }
                 * 
                 * 
                 */
                public List<Boolean> getValue() {
                    if (value == null) {
                        value = new ArrayList<Boolean>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Boolean> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="face">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_face_type" default="FRONT_AND_BACK" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="mode">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_material_type" default="AMBIENT_AND_DIFFUSE" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "face",
                "mode"
            })
            public static class ColorMaterial {

                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.ColorMaterial.Face face;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.ColorMaterial.Mode mode;

                /**
                 * Ruft den Wert der face-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.ColorMaterial.Face }
                 *     
                 */
                public ProfileCG.Technique.Pass.ColorMaterial.Face getFace() {
                    return face;
                }

                /**
                 * Legt den Wert der face-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.ColorMaterial.Face }
                 *     
                 */
                public void setFace(ProfileCG.Technique.Pass.ColorMaterial.Face value) {
                    this.face = value;
                }

                public boolean isSetFace() {
                    return (this.face!= null);
                }

                /**
                 * Ruft den Wert der mode-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.ColorMaterial.Mode }
                 *     
                 */
                public ProfileCG.Technique.Pass.ColorMaterial.Mode getMode() {
                    return mode;
                }

                /**
                 * Legt den Wert der mode-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.ColorMaterial.Mode }
                 *     
                 */
                public void setMode(ProfileCG.Technique.Pass.ColorMaterial.Mode value) {
                    this.mode = value;
                }

                public boolean isSetMode() {
                    return (this.mode!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_face_type" default="FRONT_AND_BACK" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Face {

                    @XmlAttribute(name = "value")
                    protected GlFaceType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlFaceType }
                     *     
                     */
                    public GlFaceType getValue() {
                        if (value == null) {
                            return GlFaceType.FRONT_AND_BACK;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlFaceType }
                     *     
                     */
                    public void setValue(GlFaceType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_material_type" default="AMBIENT_AND_DIFFUSE" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Mode {

                    @XmlAttribute(name = "value")
                    protected GlMaterialType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlMaterialType }
                     *     
                     */
                    public GlMaterialType getValue() {
                        if (value == null) {
                            return GlMaterialType.AMBIENT_AND_DIFFUSE;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlMaterialType }
                     *     
                     */
                    public void setValue(GlMaterialType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="true" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ColorMaterialEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return true;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_face_type" default="BACK" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class CullFace {

                @XmlAttribute(name = "value")
                protected GlFaceType value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlFaceType }
                 *     
                 */
                public GlFaceType getValue() {
                    if (value == null) {
                        return GlFaceType.BACK;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlFaceType }
                 *     
                 */
                public void setValue(GlFaceType value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class CullFaceEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float2" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class DepthBounds {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class DepthBoundsEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class DepthClampEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_func_type" default="ALWAYS" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class DepthFunc {

                @XmlAttribute(name = "value")
                protected GlFuncType value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlFuncType }
                 *     
                 */
                public GlFuncType getValue() {
                    if (value == null) {
                        return GlFuncType.ALWAYS;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlFuncType }
                 *     
                 */
                public void setValue(GlFuncType value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="true" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class DepthMask {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return true;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float2" default="0 1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class DepthRange {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class DepthTestEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="true" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class DitherEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return true;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0 0 0 0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class FogColor {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_fog_coord_src_type" default="FOG_COORDINATE" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class FogCoordSrc {

                @XmlAttribute(name = "value")
                protected GlFogCoordSrcType value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlFogCoordSrcType }
                 *     
                 */
                public GlFogCoordSrcType getValue() {
                    if (value == null) {
                        return GlFogCoordSrcType.FOG_COORDINATE;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlFogCoordSrcType }
                 *     
                 */
                public void setValue(GlFogCoordSrcType value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class FogDensity {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  1.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class FogEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class FogEnd {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  1.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_fog_type" default="EXP" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class FogMode {

                @XmlAttribute(name = "value")
                protected GlFogType value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlFogType }
                 *     
                 */
                public GlFogType getValue() {
                    if (value == null) {
                        return GlFogType.EXP;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlFogType }
                 *     
                 */
                public void setValue(GlFogType value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class FogStart {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  0.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_front_face_type" default="CCW" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class FrontFace {

                @XmlAttribute(name = "value")
                protected GlFrontFaceType value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlFrontFaceType }
                 *     
                 */
                public GlFrontFaceType getValue() {
                    if (value == null) {
                        return GlFrontFaceType.CCW;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlFrontFaceType }
                 *     
                 */
                public void setValue(GlFrontFaceType value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0 0 0 1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightAmbient {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightConstantAttenuation {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  1.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0 0 0 0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightDiffuse {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightLinearAttenuation {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  0.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0.2 0.2 0.2 1.0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightModelAmbient {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_light_model_color_control_type" default="SINGLE_COLOR" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightModelColorControl {

                @XmlAttribute(name = "value")
                protected GlLightModelColorControlType value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlLightModelColorControlType }
                 *     
                 */
                public GlLightModelColorControlType getValue() {
                    if (value == null) {
                        return GlLightModelColorControlType.SINGLE_COLOR;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlLightModelColorControlType }
                 *     
                 */
                public void setValue(GlLightModelColorControlType value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightModelLocalViewerEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightModelTwoSideEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0 0 1 0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightPosition {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightQuadraticAttenuation {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  0.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0 0 0 0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightSpecular {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="180" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightSpotCutoff {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  180.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float3" default="0 0 -1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightSpotDirection {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightSpotExponent {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  0.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightingEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LineSmoothEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}int2" default="1 65536" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LineStipple {

                @XmlAttribute(name = "value")
                protected List<Long> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Long }
                 * 
                 * 
                 */
                public List<Long> getValue() {
                    if (value == null) {
                        value = new ArrayList<Long>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Long> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LineStippleEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LineWidth {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  1.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_logic_op_type" default="COPY" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LogicOp {

                @XmlAttribute(name = "value")
                protected GlLogicOpType value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlLogicOpType }
                 *     
                 */
                public GlLogicOpType getValue() {
                    if (value == null) {
                        return GlLogicOpType.COPY;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlLogicOpType }
                 *     
                 */
                public void setValue(GlLogicOpType value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LogicOpEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0.2 0.2 0.2 1.0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class MaterialAmbient {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0.8 0.8 0.8 1.0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class MaterialDiffuse {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0 0 0 1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class MaterialEmission {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class MaterialShininess {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  0.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" default="0 0 0 1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class MaterialSpecular {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4x4" default="1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ModelViewMatrix {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class MultisampleEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class NormalizeEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float3" default="1 0 0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PointDistanceAttenuation {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PointFadeThresholdSize {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  1.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PointSize {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  1.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PointSizeMax {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  1.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" default="0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PointSizeMin {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Double }
                 *     
                 */
                public double getValue() {
                    if (value == null) {
                        return  0.0D;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Double }
                 *     
                 */
                public void setValue(double value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PointSmoothEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="face">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_face_type" default="FRONT_AND_BACK" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="mode">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_polygon_mode_type" default="FILL" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "face",
                "mode"
            })
            public static class PolygonMode {

                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.PolygonMode.Face face;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.PolygonMode.Mode mode;

                /**
                 * Ruft den Wert der face-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.PolygonMode.Face }
                 *     
                 */
                public ProfileCG.Technique.Pass.PolygonMode.Face getFace() {
                    return face;
                }

                /**
                 * Legt den Wert der face-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.PolygonMode.Face }
                 *     
                 */
                public void setFace(ProfileCG.Technique.Pass.PolygonMode.Face value) {
                    this.face = value;
                }

                public boolean isSetFace() {
                    return (this.face!= null);
                }

                /**
                 * Ruft den Wert der mode-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.PolygonMode.Mode }
                 *     
                 */
                public ProfileCG.Technique.Pass.PolygonMode.Mode getMode() {
                    return mode;
                }

                /**
                 * Legt den Wert der mode-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.PolygonMode.Mode }
                 *     
                 */
                public void setMode(ProfileCG.Technique.Pass.PolygonMode.Mode value) {
                    this.mode = value;
                }

                public boolean isSetMode() {
                    return (this.mode!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_face_type" default="FRONT_AND_BACK" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Face {

                    @XmlAttribute(name = "value")
                    protected GlFaceType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlFaceType }
                     *     
                     */
                    public GlFaceType getValue() {
                        if (value == null) {
                            return GlFaceType.FRONT_AND_BACK;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlFaceType }
                     *     
                     */
                    public void setValue(GlFaceType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_polygon_mode_type" default="FILL" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Mode {

                    @XmlAttribute(name = "value")
                    protected GlPolygonModeType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlPolygonModeType }
                     *     
                     */
                    public GlPolygonModeType getValue() {
                        if (value == null) {
                            return GlPolygonModeType.FILL;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlPolygonModeType }
                     *     
                     */
                    public void setValue(GlPolygonModeType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float2" default="0 0" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PolygonOffset {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PolygonOffsetFillEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PolygonOffsetLineEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PolygonOffsetPointEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PolygonSmoothEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class PolygonStippleEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4x4" default="1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 1" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ProjectionMatrix {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class RescaleNormalEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class SampleAlphaToCoverageEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class SampleAlphaToOneEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class SampleCoverageEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}int4" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Scissor {

                @XmlAttribute(name = "value")
                protected List<Long> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Long }
                 * 
                 * 
                 */
                public List<Long> getValue() {
                    if (value == null) {
                        value = new ArrayList<Long>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                public void setValue(List<Long> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ScissorTestEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_shade_model_type" default="SMOOTH" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class ShadeModel {

                @XmlAttribute(name = "value")
                protected GlShadeModelType value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlShadeModelType }
                 *     
                 */
                public GlShadeModelType getValue() {
                    if (value == null) {
                        return GlShadeModelType.SMOOTH;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlShadeModelType }
                 *     
                 */
                public void setValue(GlShadeModelType value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
             *         &lt;sequence minOccurs="0">
             *           &lt;element name="compiler_target">
             *             &lt;complexType>
             *               &lt;simpleContent>
             *                 &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NMTOKEN">
             *                 &lt;/extension>
             *               &lt;/simpleContent>
             *             &lt;/complexType>
             *           &lt;/element>
             *           &lt;element name="compiler_options" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         &lt;/sequence>
             *         &lt;element name="name">
             *           &lt;complexType>
             *             &lt;simpleContent>
             *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NCName">
             *                 &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/extension>
             *             &lt;/simpleContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="bind" maxOccurs="unbounded" minOccurs="0">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;choice>
             *                   &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}cg_param_type"/>
             *                   &lt;element name="param">
             *                     &lt;complexType>
             *                       &lt;complexContent>
             *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                           &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *                         &lt;/restriction>
             *                       &lt;/complexContent>
             *                     &lt;/complexType>
             *                   &lt;/element>
             *                 &lt;/choice>
             *                 &lt;attribute name="symbol" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *       &lt;attribute name="stage" type="{http://www.collada.org/2005/11/COLLADASchema}cg_pipeline_stage" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "annotate",
                "compilerTarget",
                "compilerOptions",
                "name",
                "bind"
            })
            public static class Shader {

                protected List<FxAnnotateCommon> annotate;
                @XmlElement(name = "compiler_target")
                protected ProfileCG.Technique.Pass.Shader.CompilerTarget compilerTarget;
                @XmlElement(name = "compiler_options")
                protected String compilerOptions;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.Shader.Name name;
                protected List<ProfileCG.Technique.Pass.Shader.Bind> bind;
                @XmlAttribute(name = "stage")
                protected CgPipelineStage stage;

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
                 * Ruft den Wert der compilerTarget-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.Shader.CompilerTarget }
                 *     
                 */
                public ProfileCG.Technique.Pass.Shader.CompilerTarget getCompilerTarget() {
                    return compilerTarget;
                }

                /**
                 * Legt den Wert der compilerTarget-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.Shader.CompilerTarget }
                 *     
                 */
                public void setCompilerTarget(ProfileCG.Technique.Pass.Shader.CompilerTarget value) {
                    this.compilerTarget = value;
                }

                public boolean isSetCompilerTarget() {
                    return (this.compilerTarget!= null);
                }

                /**
                 * Ruft den Wert der compilerOptions-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getCompilerOptions() {
                    return compilerOptions;
                }

                /**
                 * Legt den Wert der compilerOptions-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setCompilerOptions(String value) {
                    this.compilerOptions = value;
                }

                public boolean isSetCompilerOptions() {
                    return (this.compilerOptions!= null);
                }

                /**
                 * Ruft den Wert der name-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.Shader.Name }
                 *     
                 */
                public ProfileCG.Technique.Pass.Shader.Name getName() {
                    return name;
                }

                /**
                 * Legt den Wert der name-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.Shader.Name }
                 *     
                 */
                public void setName(ProfileCG.Technique.Pass.Shader.Name value) {
                    this.name = value;
                }

                public boolean isSetName() {
                    return (this.name!= null);
                }

                /**
                 * Gets the value of the bind property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the bind property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getBind().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link ProfileCG.Technique.Pass.Shader.Bind }
                 * 
                 * 
                 */
                public List<ProfileCG.Technique.Pass.Shader.Bind> getBind() {
                    if (bind == null) {
                        bind = new ArrayList<ProfileCG.Technique.Pass.Shader.Bind>();
                    }
                    return this.bind;
                }

                public boolean isSetBind() {
                    return ((this.bind!= null)&&(!this.bind.isEmpty()));
                }

                public void unsetBind() {
                    this.bind = null;
                }

                /**
                 * Ruft den Wert der stage-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link CgPipelineStage }
                 *     
                 */
                public CgPipelineStage getStage() {
                    return stage;
                }

                /**
                 * Legt den Wert der stage-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link CgPipelineStage }
                 *     
                 */
                public void setStage(CgPipelineStage value) {
                    this.stage = value;
                }

                public boolean isSetStage() {
                    return (this.stage!= null);
                }

                public void setAnnotate(List<FxAnnotateCommon> value) {
                    this.annotate = value;
                }

                public void setBind(List<ProfileCG.Technique.Pass.Shader.Bind> value) {
                    this.bind = value;
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;choice>
                 *         &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}cg_param_type"/>
                 *         &lt;element name="param">
                 *           &lt;complexType>
                 *             &lt;complexContent>
                 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *                 &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *               &lt;/restriction>
                 *             &lt;/complexContent>
                 *           &lt;/complexType>
                 *         &lt;/element>
                 *       &lt;/choice>
                 *       &lt;attribute name="symbol" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
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
                    "_enum",
                    "param"
                })
                public static class Bind {

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
                    protected ProfileCG.Technique.Pass.Shader.Bind.Param param;
                    @XmlAttribute(name = "symbol", required = true)
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String symbol;

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
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link ProfileCG.Technique.Pass.Shader.Bind.Param }
                     *     
                     */
                    public ProfileCG.Technique.Pass.Shader.Bind.Param getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link ProfileCG.Technique.Pass.Shader.Bind.Param }
                     *     
                     */
                    public void setParam(ProfileCG.Technique.Pass.Shader.Bind.Param value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                    /**
                     * Ruft den Wert der symbol-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getSymbol() {
                        return symbol;
                    }

                    /**
                     * Legt den Wert der symbol-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setSymbol(String value) {
                        this.symbol = value;
                    }

                    public boolean isSetSymbol() {
                        return (this.symbol!= null);
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


                    /**
                     * <p>Java-Klasse für anonymous complex type.
                     * 
                     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                     * 
                     * <pre>
                     * &lt;complexType>
                     *   &lt;complexContent>
                     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                     *       &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                     *     &lt;/restriction>
                     *   &lt;/complexContent>
                     * &lt;/complexType>
                     * </pre>
                     * 
                     * 
                     */
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "")
                    public static class Param {

                        @XmlAttribute(name = "ref", required = true)
                        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                        @XmlSchemaType(name = "NCName")
                        protected String ref;

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

                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;simpleContent>
                 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NMTOKEN">
                 *     &lt;/extension>
                 *   &lt;/simpleContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                    "value"
                })
                public static class CompilerTarget {

                    @XmlValue
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NMTOKEN")
                    protected String value;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getValue() {
                        return value;
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setValue(String value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;simpleContent>
                 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NCName">
                 *       &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/extension>
                 *   &lt;/simpleContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                    "value"
                })
                public static class Name {

                    @XmlValue
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String value;
                    @XmlAttribute(name = "source")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String source;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getValue() {
                        return value;
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setValue(String value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

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

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="func">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_func_type" default="ALWAYS" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="ref">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="mask">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="255" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "func",
                "ref",
                "mask"
            })
            public static class StencilFunc {

                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilFunc.Func func;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilFunc.Ref ref;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilFunc.Mask mask;

                /**
                 * Ruft den Wert der func-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilFunc.Func }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilFunc.Func getFunc() {
                    return func;
                }

                /**
                 * Legt den Wert der func-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilFunc.Func }
                 *     
                 */
                public void setFunc(ProfileCG.Technique.Pass.StencilFunc.Func value) {
                    this.func = value;
                }

                public boolean isSetFunc() {
                    return (this.func!= null);
                }

                /**
                 * Ruft den Wert der ref-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilFunc.Ref }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilFunc.Ref getRef() {
                    return ref;
                }

                /**
                 * Legt den Wert der ref-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilFunc.Ref }
                 *     
                 */
                public void setRef(ProfileCG.Technique.Pass.StencilFunc.Ref value) {
                    this.ref = value;
                }

                public boolean isSetRef() {
                    return (this.ref!= null);
                }

                /**
                 * Ruft den Wert der mask-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilFunc.Mask }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilFunc.Mask getMask() {
                    return mask;
                }

                /**
                 * Legt den Wert der mask-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilFunc.Mask }
                 *     
                 */
                public void setMask(ProfileCG.Technique.Pass.StencilFunc.Mask value) {
                    this.mask = value;
                }

                public boolean isSetMask() {
                    return (this.mask!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_func_type" default="ALWAYS" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Func {

                    @XmlAttribute(name = "value")
                    protected GlFuncType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlFuncType }
                     *     
                     */
                    public GlFuncType getValue() {
                        if (value == null) {
                            return GlFuncType.ALWAYS;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlFuncType }
                     *     
                     */
                    public void setValue(GlFuncType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="255" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Mask {

                    @XmlAttribute(name = "value")
                    @XmlSchemaType(name = "unsignedByte")
                    protected Short value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link Short }
                     *     
                     */
                    public short getValue() {
                        if (value == null) {
                            return ((short) 255);
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link Short }
                     *     
                     */
                    public void setValue(short value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    public void unsetValue() {
                        this.value = null;
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Ref {

                    @XmlAttribute(name = "value")
                    @XmlSchemaType(name = "unsignedByte")
                    protected Short value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link Short }
                     *     
                     */
                    public short getValue() {
                        if (value == null) {
                            return ((short) 0);
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link Short }
                     *     
                     */
                    public void setValue(short value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    public void unsetValue() {
                        this.value = null;
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="front">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_func_type" default="ALWAYS" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="back">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_func_type" default="ALWAYS" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="ref">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="mask">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="255" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "front",
                "back",
                "ref",
                "mask"
            })
            public static class StencilFuncSeparate {

                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilFuncSeparate.Front front;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilFuncSeparate.Back back;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilFuncSeparate.Ref ref;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilFuncSeparate.Mask mask;

                /**
                 * Ruft den Wert der front-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilFuncSeparate.Front }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilFuncSeparate.Front getFront() {
                    return front;
                }

                /**
                 * Legt den Wert der front-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilFuncSeparate.Front }
                 *     
                 */
                public void setFront(ProfileCG.Technique.Pass.StencilFuncSeparate.Front value) {
                    this.front = value;
                }

                public boolean isSetFront() {
                    return (this.front!= null);
                }

                /**
                 * Ruft den Wert der back-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilFuncSeparate.Back }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilFuncSeparate.Back getBack() {
                    return back;
                }

                /**
                 * Legt den Wert der back-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilFuncSeparate.Back }
                 *     
                 */
                public void setBack(ProfileCG.Technique.Pass.StencilFuncSeparate.Back value) {
                    this.back = value;
                }

                public boolean isSetBack() {
                    return (this.back!= null);
                }

                /**
                 * Ruft den Wert der ref-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilFuncSeparate.Ref }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilFuncSeparate.Ref getRef() {
                    return ref;
                }

                /**
                 * Legt den Wert der ref-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilFuncSeparate.Ref }
                 *     
                 */
                public void setRef(ProfileCG.Technique.Pass.StencilFuncSeparate.Ref value) {
                    this.ref = value;
                }

                public boolean isSetRef() {
                    return (this.ref!= null);
                }

                /**
                 * Ruft den Wert der mask-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilFuncSeparate.Mask }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilFuncSeparate.Mask getMask() {
                    return mask;
                }

                /**
                 * Legt den Wert der mask-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilFuncSeparate.Mask }
                 *     
                 */
                public void setMask(ProfileCG.Technique.Pass.StencilFuncSeparate.Mask value) {
                    this.mask = value;
                }

                public boolean isSetMask() {
                    return (this.mask!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_func_type" default="ALWAYS" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Back {

                    @XmlAttribute(name = "value")
                    protected GlFuncType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlFuncType }
                     *     
                     */
                    public GlFuncType getValue() {
                        if (value == null) {
                            return GlFuncType.ALWAYS;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlFuncType }
                     *     
                     */
                    public void setValue(GlFuncType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_func_type" default="ALWAYS" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Front {

                    @XmlAttribute(name = "value")
                    protected GlFuncType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlFuncType }
                     *     
                     */
                    public GlFuncType getValue() {
                        if (value == null) {
                            return GlFuncType.ALWAYS;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlFuncType }
                     *     
                     */
                    public void setValue(GlFuncType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="255" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Mask {

                    @XmlAttribute(name = "value")
                    @XmlSchemaType(name = "unsignedByte")
                    protected Short value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link Short }
                     *     
                     */
                    public short getValue() {
                        if (value == null) {
                            return ((short) 255);
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link Short }
                     *     
                     */
                    public void setValue(short value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    public void unsetValue() {
                        this.value = null;
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Ref {

                    @XmlAttribute(name = "value")
                    @XmlSchemaType(name = "unsignedByte")
                    protected Short value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link Short }
                     *     
                     */
                    public short getValue() {
                        if (value == null) {
                            return ((short) 0);
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link Short }
                     *     
                     */
                    public void setValue(short value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    public void unsetValue() {
                        this.value = null;
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}int" default="4294967295" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class StencilMask {

                @XmlAttribute(name = "value")
                protected Long value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Long }
                 *     
                 */
                public long getValue() {
                    if (value == null) {
                        return  4294967295L;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Long }
                 *     
                 */
                public void setValue(long value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="face">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_face_type" default="FRONT_AND_BACK" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="mask">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="255" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "face",
                "mask"
            })
            public static class StencilMaskSeparate {

                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilMaskSeparate.Face face;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilMaskSeparate.Mask mask;

                /**
                 * Ruft den Wert der face-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilMaskSeparate.Face }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilMaskSeparate.Face getFace() {
                    return face;
                }

                /**
                 * Legt den Wert der face-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilMaskSeparate.Face }
                 *     
                 */
                public void setFace(ProfileCG.Technique.Pass.StencilMaskSeparate.Face value) {
                    this.face = value;
                }

                public boolean isSetFace() {
                    return (this.face!= null);
                }

                /**
                 * Ruft den Wert der mask-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilMaskSeparate.Mask }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilMaskSeparate.Mask getMask() {
                    return mask;
                }

                /**
                 * Legt den Wert der mask-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilMaskSeparate.Mask }
                 *     
                 */
                public void setMask(ProfileCG.Technique.Pass.StencilMaskSeparate.Mask value) {
                    this.mask = value;
                }

                public boolean isSetMask() {
                    return (this.mask!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_face_type" default="FRONT_AND_BACK" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Face {

                    @XmlAttribute(name = "value")
                    protected GlFaceType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlFaceType }
                     *     
                     */
                    public GlFaceType getValue() {
                        if (value == null) {
                            return GlFaceType.FRONT_AND_BACK;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlFaceType }
                     *     
                     */
                    public void setValue(GlFaceType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="255" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Mask {

                    @XmlAttribute(name = "value")
                    @XmlSchemaType(name = "unsignedByte")
                    protected Short value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link Short }
                     *     
                     */
                    public short getValue() {
                        if (value == null) {
                            return ((short) 255);
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link Short }
                     *     
                     */
                    public void setValue(short value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    public void unsetValue() {
                        this.value = null;
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="fail">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="zfail">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="zpass">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "fail",
                "zfail",
                "zpass"
            })
            public static class StencilOp {

                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilOp.Fail fail;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilOp.Zfail zfail;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilOp.Zpass zpass;

                /**
                 * Ruft den Wert der fail-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilOp.Fail }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilOp.Fail getFail() {
                    return fail;
                }

                /**
                 * Legt den Wert der fail-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilOp.Fail }
                 *     
                 */
                public void setFail(ProfileCG.Technique.Pass.StencilOp.Fail value) {
                    this.fail = value;
                }

                public boolean isSetFail() {
                    return (this.fail!= null);
                }

                /**
                 * Ruft den Wert der zfail-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilOp.Zfail }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilOp.Zfail getZfail() {
                    return zfail;
                }

                /**
                 * Legt den Wert der zfail-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilOp.Zfail }
                 *     
                 */
                public void setZfail(ProfileCG.Technique.Pass.StencilOp.Zfail value) {
                    this.zfail = value;
                }

                public boolean isSetZfail() {
                    return (this.zfail!= null);
                }

                /**
                 * Ruft den Wert der zpass-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilOp.Zpass }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilOp.Zpass getZpass() {
                    return zpass;
                }

                /**
                 * Legt den Wert der zpass-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilOp.Zpass }
                 *     
                 */
                public void setZpass(ProfileCG.Technique.Pass.StencilOp.Zpass value) {
                    this.zpass = value;
                }

                public boolean isSetZpass() {
                    return (this.zpass!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Fail {

                    @XmlAttribute(name = "value")
                    protected GlStencilOpType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public GlStencilOpType getValue() {
                        if (value == null) {
                            return GlStencilOpType.KEEP;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public void setValue(GlStencilOpType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Zfail {

                    @XmlAttribute(name = "value")
                    protected GlStencilOpType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public GlStencilOpType getValue() {
                        if (value == null) {
                            return GlStencilOpType.KEEP;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public void setValue(GlStencilOpType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Zpass {

                    @XmlAttribute(name = "value")
                    protected GlStencilOpType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public GlStencilOpType getValue() {
                        if (value == null) {
                            return GlStencilOpType.KEEP;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public void setValue(GlStencilOpType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="face">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_face_type" default="FRONT_AND_BACK" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="fail">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="zfail">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="zpass">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "face",
                "fail",
                "zfail",
                "zpass"
            })
            public static class StencilOpSeparate {

                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilOpSeparate.Face face;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilOpSeparate.Fail fail;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilOpSeparate.Zfail zfail;
                @XmlElement(required = true)
                protected ProfileCG.Technique.Pass.StencilOpSeparate.Zpass zpass;

                /**
                 * Ruft den Wert der face-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilOpSeparate.Face }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilOpSeparate.Face getFace() {
                    return face;
                }

                /**
                 * Legt den Wert der face-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilOpSeparate.Face }
                 *     
                 */
                public void setFace(ProfileCG.Technique.Pass.StencilOpSeparate.Face value) {
                    this.face = value;
                }

                public boolean isSetFace() {
                    return (this.face!= null);
                }

                /**
                 * Ruft den Wert der fail-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilOpSeparate.Fail }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilOpSeparate.Fail getFail() {
                    return fail;
                }

                /**
                 * Legt den Wert der fail-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilOpSeparate.Fail }
                 *     
                 */
                public void setFail(ProfileCG.Technique.Pass.StencilOpSeparate.Fail value) {
                    this.fail = value;
                }

                public boolean isSetFail() {
                    return (this.fail!= null);
                }

                /**
                 * Ruft den Wert der zfail-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilOpSeparate.Zfail }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilOpSeparate.Zfail getZfail() {
                    return zfail;
                }

                /**
                 * Legt den Wert der zfail-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilOpSeparate.Zfail }
                 *     
                 */
                public void setZfail(ProfileCG.Technique.Pass.StencilOpSeparate.Zfail value) {
                    this.zfail = value;
                }

                public boolean isSetZfail() {
                    return (this.zfail!= null);
                }

                /**
                 * Ruft den Wert der zpass-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileCG.Technique.Pass.StencilOpSeparate.Zpass }
                 *     
                 */
                public ProfileCG.Technique.Pass.StencilOpSeparate.Zpass getZpass() {
                    return zpass;
                }

                /**
                 * Legt den Wert der zpass-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileCG.Technique.Pass.StencilOpSeparate.Zpass }
                 *     
                 */
                public void setZpass(ProfileCG.Technique.Pass.StencilOpSeparate.Zpass value) {
                    this.zpass = value;
                }

                public boolean isSetZpass() {
                    return (this.zpass!= null);
                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_face_type" default="FRONT_AND_BACK" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Face {

                    @XmlAttribute(name = "value")
                    protected GlFaceType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlFaceType }
                     *     
                     */
                    public GlFaceType getValue() {
                        if (value == null) {
                            return GlFaceType.FRONT_AND_BACK;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlFaceType }
                     *     
                     */
                    public void setValue(GlFaceType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Fail {

                    @XmlAttribute(name = "value")
                    protected GlStencilOpType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public GlStencilOpType getValue() {
                        if (value == null) {
                            return GlStencilOpType.KEEP;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public void setValue(GlStencilOpType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Zfail {

                    @XmlAttribute(name = "value")
                    protected GlStencilOpType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public GlStencilOpType getValue() {
                        if (value == null) {
                            return GlStencilOpType.KEEP;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public void setValue(GlStencilOpType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }


                /**
                 * <p>Java-Klasse für anonymous complex type.
                 * 
                 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_stencil_op_type" default="KEEP" />
                 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Zpass {

                    @XmlAttribute(name = "value")
                    protected GlStencilOpType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public GlStencilOpType getValue() {
                        if (value == null) {
                            return GlStencilOpType.KEEP;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlStencilOpType }
                     *     
                     */
                    public void setValue(GlStencilOpType value) {
                        this.value = value;
                    }

                    public boolean isSetValue() {
                        return (this.value!= null);
                    }

                    /**
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setParam(String value) {
                        this.param = value;
                    }

                    public boolean isSetParam() {
                        return (this.param!= null);
                    }

                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class StencilTestEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;choice>
             *         &lt;element name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_sampler1D"/>
             *         &lt;element name="param" type="{http://www.w3.org/2001/XMLSchema}NCName"/>
             *       &lt;/choice>
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value",
                "param"
            })
            public static class Texture1D {

                protected GlSampler1D value;
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlSampler1D }
                 *     
                 */
                public GlSampler1D getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlSampler1D }
                 *     
                 */
                public void setValue(GlSampler1D value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Texture1DEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index")
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;choice>
             *         &lt;element name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_sampler2D"/>
             *         &lt;element name="param" type="{http://www.w3.org/2001/XMLSchema}NCName"/>
             *       &lt;/choice>
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value",
                "param"
            })
            public static class Texture2D {

                protected GlSampler2D value;
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlSampler2D }
                 *     
                 */
                public GlSampler2D getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlSampler2D }
                 *     
                 */
                public void setValue(GlSampler2D value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Texture2DEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index")
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;choice>
             *         &lt;element name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_sampler3D"/>
             *         &lt;element name="param" type="{http://www.w3.org/2001/XMLSchema}NCName"/>
             *       &lt;/choice>
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value",
                "param"
            })
            public static class Texture3D {

                protected GlSampler3D value;
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlSampler3D }
                 *     
                 */
                public GlSampler3D getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlSampler3D }
                 *     
                 */
                public void setValue(GlSampler3D value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Texture3DEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index")
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;choice>
             *         &lt;element name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_samplerCUBE"/>
             *         &lt;element name="param" type="{http://www.w3.org/2001/XMLSchema}NCName"/>
             *       &lt;/choice>
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value",
                "param"
            })
            public static class TextureCUBE {

                protected GlSamplerCUBE value;
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlSamplerCUBE }
                 *     
                 */
                public GlSamplerCUBE getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlSamplerCUBE }
                 *     
                 */
                public void setValue(GlSamplerCUBE value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class TextureCUBEEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index")
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;choice>
             *         &lt;element name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_samplerDEPTH"/>
             *         &lt;element name="param" type="{http://www.w3.org/2001/XMLSchema}NCName"/>
             *       &lt;/choice>
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value",
                "param"
            })
            public static class TextureDEPTH {

                protected GlSamplerDEPTH value;
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlSamplerDEPTH }
                 *     
                 */
                public GlSamplerDEPTH getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlSamplerDEPTH }
                 *     
                 */
                public void setValue(GlSamplerDEPTH value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class TextureDEPTHEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index")
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class TextureEnvColor {

                @XmlAttribute(name = "value")
                protected List<Double> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index")
                protected BigInteger index;

                /**
                 * Gets the value of the value property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the value property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValue().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Double }
                 * 
                 * 
                 */
                public List<Double> getValue() {
                    if (value == null) {
                        value = new ArrayList<Double>();
                    }
                    return this.value;
                }

                public boolean isSetValue() {
                    return ((this.value!= null)&&(!this.value.isEmpty()));
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

                public void setValue(List<Double> value) {
                    this.value = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}string" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class TextureEnvMode {

                @XmlAttribute(name = "value")
                protected String value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index")
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setValue(String value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;choice>
             *         &lt;element name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gl_samplerRECT"/>
             *         &lt;element name="param" type="{http://www.w3.org/2001/XMLSchema}NCName"/>
             *       &lt;/choice>
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value",
                "param"
            })
            public static class TextureRECT {

                protected GlSamplerRECT value;
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlSamplerRECT }
                 *     
                 */
                public GlSamplerRECT getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlSamplerRECT }
                 *     
                 */
                public void setValue(GlSamplerRECT value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GL_MAX_TEXTURE_IMAGE_UNITS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class TextureRECTEnable {

                @XmlAttribute(name = "value")
                protected Boolean value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index")
                protected BigInteger index;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Boolean }
                 *     
                 */
                public boolean isValue() {
                    if (value == null) {
                        return false;
                    } else {
                        return value;
                    }
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Boolean }
                 *     
                 */
                public void setValue(boolean value) {
                    this.value = value;
                }

                public boolean isSetValue() {
                    return (this.value!= null);
                }

                public void unsetValue() {
                    this.value = null;
                }

                /**
                 * Ruft den Wert der param-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getParam() {
                    return param;
                }

                /**
                 * Legt den Wert der param-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParam(String value) {
                    this.param = value;
                }

                public boolean isSetParam() {
                    return (this.param!= null);
                }

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setIndex(BigInteger value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

            }

        }

    }

}
