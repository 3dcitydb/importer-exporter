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
import javax.xml.bind.annotation.XmlList;
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
 *         &lt;element name="technique_common">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="angular_velocity" type="{http://www.collada.org/2005/11/COLLADASchema}float3" minOccurs="0"/>
 *                   &lt;element name="velocity" type="{http://www.collada.org/2005/11/COLLADASchema}float3" minOccurs="0"/>
 *                   &lt;element name="dynamic" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
 *                           &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="mass" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
 *                   &lt;element name="mass_frame" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;choice maxOccurs="unbounded">
 *                             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
 *                             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
 *                           &lt;/choice>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="inertia" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
 *                   &lt;choice minOccurs="0">
 *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_physics_material"/>
 *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}physics_material"/>
 *                   &lt;/choice>
 *                   &lt;element name="shape" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="hollow" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;simpleContent>
 *                                   &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
 *                                     &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                                   &lt;/extension>
 *                                 &lt;/simpleContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="mass" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
 *                             &lt;element name="density" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
 *                             &lt;choice minOccurs="0">
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_physics_material"/>
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}physics_material"/>
 *                             &lt;/choice>
 *                             &lt;choice>
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_geometry"/>
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}plane"/>
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}box"/>
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}sphere"/>
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}cylinder"/>
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}tapered_cylinder"/>
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}capsule"/>
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}tapered_capsule"/>
 *                             &lt;/choice>
 *                             &lt;choice maxOccurs="unbounded" minOccurs="0">
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
 *                               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
 *                             &lt;/choice>
 *                             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
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
 *       &lt;attribute name="body" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="target" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
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
@XmlRootElement(name = "instance_rigid_body")
public class InstanceRigidBody {

