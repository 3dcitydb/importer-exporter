//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für StyleType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="StyleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractStyleSelectorType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}IconStyle" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LabelStyle" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LineStyle" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PolyStyle" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}BalloonStyle" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ListStyle" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}StyleSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}StyleObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StyleType", propOrder = {
    "iconStyle",
    "labelStyle",
    "lineStyle",
    "polyStyle",
    "balloonStyle",
    "listStyle",
    "styleSimpleExtensionGroup",
    "styleObjectExtensionGroup"
})
public class StyleType
    extends AbstractStyleSelectorType
{

    @XmlElement(name = "IconStyle")
    protected IconStyleType iconStyle;
    @XmlElement(name = "LabelStyle")
    protected LabelStyleType labelStyle;
    @XmlElement(name = "LineStyle")
    protected LineStyleType lineStyle;
    @XmlElement(name = "PolyStyle")
    protected PolyStyleType polyStyle;
    @XmlElement(name = "BalloonStyle")
    protected BalloonStyleType balloonStyle;
    @XmlElement(name = "ListStyle")
    protected ListStyleType listStyle;
    @XmlElement(name = "StyleSimpleExtensionGroup")
    protected List<Object> styleSimpleExtensionGroup;
    @XmlElement(name = "StyleObjectExtensionGroup")
    protected List<AbstractObjectType> styleObjectExtensionGroup;

    /**
     * Ruft den Wert der iconStyle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link IconStyleType }
     *     
     */
    public IconStyleType getIconStyle() {
        return iconStyle;
    }

    /**
     * Legt den Wert der iconStyle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link IconStyleType }
     *     
     */
    public void setIconStyle(IconStyleType value) {
        this.iconStyle = value;
    }

    public boolean isSetIconStyle() {
        return (this.iconStyle!= null);
    }

    /**
     * Ruft den Wert der labelStyle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LabelStyleType }
     *     
     */
    public LabelStyleType getLabelStyle() {
        return labelStyle;
    }

    /**
     * Legt den Wert der labelStyle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LabelStyleType }
     *     
     */
    public void setLabelStyle(LabelStyleType value) {
        this.labelStyle = value;
    }

    public boolean isSetLabelStyle() {
        return (this.labelStyle!= null);
    }

    /**
     * Ruft den Wert der lineStyle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LineStyleType }
     *     
     */
    public LineStyleType getLineStyle() {
        return lineStyle;
    }

    /**
     * Legt den Wert der lineStyle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LineStyleType }
     *     
     */
    public void setLineStyle(LineStyleType value) {
        this.lineStyle = value;
    }

    public boolean isSetLineStyle() {
        return (this.lineStyle!= null);
    }

    /**
     * Ruft den Wert der polyStyle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PolyStyleType }
     *     
     */
    public PolyStyleType getPolyStyle() {
        return polyStyle;
    }

    /**
     * Legt den Wert der polyStyle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PolyStyleType }
     *     
     */
    public void setPolyStyle(PolyStyleType value) {
        this.polyStyle = value;
    }

    public boolean isSetPolyStyle() {
        return (this.polyStyle!= null);
    }

    /**
     * Ruft den Wert der balloonStyle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BalloonStyleType }
     *     
     */
    public BalloonStyleType getBalloonStyle() {
        return balloonStyle;
    }

    /**
     * Legt den Wert der balloonStyle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BalloonStyleType }
     *     
     */
    public void setBalloonStyle(BalloonStyleType value) {
        this.balloonStyle = value;
    }

    public boolean isSetBalloonStyle() {
        return (this.balloonStyle!= null);
    }

    /**
     * Ruft den Wert der listStyle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ListStyleType }
     *     
     */
    public ListStyleType getListStyle() {
        return listStyle;
    }

    /**
     * Legt den Wert der listStyle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ListStyleType }
     *     
     */
    public void setListStyle(ListStyleType value) {
        this.listStyle = value;
    }

    public boolean isSetListStyle() {
        return (this.listStyle!= null);
    }

    /**
     * Gets the value of the styleSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the styleSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStyleSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getStyleSimpleExtensionGroup() {
        if (styleSimpleExtensionGroup == null) {
            styleSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.styleSimpleExtensionGroup;
    }

    public boolean isSetStyleSimpleExtensionGroup() {
        return ((this.styleSimpleExtensionGroup!= null)&&(!this.styleSimpleExtensionGroup.isEmpty()));
    }

    public void unsetStyleSimpleExtensionGroup() {
        this.styleSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the styleObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the styleObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStyleObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getStyleObjectExtensionGroup() {
        if (styleObjectExtensionGroup == null) {
            styleObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.styleObjectExtensionGroup;
    }

    public boolean isSetStyleObjectExtensionGroup() {
        return ((this.styleObjectExtensionGroup!= null)&&(!this.styleObjectExtensionGroup.isEmpty()));
    }

    public void unsetStyleObjectExtensionGroup() {
        this.styleObjectExtensionGroup = null;
    }

    public void setStyleSimpleExtensionGroup(List<Object> value) {
        this.styleSimpleExtensionGroup = value;
    }

    public void setStyleObjectExtensionGroup(List<AbstractObjectType> value) {
        this.styleObjectExtensionGroup = value;
    }

}
