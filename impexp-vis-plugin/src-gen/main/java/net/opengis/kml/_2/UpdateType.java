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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für UpdateType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="UpdateType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}targetHref"/&gt;
 *         &lt;choice maxOccurs="unbounded"&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Create"/&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Delete"/&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Change"/&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}UpdateOpExtensionGroup"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}UpdateExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateType", propOrder = {
    "targetHref",
    "createOrDeleteOrChange",
    "updateExtensionGroup"
})
public class UpdateType {

    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String targetHref;
    @XmlElements({
        @XmlElement(name = "Create", type = CreateType.class),
        @XmlElement(name = "Delete", type = DeleteType.class),
        @XmlElement(name = "Change", type = ChangeType.class),
        @XmlElement(name = "UpdateOpExtensionGroup")
    })
    protected List<Object> createOrDeleteOrChange;
    @XmlElement(name = "UpdateExtensionGroup")
    protected List<Object> updateExtensionGroup;

    /**
     * Ruft den Wert der targetHref-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetHref() {
        return targetHref;
    }

    /**
     * Legt den Wert der targetHref-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetHref(String value) {
        this.targetHref = value;
    }

    public boolean isSetTargetHref() {
        return (this.targetHref!= null);
    }

    /**
     * Gets the value of the createOrDeleteOrChange property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the createOrDeleteOrChange property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCreateOrDeleteOrChange().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CreateType }
     * {@link DeleteType }
     * {@link ChangeType }
     * {@link Object }
     * 
     * 
     */
    public List<Object> getCreateOrDeleteOrChange() {
        if (createOrDeleteOrChange == null) {
            createOrDeleteOrChange = new ArrayList<Object>();
        }
        return this.createOrDeleteOrChange;
    }

    public boolean isSetCreateOrDeleteOrChange() {
        return ((this.createOrDeleteOrChange!= null)&&(!this.createOrDeleteOrChange.isEmpty()));
    }

    public void unsetCreateOrDeleteOrChange() {
        this.createOrDeleteOrChange = null;
    }

    /**
     * Gets the value of the updateExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the updateExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUpdateExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getUpdateExtensionGroup() {
        if (updateExtensionGroup == null) {
            updateExtensionGroup = new ArrayList<Object>();
        }
        return this.updateExtensionGroup;
    }

    public boolean isSetUpdateExtensionGroup() {
        return ((this.updateExtensionGroup!= null)&&(!this.updateExtensionGroup.isEmpty()));
    }

    public void unsetUpdateExtensionGroup() {
        this.updateExtensionGroup = null;
    }

    public void setCreateOrDeleteOrChange(List<Object> value) {
        this.createOrDeleteOrChange = value;
    }

    public void setUpdateExtensionGroup(List<Object> value) {
        this.updateExtensionGroup = value;
    }

}
