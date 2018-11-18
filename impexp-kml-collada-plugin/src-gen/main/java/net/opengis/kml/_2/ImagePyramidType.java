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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ImagePyramidType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ImagePyramidType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}tileSize" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}maxWidth" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}maxHeight" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}gridOrigin" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ImagePyramidSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ImagePyramidObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ImagePyramidType", propOrder = {
    "tileSize",
    "maxWidth",
    "maxHeight",
    "gridOrigin",
    "imagePyramidSimpleExtensionGroup",
    "imagePyramidObjectExtensionGroup"
})
public class ImagePyramidType
    extends AbstractObjectType
{

    @XmlElement(defaultValue = "256")
    protected Integer tileSize;
    @XmlElement(defaultValue = "0")
    protected Integer maxWidth;
    @XmlElement(defaultValue = "0")
    protected Integer maxHeight;
    @XmlElement(defaultValue = "lowerLeft")
    @XmlSchemaType(name = "string")
    protected GridOriginEnumType gridOrigin;
    @XmlElement(name = "ImagePyramidSimpleExtensionGroup")
    protected List<Object> imagePyramidSimpleExtensionGroup;
    @XmlElement(name = "ImagePyramidObjectExtensionGroup")
    protected List<AbstractObjectType> imagePyramidObjectExtensionGroup;

    /**
     * Ruft den Wert der tileSize-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTileSize() {
        return tileSize;
    }

    /**
     * Legt den Wert der tileSize-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTileSize(Integer value) {
        this.tileSize = value;
    }

    public boolean isSetTileSize() {
        return (this.tileSize!= null);
    }

    /**
     * Ruft den Wert der maxWidth-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxWidth() {
        return maxWidth;
    }

    /**
     * Legt den Wert der maxWidth-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxWidth(Integer value) {
        this.maxWidth = value;
    }

    public boolean isSetMaxWidth() {
        return (this.maxWidth!= null);
    }

    /**
     * Ruft den Wert der maxHeight-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxHeight() {
        return maxHeight;
    }

    /**
     * Legt den Wert der maxHeight-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxHeight(Integer value) {
        this.maxHeight = value;
    }

    public boolean isSetMaxHeight() {
        return (this.maxHeight!= null);
    }

    /**
     * Ruft den Wert der gridOrigin-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GridOriginEnumType }
     *     
     */
    public GridOriginEnumType getGridOrigin() {
        return gridOrigin;
    }

    /**
     * Legt den Wert der gridOrigin-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GridOriginEnumType }
     *     
     */
    public void setGridOrigin(GridOriginEnumType value) {
        this.gridOrigin = value;
    }

    public boolean isSetGridOrigin() {
        return (this.gridOrigin!= null);
    }

    /**
     * Gets the value of the imagePyramidSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the imagePyramidSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImagePyramidSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getImagePyramidSimpleExtensionGroup() {
        if (imagePyramidSimpleExtensionGroup == null) {
            imagePyramidSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.imagePyramidSimpleExtensionGroup;
    }

    public boolean isSetImagePyramidSimpleExtensionGroup() {
        return ((this.imagePyramidSimpleExtensionGroup!= null)&&(!this.imagePyramidSimpleExtensionGroup.isEmpty()));
    }

    public void unsetImagePyramidSimpleExtensionGroup() {
        this.imagePyramidSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the imagePyramidObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the imagePyramidObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImagePyramidObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getImagePyramidObjectExtensionGroup() {
        if (imagePyramidObjectExtensionGroup == null) {
            imagePyramidObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.imagePyramidObjectExtensionGroup;
    }

    public boolean isSetImagePyramidObjectExtensionGroup() {
        return ((this.imagePyramidObjectExtensionGroup!= null)&&(!this.imagePyramidObjectExtensionGroup.isEmpty()));
    }

    public void unsetImagePyramidObjectExtensionGroup() {
        this.imagePyramidObjectExtensionGroup = null;
    }

    public void setImagePyramidSimpleExtensionGroup(List<Object> value) {
        this.imagePyramidSimpleExtensionGroup = value;
    }

    public void setImagePyramidObjectExtensionGroup(List<AbstractObjectType> value) {
        this.imagePyramidObjectExtensionGroup = value;
    }

}
