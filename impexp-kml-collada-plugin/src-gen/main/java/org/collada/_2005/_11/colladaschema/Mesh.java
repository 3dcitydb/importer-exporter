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
import javax.xml.bind.annotation.XmlElements;
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
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}source" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}vertices"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}lines"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}linestrips"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}polygons"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}polylist"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}triangles"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}trifans"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}tristrips"/>
 *         &lt;/choice>
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
    "source",
    "vertices",
    "linesOrLinestripsOrPolygons",
    "extra"
})
@XmlRootElement(name = "mesh")
public class Mesh {

    @XmlElement(required = true)
    protected List<Source> source;
    @XmlElement(required = true)
    protected Vertices vertices;
    @XmlElements({
        @XmlElement(name = "lines", type = Lines.class),
        @XmlElement(name = "linestrips", type = Linestrips.class),
        @XmlElement(name = "polygons", type = Polygons.class),
        @XmlElement(name = "polylist", type = Polylist.class),
        @XmlElement(name = "triangles", type = Triangles.class),
        @XmlElement(name = "trifans", type = Trifans.class),
        @XmlElement(name = "tristrips", type = Tristrips.class)
    })
    protected List<Object> linesOrLinestripsOrPolygons;
    protected List<Extra> extra;

    /**
     * 
     * 						The mesh element must contain one or more source elements.
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
     * 
     * 						The mesh element must contain one vertices element.
     * 						
     * 
     * @return
     *     possible object is
     *     {@link Vertices }
     *     
     */
    public Vertices getVertices() {
        return vertices;
    }

    /**
     * Legt den Wert der vertices-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Vertices }
     *     
     */
    public void setVertices(Vertices value) {
        this.vertices = value;
    }

    public boolean isSetVertices() {
        return (this.vertices!= null);
    }

    /**
     * Gets the value of the linesOrLinestripsOrPolygons property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linesOrLinestripsOrPolygons property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinesOrLinestripsOrPolygons().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Lines }
     * {@link Linestrips }
     * {@link Polygons }
     * {@link Polylist }
     * {@link Triangles }
     * {@link Trifans }
     * {@link Tristrips }
     * 
     * 
     */
    public List<Object> getLinesOrLinestripsOrPolygons() {
        if (linesOrLinestripsOrPolygons == null) {
            linesOrLinestripsOrPolygons = new ArrayList<Object>();
        }
        return this.linesOrLinestripsOrPolygons;
    }

    public boolean isSetLinesOrLinestripsOrPolygons() {
        return ((this.linesOrLinestripsOrPolygons!= null)&&(!this.linesOrLinestripsOrPolygons.isEmpty()));
    }

    public void unsetLinesOrLinestripsOrPolygons() {
        this.linesOrLinestripsOrPolygons = null;
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

    public void setSource(List<Source> value) {
        this.source = value;
    }

    public void setLinesOrLinestripsOrPolygons(List<Object> value) {
        this.linesOrLinestripsOrPolygons = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

}
