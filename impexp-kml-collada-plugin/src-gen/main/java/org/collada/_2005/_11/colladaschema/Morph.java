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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}source" maxOccurs="unbounded" minOccurs="2"/>
 *         &lt;element name="targets">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocal" maxOccurs="unbounded" minOccurs="2"/>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="method" type="{http://www.collada.org/2005/11/COLLADASchema}MorphMethodType" default="NORMALIZED" />
 *       &lt;attribute name="source" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "source",
    "targets",
    "extra"
})
@XmlRootElement(name = "morph")
public class Morph {

    @XmlElement(required = true)
    protected List<Source> source;
    @XmlElement(required = true)
    protected Morph.Targets targets;
    protected List<Extra> extra;
    @XmlAttribute(name = "method")
    protected MorphMethodType method;
    @XmlAttribute(name = "source", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String sourceAttr;

    /**
     * 
     * 						The morph element must contain at least two source elements.
     * 						Gets the value of the source property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the source property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Source }
     * 
     * 
     */
    public List<Source> getSource() {
        if (source == null) {
            source = new ArrayList<Source>();
        }
        return this.source;
    }

    public boolean isSetSource() {
        return ((this.source!= null)&&(!this.source.isEmpty()));
    }

    public void unsetSource() {
        this.source = null;
    }

    /**
     * Ruft den Wert der targets-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Morph.Targets }
     *     
     */
    public Morph.Targets getTargets() {
        return targets;
    }

    /**
     * Legt den Wert der targets-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Morph.Targets }
     *     
     */
    public void setTargets(Morph.Targets value) {
        this.targets = value;
    }

    public boolean isSetTargets() {
        return (this.targets!= null);
    }

    /**
     * 
     * 						The extra element may appear any number of times.
     * 						Gets the value of the extra property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extra property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtra().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Extra }
     * 
     * 
     */
    public List<Extra> getExtra() {
        if (extra == null) {
            extra = new ArrayList<Extra>();
        }
        return this.extra;
    }

    public boolean isSetExtra() {
        return ((this.extra!= null)&&(!this.extra.isEmpty()));
    }

    public void unsetExtra() {
        this.extra = null;
    }

    /**
     * Ruft den Wert der method-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MorphMethodType }
     *     
     */
    public MorphMethodType getMethod() {
        if (method == null) {
            return MorphMethodType.NORMALIZED;
        } else {
            return method;
        }
    }

    /**
     * Legt den Wert der method-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MorphMethodType }
     *     
     */
    public void setMethod(MorphMethodType value) {
        this.method = value;
    }

    public boolean isSetMethod() {
        return (this.method!= null);
    }

    /**
     * Ruft den Wert der sourceAttr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceAttr() {
        return sourceAttr;
    }

    /**
     * Legt den Wert der sourceAttr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceAttr(String value) {
        this.sourceAttr = value;
    }

    public boolean isSetSourceAttr() {
        return (this.sourceAttr!= null);
    }

    public void setSource(List<Source> value) {
        this.source = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
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
     *         &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocal" maxOccurs="unbounded" minOccurs="2"/>
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
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
        "input",
        "extra"
    })
    public static class Targets {

        @XmlElement(required = true)
        protected List<InputLocal> input;
        protected List<Extra> extra;

        /**
         * Gets the value of the input property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the input property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getInput().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link InputLocal }
         * 
         * 
         */
        public List<InputLocal> getInput() {
            if (input == null) {
                input = new ArrayList<InputLocal>();
            }
            return this.input;
        }

        public boolean isSetInput() {
            return ((this.input!= null)&&(!this.input.isEmpty()));
        }

        public void unsetInput() {
            this.input = null;
        }

        /**
         * 
         * 									The extra element may appear any number of times.
         * 									Gets the value of the extra property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the extra property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getExtra().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Extra }
         * 
         * 
         */
        public List<Extra> getExtra() {
            if (extra == null) {
                extra = new ArrayList<Extra>();
            }
            return this.extra;
        }

        public boolean isSetExtra() {
            return ((this.extra!= null)&&(!this.extra.isEmpty()));
        }

        public void unsetExtra() {
            this.extra = null;
        }

        public void setInput(List<InputLocal> value) {
            this.input = value;
        }

        public void setExtra(List<Extra> value) {
            this.extra = value;
        }

    }

}
