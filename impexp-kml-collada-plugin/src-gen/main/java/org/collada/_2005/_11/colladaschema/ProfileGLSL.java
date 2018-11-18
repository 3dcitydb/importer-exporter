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
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="code" type="{http://www.collada.org/2005/11/COLLADASchema}fx_code_profile"/&gt;
 *           &lt;element name="include" type="{http://www.collada.org/2005/11/COLLADASchema}fx_include_common"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/&gt;
 *           &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_newparam"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="technique" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                   &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;element name="code" type="{http://www.collada.org/2005/11/COLLADASchema}fx_code_profile"/&gt;
 *                     &lt;element name="include" type="{http://www.collada.org/2005/11/COLLADASchema}fx_include_common"/&gt;
 *                   &lt;/choice&gt;
 *                   &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/&gt;
 *                     &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_newparam"/&gt;
 *                     &lt;element name="setparam" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_setparam"/&gt;
 *                   &lt;/choice&gt;
 *                   &lt;element name="pass" maxOccurs="unbounded"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                             &lt;element name="color_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_colortarget_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                             &lt;element name="depth_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_depthtarget_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                             &lt;element name="stencil_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_stenciltarget_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                             &lt;element name="color_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearcolor_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                             &lt;element name="depth_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_cleardepth_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                             &lt;element name="stencil_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearstencil_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                             &lt;element name="draw" type="{http://www.collada.org/2005/11/COLLADASchema}fx_draw_common" minOccurs="0"/&gt;
 *                             &lt;choice maxOccurs="unbounded"&gt;
 *                               &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gl_pipeline_settings"/&gt;
 *                               &lt;element name="shader"&gt;
 *                                 &lt;complexType&gt;
 *                                   &lt;complexContent&gt;
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                       &lt;sequence&gt;
 *                                         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                                         &lt;sequence minOccurs="0"&gt;
 *                                           &lt;element name="compiler_target"&gt;
 *                                             &lt;complexType&gt;
 *                                               &lt;simpleContent&gt;
 *                                                 &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NMTOKEN"&gt;
 *                                                 &lt;/extension&gt;
 *                                               &lt;/simpleContent&gt;
 *                                             &lt;/complexType&gt;
 *                                           &lt;/element&gt;
 *                                           &lt;element name="compiler_options" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                                         &lt;/sequence&gt;
 *                                         &lt;element name="name"&gt;
 *                                           &lt;complexType&gt;
 *                                             &lt;simpleContent&gt;
 *                                               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NCName"&gt;
 *                                                 &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *                                               &lt;/extension&gt;
 *                                             &lt;/simpleContent&gt;
 *                                           &lt;/complexType&gt;
 *                                         &lt;/element&gt;
 *                                         &lt;element name="bind" maxOccurs="unbounded" minOccurs="0"&gt;
 *                                           &lt;complexType&gt;
 *                                             &lt;complexContent&gt;
 *                                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                                 &lt;choice&gt;
 *                                                   &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}glsl_param_type"/&gt;
 *                                                   &lt;element name="param"&gt;
 *                                                     &lt;complexType&gt;
 *                                                       &lt;complexContent&gt;
 *                                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                                           &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                                         &lt;/restriction&gt;
 *                                                       &lt;/complexContent&gt;
 *                                                     &lt;/complexType&gt;
 *                                                   &lt;/element&gt;
 *                                                 &lt;/choice&gt;
 *                                                 &lt;attribute name="symbol" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *                                               &lt;/restriction&gt;
 *                                             &lt;/complexContent&gt;
 *                                           &lt;/complexType&gt;
 *                                         &lt;/element&gt;
 *                                       &lt;/sequence&gt;
 *                                       &lt;attribute name="stage" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_pipeline_stage" /&gt;
 *                                     &lt;/restriction&gt;
 *                                   &lt;/complexContent&gt;
 *                                 &lt;/complexType&gt;
 *                               &lt;/element&gt;
 *                             &lt;/choice&gt;
 *                             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                           &lt;/sequence&gt;
 *                           &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
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
    "codeOrInclude",
    "imageOrNewparam",
    "technique",
    "extra"
})
public class ProfileGLSL {

