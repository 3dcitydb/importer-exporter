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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlList;
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
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/>
 *           &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}gles_newparam"/>
 *         &lt;/choice>
 *         &lt;element name="technique" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/>
 *                   &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;choice maxOccurs="unbounded" minOccurs="0">
 *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/>
 *                     &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}gles_newparam"/>
 *                     &lt;element name="setparam">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
 *                               &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gles_basic_type_common"/>
 *                             &lt;/sequence>
 *                             &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                   &lt;/choice>
 *                   &lt;element name="pass" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="color_target" type="{http://www.collada.org/2005/11/COLLADASchema}gles_rendertarget_common" minOccurs="0"/>
 *                             &lt;element name="depth_target" type="{http://www.collada.org/2005/11/COLLADASchema}gles_rendertarget_common" minOccurs="0"/>
 *                             &lt;element name="stencil_target" type="{http://www.collada.org/2005/11/COLLADASchema}gles_rendertarget_common" minOccurs="0"/>
 *                             &lt;element name="color_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_color_common" minOccurs="0"/>
 *                             &lt;element name="depth_clear" type="{http://www.collada.org/2005/11/COLLADASchema}float" minOccurs="0"/>
 *                             &lt;element name="stencil_clear" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *                             &lt;element name="draw" type="{http://www.collada.org/2005/11/COLLADASchema}fx_draw_common" minOccurs="0"/>
 *                             &lt;choice maxOccurs="unbounded" minOccurs="0">
 *                               &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gles_pipeline_settings"/>
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
    "imageOrNewparam",
    "technique",
    "extra"
})
public class ProfileGLES {

    protected Asset asset;
    @XmlElements({
        @XmlElement(name = "image", type = Image.class),
        @XmlElement(name = "newparam", type = GlesNewparam.class)
    })
    protected List<Object> imageOrNewparam;
    @XmlElement(required = true)
    protected List<ProfileGLES.Technique> technique;
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
     * {@link GlesNewparam }
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
     * {@link ProfileGLES.Technique }
     * 
     * 
     */
    public List<ProfileGLES.Technique> getTechnique() {
        if (technique == null) {
            technique = new ArrayList<ProfileGLES.Technique>();
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

    public void setImageOrNewparam(List<Object> value) {
        this.imageOrNewparam = value;
    }

    public void setTechnique(List<ProfileGLES.Technique> value) {
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
     *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}image"/>
     *           &lt;element name="newparam" type="{http://www.collada.org/2005/11/COLLADASchema}gles_newparam"/>
     *           &lt;element name="setparam">
     *             &lt;complexType>
     *               &lt;complexContent>
     *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                   &lt;sequence>
     *                     &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
     *                     &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gles_basic_type_common"/>
     *                   &lt;/sequence>
     *                   &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *                 &lt;/restriction>
     *               &lt;/complexContent>
     *             &lt;/complexType>
     *           &lt;/element>
     *         &lt;/choice>
     *         &lt;element name="pass" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/>
     *                   &lt;element name="color_target" type="{http://www.collada.org/2005/11/COLLADASchema}gles_rendertarget_common" minOccurs="0"/>
     *                   &lt;element name="depth_target" type="{http://www.collada.org/2005/11/COLLADASchema}gles_rendertarget_common" minOccurs="0"/>
     *                   &lt;element name="stencil_target" type="{http://www.collada.org/2005/11/COLLADASchema}gles_rendertarget_common" minOccurs="0"/>
     *                   &lt;element name="color_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_color_common" minOccurs="0"/>
     *                   &lt;element name="depth_clear" type="{http://www.collada.org/2005/11/COLLADASchema}float" minOccurs="0"/>
     *                   &lt;element name="stencil_clear" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
     *                   &lt;element name="draw" type="{http://www.collada.org/2005/11/COLLADASchema}fx_draw_common" minOccurs="0"/>
     *                   &lt;choice maxOccurs="unbounded" minOccurs="0">
     *                     &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gles_pipeline_settings"/>
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
        "imageOrNewparamOrSetparam",
        "pass",
        "extra"
    })
    public static class Technique {

