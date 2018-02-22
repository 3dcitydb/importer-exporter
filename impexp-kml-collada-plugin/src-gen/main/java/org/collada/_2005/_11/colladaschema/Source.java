//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}IDREF_array"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}Name_array"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}bool_array"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}float_array"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}int_array"/>
 *         &lt;/choice>
 *         &lt;element name="technique_common" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}accessor"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}technique" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "asset",
    "idrefArray",
    "nameArray",
    "boolArray",
    "floatArray",
    "intArray",
    "techniqueCommon",
    "technique"
})
@XmlRootElement(name = "source")
public class Source {

    protected Asset asset;
    @XmlElement(name = "IDREF_array")
    protected IDREFArray idrefArray;
    @XmlElement(name = "Name_array")
    protected NameArray nameArray;
    @XmlElement(name = "bool_array")
    protected BoolArray boolArray;
    @XmlElement(name = "float_array")
    protected FloatArray floatArray;
    @XmlElement(name = "int_array")
    protected IntArray intArray;
    @XmlElement(name = "technique_common")
    protected Source.TechniqueCommon techniqueCommon;
    protected List<Technique> technique;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String name;

    /**
     * 
     * 						The source element may contain an asset element.
     * 						
     * 
     * @return
     *     possible object is
     *     {@link Asset }
     *     
     */
    public Asset getAsset() {
        return asset;
    }

    /**
     * Legt den Wert der asset-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Asset }
     *     
     */
    public void setAsset(Asset value) {
        this.asset = value;
    }

    public boolean isSetAsset() {
        return (this.asset!= null);
    }

    /**
     * 
     * 							The source element may contain an IDREF_array.
     * 							
     * 
     * @return
     *     possible object is
     *     {@link IDREFArray }
     *     
     */
    public IDREFArray getIDREFArray() {
        return idrefArray;
    }

    /**
     * Legt den Wert der idrefArray-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link IDREFArray }
     *     
     */
    public void setIDREFArray(IDREFArray value) {
        this.idrefArray = value;
    }

    public boolean isSetIDREFArray() {
        return (this.idrefArray!= null);
    }

    /**
     * 
     * 							The source element may contain a Name_array.
     * 							
     * 
     * @return
     *     possible object is
     *     {@link NameArray }
     *     
     */
    public NameArray getNameArray() {
        return nameArray;
    }

    /**
     * Legt den Wert der nameArray-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NameArray }
     *     
     */
    public void setNameArray(NameArray value) {
        this.nameArray = value;
    }

    public boolean isSetNameArray() {
        return (this.nameArray!= null);
    }

    /**
     * 
     * 							The source element may contain a bool_array.
     * 							
     * 
     * @return
     *     possible object is
     *     {@link BoolArray }
     *     
     */
    public BoolArray getBoolArray() {
        return boolArray;
    }

    /**
     * Legt den Wert der boolArray-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BoolArray }
     *     
     */
    public void setBoolArray(BoolArray value) {
        this.boolArray = value;
    }

    public boolean isSetBoolArray() {
        return (this.boolArray!= null);
    }

    /**
     * 
     * 							The source element may contain a float_array.
     * 							
     * 
     * @return
     *     possible object is
     *     {@link FloatArray }
     *     
     */
    public FloatArray getFloatArray() {
        return floatArray;
    }

    /**
     * Legt den Wert der floatArray-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FloatArray }
     *     
     */
    public void setFloatArray(FloatArray value) {
        this.floatArray = value;
    }

    public boolean isSetFloatArray() {
        return (this.floatArray!= null);
    }

    /**
     * 
     * 							The source element may contain an int_array.
     * 							
     * 
     * @return
     *     possible object is
     *     {@link IntArray }
     *     
     */
    public IntArray getIntArray() {
        return intArray;
    }

    /**
     * Legt den Wert der intArray-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link IntArray }
     *     
     */
    public void setIntArray(IntArray value) {
        this.intArray = value;
    }

    public boolean isSetIntArray() {
        return (this.intArray!= null);
    }

    /**
     * Ruft den Wert der techniqueCommon-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Source.TechniqueCommon }
     *     
     */
    public Source.TechniqueCommon getTechniqueCommon() {
        return techniqueCommon;
    }

    /**
     * Legt den Wert der techniqueCommon-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Source.TechniqueCommon }
     *     
     */
    public void setTechniqueCommon(Source.TechniqueCommon value) {
        this.techniqueCommon = value;
    }

    public boolean isSetTechniqueCommon() {
        return (this.techniqueCommon!= null);
    }

    /**
     * 
     * 						This element may contain any number of non-common profile techniques.
     * 						Gets the value of the technique property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the technique property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTechnique().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Technique }
     * 
     * 
     */
    public List<Technique> getTechnique() {
        if (technique == null) {
            technique = new ArrayList<Technique>();
        }
        return this.technique;
    }

    public boolean isSetTechnique() {
        return ((this.technique!= null)&&(!this.technique.isEmpty()));
    }

    public void unsetTechnique() {
        this.technique = null;
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

    public void setTechnique(List<Technique> value) {
        this.technique = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}accessor"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "accessor"
    })
    public static class TechniqueCommon {

        @XmlElement(required = true)
        protected Accessor accessor;

        /**
         * 
         * 									The source's technique_common must have one and only one accessor.
         * 									
         * 
         * @return
         *     possible object is
         *     {@link Accessor }
         *     
         */
        public Accessor getAccessor() {
            return accessor;
        }

        /**
         * Legt den Wert der accessor-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Accessor }
         *     
         */
        public void setAccessor(Accessor value) {
            this.accessor = value;
        }

        public boolean isSetAccessor() {
            return (this.accessor!= null);
        }

    }

}
