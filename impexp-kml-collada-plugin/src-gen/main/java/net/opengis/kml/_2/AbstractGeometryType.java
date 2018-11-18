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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für AbstractGeometryType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractGeometryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractGeometrySimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractGeometryObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractGeometryType", propOrder = {
    "abstractGeometrySimpleExtensionGroup",
    "abstractGeometryObjectExtensionGroup"
})
@XmlSeeAlso({
    MultiGeometryType.class,
    PointType.class,
    LineStringType.class,
    LinearRingType.class,
    PolygonType.class,
    ModelType.class
})
public abstract class AbstractGeometryType
    extends AbstractObjectType
{

    @XmlElement(name = "AbstractGeometrySimpleExtensionGroup")
    protected List<Object> abstractGeometrySimpleExtensionGroup;
    @XmlElement(name = "AbstractGeometryObjectExtensionGroup")
    protected List<AbstractObjectType> abstractGeometryObjectExtensionGroup;

    /**
     * Gets the value of the abstractGeometrySimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractGeometrySimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractGeometrySimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAbstractGeometrySimpleExtensionGroup() {
        if (abstractGeometrySimpleExtensionGroup == null) {
            abstractGeometrySimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.abstractGeometrySimpleExtensionGroup;
    }

    public boolean isSetAbstractGeometrySimpleExtensionGroup() {
        return ((this.abstractGeometrySimpleExtensionGroup!= null)&&(!this.abstractGeometrySimpleExtensionGroup.isEmpty()));
    }

    public void unsetAbstractGeometrySimpleExtensionGroup() {
        this.abstractGeometrySimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the abstractGeometryObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractGeometryObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractGeometryObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getAbstractGeometryObjectExtensionGroup() {
        if (abstractGeometryObjectExtensionGroup == null) {
            abstractGeometryObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.abstractGeometryObjectExtensionGroup;
    }

    public boolean isSetAbstractGeometryObjectExtensionGroup() {
        return ((this.abstractGeometryObjectExtensionGroup!= null)&&(!this.abstractGeometryObjectExtensionGroup.isEmpty()));
    }

    public void unsetAbstractGeometryObjectExtensionGroup() {
        this.abstractGeometryObjectExtensionGroup = null;
    }

    public void setAbstractGeometrySimpleExtensionGroup(List<Object> value) {
        this.abstractGeometrySimpleExtensionGroup = value;
    }

    public void setAbstractGeometryObjectExtensionGroup(List<AbstractObjectType> value) {
        this.abstractGeometryObjectExtensionGroup = value;
    }

}
