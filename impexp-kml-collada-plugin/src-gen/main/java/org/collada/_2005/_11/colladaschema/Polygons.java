//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlList;
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
 *         &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocalOffset" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}p"/>
 *           &lt;element name="ph">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}p"/>
 *                     &lt;element name="h" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfUInts" maxOccurs="unbounded"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="count" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}uint" />
 *       &lt;attribute name="material" type="{http://www.w3.org/2001/XMLSchema}NCName" />
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
    "pOrPh",
    "extra"
})
@XmlRootElement(name = "polygons")
public class Polygons {

    protected List<InputLocalOffset> input;
    @XmlElementRefs({
        @XmlElementRef(name = "p", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "ph", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> pOrPh;
    protected List<Extra> extra;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String name;
    @XmlAttribute(name = "count", required = true)
    protected BigInteger count;
    @XmlAttribute(name = "material")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String material;

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
     * {@link InputLocalOffset }
     * 
     * 
     */
    public List<InputLocalOffset> getInput() {
        if (input == null) {
            input = new ArrayList<InputLocalOffset>();
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
     * Gets the value of the pOrPh property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pOrPh property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPOrPh().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link List }{@code <}{@link BigInteger }{@code >}{@code >}
     * {@link JAXBElement }{@code <}{@link Polygons.Ph }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getPOrPh() {
        if (pOrPh == null) {
            pOrPh = new ArrayList<JAXBElement<?>>();
        }
        return this.pOrPh;
    }

    public boolean isSetPOrPh() {
        return ((this.pOrPh!= null)&&(!this.pOrPh.isEmpty()));
    }

    public void unsetPOrPh() {
        this.pOrPh = null;
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

    /**
     * Ruft den Wert der count-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCount() {
        return count;
    }

    /**
     * Legt den Wert der count-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCount(BigInteger value) {
        this.count = value;
    }

    public boolean isSetCount() {
        return (this.count!= null);
    }

    /**
     * Ruft den Wert der material-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaterial() {
        return material;
    }

    /**
     * Legt den Wert der material-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaterial(String value) {
        this.material = value;
    }

    public boolean isSetMaterial() {
        return (this.material!= null);
    }

    public void setInput(List<InputLocalOffset> value) {
        this.input = value;
    }

    public void setPOrPh(List<JAXBElement<?>> value) {
        this.pOrPh = value;
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
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}p"/>
     *         &lt;element name="h" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfUInts" maxOccurs="unbounded"/>
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
        "p",
        "h"
    })
    public static class Ph {

        @XmlList
        @XmlElement(required = true)
        protected List<BigInteger> p;
        @XmlElementRef(name = "h", namespace = "http://www.collada.org/2005/11/COLLADASchema", type = JAXBElement.class)
        protected List<JAXBElement<List<BigInteger>>> h;

        /**
         * 
         * 										Theere may only be one p element.
         * 										Gets the value of the p property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the p property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getP().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BigInteger }
         * 
         * 
         */
        public List<BigInteger> getP() {
            if (p == null) {
                p = new ArrayList<BigInteger>();
            }
            return this.p;
        }

        public boolean isSetP() {
            return ((this.p!= null)&&(!this.p.isEmpty()));
        }

        public void unsetP() {
            this.p = null;
        }

        /**
         * Gets the value of the h property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the h property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getH().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link JAXBElement }{@code <}{@link List }{@code <}{@link BigInteger }{@code >}{@code >}
         * 
         * 
         */
        public List<JAXBElement<List<BigInteger>>> getH() {
            if (h == null) {
                h = new ArrayList<JAXBElement<List<BigInteger>>>();
            }
            return this.h;
        }

        public boolean isSetH() {
            return ((this.h!= null)&&(!this.h.isEmpty()));
        }

        public void unsetH() {
            this.h = null;
        }

        public void setP(List<BigInteger> value) {
            this.p = value;
        }

        public void setH(List<JAXBElement<List<BigInteger>>> value) {
            this.h = value;
        }

    }

}
