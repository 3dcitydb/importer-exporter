//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für AbstractObjectType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractObjectType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ObjectSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://www.opengis.net/kml/2.2}idAttributes"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractObjectType", propOrder = {
    "objectSimpleExtensionGroup"
})
@XmlSeeAlso({
    AbstractViewType.class,
    SchemaDataType.class,
    DataType.class,
    RegionType.class,
    LodType.class,
    AbstractGeometryType.class,
    LocationType.class,
    OrientationType.class,
    ScaleType.class,
    ResourceMapType.class,
    AliasType.class,
    AbstractFeatureType.class,
    ViewVolumeType.class,
    ImagePyramidType.class,
    AbstractStyleSelectorType.class,
    PairType.class,
    AbstractSubStyleType.class,
    ItemIconType.class,
    AbstractTimePrimitiveType.class,
    AbstractLatLonBoxType.class,
    BasicLinkType.class
})
public abstract class AbstractObjectType {

    @XmlElement(name = "ObjectSimpleExtensionGroup")
    protected List<Object> objectSimpleExtensionGroup;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "targetId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String targetId;

    /**
     * Gets the value of the objectSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getObjectSimpleExtensionGroup() {
        if (objectSimpleExtensionGroup == null) {
            objectSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.objectSimpleExtensionGroup;
    }

    public boolean isSetObjectSimpleExtensionGroup() {
        return ((this.objectSimpleExtensionGroup!= null)&&(!this.objectSimpleExtensionGroup.isEmpty()));
    }

    public void unsetObjectSimpleExtensionGroup() {
        this.objectSimpleExtensionGroup = null;
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
     * Ruft den Wert der targetId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Legt den Wert der targetId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetId(String value) {
        this.targetId = value;
    }

    public boolean isSetTargetId() {
        return (this.targetId!= null);
    }

    public void setObjectSimpleExtensionGroup(List<Object> value) {
        this.objectSimpleExtensionGroup = value;
    }

}
