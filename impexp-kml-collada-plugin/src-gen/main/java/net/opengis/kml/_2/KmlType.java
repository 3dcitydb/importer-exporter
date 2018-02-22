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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für KmlType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="KmlType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}NetworkLinkControl" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractFeatureGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}KmlSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}KmlObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="hint" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KmlType", propOrder = {
    "networkLinkControl",
    "abstractFeatureGroup",
    "kmlSimpleExtensionGroup",
    "kmlObjectExtensionGroup"
})
public class KmlType {

    @XmlElement(name = "NetworkLinkControl")
    protected NetworkLinkControlType networkLinkControl;
    @XmlElementRef(name = "AbstractFeatureGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends AbstractFeatureType> abstractFeatureGroup;
    @XmlElement(name = "KmlSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> kmlSimpleExtensionGroup;
    @XmlElement(name = "KmlObjectExtensionGroup")
    protected List<AbstractObjectType> kmlObjectExtensionGroup;
    @XmlAttribute(name = "hint")
    protected String hint;

    /**
     * Ruft den Wert der networkLinkControl-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NetworkLinkControlType }
     *     
     */
    public NetworkLinkControlType getNetworkLinkControl() {
        return networkLinkControl;
    }

    /**
     * Legt den Wert der networkLinkControl-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NetworkLinkControlType }
     *     
     */
    public void setNetworkLinkControl(NetworkLinkControlType value) {
        this.networkLinkControl = value;
    }

    public boolean isSetNetworkLinkControl() {
        return (this.networkLinkControl!= null);
    }

    /**
     * Ruft den Wert der abstractFeatureGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AbstractContainerType }{@code >}
     *     {@link JAXBElement }{@code <}{@link NetworkLinkType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ScreenOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link GroundOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractFeatureType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PhotoOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DocumentType }{@code >}
     *     {@link JAXBElement }{@code <}{@link FolderType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PlacemarkType }{@code >}
     *     
     */
    public JAXBElement<? extends AbstractFeatureType> getAbstractFeatureGroup() {
        return abstractFeatureGroup;
    }

    /**
     * Legt den Wert der abstractFeatureGroup-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AbstractContainerType }{@code >}
     *     {@link JAXBElement }{@code <}{@link NetworkLinkType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ScreenOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link GroundOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractFeatureType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PhotoOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DocumentType }{@code >}
     *     {@link JAXBElement }{@code <}{@link FolderType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PlacemarkType }{@code >}
     *     
     */
    public void setAbstractFeatureGroup(JAXBElement<? extends AbstractFeatureType> value) {
        this.abstractFeatureGroup = value;
    }

    public boolean isSetAbstractFeatureGroup() {
        return (this.abstractFeatureGroup!= null);
    }

    /**
     * Gets the value of the kmlSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the kmlSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKmlSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getKmlSimpleExtensionGroup() {
        if (kmlSimpleExtensionGroup == null) {
            kmlSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.kmlSimpleExtensionGroup;
    }

    public boolean isSetKmlSimpleExtensionGroup() {
        return ((this.kmlSimpleExtensionGroup!= null)&&(!this.kmlSimpleExtensionGroup.isEmpty()));
    }

    public void unsetKmlSimpleExtensionGroup() {
        this.kmlSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the kmlObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the kmlObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKmlObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getKmlObjectExtensionGroup() {
        if (kmlObjectExtensionGroup == null) {
            kmlObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.kmlObjectExtensionGroup;
    }

    public boolean isSetKmlObjectExtensionGroup() {
        return ((this.kmlObjectExtensionGroup!= null)&&(!this.kmlObjectExtensionGroup.isEmpty()));
    }

    public void unsetKmlObjectExtensionGroup() {
        this.kmlObjectExtensionGroup = null;
    }

    /**
     * Ruft den Wert der hint-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHint() {
        return hint;
    }

    /**
     * Legt den Wert der hint-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHint(String value) {
        this.hint = value;
    }

    public boolean isSetHint() {
        return (this.hint!= null);
    }

    public void setKmlSimpleExtensionGroup(List<Object> value) {
        this.kmlSimpleExtensionGroup = value;
    }

    public void setKmlObjectExtensionGroup(List<AbstractObjectType> value) {
        this.kmlObjectExtensionGroup = value;
    }

}
