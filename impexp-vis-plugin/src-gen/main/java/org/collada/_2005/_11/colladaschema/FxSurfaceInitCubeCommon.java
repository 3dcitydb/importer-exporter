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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für fx_surface_init_cube_common complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fx_surface_init_cube_common"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="all"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="primary"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence minOccurs="0"&gt;
 *                   &lt;element name="order" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_face_enum" maxOccurs="6" minOccurs="6"/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="face" maxOccurs="6" minOccurs="6"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
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
@XmlType(name = "fx_surface_init_cube_common", propOrder = {
    "all",
    "primary",
    "face"
})
public class FxSurfaceInitCubeCommon {

    protected FxSurfaceInitCubeCommon.All all;
    protected FxSurfaceInitCubeCommon.Primary primary;
    protected List<FxSurfaceInitCubeCommon.Face> face;

    /**
     * Ruft den Wert der all-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceInitCubeCommon.All }
     *     
     */
    public FxSurfaceInitCubeCommon.All getAll() {
        return all;
    }

    /**
     * Legt den Wert der all-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceInitCubeCommon.All }
     *     
     */
    public void setAll(FxSurfaceInitCubeCommon.All value) {
        this.all = value;
    }

    public boolean isSetAll() {
        return (this.all!= null);
    }

    /**
     * Ruft den Wert der primary-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceInitCubeCommon.Primary }
     *     
     */
    public FxSurfaceInitCubeCommon.Primary getPrimary() {
        return primary;
    }

    /**
     * Legt den Wert der primary-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceInitCubeCommon.Primary }
     *     
     */
    public void setPrimary(FxSurfaceInitCubeCommon.Primary value) {
        this.primary = value;
    }

    public boolean isSetPrimary() {
        return (this.primary!= null);
    }

    /**
     * Gets the value of the face property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the face property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FxSurfaceInitCubeCommon.Face }
     * 
     * 
     */
    public List<FxSurfaceInitCubeCommon.Face> getFace() {
        if (face == null) {
            face = new ArrayList<FxSurfaceInitCubeCommon.Face>();
        }
        return this.face;
    }

    public boolean isSetFace() {
        return ((this.face!= null)&&(!this.face.isEmpty()));
    }

    public void unsetFace() {
        this.face = null;
    }

    public void setFace(List<FxSurfaceInitCubeCommon.Face> value) {
        this.face = value;
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
     *       &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class All {

        @XmlAttribute(name = "ref", required = true)
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        protected Object ref;

        /**
         * Ruft den Wert der ref-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getRef() {
            return ref;
        }

        /**
         * Legt den Wert der ref-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setRef(Object value) {
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
     *       &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Face {

        @XmlAttribute(name = "ref", required = true)
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        protected Object ref;

        /**
         * Ruft den Wert der ref-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getRef() {
            return ref;
        }

        /**
         * Legt den Wert der ref-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setRef(Object value) {
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
     *       &lt;sequence minOccurs="0"&gt;
     *         &lt;element name="order" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_face_enum" maxOccurs="6" minOccurs="6"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "order"
    })
    public static class Primary {

        @XmlSchemaType(name = "string")
        protected List<FxSurfaceFaceEnum> order;
        @XmlAttribute(name = "ref", required = true)
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        protected Object ref;

        /**
         * Gets the value of the order property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the order property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOrder().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FxSurfaceFaceEnum }
         * 
         * 
         */
        public List<FxSurfaceFaceEnum> getOrder() {
            if (order == null) {
                order = new ArrayList<FxSurfaceFaceEnum>();
            }
            return this.order;
        }

        public boolean isSetOrder() {
            return ((this.order!= null)&&(!this.order.isEmpty()));
        }

        public void unsetOrder() {
            this.order = null;
        }

        /**
         * Ruft den Wert der ref-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getRef() {
            return ref;
        }

        /**
         * Legt den Wert der ref-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setRef(Object value) {
            this.ref = value;
        }

        public boolean isSetRef() {
            return (this.ref!= null);
        }

        public void setOrder(List<FxSurfaceFaceEnum> value) {
            this.order = value;
        }

    }

}