    protected Asset asset;
    @XmlElements({
        @XmlElement(name = "code", type = FxCodeProfile.class),
        @XmlElement(name = "include", type = FxIncludeCommon.class)
    })
    protected List<Object> codeOrInclude;
    @XmlElements({
        @XmlElement(name = "image", type = Image.class),
        @XmlElement(name = "newparam", type = GlslNewparam.class)
    })
    protected List<Object> imageOrNewparam;
    @XmlElement(required = true)
    protected List<ProfileGLSL.Technique> technique;
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
     * {@link GlslNewparam }
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
     * {@link ProfileGLSL.Technique }
     * 
     * 
     */
    public List<ProfileGLSL.Technique> getTechnique() {
        if (technique == null) {
            technique = new ArrayList<ProfileGLSL.Technique>();
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

    public void setCodeOrInclude(List<Object> value) {
        this.codeOrInclude = value;
    }

    public void setImageOrNewparam(List<Object> value) {
        this.imageOrNewparam = value;
    }

    public void setTechnique(List<ProfileGLSL.Technique> value) {
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
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
     *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;element name="code" type="{http://www.collada.org/2005/11/COLLADASchema}fx_code_profile"/&gt;
     *           &lt;element name="include" type="{http://www.collada.org/2005/11/COLLADASchema}fx_include_common"/&gt;
     *         &lt;/choice&gt;
     *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/&gt;
     *           &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_newparam"/&gt;
     *           &lt;element name="setparam" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_setparam"/&gt;
     *         &lt;/choice&gt;
     *         &lt;element name="pass" maxOccurs="unbounded"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                   &lt;element name="color_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_colortarget_common" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                   &lt;element name="depth_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_depthtarget_common" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                   &lt;element name="stencil_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_stenciltarget_common" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                   &lt;element name="color_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearcolor_common" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                   &lt;element name="depth_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_cleardepth_common" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                   &lt;element name="stencil_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearstencil_common" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                   &lt;element name="draw" type="{http://www.collada.org/2005/11/COLLADASchema}fx_draw_common" minOccurs="0"/&gt;
     *                   &lt;choice maxOccurs="unbounded"&gt;
     *                     &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gl_pipeline_settings"/&gt;
     *                     &lt;element name="shader"&gt;
     *                       &lt;complexType&gt;
     *                         &lt;complexContent&gt;
     *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                             &lt;sequence&gt;
     *                               &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                               &lt;sequence minOccurs="0"&gt;
     *                                 &lt;element name="compiler_target"&gt;
     *                                   &lt;complexType&gt;
     *                                     &lt;simpleContent&gt;
     *                                       &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NMTOKEN"&gt;
     *                                       &lt;/extension&gt;
     *                                     &lt;/simpleContent&gt;
     *                                   &lt;/complexType&gt;
     *                                 &lt;/element&gt;
     *                                 &lt;element name="compiler_options" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *                               &lt;/sequence&gt;
     *                               &lt;element name="name"&gt;
     *                                 &lt;complexType&gt;
     *                                   &lt;simpleContent&gt;
     *                                     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NCName"&gt;
     *                                       &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
     *                                     &lt;/extension&gt;
     *                                   &lt;/simpleContent&gt;
     *                                 &lt;/complexType&gt;
     *                               &lt;/element&gt;
     *                               &lt;element name="bind" maxOccurs="unbounded" minOccurs="0"&gt;
     *                                 &lt;complexType&gt;
     *                                   &lt;complexContent&gt;
     *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                                       &lt;choice&gt;
     *                                         &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}glsl_param_type"/&gt;
     *                                         &lt;element name="param"&gt;
     *                                           &lt;complexType&gt;
     *                                             &lt;complexContent&gt;
     *                                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                                                 &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                                               &lt;/restriction&gt;
     *                                             &lt;/complexContent&gt;
     *                                           &lt;/complexType&gt;
     *                                         &lt;/element&gt;
     *                                       &lt;/choice&gt;
     *                                       &lt;attribute name="symbol" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
     *                                     &lt;/restriction&gt;
     *                                   &lt;/complexContent&gt;
     *                                 &lt;/complexType&gt;
     *                               &lt;/element&gt;
     *                             &lt;/sequence&gt;
     *                             &lt;attribute name="stage" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_pipeline_stage" /&gt;
     *                           &lt;/restriction&gt;
     *                         &lt;/complexContent&gt;
     *                       &lt;/complexType&gt;
     *                     &lt;/element&gt;
     *                   &lt;/choice&gt;
     *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                 &lt;/sequence&gt;
     *                 &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
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
        "annotate",
        "codeOrInclude",
        "imageOrNewparamOrSetparam",
        "pass",
        "extra"
    })
    public static class Technique {

