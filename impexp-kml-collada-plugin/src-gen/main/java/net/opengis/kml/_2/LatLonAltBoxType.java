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
 * <p>Java-Klasse für LatLonAltBoxType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="LatLonAltBoxType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractLatLonBoxType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}minAltitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}maxAltitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitudeModeGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LatLonAltBoxSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LatLonAltBoxObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LatLonAltBoxType", propOrder = {
    "minAltitude",
    "maxAltitude",
    "altitudeModeGroup",
    "latLonAltBoxSimpleExtensionGroup",
    "latLonAltBoxObjectExtensionGroup"
})
public class LatLonAltBoxType
    extends AbstractLatLonBoxType
{

    @XmlElement(defaultValue = "0.0")
    protected Double minAltitude;
    @XmlElement(defaultValue = "0.0")
    protected Double maxAltitude;
    @XmlElementRef(name = "altitudeModeGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<?> altitudeModeGroup;
    @XmlElement(name = "LatLonAltBoxSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> latLonAltBoxSimpleExtensionGroup;
    @XmlElement(name = "LatLonAltBoxObjectExtensionGroup")
    protected List<AbstractObjectType> latLonAltBoxObjectExtensionGroup;

    /**
     * Ruft den Wert der minAltitude-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMinAltitude() {
        return minAltitude;
    }

    /**
     * Legt den Wert der minAltitude-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMinAltitude(Double value) {
        this.minAltitude = value;
    }

    public boolean isSetMinAltitude() {
        return (this.minAltitude!= null);
    }

    /**
     * Ruft den Wert der maxAltitude-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMaxAltitude() {
        return maxAltitude;
    }

    /**
     * Legt den Wert der maxAltitude-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMaxAltitude(Double value) {
        this.maxAltitude = value;
    }

    public boolean isSetMaxAltitude() {
        return (this.maxAltitude!= null);
    }

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
     * Gets the value of the latLonAltBoxSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the latLonAltBoxSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLatLonAltBoxSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getLatLonAltBoxSimpleExtensionGroup() {
        if (latLonAltBoxSimpleExtensionGroup == null) {
            latLonAltBoxSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.latLonAltBoxSimpleExtensionGroup;
    }

    public boolean isSetLatLonAltBoxSimpleExtensionGroup() {
        return ((this.latLonAltBoxSimpleExtensionGroup!= null)&&(!this.latLonAltBoxSimpleExtensionGroup.isEmpty()));
    }

    public void unsetLatLonAltBoxSimpleExtensionGroup() {
        this.latLonAltBoxSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the latLonAltBoxObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the latLonAltBoxObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLatLonAltBoxObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getLatLonAltBoxObjectExtensionGroup() {
        if (latLonAltBoxObjectExtensionGroup == null) {
            latLonAltBoxObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.latLonAltBoxObjectExtensionGroup;
    }

    public boolean isSetLatLonAltBoxObjectExtensionGroup() {
        return ((this.latLonAltBoxObjectExtensionGroup!= null)&&(!this.latLonAltBoxObjectExtensionGroup.isEmpty()));
    }

    public void unsetLatLonAltBoxObjectExtensionGroup() {
        this.latLonAltBoxObjectExtensionGroup = null;
    }

    public void setLatLonAltBoxSimpleExtensionGroup(List<Object> value) {
        this.latLonAltBoxSimpleExtensionGroup = value;
    }

    public void setLatLonAltBoxObjectExtensionGroup(List<AbstractObjectType> value) {
        this.latLonAltBoxObjectExtensionGroup = value;
    }

}
