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
 * <p>Java-Klasse für RegionType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RegionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LatLonAltBox" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Lod" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}RegionSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}RegionObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegionType", propOrder = {
    "latLonAltBox",
    "lod",
    "regionSimpleExtensionGroup",
    "regionObjectExtensionGroup"
})
public class RegionType
    extends AbstractObjectType
{

    @XmlElement(name = "LatLonAltBox")
    protected LatLonAltBoxType latLonAltBox;
    @XmlElement(name = "Lod")
    protected LodType lod;
    @XmlElement(name = "RegionSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> regionSimpleExtensionGroup;
    @XmlElement(name = "RegionObjectExtensionGroup")
    protected List<AbstractObjectType> regionObjectExtensionGroup;

    /**
     * Ruft den Wert der latLonAltBox-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LatLonAltBoxType }
     *     
     */
    public LatLonAltBoxType getLatLonAltBox() {
        return latLonAltBox;
    }

    /**
     * Legt den Wert der latLonAltBox-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LatLonAltBoxType }
     *     
     */
    public void setLatLonAltBox(LatLonAltBoxType value) {
        this.latLonAltBox = value;
    }

    public boolean isSetLatLonAltBox() {
        return (this.latLonAltBox!= null);
    }

    /**
     * Ruft den Wert der lod-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LodType }
     *     
     */
    public LodType getLod() {
        return lod;
    }

    /**
     * Legt den Wert der lod-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LodType }
     *     
     */
    public void setLod(LodType value) {
        this.lod = value;
    }

    public boolean isSetLod() {
        return (this.lod!= null);
    }

    /**
     * Gets the value of the regionSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the regionSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegionSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getRegionSimpleExtensionGroup() {
        if (regionSimpleExtensionGroup == null) {
            regionSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.regionSimpleExtensionGroup;
    }

    public boolean isSetRegionSimpleExtensionGroup() {
        return ((this.regionSimpleExtensionGroup!= null)&&(!this.regionSimpleExtensionGroup.isEmpty()));
    }

    public void unsetRegionSimpleExtensionGroup() {
        this.regionSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the regionObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the regionObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegionObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getRegionObjectExtensionGroup() {
        if (regionObjectExtensionGroup == null) {
            regionObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.regionObjectExtensionGroup;
    }

    public boolean isSetRegionObjectExtensionGroup() {
        return ((this.regionObjectExtensionGroup!= null)&&(!this.regionObjectExtensionGroup.isEmpty()));
    }

    public void unsetRegionObjectExtensionGroup() {
        this.regionObjectExtensionGroup = null;
    }

    public void setRegionSimpleExtensionGroup(List<Object> value) {
        this.regionSimpleExtensionGroup = value;
    }

    public void setRegionObjectExtensionGroup(List<AbstractObjectType> value) {
        this.regionObjectExtensionGroup = value;
    }

}
