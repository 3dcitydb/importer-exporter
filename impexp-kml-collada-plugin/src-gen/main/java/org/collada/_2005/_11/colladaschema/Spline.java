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
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}source" maxOccurs="unbounded"/>
 *         &lt;element name="control_vertices">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocal" maxOccurs="unbounded"/>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="closed" type="{http://www.collada.org/2005/11/COLLADASchema}bool" default="false" />
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
    "controlVertices",
    "extra"
})
@XmlRootElement(name = "spline")
public class Spline {

    @XmlElement(required = true)
    protected List<Source> source;
    @XmlElement(name = "control_vertices", required = true)
    protected Spline.ControlVertices controlVertices;
    protected List<Extra> extra;
    @XmlAttribute(name = "closed")
    protected Boolean closed;

    /**
     * 
     * 						The mesh element must contain one or more source elements.
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
     * Ruft den Wert der controlVertices-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Spline.ControlVertices }
     *     
     */
    public Spline.ControlVertices getControlVertices() {
        return controlVertices;
    }

    /**
     * Legt den Wert der controlVertices-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Spline.ControlVertices }
     *     
     */
    public void setControlVertices(Spline.ControlVertices value) {
        this.controlVertices = value;
    }

    public boolean isSetControlVertices() {
        return (this.controlVertices!= null);
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
     * Ruft den Wert der closed-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isClosed() {
        if (closed == null) {
            return false;
        } else {
            return closed;
        }
    }

    /**
     * Legt den Wert der closed-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setClosed(boolean value) {
        this.closed = value;
    }

    public boolean isSetClosed() {
        return (this.closed!= null);
    }

    public void unsetClosed() {
        this.closed = null;
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
     *         &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocal" maxOccurs="unbounded"/>
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
    public static class ControlVertices {

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

        public void setInput(List<InputLocal> value) {
            this.input = value;
        }

        public void setExtra(List<Extra> value) {
            this.extra = value;
        }

    }

}
