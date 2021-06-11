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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für AbstractLatLonBoxType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractLatLonBoxType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}north" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}south" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}east" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}west" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractLatLonBoxSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractLatLonBoxObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractLatLonBoxType", propOrder = {
    "north",
    "south",
    "east",
    "west",
    "abstractLatLonBoxSimpleExtensionGroup",
    "abstractLatLonBoxObjectExtensionGroup"
})
@XmlSeeAlso({
    LatLonAltBoxType.class,
    LatLonBoxType.class
})
public abstract class AbstractLatLonBoxType
    extends AbstractObjectType
{

    @XmlElement(defaultValue = "180.0")
    protected Double north;
    @XmlElement(defaultValue = "-180.0")
    protected Double south;
    @XmlElement(defaultValue = "180.0")
    protected Double east;
    @XmlElement(defaultValue = "-180.0")
    protected Double west;
    @XmlElement(name = "AbstractLatLonBoxSimpleExtensionGroup")
    protected List<Object> abstractLatLonBoxSimpleExtensionGroup;
    @XmlElement(name = "AbstractLatLonBoxObjectExtensionGroup")
    protected List<AbstractObjectType> abstractLatLonBoxObjectExtensionGroup;

    /**
     * Ruft den Wert der north-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getNorth() {
        return north;
    }

    /**
     * Legt den Wert der north-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setNorth(Double value) {
        this.north = value;
    }

    public boolean isSetNorth() {
        return (this.north!= null);
    }

    /**
     * Ruft den Wert der south-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getSouth() {
        return south;
    }

    /**
     * Legt den Wert der south-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setSouth(Double value) {
        this.south = value;
    }

    public boolean isSetSouth() {
        return (this.south!= null);
    }

    /**
     * Ruft den Wert der east-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getEast() {
        return east;
    }

    /**
     * Legt den Wert der east-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setEast(Double value) {
        this.east = value;
    }

    public boolean isSetEast() {
        return (this.east!= null);
    }

    /**
     * Ruft den Wert der west-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWest() {
        return west;
    }

    /**
     * Legt den Wert der west-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWest(Double value) {
        this.west = value;
    }

    public boolean isSetWest() {
        return (this.west!= null);
    }

    /**
     * Gets the value of the abstractLatLonBoxSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractLatLonBoxSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractLatLonBoxSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAbstractLatLonBoxSimpleExtensionGroup() {
        if (abstractLatLonBoxSimpleExtensionGroup == null) {
            abstractLatLonBoxSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.abstractLatLonBoxSimpleExtensionGroup;
    }

    public boolean isSetAbstractLatLonBoxSimpleExtensionGroup() {
        return ((this.abstractLatLonBoxSimpleExtensionGroup!= null)&&(!this.abstractLatLonBoxSimpleExtensionGroup.isEmpty()));
    }

    public void unsetAbstractLatLonBoxSimpleExtensionGroup() {
        this.abstractLatLonBoxSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the abstractLatLonBoxObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractLatLonBoxObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractLatLonBoxObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getAbstractLatLonBoxObjectExtensionGroup() {
        if (abstractLatLonBoxObjectExtensionGroup == null) {
            abstractLatLonBoxObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.abstractLatLonBoxObjectExtensionGroup;
    }

    public boolean isSetAbstractLatLonBoxObjectExtensionGroup() {
        return ((this.abstractLatLonBoxObjectExtensionGroup!= null)&&(!this.abstractLatLonBoxObjectExtensionGroup.isEmpty()));
    }

    public void unsetAbstractLatLonBoxObjectExtensionGroup() {
        this.abstractLatLonBoxObjectExtensionGroup = null;
    }

    public void setAbstractLatLonBoxSimpleExtensionGroup(List<Object> value) {
        this.abstractLatLonBoxSimpleExtensionGroup = value;
    }

    public void setAbstractLatLonBoxObjectExtensionGroup(List<AbstractObjectType> value) {
        this.abstractLatLonBoxObjectExtensionGroup = value;
    }

}
