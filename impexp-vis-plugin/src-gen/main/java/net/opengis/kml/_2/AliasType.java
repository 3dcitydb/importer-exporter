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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für AliasType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AliasType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}targetHref" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}sourceHref" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AliasSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AliasObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AliasType", propOrder = {
    "targetHref",
    "sourceHref",
    "aliasSimpleExtensionGroup",
    "aliasObjectExtensionGroup"
})
public class AliasType
    extends AbstractObjectType
{

    @XmlSchemaType(name = "anyURI")
    protected String targetHref;
    @XmlSchemaType(name = "anyURI")
    protected String sourceHref;
    @XmlElement(name = "AliasSimpleExtensionGroup")
    protected List<Object> aliasSimpleExtensionGroup;
    @XmlElement(name = "AliasObjectExtensionGroup")
    protected List<AbstractObjectType> aliasObjectExtensionGroup;

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
     * Ruft den Wert der sourceHref-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceHref() {
        return sourceHref;
    }

    /**
     * Legt den Wert der sourceHref-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceHref(String value) {
        this.sourceHref = value;
    }

    public boolean isSetSourceHref() {
        return (this.sourceHref!= null);
    }

    /**
     * Gets the value of the aliasSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aliasSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAliasSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAliasSimpleExtensionGroup() {
        if (aliasSimpleExtensionGroup == null) {
            aliasSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.aliasSimpleExtensionGroup;
    }

    public boolean isSetAliasSimpleExtensionGroup() {
        return ((this.aliasSimpleExtensionGroup!= null)&&(!this.aliasSimpleExtensionGroup.isEmpty()));
    }

    public void unsetAliasSimpleExtensionGroup() {
        this.aliasSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the aliasObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aliasObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAliasObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getAliasObjectExtensionGroup() {
        if (aliasObjectExtensionGroup == null) {
            aliasObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.aliasObjectExtensionGroup;
    }

    public boolean isSetAliasObjectExtensionGroup() {
        return ((this.aliasObjectExtensionGroup!= null)&&(!this.aliasObjectExtensionGroup.isEmpty()));
    }

    public void unsetAliasObjectExtensionGroup() {
        this.aliasObjectExtensionGroup = null;
    }

    public void setAliasSimpleExtensionGroup(List<Object> value) {
        this.aliasSimpleExtensionGroup = value;
    }

    public void setAliasObjectExtensionGroup(List<AbstractObjectType> value) {
        this.aliasObjectExtensionGroup = value;
    }

}
