//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für fx_stenciltarget_common complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fx_stenciltarget_common">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>NCName">
 *       &lt;attribute name="index" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" default="0" />
 *       &lt;attribute name="face" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_face_enum" default="POSITIVE_X" />
 *       &lt;attribute name="mip" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" default="0" />
 *       &lt;attribute name="slice" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" default="0" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fx_stenciltarget_common", propOrder = {
    "value"
})
public class FxStenciltargetCommon {

    @XmlValue
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String value;
    @XmlAttribute(name = "index")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger index;
    @XmlAttribute(name = "face")
    protected FxSurfaceFaceEnum face;
    @XmlAttribute(name = "mip")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger mip;
    @XmlAttribute(name = "slice")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger slice;

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
     * Ruft den Wert der index-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getIndex() {
        if (index == null) {
            return new BigInteger("0");
        } else {
            return index;
        }
    }

    /**
     * Legt den Wert der index-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setIndex(BigInteger value) {
        this.index = value;
    }

    public boolean isSetIndex() {
        return (this.index!= null);
    }

    /**
     * Ruft den Wert der face-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceFaceEnum }
     *     
     */
    public FxSurfaceFaceEnum getFace() {
        if (face == null) {
            return FxSurfaceFaceEnum.POSITIVE_X;
        } else {
            return face;
        }
    }

    /**
     * Legt den Wert der face-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceFaceEnum }
     *     
     */
    public void setFace(FxSurfaceFaceEnum value) {
        this.face = value;
    }

    public boolean isSetFace() {
        return (this.face!= null);
    }

    /**
     * Ruft den Wert der mip-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMip() {
        if (mip == null) {
            return new BigInteger("0");
        } else {
            return mip;
        }
    }

    /**
     * Legt den Wert der mip-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMip(BigInteger value) {
        this.mip = value;
    }

    public boolean isSetMip() {
        return (this.mip!= null);
    }

    /**
     * Ruft den Wert der slice-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSlice() {
        if (slice == null) {
            return new BigInteger("0");
        } else {
            return slice;
        }
    }

    /**
     * Legt den Wert der slice-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSlice(BigInteger value) {
        this.slice = value;
    }

    public boolean isSetSlice() {
        return (this.slice!= null);
    }

}
