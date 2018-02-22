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
 * <p>Java-Klasse für ViewVolumeType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ViewVolumeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}leftFov" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}rightFov" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}bottomFov" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}topFov" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}near" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ViewVolumeSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ViewVolumeObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ViewVolumeType", propOrder = {
    "leftFov",
    "rightFov",
    "bottomFov",
    "topFov",
    "near",
    "viewVolumeSimpleExtensionGroup",
    "viewVolumeObjectExtensionGroup"
})
public class ViewVolumeType
    extends AbstractObjectType
{

    @XmlElement(defaultValue = "0.0")
    protected Double leftFov;
    @XmlElement(defaultValue = "0.0")
    protected Double rightFov;
    @XmlElement(defaultValue = "0.0")
    protected Double bottomFov;
    @XmlElement(defaultValue = "0.0")
    protected Double topFov;
    @XmlElement(defaultValue = "0.0")
    protected Double near;
    @XmlElement(name = "ViewVolumeSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> viewVolumeSimpleExtensionGroup;
    @XmlElement(name = "ViewVolumeObjectExtensionGroup")
    protected List<AbstractObjectType> viewVolumeObjectExtensionGroup;

    /**
     * Ruft den Wert der leftFov-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLeftFov() {
        return leftFov;
    }

    /**
     * Legt den Wert der leftFov-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLeftFov(Double value) {
        this.leftFov = value;
    }

    public boolean isSetLeftFov() {
        return (this.leftFov!= null);
    }

    /**
     * Ruft den Wert der rightFov-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRightFov() {
        return rightFov;
    }

    /**
     * Legt den Wert der rightFov-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRightFov(Double value) {
        this.rightFov = value;
    }

    public boolean isSetRightFov() {
        return (this.rightFov!= null);
    }

    /**
     * Ruft den Wert der bottomFov-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getBottomFov() {
        return bottomFov;
    }

    /**
     * Legt den Wert der bottomFov-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setBottomFov(Double value) {
        this.bottomFov = value;
    }

    public boolean isSetBottomFov() {
        return (this.bottomFov!= null);
    }

    /**
     * Ruft den Wert der topFov-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getTopFov() {
        return topFov;
    }

    /**
     * Legt den Wert der topFov-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTopFov(Double value) {
        this.topFov = value;
    }

    public boolean isSetTopFov() {
        return (this.topFov!= null);
    }

    /**
     * Ruft den Wert der near-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getNear() {
        return near;
    }

    /**
     * Legt den Wert der near-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setNear(Double value) {
        this.near = value;
    }

    public boolean isSetNear() {
        return (this.near!= null);
    }

    /**
     * Gets the value of the viewVolumeSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the viewVolumeSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getViewVolumeSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getViewVolumeSimpleExtensionGroup() {
        if (viewVolumeSimpleExtensionGroup == null) {
            viewVolumeSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.viewVolumeSimpleExtensionGroup;
    }

    public boolean isSetViewVolumeSimpleExtensionGroup() {
        return ((this.viewVolumeSimpleExtensionGroup!= null)&&(!this.viewVolumeSimpleExtensionGroup.isEmpty()));
    }

    public void unsetViewVolumeSimpleExtensionGroup() {
        this.viewVolumeSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the viewVolumeObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the viewVolumeObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getViewVolumeObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getViewVolumeObjectExtensionGroup() {
        if (viewVolumeObjectExtensionGroup == null) {
            viewVolumeObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.viewVolumeObjectExtensionGroup;
    }

    public boolean isSetViewVolumeObjectExtensionGroup() {
        return ((this.viewVolumeObjectExtensionGroup!= null)&&(!this.viewVolumeObjectExtensionGroup.isEmpty()));
    }

    public void unsetViewVolumeObjectExtensionGroup() {
        this.viewVolumeObjectExtensionGroup = null;
    }

    public void setViewVolumeSimpleExtensionGroup(List<Object> value) {
        this.viewVolumeSimpleExtensionGroup = value;
    }

    public void setViewVolumeObjectExtensionGroup(List<AbstractObjectType> value) {
        this.viewVolumeObjectExtensionGroup = value;
    }

}
