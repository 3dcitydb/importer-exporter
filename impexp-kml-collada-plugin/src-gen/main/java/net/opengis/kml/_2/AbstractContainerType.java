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
 * <p>Java-Klasse für AbstractContainerType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractContainerType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractFeatureType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractContainerSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractContainerObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractContainerType", propOrder = {
    "abstractContainerSimpleExtensionGroup",
    "abstractContainerObjectExtensionGroup"
})
@XmlSeeAlso({
    DocumentType.class,
    FolderType.class
})
public abstract class AbstractContainerType
    extends AbstractFeatureType
{

    @XmlElement(name = "AbstractContainerSimpleExtensionGroup")
    protected List<Object> abstractContainerSimpleExtensionGroup;
    @XmlElement(name = "AbstractContainerObjectExtensionGroup")
    protected List<AbstractObjectType> abstractContainerObjectExtensionGroup;

    /**
     * Gets the value of the abstractContainerSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractContainerSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractContainerSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAbstractContainerSimpleExtensionGroup() {
        if (abstractContainerSimpleExtensionGroup == null) {
            abstractContainerSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.abstractContainerSimpleExtensionGroup;
    }

    public boolean isSetAbstractContainerSimpleExtensionGroup() {
        return ((this.abstractContainerSimpleExtensionGroup!= null)&&(!this.abstractContainerSimpleExtensionGroup.isEmpty()));
    }

    public void unsetAbstractContainerSimpleExtensionGroup() {
        this.abstractContainerSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the abstractContainerObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractContainerObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractContainerObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getAbstractContainerObjectExtensionGroup() {
        if (abstractContainerObjectExtensionGroup == null) {
            abstractContainerObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.abstractContainerObjectExtensionGroup;
    }

    public boolean isSetAbstractContainerObjectExtensionGroup() {
        return ((this.abstractContainerObjectExtensionGroup!= null)&&(!this.abstractContainerObjectExtensionGroup.isEmpty()));
    }

    public void unsetAbstractContainerObjectExtensionGroup() {
        this.abstractContainerObjectExtensionGroup = null;
    }

    public void setAbstractContainerSimpleExtensionGroup(List<Object> value) {
        this.abstractContainerSimpleExtensionGroup = value;
    }

    public void setAbstractContainerObjectExtensionGroup(List<AbstractObjectType> value) {
        this.abstractContainerObjectExtensionGroup = value;
    }

}
