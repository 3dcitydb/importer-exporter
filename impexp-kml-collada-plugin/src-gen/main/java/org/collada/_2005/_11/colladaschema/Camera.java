//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
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
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/&gt;
 *         &lt;element name="optics"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="technique_common"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;choice&gt;
 *                             &lt;element name="orthographic"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;sequence&gt;
 *                                       &lt;choice&gt;
 *                                         &lt;sequence&gt;
 *                                           &lt;element name="xmag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                           &lt;choice minOccurs="0"&gt;
 *                                             &lt;element name="ymag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                             &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                           &lt;/choice&gt;
 *                                         &lt;/sequence&gt;
 *                                         &lt;sequence&gt;
 *                                           &lt;element name="ymag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                           &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
 *                                         &lt;/sequence&gt;
 *                                       &lt;/choice&gt;
 *                                       &lt;element name="znear" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                       &lt;element name="zfar" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                     &lt;/sequence&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="perspective"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;sequence&gt;
 *                                       &lt;choice&gt;
 *                                         &lt;sequence&gt;
 *                                           &lt;element name="xfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                           &lt;choice minOccurs="0"&gt;
 *                                             &lt;element name="yfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                             &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                           &lt;/choice&gt;
 *                                         &lt;/sequence&gt;
 *                                         &lt;sequence&gt;
 *                                           &lt;element name="yfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                           &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
 *                                         &lt;/sequence&gt;
 *                                       &lt;/choice&gt;
 *                                       &lt;element name="znear" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                       &lt;element name="zfar" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
 *                                     &lt;/sequence&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                           &lt;/choice&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}technique" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="imager" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}technique" maxOccurs="unbounded"/&gt;
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
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
    "optics",
    "imager",
    "extra"
})
@XmlRootElement(name = "camera")
public class Camera {

    protected Asset asset;
    @XmlElement(required = true)
    protected Camera.Optics optics;
    protected Camera.Imager imager;
    protected List<Extra> extra;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String name;

    /**
     * 
     * 						The camera element may contain an asset element.
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
     * Ruft den Wert der optics-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Camera.Optics }
     *     
     */
    public Camera.Optics getOptics() {
        return optics;
    }

    /**
     * Legt den Wert der optics-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Camera.Optics }
     *     
     */
    public void setOptics(Camera.Optics value) {
        this.optics = value;
    }

    public boolean isSetOptics() {
        return (this.optics!= null);
    }

    /**
     * Ruft den Wert der imager-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Camera.Imager }
     *     
     */
    public Camera.Imager getImager() {
        return imager;
    }

    /**
     * Legt den Wert der imager-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Camera.Imager }
     *     
     */
    public void setImager(Camera.Imager value) {
        this.imager = value;
    }

    public boolean isSetImager() {
        return (this.imager!= null);
    }

