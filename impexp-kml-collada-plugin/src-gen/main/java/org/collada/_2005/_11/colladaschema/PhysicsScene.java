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
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_force_field" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_physics_model" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="technique_common">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="gravity" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
 *                   &lt;element name="time_step" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}technique" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
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
    "asset",
    "instanceForceField",
    "instancePhysicsModel",
    "techniqueCommon",
    "technique",
    "extra"
})
@XmlRootElement(name = "physics_scene")
public class PhysicsScene {

    protected Asset asset;
    @XmlElement(name = "instance_force_field")
    protected List<InstanceWithExtra> instanceForceField;
    @XmlElement(name = "instance_physics_model")
    protected List<InstancePhysicsModel> instancePhysicsModel;
    @XmlElement(name = "technique_common", required = true)
    protected PhysicsScene.TechniqueCommon techniqueCommon;
    protected List<Technique> technique;
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
     * 						The physics_scene element may contain an asset element.
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
     * 
     * 						There may be any number of instance_force_field elements.
     * 						Gets the value of the instanceForceField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instanceForceField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstanceForceField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstanceWithExtra }
     * 
     * 
     */
    public List<InstanceWithExtra> getInstanceForceField() {
        if (instanceForceField == null) {
            instanceForceField = new ArrayList<InstanceWithExtra>();
        }
        return this.instanceForceField;
    }

    public boolean isSetInstanceForceField() {
        return ((this.instanceForceField!= null)&&(!this.instanceForceField.isEmpty()));
    }

    public void unsetInstanceForceField() {
        this.instanceForceField = null;
    }

    /**
     * 
     * 						There may be any number of instance_physics_model elements.
     * 						Gets the value of the instancePhysicsModel property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instancePhysicsModel property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstancePhysicsModel().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstancePhysicsModel }
     * 
     * 
     */
    public List<InstancePhysicsModel> getInstancePhysicsModel() {
        if (instancePhysicsModel == null) {
            instancePhysicsModel = new ArrayList<InstancePhysicsModel>();
        }
        return this.instancePhysicsModel;
    }

    public boolean isSetInstancePhysicsModel() {
        return ((this.instancePhysicsModel!= null)&&(!this.instancePhysicsModel.isEmpty()));
    }

    public void unsetInstancePhysicsModel() {
        this.instancePhysicsModel = null;
    }

    /**
     * Ruft den Wert der techniqueCommon-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PhysicsScene.TechniqueCommon }
     *     
     */
    public PhysicsScene.TechniqueCommon getTechniqueCommon() {
        return techniqueCommon;
    }

    /**
     * Legt den Wert der techniqueCommon-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PhysicsScene.TechniqueCommon }
     *     
     */
    public void setTechniqueCommon(PhysicsScene.TechniqueCommon value) {
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

    public void setInstanceForceField(List<InstanceWithExtra> value) {
        this.instanceForceField = value;
    }

    public void setInstancePhysicsModel(List<InstancePhysicsModel> value) {
        this.instancePhysicsModel = value;
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
     *         &lt;element name="gravity" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3" minOccurs="0"/>
     *         &lt;element name="time_step" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/>
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
        "gravity",
        "timeStep"
    })
    public static class TechniqueCommon {

        protected TargetableFloat3 gravity;
        @XmlElement(name = "time_step")
        protected TargetableFloat timeStep;

        /**
         * Ruft den Wert der gravity-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link TargetableFloat3 }
         *     
         */
        public TargetableFloat3 getGravity() {
            return gravity;
        }

        /**
         * Legt den Wert der gravity-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link TargetableFloat3 }
         *     
         */
        public void setGravity(TargetableFloat3 value) {
            this.gravity = value;
        }

        public boolean isSetGravity() {
            return (this.gravity!= null);
        }

        /**
         * Ruft den Wert der timeStep-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link TargetableFloat }
         *     
         */
        public TargetableFloat getTimeStep() {
            return timeStep;
        }

        /**
         * Legt den Wert der timeStep-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link TargetableFloat }
         *     
         */
        public void setTimeStep(TargetableFloat value) {
            this.timeStep = value;
        }

        public boolean isSetTimeStep() {
            return (this.timeStep!= null);
        }

    }

}
