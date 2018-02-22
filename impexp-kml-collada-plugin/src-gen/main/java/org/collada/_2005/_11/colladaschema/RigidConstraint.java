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
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element name="ref_attachment">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice maxOccurs="unbounded" minOccurs="0">
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/choice>
 *                 &lt;attribute name="rigid_body" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="attachment">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice maxOccurs="unbounded" minOccurs="0">
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/choice>
 *                 &lt;attribute name="rigid_body" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="technique_common">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="enabled" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
 *                           &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="interpenetrate" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
 *                           &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="limits" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="swing_cone_and_twist" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="min" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
 *                                       &lt;element name="max" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="linear" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="min" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
 *                                       &lt;element name="max" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="spring" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="angular" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="stiffness" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
 *                                       &lt;element name="damping" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
 *                                       &lt;element name="target_value" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="linear" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="stiffness" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
 *                                       &lt;element name="damping" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
 *                                       &lt;element name="target_value" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}technique" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="sid" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "refAttachment",
    "attachment",
    "techniqueCommon",
    "technique",
    "extra"
})
@XmlRootElement(name = "rigid_constraint")
public class RigidConstraint {

    @XmlElement(name = "ref_attachment", required = true)
    protected RigidConstraint.RefAttachment refAttachment;
    @XmlElement(required = true)
    protected RigidConstraint.Attachment attachment;
    @XmlElement(name = "technique_common", required = true)
    protected RigidConstraint.TechniqueCommon techniqueCommon;
    protected List<Technique> technique;
    protected List<Extra> extra;
    @XmlAttribute(name = "sid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String sid;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String name;

    /**
     * Ruft den Wert der refAttachment-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RigidConstraint.RefAttachment }
     *     
     */
    public RigidConstraint.RefAttachment getRefAttachment() {
        return refAttachment;
    }

    /**
     * Legt den Wert der refAttachment-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RigidConstraint.RefAttachment }
     *     
     */
    public void setRefAttachment(RigidConstraint.RefAttachment value) {
        this.refAttachment = value;
    }

    public boolean isSetRefAttachment() {
        return (this.refAttachment!= null);
    }

    /**
     * Ruft den Wert der attachment-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RigidConstraint.Attachment }
     *     
     */
    public RigidConstraint.Attachment getAttachment() {
        return attachment;
    }

    /**
     * Legt den Wert der attachment-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RigidConstraint.Attachment }
     *     
     */
    public void setAttachment(RigidConstraint.Attachment value) {
        this.attachment = value;
    }

    public boolean isSetAttachment() {
        return (this.attachment!= null);
    }

    /**
     * Ruft den Wert der techniqueCommon-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RigidConstraint.TechniqueCommon }
     *     
     */
    public RigidConstraint.TechniqueCommon getTechniqueCommon() {
        return techniqueCommon;
    }

    /**
     * Legt den Wert der techniqueCommon-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RigidConstraint.TechniqueCommon }
     *     
     */
    public void setTechniqueCommon(RigidConstraint.TechniqueCommon value) {
        this.techniqueCommon = value;
    }

    public boolean isSetTechniqueCommon() {
        return (this.techniqueCommon!= null);
    }

    /**
     * 
     * 						This element may contain any number of non-common profile techniques.
     * 						Gets the value of the technique property.
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
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice maxOccurs="unbounded" minOccurs="0">
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/choice>
     *       &lt;attribute name="rigid_body" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "translateOrRotateOrExtra"
    })
    public static class Attachment {

        @XmlElements({
            @XmlElement(name = "translate", type = TargetableFloat3 .class),
            @XmlElement(name = "rotate", type = Rotate.class),
            @XmlElement(name = "extra", type = Extra.class)
        })
        protected List<Object> translateOrRotateOrExtra;
        @XmlAttribute(name = "rigid_body")
        @XmlSchemaType(name = "anyURI")
        protected String rigidBody;

        /**
         * Gets the value of the translateOrRotateOrExtra property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the translateOrRotateOrExtra property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTranslateOrRotateOrExtra().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link TargetableFloat3 }
         * {@link Rotate }
         * {@link Extra }
         * 
         * 
         */
        public List<Object> getTranslateOrRotateOrExtra() {
            if (translateOrRotateOrExtra == null) {
                translateOrRotateOrExtra = new ArrayList<Object>();
            }
            return this.translateOrRotateOrExtra;
        }

        public boolean isSetTranslateOrRotateOrExtra() {
            return ((this.translateOrRotateOrExtra!= null)&&(!this.translateOrRotateOrExtra.isEmpty()));
        }

        public void unsetTranslateOrRotateOrExtra() {
            this.translateOrRotateOrExtra = null;
        }

        /**
         * Ruft den Wert der rigidBody-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRigidBody() {
            return rigidBody;
        }

        /**
         * Legt den Wert der rigidBody-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRigidBody(String value) {
            this.rigidBody = value;
        }

        public boolean isSetRigidBody() {
            return (this.rigidBody!= null);
        }

        public void setTranslateOrRotateOrExtra(List<Object> value) {
            this.translateOrRotateOrExtra = value;
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
     *       &lt;choice maxOccurs="unbounded" minOccurs="0">
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/choice>
     *       &lt;attribute name="rigid_body" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "translateOrRotateOrExtra"
    })
    public static class RefAttachment {

        @XmlElements({
            @XmlElement(name = "translate", type = TargetableFloat3 .class),
            @XmlElement(name = "rotate", type = Rotate.class),
            @XmlElement(name = "extra", type = Extra.class)
        })
        protected List<Object> translateOrRotateOrExtra;
        @XmlAttribute(name = "rigid_body")
        @XmlSchemaType(name = "anyURI")
        protected String rigidBody;

        /**
         * Gets the value of the translateOrRotateOrExtra property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the translateOrRotateOrExtra property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTranslateOrRotateOrExtra().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link TargetableFloat3 }
         * {@link Rotate }
         * {@link Extra }
         * 
         * 
         */
        public List<Object> getTranslateOrRotateOrExtra() {
            if (translateOrRotateOrExtra == null) {
                translateOrRotateOrExtra = new ArrayList<Object>();
            }
            return this.translateOrRotateOrExtra;
        }

        public boolean isSetTranslateOrRotateOrExtra() {
            return ((this.translateOrRotateOrExtra!= null)&&(!this.translateOrRotateOrExtra.isEmpty()));
        }

        public void unsetTranslateOrRotateOrExtra() {
            this.translateOrRotateOrExtra = null;
        }

        /**
         * Ruft den Wert der rigidBody-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRigidBody() {
            return rigidBody;
        }

        /**
         * Legt den Wert der rigidBody-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRigidBody(String value) {
            this.rigidBody = value;
        }

        public boolean isSetRigidBody() {
            return (this.rigidBody!= null);
        }

        public void setTranslateOrRotateOrExtra(List<Object> value) {
            this.translateOrRotateOrExtra = value;
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
     *         &lt;element name="enabled" minOccurs="0">
     *           &lt;complexType>
     *             &lt;simpleContent>
     *               &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
     *                 &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *               &lt;/extension>
     *             &lt;/simpleContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="interpenetrate" minOccurs="0">
     *           &lt;complexType>
     *             &lt;simpleContent>
     *               &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
     *                 &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *               &lt;/extension>
     *             &lt;/simpleContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="limits" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="swing_cone_and_twist" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="min" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
     *                             &lt;element name="max" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="linear" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="min" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
     *                             &lt;element name="max" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="spring" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="angular" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="stiffness" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
     *                             &lt;element name="damping" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
     *                             &lt;element name="target_value" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="linear" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="stiffness" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
     *                             &lt;element name="damping" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
     *                             &lt;element name="target_value" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
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
        "enabled",
        "interpenetrate",
        "limits",
        "spring"
    })
    public static class TechniqueCommon {

        @XmlElement(defaultValue = "true")
        protected RigidConstraint.TechniqueCommon.Enabled enabled;
        @XmlElement(defaultValue = "false")
        protected RigidConstraint.TechniqueCommon.Interpenetrate interpenetrate;
        protected RigidConstraint.TechniqueCommon.Limits limits;
        protected RigidConstraint.TechniqueCommon.Spring spring;

        /**
         * Ruft den Wert der enabled-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link RigidConstraint.TechniqueCommon.Enabled }
         *     
         */
        public RigidConstraint.TechniqueCommon.Enabled getEnabled() {
            return enabled;
        }

        /**
         * Legt den Wert der enabled-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link RigidConstraint.TechniqueCommon.Enabled }
         *     
         */
        public void setEnabled(RigidConstraint.TechniqueCommon.Enabled value) {
            this.enabled = value;
        }

        public boolean isSetEnabled() {
            return (this.enabled!= null);
        }

        /**
         * Ruft den Wert der interpenetrate-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link RigidConstraint.TechniqueCommon.Interpenetrate }
         *     
         */
        public RigidConstraint.TechniqueCommon.Interpenetrate getInterpenetrate() {
            return interpenetrate;
        }

        /**
         * Legt den Wert der interpenetrate-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link RigidConstraint.TechniqueCommon.Interpenetrate }
         *     
         */
        public void setInterpenetrate(RigidConstraint.TechniqueCommon.Interpenetrate value) {
            this.interpenetrate = value;
        }

        public boolean isSetInterpenetrate() {
            return (this.interpenetrate!= null);
        }

        /**
         * Ruft den Wert der limits-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link RigidConstraint.TechniqueCommon.Limits }
         *     
         */
        public RigidConstraint.TechniqueCommon.Limits getLimits() {
            return limits;
        }

        /**
         * Legt den Wert der limits-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link RigidConstraint.TechniqueCommon.Limits }
         *     
         */
        public void setLimits(RigidConstraint.TechniqueCommon.Limits value) {
            this.limits = value;
        }

        public boolean isSetLimits() {
            return (this.limits!= null);
        }

        /**
         * Ruft den Wert der spring-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link RigidConstraint.TechniqueCommon.Spring }
         *     
         */
        public RigidConstraint.TechniqueCommon.Spring getSpring() {
            return spring;
        }

        /**
         * Legt den Wert der spring-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link RigidConstraint.TechniqueCommon.Spring }
         *     
         */
        public void setSpring(RigidConstraint.TechniqueCommon.Spring value) {
            this.spring = value;
        }

        public boolean isSetSpring() {
            return (this.spring!= null);
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;simpleContent>
         *     &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
         *       &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
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
        public static class Enabled {

            @XmlValue
            protected boolean value;
            @XmlAttribute(name = "sid")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlSchemaType(name = "NCName")
            protected String sid;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             */
            public boolean isValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             */
            public void setValue(boolean value) {
                this.value = value;
            }

            public boolean isSetValue() {
                return true;
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

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;simpleContent>
         *     &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
         *       &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
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
        public static class Interpenetrate {

            @XmlValue
            protected boolean value;
            @XmlAttribute(name = "sid")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlSchemaType(name = "NCName")
            protected String sid;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             */
            public boolean isValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             */
            public void setValue(boolean value) {
                this.value = value;
            }

            public boolean isSetValue() {
                return true;
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
         *         &lt;element name="swing_cone_and_twist" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="min" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
         *                   &lt;element name="max" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="linear" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="min" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
         *                   &lt;element name="max" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
         *                 &lt;/sequence>
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
            "swingConeAndTwist",
            "linear"
        })
        public static class Limits {

            @XmlElement(name = "swing_cone_and_twist")
            protected RigidConstraint.TechniqueCommon.Limits.SwingConeAndTwist swingConeAndTwist;
            protected RigidConstraint.TechniqueCommon.Limits.Linear linear;

            /**
             * Ruft den Wert der swingConeAndTwist-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link RigidConstraint.TechniqueCommon.Limits.SwingConeAndTwist }
             *     
             */
            public RigidConstraint.TechniqueCommon.Limits.SwingConeAndTwist getSwingConeAndTwist() {
                return swingConeAndTwist;
            }

            /**
             * Legt den Wert der swingConeAndTwist-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link RigidConstraint.TechniqueCommon.Limits.SwingConeAndTwist }
             *     
             */
            public void setSwingConeAndTwist(RigidConstraint.TechniqueCommon.Limits.SwingConeAndTwist value) {
                this.swingConeAndTwist = value;
            }

            public boolean isSetSwingConeAndTwist() {
                return (this.swingConeAndTwist!= null);
            }

            /**
             * Ruft den Wert der linear-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link RigidConstraint.TechniqueCommon.Limits.Linear }
             *     
             */
            public RigidConstraint.TechniqueCommon.Limits.Linear getLinear() {
                return linear;
            }

            /**
             * Legt den Wert der linear-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link RigidConstraint.TechniqueCommon.Limits.Linear }
             *     
             */
            public void setLinear(RigidConstraint.TechniqueCommon.Limits.Linear value) {
                this.linear = value;
            }

            public boolean isSetLinear() {
                return (this.linear!= null);
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
             *         &lt;element name="min" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
             *         &lt;element name="max" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
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
                "min",
                "max"
            })
            public static class Linear {

                @XmlElement(defaultValue = "0.0 0.0 0.0")
                protected TargetableFloat3 min;
                @XmlElement(defaultValue = "0.0 0.0 0.0")
                protected TargetableFloat3 max;

                /**
                 * Ruft den Wert der min-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link TargetableFloat3 }
                 *     
                 */
                public TargetableFloat3 getMin() {
                    return min;
                }

                /**
                 * Legt den Wert der min-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link TargetableFloat3 }
                 *     
                 */
                public void setMin(TargetableFloat3 value) {
                    this.min = value;
                }

                public boolean isSetMin() {
                    return (this.min!= null);
                }

                /**
                 * Ruft den Wert der max-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link TargetableFloat3 }
                 *     
                 */
                public TargetableFloat3 getMax() {
                    return max;
                }

                /**
                 * Legt den Wert der max-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link TargetableFloat3 }
                 *     
                 */
                public void setMax(TargetableFloat3 value) {
                    this.max = value;
                }

                public boolean isSetMax() {
                    return (this.max!= null);
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
             *         &lt;element name="min" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
             *         &lt;element name="max" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
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
                "min",
                "max"
            })
            public static class SwingConeAndTwist {

                @XmlElement(defaultValue = "0.0 0.0 0.0")
                protected TargetableFloat3 min;
                @XmlElement(defaultValue = "0.0 0.0 0.0")
                protected TargetableFloat3 max;

                /**
                 * Ruft den Wert der min-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link TargetableFloat3 }
                 *     
                 */
                public TargetableFloat3 getMin() {
                    return min;
                }

                /**
                 * Legt den Wert der min-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link TargetableFloat3 }
                 *     
                 */
                public void setMin(TargetableFloat3 value) {
                    this.min = value;
                }

                public boolean isSetMin() {
                    return (this.min!= null);
                }

                /**
                 * Ruft den Wert der max-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link TargetableFloat3 }
                 *     
                 */
                public TargetableFloat3 getMax() {
                    return max;
                }

                /**
                 * Legt den Wert der max-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link TargetableFloat3 }
                 *     
                 */
                public void setMax(TargetableFloat3 value) {
                    this.max = value;
                }

                public boolean isSetMax() {
                    return (this.max!= null);
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
         *         &lt;element name="angular" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="stiffness" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
         *                   &lt;element name="damping" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
         *                   &lt;element name="target_value" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="linear" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="stiffness" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
         *                   &lt;element name="damping" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
         *                   &lt;element name="target_value" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
         *                 &lt;/sequence>
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
            "angular",
            "linear"
        })
        public static class Spring {

            protected RigidConstraint.TechniqueCommon.Spring.Angular angular;
            protected RigidConstraint.TechniqueCommon.Spring.Linear linear;

            /**
             * Ruft den Wert der angular-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link RigidConstraint.TechniqueCommon.Spring.Angular }
             *     
             */
            public RigidConstraint.TechniqueCommon.Spring.Angular getAngular() {
                return angular;
            }

            /**
             * Legt den Wert der angular-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link RigidConstraint.TechniqueCommon.Spring.Angular }
             *     
             */
            public void setAngular(RigidConstraint.TechniqueCommon.Spring.Angular value) {
                this.angular = value;
            }

            public boolean isSetAngular() {
                return (this.angular!= null);
            }

            /**
             * Ruft den Wert der linear-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link RigidConstraint.TechniqueCommon.Spring.Linear }
             *     
             */
            public RigidConstraint.TechniqueCommon.Spring.Linear getLinear() {
                return linear;
            }

            /**
             * Legt den Wert der linear-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link RigidConstraint.TechniqueCommon.Spring.Linear }
             *     
             */
            public void setLinear(RigidConstraint.TechniqueCommon.Spring.Linear value) {
                this.linear = value;
            }

            public boolean isSetLinear() {
                return (this.linear!= null);
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
             *         &lt;element name="stiffness" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
             *         &lt;element name="damping" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
             *         &lt;element name="target_value" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
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
                "stiffness",
                "damping",
                "targetValue"
            })
            public static class Angular {

                @XmlElement(defaultValue = "1.0")
                protected TargetableFloat stiffness;
                @XmlElement(defaultValue = "0.0")
                protected TargetableFloat damping;
                @XmlElement(name = "target_value", defaultValue = "0.0")
                protected TargetableFloat targetValue;

                /**
                 * Ruft den Wert der stiffness-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public TargetableFloat getStiffness() {
                    return stiffness;
                }

                /**
                 * Legt den Wert der stiffness-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public void setStiffness(TargetableFloat value) {
                    this.stiffness = value;
                }

                public boolean isSetStiffness() {
                    return (this.stiffness!= null);
                }

                /**
                 * Ruft den Wert der damping-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public TargetableFloat getDamping() {
                    return damping;
                }

                /**
                 * Legt den Wert der damping-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public void setDamping(TargetableFloat value) {
                    this.damping = value;
                }

                public boolean isSetDamping() {
                    return (this.damping!= null);
                }

                /**
                 * Ruft den Wert der targetValue-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public TargetableFloat getTargetValue() {
                    return targetValue;
                }

                /**
                 * Legt den Wert der targetValue-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public void setTargetValue(TargetableFloat value) {
                    this.targetValue = value;
                }

                public boolean isSetTargetValue() {
                    return (this.targetValue!= null);
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
             *         &lt;element name="stiffness" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
             *         &lt;element name="damping" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
             *         &lt;element name="target_value" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
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
                "stiffness",
                "damping",
                "targetValue"
            })
            public static class Linear {

                @XmlElement(defaultValue = "1.0")
                protected TargetableFloat stiffness;
                @XmlElement(defaultValue = "0.0")
                protected TargetableFloat damping;
                @XmlElement(name = "target_value", defaultValue = "0.0")
                protected TargetableFloat targetValue;

                /**
                 * Ruft den Wert der stiffness-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public TargetableFloat getStiffness() {
                    return stiffness;
                }

                /**
                 * Legt den Wert der stiffness-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public void setStiffness(TargetableFloat value) {
                    this.stiffness = value;
                }

                public boolean isSetStiffness() {
                    return (this.stiffness!= null);
                }

                /**
                 * Ruft den Wert der damping-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public TargetableFloat getDamping() {
                    return damping;
                }

                /**
                 * Legt den Wert der damping-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public void setDamping(TargetableFloat value) {
                    this.damping = value;
                }

                public boolean isSetDamping() {
                    return (this.damping!= null);
                }

                /**
                 * Ruft den Wert der targetValue-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public TargetableFloat getTargetValue() {
                    return targetValue;
                }

                /**
                 * Legt den Wert der targetValue-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link TargetableFloat }
                 *     
                 */
                public void setTargetValue(TargetableFloat value) {
                    this.targetValue = value;
                }

                public boolean isSetTargetValue() {
                    return (this.targetValue!= null);
                }

            }

        }

    }

}
