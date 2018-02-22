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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
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
 *         &lt;element name="height" type="{http://www.collada.org/2005/11/COLLADASchema}float"/>
 *         &lt;element name="radius1" type="{http://www.collada.org/2005/11/COLLADASchema}float2"/>
 *         &lt;element name="radius2" type="{http://www.collada.org/2005/11/COLLADASchema}float2"/>
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
    "height",
    "radius1",
    "radius2",
    "extra"
})
@XmlRootElement(name = "tapered_capsule")
public class TaperedCapsule {

    protected double height;
    @XmlList
    @XmlElement(type = Double.class)
    protected List<Double> radius1;
    @XmlList
    @XmlElement(type = Double.class)
    protected List<Double> radius2;
    protected List<Extra> extra;

    /**
     * Ruft den Wert der height-Eigenschaft ab.
     * 
     */
    public double getHeight() {
        return height;
    }

    /**
     * Legt den Wert der height-Eigenschaft fest.
     * 
     */
    public void setHeight(double value) {
        this.height = value;
    }

    public boolean isSetHeight() {
        return true;
    }

    /**
     * Gets the value of the radius1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the radius1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRadius1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getRadius1() {
        if (radius1 == null) {
            radius1 = new ArrayList<Double>();
        }
        return this.radius1;
    }

    public boolean isSetRadius1() {
        return ((this.radius1 != null)&&(!this.radius1 .isEmpty()));
    }

    public void unsetRadius1() {
        this.radius1 = null;
    }

    /**
     * Gets the value of the radius2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the radius2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRadius2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getRadius2() {
        if (radius2 == null) {
            radius2 = new ArrayList<Double>();
        }
        return this.radius2;
    }

    public boolean isSetRadius2() {
        return ((this.radius2 != null)&&(!this.radius2 .isEmpty()));
    }

    public void unsetRadius2() {
        this.radius2 = null;
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

    public void setRadius1(List<Double> value) {
        this.radius1 = value;
    }

    public void setRadius2(List<Double> value) {
        this.radius2 = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

}
