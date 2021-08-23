//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für GroundOverlayType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="GroundOverlayType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractOverlayType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitude" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitudeModeGroup" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LatLonBox" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}GroundOverlaySimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}GroundOverlayObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GroundOverlayType", propOrder = {
    "altitude",
    "altitudeModeGroup",
    "latLonBox",
    "groundOverlaySimpleExtensionGroup",
    "groundOverlayObjectExtensionGroup"
})
public class GroundOverlayType
    extends AbstractOverlayType
{

    @XmlElement(defaultValue = "0.0")
    protected Double altitude;
    @XmlElementRef(name = "altitudeModeGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<?> altitudeModeGroup;
    @XmlElement(name = "LatLonBox")
    protected LatLonBoxType latLonBox;
    @XmlElement(name = "GroundOverlaySimpleExtensionGroup")
    protected List<Object> groundOverlaySimpleExtensionGroup;
    @XmlElement(name = "GroundOverlayObjectExtensionGroup")
    protected List<AbstractObjectType> groundOverlayObjectExtensionGroup;

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
     * Ruft den Wert der altitudeModeGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
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
     *     {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    public void setAltitudeModeGroup(JAXBElement<?> value) {
        this.altitudeModeGroup = value;
    }

    public boolean isSetAltitudeModeGroup() {
        return (this.altitudeModeGroup!= null);
    }

    /**
     * Ruft den Wert der latLonBox-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LatLonBoxType }
     *     
     */
    public LatLonBoxType getLatLonBox() {
        return latLonBox;
    }

    /**
     * Legt den Wert der latLonBox-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LatLonBoxType }
     *     
     */
    public void setLatLonBox(LatLonBoxType value) {
        this.latLonBox = value;
    }

    public boolean isSetLatLonBox() {
        return (this.latLonBox!= null);
    }

    /**
     * Gets the value of the groundOverlaySimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the groundOverlaySimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGroundOverlaySimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getGroundOverlaySimpleExtensionGroup() {
        if (groundOverlaySimpleExtensionGroup == null) {
            groundOverlaySimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.groundOverlaySimpleExtensionGroup;
    }

    public boolean isSetGroundOverlaySimpleExtensionGroup() {
        return ((this.groundOverlaySimpleExtensionGroup!= null)&&(!this.groundOverlaySimpleExtensionGroup.isEmpty()));
    }

    public void unsetGroundOverlaySimpleExtensionGroup() {
        this.groundOverlaySimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the groundOverlayObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the groundOverlayObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGroundOverlayObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getGroundOverlayObjectExtensionGroup() {
        if (groundOverlayObjectExtensionGroup == null) {
            groundOverlayObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.groundOverlayObjectExtensionGroup;
    }

    public boolean isSetGroundOverlayObjectExtensionGroup() {
        return ((this.groundOverlayObjectExtensionGroup!= null)&&(!this.groundOverlayObjectExtensionGroup.isEmpty()));
    }

    public void unsetGroundOverlayObjectExtensionGroup() {
        this.groundOverlayObjectExtensionGroup = null;
    }

    public void setGroundOverlaySimpleExtensionGroup(List<Object> value) {
        this.groundOverlaySimpleExtensionGroup = value;
    }

    public void setGroundOverlayObjectExtensionGroup(List<AbstractObjectType> value) {
        this.groundOverlayObjectExtensionGroup = value;
    }

}
