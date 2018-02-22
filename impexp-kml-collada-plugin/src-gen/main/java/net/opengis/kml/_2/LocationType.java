//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für LocationType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="LocationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}longitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}latitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LocationSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LocationObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocationType", propOrder = {
    "longitude",
    "latitude",
    "altitude",
    "locationSimpleExtensionGroup",
    "locationObjectExtensionGroup"
})
public class LocationType
    extends AbstractObjectType
{

    @XmlElement(defaultValue = "0.0")
    protected Double longitude;
    @XmlElement(defaultValue = "0.0")
    protected Double latitude;
    @XmlElement(defaultValue = "0.0")
    protected Double altitude;
    @XmlElement(name = "LocationSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> locationSimpleExtensionGroup;
    @XmlElement(name = "LocationObjectExtensionGroup")
    protected List<AbstractObjectType> locationObjectExtensionGroup;

    /**
     * Ruft den Wert der longitude-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Legt den Wert der longitude-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLongitude(Double value) {
        this.longitude = value;
    }

    public boolean isSetLongitude() {
        return (this.longitude!= null);
    }

    /**
     * Ruft den Wert der latitude-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Legt den Wert der latitude-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLatitude(Double value) {
        this.latitude = value;
    }

    public boolean isSetLatitude() {
        return (this.latitude!= null);
    }

    /**
     * Ruft den Wert der altitude-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getAltitude() {
        return altitude;
    }

    /**
     * Legt den Wert der altitude-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setAltitude(Double value) {
        this.altitude = value;
    }

    public boolean isSetAltitude() {
        return (this.altitude!= null);
    }

    /**
     * Gets the value of the locationSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the locationSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocationSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getLocationSimpleExtensionGroup() {
        if (locationSimpleExtensionGroup == null) {
            locationSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.locationSimpleExtensionGroup;
    }

    public boolean isSetLocationSimpleExtensionGroup() {
        return ((this.locationSimpleExtensionGroup!= null)&&(!this.locationSimpleExtensionGroup.isEmpty()));
    }

    public void unsetLocationSimpleExtensionGroup() {
        this.locationSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the locationObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the locationObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocationObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getLocationObjectExtensionGroup() {
        if (locationObjectExtensionGroup == null) {
            locationObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.locationObjectExtensionGroup;
    }

    public boolean isSetLocationObjectExtensionGroup() {
        return ((this.locationObjectExtensionGroup!= null)&&(!this.locationObjectExtensionGroup.isEmpty()));
    }

    public void unsetLocationObjectExtensionGroup() {
        this.locationObjectExtensionGroup = null;
    }

    public void setLocationSimpleExtensionGroup(List<Object> value) {
        this.locationSimpleExtensionGroup = value;
    }

    public void setLocationObjectExtensionGroup(List<AbstractObjectType> value) {
        this.locationObjectExtensionGroup = value;
    }

}
