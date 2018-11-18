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
 * <p>Java-Klasse für TimeStampType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TimeStampType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractTimePrimitiveType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}when" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}TimeStampSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}TimeStampObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeStampType", propOrder = {
    "when",
    "timeStampSimpleExtensionGroup",
    "timeStampObjectExtensionGroup"
})
public class TimeStampType
    extends AbstractTimePrimitiveType
{

    protected String when;
    @XmlElement(name = "TimeStampSimpleExtensionGroup")
    protected List<Object> timeStampSimpleExtensionGroup;
    @XmlElement(name = "TimeStampObjectExtensionGroup")
    protected List<AbstractObjectType> timeStampObjectExtensionGroup;

    /**
     * Ruft den Wert der when-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWhen() {
        return when;
    }

    /**
     * Legt den Wert der when-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWhen(String value) {
        this.when = value;
    }

    public boolean isSetWhen() {
        return (this.when!= null);
    }

    /**
     * Gets the value of the timeStampSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the timeStampSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTimeStampSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getTimeStampSimpleExtensionGroup() {
        if (timeStampSimpleExtensionGroup == null) {
            timeStampSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.timeStampSimpleExtensionGroup;
    }

    public boolean isSetTimeStampSimpleExtensionGroup() {
        return ((this.timeStampSimpleExtensionGroup!= null)&&(!this.timeStampSimpleExtensionGroup.isEmpty()));
    }

    public void unsetTimeStampSimpleExtensionGroup() {
        this.timeStampSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the timeStampObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the timeStampObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTimeStampObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getTimeStampObjectExtensionGroup() {
        if (timeStampObjectExtensionGroup == null) {
            timeStampObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.timeStampObjectExtensionGroup;
    }

    public boolean isSetTimeStampObjectExtensionGroup() {
        return ((this.timeStampObjectExtensionGroup!= null)&&(!this.timeStampObjectExtensionGroup.isEmpty()));
    }

    public void unsetTimeStampObjectExtensionGroup() {
        this.timeStampObjectExtensionGroup = null;
    }

    public void setTimeStampSimpleExtensionGroup(List<Object> value) {
        this.timeStampSimpleExtensionGroup = value;
    }

    public void setTimeStampObjectExtensionGroup(List<AbstractObjectType> value) {
        this.timeStampObjectExtensionGroup = value;
    }

}
