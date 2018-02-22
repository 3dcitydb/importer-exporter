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
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rigid_body" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rigid_constraint" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_physics_model" maxOccurs="unbounded" minOccurs="0"/>
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
    "rigidBody",
    "rigidConstraint",
    "instancePhysicsModel",
    "extra"
})
@XmlRootElement(name = "physics_model")
public class PhysicsModel {

    protected Asset asset;
    @XmlElement(name = "rigid_body")
    protected List<RigidBody> rigidBody;
    @XmlElement(name = "rigid_constraint")
    protected List<RigidConstraint> rigidConstraint;
    @XmlElement(name = "instance_physics_model")
    protected List<InstancePhysicsModel> instancePhysicsModel;
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
     * 						The physics_model element may contain an asset element.
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
     * 						The physics_model may define any number of rigid_body elements.
     * 						Gets the value of the rigidBody property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rigidBody property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRigidBody().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RigidBody }
     * 
     * 
     */
    public List<RigidBody> getRigidBody() {
        if (rigidBody == null) {
            rigidBody = new ArrayList<RigidBody>();
        }
        return this.rigidBody;
    }

    public boolean isSetRigidBody() {
        return ((this.rigidBody!= null)&&(!this.rigidBody.isEmpty()));
    }

    public void unsetRigidBody() {
        this.rigidBody = null;
    }

    /**
     * 
     * 						The physics_model may define any number of rigid_constraint elements.
     * 						Gets the value of the rigidConstraint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rigidConstraint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRigidConstraint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RigidConstraint }
     * 
     * 
     */
    public List<RigidConstraint> getRigidConstraint() {
        if (rigidConstraint == null) {
            rigidConstraint = new ArrayList<RigidConstraint>();
        }
        return this.rigidConstraint;
    }

    public boolean isSetRigidConstraint() {
        return ((this.rigidConstraint!= null)&&(!this.rigidConstraint.isEmpty()));
    }

    public void unsetRigidConstraint() {
        this.rigidConstraint = null;
    }

    /**
     * 
     * 						The physics_model may instance any number of other physics_model elements.
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

    public void setRigidBody(List<RigidBody> value) {
        this.rigidBody = value;
    }

    public void setRigidConstraint(List<RigidConstraint> value) {
        this.rigidConstraint = value;
    }

    public void setInstancePhysicsModel(List<InstancePhysicsModel> value) {
        this.instancePhysicsModel = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

}
