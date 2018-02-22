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
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}convex_mesh"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}mesh"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}spline"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "asset",
    "convexMesh",
    "mesh",
    "spline",
    "extra"
})
@XmlRootElement(name = "geometry")
public class Geometry {

    protected Asset asset;
    @XmlElement(name = "convex_mesh")
    protected ConvexMesh convexMesh;
    protected Mesh mesh;
    protected Spline spline;
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
     * 						The geometry element may contain an asset element.
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
     * 
     * 							The geometry element may contain only one mesh or convex_mesh.
     * 							
     * 
     * @return
     *     possible object is
     *     {@link ConvexMesh }
     *     
     */
    public ConvexMesh getConvexMesh() {
        return convexMesh;
    }

    /**
     * Legt den Wert der convexMesh-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConvexMesh }
     *     
     */
    public void setConvexMesh(ConvexMesh value) {
        this.convexMesh = value;
    }

    public boolean isSetConvexMesh() {
        return (this.convexMesh!= null);
    }

    /**
     * 
     * 							The geometry element may contain only one mesh or convex_mesh.
     * 							
     * 
     * @return
     *     possible object is
     *     {@link Mesh }
     *     
     */
    public Mesh getMesh() {
        return mesh;
    }

    /**
     * Legt den Wert der mesh-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Mesh }
     *     
     */
    public void setMesh(Mesh value) {
        this.mesh = value;
    }

    public boolean isSetMesh() {
        return (this.mesh!= null);
    }

    /**
     * Ruft den Wert der spline-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Spline }
     *     
     */
    public Spline getSpline() {
        return spline;
    }

    /**
     * Legt den Wert der spline-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Spline }
     *     
     */
    public void setSpline(Spline value) {
        this.spline = value;
    }

    public boolean isSetSpline() {
        return (this.spline!= null);
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

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

}