    @XmlElement(name = "technique_common", required = true)
    protected InstanceRigidBody.TechniqueCommon techniqueCommon;
    protected List<Technique> technique;
    protected List<Extra> extra;
    @XmlAttribute(name = "body", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String body;
    @XmlAttribute(name = "sid")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String sid;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String name;
    @XmlAttribute(name = "target", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String target;

    /**
     * Ruft den Wert der techniqueCommon-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link InstanceRigidBody.TechniqueCommon }
     *     
     */
    public InstanceRigidBody.TechniqueCommon getTechniqueCommon() {
        return techniqueCommon;
    }

    /**
     * Legt den Wert der techniqueCommon-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link InstanceRigidBody.TechniqueCommon }
     *     
     */
    public void setTechniqueCommon(InstanceRigidBody.TechniqueCommon value) {
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
     * Ruft den Wert der body-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBody() {
        return body;
    }

    /**
     * Legt den Wert der body-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBody(String value) {
        this.body = value;
    }

    public boolean isSetBody() {
        return (this.body!= null);
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

    /**
     * Ruft den Wert der target-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTarget() {
        return target;
    }

    /**
     * Legt den Wert der target-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTarget(String value) {
        this.target = value;
    }

    public boolean isSetTarget() {
        return (this.target!= null);
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
     *       &lt;sequence>
     *         &lt;element name="angular_velocity" type="{http://www.collada.org/2005/11/COLLADASchema}float3" minOccurs="0"/>
     *         &lt;element name="velocity" type="{http://www.collada.org/2005/11/COLLADASchema}float3" minOccurs="0"/>
     *         &lt;element name="dynamic" minOccurs="0">
     *           &lt;complexType>
     *             &lt;simpleContent>
     *               &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
     *                 &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *               &lt;/extension>
     *             &lt;/simpleContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="mass" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
     *         &lt;element name="mass_frame" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;choice maxOccurs="unbounded">
     *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
     *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
     *                 &lt;/choice>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="inertia" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
     *         &lt;choice minOccurs="0">
     *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_physics_material"/>
     *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}physics_material"/>
     *         &lt;/choice>
     *         &lt;element name="shape" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="hollow" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;simpleContent>
     *                         &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
     *                           &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *                         &lt;/extension>
     *                       &lt;/simpleContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="mass" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
     *                   &lt;element name="density" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
     *                   &lt;choice minOccurs="0">
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_physics_material"/>
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}physics_material"/>
     *                   &lt;/choice>
     *                   &lt;choice>
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_geometry"/>
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}plane"/>
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}box"/>
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}sphere"/>
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}cylinder"/>
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}tapered_cylinder"/>
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}capsule"/>
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}tapered_capsule"/>
     *                   &lt;/choice>
     *                   &lt;choice maxOccurs="unbounded" minOccurs="0">
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
     *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
     *                   &lt;/choice>
     *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
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
        "angularVelocity",
        "velocity",
        "dynamic",
        "mass",
        "massFrame",
        "inertia",
        "instancePhysicsMaterial",
        "physicsMaterial",
        "shape"
    })
    public static class TechniqueCommon {

        @XmlList
        @XmlElement(name = "angular_velocity", type = Double.class, defaultValue = "0.0 0.0 0.0")
        protected List<Double> angularVelocity;
        @XmlList
        @XmlElement(type = Double.class, defaultValue = "0.0 0.0 0.0")
        protected List<Double> velocity;
        protected InstanceRigidBody.TechniqueCommon.Dynamic dynamic;
        protected TargetableFloat mass;
        @XmlElement(name = "mass_frame")
        protected InstanceRigidBody.TechniqueCommon.MassFrame massFrame;
        protected TargetableFloat3 inertia;
        @XmlElement(name = "instance_physics_material")
        protected InstanceWithExtra instancePhysicsMaterial;
        @XmlElement(name = "physics_material")
        protected PhysicsMaterial physicsMaterial;
        protected List<InstanceRigidBody.TechniqueCommon.Shape> shape;

        /**
         * Gets the value of the angularVelocity property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the angularVelocity property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAngularVelocity().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Double }
         * 
         * 
         */
        public List<Double> getAngularVelocity() {
            if (angularVelocity == null) {
                angularVelocity = new ArrayList<Double>();
            }
            return this.angularVelocity;
        }

        public boolean isSetAngularVelocity() {
            return ((this.angularVelocity!= null)&&(!this.angularVelocity.isEmpty()));
        }

        public void unsetAngularVelocity() {
            this.angularVelocity = null;
        }

        /**
         * Gets the value of the velocity property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the velocity property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getVelocity().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Double }
         * 
         * 
         */
        public List<Double> getVelocity() {
            if (velocity == null) {
                velocity = new ArrayList<Double>();
            }
            return this.velocity;
        }

        public boolean isSetVelocity() {
            return ((this.velocity!= null)&&(!this.velocity.isEmpty()));
        }

        public void unsetVelocity() {
            this.velocity = null;
        }

        /**
         * Ruft den Wert der dynamic-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link InstanceRigidBody.TechniqueCommon.Dynamic }
         *     
         */
        public InstanceRigidBody.TechniqueCommon.Dynamic getDynamic() {
            return dynamic;
        }

        /**
         * Legt den Wert der dynamic-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link InstanceRigidBody.TechniqueCommon.Dynamic }
         *     
         */
        public void setDynamic(InstanceRigidBody.TechniqueCommon.Dynamic value) {
            this.dynamic = value;
        }

        public boolean isSetDynamic() {
            return (this.dynamic!= null);
        }

        /**
         * Ruft den Wert der mass-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link TargetableFloat }
         *     
         */
        public TargetableFloat getMass() {
            return mass;
        }

        /**
         * Legt den Wert der mass-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link TargetableFloat }
         *     
         */
        public void setMass(TargetableFloat value) {
            this.mass = value;
        }

        public boolean isSetMass() {
            return (this.mass!= null);
        }

        /**
         * Ruft den Wert der massFrame-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link InstanceRigidBody.TechniqueCommon.MassFrame }
         *     
         */
        public InstanceRigidBody.TechniqueCommon.MassFrame getMassFrame() {
            return massFrame;
        }

        /**
         * Legt den Wert der massFrame-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link InstanceRigidBody.TechniqueCommon.MassFrame }
         *     
         */
        public void setMassFrame(InstanceRigidBody.TechniqueCommon.MassFrame value) {
            this.massFrame = value;
        }

        public boolean isSetMassFrame() {
            return (this.massFrame!= null);
        }

        /**
         * Ruft den Wert der inertia-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link TargetableFloat3 }
         *     
         */
        public TargetableFloat3 getInertia() {
            return inertia;
        }

        /**
         * Legt den Wert der inertia-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link TargetableFloat3 }
         *     
         */
        public void setInertia(TargetableFloat3 value) {
            this.inertia = value;
        }

        public boolean isSetInertia() {
            return (this.inertia!= null);
        }

        /**
         * Ruft den Wert der instancePhysicsMaterial-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link InstanceWithExtra }
         *     
         */
        public InstanceWithExtra getInstancePhysicsMaterial() {
            return instancePhysicsMaterial;
        }

        /**
         * Legt den Wert der instancePhysicsMaterial-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link InstanceWithExtra }
         *     
         */
        public void setInstancePhysicsMaterial(InstanceWithExtra value) {
            this.instancePhysicsMaterial = value;
        }

        public boolean isSetInstancePhysicsMaterial() {
            return (this.instancePhysicsMaterial!= null);
        }

        /**
         * Ruft den Wert der physicsMaterial-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link PhysicsMaterial }
         *     
         */
        public PhysicsMaterial getPhysicsMaterial() {
            return physicsMaterial;
        }

        /**
         * Legt den Wert der physicsMaterial-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link PhysicsMaterial }
         *     
         */
        public void setPhysicsMaterial(PhysicsMaterial value) {
            this.physicsMaterial = value;
        }

        public boolean isSetPhysicsMaterial() {
            return (this.physicsMaterial!= null);
        }

        /**
         * Gets the value of the shape property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the shape property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getShape().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link InstanceRigidBody.TechniqueCommon.Shape }
         * 
         * 
         */
        public List<InstanceRigidBody.TechniqueCommon.Shape> getShape() {
            if (shape == null) {
                shape = new ArrayList<InstanceRigidBody.TechniqueCommon.Shape>();
            }
            return this.shape;
        }

        public boolean isSetShape() {
            return ((this.shape!= null)&&(!this.shape.isEmpty()));
        }

        public void unsetShape() {
            this.shape = null;
        }

        public void setAngularVelocity(List<Double> value) {
            this.angularVelocity = value;
        }

        public void setVelocity(List<Double> value) {
            this.velocity = value;
        }

        public void setShape(List<InstanceRigidBody.TechniqueCommon.Shape> value) {
            this.shape = value;
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
        public static class Dynamic {

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
         *       &lt;choice maxOccurs="unbounded">
         *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
         *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
         *       &lt;/choice>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "translateOrRotate"
        })
        public static class MassFrame {

            @XmlElements({
                @XmlElement(name = "translate", type = TargetableFloat3 .class),
                @XmlElement(name = "rotate", type = Rotate.class)
            })
            protected List<Object> translateOrRotate;

            /**
             * Gets the value of the translateOrRotate property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the translateOrRotate property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getTranslateOrRotate().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link TargetableFloat3 }
             * {@link Rotate }
             * 
             * 
             */
            public List<Object> getTranslateOrRotate() {
                if (translateOrRotate == null) {
                    translateOrRotate = new ArrayList<Object>();
                }
                return this.translateOrRotate;
            }

            public boolean isSetTranslateOrRotate() {
                return ((this.translateOrRotate!= null)&&(!this.translateOrRotate.isEmpty()));
            }

            public void unsetTranslateOrRotate() {
                this.translateOrRotate = null;
            }

            public void setTranslateOrRotate(List<Object> value) {
                this.translateOrRotate = value;
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
         *         &lt;element name="hollow" minOccurs="0">
         *           &lt;complexType>
         *             &lt;simpleContent>
         *               &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema>bool">
         *                 &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
         *               &lt;/extension>
         *             &lt;/simpleContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="mass" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
         *         &lt;element name="density" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
         *         &lt;choice minOccurs="0">
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_physics_material"/>
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}physics_material"/>
         *         &lt;/choice>
         *         &lt;choice>
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_geometry"/>
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}plane"/>
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}box"/>
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}sphere"/>
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}cylinder"/>
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}tapered_cylinder"/>
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}capsule"/>
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}tapered_capsule"/>
         *         &lt;/choice>
         *         &lt;choice maxOccurs="unbounded" minOccurs="0">
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
         *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
         *         &lt;/choice>
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
        @XmlType(name = "", propOrder = {
            "hollow",
            "mass",
            "density",
            "instancePhysicsMaterial",
            "physicsMaterial",
            "instanceGeometry",
            "plane",
            "box",
            "sphere",
            "cylinder",
            "taperedCylinder",
            "capsule",
            "taperedCapsule",
            "translateOrRotate",
            "extra"
        })
        public static class Shape {

            protected InstanceRigidBody.TechniqueCommon.Shape.Hollow hollow;
            protected TargetableFloat mass;
            protected TargetableFloat density;
            @XmlElement(name = "instance_physics_material")
            protected InstanceWithExtra instancePhysicsMaterial;
            @XmlElement(name = "physics_material")
            protected PhysicsMaterial physicsMaterial;
            @XmlElement(name = "instance_geometry")
            protected InstanceGeometry instanceGeometry;
            protected Plane plane;
            protected Box box;
            protected Sphere sphere;
            protected Cylinder cylinder;
            @XmlElement(name = "tapered_cylinder")
            protected TaperedCylinder taperedCylinder;
            protected Capsule capsule;
            @XmlElement(name = "tapered_capsule")
            protected TaperedCapsule taperedCapsule;
            @XmlElements({
                @XmlElement(name = "translate", type = TargetableFloat3 .class),
                @XmlElement(name = "rotate", type = Rotate.class)
            })
            protected List<Object> translateOrRotate;
            protected List<Extra> extra;

            /**
             * Ruft den Wert der hollow-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link InstanceRigidBody.TechniqueCommon.Shape.Hollow }
             *     
             */
            public InstanceRigidBody.TechniqueCommon.Shape.Hollow getHollow() {
                return hollow;
            }

            /**
             * Legt den Wert der hollow-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link InstanceRigidBody.TechniqueCommon.Shape.Hollow }
             *     
             */
            public void setHollow(InstanceRigidBody.TechniqueCommon.Shape.Hollow value) {
                this.hollow = value;
            }

            public boolean isSetHollow() {
                return (this.hollow!= null);
            }

            /**
             * Ruft den Wert der mass-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat }
             *     
             */
            public TargetableFloat getMass() {
                return mass;
            }

            /**
             * Legt den Wert der mass-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat }
             *     
             */
            public void setMass(TargetableFloat value) {
                this.mass = value;
            }

            public boolean isSetMass() {
                return (this.mass!= null);
            }

            /**
             * Ruft den Wert der density-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat }
             *     
             */
            public TargetableFloat getDensity() {
                return density;
            }

            /**
             * Legt den Wert der density-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat }
             *     
             */
            public void setDensity(TargetableFloat value) {
                this.density = value;
            }

            public boolean isSetDensity() {
                return (this.density!= null);
            }

            /**
             * Ruft den Wert der instancePhysicsMaterial-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link InstanceWithExtra }
             *     
             */
            public InstanceWithExtra getInstancePhysicsMaterial() {
                return instancePhysicsMaterial;
            }

            /**
             * Legt den Wert der instancePhysicsMaterial-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link InstanceWithExtra }
             *     
             */
            public void setInstancePhysicsMaterial(InstanceWithExtra value) {
                this.instancePhysicsMaterial = value;
            }

            public boolean isSetInstancePhysicsMaterial() {
                return (this.instancePhysicsMaterial!= null);
            }

            /**
             * Ruft den Wert der physicsMaterial-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link PhysicsMaterial }
             *     
             */
            public PhysicsMaterial getPhysicsMaterial() {
                return physicsMaterial;
            }

            /**
             * Legt den Wert der physicsMaterial-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link PhysicsMaterial }
             *     
             */
            public void setPhysicsMaterial(PhysicsMaterial value) {
                this.physicsMaterial = value;
            }

            public boolean isSetPhysicsMaterial() {
                return (this.physicsMaterial!= null);
            }

            /**
             * Ruft den Wert der instanceGeometry-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link InstanceGeometry }
             *     
             */
            public InstanceGeometry getInstanceGeometry() {
                return instanceGeometry;
            }

            /**
             * Legt den Wert der instanceGeometry-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link InstanceGeometry }
             *     
             */
            public void setInstanceGeometry(InstanceGeometry value) {
                this.instanceGeometry = value;
            }

            public boolean isSetInstanceGeometry() {
                return (this.instanceGeometry!= null);
            }

            /**
             * Ruft den Wert der plane-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Plane }
             *     
             */
            public Plane getPlane() {
                return plane;
            }

            /**
             * Legt den Wert der plane-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Plane }
             *     
             */
            public void setPlane(Plane value) {
                this.plane = value;
            }

            public boolean isSetPlane() {
                return (this.plane!= null);
            }

            /**
             * Ruft den Wert der box-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Box }
             *     
             */
            public Box getBox() {
                return box;
            }

            /**
             * Legt den Wert der box-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Box }
             *     
             */
            public void setBox(Box value) {
                this.box = value;
            }

            public boolean isSetBox() {
                return (this.box!= null);
            }

            /**
             * Ruft den Wert der sphere-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Sphere }
             *     
             */
            public Sphere getSphere() {
                return sphere;
            }

            /**
             * Legt den Wert der sphere-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Sphere }
             *     
             */
            public void setSphere(Sphere value) {
                this.sphere = value;
            }

            public boolean isSetSphere() {
                return (this.sphere!= null);
            }

            /**
             * Ruft den Wert der cylinder-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Cylinder }
             *     
             */
            public Cylinder getCylinder() {
                return cylinder;
            }

            /**
             * Legt den Wert der cylinder-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Cylinder }
             *     
             */
            public void setCylinder(Cylinder value) {
                this.cylinder = value;
            }

            public boolean isSetCylinder() {
                return (this.cylinder!= null);
            }

            /**
             * Ruft den Wert der taperedCylinder-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TaperedCylinder }
             *     
             */
            public TaperedCylinder getTaperedCylinder() {
                return taperedCylinder;
            }

            /**
             * Legt den Wert der taperedCylinder-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TaperedCylinder }
             *     
             */
            public void setTaperedCylinder(TaperedCylinder value) {
                this.taperedCylinder = value;
            }

            public boolean isSetTaperedCylinder() {
                return (this.taperedCylinder!= null);
            }

            /**
             * Ruft den Wert der capsule-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Capsule }
             *     
             */
            public Capsule getCapsule() {
                return capsule;
            }

            /**
             * Legt den Wert der capsule-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Capsule }
             *     
             */
            public void setCapsule(Capsule value) {
                this.capsule = value;
            }

            public boolean isSetCapsule() {
                return (this.capsule!= null);
            }

            /**
             * Ruft den Wert der taperedCapsule-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TaperedCapsule }
             *     
             */
            public TaperedCapsule getTaperedCapsule() {
                return taperedCapsule;
            }

            /**
             * Legt den Wert der taperedCapsule-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TaperedCapsule }
             *     
             */
            public void setTaperedCapsule(TaperedCapsule value) {
                this.taperedCapsule = value;
            }

            public boolean isSetTaperedCapsule() {
                return (this.taperedCapsule!= null);
            }

            /**
             * Gets the value of the translateOrRotate property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the translateOrRotate property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getTranslateOrRotate().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link TargetableFloat3 }
             * {@link Rotate }
             * 
             * 
             */
            public List<Object> getTranslateOrRotate() {
                if (translateOrRotate == null) {
                    translateOrRotate = new ArrayList<Object>();
                }
                return this.translateOrRotate;
            }

            public boolean isSetTranslateOrRotate() {
                return ((this.translateOrRotate!= null)&&(!this.translateOrRotate.isEmpty()));
            }

            public void unsetTranslateOrRotate() {
                this.translateOrRotate = null;
            }

            /**
             * 
             * 												The extra element may appear any number of times.
             * 												Gets the value of the extra property.
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

            public void setTranslateOrRotate(List<Object> value) {
                this.translateOrRotate = value;
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
            public static class Hollow {

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

        }

    }

}
