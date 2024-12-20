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
 * <p>Java-Klasse für AbstractViewType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractViewType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractViewSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractViewObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractViewType", propOrder = {
    "abstractViewSimpleExtensionGroup",
    "abstractViewObjectExtensionGroup"
})
@XmlSeeAlso({
    LookAtType.class,
    CameraType.class
})
public abstract class AbstractViewType
    extends AbstractObjectType
{

    @XmlElement(name = "AbstractViewSimpleExtensionGroup")
    protected List<Object> abstractViewSimpleExtensionGroup;
    @XmlElement(name = "AbstractViewObjectExtensionGroup")
    protected List<AbstractObjectType> abstractViewObjectExtensionGroup;

    /**
     * Gets the value of the abstractViewSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractViewSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractViewSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAbstractViewSimpleExtensionGroup() {
        if (abstractViewSimpleExtensionGroup == null) {
            abstractViewSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.abstractViewSimpleExtensionGroup;
    }

    public boolean isSetAbstractViewSimpleExtensionGroup() {
        return ((this.abstractViewSimpleExtensionGroup!= null)&&(!this.abstractViewSimpleExtensionGroup.isEmpty()));
    }

    public void unsetAbstractViewSimpleExtensionGroup() {
        this.abstractViewSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the abstractViewObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractViewObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractViewObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getAbstractViewObjectExtensionGroup() {
        if (abstractViewObjectExtensionGroup == null) {
            abstractViewObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.abstractViewObjectExtensionGroup;
    }

    public boolean isSetAbstractViewObjectExtensionGroup() {
        return ((this.abstractViewObjectExtensionGroup!= null)&&(!this.abstractViewObjectExtensionGroup.isEmpty()));
    }

    public void unsetAbstractViewObjectExtensionGroup() {
        this.abstractViewObjectExtensionGroup = null;
    }

    public void setAbstractViewSimpleExtensionGroup(List<Object> value) {
        this.abstractViewSimpleExtensionGroup = value;
    }

    public void setAbstractViewObjectExtensionGroup(List<AbstractObjectType> value) {
        this.abstractViewObjectExtensionGroup = value;
    }

}
