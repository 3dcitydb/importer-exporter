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
 * <p>Java-Klasse für PlacemarkType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PlacemarkType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractFeatureType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractGeometryGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PlacemarkSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PlacemarkObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlacemarkType", propOrder = {
    "abstractGeometryGroup",
    "placemarkSimpleExtensionGroup",
    "placemarkObjectExtensionGroup"
})
public class PlacemarkType
    extends AbstractFeatureType
{

    @XmlElementRef(name = "AbstractGeometryGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends AbstractGeometryType> abstractGeometryGroup;
    @XmlElement(name = "PlacemarkSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> placemarkSimpleExtensionGroup;
    @XmlElement(name = "PlacemarkObjectExtensionGroup")
    protected List<AbstractObjectType> placemarkObjectExtensionGroup;

    /**
     * Ruft den Wert der abstractGeometryGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link MultiGeometryType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ModelType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PointType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LineStringType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PolygonType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LinearRingType }{@code >}
     *     
     */
    public JAXBElement<? extends AbstractGeometryType> getAbstractGeometryGroup() {
        return abstractGeometryGroup;
    }

    /**
     * Legt den Wert der abstractGeometryGroup-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link MultiGeometryType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ModelType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PointType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LineStringType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PolygonType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LinearRingType }{@code >}
     *     
     */
    public void setAbstractGeometryGroup(JAXBElement<? extends AbstractGeometryType> value) {
        this.abstractGeometryGroup = value;
    }

    public boolean isSetAbstractGeometryGroup() {
        return (this.abstractGeometryGroup!= null);
    }

    /**
     * Gets the value of the placemarkSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the placemarkSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlacemarkSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getPlacemarkSimpleExtensionGroup() {
        if (placemarkSimpleExtensionGroup == null) {
            placemarkSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.placemarkSimpleExtensionGroup;
    }

    public boolean isSetPlacemarkSimpleExtensionGroup() {
        return ((this.placemarkSimpleExtensionGroup!= null)&&(!this.placemarkSimpleExtensionGroup.isEmpty()));
    }

    public void unsetPlacemarkSimpleExtensionGroup() {
        this.placemarkSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the placemarkObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the placemarkObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlacemarkObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getPlacemarkObjectExtensionGroup() {
        if (placemarkObjectExtensionGroup == null) {
            placemarkObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.placemarkObjectExtensionGroup;
    }

    public boolean isSetPlacemarkObjectExtensionGroup() {
        return ((this.placemarkObjectExtensionGroup!= null)&&(!this.placemarkObjectExtensionGroup.isEmpty()));
    }

    public void unsetPlacemarkObjectExtensionGroup() {
        this.placemarkObjectExtensionGroup = null;
    }

    public void setPlacemarkSimpleExtensionGroup(List<Object> value) {
        this.placemarkSimpleExtensionGroup = value;
    }

    public void setPlacemarkObjectExtensionGroup(List<AbstractObjectType> value) {
        this.placemarkObjectExtensionGroup = value;
    }

}
