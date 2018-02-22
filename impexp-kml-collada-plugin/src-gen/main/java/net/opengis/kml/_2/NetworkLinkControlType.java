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
 * <p>Java-Klasse für NetworkLinkControlType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="NetworkLinkControlType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}minRefreshPeriod" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}maxSessionLength" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}cookie" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}message" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}linkName" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}linkDescription" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}linkSnippet" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}expires" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Update" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractViewGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}NetworkLinkControlSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}NetworkLinkControlObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NetworkLinkControlType", propOrder = {
    "minRefreshPeriod",
    "maxSessionLength",
    "cookie",
    "message",
    "linkName",
    "linkDescription",
    "linkSnippet",
    "expires",
    "update",
    "abstractViewGroup",
    "networkLinkControlSimpleExtensionGroup",
    "networkLinkControlObjectExtensionGroup"
})
public class NetworkLinkControlType {

    @XmlElement(defaultValue = "0.0")
    protected Double minRefreshPeriod;
    @XmlElement(defaultValue = "-1.0")
    protected Double maxSessionLength;
    protected String cookie;
    protected String message;
    protected String linkName;
    protected String linkDescription;
    protected SnippetType linkSnippet;
    protected String expires;
    @XmlElement(name = "Update")
    protected UpdateType update;
    @XmlElementRef(name = "AbstractViewGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends AbstractViewType> abstractViewGroup;
    @XmlElement(name = "NetworkLinkControlSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> networkLinkControlSimpleExtensionGroup;
    @XmlElement(name = "NetworkLinkControlObjectExtensionGroup")
    protected List<AbstractObjectType> networkLinkControlObjectExtensionGroup;

    /**
     * Ruft den Wert der minRefreshPeriod-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMinRefreshPeriod() {
        return minRefreshPeriod;
    }

    /**
     * Legt den Wert der minRefreshPeriod-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMinRefreshPeriod(Double value) {
        this.minRefreshPeriod = value;
    }

    public boolean isSetMinRefreshPeriod() {
        return (this.minRefreshPeriod!= null);
    }

    /**
     * Ruft den Wert der maxSessionLength-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMaxSessionLength() {
        return maxSessionLength;
    }

    /**
     * Legt den Wert der maxSessionLength-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMaxSessionLength(Double value) {
        this.maxSessionLength = value;
    }

    public boolean isSetMaxSessionLength() {
        return (this.maxSessionLength!= null);
    }

    /**
     * Ruft den Wert der cookie-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCookie() {
        return cookie;
    }

    /**
     * Legt den Wert der cookie-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCookie(String value) {
        this.cookie = value;
    }

    public boolean isSetCookie() {
        return (this.cookie!= null);
    }

    /**
     * Ruft den Wert der message-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Legt den Wert der message-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    public boolean isSetMessage() {
        return (this.message!= null);
    }

    /**
     * Ruft den Wert der linkName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkName() {
        return linkName;
    }

    /**
     * Legt den Wert der linkName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkName(String value) {
        this.linkName = value;
    }

    public boolean isSetLinkName() {
        return (this.linkName!= null);
    }

    /**
     * Ruft den Wert der linkDescription-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkDescription() {
        return linkDescription;
    }

    /**
     * Legt den Wert der linkDescription-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkDescription(String value) {
        this.linkDescription = value;
    }

    public boolean isSetLinkDescription() {
        return (this.linkDescription!= null);
    }

    /**
     * Ruft den Wert der linkSnippet-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SnippetType }
     *     
     */
    public SnippetType getLinkSnippet() {
        return linkSnippet;
    }

    /**
     * Legt den Wert der linkSnippet-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SnippetType }
     *     
     */
    public void setLinkSnippet(SnippetType value) {
        this.linkSnippet = value;
    }

    public boolean isSetLinkSnippet() {
        return (this.linkSnippet!= null);
    }

    /**
     * Ruft den Wert der expires-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpires() {
        return expires;
    }

    /**
     * Legt den Wert der expires-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpires(String value) {
        this.expires = value;
    }

    public boolean isSetExpires() {
        return (this.expires!= null);
    }

    /**
     * Ruft den Wert der update-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UpdateType }
     *     
     */
    public UpdateType getUpdate() {
        return update;
    }

    /**
     * Legt den Wert der update-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UpdateType }
     *     
     */
    public void setUpdate(UpdateType value) {
        this.update = value;
    }

    public boolean isSetUpdate() {
        return (this.update!= null);
    }

    /**
     * Ruft den Wert der abstractViewGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CameraType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
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
     *     {@link JAXBElement }{@code <}{@link CameraType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
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
     * Gets the value of the networkLinkControlSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the networkLinkControlSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNetworkLinkControlSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getNetworkLinkControlSimpleExtensionGroup() {
        if (networkLinkControlSimpleExtensionGroup == null) {
            networkLinkControlSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.networkLinkControlSimpleExtensionGroup;
    }

    public boolean isSetNetworkLinkControlSimpleExtensionGroup() {
        return ((this.networkLinkControlSimpleExtensionGroup!= null)&&(!this.networkLinkControlSimpleExtensionGroup.isEmpty()));
    }

    public void unsetNetworkLinkControlSimpleExtensionGroup() {
        this.networkLinkControlSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the networkLinkControlObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the networkLinkControlObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNetworkLinkControlObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getNetworkLinkControlObjectExtensionGroup() {
        if (networkLinkControlObjectExtensionGroup == null) {
            networkLinkControlObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.networkLinkControlObjectExtensionGroup;
    }

    public boolean isSetNetworkLinkControlObjectExtensionGroup() {
        return ((this.networkLinkControlObjectExtensionGroup!= null)&&(!this.networkLinkControlObjectExtensionGroup.isEmpty()));
    }

    public void unsetNetworkLinkControlObjectExtensionGroup() {
        this.networkLinkControlObjectExtensionGroup = null;
    }

    public void setNetworkLinkControlSimpleExtensionGroup(List<Object> value) {
        this.networkLinkControlSimpleExtensionGroup = value;
    }

    public void setNetworkLinkControlObjectExtensionGroup(List<AbstractObjectType> value) {
        this.networkLinkControlObjectExtensionGroup = value;
    }

}
