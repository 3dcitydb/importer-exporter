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
 * <p>Java-Klasse für LodType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="LodType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}minLodPixels" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}maxLodPixels" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}minFadeExtent" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}maxFadeExtent" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LodSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LodObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LodType", propOrder = {
    "minLodPixels",
    "maxLodPixels",
    "minFadeExtent",
    "maxFadeExtent",
    "lodSimpleExtensionGroup",
    "lodObjectExtensionGroup"
})
public class LodType
    extends AbstractObjectType
{

    @XmlElement(defaultValue = "0.0")
    protected Double minLodPixels;
    @XmlElement(defaultValue = "-1.0")
    protected Double maxLodPixels;
    @XmlElement(defaultValue = "0.0")
    protected Double minFadeExtent;
    @XmlElement(defaultValue = "0.0")
    protected Double maxFadeExtent;
    @XmlElement(name = "LodSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> lodSimpleExtensionGroup;
    @XmlElement(name = "LodObjectExtensionGroup")
    protected List<AbstractObjectType> lodObjectExtensionGroup;

    /**
     * Ruft den Wert der minLodPixels-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMinLodPixels() {
        return minLodPixels;
    }

    /**
     * Legt den Wert der minLodPixels-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMinLodPixels(Double value) {
        this.minLodPixels = value;
    }

    public boolean isSetMinLodPixels() {
        return (this.minLodPixels!= null);
    }

    /**
     * Ruft den Wert der maxLodPixels-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMaxLodPixels() {
        return maxLodPixels;
    }

    /**
     * Legt den Wert der maxLodPixels-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMaxLodPixels(Double value) {
        this.maxLodPixels = value;
    }

    public boolean isSetMaxLodPixels() {
        return (this.maxLodPixels!= null);
    }

    /**
     * Ruft den Wert der minFadeExtent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMinFadeExtent() {
        return minFadeExtent;
    }

    /**
     * Legt den Wert der minFadeExtent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMinFadeExtent(Double value) {
        this.minFadeExtent = value;
    }

    public boolean isSetMinFadeExtent() {
        return (this.minFadeExtent!= null);
    }

    /**
     * Ruft den Wert der maxFadeExtent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMaxFadeExtent() {
        return maxFadeExtent;
    }

    /**
     * Legt den Wert der maxFadeExtent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMaxFadeExtent(Double value) {
        this.maxFadeExtent = value;
    }

    public boolean isSetMaxFadeExtent() {
        return (this.maxFadeExtent!= null);
    }

    /**
     * Gets the value of the lodSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lodSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLodSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getLodSimpleExtensionGroup() {
        if (lodSimpleExtensionGroup == null) {
            lodSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.lodSimpleExtensionGroup;
    }

    public boolean isSetLodSimpleExtensionGroup() {
        return ((this.lodSimpleExtensionGroup!= null)&&(!this.lodSimpleExtensionGroup.isEmpty()));
    }

    public void unsetLodSimpleExtensionGroup() {
        this.lodSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the lodObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lodObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLodObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getLodObjectExtensionGroup() {
        if (lodObjectExtensionGroup == null) {
            lodObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.lodObjectExtensionGroup;
    }

    public boolean isSetLodObjectExtensionGroup() {
        return ((this.lodObjectExtensionGroup!= null)&&(!this.lodObjectExtensionGroup.isEmpty()));
    }

    public void unsetLodObjectExtensionGroup() {
        this.lodObjectExtensionGroup = null;
    }

    public void setLodSimpleExtensionGroup(List<Object> value) {
        this.lodSimpleExtensionGroup = value;
    }

    public void setLodObjectExtensionGroup(List<AbstractObjectType> value) {
        this.lodObjectExtensionGroup = value;
    }

}
