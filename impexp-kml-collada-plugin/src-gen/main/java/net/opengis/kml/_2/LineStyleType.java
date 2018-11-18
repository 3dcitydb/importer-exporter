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
 * <p>Java-Klasse für LineStyleType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="LineStyleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractColorStyleType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}width" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LineStyleSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LineStyleObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LineStyleType", propOrder = {
    "width",
    "lineStyleSimpleExtensionGroup",
    "lineStyleObjectExtensionGroup"
})
public class LineStyleType
    extends AbstractColorStyleType
{

    @XmlElement(defaultValue = "1.0")
    protected Double width;
    @XmlElement(name = "LineStyleSimpleExtensionGroup")
    protected List<Object> lineStyleSimpleExtensionGroup;
    @XmlElement(name = "LineStyleObjectExtensionGroup")
    protected List<AbstractObjectType> lineStyleObjectExtensionGroup;

    /**
     * Ruft den Wert der width-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWidth() {
        return width;
    }

    /**
     * Legt den Wert der width-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWidth(Double value) {
        this.width = value;
    }

    public boolean isSetWidth() {
        return (this.width!= null);
    }

    /**
     * Gets the value of the lineStyleSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lineStyleSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLineStyleSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getLineStyleSimpleExtensionGroup() {
        if (lineStyleSimpleExtensionGroup == null) {
            lineStyleSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.lineStyleSimpleExtensionGroup;
    }

    public boolean isSetLineStyleSimpleExtensionGroup() {
        return ((this.lineStyleSimpleExtensionGroup!= null)&&(!this.lineStyleSimpleExtensionGroup.isEmpty()));
    }

    public void unsetLineStyleSimpleExtensionGroup() {
        this.lineStyleSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the lineStyleObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lineStyleObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLineStyleObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getLineStyleObjectExtensionGroup() {
        if (lineStyleObjectExtensionGroup == null) {
            lineStyleObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.lineStyleObjectExtensionGroup;
    }

    public boolean isSetLineStyleObjectExtensionGroup() {
        return ((this.lineStyleObjectExtensionGroup!= null)&&(!this.lineStyleObjectExtensionGroup.isEmpty()));
    }

    public void unsetLineStyleObjectExtensionGroup() {
        this.lineStyleObjectExtensionGroup = null;
    }

    public void setLineStyleSimpleExtensionGroup(List<Object> value) {
        this.lineStyleSimpleExtensionGroup = value;
    }

    public void setLineStyleObjectExtensionGroup(List<AbstractObjectType> value) {
        this.lineStyleObjectExtensionGroup = value;
    }

}
