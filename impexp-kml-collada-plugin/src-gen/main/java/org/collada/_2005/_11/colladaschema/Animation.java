//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;sequence&gt;
 *             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}source" maxOccurs="unbounded"/&gt;
 *             &lt;choice&gt;
 *               &lt;sequence&gt;
 *                 &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}sampler" maxOccurs="unbounded"/&gt;
 *                 &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}channel" maxOccurs="unbounded"/&gt;
 *                 &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}animation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *               &lt;/sequence&gt;
 *               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}animation" maxOccurs="unbounded"/&gt;
 *             &lt;/choice&gt;
 *           &lt;/sequence&gt;
 *           &lt;sequence&gt;
 *             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}sampler" maxOccurs="unbounded"/&gt;
 *             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}channel" maxOccurs="unbounded"/&gt;
 *             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}animation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;/sequence&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}animation" maxOccurs="unbounded"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "animation")
public class Animation {

    @XmlElementRefs({
        @XmlElementRef(name = "asset", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = Asset.class, required = false),
        @XmlElementRef(name = "source", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = Source.class, required = false),
        @XmlElementRef(name = "sampler", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = Sampler.class, required = false),
        @XmlElementRef(name = "channel", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = Channel.class, required = false),
        @XmlElementRef(name = "animation", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = Animation.class, required = false),
        @XmlElementRef(name = "extra", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = Extra.class, required = false)
    })
    protected List<Object> content;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String name;

    /**
     * Ruft das restliche Contentmodell ab. 
     * 
     * <p>
     * Sie rufen diese "catch-all"-Eigenschaft aus folgendem Grund ab: 
     * Der Feldname "Animation" wird von zwei verschiedenen Teilen eines Schemas verwendet. Siehe: 
     * Zeile 2439 von file:/C:/devel/java/impexp/impexp-kml-collada-plugin/resources/jaxb/collada/1.4/collada_schema_1_4.xsd
     * Zeile 2431 von file:/C:/devel/java/impexp/impexp-kml-collada-plugin/resources/jaxb/collada/1.4/collada_schema_1_4.xsd
     * <p>
     * Um diese Eigenschaft zu entfernen, wenden Sie eine Eigenschaftenanpassung für eine
     * der beiden folgenden Deklarationen an, um deren Namen zu ändern: 
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Asset }
     * {@link Source }
     * {@link Sampler }
     * {@link Channel }
     * {@link Animation }
     * {@link Extra }
     * 
     * 
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<Object>();
        }
        return this.content;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    public boolean isSetId() {
        return (this.id!= null);
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    public boolean isSetName() {
        return (this.name!= null);
    }

    public void setContent(List<Object> value) {
        this.content = value;
    }

}
