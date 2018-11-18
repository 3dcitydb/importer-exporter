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
 * <p>Java-Klasse für ScreenOverlayType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ScreenOverlayType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractOverlayType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}overlayXY" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}screenXY" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}rotationXY" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}size" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}rotation" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ScreenOverlaySimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ScreenOverlayObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScreenOverlayType", propOrder = {
    "overlayXY",
    "screenXY",
    "rotationXY",
    "size",
    "rotation",
    "screenOverlaySimpleExtensionGroup",
    "screenOverlayObjectExtensionGroup"
})
public class ScreenOverlayType
    extends AbstractOverlayType
{

    protected Vec2Type overlayXY;
    protected Vec2Type screenXY;
    protected Vec2Type rotationXY;
    protected Vec2Type size;
    @XmlElement(defaultValue = "0.0")
    protected Double rotation;
    @XmlElement(name = "ScreenOverlaySimpleExtensionGroup")
    protected List<Object> screenOverlaySimpleExtensionGroup;
    @XmlElement(name = "ScreenOverlayObjectExtensionGroup")
    protected List<AbstractObjectType> screenOverlayObjectExtensionGroup;

    /**
     * Ruft den Wert der overlayXY-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Vec2Type }
     *     
     */
    public Vec2Type getOverlayXY() {
        return overlayXY;
    }

    /**
     * Legt den Wert der overlayXY-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Vec2Type }
     *     
     */
    public void setOverlayXY(Vec2Type value) {
        this.overlayXY = value;
    }

    public boolean isSetOverlayXY() {
        return (this.overlayXY!= null);
    }

    /**
     * Ruft den Wert der screenXY-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Vec2Type }
     *     
     */
    public Vec2Type getScreenXY() {
        return screenXY;
    }

    /**
     * Legt den Wert der screenXY-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Vec2Type }
     *     
     */
    public void setScreenXY(Vec2Type value) {
        this.screenXY = value;
    }

    public boolean isSetScreenXY() {
        return (this.screenXY!= null);
    }

    /**
     * Ruft den Wert der rotationXY-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Vec2Type }
     *     
     */
    public Vec2Type getRotationXY() {
        return rotationXY;
    }

    /**
     * Legt den Wert der rotationXY-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Vec2Type }
     *     
     */
    public void setRotationXY(Vec2Type value) {
        this.rotationXY = value;
    }

    public boolean isSetRotationXY() {
        return (this.rotationXY!= null);
    }

    /**
     * Ruft den Wert der size-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Vec2Type }
     *     
     */
    public Vec2Type getSize() {
        return size;
    }

    /**
     * Legt den Wert der size-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Vec2Type }
     *     
     */
    public void setSize(Vec2Type value) {
        this.size = value;
    }

    public boolean isSetSize() {
        return (this.size!= null);
    }

    /**
     * Ruft den Wert der rotation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRotation() {
        return rotation;
    }

    /**
     * Legt den Wert der rotation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRotation(Double value) {
        this.rotation = value;
    }

    public boolean isSetRotation() {
        return (this.rotation!= null);
    }

    /**
     * Gets the value of the screenOverlaySimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the screenOverlaySimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScreenOverlaySimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getScreenOverlaySimpleExtensionGroup() {
        if (screenOverlaySimpleExtensionGroup == null) {
            screenOverlaySimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.screenOverlaySimpleExtensionGroup;
    }

    public boolean isSetScreenOverlaySimpleExtensionGroup() {
        return ((this.screenOverlaySimpleExtensionGroup!= null)&&(!this.screenOverlaySimpleExtensionGroup.isEmpty()));
    }

    public void unsetScreenOverlaySimpleExtensionGroup() {
        this.screenOverlaySimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the screenOverlayObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the screenOverlayObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScreenOverlayObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getScreenOverlayObjectExtensionGroup() {
        if (screenOverlayObjectExtensionGroup == null) {
            screenOverlayObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.screenOverlayObjectExtensionGroup;
    }

    public boolean isSetScreenOverlayObjectExtensionGroup() {
        return ((this.screenOverlayObjectExtensionGroup!= null)&&(!this.screenOverlayObjectExtensionGroup.isEmpty()));
    }

    public void unsetScreenOverlayObjectExtensionGroup() {
        this.screenOverlayObjectExtensionGroup = null;
    }

    public void setScreenOverlaySimpleExtensionGroup(List<Object> value) {
        this.screenOverlaySimpleExtensionGroup = value;
    }

    public void setScreenOverlayObjectExtensionGroup(List<AbstractObjectType> value) {
        this.screenOverlayObjectExtensionGroup = value;
    }

}
