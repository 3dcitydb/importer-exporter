//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package net.opengis.kml._2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für vec2Type complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="vec2Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="x" type="{http://www.w3.org/2001/XMLSchema}double" default="1.0" /&gt;
 *       &lt;attribute name="y" type="{http://www.w3.org/2001/XMLSchema}double" default="1.0" /&gt;
 *       &lt;attribute name="xunits" type="{http://www.opengis.net/kml/2.2}unitsEnumType" default="fraction" /&gt;
 *       &lt;attribute name="yunits" type="{http://www.opengis.net/kml/2.2}unitsEnumType" default="fraction" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "vec2Type")
public class Vec2Type {

    @XmlAttribute(name = "x")
    protected Double x;
    @XmlAttribute(name = "y")
    protected Double y;
    @XmlAttribute(name = "xunits")
    protected UnitsEnumType xunits;
    @XmlAttribute(name = "yunits")
    protected UnitsEnumType yunits;

    /**
     * Ruft den Wert der x-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getX() {
        if (x == null) {
            return  1.0D;
        } else {
            return x;
        }
    }

    /**
     * Legt den Wert der x-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setX(double value) {
        this.x = value;
    }

    public boolean isSetX() {
        return (this.x!= null);
    }

    public void unsetX() {
        this.x = null;
    }

    /**
     * Ruft den Wert der y-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getY() {
        if (y == null) {
            return  1.0D;
        } else {
            return y;
        }
    }

    /**
     * Legt den Wert der y-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setY(double value) {
        this.y = value;
    }

    public boolean isSetY() {
        return (this.y!= null);
    }

    public void unsetY() {
        this.y = null;
    }

    /**
     * Ruft den Wert der xunits-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UnitsEnumType }
     *     
     */
    public UnitsEnumType getXunits() {
        if (xunits == null) {
            return UnitsEnumType.FRACTION;
        } else {
            return xunits;
        }
    }

    /**
     * Legt den Wert der xunits-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UnitsEnumType }
     *     
     */
    public void setXunits(UnitsEnumType value) {
        this.xunits = value;
    }

    public boolean isSetXunits() {
        return (this.xunits!= null);
    }

    /**
     * Ruft den Wert der yunits-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UnitsEnumType }
     *     
     */
    public UnitsEnumType getYunits() {
        if (yunits == null) {
            return UnitsEnumType.FRACTION;
        } else {
            return yunits;
        }
    }

    /**
     * Legt den Wert der yunits-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UnitsEnumType }
     *     
     */
    public void setYunits(UnitsEnumType value) {
        this.yunits = value;
    }

    public boolean isSetYunits() {
        return (this.yunits!= null);
    }

}
