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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 			Declares a resource that can be used both as the source for texture samples and as the target of a rendering pass.
 * 			
 * 
 * <p>Java-Klasse für cg_surface_type complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="cg_surface_type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_common"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="generator" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                   &lt;choice maxOccurs="unbounded"&gt;
 *                     &lt;element name="code" type="{http://www.collada.org/2005/11/COLLADASchema}fx_code_profile"/&gt;
 *                     &lt;element name="include" type="{http://www.collada.org/2005/11/COLLADASchema}fx_include_common"/&gt;
 *                   &lt;/choice&gt;
 *                   &lt;element name="name"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NCName"&gt;
 *                           &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="setparam" type="{http://www.collada.org/2005/11/COLLADASchema}cg_setparam_simple" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cg_surface_type", propOrder = {
    "generator"
})
public class CgSurfaceType
    extends FxSurfaceCommon
{

    protected CgSurfaceType.Generator generator;

    /**
     * Ruft den Wert der generator-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CgSurfaceType.Generator }
     *     
     */
    public CgSurfaceType.Generator getGenerator() {
        return generator;
    }

    /**
     * Legt den Wert der generator-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CgSurfaceType.Generator }
     *     
     */
    public void setGenerator(CgSurfaceType.Generator value) {
        this.generator = value;
    }

    public boolean isSetGenerator() {
        return (this.generator!= null);
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
     *         &lt;element name="annotate" type="{http://www.collada.org/2005/11/COLLADASchema}fx_annotate_common" maxOccurs="unbounded" minOccurs="0"/&gt;
     *         &lt;choice maxOccurs="unbounded"&gt;
     *           &lt;element name="code" type="{http://www.collada.org/2005/11/COLLADASchema}fx_code_profile"/&gt;
     *           &lt;element name="include" type="{http://www.collada.org/2005/11/COLLADASchema}fx_include_common"/&gt;
     *         &lt;/choice&gt;
     *         &lt;element name="name"&gt;
     *           &lt;complexType&gt;
     *             &lt;simpleContent&gt;
     *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NCName"&gt;
     *                 &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
     *               &lt;/extension&gt;
     *             &lt;/simpleContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="setparam" type="{http://www.collada.org/2005/11/COLLADASchema}cg_setparam_simple" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "annotate",
        "codeOrInclude",
        "name",
        "setparam"
    })
    public static class Generator {

        protected List<FxAnnotateCommon> annotate;
        @XmlElements({
            @XmlElement(name = "code", type = FxCodeProfile.class),
            @XmlElement(name = "include", type = FxIncludeCommon.class)
        })
        protected List<Object> codeOrInclude;
        @XmlElement(required = true)
        protected CgSurfaceType.Generator.Name name;
        protected List<CgSetparamSimple> setparam;

        /**
         * Gets the value of the annotate property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the annotate property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAnnotate().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FxAnnotateCommon }
         * 
         * 
         */
        public List<FxAnnotateCommon> getAnnotate() {
            if (annotate == null) {
                annotate = new ArrayList<FxAnnotateCommon>();
            }
            return this.annotate;
        }

        public boolean isSetAnnotate() {
            return ((this.annotate!= null)&&(!this.annotate.isEmpty()));
        }

        public void unsetAnnotate() {
            this.annotate = null;
        }

        /**
         * Gets the value of the codeOrInclude property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the codeOrInclude property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCodeOrInclude().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FxCodeProfile }
         * {@link FxIncludeCommon }
         * 
         * 
         */
        public List<Object> getCodeOrInclude() {
            if (codeOrInclude == null) {
                codeOrInclude = new ArrayList<Object>();
            }
            return this.codeOrInclude;
        }

        public boolean isSetCodeOrInclude() {
            return ((this.codeOrInclude!= null)&&(!this.codeOrInclude.isEmpty()));
        }

        public void unsetCodeOrInclude() {
            this.codeOrInclude = null;
        }

        /**
         * Ruft den Wert der name-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link CgSurfaceType.Generator.Name }
         *     
         */
        public CgSurfaceType.Generator.Name getName() {
            return name;
        }

        /**
         * Legt den Wert der name-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link CgSurfaceType.Generator.Name }
         *     
         */
        public void setName(CgSurfaceType.Generator.Name value) {
            this.name = value;
        }

        public boolean isSetName() {
            return (this.name!= null);
        }

        /**
         * Gets the value of the setparam property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the setparam property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSetparam().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CgSetparamSimple }
         * 
         * 
         */
        public List<CgSetparamSimple> getSetparam() {
            if (setparam == null) {
                setparam = new ArrayList<CgSetparamSimple>();
            }
            return this.setparam;
        }

        public boolean isSetSetparam() {
            return ((this.setparam!= null)&&(!this.setparam.isEmpty()));
        }

        public void unsetSetparam() {
            this.setparam = null;
        }

        public void setAnnotate(List<FxAnnotateCommon> value) {
            this.annotate = value;
        }

        public void setCodeOrInclude(List<Object> value) {
            this.codeOrInclude = value;
        }

        public void setSetparam(List<CgSetparamSimple> value) {
            this.setparam = value;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;NCName"&gt;
         *       &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
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
        public static class Name {

            @XmlValue
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlSchemaType(name = "NCName")
            protected String value;
            @XmlAttribute(name = "source")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlSchemaType(name = "NCName")
            protected String source;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
            }

            public boolean isSetValue() {
                return (this.value!= null);
            }

            /**
             * Ruft den Wert der source-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getSource() {
                return source;
            }

            /**
             * Legt den Wert der source-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setSource(String value) {
                this.source = value;
            }

            public boolean isSetSource() {
                return (this.source!= null);
            }

        }

    }

}