        protected List<FxAnnotateCommon> annotate;
        @XmlElements({
            @XmlElement(name = "code", type = FxCodeProfile.class),
            @XmlElement(name = "include", type = FxIncludeCommon.class)
        })
        protected List<Object> codeOrInclude;
        @XmlElements({
            @XmlElement(name = "image", type = Image.class),
            @XmlElement(name = "newparam", type = GlslNewparam.class),
            @XmlElement(name = "setparam", type = GlslSetparam.class)
        })
        protected List<Object> imageOrNewparamOrSetparam;
        @XmlElement(required = true)
        protected List<ProfileGLSL.Technique.Pass> pass;
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
         * {@link GlslNewparam }
         * {@link GlslSetparam }
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
         * {@link ProfileGLSL.Technique.Pass }
         * 
         * 
         */
        public List<ProfileGLSL.Technique.Pass> getPass() {
            if (pass == null) {
                pass = new ArrayList<ProfileGLSL.Technique.Pass>();
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

        public void setPass(List<ProfileGLSL.Technique.Pass> value) {
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
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
         *         &lt;element name="color_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_colortarget_common" maxOccurs="unbounded" minOccurs="0"/&gt;
         *         &lt;element name="depth_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_depthtarget_common" maxOccurs="unbounded" minOccurs="0"/&gt;
         *         &lt;element name="stencil_target" type="{http://www.collada.org/2005/11/COLLADASchema}fx_stenciltarget_common" maxOccurs="unbounded" minOccurs="0"/&gt;
         *         &lt;element name="color_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearcolor_common" maxOccurs="unbounded" minOccurs="0"/&gt;
         *         &lt;element name="depth_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_cleardepth_common" maxOccurs="unbounded" minOccurs="0"/&gt;
         *         &lt;element name="stencil_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_clearstencil_common" maxOccurs="unbounded" minOccurs="0"/&gt;
         *         &lt;element name="draw" type="{http://www.collada.org/2005/11/COLLADASchema}fx_draw_common" minOccurs="0"/&gt;
         *         &lt;choice maxOccurs="unbounded"&gt;
         *           &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gl_pipeline_settings"/&gt;
         *           &lt;element name="shader"&gt;
         *             &lt;complexType&gt;
         *               &lt;complexContent&gt;
         *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                   &lt;sequence&gt;
         *                     &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
         *                     &lt;sequence minOccurs="0"&gt;
         *                       &lt;element name="compiler_target"&gt;
         *                         &lt;complexType&gt;
         *                           &lt;simpleContent&gt;
         *                             &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NMTOKEN"&gt;
         *                             &lt;/extension&gt;
         *                           &lt;/simpleContent&gt;
         *                         &lt;/complexType&gt;
         *                       &lt;/element&gt;
         *                       &lt;element name="compiler_options" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
         *                     &lt;/sequence&gt;
         *                     &lt;element name="name"&gt;
         *                       &lt;complexType&gt;
         *                         &lt;simpleContent&gt;
         *                           &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NCName"&gt;
         *                             &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
         *                           &lt;/extension&gt;
         *                         &lt;/simpleContent&gt;
         *                       &lt;/complexType&gt;
         *                     &lt;/element&gt;
         *                     &lt;element name="bind" maxOccurs="unbounded" minOccurs="0"&gt;
         *                       &lt;complexType&gt;
         *                         &lt;complexContent&gt;
         *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                             &lt;choice&gt;
         *                               &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}glsl_param_type"/&gt;
         *                               &lt;element name="param"&gt;
         *                                 &lt;complexType&gt;
         *                                   &lt;complexContent&gt;
         *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                                       &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *                                     &lt;/restriction&gt;
         *                                   &lt;/complexContent&gt;
         *                                 &lt;/complexType&gt;
         *                               &lt;/element&gt;
         *                             &lt;/choice&gt;
         *                             &lt;attribute name="symbol" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
         *                           &lt;/restriction&gt;
         *                         &lt;/complexContent&gt;
         *                       &lt;/complexType&gt;
         *                     &lt;/element&gt;
         *                   &lt;/sequence&gt;
         *                   &lt;attribute name="stage" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_pipeline_stage" /&gt;
         *                 &lt;/restriction&gt;
         *               &lt;/complexContent&gt;
         *             &lt;/complexType&gt;
         *           &lt;/element&gt;
         *         &lt;/choice&gt;
         *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
         *       &lt;/sequence&gt;
         *       &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
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
                @XmlElement(name = "alpha_func", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.AlphaFunc.class),
                @XmlElement(name = "blend_func", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendFunc.class),
                @XmlElement(name = "blend_func_separate", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendFuncSeparate.class),
                @XmlElement(name = "blend_equation", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendEquation.class),
                @XmlElement(name = "blend_equation_separate", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendEquationSeparate.class),
                @XmlElement(name = "color_material", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ColorMaterial.class),
                @XmlElement(name = "cull_face", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.CullFace.class),
                @XmlElement(name = "depth_func", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthFunc.class),
                @XmlElement(name = "fog_mode", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogMode.class),
                @XmlElement(name = "fog_coord_src", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogCoordSrc.class),
                @XmlElement(name = "front_face", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FrontFace.class),
                @XmlElement(name = "light_model_color_control", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightModelColorControl.class),
                @XmlElement(name = "logic_op", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LogicOp.class),
                @XmlElement(name = "polygon_mode", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonMode.class),
                @XmlElement(name = "shade_model", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ShadeModel.class),
                @XmlElement(name = "stencil_func", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilFunc.class),
                @XmlElement(name = "stencil_op", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilOp.class),
                @XmlElement(name = "stencil_func_separate", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilFuncSeparate.class),
                @XmlElement(name = "stencil_op_separate", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilOpSeparate.class),
                @XmlElement(name = "stencil_mask_separate", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilMaskSeparate.class),
                @XmlElement(name = "light_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightEnable.class),
                @XmlElement(name = "light_ambient", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightAmbient.class),
                @XmlElement(name = "light_diffuse", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightDiffuse.class),
                @XmlElement(name = "light_specular", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightSpecular.class),
                @XmlElement(name = "light_position", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightPosition.class),
                @XmlElement(name = "light_constant_attenuation", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightConstantAttenuation.class),
                @XmlElement(name = "light_linear_attenuation", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightLinearAttenuation.class),
                @XmlElement(name = "light_quadratic_attenuation", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightQuadraticAttenuation.class),
                @XmlElement(name = "light_spot_cutoff", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightSpotCutoff.class),
                @XmlElement(name = "light_spot_direction", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightSpotDirection.class),
                @XmlElement(name = "light_spot_exponent", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightSpotExponent.class),
                @XmlElement(name = "texture1D", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture1D.class),
                @XmlElement(name = "texture2D", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture2D.class),
                @XmlElement(name = "texture3D", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture3D.class),
                @XmlElement(name = "textureCUBE", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureCUBE.class),
                @XmlElement(name = "textureRECT", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureRECT.class),
                @XmlElement(name = "textureDEPTH", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureDEPTH.class),
                @XmlElement(name = "texture1D_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture1DEnable.class),
                @XmlElement(name = "texture2D_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture2DEnable.class),
                @XmlElement(name = "texture3D_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture3DEnable.class),
                @XmlElement(name = "textureCUBE_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureCUBEEnable.class),
                @XmlElement(name = "textureRECT_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureRECTEnable.class),
                @XmlElement(name = "textureDEPTH_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureDEPTHEnable.class),
                @XmlElement(name = "texture_env_color", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureEnvColor.class),
                @XmlElement(name = "texture_env_mode", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureEnvMode.class),
                @XmlElement(name = "clip_plane", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ClipPlane.class),
                @XmlElement(name = "clip_plane_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ClipPlaneEnable.class),
                @XmlElement(name = "blend_color", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendColor.class),
                @XmlElement(name = "clear_color", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ClearColor.class),
                @XmlElement(name = "clear_stencil", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ClearStencil.class),
                @XmlElement(name = "clear_depth", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ClearDepth.class),
                @XmlElement(name = "color_mask", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ColorMask.class),
                @XmlElement(name = "depth_bounds", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthBounds.class),
                @XmlElement(name = "depth_mask", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthMask.class),
                @XmlElement(name = "depth_range", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthRange.class),
                @XmlElement(name = "fog_density", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogDensity.class),
                @XmlElement(name = "fog_start", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogStart.class),
                @XmlElement(name = "fog_end", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogEnd.class),
                @XmlElement(name = "fog_color", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogColor.class),
                @XmlElement(name = "light_model_ambient", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightModelAmbient.class),
                @XmlElement(name = "lighting_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightingEnable.class),
                @XmlElement(name = "line_stipple", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LineStipple.class),
                @XmlElement(name = "line_width", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LineWidth.class),
                @XmlElement(name = "material_ambient", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MaterialAmbient.class),
                @XmlElement(name = "material_diffuse", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MaterialDiffuse.class),
                @XmlElement(name = "material_emission", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MaterialEmission.class),
                @XmlElement(name = "material_shininess", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MaterialShininess.class),
                @XmlElement(name = "material_specular", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MaterialSpecular.class),
                @XmlElement(name = "model_view_matrix", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ModelViewMatrix.class),
                @XmlElement(name = "point_distance_attenuation", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointDistanceAttenuation.class),
                @XmlElement(name = "point_fade_threshold_size", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointFadeThresholdSize.class),
                @XmlElement(name = "point_size", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointSize.class),
                @XmlElement(name = "point_size_min", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointSizeMin.class),
                @XmlElement(name = "point_size_max", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointSizeMax.class),
                @XmlElement(name = "polygon_offset", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonOffset.class),
                @XmlElement(name = "projection_matrix", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ProjectionMatrix.class),
                @XmlElement(name = "scissor", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Scissor.class),
                @XmlElement(name = "stencil_mask", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilMask.class),
                @XmlElement(name = "alpha_test_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.AlphaTestEnable.class),
                @XmlElement(name = "auto_normal_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.AutoNormalEnable.class),
                @XmlElement(name = "blend_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendEnable.class),
                @XmlElement(name = "color_logic_op_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ColorLogicOpEnable.class),
                @XmlElement(name = "color_material_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ColorMaterialEnable.class),
                @XmlElement(name = "cull_face_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.CullFaceEnable.class),
                @XmlElement(name = "depth_bounds_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthBoundsEnable.class),
                @XmlElement(name = "depth_clamp_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthClampEnable.class),
                @XmlElement(name = "depth_test_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthTestEnable.class),
                @XmlElement(name = "dither_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DitherEnable.class),
                @XmlElement(name = "fog_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogEnable.class),
                @XmlElement(name = "light_model_local_viewer_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightModelLocalViewerEnable.class),
                @XmlElement(name = "light_model_two_side_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightModelTwoSideEnable.class),
                @XmlElement(name = "line_smooth_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LineSmoothEnable.class),
                @XmlElement(name = "line_stipple_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LineStippleEnable.class),
                @XmlElement(name = "logic_op_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LogicOpEnable.class),
                @XmlElement(name = "multisample_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MultisampleEnable.class),
                @XmlElement(name = "normalize_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.NormalizeEnable.class),
                @XmlElement(name = "point_smooth_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointSmoothEnable.class),
                @XmlElement(name = "polygon_offset_fill_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonOffsetFillEnable.class),
                @XmlElement(name = "polygon_offset_line_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonOffsetLineEnable.class),
                @XmlElement(name = "polygon_offset_point_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonOffsetPointEnable.class),
                @XmlElement(name = "polygon_smooth_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonSmoothEnable.class),
                @XmlElement(name = "polygon_stipple_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonStippleEnable.class),
                @XmlElement(name = "rescale_normal_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.RescaleNormalEnable.class),
                @XmlElement(name = "sample_alpha_to_coverage_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.SampleAlphaToCoverageEnable.class),
                @XmlElement(name = "sample_alpha_to_one_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.SampleAlphaToOneEnable.class),
                @XmlElement(name = "sample_coverage_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.SampleCoverageEnable.class),
                @XmlElement(name = "scissor_test_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ScissorTestEnable.class),
                @XmlElement(name = "stencil_test_enable", type = org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilTestEnable.class),
                @XmlElement(name = "gl_hook_abstract"),
                @XmlElement(name = "shader", type = ProfileGLSL.Technique.Pass.Shader.class)
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
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.AlphaFunc }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendFunc }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendFuncSeparate }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendEquation }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendEquationSeparate }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ColorMaterial }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.CullFace }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthFunc }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogMode }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogCoordSrc }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FrontFace }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightModelColorControl }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LogicOp }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonMode }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ShadeModel }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilFunc }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilOp }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilFuncSeparate }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilOpSeparate }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilMaskSeparate }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightAmbient }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightDiffuse }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightSpecular }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightPosition }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightConstantAttenuation }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightLinearAttenuation }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightQuadraticAttenuation }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightSpotCutoff }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightSpotDirection }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightSpotExponent }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture1D }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture2D }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture3D }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureCUBE }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureRECT }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureDEPTH }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture1DEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture2DEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Texture3DEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureCUBEEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureRECTEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureDEPTHEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureEnvColor }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.TextureEnvMode }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ClipPlane }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ClipPlaneEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendColor }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ClearColor }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ClearStencil }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ClearDepth }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ColorMask }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthBounds }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthMask }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthRange }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogDensity }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogStart }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogEnd }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogColor }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightModelAmbient }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightingEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LineStipple }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LineWidth }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MaterialAmbient }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MaterialDiffuse }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MaterialEmission }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MaterialShininess }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MaterialSpecular }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ModelViewMatrix }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointDistanceAttenuation }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointFadeThresholdSize }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointSize }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointSizeMin }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointSizeMax }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonOffset }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ProjectionMatrix }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.Scissor }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilMask }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.AlphaTestEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.AutoNormalEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.BlendEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ColorLogicOpEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ColorMaterialEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.CullFaceEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthBoundsEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthClampEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DepthTestEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.DitherEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.FogEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightModelLocalViewerEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LightModelTwoSideEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LineSmoothEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LineStippleEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.LogicOpEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.MultisampleEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.NormalizeEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PointSmoothEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonOffsetFillEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonOffsetLineEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonOffsetPointEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonSmoothEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.PolygonStippleEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.RescaleNormalEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.SampleAlphaToCoverageEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.SampleAlphaToOneEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.SampleCoverageEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.ScissorTestEnable }
             * {@link org.collada._2005._11.colladaschema.ProfileCG.Technique.Pass.StencilTestEnable }
             * {@link Object }
             * {@link ProfileGLSL.Technique.Pass.Shader }
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
             * &lt;complexType&gt;
             *   &lt;complexContent&gt;
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
             *       &lt;sequence&gt;
             *         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
             *         &lt;sequence minOccurs="0"&gt;
             *           &lt;element name="compiler_target"&gt;
             *             &lt;complexType&gt;
             *               &lt;simpleContent&gt;
             *                 &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NMTOKEN"&gt;
             *                 &lt;/extension&gt;
             *               &lt;/simpleContent&gt;
             *             &lt;/complexType&gt;
             *           &lt;/element&gt;
             *           &lt;element name="compiler_options" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
             *         &lt;/sequence&gt;
             *         &lt;element name="name"&gt;
             *           &lt;complexType&gt;
             *             &lt;simpleContent&gt;
             *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NCName"&gt;
             *                 &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
             *               &lt;/extension&gt;
             *             &lt;/simpleContent&gt;
             *           &lt;/complexType&gt;
             *         &lt;/element&gt;
             *         &lt;element name="bind" maxOccurs="unbounded" minOccurs="0"&gt;
             *           &lt;complexType&gt;
             *             &lt;complexContent&gt;
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
             *                 &lt;choice&gt;
             *                   &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}glsl_param_type"/&gt;
             *                   &lt;element name="param"&gt;
             *                     &lt;complexType&gt;
             *                       &lt;complexContent&gt;
             *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
             *                           &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
             *                         &lt;/restriction&gt;
             *                       &lt;/complexContent&gt;
             *                     &lt;/complexType&gt;
             *                   &lt;/element&gt;
             *                 &lt;/choice&gt;
             *                 &lt;attribute name="symbol" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
             *               &lt;/restriction&gt;
             *             &lt;/complexContent&gt;
             *           &lt;/complexType&gt;
             *         &lt;/element&gt;
             *       &lt;/sequence&gt;
             *       &lt;attribute name="stage" type="{http://www.collada.org/2005/11/COLLADASchema}glsl_pipeline_stage" /&gt;
             *     &lt;/restriction&gt;
             *   &lt;/complexContent&gt;
             * &lt;/complexType&gt;
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
                protected ProfileGLSL.Technique.Pass.Shader.CompilerTarget compilerTarget;
                @XmlElement(name = "compiler_options")
                protected String compilerOptions;
                @XmlElement(required = true)
                protected ProfileGLSL.Technique.Pass.Shader.Name name;
                protected List<ProfileGLSL.Technique.Pass.Shader.Bind> bind;
                @XmlAttribute(name = "stage")
                protected GlslPipelineStage stage;

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
                 *     {@link ProfileGLSL.Technique.Pass.Shader.CompilerTarget }
                 *     
                 */
                public ProfileGLSL.Technique.Pass.Shader.CompilerTarget getCompilerTarget() {
                    return compilerTarget;
                }

                /**
                 * Legt den Wert der compilerTarget-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLSL.Technique.Pass.Shader.CompilerTarget }
                 *     
                 */
                public void setCompilerTarget(ProfileGLSL.Technique.Pass.Shader.CompilerTarget value) {
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
                 *     {@link ProfileGLSL.Technique.Pass.Shader.Name }
                 *     
                 */
                public ProfileGLSL.Technique.Pass.Shader.Name getName() {
                    return name;
                }

                /**
                 * Legt den Wert der name-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLSL.Technique.Pass.Shader.Name }
                 *     
                 */
                public void setName(ProfileGLSL.Technique.Pass.Shader.Name value) {
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
                 * {@link ProfileGLSL.Technique.Pass.Shader.Bind }
                 * 
                 * 
                 */
                public List<ProfileGLSL.Technique.Pass.Shader.Bind> getBind() {
                    if (bind == null) {
                        bind = new ArrayList<ProfileGLSL.Technique.Pass.Shader.Bind>();
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
                 *     {@link GlslPipelineStage }
                 *     
                 */
                public GlslPipelineStage getStage() {
                    return stage;
                }

                /**
                 * Legt den Wert der stage-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlslPipelineStage }
                 *     
                 */
                public void setStage(GlslPipelineStage value) {
                    this.stage = value;
                }

                public boolean isSetStage() {
                    return (this.stage!= null);
                }

                public void setAnnotate(List<FxAnnotateCommon> value) {
                    this.annotate = value;
                }

                public void setBind(List<ProfileGLSL.Technique.Pass.Shader.Bind> value) {
                    this.bind = value;
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
                 *       &lt;choice&gt;
                 *         &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}glsl_param_type"/&gt;
                 *         &lt;element name="param"&gt;
                 *           &lt;complexType&gt;
                 *             &lt;complexContent&gt;
                 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
                 *                 &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
                 *               &lt;/restriction&gt;
                 *             &lt;/complexContent&gt;
                 *           &lt;/complexType&gt;
                 *         &lt;/element&gt;
                 *       &lt;/choice&gt;
                 *       &lt;attribute name="symbol" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
                 *     &lt;/restriction&gt;
                 *   &lt;/complexContent&gt;
                 * &lt;/complexType&gt;
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
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
                    "param"
                })
                public static class Bind {

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
                    protected ProfileGLSL.Technique.Pass.Shader.Bind.Param param;
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
                     * Ruft den Wert der param-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link ProfileGLSL.Technique.Pass.Shader.Bind.Param }
                     *     
                     */
                    public ProfileGLSL.Technique.Pass.Shader.Bind.Param getParam() {
                        return param;
                    }

                    /**
                     * Legt den Wert der param-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link ProfileGLSL.Technique.Pass.Shader.Bind.Param }
                     *     
                     */
                    public void setParam(ProfileGLSL.Technique.Pass.Shader.Bind.Param value) {
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


                    /**
                     * <p>Java-Klasse für anonymous complex type.
                     * 
                     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
                     * 
                     * <pre>
                     * &lt;complexType&gt;
                     *   &lt;complexContent&gt;
                     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
                     *       &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
                     *     &lt;/restriction&gt;
                     *   &lt;/complexContent&gt;
                     * &lt;/complexType&gt;
                     * </pre>
                     * 
                     * 
                     */
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "")
                    public static class Param {

                        @XmlAttribute(name = "ref", required = true)
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
                 * &lt;complexType&gt;
                 *   &lt;simpleContent&gt;
                 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NMTOKEN"&gt;
                 *     &lt;/extension&gt;
                 *   &lt;/simpleContent&gt;
                 * &lt;/complexType&gt;
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
                 * &lt;complexType&gt;
                 *   &lt;simpleContent&gt;
                 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NCName"&gt;
                 *       &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
                 *     &lt;/extension&gt;
                 *   &lt;/simpleContent&gt;
                 * &lt;/complexType&gt;
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

        }

    }

}