        protected Asset asset;
        protected List<FxAnnotateCommon> annotate;
        @XmlElements({
            @XmlElement(name = "image", type = Image.class),
            @XmlElement(name = "newparam", type = GlesNewparam.class),
            @XmlElement(name = "setparam", type = ProfileGLES.Technique.Setparam.class)
        })
        protected List<Object> imageOrNewparamOrSetparam;
        @XmlElement(required = true)
        protected List<ProfileGLES.Technique.Pass> pass;
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
         * {@link GlesNewparam }
         * {@link ProfileGLES.Technique.Setparam }
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
         * {@link ProfileGLES.Technique.Pass }
         * 
         * 
         */
        public List<ProfileGLES.Technique.Pass> getPass() {
            if (pass == null) {
                pass = new ArrayList<ProfileGLES.Technique.Pass>();
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

        public void setImageOrNewparamOrSetparam(List<Object> value) {
            this.imageOrNewparamOrSetparam = value;
        }

        public void setPass(List<ProfileGLES.Technique.Pass> value) {
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
         *         &lt;element name="color_target" type="{http://www.collada.org/2005/11/COLLADASchema}gles_rendertarget_common" minOccurs="0"/>
         *         &lt;element name="depth_target" type="{http://www.collada.org/2005/11/COLLADASchema}gles_rendertarget_common" minOccurs="0"/>
         *         &lt;element name="stencil_target" type="{http://www.collada.org/2005/11/COLLADASchema}gles_rendertarget_common" minOccurs="0"/>
         *         &lt;element name="color_clear" type="{http://www.collada.org/2005/11/COLLADASchema}fx_color_common" minOccurs="0"/>
         *         &lt;element name="depth_clear" type="{http://www.collada.org/2005/11/COLLADASchema}float" minOccurs="0"/>
         *         &lt;element name="stencil_clear" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
         *         &lt;element name="draw" type="{http://www.collada.org/2005/11/COLLADASchema}fx_draw_common" minOccurs="0"/>
         *         &lt;choice maxOccurs="unbounded" minOccurs="0">
         *           &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gles_pipeline_settings"/>
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
            "alphaFuncOrBlendFuncOrClearColor",
            "extra"
        })
        public static class Pass {

            protected List<FxAnnotateCommon> annotate;
            @XmlElement(name = "color_target")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            protected String colorTarget;
            @XmlElement(name = "depth_target")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            protected String depthTarget;
            @XmlElement(name = "stencil_target")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            protected String stencilTarget;
            @XmlList
            @XmlElement(name = "color_clear", type = Double.class)
            protected List<Double> colorClear;
            @XmlElement(name = "depth_clear")
            protected Double depthClear;
            @XmlElement(name = "stencil_clear")
            protected Byte stencilClear;
            protected String draw;
            @XmlElements({
                @XmlElement(name = "alpha_func", type = ProfileGLES.Technique.Pass.AlphaFunc.class),
                @XmlElement(name = "blend_func", type = ProfileGLES.Technique.Pass.BlendFunc.class),
                @XmlElement(name = "clear_color", type = ProfileGLES.Technique.Pass.ClearColor.class),
                @XmlElement(name = "clear_stencil", type = ProfileGLES.Technique.Pass.ClearStencil.class),
                @XmlElement(name = "clear_depth", type = ProfileGLES.Technique.Pass.ClearDepth.class),
                @XmlElement(name = "clip_plane", type = ProfileGLES.Technique.Pass.ClipPlane.class),
                @XmlElement(name = "color_mask", type = ProfileGLES.Technique.Pass.ColorMask.class),
                @XmlElement(name = "cull_face", type = ProfileGLES.Technique.Pass.CullFace.class),
                @XmlElement(name = "depth_func", type = ProfileGLES.Technique.Pass.DepthFunc.class),
                @XmlElement(name = "depth_mask", type = ProfileGLES.Technique.Pass.DepthMask.class),
                @XmlElement(name = "depth_range", type = ProfileGLES.Technique.Pass.DepthRange.class),
                @XmlElement(name = "fog_color", type = ProfileGLES.Technique.Pass.FogColor.class),
                @XmlElement(name = "fog_density", type = ProfileGLES.Technique.Pass.FogDensity.class),
                @XmlElement(name = "fog_mode", type = ProfileGLES.Technique.Pass.FogMode.class),
                @XmlElement(name = "fog_start", type = ProfileGLES.Technique.Pass.FogStart.class),
                @XmlElement(name = "fog_end", type = ProfileGLES.Technique.Pass.FogEnd.class),
                @XmlElement(name = "front_face", type = ProfileGLES.Technique.Pass.FrontFace.class),
                @XmlElement(name = "texture_pipeline", type = ProfileGLES.Technique.Pass.TexturePipeline.class),
                @XmlElement(name = "logic_op", type = ProfileGLES.Technique.Pass.LogicOp.class),
                @XmlElement(name = "light_ambient", type = ProfileGLES.Technique.Pass.LightAmbient.class),
                @XmlElement(name = "light_diffuse", type = ProfileGLES.Technique.Pass.LightDiffuse.class),
                @XmlElement(name = "light_specular", type = ProfileGLES.Technique.Pass.LightSpecular.class),
                @XmlElement(name = "light_position", type = ProfileGLES.Technique.Pass.LightPosition.class),
                @XmlElement(name = "light_constant_attenuation", type = ProfileGLES.Technique.Pass.LightConstantAttenuation.class),
                @XmlElement(name = "light_linear_attenutation", type = ProfileGLES.Technique.Pass.LightLinearAttenutation.class),
                @XmlElement(name = "light_quadratic_attenuation", type = ProfileGLES.Technique.Pass.LightQuadraticAttenuation.class),
                @XmlElement(name = "light_spot_cutoff", type = ProfileGLES.Technique.Pass.LightSpotCutoff.class),
                @XmlElement(name = "light_spot_direction", type = ProfileGLES.Technique.Pass.LightSpotDirection.class),
                @XmlElement(name = "light_spot_exponent", type = ProfileGLES.Technique.Pass.LightSpotExponent.class),
                @XmlElement(name = "light_model_ambient", type = ProfileGLES.Technique.Pass.LightModelAmbient.class),
                @XmlElement(name = "line_width", type = ProfileGLES.Technique.Pass.LineWidth.class),
                @XmlElement(name = "material_ambient", type = ProfileGLES.Technique.Pass.MaterialAmbient.class),
                @XmlElement(name = "material_diffuse", type = ProfileGLES.Technique.Pass.MaterialDiffuse.class),
                @XmlElement(name = "material_emission", type = ProfileGLES.Technique.Pass.MaterialEmission.class),
                @XmlElement(name = "material_shininess", type = ProfileGLES.Technique.Pass.MaterialShininess.class),
                @XmlElement(name = "material_specular", type = ProfileGLES.Technique.Pass.MaterialSpecular.class),
                @XmlElement(name = "model_view_matrix", type = ProfileGLES.Technique.Pass.ModelViewMatrix.class),
                @XmlElement(name = "point_distance_attenuation", type = ProfileGLES.Technique.Pass.PointDistanceAttenuation.class),
                @XmlElement(name = "point_fade_threshold_size", type = ProfileGLES.Technique.Pass.PointFadeThresholdSize.class),
                @XmlElement(name = "point_size", type = ProfileGLES.Technique.Pass.PointSize.class),
                @XmlElement(name = "point_size_min", type = ProfileGLES.Technique.Pass.PointSizeMin.class),
                @XmlElement(name = "point_size_max", type = ProfileGLES.Technique.Pass.PointSizeMax.class),
                @XmlElement(name = "polygon_offset", type = ProfileGLES.Technique.Pass.PolygonOffset.class),
                @XmlElement(name = "projection_matrix", type = ProfileGLES.Technique.Pass.ProjectionMatrix.class),
                @XmlElement(name = "scissor", type = ProfileGLES.Technique.Pass.Scissor.class),
                @XmlElement(name = "shade_model", type = ProfileGLES.Technique.Pass.ShadeModel.class),
                @XmlElement(name = "stencil_func", type = ProfileGLES.Technique.Pass.StencilFunc.class),
                @XmlElement(name = "stencil_mask", type = ProfileGLES.Technique.Pass.StencilMask.class),
                @XmlElement(name = "stencil_op", type = ProfileGLES.Technique.Pass.StencilOp.class),
                @XmlElement(name = "alpha_test_enable", type = ProfileGLES.Technique.Pass.AlphaTestEnable.class),
                @XmlElement(name = "blend_enable", type = ProfileGLES.Technique.Pass.BlendEnable.class),
                @XmlElement(name = "clip_plane_enable", type = ProfileGLES.Technique.Pass.ClipPlaneEnable.class),
                @XmlElement(name = "color_logic_op_enable", type = ProfileGLES.Technique.Pass.ColorLogicOpEnable.class),
                @XmlElement(name = "color_material_enable", type = ProfileGLES.Technique.Pass.ColorMaterialEnable.class),
                @XmlElement(name = "cull_face_enable", type = ProfileGLES.Technique.Pass.CullFaceEnable.class),
                @XmlElement(name = "depth_test_enable", type = ProfileGLES.Technique.Pass.DepthTestEnable.class),
                @XmlElement(name = "dither_enable", type = ProfileGLES.Technique.Pass.DitherEnable.class),
                @XmlElement(name = "fog_enable", type = ProfileGLES.Technique.Pass.FogEnable.class),
                @XmlElement(name = "texture_pipeline_enable", type = ProfileGLES.Technique.Pass.TexturePipelineEnable.class),
                @XmlElement(name = "light_enable", type = ProfileGLES.Technique.Pass.LightEnable.class),
                @XmlElement(name = "lighting_enable", type = ProfileGLES.Technique.Pass.LightingEnable.class),
                @XmlElement(name = "light_model_two_side_enable", type = ProfileGLES.Technique.Pass.LightModelTwoSideEnable.class),
                @XmlElement(name = "line_smooth_enable", type = ProfileGLES.Technique.Pass.LineSmoothEnable.class),
                @XmlElement(name = "multisample_enable", type = ProfileGLES.Technique.Pass.MultisampleEnable.class),
                @XmlElement(name = "normalize_enable", type = ProfileGLES.Technique.Pass.NormalizeEnable.class),
                @XmlElement(name = "point_smooth_enable", type = ProfileGLES.Technique.Pass.PointSmoothEnable.class),
                @XmlElement(name = "polygon_offset_fill_enable", type = ProfileGLES.Technique.Pass.PolygonOffsetFillEnable.class),
                @XmlElement(name = "rescale_normal_enable", type = ProfileGLES.Technique.Pass.RescaleNormalEnable.class),
                @XmlElement(name = "sample_alpha_to_coverage_enable", type = ProfileGLES.Technique.Pass.SampleAlphaToCoverageEnable.class),
                @XmlElement(name = "sample_alpha_to_one_enable", type = ProfileGLES.Technique.Pass.SampleAlphaToOneEnable.class),
                @XmlElement(name = "sample_coverage_enable", type = ProfileGLES.Technique.Pass.SampleCoverageEnable.class),
                @XmlElement(name = "scissor_test_enable", type = ProfileGLES.Technique.Pass.ScissorTestEnable.class),
                @XmlElement(name = "stencil_test_enable", type = ProfileGLES.Technique.Pass.StencilTestEnable.class)
            })
            protected List<Object> alphaFuncOrBlendFuncOrClearColor;
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
             * Ruft den Wert der colorTarget-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getColorTarget() {
                return colorTarget;
            }

            /**
             * Legt den Wert der colorTarget-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setColorTarget(String value) {
                this.colorTarget = value;
            }

            public boolean isSetColorTarget() {
                return (this.colorTarget!= null);
            }

            /**
             * Ruft den Wert der depthTarget-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDepthTarget() {
                return depthTarget;
            }

            /**
             * Legt den Wert der depthTarget-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDepthTarget(String value) {
                this.depthTarget = value;
            }

            public boolean isSetDepthTarget() {
                return (this.depthTarget!= null);
            }

            /**
             * Ruft den Wert der stencilTarget-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getStencilTarget() {
                return stencilTarget;
            }

            /**
             * Legt den Wert der stencilTarget-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setStencilTarget(String value) {
                this.stencilTarget = value;
            }

            public boolean isSetStencilTarget() {
                return (this.stencilTarget!= null);
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
             * {@link Double }
             * 
             * 
             */
            public List<Double> getColorClear() {
                if (colorClear == null) {
                    colorClear = new ArrayList<Double>();
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
             * Ruft den Wert der depthClear-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Double }
             *     
             */
            public Double getDepthClear() {
                return depthClear;
            }

            /**
             * Legt den Wert der depthClear-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Double }
             *     
             */
            public void setDepthClear(Double value) {
                this.depthClear = value;
            }

            public boolean isSetDepthClear() {
                return (this.depthClear!= null);
            }

            /**
             * Ruft den Wert der stencilClear-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Byte }
             *     
             */
            public Byte getStencilClear() {
                return stencilClear;
            }

            /**
             * Legt den Wert der stencilClear-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Byte }
             *     
             */
            public void setStencilClear(Byte value) {
                this.stencilClear = value;
            }

            public boolean isSetStencilClear() {
                return (this.stencilClear!= null);
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
             * Gets the value of the alphaFuncOrBlendFuncOrClearColor property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the alphaFuncOrBlendFuncOrClearColor property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAlphaFuncOrBlendFuncOrClearColor().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link ProfileGLES.Technique.Pass.AlphaFunc }
             * {@link ProfileGLES.Technique.Pass.BlendFunc }
             * {@link ProfileGLES.Technique.Pass.ClearColor }
             * {@link ProfileGLES.Technique.Pass.ClearStencil }
             * {@link ProfileGLES.Technique.Pass.ClearDepth }
             * {@link ProfileGLES.Technique.Pass.ClipPlane }
             * {@link ProfileGLES.Technique.Pass.ColorMask }
             * {@link ProfileGLES.Technique.Pass.CullFace }
             * {@link ProfileGLES.Technique.Pass.DepthFunc }
             * {@link ProfileGLES.Technique.Pass.DepthMask }
             * {@link ProfileGLES.Technique.Pass.DepthRange }
             * {@link ProfileGLES.Technique.Pass.FogColor }
             * {@link ProfileGLES.Technique.Pass.FogDensity }
             * {@link ProfileGLES.Technique.Pass.FogMode }
             * {@link ProfileGLES.Technique.Pass.FogStart }
             * {@link ProfileGLES.Technique.Pass.FogEnd }
             * {@link ProfileGLES.Technique.Pass.FrontFace }
             * {@link ProfileGLES.Technique.Pass.TexturePipeline }
             * {@link ProfileGLES.Technique.Pass.LogicOp }
             * {@link ProfileGLES.Technique.Pass.LightAmbient }
             * {@link ProfileGLES.Technique.Pass.LightDiffuse }
             * {@link ProfileGLES.Technique.Pass.LightSpecular }
             * {@link ProfileGLES.Technique.Pass.LightPosition }
             * {@link ProfileGLES.Technique.Pass.LightConstantAttenuation }
             * {@link ProfileGLES.Technique.Pass.LightLinearAttenutation }
             * {@link ProfileGLES.Technique.Pass.LightQuadraticAttenuation }
             * {@link ProfileGLES.Technique.Pass.LightSpotCutoff }
             * {@link ProfileGLES.Technique.Pass.LightSpotDirection }
             * {@link ProfileGLES.Technique.Pass.LightSpotExponent }
             * {@link ProfileGLES.Technique.Pass.LightModelAmbient }
             * {@link ProfileGLES.Technique.Pass.LineWidth }
             * {@link ProfileGLES.Technique.Pass.MaterialAmbient }
             * {@link ProfileGLES.Technique.Pass.MaterialDiffuse }
             * {@link ProfileGLES.Technique.Pass.MaterialEmission }
             * {@link ProfileGLES.Technique.Pass.MaterialShininess }
             * {@link ProfileGLES.Technique.Pass.MaterialSpecular }
             * {@link ProfileGLES.Technique.Pass.ModelViewMatrix }
             * {@link ProfileGLES.Technique.Pass.PointDistanceAttenuation }
             * {@link ProfileGLES.Technique.Pass.PointFadeThresholdSize }
             * {@link ProfileGLES.Technique.Pass.PointSize }
             * {@link ProfileGLES.Technique.Pass.PointSizeMin }
             * {@link ProfileGLES.Technique.Pass.PointSizeMax }
             * {@link ProfileGLES.Technique.Pass.PolygonOffset }
             * {@link ProfileGLES.Technique.Pass.ProjectionMatrix }
             * {@link ProfileGLES.Technique.Pass.Scissor }
             * {@link ProfileGLES.Technique.Pass.ShadeModel }
             * {@link ProfileGLES.Technique.Pass.StencilFunc }
             * {@link ProfileGLES.Technique.Pass.StencilMask }
             * {@link ProfileGLES.Technique.Pass.StencilOp }
             * {@link ProfileGLES.Technique.Pass.AlphaTestEnable }
             * {@link ProfileGLES.Technique.Pass.BlendEnable }
             * {@link ProfileGLES.Technique.Pass.ClipPlaneEnable }
             * {@link ProfileGLES.Technique.Pass.ColorLogicOpEnable }
             * {@link ProfileGLES.Technique.Pass.ColorMaterialEnable }
             * {@link ProfileGLES.Technique.Pass.CullFaceEnable }
             * {@link ProfileGLES.Technique.Pass.DepthTestEnable }
             * {@link ProfileGLES.Technique.Pass.DitherEnable }
             * {@link ProfileGLES.Technique.Pass.FogEnable }
             * {@link ProfileGLES.Technique.Pass.TexturePipelineEnable }
             * {@link ProfileGLES.Technique.Pass.LightEnable }
             * {@link ProfileGLES.Technique.Pass.LightingEnable }
             * {@link ProfileGLES.Technique.Pass.LightModelTwoSideEnable }
             * {@link ProfileGLES.Technique.Pass.LineSmoothEnable }
             * {@link ProfileGLES.Technique.Pass.MultisampleEnable }
             * {@link ProfileGLES.Technique.Pass.NormalizeEnable }
             * {@link ProfileGLES.Technique.Pass.PointSmoothEnable }
             * {@link ProfileGLES.Technique.Pass.PolygonOffsetFillEnable }
             * {@link ProfileGLES.Technique.Pass.RescaleNormalEnable }
             * {@link ProfileGLES.Technique.Pass.SampleAlphaToCoverageEnable }
             * {@link ProfileGLES.Technique.Pass.SampleAlphaToOneEnable }
             * {@link ProfileGLES.Technique.Pass.SampleCoverageEnable }
             * {@link ProfileGLES.Technique.Pass.ScissorTestEnable }
             * {@link ProfileGLES.Technique.Pass.StencilTestEnable }
             * 
             * 
             */
            public List<Object> getAlphaFuncOrBlendFuncOrClearColor() {
                if (alphaFuncOrBlendFuncOrClearColor == null) {
                    alphaFuncOrBlendFuncOrClearColor = new ArrayList<Object>();
                }
                return this.alphaFuncOrBlendFuncOrClearColor;
            }

            public boolean isSetAlphaFuncOrBlendFuncOrClearColor() {
                return ((this.alphaFuncOrBlendFuncOrClearColor!= null)&&(!this.alphaFuncOrBlendFuncOrClearColor.isEmpty()));
            }

            public void unsetAlphaFuncOrBlendFuncOrClearColor() {
                this.alphaFuncOrBlendFuncOrClearColor = null;
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

            public void setColorClear(List<Double> value) {
                this.colorClear = value;
            }

            public void setAlphaFuncOrBlendFuncOrClearColor(List<Object> value) {
                this.alphaFuncOrBlendFuncOrClearColor = value;
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
                protected ProfileGLES.Technique.Pass.AlphaFunc.Func func;
                @XmlElement(required = true)
                protected ProfileGLES.Technique.Pass.AlphaFunc.Value value;

                /**
                 * Ruft den Wert der func-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileGLES.Technique.Pass.AlphaFunc.Func }
                 *     
                 */
                public ProfileGLES.Technique.Pass.AlphaFunc.Func getFunc() {
                    return func;
                }

                /**
                 * Legt den Wert der func-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLES.Technique.Pass.AlphaFunc.Func }
                 *     
                 */
                public void setFunc(ProfileGLES.Technique.Pass.AlphaFunc.Func value) {
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
                 *     {@link ProfileGLES.Technique.Pass.AlphaFunc.Value }
                 *     
                 */
                public ProfileGLES.Technique.Pass.AlphaFunc.Value getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLES.Technique.Pass.AlphaFunc.Value }
                 *     
                 */
                public void setValue(ProfileGLES.Technique.Pass.AlphaFunc.Value value) {
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
                protected ProfileGLES.Technique.Pass.BlendFunc.Src src;
                @XmlElement(required = true)
                protected ProfileGLES.Technique.Pass.BlendFunc.Dest dest;

                /**
                 * Ruft den Wert der src-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileGLES.Technique.Pass.BlendFunc.Src }
                 *     
                 */
                public ProfileGLES.Technique.Pass.BlendFunc.Src getSrc() {
                    return src;
                }

                /**
                 * Legt den Wert der src-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLES.Technique.Pass.BlendFunc.Src }
                 *     
                 */
                public void setSrc(ProfileGLES.Technique.Pass.BlendFunc.Src value) {
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
                 *     {@link ProfileGLES.Technique.Pass.BlendFunc.Dest }
                 *     
                 */
                public ProfileGLES.Technique.Pass.BlendFunc.Dest getDest() {
                    return dest;
                }

                /**
                 * Legt den Wert der dest-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLES.Technique.Pass.BlendFunc.Dest }
                 *     
                 */
                public void setDest(ProfileGLES.Technique.Pass.BlendFunc.Dest value) {
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
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float4" />
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
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}float" />
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
                    return value;
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
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}int" />
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
                    return value;
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
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool4" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_CLIP_PLANES_index" />
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
                protected List<Boolean> value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected int index;

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

                /**
                 * Ruft den Wert der index-Eigenschaft ab.
                 * 
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *       &lt;attribute name="index" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_CLIP_PLANES_index" />
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
                protected Integer index;

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
                 *     {@link Integer }
                 *     
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Integer }
                 *     
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return (this.index!= null);
                }

                public void unsetIndex() {
                    this.index = null;
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
             *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}bool4" />
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
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
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
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
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
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
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
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
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class LightLinearAttenutation {

                @XmlAttribute(name = "value")
                protected Double value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;
                @XmlAttribute(name = "index", required = true)
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
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
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
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
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
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
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
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
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
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
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
             *       &lt;attribute name="index" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}GLES_MAX_LIGHTS_index" />
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
                protected int index;

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
                 */
                public int getIndex() {
                    return index;
                }

                /**
                 * Legt den Wert der index-Eigenschaft fest.
                 * 
                 */
                public void setIndex(int value) {
                    this.index = value;
                }

                public boolean isSetIndex() {
                    return true;
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
                protected ProfileGLES.Technique.Pass.StencilFunc.Func func;
                @XmlElement(required = true)
                protected ProfileGLES.Technique.Pass.StencilFunc.Ref ref;
                @XmlElement(required = true)
                protected ProfileGLES.Technique.Pass.StencilFunc.Mask mask;

                /**
                 * Ruft den Wert der func-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileGLES.Technique.Pass.StencilFunc.Func }
                 *     
                 */
                public ProfileGLES.Technique.Pass.StencilFunc.Func getFunc() {
                    return func;
                }

                /**
                 * Legt den Wert der func-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLES.Technique.Pass.StencilFunc.Func }
                 *     
                 */
                public void setFunc(ProfileGLES.Technique.Pass.StencilFunc.Func value) {
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
                 *     {@link ProfileGLES.Technique.Pass.StencilFunc.Ref }
                 *     
                 */
                public ProfileGLES.Technique.Pass.StencilFunc.Ref getRef() {
                    return ref;
                }

                /**
                 * Legt den Wert der ref-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLES.Technique.Pass.StencilFunc.Ref }
                 *     
                 */
                public void setRef(ProfileGLES.Technique.Pass.StencilFunc.Ref value) {
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
                 *     {@link ProfileGLES.Technique.Pass.StencilFunc.Mask }
                 *     
                 */
                public ProfileGLES.Technique.Pass.StencilFunc.Mask getMask() {
                    return mask;
                }

                /**
                 * Legt den Wert der mask-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLES.Technique.Pass.StencilFunc.Mask }
                 *     
                 */
                public void setMask(ProfileGLES.Technique.Pass.StencilFunc.Mask value) {
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
             *         &lt;element name="fail">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gles_stencil_op_type" default="KEEP" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="zfail">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gles_stencil_op_type" default="KEEP" />
             *                 &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="zpass">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gles_stencil_op_type" default="KEEP" />
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
                protected ProfileGLES.Technique.Pass.StencilOp.Fail fail;
                @XmlElement(required = true)
                protected ProfileGLES.Technique.Pass.StencilOp.Zfail zfail;
                @XmlElement(required = true)
                protected ProfileGLES.Technique.Pass.StencilOp.Zpass zpass;

                /**
                 * Ruft den Wert der fail-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link ProfileGLES.Technique.Pass.StencilOp.Fail }
                 *     
                 */
                public ProfileGLES.Technique.Pass.StencilOp.Fail getFail() {
                    return fail;
                }

                /**
                 * Legt den Wert der fail-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLES.Technique.Pass.StencilOp.Fail }
                 *     
                 */
                public void setFail(ProfileGLES.Technique.Pass.StencilOp.Fail value) {
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
                 *     {@link ProfileGLES.Technique.Pass.StencilOp.Zfail }
                 *     
                 */
                public ProfileGLES.Technique.Pass.StencilOp.Zfail getZfail() {
                    return zfail;
                }

                /**
                 * Legt den Wert der zfail-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLES.Technique.Pass.StencilOp.Zfail }
                 *     
                 */
                public void setZfail(ProfileGLES.Technique.Pass.StencilOp.Zfail value) {
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
                 *     {@link ProfileGLES.Technique.Pass.StencilOp.Zpass }
                 *     
                 */
                public ProfileGLES.Technique.Pass.StencilOp.Zpass getZpass() {
                    return zpass;
                }

                /**
                 * Legt den Wert der zpass-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link ProfileGLES.Technique.Pass.StencilOp.Zpass }
                 *     
                 */
                public void setZpass(ProfileGLES.Technique.Pass.StencilOp.Zpass value) {
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
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gles_stencil_op_type" default="KEEP" />
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
                    protected GlesStencilOpType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlesStencilOpType }
                     *     
                     */
                    public GlesStencilOpType getValue() {
                        if (value == null) {
                            return GlesStencilOpType.KEEP;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlesStencilOpType }
                     *     
                     */
                    public void setValue(GlesStencilOpType value) {
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
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gles_stencil_op_type" default="KEEP" />
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
                    protected GlesStencilOpType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlesStencilOpType }
                     *     
                     */
                    public GlesStencilOpType getValue() {
                        if (value == null) {
                            return GlesStencilOpType.KEEP;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlesStencilOpType }
                     *     
                     */
                    public void setValue(GlesStencilOpType value) {
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
                 *       &lt;attribute name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gles_stencil_op_type" default="KEEP" />
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
                    protected GlesStencilOpType value;
                    @XmlAttribute(name = "param")
                    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                    @XmlSchemaType(name = "NCName")
                    protected String param;

                    /**
                     * Ruft den Wert der value-Eigenschaft ab.
                     * 
                     * @return
                     *     possible object is
                     *     {@link GlesStencilOpType }
                     *     
                     */
                    public GlesStencilOpType getValue() {
                        if (value == null) {
                            return GlesStencilOpType.KEEP;
                        } else {
                            return value;
                        }
                    }

                    /**
                     * Legt den Wert der value-Eigenschaft fest.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link GlesStencilOpType }
                     *     
                     */
                    public void setValue(GlesStencilOpType value) {
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
             *       &lt;sequence>
             *         &lt;element name="value" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texture_pipeline" minOccurs="0"/>
             *       &lt;/sequence>
             *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}NCName" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value"
            })
            public static class TexturePipeline {

                protected GlesTexturePipeline value;
                @XmlAttribute(name = "param")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                @XmlSchemaType(name = "NCName")
                protected String param;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GlesTexturePipeline }
                 *     
                 */
                public GlesTexturePipeline getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GlesTexturePipeline }
                 *     
                 */
                public void setValue(GlesTexturePipeline value) {
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
            public static class TexturePipelineEnable {

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
         *         &lt;group ref="{http://www.collada.org/2005/11/COLLADASchema}gles_basic_type_common"/>
         *       &lt;/sequence>
         *       &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
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
            "texturePipeline",
            "samplerState",
            "textureUnit",
            "_enum"
        })
        public static class Setparam {

            protected List<FxAnnotateCommon> annotate;
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
            @XmlElement(name = "texture_pipeline")
            protected GlesTexturePipeline texturePipeline;
            @XmlElement(name = "sampler_state")
            protected GlesSamplerState samplerState;
            @XmlElement(name = "texture_unit")
            protected GlesTextureUnit textureUnit;
            @XmlElement(name = "enum")
            protected String _enum;
            @XmlAttribute(name = "ref", required = true)
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlSchemaType(name = "NCName")
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
             * Ruft den Wert der texturePipeline-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link GlesTexturePipeline }
             *     
             */
            public GlesTexturePipeline getTexturePipeline() {
                return texturePipeline;
            }

            /**
             * Legt den Wert der texturePipeline-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link GlesTexturePipeline }
             *     
             */
            public void setTexturePipeline(GlesTexturePipeline value) {
                this.texturePipeline = value;
            }

            public boolean isSetTexturePipeline() {
                return (this.texturePipeline!= null);
            }

            /**
             * Ruft den Wert der samplerState-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link GlesSamplerState }
             *     
             */
            public GlesSamplerState getSamplerState() {
                return samplerState;
            }

            /**
             * Legt den Wert der samplerState-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link GlesSamplerState }
             *     
             */
            public void setSamplerState(GlesSamplerState value) {
                this.samplerState = value;
            }

            public boolean isSetSamplerState() {
                return (this.samplerState!= null);
            }

            /**
             * Ruft den Wert der textureUnit-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link GlesTextureUnit }
             *     
             */
            public GlesTextureUnit getTextureUnit() {
                return textureUnit;
            }

            /**
             * Legt den Wert der textureUnit-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link GlesTextureUnit }
             *     
             */
            public void setTextureUnit(GlesTextureUnit value) {
                this.textureUnit = value;
            }

            public boolean isSetTextureUnit() {
                return (this.textureUnit!= null);
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

    }

}
