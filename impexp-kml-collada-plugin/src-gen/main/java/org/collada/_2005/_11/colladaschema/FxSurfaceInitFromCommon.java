//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * 
 * 				This element is an IDREF which specifies the image to use to initialize a specific mip of a 1D or 2D surface, 3D slice, or Cube face.
 * 			
 * 
 * <p>Java-Klasse für fx_surface_init_from_common complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fx_surface_init_from_common">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>IDREF">
 *       &lt;attribute name="mip" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="slice" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="face" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_face_enum" default="POSITIVE_X" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fx_surface_init_from_common", propOrder = {
    "value"
})
public class FxSurfaceInitFromCommon {

    @XmlValue
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object value;
    @XmlAttribute(name = "mip")
    @XmlSchemaType(name = "unsignedInt")
    protected Long mip;
    @XmlAttribute(name = "slice")
    @XmlSchemaType(name = "unsignedInt")
    protected Long slice;
    @XmlAttribute(name = "face")
    protected FxSurfaceFaceEnum face;

    /**
     * Ruft den Wert der value-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getValue() {
        return value;
    }

    /**
     * Legt den Wert der value-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isSetValue() {
        return (this.value!= null);
    }

    /**
     * Ruft den Wert der mip-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getMip() {
        if (mip == null) {
            return  0L;
        } else {
            return mip;
        }
    }

    /**
     * Legt den Wert der mip-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMip(long value) {
        this.mip = value;
    }

    public boolean isSetMip() {
        return (this.mip!= null);
    }

    public void unsetMip() {
        this.mip = null;
    }

    /**
     * Ruft den Wert der slice-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getSlice() {
        if (slice == null) {
            return  0L;
        } else {
            return slice;
        }
    }

    /**
     * Legt den Wert der slice-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSlice(long value) {
        this.slice = value;
    }

    public boolean isSetSlice() {
        return (this.slice!= null);
    }

    public void unsetSlice() {
        this.slice = null;
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

}
