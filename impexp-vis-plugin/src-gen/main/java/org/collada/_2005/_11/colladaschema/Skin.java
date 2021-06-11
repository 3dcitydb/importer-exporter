//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="bind_shape_matrix" type="{http://www.collada.org/2005/11/COLLADASchema}float4x4" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}source" maxOccurs="unbounded" minOccurs="3"/&gt;
 *         &lt;element name="joints"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocal" maxOccurs="unbounded" minOccurs="2"/&gt;
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="vertex_weights"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocalOffset" maxOccurs="unbounded" minOccurs="2"/&gt;
 *                   &lt;element name="vcount" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfUInts" minOccurs="0"/&gt;
 *                   &lt;element name="v" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfInts" minOccurs="0"/&gt;
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="count" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}uint" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="source" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "bindShapeMatrix",
    "source",
    "joints",
    "vertexWeights",
    "extra"
})
@XmlRootElement(name = "skin")
public class Skin {

    @XmlList
    @XmlElement(name = "bind_shape_matrix", type = Double.class)
    protected List<Double> bindShapeMatrix;
    @XmlElement(required = true)
    protected List<Source> source;
    @XmlElement(required = true)
    protected Skin.Joints joints;
    @XmlElement(name = "vertex_weights", required = true)
    protected Skin.VertexWeights vertexWeights;
    protected List<Extra> extra;
    @XmlAttribute(name = "source", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String sourceAttr;

    /**
     * Gets the value of the bindShapeMatrix property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bindShapeMatrix property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBindShapeMatrix().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getBindShapeMatrix() {
        if (bindShapeMatrix == null) {
            bindShapeMatrix = new ArrayList<Double>();
        }
        return this.bindShapeMatrix;
    }

    public boolean isSetBindShapeMatrix() {
        return ((this.bindShapeMatrix!= null)&&(!this.bindShapeMatrix.isEmpty()));
    }

    public void unsetBindShapeMatrix() {
        this.bindShapeMatrix = null;
    }

    /**
     * 
     * 						The skin element must contain at least three source elements.
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
     * Ruft den Wert der joints-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Skin.Joints }
     *     
     */
    public Skin.Joints getJoints() {
        return joints;
    }

    /**
     * Legt den Wert der joints-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Skin.Joints }
     *     
     */
    public void setJoints(Skin.Joints value) {
        this.joints = value;
    }

    public boolean isSetJoints() {
        return (this.joints!= null);
    }

    /**
     * Ruft den Wert der vertexWeights-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Skin.VertexWeights }
     *     
     */
    public Skin.VertexWeights getVertexWeights() {
        return vertexWeights;
    }

    /**
     * Legt den Wert der vertexWeights-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Skin.VertexWeights }
     *     
     */
    public void setVertexWeights(Skin.VertexWeights value) {
        this.vertexWeights = value;
    }

    public boolean isSetVertexWeights() {
        return (this.vertexWeights!= null);
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

    public void setBindShapeMatrix(List<Double> value) {
        this.bindShapeMatrix = value;
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
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocal" maxOccurs="unbounded" minOccurs="2"/&gt;
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "input",
        "extra"
    })
    public static class Joints {

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
     *         &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocalOffset" maxOccurs="unbounded" minOccurs="2"/&gt;
     *         &lt;element name="vcount" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfUInts" minOccurs="0"/&gt;
     *         &lt;element name="v" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfInts" minOccurs="0"/&gt;
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="count" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}uint" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "input",
        "vcount",
        "v",
        "extra"
    })
    public static class VertexWeights {

        @XmlElement(required = true)
        protected List<InputLocalOffset> input;
        @XmlList
        protected List<BigInteger> vcount;
        @XmlList
        @XmlElement(type = Long.class)
        protected List<Long> v;
        protected List<Extra> extra;
        @XmlAttribute(name = "count", required = true)
        protected BigInteger count;

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
         * Gets the value of the vcount property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the vcount property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getVcount().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BigInteger }
         * 
         * 
         */
        public List<BigInteger> getVcount() {
            if (vcount == null) {
                vcount = new ArrayList<BigInteger>();
            }
            return this.vcount;
        }

        public boolean isSetVcount() {
            return ((this.vcount!= null)&&(!this.vcount.isEmpty()));
        }

        public void unsetVcount() {
            this.vcount = null;
        }

        /**
         * Gets the value of the v property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the v property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getV().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Long }
         * 
         * 
         */
        public List<Long> getV() {
            if (v == null) {
                v = new ArrayList<Long>();
            }
            return this.v;
        }

        public boolean isSetV() {
            return ((this.v!= null)&&(!this.v.isEmpty()));
        }

        public void unsetV() {
            this.v = null;
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

        public void setInput(List<InputLocalOffset> value) {
            this.input = value;
        }

        public void setVcount(List<BigInteger> value) {
            this.vcount = value;
        }

        public void setV(List<Long> value) {
            this.v = value;
        }

        public void setExtra(List<Extra> value) {
            this.extra = value;
        }

    }

}
