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
 * <p>Java-Klasse für TimeSpanType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TimeSpanType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractTimePrimitiveType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}begin" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}end" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}TimeSpanSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}TimeSpanObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeSpanType", propOrder = {
    "begin",
    "end",
    "timeSpanSimpleExtensionGroup",
    "timeSpanObjectExtensionGroup"
})
public class TimeSpanType
    extends AbstractTimePrimitiveType
{

    protected String begin;
    protected String end;
    @XmlElement(name = "TimeSpanSimpleExtensionGroup")
    protected List<Object> timeSpanSimpleExtensionGroup;
    @XmlElement(name = "TimeSpanObjectExtensionGroup")
    protected List<AbstractObjectType> timeSpanObjectExtensionGroup;

    /**
     * Ruft den Wert der begin-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBegin() {
        return begin;
    }

    /**
     * Legt den Wert der begin-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBegin(String value) {
        this.begin = value;
    }

    public boolean isSetBegin() {
        return (this.begin!= null);
    }

    /**
     * Ruft den Wert der end-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnd() {
        return end;
    }

    /**
     * Legt den Wert der end-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnd(String value) {
        this.end = value;
    }

    public boolean isSetEnd() {
        return (this.end!= null);
    }

    /**
     * Gets the value of the timeSpanSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the timeSpanSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTimeSpanSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getTimeSpanSimpleExtensionGroup() {
        if (timeSpanSimpleExtensionGroup == null) {
            timeSpanSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.timeSpanSimpleExtensionGroup;
    }

    public boolean isSetTimeSpanSimpleExtensionGroup() {
        return ((this.timeSpanSimpleExtensionGroup!= null)&&(!this.timeSpanSimpleExtensionGroup.isEmpty()));
    }

    public void unsetTimeSpanSimpleExtensionGroup() {
        this.timeSpanSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the timeSpanObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the timeSpanObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTimeSpanObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getTimeSpanObjectExtensionGroup() {
        if (timeSpanObjectExtensionGroup == null) {
            timeSpanObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.timeSpanObjectExtensionGroup;
    }

    public boolean isSetTimeSpanObjectExtensionGroup() {
        return ((this.timeSpanObjectExtensionGroup!= null)&&(!this.timeSpanObjectExtensionGroup.isEmpty()));
    }

    public void unsetTimeSpanObjectExtensionGroup() {
        this.timeSpanObjectExtensionGroup = null;
    }

    public void setTimeSpanSimpleExtensionGroup(List<Object> value) {
        this.timeSpanSimpleExtensionGroup = value;
    }

    public void setTimeSpanObjectExtensionGroup(List<AbstractObjectType> value) {
        this.timeSpanObjectExtensionGroup = value;
    }

}
