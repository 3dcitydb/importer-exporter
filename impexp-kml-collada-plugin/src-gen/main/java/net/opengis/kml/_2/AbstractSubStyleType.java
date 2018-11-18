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
 * <p>Java-Klasse für AbstractSubStyleType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractSubStyleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractSubStyleSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractSubStyleObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractSubStyleType", propOrder = {
    "abstractSubStyleSimpleExtensionGroup",
    "abstractSubStyleObjectExtensionGroup"
})
@XmlSeeAlso({
    AbstractColorStyleType.class,
    BalloonStyleType.class,
    ListStyleType.class
})
public abstract class AbstractSubStyleType
    extends AbstractObjectType
{

    @XmlElement(name = "AbstractSubStyleSimpleExtensionGroup")
    protected List<Object> abstractSubStyleSimpleExtensionGroup;
    @XmlElement(name = "AbstractSubStyleObjectExtensionGroup")
    protected List<AbstractObjectType> abstractSubStyleObjectExtensionGroup;

    /**
     * Gets the value of the abstractSubStyleSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractSubStyleSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractSubStyleSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAbstractSubStyleSimpleExtensionGroup() {
        if (abstractSubStyleSimpleExtensionGroup == null) {
            abstractSubStyleSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.abstractSubStyleSimpleExtensionGroup;
    }

    public boolean isSetAbstractSubStyleSimpleExtensionGroup() {
        return ((this.abstractSubStyleSimpleExtensionGroup!= null)&&(!this.abstractSubStyleSimpleExtensionGroup.isEmpty()));
    }

    public void unsetAbstractSubStyleSimpleExtensionGroup() {
        this.abstractSubStyleSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the abstractSubStyleObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractSubStyleObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractSubStyleObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getAbstractSubStyleObjectExtensionGroup() {
        if (abstractSubStyleObjectExtensionGroup == null) {
            abstractSubStyleObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.abstractSubStyleObjectExtensionGroup;
    }

    public boolean isSetAbstractSubStyleObjectExtensionGroup() {
        return ((this.abstractSubStyleObjectExtensionGroup!= null)&&(!this.abstractSubStyleObjectExtensionGroup.isEmpty()));
    }

    public void unsetAbstractSubStyleObjectExtensionGroup() {
        this.abstractSubStyleObjectExtensionGroup = null;
    }

    public void setAbstractSubStyleSimpleExtensionGroup(List<Object> value) {
        this.abstractSubStyleSimpleExtensionGroup = value;
    }

    public void setAbstractSubStyleObjectExtensionGroup(List<AbstractObjectType> value) {
        this.abstractSubStyleObjectExtensionGroup = value;
    }

}
