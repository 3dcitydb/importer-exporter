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
 * <p>Java-Klasse für ScaleType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ScaleType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}x" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}y" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}z" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ScaleSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ScaleObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScaleType", propOrder = {
    "x",
    "y",
    "z",
    "scaleSimpleExtensionGroup",
    "scaleObjectExtensionGroup"
})
public class ScaleType
    extends AbstractObjectType
{

    @XmlElement(defaultValue = "1.0")
    protected Double x;
    @XmlElement(defaultValue = "1.0")
    protected Double y;
    @XmlElement(defaultValue = "1.0")
    protected Double z;
    @XmlElement(name = "ScaleSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> scaleSimpleExtensionGroup;
    @XmlElement(name = "ScaleObjectExtensionGroup")
    protected List<AbstractObjectType> scaleObjectExtensionGroup;

    /**
     * Ruft den Wert der x-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getX() {
        return x;
    }

    /**
     * Legt den Wert der x-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setX(Double value) {
        this.x = value;
    }

    public boolean isSetX() {
        return (this.x!= null);
    }

    /**
     * Ruft den Wert der y-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getY() {
        return y;
    }

    /**
     * Legt den Wert der y-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setY(Double value) {
        this.y = value;
    }

    public boolean isSetY() {
        return (this.y!= null);
    }

    /**
     * Ruft den Wert der z-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getZ() {
        return z;
    }

    /**
     * Legt den Wert der z-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setZ(Double value) {
        this.z = value;
    }

    public boolean isSetZ() {
        return (this.z!= null);
    }

    /**
     * Gets the value of the scaleSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scaleSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScaleSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getScaleSimpleExtensionGroup() {
        if (scaleSimpleExtensionGroup == null) {
            scaleSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.scaleSimpleExtensionGroup;
    }

    public boolean isSetScaleSimpleExtensionGroup() {
        return ((this.scaleSimpleExtensionGroup!= null)&&(!this.scaleSimpleExtensionGroup.isEmpty()));
    }

    public void unsetScaleSimpleExtensionGroup() {
        this.scaleSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the scaleObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scaleObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScaleObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getScaleObjectExtensionGroup() {
        if (scaleObjectExtensionGroup == null) {
            scaleObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.scaleObjectExtensionGroup;
    }

    public boolean isSetScaleObjectExtensionGroup() {
        return ((this.scaleObjectExtensionGroup!= null)&&(!this.scaleObjectExtensionGroup.isEmpty()));
    }

    public void unsetScaleObjectExtensionGroup() {
        this.scaleObjectExtensionGroup = null;
    }

    public void setScaleSimpleExtensionGroup(List<Object> value) {
        this.scaleSimpleExtensionGroup = value;
    }

    public void setScaleObjectExtensionGroup(List<AbstractObjectType> value) {
        this.scaleObjectExtensionGroup = value;
    }

}
