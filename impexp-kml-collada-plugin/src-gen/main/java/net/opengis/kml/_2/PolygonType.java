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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für PolygonType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PolygonType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractGeometryType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}extrude" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}tessellate" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitudeModeGroup" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}outerBoundaryIs" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}innerBoundaryIs" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PolygonSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PolygonObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolygonType", propOrder = {
    "extrude",
    "tessellate",
    "altitudeModeGroup",
    "outerBoundaryIs",
    "innerBoundaryIs",
    "polygonSimpleExtensionGroup",
    "polygonObjectExtensionGroup"
})
public class PolygonType
    extends AbstractGeometryType
{

    @XmlElement(defaultValue = "0")
    protected Boolean extrude;
    @XmlElement(defaultValue = "0")
    protected Boolean tessellate;
    @XmlElementRef(name = "altitudeModeGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<?> altitudeModeGroup;
    protected BoundaryType outerBoundaryIs;
    protected List<BoundaryType> innerBoundaryIs;
    @XmlElement(name = "PolygonSimpleExtensionGroup")
    protected List<Object> polygonSimpleExtensionGroup;
    @XmlElement(name = "PolygonObjectExtensionGroup")
    protected List<AbstractObjectType> polygonObjectExtensionGroup;

    /**
     * Ruft den Wert der extrude-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isExtrude() {
        return extrude;
    }

    /**
     * Legt den Wert der extrude-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExtrude(Boolean value) {
        this.extrude = value;
    }

    public boolean isSetExtrude() {
        return (this.extrude!= null);
    }

    /**
     * Ruft den Wert der tessellate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isTessellate() {
        return tessellate;
    }

    /**
     * Legt den Wert der tessellate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTessellate(Boolean value) {
        this.tessellate = value;
    }

    public boolean isSetTessellate() {
        return (this.tessellate!= null);
    }

    /**
     * Ruft den Wert der altitudeModeGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    public JAXBElement<?> getAltitudeModeGroup() {
        return altitudeModeGroup;
    }

    /**
     * Legt den Wert der altitudeModeGroup-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    public void setAltitudeModeGroup(JAXBElement<?> value) {
        this.altitudeModeGroup = value;
    }

    public boolean isSetAltitudeModeGroup() {
        return (this.altitudeModeGroup!= null);
    }

    /**
     * Ruft den Wert der outerBoundaryIs-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BoundaryType }
     *     
     */
    public BoundaryType getOuterBoundaryIs() {
        return outerBoundaryIs;
    }

    /**
     * Legt den Wert der outerBoundaryIs-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BoundaryType }
     *     
     */
    public void setOuterBoundaryIs(BoundaryType value) {
        this.outerBoundaryIs = value;
    }

    public boolean isSetOuterBoundaryIs() {
        return (this.outerBoundaryIs!= null);
    }

    /**
     * Gets the value of the innerBoundaryIs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the innerBoundaryIs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInnerBoundaryIs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BoundaryType }
     * 
     * 
     */
    public List<BoundaryType> getInnerBoundaryIs() {
        if (innerBoundaryIs == null) {
            innerBoundaryIs = new ArrayList<BoundaryType>();
        }
        return this.innerBoundaryIs;
    }

    public boolean isSetInnerBoundaryIs() {
        return ((this.innerBoundaryIs!= null)&&(!this.innerBoundaryIs.isEmpty()));
    }

    public void unsetInnerBoundaryIs() {
        this.innerBoundaryIs = null;
    }

    /**
     * Gets the value of the polygonSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the polygonSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPolygonSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getPolygonSimpleExtensionGroup() {
        if (polygonSimpleExtensionGroup == null) {
            polygonSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.polygonSimpleExtensionGroup;
    }

    public boolean isSetPolygonSimpleExtensionGroup() {
        return ((this.polygonSimpleExtensionGroup!= null)&&(!this.polygonSimpleExtensionGroup.isEmpty()));
    }

    public void unsetPolygonSimpleExtensionGroup() {
        this.polygonSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the polygonObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the polygonObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPolygonObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getPolygonObjectExtensionGroup() {
        if (polygonObjectExtensionGroup == null) {
            polygonObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.polygonObjectExtensionGroup;
    }

    public boolean isSetPolygonObjectExtensionGroup() {
        return ((this.polygonObjectExtensionGroup!= null)&&(!this.polygonObjectExtensionGroup.isEmpty()));
    }

    public void unsetPolygonObjectExtensionGroup() {
        this.polygonObjectExtensionGroup = null;
    }

    public void setInnerBoundaryIs(List<BoundaryType> value) {
        this.innerBoundaryIs = value;
    }

    public void setPolygonSimpleExtensionGroup(List<Object> value) {
        this.polygonSimpleExtensionGroup = value;
    }

    public void setPolygonObjectExtensionGroup(List<AbstractObjectType> value) {
        this.polygonObjectExtensionGroup = value;
    }

}
