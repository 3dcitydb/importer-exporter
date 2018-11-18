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
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/&gt;
 *         &lt;element name="technique_common"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="ambient"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="directional"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="point"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
 *                             &lt;element name="constant_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
 *                             &lt;element name="linear_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
 *                             &lt;element name="quadratic_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="spot"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
 *                             &lt;element name="constant_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
 *                             &lt;element name="linear_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
 *                             &lt;element name="quadratic_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
 *                             &lt;element name="falloff_angle" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
 *                             &lt;element name="falloff_exponent" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}technique" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "asset",
    "techniqueCommon",
    "technique",
    "extra"
})
@XmlRootElement(name = "light")
public class Light {

    protected Asset asset;
    @XmlElement(name = "technique_common", required = true)
    protected Light.TechniqueCommon techniqueCommon;
    protected List<Technique> technique;
    protected List<Extra> extra;
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
     * 
     * 						The light element may contain an asset element.
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
     * Ruft den Wert der techniqueCommon-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Light.TechniqueCommon }
     *     
     */
    public Light.TechniqueCommon getTechniqueCommon() {
        return techniqueCommon;
    }

    /**
     * Legt den Wert der techniqueCommon-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Light.TechniqueCommon }
     *     
     */
    public void setTechniqueCommon(Light.TechniqueCommon value) {
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
     *       &lt;choice&gt;
     *         &lt;element name="ambient"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="directional"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="point"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
     *                   &lt;element name="constant_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
     *                   &lt;element name="linear_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
     *                   &lt;element name="quadratic_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="spot"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
     *                   &lt;element name="constant_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
     *                   &lt;element name="linear_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
     *                   &lt;element name="quadratic_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
     *                   &lt;element name="falloff_angle" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
     *                   &lt;element name="falloff_exponent" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
     *                 &lt;/sequence&gt;
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
    @XmlType(name = "", propOrder = {
        "ambient",
        "directional",
        "point",
        "spot"
    })
    public static class TechniqueCommon {

        protected Light.TechniqueCommon.Ambient ambient;
        protected Light.TechniqueCommon.Directional directional;
        protected Light.TechniqueCommon.Point point;
        protected Light.TechniqueCommon.Spot spot;

        /**
         * Ruft den Wert der ambient-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Light.TechniqueCommon.Ambient }
         *     
         */
        public Light.TechniqueCommon.Ambient getAmbient() {
            return ambient;
        }

        /**
         * Legt den Wert der ambient-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Light.TechniqueCommon.Ambient }
         *     
         */
        public void setAmbient(Light.TechniqueCommon.Ambient value) {
            this.ambient = value;
        }

        public boolean isSetAmbient() {
            return (this.ambient!= null);
        }

        /**
         * Ruft den Wert der directional-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Light.TechniqueCommon.Directional }
         *     
         */
        public Light.TechniqueCommon.Directional getDirectional() {
            return directional;
        }

        /**
         * Legt den Wert der directional-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Light.TechniqueCommon.Directional }
         *     
         */
        public void setDirectional(Light.TechniqueCommon.Directional value) {
            this.directional = value;
        }

        public boolean isSetDirectional() {
            return (this.directional!= null);
        }

        /**
         * Ruft den Wert der point-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Light.TechniqueCommon.Point }
         *     
         */
        public Light.TechniqueCommon.Point getPoint() {
            return point;
        }

        /**
         * Legt den Wert der point-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Light.TechniqueCommon.Point }
         *     
         */
        public void setPoint(Light.TechniqueCommon.Point value) {
            this.point = value;
        }

        public boolean isSetPoint() {
            return (this.point!= null);
        }

        /**
         * Ruft den Wert der spot-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Light.TechniqueCommon.Spot }
         *     
         */
        public Light.TechniqueCommon.Spot getSpot() {
            return spot;
        }

        /**
         * Legt den Wert der spot-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Light.TechniqueCommon.Spot }
         *     
         */
        public void setSpot(Light.TechniqueCommon.Spot value) {
            this.spot = value;
        }

        public boolean isSetSpot() {
            return (this.spot!= null);
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
         *         &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
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
            "color"
        })
        public static class Ambient {

            @XmlElement(required = true)
            protected TargetableFloat3 color;

            /**
             * Ruft den Wert der color-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat3 }
             *     
             */
            public TargetableFloat3 getColor() {
                return color;
            }

            /**
             * Legt den Wert der color-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat3 }
             *     
             */
            public void setColor(TargetableFloat3 value) {
                this.color = value;
            }

            public boolean isSetColor() {
                return (this.color!= null);
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
         *         &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
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
            "color"
        })
        public static class Directional {

            @XmlElement(required = true)
            protected TargetableFloat3 color;

            /**
             * Ruft den Wert der color-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat3 }
             *     
             */
            public TargetableFloat3 getColor() {
                return color;
            }

            /**
             * Legt den Wert der color-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat3 }
             *     
             */
            public void setColor(TargetableFloat3 value) {
                this.color = value;
            }

            public boolean isSetColor() {
                return (this.color!= null);
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
         *         &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
         *         &lt;element name="constant_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
         *         &lt;element name="linear_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
         *         &lt;element name="quadratic_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
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
            "color",
            "constantAttenuation",
            "linearAttenuation",
            "quadraticAttenuation"
        })
        public static class Point {

            @XmlElement(required = true)
            protected TargetableFloat3 color;
            @XmlElement(name = "constant_attenuation", defaultValue = "1.0")
            protected TargetableFloat constantAttenuation;
            @XmlElement(name = "linear_attenuation", defaultValue = "0.0")
            protected TargetableFloat linearAttenuation;
            @XmlElement(name = "quadratic_attenuation", defaultValue = "0.0")
            protected TargetableFloat quadraticAttenuation;

            /**
             * Ruft den Wert der color-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat3 }
             *     
             */
            public TargetableFloat3 getColor() {
                return color;
            }

            /**
             * Legt den Wert der color-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat3 }
             *     
             */
            public void setColor(TargetableFloat3 value) {
                this.color = value;
            }

            public boolean isSetColor() {
                return (this.color!= null);
            }

            /**
             * Ruft den Wert der constantAttenuation-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat }
             *     
             */
            public TargetableFloat getConstantAttenuation() {
                return constantAttenuation;
            }

            /**
             * Legt den Wert der constantAttenuation-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat }
             *     
             */
            public void setConstantAttenuation(TargetableFloat value) {
                this.constantAttenuation = value;
            }

            public boolean isSetConstantAttenuation() {
                return (this.constantAttenuation!= null);
            }

            /**
             * Ruft den Wert der linearAttenuation-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat }
             *     
             */
            public TargetableFloat getLinearAttenuation() {
                return linearAttenuation;
            }

            /**
             * Legt den Wert der linearAttenuation-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat }
             *     
             */
            public void setLinearAttenuation(TargetableFloat value) {
                this.linearAttenuation = value;
            }

            public boolean isSetLinearAttenuation() {
                return (this.linearAttenuation!= null);
            }

            /**
             * Ruft den Wert der quadraticAttenuation-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat }
             *     
             */
            public TargetableFloat getQuadraticAttenuation() {
                return quadraticAttenuation;
            }

            /**
             * Legt den Wert der quadraticAttenuation-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat }
             *     
             */
            public void setQuadraticAttenuation(TargetableFloat value) {
                this.quadraticAttenuation = value;
            }

            public boolean isSetQuadraticAttenuation() {
                return (this.quadraticAttenuation!= null);
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
         *         &lt;element name="color" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat3"/&gt;
         *         &lt;element name="constant_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
         *         &lt;element name="linear_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
         *         &lt;element name="quadratic_attenuation" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
         *         &lt;element name="falloff_angle" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
         *         &lt;element name="falloff_exponent" type="{http://www.collada.org/2005/11/COLLADASchema}TargetableFloat" minOccurs="0"/&gt;
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
            "color",
            "constantAttenuation",
            "linearAttenuation",
            "quadraticAttenuation",
            "falloffAngle",
            "falloffExponent"
        })
        public static class Spot {

            @XmlElement(required = true)
            protected TargetableFloat3 color;
            @XmlElement(name = "constant_attenuation", defaultValue = "1.0")
            protected TargetableFloat constantAttenuation;
            @XmlElement(name = "linear_attenuation", defaultValue = "0.0")
            protected TargetableFloat linearAttenuation;
            @XmlElement(name = "quadratic_attenuation", defaultValue = "0.0")
            protected TargetableFloat quadraticAttenuation;
            @XmlElement(name = "falloff_angle", defaultValue = "180.0")
            protected TargetableFloat falloffAngle;
            @XmlElement(name = "falloff_exponent", defaultValue = "0.0")
            protected TargetableFloat falloffExponent;

            /**
             * Ruft den Wert der color-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat3 }
             *     
             */
            public TargetableFloat3 getColor() {
                return color;
            }

            /**
             * Legt den Wert der color-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat3 }
             *     
             */
            public void setColor(TargetableFloat3 value) {
                this.color = value;
            }

            public boolean isSetColor() {
                return (this.color!= null);
            }

            /**
             * Ruft den Wert der constantAttenuation-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat }
             *     
             */
            public TargetableFloat getConstantAttenuation() {
                return constantAttenuation;
            }

            /**
             * Legt den Wert der constantAttenuation-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat }
             *     
             */
            public void setConstantAttenuation(TargetableFloat value) {
                this.constantAttenuation = value;
            }

            public boolean isSetConstantAttenuation() {
                return (this.constantAttenuation!= null);
            }

            /**
             * Ruft den Wert der linearAttenuation-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat }
             *     
             */
            public TargetableFloat getLinearAttenuation() {
                return linearAttenuation;
            }

            /**
             * Legt den Wert der linearAttenuation-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat }
             *     
             */
            public void setLinearAttenuation(TargetableFloat value) {
                this.linearAttenuation = value;
            }

            public boolean isSetLinearAttenuation() {
                return (this.linearAttenuation!= null);
            }

            /**
             * Ruft den Wert der quadraticAttenuation-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat }
             *     
             */
            public TargetableFloat getQuadraticAttenuation() {
                return quadraticAttenuation;
            }

            /**
             * Legt den Wert der quadraticAttenuation-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat }
             *     
             */
            public void setQuadraticAttenuation(TargetableFloat value) {
                this.quadraticAttenuation = value;
            }

            public boolean isSetQuadraticAttenuation() {
                return (this.quadraticAttenuation!= null);
            }

            /**
             * Ruft den Wert der falloffAngle-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat }
             *     
             */
            public TargetableFloat getFalloffAngle() {
                return falloffAngle;
            }

            /**
             * Legt den Wert der falloffAngle-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat }
             *     
             */
            public void setFalloffAngle(TargetableFloat value) {
                this.falloffAngle = value;
            }

            public boolean isSetFalloffAngle() {
                return (this.falloffAngle!= null);
            }

            /**
             * Ruft den Wert der falloffExponent-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TargetableFloat }
             *     
             */
            public TargetableFloat getFalloffExponent() {
                return falloffExponent;
            }

            /**
             * Legt den Wert der falloffExponent-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetableFloat }
             *     
             */
            public void setFalloffExponent(TargetableFloat value) {
                this.falloffExponent = value;
            }

            public boolean isSetFalloffExponent() {
                return (this.falloffExponent!= null);
            }

        }

    }

}
