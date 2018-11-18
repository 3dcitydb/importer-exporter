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
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_force_field" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_rigid_body" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_rigid_constraint" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="url" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *       &lt;attribute name="parent" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "instanceForceField",
    "instanceRigidBody",
    "instanceRigidConstraint",
    "extra"
})
@XmlRootElement(name = "instance_physics_model")
public class InstancePhysicsModel {

    @XmlElement(name = "instance_force_field")
    protected List<InstanceWithExtra> instanceForceField;
    @XmlElement(name = "instance_rigid_body")
    protected List<InstanceRigidBody> instanceRigidBody;
    @XmlElement(name = "instance_rigid_constraint")
    protected List<InstanceRigidConstraint> instanceRigidConstraint;
    protected List<Extra> extra;
    @XmlAttribute(name = "url", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String url;
    @XmlAttribute(name = "sid")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String sid;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String name;
    @XmlAttribute(name = "parent")
    @XmlSchemaType(name = "anyURI")
    protected String parent;

    /**
     * 
     * 						The instance_physics_model element may instance any number of force_field elements.
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
     * 						The instance_physics_model element may instance any number of rigid_body elements.
     * 						Gets the value of the instanceRigidBody property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instanceRigidBody property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstanceRigidBody().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstanceRigidBody }
     * 
     * 
     */
    public List<InstanceRigidBody> getInstanceRigidBody() {
        if (instanceRigidBody == null) {
            instanceRigidBody = new ArrayList<InstanceRigidBody>();
        }
        return this.instanceRigidBody;
    }

    public boolean isSetInstanceRigidBody() {
        return ((this.instanceRigidBody!= null)&&(!this.instanceRigidBody.isEmpty()));
    }

    public void unsetInstanceRigidBody() {
        this.instanceRigidBody = null;
    }

    /**
     * 
     * 						The instance_physics_model element may instance any number of rigid_constraint elements.
     * 						Gets the value of the instanceRigidConstraint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instanceRigidConstraint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstanceRigidConstraint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstanceRigidConstraint }
     * 
     * 
     */
    public List<InstanceRigidConstraint> getInstanceRigidConstraint() {
        if (instanceRigidConstraint == null) {
            instanceRigidConstraint = new ArrayList<InstanceRigidConstraint>();
        }
        return this.instanceRigidConstraint;
    }

    public boolean isSetInstanceRigidConstraint() {
        return ((this.instanceRigidConstraint!= null)&&(!this.instanceRigidConstraint.isEmpty()));
    }

    public void unsetInstanceRigidConstraint() {
        this.instanceRigidConstraint = null;
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
     * Ruft den Wert der url-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Legt den Wert der url-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

    public boolean isSetUrl() {
        return (this.url!= null);
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
     * Ruft den Wert der parent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParent() {
        return parent;
    }

    /**
     * Legt den Wert der parent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParent(String value) {
        this.parent = value;
    }

    public boolean isSetParent() {
        return (this.parent!= null);
    }

    public void setInstanceForceField(List<InstanceWithExtra> value) {
        this.instanceForceField = value;
    }

    public void setInstanceRigidBody(List<InstanceRigidBody> value) {
        this.instanceRigidBody = value;
    }

    public void setInstanceRigidConstraint(List<InstanceRigidConstraint> value) {
        this.instanceRigidConstraint = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

}
