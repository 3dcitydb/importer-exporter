//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
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
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}lookat"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}matrix"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}rotate"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}scale"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}skew"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}translate"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_camera" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_controller" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_geometry" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_light" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}instance_node" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}node" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="type" type="{http://www.collada.org/2005/11/COLLADASchema}NodeType" default="NODE" />
 *       &lt;attribute name="layer" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfNames" />
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
    "lookatOrMatrixOrRotate",
    "instanceCamera",
    "instanceController",
    "instanceGeometry",
    "instanceLight",
    "instanceNode",
    "node",
    "extra"
})
@XmlRootElement(name = "node")
public class Node {

    protected Asset asset;
    @XmlElementRefs({
        @XmlElementRef(name = "rotate", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = Rotate.class, required = false),
        @XmlElementRef(name = "scale", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "lookat", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = Lookat.class, required = false),
        @XmlElementRef(name = "matrix", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = Matrix.class, required = false),
        @XmlElementRef(name = "translate", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "skew", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = Skew.class, required = false)
    })
    protected List<Object> lookatOrMatrixOrRotate;
    @XmlElement(name = "instance_camera")
    protected List<InstanceWithExtra> instanceCamera;
    @XmlElement(name = "instance_controller")
    protected List<InstanceController> instanceController;
    @XmlElement(name = "instance_geometry")
    protected List<InstanceGeometry> instanceGeometry;
    @XmlElement(name = "instance_light")
    protected List<InstanceWithExtra> instanceLight;
    @XmlElement(name = "instance_node")
    protected List<InstanceWithExtra> instanceNode;
    protected List<Node> node;
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
    @XmlAttribute(name = "sid")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String sid;
    @XmlAttribute(name = "type")
    protected NodeType type;
    @XmlAttribute(name = "layer")
    protected List<String> layer;

    /**
     * 
     * 						The node element may contain an asset element.
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
     * Gets the value of the lookatOrMatrixOrRotate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lookatOrMatrixOrRotate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLookatOrMatrixOrRotate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Rotate }
     * {@link JAXBElement }{@code <}{@link TargetableFloat3 }{@code >}
     * {@link Lookat }
     * {@link Matrix }
     * {@link JAXBElement }{@code <}{@link TargetableFloat3 }{@code >}
     * {@link Skew }
     * 
     * 
     */
    public List<Object> getLookatOrMatrixOrRotate() {
        if (lookatOrMatrixOrRotate == null) {
            lookatOrMatrixOrRotate = new ArrayList<Object>();
        }
        return this.lookatOrMatrixOrRotate;
    }

    public boolean isSetLookatOrMatrixOrRotate() {
        return ((this.lookatOrMatrixOrRotate!= null)&&(!this.lookatOrMatrixOrRotate.isEmpty()));
    }

    public void unsetLookatOrMatrixOrRotate() {
        this.lookatOrMatrixOrRotate = null;
    }

    /**
     * 
     * 						The node element may instance any number of camera objects.
     * 						Gets the value of the instanceCamera property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instanceCamera property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstanceCamera().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstanceWithExtra }
     * 
     * 
     */
    public List<InstanceWithExtra> getInstanceCamera() {
        if (instanceCamera == null) {
            instanceCamera = new ArrayList<InstanceWithExtra>();
        }
        return this.instanceCamera;
    }

    public boolean isSetInstanceCamera() {
        return ((this.instanceCamera!= null)&&(!this.instanceCamera.isEmpty()));
    }

    public void unsetInstanceCamera() {
        this.instanceCamera = null;
    }

    /**
     * 
     * 						The node element may instance any number of controller objects.
     * 						Gets the value of the instanceController property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instanceController property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstanceController().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstanceController }
     * 
     * 
     */
    public List<InstanceController> getInstanceController() {
        if (instanceController == null) {
            instanceController = new ArrayList<InstanceController>();
        }
        return this.instanceController;
    }

