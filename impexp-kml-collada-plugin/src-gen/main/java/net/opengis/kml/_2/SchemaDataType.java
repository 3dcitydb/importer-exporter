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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für SchemaDataType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="SchemaDataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}SimpleData" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}SchemaDataExtension" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="schemaUrl" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SchemaDataType", propOrder = {
    "simpleData",
    "schemaDataExtension"
})
public class SchemaDataType
    extends AbstractObjectType
{

    @XmlElement(name = "SimpleData")
    protected List<SimpleDataType> simpleData;
    @XmlElement(name = "SchemaDataExtension")
    protected List<Object> schemaDataExtension;
    @XmlAttribute(name = "schemaUrl")
    @XmlSchemaType(name = "anyURI")
    protected String schemaUrl;

    /**
     * Gets the value of the simpleData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the simpleData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSimpleData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SimpleDataType }
     * 
     * 
     */
    public List<SimpleDataType> getSimpleData() {
        if (simpleData == null) {
            simpleData = new ArrayList<SimpleDataType>();
        }
        return this.simpleData;
    }

    public boolean isSetSimpleData() {
        return ((this.simpleData!= null)&&(!this.simpleData.isEmpty()));
    }

    public void unsetSimpleData() {
        this.simpleData = null;
    }

    /**
     * Gets the value of the schemaDataExtension property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the schemaDataExtension property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSchemaDataExtension().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getSchemaDataExtension() {
        if (schemaDataExtension == null) {
            schemaDataExtension = new ArrayList<Object>();
        }
        return this.schemaDataExtension;
    }

    public boolean isSetSchemaDataExtension() {
        return ((this.schemaDataExtension!= null)&&(!this.schemaDataExtension.isEmpty()));
    }

    public void unsetSchemaDataExtension() {
        this.schemaDataExtension = null;
    }

    /**
     * Ruft den Wert der schemaUrl-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemaUrl() {
        return schemaUrl;
    }

    /**
     * Legt den Wert der schemaUrl-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemaUrl(String value) {
        this.schemaUrl = value;
    }

    public boolean isSetSchemaUrl() {
        return (this.schemaUrl!= null);
    }

    public void setSimpleData(List<SimpleDataType> value) {
        this.simpleData = value;
    }

    public void setSchemaDataExtension(List<Object> value) {
        this.schemaDataExtension = value;
    }

}
