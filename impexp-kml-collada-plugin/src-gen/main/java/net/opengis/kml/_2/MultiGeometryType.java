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
 * <p>Java-Klasse für MultiGeometryType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="MultiGeometryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractGeometryType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractGeometryGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}MultiGeometrySimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}MultiGeometryObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultiGeometryType", propOrder = {
    "abstractGeometryGroup",
    "multiGeometrySimpleExtensionGroup",
    "multiGeometryObjectExtensionGroup"
})
public class MultiGeometryType
    extends AbstractGeometryType
{

    @XmlElementRef(name = "AbstractGeometryGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends AbstractGeometryType>> abstractGeometryGroup;
    @XmlElement(name = "MultiGeometrySimpleExtensionGroup")
    protected List<Object> multiGeometrySimpleExtensionGroup;
    @XmlElement(name = "MultiGeometryObjectExtensionGroup")
    protected List<AbstractObjectType> multiGeometryObjectExtensionGroup;

    /**
     * Gets the value of the abstractGeometryGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractGeometryGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractGeometryGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link ModelType }{@code >}
     * {@link JAXBElement }{@code <}{@link PolygonType }{@code >}
     * {@link JAXBElement }{@code <}{@link PointType }{@code >}
     * {@link JAXBElement }{@code <}{@link LineStringType }{@code >}
     * {@link JAXBElement }{@code <}{@link LinearRingType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiGeometryType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractGeometryType>> getAbstractGeometryGroup() {
        if (abstractGeometryGroup == null) {
            abstractGeometryGroup = new ArrayList<JAXBElement<? extends AbstractGeometryType>>();
        }
        return this.abstractGeometryGroup;
    }

    public boolean isSetAbstractGeometryGroup() {
        return ((this.abstractGeometryGroup!= null)&&(!this.abstractGeometryGroup.isEmpty()));
    }

    public void unsetAbstractGeometryGroup() {
        this.abstractGeometryGroup = null;
    }

    /**
     * Gets the value of the multiGeometrySimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the multiGeometrySimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMultiGeometrySimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getMultiGeometrySimpleExtensionGroup() {
        if (multiGeometrySimpleExtensionGroup == null) {
            multiGeometrySimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.multiGeometrySimpleExtensionGroup;
    }

    public boolean isSetMultiGeometrySimpleExtensionGroup() {
        return ((this.multiGeometrySimpleExtensionGroup!= null)&&(!this.multiGeometrySimpleExtensionGroup.isEmpty()));
    }

    public void unsetMultiGeometrySimpleExtensionGroup() {
        this.multiGeometrySimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the multiGeometryObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the multiGeometryObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMultiGeometryObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getMultiGeometryObjectExtensionGroup() {
        if (multiGeometryObjectExtensionGroup == null) {
            multiGeometryObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.multiGeometryObjectExtensionGroup;
    }

    public boolean isSetMultiGeometryObjectExtensionGroup() {
        return ((this.multiGeometryObjectExtensionGroup!= null)&&(!this.multiGeometryObjectExtensionGroup.isEmpty()));
    }

    public void unsetMultiGeometryObjectExtensionGroup() {
        this.multiGeometryObjectExtensionGroup = null;
    }

    public void setAbstractGeometryGroup(List<JAXBElement<? extends AbstractGeometryType>> value) {
        this.abstractGeometryGroup = value;
    }

    public void setMultiGeometrySimpleExtensionGroup(List<Object> value) {
        this.multiGeometrySimpleExtensionGroup = value;
    }

    public void setMultiGeometryObjectExtensionGroup(List<AbstractObjectType> value) {
        this.multiGeometryObjectExtensionGroup = value;
    }

}