    /**
     * 
     * 						The extra element may appear any number of times.
     * 						Gets the value of the extra property.
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
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    public boolean isSetName() {
        return (this.name!= null);
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
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}technique" maxOccurs="unbounded"/&gt;
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
    @XmlType(name = "", propOrder = {
        "technique",
        "extra"
    })
    public static class Imager {

        @XmlElement(required = true)
        protected List<Technique> technique;
        protected List<Extra> extra;

        /**
         * 
         * 									This element may contain any number of non-common profile techniques.
         * 									There is no common technique for imager.
         * 									Gets the value of the technique property.
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
         * {@link Technique }
         * 
         * 
         */
        public List<Technique> getTechnique() {
            if (technique == null) {
                technique = new ArrayList<Technique>();
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
         * 
         * 									The extra element may appear any number of times.
         * 									Gets the value of the extra property.
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

        public void setTechnique(List<Technique> value) {
            this.technique = value;
        }

        public void setExtra(List<Extra> value) {
            this.extra = value;
        }

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
     *         &lt;element name="technique_common"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;choice&gt;
     *                   &lt;element name="orthographic"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;complexContent&gt;
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                           &lt;sequence&gt;
     *                             &lt;choice&gt;
     *                               &lt;sequence&gt;
     *                                 &lt;element name="xmag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                                 &lt;choice minOccurs="0"&gt;
     *                                   &lt;element name="ymag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                                   &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                                 &lt;/choice&gt;
     *                               &lt;/sequence&gt;
     *                               &lt;sequence&gt;
     *                                 &lt;element name="ymag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                                 &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
     *                               &lt;/sequence&gt;
     *                             &lt;/choice&gt;
     *                             &lt;element name="znear" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                             &lt;element name="zfar" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                           &lt;/sequence&gt;
     *                         &lt;/restriction&gt;
     *                       &lt;/complexContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="perspective"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;complexContent&gt;
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                           &lt;sequence&gt;
     *                             &lt;choice&gt;
     *                               &lt;sequence&gt;
     *                                 &lt;element name="xfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                                 &lt;choice minOccurs="0"&gt;
     *                                   &lt;element name="yfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                                   &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                                 &lt;/choice&gt;
     *                               &lt;/sequence&gt;
     *                               &lt;sequence&gt;
     *                                 &lt;element name="yfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                                 &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
     *                               &lt;/sequence&gt;
     *                             &lt;/choice&gt;
     *                             &lt;element name="znear" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                             &lt;element name="zfar" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
     *                           &lt;/sequence&gt;
     *                         &lt;/restriction&gt;
     *                       &lt;/complexContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                 &lt;/choice&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}technique" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    @XmlType(name = "", propOrder = {
        "techniqueCommon",
        "technique",
        "extra"
    })
    public static class Optics {

        @XmlElement(name = "technique_common", required = true)
        protected Camera.Optics.TechniqueCommon techniqueCommon;
        protected List<Technique> technique;
        protected List<Extra> extra;

        /**
         * Ruft den Wert der techniqueCommon-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Camera.Optics.TechniqueCommon }
         *     
         */
        public Camera.Optics.TechniqueCommon getTechniqueCommon() {
            return techniqueCommon;
        }

        /**
         * Legt den Wert der techniqueCommon-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Camera.Optics.TechniqueCommon }
         *     
         */
        public void setTechniqueCommon(Camera.Optics.TechniqueCommon value) {
            this.techniqueCommon = value;
        }

        public boolean isSetTechniqueCommon() {
            return (this.techniqueCommon!= null);
        }

        /**
         * 
         * 									This element may contain any number of non-common profile techniques.
         * 									Gets the value of the technique property.
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
         * {@link Technique }
         * 
         * 
         */
        public List<Technique> getTechnique() {
            if (technique == null) {
                technique = new ArrayList<Technique>();
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
         * 
         * 									The extra element may appear any number of times.
         * 									Gets the value of the extra property.
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

        public void setTechnique(List<Technique> value) {
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
         *       &lt;choice&gt;
         *         &lt;element name="orthographic"&gt;
         *           &lt;complexType&gt;
         *             &lt;complexContent&gt;
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                 &lt;sequence&gt;
         *                   &lt;choice&gt;
         *                     &lt;sequence&gt;
         *                       &lt;element name="xmag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                       &lt;choice minOccurs="0"&gt;
         *                         &lt;element name="ymag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                         &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                       &lt;/choice&gt;
         *                     &lt;/sequence&gt;
         *                     &lt;sequence&gt;
         *                       &lt;element name="ymag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                       &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
         *                     &lt;/sequence&gt;
         *                   &lt;/choice&gt;
         *                   &lt;element name="znear" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                   &lt;element name="zfar" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                 &lt;/sequence&gt;
         *               &lt;/restriction&gt;
         *             &lt;/complexContent&gt;
         *           &lt;/complexType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="perspective"&gt;
         *           &lt;complexType&gt;
         *             &lt;complexContent&gt;
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                 &lt;sequence&gt;
         *                   &lt;choice&gt;
         *                     &lt;sequence&gt;
         *                       &lt;element name="xfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                       &lt;choice minOccurs="0"&gt;
         *                         &lt;element name="yfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                         &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                       &lt;/choice&gt;
         *                     &lt;/sequence&gt;
         *                     &lt;sequence&gt;
         *                       &lt;element name="yfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                       &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
         *                     &lt;/sequence&gt;
         *                   &lt;/choice&gt;
         *                   &lt;element name="znear" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                   &lt;element name="zfar" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
         *                 &lt;/sequence&gt;
         *               &lt;/restriction&gt;
         *             &lt;/complexContent&gt;
         *           &lt;/complexType&gt;
         *         &lt;/element&gt;
         *       &lt;/choice&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "orthographic",
            "perspective"
        })
        public static class TechniqueCommon {

            protected Camera.Optics.TechniqueCommon.Orthographic orthographic;
            protected Camera.Optics.TechniqueCommon.Perspective perspective;

            /**
             * Ruft den Wert der orthographic-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Camera.Optics.TechniqueCommon.Orthographic }
             *     
             */
            public Camera.Optics.TechniqueCommon.Orthographic getOrthographic() {
                return orthographic;
            }

            /**
             * Legt den Wert der orthographic-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Camera.Optics.TechniqueCommon.Orthographic }
             *     
             */
            public void setOrthographic(Camera.Optics.TechniqueCommon.Orthographic value) {
                this.orthographic = value;
            }

            public boolean isSetOrthographic() {
                return (this.orthographic!= null);
            }

            /**
             * Ruft den Wert der perspective-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Camera.Optics.TechniqueCommon.Perspective }
             *     
             */
            public Camera.Optics.TechniqueCommon.Perspective getPerspective() {
                return perspective;
            }

            /**
             * Legt den Wert der perspective-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Camera.Optics.TechniqueCommon.Perspective }
             *     
             */
            public void setPerspective(Camera.Optics.TechniqueCommon.Perspective value) {
                this.perspective = value;
            }

            public boolean isSetPerspective() {
                return (this.perspective!= null);
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
             *         &lt;choice&gt;
             *           &lt;sequence&gt;
             *             &lt;element name="xmag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *             &lt;choice minOccurs="0"&gt;
             *               &lt;element name="ymag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *               &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *             &lt;/choice&gt;
             *           &lt;/sequence&gt;
             *           &lt;sequence&gt;
             *             &lt;element name="ymag" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *             &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
             *           &lt;/sequence&gt;
             *         &lt;/choice&gt;
             *         &lt;element name="znear" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *         &lt;element name="zfar" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *       &lt;/sequence&gt;
             *     &lt;/restriction&gt;
             *   &lt;/complexContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "content"
            })
            public static class Orthographic {

                @XmlElementRefs({
                    @XmlElementRef(name = "xmag", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
                    @XmlElementRef(name = "ymag", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
                    @XmlElementRef(name = "aspect_ratio", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
                    @XmlElementRef(name = "znear", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
                    @XmlElementRef(name = "zfar", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false)
                })
                protected List<JAXBElement<TargetableFloat>> content;

                /**
                 * Ruft das restliche Contentmodell ab. 
                 * 
                 * <p>
                 * Sie rufen diese "catch-all"-Eigenschaft aus folgendem Grund ab: 
                 * Der Feldname "Ymag" wird von zwei verschiedenen Teilen eines Schemas verwendet. Siehe: 
                 * Zeile 2234 von file:/C:/devel/java/impexp/impexp-kml-collada-plugin/resources/jaxb/collada/1.4/collada_schema_1_4.xsd
                 * Zeile 2214 von file:/C:/devel/java/impexp/impexp-kml-collada-plugin/resources/jaxb/collada/1.4/collada_schema_1_4.xsd
                 * <p>
                 * Um diese Eigenschaft zu entfernen, wenden Sie eine Eigenschaftenanpassung für eine
                 * der beiden folgenden Deklarationen an, um deren Namen zu ändern: 
                 * Gets the value of the content property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the content property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getContent().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link JAXBElement }{@code <}{@link TargetableFloat }{@code >}
                 * {@link JAXBElement }{@code <}{@link TargetableFloat }{@code >}
                 * {@link JAXBElement }{@code <}{@link TargetableFloat }{@code >}
                 * {@link JAXBElement }{@code <}{@link TargetableFloat }{@code >}
                 * {@link JAXBElement }{@code <}{@link TargetableFloat }{@code >}
                 * 
                 * 
                 */
                public List<JAXBElement<TargetableFloat>> getContent() {
                    if (content == null) {
                        content = new ArrayList<JAXBElement<TargetableFloat>>();
                    }
                    return this.content;
                }

                public void setContent(List<JAXBElement<TargetableFloat>> value) {
                    this.content = value;
                }

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
             *         &lt;choice&gt;
             *           &lt;sequence&gt;
             *             &lt;element name="xfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *             &lt;choice minOccurs="0"&gt;
             *               &lt;element name="yfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *               &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *             &lt;/choice&gt;
             *           &lt;/sequence&gt;
             *           &lt;sequence&gt;
             *             &lt;element name="yfov" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *             &lt;element name="aspect_ratio" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
             *           &lt;/sequence&gt;
             *         &lt;/choice&gt;
             *         &lt;element name="znear" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *         &lt;element name="zfar" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat"/&gt;
             *       &lt;/sequence&gt;
             *     &lt;/restriction&gt;
             *   &lt;/complexContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "content"
            })
            public static class Perspective {

                @XmlElementRefs({
                    @XmlElementRef(name = "xfov", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
                    @XmlElementRef(name = "yfov", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
                    @XmlElementRef(name = "aspect_ratio", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
                    @XmlElementRef(name = "znear", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
                    @XmlElementRef(name = "zfar", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false)
                })
                protected List<JAXBElement<TargetableFloat>> content;

                /**
                 * Ruft das restliche Contentmodell ab. 
                 * 
                 * <p>
                 * Sie rufen diese "catch-all"-Eigenschaft aus folgendem Grund ab: 
                 * Der Feldname "Yfov" wird von zwei verschiedenen Teilen eines Schemas verwendet. Siehe: 
                 * Zeile 2294 von file:/C:/devel/java/impexp/impexp-kml-collada-plugin/resources/jaxb/collada/1.4/collada_schema_1_4.xsd
                 * Zeile 2275 von file:/C:/devel/java/impexp/impexp-kml-collada-plugin/resources/jaxb/collada/1.4/collada_schema_1_4.xsd
                 * <p>
                 * Um diese Eigenschaft zu entfernen, wenden Sie eine Eigenschaftenanpassung für eine
                 * der beiden folgenden Deklarationen an, um deren Namen zu ändern: 
                 * Gets the value of the content property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the content property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getContent().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link JAXBElement }{@code <}{@link TargetableFloat }{@code >}
                 * {@link JAXBElement }{@code <}{@link TargetableFloat }{@code >}
                 * {@link JAXBElement }{@code <}{@link TargetableFloat }{@code >}
                 * {@link JAXBElement }{@code <}{@link TargetableFloat }{@code >}
                 * {@link JAXBElement }{@code <}{@link TargetableFloat }{@code >}
                 * 
                 * 
                 */
                public List<JAXBElement<TargetableFloat>> getContent() {
                    if (content == null) {
                        content = new ArrayList<JAXBElement<TargetableFloat>>();
                    }
                    return this.content;
                }

                public void setContent(List<JAXBElement<TargetableFloat>> value) {
                    this.content = value;
                }

            }

        }

    }

}
