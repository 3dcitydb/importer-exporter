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
 * <p>Java-Klasse für LinkType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="LinkType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}BasicLinkType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}refreshMode" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}refreshInterval" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}viewRefreshMode" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}viewRefreshTime" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}viewBoundScale" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}viewFormat" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}httpQuery" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LinkSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LinkObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LinkType", propOrder = {
    "refreshMode",
    "refreshInterval",
    "viewRefreshMode",
    "viewRefreshTime",
    "viewBoundScale",
    "viewFormat",
    "httpQuery",
    "linkSimpleExtensionGroup",
    "linkObjectExtensionGroup"
})
public class LinkType
    extends BasicLinkType
{

    @XmlElement(defaultValue = "onChange")
    protected RefreshModeEnumType refreshMode;
    @XmlElement(defaultValue = "4.0")
    protected Double refreshInterval;
    @XmlElement(defaultValue = "never")
    protected ViewRefreshModeEnumType viewRefreshMode;
    @XmlElement(defaultValue = "4.0")
    protected Double viewRefreshTime;
    @XmlElement(defaultValue = "1.0")
    protected Double viewBoundScale;
    protected String viewFormat;
    protected String httpQuery;
    @XmlElement(name = "LinkSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> linkSimpleExtensionGroup;
    @XmlElement(name = "LinkObjectExtensionGroup")
    protected List<AbstractObjectType> linkObjectExtensionGroup;

    /**
     * Ruft den Wert der refreshMode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RefreshModeEnumType }
     *     
     */
    public RefreshModeEnumType getRefreshMode() {
        return refreshMode;
    }

    /**
     * Legt den Wert der refreshMode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RefreshModeEnumType }
     *     
     */
    public void setRefreshMode(RefreshModeEnumType value) {
        this.refreshMode = value;
    }

    public boolean isSetRefreshMode() {
        return (this.refreshMode!= null);
    }

    /**
     * Ruft den Wert der refreshInterval-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Legt den Wert der refreshInterval-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRefreshInterval(Double value) {
        this.refreshInterval = value;
    }

    public boolean isSetRefreshInterval() {
        return (this.refreshInterval!= null);
    }

    /**
     * Ruft den Wert der viewRefreshMode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ViewRefreshModeEnumType }
     *     
     */
    public ViewRefreshModeEnumType getViewRefreshMode() {
        return viewRefreshMode;
    }

    /**
     * Legt den Wert der viewRefreshMode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ViewRefreshModeEnumType }
     *     
     */
    public void setViewRefreshMode(ViewRefreshModeEnumType value) {
        this.viewRefreshMode = value;
    }

    public boolean isSetViewRefreshMode() {
        return (this.viewRefreshMode!= null);
    }

    /**
     * Ruft den Wert der viewRefreshTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getViewRefreshTime() {
        return viewRefreshTime;
    }

    /**
     * Legt den Wert der viewRefreshTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setViewRefreshTime(Double value) {
        this.viewRefreshTime = value;
    }

    public boolean isSetViewRefreshTime() {
        return (this.viewRefreshTime!= null);
    }

    /**
     * Ruft den Wert der viewBoundScale-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getViewBoundScale() {
        return viewBoundScale;
    }

    /**
     * Legt den Wert der viewBoundScale-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setViewBoundScale(Double value) {
        this.viewBoundScale = value;
    }

    public boolean isSetViewBoundScale() {
        return (this.viewBoundScale!= null);
    }

    /**
     * Ruft den Wert der viewFormat-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getViewFormat() {
        return viewFormat;
    }

    /**
     * Legt den Wert der viewFormat-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setViewFormat(String value) {
        this.viewFormat = value;
    }

    public boolean isSetViewFormat() {
        return (this.viewFormat!= null);
    }

    /**
     * Ruft den Wert der httpQuery-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHttpQuery() {
        return httpQuery;
    }

    /**
     * Legt den Wert der httpQuery-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHttpQuery(String value) {
        this.httpQuery = value;
    }

    public boolean isSetHttpQuery() {
        return (this.httpQuery!= null);
    }

    /**
     * Gets the value of the linkSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getLinkSimpleExtensionGroup() {
        if (linkSimpleExtensionGroup == null) {
            linkSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.linkSimpleExtensionGroup;
    }

    public boolean isSetLinkSimpleExtensionGroup() {
        return ((this.linkSimpleExtensionGroup!= null)&&(!this.linkSimpleExtensionGroup.isEmpty()));
    }

    public void unsetLinkSimpleExtensionGroup() {
        this.linkSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the linkObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getLinkObjectExtensionGroup() {
        if (linkObjectExtensionGroup == null) {
            linkObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.linkObjectExtensionGroup;
    }

    public boolean isSetLinkObjectExtensionGroup() {
        return ((this.linkObjectExtensionGroup!= null)&&(!this.linkObjectExtensionGroup.isEmpty()));
    }

    public void unsetLinkObjectExtensionGroup() {
        this.linkObjectExtensionGroup = null;
    }

    public void setLinkSimpleExtensionGroup(List<Object> value) {
        this.linkSimpleExtensionGroup = value;
    }

    public void setLinkObjectExtensionGroup(List<AbstractObjectType> value) {
        this.linkObjectExtensionGroup = value;
    }

}
