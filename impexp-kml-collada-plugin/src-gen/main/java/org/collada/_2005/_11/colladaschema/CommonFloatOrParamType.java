//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package org.collada._2005._11.colladaschema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für common_float_or_param_type complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="common_float_or_param_type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="float"&gt;
 *           &lt;complexType&gt;
 *             &lt;simpleContent&gt;
 *               &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema&gt;float"&gt;
 *                 &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *               &lt;/extension&gt;
 *             &lt;/simpleContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="param"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "common_float_or_param_type", propOrder = {
    "_float",
    "param"
})
public class CommonFloatOrParamType {

    @XmlElement(name = "float")
    protected CommonFloatOrParamType.Float _float;
    protected CommonFloatOrParamType.Param param;

    /**
     * Ruft den Wert der float-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CommonFloatOrParamType.Float }
     *     
     */
    public CommonFloatOrParamType.Float getFloat() {
        return _float;
    }

    /**
     * Legt den Wert der float-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CommonFloatOrParamType.Float }
     *     
     */
    public void setFloat(CommonFloatOrParamType.Float value) {
        this._float = value;
    }

    public boolean isSetFloat() {
        return (this._float!= null);
    }

    /**
     * Ruft den Wert der param-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CommonFloatOrParamType.Param }
     *     
     */
    public CommonFloatOrParamType.Param getParam() {
        return param;
    }

    /**
     * Legt den Wert der param-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CommonFloatOrParamType.Param }
     *     
     */
    public void setParam(CommonFloatOrParamType.Param value) {
        this.param = value;
    }

    public boolean isSetParam() {
        return (this.param!= null);
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;simpleContent&gt;
     *     &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema&gt;float"&gt;
     *       &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
     *     &lt;/extension&gt;
     *   &lt;/simpleContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Float {

        @XmlValue
        protected double value;
        @XmlAttribute(name = "sid")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String sid;

        /**
         * Ruft den Wert der value-Eigenschaft ab.
         * 
         */
        public double getValue() {
            return value;
        }

        /**
         * Legt den Wert der value-Eigenschaft fest.
         * 
         */
        public void setValue(double value) {
            this.value = value;
        }

        public boolean isSetValue() {
            return true;
        }

        /**
         * Ruft den Wert der sid-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSid() {
            return sid;
        }

        /**
         * Legt den Wert der sid-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSid(String value) {
            this.sid = value;
        }

        public boolean isSetSid() {
            return (this.sid!= null);
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
     *       &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Param {

        @XmlAttribute(name = "ref", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String ref;

        /**
         * Ruft den Wert der ref-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRef() {
            return ref;
        }

        /**
         * Legt den Wert der ref-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRef(String value) {
            this.ref = value;
        }

        public boolean isSetRef() {
            return (this.ref!= null);
        }

    }

}