    public boolean isSetInstanceController() {
        return ((this.instanceController!= null)&&(!this.instanceController.isEmpty()));
    }

    public void unsetInstanceController() {
        this.instanceController = null;
    }

    /**
     * 
     * 						The node element may instance any number of geometry objects.
     * 						Gets the value of the instanceGeometry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instanceGeometry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstanceGeometry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstanceGeometry }
     * 
     * 
     */
    public List<InstanceGeometry> getInstanceGeometry() {
        if (instanceGeometry == null) {
            instanceGeometry = new ArrayList<InstanceGeometry>();
        }
        return this.instanceGeometry;
    }

    public boolean isSetInstanceGeometry() {
        return ((this.instanceGeometry!= null)&&(!this.instanceGeometry.isEmpty()));
    }

    public void unsetInstanceGeometry() {
        this.instanceGeometry = null;
    }

    /**
     * 
     * 						The node element may instance any number of light objects.
     * 						Gets the value of the instanceLight property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instanceLight property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstanceLight().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstanceWithExtra }
     * 
     * 
     */
    public List<InstanceWithExtra> getInstanceLight() {
        if (instanceLight == null) {
            instanceLight = new ArrayList<InstanceWithExtra>();
        }
        return this.instanceLight;
    }

    public boolean isSetInstanceLight() {
        return ((this.instanceLight!= null)&&(!this.instanceLight.isEmpty()));
    }

    public void unsetInstanceLight() {
        this.instanceLight = null;
    }

    /**
     * 
     * 						The node element may instance any number of node elements or hierarchies objects.
     * 						Gets the value of the instanceNode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instanceNode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstanceNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstanceWithExtra }
     * 
     * 
     */
    public List<InstanceWithExtra> getInstanceNode() {
        if (instanceNode == null) {
            instanceNode = new ArrayList<InstanceWithExtra>();
        }
        return this.instanceNode;
    }

    public boolean isSetInstanceNode() {
        return ((this.instanceNode!= null)&&(!this.instanceNode.isEmpty()));
    }

    public void unsetInstanceNode() {
        this.instanceNode = null;
    }

    /**
     * 
     * 						The node element may be hierarchical and be the parent of any number of other node elements.
     * 						Gets the value of the node property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the node property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Node }
     * 
     * 
     */
    public List<Node> getNode() {
        if (node == null) {
            node = new ArrayList<Node>();
        }
        return this.node;
    }

    public boolean isSetNode() {
        return ((this.node!= null)&&(!this.node.isEmpty()));
    }

    public void unsetNode() {
        this.node = null;
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
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NodeType }
     *     
     */
    public NodeType getType() {
        if (type == null) {
            return NodeType.NODE;
        } else {
            return type;
        }
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeType }
     *     
     */
    public void setType(NodeType value) {
        this.type = value;
    }

    public boolean isSetType() {
        return (this.type!= null);
    }

    /**
     * Gets the value of the layer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the layer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLayer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLayer() {
        if (layer == null) {
            layer = new ArrayList<String>();
        }
        return this.layer;
    }

    public boolean isSetLayer() {
        return ((this.layer!= null)&&(!this.layer.isEmpty()));
    }

    public void unsetLayer() {
        this.layer = null;
    }

    public void setLookatOrMatrixOrRotate(List<Object> value) {
        this.lookatOrMatrixOrRotate = value;
    }

    public void setInstanceCamera(List<InstanceWithExtra> value) {
        this.instanceCamera = value;
    }

    public void setInstanceController(List<InstanceController> value) {
        this.instanceController = value;
    }

    public void setInstanceGeometry(List<InstanceGeometry> value) {
        this.instanceGeometry = value;
    }

    public void setInstanceLight(List<InstanceWithExtra> value) {
        this.instanceLight = value;
    }

    public void setInstanceNode(List<InstanceWithExtra> value) {
        this.instanceNode = value;
    }

    public void setNode(List<Node> value) {
        this.node = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

    public void setLayer(List<String> value) {
        this.layer = value;
    }

}
