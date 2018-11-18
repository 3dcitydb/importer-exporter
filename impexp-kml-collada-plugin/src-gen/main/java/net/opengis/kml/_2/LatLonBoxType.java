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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für LatLonBoxType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="LatLonBoxType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractLatLonBoxType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}rotation" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LatLonBoxSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LatLonBoxObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LatLonBoxType", propOrder = {
    "rotation",
    "latLonBoxSimpleExtensionGroup",
    "latLonBoxObjectExtensionGroup"
})
public class LatLonBoxType
    extends AbstractLatLonBoxType
{

    @XmlElement(defaultValue = "0.0")
    protected Double rotation;
    @XmlElement(name = "LatLonBoxSimpleExtensionGroup")
    protected List<Object> latLonBoxSimpleExtensionGroup;
    @XmlElement(name = "LatLonBoxObjectExtensionGroup")
    protected List<AbstractObjectType> latLonBoxObjectExtensionGroup;

    /**
     * Ruft den Wert der rotation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRotation() {
        return rotation;
    }

    /**
     * Legt den Wert der rotation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRotation(Double value) {
        this.rotation = value;
    }

    public boolean isSetRotation() {
        return (this.rotation!= null);
    }

    /**
     * Gets the value of the latLonBoxSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the latLonBoxSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLatLonBoxSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getLatLonBoxSimpleExtensionGroup() {
        if (latLonBoxSimpleExtensionGroup == null) {
            latLonBoxSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.latLonBoxSimpleExtensionGroup;
    }

    public boolean isSetLatLonBoxSimpleExtensionGroup() {
        return ((this.latLonBoxSimpleExtensionGroup!= null)&&(!this.latLonBoxSimpleExtensionGroup.isEmpty()));
    }

    public void unsetLatLonBoxSimpleExtensionGroup() {
        this.latLonBoxSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the latLonBoxObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the latLonBoxObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLatLonBoxObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getLatLonBoxObjectExtensionGroup() {
        if (latLonBoxObjectExtensionGroup == null) {
            latLonBoxObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.latLonBoxObjectExtensionGroup;
    }

    public boolean isSetLatLonBoxObjectExtensionGroup() {
        return ((this.latLonBoxObjectExtensionGroup!= null)&&(!this.latLonBoxObjectExtensionGroup.isEmpty()));
    }

    public void unsetLatLonBoxObjectExtensionGroup() {
        this.latLonBoxObjectExtensionGroup = null;
    }

    public void setLatLonBoxSimpleExtensionGroup(List<Object> value) {
        this.latLonBoxSimpleExtensionGroup = value;
    }

    public void setLatLonBoxObjectExtensionGroup(List<AbstractObjectType> value) {
        this.latLonBoxObjectExtensionGroup = value;
    }

}
