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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für common_color_or_texture_type complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="common_color_or_texture_type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="color"&gt;
 *           &lt;complexType&gt;
 *             &lt;simpleContent&gt;
 *               &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema&gt;fx_color_common"&gt;
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
 *         &lt;element name="texture"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="texture" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *                 &lt;attribute name="texcoord" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
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
@XmlType(name = "common_color_or_texture_type", propOrder = {
    "color",
    "param",
    "texture"
})
@XmlSeeAlso({
    CommonTransparentType.class
})
public class CommonColorOrTextureType {

    protected CommonColorOrTextureType.Color color;
    protected CommonColorOrTextureType.Param param;
    protected CommonColorOrTextureType.Texture texture;

    /**
     * Ruft den Wert der color-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CommonColorOrTextureType.Color }
     *     
     */
    public CommonColorOrTextureType.Color getColor() {
        return color;
    }

    /**
     * Legt den Wert der color-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CommonColorOrTextureType.Color }
     *     
     */
    public void setColor(CommonColorOrTextureType.Color value) {
        this.color = value;
    }

    public boolean isSetColor() {
        return (this.color!= null);
    }

    /**
     * Ruft den Wert der param-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CommonColorOrTextureType.Param }
     *     
     */
    public CommonColorOrTextureType.Param getParam() {
        return param;
    }

    /**
     * Legt den Wert der param-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CommonColorOrTextureType.Param }
     *     
     */
    public void setParam(CommonColorOrTextureType.Param value) {
        this.param = value;
    }

    public boolean isSetParam() {
        return (this.param!= null);
    }

    /**
     * Ruft den Wert der texture-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CommonColorOrTextureType.Texture }
     *     
     */
    public CommonColorOrTextureType.Texture getTexture() {
        return texture;
    }

    /**
     * Legt den Wert der texture-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CommonColorOrTextureType.Texture }
     *     
     */
    public void setTexture(CommonColorOrTextureType.Texture value) {
        this.texture = value;
    }

    public boolean isSetTexture() {
        return (this.texture!= null);
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;simpleContent&gt;
     *     &lt;extension base="&lt;http://www.collada.org/2005/11/COLLADASchema&gt;fx_color_common"&gt;
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
    public static class Color {

        @XmlValue
        protected List<Double> value;
        @XmlAttribute(name = "sid")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String sid;

        /**
         * Gets the value of the value property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the value property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getValue().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Double }
         * 
         * 
         */
        public List<Double> getValue() {
            if (value == null) {
                value = new ArrayList<Double>();
            }
            return this.value;
        }

        public boolean isSetValue() {
            return ((this.value!= null)&&(!this.value.isEmpty()));
        }

        public void unsetValue() {
            this.value = null;
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

        public void setValue(List<Double> value) {
            this.value = value;
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
     *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="texture" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
     *       &lt;attribute name="texcoord" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "extra"
    })
    public static class Texture {

        protected Extra extra;
        @XmlAttribute(name = "texture", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String texture;
        @XmlAttribute(name = "texcoord", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String texcoord;

        /**
         * Ruft den Wert der extra-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Extra }
         *     
         */
        public Extra getExtra() {
            return extra;
        }

        /**
         * Legt den Wert der extra-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Extra }
         *     
         */
        public void setExtra(Extra value) {
            this.extra = value;
        }

        public boolean isSetExtra() {
            return (this.extra!= null);
        }

        /**
         * Ruft den Wert der texture-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTexture() {
            return texture;
        }

        /**
         * Legt den Wert der texture-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTexture(String value) {
            this.texture = value;
        }

        public boolean isSetTexture() {
            return (this.texture!= null);
        }

        /**
         * Ruft den Wert der texcoord-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTexcoord() {
            return texcoord;
        }

        /**
         * Legt den Wert der texcoord-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTexcoord(String value) {
            this.texcoord = value;
        }

        public boolean isSetTexcoord() {
            return (this.texcoord!= null);
        }

    }

}
