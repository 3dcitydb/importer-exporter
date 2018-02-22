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
 * <p>Java-Klasse für PhotoOverlayType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PhotoOverlayType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractOverlayType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}rotation" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ViewVolume" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ImagePyramid" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Point" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}shape" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PhotoOverlaySimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PhotoOverlayObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PhotoOverlayType", propOrder = {
    "rotation",
    "viewVolume",
    "imagePyramid",
    "point",
    "shape",
    "photoOverlaySimpleExtensionGroup",
    "photoOverlayObjectExtensionGroup"
})
public class PhotoOverlayType
    extends AbstractOverlayType
{

    @XmlElement(defaultValue = "0.0")
    protected Double rotation;
    @XmlElement(name = "ViewVolume")
    protected ViewVolumeType viewVolume;
    @XmlElement(name = "ImagePyramid")
    protected ImagePyramidType imagePyramid;
    @XmlElement(name = "Point")
    protected PointType point;
    @XmlElement(defaultValue = "rectangle")
    protected ShapeEnumType shape;
    @XmlElement(name = "PhotoOverlaySimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> photoOverlaySimpleExtensionGroup;
    @XmlElement(name = "PhotoOverlayObjectExtensionGroup")
    protected List<AbstractObjectType> photoOverlayObjectExtensionGroup;

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
     * Ruft den Wert der viewVolume-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ViewVolumeType }
     *     
     */
    public ViewVolumeType getViewVolume() {
        return viewVolume;
    }

    /**
     * Legt den Wert der viewVolume-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ViewVolumeType }
     *     
     */
    public void setViewVolume(ViewVolumeType value) {
        this.viewVolume = value;
    }

    public boolean isSetViewVolume() {
        return (this.viewVolume!= null);
    }

    /**
     * Ruft den Wert der imagePyramid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ImagePyramidType }
     *     
     */
    public ImagePyramidType getImagePyramid() {
        return imagePyramid;
    }

    /**
     * Legt den Wert der imagePyramid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ImagePyramidType }
     *     
     */
    public void setImagePyramid(ImagePyramidType value) {
        this.imagePyramid = value;
    }

    public boolean isSetImagePyramid() {
        return (this.imagePyramid!= null);
    }

    /**
     * Ruft den Wert der point-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PointType }
     *     
     */
    public PointType getPoint() {
        return point;
    }

    /**
     * Legt den Wert der point-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PointType }
     *     
     */
    public void setPoint(PointType value) {
        this.point = value;
    }

    public boolean isSetPoint() {
        return (this.point!= null);
    }

    /**
     * Ruft den Wert der shape-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ShapeEnumType }
     *     
     */
    public ShapeEnumType getShape() {
        return shape;
    }

    /**
     * Legt den Wert der shape-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ShapeEnumType }
     *     
     */
    public void setShape(ShapeEnumType value) {
        this.shape = value;
    }

    public boolean isSetShape() {
        return (this.shape!= null);
    }

    /**
     * Gets the value of the photoOverlaySimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the photoOverlaySimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPhotoOverlaySimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getPhotoOverlaySimpleExtensionGroup() {
        if (photoOverlaySimpleExtensionGroup == null) {
            photoOverlaySimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.photoOverlaySimpleExtensionGroup;
    }

    public boolean isSetPhotoOverlaySimpleExtensionGroup() {
        return ((this.photoOverlaySimpleExtensionGroup!= null)&&(!this.photoOverlaySimpleExtensionGroup.isEmpty()));
    }

    public void unsetPhotoOverlaySimpleExtensionGroup() {
        this.photoOverlaySimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the photoOverlayObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the photoOverlayObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPhotoOverlayObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getPhotoOverlayObjectExtensionGroup() {
        if (photoOverlayObjectExtensionGroup == null) {
            photoOverlayObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.photoOverlayObjectExtensionGroup;
    }

    public boolean isSetPhotoOverlayObjectExtensionGroup() {
        return ((this.photoOverlayObjectExtensionGroup!= null)&&(!this.photoOverlayObjectExtensionGroup.isEmpty()));
    }

    public void unsetPhotoOverlayObjectExtensionGroup() {
        this.photoOverlayObjectExtensionGroup = null;
    }

    public void setPhotoOverlaySimpleExtensionGroup(List<Object> value) {
        this.photoOverlaySimpleExtensionGroup = value;
    }

    public void setPhotoOverlayObjectExtensionGroup(List<AbstractObjectType> value) {
        this.photoOverlayObjectExtensionGroup = value;
    }

}
