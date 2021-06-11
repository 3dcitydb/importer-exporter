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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import oasis.names.tc.ciq.xsdschema.xal._2.AddressDetails;
import org.w3._2005.atom.AtomPersonConstruct;
import org.w3._2005.atom.Link;


/**
 * <p>Java-Klasse für AbstractFeatureType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractFeatureType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}name" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}visibility" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}open" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.w3.org/2005/Atom}author" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.w3.org/2005/Atom}link" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}address" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}AddressDetails" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}phoneNumber" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}snippet" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}description" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractViewGroup" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractTimePrimitiveGroup" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}styleUrl" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractStyleSelectorGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Region" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Metadata" minOccurs="0"/&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}ExtendedData" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractFeatureSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractFeatureObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractFeatureType", propOrder = {
    "name",
    "visibility",
    "open",
    "author",
    "link",
    "address",
    "addressDetails",
    "phoneNumber",
    "snippet",
    "description",
    "abstractViewGroup",
    "abstractTimePrimitiveGroup",
    "styleUrl",
    "abstractStyleSelectorGroup",
    "region",
    "metadata",
    "extendedData",
    "abstractFeatureSimpleExtensionGroup",
    "abstractFeatureObjectExtensionGroup"
})
@XmlSeeAlso({
    AbstractContainerType.class,
    PlacemarkType.class,
    NetworkLinkType.class,
    AbstractOverlayType.class
})
public abstract class AbstractFeatureType
    extends AbstractObjectType
{

    protected String name;
    @XmlElement(defaultValue = "1")
    protected Boolean visibility;
    @XmlElement(defaultValue = "0")
    protected Boolean open;
    @XmlElement(namespace = "http://www.w3.org/2005/Atom")
    protected AtomPersonConstruct author;
    @XmlElement(namespace = "http://www.w3.org/2005/Atom")
    protected Link link;
    protected String address;
    @XmlElement(name = "AddressDetails", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected AddressDetails addressDetails;
    protected String phoneNumber;
    protected String snippet;
    protected String description;
    @XmlElementRef(name = "AbstractViewGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends AbstractViewType> abstractViewGroup;
    @XmlElementRef(name = "AbstractTimePrimitiveGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends AbstractTimePrimitiveType> abstractTimePrimitiveGroup;
    @XmlSchemaType(name = "anyURI")
    protected String styleUrl;
    @XmlElementRef(name = "AbstractStyleSelectorGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends AbstractStyleSelectorType>> abstractStyleSelectorGroup;
    @XmlElement(name = "Region")
    protected RegionType region;
    @XmlElement(name = "Metadata")
    protected MetadataType metadata;
    @XmlElement(name = "ExtendedData")
    protected ExtendedDataType extendedData;
    @XmlElement(name = "AbstractFeatureSimpleExtensionGroup")
    protected List<Object> abstractFeatureSimpleExtensionGroup;
    @XmlElement(name = "AbstractFeatureObjectExtensionGroup")
    protected List<AbstractObjectType> abstractFeatureObjectExtensionGroup;

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    public boolean isSetName() {
        return (this.name!= null);
    }

    /**
     * Ruft den Wert der visibility-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isVisibility() {
        return visibility;
    }

    /**
     * Legt den Wert der visibility-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVisibility(Boolean value) {
        this.visibility = value;
    }

    public boolean isSetVisibility() {
        return (this.visibility!= null);
    }

    /**
     * Ruft den Wert der open-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOpen() {
        return open;
    }

    /**
     * Legt den Wert der open-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOpen(Boolean value) {
        this.open = value;
    }

    public boolean isSetOpen() {
        return (this.open!= null);
    }

    /**
     * Ruft den Wert der author-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AtomPersonConstruct }
     *     
     */
    public AtomPersonConstruct getAuthor() {
        return author;
    }

    /**
     * Legt den Wert der author-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AtomPersonConstruct }
     *     
     */
    public void setAuthor(AtomPersonConstruct value) {
        this.author = value;
    }

    public boolean isSetAuthor() {
        return (this.author!= null);
    }

    /**
     * Ruft den Wert der link-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Link }
     *     
     */
    public Link getLink() {
        return link;
    }

    /**
     * Legt den Wert der link-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Link }
     *     
     */
    public void setLink(Link value) {
        this.link = value;
    }

    public boolean isSetLink() {
        return (this.link!= null);
    }

    /**
     * Ruft den Wert der address-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddress() {
        return address;
    }

    /**
     * Legt den Wert der address-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddress(String value) {
        this.address = value;
    }

    public boolean isSetAddress() {
        return (this.address!= null);
    }

    /**
     * Ruft den Wert der addressDetails-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AddressDetails }
     *     
     */
    public AddressDetails getAddressDetails() {
        return addressDetails;
    }

    /**
     * Legt den Wert der addressDetails-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressDetails }
     *     
     */
    public void setAddressDetails(AddressDetails value) {
        this.addressDetails = value;
    }

    public boolean isSetAddressDetails() {
        return (this.addressDetails!= null);
    }

    /**
     * Ruft den Wert der phoneNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Legt den Wert der phoneNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneNumber(String value) {
        this.phoneNumber = value;
    }

    public boolean isSetPhoneNumber() {
        return (this.phoneNumber!= null);
    }

    /**
     * Ruft den Wert der snippet-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSnippet() {
        return snippet;
    }

    /**
     * Legt den Wert der snippet-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSnippet(String value) {
        this.snippet = value;
    }

    public boolean isSetSnippet() {
        return (this.snippet!= null);
    }

    /**
     * Ruft den Wert der description-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Legt den Wert der description-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    public boolean isSetDescription() {
        return (this.description!= null);
    }

    /**
     * Ruft den Wert der abstractViewGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CameraType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractViewType }{@code >}
     *     
     */
    public JAXBElement<? extends AbstractViewType> getAbstractViewGroup() {
        return abstractViewGroup;
    }

    /**
     * Legt den Wert der abstractViewGroup-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CameraType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractViewType }{@code >}
     *     
     */
    public void setAbstractViewGroup(JAXBElement<? extends AbstractViewType> value) {
        this.abstractViewGroup = value;
    }

    public boolean isSetAbstractViewGroup() {
        return (this.abstractViewGroup!= null);
    }

    /**
     * Ruft den Wert der abstractTimePrimitiveGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link TimeStampType }{@code >}
     *     {@link JAXBElement }{@code <}{@link TimeSpanType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractTimePrimitiveType }{@code >}
     *     
     */
    public JAXBElement<? extends AbstractTimePrimitiveType> getAbstractTimePrimitiveGroup() {
        return abstractTimePrimitiveGroup;
    }

    /**
     * Legt den Wert der abstractTimePrimitiveGroup-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link TimeStampType }{@code >}
     *     {@link JAXBElement }{@code <}{@link TimeSpanType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractTimePrimitiveType }{@code >}
     *     
     */
    public void setAbstractTimePrimitiveGroup(JAXBElement<? extends AbstractTimePrimitiveType> value) {
        this.abstractTimePrimitiveGroup = value;
    }

    public boolean isSetAbstractTimePrimitiveGroup() {
        return (this.abstractTimePrimitiveGroup!= null);
    }

    /**
     * Ruft den Wert der styleUrl-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyleUrl() {
        return styleUrl;
    }

    /**
     * Legt den Wert der styleUrl-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyleUrl(String value) {
        this.styleUrl = value;
    }

    public boolean isSetStyleUrl() {
        return (this.styleUrl!= null);
    }

    /**
     * Gets the value of the abstractStyleSelectorGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractStyleSelectorGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractStyleSelectorGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link StyleMapType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractStyleSelectorType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractStyleSelectorType>> getAbstractStyleSelectorGroup() {
        if (abstractStyleSelectorGroup == null) {
            abstractStyleSelectorGroup = new ArrayList<JAXBElement<? extends AbstractStyleSelectorType>>();
        }
        return this.abstractStyleSelectorGroup;
    }

    public boolean isSetAbstractStyleSelectorGroup() {
        return ((this.abstractStyleSelectorGroup!= null)&&(!this.abstractStyleSelectorGroup.isEmpty()));
    }

    public void unsetAbstractStyleSelectorGroup() {
        this.abstractStyleSelectorGroup = null;
    }

    /**
     * Ruft den Wert der region-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RegionType }
     *     
     */
    public RegionType getRegion() {
        return region;
    }

    /**
     * Legt den Wert der region-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RegionType }
     *     
     */
    public void setRegion(RegionType value) {
        this.region = value;
    }

    public boolean isSetRegion() {
        return (this.region!= null);
    }

    /**
     * Ruft den Wert der metadata-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MetadataType }
     *     
     */
    public MetadataType getMetadata() {
        return metadata;
    }

    /**
     * Legt den Wert der metadata-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MetadataType }
     *     
     */
    public void setMetadata(MetadataType value) {
        this.metadata = value;
    }

    public boolean isSetMetadata() {
        return (this.metadata!= null);
    }

    /**
     * Ruft den Wert der extendedData-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ExtendedDataType }
     *     
     */
    public ExtendedDataType getExtendedData() {
        return extendedData;
    }

    /**
     * Legt den Wert der extendedData-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtendedDataType }
     *     
     */
    public void setExtendedData(ExtendedDataType value) {
        this.extendedData = value;
    }

    public boolean isSetExtendedData() {
        return (this.extendedData!= null);
    }

    /**
     * Gets the value of the abstractFeatureSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractFeatureSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractFeatureSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAbstractFeatureSimpleExtensionGroup() {
        if (abstractFeatureSimpleExtensionGroup == null) {
            abstractFeatureSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.abstractFeatureSimpleExtensionGroup;
    }

    public boolean isSetAbstractFeatureSimpleExtensionGroup() {
        return ((this.abstractFeatureSimpleExtensionGroup!= null)&&(!this.abstractFeatureSimpleExtensionGroup.isEmpty()));
    }

    public void unsetAbstractFeatureSimpleExtensionGroup() {
        this.abstractFeatureSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the abstractFeatureObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractFeatureObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractFeatureObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getAbstractFeatureObjectExtensionGroup() {
        if (abstractFeatureObjectExtensionGroup == null) {
            abstractFeatureObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.abstractFeatureObjectExtensionGroup;
    }

    public boolean isSetAbstractFeatureObjectExtensionGroup() {
        return ((this.abstractFeatureObjectExtensionGroup!= null)&&(!this.abstractFeatureObjectExtensionGroup.isEmpty()));
    }

    public void unsetAbstractFeatureObjectExtensionGroup() {
        this.abstractFeatureObjectExtensionGroup = null;
    }

    public void setAbstractStyleSelectorGroup(List<JAXBElement<? extends AbstractStyleSelectorType>> value) {
        this.abstractStyleSelectorGroup = value;
    }

    public void setAbstractFeatureSimpleExtensionGroup(List<Object> value) {
        this.abstractFeatureSimpleExtensionGroup = value;
    }

    public void setAbstractFeatureObjectExtensionGroup(List<AbstractObjectType> value) {
        this.abstractFeatureObjectExtensionGroup = value;
    }

}
