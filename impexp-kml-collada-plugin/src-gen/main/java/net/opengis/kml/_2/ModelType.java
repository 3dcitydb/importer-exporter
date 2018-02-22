//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ModelType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ModelType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractGeometryType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitudeModeGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Location" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Orientation" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Scale" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Link" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ResourceMap" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ModelSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ModelObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelType", propOrder = {
    "altitudeModeGroup",
    "location",
    "orientation",
    "scale",
    "link",
    "resourceMap",
    "modelSimpleExtensionGroup",
    "modelObjectExtensionGroup"
})
public class ModelType
    extends AbstractGeometryType
{

    @XmlElementRef(name = "altitudeModeGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<?> altitudeModeGroup;
    @XmlElement(name = "Location")
    protected LocationType location;
    @XmlElement(name = "Orientation")
    protected OrientationType orientation;
    @XmlElementRef(name = "Scale", namespace = "http://www.opengis.net/kml/2.2", type = ScaleElement.class, required = false)
    protected ScaleElement scale;
    @XmlElement(name = "Link")
    protected LinkType link;
    @XmlElement(name = "ResourceMap")
    protected ResourceMapType resourceMap;
    @XmlElement(name = "ModelSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> modelSimpleExtensionGroup;
    @XmlElement(name = "ModelObjectExtensionGroup")
    protected List<AbstractObjectType> modelObjectExtensionGroup;

    /**
     * Ruft den Wert der altitudeModeGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *     
     */
    public JAXBElement<?> getAltitudeModeGroup() {
        return altitudeModeGroup;
    }

    /**
     * Legt den Wert der altitudeModeGroup-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *     
     */
    public void setAltitudeModeGroup(JAXBElement<?> value) {
        this.altitudeModeGroup = value;
    }

    public boolean isSetAltitudeModeGroup() {
        return (this.altitudeModeGroup!= null);
    }

    /**
     * Ruft den Wert der location-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LocationType }
     *     
     */
    public LocationType getLocation() {
        return location;
    }

    /**
     * Legt den Wert der location-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationType }
     *     
     */
    public void setLocation(LocationType value) {
        this.location = value;
    }

    public boolean isSetLocation() {
        return (this.location!= null);
    }

    /**
     * Ruft den Wert der orientation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OrientationType }
     *     
     */
    public OrientationType getOrientation() {
        return orientation;
    }

    /**
     * Legt den Wert der orientation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OrientationType }
     *     
     */
    public void setOrientation(OrientationType value) {
        this.orientation = value;
    }

    public boolean isSetOrientation() {
        return (this.orientation!= null);
    }

    /**
     * Ruft den Wert der scale-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ScaleElement }
     *     
     */
    public ScaleElement getScale() {
        return scale;
    }

    /**
     * Legt den Wert der scale-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ScaleElement }
     *     
     */
    public void setScale(ScaleElement value) {
        this.scale = value;
    }

    public boolean isSetScale() {
        return (this.scale!= null);
    }

    /**
     * Ruft den Wert der link-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LinkType }
     *     
     */
    public LinkType getLink() {
        return link;
    }

    /**
     * Legt den Wert der link-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkType }
     *     
     */
    public void setLink(LinkType value) {
        this.link = value;
    }

    public boolean isSetLink() {
        return (this.link!= null);
    }

    /**
     * Ruft den Wert der resourceMap-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ResourceMapType }
     *     
     */
    public ResourceMapType getResourceMap() {
        return resourceMap;
    }

    /**
     * Legt den Wert der resourceMap-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ResourceMapType }
     *     
     */
    public void setResourceMap(ResourceMapType value) {
        this.resourceMap = value;
    }

    public boolean isSetResourceMap() {
        return (this.resourceMap!= null);
    }

    /**
     * Gets the value of the modelSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModelSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getModelSimpleExtensionGroup() {
        if (modelSimpleExtensionGroup == null) {
            modelSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.modelSimpleExtensionGroup;
    }

    public boolean isSetModelSimpleExtensionGroup() {
        return ((this.modelSimpleExtensionGroup!= null)&&(!this.modelSimpleExtensionGroup.isEmpty()));
    }

    public void unsetModelSimpleExtensionGroup() {
        this.modelSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the modelObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModelObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getModelObjectExtensionGroup() {
        if (modelObjectExtensionGroup == null) {
            modelObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.modelObjectExtensionGroup;
    }

    public boolean isSetModelObjectExtensionGroup() {
        return ((this.modelObjectExtensionGroup!= null)&&(!this.modelObjectExtensionGroup.isEmpty()));
    }

    public void unsetModelObjectExtensionGroup() {
        this.modelObjectExtensionGroup = null;
    }

    public void setModelSimpleExtensionGroup(List<Object> value) {
        this.modelSimpleExtensionGroup = value;
    }

    public void setModelObjectExtensionGroup(List<AbstractObjectType> value) {
        this.modelObjectExtensionGroup = value;
    }

}
